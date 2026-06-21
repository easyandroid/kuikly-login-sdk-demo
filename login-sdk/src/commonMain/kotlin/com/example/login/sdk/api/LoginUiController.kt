package com.example.login.sdk.api

import com.example.login.sdk.auth.AuthMethod
import com.example.login.sdk.auth.AuthProviderRegistry
import com.example.login.sdk.auth.AuthResult
import com.example.login.sdk.auth.LoginCredentials
import com.example.login.sdk.internal.LoginRepository
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

internal class LoginUiController(
    private val repository: LoginRepository,
    private val registry: AuthProviderRegistry,
    private val callback: LoginCallback,
) : LoginUiContract {

    private val scope = CoroutineScope(SupervisorJob())
    private val _state = MutableStateFlow(
        LoginUiState(
            availableMethods = registry.availableMethods(),
        )
    )
    override val state: StateFlow<LoginUiState> = _state.asStateFlow()

    override fun selectMethod(method: AuthMethod) {
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
        scope.launch {
            _state.update { it.copy(isSendingCode = true, errorMessage = null) }
            repository.sendVerificationCode(method, target)
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

    override fun login() {
        val method = _state.value.selectedMethod ?: return
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            val credentials = buildCredentials(method) ?: run {
                _state.update { it.copy(isLoading = false, errorMessage = "请填写完整信息") }
                return@launch
            }
            when (val result = repository.login(method, credentials)) {
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
    }

    override fun loginWithThirdParty(method: AuthMethod) {
        selectMethod(method)
        scope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.login(method, LoginCredentials.ThirdParty(method))) {
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
    }

    override fun dismissError() {
        _state.update { it.copy(errorMessage = null) }
    }

    private fun buildCredentials(method: AuthMethod): LoginCredentials? = when (method) {
        AuthMethod.PHONE -> {
            val s = _state.value
            if (s.phone.isBlank() || s.verificationCode.isBlank()) null
            else LoginCredentials.PhoneOtp(s.phone, s.verificationCode)
        }
        AuthMethod.EMAIL -> {
            val s = _state.value
            if (s.email.isBlank() || s.password.isBlank()) null
            else LoginCredentials.EmailPassword(s.email, s.password)
        }
        AuthMethod.WECHAT, AuthMethod.APPLE_ID, AuthMethod.GOOGLE ->
            LoginCredentials.ThirdParty(method)
    }

    private fun mapError(method: AuthMethod, result: AuthResult.Failure): LoginError = when (result.code) {
        "PROVIDER_NOT_AVAILABLE" -> LoginError.ProviderNotAvailable(method)
        "INVALID_CREDENTIALS" -> LoginError.InvalidCredentials(result.message)
        "NETWORK" -> LoginError.Network(result.message)
        "THIRD_PARTY" -> LoginError.ThirdParty(method, result.message)
        else -> LoginError.Unknown(result.message)
    }
}
