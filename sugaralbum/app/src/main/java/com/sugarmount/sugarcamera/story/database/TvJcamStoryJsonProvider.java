package com.sugarmount.sugarcamera.story.database;

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
import com.sugarmount.sugaralbum.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TvJcamStoryJsonProvider extends ContentProvider {

	private SQLiteOpenHelper mOpenHelper;

	private final static String AUTHORITIES = "com.sugarmount.sugarcamera.tvjjson";
	private final static String VND_ANDROID_ANALYSIS = "vnd.android/tvjjson";
	private final static String VND_ANDROID_DIR_ANALYSIS = "vnd.android-dir/tvjjson";

	private static final int JSON_ALL = 0;
	private static final int JSON_ID = 1;

	private static final UriMatcher sURLMatcher = new UriMatcher(UriMatcher.NO_MATCH);

	private StoryFileNamer storyFileNamer;

	static {
		sURLMatcher.addURI(AUTHORITIES, "json", JSON_ALL);
		sURLMatcher.addURI(AUTHORITIES, "json/#", JSON_ID);
	}

	@Override
	public boolean onCreate() {
		String str = getContext().getString(R.string.story_file_name_format);
		storyFileNamer = new StoryFileNamer(getContext().getString(R.string.story_file_name_format));
		mOpenHelper = StoryJsonDatabaseHelper.getInstance(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		int match = sURLMatcher.match(uri);
		switch (match) {
			case JSON_ALL:
				qb.setTables(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE);
				break;

			case JSON_ID:
				qb.setTables(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE);
				qb.appendWhere(StoryJsonDatabaseConstants.JsonDatabaseField._ID + "=" + uri.getLastPathSegment());
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
		String table = StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE;
		Uri res = StoryJsonDatabaseConstants.JsonDatabaseField.CONTENT_URI;
		switch (match) {
			case JSON_ALL:
				table = StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE;
				res = StoryJsonDatabaseConstants.JsonDatabaseField.CONTENT_URI;
				break;

			case JSON_ID:
			default:
				return null;
		}

		long rowId;
		ContentValues finalValues;

		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		if (table.equals(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE)) {
			db.beginTransaction();
			try {
				finalValues = new ContentValues(values);
				long timeMills = System.currentTimeMillis();
				finalValues.put(StoryJsonDatabaseConstants.JsonDatabaseField.DATE, timeMills);
				finalValues.put(StoryJsonDatabaseConstants.JsonDatabaseField.DATE_STRING,
						new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date(timeMills)));

				String path = StoryJsonUtils.getStoryThumbDirectoryPath(getContext());

				File folder = new File(path);
				if (!folder.exists()) {
					folder.mkdirs();
				}

				StringBuilder buffer = new StringBuilder();
				buffer.append(path);
				buffer.append("/");
				buffer.append(storyFileNamer.generateName(System.currentTimeMillis()));
				buffer.append(StoryJsonDatabaseConstants.UPLUS_STORY_EXTENSION);

				finalValues.put(StoryJsonDatabaseConstants.JsonDatabaseField._DATA, buffer.toString());
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
		}

		notifyChange(uri);
		return res;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int match = sURLMatcher.match(uri);
		String extraSelection = null;
		String table = StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE;
		switch (match) {
			case JSON_ID:
				table = StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE;
				extraSelection = StoryJsonDatabaseConstants.JsonDatabaseField._ID + "=" + uri.getLastPathSegment();
				break;

			case JSON_ALL:
				table = StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE;
				break;

			default:
				return 0;
		}

		int deletedRows = 0;
		String finalSelection = concatSelections(selection, extraSelection);
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();

		db.beginTransaction();
		try {
			if (table.equals(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE)) {
				deletedRows = deleteJsonTable(db, finalSelection, selectionArgs);
			} else {
				deletedRows = db.delete(table, finalSelection, selectionArgs);
			}
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
		L.e();
		int match = sURLMatcher.match(uri);
		String extraSelection = null;
		String table = StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE;
		L.e("table = "+ table);
		switch (match) {
			case JSON_ID:
				table = StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE;
				extraSelection = StoryJsonDatabaseConstants.JsonDatabaseField._ID + "=" + uri.getLastPathSegment();
				break;

			case JSON_ALL:
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
			case JSON_ALL:
				return VND_ANDROID_DIR_ANALYSIS;
			case JSON_ID:
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
		if (uri.toString().startsWith("content://"+AUTHORITIES+"/json")) {
			getContext().getContentResolver().notifyChange(StoryJsonDatabaseConstants.JsonDatabaseField.CONTENT_URI, null);
		}
	}

	private int deleteJsonTable(SQLiteDatabase db, String selection, String[] selectionArgs) {
		Cursor cursor = db.query(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE,
				new String[] { StoryJsonDatabaseConstants.JsonDatabaseField._DATA }, selection, selectionArgs, null, null, null);
		if (cursor == null) {
			return 0;
		}

		try {
			if (cursor.getCount() == 0) {
				return 0;
			}

			while (cursor.moveToNext()) {
				String path = cursor.getString(cursor.getColumnIndexOrThrow(StoryJsonDatabaseConstants.JsonDatabaseField._DATA));
				if (path != null) {
					new File(path).delete();
				}
			}
		} finally {
			cursor.close();
		}

		int count = db.delete(StoryJsonDatabaseConstants.JsonDatabaseField.JSON_TABLE, selection, selectionArgs);
		return count;
	}

	private static class StoryFileNamer {
		private SimpleDateFormat mFormat;

		// The date (in milliseconds) used to generate the last name.
		private long mLastDate;

		// Number of names generated for the same second.
		private int mSameSecondCount;

		public StoryFileNamer(String format) {
			mFormat = new SimpleDateFormat(format);
		}

		public String generateName(long dateTaken) {
			Date date = new Date(dateTaken);
			String result = mFormat.format(date);

			// If the last name was generated for the same second,
			// we append _1, _2, etc to the name.
			if (dateTaken / 1000 == mLastDate / 1000) {
				mSameSecondCount++;
				result += "_" + mSameSecondCount;
			} else {
				mLastDate = dateTaken;
				mSameSecondCount = 0;
			}

			return result;
		}
	}
}
