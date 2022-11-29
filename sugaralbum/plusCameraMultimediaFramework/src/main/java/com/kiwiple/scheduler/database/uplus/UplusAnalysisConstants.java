package com.kiwiple.scheduler.database.uplus;

import android.net.Uri;

import android.provider.BaseColumns;

public class UplusAnalysisConstants {
	public static final int INFO_TYPE_MAIN_DATE = 0;
	public static final int INFO_TYPE_LOCATION = 1;
	/**
	 * 하루 동안의 사진으로 자동 생성을 한 타입. 
	 */
	public static final int INFO_TYPE_A_DAY_MOVIE_DIARY = 2; 
	

	public static final int BG_MUSIC_IS_EXTERNAL = 0;
	public static final int BG_MUSIC_IS_INTERNAL = 1;

	public static class VideoAnalysisDatabaseField implements BaseColumns {

		public static final Uri CONTENT_URI = Uri.parse("content://com.sugarmount.sugarcamera.uplusanalysis/video");

		public static final String VIDEO_ANALYSIS_TABLE = "uplusVideoAnalysis";

		public static final String VIDEO_ID = "video_id";

		public static final String VIDEO_PATH = "video_path";

		public static final String START_POSITION = "start_position";

		public static final String END_POSITION = "end_position";

		public static final String VIDEO_ORIENTATION = "orientation";
	}

	public static class InfoDatabaseField implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://com.sugarmount.sugarcamera.uplusanalysis/info");

		public static final String INFO_TABLE = "uplusInfo";

		public static final String DATE_STRING = "date_string";

		public static final String LOCATION_NAME = "location_name";
	}

	public static class BatchSetDatabaseField implements BaseColumns {
		public static final Uri CONTENT_URI = Uri.parse("content://com.sugarmount.sugarcamera.uplusanalysis/batchset");

		public static final String BATCH_SET_TABLE = "uplusBatchSet";

		public static final String DURATION_SET = "duration_set";
	}
}
