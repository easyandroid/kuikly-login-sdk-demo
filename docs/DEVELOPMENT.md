# Login SDK 开发文档

> 版本：0.2.0-preview  
> 读者：SDK 维护者、Android / iOS 业务开发  
> 接入方请优先阅读：[INTEGRATION.md](./INTEGRATION.md)

---

## 1. 产物定位

`login-sdk` 是**可独立发布的登录 SDK**，目标：

| 目标 | 说明 |
|------|------|
| **双端一致 UI** | Android / iOS 共用 `LoginUiContract`；UI 最终统一为 Kuikly Page |
| **多 App 复用** | 个人中心、业务 App A/B/C 均可 `implementation(login-sdk)` |
| **业务与 UI 解耦** | 可 `launchLogin()` 用内置 UI，也可仅调 `login()` 自绘 UI |
| **与账号中台解耦** | Token 中台（ContentProvider）为**可选独立模块**，不耦合进 login-sdk |

```
┌─────────────────────────────────────────────────────────┐
│ login-sdk（本模块，多 App 依赖）                          │
│  commonMain    API / Repository / LoginUiContract        │
│  androidMain   LoginActivity + Compose UI + Providers    │
│  iosMain       Providers + IosLoginPageFactory（占位）   │
└─────────────────────────────────────────────────────────┘
         ▲                    ▲                    ▲
         │                    │                    │
   个人中心 App           业务 App A            业务 App B

（可选，另模块）account-broker-android — 仅个人中心 App，ContentProvider 分发 Token
```

---

## 2. 模块结构（当前）

```
login-sdk/
├── src/commonMain/kotlin/com/example/login/sdk/
│   ├── api/              # LoginSDK、LoginConfig、launchLogin
│   ├── auth/             # AuthMethod、AuthProvider、TokenStore
│   ├── ui/
│   │   ├── LoginScreen.kt      # ★ 跨端登录 UI（Compose Multiplatform）
│   │   ├── LoginSdkTheme.kt    # ★ 跨端主题
│   │   ├── LoginUiContract.kt
│   │   └── LoginUiState
│   ├── provider/         # PhoneAuthProvider、EmailAuthProvider
│   └── internal/         # Repository、Registry
├── src/androidMain/.../
│   ├── ui/android/LoginActivity.kt   # 加载共用 LoginScreen
│   └── provider/android/
└── src/iosMain/.../
    ├── ui/ios/LoginComposeScreen.kt  # ComposeUIViewController，加载共用 LoginScreen
    └── provider/ios/
```

### 2.1 UI 分层原则

```
commonMain/LoginScreen.kt     ← Android / iOS 同一套 UI（已实现）
    ↕ LoginUiContract
LoginUiController
    ↕
LoginRepository
```

Android 容器：`LoginActivity`  
iOS 容器：`LoginComposeScreen` / `ComposeUIViewController`

---

## 3. 核心 API（开发视角）

### 3.1 初始化

**Android**（必须传入 `Context`，供 `launchLogin` 使用）：

```kotlin
import com.example.login.sdk.api.init

LoginSDK.init(
    context = applicationContext,
    config = LoginConfig(
        appId = "your-app-id",
        providers = createAndroidAuthProviders(launcher = ...),
        tokenStore = SecureTokenStore(context),
        theme = LoginTheme(primaryColor = 0xFF1976D2),
        uiOptions = LoginUiOptions(
            prefillDemoCredentials = false,  // 生产 false
            showDemoHint = false,
        ),
    ),
)
```

**iOS**（`init(config)` + 安装跨端 UI）：

```kotlin
LoginSDK.init(LoginConfig(appId = "...", providers = createIosAuthProviders()))
LoginSDK.installIosLoginUi { window.rootViewController!! }
```

### 3.2 拉起内置登录页（推荐，多 App 接入）

```kotlin
LoginSDK.launchLogin(object : LoginCallback {
    override fun onSuccess(session: LoginSession) { /* 进入业务 */ }
    override fun onError(error: LoginError) { }
    override fun onCancel() { }
})
```

**Android 实现路径**：`LoginSDK.launchLogin` → `LoginActivity` → `commonMain/LoginScreen`  
**iOS 实现路径**：`LoginSDK.launchLogin` → `LoginComposeScreen` → **同一** `commonMain/LoginScreen`

### 3.3 宿主自绘 UI（可选）

```kotlin
val controller = LoginSDK.createLoginController(callback)
// 绑定 controller.state；也可继续用 commonMain/LoginScreen 嵌入自有页面
```

### 3.4 编程式登录（无 UI）

```kotlin
when (val r = LoginSDK.login(AuthMethod.PHONE, LoginCredentials.PhoneOtp(phone, code))) {
    is AuthResult.Success -> ...
    is AuthResult.Failure -> ...
    AuthResult.Cancelled -> ...
}
```

---

## 4. iOS 登录页（已实现跨端 UI）

### 4.1 推荐接入

```kotlin
LoginSDK.init(LoginConfig(...))
LoginSDK.installIosLoginUi { window.rootViewController!! }

LoginSDK.launchLogin(callback)  // 与 Android API 一致
```

