package com.example.login.host

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.login.host.ui.screen.LoginScreen
import com.example.login.host.ui.theme.LoginSdkDemoTheme
import com.example.login.sdk.api.LoginCallback
import com.example.login.sdk.api.LoginError
import com.example.login.sdk.api.LoginSDK
import com.example.login.sdk.auth.LoginSession
import com.example.login.sdk.ui.LoginUiContract

/**
 * 登录页 —— Jetpack Compose 实现。
 *
 * UI 只依赖 [LoginUiContract]；迁移到 Kuikly Compose Page 时替换 Composable 层即可。
 */
class LoginActivity : ComponentActivity(), LoginCallback {

    private lateinit var controller: LoginUiContract

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        controller = LoginSDK.createLoginController(this)
        setContent {
            LoginSdkDemoTheme {
                LoginScreen(
                    controller = controller,
                    onShowMessage = { message ->
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                    },
                )
            }
        }
    }

    override fun onSuccess(session: LoginSession) {
        Toast.makeText(this, "登录成功: ${session.profile?.nickname}", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }

    override fun onError(error: LoginError) {
        Toast.makeText(this, error.message, Toast.LENGTH_LONG).show()
    }

    override fun onCancel() {
        Toast.makeText(this, "已取消登录", Toast.LENGTH_SHORT).show()
    }
}
