package com.example.login.sdk.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.login.sdk.api.LoginTheme

/**
 * 跨端登录页主题（Android / iOS 共用）。
 */
@Composable
fun LoginSdkTheme(
    theme: LoginTheme,
    content: @Composable () -> Unit,
) {
    val primary = Color(theme.primaryColor)
    MaterialTheme(
        colorScheme = lightColorScheme(primary = primary),
        content = content,
    )
}
