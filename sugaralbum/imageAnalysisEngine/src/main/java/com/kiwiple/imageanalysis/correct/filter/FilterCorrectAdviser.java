
package com.kiwiple.imageanalysis.correct.filter;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import android.content.Context;

import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.utils.DateUtil;
import com.kiwiple.imageframework.filter.FilterData;

/**
 * 이미지를 다음과 같은 조건에 의해 카테고리를 분류하고 보정할 필터를 추천해주는 클래스.<br>
 * <br>
 * 카테고리는 다음과 같다. <br>
 * 우선순위 1. 얼굴 사진인가? => Category (Face) <br>
 * 우선순위 2. 어두운 사진인가? => Category (Dark) <br>
 * 우선순위 3. 선명도가 낮은 사진인가? => Category (Defocused) <br>
 * 우선순위 4. 컬러풀한 사진인가? => Category (Colorful) <br>
 * 우선순위 5. 1년 이상 이전의 사진인가? => Category (Old) <br>
 */
public class FilterCorrectAdviser {

    protected static final String FILTER_LIST = "FilterList";

    private static final String FACE_CATEGORY = "FACE";
    private static final int FACE_CATEGORY_INDEX = 1;
    private static final String DARK_CATEGORY = "DARK";
    private static final int DARK_CATEGORY_INDEX = 2;
    private static final String DEFOCUSED_CATEGORY = "DEFOCUSED";
    private static final int DEFOCUSED_CATEGORY_INDEX = 3;
    private static final String COLORFUL_CATEGORY = "COLORFUL";
    private static final int COLORFUL_CATEGORY_INDEX = 4;
    private static final String OLD_CATEGORY = "OLD";
    private static final int OLD_CATEGORY_INDEX = 5;
    private static final String NONE_CATEGORY = "NONE";
    private static final int NONE_CATEGORY_INDEX = 6;

    private static final String ASSET_FACE_FILTER = "filter/defaultFaceFilter.json";
    private static final String ASSET_DARK_FILTER = "filter/defaultDarkFilter.json";
    private static final String ASSET_DEFOCUSED_FILTER = "filter/defaultDefocusedFilter.json";
    private static final String ASSET_COLORFUL_FILTER = "filter/defaultColorfulFilter.json";
    private static final String ASSET_OLD_FILTER = "filter/defaultOldFilter.json";
    private static final String ASSET_NONE_FILTER = "filter/defaultNoneFilter.json";

    private Context mApplicationContext;

    private ArrayList<FilterData> mFaceFilterList = null;
    private ArrayList<FilterData> mDarkFilterList = null;
    private ArrayList<FilterData> mDefocusedFilterList = null;
    private ArrayList<FilterData> mColorfulFilterList = null;
    private ArrayList<FilterData> mOldFilterList = null;
    private ArrayList<FilterData> mNoneFilterList = null;

