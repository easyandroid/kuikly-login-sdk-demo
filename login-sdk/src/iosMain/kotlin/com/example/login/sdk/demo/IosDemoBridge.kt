package com.example.login.sdk.demo

import com.example.login.sdk.api.LoginCallback
import com.example.login.sdk.api.LoginConfig
import com.example.login.sdk.api.LoginError
import com.example.login.sdk.api.LoginSDK
import com.example.login.sdk.api.LoginUiOptions
import com.example.login.sdk.api.installIosLoginUi
import com.example.login.sdk.auth.LoginSession
import com.example.login.sdk.provider.ios.createIosAuthProviders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import platform.UIKit.UIViewController

/**
 * iOS Demo 宿主桥接 —— 供 Swift 侧调用，避免在 Swift 实现 [LoginCallback]。
 */
object IosDemoBridge {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    fun initDemo() {
        LoginSDK.init(
            LoginConfig(
                appId = "demo-app-id",
                providers = createIosAuthProviders(),
                uiOptions = LoginUiOptions(
                    prefillDemoCredentials = true,
                    showDemoHint = true,
                ),
            ),
        )
    }

    fun installLoginUi(rootViewController: UIViewController) {
        LoginSDK.installIosLoginUi { rootViewController }
    }

    fun isLoggedIn(): Boolean = LoginSDK.isLoggedIn()

    fun currentUserSummary(): String {
        val session = LoginSDK.currentSession() ?: return ""
        val name = session.profile?.nickname ?: session.userId
        return "用户: $name\n方式: ${session.loginMethod.name}"
    }

    fun launchLoginDemo(onResult: (String) -> Unit) {
        LoginSDK.launchLogin(
            object : LoginCallback {
                override fun onSuccess(session: LoginSession) {
                    val name = session.profile?.nickname ?: session.userId
                    onResult("登录成功: $name")
                }

                override fun onError(error: LoginError) {
                    onResult("登录失败: ${error.message}")
                }

                override fun onCancel() {
                    onResult("已取消登录")
                }
            },
        )
    }

    fun logoutDemo(onDone: () -> Unit) {
        scope.launch {
            LoginSDK.logout()
            onDone()
        }
    }
}
