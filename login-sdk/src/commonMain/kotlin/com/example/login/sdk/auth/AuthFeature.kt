package com.example.login.sdk.auth

/**
 * 账号页业务流程（登录 / 注册 / 找回密码）。
 */
enum class AuthFlow {
    LOGIN,
    REGISTER,
    FORGOT_PASSWORD,
}

/**
 * 账号 SDK 能力开关 —— 控制 UI 展示与可用流程，由宿主在 [com.example.login.sdk.api.LoginConfig] 配置。
 */
data class AuthFeatureConfig(
    /** 显示「登录」流程 */
    val showLogin: Boolean = true,
    /** 显示「注册」流程 */
    val showRegister: Boolean = true,
    /** 显示「忘记密码 / 重置密码」流程 */
    val showForgotPassword: Boolean = true,
    /** 手机号 + 验证码 */
    val showPhoneAuth: Boolean = true,
    /** 邮箱 + 密码 */
    val showEmailAuth: Boolean = true,
    /** 微信 / Apple / Google（仅登录、注册流程展示） */
    val showThirdPartyAuth: Boolean = true,
    /** 打开页面时的默认流程 */
    val defaultFlow: AuthFlow = AuthFlow.LOGIN,
) {
    fun enabledFlows(): List<AuthFlow> = buildList {
        if (showLogin) add(AuthFlow.LOGIN)
        if (showRegister) add(AuthFlow.REGISTER)
        if (showForgotPassword) add(AuthFlow.FORGOT_PASSWORD)
    }

    fun resolveDefaultFlow(): AuthFlow {
        val flows = enabledFlows()
        return when {
            flows.isEmpty() -> AuthFlow.LOGIN
            defaultFlow in flows -> defaultFlow
            else -> flows.first()
        }
    }
}
