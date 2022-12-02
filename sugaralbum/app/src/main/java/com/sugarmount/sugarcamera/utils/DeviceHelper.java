package com.sugarmount.sugarcamera.utils;


import android.os.Build;

import com.kiwiple.debug.L;

import java.util.ArrayList;
import java.util.HashMap;

public class DeviceHelper {

	private static final String[] TAKE_PICTURE_POSSIBLE_DEVICES 	   = {"CA-201"};
	private static final String[] FIXED_QUALITY_HIGH_DEVICES  		   = {"SM-P605", "SHV-E330"};
	private static final String[] RECORDERING_HINT_IMPOSSIBLE_DEVICES  = {"SM-N900"};
	private static final String[] UPSUPPORTED_TOGETHER_DEVICES  	   = {"SHV-E500"};
	private static final String[] OPTIMUS_VU_SERIES  	               = {"LG-F100", "LG-F200", "LG-F300"};
	private static final String[] CAMERA_DEVICE_TAKE_PICTURE_DEVICES   = {"LG-F510"};
	public static final int MAX_VIDEO_COUNT = 2; 
	
	private static final String[] FIXED_QUALITY_720P_DEVICES  
	= {
		"LG-F100", // 옵티머스 Vu
		"LG-F160", // 옵티머스 LTE2
		"LG-F180", // 옵티머스 G
		"LG-F200", // 옵티머스 Vu2
		"LG-F240", // 옵티머스 G Pro
		"LG-F300", // 옵티머스 Vu3
		"LG-F310", // M 폰
		"LG-F320", // 옵티머스 G2
		"LG-F340", // G Flex
//		"IM-A820", // 베가 LTE EX
//		"IM-A830", // 베가 레이서2
//		"IM-A850", // 베가 레이서3
		"IM-A860", // 베가 No6
//		"IM-A870", // 베가 아이언
//		"IM-A890", // 베가 시크릿노트				
		"SHV-E120", // 갤럭시 S2 HD LTE
		"SHV-E170", // 갤럭시 R style
		"SHV-E250", // 갤럭시 노트2
		"SHV-E210", // 갤럭시 S3
		"SHV-E300", // 갤럭시 S4
		"SM-N900", // 갤럭시 노트3
		"CA-201" // CASIO LTE 
		
	};
	
//	public static Map videoCountPerDevice = new HashMap() {{
//		//TODO : 비디오를 추가 하지 못하는 모델만 조사한다.  
//	    
//	}};
	
	/**
	 * 코덱이상으로 미지원 단말 리스트 
	 */
	public static ArrayList<String> unSupportedDevice = new ArrayList<String>();
	static {
		unSupportedDevice.add("LG-F100");		//옵티머스 뷰1  , QOMX colorformat : 2141391875
		unSupportedDevice.add("SHV-E160");	//노트 1	, QOMX colorformat : 2141391875
		unSupportedDevice.add("IM-A820");		//베가 LTE-ex  , QOMX colorformat : 2141391875

		unSupportedDevice.add("LG-F480");		//Wine Smart	>>

		unSupportedDevice.add("IM-A830");		//베가레이서 2	>>
		unSupportedDevice.add("IM-A850");		//베가 R3	>>
		unSupportedDevice.add("IM-A860");		//베가 No.6 	>>

		unSupportedDevice.add("Galaxy Nexus"); 		//갤럭시 넥서스 
		
		unSupportedDevice.add("SHV-E220");	//갤럭시 팝	>>
		unSupportedDevice.add("LG-LU6200");	//옵티머스 LTE 무비다이어리 미지원 단말 추가 : 15.12.29

	}
	
	public static ArrayList<String> setJpegThumbnailSizeDevice = new ArrayList<String>();
	static {
		setJpegThumbnailSizeDevice.add("SM-G920"); //갤럭시 s6 
		setJpegThumbnailSizeDevice.add("SM-G928"); //갤럭시 s6 edge 
	}
	
	public static ArrayList<String> maxiumlimitationReleaseDevice = new ArrayList<String>();
	static{
		maxiumlimitationReleaseDevice.add("SM-G920");   // S6 
		maxiumlimitationReleaseDevice.add("LG-F500");    // G4
	}
	
	public static HashMap<String, Integer> videoCountPerDevice = new HashMap<String, Integer>();
	
