package com.sugarmount.sugarcamera.utils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.media.MediaMetadataRetriever;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Display;
import android.view.WindowManager;

public class Utils {

	private static final String DEFAULT_CACHE_DIRECTORY = "/pluscamera/.cache";
	private static final String CACHE_EXTENSION = ".cache";
	
	@Deprecated
	public static String getExternalStorageDirectory() {
		if (isExternalStorageMounted()) {
			return Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		return null;
	}
	
	/**
	 * Get app-specific external storage directory for Android 15 compatibility
	 * @param context Application context
	 * @return App-specific external directory path
	 */
	public static String getAppSpecificDirectory(Context context) {
		File externalDir = context.getExternalFilesDir(null);
		if (externalDir != null) {
			return externalDir.getAbsolutePath();
		}
		return context.getFilesDir().getAbsolutePath();
	}
	
	public static String getExternalStoragePublicDirectoryDCIM() {
		if (isExternalStorageMounted()) {
			return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath();
		}
		return null;
	}
	
	@Deprecated
	public static boolean isExternalStorageMounted() {
		return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()); 
	}
	
	/**
	 * Check if app-specific external storage is available
	 * @param context Application context
	 * @return true if available
	 */
	public static boolean isAppStorageAvailable(Context context) {
		File externalDir = context.getExternalFilesDir(null);
		return externalDir != null && externalDir.canWrite();
	}
	
	@Deprecated
	public static long getAvailableSDcardSize() {
        if(isExternalStorageMounted()) {
        	File path = Environment.getExternalStorageDirectory();
        	StatFs stat = new StatFs(path.getPath());
        	long blockSize = stat.getBlockSize();
        	long availableBlocks = stat.getAvailableBlocks();
        	return availableBlocks * blockSize;
        }
        return 0;
	}
	
	/**
	 * Get available storage size in app-specific directory
	 * @param context Application context
	 * @return Available bytes
	 */
	public static long getAvailableAppStorageSize(Context context) {
		File externalDir = context.getExternalFilesDir(null);
		if (externalDir == null) {
			externalDir = context.getFilesDir();
		}
		
		try {
			StatFs stat = new StatFs(externalDir.getPath());
			long blockSize = stat.getBlockSize();
			long availableBlocks = stat.getAvailableBlocks();
			return availableBlocks * blockSize;
		} catch (Exception e) {
			return 0;
		}
	}
	
	public static String getCacheDirectory() {
		return getExternalStorageDirectory(DEFAULT_CACHE_DIRECTORY);
	}
	
	@Deprecated
	public static String getExternalStorageDirectory(String subDirectory) {
		String dir = Environment.getExternalStorageDirectory().getAbsolutePath() + subDirectory + "/";
		try {
			return getDirectory(dir);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Get app-specific directory with subdirectory for Android 15 compatibility
	 * @param context Application context
	 * @param subDirectory Subdirectory name
	 * @return App-specific directory path
	 */
	public static String getAppSpecificDirectory(Context context, String subDirectory) {
		File externalDir = context.getExternalFilesDir(null);
		if (externalDir == null) {
			externalDir = context.getFilesDir();
		}
		String dir = externalDir.getAbsolutePath() + subDirectory + "/";
		try {
			return getDirectory(dir);
		} catch (Exception e) {
			return null;
		}
	}
	
	public static String getCacheFileNameFromURL(String url) {
		return getFileNameFromURL(url) + CACHE_EXTENSION;
	}
	
	public static String getFileNameFromURL(String url) {
		String fileName = url.substring(url.substring(0, url.lastIndexOf('/')).lastIndexOf('/') + 1, url.length());
		return fileName.replace('/', '_');
	}
	
	public static String getDirectory(String path) {
		try {
			File file = new File(path);
			if (!file.exists()) {
				file.mkdirs();
				if (!file.exists()) {
					Logger.v("Utils", "NOT CREATE DIRECTORY");
				}
			}
			return path;
		} catch (Exception e) {
			return null;
		}
	}
	
	public static int valueOfInt(String str, int defalutValue) {
		if(str != null && str.length() > 0) {
			try {
				return Integer.valueOf(str);
			} catch (NumberFormatException e) {
				// TODO: handle exception
			}
		}
		return defalutValue;
	}
	
	public static String getReadableFileSize(long size) {
	    if(size <= 0) return "0";
	    final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
	    int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
	    return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups];
	}
	
	public static long getVideoDuration(String path) {
		if(TextUtils.isEmpty(path)) { return 0; }
		try {
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();
			retriever.setDataSource(path);
			String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			return Long.parseLong(duration);
		}catch (RuntimeException e) {
			// TODO: handle exception
		}
		return 0;
	}
	
	public static boolean isFlagContain(int sourceFlag, int compareFlag) {
        return (sourceFlag & compareFlag) == compareFlag;
    }
	
	public static boolean isNull(Object o) {
        return o == null ? true : false;
    }

    public static boolean isNull(List<?> list) {
        return list == null || list.size() == 0 ? true : false;
    }

    public static boolean isNull(String str) {
        return TextUtils.isEmpty(str) ? true : false;
    }	
    
    /////////////////////////////////////////////
    // for gallery2
    /////////////////////////////////////////////
    public static int getDisplayWidth(Context context) {
    	WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
	
	public static int dp(Context context, float dp) {
		return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
	}
}
