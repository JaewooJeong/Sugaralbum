
package com.kiwiple.imageanalysis.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import lg.uplusbox.photo.Cxffilter;
import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore.MediaColumns;
import android.provider.MediaStore.Images.ImageColumns;

import com.kiwiple.imageanalysis.Global;
import com.kiwiple.imageanalysis.analysis.operator.FaceOperator;
import com.kiwiple.imageanalysis.database.GalleryDBManager;
import com.kiwiple.imageanalysis.database.GalleryDBManager.DateAndCount;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.utils.ContentResolverUtil;
import com.kiwiple.imageanalysis.utils.DateUtil;
import com.kiwiple.imageanalysis.utils.SmartLog;

/**
 * ImageAnalysis를 통해 분석된 이미지를 검색하는 클래스. <br>
 * ImageSearchCondition에 각 조건을 설정하여 이미지를 검색할 수 있다.
 */
public class ImageSearch {

    private static final String TAG = ImageSearch.class.getSimpleName();
    private static final String KEY_CHILDS_HASH_PREFIX = "ImageDataId_";

    // 기본 갤러리 커서 항목
    private static final String[] IMAGE_PROJECTION = {
            BaseColumns._ID, MediaColumns.DATA, ImageColumns.DATE_TAKEN, ImageColumns.DATE_ADDED,
            ImageColumns.ORIENTATION, ImageColumns.LATITUDE, ImageColumns.LONGITUDE,
            ImageColumns.BUCKET_ID, ImageColumns.BUCKET_DISPLAY_NAME, MediaColumns.WIDTH,
            MediaColumns.HEIGHT, MediaColumns.MIME_TYPE, ImageColumns.TITLE, ImageColumns.SIZE
    };

    private Context mContext;
    private GalleryDBManager mGalleryDBManager;
    private ImageSearchCondition mImageSearchCondition;
    private ImageSearchDetailCondition mImageSearchDetailCondition;
    private ImageSearchQueryWhere mImageSearchQueryWhere;

    /**
     * 이미지 검색 생성자.
     * 
     * @param context Context
     * @param imageSearchCondition 이미지 검색 조건
     */
    public ImageSearch(Context context, ImageSearchCondition imageSearchCondition) {
        mContext = context;
        mImageSearchCondition = imageSearchCondition;
        if(mImageSearchCondition == null) {
            mImageSearchCondition = new ImageSearchCondition(0, 0);
        }
        mImageSearchDetailCondition = mImageSearchCondition.getImageSearchDetailCondition();
        mGalleryDBManager = new GalleryDBManager(context);
        mImageSearchQueryWhere = new ImageSearchQueryWhere(
                                                           ImageSearchQueryWhere.GALLERY_DATA_TABLE_NAME);
    }

    /**
     * 이미지 조건을 설정한다. <br>
     * 이미지 조건을 변경하고 싶을 경우에 사용함.
     * 
     * @param imageSearchCondition
     */
    public void setImageSearchCondition(ImageSearchCondition imageSearchCondition) {
        mImageSearchCondition = imageSearchCondition;
        mImageSearchDetailCondition = imageSearchCondition.getImageSearchDetailCondition();
    }

    /**
     * 검색 조건을 통해 검색된 이미지 리스트를 반환. <br>
     * 사전에 설정된 조건이 없을 경우 모든 이미지를 반환한다.
     * 
     * @return ArrayList 검색 조건에 부합한 이미지 데이터.
     */
    public ArrayList<ImageData> getImageDatasFromCondition() {
        ArrayList<ImageData> imageDatas = null;
        if(mImageSearchCondition == null) {
            imageDatas = mGalleryDBManager.selectAllImageDatas();
            return imageDatas;
        }

        String queryWhereString = generateQueryTable();

        int imageCount = mImageSearchCondition.getImageSearchCount();
        if(imageCount != ImageSearchCondition.DEFAULT_SEARCH_COUNT) {
            queryWhereString += " limit " + imageCount;
        }

        SmartLog.e(TAG, queryWhereString);
        imageDatas = mGalleryDBManager.selectImageDataFromQuery(queryWhereString);

        return imageDatas;
    }

    /**
     * 이미지 고유 Id값으로 ImageData를 가져온다.<br>
     * 단, ImageCorrectData는 포함되어 있지 않음.
     * 
     * @param imageId 이미지 고유 Id
     * @return ImageData 이미지 Id값과 매칭되는 이미지의 정보
     */
    public ImageData getImagaeDataForImageId(int imageId) {
        return mGalleryDBManager.selectImageDataForImageId(imageId);
    }

