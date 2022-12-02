package com.sugarmount.sugarcamera.story.music;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.kiwiple.debug.L;

import java.util.ArrayList;

public class StoryMusicAnalysisHelper extends SQLiteOpenHelper {

	private static StoryMusicAnalysisHelper sInstance = null;
	private final Context mContext;

	private SQLiteStatement mInsertMusicStatement = null;


	public synchronized Cursor selectAllMusicDatas() {
		Cursor cursor = null;
		try {

			cursor = getReadableDatabase().rawQuery(StoryMusicAnalysisConstants.QueryStatement.sql_select_all, null);

			if (cursor == null)
				return null;
			else if (cursor.getCount() <= 0) {
				cursor.close();
				cursor = null;
				return null;
			}

		} catch (Exception e) {
			return null;
		}
		return cursor;
	}

	public synchronized Cursor selectMusicData(int id) {
		Cursor cursor = null;
		try {
			String query = StoryMusicAnalysisConstants.QueryStatement.sql_select_one + id;
			cursor = getReadableDatabase().rawQuery(query, null);

			if (cursor == null)
				return null;
			else if (cursor.getCount() <= 0) {
				cursor.close();
				cursor = null;
				return null;
			}

		} catch (Exception e) {
			return null;
		}
		return cursor;
	}

	
	//asset, iternal mp3를 제외한 media store에 저장되어 분석된 음원(30초 초과)  id list 반환 
	public synchronized ArrayList<Integer> selectAllMusicDataIds() {
		Cursor cursor = null;
		ArrayList<Integer> idsList = null;
		try {
			
			String query = StoryMusicAnalysisConstants.QueryStatement.sql_select_ids + StoryMusicAnalysisConstants.EXTERNAL + "'";
			cursor = getReadableDatabase().rawQuery(query, null);
			if(cursor != null && cursor.getCount() > 0 ){
				idsList = getMusicDataIds(cursor);
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally{
			if(cursor != null && !cursor.isClosed()){
				cursor.close();
				cursor = null;
			}
		}
		return idsList ;
	}

	private synchronized ArrayList<Integer> getMusicDataIds(Cursor cursor) {
		ArrayList<Integer> musicIds = new ArrayList<Integer>();
		cursor.moveToFirst();
		final int  idIndex = cursor.getColumnIndexOrThrow(StoryMusicAnalysisConstants.MusicDatabaseField.MEDIA_STORE_ID);
		while(true){
			int mediaStoraId = cursor.getInt(idIndex);
			musicIds.add(mediaStoraId);
			
			if(!cursor.moveToNext())
				break;
		}
		
		return musicIds;
	}

	public synchronized long deleteAnalysisMusicData(long id) {
		try {
			String query = StoryMusicAnalysisConstants.QueryStatement.sql_delete_id + id;
			L.i("query = " + query);
			getReadableDatabase().execSQL(query);
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
		return id;
	}

	//asset 음원 정보 삭제 
	public synchronized void deleteAnalysisAssetMusicData() {
		try {
			String query = StoryMusicAnalysisConstants.QueryStatement.sql_delete_asset;
			L.i("query = " + query);
			getReadableDatabase().execSQL(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	//다운로드 음원 정보 삭제 
	public synchronized void deleteAnalysisDownloadMusicData() {
		try {
			String query = StoryMusicAnalysisConstants.QueryStatement.sql_delete_download;
			L.i("query = " + query);
			getReadableDatabase().execSQL(query);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//해당 타이들을 가지는 default 음원 존재 유무 
	public synchronized Cursor getExistAudioFileInDBWithPath(String path) {
		Cursor cursor = null;
		try {
			String query = StoryMusicAnalysisConstants.QueryStatement.sql_select_path;
			cursor = getReadableDatabase().rawQuery(query, new String[]{path});

		} catch (Exception e) {
			e.printStackTrace();
		}
		return cursor;
	}

	//해당 경로를 가지는 default 음원 존재 유무 
	public synchronized Cursor getExistAudioFileInDBWithTitle(String title) {
		Cursor cursor = null;
		try {
			String query = StoryMusicAnalysisConstants.QueryStatement.sql_select_title;
			cursor = getReadableDatabase().rawQuery(query, new String[]{title});
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return cursor;
	}
	
	
	public StoryMusicAnalysisHelper(Context context) {
		super(context, StoryMusicAnalysisConstants.DATABASE_NAME, null, StoryMusicAnalysisConstants.DATABASE_VERSION);
		mContext = context;
	}

	
	private void createMusicAnalysisTable(SQLiteDatabase db) {
		L.i("" + StoryMusicAnalysisConstants.MUSIC_ANALYSIS_SCHEMA);
		db.execSQL(StoryMusicAnalysisConstants.MUSIC_ANALYSIS_SCHEMA);
	}

	public static synchronized StoryMusicAnalysisHelper getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new StoryMusicAnalysisHelper(context.getApplicationContext());
		}
		return sInstance;
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqlitedatabase, int oldVersion, int newVersion) {
		L.i("oldVersion = "+ oldVersion +", newVersion  = "+ newVersion);
		try {
			if(oldVersion != newVersion)
				sqlitedatabase.execSQL(StoryMusicAnalysisConstants.ANNIVERSARY_DROP);
		} catch (Exception e) {
			e.printStackTrace();
		}
		onCreate(sqlitedatabase);

	}

	public void close() {
		L.i();
		if (mInsertMusicStatement != null) {
			mInsertMusicStatement.close();
			mInsertMusicStatement = null;
		}

		try {
			SQLiteDatabase db = getWritableDatabase();
			if (!db.isOpen())
				return;

			if (db.inTransaction()) {
				db.endTransaction();
			}
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		L.i();
		createMusicAnalysisTable(db);
	}

	public void printLog(Cursor cursor) {
		boolean isRelease = false;
		if(cursor == null){
			isRelease = true;
			cursor = selectAllMusicDatas();
		}

		if (cursor != null && cursor.getCount() > 0) {
			L.i("cursor.size = " + cursor.getCount());
			cursor.moveToFirst();

			final int idIndex = cursor.getColumnIndexOrThrow(StoryMusicAnalysisConstants.MusicDatabaseField._ID);
			final int typeIndex = cursor.getColumnIndexOrThrow(StoryMusicAnalysisConstants.MusicDatabaseField.MUSIC_TYPE);
			final int titleIndex = cursor.getColumnIndexOrThrow(StoryMusicAnalysisConstants.MusicDatabaseField.MUSIC_TITLE);
			final int dataIndex = cursor.getColumnIndexOrThrow(StoryMusicAnalysisConstants.MusicDatabaseField.MUSIC_DATA);
			final int durationIndex = cursor.getColumnIndexOrThrow(StoryMusicAnalysisConstants.MusicDatabaseField.MUSIC_DURAION);
			final int mediaStoreIdIndex = cursor.getColumnIndexOrThrow(StoryMusicAnalysisConstants.MusicDatabaseField.MEDIA_STORE_ID);
			final int analysisDataIndex = cursor.getColumnIndexOrThrow(StoryMusicAnalysisConstants.MusicDatabaseField.MUSIC_ANALYSIS_DATA);
			final int analysisStateIndex = cursor.getColumnIndexOrThrow(StoryMusicAnalysisConstants.MusicDatabaseField.MUSIC_ANALYSIS_STATE);

			do {
				int id = cursor.getInt(idIndex);
				String type = cursor.getString(typeIndex);
				String title = cursor.getString(titleIndex);
				String data = cursor.getString(dataIndex);
				int duration = cursor.getInt(durationIndex);
				int mediaStoreId = cursor.getInt(mediaStoreIdIndex);
				String analysisStr = cursor.getString(analysisDataIndex);
				int state = cursor.getInt(analysisStateIndex);

				L.i("id = " + id);
				L.i("type = " + type);
				L.i("title = " + title);
				L.i("data = " + data);
				L.i("duration = " + duration );
				L.i("mediaStoreId = " + mediaStoreId);
				L.i("analysisData = " + analysisStr);
				L.i("analysisState = " + state);

				if (!cursor.moveToNext())
					break;

			} while (true);
			//print 한 이후 cursor 위치를 첨으로 이동하자
			if(isRelease){
				cursor.close();
				cursor = null;
			}else{
				cursor.moveToFirst();
			}
		}
	}

}
