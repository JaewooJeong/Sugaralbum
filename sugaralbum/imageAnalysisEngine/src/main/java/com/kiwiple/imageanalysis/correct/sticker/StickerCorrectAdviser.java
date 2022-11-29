
package com.kiwiple.imageanalysis.correct.sticker;

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.RectF;
import android.text.TextUtils;

import com.kiwiple.imageanalysis.Constants;
import com.kiwiple.imageanalysis.correct.ImageCorrectData;
import com.kiwiple.imageanalysis.correct.ImageCorrectStickerData;
import com.kiwiple.imageanalysis.correct.collage.FaceInfomation;
import com.kiwiple.imageanalysis.database.FacePointF;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.database.ImageFaceData;
import com.kiwiple.imageanalysis.utils.SmartLog;

/**
 * 사진 이미지에 맞는 스티커를 추천하고 스티커를 붙일 위치 크기 등을 추천해주는 클래스. <br>
 * 스티커 추가하면 그에 맞는 json파일과 함께 스티커 이미지를 해당 앱의 "asset/sticker/" 위치에 넣는다.<br>
 * 카테고리를 추가할 경우 getCategoryName(int, int) 함수와 위의 이미지와 json파일을 추가하거나 수정해준다.
 */
public class StickerCorrectAdviser {

    private static final String TAG = StickerCorrectAdviser.class.getSimpleName();

    public static final String CATEGORY_NEW_YEAR = "newyear";
    public static final String CATEGORY_CHRISMAS = "chrismas";
    public static final String CATEGORY_SPRING = "spring";
    public static final String CATEGORY_SUMMER = "summer";
    public static final String CATEGORY_AUTUMN = "autumn";
    public static final String CATEGORY_WINTER = "winter";

    public static final String CATEGORY_BABY = "baby";
    public static final String CATEGORY_FACE = "face";
    
    private static final String SUB_CATEGORY_ALL = "All";
    private static final String SUB_CATEGORY_TOP = "Top";
    private static final String SUB_CATEGORY_BOTTOM = "Bottom";
    private static final String SUB_CATEGORY_EYE = "Eye";
    private static final String SUB_CATEGORY_HAT = "Hat";

    // 4x4 분할
    private static final float divideAreaCount = 4.f;
    
    private Context mApplicationContext;

    /**
     * 생성자
     * 
     * @param applicationContext ApplicationContext
     */
    public StickerCorrectAdviser(Context applicationContext) {
        mApplicationContext = applicationContext;
    }

    /**
     * 원하는 이미지의 사이즈와 함께 imageData 정보를 넣으면 <br>
     * imageData.imageCorrectData에 스티커에 관련된 값을 추천하여 셋팅한다.<br>
     * 추천할만한 스티커가 해당 카테고리에 없는 경우에는 값을 셋팅하지 않는다. <br>
     * 
     * @param imageData 이미지 정보
     * @param size 디코딩시 이미지 사이즈 (최대한 가까운 쪽으로 디코딩하여 계산한다.)
     * @return ImageData 스티커 추천 관련 값이 추가된 이미지 정보
     */
    public ImageData setStickerCorrectData(ImageData imageData) {

        if(imageData == null || imageData.path == null) {
            return imageData;
        }

        // 이미지 데이터의 날짜를 읽어온다.
        String[] dateArr = imageData.dateFormat.split("-");
        if(dateArr == null || dateArr.length < 3) {
            SmartLog.e(TAG, "fail date parsing");
            return imageData;
        }
        String dateMonthStr = dateArr[1];
        int dateMonth = Integer.valueOf(dateMonthStr);
        String dateDayStr = dateArr[2];
        int dateDay = Integer.valueOf(dateDayStr);

        String categoryName = getCategoryName(imageData, dateMonth, dateDay);
        ImageData correctImageData = getStickerCorrectWithCategoryName(imageData, categoryName);

        return correctImageData;
    }

    private boolean isFinishCheckSticker(boolean[] isCheckSticker) {
        boolean isFinishCheck = true;
        for(boolean isCheck : isCheckSticker) {
            if(!isCheck) {
                return false;
            }
        }
        return isFinishCheck;
    }

