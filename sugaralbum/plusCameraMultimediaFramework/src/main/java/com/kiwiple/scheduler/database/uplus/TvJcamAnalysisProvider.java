package com.kiwiple.scheduler.database.uplus;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;

import com.kiwiple.debug.L;

import java.io.FileNotFoundException;

public class TvJcamAnalysisProvider extends ContentProvider {

	private SQLiteOpenHelper mOpenHelper;

	private final static String AUTHORITIES = "com.sugarmount.sugarcamera.tvjanalysis";
	private final static String VND_ANDROID_ANALYSIS = "vnd.android/tvjanalysis";
	private final static String VND_ANDROID_DIR_ANALYSIS = "vnd.android-dir/tvjanalysis";

	private static final int VIDEO_ALL = 0;
	private static final int VIDEO_ID = 1;
	private static final int INFO_ALL = 2;
	private static final int INFO_ID = 3;
	private static final int BATCHSET_ALL = 4;
	private static final int BATCHSET_ID = 5;

	private static final UriMatcher sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	static {
		sURLMatcher.addURI(AUTHORITIES, "video", VIDEO_ALL);
		sURLMatcher.addURI(AUTHORITIES, "video/#", VIDEO_ID);
		sURLMatcher.addURI(AUTHORITIES, "info", INFO_ALL);
		sURLMatcher.addURI(AUTHORITIES, "info/#", INFO_ID);
		sURLMatcher.addURI(AUTHORITIES, "batchset", BATCHSET_ALL);
		sURLMatcher.addURI(AUTHORITIES, "batchset/#", BATCHSET_ID);
	}

