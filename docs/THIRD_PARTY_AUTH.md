# 第三方登录接入指南

本文说明如何在生产环境中接入微信、Apple ID、Google 登录，替换 Demo 中的 `DemoThirdPartyAuthLauncher`。

---

## 1. 总体原则

SDK **不直接依赖** 第三方 SDK，而是通过 `ThirdPartyAuthLauncher` 接口由宿主注入：

```kotlin
fun interface ThirdPartyAuthLauncher {
    suspend fun launch(method: AuthMethod): Result<ThirdPartyAuthPayload>
}
```

好处：
- SDK AAR 不携带微信/Google 等重型依赖
- 宿主可控制 SDK 版本
- Activity 回调由宿主处理，避免 SDK 耦合生命周期

---

## 2. 微信登录

### 2.1 依赖

```kotlin
// 宿主 app/build.gradle.kts（不是 login-sdk）
implementation("com.tencent.mm.opensdk:wechat-sdk-android:6.8.0")
```

### 2.2 宿主配置

1. 微信开放平台注册 App，获取 AppID
2. `AndroidManifest.xml` 注册 `WXEntryActivity`（包名路径固定：`{applicationId}.wxapi.WXEntryActivity`）

### 2.3 实现 Launcher

```kotlin
class WeChatAuthLauncherImpl(
    private val activity: Activity,
    private val appId: String,
) : ThirdPartyAuthLauncher {

    private var continuation: CancellableContinuation<Result<ThirdPartyAuthPayload>>? = null

    override suspend fun launch(method: AuthMethod): Result<ThirdPartyAuthPayload> =
        suspendCancellableCoroutine { cont ->
            if (method != AuthMethod.WECHAT) {
                cont.resume(Result.failure(IllegalArgumentException("Not WeChat")))
                return@suspendCancellableCoroutine
            }
            continuation = cont
            val req = SendAuth.Req().apply {
                scope = "snsapi_userinfo"
                state = "login_${System.currentTimeMillis()}"
            }
            WXAPIFactory.createWXAPI(activity, appId, true).sendReq(req)
        }

    /** 在 WXEntryActivity.onResp 中调用 */
    fun onAuthResponse(resp: SendAuth.Resp) {
        val cont = continuation ?: return
        continuation = null
        if (resp.errCode == BaseResp.ErrCode.ERR_OK) {
            cont.resume(Result.success(
                ThirdPartyAuthPayload(
                    method = AuthMethod.WECHAT,
                    authorizationCode = resp.code,
                )
            ))
        } else {
            cont.resume(Result.failure(Exception("WeChat auth failed: ${resp.errCode}")))
        }
    }
}
```

### 2.4 注册

```kotlin
val wechatLauncher = WeChatAuthLauncherImpl(this, "your-wx-app-id")

LoginSDK.init(LoginConfig(
    appId = "your-app-id",
    providers = createAndroidAuthProviders(
        launcher = wechatLauncher,
        isWeChatInstalled = {
            WXAPIFactory.createWXAPI(context, "your-wx-app-id", true).isWXAppInstalled
        },
    ),
))
```

### 2.5 后端

`AuthApi.loginWithThirdParty` 将 `authorizationCode` 发送到后端，后端调用微信 `oauth2/access_token` 换取 openId 和 session。

---

## 3. Apple ID 登录

### 3.1 iOS（原生）

使用 `AuthenticationServices`：

```swift
// iOS 宿主实现，通过 Kotlin/Native 回调或 Module 桥接
import AuthenticationServices

class AppleAuthLauncher: ASAuthorizationControllerDelegate {
    func launch() {
        let provider = ASAuthorizationAppleIDProvider()
        let request = provider.createRequest()
        request.requestedScopes = [.email, .fullName]
        let controller = ASAuthorizationController(authorizationRequests: [request])
        controller.delegate = self
        controller.performRequests()
    }
}
```

Kuikly iOS 接入时，通过 `Module` 机制桥接。

### 3.2 Android（Web OAuth）

