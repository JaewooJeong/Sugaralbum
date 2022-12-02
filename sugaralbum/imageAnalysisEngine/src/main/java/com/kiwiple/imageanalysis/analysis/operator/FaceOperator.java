
package com.kiwiple.imageanalysis.analysis.operator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.media.FaceDetector.Face;

import com.kiwiple.imageanalysis.analysis.ImageAnalysis;
import com.kiwiple.imageanalysis.correct.collage.FaceInfomation;
import com.kiwiple.imageanalysis.database.FacePoint;
import com.kiwiple.imageanalysis.database.FacePointF;
import com.kiwiple.imageanalysis.database.FaceRect;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.database.ImageFaceData;
import com.kiwiple.imageanalysis.utils.BitmapUtils;
import com.kiwiple.imageanalysis.utils.SmartLog;
import com.qualcomm.snapdragon.sdk.deviceinfo.DeviceInfo;
import com.qualcomm.snapdragon.sdk.face.FaceData;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.FEATURE_LIST;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing.FP_MODES;
import com.qualcomm.snapdragon.sdk.face.FacialProcessingConstants;

/**
 * 이미지 얼굴 분석 클래스
 */
public class FaceOperator {

    private static final String TAG = FaceOperator.class.getSimpleName();

    /**
     * SnapDragon SDK FaceProcessing 사용 가능 여부
     */
    public static final boolean IS_SUPPORT_SNAPDRAGON_FACE_PROCESSING = getIsSupportedSnapdragonFaceProcessing();
    /**
     * SnapDragon SDK FaceRecognition 사용 가능 여부
     */
    public static final boolean IS_SUPPORT_SNAPDRAGON_FACE_RECOGNITION = getIsSupportedSnapdragonFaceRecognition();
    public static final String DEFAULT_PERSON_NAME = "이름없음";

    private static final int NONE_DEFAULT_VALUE = -1000;
    public static final int NONE_PERSON_ID_VALUE = -111;
    public static final int DELETE_PERSON_ID_VALUE = -112;

    private static FaceOperator mInstance;
    private Context mContext;

    // Face Detector For Snapdragon SDK
    private static FacialProcessing mFacialProcessing;
    // 인물 정보를 저장하는 키값
    public static final String FACE_RECOGNITION_HASH_KEY = "Recognition";
    // 인물 인식 관련 정보를 저장할 SharedPreference 이름
    public static final String SHARED_PREFERENCE_NAME_FACIAL_ALBUM_GROUP = "facial_album_group";
    // 추가한 인물 인식 앨범 그룹의 개수를 가져오기 위한 key 값
    public static final String SHARED_PREFERENCE_KEY_FACIAL_ALBUM_GROUP_COUNT = "facial_album_group_count";
    // 인물 인식 앨범 그룹의 데이터를 가져오기 위한 key의 prefix 값. n번째 앨범의 key 값:
    // SHARED_PREFERENCE_KEY_FACIAL_ALBUM_GROUP_DATA_PREFIX+n
    public static final String SHARED_PREFERENCE_KEY_FACIAL_ALBUM_GROUP_DATA_PREFIX = "facial_album_group_data_prefix";

    public static final int FACE_CONFIDENCE_VALUE = 58;
    public static HashMap<String, String> sPersonNameHash;

    // Face Detector For Android SDK
    private static final int FACE_MAXIMUM_COUNT = 5;
    private FaceDetector.Face[] mFaceObjects = new FaceDetector.Face[FACE_MAXIMUM_COUNT];

    // Bitmap 디코딩 옵션 (이미지의 크기를 알아내기 위한 변수)
    private BitmapFactory.Options mCheckBitmapOptions = new BitmapFactory.Options();

    private boolean mIsAutoFaceRecognition = false;

    // 추가 가능한 최대 그룹 개수. 최대 인물 개수는 MAX_FACIAL_ALBUM_GROUP_COUNT x MAX_FACIAL_ALBUM_PERSON_COUNT
    private static final int MAX_FACIAL_ALBUM_GROUP_COUNT = 50;
    // 앨범 그룹 당 등록 가능한 최대 인물 개수
    private static final int MAX_FACIAL_ALBUM_PERSON_COUNT = 1;
    // 현재 로드되어 있는 앨범 그룹의 index
    private int mCurrentFacialAlbumGroupIndex = -1;
    // 앨범 그룹의 데이터
    private List<String> mFacialAlbumGroupData = new ArrayList<String>();

