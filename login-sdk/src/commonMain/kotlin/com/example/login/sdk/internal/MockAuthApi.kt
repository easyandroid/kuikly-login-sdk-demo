package com.example.login.sdk.internal

import com.example.login.sdk.auth.AuthApi
import com.example.login.sdk.auth.AuthMethod
import com.example.login.sdk.auth.LoginSession
import com.example.login.sdk.auth.ThirdPartyAuthPayload
import com.example.login.sdk.auth.TokenStore
import com.example.login.sdk.auth.UserProfile
import kotlinx.coroutines.delay

/**
 * 预演用 Mock API —— 生产环境替换为真实后端 Client
 */
class MockAuthApi : AuthApi {

    override suspend fun loginWithPhone(phone: String, code: String): Result<LoginSession> {
        delay(300)
        if (code != "123456") return Result.failure(IllegalArgumentException("验证码错误"))
        return Result.success(mockSession(AuthMethod.PHONE, phone = phone))
    }

    override suspend fun loginWithEmail(email: String, password: String): Result<LoginSession> {
        delay(300)
        if (password.length < 6) return Result.failure(IllegalArgumentException("密码至少 6 位"))
        return Result.success(mockSession(AuthMethod.EMAIL, email = email))
    }

    override suspend fun loginWithThirdParty(payload: ThirdPartyAuthPayload): Result<LoginSession> {
        delay(300)
        return Result.success(
            mockSession(
                method = payload.method,
                email = payload.email,
                nickname = payload.displayName ?: payload.method.name,
            )
        )
    }

    override suspend fun sendPhoneCode(phone: String): Result<Unit> {
        delay(200)
        if (phone.length < 11) return Result.failure(IllegalArgumentException("手机号格式错误"))
        return Result.success(Unit)
    }

    override suspend fun sendEmailCode(email: String): Result<Unit> {
        delay(200)
        if (!email.contains("@")) return Result.failure(IllegalArgumentException("邮箱格式错误"))
        return Result.success(Unit)
    }

    override suspend fun refreshToken(refreshToken: String): Result<LoginSession> {
        delay(200)
        return Result.success(
            LoginSession(
                userId = "user_refreshed",
                accessToken = "access_${System.currentTimeMillis()}",
                refreshToken = refreshToken,
                expiresAtEpochMs = System.currentTimeMillis() + 3600_000,
                loginMethod = AuthMethod.EMAIL,
            )
        )
    }

    override suspend fun logout(accessToken: String): Result<Unit> {
        delay(100)
        return Result.success(Unit)
    }

    private fun mockSession(
        method: AuthMethod,
        phone: String? = null,
        email: String? = null,
        nickname: String? = null,
    ) = LoginSession(
        userId = "user_${method.name.lowercase()}_${System.currentTimeMillis() % 10000}",
        accessToken = "access_${System.currentTimeMillis()}",
        refreshToken = "refresh_${System.currentTimeMillis()}",
        expiresAtEpochMs = System.currentTimeMillis() + 7200_000,
        loginMethod = method,
        profile = UserProfile(
            nickname = nickname ?: "Demo用户",
            email = email,
            phone = phone,
        ),
    )
}

class InMemoryTokenStore : TokenStore {
    private var session: LoginSession? = null

    override suspend fun save(session: LoginSession) {
        this.session = session
    }

    override suspend fun load(): LoginSession? = session

    override suspend fun clear() {
        session = null
    }
}