	@Override
	public boolean onCreate() {
		mOpenHelper = UplusAnalysisDatabaseHelper.getInstance(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		int match = sURLMatcher.match(uri);
		switch (match) {
		case VIDEO_ALL:
			qb.setTables(UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ANALYSIS_TABLE);
			break;

		case VIDEO_ID:
			qb.setTables(UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ANALYSIS_TABLE);
			qb.appendWhere(UplusAnalysisConstants.VideoAnalysisDatabaseField._ID + "=" + uri.getLastPathSegment());
			break;

		case INFO_ALL:
			qb.setTables(UplusAnalysisConstants.InfoDatabaseField.INFO_TABLE);
			break;

		case INFO_ID:
			qb.setTables(UplusAnalysisConstants.InfoDatabaseField.INFO_TABLE);
			qb.appendWhere(UplusAnalysisConstants.InfoDatabaseField._ID + "=" + uri.getLastPathSegment());
			break;

		case BATCHSET_ALL:
			qb.setTables(UplusAnalysisConstants.BatchSetDatabaseField.BATCH_SET_TABLE);
			break;

		case BATCHSET_ID:
			qb.setTables(UplusAnalysisConstants.BatchSetDatabaseField.BATCH_SET_TABLE);
			qb.appendWhere(UplusAnalysisConstants.BatchSetDatabaseField._ID + "=" + uri.getLastPathSegment());
			break;

		default:
			return null;
		}

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();
		Cursor ret;
		ret = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		ret.setNotificationUri(getContext().getContentResolver(), uri);
		return ret;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int match = sURLMatcher.match(uri);
		String table = UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ANALYSIS_TABLE;
		Uri res = UplusAnalysisConstants.VideoAnalysisDatabaseField.CONTENT_URI;
		switch (match) {
		case VIDEO_ALL:
			table = UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ANALYSIS_TABLE;
			res = UplusAnalysisConstants.VideoAnalysisDatabaseField.CONTENT_URI;
			break;

		case INFO_ALL:
			table = UplusAnalysisConstants.InfoDatabaseField.INFO_TABLE;
			res = UplusAnalysisConstants.InfoDatabaseField.CONTENT_URI;
			break;

		case BATCHSET_ALL:
			table = UplusAnalysisConstants.BatchSetDatabaseField.BATCH_SET_TABLE;
			res = UplusAnalysisConstants.BatchSetDatabaseField.CONTENT_URI;
			break;

		case VIDEO_ID:
		case INFO_ID:
		case BATCHSET_ID:
		default:
			return null;
		}

		long rowId;
		ContentValues finalValues;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		db.beginTransaction();
		try {
			finalValues = new ContentValues(values);
			if ((rowId = db.insert(table, null, finalValues)) <= 0) {
				return null;
			}
			res = Uri.parse(res + "/" + rowId);
			db.setTransactionSuccessful();
		} catch (Throwable ex) {
			L.e(ex.getMessage());
		} finally {
			db.endTransaction();
		}

		notifyChange(uri);
		return res;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int match = sURLMatcher.match(uri);
		String extraSelection = null;
		String table = UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ANALYSIS_TABLE;
		switch (match) {
		case VIDEO_ID:
			table = UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ANALYSIS_TABLE;
			extraSelection = UplusAnalysisConstants.VideoAnalysisDatabaseField._ID + "=" + uri.getLastPathSegment();
			break;

		case INFO_ID:
			table = UplusAnalysisConstants.InfoDatabaseField.INFO_TABLE;
			extraSelection = UplusAnalysisConstants.InfoDatabaseField._ID + "=" + uri.getLastPathSegment();
			break;

		case BATCHSET_ID:
			table = UplusAnalysisConstants.BatchSetDatabaseField.BATCH_SET_TABLE;
			extraSelection = UplusAnalysisConstants.BatchSetDatabaseField._ID + "=" + uri.getLastPathSegment();
			break;

		case VIDEO_ALL:
			table = UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ANALYSIS_TABLE;
			break;

		case INFO_ALL:
			table = UplusAnalysisConstants.InfoDatabaseField.INFO_TABLE;
			break;

		case BATCHSET_ALL:
			table = UplusAnalysisConstants.BatchSetDatabaseField.BATCH_SET_TABLE;
			break;

		default:
			return 0;
		}

		int deletedRows = 0;
		String finalSelection = concatSelections(selection, extraSelection);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		db.beginTransaction();
		try {
			deletedRows = db.delete(table, finalSelection, selectionArgs);
			db.setTransactionSuccessful();
		} catch (Throwable ex) {
			L.e(ex.getMessage());
		} finally {
			db.endTransaction();
		}

		if (deletedRows > 0) {
			notifyChange(uri);
		}
		return deletedRows;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int match = sURLMatcher.match(uri);
		String extraSelection = null;
		String table = UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ANALYSIS_TABLE;
		switch (match) {
		case VIDEO_ID:
			table = UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ANALYSIS_TABLE;
			extraSelection = UplusAnalysisConstants.VideoAnalysisDatabaseField._ID + "=" + uri.getLastPathSegment();
			break;

		case INFO_ID:
			table = UplusAnalysisConstants.InfoDatabaseField.INFO_TABLE;
			extraSelection = UplusAnalysisConstants.InfoDatabaseField._ID + "=" + uri.getLastPathSegment();
			break;

		case BATCHSET_ID:
			table = UplusAnalysisConstants.BatchSetDatabaseField.BATCH_SET_TABLE;
			extraSelection = UplusAnalysisConstants.BatchSetDatabaseField._ID + "=" + uri.getLastPathSegment();
			break;

		case VIDEO_ALL:
		case INFO_ALL:
		case BATCHSET_ALL:
		default:
			return 0;
		}

		ContentValues finalValues;
		finalValues = new ContentValues(values);

		int updatedRows = 0;
		String finalSelection = concatSelections(selection, extraSelection);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		db.beginTransaction();
		try {
			updatedRows = db.update(table, finalValues, finalSelection, selectionArgs);
			db.setTransactionSuccessful();
		} catch (Throwable ex) {
			L.e(ex.getMessage());
		} finally {
			db.endTransaction();
		}

		if (updatedRows > 0) {
			notifyChange(uri);
		}
		return updatedRows;
	}

	@Override
	public String getType(Uri uri) {
		int match = sURLMatcher.match(uri);
		switch (match) {
		case VIDEO_ALL:
		case INFO_ALL:
		case BATCHSET_ALL:
			return VND_ANDROID_DIR_ANALYSIS;
		case VIDEO_ID:
		case INFO_ID:
		case BATCHSET_ID:
			return VND_ANDROID_ANALYSIS;
		default:
			return "*/*";
		}
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		return openFileHelper(uri, mode);
	}

	private static String concatSelections(String selection1, String selection2) {
		if (TextUtils.isEmpty(selection1)) {
			return selection2;
		} else if (TextUtils.isEmpty(selection2)) {
			return selection1;
		} else {
			return selection1 + " AND " + selection2;
		}
	}

	private void notifyChange(Uri uri) {
		if (uri.toString().startsWith("content://"+AUTHORITIES+"/video")) {
			getContext().getContentResolver().notifyChange(UplusAnalysisConstants.VideoAnalysisDatabaseField.CONTENT_URI, null);
		} else if (uri.toString().startsWith("content://"+AUTHORITIES+"/info")) {
			getContext().getContentResolver().notifyChange(UplusAnalysisConstants.InfoDatabaseField.CONTENT_URI, null);
		} else if (uri.toString().startsWith("content://"+AUTHORITIES+"/batchset")) {
			getContext().getContentResolver().notifyChange(UplusAnalysisConstants.BatchSetDatabaseField.CONTENT_URI, null);
		}
	}
}
