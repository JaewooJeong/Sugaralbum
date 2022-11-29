
package com.kiwiple.imageanalysis.correct;

import java.io.Serializable;
import java.util.ArrayList;

import com.kiwiple.imageanalysis.database.FacePointF;

/**
 * 1개의 이미지의 보정 데이터 구조 클래스.
 */
public class ImageCorrectData implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 6885177072014196503L;

    /**
     * 보정 필터 아이디.<br>
     * 없을 경우 -1
     */
    public int filterId = -1;

    /**
     * 스티커에 대한 보정 정보
     */
    public ArrayList<ImageCorrectStickerData> stickerCorrectDataArray;

    /**
     * 콜라주 템플릿 고유 번호
     */
    public int collageTempletId;
    /**
     * 콜라주 내부의 이동 좌표
     */
    public FacePointF collageCoordinate = new FacePointF(0.f, 0.f);
    /**
     * 콜라주 프레임 내의 배율
     */
    public float collageScale = 1.f;
    /**
     * 콜라주 프레임 내의 회전정도
     */
    public float collageRotate = 0;
    /**
     * 콜라주 프레임 가로 길이
     */
    public int collageWidth = 0;
    /**
     * 콜라주 프레임 세로 길이
     */
    public int collageHeight = 0;

    /**
     * 콜라주 테두리 두께
     */
    public float collageFrameBorderWidth = 0;
    /**
     * 콜라주 모서리 둥근 정도
     */
    public float collageFrameCornerRadius = 0;
    /**
     * 콜라주 배경 색상 값
     */
    public int collageBackgroundColor = 0;
    /**
     * 콜라주 배경 컬러 태그 값
     */
    public String collageBackgroundColorTag = null;
    /**
     * 콜라주 배경 이미지 파일 이름
     */
    public String collageBackgroundImageFileName = null;
    
    public long videoStartPosition = 0l;
    public long videoEndPosition = 0l;
    public int videoDuration = 0;

    /**
     * 생성자
     */
    public ImageCorrectData() {

    }

    /**
     * 콜라주 데이터를 초기화 시킨다.
     */
    public void initailizeCollageData() {
        collageCoordinate = new FacePointF(0.f, 0.f);
        collageRotate = 0.f;
        collageScale = 1.f;
    }

    /**
     * Scale에 따른 콜라주 좌표를 반환
     * 
     * @param layoutScale Scale
     * @return 콜라주 좌표
     */
    public FacePointF getCollageCoordinate(float layoutScale) {
        FacePointF point = new FacePointF(0.f, 0.f);
        point.x = collageCoordinate.x - collageWidth * (1f - collageScale) / 2f;
        point.y = collageCoordinate.y - collageHeight * (1f - collageScale) / 2f;
        return point;
    }

    /**
     * Scale과 콜라주 좌표 값을 설정
     * 
     * @param layoutScale Scale
     * @param x x좌표
     * @param y y좌표
     */
    public void setCollageCoordinate(float layoutScale, float x, float y) {
        collageCoordinate.x = (x + collageWidth * (1f - collageScale / layoutScale) / 2f)
                * layoutScale;
        collageCoordinate.y = (y + collageHeight * (1f - collageScale / layoutScale) / 2f)
                * layoutScale;
    }
}
