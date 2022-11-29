
package com.kiwiple.imageframework.util;

import java.security.MessageDigest;

/**
 * Base64 관련 코드
 */
public class Base64Coder {

    /**
     * 주어진 문자열을 MD5 + Hex 형태로 변경
     * 
     * @param str 주어진 문자열
     * @return MD5 Hex가 적용된 문자열
     */
    public static String getMD5HashString(String str) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            String utf8String = new String(str.getBytes(), "UTF-8");
            digest.update(utf8String.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for(int i = 0; i < messageDigest.length; i++) {
                String hex = Integer.toHexString(0xFF & messageDigest[i]);
                if(hex.length() == 1) {
                    hex = "0" + hex;
                }
                hexString.append(hex);
            }
            return hexString.toString();

        } catch(Exception e) {
        }
        return "";
    }
}
