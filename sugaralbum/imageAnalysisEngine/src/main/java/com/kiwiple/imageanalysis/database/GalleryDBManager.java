
package com.kiwiple.imageanalysis.database;

import java.io.File;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.kiwiple.imageanalysis.Global;
import com.kiwiple.imageanalysis.analysis.operator.LocationOperator;
import com.kiwiple.imageanalysis.search.ImageSearchQueryWhere;
import com.kiwiple.imageanalysis.utils.DateUtil;

/**
 * 갤러리 이미지에 관한 데이터 베이스 클래스 <br>
 * 데이터베이스 쿼리 등을 처리한다
 */
public class GalleryDBManager {

    private static final String GALLERY_DB_NAME = "GalleryDatabase.db";
    private static final String GALLERY_TABLE_NAME = "GalleryDataTable";
    private static final String GALLERY_FACE_TABLE_NAME = "GalleryFaceDataTable";
    private static final String GALLERY_COLLAGE_TABLE_NAME = "CollageTable";
    // Version 2: modify type(text to integer). qualityScore, sharpnessScore, totalScore,
    // brightnessValue
    public static final int DB_VERSION = 2;

    // DB 관련 객체
    private SQLiteDatabase mDataBase;
    private GalleryDBOpenHelper mOpenHelper;

    /**
     * GalleryDBManager 생성자
     * 
     * @param context
     */
    public GalleryDBManager(Context context) {
        mOpenHelper = new GalleryDBOpenHelper(context, GALLERY_DB_NAME, GALLERY_TABLE_NAME, null,
                                              DB_VERSION);
    }

    /**
     * 현재 분석된 모든 이미지 데이터 저장 정보를 불러온다. <br>
     * 
     * @return ArrayList 이미지 데이터 배열
     */
    public synchronized ArrayList<ImageData> selectAllImageDatas() {
        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<ImageData> childs = null;
        String query = "select * from " + GALLERY_TABLE_NAME + ";";
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor.getCount() > 0) {
            childs = getLoadImageDatas(cursor);
        }
        cursor.close();
        mDataBase.close();

