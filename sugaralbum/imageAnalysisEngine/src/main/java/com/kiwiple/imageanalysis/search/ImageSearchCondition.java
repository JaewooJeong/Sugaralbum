
package com.kiwiple.imageanalysis.search;

import java.io.Serializable;

/**
 * ImageAnalysis를 통해 분석된 내용을 기반으로 사진을 읽어오기 위한 특정 조건을 정의하는 클래스.<br>
 * 조건은 일반조건과 상세조건으로 분류된다. <br>
 * 일반조건 : 사진 갯수(콜라주 비적용), 해상도, 콜라주 유무 및 갯수, 각 콜라주의 사진갯수. <br>
 * 상세분류 조건 : 시간, 위치, 얼굴, 색상, 퀄리티 정의 <br>
 */
public class ImageSearchCondition implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1504573312796487921L;
    public static final int DEFAULT_SEARCH_COUNT = -1;
    public static final int DEFAULT_SEARCH_IMAGE_SIZE = 640;

    /**
     * 일반 조건
     */
    // 필요한 이미지 갯수
    private int mImageSearchCount;
    // 필요한 이미지의 해상도 값 (긴 값을 기준으로 함)
    private int mImageLongSize;
    private ImageSearchDetailCondition mSearchDetailCondition;

    /**
     * 이미지 검색시 일반 조건을 설정하기 위한 생성자. <br>
     * 
     * @param searchCount 필요한 이미지의 갯수. 검색 조건에 의한 결과과 갯수보다 적을 수 있음.
     * @param imageLongSize 필요한 이미지의 최저 해상도 값. Width 과 Height 중에 작은 값이 이 값보다 크거나 같은 이미지를 검색한다.
     */
    public ImageSearchCondition(int searchCount, int imageLongSize) {
        mImageSearchCount = searchCount;
        if(mImageSearchCount <= 0) {
            mImageSearchCount = DEFAULT_SEARCH_COUNT;
        }
        mImageLongSize = imageLongSize;
        if(mImageLongSize <= 0) {
            mImageLongSize = DEFAULT_SEARCH_IMAGE_SIZE;
        }
    }

    /**
     * 검색 이미지 갯수를 반환.
     * 
     * @return int 검색할 이미지의 갯수
     */
    public int getImageSearchCount() {
        return mImageSearchCount;
    }

    /**
     * 검색 이미지 갯수를 설정.
     * 
     * @param searchCount 검색할 이미지의 갯수
     */
    public void setImageSearchCount(int searchCount) {
        mImageSearchCount = searchCount;
        if(mImageSearchCount <= 0) {
            mImageSearchCount = DEFAULT_SEARCH_COUNT;
        }
    }

    /**
     * 최저 이미지 해상도를 반환.
     * 
     * @return 최저 이미지 해상도
     */
    public int getImageLongsize() {
        return mImageLongSize;
    }

    /**
     * 최저 이미지 해상도를 설정
     * 
     * @param imageLongSize 필요한 이미지의 최저 해상도 값. Width 과 Height 중에 작은 값이 이 값보다 크거나 같은 이미지를 검색한다.
     */
    public void setImageLongsize(int imageLongSize) {
        mImageLongSize = imageLongSize;
        if(mImageLongSize <= 0) {
            mImageLongSize = DEFAULT_SEARCH_IMAGE_SIZE;
        }
    }

    /**
     * 이미지 검색 상세 조건을 반환.
     * 
     * @return ImageSearchDetailCondtion 설정된 상세 조건
     */
    public ImageSearchDetailCondition getImageSearchDetailCondition() {
        return mSearchDetailCondition;
    }

    /**
     * 검색시 상세조건을 설정한다.<br>
     * 
     * @param imageSearchDetailCondition 설정된 상세 조건
     */
    public void setImageSearchDetailCondition(ImageSearchDetailCondition imageSearchDetailCondition) {
        this.mSearchDetailCondition = imageSearchDetailCondition;
    }
}
