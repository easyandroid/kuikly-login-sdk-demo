# Kuikly Login SDK Demo

基于 KMP 的**独立登录 SDK**，支持多 App、多技术栈（Android / iOS 原生、Flutter）复用。

## 特性

- **五种登录方式**：手机号、邮箱、微信、Apple ID、Google
- **双 SDK 形态**：`login-sdk-core`（业务）+ `ui-native` / `ui-flutter`（UI，可选）
- **多 App 复用**：个人中心、业务 App 均可接入；可只用业务、不用官方 UI
- **解耦架构**：Facade + Strategy Provider + Repository + UI Contract
- **跨端一致 UI**：`commonMain` Compose Multiplatform，Android / iOS 原生 **同一套 UI**
- **Kuikly 可选**：后续可将容器换为 Kuikly Page，core API 不变

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
    ├── MULTI_APP_ARCHITECTURE.md  # 多 App 多栈方案（主文档）
    ├── DISTRIBUTION.md            # 分发与多端依赖
    ├── INTEGRATION.md             # 宿主接入
    ├── DEVELOPMENT.md             # SDK 开发维护
    ├── ARCHITECTURE.md            # 架构详解
    ├── PERFORMANCE.md             # 性能对比
    ├── ACCOUNT_BROKER.md          # 401 刷新 + 账号中转站
    └── THIRD_PARTY_AUTH.md        # 第三方登录
```

## 文档

| 文档 | 内容 |
|------|------|
| [多 App 多栈方案](docs/MULTI_APP_ARCHITECTURE.md) | **双 SDK 拆分、KMP+Flutter 分工、UI 套数、模块协作** |
| [分发与依赖](docs/DISTRIBUTION.md) | Git / AAR / XCFramework、多 App 版本策略 |
| [接入文档](docs/INTEGRATION.md) | 多 App 接入、`launchLogin`、iOS 概要 |
| [开发文档](docs/DEVELOPMENT.md) | 模块结构、Kuikly 迁移、账号中台关系 |
| [架构设计](docs/ARCHITECTURE.md) | 分层、时序图、模块边界 |
| [性能对比](docs/PERFORMANCE.md) | KMP vs Flutter vs H5 |
| [第三方登录](docs/THIRD_PARTY_AUTH.md) | 微信/Apple/Google 生产接入 |
| [账号中转站](docs/ACCOUNT_BROKER.md) | 401 自动刷新、ContentProvider Token 分发 |

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

// 仅业务、自绘 UI
LoginSDK.login(AuthMethod.PHONE, LoginCredentials.PhoneOtp(phone, code))

// 检查登录态
if (LoginSDK.isLoggedIn()) { /* 已登录 */ }
```

## 演进路线

1. **Phase 1**（当前）：拆 core / ui-native / platform；CMP 跨端 UI + 多 App 接入
2. **Phase 2**：Flutter Plugin + ui-flutter；iOS Provider 实装
3. **Phase 3**：发布 AAR / XCFramework；Benchmark
4. **Phase 4**：account-core 账号中台；可选 Kuikly Page

## License

Apache 2.0（预演工程，正式产品请自行补充 License）
