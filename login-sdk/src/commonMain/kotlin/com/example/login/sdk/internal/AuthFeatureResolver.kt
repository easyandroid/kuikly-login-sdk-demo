package com.example.login.sdk.internal

import com.example.login.sdk.auth.AuthFeatureConfig
import com.example.login.sdk.auth.AuthFlow
import com.example.login.sdk.auth.AuthMethod
import com.example.login.sdk.auth.AuthProviderRegistry

internal fun resolveAvailableMethods(
    registry: AuthProviderRegistry,
    featureConfig: AuthFeatureConfig,
    enabledMethods: Set<AuthMethod>?,
): List<AuthMethod> {
    return registry.availableMethods()
        .filter { method ->
            when (method) {
                AuthMethod.PHONE -> featureConfig.showPhoneAuth
                AuthMethod.EMAIL -> featureConfig.showEmailAuth
                AuthMethod.WECHAT, AuthMethod.APPLE_ID, AuthMethod.GOOGLE -> featureConfig.showThirdPartyAuth
            }
        }
        .filter { method -> enabledMethods?.contains(method) != false }
        .sortedBy { it.ordinal }
}

internal fun resolveInitialMethod(
    available: List<AuthMethod>,
    preferred: AuthMethod? = null,
): AuthMethod? {
    if (available.isEmpty()) return null
    if (preferred != null && preferred in available) return preferred
    return available.firstOrNull { it == AuthMethod.PHONE }
        ?: available.firstOrNull { it == AuthMethod.EMAIL }
        ?: available.first()
}

internal fun AuthFlow.title(): String = when (this) {
    AuthFlow.LOGIN -> "登录"
    AuthFlow.REGISTER -> "注册"
    AuthFlow.FORGOT_PASSWORD -> "找回密码"
}

internal fun AuthFlow.primaryButtonLabel(): String = when (this) {
    AuthFlow.LOGIN -> "登录"
    AuthFlow.REGISTER -> "注册"
    AuthFlow.FORGOT_PASSWORD -> "重置密码"
}
