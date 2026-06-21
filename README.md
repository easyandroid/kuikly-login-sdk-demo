# Kuikly Login SDK Demo

基于 KMP 的解耦登录 SDK 预演工程，支持 Android 先行、后期扩展 iOS / 鸿蒙。

## 特性

- **五种登录方式**：手机号、邮箱、微信、Apple ID、Google
- **解耦架构**：Facade + Strategy Provider + Repository + UI Contract
- **Kuikly 就绪**：UI 层可无缝迁移至 Kuikly Page，SDK API 不变
- **可运行 Demo**：Android 宿主 App（Jetpack Compose）演示完整登录 → 个人中心流程

## 快速开始

```bash
cd kuikly-login-sdk-demo
gradle wrapper   # 首次
./gradlew :android-host:installDebug
```

Demo 测试账号：
- 手机：`13800138000` / 验证码：`123456`
- 邮箱：`demo@example.com` / 密码：`123456`

## 工程结构

```
├── login-sdk/       # KMP 登录 SDK
├── android-host/    # Android 宿主 Demo
└── docs/
    ├── INTEGRATION.md       # 接入文档（主文档）
    ├── ARCHITECTURE.md      # 架构详解
    ├── PERFORMANCE.md       # 性能对比（Kuikly vs Flutter vs H5）
    └── THIRD_PARTY_AUTH.md  # 第三方登录指南
```

## 文档

| 文档 | 内容 |
|------|------|
| [接入文档](docs/INTEGRATION.md) | 快速接入、API 说明、Kuikly 迁移路径 |
| [架构设计](docs/ARCHITECTURE.md) | 分层、时序图、模块边界 |
| [性能对比](docs/PERFORMANCE.md) | Kuikly/KMP vs Flutter vs H5 登录 SDK 性能分析 |
| [第三方登录](docs/THIRD_PARTY_AUTH.md) | 微信/Apple/Google 生产接入 |

## 核心 API

```kotlin
// 初始化
LoginSDK.init(LoginConfig(appId = "...", providers = createAndroidAuthProviders()))

// 检查登录态
if (LoginSDK.isLoggedIn()) { /* 个人中心 */ }

// UI 控制器
val controller = LoginSDK.createLoginController(callback)
controller.loginWithThirdParty(AuthMethod.WECHAT)
```

## 演进路线

1. **Phase 1**（当前）：Android Jetpack Compose UI + KMP SDK
2. **Phase 2**：登录 UI 迁移 Kuikly Page
3. **Phase 3**：发布 AAR / XCFramework，iOS / 鸿蒙接入
4. **Phase 4**：个人中心逐步 Kuikly 化

## License

Apache 2.0（预演工程，正式产品请自行补充 License）