    private ImageData getStickerCorrectWithCategoryName(ImageData imageData, String categoryName) {

        if(imageData == null || imageData.path == null || TextUtils.isEmpty(categoryName)) {
            return imageData;
        }

        int width = imageData.width;
        int height = imageData.height;

        // 회전 정도에 따라 이미지의 크기가 바뀜
        if("90".equals(imageData.orientation) || "270".equals(imageData.orientation)) {
            int tmp = width;
            width = height;
            height = tmp;
        }

        // 대분류 카테고리에 따른 스티커 배열을 읽어온다.
        ArrayList<StickerInfo> stickerInfoArr = null;
        try {
            stickerInfoArr = StickerManager.getInstance(mApplicationContext)
                                           .getStickerListAsset("sticker/" + categoryName + ".json");
        } catch(IOException e) {
            // TODO Auto-generated catch block
            SmartLog.e(TAG, "no Sticker Infos");
            e.printStackTrace();
            return imageData;
        }

        // 해당 카테고리에 스티커가 없다면 그대로 리턴.
        if(stickerInfoArr == null || stickerInfoArr.isEmpty()) {
            SmartLog.e(TAG, "no Sticker Infos");
            return imageData;
        }

        RectF stickerRect = null;
        StickerInfo stickerInfo = null;
        boolean isCheckLoop = true;
        boolean[] isCheckSticker = new boolean[stickerInfoArr.size()];
        do {
            // 스티커 배열에서 랜덤하게 스티커 하나를 꺼낸다.
            int randomIndex = (int)(Math.random() * stickerInfoArr.size());
            stickerInfo = stickerInfoArr.get(randomIndex);
            if(!isCheckSticker[randomIndex]) {
                isCheckSticker[randomIndex] = true;
                SmartLog.e(TAG, "stickerIndex : " + randomIndex);

                if(imageData.faceDataItems == null || imageData.faceDataItems.isEmpty()) {
                    // 카테고리에 따른 스티커 이동 포인트
                    stickerRect = getStickerRectF(stickerInfo, width, height, null, null, 0.f);
                    // 얼굴이 없다면 랜덤배치
                    setStickerCorrectData(imageData, stickerInfo, stickerRect, width, height);
                    return imageData;
                }

                // 얼굴 전체 영역을 가져온다. (기준 크기는 Face Detecting 당시 크기)
                FaceInfomation faceInfomation = FaceInfomation.getFaceInfomation(imageData.faceDataItems.get(0));

                RectF faceRectF = FaceInfomation.getFaceRect(width, height,
                                                             faceInfomation.leftEyePoint,
                                                             faceInfomation.rightEyePoint,
                                                             faceInfomation.mouthPoint);
                RectF faceRealRectF = new RectF(faceRectF.left * imageData.faceBitmapScale,
                                                faceRectF.top * imageData.faceBitmapScale,
                                                faceRectF.right * imageData.faceBitmapScale,
                                                faceRectF.bottom * imageData.faceBitmapScale);

                // 실제 크기와의 차이가 있으므로 비율을 곱해준다.
                float ratio = width / (float)imageData.faceBitmapWidth;
                PointF eyeCenterPoint = new PointF(faceInfomation.eyeCenterX * ratio,
                                                   faceInfomation.eyeCenterY * ratio);
                float distanceHeadToEye = faceInfomation.distanceFaceTopToEye * ratio;

                // 스티커 추천 영역을 구하자
                stickerRect = getStickerRectF(stickerInfo, width, height, faceRealRectF,
                                              eyeCenterPoint, distanceHeadToEye);

                // 얼굴이 있다면 모자 안경 마스크 등의 얼굴에 맞는 타입은 얼굴 각도가 틀어져있다면 추천하지 말자.
                if(stickerInfo.mSubCategory.equals(SUB_CATEGORY_HAT)
                        || stickerInfo.mSubCategory.equals(SUB_CATEGORY_EYE)
                        || "face".equals(stickerInfo.mSubCategory)) {
                    for(ImageFaceData faceData : imageData.faceDataItems) {
                        if(Math.abs(faceData.yaw) >= 25) {
                            stickerRect = null;
                        }
                    }
                    isCheckLoop = stickerRect == null;
                } else {
                    isCheckLoop = stickerRect == null || stickerRect.intersect(faceRealRectF);
                }
            }

            if(isFinishCheckSticker(isCheckSticker)) {
                stickerRect = null;
                setStickerCorrectData(imageData, stickerInfo, stickerRect, width, height);
                return imageData;
            }

        } while(isCheckLoop);

        setStickerCorrectData(imageData, stickerInfo, stickerRect, width, height);

        return imageData;
    }

