
package com.kiwiple.imageanalysis.analysis.operator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

import com.kiwiple.imageanalysis.Global;
import com.kiwiple.imageanalysis.database.GalleryDBManager;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.search.ImageSearchQueryWhere;
import com.kiwiple.imageanalysis.utils.SmartLog;

/**
 * 이미지 1개에 대한 위치 분석 클래스
 */
public class LocationOperator {

    /**
     * getAddressValue에서 사용될 인덱스. 국가기준
     */
    public static final int ADDRESS_INDEX_OF_COUNTRY = 0;
    /**
     * getAddressValue에서 사용될 인덱스. 시/도 기준
     */
    public static final int ADDRESS_INDEX_OF_CITY = 1;
    /**
     * getAddressValue에서 사용될 인덱스. 시/군/구 기준
     */
    public static final int ADDRESS_INDEX_OF_DISTRICT = 2;
    /**
     * getAddressValue에서 사용될 인덱스. 읍/면/동 기준
     */
    public static final int ADDRESS_INDEX_OF_TOWN = 3;

    private Context mContext;
    // 위치 탐색을 위한 지오코더
    private ReverseGeocoder mReverseGeocoder;

    /**
     * 생성자
     * 
     * @param context Context
     */
    public LocationOperator(Context context) {
        mContext = context;
        mReverseGeocoder = new ReverseGeocoder(mContext);
    }

    /**
     * 분석 된 이미지 들의 주소 목록을 가져온다. (중복 제거) <br>
     * 단, 분석이 종료된 이미지들의 주소 목록만 가져올 수 있다. <br>
     * 분석된 이미지가 없을 경우 null을 반환
     * 
     * @param context Context
     * @return ArrayList 위치 목록
     */
    public static ArrayList<String> getAddressNameList(Context context) {
        GalleryDBManager galleryDBManager = new GalleryDBManager(context);
        String queryString = ImageSearchQueryWhere.getAddressNameListQuery();
        ArrayList<ImageData> imageDatas = galleryDBManager.selectImageDataFromQuery(queryString);
        if(imageDatas != null) {
            ArrayList<String> addressNames = new ArrayList<String>();
            for(int i = 0; i < imageDatas.size(); i++) {
                addressNames.add(imageDatas.get(i).addressShortName);
            }
            return addressNames;
        }
        return null;
    }

    /**
     * 분석 된 이미지 들의 주소 목록을 가져온다. (중복 제거) <br>
     * 단, 분석이 종료된 이미지들의 주소 목록만 가져올 수 있다. <br>
     * 분석된 이미지가 없을 경우 null을 반환 <br>
     * 카테고리 항목은 다음과 같다. <br>
     * 1. LocationOperator.ADDRESS_INDEX_OF_COUNTRY (국가별) <br>
     * 2. LocationOperator.ADDRESS_INDEX_OF_CITY (국가별 시/도) <br>
     * 3. LocationOperator.ADDRESS_INDEX_OF_DISTRICT (국가별 시/도 시/군/구) <br>
     * 4. LocationOperator.ADDRESS_INDEX_OF_TOWN (국가별 시/도 시/군/구 읍/면/동) <br>
     * 카테고리 값이 비정상적인 경우 4번을 기준으로 한다.
     * 
     * @param category 주석에 표기된 카테고리 값
     * @param context Context
     * @return ArrayList 위치 목록
     */
    public static ArrayList<String> getAddressNameWithCategory(Context context, int category) {
        if(category < ADDRESS_INDEX_OF_COUNTRY || category > ADDRESS_INDEX_OF_TOWN) {
            return null;
        }

        GalleryDBManager galleryDBManager = new GalleryDBManager(context);
        String queryString = ImageSearchQueryWhere.getAddressNameListWithCategory(category);
        ArrayList<ImageData> imageDatas = galleryDBManager.selectImageDataFromQuery(queryString);
        if(imageDatas != null) {
            ArrayList<String> addressNames = new ArrayList<String>();
            for(int i = 0; i < imageDatas.size(); i++) {
                String addressValue = imageDatas.get(i).addressTown;
                switch(category) {
                    case LocationOperator.ADDRESS_INDEX_OF_COUNTRY:
                        addressValue = imageDatas.get(i).addressCountry;
                        break;
                    case LocationOperator.ADDRESS_INDEX_OF_CITY:
                        addressValue = imageDatas.get(i).addressCity;
                        break;
                    case LocationOperator.ADDRESS_INDEX_OF_DISTRICT:
                        addressValue = imageDatas.get(i).addressDistrict;
                        break;
                    default:
                        break;
                }
                addressNames.add(addressValue);
            }
            return addressNames;
        }
        return null;
    }

