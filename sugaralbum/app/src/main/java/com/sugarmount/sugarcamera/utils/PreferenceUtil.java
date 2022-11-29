package com.sugarmount.sugarcamera.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.sugarmount.sugarcamera.PublicVariable;
import com.sugarmount.sugarcamera.story.gallery.MovieDiaryConfig;
import com.sugarmount.sugarcamera.ui.gallery2.Config;
//import com.sugarmount.sugarcamera.story.gallery.Config;

public class PreferenceUtil {

	public static final String PREF_NAME = "SENSITIVE_PREF"; // Preference Name

	
	private static final String USER_UBOX_AUTH_ID = "user_ubox_auth_id";
	private static final String USER_UBOX_AUTH_KEY = "user_ubox_auth_key";
	
	private static final String ALPAH_UBOX_SERVER_URL = "alpah_ubox_server_url";
	private static final String ALPAH_UBOX_API_SERVER_URL = "alpah_ubox_api_server_url";
	

	private static final String CAMERA_MODULE_INDEX = "camera_module_index";
	private static final String SERVER_TYPE = "server_type";
	private static final String USER_UBOX_SERVER_URL = "user_ubox_server_url";
	private static final String USER_UBOX_API_SERVER_URL = "user_ubox_api_server_url";
	private static final String FAQ_SERVER_URL = "faq_server_url";

	private static final String USE_PREVIEW_AFTER_SHOOT = "use_preview_after_shoot";

	private static final String BUBBLE_POPUP_LAST_CHECKED_TIME = "bubble_popup_last_checked_time";
	private static final String BUBBLE_POPUP_LAST_CHECKED_COUNT = "bubble_popup_last_checked_count";
	private static final String BUBBLE_POPUP_LAST_CHECKED_COUNT_MOVIE_DIARY_REWARD = "bubble_popup_last_checked_count_movie_diary_reward";
	private static final String FB_LAST_NOTIFICATION_ID = "fb_last_notification_id";

	private static final String SETTING_MAIN_PAGE = "setting_maing_page";

	private static final String SETTING_MOVIE_DIARY_RESOLUTION = "setting_movie_diary_resolution";
	private static final String SETTING_MOVIE_DIARY_AUTO_UPDATE = "setting_movie_diary_AUTO_UPDATE";

	private static final String MOVIE_DIARY_GUIDE_DIALOG_VISIBLE = "movie_diary_guide_dialog_visible";

	// for gallery2
	private static final String GALLERY_COLUMN_MODE = "gallery_column_mode";
	private static final String MOVIEDIARY_GALLERY_FOLDER_COLUMN_MODE = "moviediary_gallery_folder_column_mode";
	private static final String CAMERA_GALLERY_FOLDER_COLUMN_MODE = "camera_gallery_folder_column_mode";

	private static final String KEY_CHECKED_COACH_GUIDE = "key_checked_coach_guide";
	private static final String KEY_CHECKED_GALLERY_MOVIEDIARY = "key_checked_gallery_moviediary";
	
	private static final String KEY_CHECKED_FIRST_AUTO_MOVIEDIARY = "key_checked_first_auto_moviediary";
	private static final String KEY_CHECKED_FIRST_MOVIEDIARY = "key_checked_first_moviediary";
	private static final String KEY_CHECKED_SMARTBULLETIN_ONUPDATE = "key_checked_smartbulletin_onupdate";
	
	private static final String KEY_CHECKED_TAB_PERSON_BUBBLE_POPUP = "key_checked_tab_person_bubble_popup";
	
	private static SharedPreferences mPreference = null;
	private static Context mContext = null;

	/**
	 * Singleton
	 * 
	 * @param context
	 *            : getApplicationContext();
	 **/
	public static void init(Context context) {
		mContext = context;
		if (context != null) {
			mPreference = mContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		}
	}

	// ------- Overloading Method (Set preference data) ------- //
	private static void setPreferenceValue(String preName, boolean flag) {
		Editor editor = mPreference.edit();
		editor.putBoolean(preName, flag);
		editor.commit();
	}

	private static void setPreferenceValue(String preName, String str) {
		Editor editor = mPreference.edit();
		editor.putString(preName, str);
		editor.commit();
	}

	private static void setPreferenceValue(String preName, int value) {
		Editor editor = mPreference.edit();
		editor.putInt(preName, value);
		editor.commit();
	}

	private static void setPreferenceValue(String preName, long value) {
		Editor editor = mPreference.edit();
		editor.putLong(preName, value);
		editor.commit();
	}