    /**
     * 날짜의 월, 일을 가지고 카테고리를 분류하여 해당되는 카테고리의 이름을 반환한다. <br>
     * 월 값이 음수라면 null을 반환.
     * 
     * @param imageData
     * @param month 월 값 (ex 1 => 1월)
     * @param day 일 값 (ex 23 => 23일)
     * @return String 분류되는 카테고리 이름
     */
    private String getCategoryName(ImageData imageData, int month, int day) {
        String category;
        if(imageData != null && imageData.faceDataItems != null
                && !imageData.faceDataItems.isEmpty()
                && Math.abs(imageData.faceDataItems.get(0).yaw) < 25) {
            category = CATEGORY_FACE;
        } else {
            category = getCategoryName(month, day);
        }
        SmartLog.d(TAG, "sticker category : " + category);

        return category;
    }

    private String getCategoryName(int month, int day) {
    	if(Constants.DEMO_BUILD) {
    		return null;
    	}
        // 크리스마스 체크. 범위는 12월 1일 ~ 12월 31일
        if(month == 12 && day > 0 && day < 32) {
            return CATEGORY_CHRISMAS;
        }

        // 신년인지 체크. 범위는 1월 1일 ~ 1월 31일
        if(month == 1 && day > 0 && day < 32) {
            return CATEGORY_NEW_YEAR;
        }

        // 봄 체크. 범위는 3월 ~ 6월
        if(month >= 3 && month < 7) {
            return CATEGORY_SPRING;
        }

        // 여름 체크. 범위는 7월 ~ 8월
        if(month >= 7 && month < 9) {
            return CATEGORY_SUMMER;
        }

        // 가을 체크. 범위는 9월 ~ 11월
        if(month >= 9 && month < 12) {
            return CATEGORY_AUTUMN;
        }

        // 겨울 체크. 범위는 12월 ~ 2월
        if(month == 12 || month == 1 || month == 2) {
            return CATEGORY_WINTER;
        }

        return null;
    }

