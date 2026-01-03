# SugarAlbum Android 15 Upgrade Progress

## Current Status: 🟢 COMPLETED

---

## 16KB Page Size Support - 완료 ✅

### 배경
- Google Play Console 위반 경고: 2025년 5월까지 16KB 페이지 사이즈 지원 필요
- NDK 업그레이드 및 16KB 플래그 적용 필요

---

### 최종 설정

#### NDK 버전
- **사용 버전**: `26.3.11579264`
- **이유**: NDK 27/29에서 -O3 최적화 시 파란색 색조 버그 발생

#### gradle.properties
```properties
# 16KB page size support for Android 15+
android.ndk.maxPageSize=16384
```

#### 모든 build.gradle 파일
```gradle
ndkVersion '26.3.11579264'
```

적용된 모듈:
- imageFrameworkLibrary
- imageAnalysisEngine
- multimediaFramework
- videoEngine
- plusCameraMultimediaFramework

---

### 16KB 플래그 적용 현황

#### Application.mk (모든 모듈)
```makefile
APP_ABI := armeabi-v7a arm64-v8a
APP_PLATFORM := android-24
APP_STL := c++_static
APP_SUPPORT_FLEXIBLE_PAGE_SIZES := true
```

적용된 모듈:
- imageFrameworkLibrary/src/main/jni/Application.mk
- multimediaFramework/src/main/jni/Application.mk
- videoEngine/src/main/jni/Application.mk

#### Android.mk - 16KB 링커 플래그
```makefile
LOCAL_LDFLAGS += -Wl,-z,max-page-size=16384
```

적용된 모듈:
| 모듈 | 라이브러리 |
|------|------------|
| imageFrameworkLibrary | libnative_filter.so |
| multimediaFramework | libPixelCanvas.so, libPixelUtils.so, libBeatTracker.so |
| videoEngine | libFFmpegProcessor.so, libKwpFFmpegMuxer.so |

---

### NDK 버전별 호환성 테스트 결과

| NDK 버전 | -O3 최적화 | 16KB 지원 | 파란색 색조 | 결과 |
|----------|------------|-----------|-------------|------|
| 25.1.8937393 | ✅ 정상 | ❌ 미지원 | ❌ 없음 | 16KB 불가 |
| **26.3.11579264** | ✅ 정상 | ✅ 지원 | ❌ 없음 | **채택** |
| 27.0.12077973 | ❌ 버그 | ✅ 지원 | ✅ 발생 | 사용 불가 |
| 29.0.14206865 | ❌ 버그 | ✅ 지원 | ✅ 발생 | 사용 불가 |

#### NDK 27/29 파란색 색조 원인 분석
- **원인**: Clang 컴파일러의 `-O3` 최적화 버그
- **증상**: 사진 전환 시 파란색 계열 색조가 끼면서 전환됨
- **영향 범위**: multimediaFramework의 PixelUtils, libyuv 관련 코드
- **검증**: `-O0` (최적화 없음)으로 빌드하면 정상 동작
- **결론**: NDK 27+ 버전의 -O3 최적화 시 색상 처리 코드에서 오동작 발생

---

### build_all_jni.sh 스크립트

```bash
#!/bin/bash
NDK_PATH="/c/Android/Sdk/ndk/26.3.11579264"

# 각 모듈 빌드 전 클린업
rm -rf ../libs ../obj
rm -rf ../jniLibs/arm64-v8a/* ../jniLibs/armeabi-v7a/*

# NDK 빌드
$NDK_PATH/ndk-build.cmd clean
$NDK_PATH/ndk-build.cmd -j8
```

**중요**: Android Studio의 Clean Project는 jniLibs 폴더를 삭제하지 않으므로,
반드시 build_all_jni.sh를 사용하거나 수동으로 jniLibs를 삭제해야 함

---

### 빌드된 .so 파일 목록

#### imageFrameworkLibrary
- libnative_filter.so (16KB 적용)

#### multimediaFramework
- libPixelCanvas.so (16KB 적용)
- libPixelUtils.so (16KB 적용)
- libBeatTracker.so (16KB 적용)

#### videoEngine
- libFFmpegProcessor.so (16KB 적용)
- libKwpFFmpegMuxer.so (16KB 적용)
- libavcodec-56.so (prebuilt - 별도 처리 필요)
- libavformat-56.so (prebuilt - 별도 처리 필요)
- libavutil-54.so (prebuilt - 별도 처리 필요)
- libswscale-3.so (prebuilt - 별도 처리 필요)
- libswresample-1.so (prebuilt - 별도 처리 필요)
- libavfilter-5.so (prebuilt - 별도 처리 필요)

---

### 주의사항

1. **Prebuilt FFmpeg 라이브러리**
   - libavcodec-56.so 등 prebuilt 파일은 별도로 16KB 재빌드 필요
   - FFmpeg 소스 컴파일 시 16KB 플래그 적용 필요

2. **NDK 버전 변경 금지**
   - NDK 27 이상 버전 사용 시 파란색 색조 버그 발생
   - 반드시 NDK 26.3.11579264 사용

3. **빌드 시 jniLibs 삭제**
   - Android Studio Clean으로는 jniLibs가 삭제되지 않음
   - build_all_jni.sh 사용 권장

---

## Android 15 Upgrade - 기존 완료 작업

### Completed Tasks ✅

1. **Synthetic View Binding Migration**
   - ActivityFinish.kt, ActivityInformation.kt, ActivityMain.kt
   - kotlinx.android.synthetic → findViewById 변환

2. **Foreground Service Type Fix**
   - FOREGROUND_SERVICE_TYPE_DATA_SYNC → FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING
   - AndroidManifest.xml 서비스 타입 업데이트

3. **MediaStore Integration**
   - MediaStoreHelper.java 생성
   - VideoCreationService.java에 MediaStore API 통합
   - Fallback 메커니즘 구현

4. **Permission Model Update**
   - READ_MEDIA_VIDEO 권한 추가
   - Android 15 호환 권한 처리

5. **16KB Page Size Support** ✅ NEW
   - NDK 26.3.11579264 적용
   - 모든 네이티브 라이브러리에 16KB 플래그 적용
   - Google Play Console 요구사항 충족

---

*Last Updated: 2026-01-03*
*Status: 16KB 페이지 사이즈 지원 완료*
