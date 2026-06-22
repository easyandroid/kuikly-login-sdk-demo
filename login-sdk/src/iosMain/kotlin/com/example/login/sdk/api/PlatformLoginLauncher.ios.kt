package com.example.login.sdk.api

import com.example.login.sdk.internal.LoginCallbackHolder
import com.example.login.sdk.ui.ios.presentLoginViewController
import platform.UIKit.UIViewController

internal actual object PlatformLoginLauncher {
    actual fun launch() {
        val callback = LoginCallbackHolder.peek()
            ?: error("LoginCallback not set. Call LoginSDK.launchLogin(callback) first.")
        val presenter = LoginIosRuntime.loginPresenter
            ?: LoginIosRuntime.defaultPresenter
            ?: error(
                "iOS: call LoginSDK.installIosLoginUi { ... } before launchLogin(). " +
                    "See docs/INTEGRATION.md",
            )
        presenter(callback)
    }
}

object LoginIosRuntime {
    var loginPresenter: ((LoginCallback) -> Unit)? = null
    internal var defaultPresenter: ((LoginCallback) -> Unit)? = null
    var rootViewControllerProvider: (() -> UIViewController)? = null
}

/**
 * 注册 iOS 登录页展示逻辑（自定义容器时使用）。
 */
fun LoginSDK.setIosLoginPresenter(presenter: (LoginCallback) -> Unit) {
    LoginIosRuntime.loginPresenter = presenter
}

/**
 * 一键接入 SDK 内置跨端登录 UI（与 Android [LoginScreen] 相同）。
 *
 * ```kotlin
 * LoginSDK.installIosLoginUi {
 *     // 返回当前可用于 present 的 UIViewController（如 window.rootViewController）
 *     window.rootViewController!!
 * }
 * ```
 */
fun LoginSDK.installIosLoginUi(rootViewController: () -> UIViewController) {
    LoginIosRuntime.rootViewControllerProvider = rootViewController
    LoginIosRuntime.defaultPresenter = { callback ->
        val root = rootViewController()
        presentLoginViewController(
            hostCallback = callback,
            config = config(),
            from = root,
        )
    }
}

/**
 * 创建登录 ViewController，供宿主嵌入 Navigation / Tab。
 */
fun LoginSDK.createLoginViewController(
    callback: LoginCallback,
    onDismiss: () -> Unit = {},
): UIViewController = com.example.login.sdk.ui.ios.LoginComposeScreen.createViewController(
    hostCallback = callback,
    config = config(),
    onDismiss = onDismiss,
)
