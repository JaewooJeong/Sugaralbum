package com.sugarmount.sugarcamera.story.story;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import com.sugarmount.sugarcamera.story.database.StoryJsonDatabaseConstants;
import com.sugarmount.sugarcamera.story.utils.DateUtil;

import java.util.ArrayList;

public class StoryLatestDataManager {
	
	private static StoryLatestDataManager sInstance;

	private Context mContext;

	private LoadStoryDataThread mLoadStoryDataThread;

	private Handler mHandler;;

	private ArrayList<StoryData> mStoryDataList;

	private OnStoryDataChangedListener mOnStoryDataChangedListener;

	public static interface OnStoryDataChangedListener {
		public void onStoryDataChanged();
	}

	private StoryLatestDataManager(Context context) {
		mContext = context;
		mHandler = new Handler();
		mStoryDataList = new ArrayList<StoryData>();
	}

	/** Get or create if not exist an instance of StoryDataManager */
	public static StoryLatestDataManager getStoryDataManager(Context context) {
		if (sInstance == null) {
			sInstance = new StoryLatestDataManager(context);
		}

		return sInstance;
	}

	public void setOnStoryDataChangedListener(OnStoryDataChangedListener listener) {
		mOnStoryDataChangedListener = listener;
	}

	public void update() {
		makeStoryData();
	}

	public void clear() {
		if (mStoryDataList != null) {
			mStoryDataList.clear();
			mStoryDataList = null;
		}
	}

	private void makeStoryData() {
		cancelStoryDataThread();
		mLoadStoryDataThread = new LoadStoryDataThread();
		mLoadStoryDataThread.start();
	}

	private void cancelStoryDataThread() {
		if (mLoadStoryDataThread != null) {
			mLoadStoryDataThread.cancel();
			mLoadStoryDataThread = null;
		}
	}

	private class LoadStoryDataThread extends Thread {
		private boolean mIsCancelled = false;

		public void cancel() {
			mIsCancelled = true;
		}

		private Cursor getStoryDataCursor() {
			Cursor cursor = mContext.getContentResolver().query(StoryJsonDatabaseConstants.JsonDatabaseField.CONTENT_URI, getStoryProjections(),
					null, null, getStoryOrderBy());

			if (cursor == null) {
				return null;
			}

			if (cursor.getCount() == 0) {
				cursor.close();
				return null;
			}

			return cursor;

		}

		private String[] getStoryProjections() {
			return new String[] { StoryJsonDatabaseConstants.JsonDatabaseField._ID, StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING_TITLE,
					StoryJsonDatabaseConstants.JsonDatabaseField.DATE_STRING, StoryJsonDatabaseConstants.JsonDatabaseField.DURAION,
					StoryJsonDatabaseConstants.JsonDatabaseField.THUMB_ORIENTATION , StoryJsonDatabaseConstants.JsonDatabaseField.DATE, 
					StoryJsonDatabaseConstants.JsonDatabaseField.SCHEDULER_VERSION, StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING, 
					StoryJsonDatabaseConstants.JsonDatabaseField.THEME};
		}

		private String getStoryOrderBy() {
			StringBuffer buffer = new StringBuffer();
			buffer.append(StoryJsonDatabaseConstants.JsonDatabaseField.DATE);
			buffer.append(" DESC ");
			return buffer.toString();
		}

		@Override
		public void run() {
			super.run();
			Cursor cursor = getStoryDataCursor();
			//String date="" ;
			long date=0 ;
			StoryData storyData = null;

			final ArrayList<StoryData> tStroyDataList = new ArrayList<StoryData>();
			int index = 0; 
			if (cursor != null) {
				while (cursor.moveToNext()) {
					if (mIsCancelled) {
						break;
					}
					
					if(index == 3){
						break; 
					}
					index++; 
			
					//String curDate = cursor.getString(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.DATE_STRING));/
					
					long curDate = cursor.getLong(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.DATE));
					String schedulerVersion = cursor.getString(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.SCHEDULER_VERSION));
					String jsonString = cursor.getString(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING)); 
					String themeName = cursor.getString(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.THEME));
					int id = cursor.getInt(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField._ID)); 
					// 20150217 olive : convert millisecond to days
					long days = DateUtil.getDayMillisecond(curDate); 
					String title = cursor.getString(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING_TITLE));
					int duration = (int) cursor.getLong(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.DURAION));
					int orientation = (int)cursor.getInt(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField.THUMB_ORIENTATION)); 
					if(schedulerVersion == null){
						schedulerVersion = "0_0_0"; 
					}					
					Uri jsonUri = ContentUris.withAppendedId(StoryJsonDatabaseConstants.JsonDatabaseField.CONTENT_URI, id);

					tStroyDataList.add(new StoryData(curDate, jsonUri, id, schedulerVersion, title, duration, orientation, jsonString, themeName)); 
					
				}

				if (cursor != null) {
					cursor.close();
				}
			}

			if (!mIsCancelled) {
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						mStoryDataList = tStroyDataList;
						if (mOnStoryDataChangedListener != null) {
							mOnStoryDataChangedListener.onStoryDataChanged();
						}
					}
				});
			}
		}
	}

	public int getGroupCount() {
		if (mStoryDataList != null && !mStoryDataList.isEmpty()) {
			return mStoryDataList.size();
		} else {
			return 0;
		}
	}

	public ArrayList<StoryData> getStoryDataList(){
		if (mStoryDataList != null && !mStoryDataList.isEmpty()) {
			return mStoryDataList; 
		}else{
			return null; 
		}
	}
	public StoryData getStoryData(int position) {
		if (mStoryDataList != null && !mStoryDataList.isEmpty()) {
			return mStoryDataList.get(position);
		} else {
			return null;
		}
	}
	public boolean checkLatestStoryData(Uri jsonUri){
		if (mStoryDataList != null && !mStoryDataList.isEmpty()) {
			for(StoryData data : mStoryDataList){
				if(jsonUri.equals(data.getStoryUri())){
					return true; 
				}
			}
		}
		return false; 
	}
	
	public boolean checkLatestStoryData(int id){
		if (mStoryDataList != null && !mStoryDataList.isEmpty()) {
			for(StoryData data : mStoryDataList){
				if(id == data.getStoryId()){
					return true; 
				}
			}
		}
		return false; 
	}
}
