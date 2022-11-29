package com.kiwiple.scheduler.util;

import java.io.IOException;
import java.util.Locale;
import java.util.StringTokenizer;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.webkit.MimeTypeMap;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.search.ImageSearch;
import com.kiwiple.multimedia.canvas.BurstShotScene;
import com.kiwiple.multimedia.canvas.CollageScene;
import com.kiwiple.multimedia.canvas.FileImageResource;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.LayerScene;
import com.kiwiple.multimedia.canvas.MultiLayerScene;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.VideoFileScene;
import com.kiwiple.multimedia.util.ImageUtils;
import com.kiwiple.multimedia.util.Size;
import com.kiwiple.scheduler.database.uplus.UplusAnalysisPersister;

public class ImageUtil {

	static final String MIME_TYPE_IMAGE_PREFIX = "image/";
	static final String MIME_TYPE_VIDEO_PREFIX = "video/";

	public static boolean isVideoFile(String filePath) {
		String mimeType = getMimeType(filePath);
		if(mimeType != null){
			if (mimeType.startsWith(MIME_TYPE_VIDEO_PREFIX)) {
				return true;
			} else {
				return false;
			}
		}else{
			L.e("unknown mime type, file : " + filePath); 
			return false; 
		}
	}

	public static String getMimeType(String fileName) {
		Precondition.checkString(fileName).checkNotEmpty();

		int lastIndexOfDot = fileName.lastIndexOf(".");
		if (lastIndexOfDot == -1)
			return null;

		String extension = fileName.substring(lastIndexOfDot + 1, fileName.length()).toLowerCase(Locale.ENGLISH);
		return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
	}

	static public ImageData getImageData(Context context, int id, String path) {
		ImageSearch imageSearch = new ImageSearch(context, null);
		ImageData imageData = null;
		imageData = imageSearch.getImagaeDataForImageId(id);
		if (imageData == null) {
			imageData = new ImageData();
			imageData.id = id;
			imageData.path = path;
			imageData.fileName = getFileName(path);
			imageData.orientation = ImageUtils.getOrientation(path) + "";
			Size size = ImageUtils.measureImageSize(path);
			imageData.width = size.width;
			imageData.height = size.height;

		}
		return imageData;
	}

	static public String getFileName(String path) {
		String fileName = null;
		StringTokenizer st = new StringTokenizer(path, "/");
		while (st.hasMoreTokens()) {
			fileName = st.nextToken();
		}
		StringTokenizer st2 = new StringTokenizer(fileName, ".");
		while (st2.hasMoreTokens()) {
			fileName = st2.nextToken();
			break;
		}

		return fileName;
	}

	static public ImageData getVideoData(Context context, int id, String path) {

		ImageData imageData = null;
		long date = 0;
		int width = 0; 
		int height = 0; 
		int orientation = 0; 

		UplusAnalysisPersister persister = UplusAnalysisPersister.getAnalysisPersister(context.getApplicationContext());
		Cursor cursor = persister.getVideoDataCursorInGallery(id);

		if (cursor != null) {
			cursor.moveToNext();
			date = cursor.getLong(cursor.getColumnIndexOrThrow("datetaken"));
			if (date == 0) {
				date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)) * 1000;
			}
		}
		
		MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
		mediaMetadataRetriever.setDataSource(path);
		Bitmap bitmap = mediaMetadataRetriever.getFrameAtTime();
		try {
			mediaMetadataRetriever.release();
		} catch (IOException e) {
			e.printStackTrace();
		}
		width = bitmap.getWidth(); 
		height = bitmap.getHeight(); 
		
		L.d("video file path : " + path +", width : " + width +", height : " + height); 

		imageData = new ImageData();
		imageData.id = id;
		imageData.path = path;
		imageData.date = date;
		imageData.width = width; 
		imageData.height = height; 
		imageData.orientation = orientation+"";

		return imageData;
	}
}