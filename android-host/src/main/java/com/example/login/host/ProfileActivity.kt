package com.example.login.host

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.login.sdk.api.LoginSDK
import com.example.login.host.databinding.ActivityProfileBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * 个人中心 Demo —— 宿主原生页面，登录 SDK 仅提供鉴权能力
 */
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val session = LoginSDK.currentSession()
        if (session == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        binding.tvUserId.text = "用户 ID: ${session.userId}"
        binding.tvLoginMethod.text = "登录方式: ${session.loginMethod.name}"
        binding.tvNickname.text = "昵称: ${session.profile?.nickname ?: "-"}"
        binding.tvEmail.text = "邮箱: ${session.profile?.email ?: "-"}"
        binding.tvPhone.text = "手机: ${session.profile?.phone ?: "-"}"

        binding.btnLogout.setOnClickListener {
            lifecycleScope.launch {
                LoginSDK.logout()
                Snackbar.make(binding.root, "已退出登录", Snackbar.LENGTH_SHORT).show()
                startActivity(Intent(this@ProfileActivity, LoginActivity::class.java))
                finish()
            }
        }
    }
}
