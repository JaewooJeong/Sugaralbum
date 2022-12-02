
package com.kiwiple.imageanalysis.analysis;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.codehaus.jackson.map.ObjectMapper;

import android.content.Context;
import android.provider.MediaStore.Images.ImageColumns;

import com.kiwiple.imageanalysis.search.ImageSearchQueryWhere;
import com.kiwiple.imageanalysis.utils.DateUtil;
import com.kiwiple.imageanalysis.utils.JsonConverterUtil;

/**
 * 이미지 분석을 어떻게 할 것인가의 조건 클래스.<br>
 * 시간, 위치, 인물, 색감/선명도/퀄리티, 특정 앨범 or 전체 갤러리 여부를 결정한다.
 */
public class ImageAnalysisCondition {

    public static final String KEY_START_DATE_AMOUNT = "startDateAmount";
    public static final String KEY_END_DATE_AMOUNT = "endDateAmount";
    public static final String KEY_IS_LOCATION_ANALYSIS = "isLocationAnalysis";
    public static final String KEY_IS_FACE_ANALYSIS = "isFaceAnalysis";
    public static final String KEY_IS_QUALITY_ANALYSIS = "isQualityAnalysis";
    public static final String KEY_ALBUN_NAMES = "albumNames";
    public static final String KEY_ALBUN_NAME = "albumName";
    public static final String KEY_PREVIOUS_ANALYSIS_COUNT = "previousAnalysisCount";
    public static final String KEY_IS_AUTO_FACE_RECOGNITION = "isAutoFaceRecognition";
    public static final String KEY_PROTAGONIST_COUNT = "protagonistCount";

    // ImageAnalysis 초기 분석 숫자 기본 값
    public static final int DEFAULT_PROTAGONIST_COUNT = 15;

    private Context mContext;

    private int mStartDateAmount;
    private int mEndDateAmount;
    private boolean mIsAnalysisLocation = true;
    private boolean mIsAnalysisFace = false;
    private boolean mIsAnalysisQuality = true;
    private boolean mIsAnalysisSpecificAlbum = false;
    private ArrayList<String> mAnalysisAlbumNames = null;
    // 분석 갯수
    private int mAnalysisCount = 0;
    // 오토 주인공 설정을 할 것인지
    private boolean mIsAutoFaceRecognition = false;
    private int mProtagonistCount = DEFAULT_PROTAGONIST_COUNT;

    /**
     * 생성자
     * 
     * @param context Context
     */
    public ImageAnalysisCondition(Context context) {
        mContext = context;
        mAnalysisAlbumNames = new ArrayList<String>();
        mAnalysisCount = 0;
        setAnalysisTimeCondition(0, 0);
    }

