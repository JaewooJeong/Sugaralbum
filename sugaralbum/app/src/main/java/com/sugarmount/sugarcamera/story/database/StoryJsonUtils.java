package com.sugarmount.sugarcamera.story.database;

import java.io.File;

import android.content.Context;

import com.kiwiple.imageframework.util.FileUtils;
import com.kiwiple.debug.L;

public class StoryJsonUtils {
	private static final String STORY_THUMB_FOLDER_NAME = "story/thumb";

	public static String getStoryThumbDirectoryPath(Context context) {
		String path = new StringBuilder().append(context.getFilesDir().toString()).append(File.separator).append(STORY_THUMB_FOLDER_NAME).toString();
		if (!FileUtils.isExist(path)) {
			new File(path).mkdirs();
		}
		L.i("story thumb path = "+ path);
		return path;
	}
}
