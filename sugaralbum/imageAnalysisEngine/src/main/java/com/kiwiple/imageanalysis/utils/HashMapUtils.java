
package com.kiwiple.imageanalysis.utils;

import java.util.HashMap;

/**
 * 해시 값을 쉽게 꺼내기 위한 유틸 클래스
 */
public class HashMapUtils {

    /**
     * 해시 값 반환 (Object)
     * 
     * @param data 대상 해시
     * @param key 키
     * @param defaultValue null일 경우 반환될 값
     * @return 해시 값
     */
    public static Object getValue(HashMap<String, Object> data, String key, Object defaultValue) {
        return data.get(key) == null ? defaultValue : data.get(key);
    }

    /**
     * 해시 값 반환 (Integer)
     * 
     * @param data 대상 해시
     * @param key 키
     * @param defaultValue null일 경우 반환될 값
     * @return 해시 값
     */
    public static Integer getIntValue(HashMap<String, Object> data, String key, Integer defaultValue) {
        Object object = getValue(data, key, defaultValue);
        if(!(object instanceof Integer)) {
            object = defaultValue;
        }
        return (Integer)object;
    }

    /**
     * 해시 값 반환 (String)
     * 
     * @param data 대상 해시
     * @param key 키
     * @param defaultValue null일 경우 반환될 값
     * @return 해시 값
     */
    public static String getStringValue(HashMap<String, Object> data, String key,
            String defaultValue) {
        Object object = getValue(data, key, defaultValue);
        if(!(object instanceof String)) {
            object = defaultValue;
        }
        return (String)object;
    }

    /**
     * 해시 값 반환 (Boolean)
     * 
     * @param data 대상 해시
     * @param key 키
     * @param defaultValue null일 경우 반환될 값
     * @return 해시 값
     */
    public static Boolean getBooleanValue(HashMap<String, Object> data, String key,
            Boolean defaultValue) {
        Object object = getValue(data, key, defaultValue);
        if(!(object instanceof Boolean)) {
            object = defaultValue;
        }
        return (Boolean)object;
    }

    /**
     * 해시 값 반환 (Double)
     * 
     * @param data 대상 해시
     * @param key 키
     * @param defaultValue null일 경우 반환될 값
     * @return 해시 값
     */
    public static Double getDoubleValue(HashMap<String, Object> data, String key,
            Double defaultValue) {
        Object object = getValue(data, key, defaultValue);
        if(!(object instanceof Double)) {
            object = defaultValue;
        }
        return (Double)object;
    }
}
