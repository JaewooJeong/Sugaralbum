package com.sugarmount.sugarcamera;

import com.sugarmount.sugaralbum.R;

import java.util.Random;

public class PublicVariable {

	// GUI 가이드 기준 해상도
	public static final int BASE_RESOLUTION_WIDTH  = 720;
	public static final int BASE_RESOLUTION_HEIGHT = 1280;
	
	/**
	 * 릴리즈 버전 진행시 true로 설정함.
	 * 개발/검수 단계에서는 false로 함.
	 */
	public static final boolean RELEASE = true;
	
	// SERVER URL
	public static final int SERVER_TYPE_DEV		= 0;	// 개발서버
	public static final int SERVER_TYPE_RELEASE	= 3;	// 상용 서버
	public static final int SERVER_TYPE_DEFAULT = RELEASE ? SERVER_TYPE_RELEASE : SERVER_TYPE_DEV;

	public static final int[] DRAWABLE_BG = {R.drawable.bg_img_1,
												R.drawable.bg_img_2};

	public static int getBackgroundImageID() {
		Random random = new Random();
		if(random.nextBoolean()){
			return DRAWABLE_BG[0];
		}else{
			return DRAWABLE_BG[1];
		}
		
	}

	public static final int UBOX_SUPPORT_PHOTOSTORE_VERSION = 30404;
	public static final int MEDIA_TYPE_IMAGE = 0;
	public static final int MEDIA_TYPE_VIDEO = 1;

	public static final String INTENT_ACTION_DELETE_THUMBNAIL = "intent_action_delete_thumbnail";
	public static final String INTENT_EXTRA_THUMBNAIL_ID = "intent_extra_thumbnail_id";
	

	// Setting movie diary resolution
	public static final int MOVIE_DIARY_RESOLUTION_FHD = 0x04;
	public static final int MOVIE_DIARY_RESOLUTION_HD = 0x05;
	public static final int MOVIE_DIARY_RESOLUTION_NHD = 0x06;

}