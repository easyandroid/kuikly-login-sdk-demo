package com.example.login.sdk.ui

import com.example.login.sdk.auth.AuthFlow
import com.example.login.sdk.auth.AuthMethod
import kotlinx.coroutines.flow.StateFlow

/**
 * 登录 UI 状态 —— Kuikly Page / Compose / 原生 UI 共用
 */
data class LoginUiState(
    val currentFlow: AuthFlow = AuthFlow.LOGIN,
    val enabledFlows: List<AuthFlow> = listOf(AuthFlow.LOGIN),
    val availableMethods: List<AuthMethod> = emptyList(),
    val selectedMethod: AuthMethod? = null,
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val verificationCode: String = "",
    val isLoading: Boolean = false,
    val isSendingCode: Boolean = false,
    val codeSent: Boolean = false,
    val errorMessage: String? = null,
)

/**
 * UI 控制器契约 —— View 层只依赖此接口，不依赖 Repository
 */
interface LoginUiContract {
    val state: StateFlow<LoginUiState>
    fun selectFlow(flow: AuthFlow)
    fun selectMethod(method: AuthMethod)
    fun updatePhone(phone: String)
    fun updateEmail(email: String)
    fun updatePassword(password: String)
    fun updateConfirmPassword(password: String)
    fun updateVerificationCode(code: String)
    fun sendCode()
    /** 按当前流程提交（登录 / 注册 / 重置密码） */
    fun submit()
    fun login()
    fun loginWithThirdParty(method: AuthMethod)
    fun dismissError()
}

/**
 * 登录方式 UI 元数据（用于渲染按钮/icon）
 */
data class AuthMethodUiMeta(
    val method: AuthMethod,
    val label: String,
    val iconName: String,
    val requiresForm: Boolean,
)

fun authMethodUiMeta(method: AuthMethod): AuthMethodUiMeta = when (method) {
    AuthMethod.PHONE -> AuthMethodUiMeta(method, "手机号", "phone", requiresForm = true)
    AuthMethod.EMAIL -> AuthMethodUiMeta(method, "邮箱", "email", requiresForm = true)
    AuthMethod.WECHAT -> AuthMethodUiMeta(method, "微信登录", "wechat", requiresForm = false)
    AuthMethod.APPLE_ID -> AuthMethodUiMeta(method, "Apple 登录", "apple", requiresForm = false)
    AuthMethod.GOOGLE -> AuthMethodUiMeta(method, "Google 登录", "google", requiresForm = false)
}