    /**
     * 각 조건이 설정되어 있는 Json 데이터를 받아 조건을 설정하고,<br>
     * Json 데이터가 정상적으로 입력되었는지를 반환한다.
     * 
     * @param jsonString 각 조건이 설정되어있는 Json String
     * @return boolean 조건이 제대로 설정되었는지 여부
     */
    @SuppressWarnings("unchecked")
    public boolean setJsonStringCondition(String jsonString) {
        if(jsonString == null) {
            return false;
        }

        InputStream in = null;
        try {
            in = new ByteArrayInputStream(jsonString.getBytes("UTF-8"));
        } catch(UnsupportedEncodingException e1) {
            e1.printStackTrace();
            return false;
        }

        try {
            HashMap<String, Object> mData = new ObjectMapper().readValue(in, HashMap.class);
            mAnalysisCount = (Integer)mData.get(KEY_PREVIOUS_ANALYSIS_COUNT);
            mStartDateAmount = (Integer)mData.get(KEY_START_DATE_AMOUNT);
            mEndDateAmount = (Integer)mData.get(KEY_END_DATE_AMOUNT);
            mIsAnalysisLocation = (Boolean)mData.get(KEY_IS_LOCATION_ANALYSIS);
            mIsAnalysisFace = (Boolean)mData.get(KEY_IS_FACE_ANALYSIS);
            mIsAnalysisQuality = (Boolean)mData.get(KEY_IS_QUALITY_ANALYSIS);
            mIsAutoFaceRecognition = (Boolean)mData.get(KEY_IS_AUTO_FACE_RECOGNITION);
            mProtagonistCount = (Integer)mData.get(KEY_PROTAGONIST_COUNT);
            ArrayList<HashMap<String, Object>> albumList = (ArrayList<HashMap<String, Object>>)mData.get(KEY_ALBUN_NAMES);
            mAnalysisAlbumNames = new ArrayList<String>();
            for(HashMap<String, Object> albumNameInfo : albumList) {
                mAnalysisAlbumNames.add((String)albumNameInfo.get(KEY_ALBUN_NAME));
            }

            if(mAnalysisAlbumNames != null && !mAnalysisAlbumNames.isEmpty()) {
                mIsAnalysisSpecificAlbum = true;
            } else {
                mIsAnalysisSpecificAlbum = false;
            }

            return true;

        } catch(IOException e) {
            mAnalysisAlbumNames = new ArrayList<String>();
            mAnalysisCount = 0;
            setAnalysisTimeCondition(0, 0);
            return false;
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch(IOException e) {
                }
            }
        }
    }

    /**
     * 현재 설정되어 있는 조건을 Json 형태의 데이터로 반환한다.
     * 
     * @return String 현재 설정된 조건의 Json Data
     */
    public String getJsonStringCondition() {

        StringBuilder sb = new StringBuilder();
        sb.append("{");

        // 선행분석 갯수
        sb.append(JsonConverterUtil.getConvertString(KEY_PREVIOUS_ANALYSIS_COUNT,
                                                     mAnalysisCount));
        sb.append(",");
        // 시간
        sb.append(JsonConverterUtil.getConvertString(KEY_START_DATE_AMOUNT, mStartDateAmount));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_END_DATE_AMOUNT, mEndDateAmount));
        sb.append(",");
        // 위치
        sb.append(JsonConverterUtil.getConvertString(KEY_IS_LOCATION_ANALYSIS, mIsAnalysisLocation));
        sb.append(",");
        // 인물
        sb.append(JsonConverterUtil.getConvertString(KEY_IS_FACE_ANALYSIS, mIsAnalysisFace));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_IS_AUTO_FACE_RECOGNITION,
                                                     mIsAutoFaceRecognition));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_PROTAGONIST_COUNT, mProtagonistCount));
        sb.append(",");
        // 퀄리티
        sb.append(JsonConverterUtil.getConvertString(KEY_IS_QUALITY_ANALYSIS, mIsAnalysisQuality));
        sb.append(",");
        // 특정 앨범
        sb.append(JsonConverterUtil.getConvertKey(KEY_ALBUN_NAMES));
        sb.append("[");
        if(mAnalysisAlbumNames != null) {
            for(int i = 0; i < mAnalysisAlbumNames.size(); i++) {
                String albumName = mAnalysisAlbumNames.get(i);
                sb.append("{");
                sb.append(JsonConverterUtil.getConvertString(KEY_ALBUN_NAME, albumName));
                sb.append("}");
                if(i < mAnalysisAlbumNames.size() - 1) {
                    sb.append(",");
                }
            }
        }
        sb.append("]");

        sb.append("}");

        return sb.toString();
    }

    /**
     * 현재 시간에서 몇일 전부터(startDateAmount) 몇일 전까지(endDateAmount) 검색할지를 설정한다.<br>
     * 
     * @param startDateAmount
     * @param endDateAmount
     */
    public void setAnalysisTimeCondition(int startDateAmount, int endDateAmount) {
        if(startDateAmount < 0) {
            startDateAmount = 0;
        }

        if(endDateAmount < 0) {
            endDateAmount = 0;
        }

        this.mStartDateAmount = startDateAmount;
        this.mEndDateAmount = endDateAmount;
    }

    /**
     * 현재 시간에서 몇일 전 부터 검색할지의 값을 반환. <br>
     * ex) 3이라면 3일전 부터 검색 시작.
     * 
     * @return 현재 시간에서 몇일 전의 값.
     */
    public int getStartDateAmount() {
        return mStartDateAmount;
    }

    /**
     * 현재 시간에서 몇일 전 부터 검색을 할 것인가를 설정한다. <br>
     * Default Value = 0; <br>
     * 0으로 설정할 경우 가장 오래된 날짜부터 시작.<br>
     * ex) 3이라면 3일전 부터 검색 시작.
     * 
     * @param mStartDateAmount 현재 시간에서 몇일 전의 값
     */
    public void setStartDateAmount(int startDateAmount) {
        if(startDateAmount < 0) {
            startDateAmount = 0;
        }
        this.mStartDateAmount = startDateAmount;
    }

    /**
     * 현재 시간에서 몇일 전까지 검색을 할지 설정한 값을 반환.
     * 
     * @return 현재 시간에서 몇일 전 까지의 값
     */
    public int getEndDateAmount() {
        return mEndDateAmount;
    }

    /**
     * 현재 시간에서 몇일 전 까지 검색을 할 것인가를 설정한다. <br>
     * Default Value = 0; <br>
     * 0으로 설정할 경우 가장 오늘까지로 설정한다.<br>
     * ex) 3이라면 3일전 까지 검색 시작.
     * 
     * @param endDateAmount 현재 시간에서 몇일 전의 값
     */
    public void setEndDateAmount(int endDateAmount) {
        if(endDateAmount < 0) {
            endDateAmount = 0;
        }
        this.mEndDateAmount = endDateAmount;
    }

    /**
     * 이미지의 위치를 분석할 것인가의 여부를 결정.
     * 
     * @param isLocationAnalysis 설정할 위치 분석 여부
     */
    public void setAnalysisLocationCondition(boolean isLocationAnalysis) {
        mIsAnalysisLocation = isLocationAnalysis;
    }

    /**
     * 설정된 위치 분석 여부를 리턴.
     * 
     * @return boolean 위치 분석 여부
     */
    public boolean getAnalysisLocationCondition() {
        return mIsAnalysisLocation;
    }

    /**
     * 이미지에서 얼굴 관련 분석을 할 것인가의 여부를 결정.<br>
     * 단, Face Recognition은 별개의 루틴으로 취급되어 여기서는 포함치 않는다.
     * 
     * @param isFaceAnalysis 설정할 인물 분석 여부
     */
    public void setAnalysisFaceCondition(boolean isFaceAnalysis) {
        mIsAnalysisFace = isFaceAnalysis;
    }

    /**
     * 설정된 인물 분석 여부를 리턴.
     * 
     * @return boolean 인물 분석 여부
     */
    public boolean getAnalysisFaceCondition() {
        return mIsAnalysisFace;
    }

    /**
     * 이미지의 퀄리티/선명도/색감 의 분석을 할 것인가의 여부를 결정.
     * 
     * @param isQualityAnalysis 설정할 퀄리티/선명도/색감 분석 여부
     */
    public void setAnalysisQualityCondition(boolean isQualityAnalysis) {
        mIsAnalysisQuality = isQualityAnalysis;
    }

    /**
     * 설정된 퀄리티/선명도/색감의 분석 여부를 리턴.
     * 
     * @return boolean 퀄리티/선명도/색감의 분석 여부
     */
    public boolean getAnalysisQualityCondition() {
        return mIsAnalysisQuality;
    }

    /**
     * 분석할 갤러리를 결정.<br>
     * null을 입력하거나 이름을 입력하지 않은 경우 전체 갤러리를 검색함.
     * 
     * @param albumName ImageAnalysis.getAlbumNames 에서 추출된 앨범 이름을 추가한다.
     */
    public void setAnalysisAlbumNames(ArrayList<String> albumNames) {
        mAnalysisAlbumNames = albumNames;
        // 없다면 전체 갤러리로 설정한다.
        if(mAnalysisAlbumNames == null || mAnalysisAlbumNames.isEmpty()) {
            mAnalysisAlbumNames = ImageAnalysis.getAlbumNames(mContext);
            mIsAnalysisSpecificAlbum = false;
        } else {
            mIsAnalysisSpecificAlbum = true;
        }
    }

    /**
     * 분석할 갤러리의 이름 리스트를 리턴.
     * 
     * @return String 분석할 갤러리의 이름 리스트. 없을 경우 null을 반환
     */
    public ArrayList<String> getAnalysisAlbumNames() {
        if(mIsAnalysisSpecificAlbum) {
            return mAnalysisAlbumNames;
        }

        return null;
    }

    /**
     * 이미지 분석 갯수를 정의 <br>
     * 설정하지 않을 경우, Default 값은 0이 되며, 해당 값이 0일 경우 전체 분석을 수행함.
     * 
     * @param analysisCount 설정할 분석 갯수
     */
    public void setAnalysisCount(int analysisCount) {
        mAnalysisCount = analysisCount;
    }

    /**
     * 설정된 이미지 분석 갯수를 리턴.
     * 
     * @return int 선행 분석 갯수
     */
    public int getAnalysisCount() {
        return mAnalysisCount;
    }

    /**
     * 인물 탐색시 자동으로 주인공으로 등록하여 특정 인물끼리 묶을지 여부<br>
     * 분석시 최근 사진을 기준으로 주인공을 Count만큼 뽑는다.<br>
     * Count가 넘어간 경우 그 이후의 Unknown 인물에 대한 Recognition은 무시한다.<br>
     * 단, FaceOperator.getIsSupportedSnapdragonFaceRecognition() 의 결과가 true여야만 수행 할 수 있다.<br>
     * true로 설정될 경우, 기존에 저장된 주인공 데이터가 초기화됨에 주의!
     * 
     * @param isAutoFaceRecognition 설정할 자동 인물 인식 여부
     * @param protagonistCount 주인공 등록 숫자. 최대 값은 15명. 15이 넘는 값이 올 경우, 15으로 통일.
     */
    public void setIsAutoFaceRecognition(boolean isAutoFaceRecognition, int protagonistCount) {
        mIsAutoFaceRecognition = isAutoFaceRecognition;
        // addPerson에 대한 limit은 없으나, SDK 버그로 15개까지만 추가 가능
        if(protagonistCount > 0 && protagonistCount <= DEFAULT_PROTAGONIST_COUNT) {
            mProtagonistCount = protagonistCount;
        } else if(protagonistCount < 0) {
            mProtagonistCount = 0;
        } else if(protagonistCount > DEFAULT_PROTAGONIST_COUNT) {
            mProtagonistCount = DEFAULT_PROTAGONIST_COUNT;
        }
    }

    /**
     * 설정된 자동 주인공 설정 여부를 리턴.
     * 
     * @return boolean 주인공 설정 여부
     */
    public boolean getIsAutoFaceRecognition() {
        return mIsAutoFaceRecognition;
    }

    /**
     * 설정된 자동 주인공의 인물 수. (최대값은 10)
     * 
     * @return int 주인공 인물 수.
     */
    public int getProtagonistCount() {
        return mProtagonistCount;
    }

    /**
     * 설정된 조건들을 Where 절에 들어갈 쿼리로 반환해준다.
     * 
     * @return String Where절에 들어갈 조건문
     */
    public String getGenerateQueryFromCondition() {
        ArrayList<String> whereStrings = new ArrayList<String>();
        // 시간 조건
        if(mStartDateAmount > 0) {
            long startTime = DateUtil.getDateTimeAgoDay(mStartDateAmount);
            whereStrings.add(ImageColumns.DATE_TAKEN + ">=" + startTime);
        }

        if(mEndDateAmount > 0) {
            // 종료 시간의 경우 1일 뒤를 잡아야 해당 날짜 이내의 모든 사진이 검출될 것!!
            long endTime = DateUtil.getDateTimeAgoDay(mEndDateAmount);
            whereStrings.add(ImageColumns.DATE_TAKEN + "<" + endTime);
        }

        // 갤러리 이름이 설정되어 있는가?
        mAnalysisAlbumNames = getAnalysisAlbumNames();
        String albumWhere = "";
        if(mAnalysisAlbumNames != null) {
            for(int i = 0; i < mAnalysisAlbumNames.size(); i++) {
                if(i == 0) {
                    albumWhere += "(";
                }
                albumWhere += ImageColumns.BUCKET_DISPLAY_NAME + "='" + mAnalysisAlbumNames.get(i)
                        + "'";
                if(i < mAnalysisAlbumNames.size() - 1) {
                    albumWhere += ImageSearchQueryWhere.DB_OR_KEYWORD;
                } else {
                    albumWhere += ")";
                }
            }
            whereStrings.add(albumWhere);
        }

        StringBuilder where = new StringBuilder();
        for(int i = 0; i < whereStrings.size(); i++) {
            if(i > 0) {
                where.append(ImageSearchQueryWhere.DB_AND_KEYWORD);
            }
            where.append(whereStrings.get(i));
        }

        return where.toString();
    }
}