        return childs;
    }

    /**
     * 현재까지 분석된 모든 이미지 데이터의 고유 번호를 반환한다.
     * 
     * @return ArrayList 이미지 데이터 고유번호 배열
     */
    public synchronized ArrayList<Integer> selectAllImageDatasId() {
        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<Integer> childs = new ArrayList<Integer>();
        String query = "select imageId from " + GALLERY_TABLE_NAME + ";";
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor != null && cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                int id = Integer.valueOf(cursor.getString(cursor.getColumnIndex("imageId")));
                childs.add(id);
            }
        }
        cursor.close();
        mDataBase.close();

        return childs;
    }

    private String getQuerySelectWhereImageData(String tableName, String where) {
        String query = "select * from " + tableName;
        if(where != null && where.length() > 0) {
            query += (" Where " + where + ";");
        }

        return query;
    }

    private String getQuerySelectWhereImageDataCount(String tableName, String where) {
        String query = "select count(*) from " + tableName;
        if(where != null && where.length() > 0) {
            query += (" Where " + where + ";");
        }

        return query;
    }

    private String getQueryInsertImageData(String tableName, ImageData imageData) {
        double cosLatitude = -100;
        double sinLatitude = -100;
        double cosLongitude = -100;
        double sinLongitude = -100;

        if(!Global.isNullString(imageData.latitude) && !Global.isNullString(imageData.longitude)) {
            cosLatitude = LocationOperator.getCosValue(Double.parseDouble(imageData.latitude));
            sinLatitude = LocationOperator.getSinValue(Double.parseDouble(imageData.latitude));
            cosLongitude = LocationOperator.getCosValue(Double.parseDouble(imageData.longitude));
            sinLongitude = LocationOperator.getSinValue(Double.parseDouble(imageData.longitude));
        }
        
        String fileName = imageData.fileName;
        if (fileName.contains("'")) {
            fileName = fileName.replaceAll("'", "''");
        }
        fileName = DatabaseUtils.sqlEscapeString(imageData.fileName);
        
        String path = imageData.path;
        if (path.contains("'")) {
            path = path.replaceAll("'", "''");
        }
        path = DatabaseUtils.sqlEscapeString(imageData.path);

        String query = "insert into " + tableName + " values (NULL, " //
                + "'" + imageData.id + "'," //
                + "'" + imageData.albumName + "'," //
                + imageData.date + "," //
                + "'" + imageData.dateFormat + "', " //
                + imageData.dateAdded + "," //
                + fileName + ", " //
                + imageData.fileSize + "," //
                + path + "," //
                + "'" + imageData.mimeType + "'," //
                + imageData.latitude + "," //
                + imageData.longitude + "," //
                + cosLatitude + "," //
                + sinLatitude + "," //
                + cosLongitude + "," //
                + sinLongitude + "," //
                + "'" + imageData.orientation + "'," //
                + imageData.width + "," //
                + imageData.height + "," //
                + "'" + imageData.addressShortName + "'," //
                + "'" + imageData.addressFullName + "'," //
                + "'" + imageData.addressCountry + "'," //
                + "'" + imageData.addressCity + "'," //
                + "'" + imageData.addressDistrict + "'," //
                + "'" + imageData.addressTown + "'," //
                + imageData.numberOfFace + "," //
                + imageData.faceBitmapScale + "," //
                + imageData.faceBitmapWidth + "," //
                + imageData.faceBitmapHeight + "," //
                + "'" + imageData.isFinishFaceDetecting + "'," //
                + imageData.avgSmileValue + "," //
                + imageData.avgLeftBlinkValue + "," //
                + imageData.avgRightBlinkValue + "," //
                + imageData.totalScore + "," //
                + imageData.qualityScore + "," //
                + imageData.sharpnessScore + "," //
                + "'" + imageData.colorSet + "'," //
                + "'" + imageData.representColorName + "'," //
                + imageData.brightnessValue + "," //
                + imageData.numberOfRepresentColor + "," //
                + "'" + imageData.isFinishColorsetAnalysis + "')"; //
        return query;
    }

    private String getQueryUpdateImageData(String tableName, ImageData imageData) {
        double cosLatitude = -100;
        double sinLatitude = -100;
        double cosLongitude = -100;
        double sinLongitude = -100;

        if(!Global.isNullString(imageData.latitude) && !Global.isNullString(imageData.longitude)) {
            cosLatitude = LocationOperator.getCosValue(Double.parseDouble(imageData.latitude));
            sinLatitude = LocationOperator.getSinValue(Double.parseDouble(imageData.latitude));
            cosLongitude = LocationOperator.getCosValue(Double.parseDouble(imageData.longitude));
            sinLongitude = LocationOperator.getSinValue(Double.parseDouble(imageData.longitude));
        }

        String query = "update " + tableName + " set " //
                + "albumName = '" + imageData.albumName + "', " //
                + "date = " + imageData.date + ", " //
                + "dateFormat = '" + imageData.dateFormat + "', " //
                + "dateAdded = " + imageData.dateAdded + ", " //
                + "fileName = " + DatabaseUtils.sqlEscapeString(imageData.fileName) + ", " //
                + "fileSize = " + imageData.fileSize + ", " //
                + "path = " + DatabaseUtils.sqlEscapeString(imageData.path) + ", " //
                + "mimeType = '" + imageData.mimeType + "', " //
                + "latitude = " + imageData.latitude + ", " //
                + "longitude = " + imageData.longitude + ", " //
                + "cos_latitude = " + cosLatitude + ", " //
                + "sin_latitude = " + sinLatitude + ", " //
                + "cos_longitude = " + cosLongitude + ", " //
                + "sin_longitude = " + sinLongitude + ", " //
                + "orientation = '" + imageData.orientation + "', " //
                + "width = " + imageData.width + ", " //
                + "height = " + imageData.height + ", " //
                + "addressShortName = '" + imageData.addressShortName + "', " //
                + "addressFullName = '" + imageData.addressFullName + "', " //
                + "addressCountry = '" + imageData.addressCountry + "', " //
                + "addressCity = '" + imageData.addressCity + "', " //
                + "addressDistrict = '" + imageData.addressDistrict + "', " //
                + "addressTown = '" + imageData.addressTown + "', " //
                + "numberOfFaces = " + imageData.numberOfFace + ", " //
                + "faceBitmapScale = " + imageData.faceBitmapScale + ", " //
                + "faceBitmapWidth = " + imageData.faceBitmapWidth + ", " //
                + "faceBitmapHeight = " + imageData.faceBitmapHeight + ", " //
                + "isFinishFaceDetecting = '" + imageData.isFinishFaceDetecting + "', " //
                + "avgSmileValue = " + imageData.avgSmileValue + ", " //
                + "avgLeftBlinkValue = " + imageData.avgLeftBlinkValue + ", " //
                + "avgRightBlinkValue = " + imageData.avgRightBlinkValue + ", " //
                + "totalScore = " + imageData.totalScore + ", " //
                + "qualityScore = " + imageData.qualityScore + ", " //
                + "sharpnessScore = " + imageData.sharpnessScore + ", " //
                + "colorSet = '" + imageData.colorSet + "', " //
                + "representColorName = '" + imageData.representColorName + "', " //
                + "brightnessValue = " + imageData.brightnessValue + ", " //
                + "numberOfRepresentColor = " + imageData.numberOfRepresentColor + ", " //
                + "isFinishColorsetAnalysis = '" + imageData.isFinishColorsetAnalysis + "'" //
                + " where imageId = '" + imageData.id + "';";
        return query;
    }

    /**
     * 이미지 데이터 날짜와 해당 날짜에 속한 이미지의 갯수 구조 클래스
     */
    public class DateAndCount {
        /**
         * 특정 날짜
         */
        public String dateFormat;
        /**
         * 특정 날짜에 속한 이미지 갯수
         */
        public int count;
    }

    /**
     * 갤러리에 저장된 이미지의 각 날짜별 이미지 갯수를 반환한다.
     * 
     * @return ArrayList 각 날짜별 이미지 갯수 정보를 담고 있는 객체 배열
     */
    public synchronized ArrayList<DateAndCount> getImageDataCountsInDate() {
        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<DateAndCount> childs = null;
        String query = ImageSearchQueryWhere.getQueryWithDateFormatAndCount();
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor.getCount() > 0) {
            childs = new ArrayList<DateAndCount>();
            while(cursor.moveToNext()) {
                DateAndCount dateAndCount = new DateAndCount();
                dateAndCount.dateFormat = cursor.getString(cursor.getColumnIndex("dateFormat"));
                dateAndCount.count = cursor.getInt(cursor.getColumnIndex("count"));
                childs.add(dateAndCount);
            }
        }
        cursor.close();
        mDataBase.close();
        return childs;
    }

    /**
     * 기준 위치(latitude, longitude)에서 반경(distance)에 위치값이 있는 이미지 리스트를 반환.<br>
     * 없다면 null을 반환한다.
     * 
     * @param latitude 기준 위치 위도
     * @param longitude 기준 위치 경도
     * @param distance 반경 거리 (km단위)
     * @return
     */
    public synchronized ArrayList<ImageData> getImageDataFromLocationDistance(double latitude,
            double longitude, float distance) {
        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<ImageData> childs = null;
        String query = ImageSearchQueryWhere.getLocationDistanceQuery(latitude, longitude, distance);
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor.getCount() > 0) {
            childs = getLoadImageDatas(cursor);
        }
        cursor.close();
        mDataBase.close();
        return childs;
    }

    /**
     * 해당 주소값과 카테고리에 해당되는 이미지 리스트를 반환한다.
     * 
     * @param addressValue 주소 값
     * @param category 위치 카테고리 LocationOperator.ADDRESS_INDEX_OF_
     * @return ArrayList 주소값과 카테고리에 부합하는 이미지 리스트
     */
    public synchronized ArrayList<ImageData> getImageDatasFromLocationName(String addressValue,
            int category) {

        if(Global.isNullString(addressValue)
                || category < LocationOperator.ADDRESS_INDEX_OF_COUNTRY
                || category > LocationOperator.ADDRESS_INDEX_OF_TOWN) {
            return null;
        }

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

        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<ImageData> childs = null;
        String query = "select * from " + GALLERY_TABLE_NAME + " where " + columnName + "='"
                + addressValue + "'";
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor.getCount() > 0) {
            childs = getLoadImageDatas(cursor);
        }
        cursor.close();
        mDataBase.close();
        return childs;
    }

    /**
     * 등록된 주인공 중 이미지 숫자가 1개밖에 없는 주인공 아이디를 반환
     * 
     * @return 주인공 아이디 배열
     */
    public synchronized ArrayList<Integer> getPersonIdsForMinCount() {
        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<Integer> personIds = null;
        String query = "select personId, cnt from (select personId, count(personId) cnt from "
                + GALLERY_FACE_TABLE_NAME
                + " where personId >= 0 group by personId) a where a.cnt = 1";
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor != null && cursor.getCount() > 0) {
            personIds = new ArrayList<Integer>();
            while(cursor.moveToNext()) {
                int personId = cursor.getInt(cursor.getColumnIndex("personId"));
                personIds.add(personId);
            }
        }
        mDataBase.close();
        return personIds;
    }

    /**
     * 여행이라고 판단되는 이미지를 반환한다.<br>
     * 해당 메소드는 아래와 같은 가정을 전제로 한다.<br>
     * 1. 최저 1박2일 이상의 여행 (1일짜리 여행의 경우 MainDate 등을 통해서도 찾을 수 있다고 판단) <br>
     * 2. 특정 기간 (startDateAmount ~ endDateAmount)사이에 사진이 가장 많은 날짜를 3~6개 정도를 뽑는다. <br>
     * 3. 2.에서 뽑은 날짜가 연속되어진 것이 있다면 연속된 날짜의 모든 이미지를 묶어 하나의 배열로 만들고 배열로 이루어진 배열을 반환. <br>
     * 4. 연속된 날짜가 2번 이상일 경우 각각 연속된 날짜의 이미지를 배열로 감싼 배열을 반환한다. <br>
     * (ex 5/17, 5/14, 5/13, 5/12, 5/9, 5/8, 5/5 등으로 있다면 <br>
     * ( 5/14, 5/13, 5/12일에 해당되는 이미지의 전부를 배열로 묶고, <br>
     * 5/9, 5/8일에 해당되는 이미지의 전부를 다른 배열로 묶어 두개의 배열을 가진 배열을 반환한다.
     * 
     * @param startDateAmount 시작 기간이 오늘부터 몇일 전인가 (ex 30 => 오늘로부터 30일 이전부터)
     * @param endDateAmount 종료 기간이 오늘부터 몇일 전인가 (ex 10 => 오늘로부터 10일 이전까지)
     * @param minImageCount 최소 여행이라 판단될 이미지 갯수 (ex 20 => 이미지가 20장 이상인 날짜만)
     * @return ArrayList(ArrayList) 해당되는 이미지 배열
     */
    public synchronized ArrayList<ArrayList<ImageData>> getTravelImageDatas(int startDateAmount,
            int endDateAmount, int minImageCount) {
        if(startDateAmount < 0) {
            startDateAmount = 0;
        }
        if(endDateAmount < 0) {
            endDateAmount = 0;
        }
        if(minImageCount < 1) {
            minImageCount = 5;
        }

        // 기간 내에 최저 minImageCount 이상의 이미지가 있는 날짜를 찾는다.
        long startTime = DateUtil.getDateTimeAgoDay(startDateAmount);
        long endTime = DateUtil.getDateTimeAgoDay(endDateAmount);
        StringBuilder dateQuery = new StringBuilder();
        dateQuery.append("select a.dateFormat, a.cnt from " + GALLERY_TABLE_NAME
                + ", (select dateFormat, count(dateFormat) cnt from " + GALLERY_TABLE_NAME
                + " where ");
        if(startDateAmount > 0 && endDateAmount > 0) {
            dateQuery.append("date >= " + startTime + " and date <= " + endTime + " and ");
        } else if(startDateAmount > 0) {
            dateQuery.append("date >= " + startTime + " and ");
        }

        dateQuery.append("dateFormat != 'null' and dateFormat is not null group by dateFormat order by cnt desc) a where GalleryDataTable.dateFormat = a.dateFormat and ");
        dateQuery.append("a.cnt >= " + minImageCount + " ");
        // 최신 날짜 순으로 정렬
        dateQuery.append("group by a.dateFormat order by a.dateFormat desc");

        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<String> dateResultArray = new ArrayList<String>();
        Cursor cursor = mDataBase.rawQuery(dateQuery.toString(), null);
        if(cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                String dateString = cursor.getString(cursor.getColumnIndex("dateFormat"));
                dateResultArray.add(dateString);
            }
        }
        mDataBase.close();

        // 위에서 찾은 날짜가 연속된 날짜가 있는지 검사한다.
        ArrayList<ArrayList<String>> pickDateArray = new ArrayList<ArrayList<String>>();
        ArrayList<String> straightDateArray = new ArrayList<String>();
        int i = 0;
        int j = 1;
        while(i < dateResultArray.size() && j < dateResultArray.size()) {
            String firstDateString = dateResultArray.get(i);
            String secondDateString = dateResultArray.get(j);

            long firstDateTime = 0;
            long secondDateTime = 0;
            try {
                firstDateTime = DateUtil.dateToMillis(firstDateString);
                secondDateTime = DateUtil.dateToMillis(secondDateString);
            } catch(ParseException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            long oneDayAgoTime = firstDateTime - (24 * 60 * 60 * 1000);
            if(secondDateTime >= oneDayAgoTime) {
                // 두번째 비교 날짜가 1일 이전 이내라면
                // 연속된 날짜라고 판정할 수 있다.
                if(straightDateArray.isEmpty()) {
                    // 하나도 없는 경우라면 첫번째 비교대상도 포함시킨다.
                    straightDateArray.add(firstDateString);
                }
                // 기본적으로 두번째 비교대상만 포함
                straightDateArray.add(secondDateString);
            } else {
                // 도중에 끊긴 경우, 우선 여기까지 찾은 배열을 담고, 담았던 내용을 초기화 시킴
                if(!straightDateArray.isEmpty()) {
                    ArrayList<String> tmpArray = new ArrayList<String>(straightDateArray);
                    pickDateArray.add(tmpArray);
                    straightDateArray.clear();
                }
            }

            i++;
            j++;
        }

        // 연속된 날짜의 이미지를 모두 반환한다.
        ArrayList<ArrayList<ImageData>> resultImageDatas = new ArrayList<ArrayList<ImageData>>();
        ArrayList<ImageData> straightImageDatas = new ArrayList<ImageData>();

        for(int k = 0; k < pickDateArray.size(); k++) {
            ArrayList<String> dateArray = pickDateArray.get(k);
            if(!dateArray.isEmpty()) {
                StringBuilder whereQuery = new StringBuilder();
                for(int l = 0; l < dateArray.size(); l++) {
                    whereQuery.append("dateFormat = '" + dateArray.get(l) + "'");
                    if(l < dateArray.size() - 1) {
                        whereQuery.append(ImageSearchQueryWhere.DB_OR_KEYWORD);
                    }
                }
                String query = getQuerySelectWhereImageData(GALLERY_TABLE_NAME,
                                                            whereQuery.toString());
                straightImageDatas = selectImageDataFromQuery(query);
                ArrayList<ImageData> tmpImageDatas = new ArrayList<ImageData>(straightImageDatas);
                resultImageDatas.add(tmpImageDatas);
                straightImageDatas.clear();
            }
        }

        // 결과가 없다면 null을 반환
        if(resultImageDatas.isEmpty()) {
            return null;
        }

        return resultImageDatas;
    }

    /**
     * 쿼리 전문을 통해 특정 데이터 저장 정보를 불러온다.
     * 
     * @param queryString 요청할 쿼리
     * @return 조건에 부합한 이미지 데이터 배열
     */
    public synchronized ArrayList<ImageData> selectImageDataFromQuery(String queryString) {
        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<ImageData> childs = null;
        Cursor cursor = mDataBase.rawQuery(queryString, null);
        if(cursor.getCount() > 0) {
            childs = getLoadImageDatas(cursor);
        }
        cursor.close();
        mDataBase.close();
        return childs;
    }

    /**
     * 이미지 고유 Id값으로 ImageData를 가져온다.<br>
     * 단, ImageCorrectData는 포함되어 있지 않음.
     * 
     * @param imageId 이미지 고유 Id
     * @return ImageData 이미지 Id값과 매칭되는 이미지의 정보
     */
    public synchronized ImageData selectImageDataForImageId(int imageId) {
        mDataBase = mOpenHelper.getReadableDatabase();
        ImageData imageData = null;
        ArrayList<ImageData> childs = null;
        String query = "select * from GalleryDataTable where imageId = '" + imageId + "'";
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor.getCount() > 0) {
            childs = getLoadImageDatas(cursor);
            if(childs != null && !childs.isEmpty()) {
                imageData = childs.get(0);
            }
        }
        cursor.close();
        mDataBase.close();

        return imageData;
    }

    /**
     * 현재 분석된 DB에서 조건에 맞는 특정 데이터 저장 정보를 불러온다. <br>
     * 
     * @param where DB에서 조회할 조건절 ("Where"은 내부적으로 포함되어있음)
     * @return ArrayList 이미지 데이터 배열
     */
    public synchronized ArrayList<ImageData> selectWhereImageDatas(String where) {
        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<ImageData> childs = null;
        String query = getQuerySelectWhereImageData(GALLERY_TABLE_NAME, where);
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor.getCount() > 0) {
            childs = getLoadImageDatas(cursor);
        }
        cursor.close();
        mDataBase.close();
        return childs;
    }

    /**
     * 현재 분석된 DB에서 조건에 맞는 특정 데이터의 갯수를 리턴.
     * 
     * @param where DB에서 조회할 조건절 ("Where"은 내부적으로 포함되어있음)
     * @return int 조건에 맞는 이미지의 갯수
     */
    public synchronized int selectWhereImageDatasCount(String where) {
        if(where == null || where.length() < 1) {
            return 0;
        }

        mDataBase = mOpenHelper.getReadableDatabase();
        String query = getQuerySelectWhereImageDataCount(GALLERY_TABLE_NAME, where);

        Cursor cursor = mDataBase.rawQuery(query, null);
        int count = 0;
        if(cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        mDataBase.close();
        return count;
    }

    /**
     * 이미지 데이터를 insert한다.
     * 
     * @param imageData 이미지 데이터.
     */
    public synchronized void insertImageData(ImageData imageData) {
        mDataBase = mOpenHelper.getWritableDatabase();
        String query = getQueryInsertImageData(GALLERY_TABLE_NAME, imageData);
        mDataBase.execSQL(query);
        mDataBase.close();
    }

    /**
     * 이미지 데이터를 update 한다.
     * 
     * @param imageData update할 이미지 데이터.
     */
    public synchronized void updateImageData(ImageData imageData) {
        // 이미 있는지 체크하자 있다면 update 없다면 insert를 하자
        int rowCount = selectWhereImageDatasCount("imageId = '" + imageData.id + "'");
        if(rowCount < 1) {
            insertImageData(imageData);
        } else {
            mDataBase = mOpenHelper.getWritableDatabase();
            String query = getQueryUpdateImageData(GALLERY_TABLE_NAME, imageData);
            mDataBase.execSQL(query);
            mDataBase.close();
        }
    }

    /**
     * 이미지 데이터를 DB에서 삭제한다.
     * 
     * @param imageData 삭제할 이미지 데이터.
     */
    public synchronized void deleteImageData(ImageData imageData) {
        mDataBase = mOpenHelper.getWritableDatabase();
        String query = "delete from " + GALLERY_TABLE_NAME + " where imageId = '" + imageData.id
                + "';";
        mDataBase.execSQL(query);
        mDataBase.close();
    }

    /**
     * 이미지 데이터를 DB에서 삭제한다.
     * 
     * @param imageId 삭제할 이미지 데이터 고유 번호.
     */
    public synchronized void deleteImageData(int imageId) {
        mDataBase = mOpenHelper.getWritableDatabase();
        String query = "delete from " + GALLERY_TABLE_NAME + " where imageId = '" + imageId + "';";
        mDataBase.execSQL(query);
        query = "delete from " + GALLERY_FACE_TABLE_NAME + " where imageId = '" + imageId + "';";
        mDataBase.execSQL(query);
        mDataBase.close();
    }

    /**
     * FaceData를 조건절을 통해 검색한다.
     * 
     * @param DB에서 조회할 조건절 ("Where"은 내부적으로 포함되어있음)
     * @return ArrayList 이미지의 얼굴 데이터 배열
     */
    public synchronized ArrayList<ImageFaceData> selectWhereFaceData(String where) {
        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<ImageFaceData> faceDatas = null;
        String query = getQuerySelectWhereImageData(GALLERY_FACE_TABLE_NAME, where);
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor != null && cursor.getCount() > 0) {
            faceDatas = getLoadImageFaceDatas(cursor);
        }
        cursor.close();
        mDataBase.close();
        return faceDatas;
    }

    /**
     * 특정 조건에 맞는 Face Count를 반환.
     * 
     * @param where DB에서 조회할 조건절 ("Where"은 내부적으로 포함되어있음)
     * @return 조건에 해당되는 FaceDatad의 갯수
     */
    public synchronized int selectWhereFaceDataCount(String where) {
        mDataBase = mOpenHelper.getReadableDatabase();
        String query = "select count(*) from " + GALLERY_FACE_TABLE_NAME + " where " + where + ";";
        Cursor cursor = mDataBase.rawQuery(query, null);
        int count = 0;
        if(cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        mDataBase.close();
        return count;
    }

    /**
     * 이미지 데이터에서 Face Data 들을 추출하여 DB에 넣는다.
     * 
     * @param imageData 이미지 데이터.
     */
    public synchronized void insertFaceAllDataItem(ImageData imageData) {
        ArrayList<ImageFaceData> faceDataItems = imageData.faceDataItems;
        if(faceDataItems == null || faceDataItems.isEmpty()) {
            return;
        }

        for(int i = 0; i < faceDataItems.size(); i++) {
            ImageFaceData faceData = faceDataItems.get(i);
            updateFaceDataItem(faceData);
        }
    }

    /**
     * 특정 FaceData를 insert한다.
     * 
     * @param faceData 특정 FaceData.
     */
    public synchronized void insertFaceDataItem(ImageFaceData faceData) {
        mDataBase = mOpenHelper.getWritableDatabase();
        String query = "insert into " + GALLERY_FACE_TABLE_NAME + " values (NULL, " //
                + "'" + faceData.faceDataKey + "'," //
                + "'" + faceData.imageId + "'," //
                + faceData.personId + "," //
                + faceData.faceIndex + "," //
                + "'" + faceData.isUpdatePerson + "'," //
                + "'" + faceData.isRepresentPerson + "'," //
                + faceData.leftEyeBlink + "," //
                + faceData.rightEyeBlink + "," //
                + faceData.pitch + "," //
                + faceData.roll + "," //
                + faceData.yaw + "," //
                + faceData.smileValue + "," //
                + faceData.leftEyePoint.x + "," //
                + faceData.leftEyePoint.y + "," //
                + faceData.rightEyePoint.x + "," //
                + faceData.rightEyePoint.y + "," //
                + faceData.mouthPoint.x + "," //
                + faceData.mouthPoint.y + "," //
                + faceData.faceDetectRect.left + "," //
                + faceData.faceDetectRect.top + "," //
                + faceData.faceDetectRect.width() + "," //
                + faceData.faceDetectRect.height() + "," //
                + faceData.faceRect.left + "," //
                + faceData.faceRect.top + "," //
                + faceData.faceRect.width() + "," //
                + faceData.faceRect.height() + "," //
                + faceData.eyeGazePoint.x + "," //
                + faceData.eyeGazePoint.y + "," //
                + faceData.eyeHorizontalGazeAngle + "," //
                + faceData.eyeVerticalGazeAngle + ")"; //
        mDataBase.execSQL(query);
        mDataBase.close();
    }

    /**
     * 이미지 데이터에 포함되어있는 모든 FaceData를 업데이트 한다.
     * 
     * @param item 이미지 데이터.
     */
    public synchronized void updateFaceAllDataItem(ImageData imageData) {
        ArrayList<ImageFaceData> faceDataItems = imageData.faceDataItems;
        if(faceDataItems != null && !faceDataItems.isEmpty()) {
            for(int i = 0; i < faceDataItems.size(); i++) {
                ImageFaceData faceDataItem = faceDataItems.get(i);
                updateFaceDataItem(faceDataItem);
            }
        }
    }

    /**
     * 특정 FaceData를 업데이트 한다.
     * 
     * @param faceData 특정 FaceData.
     */
    public synchronized void updateFaceDataItem(ImageFaceData faceData) {
        // 없다면 새로 넣는다.
        if(selectWhereFaceDataCount("imageId = '" + faceData.imageId + "' and faceIndex = "
                + faceData.faceIndex) < 1) {
            insertFaceDataItem(faceData);
        } else {
            mDataBase = mOpenHelper.getWritableDatabase();
            String query = "update " + GALLERY_FACE_TABLE_NAME
                    + " set " //
                    + "faceDataKey = '" + faceData.faceDataKey
                    + "', " //
                    + "imageId = '" + faceData.imageId
                    + "', " //
                    + "personId = " + faceData.personId
                    + ", " //
                    + "faceIndex = " + faceData.faceIndex
                    + ", " //
                    + "isUpdatePerson = '" + faceData.isUpdatePerson
                    + "', " //
                    + "isRepresentPerson = '" + faceData.isRepresentPerson
                    + "', " //
                    + "leftEyeBlink = " + faceData.leftEyeBlink
                    + ", " //
                    + "rightEyeBlink = " + faceData.rightEyeBlink
                    + ", " //
                    + "pitch = " + faceData.pitch
                    + ", " //
                    + "roll = " + faceData.roll
                    + ", " //
                    + "yaw = " + faceData.yaw
                    + ", " //
                    + "smileValue = " + faceData.smileValue
                    + ", " //
                    + "leftEyePointX = " + faceData.leftEyePoint.x
                    + ", " //
                    + "leftEyePointY = " + faceData.leftEyePoint.y
                    + ", " //
                    + "rightEyePointX = " + faceData.rightEyePoint.x
                    + ", " //
                    + "rightEyePointY = " + faceData.rightEyePoint.y
                    + ", " //
                    + "mouthPointX = " + faceData.mouthPoint.x
                    + ", " //
                    + "mouthPointY = " + faceData.mouthPoint.y
                    + ", " //
                    + "rectX = " + faceData.faceDetectRect.left
                    + ", " //
                    + "rectY = " + faceData.faceDetectRect.top
                    + ", " //
                    + "rectWidth = " + faceData.faceDetectRect.width()
                    + ", " //
                    + "rectHeight = " + faceData.faceDetectRect.height()
                    + ", " //
                    + "faceRectX = " + faceData.faceRect.left
                    + ", " //
                    + "faceRectY = " + faceData.faceRect.top
                    + ", " //
                    + "faceRectWidth = " + faceData.faceRect.width()
                    + ", " //
                    + "faceRectHeight = " + faceData.faceRect.height()
                    + ", " //
                    + "eyeGazePointX = " + faceData.eyeGazePoint.x
                    + ", " //
                    + "eyeGazePointY = " + faceData.eyeGazePoint.y
                    + ", " //
                    + "eyeHorizontalGazeAngle = " + faceData.eyeHorizontalGazeAngle
                    + ", " //
                    + "eyeVerticalGazeAngle = " + faceData.eyeVerticalGazeAngle
                    + " " //
                    + " where imageId = '" + faceData.imageId + "' And faceIndex = "
                    + faceData.faceIndex; //
            Cursor c = mDataBase.rawQuery(query, null);
            if(c != null) {
                c.getCount();
            }
            c.close();
            mDataBase.close();
        }
    }

    /**
     * 해당 인자로 받은 personId 값을 -111 (디폴트 값)으로 초기화한다.<br>
     * 해당 메소드는 주인공이 10명이상일 경우 분석시 가장 적은 주인공을 초기화 시키기 위해서만 사용
     * 
     * @param personId 디폴트로 초기값으로 돌리고자 하는 주인공 번호
     */
    public synchronized void initPersonId(int personId) {
        mDataBase = mOpenHelper.getWritableDatabase();
        String query = "update " + GALLERY_FACE_TABLE_NAME + " set " //
                + "personId = -111, "//
                + "isUpdatePerson = 'false', "//
                + "isRepresentPerson = 'false'"//
                + " where personId = " + personId + ";"; //
        Cursor c1 = mDataBase.rawQuery(query, null);
        // Warnings 아래의 getCount를 호출하지 않으면 반영되지 않는 이슈가 발생..
        if(c1 != null) {
            c1.getCount();
        }
        c1.close();
        mDataBase.close();
    }

    /**
     * 분석된 이미지 중에서 특정 인물 (personId)이 포함된 사진 리스트를 반환한다.
     * 
     * @param personId 인물의 고유번호
     * @return ArrayList 해당 인물이 포함된 사진리스트
     */
    public synchronized ArrayList<ImageData> getImageDatasForPersonId(int personId) {
        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<ImageData> datas = null;
        String query = "select * from GalleryDataTable where imageId in (select ImageId from GalleryFaceDataTable where personId = "
                + personId + ")";
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor != null && cursor.getCount() > 0) {
            datas = getLoadImageDatas(cursor);
        }
        cursor.close();
        mDataBase.close();
        return datas;
    }

    /**
     * 분석된 이미지들 가운데 인물이 가장 많은 사람을 주인공으로 하여,<br>
     * 해당 사람이 포함된 사진 리스트를 반환함.
     * 
     * @return ArrayList 가장 많은 인물의 사진리스트
     */
    public synchronized ArrayList<ImageData> getImageDataForMainProtagonist() {
        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<ImageData> datas = null;
        String query = "select GalleryDataTable.* from GalleryDataTable, GalleryFaceDataTable, (select personId, count(personId) cnt from GalleryFaceDataTable where personId >= 0 group by personId order by cnt desc limit 1) a where GalleryDataTable.imageId = GalleryFaceDataTable.imageId and GalleryFaceDataTable.personId = a.personId";
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor != null && cursor.getCount() > 0) {
            datas = getLoadImageDatas(cursor);
        }
        cursor.close();
        mDataBase.close();
        return datas;
    }
    
    /**
     * 인물 앨범에 등록된 각 인물 중 사진이 가장 많은 인물의 personId를 반환한다. <br>
     * 없을 경우, -111을 반환.
     * 
     * @return int 가장 많은 인물의 personId
     */
    public synchronized int getPersonIdForMainProtagonist() {
        mDataBase = mOpenHelper.getReadableDatabase();
        int personId = -111;
        String query = "select personId, count(personId) cnt from GalleryFaceDataTable where personId >= 0 group by personId order by cnt desc limit 1";
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor != null && cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
               personId = cursor.getInt(cursor.getColumnIndex("personId"));
            }
        }
        return personId;
    }

    /**
     * 인물 앨범에 등록된 각 인물이 포함된 사진을 1장씩 반환한다. (주인공 리스트 등에서 사용)
     * 
     * @return ArrayList 각 인물이 포함된 사진 배열
     */
    public synchronized ArrayList<ImageData> getProtagonistAlbumImageData() {
        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<ImageData> datas = null;
        String query = "select GalleryFaceDataTable.personId, GalleryDataTable.*, count(personId) from GalleryDataTable, GalleryFaceDataTable where GalleryFaceDataTable.personId != -111 and GalleryFaceDataTable.imageId = GalleryDataTable.imageId and GalleryFaceDataTable.isRepresentPerson = 'true' Group by GalleryFaceDataTable.personId order by count(personId) desc";
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor != null && cursor.getCount() > 0) {
            datas = getLoadImageDatas(cursor);
        }
        cursor.close();
        mDataBase.close();
        return datas;
    }

    /**
     * 분석된 인물의 고유 번호와 해당 인물이 들어있는 이미지 id를 하나의 String으로 묶어 모든 인물의 String을 반환한다.
     * 
     * @return ArrayList 각 인물별 고유 "인물번호:이미지id" String의 배열
     */
    public synchronized ArrayList<String> getRepresentPersonDatas() {
        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<String> datas = null;
        String query = "select * from GalleryFaceDataTable where personId != -111 and isRepresentPerson = 'true' group by personId";
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor != null && cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                if(datas == null) {
                    datas = new ArrayList<String>();
                }
                String data = "";
                int personId = cursor.getInt(cursor.getColumnIndex("personId"));
                int imageId = cursor.getInt(cursor.getColumnIndex("imageId"));
                data = personId + ":" + imageId;
                datas.add(data);
            }
        }
        cursor.close();
        mDataBase.close();
        return datas;
    }

    /**
     * 각 인물의 faceRecognition 앨범에 등록된 인물 수를 반환한다.
     * 
     * @param personId 특정 인물의 고유 번호
     * @return 특정 인물의 faceRecognition 앨범에 등록된 인물 수
     */
    public synchronized int getUpdatePersonCount(int personId) {
        int count = 0;
        mDataBase = mOpenHelper.getReadableDatabase();
        String query = "select * from GalleryFaceDataTable where GalleryFaceDataTable.personId == "
                + personId + " and GalleryFaceDataTable.isUpdatePerson = 'true'";
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor != null) {
            count = cursor.getCount();
        }
        cursor.close();
        mDataBase.close();
        return count;
    }

    /**
     * 콜라주 전용 DB에서 조건에 맞는 특정 데이터 저장 정보를 불러온다. <br>
     * 
     * @param where DB에서 조회할 조건절 ("Where"은 내부적으로 포함되어있음)
     * @return ArrayList 이미지 데이터 배열
     */
    public synchronized ArrayList<ImageData> selectWhereCollageImageDatas(String where) {
        mDataBase = mOpenHelper.getReadableDatabase();
        ArrayList<ImageData> childs = null;
        String query = getQuerySelectWhereImageData(GALLERY_COLLAGE_TABLE_NAME, where);
        Cursor cursor = mDataBase.rawQuery(query, null);
        if(cursor.getCount() > 0) {
            childs = getLoadImageDatas(cursor);
        }
        cursor.close();
        mDataBase.close();
        return childs;
    }

    /**
     * 콜라주 전용 DB에서 조건에 맞는 특정 데이터의 갯수를 리턴.
     * 
     * @param where DB에서 조회할 조건절 ("Where"은 내부적으로 포함되어있음)
     * @return int 조건에 맞는 이미지의 갯수
     */
    public synchronized int selectWhereCollageImageDatasCount(String where) {
        if(where == null || where.length() < 1) {
            return 0;
        }

        mDataBase = mOpenHelper.getReadableDatabase();
        String query = getQuerySelectWhereImageDataCount(GALLERY_COLLAGE_TABLE_NAME, where);
        Cursor cursor = mDataBase.rawQuery(query, null);
        int count = 0;
        if(cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        mDataBase.close();
        return count;
    }

    /**
     * 이미지 데이터를 콜라주 DB에 insert한다.
     * 
     * @param imageData 이미지 데이터.
     */
    public synchronized void insertCollageImageData(ImageData imageData) {
        if(imageData == null) {
            return;
        }

        mDataBase = mOpenHelper.getWritableDatabase();
        String query = getQueryInsertImageData(GALLERY_COLLAGE_TABLE_NAME, imageData);
        mDataBase.execSQL(query);
        mDataBase.close();
    }

    /**
     * 이미지 데이터를 콜라주 DB에 update 한다.
     * 
     * @param imageData update할 이미지 데이터.
     */
    public synchronized void updateCollageImageData(ImageData imageData) {
        if(imageData == null) {
            return;
        }

        // 이미 있는지 체크하자 있다면 update 없다면 insert를 하자
        int rowCount = selectWhereCollageImageDatasCount("imageId = '" + imageData.id + "'");
        if(rowCount < 1) {
            insertCollageImageData(imageData);
        } else {
            mDataBase = mOpenHelper.getWritableDatabase();
            String query = getQueryUpdateImageData(GALLERY_COLLAGE_TABLE_NAME, imageData);
            mDataBase.execSQL(query);
            mDataBase.close();
        }
    }

    /**
     * 이미지 데이터를 콜라주 DB에서 삭제한다.
     * 
     * @param imageData 삭제할 이미지 데이터.
     */
    public synchronized void deleteCollageImageData(ImageData imageData) {
        if(imageData == null) {
            return;
        }

        mDataBase = mOpenHelper.getWritableDatabase();
        String query = "delete from " + GALLERY_COLLAGE_TABLE_NAME + " where imageId = '"
                + imageData.id + "';";
        mDataBase.execSQL(query);
        mDataBase.close();
    }

    /**
     * 콜라주 DB의 전체 내용을 삭제한다.
     */
    public synchronized void deleteCollageData() {
        mDataBase = mOpenHelper.getWritableDatabase();
        String query = "delete from " + GALLERY_COLLAGE_TABLE_NAME + ";";
        mDataBase.execSQL(query);
        mDataBase.close();
    }

    /*
     * 기타 일반
     */
    private ArrayList<ImageData> getLoadImageDatas(Cursor cursor) {

        if(cursor == null) {
            return null;
        }

        ArrayList<ImageData> childs = new ArrayList<ImageData>();
        while(cursor.moveToNext()) {
            String path = cursor.getString(cursor.getColumnIndex("path"));
            File f = new File(path);
            // 없는 파일은 건너뛰기 위함
            if(f.exists()) {
                ImageData child = new ImageData();
                child.id = Integer.valueOf(cursor.getString(cursor.getColumnIndex("imageId")));
                child.albumName = cursor.getString(cursor.getColumnIndex("albumName"));
                child.date = cursor.getLong(cursor.getColumnIndex("date"));
                child.dateFormat = DateUtil.sdf.format(new Date(child.date));
                child.dateAdded = cursor.getLong(cursor.getColumnIndex("dateAdded"));
                child.fileName = cursor.getString(cursor.getColumnIndex("fileName"));
                child.fileSize = cursor.getInt(cursor.getColumnIndex("fileSize"));
                child.path = path;
                child.mimeType = cursor.getString(cursor.getColumnIndex("mimeType"));
                child.latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                child.longitude = cursor.getString(cursor.getColumnIndex("longitude"));
                child.orientation = cursor.getString(cursor.getColumnIndex("orientation"));
                child.width = cursor.getInt(cursor.getColumnIndex("width"));
                child.height = cursor.getInt(cursor.getColumnIndex("height"));
                child.addressShortName = cursor.getString(cursor.getColumnIndex("addressShortName"));
                child.addressFullName = cursor.getString(cursor.getColumnIndex("addressFullName"));
                child.addressCountry = cursor.getString(cursor.getColumnIndex("addressCountry"));
                child.addressCity = cursor.getString(cursor.getColumnIndex("addressCity"));
                child.addressDistrict = cursor.getString(cursor.getColumnIndex("addressDistrict"));
                child.addressTown = cursor.getString(cursor.getColumnIndex("addressTown"));
                child.numberOfFace = cursor.getInt(cursor.getColumnIndex("numberOfFaces"));
                child.faceBitmapScale = cursor.getFloat(cursor.getColumnIndex("faceBitmapScale"));
                child.faceBitmapWidth = cursor.getInt(cursor.getColumnIndex("faceBitmapWidth"));
                child.faceBitmapHeight = cursor.getInt(cursor.getColumnIndex("faceBitmapHeight"));
                String isFinished = cursor.getString(cursor.getColumnIndex("isFinishFaceDetecting"));
                child.isFinishFaceDetecting = "true".equals(isFinished) ? true : false;
                child.avgSmileValue = cursor.getFloat(cursor.getColumnIndex("avgSmileValue"));
                child.avgLeftBlinkValue = cursor.getFloat(cursor.getColumnIndex("avgLeftBlinkValue"));
                child.avgRightBlinkValue = cursor.getFloat(cursor.getColumnIndex("avgRightBlinkValue"));
                child.totalScore = cursor.getInt(cursor.getColumnIndex("totalScore"));
                child.qualityScore = cursor.getInt(cursor.getColumnIndex("qualityScore"));
                child.sharpnessScore = cursor.getInt(cursor.getColumnIndex("sharpnessScore"));
                child.colorSet = cursor.getString(cursor.getColumnIndex("colorSet"));
                child.representColorName = cursor.getString(cursor.getColumnIndex("representColorName"));
                child.brightnessValue = cursor.getInt(cursor.getColumnIndex("brightnessValue"));
                child.numberOfRepresentColor = cursor.getInt(cursor.getColumnIndex("numberOfRepresentColor"));
                isFinished = cursor.getString(cursor.getColumnIndex("isFinishColorsetAnalysis"));
                child.isFinishColorsetAnalysis = "true".equals(isFinished) ? true : false;
                // 얼굴 데이터 정보를 얻어온다.
                child.faceDataItems = selectWhereFaceData("imageId = '" + child.id + "'");
                childs.add(child);
            } else {
                // 파일이 없다면 삭제된 것으로 간주하여 디비에서도 삭제한다.
                int imageId = Integer.valueOf(cursor.getString(cursor.getColumnIndex("imageId")));
                deleteImageData(imageId);
            }
        }
        return childs;
    }

    private ArrayList<ImageFaceData> getLoadImageFaceDatas(Cursor cursor) {
        if(cursor == null) {
            return null;
        }

        ArrayList<ImageFaceData> imageFaceDatas = new ArrayList<ImageFaceData>();
        while(cursor.moveToNext()) {
            ImageFaceData faceData = new ImageFaceData();
            faceData.faceDataKey = cursor.getString(cursor.getColumnIndex("faceDataKey"));
            faceData.imageId = Integer.valueOf(cursor.getString(cursor.getColumnIndex("imageId")));
            faceData.personId = cursor.getInt(cursor.getColumnIndex("personId"));
            faceData.faceIndex = cursor.getInt(cursor.getColumnIndex("faceIndex"));
            String isUpdatePerson = cursor.getString(cursor.getColumnIndex("isUpdatePerson"));
            faceData.isUpdatePerson = "true".equals(isUpdatePerson) ? true : false;
            String isRepresentPerson = cursor.getString(cursor.getColumnIndex("isRepresentPerson"));
            faceData.isRepresentPerson = "true".equals(isRepresentPerson) ? true : false;
            faceData.leftEyeBlink = cursor.getInt(cursor.getColumnIndex("leftEyeBlink"));
            faceData.rightEyeBlink = cursor.getInt(cursor.getColumnIndex("rightEyeBlink"));
            faceData.pitch = cursor.getInt(cursor.getColumnIndex("pitch"));
            faceData.roll = cursor.getInt(cursor.getColumnIndex("roll"));
            faceData.yaw = cursor.getInt(cursor.getColumnIndex("yaw"));
            faceData.smileValue = cursor.getInt(cursor.getColumnIndex("smileValue"));
            int leftEyePointX = cursor.getInt(cursor.getColumnIndex("leftEyePointX"));
            int leftEyePointY = cursor.getInt(cursor.getColumnIndex("leftEyePointY"));
            faceData.leftEyePoint = new FacePoint(leftEyePointX, leftEyePointY);
            int rightEyePointX = cursor.getInt(cursor.getColumnIndex("rightEyePointX"));
            int rightEyePointY = cursor.getInt(cursor.getColumnIndex("rightEyePointY"));
            faceData.rightEyePoint = new FacePoint(rightEyePointX, rightEyePointY);
            int mouthPointX = cursor.getInt(cursor.getColumnIndex("mouthPointX"));
            int mouthPointY = cursor.getInt(cursor.getColumnIndex("mouthPointY"));
            faceData.mouthPoint = new FacePoint(mouthPointX, mouthPointY);
            int rectX = cursor.getInt(cursor.getColumnIndex("rectX"));
            int rectY = cursor.getInt(cursor.getColumnIndex("rectY"));
            int rectWidth = cursor.getInt(cursor.getColumnIndex("rectWidth"));
            int rectHeight = cursor.getInt(cursor.getColumnIndex("rectHeight"));
            faceData.faceDetectRect = new FaceRect(rectX, rectY, rectX + rectWidth, rectY
                    + rectHeight);
            int faceRectX = cursor.getInt(cursor.getColumnIndex("faceRectX"));
            int faceRectY = cursor.getInt(cursor.getColumnIndex("faceRectY"));
            int faceRectWidth = cursor.getInt(cursor.getColumnIndex("faceRectWidth"));
            int faceRectHeight = cursor.getInt(cursor.getColumnIndex("faceRectHeight"));
            faceData.faceRect = new FaceRect(faceRectX, faceRectY, faceRectX + faceRectWidth,
                                             faceRectY + faceRectHeight);
            float eyeGazePointX = cursor.getFloat(cursor.getColumnIndex("eyeGazePointX"));
            float eyeGazePointY = cursor.getFloat(cursor.getColumnIndex("eyeGazePointY"));
            faceData.eyeGazePoint = new FacePointF(eyeGazePointX, eyeGazePointY);
            int eyeHorizontalGazeAngle = cursor.getInt(cursor.getColumnIndex("eyeHorizontalGazeAngle"));
            faceData.eyeHorizontalGazeAngle = eyeHorizontalGazeAngle;
            int eyeVerticalGazeAngle = cursor.getInt(cursor.getColumnIndex("eyeVerticalGazeAngle"));
            faceData.eyeVerticalGazeAngle = eyeVerticalGazeAngle;

            imageFaceDatas.add(faceData);
        }
        return imageFaceDatas;
    }

    private class GalleryDBOpenHelper extends SQLiteOpenHelper {

        private GalleryDBOpenHelper(Context context, String dbName, String tableName,
                CursorFactory factory, int version) {
            super(context, dbName, factory, version);
        }

        // 생성된 DB가 없을 때 한번 호출
        @Override
        public void onCreate(SQLiteDatabase db) {
            String query = "create table " + GALLERY_TABLE_NAME + " ("
                    + "id integer primary key autoincrement, " //
                    + "imageId text, " //
                    + "albumName text, " //
                    + "date INT8, " //
                    + "dateFormat text, " //
                    + "dateAdded INT8, " //
                    + "fileName text, " //
                    + "fileSize integer, " //
                    + "path text, " //
                    + "mimeType text, " //
                    + "latitude REAL, " //
                    + "longitude REAL, " //
                    + "cos_latitude REAL, " //
                    + "sin_latitude REAL, " //
                    + "cos_longitude REAL, " //
                    + "sin_longitude REAL, " //
                    + "orientation text, " //
                    + "width integer, " //
                    + "height integer, " //
                    + "addressShortName text, " //
                    + "addressFullName text, " //
                    + "addressCountry text, " //
                    + "addressCity text, " //
                    + "addressDistrict text, " //
                    + "addressTown text, " //
                    + "numberOfFaces integer, " //
                    + "faceBitmapScale REAL, " //
                    + "faceBitmapWidth integer, " //
                    + "faceBitmapHeight integer, " //
                    + "isFinishFaceDetecting text, " //
                    + "avgSmileValue REAL, " //
                    + "avgLeftBlinkValue REAL, " //
                    + "avgRightBlinkValue REAL, " //
                    + "totalScore integer, " //
                    + "qualityScore integer, " //
                    + "sharpnessScore integer, " //
                    + "colorSet text, " //
                    + "representColorName text, " //
                    + "brightnessValue integer, " //
                    + "numberOfRepresentColor text, " //
                    + "isFinishColorsetAnalysis text) "; //
            db.execSQL(query);

            query = "create table " + GALLERY_FACE_TABLE_NAME + " ("
                    + "id integer primary key autoincrement, " //
                    + "faceDataKey text," //
                    + "imageId text, " //
                    + "personId integer, " //
                    + "faceIndex integer, " //
                    + "isUpdatePerson text, " //
                    + "isRepresentPerson text, " //
                    + "leftEyeBlink integer, " //
                    + "rightEyeBlink integer, " //
                    + "pitch integer, " //
                    + "roll integer, " //
                    + "yaw integer, " //
                    + "smileValue integer, " //
                    + "leftEyePointX integer, " //
                    + "leftEyePointY integer, " //
                    + "rightEyePointX integer, " //
                    + "rightEyePointY integer, " //
                    + "mouthPointX integer, " //
                    + "mouthPointY integer, " //
                    + "rectX integer, " //
                    + "rectY integer, " //
                    + "rectWidth integer, " //
                    + "rectHeight integer, " //
                    + "faceRectX integer, " //
                    + "faceRectY integer, " //
                    + "faceRectWidth integer, " //
                    + "faceRectHeight integer, " //
                    + "eyeGazePointX REAL, " //
                    + "eyeGazePointY REAL, " //
                    + "eyeHorizontalGazeAngle integer, " //
                    + "eyeVerticalGazeAngle integer) "; //
            db.execSQL(query);

            query = "create table " + GALLERY_COLLAGE_TABLE_NAME + " ("
                    + "id integer primary key autoincrement, " //
                    + "imageId text, " //
                    + "albumName text, " //
                    + "date INT8, " //
                    + "dateFormat text, " //
                    + "dateAdded INT8, " //
                    + "fileName text, " //
                    + "fileSize integer, " //
                    + "path text, " //
                    + "mimeType text, " //
                    + "latitude REAL, " //
                    + "longitude REAL, " //
                    + "cos_latitude REAL, " //
                    + "sin_latitude REAL, " //
                    + "cos_longitude REAL, " //
                    + "sin_longitude REAL, " //
                    + "orientation text, " //
                    + "width integer, " //
                    + "height integer, " //
                    + "addressShortName text, " //
                    + "addressFullName text, " //
                    + "numberOfFaces integer, " //
                    + "faceBitmapScale REAL, " //
                    + "faceBitmapWidth integer, " //
                    + "faceBitmapHeight integer, " //
                    + "isFinishFaceDetecting text, " //
                    + "avgSmileValue REAL, " //
                    + "avgLeftBlinkValue REAL, " //
                    + "avgRightBlinkValue REAL, " //
                    + "totalScore integer, " //
                    + "qualityScore integer, " //
                    + "sharpnessScore integer, " //
                    + "colorSet text, " //
                    + "representColorName text, " //
                    + "brightnessValue integer, " //
                    + "numberOfRepresentColor text, " //
                    + "isFinishColorsetAnalysis text) "; //
            db.execSQL(query);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + GALLERY_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + GALLERY_FACE_TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + GALLERY_COLLAGE_TABLE_NAME);
            onCreate(db);
        }
    }
}
