package com.example.login.sdk.api

import com.example.login.sdk.auth.AuthFeatureConfig
import com.example.login.sdk.auth.AuthFlow
import com.example.login.sdk.auth.AuthMethod
import com.example.login.sdk.auth.AuthProviderRegistry
import com.example.login.sdk.auth.AuthResult
import com.example.login.sdk.auth.LoginCredentials
import com.example.login.sdk.internal.LoginRepository
import com.example.login.sdk.internal.resolveAvailableMethods
import com.example.login.sdk.internal.resolveInitialMethod
import com.example.login.sdk.ui.LoginUiContract
import com.example.login.sdk.ui.LoginUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class LoginUiController(
    private val repository: LoginRepository,
    private val registry: AuthProviderRegistry,
    private val callback: LoginCallback,
    private val featureConfig: AuthFeatureConfig,
    private val enabledMethods: Set<AuthMethod>?,
) : LoginUiContract {

    /** UI 状态与宿主回调必须在主线程（Android Toast / Activity、Compose） */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val formMethods: List<AuthMethod> = resolveAvailableMethods(
        registry = registry,
        featureConfig = featureConfig,
        enabledMethods = enabledMethods,
    ).filter { it == AuthMethod.PHONE || it == AuthMethod.EMAIL }

    private val thirdPartyMethods: List<AuthMethod> = resolveAvailableMethods(
        registry = registry,
        featureConfig = featureConfig,
        enabledMethods = enabledMethods,
    ).filter { it == AuthMethod.WECHAT || it == AuthMethod.APPLE_ID || it == AuthMethod.GOOGLE }

    private val _state = MutableStateFlow(
        LoginUiState(
            currentFlow = featureConfig.resolveDefaultFlow(),
            enabledFlows = featureConfig.enabledFlows(),
            availableMethods = formMethods + thirdPartyMethods,
            selectedMethod = resolveInitialMethod(formMethods),
        ),
    )
    override val state: StateFlow<LoginUiState> = _state.asStateFlow()

    override fun selectFlow(flow: AuthFlow) {
        if (flow !in _state.value.enabledFlows) return
        _state.update {
            it.copy(
                currentFlow = flow,
                errorMessage = null,
                codeSent = false,
                selectedMethod = resolveInitialMethod(formMethods, it.selectedMethod),
            )
        }
    }

    override fun selectMethod(method: AuthMethod) {
        if (method != AuthMethod.PHONE && method != AuthMethod.EMAIL) return
        if (method !in formMethods) return
        _state.update { it.copy(selectedMethod = method, errorMessage = null) }
    }

    override fun updatePhone(phone: String) {
        _state.update { it.copy(phone = phone) }
    }

    override fun updateEmail(email: String) {
        _state.update { it.copy(email = email) }
    }

    override fun updatePassword(password: String) {
        _state.update { it.copy(password = password) }
    }

    override fun updateConfirmPassword(password: String) {
        _state.update { it.copy(confirmPassword = password) }
    }

    override fun updateVerificationCode(code: String) {
        _state.update { it.copy(verificationCode = code) }
    }

    override fun sendCode() {
        val method = _state.value.selectedMethod ?: return
        val target = when (method) {
            AuthMethod.PHONE -> _state.value.phone
            AuthMethod.EMAIL -> _state.value.email
            else -> return
        }
        if (target.isBlank()) {
            _state.update { it.copy(errorMessage = if (method == AuthMethod.PHONE) "请输入手机号" else "请输入邮箱") }
            return
        }
        scope.launch {
            _state.update { it.copy(isSendingCode = true, errorMessage = null) }
            val result = withContext(Dispatchers.Default) {
                repository.sendVerificationCode(method, target)
            }
            result
                .onSuccess {
                    _state.update { it.copy(isSendingCode = false, codeSent = true) }
                }
                .onFailure { e ->
                    _state.update {
                        it.copy(isSendingCode = false, errorMessage = e.message ?: "发送失败")
                    }
                }
        }
    }

    override fun submit() {
        when (_state.value.currentFlow) {
            AuthFlow.LOGIN -> performAuth { method, credentials ->
                repository.login(method, credentials)
            }
            AuthFlow.REGISTER -> performAuth { method, credentials ->
                repository.register(method, credentials)
            }
            AuthFlow.FORGOT_PASSWORD -> performAuth { method, credentials ->
                repository.resetPassword(method, credentials)
            }
        }
    }

    override fun login() {
        if (_state.value.currentFlow != AuthFlow.LOGIN) {
            selectFlow(AuthFlow.LOGIN)
        }
        submit()
    }

    override fun loginWithThirdParty(method: AuthMethod) {
        if (method !in thirdPartyMethods) return
        val flow = _state.value.currentFlow
        if (flow != AuthFlow.LOGIN && flow != AuthFlow.REGISTER) return

        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null, selectedMethod = method) }
            val result = withContext(Dispatchers.Default) {
                repository.login(method, LoginCredentials.ThirdParty(method))
            }
            handleAuthResult(method, result)
        }
    }

    override fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun performAuth(
        block: suspend (AuthMethod, LoginCredentials) -> AuthResult,
    ) {
        val method = _state.value.selectedMethod ?: run {
            _state.update { it.copy(errorMessage = "请选择登录方式") }
            return
        }
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val credentials = buildCredentials(method) ?: run {
                _state.update { it.copy(isLoading = false) }
                return@launch
            }
            val result = withContext(Dispatchers.Default) {
                block(method, credentials)
            }
            handleAuthResult(method, result)
        }
    }

    private suspend fun handleAuthResult(method: AuthMethod, result: AuthResult) {
        when (result) {
            is AuthResult.Success -> {
                _state.update { it.copy(isLoading = false) }
                callback.onSuccess(result.session)
            }
            is AuthResult.Failure -> {
                _state.update { it.copy(isLoading = false, errorMessage = result.message) }
                callback.onError(mapError(method, result))
            }
            AuthResult.Cancelled -> {
                _state.update { it.copy(isLoading = false) }
                callback.onCancel()
            }
        }
    }

    private fun buildCredentials(method: AuthMethod): LoginCredentials? {
        val s = _state.value
        return when (s.currentFlow) {
            AuthFlow.LOGIN -> when (method) {
                AuthMethod.PHONE -> {
                    if (s.phone.isBlank() || s.verificationCode.isBlank()) {
                        _state.update { it.copy(errorMessage = "请填写手机号和验证码") }
                        null
                    } else {
                        LoginCredentials.PhoneOtp(s.phone, s.verificationCode)
                    }
                }
                AuthMethod.EMAIL -> {
                    if (s.email.isBlank() || s.password.isBlank()) {
                        _state.update { it.copy(errorMessage = "请填写邮箱和密码") }
                        null
                    } else {
                        LoginCredentials.EmailPassword(s.email, s.password)
                    }
                }
                else -> null
            }
            AuthFlow.REGISTER -> when (method) {
                AuthMethod.PHONE -> buildRegisterPhoneCredentials(s)
                AuthMethod.EMAIL -> buildRegisterEmailCredentials(s)
                else -> null
            }
            AuthFlow.FORGOT_PASSWORD -> when (method) {
                AuthMethod.PHONE -> buildResetPhoneCredentials(s)
                AuthMethod.EMAIL -> buildResetEmailCredentials(s)
                else -> null
            }
        }
    }

    private fun buildRegisterPhoneCredentials(s: LoginUiState): LoginCredentials? {
        if (s.phone.isBlank() || s.verificationCode.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(errorMessage = "请填写完整注册信息") }
            return null
        }
        if (s.password != s.confirmPassword) {
            _state.update { it.copy(errorMessage = "两次密码不一致") }
            return null
        }
        return LoginCredentials.PhoneRegister(s.phone, s.verificationCode, s.password)
    }

    private fun buildRegisterEmailCredentials(s: LoginUiState): LoginCredentials? {
        if (s.email.isBlank() || s.verificationCode.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(errorMessage = "请填写完整注册信息") }
            return null
        }
        if (s.password != s.confirmPassword) {
            _state.update { it.copy(errorMessage = "两次密码不一致") }
            return null
        }
        return LoginCredentials.EmailRegister(s.email, s.password, s.verificationCode)
    }

    private fun buildResetPhoneCredentials(s: LoginUiState): LoginCredentials? {
        if (s.phone.isBlank() || s.verificationCode.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(errorMessage = "请填写手机号、验证码和新密码") }
            return null
        }
        return LoginCredentials.PhoneReset(s.phone, s.verificationCode, s.password)
    }

    private fun buildResetEmailCredentials(s: LoginUiState): LoginCredentials? {
        if (s.email.isBlank() || s.verificationCode.isBlank() || s.password.isBlank()) {
            _state.update { it.copy(errorMessage = "请填写邮箱、验证码和新密码") }
            return null
        }
        return LoginCredentials.EmailReset(s.email, s.verificationCode, s.password)
    }

    private fun mapError(method: AuthMethod, result: AuthResult.Failure): LoginError = when (result.code) {
        "PROVIDER_NOT_AVAILABLE" -> LoginError.ProviderNotAvailable(method)
        "INVALID_CREDENTIALS" -> LoginError.InvalidCredentials(result.message)
        "NETWORK" -> LoginError.Network(result.message)
        "THIRD_PARTY" -> LoginError.ThirdParty(method, result.message)
        else -> LoginError.Unknown(result.message)
    }
}
