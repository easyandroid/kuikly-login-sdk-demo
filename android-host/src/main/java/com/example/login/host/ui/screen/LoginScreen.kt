package com.example.login.host.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.login.sdk.auth.AuthMethod
import com.example.login.sdk.ui.LoginUiContract
import com.example.login.sdk.ui.LoginUiState

@Composable
fun LoginScreen(
    controller: LoginUiContract,
    onShowMessage: (String) -> Unit,
) {
    val state by controller.state.collectAsStateWithLifecycle()
    var phone by remember { mutableStateOf("13800138000") }
    var email by remember { mutableStateOf("demo@example.com") }
    var password by remember { mutableStateOf("123456") }
    var code by remember { mutableStateOf("123456") }

    LaunchedEffect(Unit) {
        controller.selectMethod(AuthMethod.PHONE)
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            onShowMessage(it)
            controller.dismissError()
        }
    }

    LoginScreenContent(
        state = state,
        phone = phone,
        email = email,
        password = password,
        code = code,
        onPhoneChange = {
            phone = it
            controller.updatePhone(it)
        },
        onEmailChange = {
            email = it
            controller.updateEmail(it)
        },
        onPasswordChange = {
            password = it
            controller.updatePassword(it)
        },
        onCodeChange = {
            code = it
            controller.updateVerificationCode(it)
        },
        onSelectPhone = { controller.selectMethod(AuthMethod.PHONE) },
        onSelectEmail = { controller.selectMethod(AuthMethod.EMAIL) },
        onSendCode = {
            controller.updatePhone(phone)
            controller.sendCode()
        },
        onLogin = {
            val method = state.selectedMethod ?: AuthMethod.PHONE
            controller.selectMethod(method)
            controller.updatePhone(phone)
            controller.updateEmail(email)
            controller.updatePassword(password)
            controller.updateVerificationCode(code)
            controller.login()
        },
        onWeChatLogin = { controller.loginWithThirdParty(AuthMethod.WECHAT) },
        onAppleLogin = { controller.loginWithThirdParty(AuthMethod.APPLE_ID) },
        onGoogleLogin = { controller.loginWithThirdParty(AuthMethod.GOOGLE) },
    )
}

@Composable
private fun LoginScreenContent(
    state: LoginUiState,
    phone: String,
    email: String,
    password: String,
    code: String,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onSelectPhone: () -> Unit,
    onSelectEmail: () -> Unit,
    onSendCode: () -> Unit,
    onLogin: () -> Unit,
    onWeChatLogin: () -> Unit,
    onAppleLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
) {
    val showPhoneForm = state.selectedMethod != AuthMethod.EMAIL
    val available = state.availableMethods.toSet()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text(text = "登录")
        Text(
            text = "Demo 验证码: 123456",
            modifier = Modifier.padding(top = 4.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = showPhoneForm,
                onClick = onSelectPhone,
                label = { Text("手机号") },
            )
            FilterChip(
                selected = !showPhoneForm,
                onClick = onSelectEmail,
                label = { Text("邮箱") },
            )
        }

        if (showPhoneForm) {
            OutlinedTextField(
                value = phone,
                onValueChange = onPhoneChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                label = { Text("手机号") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedTextField(
                    value = code,
                    onValueChange = onCodeChange,
                    modifier = Modifier.weight(1f),
                    label = { Text("验证码") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                )
                TextButton(
                    onClick = onSendCode,
                    enabled = !state.isSendingCode,
                ) {
                    Text(if (state.isSendingCode) "发送中" else "发送验证码")
                }
            }
        } else {
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                label = { Text("邮箱") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
            )
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("密码") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
            )
        }

        Button(
            onClick = onLogin,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Text("登录")
        }

        Text(
            text = "或使用以下方式登录",
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 24.dp),
        )

        if (AuthMethod.WECHAT in available) {
            OutlinedButton(
                onClick = onWeChatLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
            ) { Text("微信登录") }
        }
        if (AuthMethod.APPLE_ID in available) {
            OutlinedButton(
                onClick = onAppleLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) { Text("Apple ID 登录") }
        }
        if (AuthMethod.GOOGLE in available) {
            OutlinedButton(
                onClick = onGoogleLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            ) { Text("Google 登录") }
        }

        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 16.dp),
            )
        }
    }
}
