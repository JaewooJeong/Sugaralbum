package com.kiwiple.imageanalysis.database;

import java.io.Serializable;

/**
 * 이미지의 얼굴 정보를 담고 있는 클래스.<br>
 * 이미지 1개는 여러개의 ImageFaceData를 가질 수 있음.<br>
 * ImageFaceData는 각 인물 1명에 해당하는 정보를 가지고 있음.
 */
public class ImageFaceData implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 680859025375086501L;
    /**
     * 각 얼굴에 관한 고유 키 값 (현재는 사용되지 않음)
     */
    public String faceDataKey;
    /**
     * 어떠한 이미지에 속해있는지를 알기 위한 이미지 고유 id값
     */
    public int imageId;
    /**
     * 좌측 눈의 위치 <br>
     * only Snapdragon SDK
     */
    public FacePoint leftEyePoint;
    /**
     * 우측 눈의 위치<br>
     * only Snapdragon SDK
     */
    public FacePoint rightEyePoint;
    /**
     * 입의 위치<br>
     * only Snapdragon SDK
     */
    public FacePoint mouthPoint;
    /**
     * 얼굴 영역
     */
    public FaceRect faceRect;
    /**
     * Face 분석시 분석된 얼굴 영역<br>
     * 분석시 실제 원본 이미지 사이즈가 아닌 sampleSize 이미지로 분석했으므로 그에 대한 scaled되어있는 값. <br>
     * faceDetectRect * scaled 가 실제 사진에서의 얼굴 영역이 된다.
     */
    public FaceRect faceDetectRect;
    /**
     * Face 분석시 이미지 크기와 실제 원본 이미지 크기사이의 비율
     */
    public float faceDetectScale;
    /**
     * 카메라의 위치를 기준으로 바라보는 시점의 좌표<br>
     * only Snapdragon SDK
     */
    public FacePointF eyeGazePoint;
    /**
     * 수평 시선의 각도 값<br>
     * only Snapdragon SDK
     */
    public int eyeHorizontalGazeAngle = 0;
    /**
     * 수직 시선의 각도 값<br>
     * only Snapdragon SDK
     */
    public int eyeVerticalGazeAngle = 0;
    /**
     * 주인공 설정하였을 경우 할당된 고유값 (0 ~ 100)<br>
     * -111일 경우 주인공 또는 Recognition 대상이 아님<br>
     * only Snapdragon SDK
     */
    public int personId = -111;
    /**
     * 인물의 얼굴 번호 (1개의 이미지에 2명이상 있을 경우 값이 0~ 시작)
     */ 
    public int faceIndex = -1;
    /**
     * 해당 인물을 FaceRecognition 앨범에 넣었는가에 대한 값.<br>
     * 인물의 FaceRecognition에 영향을 미친다.
     */
    public boolean isUpdatePerson = false;
    /**
     * 해당 인물이 대표 사진인지에 대한 값.
     */
    public boolean isRepresentPerson = false;
    /**
     * 해당 얼굴의 왼쪽 눈 깜빡임 정도 (0 ~ 100)<br>
     * only Snapdragon SDK
     */
    public int leftEyeBlink = 0;
    /**
     * 해당 얼굴의 오른쪽 눈 깜빡임 정도 (0 ~ 100)<br>
     * only Snapdragon SDK
     */
    public int rightEyeBlink = 0;
    /**
     * 해당 얼굴이 위를 바라보는지 아래를 바라보는지 정도 (0 ~ 100)<br>
     * only Snapdragon SDK
     */
    public int pitch = 0;
    /**
     * 얼굴의 기울임 정도 (0 ~ 100)<br>
     * only Snapdragon SDK
     */
    public int roll = 0;
    /**
     * 얼굴이 좌측을 바라보는지 우측을 바라보는지 정도 (0 ~ 100)<br>
     * only Snapdragon SDK
     */
    public int yaw = 0;
    /**
     * 얼굴의 웃는 정도 (0 ~ 100)<br>
     * only Snapdragon SDK
     */
    public int smileValue = 0;

    public ImageFaceData() {

    }
}