    /**
     * 쿼리를 직접 날려 DB 에서 쿼리에 해당되는 데이터를 추출
     * 
     * @param query 쿼리 전문
     * @return ArrayList 쿼리 조건에 해당하는 이미지 데이터 배열
     */
    public ArrayList<ImageData> getImageDatasFromQuery(String query) {
        return mGalleryDBManager.selectImageDataFromQuery(query);
    }

    /**
     * 각 날짜별 이미지 갯수 정보 배열을 반환.<br>
     * order는 이미지 갯수가 가장 많은 날짜부터 출력해준다.
     * 
     * @return ArrayList 날짜별 이미지 갯수 정보 배열
     */
    public ArrayList<DateAndCount> getImageDataCountsInDate() {
        return mGalleryDBManager.getImageDataCountsInDate();
    }

    /**
     * 특정 주소 값으로 이미지 검색을 수행한다. <br>
     * 단, 분석이 종료된 이미지들의 주소 목록만 가져올 수 있다. <br>
     * 분석된 이미지가 없을 경우 null을 반환 <br>
     * 카테고리 항목은 다음과 같다. <br>
     * 1. LocationOperator.ADDRESS_INDEX_OF_COUNTRY (국가별) <br>
     * 2. LocationOperator.ADDRESS_INDEX_OF_CITY (국가별 시/도) <br>
     * 3. LocationOperator.ADDRESS_INDEX_OF_DISTRICT (국가별 시/도 시/군/구) <br>
     * 4. LocationOperator.ADDRESS_INDEX_OF_TOWN (국가별 시/도 시/군/구 읍/면/동) <br>
     * 카테고리 값이 비정상적인 경우 4번을 기준으로 한다. <br>
     * <br>
     * addressValue의 경우는 다음과 같다.<br>
     * LocationOperator.getAddressNameWithCategory(category)를 통해 받은 리스트의 값.
     * 
     * @param addressValue 주석 설명 참조
     * @param category 주석 설명 참조
     * @return ArrayList 인자인 주소값에 해당되는 이미지 데이터 배열
     */
    public ArrayList<ImageData> getImageDatasFromLocationName(String addressValue, int category) {
        return mGalleryDBManager.getImageDatasFromLocationName(addressValue, category);
    }

    /**
     * 등록된 주인공 중 이미지 숫자가 1개밖에 없는 주인공 아이디를 반환
     * 
     * @return ArrayList 주인공 사진이 1개밖에 없는 주인공 아이디
     */
    public ArrayList<Integer> getPersonIdsForMinCount() {
        return mGalleryDBManager.getPersonIdsForMinCount();
    }