	/**
	 * jhshin
	 * 테스트 된 단말 lists  
	 * 20150325 비디오 count는 FHD(1920 * 1080) 미만의 해상도 기준 임 
	 */
	static {
		videoCountPerDevice.put("LG-F120", 0);		//옵티머스 LTE TAG 
		videoCountPerDevice.put("LG-F160", 0);		//옵티머스 LTE 2
		videoCountPerDevice.put("LG-F180", 0);		//옵G
		
		videoCountPerDevice.put("LG-F200", 0);		//옵티머스 뷰2
		videoCountPerDevice.put("LG-F240", 0);		//옵티머스 G-pro	, QOMX colorformat : 2141391875

		videoCountPerDevice.put("LG-F300", 2);		//옵티머스 Vu-3
		videoCountPerDevice.put("LG-F310", 0);		//옵티머스Gx == 오메가 
		videoCountPerDevice.put("LG-F320", 2);		//G2
		videoCountPerDevice.put("LG-F340", 2);		//G-Flex
		videoCountPerDevice.put("LG-F350", 2);		//G-Pro 2
		videoCountPerDevice.put("LG-F370", 2);		//G-Pro mini
		
		videoCountPerDevice.put("LG-F400", 2);		//옵티머스 G3
		videoCountPerDevice.put("LG-F470", 2);		//G3 beat 
		videoCountPerDevice.put("LG-F430", 1);		//G3 Vista == Gx2
		videoCountPerDevice.put("LG-F460", 2);		//G3 cat.6
		videoCountPerDevice.put("LG-F490", 2);		//G3 Screen

		videoCountPerDevice.put("LG-F500", 2);		//G4 
		videoCountPerDevice.put("LG-F510", 2);		//G-Flex2
		videoCountPerDevice.put("LG-F520", 1);		//AKA   //20150414  test   1 to 2
		videoCountPerDevice.put("LG-F540", 2);		//미정 

		videoCountPerDevice.put("SHV-E120", 0);		//갤럭시 S2 HD
		videoCountPerDevice.put("SHV-E170", 0);		//갤럭시 R Style
		
		videoCountPerDevice.put("SHV-E210", 1);		//갤럭시 S3 LTE  
		videoCountPerDevice.put("SHV-E250", 1);		//갤럭시 노트2
		videoCountPerDevice.put("SHV-E270", 1);		//갤럭시 그랜드 
		
		videoCountPerDevice.put("SHV-E300", 2);		//갤럭시 S4
		videoCountPerDevice.put("SHV-E310", 0);		//갤럭시 메가  , QOMX colorformat : 2141391875 
		videoCountPerDevice.put("SHV-E330", 2);		//갤럭시 S4 LTE-A 
		videoCountPerDevice.put("SHV-E500", 1);		//갤럭시 윈 
		
		videoCountPerDevice.put("SM-N900", 2);		//갤럭시 노트3
		videoCountPerDevice.put("SM-N750", 2);		//갤럭시 노트3 Neo
		videoCountPerDevice.put("SM-G900", 2);		//갤럭시 S5
		
		videoCountPerDevice.put("SM-C105", 1);		//갤럭시 S4-Zoom
		videoCountPerDevice.put("SM-C115", 1);		//갤럭시 Zoom2

		videoCountPerDevice.put("SM-G906", 2);		//갤럭시 S5 광대역 
		videoCountPerDevice.put("SM-G850", 2);		//갤럭시 알파 
		videoCountPerDevice.put("SM-N910", 2);		//갤럭시 노트4 
		videoCountPerDevice.put("SM-N915", 2);		//갤럭시 노트4 엣지 
		videoCountPerDevice.put("SM-N916", 2);		//갤럭시 노트4 S-LTE
		
		videoCountPerDevice.put("SM-A500", 1);		//갤럭시 A5
		videoCountPerDevice.put("SM-A700", 1);		//갤럭시 A7
		videoCountPerDevice.put("SM-G710", 1);		//갤럭시 Grand2

		videoCountPerDevice.put("SM-G920", 2);		//갤럭시 S6
		videoCountPerDevice.put("SM-G925", 2);		//갤럭시 S6 엣지 
		
		videoCountPerDevice.put("IM-A870", 0);		//베가 아이언 
		videoCountPerDevice.put("IM-A890", 2);		//베가시크릿노트
		videoCountPerDevice.put("IM-A900", 2);		//베가시크릿업 
		videoCountPerDevice.put("IM-A910", 2);		//베가 아이언2  
		
	}
	
	
	public static boolean isMaxiumLimitationReleaseDevice(){
		
		boolean result = false;
		StringBuilder info = new StringBuilder();
		char check;
		String modelFullName = Build.MODEL;
		int end = modelFullName.length(); 
		final int ASCII_ZERO = 48; 
		final int ASCII_NINE = 57; 

		
		for(int i = modelFullName.length() - 1; i > 0 ; i--){
			check = modelFullName.charAt(i);
			if( check >= ASCII_ZERO && check <= ASCII_NINE)
			{	
				end = i + 1;
				break; 
			}
		}	
		
		String model = modelFullName.substring(0, end);
		info.append(model);
		if(maxiumlimitationReleaseDevice.contains(model)){
			result = true;
			info.append(" ");
			info.append("is supported");
		}else{
			result = false;
			info.append(" ");
			info.append("is not supported");
		}
		L.i(info.toString());
		return result;	
	}
	
	
	
	/**
	 * 미지원 단말에 대해서 무비다이어리 기능 Disable
	 */
	
