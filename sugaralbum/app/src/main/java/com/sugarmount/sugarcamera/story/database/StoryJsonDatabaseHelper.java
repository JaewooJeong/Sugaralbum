package com.sugarmount.sugarcamera.story.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class StoryJsonDatabaseHelper extends SQLiteOpenHelper {
	
	private static StoryJsonDatabaseHelper sInstance = null;
	private static final String DATABASE_NAME = "uplusjson.db";
	private static final int DATABASE_VERSION = 3;
	
	private StoryJsonDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	static synchronized StoryJsonDatabaseHelper getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new StoryJsonDatabaseHelper(context);
		}
		return sInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createJsonTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
//		drop(db);
//		onCreate(db);
		addColumn(db, oldVersion); 
		onCreate(db); 
	}

	private void createJsonTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE + " ( "
				+ StoryJsonDatabaseConstants.JsonDatabaseField._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING_TITLE + " TEXT, "
				+ StoryJsonDatabaseConstants.JsonDatabaseField.JSON_STRING + " TEXT, "
				+ StoryJsonDatabaseConstants.JsonDatabaseField.DATE + " LONG, "
				+ StoryJsonDatabaseConstants.JsonDatabaseField.DATE_STRING + " TEXT, "
				+ StoryJsonDatabaseConstants.JsonDatabaseField.DURAION + " LONG, "
				+ StoryJsonDatabaseConstants.JsonDatabaseField.BG_MUSIC + " TEXT, "
				+ StoryJsonDatabaseConstants.JsonDatabaseField.IS_INTERNAL + " INTEGER, "
				+ StoryJsonDatabaseConstants.JsonDatabaseField.THEME + " TEXT, "
				+ StoryJsonDatabaseConstants.JsonDatabaseField.THUMB_ORIENTATION + " INTEGER, "
				+ StoryJsonDatabaseConstants.JsonDatabaseField._DATA + " TEXT, "
				+ StoryJsonDatabaseConstants.JsonDatabaseField.SCHEDULER_VERSION +" TEXT, "
				+ StoryJsonDatabaseConstants.JsonDatabaseField.IS_FROM_UBOX +" INTEGER, "
				+ StoryJsonDatabaseConstants.JsonDatabaseField.READ + " INTEGER DEFAULT "
			    + StoryJsonDatabaseConstants.JSON_UNREAD +  " ) ");
	}

	private void addColumn(SQLiteDatabase db, int oldVersion){
		if(oldVersion == 1){
			db.execSQL("ALTER TABLE " + StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE + " ADD COLUMN " + StoryJsonDatabaseConstants.JsonDatabaseField.SCHEDULER_VERSION + " TEXT"); 
			db.execSQL("ALTER TABLE " + StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE + " ADD COLUMN " + StoryJsonDatabaseConstants.JsonDatabaseField.IS_FROM_UBOX + " INTEGER "); 
		}else if(oldVersion ==2){
			db.execSQL("ALTER TABLE " + StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE + " ADD COLUMN " + StoryJsonDatabaseConstants.JsonDatabaseField.IS_FROM_UBOX + " INTEGER "); 
		}
	}
	private void drop(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS "
				+ StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE);
	}
}
