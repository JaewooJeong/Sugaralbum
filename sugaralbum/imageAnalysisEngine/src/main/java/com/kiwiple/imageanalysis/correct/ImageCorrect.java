
package com.kiwiple.imageanalysis.correct;

import java.util.ArrayList;

import android.content.Context;

import com.kiwiple.imageanalysis.correct.collage.CollageCorrectAdviser;
import com.kiwiple.imageanalysis.correct.filter.FilterCorrectAdviser;
import com.kiwiple.imageanalysis.correct.filter.FilterCorrectCondition;
import com.kiwiple.imageanalysis.correct.sticker.StickerCorrectAdviser;
import com.kiwiple.imageanalysis.database.ImageData;

/**
 * 이미지 보정 데이터를 받아오는 클래스. <br>
 * 이미지 배열과 조건을 인자로 받아 이미지 데이터의 보정 데이터를 계산하여 반환한다.
 */
public class ImageCorrect {

    private Context mApplicationContext;

    private ArrayList<ImageData> mImageDatas;
    private ImageCorrectCondition mImageCorrectCondition;

    /**
     * 생성자
     * 
     * @param applicationContext ApplicationContext
     * @param imageDatas 보정 데이터를 얻을 이미지 정보 배열
     * @param imageCorrectCondition 보정 데이터의 조건
     */
    public ImageCorrect(Context applicationContext, ArrayList<ImageData> imageDatas,
            ImageCorrectCondition imageCorrectCondition) {
        mApplicationContext = applicationContext;
        mImageDatas = imageDatas;
        mImageCorrectCondition = imageCorrectCondition;
    }

    /**
     * 이미지 배열에서 보정 데이터 조건을 적용해 보정 데이터를 얻어온다. <br>
     * 생성시 이미지 정보 배열이 null이거나 비어있다면 null을 반환
     * 
     * @return ArrayList 보정 데이터가 포함되어있는 이미지 데이터 배열
     */
    public ArrayList<ImageData> getImageCorrectData(String collageJsonPath, boolean isAsset) {

        // 이미지 데이터가 없다면 null을 반환
        if(mImageDatas == null || mImageDatas.isEmpty()) {
            return null;
        }

        /**
         * 필터
         */
        // 설정된 필터 조건을 가져온다.
        FilterCorrectCondition filterCorrectCondtion = mImageCorrectCondition.getFilterCorrectCondition();
        FilterCorrectAdviser filterCorrectAdviser = new FilterCorrectAdviser(mApplicationContext);
        int filterCorrectType = filterCorrectCondtion.getDefaultCorrectCondition();
        // 대표 추천 필터 (1개로 적용시에만 구한다)
        int representFilterId = -1;
        if(filterCorrectType == FilterCorrectCondition.FILTER_CORRECT_DEFAULT_ENTIRE_ONE) {
            representFilterId = filterCorrectAdviser.getRepresentFilter(mImageDatas);
        }

        /**
         * 콜라주
         */
        // 설정된 콜라주 조건을 가져온다.
        // CollageCorrectCondition collageCorrectCondition =
        // mImageCorrectCondition.getCollageCorrectCondition();
        CollageCorrectAdviser collageCorrectAdviser = new CollageCorrectAdviser(mApplicationContext);
        ArrayList<ArrayList<ImageData>> collageCorrectImageDatas = collageCorrectAdviser.getCollageCorrectData(mImageDatas,
                                                                                                               2,
                                                                                                               "",
                                                                                                               1200);

        /**
         * 스티커
         */
        // 스티커 관련 설정
        StickerCorrectAdviser stickerCorrectAdviser = null;
        if(mImageCorrectCondition.getIsAddStickerCorrectData()) {
            stickerCorrectAdviser = new StickerCorrectAdviser(mApplicationContext);
        }

        /**
         * 보정 데이터 추가
         */
        // 각 이미지에 어울리는 보정 필터를 설정
        for(int i = 0; i < mImageDatas.size(); i++) {
            ImageData imageData = mImageDatas.get(i);
            ImageCorrectData imageCorrectData = imageData.imageCorrectData;
            // 필터 추천
            if(filterCorrectType == FilterCorrectCondition.FILTER_CORRECT_DEFAULT_EACH) {
                // 필터를 각각 추천해야한다면 가져온다.
                imageCorrectData.filterId = filterCorrectAdviser.getRecommendCorrectFilter(imageData);
            } else if(filterCorrectType == FilterCorrectCondition.FILTER_CORRECT_DEFAULT_ENTIRE_ONE) {
                imageCorrectData.filterId = representFilterId;
            }

            // 콜라주 추천
            for(int j = 0; j < collageCorrectImageDatas.size(); j++) {
                ArrayList<ImageData> collageImageDatas = collageCorrectImageDatas.get(j);
                if(collageImageDatas != null) {
                    for(int k = 0; k < collageImageDatas.size(); k++) {
                        ImageData collageImageData = collageImageDatas.get(k);
                        if(collageImageData.id == imageData.id) {
                            imageCorrectData.collageTempletId = collageImageData.imageCorrectData.collageTempletId;
                            imageCorrectData.collageCoordinate = collageImageData.imageCorrectData.collageCoordinate;
                            imageCorrectData.collageRotate = collageImageData.imageCorrectData.collageRotate;
                            imageCorrectData.collageScale = collageImageData.imageCorrectData.collageScale;
                        }
                    }
                }
            }

            // 스티커 추천을 해야한다면 추천
            if(mImageCorrectCondition.getIsAddStickerCorrectData()) {
                stickerCorrectAdviser.setStickerCorrectData(imageData);
            }
        }

        return mImageDatas;
    }
}
