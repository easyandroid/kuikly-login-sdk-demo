# 构建排错（Windows / 国内网络）

## 1. `dl.google.com` Read timed out

**现象：**

```
Could not GET 'https://dl.google.com/dl/android/maven2/androidx/compose/...'
Connect to dl.google.com:443 failed: Read timed out
```

**原因：** Gradle 默认从 Google Maven（`dl.google.com`）拉 AndroidX / Compose，国内网络易超时。

**已做配置（本仓库）：**

- `settings.gradle.kts` 中 **阿里云 / 腾讯镜像优先**于 `google()`
- `gradle.properties` 中 HTTP 超时调至 180s

**请你本地操作：**

1. Android Studio → **File → Sync Project with Gradle Files**
2. 若仍失败：**File → Invalidate Caches → Invalidate and Restart**
3. 确认存在 `local.properties`（参考 `local.properties.example`）：
   ```properties
   sdk.dir=D\:\\你的路径\\Android\\Sdk
   ```

**仍超时：**

- 配置公司/本机 HTTP 代理，在 `gradle.properties` 增加：
  ```properties
  systemProp.http.proxyHost=127.0.0.1
  systemProp.http.proxyPort=7890
  systemProp.https.proxyHost=127.0.0.1
  systemProp.https.proxyPort=7890
  ```
- 或在 Android Studio：**Settings → HTTP Proxy** 配置代理后重试 Sync

---

## 2. 缺少 `gradlew.bat` / wrapper

本仓库若只有 `gradlew`（Unix），Windows 建议：

- **优先用 Android Studio 打开工程**（自带 Gradle，不依赖 `gradlew.bat`）
- 或安装 Gradle 后执行：`gradle wrapper`，生成 `gradlew.bat` 与 `gradle-wrapper.jar`

命令行构建（生成 wrapper 后）：

```bat
gradlew.bat :android-host:assembleDebug
```

---

## 3. JDK 版本

- 工程要求 **JDK 17**
- Android Studio：**Settings → Build → Gradle → Gradle JDK** 选 **17**（不要用 21 若遇兼容问题）

---

## 4. 仅编 Android（Windows）

```bat
gradlew.bat :login-sdk:assembleRelease :android-host:assembleDebug
```

**iOS Framework 需 Mac**，Windows 无法链接 iOS 目标。

---

## 5. 相关文档

- [DISTRIBUTION.md](./DISTRIBUTION.md) — 产物与多端依赖  
- [INTEGRATION.md](./INTEGRATION.md) — 接入步骤  
