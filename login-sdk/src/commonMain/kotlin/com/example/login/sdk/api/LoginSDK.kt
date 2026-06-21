package com.example.login.sdk.api

import com.example.login.sdk.auth.AuthMethod
import com.example.login.sdk.auth.AuthProvider
import com.example.login.sdk.auth.AuthProviderRegistry
import com.example.login.sdk.auth.AuthResult
import com.example.login.sdk.auth.LoginCredentials
import com.example.login.sdk.auth.LoginSession
import com.example.login.sdk.auth.TokenStore
import com.example.login.sdk.internal.DefaultAuthProviderRegistry
import com.example.login.sdk.internal.DefaultLoginRepository
import com.example.login.sdk.internal.InMemoryTokenStore
import com.example.login.sdk.internal.LoginRepository
import com.example.login.sdk.ui.LoginUiContract
import com.example.login.sdk.ui.LoginUiState
import kotlinx.coroutines.flow.StateFlow

/**
 * 登录 SDK 对外唯一入口（Facade）。
 *
 * 宿主 App 只需依赖此对象，不直接接触 Provider / Repository 实现。
 */
object LoginSDK {

    private var initialized = false
    private lateinit var config: LoginConfig
    private lateinit var repository: LoginRepository
    private lateinit var registry: AuthProviderRegistry
    private lateinit var tokenStore: TokenStore

    /** 初始化 SDK，必须在任何登录操作前调用 */
    fun init(config: LoginConfig) {
        if (initialized) return
        this.config = config
        this.tokenStore = config.tokenStore ?: InMemoryTokenStore()
        this.registry = DefaultAuthProviderRegistry(config.providers)
        this.repository = DefaultLoginRepository(
            registry = registry,
            tokenStore = tokenStore,
            authApi = config.authApi,
        )
        initialized = true
    }

    /** 注册/替换某个登录 Provider（用于宿主注入平台实现） */
    fun registerProvider(provider: AuthProvider) {
        ensureInit()
        registry.register(provider)
    }

    /** 获取当前可用登录方式（按平台过滤后） */
    fun availableMethods(): List<AuthMethod> {
        ensureInit()
        return registry.availableMethods()
    }

    /** 是否已登录 */
    fun isLoggedIn(): Boolean {
        ensureInit()
        return repository.currentSession() != null
    }

    /** 获取当前会话 */
    fun currentSession(): LoginSession? {
        ensureInit()
        return repository.currentSession()
    }

    /** 执行登录（由 UI 层或宿主直接调用） */
    suspend fun login(method: AuthMethod, credentials: LoginCredentials): AuthResult {
        ensureInit()
        return repository.login(method, credentials)
    }

    /** 发送验证码（手机号 / 邮箱） */
    suspend fun sendVerificationCode(method: AuthMethod, target: String): Result<Unit> {
        ensureInit()
        return repository.sendVerificationCode(method, target)
    }

    /** 退出登录 */
    suspend fun logout(): Result<Unit> {
        ensureInit()
        return repository.logout()
    }

    /** 刷新 Token */
    suspend fun refreshToken(): AuthResult {
        ensureInit()
        return repository.refreshToken()
    }

    /** 创建 UI 控制器（Kuikly Page / Compose 页面共用） */
    fun createLoginController(callback: LoginCallback): LoginUiContract {
        ensureInit()
        return LoginUiController(repository, registry, callback)
    }

    /** 读取 UI 状态流 */
    fun loginUiState(): StateFlow<LoginUiState>? {
        ensureInit()
        return (repository as? DefaultLoginRepository)?.uiState
    }

    internal fun config(): LoginConfig {
        ensureInit()
        return config
    }

    private fun ensureInit() {
        check(initialized) { "LoginSDK.init() must be called before use." }
    }
}

/**
 * 宿主回调接口
 */
interface LoginCallback {
    fun onSuccess(session: LoginSession)
    fun onError(error: LoginError)
    fun onCancel()
}

sealed class LoginError(open val message: String) {
    data class ProviderNotAvailable(val method: AuthMethod) :
        LoginError("Provider not available: $method")

    data class InvalidCredentials(override val message: String) :
        LoginError(message)

    data class Network(override val message: String) :
        LoginError(message)

    data class ThirdParty(val method: AuthMethod, override val message: String) :
        LoginError(message)

    data class Unknown(override val message: String) :
        LoginError(message)
}