    /**
     * 인물 처리를 위한 싱글톤 생성자.<br>
     * FacialProcessing의 경우 static으로 하나만 유지되어야함.
     * 
     * @param ctx Context
     * @return FaceOperator 인스턴스
     */
    public static FaceOperator getInstance(Context ctx) {
        if(mInstance == null) {
            mInstance = new FaceOperator(ctx);
        }
        return mInstance;
    }

    /**
     * 싱글톤 해제
     */
    public static void release() {
        if(mFacialProcessing != null) {
            mFacialProcessing.release();
            mFacialProcessing = null;
        }
        mInstance = null;
    }

    /**
     * 단말이 Snapdragon SDK 의 Face Processing을 지원하는지 여부 체크
     * 
     * @return boolean 단말의 Face Processing 지원가능 여부
     */
    private static boolean getIsSupportedSnapdragonFaceProcessing() {
        return DeviceInfo.isSnapdragon()
                && FacialProcessing.isFeatureSupported(FEATURE_LIST.FEATURE_FACIAL_PROCESSING);
    }

    /**
     * 단말이 Snapdragon SDK의 Face Recognition을 지원하는지 여부 체크
     * 
     * @return boolean 단말의 Face Recognition 지원 가능 여부
     */
    private static boolean getIsSupportedSnapdragonFaceRecognition() {
        return FacialProcessing.isFeatureSupported(FEATURE_LIST.FEATURE_FACIAL_RECOGNITION);
    }

    private FaceOperator(Context ctx) {
        mContext = ctx;
        if(IS_SUPPORT_SNAPDRAGON_FACE_PROCESSING && IS_SUPPORT_SNAPDRAGON_FACE_RECOGNITION) {
            SmartLog.e(TAG, "ok snapdragon");
            getFacialInstance();
            loadFaceRecognitionAlbumGroup();
            sPersonNameHash = retrieveFaceRecognitionHash();
            mCheckBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
        } else {
            SmartLog.e(TAG, "no snapdragon");
            mCheckBitmapOptions.inPreferredConfig = Bitmap.Config.RGB_565;
        }
        mCheckBitmapOptions.inJustDecodeBounds = true;
    }

    /**
     * FacialProcessing 객체를 반환한다.<br>
     * 해당 객체의 경우 앱의 단 1개의 객체만이 존재해야하므로 해당 메소드를 통해서만 얻어오자.
     * 
     * @return FacialProcessing 생성된 FacialProcessing 객체
     */
    public static FacialProcessing getFacialInstance() {
        if(mFacialProcessing == null && getIsSupportedSnapdragonFaceProcessing()
                && getIsSupportedSnapdragonFaceRecognition()) {
            mFacialProcessing = FacialProcessing.getInstance();
        }
        return mFacialProcessing;
    }

    /**
     * 자동 주인공 설정 여부
     * 
     * @param isAutoFaceRecognition 자동 주인공 기능 사용 여부
     * @param protagonistCount 자동 주인공 사용시 서칭할 인물 수 (최대값 15)
     */
    public void setIsAutoFaceRecognition(boolean isAutoFaceRecognition, int protagonistCount) {
        mIsAutoFaceRecognition = isAutoFaceRecognition;
        // addPerson에 대한 limit은 없으나, SDK 버그로 15개까지만 추가 가능
        if(protagonistCount > MAX_FACIAL_ALBUM_PERSON_COUNT * MAX_FACIAL_ALBUM_GROUP_COUNT
                || protagonistCount < 0) {
            protagonistCount = MAX_FACIAL_ALBUM_PERSON_COUNT * MAX_FACIAL_ALBUM_GROUP_COUNT;
        }
        if(protagonistCount < 0) {
            protagonistCount = 0;
        }
        if(mIsAutoFaceRecognition && IS_SUPPORT_SNAPDRAGON_FACE_RECOGNITION) {
            getFacialInstance().setRecognitionConfidence(FACE_CONFIDENCE_VALUE);
            getFacialInstance().setProcessingMode(FP_MODES.FP_MODE_STILL);
        } else {
            mIsAutoFaceRecognition = false;
        }
    }

