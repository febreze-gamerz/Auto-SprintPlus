#!/usr/bin/env bash
set -euo pipefail

MC_VERSION="${1:-}"

if [[ -z "$MC_VERSION" ]]; then
  echo "Usage: ./build-version.sh <minecraft-version>"
  echo
  echo "Available profiles:"
  find versions -maxdepth 1 -type f -name '*.properties' -printf '  %f\n' 2>/dev/null | sort
  exit 1
fi

if [[ ! -f "versions/${MC_VERSION}.properties" ]]; then
  echo "Unknown Minecraft profile: ${MC_VERSION}"
  echo "Create versions/${MC_VERSION}.properties first."
  exit 1
fi

echo "Building Auto Sprint+ for Minecraft ${MC_VERSION}..."
echo

if [[ -x "./gradlew" && -f "gradle/wrapper/gradle-wrapper.jar" ]]; then
  ./gradlew clean build -Ptarget_mc="${MC_VERSION}"
elif command -v gradle >/dev/null 2>&1; then
  gradle clean build -Ptarget_mc="${MC_VERSION}"
elif [[ -x "$HOME/Documents/gradle-9.7.1/bin/gradle" ]]; then
  "$HOME/Documents/gradle-9.7.1/bin/gradle" clean build -Ptarget_mc="${MC_VERSION}"
else
  echo "Gradle was not found."
  echo "Install/use Gradle 9.x, or restore gradle/wrapper/gradle-wrapper.jar."
  exit 1
fi
