#!/bin/bash
# 在 Xcode 之外预构建 LoginSdk.framework（首次运行 iOS Demo 前建议执行）
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

if command -v /usr/libexec/java_home >/dev/null 2>&1; then
  export JAVA_HOME="${JAVA_HOME:-$(/usr/libexec/java_home -v 17 2>/dev/null || /usr/libexec/java_home)}"
fi

chmod +x ./gradlew

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
else
  echo "error: framework not found at $FRAMEWORK"
  exit 1
fi