    /**
     * 이미지 데이터 정보에 "인물"에 관련된 정보를 추가한다.
     * 
     * @param itemObject 처리할 사진정보
     */
    public void operatingFaceProcessing(ImageData itemObject) {

        // 사용자에 의한 삭제 데이터가 포함되어있는지 여부(포함되어있다면 재분석하지 않는다)
        boolean isFaceDeletePicture = false;
        if(itemObject.faceDataItems != null) {
            for(ImageFaceData faceData : itemObject.faceDataItems) {
                if(faceData.personId == DELETE_PERSON_ID_VALUE) {
                    isFaceDeletePicture = true;
                }

                if(isFaceDeletePicture) {
                    break;
                }
            }
        }

        // 분석하지 않는다.
        if(isFaceDeletePicture) {
            return;
        }

        // 가상 디코딩을 통해 이미지 사이즈를 알아냄
        Bitmap bmp = getBitmapFromImageData(itemObject);
        if(bmp != null && bmp.getWidth() >= 100 && bmp.getHeight() >= 100) {
            int originalWidth = itemObject.width;
            int originalHeight = itemObject.height;
            if("90".equals(itemObject.orientation) || "270".equals(itemObject.orientation)) {
                int tmp = originalWidth;
                originalWidth = originalHeight;
                originalHeight = tmp;
            }

            final int heightRatio = (int)Math.ceil((float)originalHeight / (float)bmp.getHeight());
            final int widthRatio = (int)Math.ceil((float)originalWidth / (float)bmp.getWidth());
            float scale = heightRatio < widthRatio ? heightRatio : widthRatio;
            itemObject.faceBitmapWidth = bmp.getWidth();
            itemObject.faceBitmapHeight = bmp.getHeight();
            itemObject.faceBitmapScale = scale;
            // 얼굴 데이터를 넣는다
            if(IS_SUPPORT_SNAPDRAGON_FACE_PROCESSING) {
                boolean analyzed = getFacialInstance().setBitmap(bmp);
                if(!analyzed) {
                    SmartLog.e(TAG, "fail to face detecting");
                    itemObject.numberOfFace = 0;
                } else {
                    // 얼굴 데이터 셋팅
                    setFaceDataItems(itemObject, getFacialInstance().getFaceData());
                }
            } else {
                mFaceObjects = new FaceDetector.Face[FACE_MAXIMUM_COUNT];
                FaceDetector faceDetector = new FaceDetector(bmp.getWidth(), bmp.getHeight(),
                                                             FACE_MAXIMUM_COUNT);
                itemObject.numberOfFace = faceDetector.findFaces(bmp, mFaceObjects);
                setFaceDataItems(itemObject, mFaceObjects);
            }
            bmp.recycle();
        } else {
            itemObject.numberOfFace = 0;
        }
    }

    /**
     * 이미지 데이터 정보에 "인물"에 관련된 정보를 추가한다.
     * 
     * @param bmp 처리할 사진정보
     */
    public void operatingFaceProcessing(Bitmap bmp, ImageAnalysis.IFrameData iFrameData) {
        if(bmp == null || bmp.isRecycled()) {
            return;
        }
        // 얼굴 데이터를 넣는다
        if(IS_SUPPORT_SNAPDRAGON_FACE_PROCESSING) {
            boolean analyzed = getFacialInstance().setBitmap(bmp);
            if(analyzed) {
                // 얼굴 갯수
                iFrameData.numberOfFace = getFacialInstance().getNumFaces();
            }
        } else {
            mFaceObjects = new FaceDetector.Face[FACE_MAXIMUM_COUNT];
            FaceDetector faceDetector = new FaceDetector(bmp.getWidth(), bmp.getHeight(),
                                                         FACE_MAXIMUM_COUNT);
            iFrameData.numberOfFace = faceDetector.findFaces(bmp, mFaceObjects);
        }
    }

