
package com.kiwiple.imageanalysis.correct.collage;

import android.graphics.RectF;

import com.kiwiple.imageanalysis.database.FacePoint;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.database.ImageFaceData;

/**
 * 얼굴 영역에 관련된 값을 포함하는 구조체 클래스
 */
public class FaceInfomation {
    /**
     * 좌측 눈의 위치
     */
    public FacePoint leftEyePoint;
    /**
     * 우측 눈의 위치
     */
    public FacePoint rightEyePoint;
    /**
     * 입의 위치
     */
    public FacePoint mouthPoint;
    /**
     * 좌우 양쪽 눈의 가운데 위치 X값
     */
    public float eyeCenterX;
    /**
     * 좌우 양쪽 눈의 가운데 위치 Y값
     */
    public float eyeCenterY;
    /**
     * 눈 사이의 거리
     */
    public float eyesDistance;
    /**
     * 얼굴의 가로길이
     */
    public float faceWidth;
    /**
     * 얼굴의 세로길이
     */
    public float faceHeight;
    // reference: http://www.intmath.com/blog/is-she-beautiful-the-new-golden-ratio/4149
    /**
     * 눈과 입사이의 거리
     */
    public float distanceEyeToMouse;
    /**
     * 입과 얼굴 하단 (턱)과의 거리
     */
    public float distanceMouthToFaceBottom;
    /**
     * 눈과 이마사이의 거리
     */
    public float distanceFaceTopToEye;
    /**
     * 얼굴 영역으로 추정되는 크기
     */
    public RectF faceRect = new RectF();

    /**
     * 양쪽 눈 위치, 입의 위치, 눈의 가운데 위치, 눈 사이의 거리 등 Face에 관련된 정보를 기록한다.<br>
     * 
     * @param faceData 얼굴 정보
     * @param orientation 이미지의 회전 정보
     */
    public static FaceInfomation getFaceInfomation(ImageFaceData faceData) {

        if(faceData == null) {
            return null;
        }

        FaceInfomation faceInfomation = new FaceInfomation();

        FacePoint leftEyePoint = new FacePoint(faceData.leftEyePoint.x, faceData.leftEyePoint.y);
        FacePoint rightEyePoint = new FacePoint(faceData.rightEyePoint.x, faceData.rightEyePoint.y);
        FacePoint mouthPoint = new FacePoint(faceData.mouthPoint.x, faceData.mouthPoint.y);

        if(leftEyePoint == null || rightEyePoint == null) {
            return null;
        }

        faceInfomation.leftEyePoint = new FacePoint(leftEyePoint.x, leftEyePoint.y);
        faceInfomation.rightEyePoint = new FacePoint(rightEyePoint.x, rightEyePoint.y);
        faceInfomation.mouthPoint = new FacePoint(mouthPoint.x, mouthPoint.y);

        faceInfomation.eyeCenterX = (rightEyePoint.x - leftEyePoint.x) / 2 + leftEyePoint.x;
        faceInfomation.eyeCenterY = (rightEyePoint.y - leftEyePoint.y) / 2 + leftEyePoint.y;
        faceInfomation.eyesDistance = (float)Math.sqrt(Math.pow(rightEyePoint.x - leftEyePoint.x, 2)
                + Math.pow(rightEyePoint.y - leftEyePoint.y, 2));
        // reference: http://www.intmath.com/blog/is-she-beautiful-the-new-golden-ratio/4149
        faceInfomation.distanceEyeToMouse = (float)Math.sqrt(Math.pow(faceInfomation.eyeCenterX - mouthPoint.x, 2)
                                                             + Math.pow(faceInfomation.eyeCenterY - mouthPoint.y, 2));;
        faceInfomation.faceHeight = faceInfomation.distanceEyeToMouse * (390.f / 148.f);
        faceInfomation.faceWidth = faceInfomation.eyesDistance * (200.f / 72.f);
        faceInfomation.distanceMouthToFaceBottom = faceInfomation.distanceEyeToMouse * 1000 / 1618;
        faceInfomation.distanceFaceTopToEye = faceInfomation.faceHeight
                - faceInfomation.distanceMouthToFaceBottom - faceInfomation.distanceEyeToMouse;

        return faceInfomation;
    }

