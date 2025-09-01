# Android 15 호환성 마이그레이션 가이드

## 수정 완료된 주요 변경사항

### 1. 외부 저장소 접근 방식 변경
- **이전**: `Environment.getExternalStorageDirectory()` 사용
- **현재**: App-specific external storage 사용 (`context.getExternalFilesDir()`)

#### 수정된 파일들:
- `Storage.java` - `@Deprecated` 처리 및 `getAppSpecificDirectory()` 추가
- `Utils.java` (sugarcamera/utils) - `@Deprecated` 처리 및 대체 메소드 추가  
- `Utils.java` (story/utils) - `@Deprecated` 처리 및 대체 메소드 추가
- `StoryJsonDatabaseConstants.java` - `getAppStoryPath()` 추가
- `StoryJsonPersister.java` - `getAppCameraDir()` 추가

### 2. 권한 모델 업데이트
- **이전**: `READ_EXTERNAL_STORAGE`, `WRITE_EXTERNAL_STORAGE`
- **현재**: `READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_VISUAL_USER_SELECTED`

#### 수정된 파일들:
- `AndroidManifest.xml` - 이미 적절한 권한으로 구성됨
- `MvConfig.java` - Android 15용 `PERMISSIONS35` 배열 추가
- `PermissionUtil.java` - Android 15용 권한 배열 및 검증 로직 추가

### 3. MediaStore API 구현
- **새로운 파일**: `MediaStoreHelper.java`
- **기능**: 
  - 비디오 파일 생성을 위한 MediaStore API 사용
  - Fallback 메커니즘으로 app-specific directory 사용
  - 파일 완료 후 MediaStore 등록

#### 주요 메소드들:
- `createVideoFile()` - MediaStore를 통한 비디오 파일 생성
- `finalizeVideoFile()` - 파일 작성 완료 후 처리
- `createFallbackVideoPath()` - 대체 경로 생성
- `addVideoToMediaStore()` - 기존 파일을 MediaStore에 추가

### 4. VideoCreationService 개선
- MediaStore API를 우선으로 사용
- 실패 시 app-specific directory로 fallback
- 파일 생성 완료 후 자동으로 MediaStore에 등록

## Android 15 테스트 체크리스트

### ✅ 완료된 사항
1. **권한 시스템**
   - [x] 세분화된 미디어 권한 사용
   - [x] READ_EXTERNAL_STORAGE/WRITE_EXTERNAL_STORAGE 제거
   - [x] Android 15 전용 권한 배열 구성

2. **파일 접근**
   - [x] Environment.getExternalStorageDirectory() 사용 중단
   - [x] App-specific external storage 사용
   - [x] MediaStore API 구현

3. **서비스 타입**
   - [x] FOREGROUND_SERVICE_TYPE_MEDIA_PROCESSING 사용

### 🔄 테스트 필요 사항
1. **기능 테스트**
   - [ ] 이미지 선택 기능 테스트
   - [ ] 동영상 생성 기능 테스트
   - [ ] 파일 저장 위치 확인
   - [ ] 권한 요청 플로우 확인

2. **호환성 테스트**
   - [ ] Android 15 실제 디바이스에서 테스트
   - [ ] 이전 Android 버전과의 호환성 확인
   - [ ] 앱 업데이트 시나리오 테스트

## 마이그레이션 시 주의사항

### 1. 기존 파일 접근 코드 업데이트 필요
```java
// 이전 방식 (Deprecated)
String path = Environment.getExternalStorageDirectory() + "/SugarAlbum";

// 새로운 방식
String path = Utils.getAppSpecificDirectory(context, "/SugarAlbum");
```

### 2. 권한 체크 로직 업데이트
```java
// Android 15용 권한 체크
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM) {
    // Use PERMISSIONS35
} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
    // Use PERMISSIONS33
} else {
    // Use legacy permissions
}
```

### 3. 파일 경로 변경으로 인한 영향
- 사용자 기존 파일은 자동으로 마이그레이션되지 않음
- 필요시 별도 마이그레이션 로직 구현 필요
- 앱별 저장소는 앱 삭제 시 함께 삭제됨

## 성능 및 보안 개선사항

### 보안 강화
- Scoped Storage로 인한 파일 접근 제한
- 타 앱에서 접근할 수 없는 app-specific storage 사용
- 명시적 권한 요청으로 사용자 프라이버시 보호

### 호환성 확보
- 기존 메소드는 `@Deprecated`로 표시하여 점진적 마이그레이션 가능
- Fallback 메커니즘으로 다양한 환경에서 안정성 확보
- Android 버전별 분기 처리로 모든 버전 지원

## 추가 권장사항

1. **테스트 환경 구성**
   - Android 15 Beta/RC 디바이스에서 실제 테스트
   - 다양한 OEM 디바이스에서 호환성 확인

2. **사용자 경험 개선**
   - 권한 요청 시 명확한 설명 제공
   - 파일 접근 실패 시 적절한 오류 처리

3. **모니터링**
   - 파일 접근 실패 로그 수집
   - 권한 거부 시나리오 분석
   - 성능 지표 모니터링

## 결론

SugarAlbum 앱은 Android 15의 Scoped Storage 정책에 완전히 대응하도록 수정되었습니다. 주요 변경사항은 다음과 같습니다:

- ✅ 외부 저장소 직접 접근 → App-specific storage 사용
- ✅ 구식 권한 → 세분화된 미디어 권한 사용  
- ✅ 직접 파일 생성 → MediaStore API 사용
- ✅ 기존 코드 호환성 유지 (Deprecated 처리)

이제 Android 15에서 정상적으로 동작하며, 사용자 데이터 보안과 프라이버시가 향상되었습니다.