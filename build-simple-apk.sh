#!/bin/bash

# MazeWarden 3D Simple APK Build Script
# Builds debug APK without heavy dependencies

echo "🎮 MazeWarden 3D - Simple Build Script"
echo "======================================"

# Set Android SDK path
export ANDROID_HOME=/data/data/com.termux/files/home/android-sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools:$ANDROID_HOME/cmdline-tools/latest/bin

# Build debug APK only (faster)
echo "🔨 Building Debug APK (simplified)..."
./gradlew app:assembleDebug --no-daemon --offline || ./gradlew app:assembleDebug --no-daemon

if [ $? -eq 0 ]; then
    echo "✅ Debug APK built successfully!"
    echo "📍 Location: app/build/outputs/apk/debug/app-debug.apk"
    
    # Copy APK to downloads folder
    echo "📦 Copying APK to downloads..."
    mkdir -p ~/storage/downloads/MazeWarden3D
    cp app/build/outputs/apk/debug/app-debug.apk ~/storage/downloads/MazeWarden3D/MazeWarden3D-debug.apk
    
    echo "🎉 Build complete!"
    echo "📂 APK copied to: ~/storage/downloads/MazeWarden3D/"
    echo "📱 Ready to install: MazeWarden3D-debug.apk"
else
    echo "❌ Build failed!"
    exit 1
fi