    /**
     * ************************* 얼굴 영역 관련 ****************************
     */
    /**
     * 좌측 눈의 위치, 우측 눈의 위치, 입의 위치를 가지고 얼굴 영역을 검출해낸다. <br>
     * leftEye, rightEye, mouth 중 하나라도 없을 경우 null을 반환한다.
     * 
     * @param imageWidth 눈과 입의 좌표의 기준이 된 이미지의 가로 길이
     * @param imageHeight 눈과 입의 좌표의 기준이 된 이미지의 세로 길이
     * @param leftEye 좌측 눈의 좌표
     * @param rightEye 우측 눈의 좌표
     * @param mouth 입의 좌표
     * @return RectF 얼굴로 추정되는 영역 (가로 : 얼굴옆선+여백(머리카락 등으로 인한 여백이 있음), 세로 : 이마~턱밑)
     */
    public static RectF getFaceRect(int imageWidth, int imageHeight, FacePoint leftEye,
            FacePoint rightEye, FacePoint mouth) {
        if(leftEye == null || rightEye == null || mouth == null) {
            return null;
        }

        FacePoint leftEyePoint = new FacePoint(leftEye.x, leftEye.y);
        FacePoint rightEyePoint = new FacePoint(rightEye.x, rightEye.y);
        FacePoint mouthPoint = new FacePoint(mouth.x, mouth.y);

        if(leftEyePoint == null || rightEyePoint == null) {
            return null;
        }

        float eyeCenterX = (rightEyePoint.x - leftEyePoint.x) / 2 + leftEyePoint.x;
        float eyeCenterY = (rightEyePoint.y - leftEyePoint.y) / 2 + leftEyePoint.y;
        float eyesDistance = (float)Math.sqrt(Math.pow(rightEyePoint.x - leftEyePoint.x, 2)
                + Math.pow(rightEyePoint.y - leftEyePoint.y, 2));
        // reference: http://www.intmath.com/blog/is-she-beautiful-the-new-golden-ratio/4149
        float distanceEyeToMouse = (float)Math.sqrt(Math.pow(eyeCenterX - mouthPoint.x, 2)
                                              + Math.pow(eyeCenterY - mouthPoint.y, 2));
        RectF faceBound = new RectF();
        float faceHeight = distanceEyeToMouse * (390.f / 148.f);
        float faceWidth = eyesDistance * (200.f / 72.f);
        float distanceMouthToFaceBottom = distanceEyeToMouse * 1000 / 1618;
        float distanceFaceTopToEye = faceHeight - distanceMouthToFaceBottom - distanceEyeToMouse;
        
        if (mouthPoint.x > rightEyePoint.x && leftEyePoint.y > rightEyePoint.y && mouthPoint.y > rightEyePoint.y) {
            // 좌로 누움
            faceBound.left = eyeCenterX - distanceFaceTopToEye * 1.5f;
            faceBound.right = mouthPoint.x + distanceMouthToFaceBottom;
            faceBound.bottom = eyeCenterY + (faceWidth) / 2;
            faceBound.top = faceBound.bottom - faceWidth;
        } else if (mouthPoint.x < leftEyePoint.x && leftEyePoint.y < rightEyePoint.y && mouthPoint.y > leftEyePoint.y) {
            // 우로 누움
            faceBound.left = mouthPoint.x - distanceMouthToFaceBottom;
            faceBound.right = eyeCenterX + distanceFaceTopToEye * 1.5f;
            faceBound.top = eyeCenterY - (faceWidth) / 2;
            faceBound.bottom = faceBound.top + faceWidth;
        } else if (mouthPoint.y < leftEyePoint.y && mouthPoint.y < rightEyePoint.y && rightEyePoint.x < leftEyePoint.x) {
            // 뒤집힘
            faceBound.left = eyeCenterX - (faceWidth) / 2;
            faceBound.right = faceBound.left + faceWidth;
            faceBound.top = mouthPoint.y - distanceMouthToFaceBottom;
            faceBound.bottom = eyeCenterY + distanceFaceTopToEye * 1.5f;
        } else {
            // 정면
            faceBound.bottom = mouthPoint.y + distanceMouthToFaceBottom;
            faceBound.top = eyeCenterY - distanceFaceTopToEye * 1.5f;
            faceBound.left = eyeCenterX - (faceWidth) / 2;
            faceBound.right = faceBound.left + faceWidth;
        }

        return faceBound;
    }