### 4.2 嵌入导航栈

```kotlin
val vc = LoginSDK.createLoginViewController(callback) {
    navigationController?.popViewControllerAnimated(true)
}
navigationController?.pushViewController(vc, animated = true)
```

### 4.3 待补全

- `createIosAuthProviders()` 中微信 / Apple / Google **真实 SDK**（当前 Stub）
- 生产环境 `SecureTokenStore`（Keychain）

---

## 5. UI 技术栈说明

登录 UI 使用 **Compose Multiplatform** 在 `commonMain` 统一实现，Android / iOS 视觉与交互一致。

若后续需迁移至 Kuikly Page，只需替换平台容器（Activity / UIViewController），`LoginUiContract` 与 `LoginSDK` API 保持不变。

---

## 6. 多 App 依赖开发约定

### 6.1 每个 App 独立初始化

```kotlin
// 各 App 的 Application
LoginSDK.init(context, LoginConfig(appId = "app-a", ...))
```

| 配置项 | 多 App 建议 |
|--------|-------------|
| `appId` | 按租户/应用区分，或共用同一 `appId` |
| `tokenStore` | 各 App 独立存储，或由账号中台统一存储 |
| `providers` | 注入各自 `ThirdPartyAuthLauncher`（微信需各 App 包名配置） |
| `theme` | 可按品牌覆盖主色、协议链接 |
| SDK 版本 | **所有 App 锁定同一版本**，避免 Session 模型不一致 |

### 6.2 发布产物

详见 **[DISTRIBUTION.md](./DISTRIBUTION.md)**（分发方式选型、Git Submodule、CI 双产物、多 App 版本约定）。

```bash
# Android
./gradlew :login-sdk:assembleRelease

# iOS（Mac）
./gradlew :login-sdk:assembleLoginSdkReleaseXCFramework
```

### 6.3 ProGuard

发布 AAR 时附带 `login-sdk/proguard-rules.pro`；`internal` 包对外不可见。

---

## 7. 与账号中台（Token Broker）的关系

**login-sdk 不负责跨 App 分发 Token。** 若个人中心 App 作为账号中台：

| 模块 | 职责 | 依赖方 |
|------|------|--------|
| `login-sdk` | 登录 UI + 鉴权 + 本 App Session | 所有需要登录页的 App |
| `account-broker-android`（规划） | ContentProvider、保活刷新、签名校验 | 仅个人中心 App |
| `account-client-android`（规划） | `getToken()` / `ensureLogin()` | 仅需 Token、不展示登录 UI 的业务 App |

登录成功后，个人中心 App 在 `onSuccess` 中可将 Session 写入 Broker：

```kotlin
LoginSDK.launchLogin(object : LoginCallback {
    override fun onSuccess(session: LoginSession) {
        AccountBroker.publish(session)  // 规划 API，非 login-sdk 内置
    }
    // ...
})
```

---

## 8. 扩展登录方式

1. `AuthMethod` 枚举新增值  
2. 实现 `AuthProvider`  
3. `LoginConfig.providers` 或 `LoginSDK.registerProvider()` 注册  
4. UI 层增加入口，调用 `loginWithThirdParty()` 或 `selectMethod()`  

**无需修改** `LoginRepository`、`LoginSDK` Facade 内部编排逻辑。

---

## 9. 测试

```bash
# 单元测试
./gradlew :login-sdk:cleanAllTests :login-sdk:allTests

# Demo 手动验证
./gradlew :android-host:installDebug
```

验证清单：

- [ ] `LoginSDK.init(context, config)` 后 `launchLogin` 可打开 SDK 登录页  
- [ ] 手机号 / 邮箱 / 第三方（Demo Mock）登录成功  
- [ ] `LoginSDK.isLoggedIn()` / `currentSession()` 正确  
- [ ] 多 App 依赖同一 AAR 时 Manifest 合并无冲突（仅一个 LoginActivity）  
- [ ] 返回键触发 `onCancel`

---

## 10. 演进路线

| 阶段 | 内容 | 状态 |
|------|------|------|
| Phase 1 | KMP 业务 + **Compose Multiplatform 跨端登录 UI** + `launchLogin` | ✅ 当前 |
| Phase 2 | iOS 第三方 Provider 实装 + XCFramework 发布 | 待开发 |
| Phase 3 | 可选迁移 Kuikly Page（容器替换，API 不变） | 规划 |
| Phase 4 | `account-broker` 账号中台（可选） | 规划 |

---

## 11. 相关文档

- [DISTRIBUTION.md](./DISTRIBUTION.md) — 分发与多端依赖  
- [INTEGRATION.md](./INTEGRATION.md) — 宿主接入指南  
- [ARCHITECTURE.md](./ARCHITECTURE.md) — 架构与时序  
- [THIRD_PARTY_AUTH.md](./THIRD_PARTY_AUTH.md) — 第三方登录  
- [PERFORMANCE.md](./PERFORMANCE.md) — 方案选型对比  
