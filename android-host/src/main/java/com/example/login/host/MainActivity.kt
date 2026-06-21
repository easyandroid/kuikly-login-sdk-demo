package com.example.login.host

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.login.sdk.api.LoginSDK
import com.example.login.host.databinding.ActivityMainBinding

/**
 * 宿主 App 入口 —— 演示「未登录跳转登录页，已登录进入个人中心」
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnEnter.setOnClickListener {
            if (LoginSDK.isLoggedIn()) {
                startActivity(Intent(this, ProfileActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
        }
    }
}
