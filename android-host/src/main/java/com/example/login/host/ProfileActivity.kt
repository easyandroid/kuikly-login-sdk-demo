package com.example.login.host

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.login.host.ui.screen.ProfileScreen
import com.example.login.host.ui.theme.LoginSdkDemoTheme
import com.example.login.sdk.api.LoginCallback
import com.example.login.sdk.api.LoginError
import com.example.login.sdk.api.LoginSDK
import com.example.login.sdk.auth.LoginSession
import kotlinx.coroutines.launch

class ProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = LoginSDK.currentSession()
        if (session == null) {
            LoginSDK.launchLogin(reloginCallback)
            finish()
            return
        }

        setContent {
            LoginSdkDemoTheme {
                ProfileScreen(
                    session = session,
                    onLogout = {
                        lifecycleScope.launch {
                            LoginSDK.logout()
                            Toast.makeText(this@ProfileActivity, "已退出登录", Toast.LENGTH_SHORT).show()
                            LoginSDK.launchLogin(reloginCallback)
                            finish()
                        }
                    },
                )
            }
        }
    }

    private val reloginCallback = object : LoginCallback {
        override fun onSuccess(session: LoginSession) {
            startActivity(Intent(this@ProfileActivity, ProfileActivity::class.java))
        }

        override fun onError(error: LoginError) {
            Toast.makeText(this@ProfileActivity, error.message, Toast.LENGTH_LONG).show()
            finish()
        }

        override fun onCancel() {
            finish()
        }
    }
}
