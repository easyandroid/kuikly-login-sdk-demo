package com.example.login.host.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.login.sdk.auth.LoginSession

@Composable
fun ProfileScreen(
    session: LoginSession,
    onLogout: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = "个人中心",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "用户 ID: ${session.userId}",
            modifier = Modifier.padding(top = 24.dp),
        )
        Text(text = "登录方式: ${session.loginMethod.name}")
        Text(text = "昵称: ${session.profile?.nickname ?: "-"}")
        Text(text = "邮箱: ${session.profile?.email ?: "-"}")
        Text(text = "手机: ${session.profile?.phone ?: "-"}")
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 32.dp),
        ) {
            Text("退出登录")
        }
    }
}
