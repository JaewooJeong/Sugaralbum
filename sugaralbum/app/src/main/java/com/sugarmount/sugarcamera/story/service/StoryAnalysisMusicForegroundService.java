package com.sugarmount.sugarcamera.story.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;

import com.kiwiple.debug.L;
import com.sugarmount.sugarcamera.story.music.StoryMusicAnalysisManager;
import com.sugarmount.sugarcamera.story.noti.StoryNotification;

public class StoryAnalysisMusicForegroundService extends Service {

	//not used
	public final static String ACTION_START_ANALYSIS_MUSIC = "action.start.analysis.music";
	public final static String ACTION_UPDATE_ANALYSIS_MUSIC = "action.update.analysis.music";
	public final static String ACTION_FINISH_ANALYSIS_MUSIC = "action.finish.analysis.music";
	public final static String ACTION_CANCEL_ANALYSIS_MUSIC = "action.cancel.analysis.music";
	public final static String EXTRA_NOTIFICATION_PROGRESS_VALUE = "extra.notification.progress.value";
	public final static String ACTION_START_AUTO_ANALYSIS_MUSIC = "action.start.auto.analysis.music";
	public final static String ACTION_FINISH_AUTO_ANALYSIS_MUSIC = "action.finish.auto.analysis.music";

	public final static String ACTION_FINISH_ANALYSIS_MUSIC_FROM_HIDDEN = "action.finishanalysis.music.from.hidden";
	public final static String ACTION_FORCE_FINISHING_ANALYSIS_MUSIC_SERVICE = "action.force.finishing.analysis.music.service";
	
	private final IBinder mServiceBinder = new StoryAnalysisBinder();
	private static boolean sIsAnalysisServiceAlive = false;
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mServiceBinder;
	}
	
	public static boolean getAnalysisMusicServiceStatus(){
		L.i("sIsAnalysisServiceAlive = "+ sIsAnalysisServiceAlive);
		return sIsAnalysisServiceAlive;
	}

	public class StoryAnalysisBinder extends Binder{
		public StoryAnalysisMusicForegroundService getAnalysisService(){
			return StoryAnalysisMusicForegroundService.this;
		}
	}
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	//jhshin  recent-app에서 swipe로 어플 삭제시 호출됨 
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		// TODO Auto-generated method stub
		super.onTaskRemoved(rootIntent);
	}
	
	//not used
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		String action = null;
		if(intent != null)
			action = intent.getAction();
		
		L.e("action = "+ action);
		
		if(!TextUtils.isEmpty(action)){
		

			if(action.equalsIgnoreCase(ACTION_FINISH_ANALYSIS_MUSIC)){
				stopForeground(true);
				stopSelf();

			}else if(action.equalsIgnoreCase(ACTION_CANCEL_ANALYSIS_MUSIC)){
				StoryNotification.removeAnalysisMusicNotification(this);
				stopForeground(true);
				stopSelf();
			}else if(action.equalsIgnoreCase(ACTION_FINISH_AUTO_ANALYSIS_MUSIC)){
				StoryNotification.removeAnalysisMusicNotification(this);
				stopForeground(true);
				stopSelf();
				
			}else if(action.equalsIgnoreCase(ACTION_FINISH_ANALYSIS_MUSIC_FROM_HIDDEN)){
				stopForeground(true);
				stopSelf();

			}else if(action.equalsIgnoreCase(ACTION_FORCE_FINISHING_ANALYSIS_MUSIC_SERVICE)){
				stopForeground(true);
				stopSelf();
			}
		}
		
		return START_NOT_STICKY;
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		super.onLowMemory();
        boolean isAliveAnalysisTask = StoryMusicAnalysisManager.getInstance(getApplicationContext()).isAnalyzingMusicTask();
		L.i("analysis task isAlive : "+ isAliveAnalysisTask);
		if(!isAliveAnalysisTask){
			stopForeground(true);
			stopSelf();
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