    /**
     * 인자로 받은 imageData에 연사로 판단되는 이미지를 가려서 추출한다.<br>
     * 이미지가 burstCount보다 많을 수 있으며, 결과는 최근순서로 정렬되어서 반환된다.<br>
     * 연사 이미지가 있다면 연사 이미지를 묶어 하나의 배열로 만들고 이 배열을 인수로 가진 배열을 반환. 2종류 이상의 연사가 있을 수 있다. <br>
     * 연사가 2번 이상일 경우 각 연사의 이미지를 배열로 감싼 배열을 반환한다. <br>
     * (ex 이미지 내부에 10장의 연사, 5장의 연사가 있다면 10장의 연사를 하나의 배열, 다른 5장의 연사를 또 하나의 배열로 만들고 <br>
     * 두개의 배열을 가진 배열을 반환한다. <br>
     * 인자의 imageData가 null이거나 burstCount가 맞지 않다면 null을 리턴.<br>
     * 연사로 판단되는 이미지가 없을 경우에는 빈 배열을 반환함.
     * 
     * @param imageDatas 판단 대상이 될 이미지 데이터 배열
     * @param burstCount 연사라고 생각되는 기준 갯수 (ex 5라면 5장 이상)
     * @param burstTimeSecond 연사라고 생각되는 시간 기준 (ex 3이라면 3초 이내에)
     * @return ArrayList(ArrayList) burstTimeSecond 이내에 burstCount 이상 이미지가 있다면 연사라고 판단하여 해당되는 이미지들의
     *         배열
     */
    public static ArrayList<ArrayList<ImageData>> getBurstShotImageDatas(
            ArrayList<ImageData> imageDatas, int burstCount, float burstTimeSecond) {

        if(imageDatas == null || imageDatas.size() < 2 || burstCount < 1 || burstTimeSecond < 0) {
            return null;
        }

        // 시간순으로 정렬 (최근 날짜가 가장 앞으로 오도록)
        List<ImageData> sortImageDatas = new ArrayList<ImageData>(imageDatas);
        Comparator<ImageData> comparator = new Comparator<ImageData>() {
            @Override
            public int compare(ImageData lhs, ImageData rhs) {
                if(lhs.date < rhs.date) {
                    return 1;
                } else if(lhs.date == rhs.date) {
                    return 0;
                } else {
                    return -1;
                }
            }
        };
        Collections.sort(sortImageDatas, comparator);

        ArrayList<ArrayList<ImageData>> burstImageDataArray = new ArrayList<ArrayList<ImageData>>();
        ArrayList<ImageData> burstImageDatas = new ArrayList<ImageData>();
        int i = 0;
        int j = 1;
        while(i < sortImageDatas.size() && j < sortImageDatas.size()) {
            ImageData firstImageData = sortImageDatas.get(i);
            ImageData secondImageData = sortImageDatas.get(j);

            // 비교할 시간 값 계산
            long secondDifferenceValue = Math.abs(firstImageData.date - secondImageData.date) / 1000;
            // 첫번째 시간과 두번째 시간의 차이가 설정된 시간보다 크다면 연사가 아니라고 판단
            if(secondDifferenceValue > burstTimeSecond) {
                // 설정된 숫자보다 많이 모였을 경우엔 연사가 종료됨. (이전까지의 이미지를 연사로 판단)
                if(burstImageDatas.size() >= burstCount) {
                    // 최근 시간 순으로 재정렬한다.
                    Collections.sort(burstImageDatas, comparator);
                    // 여기까지 연사를 담고, 새로운 연사를 찾자.
                    ArrayList<ImageData> tmpArray = new ArrayList<ImageData>(burstImageDatas);
                    burstImageDataArray.add(tmpArray);
                    burstImageDatas.clear();
                } else if(!burstImageDatas.isEmpty() && burstImageDatas.size() < burstCount) {
                    // 연사는 끊겼는데 아직 설정된 숫자보다 적다면.. 연사에서 제외하고 다시 새로운 연사를 찾아야함.
                    // 제외하면서 첫번째 대상이 i번째에서 현재 시점의 인식이 되어야하므로 현재 위치를 i번째로 변경한다.
                    burstImageDatas.clear();
                    i = j - 1;
                }
                // 다음 비교로 넘어감
                i++;
                j++;
            } else {
                // 연사 시간 이내의 사진이네?
                // 비어있다면 두개 다 집어넣어야하므로
                if(burstImageDatas.isEmpty()) {
                    burstImageDatas.add(firstImageData);
                }
                burstImageDatas.add(secondImageData);

                // 마지막 사진까지 연사로 넣어진 경우는 연사가 종료됨.
                if(i == sortImageDatas.size() - 1 || j == sortImageDatas.size() - 1) {
                    burstImageDataArray.add(burstImageDatas);
                }
                j++;
            }
        }

        return burstImageDataArray;
    }

    /**
     * 인자로 받은 inputImageData배열에서 컬러 유사도가 <br>
     * 또 다른 인자인 similarityValue보다 낮은 이미지 데이터를 제외한 나머지 배열을 반환한다.<br>
     * 기준 컬러 유사도 값의 디폴트 값은 70.
     * 
     * @param inputImageDatas 검색 대상이 될 이미지 데이터 배열
     * @param similarityValue 기준 컬러 유사도 값 (0~100). 기본 값은 70.
     * @return
     */
    public ArrayList<ImageData> getRemoveNotSimilarityImageData(
            ArrayList<ImageData> inputImageDatas, int similarityValue) {

        if(inputImageDatas == null || inputImageDatas.size() < 2) {
            return null;
        }

        if(similarityValue < 0 || similarityValue > 100) {
            // 기본값은 70으로 설정된다.
            similarityValue = 70;
        }

        Cxffilter cxffilter = new Cxffilter();
        ArrayList<ImageData> resultImageData = new ArrayList<ImageData>();

        // 이미지 데이터 중에 연관 유사도가 떨어지는 것을 제거할 필요가 있음.
        // 두개의 컬러 유사도를 분석하여 30미만인 녀석들을 제거하자.
        int i = 0;
        int j = 1;
        while(i < inputImageDatas.size() && j < inputImageDatas.size()) {
            ImageData firstImageData = inputImageDatas.get(i);
            ImageData secondImageData = inputImageDatas.get(j);
            String firstColorSet = firstImageData.colorSet;
            String secondColorSet = secondImageData.colorSet;

            // 컬러셋이 없다면 비교가 불가능하므로 패스
            if(Global.isNullString(firstColorSet) || Global.isNullString(secondColorSet)) {
                i++;
                j++;
            } else {
                // 컬러셋이 있다면 비교가 가능
                String colorSimilarity = cxffilter.colorSetCompare(firstColorSet, secondColorSet);
                // 비교값이 잘못되어 있다면 패스
                if(colorSimilarity == null || "Error".equals(colorSimilarity)) {
                    i++;
                    j++;
                } else {
                    // 비교값이 설정된 유사도보다 작다면 패스
                    int colorSimilarityValue = Integer.parseInt(colorSimilarity);
                    if(colorSimilarityValue < similarityValue) {
                        i++;
                        j++;
                    } else {
                        // 유사도보다 설정값이 크다면 결과값에 추가해준다.
                        if(resultImageData.isEmpty()) {
                            resultImageData.add(firstImageData);
                        }
                        resultImageData.add(secondImageData);
                    }
                }
            }
        }

        return resultImageData;
    }

