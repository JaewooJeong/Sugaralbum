package com.sugarmount.sugarcamera.story;

import android.content.Context;
import android.os.PowerManager;

import com.kiwiple.debug.L;
import com.sugarmount.sugarcamera.story.service.StoryAnalysisMusicForegroundService;
import com.sugarmount.sugarcamera.story.service.VideoCreationService;

 public class StoryAlarmWakeLock {

	private static PowerManager.WakeLock sWakeLock;

	private final static String TAG = StoryAlarmWakeLock.class.getSimpleName();

	public 	static void acquireWakeLock(Context context) {
		
		if (sWakeLock != null) {
			return;
		}

		PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

		sWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);
		//wakeLock이 이미 acquire되었을 경우 pass
		if(!sWakeLock.isHeld())
			sWakeLock.acquire();
	}

	public static void releaseWakeLock(Context context) {
		
		L.e("analysing music : "+ StoryAnalysisMusicForegroundService.getAnalysisMusicServiceStatus() +", saving diary : "+ VideoCreationService.getIsSavingMovieDiary());
		//wakeLock release시 분석 or 저장이 완료 되었을 경우 해제 
		if(!StoryAnalysisMusicForegroundService.getAnalysisMusicServiceStatus() && !VideoCreationService.getIsSavingMovieDiary()){
			if (sWakeLock != null && sWakeLock.isHeld()) {
				sWakeLock.release();
				sWakeLock = null;
			}
		}
	}
}
