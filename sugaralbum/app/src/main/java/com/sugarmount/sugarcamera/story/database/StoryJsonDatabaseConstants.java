package com.sugarmount.sugarcamera.story.database;

import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.content.Context;
import java.io.File;

public class StoryJsonDatabaseConstants {
	@Deprecated
	public static final String UPLUS_STORY_PATH = Environment.getExternalStorageDirectory() + "/story/thumb";
	
	/**
	 * Get app-specific story path for Android 15 compatibility
	 * @param context Application context
	 * @return App-specific story directory path
	 */
	public static String getAppStoryPath(Context context) {
		File externalDir = context.getExternalFilesDir(null);
		if (externalDir == null) {
			externalDir = context.getFilesDir();
		}
		
		File storyDir = new File(externalDir, "story/thumb");
		if (!storyDir.exists()) {
			storyDir.mkdirs();
		}
		
		return storyDir.getAbsolutePath();
	}

	public static final int BG_MUSIC_IS_EXTERNAL = 0;
	public static final int BG_MUSIC_IS_INTERNAL = 1;

	public static final int JSON_UNREAD = 0;
	public static final int JSON_READ = 1;

	public static final String UPLUS_STORY_EXTENSION = ".story";

	public static class JsonDatabaseField implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://com.sugarmount.sugarcamera.tvjjson/json");

		public static final String JSON_TABLE = "json";

		public static final String JSON_STRING_TITLE = "json_string_title";

		public static final String JSON_STRING = "json_string";

		public static final String DATE = "date";

		public static final String DATE_STRING = "date_string";

		public static final String _DATA = "_data";

		public static final String DURAION = "duration";

		public static final String BG_MUSIC = "bg_music";

		public static final String IS_INTERNAL = "is_internal";

		public static final String THEME = "theme";

		public static final String THUMB_ORIENTATION = "orienataion";

		public static final String SCHEDULER_VERSION = "scheduler_version";

		public static final String READ = "read";

		public static final String IS_FROM_UBOX = "is_from_ubox";

	}
}
