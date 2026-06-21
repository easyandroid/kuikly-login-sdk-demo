package com.example.login.host

import android.app.Application
import com.example.login.sdk.api.LoginCallback
import com.example.login.sdk.api.LoginConfig
import com.example.login.sdk.api.LoginSDK
import com.example.login.sdk.auth.LoginSession
import com.example.login.sdk.provider.android.createAndroidAuthProviders

class LoginHostApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initLoginSdk()
    }

    private fun initLoginSdk() {
        LoginSDK.init(
            LoginConfig(
                appId = "demo-app-id",
                providers = createAndroidAuthProviders(),
            )
        )
    }
}
