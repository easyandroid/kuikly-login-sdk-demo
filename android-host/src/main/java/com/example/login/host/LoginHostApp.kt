package com.example.login.host

import android.app.Application
import com.example.login.sdk.api.LoginConfig
import com.example.login.sdk.api.LoginSDK
import com.example.login.sdk.api.LoginUiOptions
import com.example.login.sdk.api.init
import com.example.login.sdk.auth.AuthFeatureConfig
import com.example.login.sdk.provider.android.createAndroidAuthProviders

class LoginHostApp : Application() {

    override fun onCreate() {
        super.onCreate()
        LoginSDK.init(
            context = this,
            config = LoginConfig(
                appId = "demo-app-id",
                providers = createAndroidAuthProviders(),
                uiOptions = LoginUiOptions(
                    prefillDemoCredentials = true,
                    showDemoHint = true,
                ),
                featureConfig = AuthFeatureConfig(
                    showLogin = true,
                    showRegister = true,
                    showForgotPassword = true,
                    showThirdPartyAuth = true,
                ),
            ),
        )
    }
}
