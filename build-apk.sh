#!/bin/bash
# Build script for ETV Go
# Uses Gradle (requires x86-64 environment with Android SDK)
set -e
cd "$(dirname "$0")"
echo "Building ETV Go..."
./gradlew :app:assembleDebug
echo "APK built at: app/build/outputs/apk/debug/app-debug.apk"
