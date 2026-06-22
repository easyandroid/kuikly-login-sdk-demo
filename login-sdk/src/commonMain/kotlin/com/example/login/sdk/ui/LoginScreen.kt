package com.example.login.sdk.ui

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.login.sdk.api.LoginUiOptions
import com.example.login.sdk.auth.AuthFlow
import com.example.login.sdk.auth.AuthMethod
import com.example.login.sdk.internal.primaryButtonLabel
import com.example.login.sdk.internal.title

/**
 * 跨端账号页 UI（登录 / 注册 / 找回密码，Android / iOS 同一套 Compose Multiplatform）。
 */
@Composable
fun LoginScreen(
    controller: LoginUiContract,
    uiOptions: LoginUiOptions,
    onShowMessage: (String) -> Unit,
) {
    val state by controller.state.collectAsState(initial = LoginUiState())
    var phone by remember {
        mutableStateOf(if (uiOptions.prefillDemoCredentials) "13800138000" else "")
    }
    var email by remember {
        mutableStateOf(if (uiOptions.prefillDemoCredentials) "demo@example.com" else "")
    }
    var password by remember {
        mutableStateOf(if (uiOptions.prefillDemoCredentials) "123456" else "")
    }
    var confirmPassword by remember {
        mutableStateOf(if (uiOptions.prefillDemoCredentials) "123456" else "")
    }
    var code by remember {
        mutableStateOf(if (uiOptions.prefillDemoCredentials) "123456" else "")
    }

    LaunchedEffect(Unit) {
        if (uiOptions.prefillDemoCredentials) {
            controller.updatePhone(phone)
            controller.updateEmail(email)
            controller.updatePassword(password)
            controller.updateConfirmPassword(confirmPassword)
            controller.updateVerificationCode(code)
        }
    }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            onShowMessage(it)
            controller.dismissError()
        }
    }

    LoginScreenContent(
        state = state,
        showDemoHint = uiOptions.showDemoHint,
        phone = phone,
        email = email,
        password = password,
        confirmPassword = confirmPassword,
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
        onConfirmPasswordChange = {
            confirmPassword = it
            controller.updateConfirmPassword(it)
        },
        onCodeChange = {
            code = it
            controller.updateVerificationCode(it)
        },
        onSelectFlow = { controller.selectFlow(it) },
        onSelectPhone = { controller.selectMethod(AuthMethod.PHONE) },
        onSelectEmail = { controller.selectMethod(AuthMethod.EMAIL) },
        onSendCode = {
            controller.updatePhone(phone)
            controller.updateEmail(email)
            controller.sendCode()
        },
        onSubmit = {
            controller.updatePhone(phone)
            controller.updateEmail(email)
            controller.updatePassword(password)
            controller.updateConfirmPassword(confirmPassword)
            controller.updateVerificationCode(code)
            controller.submit()
        },
        onWeChatLogin = { controller.loginWithThirdParty(AuthMethod.WECHAT) },
        onAppleLogin = { controller.loginWithThirdParty(AuthMethod.APPLE_ID) },
        onGoogleLogin = { controller.loginWithThirdParty(AuthMethod.GOOGLE) },
    )
}

