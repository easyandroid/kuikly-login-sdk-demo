package com.example.login.sdk.internal

import com.example.login.sdk.auth.AuthApi
import com.example.login.sdk.auth.AuthMethod
import com.example.login.sdk.auth.AuthResult
import com.example.login.sdk.auth.LoginCredentials
import com.example.login.sdk.auth.LoginSession
import com.example.login.sdk.auth.TokenStore
import com.example.login.sdk.ui.LoginUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

internal interface LoginRepository {
    suspend fun login(method: AuthMethod, credentials: LoginCredentials): AuthResult
    suspend fun sendVerificationCode(method: AuthMethod, target: String): Result<Unit>
    suspend fun logout(): Result<Unit>
    suspend fun refreshToken(): AuthResult
    fun currentSession(): LoginSession?
}

internal class DefaultLoginRepository(
    private val registry: com.example.login.sdk.auth.AuthProviderRegistry,
    private val tokenStore: TokenStore,
    private val authApi: AuthApi,
) : LoginRepository {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private var cachedSession: LoginSession? = null

    override fun currentSession(): LoginSession? = cachedSession

    override suspend fun login(method: AuthMethod, credentials: LoginCredentials): AuthResult {
        val provider = registry.get(method)
            ?: return AuthResult.Failure("PROVIDER_NOT_AVAILABLE", "登录方式不可用: $method")

        if (!provider.isAvailable()) {
            return AuthResult.Failure("PROVIDER_NOT_AVAILABLE", "当前平台不支持: $method")
        }

        return try {
            val session = when (method) {
                AuthMethod.PHONE -> {
                    val cred = credentials as? LoginCredentials.PhoneOtp
                        ?: return AuthResult.Failure("INVALID_CREDENTIALS", "手机号或验证码为空")
                    authApi.loginWithPhone(cred.phone, cred.code).getOrThrow()
                }
                AuthMethod.EMAIL -> {
                    val cred = credentials as? LoginCredentials.EmailPassword
                        ?: return AuthResult.Failure("INVALID_CREDENTIALS", "邮箱或密码为空")
                    authApi.loginWithEmail(cred.email, cred.password).getOrThrow()
                }
                AuthMethod.WECHAT, AuthMethod.APPLE_ID, AuthMethod.GOOGLE -> {
                    val payload = provider.authenticate(credentials).getOrThrow()
                    authApi.loginWithThirdParty(payload).getOrThrow()
                }
            }
            tokenStore.save(session)
            cachedSession = session
            AuthResult.Success(session)
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: ThirdPartyCancelledException) {
            AuthResult.Cancelled
        } catch (e: Exception) {
            val code = when (method) {
                AuthMethod.WECHAT, AuthMethod.APPLE_ID, AuthMethod.GOOGLE -> "THIRD_PARTY"
                else -> "NETWORK"
            }
            AuthResult.Failure(code, e.message ?: "登录失败")
        }
    }

    override suspend fun sendVerificationCode(method: AuthMethod, target: String): Result<Unit> {
        val provider = registry.get(method)
            ?: return Result.failure(IllegalStateException("Provider not found: $method"))

        return when (method) {
            AuthMethod.PHONE -> authApi.sendPhoneCode(target)
            AuthMethod.EMAIL -> provider.sendVerificationCode(target)
            else -> Result.failure(UnsupportedOperationException("不支持验证码: $method"))
        }
    }

    override suspend fun logout(): Result<Unit> {
        val session = tokenStore.load()
        if (session != null) {
            authApi.logout(session.accessToken)
        }
        tokenStore.clear()
        cachedSession = null
        return Result.success(Unit)
    }

    override suspend fun refreshToken(): AuthResult {
        val session = tokenStore.load()
            ?: return AuthResult.Failure("INVALID_CREDENTIALS", "无有效会话")
        val refresh = session.refreshToken
            ?: return AuthResult.Failure("INVALID_CREDENTIALS", "无 refresh token")

        return try {
            val newSession = authApi.refreshToken(refresh).getOrThrow()
            tokenStore.save(newSession)
            AuthResult.Success(newSession)
        } catch (e: Exception) {
            AuthResult.Failure("NETWORK", e.message ?: "刷新失败")
        }
    }
}

/** 第三方授权被用户取消 */
class ThirdPartyCancelledException : Exception("User cancelled")
