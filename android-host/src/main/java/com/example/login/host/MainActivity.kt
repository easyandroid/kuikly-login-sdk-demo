package com.example.login.host

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.login.host.ui.screen.MainScreen
import com.example.login.host.ui.theme.LoginSdkDemoTheme
import com.example.login.sdk.api.LoginSDK

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
                            startActivity(Intent(this, LoginActivity::class.java))
                        }
                    },
                )
            }
        }
    }
}
