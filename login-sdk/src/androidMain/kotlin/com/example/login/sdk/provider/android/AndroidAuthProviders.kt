package com.example.login.sdk.provider.android

import com.example.login.sdk.auth.AuthMethod
import com.example.login.sdk.auth.AuthProvider
import com.example.login.sdk.auth.LoginCredentials
import com.example.login.sdk.auth.ThirdPartyAuthPayload
import com.example.login.sdk.internal.ThirdPartyCancelledException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 第三方授权启动器 —— 由宿主 Activity 注入，解耦 SDK 与 Activity 生命周期。
 *
 * 生产环境：在此回调中拉起微信 SDK / Google Sign-In / Sign in with Apple。
 */
fun interface ThirdPartyAuthLauncher {
    suspend fun launch(method: AuthMethod): Result<ThirdPartyAuthPayload>
}

/**
 * 微信登录 Provider（Android）
 *
 * 生产接入：
 * - 依赖 com.tencent.mm.opensdk:wechat-sdk-android
 * - 在 WXEntryActivity 接收回调
 * - launcher 内调用 SendAuth.Req
 */
class WeChatAuthProvider(
    private val launcher: ThirdPartyAuthLauncher,
    private val installedChecker: () -> Boolean = { true },
) : AuthProvider {
    override val method = AuthMethod.WECHAT
    override fun isAvailable() = installedChecker()

    override suspend fun authenticate(credentials: LoginCredentials): Result<ThirdPartyAuthPayload> {
        return launcher.launch(method).map { payload ->
            payload.copy(method = AuthMethod.WECHAT)
        }
    }
}

/**
 * Apple ID 登录 Provider（Android 通过 Web/OAuth 或后端中转）
 *
 * 生产接入：
 * - iOS: AuthenticationServices (ASAuthorizationController)
 * - Android: Apple JS SDK / 后端 OAuth redirect
 */
class AppleAuthProvider(
    private val launcher: ThirdPartyAuthLauncher,
) : AuthProvider {
    override val method = AuthMethod.APPLE_ID

    override fun isAvailable(): Boolean {
        // Android 可通过 Web OAuth 支持；iOS 原生支持
        return true
    }

    override suspend fun authenticate(credentials: LoginCredentials): Result<ThirdPartyAuthPayload> {
        return launcher.launch(method).map { payload ->
            payload.copy(method = AuthMethod.APPLE_ID)
        }
    }
}

/**
 * Google 登录 Provider（Android）
 *
 * 生产接入：
 * - 依赖 com.google.android.gms:play-services-auth
 * - Credential Manager API (Android 14+)
 */
class GoogleAuthProvider(
    private val launcher: ThirdPartyAuthLauncher,
) : AuthProvider {
    override val method = AuthMethod.GOOGLE
    override fun isAvailable() = true

    override suspend fun authenticate(credentials: LoginCredentials): Result<ThirdPartyAuthPayload> {
        return launcher.launch(method).map { payload ->
            payload.copy(method = AuthMethod.GOOGLE)
        }
    }
}

/**
 * Demo 用 Mock Launcher —— 模拟第三方授权流程
 */
class DemoThirdPartyAuthLauncher : ThirdPartyAuthLauncher {
    override suspend fun launch(method: AuthMethod): Result<ThirdPartyAuthPayload> =
        suspendCancellableCoroutine { cont ->
            // Demo 直接返回模拟 payload；生产环境在此拉起 SDK 并在回调中 resume
            val payload = when (method) {
                AuthMethod.WECHAT -> ThirdPartyAuthPayload(
                    method = method,
                    authorizationCode = "wx_demo_code_${System.currentTimeMillis()}",
                    openId = "wx_openid_demo",
                    displayName = "微信用户",
                )
                AuthMethod.APPLE_ID -> ThirdPartyAuthPayload(
                    method = method,
                    idToken = "apple_id_token_demo",
                    email = "user@privaterelay.appleid.com",
                    displayName = "Apple 用户",
                )
                AuthMethod.GOOGLE -> ThirdPartyAuthPayload(
                    method = method,
                    idToken = "google_id_token_demo",
                    accessToken = "google_access_token_demo",
                    email = "user@gmail.com",
                    displayName = "Google 用户",
                )
                else -> return@suspendCancellableCoroutine cont.resume(
                    Result.failure(IllegalArgumentException("Not a third-party method"))
                )
            }
            cont.resume(Result.success(payload))
        }
}

/**
 * 一键注册 Android 平台全部 Provider
 */
fun createAndroidAuthProviders(
    launcher: ThirdPartyAuthLauncher = DemoThirdPartyAuthLauncher(),
    isWeChatInstalled: () -> Boolean = { true },
): List<AuthProvider> = listOf(
    com.example.login.sdk.provider.PhoneAuthProvider(),
    com.example.login.sdk.provider.EmailAuthProvider(),
    WeChatAuthProvider(launcher, isWeChatInstalled),
    AppleAuthProvider(launcher),
    GoogleAuthProvider(launcher),
)
