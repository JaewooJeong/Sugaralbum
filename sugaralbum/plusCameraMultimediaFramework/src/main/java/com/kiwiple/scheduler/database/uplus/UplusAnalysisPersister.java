package com.kiwiple.scheduler.database.uplus;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * DB 접근 유지 class
 * @author aronia
 *
 */
public class UplusAnalysisPersister {
	private static UplusAnalysisPersister sPersister;

	private Context mContext;
	private ContentResolver mContentResolver;

	private UplusAnalysisPersister(Context context) {
		mContext = context;
		mContentResolver = context.getContentResolver();
	}

	/** Get or create if not exist an instance of UplusAnalysisPersister */
	public static UplusAnalysisPersister getAnalysisPersister(Context context) {
		if ((sPersister == null) || !context.equals(sPersister.mContext)) {
			sPersister = new UplusAnalysisPersister(context);
		}

		return sPersister;
	}

	public void verifyVideoDataInGallery() {
		Cursor cursor = mContentResolver
				.query(UplusAnalysisConstants.VideoAnalysisDatabaseField.CONTENT_URI, new String[] {
						UplusAnalysisConstants.VideoAnalysisDatabaseField._ID, UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ID }, null,
						null, null);

		if (cursor == null) {
			return;
		}

		if (cursor.getCount() == 0) {
			cursor.close();
			return;
		}

		while (cursor.moveToNext()) {
			int id = cursor.getInt(cursor.getColumnIndexOrThrow(UplusAnalysisConstants.VideoAnalysisDatabaseField._ID));
			int videoId = cursor.getInt(cursor.getColumnIndexOrThrow(UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ID));
			if (!isExistVideoDataInGallery(videoId)) {
				deleteVideoAnalysisData(id);
			}
		}

		if (cursor != null) {
			cursor.close();
		}
	}

	public boolean isExistVideoDataInAnalysis(int id) {
		Cursor cursor = mContentResolver.query(UplusAnalysisConstants.VideoAnalysisDatabaseField.CONTENT_URI,
				new String[] { UplusAnalysisConstants.VideoAnalysisDatabaseField._ID }, getExistVideoInAnalysisSelection(id), null, null);

		if (cursor == null) {
			return false;
		}

		if (cursor.getCount() == 0) {
			cursor.close();
			return false;
		}

		if (cursor != null) {
			cursor.close();
		}
		return true;
	}

	public Uri insertVideoData(int id, String path, long startPosition, long endPosition, String orientation) {
		ContentValues values = new ContentValues(5);
		values.put(UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ID, id);
		values.put(UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_PATH, path);
		values.put(UplusAnalysisConstants.VideoAnalysisDatabaseField.START_POSITION, startPosition);
		values.put(UplusAnalysisConstants.VideoAnalysisDatabaseField.END_POSITION, endPosition);
		if (orientation != null) {
			values.put(UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ORIENTATION, orientation);
		} else {
			values.put(UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ORIENTATION, "0");
		}

		return mContentResolver.insert(UplusAnalysisConstants.VideoAnalysisDatabaseField.CONTENT_URI, values);
	}

	public Cursor getVideoDataCursorInAnalysis(int id) {
		Cursor cursor = mContentResolver.query(UplusAnalysisConstants.VideoAnalysisDatabaseField.CONTENT_URI,
				new String[] { UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_PATH,
						UplusAnalysisConstants.VideoAnalysisDatabaseField.START_POSITION,
						UplusAnalysisConstants.VideoAnalysisDatabaseField.END_POSITION,
						UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ORIENTATION }, getExistVideoInAnalysisSelection(id), null, null);

		if (cursor == null) {
			return null;
		}

		if (cursor.getCount() == 0) {
			cursor.close();
			return null;
		}
		return cursor;
	}

	public Cursor getVideoDataCursorInGallery(int id) {
		Cursor cursor = mContentResolver.query(ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id), new String[] {
				MediaStore.Video.VideoColumns.DATA, MediaStore.Video.VideoColumns.DURATION , "datetaken"}, null, null, null);

		if (cursor == null) {
			return null;
		}

