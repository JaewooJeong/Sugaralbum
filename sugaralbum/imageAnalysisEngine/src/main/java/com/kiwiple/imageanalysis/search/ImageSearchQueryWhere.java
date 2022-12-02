
package com.kiwiple.imageanalysis.search;

import java.text.ParseException;
import java.util.Date;

import com.kiwiple.imageanalysis.analysis.operator.LocationOperator;
import com.kiwiple.imageanalysis.utils.DateUtil;

/**
 * 각종 DB Where 조건문을 손쉽게 얻기 위한 클래스.
 */
public class ImageSearchQueryWhere {

    // DB Search Keywork
    public static final String DB_AND_KEYWORD = " And ";
    public static final String DB_OR_KEYWORD = " OR ";

    public static final String GALLERY_DATA_TABLE_NAME = "GalleryDataTable";
    public static final String COLLAGE_TABLE_NAME = "CollageTable";
    private String mTableName;

    /**
     * 생성자 <br>
     * 인자로 입력받은 테이블에서 조건문을 생성한다.
     * 
     * @param tableName 테이블 이름
     */
    public ImageSearchQueryWhere(String tableName) {
        setTableName(tableName);
    }

    /**
     * 쿼리문을 얻기 위한 테이블 명을 설정한다.
     * 
     * @param tableName 테이블 이름
     */
    public void setTableName(String tableName) {
        if(tableName == null || tableName.isEmpty()) {
            mTableName = GALLERY_DATA_TABLE_NAME;
        } else {
            mTableName = tableName;
        }
    }

    /**
     * GalleryDataTable의 기본 select 쿼리를 반환.
     * 
     * @return String 기본 select 쿼리
     */
    public String getDefaultSelectGalleryDataTable() {
        return "select " + mTableName + ".* from " + mTableName;
    }

    // ----------------------------------------------------------------------
    // --------------------------- 해상도 관련 ---------------------------------
    // ----------------------------------------------------------------------
    /**
     * 이미지가 특정 사이즈 이상 (작은 쪽이 인자 값보다 크거나 같은)인 조건문 반환
     * 
     * @param size 검색할 이미지 사이즈
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringFromImageSize(int size) {
        return "(" + mTableName + ".width >= " + size + DB_OR_KEYWORD + mTableName + ".height >= "
                + size + ")";
    }

    // ----------------------------------------------------------------------
    // --------------------------- 시간 관련 ----------------------------------
    // ----------------------------------------------------------------------
    /**
     * 시간 관련하여 시작 시점 결정하는 조건문을 반환
     * 
     * @param time 시작 시간
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringStartTime(long time) {
        return mTableName + ".date >= " + time;
    }

    /**
     * 시간 관련하여 시작 시점 결정하는 조건문을 반환
     * 
     * @param time 시작 시간
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringStartTime(Date time) {
        String dateStr = DateUtil.sdf.format(time);
        long startTime = 0;
        try {
            startTime = DateUtil.dateToMillis(dateStr);
        } catch(ParseException e) {
            e.printStackTrace();
        }
        return getWhereStringStartTime(startTime);
    }

    /**
     * 시간 관련하여 종료 시점 결정하는 조건문을 반환
     * 
     * @param time 종료 시간
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringEndTime(long time) {
        return mTableName + ".date < " + time;
    }

    /**
     * 시간 관련하여 종료 시점 결정하는 조건문을 반환
     * 
     * @param time 종료 시간
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringEndTime(Date time) {
        String dateStr = DateUtil.sdf.format(time);
        long endTime = 0;
        try {
            endTime = DateUtil.dateToMillis(dateStr);
        } catch(ParseException e) {
            e.printStackTrace();
        }
        return getWhereStringEndTime(endTime);
    }

    /**
     * 시간 관련 특정 기간의 조건문 반환
     * 
     * @param startDate 조회 시작 기간. null일 경우, 리턴 값도 null.
     * @param endDate 조회 종료 기간. null일 경우, 현재 시간으로 설정.
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringBetweenDate(Date startDate, Date endDate) {
        if(startDate == null) {
            return null;
        }
        Date tmpStartDate = startDate;
        Date tmpEndDate = null;
        if(endDate == null) {
            long currentTime = System.currentTimeMillis();
            tmpEndDate = new Date(currentTime);
        } else {
            tmpEndDate = endDate;
        }
        String startDateStr = DateUtil.sdf.format(tmpStartDate);
        String endDateStr = DateUtil.sdf.format(tmpEndDate);
        long startDatel = 0;
        long endDatel = 0;
        try {
            startDatel = DateUtil.dateToMillis(startDateStr);
            endDatel = DateUtil.dateToMillis(endDateStr);
        } catch(ParseException e) {
            e.printStackTrace();
            return null;
        }
        return mTableName + ".date >= " + startDatel + " And " + mTableName + ".date < " + endDatel;
    }
    
    /**
     * 각 날짜별 이미지 갯수를 출력하는 쿼리문을 반환<br>
     * order순서는 가장 갯수가 많은 날짜부터 출력하도록 한다.
     * 
     * @return String 각 날짜별 이미지 갯수를 출력하는 쿼리문
     */
    public static String getQueryWithDateFormatAndCount() {
        return "select dateFormat, count(dateFormat) as count from " + GALLERY_DATA_TABLE_NAME + " group by dateFormat order by count desc";
    }