	// ---------------------- Setter -------------------------- //
	public static void setCameraModuleIndex(int index) {
		setPreferenceValue(CAMERA_MODULE_INDEX, index);
	}

	public static void setServerType(int type) {
		setPreferenceValue(SERVER_TYPE, type);
	}

	public static void setUserUboxServerUrl(String url) {
		setPreferenceValue(USER_UBOX_SERVER_URL, url);
	}

	public static void setUserUboxApiServerUrl(String url) {
		setPreferenceValue(USER_UBOX_API_SERVER_URL, url);
	}
	
	public static void setAlpahUboxServerUrl(String url) {
		setPreferenceValue(ALPAH_UBOX_SERVER_URL, url);
	}

	public static void setAlpahUboxApiServerUrl(String url) {
		setPreferenceValue(ALPAH_UBOX_API_SERVER_URL, url);
	}
	
	public static void setUserUboxAuthId(String url) {
		setPreferenceValue(USER_UBOX_AUTH_ID, url);
	}

	public static void setUserUboxAuthKey(String url) {
		setPreferenceValue(USER_UBOX_AUTH_KEY, url);
	}
	

	public static void setFaqServerUrl(String url) {
		setPreferenceValue(FAQ_SERVER_URL, url);
	}

	public static void setShouldPreivewAfterShoot(boolean flag) {
		setPreferenceValue(USE_PREVIEW_AFTER_SHOOT, flag);
	}

	// movie diary resolition 
	public static void setSettingMovieDiaryResolution(int resolution) {
		setPreferenceValue(SETTING_MOVIE_DIARY_RESOLUTION, resolution);
	}
	// movie diary auto update
	public static void setSettingMovieDiaryAutoUpdate(boolean enable) {
		setPreferenceValue(SETTING_MOVIE_DIARY_AUTO_UPDATE, enable);
	}

	public static void setSettingMaingPage(int mainPage) {
		setPreferenceValue(SETTING_MAIN_PAGE, mainPage);
	}

	public static void setCheckedGalleryBubblePopupTime(long value) {
		setPreferenceValue(BUBBLE_POPUP_LAST_CHECKED_TIME, value);
	}

	public static void setCheckedGalleryBubblePopupCount(int value) {
		setPreferenceValue(BUBBLE_POPUP_LAST_CHECKED_COUNT, value);
	}

	public static void setGalleryColumnMode(int mode) {
		setPreferenceValue(GALLERY_COLUMN_MODE, mode);
	}
	
	public static void setMovieDiaryGalleryFolderColumnMode(int mode) {
		setPreferenceValue(MOVIEDIARY_GALLERY_FOLDER_COLUMN_MODE, mode);
	}
	
	public static void setCameraGalleryFolderColumnMode(int mode) {
		setPreferenceValue(CAMERA_GALLERY_FOLDER_COLUMN_MODE, mode);
	}

	public static void setCheckedCoachGuide(boolean value) {
		setPreferenceValue(KEY_CHECKED_COACH_GUIDE, value);
	}
	
	public static void setCheckedFirstAutoMovieDiary(boolean value) {
		setPreferenceValue(KEY_CHECKED_FIRST_AUTO_MOVIEDIARY, value);
	}
	
	public static void setCheckedFirstMovieDiary(boolean value) {
		setPreferenceValue(KEY_CHECKED_FIRST_MOVIEDIARY, value);
	}
	public static void setCheckedSmartBulletInOnUpdate(boolean value) {
		setPreferenceValue(KEY_CHECKED_SMARTBULLETIN_ONUPDATE, value);
	}
	
	public static void setCheckedGalleryMovieDiary(boolean value) {
		setPreferenceValue(KEY_CHECKED_GALLERY_MOVIEDIARY, value);
	}
	
	public static void setCheckedTabPersonBubblePopUp(boolean value) {
		setPreferenceValue(KEY_CHECKED_TAB_PERSON_BUBBLE_POPUP, value);
	}
	
	// ---------------------- Getter -------------------------- //
	public static int getCameraModuleIndex() {
		return mPreference.getInt(CAMERA_MODULE_INDEX, 0);
	}

	public static int getServerType() {
		return mPreference.getInt(SERVER_TYPE, PublicVariable.SERVER_TYPE_DEFAULT);
	}

	public static String getUserUboxServerUrl() {
		return mPreference.getString(USER_UBOX_SERVER_URL, "");
	}

	public static String getUserUboxApiServerUrl() {
		return mPreference.getString(USER_UBOX_API_SERVER_URL, "");
	}

	
	public static String getAlpahUboxServerUrl() {
		return mPreference.getString(ALPAH_UBOX_SERVER_URL, "");
	}

