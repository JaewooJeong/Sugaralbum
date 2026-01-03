# 16KB 메모리 페이지 크기 지원 작업 기록

## 작업 일자
2026-01-03

## 작업 배경
Google Play Console에서 16KB 메모리 페이지 크기 미지원 위반 경고 발생. 2025년 5월까지 수정 필요.
Android 15+ 기기에서 16KB 페이지 크기를 사용하므로 Native 라이브러리들의 정렬이 필요함.

---

## 주요 변경사항

### 1. NDK 버전 업그레이드
- **변경 전**: NDK 25.1.8937393
- **변경 후**: NDK 27.0.12077973
- **이유**: `APP_SUPPORT_FLEXIBLE_PAGE_SIZES` 플래그는 NDK r27 이상에서만 지원

### 2. 아키텍처 변경
- **제거됨**: `armeabi` (NDK r17에서 deprecated, 더 이상 지원 안됨)
- **유지됨**: `armeabi-v7a`, `arm64-v8a`

### 3. 16KB 페이지 크기 링커 플래그 추가
```
-Wl,-z,max-page-size=16384
```

---

## 수정된 파일 목록

### build.gradle 파일들 (5개 모듈)

| 파일 경로 | 변경 내용 |
|-----------|-----------|
| `imageFrameworkLibrary/build.gradle` | ndkVersion 27.0, armeabi 제거 |
| `videoEngine/build.gradle` | ndkVersion 27.0, armeabi 제거 |
| `multimediaFramework/build.gradle` | ndkVersion 27.0, armeabi 제거 |
| `imageAnalysisEngine/build.gradle` | ndkVersion 27.0, armeabi 제거 |
| `plusCameraMultimediaFramework/build.gradle` | ndkVersion 27.0, armeabi 제거 |

```groovy
android {
    ndkVersion '27.0.12077973'
    defaultConfig {
        ndk.abiFilters 'armeabi-v7a', 'arm64-v8a'
    }
}
```

### Application.mk 파일들 (3개)

| 파일 경로 |
|-----------|
| `imageFrameworkLibrary/src/main/jni/Application.mk` |
| `multimediaFramework/src/main/jni/Application.mk` |
| `videoEngine/jni/Application.mk` |

```makefile
APP_ABI := armeabi-v7a arm64-v8a
APP_PLATFORM := android-24
APP_STL := c++_static
APP_SUPPORT_FLEXIBLE_PAGE_SIZES := true
```

### Android.mk 파일들 (2개)

| 파일 경로 |
|-----------|
| `imageFrameworkLibrary/src/main/jni/Android.mk` |
| `multimediaFramework/src/main/jni/Android.mk` |

```makefile
LOCAL_LDFLAGS += -Wl,-z,max-page-size=16384
```

### FFmpeg 빌드 스크립트 (2개)

| 파일 경로 |
|-----------|
| `videoEngine/jni/FFmpeg/build_arm64-v8a.sh` |
| `videoEngine/jni/FFmpeg/build_armeabi-v7a.sh` |

ADDI_LDFLAGS에 추가:
```bash
-Wl,-z,max-page-size=16384
```

### gradle.properties

```properties
# 16KB page size support for Android 15+
android.ndk.maxPageSize=16384
```

---

## JNI 빌드 명령어

### 환경 설정 (Windows)
```batch
set ANDROID_NDK=C:\Users\steve\AppData\Local\Android\Sdk\ndk\27.0.12077973
set PATH=%ANDROID_NDK%;%PATH%
```

### 1. imageFrameworkLibrary
```batch
cd P:\Projects\Projects_SM\sugaralbum\sugaralbum\imageFrameworkLibrary\src\main\jni
ndk-build clean
ndk-build -j8
```

**빌드 결과물:**
- `libs/armeabi-v7a/libnative_filter.so`
- `libs/arm64-v8a/libnative_filter.so`

