
package com.kiwiple.imageanalysis.correct;

import com.kiwiple.imageanalysis.correct.collage.CollageCorrectCondition;
import com.kiwiple.imageanalysis.correct.filter.FilterCorrectCondition;

/**
 * 이미지 보정 데이터 조건을 정의하는 클래스.<br>
 * 이미지 보정의 종류에는 "필터", "스티커", "콜라주" 조건이 있다.
 */
public class ImageCorrectCondition {

    /**
     * 스티커 이미지 기본 사이즈 (스티커를 붙일 때 배경이 되는 이미지의 사이즈)
     */
    private static final int DEFAULT_STICKER_IMAGE_SIZE = 1200;

    // 필터 조건
    private FilterCorrectCondition mFilterCorrectCondition;
    // 콜라주 조건
    private CollageCorrectCondition mCollageCorrectCondition;
    // 스티커 조건
    private boolean mIsAddStickerCorrectData = false;
    private int mStickerImageSize = DEFAULT_STICKER_IMAGE_SIZE;

    /**
     * 이미지 보정 조건 생성자
     * 
     * @param filterCorrectCondition 필터 관련 조건 설정 클래스
     * @param collageCorrectCondition 콜라주 관련 조건 설정 클래스
     * @param isAddStickerCorrectData 스티커 보정 데이터를 추가할 지 여부
     * @param stickerImageSize 스티커 보정 데이터를 추가할 경우 배경이 되는 이미지 사이즈 (0이하일 경우 Default 값은 800으로 설정된다)
     */
    public ImageCorrectCondition(FilterCorrectCondition filterCorrectCondition,
            CollageCorrectCondition collageCorrectCondition, boolean isAddStickerCorrectData,
            int stickerImageSize) {
        if(filterCorrectCondition != null) {
            mFilterCorrectCondition = filterCorrectCondition;
        } else {
            // 필터 보정조건이 null이라면 미적용을 기본으로 조건을 생성한다.
            mFilterCorrectCondition = new FilterCorrectCondition(
                                                                 FilterCorrectCondition.FILTER_CORRECT_DEFAULT_NONE);
        }

        if(collageCorrectCondition != null) {
            mCollageCorrectCondition = collageCorrectCondition;
        } else {
            // 콜라주 보정조건이 null이라면 0으로 입력 (콜라주를 필요로 하지 않는다.)
            mCollageCorrectCondition = new CollageCorrectCondition(
                                                                   0,
                                                                   CollageCorrectCondition.COLLAGE_DEFAULT_IMAGE_SIZE);
        }

        mIsAddStickerCorrectData = isAddStickerCorrectData;

        if(stickerImageSize < 1) {
            mStickerImageSize = DEFAULT_STICKER_IMAGE_SIZE;
        } else {
            mStickerImageSize = stickerImageSize;
        }
    }

    /**
     * 설정된 필터 보정 조건을 반환한다.
     * 
     * @return FilterCorrectCondition 설정된 보정 조건 클래스
     */
    public FilterCorrectCondition getFilterCorrectCondition() {
        return mFilterCorrectCondition;
    }

    /**
     * 필터 보정 조건을 설정한다.
     * 
     * @param filterCorrectCondition 필터 보정 조건 클래스
     */
    public void setFilterCorrectCondition(FilterCorrectCondition filterCorrectCondition) {
        mFilterCorrectCondition = filterCorrectCondition;
    }

    /**
     * 설정된 콜라주 보정 조건을 반환한다.
     * 
     * @return CollageCorrectCondition 설정된 보정 조건 클래스
     */
    public CollageCorrectCondition getCollageCorrectCondition() {
        return mCollageCorrectCondition;
    }

    /**
     * 콜라주 보정 조건을 설정한다.
     * 
     * @param collageCorrectCondition 콜라주 보정 조건 정의 클래스
     */
    public void setCollageCorrectCondition(CollageCorrectCondition collageCorrectCondition) {
        mCollageCorrectCondition = collageCorrectCondition;
    }

    /**
     * 스티커 보정 데이터를 추가할 것인지 여부 설정
     * 
     * @param isAddStickerCorrectData 스티커 보정 데이터를 추가할 것인지 여부
     */
    public void setIsAddStickerCorrectData(boolean isAddStickerCorrectData) {
        mIsAddStickerCorrectData = isAddStickerCorrectData;
    }

    /**
     * 설정된 스티커 보정 데이터 추가 여부를 반환
     * 
     * @return boolean 설정된 스티커 보정 데이터 추가 여부
     */
    public boolean getIsAddStickerCorrectData() {
        return mIsAddStickerCorrectData;
    }

    /**
     * 스티커 보정 데이터 추가시 배경이 되는 이미지의 크기.<br>
     * 이미지의 경우 Bitmap을 sampleSize해야하므로 반드시 설정된 값의 크기가 아닌 가까운 크기가 될 수 있다.<br>
     * ex) 800을 설정할 경우, 원본 사이즈가 2400이라면 sampleSize값이 3이 되나,<br>
     * sampleSize는 1외의 짝수만 가능하므로 2가 되어 1200사이즈의 이미지로 계산된다.<br>
     * 이는, StickerExecuter의 인자 값도 동일함
     * 
     * @param stickerCorrectImageSize 설정할 배경 이미지 크기
     */
    public void setStickerCorrectImageSize(int stickerCorrectImageSize) {
        mStickerImageSize = stickerCorrectImageSize;
    }

    /**
     * 설정한 스티커 보정 데이터 추가시 배경이 되는 이미지의 크기 반환.<br>
     * 이미지의 경우 Bitmap을 sampleSize해야하므로 반드시 설정된 값의 크기가 아닌 가까운 크기가 될 수 있다.<br>
     * ex) 800을 설정할 경우, 원본 사이즈가 2400이라면 sampleSize값이 3이 되나,<br>
     * sampleSize는 1외의 짝수만 가능하므로 2가 되어 1200사이즈의 이미지로 계산된다.<br>
     * 이는, StickerExecuter의 인자 값도 동일함
     */
    public int getStickerCorrectImageSize() {
        return mStickerImageSize;
    }
}
