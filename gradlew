#!/bin/sh
#
# Gradle wrapper launcher (LF only).
#
APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD=java
fi

if [ ! -f "$WRAPPER_JAR" ]; then
  echo "error: missing $WRAPPER_JAR" >&2
  exit 1
fi

exec "$JAVACMD" -Xmx64m -Xms64m \
  -classpath "$WRAPPER_JAR" \
  org.gradle.wrapper.GradleWrapperMain "$@"