	public static boolean isUnSupportedDevice(){
		
		boolean result = false;
		StringBuilder info = new StringBuilder();
		char check;
		String modelFullName = Build.MODEL;
		int end = modelFullName.length(); 
		final int ASCII_ZERO = 48; 
		final int ASCII_NINE = 57; 

		
		for(int i = modelFullName.length() - 1; i > 0 ; i--){
			check = modelFullName.charAt(i);
			if( check >= ASCII_ZERO && check <= ASCII_NINE)
			{	
				end = i + 1;
				break; 
			}
		}	
		
		String model = modelFullName.substring(0, end);
		info.append(model);
		if(unSupportedDevice.contains(model)){
			result = true;
			info.append(" ");
			info.append("is not supported");
		}else{
			result = false;
			info.append(" ");
			info.append("is supported");
		}
		L.i(info.toString());
		return result;	
	}
	
	public static boolean isSetJpegThumbnailSize(){		
		boolean result = false;
		StringBuilder info = new StringBuilder();
		char check;
		String modelFullName = Build.MODEL;
		int end = modelFullName.length(); 
		final int ASCII_ZERO = 48; 
		final int ASCII_NINE = 57; 
	
		
		for(int i = modelFullName.length() - 1; i > 0 ; i--){
			check = modelFullName.charAt(i);
			if( check >= ASCII_ZERO && check <= ASCII_NINE)
			{	
				end = i + 1;
				break; 
			}
		}	
		
		String model = modelFullName.substring(0, end);
		info.append(model);
		if(setJpegThumbnailSizeDevice.contains(model)){
			result = true;
			info.append(" ");
			info.append(" need to set jpeg thumbnail size");
		}else{
			result = false;
			info.append(" ");
			info.append(" does not need to set jpeg thunmail size");
		}
		L.i(info.toString());
		return result;	
	}
	
	public static int checkPossibleVideoCount(boolean isHighEnd){
		/**
		 * jhshin
		 * 조사된 단말 외 >>Video Scene 2건 
		 */
		int videoCount =0; 
		char check;
		int end = 0; 
		String modelFullName = Build.MODEL;
		final int ASCII_ZERO = 48; 
		final int ASCII_NINE = 57; 
		
		if(isHighEnd){
			videoCount = 2;
		}
		
		for(int i = modelFullName.length() - 1; i > 0 ; i--){
			check = modelFullName.charAt(i);
			if( check >= ASCII_ZERO && check <= ASCII_NINE)
			{	
				end = i + 1;
				break; 
			}
		}	
		
		String model = modelFullName.substring(0, end);
		if(videoCountPerDevice.containsKey(model)){
			videoCount = (int) videoCountPerDevice.get(model);
		}
		L.i("possible video scene :: "+ videoCount);
		return videoCount; 
	}
	
	public static boolean isPossibleTakePicture() {
		for(String device : TAKE_PICTURE_POSSIBLE_DEVICES) {
			String model = Build.MODEL;
			if(model.contains(device)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isFixedQualityHigh() {
		for(String device : FIXED_QUALITY_HIGH_DEVICES) {
			String model = Build.MODEL;
			if(model.contains(device)) {
				return true;
			}
		}
		
		return false;		
	}
	
	public static boolean isFixedQuality720() {
		for(String device : FIXED_QUALITY_720P_DEVICES) {
			String model = Build.MODEL;
			if(model.contains(device)) {
				return true;
			}
		}
		
		return false;		
	}
	
	public static boolean isPossibleRecoderingHint() {
		for(String device : RECORDERING_HINT_IMPOSSIBLE_DEVICES) {
			String model = Build.MODEL;
			if(model.contains(device)) {
				return false;
			}
		}
		
		return true;
	}
	
	public static boolean isUnsupportedTogether() {
		for(String device : UPSUPPORTED_TOGETHER_DEVICES) {
			String model = Build.MODEL;
			if(model.contains(device)) {
				return true;
			}
		}
		
		return false;
	}
	
	public static boolean isOptimusVuSeries() {
		for(String device : OPTIMUS_VU_SERIES) {
			String model = Build.MODEL;
			if(model.contains(device)) {
				return true;
			}
		}
		
		return false;

	}
	
	public static boolean isOptimusG() {
		String model = Build.MODEL;
		if(model.contains("LG-F180")) {
			return true;
		}
		
		return false;
	}
	
	public static boolean isGallexyNote2() {
		String model = Build.MODEL;
		if(model.contains("SHV-E250")) {
			return true;
		}
		
		return false;
	}
	
	public static boolean is070Player3() {
		String model = Build.MODEL;
		if(model.contains("LG-FL40")) {
			return true;
		}
		
		return false;
	}

    public static boolean isCameraDeviceTakePicture() {
        // for(String device : CAMERA_DEVICE_TAKE_PICTURE_DEVICES) {
        // String model = Build.MODEL;
        // if(model.contains(device)) {
        // return true;
        // }
        // }
        //
        // return false;

        // 20150216 olive : #10672 fixes java.lang.NumberFormatException on VideoModule
        // String versionString = android.os.Build.VERSION.RELEASE;
        // float version = Float.parseFloat(versionString);
        //
        // if(version >= 5.0) return true;
        //
        // return false;
        return android.os.Build.VERSION.SDK_INT > 20;
    }
}