		if (cursor.getCount() == 0) {
			cursor.close();
			return null;
		}
		return cursor;
	}

	public Uri insertInfo(int infoType, String mainDate, String locationName) {
		ContentValues values = new ContentValues(1);
		if (infoType == UplusAnalysisConstants.INFO_TYPE_A_DAY_MOVIE_DIARY) {
			values.put(UplusAnalysisConstants.InfoDatabaseField.DATE_STRING, mainDate);
		} else if (infoType == UplusAnalysisConstants.INFO_TYPE_LOCATION) {
			values.put(UplusAnalysisConstants.InfoDatabaseField.LOCATION_NAME, locationName);
		}
		return mContentResolver.insert(UplusAnalysisConstants.InfoDatabaseField.CONTENT_URI, values);
	}

	public boolean isExistInfoData(int infoType, String mainDate, String locationName) {
		Cursor cursor = mContentResolver.query(UplusAnalysisConstants.InfoDatabaseField.CONTENT_URI,
				new String[] { UplusAnalysisConstants.InfoDatabaseField._ID }, getExistInfoDataSelection(infoType, mainDate, locationName), null,
				null);

		if (cursor == null) {
			return false;
		}

		if (cursor.getCount() == 0) {
			cursor.close();
			return false;
		}

		cursor.moveToNext();
		if (cursor != null) {
			cursor.close();
		}
		return true;
	}

	public String getDurationSet(int id) {
		Cursor cursor = mContentResolver.query(UplusAnalysisConstants.BatchSetDatabaseField.CONTENT_URI,
				new String[] { UplusAnalysisConstants.BatchSetDatabaseField.DURATION_SET }, getDurationSetSelection(id), null, null);

		if (cursor == null) {
			return null;
		}

		if (cursor.getCount() == 0) {
			cursor.close();
			return null;
		}

		cursor.moveToNext();
		String durationSet = cursor.getString(cursor.getColumnIndexOrThrow(UplusAnalysisConstants.BatchSetDatabaseField.DURATION_SET));
		cursor.close();
		return durationSet;
	}

	private boolean isExistVideoDataInGallery(int id) {
		Cursor cursor = mContentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, new String[] { MediaStore.Video.VideoColumns._ID },
				getExistVideoDataInGallerySelection(id), null, null);

		if (cursor == null) {
			return false;
		}

		if (cursor.getCount() == 0) {
			cursor.close();
			return false;
		}

		if (cursor != null) {
			cursor.close();
		}

		return true;
	}

	private String getExistVideoDataInGallerySelection(int id) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(" ( ");
		buffer.append(MediaStore.Video.VideoColumns._ID);
		buffer.append(" = ");
		buffer.append(id);
		buffer.append(" ) ");
		return buffer.toString();
	}

	private int deleteVideoAnalysisData(int id) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(" ( ");
		buffer.append(UplusAnalysisConstants.VideoAnalysisDatabaseField._ID);
		buffer.append(" = ");
		buffer.append(id);
		buffer.append(" ) ");
		return mContentResolver.delete(UplusAnalysisConstants.VideoAnalysisDatabaseField.CONTENT_URI, buffer.toString(), null);
	}

	private String getExistVideoInAnalysisSelection(int id) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(" ( ");
		buffer.append(UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_ID);
		buffer.append(" = ");
		buffer.append(id);
		buffer.append(" ) ");
		return buffer.toString();
	}

	private String getExistInfoDataSelection(int infoType, String mainDate, String locationName) {
		StringBuilder buffer = new StringBuilder();
		if (infoType == UplusAnalysisConstants.INFO_TYPE_A_DAY_MOVIE_DIARY) {
			buffer.append(" ( ");
			buffer.append(UplusAnalysisConstants.InfoDatabaseField.DATE_STRING);
			buffer.append(" = ");
			buffer.append("'");
			buffer.append(mainDate);
			buffer.append("'");
			buffer.append(" ) ");
		} else if (infoType == UplusAnalysisConstants.INFO_TYPE_LOCATION) {
			buffer.append(" ( ");
			buffer.append(UplusAnalysisConstants.InfoDatabaseField.LOCATION_NAME);
			buffer.append(" = ");
			buffer.append("'");
			buffer.append(locationName);
			buffer.append("'");
			buffer.append(" ) ");
		}
		return buffer.toString();
	}

	private String getDurationSetSelection(int id) {
		StringBuilder buffer = new StringBuilder();
		buffer.append(" ( ");
		buffer.append(UplusAnalysisConstants.BatchSetDatabaseField._ID);
		buffer.append(" = ");
		buffer.append(id);
		buffer.append(" ) ");
		return buffer.toString();
	}
}
