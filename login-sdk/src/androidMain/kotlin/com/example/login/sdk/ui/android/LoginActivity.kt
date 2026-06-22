package com.example.login.sdk.ui.android

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.addCallback
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.login.sdk.api.LoginCallback
import com.example.login.sdk.api.LoginError
import com.example.login.sdk.api.LoginSDK
import com.example.login.sdk.auth.LoginSession
import com.example.login.sdk.internal.LoginCallbackHolder
import com.example.login.sdk.ui.LoginScreen
import com.example.login.sdk.ui.LoginSdkTheme

/**
 * SDK 内置登录页（Compose Multiplatform，与 iOS 共用 [LoginScreen]）。
 */
class LoginActivity : ComponentActivity() {

    private lateinit var hostCallback: LoginCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hostCallback = LoginCallbackHolder.consume()
            ?: run {
                finish()
                return
            }
        val controller = LoginSDK.createLoginController(activityCallback)
        val uiOptions = LoginSDK.config().uiOptions
        setContent {
            LoginSdkTheme(theme = LoginSDK.config().theme) {
                LoginScreen(
                    controller = controller,
                    uiOptions = uiOptions,
                    onShowMessage = { message ->
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    },
                )
            }
        }
        onBackPressedDispatcher.addCallback(this) {
            hostCallback.onCancel()
            finish()
        }
    }

    private val activityCallback = object : LoginCallback {
        override fun onSuccess(session: LoginSession) {
            hostCallback.onSuccess(session)
            finish()
        }

        override fun onError(error: LoginError) {
            hostCallback.onError(error)
        }

        override fun onCancel() {
            hostCallback.onCancel()
            finish()
        }
    }

    companion object {
        fun newIntent(context: Context): Intent =
            Intent(context, LoginActivity::class.java)
    }
}