    /**
     * 여행이라고 판단되는 이미지를 반환한다.<br>
     * 해당 메소드는 아래와 같은 가정을 전제로 한다.<br>
     * 1. 최저 1박2일 이상의 여행 (1일짜리 여행의 경우 MainDate 등을 통해서도 찾을 수 있다고 판단) <br>
     * 2. 특정 기간 (startDateAmount ~ endDateAmount)사이에 사진이 minImageCount이상인 날짜를 뽑는다. <br>
     * 3. 2.에서 뽑은 날짜가 연속되어진 것이 있다면 연속된 날짜의 모든 이미지를 묶어 하나의 배열로 만들고 배열로 이루어진 배열을 반환. <br>
     * 4. 연속된 날짜가 2번 이상일 경우 각각 연속된 날짜의 이미지를 배열로 감싼 배열을 반환한다. <br>
     * (ex 5/17, 5/14, 5/13, 5/12, 5/9, 5/8, 5/5 등으로 있다면 <br>
     * ( 5/14, 5/13, 5/12일에 해당되는 이미지의 전부를 배열로 묶고, <br>
     * 5/9, 5/8일에 해당되는 이미지의 전부를 다른 배열로 묶어 두개의 배열을 가진 배열을 반환한다.<br>
     * 5. 없다면 null을 반환한다.
     * 
     * @param startDateAmount 시작 기간이 오늘부터 몇일 전인가 (ex 30 => 오늘로부터 30일 이전부터)
     * @param endDateAmount 종료 기간이 오늘부터 몇일 전인가 (ex 10 => 오늘로부터 10일 이전까지)
     * @param minImageCount 최소 여행이라 판단될 이미지 갯수 (ex 20 => 이미지가 20장 이상인 날짜만)
     * @return ArrayList(ArrayList) 해당되는 이미지 배열
     */
    public ArrayList<ArrayList<ImageData>> getTravelImageDatas(int startDateAmount,
            int endDateAmount, int minImageCount) {
        return mGalleryDBManager.getTravelImageDatas(startDateAmount, endDateAmount, minImageCount);
    }

    /**
     * DB에서 가장 중요한 인물의 사진들을 추출한다. 최근 사진에 등장한 인물 최대 10명 중 가장 많이 등장한 인물의 사진을 추출.
     * 
     * @return ArrayList 가장 중요한 인물의 사진 데이터 배열
     */
    public ArrayList<ImageData> getMainProtagonistImageDatas() {
        return mGalleryDBManager.getImageDataForMainProtagonist();
    }
    
    /**
     * 인물 앨범 중 가장 많은 사진을 보유하고 있는 personId를 반환한다. <br>
     * 없을 경우, -111을 반환.
     * 
     * @return int 가장 중요한 인물의 personId
     */
    public int getMainProtagonistPersonId() {
        return mGalleryDBManager.getPersonIdForMainProtagonist();
    }

    /**
     * 인물 앨범에 등록된 각 인물이 포함된 사진을 1장씩 반환한다. (주인공 리스트 등에서 사용)
     * 
     * @return ArrayList 각 인물이 포함된 사진 배열
     */
    public ArrayList<ImageData> getProtagonistAlbumImageData() {
        return mGalleryDBManager.getProtagonistAlbumImageData();
    }

    /**
     * 인물 설정을 위한 DB 조회
     * 
     * @return
     */
    public ArrayList<String> getRepresentPersonDatas() {
        return mGalleryDBManager.getRepresentPersonDatas();
    }

    /**
     * 해당 인자로 받은 personId 값을 -111 (디폴트 값)으로 초기화한다.<br>
     * 해당 메소드는 주인공이 10명이상일 경우 분석시 가장 적은 주인공을 초기화 시키기 위해서만 사용
     * 
     * @param personId 디폴트로 초기값으로 돌리고자 하는 주인공 번호
     */
    public void initPersonId(int personId) {
        if(personId >= 0) {
            mGalleryDBManager.initPersonId(personId);
        }
    }

