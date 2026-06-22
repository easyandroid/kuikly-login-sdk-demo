#!/bin/bash
# 在 Xcode 之外预构建 LoginSdk.framework（首次运行 iOS Demo 前建议执行）
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

die() {
  echo "error: $*" >&2
  exit 1
}

# --- JDK 17 ---
if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  if /usr/libexec/java_home -v 17 >/dev/null 2>&1; then
    export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
  fi
fi

if [ -z "${JAVA_HOME:-}" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
  for candidate in \
    "/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home" \
    "/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home"; do
    if [ -x "$candidate/bin/java" ]; then
      export JAVA_HOME="$candidate"
      break
    fi
  done
fi

if [ -z "${JAVA_HOME:-}" ] || [ ! -x "$JAVA_HOME/bin/java" ]; then
  die "未找到 JDK 17。请执行: brew install openjdk@17
然后: export JAVA_HOME=\$(/usr/libexec/java_home -v 17)"
fi

JAVA_VERSION=$("$JAVA_HOME/bin/java" -version 2>&1 | head -n 1)
echo "Using JAVA_HOME=$JAVA_HOME"
echo "Java: $JAVA_VERSION"
case "$JAVA_VERSION" in
  *\"17.*|*\"21.*) ;;
  *)
    echo "warning: 推荐使用 JDK 17（当前可能不兼容）"
    ;;
esac

# --- Android SDK ---
if [ ! -f "$ROOT/local.properties" ]; then
  if [ -d "$HOME/Library/Android/sdk" ]; then
    echo "sdk.dir=$HOME/Library/Android/sdk" > "$ROOT/local.properties"
    echo "Created local.properties"
  else
    die "未找到 Android SDK。请先安装 Android Studio（装完不必打开）:
  https://developer.android.com/studio
安装后 SDK 通常在 ~/Library/Android/sdk
然后执行: echo \"sdk.dir=\$HOME/Library/Android/sdk\" >> local.properties"
  fi
fi

chmod +x ./gradlew
# 去掉可能存在的 Windows 换行
sed -i '' 's/\r$//' ./gradlew 2>/dev/null || sed -i 's/\r$//' ./gradlew 2>/dev/null || true

CONFIG="${1:-Debug}"
SDK_NAME="${2:-iphonesimulator}"
ARCHS="${3:-arm64}"

echo "Building LoginSdk.framework ($CONFIG / $SDK_NAME / $ARCHS) ..."
./gradlew :login-sdk:embedAndSignAppleFrameworkForXcode \
  -PXCODE_CONFIGURATION="$CONFIG" \
  -PXCODE_SDK_NAME="$SDK_NAME" \
  -PXCODE_ARCHS="$ARCHS" \
  --no-daemon

FRAMEWORK="$ROOT/login-sdk/build/xcode-frameworks/$CONFIG/$SDK_NAME/LoginSdk.framework"
if [ -d "$FRAMEWORK" ]; then
  echo "OK: $FRAMEWORK"
  echo ""
  echo "下一步: open ios-host/ios-host.xcodeproj"
else
  die "Gradle 已结束但未找到 Framework: $FRAMEWORK"
fi
