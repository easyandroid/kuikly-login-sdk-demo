-keep class com.example.login.sdk.api.** { *; }
-keep class com.example.login.sdk.auth.** { *; }
-keep class com.example.login.sdk.ui.** { *; }
-keep class com.example.login.sdk.provider.** { *; }

# 隐藏 internal 实现
-dontwarn com.example.login.sdk.internal.**