    /**
     * 특정 인물의 고유 id 값으로 특정 인물이 등장한 사진들을 추출한다.
     * 
     * @param personId 인물의 고유 id값
     * @return ArrayList 특정 인물의 사진 데이터 배열
     */
    public ArrayList<ImageData> getImageDatasForPersonId(int personId) {
        return mGalleryDBManager.getImageDatasForPersonId(personId);
    }

    /**
     * 이미지 검색 조건에 맞게 쿼리를 작성하여 반환한다.
     * 
     * @return String 검색 조건 쿼리전문
     */
    private String generateQueryTable() {

        if(mImageSearchDetailCondition == null) {
            mImageSearchDetailCondition = new ImageSearchDetailCondition();
        }

        // 반경 쿼리의 경우 다른 조건문과 혼합되어 사용할 수 없음
        if(mImageSearchDetailCondition.getLatitude() != ImageSearchDetailCondition.LOCATION_LATITUDE_NONE
                && mImageSearchDetailCondition.getLongitude() != ImageSearchDetailCondition.LOCATION_LONGITUDE_NONE
                && mImageSearchDetailCondition.getLocationDistance() > 0) {
            double latitude = mImageSearchDetailCondition.getLatitude();
            double longitude = mImageSearchDetailCondition.getLongitude();
            double distance = mImageSearchDetailCondition.getLocationDistance();

            return ImageSearchQueryWhere.getLocationDistanceQuery(latitude, longitude, distance);
        }

        // isAuto 검색 조건들은 Table쪽에 쿼리가 추가되어야하므로
        boolean isAutoMainDate = mImageSearchDetailCondition.isAutoSelectMainDate();
        boolean isAutoMainLocation = mImageSearchDetailCondition.isAutoSelectMainLocation();
        boolean isAutoMainCharacter = mImageSearchDetailCondition.isAutoSelectMainCharacter();

        StringBuilder queryString = new StringBuilder(
                                                      mImageSearchQueryWhere.getDefaultSelectGalleryDataTable());
        ArrayList<String> tableQueryArray = new ArrayList<String>();
        ArrayList<String> whereQueryArray = new ArrayList<String>();

        if(isAutoMainDate) {
            StringBuilder sb = new StringBuilder(
                                                 "(select dateFormat, count(dateFormat) cnt from GalleryDataTable where ");
            // 시간 조건
            int startDateAmount = mImageSearchDetailCondition.getStartDateAmount();
            int endDateAmount = mImageSearchDetailCondition.getEndDateAmount();
            ArrayList<String> dateWhereArray = new ArrayList<String>();
            if(startDateAmount > 0) {
                long startTime = DateUtil.getDateTimeAgoDay(startDateAmount);
                dateWhereArray.add("date >= " + startTime);
            }
            if(endDateAmount > 0) {
                long endTime = DateUtil.getDateTimeAgoDay(endDateAmount);
                dateWhereArray.add("date <= " + endTime);
            }

            if(!dateWhereArray.isEmpty()) {
                sb.append("(");
                for(int i = 0; i < dateWhereArray.size(); i++) {
                    sb.append(dateWhereArray.get(i));
                    if(i < dateWhereArray.size() - 1) {
                        sb.append(ImageSearchQueryWhere.DB_AND_KEYWORD);
                    }
                }
                sb.append(")");
                sb.append(ImageSearchQueryWhere.DB_AND_KEYWORD);
            }

            sb.append("dateFormat != 'null' and dateFormat is not null group by dateFormat order by cnt desc limit 1) dateTable");
            tableQueryArray.add(sb.toString());
            whereQueryArray.add("GalleryDataTable.dateFormat = dateTable.dateFormat");
        }

        if(isAutoMainLocation) {
            StringBuilder sb = new StringBuilder(
                                                 "(select addressShortName, count(addressShortName) cnt from GalleryDataTable where ");
            // 시간 조건
            int startDateAmount = mImageSearchDetailCondition.getStartDateAmount();
            int endDateAmount = mImageSearchDetailCondition.getEndDateAmount();
            String dateArrange = generateDateArrangeCheck(startDateAmount, endDateAmount);
            sb.append(dateArrange);
            sb.append("addressShortName != 'null' and addressShortName is not null group by addressShortName order by cnt desc limit 1) locationTable");
            tableQueryArray.add(sb.toString());
            whereQueryArray.add("GalleryDataTable.addressShortName = locationTable.addressShortName");
        }

        if(isAutoMainCharacter && FaceOperator.IS_SUPPORT_SNAPDRAGON_FACE_PROCESSING
                && FaceOperator.IS_SUPPORT_SNAPDRAGON_FACE_RECOGNITION) {
            tableQueryArray.add("GalleryFaceDataTable");

            StringBuilder sb = new StringBuilder(
                                                 "(select personId, count(personId) cnt from GalleryFaceDataTable");
            // 시간 조건
            int startDateAmount = mImageSearchDetailCondition.getStartDateAmount();
            int endDateAmount = mImageSearchDetailCondition.getEndDateAmount();
            ArrayList<String> dateWhereArray = new ArrayList<String>();
            if(startDateAmount > 0) {
                long startTime = DateUtil.getDateTimeAgoDay(startDateAmount);
                dateWhereArray.add(mImageSearchQueryWhere.getWhereStringStartTime(startTime));
            }
            if(endDateAmount > 0) {
                long endTime = DateUtil.getDateTimeAgoDay(endDateAmount);
                dateWhereArray.add(mImageSearchQueryWhere.getWhereStringEndTime(endTime));
            }

            if(!dateWhereArray.isEmpty()) {
                sb.append(", GalleryDataTable where ");
                sb.append("(");
                for(int i = 0; i < dateWhereArray.size(); i++) {
                    sb.append(dateWhereArray.get(i));
                    if(i < dateWhereArray.size() - 1) {
                        sb.append(ImageSearchQueryWhere.DB_AND_KEYWORD);
                    }
                }
                sb.append(")");
                sb.append(ImageSearchQueryWhere.DB_AND_KEYWORD);
            } else {
                sb.append(" where ");
            }

            sb.append("personId >= 0 group by personId order by cnt desc limit 1) faceTable");
            tableQueryArray.add(sb.toString());
            whereQueryArray.add("GalleryDataTable.imageId = GalleryFaceDataTable.imageId");
            whereQueryArray.add("GalleryFaceDataTable.personId = faceTable.personId");
        }

        // 테이블 설정
        if(!tableQueryArray.isEmpty()) {
            for(int i = 0; i < tableQueryArray.size(); i++) {
                queryString.append(",");
                queryString.append(tableQueryArray.get(i));
            }
        }

        // 조건절 설정
        StringBuilder whereString = new StringBuilder();
        if(!whereQueryArray.isEmpty()) {
            whereString.append(" where ");
            for(int i = 0; i < whereQueryArray.size(); i++) {
                whereString.append(whereQueryArray.get(i));
                if(i < whereQueryArray.size() - 1) {
                    whereString.append(" " + ImageSearchQueryWhere.DB_AND_KEYWORD + " ");
                }
            }
        }

        String etcWhereString = generateQueryWhereString();
        if(whereString.length() > 0) {
            if(etcWhereString.length() > 0) {
                whereString.append(ImageSearchQueryWhere.DB_AND_KEYWORD);
                whereString.append(" (" + etcWhereString + ")");
            }
        } else {
            if(etcWhereString.length() > 0) {
                whereString.append(" where ");
                whereString.append(etcWhereString);
            }
        }

        queryString.append(whereString.toString());

        return queryString.toString();
    }

