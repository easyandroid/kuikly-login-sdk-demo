package com.example.login.host

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.login.sdk.api.LoginCallback
import com.example.login.sdk.api.LoginError
import com.example.login.sdk.api.LoginSDK
import com.example.login.sdk.auth.AuthMethod
import com.example.login.sdk.auth.LoginSession
import com.example.login.sdk.ui.LoginUiContract
import com.example.login.sdk.ui.authMethodUiMeta
import com.example.login.host.databinding.ActivityLoginBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 登录页 Demo —— 模拟 Kuikly Page 与原生宿主的关系。
 *
 * 正式接入 Kuikly 时，此 UI 迁移至 Kuikly Compose Page，
 * LoginUiContract 保持不变，宿主仅负责容器与生命周期。
 */
class LoginActivity : AppCompatActivity(), LoginCallback {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var controller: LoginUiContract

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        controller = LoginSDK.createLoginController(this)
        setupThirdPartyButtons()
        setupFormActions()
        observeState()
    }

    private fun setupThirdPartyButtons() {
        binding.btnWechat.setOnClickListener {
            controller.loginWithThirdParty(AuthMethod.WECHAT)
        }
        binding.btnApple.setOnClickListener {
            controller.loginWithThirdParty(AuthMethod.APPLE_ID)
        }
        binding.btnGoogle.setOnClickListener {
            controller.loginWithThirdParty(AuthMethod.GOOGLE)
        }
    }

    private fun setupFormActions() {
        binding.tabPhone.setOnClickListener {
            controller.selectMethod(AuthMethod.PHONE)
        }
        binding.tabEmail.setOnClickListener {
            controller.selectMethod(AuthMethod.EMAIL)
        }
        binding.btnSendCode.setOnClickListener {
            controller.updatePhone(binding.inputPhone.text.toString())
            controller.sendCode()
        }
        binding.btnLogin.setOnClickListener {
            controller.selectMethod(
                if (binding.panelPhone.visibility == View.VISIBLE) AuthMethod.PHONE
                else AuthMethod.EMAIL
            )
            controller.updatePhone(binding.inputPhone.text.toString())
            controller.updateEmail(binding.inputEmail.text.toString())
            controller.updatePassword(binding.inputPassword.text.toString())
            controller.updateVerificationCode(binding.inputCode.text.toString())
            controller.login()
        }
    }

    private fun observeState() {
        lifecycleScope.launch {
            controller.state.collectLatest { state ->
                binding.progressBar.visibility =
                    if (state.isLoading || state.isSendingCode) View.VISIBLE else View.GONE

                binding.btnSendCode.isEnabled = !state.isSendingCode
                binding.btnLogin.isEnabled = !state.isLoading

                when (state.selectedMethod) {
                    AuthMethod.PHONE, null -> {
                        binding.panelPhone.visibility = View.VISIBLE
                        binding.panelEmail.visibility = View.GONE
                    }
                    AuthMethod.EMAIL -> {
                        binding.panelPhone.visibility = View.GONE
                        binding.panelEmail.visibility = View.VISIBLE
                    }
                    else -> Unit
                }

                state.errorMessage?.let { msg ->
                    Snackbar.make(binding.root, msg, Snackbar.LENGTH_LONG)
                        .setAction("关闭") { controller.dismissError() }
                        .show()
                }

                // 根据平台可用性隐藏按钮
                val methods = state.availableMethods.toSet()
                binding.btnWechat.visibility =
                    if (AuthMethod.WECHAT in methods) View.VISIBLE else View.GONE
                binding.btnApple.visibility =
                    if (AuthMethod.APPLE_ID in methods) View.VISIBLE else View.GONE
                binding.btnGoogle.visibility =
                    if (AuthMethod.GOOGLE in methods) View.VISIBLE else View.GONE
            }
        }
        controller.selectMethod(AuthMethod.PHONE)
    }

    override fun onSuccess(session: LoginSession) {
        Snackbar.make(binding.root, "登录成功: ${session.profile?.nickname}", Snackbar.LENGTH_SHORT).show()
        startActivity(Intent(this, ProfileActivity::class.java))
        finish()
    }

    override fun onError(error: LoginError) {
        Snackbar.make(binding.root, error.message, Snackbar.LENGTH_LONG).show()
    }

    override fun onCancel() {
        Snackbar.make(binding.root, "已取消登录", Snackbar.LENGTH_SHORT).show()
    }
}
