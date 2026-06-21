package com.example.login.sdk.provider

import com.example.login.sdk.auth.AuthMethod
import com.example.login.sdk.auth.AuthProvider
import com.example.login.sdk.auth.LoginCredentials
import com.example.login.sdk.auth.ThirdPartyAuthPayload
import com.example.login.sdk.internal.ThirdPartyCancelledException
import kotlinx.coroutines.delay

/**
 * 手机号 OTP Provider（common 层实现，不依赖平台 SDK）
 */
class PhoneAuthProvider : AuthProvider {
    override val method = AuthMethod.PHONE
    override fun isAvailable() = true

    override suspend fun authenticate(credentials: LoginCredentials): Result<ThirdPartyAuthPayload> {
        // 手机号走 AuthApi 直接验证，Provider 仅做格式校验
        val cred = credentials as? LoginCredentials.PhoneOtp
            ?: return Result.failure(IllegalArgumentException("Invalid credentials"))
        if (cred.phone.length < 11) return Result.failure(IllegalArgumentException("手机号格式错误"))
        if (cred.code.isBlank()) return Result.failure(IllegalArgumentException("验证码不能为空"))
        return Result.success(ThirdPartyAuthPayload(method = method))
    }
}

/**
 * 邮箱密码 Provider
 */
class EmailAuthProvider : AuthProvider {
    override val method = AuthMethod.EMAIL
    override fun isAvailable() = true

    override suspend fun authenticate(credentials: LoginCredentials): Result<ThirdPartyAuthPayload> {
        val cred = credentials as? LoginCredentials.EmailPassword
            ?: return Result.failure(IllegalArgumentException("Invalid credentials"))
        if (!cred.email.contains("@")) return Result.failure(IllegalArgumentException("邮箱格式错误"))
        if (cred.password.length < 6) return Result.failure(IllegalArgumentException("密码至少 6 位"))
        return Result.success(ThirdPartyAuthPayload(method = method, email = cred.email))
    }

    override suspend fun sendVerificationCode(target: String): Result<Unit> {
        if (!target.contains("@")) return Result.failure(IllegalArgumentException("邮箱格式错误"))
        delay(200)
        return Result.success(Unit)
    }
}
