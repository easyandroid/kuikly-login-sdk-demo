# iOS Demo 宿主运行说明

## 要在 Xcode 跑吗？

**推荐用 Xcode**（最直接）：

1. Mac 上拉代码
2. 安装 **JDK 17**、**Android SDK**（KMP 编 iOS Framework 仍需 Gradle 配置 android 模块，可用 Android Studio 或只装 Command Line Tools + SDK）
3. 配置 `local.properties`（若无 Android Studio 自动生成的）：
   ```bash
   echo "sdk.dir=$HOME/Library/Android/sdk" >> local.properties
   ```
4. **先预构建 Kotlin Framework**（首次必做，避免 Xcode 报找不到 `LoginSdk` / `LoginSDK`）：
   ```bash
   cd kuikly-login-sdk-demo
   chmod +x gradlew scripts/build-ios-framework.sh
   export JAVA_HOME=$(/usr/libexec/java_home -v 17)
   ./scripts/build-ios-framework.sh
   ```
   Intel Mac 模拟器若失败，可试：`./scripts/build-ios-framework.sh Debug iphonesimulator x86_64`
5. 用 Xcode 打开工程包：
   ```bash
   open ios-host/ios-host.xcodeproj
   ```
6. 选择 **iPhone 模拟器** → **Run (▶)**

首次在 Xcode 内编译仍会执行 Build Phase **「Compile Kotlin Framework」**；若 Framework 已存在会跳过 Gradle，直接编 Swift。

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

### 找不到 `LoginSdk` / `LoginSDK` / `import LoginSdk`

Swift 侧应写 `import LoginSdk`（Framework 名），**不是** `import com.example.login.sdk...`（那是 Kotlin 包名）。

| 现象 | 处理 |
|------|------|
| Swift: `No such module 'LoginSdk'` | Framework 未生成 → 先跑 `./scripts/build-ios-framework.sh` |
| Gradle: `Unresolved reference 'LoginSDK'` | 看完整 Gradle 日志；常见原因：JDK 非 17、无 Android SDK、网络拉依赖失败 |
| Xcode 跳过 Gradle 构建 | 已修复：仅当 Framework **已存在** 时才跳过；删 Framework 后重编 |

```bash
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
chmod +x gradlew
./scripts/build-ios-framework.sh
open ios-host/ios-host.xcodeproj
```

### Build Phase 报 gradlew not found

在工程根目录执行 `gradle wrapper`，确保存在 `gradlew` 和 `gradle/wrapper/gradle-wrapper.jar`。

### Xcode 提示 project is damaged / parse error

1. `git pull` 最新代码（已修复 `project.pbxproj` 中 `${CONFIGURATION}` 等导致解析失败的问题）。
2. 清理本地缓存后重开：
   ```bash
   rm -rf ios-host/ios-host.xcodeproj/project.xcworkspace
   rm -rf ~/Library/Developer/Xcode/DerivedData/ios-host-*
   open ios-host/ios-host.xcodeproj
   ```
3. 不要手动编辑 `project.pbxproj`；Shell 脚本里勿写 `${VAR}` 花括号（须用 `$VAR`）。

### Could not open workspace / contents.xcworkspacedata

1. **务必打开** `ios-host.xcodeproj`（蓝色图标），不要双击 `contents.xcworkspacedata`。
2. 若仍失败，删除本地自动生成的 workspace 后重开：
   ```bash
   rm -rf ios-host/ios-host.xcodeproj/project.xcworkspace
   open ios-host/ios-host.xcodeproj
   ```
3. `git pull` 最新代码（仓库不再提交 `project.xcworkspace`，由 Xcode 本地生成）。

### embedAndSignAppleFrameworkForXcode 失败

- 确认已安装 **Xcode** 与 **Command Line Tools**
- JDK 17：`export JAVA_HOME=$(/usr/libexec/java_home -v 17)`
- 见 [BUILD_TROUBLESHOOTING.md](../docs/BUILD_TROUBLESHOOTING.md)

### 登录页与 Android 不一致

两端共用 `login-sdk/src/commonMain/.../LoginScreen.kt`，若不一致请确认两端依赖同一 commit / 同一 SDK 版本。
