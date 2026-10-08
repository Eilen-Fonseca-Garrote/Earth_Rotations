#!/bin/sh
set -eu

backend_dir=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)

if [ "$(uname -s)" = Darwin ] && [ -x /usr/libexec/java_home ]; then
  java_25_home=$(/usr/libexec/java_home -v 25 2>/dev/null || true)
  if [ -n "$java_25_home" ]; then
    JAVA_HOME=$java_25_home
    export JAVA_HOME
  fi
fi

if [ -z "${JAVA_HOME:-}" ]; then
  echo "Java 25 is required. Install JDK 25 or set JAVA_HOME to its installation directory." >&2
  exit 1
fi

if [ ! -x "$JAVA_HOME/bin/java" ] || [ ! -x "$JAVA_HOME/bin/javac" ]; then
  echo "JAVA_HOME must point to a JDK containing bin/java and bin/javac: $JAVA_HOME" >&2
  exit 1
fi

java_version=$("$JAVA_HOME/bin/java" -version 2>&1 | sed -n '1s/.*"\([^"]*\)".*/\1/p')
java_major=$(printf '%s\n' "$java_version" | sed 's/^\([0-9][0-9]*\).*/\1/')
if [ -z "$java_major" ] || [ "$java_major" -lt 25 ]; then
  echo "Java 25 or newer is required; JAVA_HOME currently uses ${java_version:-an unknown version}." >&2
  echo "On macOS, install JDK 25. This script will select it automatically." >&2
  exit 1
fi

PATH="$JAVA_HOME/bin:$PATH"
export PATH

printf 'Starting Earthquakes API with '
"$JAVA_HOME/bin/java" -version 2>&1 | sed -n '1p'
exec "$backend_dir/mvnw" -f "$backend_dir/pom.xml" spring-boot:run "$@"
