package com.example.login.sdk.auth

/**
 * 支持的登录方式
 */
enum class AuthMethod {
    PHONE,
    EMAIL,
    WECHAT,
    APPLE_ID,
    GOOGLE,
}

/**
 * 登录凭证（密封类，每种方式独立数据结构）
 */
sealed class LoginCredentials {
    data class PhoneOtp(val phone: String, val code: String) : LoginCredentials()
    data class EmailPassword(val email: String, val password: String) : LoginCredentials()
    data class PhoneRegister(val phone: String, val code: String, val password: String) : LoginCredentials()
    data class EmailRegister(val email: String, val password: String, val code: String) : LoginCredentials()
    data class PhoneReset(val phone: String, val code: String, val newPassword: String) : LoginCredentials()
    data class EmailReset(val email: String, val code: String, val newPassword: String) : LoginCredentials()
    data class ThirdParty(val method: AuthMethod) : LoginCredentials()
}

/**
 * 第三方授权原始结果（Provider 产出，Repository 消费）
 */
data class ThirdPartyAuthPayload(
    val method: AuthMethod,
    val authorizationCode: String? = null,
    val idToken: String? = null,
    val accessToken: String? = null,
    val openId: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val raw: Map<String, String> = emptyMap(),
)

sealed class AuthResult {
    data class Success(val session: LoginSession) : AuthResult()
    data class Failure(val code: String, val message: String) : AuthResult()
    data object Cancelled : AuthResult()
}

data class LoginSession(
    val userId: String,
    val accessToken: String,
    val refreshToken: String? = null,
    val expiresAtEpochMs: Long? = null,
    val loginMethod: AuthMethod,
    val profile: UserProfile? = null,
)

data class UserProfile(
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val email: String? = null,
    val phone: String? = null,
)
