package com.kiwiple.scheduler.database.uplus;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;

import com.kiwiple.scheduler.R;

public class UplusAnalysisDatabaseHelper extends SQLiteOpenHelper {

	private static UplusAnalysisDatabaseHelper sInstance = null;
	private static final String DATABASE_NAME = "uplusanalysis.db";
	private static final int DATABASE_VERSION = 1;

	private Context mContext;
	private String mSql = " INSERT INTO uplusBatchSet (_id, duration_set) " + " VALUES(?,?) ";

	private UplusAnalysisDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}

	static synchronized UplusAnalysisDatabaseHelper getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new UplusAnalysisDatabaseHelper(context);
		}
		return sInstance;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		createAnalysisTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
		drop(db);
		onCreate(db);
	}

	private void createAnalysisTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE IF NOT EXISTS " + UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ANALYSIS_TABLE + " ( "
				+ UplusAnalysisConstants.VideoAnalysisDatabaseField._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ID + " INTEGER, "
				+ UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_PATH + " TEXT, "
				+ UplusAnalysisConstants.VideoAnalysisDatabaseField.START_POSITION + " LONG, "
				+ UplusAnalysisConstants.VideoAnalysisDatabaseField.END_POSITION + " LONG,"
				+ UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ORIENTATION + " TEXT " + " ) ");

		db.execSQL("CREATE TABLE IF NOT EXISTS " + UplusAnalysisConstants.InfoDatabaseField.INFO_TABLE + " ( "
				+ UplusAnalysisConstants.InfoDatabaseField._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ UplusAnalysisConstants.InfoDatabaseField.LOCATION_NAME + " TEXT, " + UplusAnalysisConstants.InfoDatabaseField.DATE_STRING
				+ " TEXT " + " ) ");

		db.execSQL("CREATE TABLE IF NOT EXISTS " + UplusAnalysisConstants.BatchSetDatabaseField.BATCH_SET_TABLE + " ( "
				+ UplusAnalysisConstants.BatchSetDatabaseField._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ UplusAnalysisConstants.BatchSetDatabaseField.DURATION_SET + " TEXT " + " ) ");

		insertDefaultBatchSet(db, mContext.getString(R.string.duration_set1));
	}

	private void drop(SQLiteDatabase db) {
		db.execSQL("DROP TABLE IF EXISTS " + UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ANALYSIS_TABLE);

		db.execSQL("DROP TABLE IF EXISTS " + UplusAnalysisConstants.InfoDatabaseField.INFO_TABLE);

		db.execSQL("DROP TABLE IF EXISTS " + UplusAnalysisConstants.BatchSetDatabaseField.BATCH_SET_TABLE);
	}

	private void insertDefaultBatchSet(SQLiteDatabase db, String durationSet) {
		SQLiteStatement insertStatnment = db.compileStatement(mSql);
		insertStatnment.clearBindings();
		insertStatnment.bindString(1, Integer.toString(1));
		insertStatnment.bindString(2, durationSet);
		insertStatnment.executeInsert();
	}
}