    /**
     * 스티커가 위치할 영역을 계산하여 반환 <br>
     * 스티커 정보가 null일 경우는 null을 반환한다.
     * 
     * @param stickerInfo 스티커 정보
     * @param imageWidth 스티커를 붙일 이미지의 가로 길이
     * @param imageHeight 스티커를 붙일 이미지의 세로 길이
     * @param faceRect 얼굴 영역. (없을 경우엔 null을 입력)
     * @param eyeCenterPoint 눈의 가운데 영역 위치 (없을 경우 null을 입력)
     * @return RectF 추천 스티커 영역
     */
    private RectF getStickerRectF(StickerInfo stickerInfo, int imageWidth, int imageHeight,
            RectF faceRect, PointF eyeCenterPoint, float distanceHeadToEye) {

        if(stickerInfo == null) {
            return null;
        }

        // 실제 이미지 사이즈와 원하는 사이즈의 배율을 계산
        int stickerWidth = stickerInfo.mWidth;
        int stickerHeight = stickerInfo.mHeight;

        PointF stickerPoint = new PointF();
        float scale = 1.f;

        if(stickerInfo.mSubCategory.equals(SUB_CATEGORY_ALL)) {
            float halfWidth = imageWidth / divideAreaCount;
            float halfHeight = imageHeight / divideAreaCount;
            // 실제 이미지 사이즈와 원하는 사이즈의 배율을 계산
            if(halfWidth < halfHeight) {
                scale = stickerInfo.mWidth / halfWidth;
            } else {
                scale = stickerInfo.mHeight / halfHeight;
            }

            // 스티커의 크기를 결정
            stickerWidth = (int)(stickerInfo.mWidth / scale);
            stickerHeight = (int)(stickerInfo.mHeight / scale);

            // lefttop righttop leftbottom rightbottom 4군데 중 랜덤으로 선택
            int random = (int)(Math.random() * 4);
            SmartLog.e(TAG, "point Index : " + random);
            switch(random) {
                case 0:
                    // left_top
                    stickerPoint.x = 0.f;
                    stickerPoint.y = 0.f;
                    break;
                case 1:
                    // right_top
                    stickerPoint.x = imageWidth - stickerWidth;
                    stickerPoint.y = 0.f;
                    break;
                case 2:
                    // left_bottom
                    stickerPoint.x = 0.f;
                    stickerPoint.y = imageHeight - stickerHeight;
                    break;
                case 3:
                    // right_bottom
                    stickerPoint.x = imageWidth - stickerWidth;
                    stickerPoint.y = imageHeight - stickerHeight;
                    break;
                default:
                    // left_top
                    stickerPoint.x = 0.f;
                    stickerPoint.y = 0.f;
                    break;
            }
        } else if(stickerInfo.mSubCategory.equals(SUB_CATEGORY_TOP)) {
            // 좌우를 맞추어야 하는 경우
            if(stickerInfo.mIsFit) {
                scale = stickerInfo.mWidth / (float)imageWidth;
                // 스티커의 크기를 결정
                stickerWidth = (int)(stickerInfo.mWidth / scale);
                stickerHeight = (int)(stickerInfo.mHeight / scale);

                // 비율을 유지하여 좌우로 fit할 때 스티커가 이미지 원본을 넘어가는 경우 배치 불가능으로 판단
                if(stickerWidth > imageWidth || stickerHeight > imageHeight) {
                    return null;
                }

                stickerPoint.x = 0.f;
                stickerPoint.y = 0.f;
            } else {
                // 실제 이미지 사이즈와 원하는 사이즈의 배율을 계산
                float halfWidth = imageWidth / 2.f;
                float halfHeight = imageHeight / 2.f;
                // 실제 이미지 사이즈와 원하는 사이즈의 배율을 계산
                if(halfWidth < halfHeight) {
                    scale = stickerInfo.mWidth / halfWidth;
                } else {
                    scale = stickerInfo.mHeight / halfHeight;
                }

                // 스티커의 크기를 결정
                stickerWidth = (int)(stickerInfo.mWidth / scale);
                stickerHeight = (int)(stickerInfo.mHeight / scale);

                // 좌측 위 또는 우측 위로 맞추자.
                int random = (int)(Math.random() * 2);
                switch(random) {
                    case 0:
                        // left_top
                        stickerPoint.x = 0.f;
                        stickerPoint.y = 0.f;
                        break;
                    case 1:
                        // right_top
                        stickerPoint.x = imageWidth - stickerWidth;
                        stickerPoint.y = 0.f;
                        break;
                    default:
                        // left_top
                        stickerPoint.x = 0.f;
                        stickerPoint.y = 0.f;
                        break;
                }
            }
        } else if(stickerInfo.mSubCategory.equals(SUB_CATEGORY_BOTTOM)) {
            // 좌우를 맞추어야 하는 경우
            if(stickerInfo.mIsFit) {
                scale = stickerInfo.mWidth / (float)imageWidth;
                // 스티커의 크기를 결정
                stickerWidth = (int)(stickerInfo.mWidth / scale);
                stickerHeight = (int)(stickerInfo.mHeight / scale);

                // 비율을 유지하여 좌우로 fit할 때 스티커가 이미지 원본을 넘어가는 경우 배치 불가능으로 판단
                if(stickerWidth > imageWidth || stickerHeight > imageHeight) {
                    return null;
                }

                stickerPoint.x = 0.f;
                stickerPoint.y = imageHeight - stickerHeight;
            } else {
                // 실제 이미지 사이즈와 원하는 사이즈의 배율을 계산
                float halfWidth = imageWidth / 2.f;
                float halfHeight = imageHeight / 2.f;
                // 실제 이미지 사이즈와 원하는 사이즈의 배율을 계산
                if(halfWidth < halfHeight) {
                    scale = stickerInfo.mWidth / halfWidth;
                } else {
                    scale = stickerInfo.mHeight / halfHeight;
                }

                // 스티커의 크기를 결정
                stickerWidth = (int)(stickerInfo.mWidth / scale);
                stickerHeight = (int)(stickerInfo.mHeight / scale);

                // 좌측 아래 또는 우측 아래로 맞추자.
                int random = (int)(Math.random() * 2);
                switch(random) {
                    case 0:
                        // left_bottom
                        stickerPoint.x = 0.f;
                        stickerPoint.y = imageHeight - stickerHeight;
                        break;
                    case 1:
                        // right_bottom
                        stickerPoint.x = imageWidth - stickerWidth;
                        stickerPoint.y = imageHeight - stickerHeight;
                        break;
                    default:
                        // left_bottom
                        stickerPoint.x = 0.f;
                        stickerPoint.y = imageHeight - stickerHeight;
                        break;
                }
            }
        } else if(stickerInfo.mSubCategory.equals(SUB_CATEGORY_EYE)) {

            if(eyeCenterPoint == null) {
                return null;
            }

            scale = faceRect.width() / (stickerWidth);

            stickerWidth *= scale;
            stickerHeight *= scale;

            stickerPoint.x = eyeCenterPoint.x - stickerInfo.mLeftPadding * scale;
            stickerPoint.y = eyeCenterPoint.y - stickerInfo.mTopPadding * scale;

            // 여기서는 스티커의 센터 자체를 눈의 가운데 지점에 맞추므로
            // 하단에서 스티커의 센터만큼 이동시킨 값을 미리 빼준다.
            stickerPoint.x -= stickerWidth / 2;
            stickerPoint.y -= stickerHeight / 2;

        } else if(stickerInfo.mSubCategory.equals(SUB_CATEGORY_HAT)) {
            if(eyeCenterPoint == null) {
                return null;
            }

            scale = faceRect.width() / (stickerWidth - stickerInfo.mLeftPadding * 2);

            stickerWidth *= scale;
            stickerHeight *= scale;

            stickerPoint.x = eyeCenterPoint.x;
            stickerPoint.y = eyeCenterPoint.y - stickerInfo.mTopPadding * scale - distanceHeadToEye;

            // 여기서는 스티커의 센터 자체를 눈의 가운데 지점에 맞추므로
            // 하단에서 스티커의 센터만큼 이동시킨 값을 미리 빼준다.
            stickerPoint.x -= stickerWidth / 2;
            stickerPoint.y -= stickerHeight / 2;
        }

        RectF stickerRect = new RectF(stickerPoint.x, stickerPoint.y,
                                      stickerPoint.x + stickerWidth, stickerPoint.y + stickerHeight);

        return stickerRect;
    }