    /**
     * 이미지의 얼굴들을 전부 포함하는 최소한의 영역을 반환한다.<br>
     * 이미지의 기준 크기는 이미지 얼굴분석할 때의 이미지 크기를 기준으로 반환한다.
     * 
     * @param imageData 이미지 데이터
     * @return RectF 이미지의 얼굴들을 전부 포함하는 최소한의 영역
     */
    public static RectF getFaceRectFromImageDetectSize(ImageData imageData) {

        if(imageData == null || imageData.faceDataItems == null
                || imageData.faceDataItems.isEmpty()) {
            return null;
        }

        RectF entireFaceRectF = new RectF(imageData.faceBitmapWidth, imageData.faceBitmapHeight, 0, 0);
        for(int i = 0; i < imageData.faceDataItems.size(); i++) {
            ImageFaceData faceData = imageData.faceDataItems.get(i);
            // 이미지 분석 시점을 기준으로 한 얼굴 정보
            FaceInfomation faceInfomation = getFaceInfomation(faceData);
            // 이미지 분석 시점을 기준으로 한 얼굴 정보
            RectF faceRect = getFaceRect(imageData.faceBitmapWidth, imageData.faceBitmapHeight,
                                         faceInfomation.leftEyePoint, faceInfomation.rightEyePoint,
                                         faceInfomation.mouthPoint);

            if(faceRect.left < entireFaceRectF.left) {
                entireFaceRectF.left = faceRect.left;
            }

            if(faceRect.top < entireFaceRectF.top) {
                entireFaceRectF.top = faceRect.top;
            }

            // right 및 bottom은 최대값을 가져야 한다.
            if(faceRect.right > entireFaceRectF.right) {
                entireFaceRectF.right = faceRect.right;
            }

            if(faceRect.bottom > entireFaceRectF.bottom) {
                entireFaceRectF.bottom = faceRect.bottom;
            }
        }

        return entireFaceRectF;
    }

    /**
     * 이미지의 얼굴들을 전부 포함하는 최소한의 영역을 반환한다.<br>
     * 이미지의 기준 크기는 실제 이미지의 크기를 기준으로 반환한다.
     * 
     * @param imageData 이미지 데이터
     * @return RectF 이미지의 얼굴들을 전부 포함하는 최소한의 영역
     */
    public static RectF getFaceRectFromImageRealSize(ImageData imageData) {

        if(imageData == null || imageData.faceDataItems == null
                || imageData.faceDataItems.isEmpty()) {
            return null;
        }

        int originalWidth = imageData.width;
        int originalHeight = imageData.height;
        if("90".equals(imageData.orientation) || "270".equals(imageData.orientation)) {
            originalWidth = imageData.height;
            originalHeight = imageData.width;
        }

        RectF entireFaceRectF = new RectF(originalWidth, originalHeight, 0, 0);
        for(int i = 0; i < imageData.faceDataItems.size(); i++) {
            ImageFaceData faceData = imageData.faceDataItems.get(i);
            // face의 눈과 입의 위치는 썸네일 이미지 사이즈에서 추출된 사이즈이므로 scale값만큼 곱해주어야함.
            int ratioImageByFaceDetecting = (int)imageData.faceBitmapScale;
            int imageWidth = imageData.faceBitmapWidth;
            int imageHeight = imageData.faceBitmapHeight;

            // Scale Down된 이미지에서의 얼굴 영역
            FaceInfomation faceInfomation = getFaceInfomation(faceData);
            RectF faceRect = getFaceRect(imageWidth, imageHeight, faceInfomation.leftEyePoint,
                                         faceInfomation.rightEyePoint, faceInfomation.mouthPoint);

            // 실제 사진에서의 얼굴 영역 (Scale Down 영역 * 비율)
            RectF faceRectF = new RectF(faceRect.left * ratioImageByFaceDetecting, faceRect.top
                    * ratioImageByFaceDetecting, faceRect.right * ratioImageByFaceDetecting,
                                        faceRect.bottom * ratioImageByFaceDetecting);

            // left 및 top은 최소값
            if(faceRectF.left < entireFaceRectF.left) {
                entireFaceRectF.left = faceRectF.left;
            }

            if(faceRectF.top < entireFaceRectF.top) {
                entireFaceRectF.top = faceRectF.top;
            }

            // right 및 bottom은 최대값을 가져야 한다.
            if(faceRectF.right > entireFaceRectF.right) {
                entireFaceRectF.right = faceRectF.right;
            }

            if(faceRectF.bottom > entireFaceRectF.bottom) {
                entireFaceRectF.bottom = faceRectF.bottom;
            }
        }

        return entireFaceRectF;
    }
}
