#!/bin/sh
#
# Minimal Gradle wrapper launcher (LF only). Avoids CRLF / DEFAULT_JVM_OPTS parse issues on macOS.
#
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD=java
fi

WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$WRAPPER_JAR" ]; then
  echo "error: missing $WRAPPER_JAR" >&2
  echo "Run: gradle wrapper   (or reinstall from repo)" >&2
  exit 1
fi

exec "$JAVACMD" -Xmx64m -Xms64m -jar "$WRAPPER_JAR" "$@"
