package com.example.login.sdk.api

import android.app.Activity
import android.content.Intent
import com.example.login.sdk.ui.android.LoginActivity

internal actual object PlatformLoginLauncher {
    actual fun launch() {
        val context = LoginAndroidRuntime.applicationContext
            ?: error("Android: call LoginSDK.init(Context, LoginConfig) before launchLogin().")
        val intent = LoginActivity.newIntent(context).apply {
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)
    }
}