Apple 不提供 Android 原生 SDK，通常使用：
- [Sign in with Apple JS](https://developer.apple.com/documentation/sign_in_with_apple/sign_in_with_apple_js)
- 或后端 OAuth redirect 流程

```kotlin
class AppleWebAuthLauncher(private val activity: Activity) : ThirdPartyAuthLauncher {
    override suspend fun launch(method: AuthMethod): Result<ThirdPartyAuthPayload> {
        // 打开 Custom Tabs / WebView 到 Apple OAuth URL
        // 回调 URI 中获取 id_token
        // 返回 ThirdPartyAuthPayload(idToken = "...")
    }
}
```

### 3.3 后端

验证 Apple `id_token`（JWT），提取 `sub` 作为 userId。

---

## 4. Google 登录

### 4.1 依赖

```kotlin
// 宿主 app
implementation("com.google.android.gms:play-services-auth:21.2.0")
// Android 14+ 推荐
implementation("androidx.credentials:credentials:1.3.0")
implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")
```

### 4.2 Credential Manager 实现（推荐）

```kotlin
class GoogleAuthLauncherImpl(
    private val activity: Activity,
    private val serverClientId: String,
) : ThirdPartyAuthLauncher {

    override suspend fun launch(method: AuthMethod): Result<ThirdPartyAuthPayload> =
        suspendCancellableCoroutine { cont ->
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(serverClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            CredentialManager.create(activity).getCredential(activity, request,
                object : CredentialManagerCallback<GetCredentialResponse, GetCredentialException> {
                    override fun onResult(result: GetCredentialResponse) {
                        val googleIdToken = (result.credential as? GoogleIdTokenCredential)
                            ?.idToken
                        if (googleIdToken != null) {
                            cont.resume(Result.success(
                                ThirdPartyAuthPayload(
                                    method = AuthMethod.GOOGLE,
                                    idToken = googleIdToken,
                                )
                            ))
                        } else {
                            cont.resume(Result.failure(Exception("No Google ID token")))
                        }
                    }
                    override fun onError(e: GetCredentialException) {
                        cont.resume(Result.failure(e))
                    }
                }
            )
        }
}
```

### 4.3 后端

验证 Google `id_token`（使用 Google API 公钥），提取 email 和 sub。

---

## 5. 统一 Launcher（推荐）

生产环境建议一个 Launcher 处理所有第三方方式：

```kotlin
class AppThirdPartyAuthLauncher(
    private val activity: Activity,
    private val wechatLauncher: WeChatAuthLauncherImpl,
    private val googleLauncher: GoogleAuthLauncherImpl,
    private val appleLauncher: AppleWebAuthLauncher,
) : ThirdPartyAuthLauncher {

    override suspend fun launch(method: AuthMethod): Result<ThirdPartyAuthPayload> =
        when (method) {
            AuthMethod.WECHAT -> wechatLauncher.launch(method)
            AuthMethod.GOOGLE -> googleLauncher.launch(method)
            AuthMethod.APPLE_ID -> appleLauncher.launch(method)
            else -> Result.failure(IllegalArgumentException("Unsupported: $method"))
        }
}
```

---

## 6. 平台可用性矩阵

| 登录方式 | Android | iOS | 鸿蒙 | 备注 |
|----------|---------|-----|------|------|
| 手机号 | ✅ | ✅ | ✅ | 纯 common |
| 邮箱 | ✅ | ✅ | ✅ | 纯 common |
| 微信 | ✅ | ✅ | ⚠️ | 需平台 SDK |
| Apple ID | ⚠️ Web | ✅ | ⚠️ | iOS 原生最佳 |
| Google | ✅ | ✅ | ❌ | 鸿蒙需替代方案 |

`AuthProvider.isAvailable()` 负责运行时过滤，`LoginSDK.availableMethods()` 返回过滤后的列表。

---

## 7. 安全建议

1. **Token 不下发前端存储明文密码**
2. **第三方 authorizationCode / idToken 立即送后端交换 session**，不在客户端长期存储
3. **实现 SecureTokenStore**：
   ```kotlin
   class SecureTokenStore(context: Context) : TokenStore {
       private val prefs = EncryptedSharedPreferences.create(/* ... */)
       // ...
   }
   ```
4. **Certificate Pinning** 用于 AuthApi 网络层
5. **微信/Apple/Google 的 Client Secret 只在后端使用**

---

## 8. 后端 API 契约（建议）

```
POST /auth/phone/login        { phone, code }           → LoginSession
POST /auth/email/login        { email, password }       → LoginSession
POST /auth/wechat/login       { code }                  → LoginSession
POST /auth/apple/login        { idToken }               → LoginSession
POST /auth/google/login       { idToken }               → LoginSession
POST /auth/phone/send-code    { phone }                 → { success }
POST /auth/token/refresh      { refreshToken }          → LoginSession
POST /auth/logout             Authorization: Bearer xxx → { success }
```

`AuthApi` 接口与此契约一一对应，替换 `MockAuthApi` 即可。
