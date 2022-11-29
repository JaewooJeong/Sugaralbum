
package com.kiwiple.imageanalysis.correct;

import java.io.Serializable;
import java.util.ArrayList;

import android.graphics.Paint.Style;
import android.text.TextUtils;

import com.kiwiple.imageanalysis.database.FacePointF;

/**
 * 스티커 1장에 대한 보정 정보를 가지고 있는 구조체 클래스
 */
public class ImageCorrectStickerData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7478236291168140170L;

    /**
     * Asset에 들어있는 스티커 파일 이름
     */
    public String stickerFileName;
    /**
     * 움직이는 스티커 파일 이름 배열
     */
    public ArrayList<String> stickerAnimatedFileNames = new ArrayList<String>();
    /**
     * 스티커 이동 좌표
     */
    public FacePointF stickerCoordinate = new FacePointF(0.f, 0.f);
    public float stickerWidth;
    public float stickerHeight;
    /**
     * 스티커 배율
     */
    public float stickerScale = 1.f;
    /**
     * 스티커 회전 정도
     */
    public int stickerRotate = 0;
    /**
     * 스티커 대분류
     */
    public String stickerCategory;
    /**
     * 스티커 소분류
     */
    public String stickerSubCategory;
    /**
     * 해당 스티커를 추천받을 때 이미지의 가로 길이 (배율 계산에 필요)
     */
    public int imageWidth;
    /**
     * 해당 스티커를 추천받을 때 이미지의 로 길이 (배율 계산에 필요)
     */
    public int imageHeight;

    /**
     * 콜라주 텍스트의 문자열
     * 
     * @version 2.0
     */
    public String text;

    /**
     * 콜라주 텍스트의 글자 색상
     * 
     * @version 2.0
     */
    public int fontColor;

    /**
     * 텍스트 바깥 테두리 색상
     * 
     * @version 2.0
     */
    public int textBorderColor;

    /**
     * 텍스트 가로 길이
     * 
     * @version 2.0
     */
    public float textWidth;

    /**
     * 텍스트 바깥 테두리 두께
     * 
     * @version 2.0
     */
    public float textBorderWidth;

    private int textStyle;

    /**
     * 텍스트 폰트 파일 경로
     * 
     * @version 2.0
     */
    public String typeFaceFilePath;

    /**
     * 텍스트 스티커인지 여부 반환
     * 
     * @return 텍스트 스티커 여부
     */
    public boolean isTextSticker() {
        if(TextUtils.isEmpty(stickerFileName) && stickerAnimatedFileNames.isEmpty()) {
            return true;
        }
        return false;
    }

    /**
     * 텍스트 스타일 값을 반환
     * 
     * @return 텍스트 스타일 값
     */
    public int getTextStyleValue() {
        return textStyle;
    }

    /**
     * 텍스트 스타일 값을 설정
     * 
     * @param textStyle 설정할 스타일 값
     */
    public void setTextStyleValue(int textStyle) {
        this.textStyle = textStyle;
    }

    /**
     * 텍스트 스타일을 설정
     * 
     * @param style 설정할 스타일
     */
    public void setTextStyle(Style style) {
        if(style.equals(Style.FILL)) {
            textStyle = 0;
        } else if(style.equals(Style.STROKE)) {
            textStyle = 1;
        } else if(style.equals(Style.FILL_AND_STROKE)) {
            textStyle = 2;
        } else {
            textStyle = 1;
        }
    }

    /**
     * 텍스트 스타일 반환
     * 
     * @return 텍스트 스타일
     */
    public Style getTextStyle() {
        Style style;
        switch(textStyle) {
            case 0:
                style = Style.FILL;
                break;
            case 1:
                style = Style.STROKE;
                break;
            case 2:
                style = Style.FILL_AND_STROKE;
                break;
            default:
                style = Style.FILL;
                break;
        }
        return style;
    }

    /**
     * Scale값에 따른 스티커의 위치를 반환
     * 
     * @param layoutScale Scale
     * @return 스티커의 좌표
     */
    public FacePointF getStickerCoordinate(float layoutScale) {
        FacePointF point = new FacePointF(0.f, 0.f);
        point.x = stickerCoordinate.x * layoutScale - stickerWidth
                * (1f - stickerScale * layoutScale) / 2f;
        point.y = stickerCoordinate.y * layoutScale - stickerHeight
                * (1f - stickerScale * layoutScale) / 2f;
        return point;
    }

    /**
     * 스티커의 좌표와 scale을 적용
     * 
     * @param layoutScale 스케일
     * @param x x좌표
     * @param y y좌표
     */
    public void setStickerCoordinate(float layoutScale, float x, float y) {
        stickerCoordinate.x = (x + stickerWidth * (1f - stickerScale / layoutScale) / 2f)
                * layoutScale;
        stickerCoordinate.y = (y + stickerHeight * (1f - stickerScale / layoutScale) / 2f)
                * layoutScale;
    }
}
