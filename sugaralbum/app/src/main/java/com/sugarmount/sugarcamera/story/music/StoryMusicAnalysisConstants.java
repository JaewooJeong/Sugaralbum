package com.sugarmount.sugarcamera.story.music;

import android.provider.BaseColumns;
import android.provider.MediaStore;

public class StoryMusicAnalysisConstants {

	public static final int MUSIC_ANALYSIS_DONE = 1;
	public static final int MUSIC_ANALYSIS_NOT_YET = 0;

	public final static String DATABASE_NAME = "musicanalysis.db";
	public static final int DATABASE_VERSION = 1;

	public static class MusicDatabaseField implements BaseColumns {


		public static final String MUSIC_TABLE = "music";

		public static final String MUSIC_TYPE = "music_type";   // asset of external 

		public static final String MUSIC_TITLE = "music_title";

		public static final String MUSIC_DATA = "music_data";

		public static final String MUSIC_DURAION = "music_duration";

		public static final String MEDIA_STORE_ID = "mediastore_id";

		public static final String MUSIC_ANALYSIS_DATA = "analysis_data";

		public static final String MUSIC_ANALYSIS_STATE = "analysis_state";
	}
	

	public final static String [] MUSIC_PROJECTION =  {
		MediaStore.Audio.Media._ID,
        MediaStore.Audio.Media.TITLE,
        MediaStore.Audio.Media.DATA,
        MediaStore.Audio.Media.DURATION,
        MediaStore.Audio.Media.MIME_TYPE,
        };
	
	public static final String ORDER_BY = " order by ";
	public static final String MUSIC_MIME_TYPE = "audio/mpeg";
	public static final String TRANSITIONS = "transitions";
	public static final String DURATIONS = "durations";
	public static final String DURATION = "duration";
	public static final String EXTERNAL = "external";
	
	public static final String ASSETS = "assets";
	public static final String DOWNLOAD = "download";
	
	public static final long ANALYSIS_MUSIC_LIMIT_DURATION = 1000 * 30;	//30초 
	private static final long MUSIC_FRAGMENT_LIMIT_DURATION = 1000 * 10;	// 10초

	//배경음악 화면에서 
	public final static String MUSIC_FRAGMENT_WHERE_CLAUSE =  MediaStore.MediaColumns.MIME_TYPE +"='"+MUSIC_MIME_TYPE+"'"+" AND " 
			+ MediaStore.Audio.Media.DURATION+" >= " + MUSIC_FRAGMENT_LIMIT_DURATION;
	
	//음원 분석 할때. 
	public final static String ANALYSIS_MUSIC_WHERE_CLAUSE =  MediaStore.MediaColumns.MIME_TYPE +"='"+MUSIC_MIME_TYPE+"'" +" AND " 
			+ MediaStore.Audio.Media.DURATION+" > " + ANALYSIS_MUSIC_LIMIT_DURATION;
	
	public final static String MUSIC_WHERE_PATH_CLAUSE =  MediaStore.MediaColumns.DATA +" = ? ";
	
	
	public static final String ANNIVERSARY_DROP = "DROP TABLE If EXISTS " + StoryMusicAnalysisConstants.MusicDatabaseField.MUSIC_TABLE;
	
	
	public final static String MUSIC_ANALYSIS_SCHEMA = "CREATE TABLE IF NOT EXISTS " + 
			MusicDatabaseField.MUSIC_TABLE + " ( " + 
			MusicDatabaseField._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
			MusicDatabaseField.MUSIC_TYPE + " TEXT, " +    // asset or external 
			MusicDatabaseField.MUSIC_TITLE + " TEXT, " + 
			MusicDatabaseField.MUSIC_DATA + " TEXT, " + 
			MusicDatabaseField.MUSIC_DURAION + " INTEGER, " + 
			MusicDatabaseField.MEDIA_STORE_ID + " INTEGER, " + 
			MusicDatabaseField.MUSIC_ANALYSIS_DATA + " TEXT, " + 
			MusicDatabaseField.MUSIC_ANALYSIS_STATE + " INTEGER DEFAULT " + StoryMusicAnalysisConstants.MUSIC_ANALYSIS_NOT_YET + " ) ";

	
	public static final class InsertMusicData{
		static final String sqlString = " INSERT INTO " + MusicDatabaseField.MUSIC_TABLE 
				+ " ( "
				+ MusicDatabaseField.MUSIC_TYPE +", "
				+ MusicDatabaseField.MUSIC_TITLE +", "
				+ MusicDatabaseField.MUSIC_DATA +", "
				+ MusicDatabaseField.MUSIC_DURAION +", "
				+ MusicDatabaseField.MEDIA_STORE_ID +", "
				+ MusicDatabaseField.MUSIC_ANALYSIS_DATA +", "   // JsonArray로 만들어서 저장하자 
				+ MusicDatabaseField.MUSIC_ANALYSIS_STATE
				+  " ) VALUES "
				+ "(?, ?, ?, ?, ?, ?, ?)";
		
		static final int MUSIC_TYPE = 1;
		static final int MUSIC_TITLE = 2;
		static final int MUSIC_DATA = 3;
		static final int MUSIC_DURATION = 4;
		static final int MEDIA_STORE_ID = 5;
		static final int MUSIC_ANALYSIS_DATA = 6;
		static final int MUSIC_ANALYSIS_STATE = 7;
	}
	

	public static final class QueryStatement{
		// + ORDER_BY + MusicDatabaseField.MUSIC_TITLE;
		static final String sql_select_all = " SELECT * FROM " + MusicDatabaseField.MUSIC_TABLE; 
		
		static final String sql_select_all_external = " SELECT * FROM " + MusicDatabaseField.MUSIC_TABLE + " WHERE " +MusicDatabaseField.MUSIC_TYPE +" ='";

		static final String sql_select_one = " SELECT * FROM "+ MusicDatabaseField.MUSIC_TABLE + " WHERE " +MusicDatabaseField.MEDIA_STORE_ID+" = ";
		
		static final String sql_select_ids = " SELECT "+ MusicDatabaseField.MEDIA_STORE_ID  + " FROM "+ MusicDatabaseField.MUSIC_TABLE + " WHERE " +MusicDatabaseField.MUSIC_TYPE +" ='";

		static final String sql_delete_id = " DELETE FROM " + MusicDatabaseField.MUSIC_TABLE + " WHERE "+ MusicDatabaseField.MEDIA_STORE_ID +" = ";

		static final String sql_delete_asset = " DELETE FROM " + MusicDatabaseField.MUSIC_TABLE + " WHERE "+ MusicDatabaseField.MUSIC_TYPE +" = '" + StoryMusicAnalysisConstants.ASSETS+"'";

		static final String sql_delete_download = " DELETE FROM " + MusicDatabaseField.MUSIC_TABLE + " WHERE "+ MusicDatabaseField.MUSIC_TYPE +" = '" + StoryMusicAnalysisConstants.DOWNLOAD+"'";

		static final String sql_select_path = " SELECT * FROM "+ MusicDatabaseField.MUSIC_TABLE + " WHERE " +MusicDatabaseField.MUSIC_DATA +" = ? ";

		static final String sql_select_title= " SELECT * FROM "+ MusicDatabaseField.MUSIC_TABLE + " WHERE " +MusicDatabaseField.MUSIC_TITLE +" = ?";
	}

}
