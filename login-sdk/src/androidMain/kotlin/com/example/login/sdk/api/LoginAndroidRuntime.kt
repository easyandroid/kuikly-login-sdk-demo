package com.example.login.sdk.api

import android.content.Context

internal object LoginAndroidRuntime {
    var applicationContext: Context? = null
}

/**
 * Android 初始化入口：注入 Application Context，供 [LoginSDK.launchLogin] 使用。
 */
fun LoginSDK.init(context: Context, config: LoginConfig) {
    LoginAndroidRuntime.applicationContext = context.applicationContext
    init(config)
}