    /**
     * 위치 값으로 주소 값을 가져온다.
     * 
     * @param latitude 위도 값
     * @param longitude 경도 값
     * @return Address 위도 경도에 맞는 주소 정보
     */
    public Address getAddress(String latitude, String longitude) {
        if(!Global.isNullString(latitude) && !Global.isNullString(longitude)) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            List<Address> address;
            double lat = Double.parseDouble(latitude);
            double lng = Double.parseDouble(longitude);
            try {
                if(geocoder != null && Geocoder.isPresent()) {
                    // 세번째 인수는 최대결과값인데 하나만 리턴받도록 설정했다
                    address = geocoder.getFromLocation(lat, lng, 1);
                    // 설정한 데이터로 주소가 리턴된 데이터가 있으면
                    if(address != null && !address.isEmpty()) {
                        // 주소
                        return address.get(0);
                        // bf.append(address.get(0).getAddressLine(0).toString());
                    }
                }

            } catch(IOException e) {
                SmartLog.e("Location", "Fail get Address");
                return null;
            }
        }
        return null;
    }

    /**
     * 위치 값을 AddressShortName으로 반환.<br>
     * 예) 서초동, 서울시 등
     * 
     * @param latitude 변환할 위도 값
     * @param longitude 변환할 경도 값
     * @return String 위치의 주소
     */
    public String getAddressShortName(String latitude, String longitude) {
        if(!Global.isNullString(latitude) && !Global.isNullString(longitude)) {
            return mReverseGeocoder.generateName(latitude, longitude);
        }
        return null;
    }

    /**
     * Address Full Name을 각각 Country, City, District, Town값으로 변환하여 반환.<br>
     * Country(국가), City(국가 시도), District(국가 시도 시군구), Town(국가 시도 시군구 읍면동) 순으로 갈수록 주소가 상세해짐. 1.
     * Country ex) 대한민국, 일본, 미국 등<br>
     * 2. City ex) 대한민국 서울특별시, 일본 도쿄시, 미국 뉴욕시 등 <br>
     * 3. District ex) 대한민국 서울특별시 강남구, 일본 사가현 니시마츠우라군 등<br>
     * 4. Town ex) 대한민국 서울특별시 서초구 서초3동, 일본 사가현 니시마츠우라군 아리타마을 등<br>
     * <br>
     * AddressFullName을 분할한 값을 반환함. District나 Town등 값이 없는 경우 null을 반환.
     * 
     * @param index 단위. (국가, 도시, 시군구, 읍면동)
     * @param addressFullName 주소 전체 값
     * @return
     */
    public static String getAddressValue(String addressFullName, int index) {

        // 국가 이전단위는 존재하지 않음
        if(Global.isNullString(addressFullName) || index < ADDRESS_INDEX_OF_COUNTRY) {
            return null;
        }

        String[] splitAddress = addressFullName.split(" ");
        StringBuilder addressValue = new StringBuilder();
        if(splitAddress != null && splitAddress.length > index) {
            for(int i = 0; i < index + 1; i++) {
                addressValue.append(splitAddress[i]);
                if(i <= index) {
                    addressValue.append(" ");
                }
            }
        }

        return addressValue.toString();
    }

    /**
     * 주소 String을 가지고 Address 객체를 생성한다.<br>
     * 생성 실패시 null을 반환
     * 
     * @param addressName 주소 이름
     * @return Address 주소 정보
     */
    public Address getLocationFromAddress(String addressName) {
        Address address = null;
        if(!Global.isNullString(addressName)) {
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            List<Address> addresses;
            try {
                if(geocoder != null) {
                    addresses = geocoder.getFromLocationName(addressName, 1);
                    // 설정한 데이터로 주소가 리턴된 데이터가 있으면
                    if(addresses != null && !addresses.isEmpty()) {
                        address = addresses.get(0);
                    }
                }

            } catch(IOException e) {
                SmartLog.e("Location", "Fail get Address");
                e.printStackTrace();
            }
        }
        return address;
    }

    /**
     * 현재 위치를 반환한다.<br>
     * 단, 사전에 GPS가 켜져있는지 체크가 필요하다.
     * 
     * @param ctx Context
     * @return Location 현재 위치
     */
    public static Location getCurrentLocation(Context ctx) {
        // Get current location, we decide the granularity of the string based
        // on this.
        LocationManager locationManager = (LocationManager)ctx.getSystemService(Context.LOCATION_SERVICE);
        Location location = null;
        List<String> providers = locationManager.getAllProviders();
        for(int i = 0; i < providers.size(); ++i) {
            String provider = providers.get(i);
            location = (provider != null) ? locationManager.getLastKnownLocation(provider) : null;
            if(location != null) {
                break;
            }
        }

        return location;
    }

    /**
     * 반경을 위한 cos값을 구하는 함수
     * 
     * @param value cos값을 구해야할 값.
     * @return double cos(radians(lat)) 값
     */
    public static double getCosValue(double value) {
        return Math.cos(deg2rad(value));
    }

    /**
     * 반경을 위한 sin값을 구하는 함수
     * 
     * @param value sin값을 구해야할 값
     * @return double sin(radians(value)) 값
     */
    public static double getSinValue(double value) {
        return Math.sin(deg2rad(value));
    }

    public static double convertPartialDistanceToKm(double result) {
        return Math.acos(result) * 6371;
    }

    public static double convertKmToPartialDistance(double result) {
        return Math.cos(result / 6371);
    }

    public static double deg2rad(double deg) {
        return deg * Math.PI / 180.0;
    }

    /**
     * 네트워크 연결 상태를 반환
     * 
     * @param context Context
     * @return 네트워크 연결 상태
     */
    public static boolean isOnline(Context context) {
        try {
            ConnectivityManager conMan = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);

            State wifi = conMan.getNetworkInfo(1).getState(); // wifi
            if(wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
                return true;
            }

            State mobile = conMan.getNetworkInfo(0).getState(); // mobile
                                                                // ConnectivityManager.TYPE_MOBILE
            if(mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
                return true;
            }

        } catch(NullPointerException e) {
            return false;
        }

        return false;
    }
}
