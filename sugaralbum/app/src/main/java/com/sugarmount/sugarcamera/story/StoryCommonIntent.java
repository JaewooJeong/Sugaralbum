package com.sugarmount.sugarcamera.story;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.kiwiple.debug.L;
import com.sugarmount.sugarcamera.PublicVariable;
import com.sugarmount.sugarcamera.story.service.UplusStorySavingService;

public class StoryCommonIntent {

	private Context mContext;

	public StoryCommonIntent(Context context) {
		mContext = context;
	}

	private static StoryCommonIntent sInstance;

	public static synchronized StoryCommonIntent getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new StoryCommonIntent(context.getApplicationContext());
		}
		return sInstance;
	}

	public void sendDeleteThumbnailAction(Uri mJsonDataUri) {
		int id = getIdFromUri(mJsonDataUri);
		Intent intent = new Intent(PublicVariable.INTENT_ACTION_DELETE_THUMBNAIL);
		intent.putExtra(PublicVariable.INTENT_EXTRA_THUMBNAIL_ID, id);
		mContext.sendBroadcast(intent);
		L.i("save thumbnail .. done");
	}

	public int getIdFromUri(Uri uri) {
		int id = -1;
		try {
			Cursor cursor = mContext.getContentResolver().query(uri, null, null, null, null);
			cursor.moveToNext();
			id = cursor.getInt(cursor.getColumnIndex("_id"));
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return id;
	}

	//무비다이어리 저장 시작 
	public void startSavingMDService(){
		Intent intent = new Intent(mContext, UplusStorySavingService.class);
    	intent.setAction(UplusStorySavingService.ACTION_START_SAVING_MD);
    	mContext.startService(intent);
	}
	
	//무비다이어리 저장 종료 
	public void finishSavingMDService(String path, Uri uri, int orientation){
		Intent intent = new Intent(mContext, UplusStorySavingService.class);
		intent.setAction(UplusStorySavingService.ACTION_FINISH_SAVING_MD);
		intent.putExtra("path", path);
		intent.setData(uri);
		intent.putExtra("orientation", orientation);
		
		mContext.startService(intent);
	}

	//무비다이어리 저장중 비정상적으로 app killed 되었을 경우 남아있는 노티 제거 
	public void cancelSavingMDService(){
		Intent intent = new Intent(mContext, UplusStorySavingService.class);
		intent.setAction(UplusStorySavingService.ACTION_CANCEL_SAVING_MD);
		mContext.startService(intent);
	}
	
}
