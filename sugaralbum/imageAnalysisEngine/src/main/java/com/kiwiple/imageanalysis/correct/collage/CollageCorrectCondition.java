
package com.kiwiple.imageanalysis.correct.collage;

import com.kiwiple.imageanalysis.search.ImageSearchDetailCondition;

/**
 * 이미지 검색 시 콜라주에 대한 조건을 설정하는 클래스.
 */
public class CollageCorrectCondition extends ImageSearchDetailCondition {

    /**
     * 
     */
    private static final long serialVersionUID = 2114866375744558140L;
    public static final int COLLAGE_DEFAULT_IMAGE_SIZE = 1200;
    public static final int COLLAGE_DEFAULT_FRAME_COUNT = 3;

    private int mCollageImageWidth = 0;
    private int mCollageFrameCount = 0;

    /**
     * 생성자 <br>
     * 기본값을 대입하여 생성함. <br>
     * 콜라주 프레임 갯수 3개, 콜라주 이미지의 가로 길이 1200으로 조건을 설정하여 생성.
     */
    public CollageCorrectCondition() {
        mCollageFrameCount = COLLAGE_DEFAULT_FRAME_COUNT;
        mCollageImageWidth = COLLAGE_DEFAULT_IMAGE_SIZE;

        super.setDefaultValue();
    }

    /**
     * 생성자 <br>
     * 
     * @param collageFrameCount 필요한 콜라주 프레임 갯수
     * @param collageImageWidth 생성될 콜라주 이미지의 가로 길이 (세로 길이는 가로에 맞춰서 설정된다)
     */
    public CollageCorrectCondition(int collageFrameCount, int collageImageWidth) {
        mCollageFrameCount = collageFrameCount;
        if(mCollageFrameCount < 2) {
            mCollageFrameCount = COLLAGE_DEFAULT_FRAME_COUNT;
        }
        mCollageImageWidth = collageImageWidth;
        if(mCollageImageWidth < 1) {
            mCollageImageWidth = COLLAGE_DEFAULT_IMAGE_SIZE;
        }

        super.setDefaultValue();
    }

    /**
     * 설정된 콜라주 이미지의 가로 길이 반환.
     * 
     * @return int 설정된 콜라주 이미지의 가로 길이
     */
    public int getCollageImageWidth() {
        return mCollageImageWidth;
    }

    /**
     * 설정된 콜라주 프레임 갯수 반환.
     * 
     * @return int 설정된 콜라주 프레임 갯수
     */
    public int getCollageFrameCount() {
        return mCollageFrameCount;
    }
}