    // 얼굴 데이터를 각각 추출하여 넣는다
    // only Snapdragon SDK
    private void setFaceDataItems(ImageData itemObject, FaceData[] faceDatas) {
        if(faceDatas == null || faceDatas.length < 1) {
            // 얼굴 갯수
            itemObject.numberOfFace = 0;
            return;
        }

        int faceCount = faceDatas.length;
        // 얼굴 갯수
        itemObject.numberOfFace = faceCount;

        int sumSmileValue = 0;
        int sumLeftBlinkValue = 0;
        int sumRightBlinkValue = 0;

        itemObject.faceDataItems = new ArrayList<ImageFaceData>();
        String faceDataKey = null;
        boolean isFaceRecognition = true;
        for(int i = 0; i < faceCount; i++) {
            FaceData data = faceDatas[i];
            ImageFaceData faceData = new ImageFaceData();
            faceDataKey = itemObject.id + "," + i;
            SmartLog.e(TAG, "FaceDataKey : " + faceDataKey);
            faceData.faceDataKey = faceDataKey;
            faceData.imageId = itemObject.id;
            faceData.faceIndex = i;
            faceData.isUpdatePerson = false;
            faceData.leftEyePoint = new FacePoint(data.leftEye.x, data.leftEye.y);
            faceData.rightEyePoint = new FacePoint(data.rightEye.x, data.rightEye.y);
            faceData.mouthPoint = new FacePoint(data.mouth.x, data.mouth.y);
            faceData.faceDetectRect = new FaceRect(data.rect);
            faceData.faceRect = new FaceRect((int)(data.rect.left * itemObject.faceBitmapScale),
                                             (int)(data.rect.top * itemObject.faceBitmapScale),
                                             (int)(data.rect.right * itemObject.faceBitmapScale),
                                             (int)(data.rect.bottom * itemObject.faceBitmapScale));
            faceData.faceDetectScale = itemObject.faceBitmapScale;
            faceData.eyeGazePoint = new FacePointF(data.getEyeGazePoint().x,
                                                   data.getEyeGazePoint().y);
            faceData.eyeHorizontalGazeAngle = data.getEyeHorizontalGazeAngle();
            faceData.eyeVerticalGazeAngle = data.getEyeVerticalGazeAngle();

            // 자동 주인공 등록이 설정되어 있으면 인물 분석을 수행한다.
            if(mIsAutoFaceRecognition) {
                // 얼굴 개수가 2개 미만일 때만 인물 분석을 수행한다. (싱글 사진만 신규 Recognition하도록 한다.)
                int newPersonId = faceCount < 2 ? addFacialRecognitionPerson(faceData, data, i)
                        : data.getPersonId();
                faceData.personId = newPersonId;
                if(faceData.personId == NONE_PERSON_ID_VALUE) {
                    isFaceRecognition = false;
                }
            }
            SmartLog.e(TAG, "faceCount : " + faceCount + ", personId : " + faceData.personId);
            faceData.leftEyeBlink = data.getLeftEyeBlink();
            faceData.rightEyeBlink = data.getRightEyeBlink();
            faceData.pitch = data.getPitch();
            faceData.roll = data.getRoll();
            faceData.yaw = data.getYaw();
            faceData.smileValue = data.getSmileValue();

            itemObject.faceDataItems.add(faceData);

            // 평균 smileValue를 구하기 위함
            sumSmileValue += data.getSmileValue();
            // 평균 leftBlinkValue
            sumLeftBlinkValue += data.getLeftEyeBlink();
            sumRightBlinkValue += data.getRightEyeBlink();
        }

        if(!isFaceRecognition) {
            itemObject.isFinishFaceDetecting = false;
        } else {
            itemObject.isFinishFaceDetecting = true;
        }
        itemObject.avgSmileValue = (float)(sumSmileValue / faceCount);
        itemObject.avgLeftBlinkValue = (float)(sumLeftBlinkValue / faceCount);
        itemObject.avgRightBlinkValue = (float)(sumRightBlinkValue / faceCount);
    }

