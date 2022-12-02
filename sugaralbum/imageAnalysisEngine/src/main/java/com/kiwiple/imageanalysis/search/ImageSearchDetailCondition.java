
package com.kiwiple.imageanalysis.search;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

import org.codehaus.jackson.map.ObjectMapper;

import com.kiwiple.imageanalysis.utils.HashMapUtils;
import com.kiwiple.imageanalysis.utils.JsonConverterUtil;

/**
 * 이미지 검색 시 상세 조건을 설정하는 클래스. <br>
 * 해당 클래스에서 시간, 위치, 인물 사진 여부 등을 설정할 수 있음.
 */
public class ImageSearchDetailCondition implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7319240012428549806L;
    public static final String KEY_IS_AUTO_SELECT_MAIN_DATE = "isAutoSelectMainDate";
    public static final String KEY_IS_AUTO_SELECT_MAIN_LOCATION = "isAutoSelectMainLocation";
    public static final String KEY_IS_AUTO_SELECT_MAIN_CHARACTER = "isAutoSelectMainCharacter";
    public static final String KEY_IS_AUTO_SELECT_COLORFUL = "isAutoSelectColorful";
    public static final String KEY_START_DATE_AMOUNT = "startDateAmount";
    public static final String KEY_END_DATE_AMOUNT = "endDateAmount";
    public static final String KEY_ADDRESS_NAMES = "addressNames";
    public static final String KEY_ADDRESS_NAME = "addressName";
    public static final String KEY_LATITUDE = "latitude";
    public static final String KEY_LONGITUDE = "longitude";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_IS_SELECT_FACE = "isSelectFace";
    public static final String KEY_FACE_MIN_SMILE_VALUE = "faceMinSmileValue";
    public static final String KEY_FACE_MAX_LEFT_BLINK_VALUE = "faceMaxLeftBlinkValue";
    public static final String KEY_FACE_MAX_RIGHT_BLINK_VALUE = "faceMaxRightBlinkValue";
    public static final String KEY_MIN_FOCUS_VALUE = "minFocusValue";
    public static final String KEY_MIN_BRIGHTNESS_VALUE = "minBrightnessValue";
    public static final String KEY_MAX_BRIGHTNESS_VALUE = "maxBrightnessValue";
    public static final String KEY_MIN_QUALITY_VALUE = "minQualityValue";
    public static final String KEY_MIN_TOTAL_SCORE_VALUE = "minTotalScoreValue";
    public static final String KEY_COLOR_NAMES = "colorNames";
    public static final String KEY_COLOR_NAME = "colorName";

    public static final double LOCATION_LATITUDE_NONE = -10000d;
    public static final double LOCATION_LONGITUDE_NONE = -10000d;
    public static final double LOCATION_DISTANCE_DEFAULT = 5;

    private boolean mIsAutoSelectMainDate = false;
    private boolean mIsAutoSelectMainLocation = false;
    private boolean mIsAutoSelectMainCharacter = false;
    private boolean mIsAutoSelectColorful = false;

    private int mStartDateAmount;
    private int mEndDateAmount;

    private ArrayList<String> mAddressNames;

    private double mLatitude;
    private double mLongitude;
    private double mLocationDistance = -1;

    private boolean mIsSelectFace = false;
    private int mFaceMinSmileValue;
    private int mFaceMaxLeftBlinkValue;
    private int mFaceMaxRightBlinkValue;

    private int mMinFocusValue;
    private int mMinBrightnessValue;
    private int mMinQualityValue;
    private int mMaxBrightnessValue;
    private int mMinTotalScoreValue;
    private ArrayList<String> mColorNames;

    /**
     * 생성자 클래스.<br>
     * 초기 설정 값을 정한다.
     */
    public ImageSearchDetailCondition() {
        // 여기선 디폴트 값을 설정해보자.
        setDefaultValue();
    }

    /**
     * 조건들을 기본 값으로 초기화한다. <br>
     * 단, 자동 설정은 초기화하지 않는다.
     */
    public void setDefaultValue() {

        setIsAutoSelectMainDate(false);
        setIsAutoSelectMainLocation(false);
        setIsAutoSelectMainCharacter(false);
        setIsAutoSelectColorful(false);

        setStartDateAmount(0);
        setEndDateAmount(0);

        mAddressNames = null;
        mLongitude = LOCATION_LATITUDE_NONE;
        mLatitude = LOCATION_LONGITUDE_NONE;
        mLocationDistance = LOCATION_DISTANCE_DEFAULT;

        setIsFace(false);
        setFaceMinSmileValue(0);
        setFaceMaxLeftBlinkValue(0);
        setFaceMaxRightBlinkValue(0);

        setMinFocusValue(0);
        setMinBrightnessValue(0);
        setMaxBrightnessValue(0);
        setMinQualityValue(0);
        setMinTotalScoreValue(0);

        setColorNames(null);
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
            mIsAutoSelectMainDate = HashMapUtils.getBooleanValue(mData,
                                                                 KEY_IS_AUTO_SELECT_MAIN_DATE,
                                                                 false);
            mIsAutoSelectMainLocation = HashMapUtils.getBooleanValue(mData,
                                                                     KEY_IS_AUTO_SELECT_MAIN_LOCATION,
                                                                     false);
            mIsAutoSelectMainCharacter = HashMapUtils.getBooleanValue(mData,
                                                                      KEY_IS_AUTO_SELECT_MAIN_CHARACTER,
                                                                      false);
            mIsAutoSelectColorful = HashMapUtils.getBooleanValue(mData,
                                                                 KEY_IS_AUTO_SELECT_COLORFUL, false);

            mStartDateAmount = HashMapUtils.getIntValue(mData, KEY_START_DATE_AMOUNT, 0);
            mEndDateAmount = HashMapUtils.getIntValue(mData, KEY_END_DATE_AMOUNT, 0);

            Object value = HashMapUtils.getValue(mData, KEY_ADDRESS_NAMES, null);
            if(value instanceof ArrayList) {
                ArrayList<HashMap<String, Object>> addressNames = (ArrayList<HashMap<String, Object>>)value;
                mAddressNames = new ArrayList<String>();
                for(HashMap<String, Object> addressNameInfo : addressNames) {
                    mAddressNames.add(HashMapUtils.getStringValue(addressNameInfo,
                                                                  KEY_ADDRESS_NAME, ""));
                }
            }
            mLatitude = HashMapUtils.getDoubleValue(mData, KEY_LATITUDE, LOCATION_LATITUDE_NONE);
            mLongitude = HashMapUtils.getDoubleValue(mData, KEY_LONGITUDE, LOCATION_LONGITUDE_NONE);
            mLocationDistance = HashMapUtils.getDoubleValue(mData, KEY_DISTANCE,
                                                            LOCATION_DISTANCE_DEFAULT);

            mIsSelectFace = HashMapUtils.getBooleanValue(mData, KEY_IS_SELECT_FACE, false);
            mFaceMinSmileValue = HashMapUtils.getIntValue(mData, KEY_FACE_MIN_SMILE_VALUE, 0);
            mFaceMaxLeftBlinkValue = HashMapUtils.getIntValue(mData, KEY_FACE_MAX_LEFT_BLINK_VALUE,
                                                              0);
            mFaceMaxRightBlinkValue = HashMapUtils.getIntValue(mData,
                                                               KEY_FACE_MAX_RIGHT_BLINK_VALUE, 0);

            mMinFocusValue = HashMapUtils.getIntValue(mData, KEY_MIN_FOCUS_VALUE, 0);
            mMinBrightnessValue = HashMapUtils.getIntValue(mData, KEY_MIN_BRIGHTNESS_VALUE, 0);
            mMaxBrightnessValue = HashMapUtils.getIntValue(mData, KEY_MAX_BRIGHTNESS_VALUE, 0);
            mMinQualityValue = HashMapUtils.getIntValue(mData, KEY_MIN_QUALITY_VALUE, 0);
            mMinTotalScoreValue = HashMapUtils.getIntValue(mData, KEY_MIN_TOTAL_SCORE_VALUE, 0);

            value = HashMapUtils.getValue(mData, KEY_COLOR_NAMES, null);
            if(value instanceof ArrayList) {
                ArrayList<HashMap<String, Object>> colorNames = (ArrayList<HashMap<String, Object>>)value;
                mColorNames = new ArrayList<String>();
                for(HashMap<String, Object> colorNameInfo : colorNames) {
                    mColorNames.add(HashMapUtils.getStringValue(colorNameInfo, KEY_COLOR_NAME, ""));
                }
            }

            return true;

        } catch(IOException e) {
            setDefaultValue();
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

        // isAuto~
        sb.append(JsonConverterUtil.getConvertString(KEY_IS_AUTO_SELECT_MAIN_DATE,
                                                     mIsAutoSelectMainDate));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_IS_AUTO_SELECT_MAIN_LOCATION,
                                                     mIsAutoSelectMainLocation));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_IS_AUTO_SELECT_MAIN_CHARACTER,
                                                     mIsAutoSelectMainCharacter));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_IS_AUTO_SELECT_COLORFUL,
                                                     mIsAutoSelectColorful));
        sb.append(",");
        // 시간
        sb.append(JsonConverterUtil.getConvertString(KEY_START_DATE_AMOUNT, mStartDateAmount));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_END_DATE_AMOUNT, mEndDateAmount));
        sb.append(",");
        // 위치
        sb.append(JsonConverterUtil.getConvertKey(KEY_ADDRESS_NAMES));
        sb.append("[");
        if(mAddressNames != null) {
            for(int i = 0; i < mAddressNames.size(); i++) {
                String addressName = mAddressNames.get(i);
                sb.append("{");
                sb.append(JsonConverterUtil.getConvertString(KEY_ADDRESS_NAME, addressName));
                sb.append("}");
                if(i < mAddressNames.size() - 1) {
                    sb.append(",");
                }
            }
        }
        sb.append("]");
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_LATITUDE, mLatitude));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_LONGITUDE, mLongitude));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_DISTANCE, mLocationDistance));
        sb.append(",");
        // 인물
        sb.append(JsonConverterUtil.getConvertString(KEY_IS_SELECT_FACE, mIsSelectFace));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_FACE_MIN_SMILE_VALUE, mFaceMinSmileValue));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_FACE_MAX_LEFT_BLINK_VALUE,
                                                     mFaceMaxLeftBlinkValue));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_FACE_MAX_RIGHT_BLINK_VALUE,
                                                     mFaceMaxRightBlinkValue));
        sb.append(",");
        // 포커스, 퀄리티
        sb.append(JsonConverterUtil.getConvertString(KEY_MIN_FOCUS_VALUE, mMinFocusValue));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_MIN_BRIGHTNESS_VALUE, mMinBrightnessValue));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_MAX_BRIGHTNESS_VALUE, mMaxBrightnessValue));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_MIN_QUALITY_VALUE, mMinQualityValue));
        sb.append(",");
        sb.append(JsonConverterUtil.getConvertString(KEY_MIN_TOTAL_SCORE_VALUE, mMinTotalScoreValue));
        sb.append(",");
        // 특정 컬러
        sb.append(JsonConverterUtil.getConvertKey(KEY_COLOR_NAMES));
        sb.append("[");
        if(mColorNames != null) {
            for(int i = 0; i < mColorNames.size(); i++) {
                String colorName = mColorNames.get(i);
                sb.append("{");
                sb.append(JsonConverterUtil.getConvertString(KEY_COLOR_NAME, colorName));
                sb.append("}");
                if(i < mColorNames.size() - 1) {
                    sb.append(",");
                }
            }
        }
        sb.append("]");

        sb.append("}");

        return sb.toString();
    }

    /**
     * 주요 날짜로 검색할지 여부의 설정값 반환
     * 
     * @return boolean 주요 날짜 검색 여부
     */
    public boolean isAutoSelectMainDate() {
        return mIsAutoSelectMainDate;
    }

    /**
     * 주요 날짜(사진이 많이 찍힌 날짜)의 검색 여부를 설정한다.<br>
     * AutoSelect의 설정은 해당 조건만 처리 된다.
     * 
     * @param isAutoSelectMainDate 주요 날짜 검색 여부
     */
    public void setIsAutoSelectMainDate(boolean isAutoSelectMainDate) {
        this.mIsAutoSelectMainDate = isAutoSelectMainDate;
    }

    /**
     * 주요 위치(사진이 많이 찍힌 장소)의 검색 여부 설정 값 반환
     * 
     * @return boolean 주요 위치 검색 여부
     */
    public boolean isAutoSelectMainLocation() {
        return mIsAutoSelectMainLocation;
    }

    /**
     * 주요 위치(사진이 많이 찍힌 장소)의 검색 여부를 설정한다.<br>
     * AutoSelect의 설정은 해당 조건만 처리 된다.
     * 
     * @param isAutoSelectMainLocation
     */
    public void setIsAutoSelectMainLocation(boolean isAutoSelectMainLocation) {
        this.mIsAutoSelectMainLocation = isAutoSelectMainLocation;
    }

    /**
     * 주요 인물(사진이 많이 찍힌 인물)의 검색 여부 설정 값 반환
     * 
     * @return boolean 주요 인물 검색 여부
     */
    public boolean isAutoSelectMainCharacter() {
        return mIsAutoSelectMainCharacter;
    }

    /**
     * 주요 인물(사진이 많이 찍힌 인물)의 검색 여부를 설정한다.<br>
     * AutoSelect의 설정은 중복하여 처리 될 수 없음.
     * 
     * @param isAutoSelectMainCharacter
     */
    public void setIsAutoSelectMainCharacter(boolean isAutoSelectMainCharacter) {
        this.mIsAutoSelectMainCharacter = isAutoSelectMainCharacter;
    }

    /**
     * 색상이 다양하게 쓰인 사진의 검색 여부 설정 값 반환
     * 
     * @return boolean 설정된 색상이 다양한 사진 검색 여부
     */
    public boolean isAutoSelectColorful() {
        return mIsAutoSelectColorful;
    }

    /**
     * 대표 색상이 다양하게 쓰인 사진의 검색 여부를 설정한다. <br>
     * 여기서의 기준은 4x4 대표 색상 중 6가지 이상의 색상이 있는 경우 컬러풀한 사진이라 결정한다.
     * 
     * @param isAutoSelectColorful 색상이 다양한 사진 검색 여부
     */
    public void setIsAutoSelectColorful(boolean isAutoSelectColorful) {
        this.mIsAutoSelectColorful = isAutoSelectColorful;
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
     * setIsSelectMainDate(true) 일 경우에만 수행한다. <br>
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
     * setIsSelectMainDate(true) 일 경우에만 수행한다. <br>
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
     * 설정한 위도 값을 반환.
     * 
     * @return double 위도값
     */
    public double getLatitude() {
        return mLatitude;
    }

    /**
     * 설정한 경도 값을 반환.
     * 
     * @return double 경도값
     */
    public double getLongitude() {
        return mLongitude;
    }

    /**
     * 설정한 범위 값을 반환.
     * 
     * @return double 반경의 값. ex) 1km 반경
     */
    public double getLocationDistance() {
        return mLocationDistance;
    }

    /**
     * 특정 위치 정보를 설정하고, 특정 위치에서 반경 몇 Km까지 검색할지 설정한다.
     * 
     * @param latitude 설정할 위도 값
     * @param longitude 설정할 경도 값
     * @param distance 설정할 반경의 범위 (Km 단위). 잘못된 값이라면 (음수 등) 기본값으로 전환됨
     */
    public void setLocationInfomation(double latitude, double longitude, double distance) {

        if(distance < 0) {
            distance = LOCATION_DISTANCE_DEFAULT;
        }

        this.mLatitude = latitude;
        this.mLongitude = longitude;
        this.mLocationDistance = distance;
    }

    /**
     * 설정한 주소 값을 반환한다.
     * 
     * @return ArrayList 주소 값 배열
     */
    public ArrayList<String> getAddressString() {
        return mAddressNames;
    }

    /**
     * 특정 위치 정보를 설정하여 같은 주소의 사진을 검색한다.<br>
     * 파라미터에 쓰일 주소값은 LocationOperator.getAddressNameList()의 값의 배열로 이루어져야함.
     * 
     * @param addressNames 위치 주소값 배열. ex) 서초3동 등.
     */
    public void setLocationInfomation(ArrayList<String> addressNames) {
        this.mAddressNames = addressNames;
    }

    /**
     * 설정된 얼굴 사진 검색 유무를 반환한다.
     * 
     * @return boolean 설정된 얼굴 사진 검색 유무
     */
    public boolean getIsFace() {
        return mIsSelectFace;
    }

    /**
     * 얼굴 사진을 검색할 것인지 여부를 설정한다.
     * 
     * @param isFace 얼굴 사진 검색 여부
     */
    public void setIsFace(boolean isFace) {
        mIsSelectFace = isFace;
    }

    /**
     * 설정된 Smile Value 값을 반환.
     * 
     * @return int 설정된 Smile Value
     */
    public int getFaceMinSmileValue() {
        return mFaceMinSmileValue;
    }

    /**
     * 최소 Smile Value (웃는 정도)를 설정한다. <br>
     * 범위는 0~100까지이며, 값이 클 수록 웃고 있는 사진이다.<br>
     * 설정한 값 이상의 웃는 정도를 가진 사진을 검색한다.
     * 
     * @param faceMinSmileValue 설정할 최저 Smile Value. range는 0 ~ 100.
     */
    public void setFaceMinSmileValue(int faceMinSmileValue) {
        if(faceMinSmileValue < 0) {
            faceMinSmileValue = 0;
        } else if(faceMinSmileValue > 100) {
            faceMinSmileValue = 100;
        }
        this.mFaceMinSmileValue = faceMinSmileValue;
    }

    /**
     * 설정된 Left Blink Value 값을 반환.
     * 
     * @return int 설정된 Left Blink Value
     */
    public int getFaceMaxLeftBlinkValue() {
        return mFaceMaxLeftBlinkValue;
    }

    /**
     * 최소 Left Blink Value (왼쪽 눈의 깜빡임 정도)를 설정한다. <br>
     * 범위는 0~100까지이며, 값이 클 수록 눈을 감고 있는 사진이다.<br>
     * 값은 사진 내부의 전체 얼굴의 평균 값을 기준으로 한다.<br>
     * 설정한 값 이하의 눈 깜빡임을 가진 사진을 검색한다.
     * 
     * @param faceMaxLeftBlinkValue 설정할 최고 Left Blink Value. range는 0 ~ 100.
     */
    public void setFaceMaxLeftBlinkValue(int faceMaxLeftBlinkValue) {
        if(faceMaxLeftBlinkValue < 0) {
            faceMaxLeftBlinkValue = 0;
        } else if(faceMaxLeftBlinkValue > 100) {
            faceMaxLeftBlinkValue = 100;
        }
        this.mFaceMaxLeftBlinkValue = faceMaxLeftBlinkValue;
    }

    /**
     * 설정된 Right Blink Value 값을 반환.
     * 
     * @return int 설정된 Right Blink Value
     */
    public int getFaceMaxRightBlinkValue() {
        return mFaceMaxRightBlinkValue;
    }

    /**
     * 최소 Right Blink Value (오른쪽 눈의 깜빡임 정도)를 설정한다. <br>
     * 범위는 0~100까지이며, 값이 클 수록 눈을 감고 있는 사진이다.<br>
     * 값은 사진 내부의 전체 얼굴의 평균 값을 기준으로 한다.<br>
     * 설정한 값 이하의 눈 깜빡임을 가진 사진을 검색한다.
     * 
     * @param faceMaxRightBlinkValue 설정할 최고 Right Blink Value. range는 0 ~ 100.
     */
    public void setFaceMaxRightBlinkValue(int faceMaxRightBlinkValue) {
        if(faceMaxRightBlinkValue < 0) {
            faceMaxRightBlinkValue = 0;
        } else if(faceMaxRightBlinkValue > 100) {
            faceMaxRightBlinkValue = 100;
        }
        this.mFaceMaxRightBlinkValue = faceMaxRightBlinkValue;
    }

    /**
     * 설정된 최저 선명도 값을 반환.
     * 
     * @return int 설정된 선명도 값
     */
    public int getMinFocusValue() {
        return mMinFocusValue;
    }

    /**
     * 최소 선명도 값을 설정한다. <br>
     * 범위는 0 ~ 100까지이며, 값이 클수록 선명한 사진이다. <br>
     * 설정한 값 이상의 선명도를 가진 사진을 검색한다.
     * 
     * @param minFocusValue 설정할 최저 선명도 값. range는 0 ~ 100.
     */
    public void setMinFocusValue(int minFocusValue) {
        if(minFocusValue < 0) {
            minFocusValue = 0;
        } else if(minFocusValue > 100) {
            minFocusValue = 100;
        }
        this.mMinFocusValue = minFocusValue;
    }

    /**
     * 설정된 최저 퀄리티 값을 반환.
     * 
     * @return int 설정된 선명도 값
     */
    public int getMinQualityValue() {
        return mMinQualityValue;
    }

    /**
     * 최소 퀄리티 값을 설정한다. <br>
     * 범위는 0 ~ 100까지이며, 값이 클수록 퀄리티가 좋은 사진이다. <br>
     * 설정한 값 이상의 퀄리티를 가진 사진을 검색한다.
     * 
     * @param minQualityValue 설정할 최저 선명도 값. range는 0 ~ 100.
     */
    public void setMinQualityValue(int minQualityValue) {
        if(minQualityValue < 0) {
            minQualityValue = 0;
        } else if(minQualityValue > 100) {
            minQualityValue = 100;
        }
        this.mMinQualityValue = minQualityValue;
    }

    /**
     * 설정된 최저 종합 점수 값을 반환.
     * 
     * @return int 설정된 종합 점수 값
     */
    public int getMinTotalScoreValue() {
        return mMinTotalScoreValue;
    }

    /**
     * 최소 종합 점수를 설정한다. <br>
     * 범위는 0 ~ 100까지이며, 값이 클수록 종합 점수가 좋은 사진이다. <br>
     * 설정한 값 이상의 종합 점수를 가진 사진을 검색한다.
     * 
     * @param minTotalScoreValue 설정할 최저 종합 점수 값. range는 0 ~ 100.
     */
    public void setMinTotalScoreValue(int minTotalScoreValue) {
        if(minTotalScoreValue < 0) {
            minTotalScoreValue = 0;
        } else if(minTotalScoreValue > 100) {
            minTotalScoreValue = 100;
        }
        this.mMinTotalScoreValue = minTotalScoreValue;
    }

    /**
     * 설정된 최저 밝기 값을 반환.
     * 
     * @return int 설정된 선명도 값
     */
    public int getMinBrightnessValue() {
        return mMinBrightnessValue;
    }

    /**
     * 최소 밝기 값을 설정한다. <br>
     * 범위는 0 ~ 100까지이며, 값이 클수록 밝은 사진이다. <br>
     * 설정한 값 이상의 밝기를 가진 사진을 검색한다.
     * 
     * @param minQualityValue 설정할 최저 밝기 값. range는 0 ~ 100.
     */
    public void setMinBrightnessValue(int minBrightnessValue) {
        if(minBrightnessValue < 0) {
            minBrightnessValue = 0;
        } else if(minBrightnessValue > 100) {
            minBrightnessValue = 100;
        }
        this.mMinBrightnessValue = minBrightnessValue;
    }

    /**
     * 설정된 최고 밝기 값을 반환.
     * 
     * @return int 설정된 선명도 값
     */
    public int getMaxBrightnessValue() {
        return mMaxBrightnessValue;
    }

    /**
     * 최고 밝기 값을 설정한다. <br>
     * 범위는 0 ~ 100까지이며, 값이 클수록 밝은 사진이다. <br>
     * 설정한 값 이하의 밝기를 가진 사진을 검색한다.
     * 
     * @param minQualityValue 설정할 최저 밝기 값. range는 0 ~ 100.
     */
    public void setMaxBrightnessValue(int maxBrightnessValue) {
        if(maxBrightnessValue < 0) {
            maxBrightnessValue = 0;
        } else if(maxBrightnessValue > 100) {
            maxBrightnessValue = 100;
        }
        this.mMaxBrightnessValue = maxBrightnessValue;
    }

    /**
     * 설정된 대표색 검색 값을 설정한다. <br>
     * 색상 각각의 값은 15가지의 색상으로 구분되며, 15가지의 색상은 ColorOperator.getColorSetNames()에서 얻을 수 있다.
     * 
     * @return ArrayList 설정된 검색 색상 이름들
     */
    public ArrayList<String> getColorNames() {
        return mColorNames;
    }

    /**
     * 검색할 대표색상 배열을 설정한다. <br>
     * 색상 각각의 값은 15가지의 색상으로 구분되며, 15가지의 색상은 ColorOperator.getColorSetNames()에서 얻을 수 있다.
     * 
     * @param colorNames 검색할 대표색상 배열
     */
    public void setColorNames(ArrayList<String> colorNames) {
        this.mColorNames = colorNames;
    }
}
