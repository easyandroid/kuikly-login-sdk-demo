package com.example.login.sdk.ui.ios

import androidx.compose.ui.window.ComposeUIViewController
import com.example.login.sdk.api.LoginCallback
import com.example.login.sdk.api.LoginConfig
import com.example.login.sdk.api.LoginError
import com.example.login.sdk.api.LoginSDK
import com.example.login.sdk.api.LoginUiOptions
import com.example.login.sdk.auth.LoginSession
import com.example.login.sdk.ui.LoginScreen
import com.example.login.sdk.ui.LoginSdkTheme
import platform.UIKit.UIAlertAction
import platform.UIKit.UIAlertActionStyleDefault
import platform.UIKit.UIAlertController
import platform.UIKit.UIAlertControllerStyleAlert
import platform.UIKit.UIApplication
import platform.UIKit.UIModalPresentationFullScreen
import platform.UIKit.UIViewController

/**
 * iOS 登录页容器 —— 与 Android 共用 [LoginScreen]（Compose Multiplatform）。
 */
object LoginComposeScreen {

    /**
     * 创建可 present 的登录 ViewController。
     */
    fun createViewController(
        hostCallback: LoginCallback,
        config: LoginConfig,
        onDismiss: () -> Unit,
    ): UIViewController {
        val activityCallback = object : LoginCallback {
            override fun onSuccess(session: LoginSession) {
                hostCallback.onSuccess(session)
                onDismiss()
            }

            override fun onError(error: LoginError) {
                hostCallback.onError(error)
            }

            override fun onCancel() {
                hostCallback.onCancel()
                onDismiss()
            }
        }
        val controller = LoginSDK.createLoginController(activityCallback)
        return ComposeUIViewController {
            LoginSdkTheme(theme = config.theme) {
                LoginScreen(
                    controller = controller,
                    uiOptions = config.uiOptions,
                    onShowMessage = ::showAlert,
                )
            }
        }
    }

    private fun showAlert(message: String) {
        val root = UIApplication.sharedApplication.keyWindow?.rootViewController ?: return
        val alert = UIAlertController.alertControllerWithTitle(
            title = "提示",
            message = message,
            preferredStyle = UIAlertControllerStyleAlert,
        )
        alert.addAction(
            UIAlertAction.actionWithTitle(
                title = "确定",
                style = UIAlertActionStyleDefault,
                handler = null,
            ),
        )
        root.presentViewController(alert, animated = true, completion = null)
    }
}

/**
 * Present 登录页（全屏模态）。
 */
fun presentLoginViewController(
    hostCallback: LoginCallback,
    config: LoginConfig,
    from: UIViewController,
) {
    var loginVc: UIViewController? = null
    val vc = LoginComposeScreen.createViewController(
        hostCallback = hostCallback,
        config = config,
        onDismiss = {
            loginVc?.dismissViewControllerAnimated(true, completion = null)
        },
    )
    loginVc = vc
    vc.modalPresentationStyle = UIModalPresentationFullScreen
    from.presentViewController(vc, animated = true, completion = null)
}
