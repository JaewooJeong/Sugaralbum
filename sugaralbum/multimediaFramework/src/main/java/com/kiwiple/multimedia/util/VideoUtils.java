package com.kiwiple.multimedia.util;

import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;

import java.io.IOException;

/**
 * 라이브러리 개발 목적으로 사용하는 클래스입니다. 라이브러리 외부에서의 사용에 대해서는 그 유효성을 보장하지 않습니다.
 */
public class VideoUtils {
	public static int getVideoRotation(String filePath) {
		int rotation;
		if (Build.VERSION.SDK_INT >= 17) {
			MediaMetadataRetriever mmr = new MediaMetadataRetriever();
			mmr.setDataSource(filePath);
			try {
				rotation = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
				while (rotation < 0) {
					rotation += 360;
				}
				while (rotation > 360) {
					rotation -= 360;
				}
				try {
					mmr.release();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (NumberFormatException e) {
				rotation = 0;
			}
		}
		// 20150213 olive : #10613 임시코드, mp4의 rotate metadata를 API level 17이상에서만 가져올 수 있다.
		// video의 width, height를 비교해서 구하는 rotation값은 90과 270, 0과 180을 구분할 수 없으며, 정확한 rotation 값이라고 볼 수 없다.
		else {
			/**
			 * MediaPlayer 사용 후 리소스 해제, 여러개의 MediaCodec 사용 불가
			 */
			MediaPlayer mp = null;
			try {
				mp = new MediaPlayer();
				mp.setDataSource(filePath);
				mp.prepare();
				if (mp.getVideoWidth() < mp.getVideoHeight()) {
					rotation = 90;
				} else {
					rotation = 0;
				}
				if (mp != null) {
					mp.release();
					mp = null;
				}
			} catch (Exception e) {
				if (mp != null) {
					mp.release();
				}
				rotation = 0;
			}
		}
		return rotation;
	}

	public static boolean isPortraitVideo(String filePath) {
		int width = 0;
		int height = 0;
		// olive 성능은 MediaMetadataRetriever을 쓰는게 빠르지만,
		// METADATA_KEY_VIDEO_ROTATION을 api17부터 사용가능하다.
		if (Build.VERSION.SDK_INT >= 17) {
			int rotation;
			MediaMetadataRetriever mmr = new MediaMetadataRetriever();
			mmr.setDataSource(filePath);
			try {
				rotation = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION));
				width = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH));
				height = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT));
				while (rotation < 0) {
					rotation += 360;
				}
				while (rotation > 360) {
					rotation -= 360;
				}
			} catch (NumberFormatException e) {
				rotation = 0;
			}
			if (rotation == 90 || rotation == 270) {
				int temp = width;
				width = height;
				height = temp;
			}
		} else {
			/**
			 * MediaPlayer 사용 후 리소스 해제, 여러개의 MediaCodec 사용 불가
			 */
			MediaPlayer mp = null;
			try {
				mp = new MediaPlayer();
				mp.setDataSource(filePath);
				mp.prepare();
				width = mp.getVideoWidth();
				height = mp.getVideoHeight();
			} catch (Exception e) {
				if (mp != null) {
					mp.release();
				}
			}
		}
		return height > width;
	}

	public static int getVideoDuration(String filePath) {
		int duration = 0;
		MediaMetadataRetriever mmr = new MediaMetadataRetriever();
		try {
			mmr.setDataSource(filePath);
			duration = Integer.parseInt(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
			mmr.release();
			mmr = null;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return duration;
	}

}
