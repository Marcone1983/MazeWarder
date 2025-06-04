#!/bin/bash

# MazeWarden 3D APK Build Script
# Builds both debug and release APKs

echo "🎮 MazeWarden 3D - Build Script"
echo "==============================="

# Set Android SDK path
export ANDROID_HOME=/data/data/com.termux/files/home/android-sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin

# Clean previous builds
echo "🧹 Cleaning previous builds..."
./gradlew clean

# Build debug APK
echo "🔨 Building Debug APK..."
./gradlew assembleDebug

if [ $? -eq 0 ]; then
    echo "✅ Debug APK built successfully!"
    echo "📍 Location: app/build/outputs/apk/debug/app-debug.apk"
else
    echo "❌ Debug build failed!"
    exit 1
fi

# Build release APK (unsigned)
echo "🔨 Building Release APK..."
./gradlew assembleRelease

if [ $? -eq 0 ]; then
    echo "✅ Release APK built successfully!"
    echo "📍 Location: app/build/outputs/apk/release/app-release-unsigned.apk"
else
    echo "❌ Release build failed!"
    exit 1
fi

# Copy APKs to downloads folder
echo "📦 Copying APKs to downloads..."
mkdir -p ~/storage/downloads/MazeWarden3D
cp app/build/outputs/apk/debug/app-debug.apk ~/storage/downloads/MazeWarden3D/MazeWarden3D-debug.apk
cp app/build/outputs/apk/release/app-release-unsigned.apk ~/storage/downloads/MazeWarden3D/MazeWarden3D-release-unsigned.apk

echo "🎉 Build complete!"
echo "📂 APKs copied to: ~/storage/downloads/MazeWarden3D/"