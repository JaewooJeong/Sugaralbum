package com.sugarmount.sugarcamera.story.utils;

import com.kiwiple.debug.L;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.os.Build;

public class KiwiplePreferenceManager {
	private static final String SHARED_PREFERENCES_ID = "KIWIPLE";
	// mangosteen 140620 : add picture size preference
	public static final String KEY_KIWIPLE_PICTURE_SIZE = "kiwiple_pref_camera_picturesize_key";
	public static final String KEY_KIWIPLE_AUTO_STORY_MAKE_CHECK_DATE = "kiwiple_pref_auto_story_make_check_date_key";
	public static final String KEY_KIWIPLE_AUTO_STORY_DIRECT_MAKE_COUNT = "kiwiple_pref_auto_story_direct_make_count_key";
	public static final String KEY_KIWIPLE_MAX_ATTACHED_VIDEO_COUNT = "kiwiple_pref_max_attached_video_count";

	private static final String KEY_KIWIPLE_ANALYSIS_AUDIO = "kiwiple_pref_analysis_default_audio";
	private static final String KEY_KIWIPLE_APP_VERSION = "kiwiple_pref_app_version";

	private static final String KEY_KIWIPLE_HIDDEN_ANALYSIS_AUDIO = "kiwiple_pref_hidden_analysis_audio";

	private static KiwiplePreferenceManager mInstance;

	private SharedPreferences mPreferences;

	public KiwiplePreferenceManager(Context ctx) {
		mPreferences = ctx.getSharedPreferences(SHARED_PREFERENCES_ID, Context.MODE_MULTI_PROCESS);
	}

	public static KiwiplePreferenceManager getInstance(Context ctx) {
		if (mInstance == null) {
			mInstance = new KiwiplePreferenceManager(ctx);
		}
		return mInstance;
	}

	public boolean isFirstExcution(String tag) {
		if (mPreferences.getBoolean(tag, true)) {
			SharedPreferences.Editor editor = mPreferences.edit();
			editor.putBoolean(tag, false);
			commit(editor);
			return true;
		}
		return false;
	}

	public void setPictureSize(Point[] size) {
		SharedPreferences.Editor editor = mPreferences.edit();
		if (size[0] != null) {
			editor.putInt(KEY_KIWIPLE_PICTURE_SIZE + "_0_width", size[0].x);
			editor.putInt(KEY_KIWIPLE_PICTURE_SIZE + "_0_height", size[0].y);
		}
		if (size[1] != null) {
			editor.putInt(KEY_KIWIPLE_PICTURE_SIZE + "_1_width", size[1].x);
			editor.putInt(KEY_KIWIPLE_PICTURE_SIZE + "_1_height", size[1].y);
		}
		commit(editor);
	}

	public void readPictureSize(Point[] size) {
		int width = mPreferences.getInt(KEY_KIWIPLE_PICTURE_SIZE + "_0_width", 0);
		int height = mPreferences.getInt(KEY_KIWIPLE_PICTURE_SIZE + "_0_height", 0);
		if (width > 0 && height > 0) {
			size[0] = new Point(width, height);
		}
		width = mPreferences.getInt(KEY_KIWIPLE_PICTURE_SIZE + "_1_width", 0);
		height = mPreferences.getInt(KEY_KIWIPLE_PICTURE_SIZE + "_1_height", 0);
		if (width > 0 && height > 0) {
			size[1] = new Point(width, height);
		}
	}

	public void putValue(String key, int value) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putInt(key, value);
		commit(editor);
	}
	
	public void putValue(String key, String value) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString(key, value);
		commit(editor);
	}

	public String getValue(String key, String value) {
		try {
			return mPreferences.getString(key, value);
		} catch (Exception e) {
			return value;
		}
	}

	public int getValue(String key, int value) {
		try {
			return mPreferences.getInt(key, value);
		} catch (Exception e) {
			return value;
		}
	}
	
	public void putBooleanValue(String key, boolean value) {
	    SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(key, value);
        commit(editor);
	}
	
	public boolean getBooleanValue(String key) {
	    try {
	        return mPreferences.getBoolean(key, false);
	    } catch (Exception e) {
	        return false;
	    }
	}

	public void clear() {
		// SharedPreferences.Editor editor = mPreferences.edit();
		// editor.clear();
		// commit(editor);
	}

	private static void commit(SharedPreferences.Editor editor) {
		if (Build.VERSION.SDK_INT >= 9) {
			editor.apply();
		} else {
			editor.commit();
		}
	}

	// >>>> settings
	public void setResolutionValue(String key, String value) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString(key, value);
		commit(editor);
	}

	public String getResolutionValue(String key) {

		return mPreferences.getString(key, "");
	}

	public void setAutoUpdateValue(String key, boolean value) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putBoolean(key, value);
		commit(editor);
	}

	public boolean getAutoUpdateValue(String key) {
		return mPreferences.getBoolean(key, false);
	}

	//무비다이어리 default 음원 분석 여부 check 
	public void setDefaultAudioAnalysisState(boolean value) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putBoolean(KEY_KIWIPLE_ANALYSIS_AUDIO, value);
		commit(editor);
	}
	
	public boolean getDefaultAudioAnalysisState() {
		return mPreferences.getBoolean(KEY_KIWIPLE_ANALYSIS_AUDIO, false);
	}

	// U-camera 버전 
	public void setAppVersion(String version) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putString(KEY_KIWIPLE_APP_VERSION, version);
		commit(editor);
	}
	
	public String getAppVersion() {
		return mPreferences.getString(KEY_KIWIPLE_APP_VERSION, null);
	}

	public void setHiddenAudioAnalysis(boolean value) {
		SharedPreferences.Editor editor = mPreferences.edit();
		editor.putBoolean(KEY_KIWIPLE_HIDDEN_ANALYSIS_AUDIO, value);
		commit(editor);
	}
	
	public boolean getHiddenAudioAnalysis() {
		return mPreferences.getBoolean(KEY_KIWIPLE_HIDDEN_ANALYSIS_AUDIO, false);
	}
	
	
}