    // 얼굴 데이터를 각각 추출하여 넣는다
    // only Google FaceDetect
    private void setFaceDataItems(ImageData itemObject, Face[] faceDatas) {
        if(faceDatas == null || faceDatas.length < 1) {
            itemObject.numberOfFace = 0;
            return;
        }

        int faceCount = faceDatas.length;

        itemObject.faceDataItems = new ArrayList<ImageFaceData>();
        long time = 0L;
        String faceDataKey = null;
        for(int i = 0; i < faceCount; i++) {
            Face face = faceDatas[i];
            if(face != null) {
                PointF eyesMP = new PointF();
                face.getMidPoint(eyesMP);
                float centerX = eyesMP.x;
                float centerY = eyesMP.y;
                float eyesDistance = Math.abs(face.eyesDistance());
                float distanceEyeToMouse = eyesDistance * (148.f / 138.f);
                float mouseY = centerY + distanceEyeToMouse;

                FacePoint leftEyePoint = new FacePoint((int)(centerX - eyesDistance / 2),
                                                       (int)eyesMP.y);
                FacePoint rightEyePoint = new FacePoint((int)(centerX + eyesDistance / 2),
                                                        (int)eyesMP.y);
                FacePoint mouthPoint = new FacePoint((int)centerX, (int)mouseY);
                RectF faceRectF = FaceInfomation.getFaceRect(itemObject.faceBitmapWidth,
                                                             itemObject.faceBitmapHeight,
                                                             leftEyePoint, rightEyePoint,
                                                             mouthPoint);
                Rect faceDetectRect = new Rect((int)faceRectF.left, (int)faceRectF.top,
                                               (int)faceRectF.right, (int)faceRectF.bottom);

                ImageFaceData faceData = new ImageFaceData();
                time = System.currentTimeMillis();
                faceDataKey = time + "_" + itemObject.id;
                SmartLog.e(TAG, "FaceDataKey : " + faceDataKey);
                faceData.faceDataKey = faceDataKey;
                faceData.imageId = itemObject.id;
                faceData.faceIndex = i;
                faceData.isUpdatePerson = false;
                faceData.leftEyePoint = leftEyePoint;
                faceData.rightEyePoint = rightEyePoint;
                faceData.mouthPoint = mouthPoint;
                faceData.faceDetectRect = new FaceRect(faceDetectRect);
                faceData.faceRect = new FaceRect(
                                                 (int)(faceDetectRect.left * itemObject.faceBitmapScale),
                                                 (int)(faceDetectRect.top * itemObject.faceBitmapScale),
                                                 (int)(faceDetectRect.right * itemObject.faceBitmapScale),
                                                 (int)(faceDetectRect.bottom * itemObject.faceBitmapScale));
                faceData.faceDetectScale = itemObject.faceBitmapScale;
                faceData.eyeGazePoint = new FacePointF(NONE_DEFAULT_VALUE, NONE_DEFAULT_VALUE);
                faceData.eyeHorizontalGazeAngle = NONE_DEFAULT_VALUE;
                faceData.eyeVerticalGazeAngle = NONE_DEFAULT_VALUE;
                faceData.personId = NONE_PERSON_ID_VALUE;
                faceData.leftEyeBlink = NONE_DEFAULT_VALUE;
                faceData.rightEyeBlink = NONE_DEFAULT_VALUE;
                faceData.pitch = NONE_DEFAULT_VALUE;
                faceData.roll = NONE_DEFAULT_VALUE;
                faceData.yaw = NONE_DEFAULT_VALUE;
                faceData.smileValue = NONE_DEFAULT_VALUE;

                itemObject.faceDataItems.add(faceData);
            }
        }
        itemObject.isFinishFaceDetecting = true;
        itemObject.avgSmileValue = NONE_DEFAULT_VALUE;
        itemObject.avgLeftBlinkValue = NONE_DEFAULT_VALUE;
        itemObject.avgRightBlinkValue = NONE_DEFAULT_VALUE;
    }

    /**
     * 이미지 데이터에서 인물 분석에 필요한 비트맵을 반환.
     * 
     * @param imageData 인물 분석할 이미지 데이터
     * @return 인물 분석에 필요한 비트맵
     */
    private Bitmap getBitmapFromImageData(ImageData imageData) {
        Bitmap bmp = null;
        // 가상 디코딩을 통해 이미지 사이즈를 알아냄
        BitmapFactory.decodeFile(imageData.path, mCheckBitmapOptions);
        if(mCheckBitmapOptions.outHeight > 0 && mCheckBitmapOptions.outWidth > 0) {

            try {
                bmp = BitmapUtils.getBitmapImage(imageData.path, imageData.orientation,
                                                 mCheckBitmapOptions.inPreferredConfig);
            } catch(IOException e) {
                SmartLog.e(TAG, "bitmap DecodeFail");
                e.printStackTrace();
            }
        }
        return bmp;
    }

    /**
     * ************************* 얼굴 인식 앨범 관련 ****************************
     */
    public static ArrayList<Integer> getPersonIdWithImageData(Context context, ImageData imageData) {
        if(context == null || imageData == null || imageData.faceDataItems == null
                || imageData.faceDataItems.isEmpty()) {
            return null;
        }

        ArrayList<Integer> personIds = new ArrayList<Integer>();
        // 1명인 사진에서 찾아야 확실하게 찾을 수 있지...
        for(int i = 0; i < imageData.faceDataItems.size(); i++) {
            personIds.add(imageData.faceDataItems.get(i).personId);
        }

        return personIds;
    }

    /**
     * 인물 인식 앨범 그룹을 로드한다. <br>
     * only Snapdragon SDK
     */
    public void loadFaceRecognitionAlbumGroup() {
        SharedPreferences settings = mContext.getSharedPreferences(SHARED_PREFERENCE_NAME_FACIAL_ALBUM_GROUP,
                                                                   0);
        int facialAlbumGroupCount = settings.getInt(SHARED_PREFERENCE_KEY_FACIAL_ALBUM_GROUP_COUNT,
                                                    1);
        for(int i = 0; i < facialAlbumGroupCount; i++) {
            String albumString = settings.getString(SHARED_PREFERENCE_KEY_FACIAL_ALBUM_GROUP_DATA_PREFIX
                                                            + i, null);
            if(albumString == null) {
                continue;
            }
            // 이미 저장된 앨범 그룹 데이터가 있으면 수정하고 없으면 추가한다.
            if(mFacialAlbumGroupData.size() > i) {
                mFacialAlbumGroupData.set(i, albumString);
            } else {
                mFacialAlbumGroupData.add(albumString);
            }
        }
        // 초기에 첫 번째 앨범 그룹 로드
        loadFaceRecognitionAlbum(0);
    }

