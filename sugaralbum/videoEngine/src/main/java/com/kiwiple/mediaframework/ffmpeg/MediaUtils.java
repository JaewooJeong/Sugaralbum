package com.kiwiple.mediaframework.ffmpeg;

import android.media.MediaMetadataRetriever;

import com.kiwiple.debug.Precondition;
import com.kiwiple.mediaframework.VideoEngineException;

/**
 * MediaUtils.
 */
final class MediaUtils {

	static int getDuration(String filePath) {
		Precondition.checkFile(filePath).checkExist();

		try {
			MediaMetadataRetriever retriever = new MediaMetadataRetriever();

			retriever.setDataSource(filePath);
			String duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
			retriever.release();

			if (duration != null) {
				return Integer.parseInt(duration);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		throw new VideoEngineException("failed to read media file: " + filePath);
	}

	private MediaUtils() {
		// Utility classes should have a private constructor.
	}
}
