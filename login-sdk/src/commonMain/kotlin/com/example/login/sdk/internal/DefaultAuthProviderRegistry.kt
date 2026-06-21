package com.example.login.sdk.internal

import com.example.login.sdk.auth.AuthMethod
import com.example.login.sdk.auth.AuthProvider
import com.example.login.sdk.auth.AuthProviderRegistry

internal class DefaultAuthProviderRegistry(
    initialProviders: List<AuthProvider> = emptyList(),
) : AuthProviderRegistry {

    private val providers = initialProviders.associateBy { it.method }.toMutableMap()

    override fun register(provider: AuthProvider) {
        providers[provider.method] = provider
    }

    override fun get(method: AuthMethod): AuthProvider? = providers[method]

    override fun availableMethods(): List<AuthMethod> =
        providers.values
            .filter { it.isAvailable() }
            .map { it.method }
            .sortedBy { it.ordinal }
}
