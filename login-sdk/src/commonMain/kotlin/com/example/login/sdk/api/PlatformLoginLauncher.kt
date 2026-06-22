package com.example.login.sdk.api

/**
 * 平台登录页启动器（Android Activity / iOS ViewController）。
 */
internal expect object PlatformLoginLauncher {
    fun launch()
}