    /**
     * 조건에 맞는 Where 조건문을 반환한다.
     * 
     * @return String 조건 쿼리문
     */
    private String generateQueryWhereString() {
        ArrayList<String> whereStringArray = new ArrayList<String>();
        /**
         * 일반 조건
         */
        int imageSize = mImageSearchCondition.getImageLongsize();
        // #Warning 우선은 콜라주는 고려하지 않는다. 2014-04-18

        // 해상도 조건 추가
        if(imageSize > 0) {
            whereStringArray.add(mImageSearchQueryWhere.getWhereStringFromImageSize(mImageSearchCondition.getImageLongsize()));
        }

        /**
         * 상세 조건
         */
        // 시간 조건
        int startDateAmount = mImageSearchDetailCondition.getStartDateAmount();
        int endDateAmount = mImageSearchDetailCondition.getEndDateAmount();
        if(startDateAmount > 0) {
            long startTime = DateUtil.getDateTimeAgoDay(startDateAmount);
            whereStringArray.add(mImageSearchQueryWhere.getWhereStringStartTime(startTime));
        }
        if(endDateAmount > 0) {
            long endTime = DateUtil.getDateTimeAgoDay(endDateAmount) + (24 * 60 * 60 * 1000 - 1);
            whereStringArray.add(mImageSearchQueryWhere.getWhereStringEndTime(endTime));
        }

        // 위치 조건
        ArrayList<String> addressNames = mImageSearchDetailCondition.getAddressString();
        if(addressNames != null) {
            // 위치 조건들을 OR로 묶기 위함
            StringBuilder locationStrBuffer = new StringBuilder();
            ArrayList<String> locationStringArray = new ArrayList<String>();
            for(int i = 0; i < addressNames.size(); i++) {
                String locationName = addressNames.get(i);
                locationStringArray.add(mImageSearchQueryWhere.getWhereStringLocationName(locationName));
            }

            if(!locationStringArray.isEmpty()) {
                locationStrBuffer.append("(");
                for(int i = 0; i < locationStringArray.size(); i++) {
                    if(i > 0) {
                        locationStrBuffer.append(ImageSearchQueryWhere.DB_OR_KEYWORD);
                    }
                    locationStrBuffer.append(locationStringArray.get(i));
                }
                locationStrBuffer.append(")");
            }

            // 위치 조건이 있으면 추가
            if(locationStrBuffer.length() > 0) {
                whereStringArray.add(locationStrBuffer.toString());
            }
        }

        /**
         * 인물 조건
         */
        boolean isSelectFace = mImageSearchDetailCondition.getIsFace();
        if(isSelectFace) {
            whereStringArray.add(mImageSearchQueryWhere.getWhereStringFacePhoto());
        }

        // 얼굴 웃는 조건이 포함되어 있다면
        int smileValue = mImageSearchDetailCondition.getFaceMinSmileValue();
        if(smileValue > 0) {
            whereStringArray.add(mImageSearchQueryWhere.getWhereStringSmileValue(smileValue));
        }

        // 얼굴 left blink 조건이 포함되어 있다면
        int leftBlinkValue = mImageSearchDetailCondition.getFaceMaxLeftBlinkValue();
        if(leftBlinkValue > 0) {
            whereStringArray.add(mImageSearchQueryWhere.getWhereStringLeftBlinkValue(leftBlinkValue));
        }

        // 얼굴 right blink 조건이 포함되어 있다면
        int rightBlinkValue = mImageSearchDetailCondition.getFaceMaxLeftBlinkValue();
        if(rightBlinkValue > 0) {
            whereStringArray.add(mImageSearchQueryWhere.getWhereStringRightBlinkValue(rightBlinkValue));
        }

        // 선명도 조건
        int focusValue = mImageSearchDetailCondition.getMinFocusValue();
        if(focusValue > 0) {
            whereStringArray.add(mImageSearchQueryWhere.getWhereStringSharpnessScore(focusValue));
        }

        // 퀄리티 조건
        int qualityValue = mImageSearchDetailCondition.getMinQualityValue();
        if(qualityValue > 0) {
            whereStringArray.add(mImageSearchQueryWhere.getWhereStringQualityScore(qualityValue));
        }

        // 종합 점수 조건
        int totalScoreValue = mImageSearchDetailCondition.getMinTotalScoreValue();
        if(totalScoreValue > 0) {
            whereStringArray.add(mImageSearchQueryWhere.getWhereStringTotalScore(qualityValue));
        }

        // 밝기 조건
        int brightnessMinValue = mImageSearchDetailCondition.getMinBrightnessValue();
        if(brightnessMinValue > 0) {
            whereStringArray.add(mImageSearchQueryWhere.getWhereStringMinBrightnessScore(brightnessMinValue));
        }
        int brightnessMaxValue = mImageSearchDetailCondition.getMaxBrightnessValue();
        if(brightnessMaxValue > 0) {
            whereStringArray.add(mImageSearchQueryWhere.getWhereStringMaxBrightnessScore(brightnessMaxValue));
        }

        // 색상 조건
        // 색상 조건의 경우 OR로 묶는다.
        ArrayList<String> colorNames = mImageSearchDetailCondition.getColorNames();
        if(colorNames != null) {
            StringBuilder colorBuffer = new StringBuilder();
            ArrayList<String> colorStringArray = new ArrayList<String>();
            for(int i = 0; i < colorNames.size(); i++) {
                String colorName = colorNames.get(i);
                colorStringArray.add(mImageSearchQueryWhere.getWhereStringColorName(colorName));
            }

            if(!colorStringArray.isEmpty()) {
                colorBuffer.append("(");
                for(int i = 0; i < colorStringArray.size(); i++) {
                    if(i > 0) {
                        colorBuffer.append(ImageSearchQueryWhere.DB_OR_KEYWORD);
                    }
                    colorBuffer.append(colorStringArray.get(i));
                }
                colorBuffer.append(")");
            }

            // 위치 조건이 있으면 추가
            if(colorBuffer.length() > 0) {
                whereStringArray.add(colorBuffer.toString());
            }
        }

        // 컬러풀한 사진은 대표색이 6종류 이상으로 우선 한정한다.
        boolean isAutoColorful = mImageSearchDetailCondition.isAutoSelectColorful();
        if(isAutoColorful) {
            whereStringArray.add(mImageSearchQueryWhere.getWhereStringColorful(6));
        }

        // 모든 조건을 통합하여 하나의 String으로 반환한다.
        StringBuilder whereStringBuffer = new StringBuilder();
        if(!whereStringArray.isEmpty()) {
            for(int i = 0; i < whereStringArray.size(); i++) {
                whereStringBuffer.append(whereStringArray.get(i));
                if(i < whereStringArray.size() - 1) {
                    whereStringBuffer.append(ImageSearchQueryWhere.DB_AND_KEYWORD);
                }
            }
        }

        return whereStringBuffer.toString();
    }

