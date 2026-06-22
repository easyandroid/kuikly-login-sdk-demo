package com.example.login.sdk.internal

import com.example.login.sdk.api.LoginCallback

/**
 * 登录页与宿主之间的单次回调持有器（launchLogin 使用）。
 */
internal object LoginCallbackHolder {
    private var callback: LoginCallback? = null

    fun set(callback: LoginCallback) {
        this.callback = callback
    }

    fun consume(): LoginCallback? = callback?.also { callback = null }

    fun peek(): LoginCallback? = callback
}
