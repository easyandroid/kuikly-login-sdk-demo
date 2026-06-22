package com.example.login.sdk.api

import com.example.login.sdk.auth.AuthApi
import com.example.login.sdk.auth.AuthFeatureConfig
import com.example.login.sdk.auth.AuthProvider
import com.example.login.sdk.auth.TokenStore
import com.example.login.sdk.internal.MockAuthApi

/**
 * SDK 初始化配置
 */
data class LoginConfig(
    /** 应用标识，用于后端区分租户 */
    val appId: String,
    /** 后端鉴权 API（可替换为真实 Retrofit/Ktor 实现） */
    val authApi: AuthApi = MockAuthApi(),
    /** 登录 Provider 列表（平台相关实现由宿主注入） */
    val providers: List<AuthProvider> = emptyList(),
    /** Token 持久化（默认内存，生产环境应注入 SecureTokenStore） */
    val tokenStore: TokenStore? = null,
    /** 启用的登录方式白名单，null 表示全部可用 */
    val enabledMethods: Set<com.example.login.sdk.auth.AuthMethod>? = null,
    /** UI 主题配置 */
    val theme: LoginTheme = LoginTheme(),
    /** 登录页 UI 行为（多 App 接入时可按宿主覆盖） */
    val uiOptions: LoginUiOptions = LoginUiOptions(),
    /** 账号能力开关：登录 / 注册 / 找回密码、手机 / 邮箱 / 第三方等 */
    val featureConfig: AuthFeatureConfig = AuthFeatureConfig(),
)

data class LoginTheme(
    val primaryColor: Long = 0xFF1976D2,
    val showLogo: Boolean = true,
    val privacyPolicyUrl: String = "",
    val termsOfServiceUrl: String = "",
)

data class LoginUiOptions(
    /** Demo 预填测试账号（生产环境保持 false） */
    val prefillDemoCredentials: Boolean = false,
    /** 是否展示 Demo 提示文案 */
    val showDemoHint: Boolean = false,
)
