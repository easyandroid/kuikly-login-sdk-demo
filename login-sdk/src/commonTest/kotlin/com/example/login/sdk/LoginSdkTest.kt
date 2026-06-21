package com.example.login.sdk

import com.example.login.sdk.api.LoginConfig
import com.example.login.sdk.api.LoginSDK
import com.example.login.sdk.auth.AuthMethod
import com.example.login.sdk.auth.AuthResult
import com.example.login.sdk.auth.LoginCredentials
import com.example.login.sdk.provider.EmailAuthProvider
import com.example.login.sdk.provider.PhoneAuthProvider
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LoginSdkTest {

    @Test
    fun phoneLogin_withValidCode_succeeds() = kotlinx.coroutines.test.runTest {
        LoginSDK.init(
            LoginConfig(
                appId = "test",
                providers = listOf(PhoneAuthProvider(), EmailAuthProvider()),
            )
        )

        val result = LoginSDK.login(
            AuthMethod.PHONE,
            LoginCredentials.PhoneOtp("13800138000", "123456"),
        )

        assertTrue(result is AuthResult.Success)
        assertNotNull(LoginSDK.currentSession())
        assertEquals(AuthMethod.PHONE, (result as AuthResult.Success).session.loginMethod)
    }

    @Test
    fun phoneLogin_withInvalidCode_fails() = kotlinx.coroutines.test.runTest {
        LoginSDK.init(
            LoginConfig(
                appId = "test",
                providers = listOf(PhoneAuthProvider()),
            )
        )

        val result = LoginSDK.login(
            AuthMethod.PHONE,
            LoginCredentials.PhoneOtp("13800138000", "000000"),
        )

        assertTrue(result is AuthResult.Failure)
    }
}
