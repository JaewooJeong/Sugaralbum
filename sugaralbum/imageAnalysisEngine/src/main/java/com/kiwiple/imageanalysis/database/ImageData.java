
package com.kiwiple.imageanalysis.database;

import android.util.Log;

import com.kiwiple.imageanalysis.correct.ImageCorrectData;
import com.kiwiple.imageframework.util.BitmapUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * 이미지 1개에 대한 정보를 담고 있는 클래스<br>
 * 본 SDK에서는 이 클래스를 이미지의 기본정보 클래스로 삼고 있다.
 */
public class ImageData implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 7445370594328074478L;
    
    
    /**
     * ImageData의 용도가 CollageScene. {@link #dataType}에서 사용
     */
    public static final int DATA_TYPE_COLLAGE = 0;
    /**
     * ImageData의 용도가 MultiLayterScene. {@link #dataType}에서 사용
     */
    public static final int DATA_TYPE_MULTI_LAYER = 1;
    
    /**
     * 갤러리DB에 할당된 이미지 고유 아이디 값
     */
    public int id;
    /**
     * 실제 분석이 이루어졌는지에 대한 데이터 (Count를 세기 위함으로 DB등에서는 쓰이지 않는다)
     */
    public boolean isAnalysisData = false;
    /**
     * 해당 이미지가 속한 앨범 이름
     */
    public String albumName;
    /**
     * 이미지가 저장된 날짜 (long 형태의 값)
     */
    public long date;
    /**
     * 이미지가 저장된 날짜 (yyyy-MM-dd 형태의 String값)
     */
    public String dateFormat;
    /**
     * 이미지가 추가된 날짜 (date가 없을 시 사용)
     */
    public long dateAdded;
    /**
     * 파일명 (확장자 미포함)
     */
    public String fileName;
    /**
     * 파일 크기
     */
    public int fileSize;
    /**
     * 이미지 path
     */
    public String path;
    /**
     * 이미지의 MimeType
     */
    public String mimeType;
    /**
     * 이미지 위도 값
     */
    public String latitude;
    /**
     * 이미지 경도 값
     */
    public String longitude;
    /**
     * 이미지 회전 값
     */
    public String orientation;
    /**
     * 이미지 가로 길이
     */
    public int width;
    /**
     * 이미지 세로 길이
     */
    public int height;
    /**
     * 이미지 위치 태그 값. Full주소 중 번지수를 제외한 가장 상세한 주소<br>
     * (ex : 서초3동, 사당동 등)
     */
    public String addressShortName;
    /**
     * 이미지 위치 태그 값. 주소 전체.<br>
     * (ex : 대한민국 서울특별시 서초구 서초 3동 등)
     */
    public String addressFullName;
    /**
     * 이미지 위치 태그 값. 국가.<br>
     * (ex : 대한민국, 미국, 일본 등)
     */
    public String addressCountry;
    /**
     * 이미지 위치 태그 값. (국가 시/도)<br>
     * (ex : 대한민국 서울특별시, 미국 텍사스주, 일본 후쿠오카현 등)
     */
    public String addressCity;
    /**
     * 이미지 위치 태그 값. (국가 시/도 시/군/구)<br>
     * (ex : 대한민국 서울특별시 서초구, 일본 사가현 니시마츠우라군 등)
     */
    public String addressDistrict;
    /**
     * 이미지 위치 태그 값. (국가 시/도 시/군/구 읍/면/동)<br>
     * (ex : 대한민국 서울특별시 서초구 서초3동, 대한민국 경기도 김포시 북변동 등)
     */
    public String addressTown;
    /**
     * 이미지의 얼굴 갯수
     */
    public int numberOfFace;
    /**
     * FaceDetecting시 얼마나 Scale하여 Detecting했는지 값
     */
    public float faceBitmapScale;
    /**
     * FaceDetecting시 비트맵 가로 길이
     */
    public int faceBitmapWidth;
    /**
     * FaceDetecting시 비트맵 세로 길이
     */
    public int faceBitmapHeight;
    /**
     * 얼굴 정보 객체 배열
     */
    public ArrayList<ImageFaceData> faceDataItems;
    /**
     * 얼굴들의 평균 웃는 정도 (0 ~ 100) <br>
     * Snapdragon SDK 전용
     */
    public float avgSmileValue = 0.f;
    /**
     * 얼굴들의 평균 왼쪽 눈 깜박임 정도 (0 ~ 100) <br>
     * Snapdragon SDK 전용
     */
    public float avgLeftBlinkValue = 0.f;
    /**
     * 얼굴들의 평균 오른쪽 눈 깜박임 정도 (0 ~ 100) <br>
     * Snapdragon SDK 전용
     */
    public float avgRightBlinkValue = 0.f;
    /**
     * 얼굴 분석이 끝났는지 여부
     */
    public boolean isFinishFaceDetecting;
    /**
     * 이미지 종합 점수
     */
    public int totalScore;
    /**
     * 이미지 퀄리티 점수
     */
    public int qualityScore;
    /**
     * 이미지 선명도 점수
     */
    public int sharpnessScore;
    /**
     * 이미지 컬러셋 (이미지를 4x4로 분할하여 각 영역별 대표 색상 문자열)<br>
     * 각 영역의 구분은 " "(띄어쓰기)로 한다. <br>
     * Jpeg 형태의 이미지만이 가능하다. <br>
     * ex)
     * "6D482D 9DAF2D 20D5FF 24EE2A 2672EA 6D482D 9DAF2D 20D5FF 24EE2A 2672EA 6D482D 9DAF2D 20D5FF"
     */
    public String colorSet;
    /**
     * 이미지 전체의 대표색<br>
     * colorSet의 4x4의 대표색을 구해 가장 빈도수가 많은 색상을 나타냄. <br>
     * colorSet이 있을 경우(Jpeg 포멧)에만 값이 있다.
     */
    public String representColorName;
    /**
     * colorSet의 대표색 컬러 갯수 (갯수가 많을 수록 알록달록한 사진이라 판단!) colorSet이 있을 경우(Jpeg 포멧)에만 값이 있다.
     */
    public int numberOfRepresentColor;
    /**
     * colorSet의 4x4영역의 대표 색상값들의 평균 밝기 값.<br>
     * colorSet이 있을 경우(Jpeg 포멧)에만 값이 있다.
     */
    public int brightnessValue;
    /**
     * 이미지의 색상분석이 끝났는지 여부 (중복 분석 방지를 위함)
     */
    public boolean isFinishColorsetAnalysis;
    
    /**
     * ImageData의 용도
     * 
     * @see {@link #DATA_TYPE_COLLAGE}<br>
     *      {@link #DATA_TYPE_MULTI_LAYER}
     */
    public int dataType = DATA_TYPE_COLLAGE;

    /**
     * 이미지 보정 데이터 정보
     */
    public ImageCorrectData imageCorrectData = new ImageCorrectData();
    
    public boolean isCollageVisible = true;

    public ImageData() {
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ImageData && ((ImageData)o).id == id;
    }

    /**
     * 가로 이미지인지 여부 반환
     * 
     * @return 가로 이미지인지 여부
     */
    public boolean isPotraitImage() {
        try {
            int rotation = BitmapUtils.getImageRotation(path);
            if(rotation == 0 || rotation == 180) {
                if(height > width) {
                    return true;
                }
            } else {
                if(height < width) {
                    return true;
                }
            }
        } catch(IOException e) {
            Log.e("#Test", e.getMessage());
        }
        return false;
    }
}