    /**
     * groupIndex에 해당하는 앨범 그룹을 로드한다.
     * 
     * @param groupIndex 로드할 앨범 그룹의 index
     */
    private void loadFaceRecognitionAlbum(int groupIndex) {
        // groupIndex에 해당하는 앨범이 생성되어 있지 않으면 생성해준다.
        if(mFacialAlbumGroupData.size() <= groupIndex
                || mFacialAlbumGroupData.get(groupIndex) == null) {
            // 이전 앨범 저장. groupIndex가 0이면 생성된 앨범이 하나도 없는 것이므로 저장하지 않는다.
            if(groupIndex != 0) {
                saveFaceRecognitionAlbum();
            }

            // 기존 앨범 정보를 초기화하고 새로운 앨범을 생성한다.
            getFacialInstance().resetAlbum();
            mCurrentFacialAlbumGroupIndex = groupIndex;
            saveFaceRecognitionAlbum();
        } else {
            // 이전 앰범과 불러오려는 앨범이 다른 경우에만 로드
            if(mCurrentFacialAlbumGroupIndex != groupIndex) {
                // 이전에 로드된 앨범이 있으면 저장
                if(mCurrentFacialAlbumGroupIndex != -1) {
                    saveFaceRecognitionAlbum();
                }

                mCurrentFacialAlbumGroupIndex = groupIndex;
                String stringData = mFacialAlbumGroupData.get(groupIndex);
                byte[] albumArray = null;
                if(stringData != null) {
                    String[] splitStringArray = stringData.substring(1, stringData.length() - 1)
                                                          .split(", ");

                    albumArray = new byte[splitStringArray.length];
                    for(int j = 0; j < splitStringArray.length; j++) {
                        albumArray[j] = Byte.parseByte(splitStringArray[j]);
                    }
                    getFacialInstance().deserializeRecognitionAlbum(albumArray);
                }
            }
        }
    }

    /**
     * Face Recognition Album을 저장한다.<br>
     * only Snapdragon SDK
     */
    public void saveFaceRecognitionAlbum() {
        byte[] albumBuffer = getFacialInstance().serializeRecogntionAlbum();
        SharedPreferences settings = mContext.getSharedPreferences(SHARED_PREFERENCE_NAME_FACIAL_ALBUM_GROUP,
                                                                   0);
        SharedPreferences.Editor editor = settings.edit();
        String albumString = Arrays.toString(albumBuffer);
        // 이미 저장된 앨범 그룹 데이터가 있으면 수정하고 없으면 추가한다.
        if(mFacialAlbumGroupData.size() > mCurrentFacialAlbumGroupIndex) {
            mFacialAlbumGroupData.set(mCurrentFacialAlbumGroupIndex, albumString);
        } else {
            mFacialAlbumGroupData.add(albumString);
        }
        editor.putString(SHARED_PREFERENCE_KEY_FACIAL_ALBUM_GROUP_DATA_PREFIX
                + mCurrentFacialAlbumGroupIndex, albumString);
        editor.putInt(SHARED_PREFERENCE_KEY_FACIAL_ALBUM_GROUP_COUNT, mFacialAlbumGroupData.size());
        editor.commit();
    }

    /**
     * 인물을 분석하고 personId를 반환한다.
     * 
     * @param faceDatas 분석하기 위한 인물 정보
     * @param faceIndex 분석하려는 인물의 index
     * @return 인물 분석이 완료되면 0보다 크거나 같은 값을, 분석되지 않으면 -111을 반환한다. 일반적으로 등록 가능한 최대 인물 개수를 넘어간 경우 -111을
     *         반환한다.
     */
    private int addFacialRecognitionPerson(ImageFaceData imageFaceData, FaceData faceDatas,
            int faceIndex) {
        int personId = faceDatas.getPersonId();
        // 현재 앨범 그룹에 속하지 않음
        if(personId == FacialProcessingConstants.FP_PERSON_NOT_REGISTERED) {
            personId = recognitionPerson(mCurrentFacialAlbumGroupIndex, faceIndex);
            // 다른 앨범 그룹에도 속하지 않음
            if(personId == FacialProcessingConstants.FP_PERSON_NOT_REGISTERED) {
                int usableGroupIndex = getUsableAlbumGroup();
                // 인물 추가 가능한 앨범 그룹이 있으면
                if(usableGroupIndex != -1) {
                    personId = getFacialInstance().addPerson(faceIndex);
                    // 인물이 정상적으로 추가되었으면 앨범을 저장한다.
                    if(personId != FacialProcessingConstants.FP_PERSON_NOT_REGISTERED) {
                        if(imageFaceData != null) {
                            imageFaceData.isUpdatePerson = true;
                            imageFaceData.isRepresentPerson = true;
                        }
                        personId = personId + usableGroupIndex * MAX_FACIAL_ALBUM_PERSON_COUNT;
                        saveFaceRecognitionAlbum();
                    }
                }
            }
        } else {
            personId = personId + mCurrentFacialAlbumGroupIndex * MAX_FACIAL_ALBUM_PERSON_COUNT;
        }
        return personId;
    }