### 2. videoEngine
```batch
cd P:\Projects\Projects_SM\sugaralbum\sugaralbum\videoEngine\jni
ndk-build clean
ndk-build -j8
```

**빌드 결과물:**
- `libs/armeabi-v7a/libFFmpegProcessor.so`
- `libs/armeabi-v7a/libKwpFFmpegMuxer.so`
- `libs/armeabi-v7a/libavcodec-57.so`
- `libs/armeabi-v7a/libavfilter-6.so`
- `libs/armeabi-v7a/libavformat-57.so`
- `libs/armeabi-v7a/libavutil-55.so`
- `libs/armeabi-v7a/libswresample-2.so`
- `libs/armeabi-v7a/libswscale-4.so`
- `libs/arm64-v8a/libFFmpegProcessor.so`
- `libs/arm64-v8a/libKwpFFmpegMuxer.so`
- `libs/arm64-v8a/libavcodec-57.so`
- `libs/arm64-v8a/libavfilter-6.so`
- `libs/arm64-v8a/libavformat-57.so`
- `libs/arm64-v8a/libavutil-55.so`
- `libs/arm64-v8a/libswresample-2.so`
- `libs/arm64-v8a/libswscale-4.so`

### 3. multimediaFramework
```batch
cd P:\Projects\Projects_SM\sugaralbum\sugaralbum\multimediaFramework\src\main\jni
ndk-build clean
ndk-build -j8
```

**빌드 결과물:**
- `libs/armeabi-v7a/libPixelCanvas.so`
- `libs/armeabi-v7a/libPixelUtils.so`
- `libs/armeabi-v7a/libBeatTracker.so`
- `libs/arm64-v8a/libPixelCanvas.so`
- `libs/arm64-v8a/libPixelUtils.so`
- `libs/arm64-v8a/libBeatTracker.so`

---

## 라이브러리 복사 위치

각 모듈의 빌드 결과물은 해당 모듈의 `src/main/jniLibs/` 폴더로 복사되어야 함:

```
module/
└── src/
    └── main/
        └── jniLibs/
            ├── armeabi-v7a/
            │   └── *.so
            └── arm64-v8a/
                └── *.so
```

---

## 추가 수정 사항

### NativeAdOptions import 수정
**파일:** `app/src/main/java/com/sugarmount/sugarcamera/story/movie/MovieEditMainActivity.java`

```java
// 변경 전 (deprecated)
import com.google.android.gms.ads.formats.NativeAdOptions;

// 변경 후
import com.google.android.gms.ads.nativead.NativeAdOptions;
```

---

## 삭제된 폴더

다음 armeabi 폴더들이 삭제됨 (더 이상 지원되지 않는 아키텍처):
- `videoEngine/jniLibs/armeabi/`
- `imageFrameworkLibrary/src/main/jniLibs/armeabi/`
- `multimediaFramework/src/main/jniLibs/armeabi/`

---

## 미해결 사항

### 1. RenderScript 라이브러리
- **위치:** `imageAnalysisEngine/libs/`, `plusCameraMultimediaFramework/libs/`
- **상태:** .rs 소스 파일 없음 (컴파일된 .so 파일만 존재)
- **조치 필요:** 다른 기술로 대체 필요 (RenderScript는 Android 12+에서 deprecated)

### 2. libfacialproc_jni.so
- **위치:** `imageAnalysisEngine/libs/armeabi-v7a/`
- **상태:** 외부 SDK로 소스 없음, arm64-v8a 버전 없음
- **조치 필요:** SDK 제공업체에 16KB 호환 버전 요청

---

## 테스트 방법

1. 앱 빌드 완료 후 APK/AAB 생성
2. Google Play Console 내부 테스트 트랙에 업로드
3. 16KB 페이지 크기 지원 여부 자동 검증됨

---

## 참고 자료

- [Android 16KB 페이지 크기 지원 가이드](https://developer.android.com/guide/practices/page-sizes)
- [NDK r27 릴리스 노트](https://github.com/android/ndk/wiki/Changelog-r27)