    // ----------------------------------------------------------------------
    // --------------------------- 위치 관련 ----------------------------------
    // ----------------------------------------------------------------------
    /**
     * 반경 계산 쿼리 전문 반환 <br>
     * 
     * @param latitude 기준 위도 값
     * @param longitude 기준 경도 값
     * @param distance 기준 위치에서의 반경 거리 (km 단위)
     * @return String 쿼리 전문
     */
    public static String getLocationDistanceQuery(double latitude, double longitude, double distance) {

        final double particleDistance = Math.cos((double)distance / 6371);
        String query = "SELECT " + "GalleryDataTable.*, " + buildDistanceQuery(latitude, longitude)
                + " AS particle_distance FROM GalleryDataTable" + " WHERE particle_distance > "
                + particleDistance + " AND GalleryDataTable.cos_latitude != -100"
                + " ORDER BY particle_distance;";
        return query;
    }

    /**
     * 반경 계산 쿼리 공식을 만들어주는 메소드
     * 
     * @param latitude 기준 위도 값
     * @param longitude 기준 경도 값
     * @return String 쿼리 일부
     */
    private static String buildDistanceQuery(double latitude, double longitude) {

        // (lat1, lng1) 현재 위치 (lat2, lng2) 기존에 있는 위치...
        final double coslat = LocationOperator.getCosValue(latitude);
        final double sinlat = LocationOperator.getSinValue(latitude);
        final double coslng = LocationOperator.getCosValue(longitude);
        final double sinlng = LocationOperator.getSinValue(longitude);

        String coslatStr = "(" + coslat + ")";
        String sinlatStr = "(" + sinlat + ")";
        String coslngStr = "(" + coslng + ")";
        String sinlngStr = "(" + sinlng + ")";

        String columnCosLat = "GalleryDataTable.cos_latitude";
        String columnSinLat = "GalleryDataTable.sin_latitude";
        String columnCosLng = "GalleryDataTable.cos_longitude";
        String columnSinLng = "GalleryDataTable.sin_longitude";

        // @formatter:off
        return "(" + coslatStr + "*" + columnCosLat + "*(" + columnCosLng + "*" + coslngStr + "+"
                + columnSinLng + "*" + sinlngStr + ")+" + sinlatStr + "*" + columnSinLat + ")";
        // @formatter:on
    }

    /**
     * 주소 목록을 가져오는 쿼리를 반환.
     * 
     * @return String 주소 목록을 가져오는 쿼리전문.
     */
    public static String getAddressNameListQuery() {
        return "select * from " + GALLERY_DATA_TABLE_NAME
                + " where addressShortName != 'null' group by addressShortName";
    }

    /**
     * 주소 목록을 가져오는 쿼리를 반환. <br>
     * 카테고리 항목은 다음과 같다. <br>
     * 1. LocationOperator.ADDRESS_INDEX_OF_COUNTRY (국가별) <br>
     * 2. LocationOperator.ADDRESS_INDEX_OF_CITY (국가별 시/도) <br>
     * 3. LocationOperator.ADDRESS_INDEX_OF_DISTRICT (국가별 시/도 시/군/구) <br>
     * 4. LocationOperator.ADDRESS_INDEX_OF_TOWN (국가별 시/도 시/군/구 읍/면/동) <br>
     * 카테고리 값이 비정상적인 경우 4번을 기준으로 한다.
     * 
     * @param category 주석에 표기된 카테고리 값
     * @return String 주소 목록을 가져오기 위한 쿼리 전문
     */
    public static String getAddressNameListWithCategory(int category) {
        String columnName = "addressTown";
        switch(category) {
            case LocationOperator.ADDRESS_INDEX_OF_COUNTRY:
                columnName = "addressCountry";
                break;
            case LocationOperator.ADDRESS_INDEX_OF_CITY:
                columnName = "addressCity";
                break;
            case LocationOperator.ADDRESS_INDEX_OF_DISTRICT:
                columnName = "addressDistrict";
                break;
            default:
                columnName = "addressTown";
                break;
        }

        return "select * from " + GALLERY_DATA_TABLE_NAME
                + " where " + columnName + " != 'null' group by " + columnName;
    }