	public static String getAlpahUboxApiServerUrl() {
		return mPreference.getString(ALPAH_UBOX_API_SERVER_URL, "");
	}
	
	public static String getUserUboxAuthId() {
		return mPreference.getString(USER_UBOX_AUTH_ID, "");
	}

	public static String getUserUboxAuthKey() {
		return mPreference.getString(USER_UBOX_AUTH_KEY, "");
	}
	
	
	
	public static String getFaqServerUrl() {
		return mPreference.getString(FAQ_SERVER_URL, "");
	}

	public static long getCheckedBubblePopupTime() {
		return mPreference.getLong(BUBBLE_POPUP_LAST_CHECKED_TIME, 0);
	}

	public static int getCheckedBubblePopupCount() {
		return mPreference.getInt(BUBBLE_POPUP_LAST_CHECKED_COUNT, 0);
	}

	public static int getCheckedBubblePopupCountMovieDiaryReward() {
		return mPreference.getInt(BUBBLE_POPUP_LAST_CHECKED_COUNT_MOVIE_DIARY_REWARD, 0);
	}
	
	public static int getFBLastNotiId() {
		return mPreference.getInt(FB_LAST_NOTIFICATION_ID, 12345);
	}

	public static boolean shouldPreviewAfterShoot() {
		// 촬영 후 미리보기 기능은 default가 true
		return mPreference.getBoolean(USE_PREVIEW_AFTER_SHOOT, true);
	}

	public static int getSettingMainPage(int defaultValue) {
		return mPreference.getInt(SETTING_MAIN_PAGE, defaultValue);
	}

	public static int getGalleryColumnMode() {
		return mPreference.getInt(GALLERY_COLUMN_MODE, Config.COLUMN_MODE_THREE);
	}
	public static int getMovieDiaryGalleryFolderColumnMode() {
		return mPreference.getInt(MOVIEDIARY_GALLERY_FOLDER_COLUMN_MODE, MovieDiaryConfig.COLUMN_MODE_FOLDER_TWO);
	}
	public static int getCamerGalleryFolderColumnMode() {
		return mPreference.getInt(CAMERA_GALLERY_FOLDER_COLUMN_MODE, Config.COLUMN_MODE_THREE);
	}
	
	public static void setCheckedGalleryBubblePopupCountMovieDiaryReward(int value) {
		setPreferenceValue(BUBBLE_POPUP_LAST_CHECKED_COUNT_MOVIE_DIARY_REWARD, value);
	}
	
	public static void setFBLastNotiId(int value) {
		setPreferenceValue(FB_LAST_NOTIFICATION_ID, value);
	}
	

	public static boolean getCheckedCoachGuide() {
		return mPreference.getBoolean(KEY_CHECKED_COACH_GUIDE, false);
	}
	
	public static boolean getCheckedFirstAutoMovieDiary() {
		return mPreference.getBoolean(KEY_CHECKED_FIRST_AUTO_MOVIEDIARY, false);
	}
	
	public static boolean getCheckedFirstMovieDiary() {
		return mPreference.getBoolean(KEY_CHECKED_FIRST_MOVIEDIARY, false);
	}
	public static boolean getCheckedSmartBulletInOnUpdate() {
		return mPreference.getBoolean(KEY_CHECKED_SMARTBULLETIN_ONUPDATE, false);
	}
	
	// movie diary resolition 
	public static int getSettingMovieDiaryResolution(int resolution) {
		return mPreference.getInt(SETTING_MOVIE_DIARY_RESOLUTION, PublicVariable.MOVIE_DIARY_RESOLUTION_NHD);
	}
	// movie diary auto update default value is true
	public static boolean getSettingMovieDiaryAutoUpdate() {
		return mPreference.getBoolean(SETTING_MOVIE_DIARY_AUTO_UPDATE, true);
	}

	public static boolean isCheckedMovieDiaryGuideDialogVisible() {
		return mPreference.getBoolean(MOVIE_DIARY_GUIDE_DIALOG_VISIBLE, true);
	}
	public static void setCheckedMovieDiaryGuideDialogVisible(boolean value) {
		setPreferenceValue(MOVIE_DIARY_GUIDE_DIALOG_VISIBLE, value);
	}
	
	public static boolean isCheckedGalleryMovieDiary() {
		return mPreference.getBoolean(KEY_CHECKED_GALLERY_MOVIEDIARY, false);
	}
	public static boolean isCheckedTabPersonBubblePopUp() {
		return mPreference.getBoolean(KEY_CHECKED_TAB_PERSON_BUBBLE_POPUP, false);
	}
}