    /**
     * 비트맵 디코딩시 원하는 사이즈에 가까운 sampleSize 값을 반환한다. <br>
     * 반환되는 값은 반드시 2의 배수임. (1은 포함됨)
     * 
     * @param imageData 이미지 정보
     * @param size 디코딩 시 원하는 사이즈
     * @return int sampleSize의 값 (2의 배수)
     */
    public static int calculateInSampleSize(ImageData imageData, int size) {
        // Raw height and width of image
        final int height = imageData.height;
        final int width = imageData.width;
        int inSampleSize = 1;

        if(height > size || width > size) {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = (int)Math.ceil((float)height / (float)size);
            final int widthRatio = (int)Math.ceil((float)width / (float)size);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        if(inSampleSize != 1 && inSampleSize % 2 != 0) {
            inSampleSize -= 1;
        }

        return inSampleSize;
    }

    /**
     * 스티커 추천 정보 값을 설정
     * 
     * @param imageData 설정할 이미지 정보
     * @param stickerInfo 스티커 정보
     * @param stickerRect 스티커가 위치할 영역
     * @param imageWidth 스티커 추천시 배경이 되는 이미지의 가로 크기 (배율 계산을 위함)
     * @param imageHeight 스티커 추천시 배경이 되는 이미지의 세로 크기 (배율 계산을 위함)
     */
    private void setStickerCorrectData(ImageData imageData, StickerInfo stickerInfo,
            RectF stickerRect, int imageWidth, int imageHeight) {

        // 스티커 rect가 없다면 스티커를 붙일 수 없음
        if(stickerRect == null || imageData == null || stickerInfo == null) {
            return;
        }

        if(imageData.imageCorrectData == null) {
            imageData.imageCorrectData = new ImageCorrectData();
        }

        if(imageData.imageCorrectData.stickerCorrectDataArray == null) {
            imageData.imageCorrectData.stickerCorrectDataArray = new ArrayList<ImageCorrectStickerData>();
        }

        // 스티커는 여러장 들어갈 수 있으나 추천은 단 1개만 한다.
        ImageCorrectStickerData stickerCorrectData = new ImageCorrectStickerData();
        stickerCorrectData.stickerCategory = stickerInfo.mCategory;
        stickerCorrectData.stickerSubCategory = stickerInfo.mSubCategory;
        stickerCorrectData.stickerFileName = stickerInfo.mFileName;
        stickerCorrectData.stickerCoordinate = new FacePointF(stickerRect.left, stickerRect.top);
        stickerCorrectData.stickerScale = stickerRect.width() / stickerInfo.mWidth;
        stickerCorrectData.stickerWidth = stickerInfo.mWidth;
        stickerCorrectData.stickerHeight = stickerInfo.mHeight;
        stickerCorrectData.imageWidth = imageWidth;
        stickerCorrectData.imageHeight = imageHeight;

        if(stickerInfo.mSubCategory.equals(StickerCorrectAdviser.SUB_CATEGORY_HAT)
                || stickerInfo.mSubCategory.equals(StickerCorrectAdviser.SUB_CATEGORY_EYE)) {
            if(imageData.faceDataItems != null && !imageData.faceDataItems.isEmpty()) {
                stickerCorrectData.stickerRotate += imageData.faceDataItems.get(0).roll * -1;
            }
        }

        imageData.imageCorrectData.stickerCorrectDataArray.add(stickerCorrectData);
    }
}
