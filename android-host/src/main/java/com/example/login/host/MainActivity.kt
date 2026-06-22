package com.example.login.host

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.login.host.ui.screen.MainScreen
import com.example.login.host.ui.theme.LoginSdkDemoTheme
import com.example.login.sdk.api.LoginCallback
import com.example.login.sdk.api.LoginError
import com.example.login.sdk.api.LoginSDK
import com.example.login.sdk.auth.LoginSession

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LoginSdkDemoTheme {
                MainScreen(
                    onEnter = {
                        if (LoginSDK.isLoggedIn()) {
                            startActivity(Intent(this, ProfileActivity::class.java))
                        } else {
                            LoginSDK.launchLogin(loginCallback)
                        }
                    },
                )
            }
        }
    }

    private val loginCallback = object : LoginCallback {
        override fun onSuccess(session: LoginSession) {
            Toast.makeText(
                this@MainActivity,
                "登录成功: ${session.profile?.nickname}",
                Toast.LENGTH_SHORT,
            ).show()
            startActivity(Intent(this@MainActivity, ProfileActivity::class.java))
        }

        override fun onError(error: LoginError) {
            Toast.makeText(this@MainActivity, error.message, Toast.LENGTH_LONG).show()
        }

        override fun onCancel() {
            Toast.makeText(this@MainActivity, "已取消登录", Toast.LENGTH_SHORT).show()
        }
    }
}
