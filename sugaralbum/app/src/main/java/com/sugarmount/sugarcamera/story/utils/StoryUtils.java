package com.sugarmount.sugarcamera.story.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.kiwiple.debug.L;
import com.kiwiple.multimedia.canvas.DummyScene;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.ImageTextScene;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusIntroSceneCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusOutroSceneCoordinator;
import com.kiwiple.scheduler.tag.UserTag;
import com.kiwiple.scheduler.theme.Theme;
import com.sugarmount.sugarcamera.story.database.StoryJsonDatabaseConstants;
import com.sugarmount.sugarcamera.story.database.StoryJsonPersister;

import java.io.File;
import java.io.FilenameFilter;

public class StoryUtils {

	// Ubox를 통해 무비다이어리 생성 중 비정상 종료로 인해 DB 미 삭제로 인한 예외처리
	// 1. Gallery진입 시 체크 > 첫 화면에서 무비다이어리 진입 경로
	// 2. Story preview 리스트 진입 시 체크 > 첫 화먼에서 갤러리 진입 경로
	public static void checkAbnormalStoryFromUbox(Context context) {

		String[] projection = new String[] { StoryJsonDatabaseConstants.JsonDatabaseField._ID, StoryJsonDatabaseConstants.JsonDatabaseField.IS_FROM_UBOX };

		String selection = StoryJsonDatabaseConstants.JsonDatabaseField.IS_FROM_UBOX + "='" + 1 + "'";

		try {
			Cursor cursor = context.getContentResolver().query(StoryJsonDatabaseConstants.JsonDatabaseField.CONTENT_URI, projection, selection, null, null);
	
			if (cursor != null) {
				L.i("abnormal story from ubox count : " + cursor.getCount());
				while (cursor.moveToNext()) {
	
					int id = cursor.getInt(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField._ID));
					int isFromUbox = cursor.getInt(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.IS_FROM_UBOX));
	
					if (isFromUbox == 1) {
						Uri deleteUri = Uri.parse(StoryJsonDatabaseConstants.JsonDatabaseField.CONTENT_URI + "/" + id);
						StoryJsonPersister.getStoryJsonPersister(context).deleteJsonData(deleteUri);
					}
				}
	
				cursor.close();
				cursor = null;
			} else {
				L.i("abnormal story nothing....");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 0908 무비다이어리 저장 중 비정상 종료시 tmp 파일 제거
	public static void checkMovieDiaryTmpFile(Context context) {

		long startTime = System.currentTimeMillis();
		String tmpPath = context.getCacheDir().getAbsolutePath();

		if (TextUtils.isEmpty(tmpPath)) {
			return;
		}

		try {
			File file = new File(tmpPath);
			if (file.exists() && file.isDirectory()) {
				File[] files = file.listFiles(new FilenameFilter() {
	
					@Override
					public boolean accept(File dir, String filename) {
						return filename.endsWith(".tmp");
					}
				});
	
				if (files == null || files.length < 1) {
					return;
				}
				for (File tmpFile : files) {
					boolean result = tmpFile.delete();
					tmpFile = null;
					L.i("delete movie diary tmp file : " + result);
				}
			}
			file = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		L.e("checking time : " + (System.currentTimeMillis() - startTime) + "ms");
	}

	// Ubox > 무비다이어리 생성 후 다운로드 받은 파일 삭제
	public static void deleteImageFilesFromUBox(final Context context) {
		final String TEMP_DIR = "temp";

		String packagePath = context.getFilesDir().getAbsolutePath();
		String tempPath = new StringBuffer().append(packagePath).append(File.separator).append(TEMP_DIR).toString();

		if (TextUtils.isEmpty(tempPath)) {
			return;
		}
		
		try {
			File file = new File(tempPath);
			if (file.exists() && file.isDirectory()) {
				File[] files = file.listFiles();
				if (files == null || files.length < 1) {
					return;
				}
	
				for (File f : files) {
					boolean result = f.delete();
					L.i("delete file from ubox : "+ result);
				}
			}
			file = null;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isValidContentImageFileScene(Scene scene) {
		if(scene instanceof ImageFileScene){
			if(UserTag.isExtraScene(scene)){
				return false; 
			}else{
				return true; 
			}
		}else{
			return false; 
		}
	}
	
	
	public static boolean isValidSceneIntroOutroType(Scene scene){
		if(UserTag.getTagSceneType(scene).equals(UplusIntroSceneCoordinator.TAG_JSON_VALE_SCENE_INTRO)
				|| UserTag.getTagSceneType(scene).equals(UplusOutroSceneCoordinator.TAG_JSON_VALE_SCENE_OUTRO)
				|| scene instanceof ImageTextScene 
				|| scene instanceof DummyScene){
			return true;
		}else{
			return false;
		}
	}

	public static boolean isValidSceneIntroType(Scene scene){
		if(UserTag.getTagSceneType(scene).equals(UplusIntroSceneCoordinator.TAG_JSON_VALE_SCENE_INTRO)
				|| scene instanceof ImageTextScene
				|| scene instanceof DummyScene){
			return true;
		}else{
			return false;
		}
	}

	
	public static boolean isValidDynamicIntroType(Scene scene, Theme theme){
		if(UserTag.getTagSceneType(scene).equals(UplusIntroSceneCoordinator.TAG_JSON_VALE_SCENE_INTRO) && theme.getDynamicIntroJson() != null){
			return true;
		}else{
			return false;
		}
	}

}