    private String generateDateArrangeCheck(int startDateAmount, int endDateAmount) {
        ArrayList<String> dateWhereArray = new ArrayList<String>();
        if(startDateAmount > 0) {
            long startTime = DateUtil.getDateTimeAgoDay(startDateAmount);
            dateWhereArray.add("date >= " + startTime);
            dateWhereArray.add(mImageSearchQueryWhere.getWhereStringStartTime(startTime));
        }
        if(endDateAmount > 0) {
            long endTime = DateUtil.getDateTimeAgoDay(endDateAmount);
            dateWhereArray.add("date <= " + endTime);
            dateWhereArray.add(mImageSearchQueryWhere.getWhereStringEndTime(endTime));
        }

        StringBuilder sb = new StringBuilder();
        if(!dateWhereArray.isEmpty()) {
            sb.append("(");
            for(int i = 0; i < dateWhereArray.size(); i++) {
                sb.append(dateWhereArray.get(i));
                if(i < dateWhereArray.size() - 1) {
                    sb.append(ImageSearchQueryWhere.DB_AND_KEYWORD);
                }
            }
            sb.append(")");
            sb.append(ImageSearchQueryWhere.DB_AND_KEYWORD);
        }
        return sb.toString();
    }

    /**
     * 검색된 Image Data Array를 Hash로 변환함. <br>
     * 이 때, 키 값은 FileManager.getKeyForChildItemHash(item.id)가 됨.
     * 
     * @param items Image Data Array
     * @return HashMap 검색된 ImageData Array를 Hash로 변환한 값
     */
    public static HashMap<String, ImageData> generateItemHash(ArrayList<ImageData> items) {
        HashMap<String, ImageData> itemHash = new HashMap<String, ImageData>();
        if(items != null && !items.isEmpty()) {
            for(int i = 0; i < items.size(); i++) {
                ImageData child = items.get(i);
                itemHash.put(getKeyForChildItemHash(child.id), child);
            }
        }
        return itemHash;
    }

