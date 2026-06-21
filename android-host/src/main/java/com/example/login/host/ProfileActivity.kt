package com.example.login.host

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.login.host.ui.screen.ProfileScreen
import com.example.login.host.ui.theme.LoginSdkDemoTheme
import com.example.login.sdk.api.LoginSDK
import kotlinx.coroutines.launch

class ProfileActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val session = LoginSDK.currentSession()
        if (session == null) {
            startActivity(Intent(this, LoginActivity::class.java))
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
                            startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
                            finish()
                        }
                    },
                )
            }
        }
    }
}
