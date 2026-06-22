# iOS Demo 宿主运行说明

## 要在 Xcode 跑吗？

**推荐用 Xcode**（最直接）：

1. Mac 上拉代码
2. 首次生成 Gradle Wrapper（若缺失）：
   ```bash
   cd kuikly-login-sdk-demo
   gradle wrapper
   ```
3. 用 Xcode 打开：
   ```
   ios-host/ios-host.xcodeproj
   ```
4. 选择 **iPhone 模拟器**（如 iPhone 15）
5. 点击 **Run (▶)**

首次编译会执行 Build Phase **「Compile Kotlin Framework」**，自动调用 Gradle 构建 `LoginSdk.framework`，稍等几分钟。

**也可以用 Android Studio（Mac 版）**：打开工程根目录，配置 iOS Run Configuration 指向 `ios-host`，但首次配置比 Xcode 麻烦，**建议先用 Xcode**。

---

## 签名（真机）

模拟器一般不需要 Team。真机调试时：

```bash
cp ios-host/Configuration/TeamID.xcconfig.example ios-host/Configuration/TeamID.xcconfig
# 编辑 TeamID.xcconfig 填入 DEVELOPMENT_TEAM = 你的TeamID
```

---

## 与 Android Demo 对齐

| | android-host | ios-host |
|--|--------------|----------|
| 入口 | MainActivity | ContentView |
| 初始化 | `LoginSDK.init(context, ...)` | `IosDemoBridge.initDemo()` |
| 登录 | `LoginSDK.launchLogin()` | `IosDemoBridge.launchLoginDemo()` |
| 登录 UI | 共用 `commonMain/LoginScreen.kt` | 同上 |

Demo 账号（预填）：手机 `13800138000` / 验证码 `123456`

---

## 常见问题

### Build Phase 报 gradlew not found

在工程根目录执行 `gradle wrapper`，确保存在 `gradlew` 和 `gradle/wrapper/gradle-wrapper.jar`。

### embedAndSignAppleFrameworkForXcode 失败

- 确认已安装 **Xcode** 与 **Command Line Tools**
- JDK 17：`export JAVA_HOME=$(/usr/libexec/java_home -v 17)`
- 见 [BUILD_TROUBLESHOOTING.md](../docs/BUILD_TROUBLESHOOTING.md)

### 登录页与 Android 不一致

两端共用 `login-sdk/src/commonMain/.../LoginScreen.kt`，若不一致请确认两端依赖同一 commit / 同一 SDK 版本。
