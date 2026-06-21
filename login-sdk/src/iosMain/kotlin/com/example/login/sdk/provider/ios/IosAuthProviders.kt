package com.example.login.sdk.provider.ios

import com.example.login.sdk.auth.AuthMethod
import com.example.login.sdk.auth.AuthProvider
import com.example.login.sdk.auth.LoginCredentials
import com.example.login.sdk.auth.ThirdPartyAuthPayload
import com.example.login.sdk.provider.EmailAuthProvider
import com.example.login.sdk.provider.PhoneAuthProvider

/**
 * iOS 平台 Provider 工厂（预演占位）。
 *
 * 正式接入 Kuikly iOS 时：
 * - WeChat: 接入 WechatOpenSDK
 * - Apple: ASAuthorizationAppleIDProvider
 * - Google: GoogleSignIn SDK
 */
fun createIosAuthProviders(): List<AuthProvider> = listOf(
    PhoneAuthProvider(),
    EmailAuthProvider(),
    StubThirdPartyProvider(AuthMethod.WECHAT),
    StubThirdPartyProvider(AuthMethod.APPLE_ID),
    StubThirdPartyProvider(AuthMethod.GOOGLE),
)

private class StubThirdPartyProvider(override val method: AuthMethod) : AuthProvider {
    override fun isAvailable() = method == AuthMethod.APPLE_ID // iOS 原生支持 Apple

    override suspend fun authenticate(credentials: LoginCredentials): Result<ThirdPartyAuthPayload> {
        return Result.success(
            ThirdPartyAuthPayload(
                method = method,
                idToken = "${method.name.lowercase()}_ios_stub_token",
            )
        )
    }
}
