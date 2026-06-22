package com.example.login.sdk.auth

/**
 * 登录 Provider 统一接口 —— 解耦核心。
 *
 * 每种登录方式实现此接口，SDK 核心不感知具体平台 SDK 细节。
 * 平台相关实现放在 androidMain / iosMain，通过 LoginSDK.registerProvider() 注入。
 */
interface AuthProvider {
    val method: AuthMethod

    /** 当前平台是否支持此登录方式 */
    fun isAvailable(): Boolean

    /**
     * 执行授权/登录。
     * - 手机号/邮箱：credentials 携带用户输入
     * - 第三方：Provider 拉起原生 SDK，返回 authorizationCode / idToken 等
     */
    suspend fun authenticate(credentials: LoginCredentials): Result<ThirdPartyAuthPayload>

    /** 发送验证码（仅 PHONE / EMAIL 需要实现） */
    suspend fun sendVerificationCode(target: String): Result<Unit> {
        return Result.failure(UnsupportedOperationException("Not supported for $method"))
    }
}

/**
 * Provider 注册表接口
 */
interface AuthProviderRegistry {
    fun register(provider: AuthProvider)
    fun get(method: AuthMethod): AuthProvider?
    fun availableMethods(): List<AuthMethod>
}

/**
 * 后端鉴权 API 接口 —— 与 UI/Provider 解耦。
 * 真实项目替换为 Ktor / Retrofit 实现。
 */
interface AuthApi {
    suspend fun loginWithPhone(phone: String, code: String): Result<LoginSession>
    suspend fun loginWithEmail(email: String, password: String): Result<LoginSession>
    suspend fun loginWithThirdParty(payload: ThirdPartyAuthPayload): Result<LoginSession>
    suspend fun registerWithPhone(phone: String, code: String, password: String): Result<LoginSession>
    suspend fun registerWithEmail(email: String, password: String, code: String): Result<LoginSession>
    suspend fun resetPasswordWithPhone(phone: String, code: String, newPassword: String): Result<LoginSession>
    suspend fun resetPasswordWithEmail(email: String, code: String, newPassword: String): Result<LoginSession>
    suspend fun sendPhoneCode(phone: String): Result<Unit>
    suspend fun sendEmailCode(email: String): Result<Unit>
    suspend fun refreshToken(refreshToken: String): Result<LoginSession>
    suspend fun logout(accessToken: String): Result<Unit>
}

/**
 * Token 持久化接口 —— 宿主可注入 EncryptedSharedPreferences / Keychain 实现
 */
interface TokenStore {
    suspend fun save(session: LoginSession)
    suspend fun load(): LoginSession?
    suspend fun clear()
}