    /**
     * 위치 관련 조건문 반환 특정 위치 <br>
     * 주소 값에 해당 문자열이 포함되어 있다면 검색한다.
     * 
     * @param locationName 특정 주소 위치 (예: 서울특별시 or 서초3동 or 방배동)
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringLocationName(String locationName) {
        return "(" + mTableName + ".addressShortName LIKE '%" + locationName + "%'" + DB_OR_KEYWORD
                + mTableName + ".addressFullName LIKE '%" + locationName + "%')";
    }

    // ----------------------------------------------------------------------
    // --------------------------- 인물 관련 ----------------------------------
    // ----------------------------------------------------------------------
    /**
     * 인물 관련 조건문 반환. <br>
     * "인물이 있으면" 의 조건문을 반환.
     * 
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringFacePhoto() {
        return mTableName + ".numberOfFaces > 0";
    }

    /**
     * 인물 관련 조건문 반환. <br>
     * 특정 SmileValue 이상의 조건문을 반환.
     * 
     * @param smileValue 0~100까지의 웃는 정도.
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringSmileValue(int smileValue) {
        if(smileValue < 0) {
            smileValue = 0;
        } else if(smileValue > 100) {
            smileValue = 100;
        }
        return mTableName + ".avgSmileValue >= " + smileValue;
    }

    /**
     * 인물 관련 조건문 반환. <br>
     * 특정 Left Blink Value 이하의 조건문을 반환.
     * 
     * @param leftBlinkValue 0~100까지의 왼쪽 눈의 깜빡임 정도.
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringLeftBlinkValue(int leftBlinkValue) {
        if(leftBlinkValue < 0) {
            leftBlinkValue = 0;
        } else if(leftBlinkValue > 100) {
            leftBlinkValue = 100;
        }
        return mTableName + ".avgLeftBlinkValue <= " + leftBlinkValue;
    }

    /**
     * 인물 관련 조건문 반환. <br>
     * 특정 Right Blink Value 이하의 조건문을 반환.
     * 
     * @param rightBlinkValue 0~100까지의 오른쪽 눈의 깜빡임 정도.
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringRightBlinkValue(int rightBlinkValue) {
        if(rightBlinkValue < 0) {
            rightBlinkValue = 0;
        } else if(rightBlinkValue > 100) {
            rightBlinkValue = 100;
        }
        return mTableName + ".avgRightBlinkValue <= " + rightBlinkValue;
    }

    // ----------------------------------------------------------------------
    // -------------------- 이미지 점수, 선명도, 퀄리티 관련 ------------------------
    // ----------------------------------------------------------------------
    /**
     * 이미지 분석 종합점수 관련 조건문. <br>
     * value값 이상의 조건문을 반환
     * 
     * @param value 조회할 종합점수
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringTotalScore(int value) {
        return mTableName + ".totalScore >= " + value;
    }

    /**
     * 이미지 분석 퀄리티 점수 관련 조건문. <br>
     * value값 이상의 조건문을 반환.
     * 
     * @param value 조회할 퀄리티 점수
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringQualityScore(int value) {
        return mTableName + ".qualityScore >= " + value;
    }

    /**
     * 이미지 분석 선명도 관련 조건문. <br>
     * value값 이상의 조건문을 반환.
     * 
     * @param value 조회할 선명도 점수
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringSharpnessScore(int value) {
        return mTableName + ".sharpnessScore >= " + value;
    }

    /**
     * 이미지 분석 밝기 관련 조건문. <br>
     * value값 이상의 조건문을 반환.
     * 
     * @param value 조회할 밝기 점수 (ex 30이면 밝기가 30이상)
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringMinBrightnessScore(int value) {
        return mTableName + ".brightnessValue >= " + value;
    }

    /**
     * 이미지 분석 밝기 관련 조건문. <br>
     * value값 이하의 조건문을 반환.
     * 
     * @param value 조회할 밝기 점수 (ex 90이면 밝기가 90이하)
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringMaxBrightnessScore(int value) {
        return mTableName + ".brightnessValue <= " + value;
    }

    // ----------------------------------------------------------------------
    // ----------------------------- 색상 관련 --------------------------------
    // ----------------------------------------------------------------------
    /**
     * 색상 분석 관련 조건문. 기본 15색 중의 하나의 값을 입력해야한다.
     * 
     * @param colorName 색상 이름
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringColorName(String colorName) {
        return mTableName + ".representColorName = '" + colorName + "'";
    }

    /**
     * 색상 분석 관련 조건문. 기본 15색 중의 이미지가 colorCount 갯수 이상의 색상이 사용되었다면.
     * 
     * @param colorCount 기본 15색 중 몇 개 이상의 이미지가 사용되었는가
     * @return String DB 조건절 (" Where " 미포함)
     */
    public String getWhereStringColorful(int colorCount) {
        return mTableName + ".numberOfRepresentColor >= " + colorCount;
    }
}