@Composable
private fun LoginScreenContent(
    state: LoginUiState,
    showDemoHint: Boolean,
    phone: String,
    email: String,
    password: String,
    confirmPassword: String,
    code: String,
    onPhoneChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onCodeChange: (String) -> Unit,
    onSelectFlow: (AuthFlow) -> Unit,
    onSelectPhone: () -> Unit,
    onSelectEmail: () -> Unit,
    onSendCode: () -> Unit,
    onSubmit: () -> Unit,
    onWeChatLogin: () -> Unit,
    onAppleLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
) {
    val flow = state.currentFlow
    val showPhoneForm = state.selectedMethod != AuthMethod.EMAIL
    val formMethods = state.availableMethods.filter { it == AuthMethod.PHONE || it == AuthMethod.EMAIL }
    val thirdPartyMethods = state.availableMethods.filter {
        it == AuthMethod.WECHAT || it == AuthMethod.APPLE_ID || it == AuthMethod.GOOGLE
    }
    val showThirdParty = flow != AuthFlow.FORGOT_PASSWORD && thirdPartyMethods.isNotEmpty()
    val needsConfirmPassword = flow == AuthFlow.REGISTER
    val needsPasswordField = when (flow) {
        AuthFlow.LOGIN -> !showPhoneForm
        AuthFlow.REGISTER -> true
        AuthFlow.FORGOT_PASSWORD -> true
    }
    val needsCodeField = when (flow) {
        AuthFlow.LOGIN -> showPhoneForm
        AuthFlow.REGISTER -> true
        AuthFlow.FORGOT_PASSWORD -> true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text(text = flow.title())

        if (state.enabledFlows.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.enabledFlows.forEach { enabledFlow ->
                    FilterChip(
                        selected = flow == enabledFlow,
                        onClick = { onSelectFlow(enabledFlow) },
                        label = { Text(enabledFlow.title()) },
                    )
                }
            }
        }

        if (showDemoHint) {
            Text(
                text = "Demo 验证码: 123456",
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        if (formMethods.size > 1) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (AuthMethod.PHONE in formMethods) {
                    FilterChip(
                        selected = showPhoneForm,
                        onClick = onSelectPhone,
                        label = { Text("手机号") },
                    )
                }
                if (AuthMethod.EMAIL in formMethods) {
                    FilterChip(
                        selected = !showPhoneForm,
                        onClick = onSelectEmail,
                        label = { Text("邮箱") },
                    )
                }
            }
        }

        if (showPhoneForm && AuthMethod.PHONE in formMethods) {
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
        } else if (AuthMethod.EMAIL in formMethods) {
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
        }

        if (needsCodeField) {
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
        }

        if (needsPasswordField) {
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = {
                    Text(
                        when (flow) {
                            AuthFlow.FORGOT_PASSWORD -> "新密码"
                            else -> "密码"
                        },
                    )
                },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
            )
        }

        if (needsConfirmPassword) {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                label = { Text("确认密码") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
            )
        }

        Button(
            onClick = onSubmit,
            enabled = !state.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
        ) {
            Text(flow.primaryButtonLabel())
        }

        if (flow == AuthFlow.LOGIN && AuthFlow.FORGOT_PASSWORD in state.enabledFlows) {
            TextButton(
                onClick = { onSelectFlow(AuthFlow.FORGOT_PASSWORD) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("忘记密码？")
            }
        }

        if (flow == AuthFlow.REGISTER && AuthFlow.LOGIN in state.enabledFlows) {
            TextButton(
                onClick = { onSelectFlow(AuthFlow.LOGIN) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("已有账号？去登录")
            }
        }

        if (flow == AuthFlow.FORGOT_PASSWORD && AuthFlow.LOGIN in state.enabledFlows) {
            TextButton(
                onClick = { onSelectFlow(AuthFlow.LOGIN) },
                modifier = Modifier.align(Alignment.CenterHorizontally),
            ) {
                Text("返回登录")
            }
        }

        if (showThirdParty) {
            Text(
                text = "或使用以下方式${if (flow == AuthFlow.REGISTER) "注册" else "登录"}",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 24.dp),
            )

            if (AuthMethod.WECHAT in thirdPartyMethods) {
                OutlinedButton(
                    onClick = onWeChatLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                ) { Text("微信${if (flow == AuthFlow.REGISTER) "注册" else "登录"}") }
            }
            if (AuthMethod.APPLE_ID in thirdPartyMethods) {
                OutlinedButton(
                    onClick = onAppleLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) { Text("Apple ID ${if (flow == AuthFlow.REGISTER) "注册" else "登录"}") }
            }
            if (AuthMethod.GOOGLE in thirdPartyMethods) {
                OutlinedButton(
                    onClick = onGoogleLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                ) { Text("Google ${if (flow == AuthFlow.REGISTER) "注册" else "登录"}") }
            }
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