    /**
     * 인물을 분석하고 personId를 반환한다.<br>
     * Only Snapdragon SDK
     * 
     * @param faceDatas 분석하기 위한 인물 정보
     * @param faceIndex 분석하려는 인물의 index
     * @return 인물 분석이 완료되면 0보다 크거나 같은 값을, 분석되지 않으면 -111을 반환한다. 일반적으로 등록 가능한 최대 인물 개수를 넘어간 경우 -111을
     *         반환한다.
     */
    public int addFacialRecognitionPerson(ImageData imageData, int faceIndex) {
        // 인물 분석을 다시 돌려야하므로 비트맵을 가져온다.
        Bitmap bmp = getBitmapFromImageData(imageData);
        if(bmp == null) {
            // 인물 분석할 이미지를 가져오지 못하였으므로 실패...
            return FacialProcessingConstants.FP_PERSON_NOT_REGISTERED;
        }

        // 인물 재분석 하여
        boolean analyzed = getFacialInstance().setBitmap(bmp);
        if(analyzed) {
            int personId = addFacialRecognitionPerson(null,
                                                      getFacialInstance().getFaceData()[faceIndex],
                                                      faceIndex);
            // 업데이트에 성공했으면 앨범 그룹을 저장한다.
            if(personId != FacialProcessingConstants.FP_PERSON_NOT_REGISTERED) {
                saveFaceRecognitionAlbum();
            }
            return personId;
        } else {
            return FacialProcessingConstants.FP_PERSON_NOT_REGISTERED;
        }
    }

    /**
     * 인물을 분석하여 특정 인물정보를 update한다.<br>
     * Only Snapdragon SDK
     * 
     * @param imageData 분석할 이미지 정보
     * @param personId update할 인물 번호
     * @param faceIndex 분석시 이미지의 인물 index
     * @return 결과를 반환.
     */
    public int updateFacialRecognitionPerson(ImageData imageData, int personId, int faceIndex) {
        // 특정 그룹의 앨범을 꺼낸다.
        int groupIndex = getGroupIndexForPersonId(personId);
        loadFaceRecognitionAlbum(groupIndex);

        int originalPersonId = personId % MAX_FACIAL_ALBUM_PERSON_COUNT;

        // 인물 분석을 다시 돌려야하므로 비트맵을 가져온다.
        Bitmap bmp = getBitmapFromImageData(imageData);
        if(bmp == null) {
            // 인물 분석할 이미지를 가져오지 못하였으므로 실패...
            return FacialProcessingConstants.FP_INTERNAL_ERROR;
        }

        // 인물 재분석 하여
        boolean analyzed = getFacialInstance().setBitmap(bmp);
        int result = FacialProcessingConstants.FP_INTERNAL_ERROR;
        try {
            if(analyzed) {
                result = getFacialInstance().updatePerson(originalPersonId, faceIndex);
                // 업데이트에 성공했으면 앨범 그룹을 저장한다.
                if(result == FacialProcessingConstants.FP_SUCCESS) {
                    saveFaceRecognitionAlbum();
                }
            }
        } catch(IllegalArgumentException e) {
            SmartLog.e(TAG, "deleteFacialRecognitionAlbum result: " + result + ", GroupIndex: "
                    + mCurrentFacialAlbumGroupIndex + ", PersonId: " + personId, e);
        } finally {
            SmartLog.e(TAG, "updateFacialRecognitionPerson result: " + result + ", GroupIndex: "
                    + mCurrentFacialAlbumGroupIndex + ", PersonId: " + personId);
        }
        return result;
    }

