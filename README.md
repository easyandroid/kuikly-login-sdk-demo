# Kuikly Login SDK Demo

基于 KMP 的**独立登录 SDK**，内置登录 UI，支持 Android / iOS 多 App 复用。

## 特性

- **五种登录方式**：手机号、邮箱、微信、Apple ID、Google
- **独立 SDK**：`login-sdk` 内置登录页，`LoginSDK.launchLogin()` 一键拉起
- **多 App 复用**：个人中心、业务 App 均可 `implementation(login-sdk)`
- **解耦架构**：Facade + Strategy Provider + Repository + UI Contract
- **跨端一致 UI**：`commonMain/LoginScreen.kt`（Compose Multiplatform），Android / iOS 同一套
- **Kuikly 可选**：后续可将容器换为 Kuikly Page，API 不变
- **可运行 Demo**：`android-host` 演示接入流程（登录 UI 在 SDK 内）

## 快速开始

```bash
cd kuikly-login-sdk-demo
gradle wrapper   # 首次
./gradlew :android-host:installDebug
```

Demo 测试账号：
- 手机：`13800138000` / 验证码：`123456`
- 邮箱：`demo@example.com` / 密码：`123456`

### iOS Demo（需 Mac + Xcode）

```bash
cd kuikly-login-sdk-demo
gradle wrapper   # 首次
open ios-host/ios-host.xcodeproj
# Xcode 选模拟器 → Run ▶
```

详见 [ios-host/README.md](ios-host/README.md)

## 工程结构

```
├── login-sdk/       # KMP 登录 SDK
├── android-host/    # Android 宿主 Demo
├── ios-host/        # iOS 宿主 Demo（Xcode 打开运行）
└── docs/
    ├── DISTRIBUTION.md      # 分发与多端依赖（技术预研）
    ├── INTEGRATION.md       # 接入文档（宿主 App）
    ├── DEVELOPMENT.md       # 开发文档（SDK 维护）
    ├── ARCHITECTURE.md      # 架构详解
    ├── PERFORMANCE.md       # 性能对比（Kuikly vs Flutter vs H5）
    └── THIRD_PARTY_AUTH.md  # 第三方登录指南
```

## 文档

| 文档 | 内容 |
|------|------|
| [分发与依赖](docs/DISTRIBUTION.md) | Git / Submodule / AAR / XCFramework、多 App 版本策略 |
| [接入文档](docs/INTEGRATION.md) | 多 App 接入、`launchLogin`、iOS 概要 |
| [开发文档](docs/DEVELOPMENT.md) | 模块结构、Kuikly 迁移、账号中台关系 |
| [架构设计](docs/ARCHITECTURE.md) | 分层、时序图、模块边界 |
| [性能对比](docs/PERFORMANCE.md) | Kuikly/KMP vs Flutter vs H5 |
| [第三方登录](docs/THIRD_PARTY_AUTH.md) | 微信/Apple/Google 生产接入 |

## 核心 API

```kotlin
// Android 初始化
LoginSDK.init(context, LoginConfig(appId = "...", providers = createAndroidAuthProviders()))

// 拉起 SDK 内置登录页（多 App 推荐入口）
LoginSDK.launchLogin(object : LoginCallback {
    override fun onSuccess(session: LoginSession) { /* 进入业务 */ }
    override fun onError(error: LoginError) { }
    override fun onCancel() { }
})

// 检查登录态
if (LoginSDK.isLoggedIn()) { /* 已登录 */ }
```

## 演进路线

1. **Phase 1**（当前）：Compose Multiplatform 跨端登录 UI + `launchLogin` + 多 App 接入
2. **Phase 2**：iOS Provider 实装 + 发布 XCFramework
3. **Phase 3**：可选迁移 Kuikly Page（API 不变）
4. **Phase 4**：账号中台 `account-broker`（可选）

## License

Apache 2.0（预演工程，正式产品请自行补充 License）
