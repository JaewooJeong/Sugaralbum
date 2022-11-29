package com.sugarmount.sugarcamera.story.service;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.text.TextUtils;

import com.kiwiple.debug.L;
import com.sugarmount.sugarcamera.story.StoryAlarmWakeLock;
import com.sugarmount.sugarcamera.story.noti.StoryNotification;

public class UplusStorySavingService extends Service {

	public final static String ACTION_START_SAVING_MD = "action.start.saving.moviediary";
	public final static String ACTION_FINISH_SAVING_MD = "action.finish.saving.moviediary";
	public final static String ACTION_CANCEL_SAVING_MD = "action.cancel.saving.moviediary";


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent == null) {
			return START_NOT_STICKY;
		}
		
		String action = intent.getAction();
		
		L.d("action : " + intent.getAction());
		
		if(!TextUtils.isEmpty(action)){
			
			if(action.equalsIgnoreCase(ACTION_START_SAVING_MD)){
				StoryAlarmWakeLock.acquireWakeLock(getApplicationContext());
				StoryNotification.getCreateStoryNotification(UplusStorySavingService.this);
				
			}else if(action.equalsIgnoreCase(ACTION_FINISH_SAVING_MD)){
				String path = intent.getStringExtra("path");
				Uri uri = intent.getData();
				int orientation = intent.getIntExtra("orientation", 0);
				
				StoryAlarmWakeLock.releaseWakeLock(getApplicationContext());
				
				if(!TextUtils.isEmpty(path) && uri != null){
					stopForeground(true);
					stopSelf();
					StoryNotification.completeCreateStoryNotification(getApplicationContext(), path, uri, orientation);
				}else{
					stopForeground(true);
					stopSelf();
				}
				
			}else if(action.equalsIgnoreCase(ACTION_CANCEL_SAVING_MD)){
				StoryAlarmWakeLock.releaseWakeLock(getApplicationContext());
				StoryNotification.removeManualStoryNotification(getApplicationContext());
				stopForeground(true);
				stopSelf();
				
			}
		}
		return START_NOT_STICKY;
	}
}