    /**
     * 생성자
     * 
     * @param applicationContext getApplicationContext()
     */
    public FilterCorrectAdviser(Context applicationContext) {
        mApplicationContext = applicationContext;
        // 필터를 셋팅
        try {
            mFaceFilterList = getFilterFromAssetFileName(ASSET_FACE_FILTER);
            mDarkFilterList = getFilterFromAssetFileName(ASSET_DARK_FILTER);
            mDefocusedFilterList = getFilterFromAssetFileName(ASSET_DEFOCUSED_FILTER);
            mColorfulFilterList = getFilterFromAssetFileName(ASSET_COLORFUL_FILTER);
            mOldFilterList = getFilterFromAssetFileName(ASSET_OLD_FILTER);
            mNoneFilterList = getFilterFromAssetFileName(ASSET_NONE_FILTER);
        } catch(JsonParseException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 필터 대표 카테고리 목록을 반환한다.<br>
     * 카테고리의 마지막에는 NONE(어떠한 카테고리에도 속하지 않음) 카테고리가 포함되어있다.
     * 
     * @return ArrayList 필터 대표 카테고리 목록
     */
    public static ArrayList<String> getFilterCorrectCategoryNames() {
        ArrayList<String> filterCategoryNames = new ArrayList<String>();
        filterCategoryNames.add(FACE_CATEGORY);
        filterCategoryNames.add(DARK_CATEGORY);
        filterCategoryNames.add(DEFOCUSED_CATEGORY);
        filterCategoryNames.add(COLORFUL_CATEGORY);
        filterCategoryNames.add(OLD_CATEGORY);
        filterCategoryNames.add(NONE_CATEGORY);
        return filterCategoryNames;
    }

    /**
     * 카테고리에 따른 필터 정보를 반환한다.
     * 
     * @param categoryName 필터 정보를 얻기 위한 카테고리
     * @return ArrayList 카테고리 포함되어 있는 필터들의 정보 배열
     */
    public ArrayList<FilterData> getFilterList(String categoryName) {
        if(categoryName.equals(FACE_CATEGORY)) {
            return mFaceFilterList;
        } else if(categoryName.equals(DARK_CATEGORY)) {
            return mDarkFilterList;
        } else if(categoryName.equals(DEFOCUSED_CATEGORY)) {
            return mDefocusedFilterList;
        } else if(categoryName.equals(COLORFUL_CATEGORY)) {
            return mColorfulFilterList;
        } else if(categoryName.equals(OLD_CATEGORY)) {
            return mOldFilterList;
        } else {
            return mNoneFilterList;
        }
    }

    /**
     * 필터 카테고리 목록의 고유 번호를 반환한다.
     * 
     * @param categoryName 필터 카테고리 이름
     * @return int 필터 카테고리 이름에 따른 고유 번호
     */
    public static int getFilterCategoryIndex(String categoryName) {
        int categoryIndex = NONE_CATEGORY_INDEX;
        if(categoryName.equals(FACE_CATEGORY)) {
            categoryIndex = FACE_CATEGORY_INDEX;
        } else if(categoryName.equals(DARK_CATEGORY)) {
            categoryIndex = DARK_CATEGORY_INDEX;
        } else if(categoryName.equals(DEFOCUSED_CATEGORY)) {
            categoryIndex = DEFOCUSED_CATEGORY_INDEX;
        } else if(categoryName.equals(COLORFUL_CATEGORY)) {
            categoryIndex = COLORFUL_CATEGORY_INDEX;
        } else if(categoryName.equals(OLD_CATEGORY)) {
            categoryIndex = OLD_CATEGORY_INDEX;
        } else {
            categoryIndex = NONE_CATEGORY_INDEX;
        }
        return categoryIndex;
    }

    /**
     * 보정할 이미지 데이터에 필터를 추천해준다. <br>
     * 현재는 카테고리 목록 중 랜덤하게 1개를 뽑아서 추천해준다.
     * 
     * @param imageData 보정할 이미지 데이터
     * @return int 추천받은 필터 고유번호. 추천 필터가 없을 경우 -1을 반환.
     */
    public int getRecommendCorrectFilter(ImageData imageData) {
        // 이미지 데이터로 카테고리를 구분하고
        String categoryName = getRecommendFilterCategory(imageData);
        return getRecommendCorrectFilter(categoryName);
    }

    /**
     * 필터 카테고리에서 필터를 하나 추천해준다.
     * 
     * @param categoryName 추천받을 필터 카테고리 이름
     * @return int 추천받은 필터 고유번호. 추천 필터가 없을 경우 -1을 반환.
     */
    public int getRecommendCorrectFilter(String categoryName) {
        FilterData recommendFilterData = null;
        // 우선 랜덤 인덱스로 추천해준다.
        int randomIndex = 0;
        if(categoryName.equals(FACE_CATEGORY)) {
            if(mFaceFilterList != null && !mFaceFilterList.isEmpty()) {
                randomIndex = (int)(Math.random() * mFaceFilterList.size());
                recommendFilterData = mFaceFilterList.get(randomIndex);
            }
        } else if(categoryName.equals(DARK_CATEGORY)) {
            if(mDarkFilterList != null && !mDarkFilterList.isEmpty()) {
                randomIndex = (int)(Math.random() * mDarkFilterList.size());
                recommendFilterData = mDarkFilterList.get(randomIndex);
            }
        } else if(categoryName.equals(DEFOCUSED_CATEGORY)) {
            if(mDefocusedFilterList != null && !mDefocusedFilterList.isEmpty()) {
                randomIndex = (int)(Math.random() * mDefocusedFilterList.size());
                recommendFilterData = mDefocusedFilterList.get(randomIndex);
            }
        } else if(categoryName.equals(COLORFUL_CATEGORY)) {
            if(mColorfulFilterList != null && !mColorfulFilterList.isEmpty()) {
                randomIndex = (int)(Math.random() * mColorfulFilterList.size());
                recommendFilterData = mColorfulFilterList.get(randomIndex);
            }
        } else if(categoryName.equals(OLD_CATEGORY)) {
            if(mOldFilterList != null && !mOldFilterList.isEmpty()) {
                randomIndex = (int)(Math.random() * mOldFilterList.size());
                recommendFilterData = mOldFilterList.get(randomIndex);
            }
        } else if(categoryName.equals(NONE_CATEGORY)) {
            if(mNoneFilterList != null && !mNoneFilterList.isEmpty()) {
                randomIndex = (int)(Math.random() * mNoneFilterList.size());
                recommendFilterData = mNoneFilterList.get(randomIndex);
            }
        }

        if(recommendFilterData != null) {
            return recommendFilterData.mServerId;
        } else {
            return -1;
        }
    }

    /**
     * 전체 이미지 데이터를 기반으로 대표 필터를 1개 추출한다. <br>
     * 개별 이미지 데이터를 분석하여 카테고리로 분류하고, 가장 많은 카테고리의 필터 중 랜덤으로 추천한다. <br>
     * 
     * @param imageDatas 이미지 데이터 배열
     * @return int 추천받은 필터 고유번호. 추천 필터가 없을 경우 -1을 반환.
     */
    public int getRepresentFilter(ArrayList<ImageData> imageDatas) {
        if(imageDatas.isEmpty()) {
            return -1;
        }

        int[] counts = new int[getFilterCorrectCategoryNames().size()];
        // 초기화
        for(int i = 0; i < counts.length; i++) {
            counts[i] = 0;
        }

        // 각 카테고리별 카운트 계산
        for(int i = 0; i < imageDatas.size(); i++) {
            ImageData imageData = imageDatas.get(i);
            String representCategoryName = getRecommendFilterCategory(imageData);
            int categoryIndex = getFilterCategoryIndex(representCategoryName);
            counts[categoryIndex] += 1;
        }

        // 가장 많은 카테고리를 찾자.
        String maxCountCategoryName = NONE_CATEGORY;
        int maxCountCategoryCount = 0;
        int maxCountCateogyIndex = NONE_CATEGORY_INDEX;
        for(int i = 0; i < counts.length; i++) {
            // 개별 카테고리 숫자
            int categoryCount = counts[i];
            if(categoryCount > maxCountCategoryCount) {
                maxCountCategoryCount = categoryCount;
                maxCountCateogyIndex = i;
            }
        }
        maxCountCategoryName = getFilterCorrectCategoryNames().get(maxCountCateogyIndex);
        return getRecommendCorrectFilter(maxCountCategoryName);
    }

    /**
     * 이미지 데이터를 기반으로 필터 카테고리를 분류하여 반환
     * 
     * @param imageData 이미지 데이터
     * @return String 카테고리 이름
     */
    public String getRecommendFilterCategory(ImageData imageData) {
        String categoryName = null;

        if(imageData == null) {
            return categoryName;
        }

        // 오늘 날짜와 1년전 날짜를 구해오자
        long oneYearAgoTime = DateUtil.getDateTimeAgoYear(1);

        if(imageData.numberOfFace > 0) {
            // 우선순위 1 : 인물 사진이라면
            categoryName = FACE_CATEGORY;
        } else if(imageData.brightnessValue < 30) {
            // 우선순위 2 : 어두운 사진이라면
            categoryName = DARK_CATEGORY;
        } else if(imageData.sharpnessScore < 50) {
            // 우선순위 3 : 흔들린 사진이라면
            categoryName = DEFOCUSED_CATEGORY;
        } else if(imageData.numberOfRepresentColor > 5) {
            // 우선순위 4 : 컬러풀한 사진이라면 (여러색이 혼합되어있다면)
            categoryName = COLORFUL_CATEGORY;
        } else if(oneYearAgoTime > imageData.date) {
            // 우선순위 5 : 오늘로부터 1년전보다 오래된 사진이라면
            categoryName = OLD_CATEGORY;
        } else {
            // 그 밖의 모든 사진을 NONE 카테고리로 분류한다.
            categoryName = NONE_CATEGORY;
        }
        return categoryName;
    }

    /**
     * 필터 Asset에 있는 Json String을 파싱하여 필터 데이터 배열로 변환한다.
     * 
     * @param assetPath Json 데이터가 있는 asset폴더 내의 파일 이름
     * @return ArrayList 필터 데이터 배열
     * @throws JsonParseException
     * @throws IOException
     */
    protected ArrayList<FilterData> getFilterFromAssetFileName(String assetPath)
            throws JsonParseException, IOException {
        InputStream in = mApplicationContext.getResources().getAssets().open(assetPath);
        JsonParser jp = null;
        ArrayList<FilterData> filterDatas = new ArrayList<FilterData>();
        try {
            jp = new JsonFactory().createJsonParser(in);
            jp.nextToken();
            while(jp.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jp.getCurrentName();
                jp.nextToken();
                if(FILTER_LIST.equals(fieldName)) {
                    while(jp.nextToken() != JsonToken.END_ARRAY) {
                        FilterData data = new FilterData();
                        data.parse(jp);
                        if(data.mFilter != null) {
                            filterDatas.add(data);
                        }
                    }
                }
            }
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch(IOException e) {
                }
            }
            if(jp != null) {
                try {
                    jp.close();
                } catch(IOException e) {
                }
            }
        }

        return filterDatas;
    }
}
