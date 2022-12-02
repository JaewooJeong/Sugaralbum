
package com.kiwiple.imageanalysis.utils;

/**
 * Json String을 작성하기 위한 유틸 클래스
 */
public class JsonConverterUtil {

    /**
     * String 형태로 Json 키를 포함한 문자열 반환 <br>
     * ex) key가 "aaa"일 경우, '"aaa" : ' 문자열 반환
     * @param key Json키
     * @return 키 값이 포함된 Json String
     */
    public static String getConvertKey(String key) {
        return "\"" + key + "\" : ";
    }

    /**
     * String 형태의 Json 키 및 값을 포함한 문자열 반환<br>
     * ex) key = "aaa", value = 3의 경우 '"aaa" : 3' 반환
     * @param key Json키
     * @param value 해당 Json키 값의 Value
     * @return 키 및 값이 포함된 Json String
     */
    public static String getConvertString(String key, int value) {
        return getConvertKey(key) + value;
    }

    /**
     * String 형태의 Json 키 및 값을 포함한 문자열 반환<br>
     * ex) key = "aaa", value = 3.0의 경우 '"aaa" : 3.0' 반환
     * @param key Json키
     * @param value 해당 Json키 값의 Value
     * @return 키 및 값이 포함된 Json String
     */
    public static String getConvertString(String key, double value) {
        return getConvertKey(key) + value;
    }

    /**
     * String 형태의 Json 키 및 값을 포함한 문자열 반환<br>
     * ex) key = "aaa", value = true의 경우 '"aaa" : true' 반환
     * @param key Json키
     * @param value 해당 Json키 값의 Value
     * @return 키 및 값이 포함된 Json String
     */
    public static String getConvertString(String key, boolean value) {
        if(value) {
            return getConvertKey(key) + "true";
        } else {
            return getConvertKey(key) + "false";
        }
    }

    /**
     * String 형태의 Json 키 및 값을 포함한 문자열 반환<br>
     * ex) key = "aaa", value = "bbb"의 경우 '"aaa" : "bbb"' 반환
     * @param key Json키
     * @param value 해당 Json키 값의 Value
     * @return 키 및 값이 포함된 Json String
     */
    public static String getConvertString(String key, String value) {
        return getConvertKey(key) + "\"" + value + "\"";
    }
}
