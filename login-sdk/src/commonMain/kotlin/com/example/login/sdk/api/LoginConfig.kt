package com.example.login.sdk.api

import com.example.login.sdk.auth.AuthApi
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
)

data class LoginTheme(
    val primaryColor: Long = 0xFF1976D2,
    val showLogo: Boolean = true,
    val privacyPolicyUrl: String = "",
    val termsOfServiceUrl: String = "",
)
