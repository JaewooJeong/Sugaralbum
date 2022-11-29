package com.kiwiple.mediaframework.ffmpeg;

import java.io.File;
import java.io.IOException;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import com.kiwiple.debug.L;

/**
 * FFmpegService.
 */
public class FFmpegService extends Service {

	// // // // // Static variable.
	// // // // //
	static final String NOTICE_FOLDER_PATH = "/ffmpeg_service_notice";
	static final String NOTICE_FILE_START = "start";
	static final String NOTICE_FILE_ERROR = "error";
	static final String NOTICE_FILE_COMPLETION = "completion";

	static final String EXTRAS_KEY_COMMAND = "com.kiwiple.mediaframework.extras.Command";

	static final String CHANNEL_ID = "FFmpegService_Notice";

	// // // // // Member variable.
	// // // // //
	private File mNoticeFolder;
	private PowerManager.WakeLock mWakeLock;

	// // // // // Method.
	// // // // //
	@Override
	public void onCreate() {
		Log.e("","FFmpegService - onCreate #1");
		mNoticeFolder = new File(getCacheDir() + NOTICE_FOLDER_PATH);
		mNoticeFolder.mkdir();
		createNoticeFile(NOTICE_FILE_START);
		NotificationManager manager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			NotificationChannel serviceChannel = new NotificationChannel(
					CHANNEL_ID,
					"SugarAlbum foreground service 2",
					NotificationManager.IMPORTANCE_HIGH
			);
			manager.createNotificationChannel(serviceChannel);
		}

		Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).build();
		startForeground(11111111, notification);


		PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, FFmpegService.class.getName());
		mWakeLock.acquire(10*60*1000L /*10 minutes*/);

		Log.e("", "FFmpegService - onCreate #2");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("","FFmpegService - onStartCommand #1");
		String command = intent.getStringExtra(EXTRAS_KEY_COMMAND);
		FFmpegProcessor.executeByService(command, mFFmpegListener);

		Log.e("","FFmpegService - onStartCommand #2");
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.e("","FFmpegService - onBind #1");
		return mBinder;
	}

	private void createNoticeFile(final String name) {
		Log.e("","FFmpegService - createNoticeFile #1");
		try {
			File file = new File(mNoticeFolder, name);
			if (file.isFile()) {
				file.delete();
			}
			file.createNewFile();
		} catch (IOException exception) {
			exception.printStackTrace(); // Do nothing.
		} finally {
			if (mWakeLock != null && mWakeLock.isHeld()) {
				mWakeLock.release();
			}
		}
	}

	// // // // // Anonymous Class.
	// // // // //
	private IBinder mBinder = new IFFmpegService.Stub() {
		// Empty IBinder.
	};

	private FFmpegListener mFFmpegListener = new FFmpegListener() {

		@Override
		public void onError() {
			Log.e("","FFmpegService - onError #1");
//			stopSelf();
			createNoticeFile(NOTICE_FILE_ERROR);
		}

		@Override
		public void onCompletion() {
			Log.e("","FFmpegService - onCompletion #1");
//			stopSelf();
			createNoticeFile(NOTICE_FILE_COMPLETION);
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			stopForeground(true);
		}
	}
}