    /**
     * Snapdragon 앨범에서 특정 personId를 삭제한다.
     * 
     * @param personId 삭제할 인물 번호
     * @return 삭제되었는지의 결과
     */
    public boolean deleteFacialRecognitionAlbum(int personId) {
        // 특정 그룹의 앨범을 꺼낸다.
        int groupIndex = getGroupIndexForPersonId(personId);
        loadFaceRecognitionAlbum(groupIndex);

        boolean result = false;
        try {
            int originalPersonId = personId % MAX_FACIAL_ALBUM_PERSON_COUNT;
            result = getFacialInstance().deletePerson(originalPersonId);
            if(result) {
                saveFaceRecognitionAlbum();
            }
        } catch(IllegalArgumentException e) {
            SmartLog.e(TAG, "deleteFacialRecognitionAlbum result: " + result + ", GroupIndex: "
                    + mCurrentFacialAlbumGroupIndex + ", PersonId: " + personId, e);
        } finally {
            SmartLog.e(TAG, "deleteFacialRecognitionAlbum result: " + result + ", GroupIndex: "
                    + mCurrentFacialAlbumGroupIndex + ", PersonId: " + personId);
        }
        return result;
    }

    /**
     * 현재 앨범 그룹에서 분석되지 않은 인물을 다른 앨범 그룹에서 분석한다.
     * 
     * @param currentAlbumIndex 현재 앨범 그룹의 index
     * @param faceIndex 분석하려는 인물의 index
     * @return 다른 앨범 그룹에서 인물이 분석되면 0보다 크거나 같은 값을, 분석되지 않으면 -111을 반환한다.
     */
    private int recognitionPerson(int currentAlbumIndex, int faceIndex) {
        int personId = FacialProcessingConstants.FP_PERSON_NOT_REGISTERED;
        for(int i = 0; i < mFacialAlbumGroupData.size(); i++) {
            if(i != currentAlbumIndex) {
                loadFaceRecognitionAlbum(i);
                // TODO: getFaceData 함수 호출을 최소화 하기 위해 한사진당 최대 앨범 개수 만큼만 호출되도록 수정이 필요하다.
                personId = getFacialInstance().getFaceData()[faceIndex].getPersonId();
                if(personId != FacialProcessingConstants.FP_PERSON_NOT_REGISTERED) {
                    personId = personId + i * MAX_FACIAL_ALBUM_PERSON_COUNT;
                    break;
                }
            }
        }
        return personId;
    }

    /**
     * 인물을 추가할 수 있는 앨범 그룹 index를 반환한다.
     * 
     * @return 인물을 추가할 수 있는 앨범 그룹 index. -1이면 더이상 인물을 추가할 수 있는 앨범 그룹이 없음.
     */
    private int getUsableAlbumGroup() {
        int usableGroupIndex = -1;
        if(mFacialAlbumGroupData.size() <= MAX_FACIAL_ALBUM_GROUP_COUNT) {
            for(int i = 0; i < MAX_FACIAL_ALBUM_GROUP_COUNT; i++) {
                loadFaceRecognitionAlbum(i);
                if(getFacialInstance().getAlbumPersonCount() < MAX_FACIAL_ALBUM_PERSON_COUNT) {
                    usableGroupIndex = i;
                    break;
                }
            }
        }
        return usableGroupIndex;
    }

    /**
     * 인물 번호로 앨범 그룹 index를 찾는다.
     * 
     * @param personId 찾을 인물 번호
     * @return 해당 인물이 들어있는 앨범 그룹 index.
     */
    private int getGroupIndexForPersonId(int personId) {
        return personId / MAX_FACIAL_ALBUM_PERSON_COUNT;
    }

    /**
     * 저장된 Face Recognition 앨범을 Hash로 꺼낸다.<br>
     * only Snapdragon SDK
     * 
     * @return HashMap Face Recognition 정보
     */
    @SuppressWarnings("unchecked")
    public HashMap<String, String> retrieveFaceRecognitionHash() {
        SharedPreferences settings = mContext.getSharedPreferences(FACE_RECOGNITION_HASH_KEY, 0);
        HashMap<String, String> hash = new HashMap<String, String>();
        hash.putAll((Map<? extends String, ? extends String>)settings.getAll());
        return hash;
    }

    /**
     * Face Recognition 정보(HashMap)을 저장한다.<br>
     * only Snapdragon SDK
     * 
     * @param hashMap Face Recognition
     */
    public void saveFaceRecognitionHash(HashMap<String, String> hashMap) {
        SharedPreferences settings = mContext.getSharedPreferences(FACE_RECOGNITION_HASH_KEY, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        for(String s : hashMap.keySet()) {
            editor.putString(s, hashMap.get(s));
        }
        editor.commit();
    }
}
