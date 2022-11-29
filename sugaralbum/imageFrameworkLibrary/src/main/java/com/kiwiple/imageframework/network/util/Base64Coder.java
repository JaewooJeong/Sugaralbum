
package com.kiwiple.imageframework.network.util;

import java.security.MessageDigest;

public class Base64Coder {
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
} // end class Base64Coder

