package com.sugarmount.sugarcamera.story.utils;

import android.util.Patterns;

import java.util.Locale;

public class StringUtils {

	public static final String PREFIX_FILE_URL = "file://";

	public static int MAX_DURATION_SEC = 0;

	public static boolean isUrl(String url) {
        return Patterns.WEB_URL.matcher(url).matches();
    }
	
	public static boolean isFileUri(String str) {
    	return str.startsWith(PREFIX_FILE_URL);
    }
	
	public static String setPrefixforFileUri(String str) {
    	if(!isFileUri(str)) {
    		return PREFIX_FILE_URL + str;
    	}
    	return str;
    }
	
	/*
	 * 01.01.01 형식의 버전이름을 1.1.1 형식으로 바꿔서 리턴
	 */
	public static String convertToDisplayVersionCode(String versionName) {
		String[] versions = versionName.split("[.]");
		StringBuffer sb = new StringBuffer();
		if(versions != null && versions.length == 3) {			
			for(int i = 0; i < 3; i++) {
				if(Integer.parseInt(versions[i]) < 10) {
					versions[i] = versions[i].replace("0", "");
				}
				sb.append(versions[i]);
				if(i != 2) {
					sb.append(".");
				}
			}			
		}
		return sb.toString();
	}
	
	public static String getTimeStringFromMillis(int timeMs) {
		int time = timeMs / 1000;
		int minute = time / 60;
		int second = time % 60;
		int div = timeMs % 1000;
		second = div == 0 ? second - 1 : second;
		return String.format(Locale.getDefault(), "%02d:%02d", minute, second);
	}

	public static void setMaxDurationSec(int maxMs){
		int time = maxMs / 1000;
		int div = maxMs % 1000;

		// 나머지가 있으면 그대로 없으면 1초를 빼준다.
		MAX_DURATION_SEC = div == 0 ? time - 1 : time;
	}

	public static int getMs2Time(int timeMs){
		int time = timeMs / 1000;

		return time; // second
	}
}