    /**
     * 이미지 ImageData Hash를 만들때 키 값을 생성한다.
     * 
     * @param id 이미지의 고유 번호
     * @return String Hash의 키 값이 될 문자열
     */
    public static String getKeyForChildItemHash(int id) {
        return KEY_CHILDS_HASH_PREFIX + id;
    }

    /**
     * 분석 DB에서 갤러리에서 지워진 이미지 데이터를 제거
     */
    public void checkDatabaseForImageData() {
        // 1.이미지 분석 db에서 전체 이미지 id리스트를 조회한다.
        ArrayList<Integer> imageDataIdArray = mGalleryDBManager.selectAllImageDatasId();
        if(imageDataIdArray == null || imageDataIdArray.isEmpty()) {
            return;
        }

        // 2.갤러리에 있는 전체 이미지의 id를 조회한다.
        ArrayList<Integer> galleryIdArray = new ArrayList<Integer>();
        Cursor imageCursor = ContentResolverUtil.getImageCursor(mContext, IMAGE_PROJECTION, null,
                                                                null, ImageColumns.DATE_TAKEN
                                                                        + " DESC");
        if(imageCursor != null && imageCursor.getCount() > 0) {
            while(imageCursor.moveToNext()) {
                int id = imageCursor.getInt(imageCursor.getColumnIndex(BaseColumns._ID));
                galleryIdArray.add(id);
            }
        }

        if(galleryIdArray.isEmpty()) {
            return;
        }

        // 3. 이미지 분석 db의 아이디가 실제 갤러리에 존재하는지 체크한다. 없다면 db에서 지워준다.
        for(int i = 0; i < imageDataIdArray.size(); i++) {
            // 각 이미지 데이터 id가 갤러리에 존재하는지 체크
            int imageDataId = imageDataIdArray.get(i);
            if(!galleryIdArray.contains(imageDataId)) {
                // 포함되어있지 않은 아이디 값이라면 DB에서 지워준다.
                mGalleryDBManager.deleteImageData(imageDataId);
            }
        }
    }
}
