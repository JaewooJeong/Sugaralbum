#!/bin/bash
# JNI 전체 빌드 및 jniLibs 복사 스크립트 (Windows Git Bash용)

set -e

# NDK 경로 설정
#NDK_PATH="/c/Android/Sdk/ndk/29.0.14206865"
#NDK_PATH="/c/Android/Sdk/ndk/27.0.12077973"
NDK_PATH="/c/Android/Sdk/ndk/26.3.11579264"
#NDK_PATH="/c/Android/Sdk/ndk/25.1.8937393"



PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"

echo "========================================"
echo "JNI 전체 빌드 스크립트"
echo "NDK: $NDK_PATH"
echo "Project: $PROJECT_ROOT"
echo "========================================"

# 1. imageFrameworkLibrary
echo ""
echo "[1/3] imageFrameworkLibrary 빌드 중..."
cd "$PROJECT_ROOT/imageFrameworkLibrary/src/main/jni"

# 기존 빌드 결과물 삭제
rm -rf ../libs ../obj
rm -rf ../jniLibs/arm64-v8a/* ../jniLibs/armeabi-v7a/*

$NDK_PATH/ndk-build.cmd clean
$NDK_PATH/ndk-build.cmd -j8

mkdir -p ../jniLibs/arm64-v8a ../jniLibs/armeabi-v7a
cp ../libs/arm64-v8a/*.so ../jniLibs/arm64-v8a/
cp ../libs/armeabi-v7a/*.so ../jniLibs/armeabi-v7a/
echo "[1/3] imageFrameworkLibrary 완료"

# 2. multimediaFramework
echo ""
echo "[2/3] multimediaFramework 빌드 중..."
cd "$PROJECT_ROOT/multimediaFramework/src/main/jni"

# 기존 빌드 결과물 삭제
rm -rf ../libs ../obj
rm -rf ../jniLibs/arm64-v8a/* ../jniLibs/armeabi-v7a/*

$NDK_PATH/ndk-build.cmd clean
$NDK_PATH/ndk-build.cmd -j8

mkdir -p ../jniLibs/arm64-v8a ../jniLibs/armeabi-v7a
cp ../libs/arm64-v8a/*.so ../jniLibs/arm64-v8a/
cp ../libs/armeabi-v7a/*.so ../jniLibs/armeabi-v7a/
echo "[2/3] multimediaFramework 완료"

# 3. videoEngine
echo ""
echo "[3/3] videoEngine 빌드 중..."
cd "$PROJECT_ROOT/videoEngine/src/main/jni"

# 기존 빌드 결과물 삭제
rm -rf ../libs ../obj
rm -rf ../jniLibs/arm64-v8a/* ../jniLibs/armeabi-v7a/*

$NDK_PATH/ndk-build.cmd clean
$NDK_PATH/ndk-build.cmd -j8

mkdir -p ../jniLibs/arm64-v8a ../jniLibs/armeabi-v7a
cp ../libs/arm64-v8a/*.so ../jniLibs/arm64-v8a/
cp ../libs/armeabi-v7a/*.so ../jniLibs/armeabi-v7a/
echo "[3/3] videoEngine 완료"

echo ""
echo "========================================"
echo "전체 빌드 완료!"
echo "========================================"
echo ""
echo "빌드된 파일 확인:"
echo "--- imageFrameworkLibrary ---"
ls -la "$PROJECT_ROOT/imageFrameworkLibrary/src/main/jniLibs/arm64-v8a/"
echo "--- multimediaFramework ---"
ls -la "$PROJECT_ROOT/multimediaFramework/src/main/jniLibs/arm64-v8a/"
echo "--- videoEngine ---"
ls -la "$PROJECT_ROOT/videoEngine/src/main/jniLibs/arm64-v8a/"
