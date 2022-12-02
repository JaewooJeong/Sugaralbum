package com.kiwiple.mediaframework.ffmpeg;

import static com.kiwiple.mediaframework.VideoEngineEnvironment.DEFAULT_THREAD_SLEEP_TIME;
import static com.kiwiple.mediaframework.ffmpeg.FFmpegService.EXTRAS_KEY_COMMAND;
import static com.kiwiple.mediaframework.ffmpeg.FFmpegService.NOTICE_FILE_COMPLETION;
import static com.kiwiple.mediaframework.ffmpeg.FFmpegService.NOTICE_FILE_START;
import static com.kiwiple.mediaframework.ffmpeg.FFmpegService.NOTICE_FOLDER_PATH;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.FileObserver;

import com.kiwiple.debug.Precondition;
import com.kiwiple.mediaframework.ffmpeg.FileFormat.AudioInput;
import com.kiwiple.mediaframework.ffmpeg.FileFormat.VideoInput;

/**
 * FFmpegProcessor.
 */
public final class FFmpegProcessor extends AbstractFFmpegProcessor {

	// // // // // Static variable.
	// // // // //
	private static final String COMMAND_PREFIX = adaptCommandFormat("ffmpeg -v quiet ");

	private static final long START_SERVICE_TIMEOUT = 5L;
	private static final int DEFAULT_QUEUE_CAPACITY = 11;

	private static WeakReference<FFmpegProcessor> sWeakInstance;

	private static boolean sIsRunning;

	// // // // // Member variable.
	// // // // //
	private final Context mContext;
	private final ActivityManager mActivityManager;
	private final FileObserver mNoticeObserver;

	private final PriorityQueue<FFmpegTask> mTaskQueue;
	private TaskQueueThread mTaskQueueThread;
	private FFmpegTask mCurrentTask;

	private CountDownLatch mCountDownLatch;

	// // // // // Static method.
	// // // // //
	static {
		System.loadLibrary("avutil-54");
		System.loadLibrary("avcodec-56");
		System.loadLibrary("avformat-56");
		System.loadLibrary("swscale-3");
		System.loadLibrary("swresample-1");
		System.loadLibrary("avfilter-5");
		System.loadLibrary("FFmpegProcessor");
	}

	public synchronized static FFmpegProcessor getInatnace(Context context) {
		Precondition.checkNotNull(context, "context must not be null.");

		if (sWeakInstance == null || sWeakInstance.get() == null) {
			sWeakInstance = new WeakReference<FFmpegProcessor>(new FFmpegProcessor(context));
		}
		return sWeakInstance.get();
	}

	static boolean executeByService(String command, FFmpegListener listener) {
		Precondition.checkNotNull(command, "command must not be null.");
		Precondition.checkNotNull(listener, "listener must not be null.");


		String[] args = (COMMAND_PREFIX + command).split(SPLIT_DELIMITER);
		return nativeExecute(args, listener);
	}

	// // // // // Constructor.
	// // // // //
	private FFmpegProcessor(Context context) {

		mContext = context.getApplicationContext();
		mActivityManager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
		mTaskQueue = new PriorityQueue<FFmpegTask>(DEFAULT_QUEUE_CAPACITY, FFmpegTask.comparator);

		final File noticeFolder = new File(mContext.getCacheDir() + NOTICE_FOLDER_PATH);
		noticeFolder.mkdir();

		mNoticeObserver = new FileObserver(noticeFolder.getAbsolutePath(), FileObserver.CREATE) {

			@Override
			public void onEvent(int event, String path) {

				if (event != FileObserver.CREATE) {
					return;
				}
				new File(noticeFolder, path).delete();
				handleNotice(path);
			}
		};
	}

	// // // // // Method.
	// // // // //
	public AudioConverter asAudioConverter() {
		return new AudioConverter(this);
	}

	public VideoMuxer asVideoMuxer() {
		return new VideoMuxer(this);
	}

	synchronized boolean execute(String command, TaskPriority priority) {
		Precondition.checkNotNull(command, "command must not be null.");

		FFmpegTask task = new FFmpegTask(command, priority, null);
		addTaskToQueue(task);

		try {
			while (!task.done) {
				FFmpegProcessor.this.wait();
			}
		} catch (InterruptedException exception) {
			// Do nothing.
		}
		return task.result;
	}

	synchronized void execute(String command, TaskPriority priority, FFmpegListener listener) {
		Precondition.checkNotNull(command, "command must not be null.");
		Precondition.checkNotNull(priority, "priority must not be null.");
		Precondition.checkNotNull(listener, "listener must not be null.");

		addTaskToQueue(new FFmpegTask(command, priority, listener));
	}

	private boolean startService(String command, FFmpegListener listener) {

		sIsRunning = true;
		mNoticeObserver.startWatching();
		mCountDownLatch = new CountDownLatch(1);

		Intent serviceIntent = new Intent(mContext, FFmpegService.class);
		serviceIntent.putExtra(EXTRAS_KEY_COMMAND, command);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			mContext.startForegroundService(serviceIntent);
		}else{
			mContext.startService(serviceIntent);
		}

//		boolean b = executeByService(command, listener);

		try {
			if (mCountDownLatch.await(START_SERVICE_TIMEOUT, TimeUnit.SECONDS)) {
				return true;
			} else {
				sIsRunning = false;
				return false;
			}
		} catch (InterruptedException exception) {
			sIsRunning = false;
			return false;
		} finally {
			if (!sIsRunning) {
				mContext.stopService(serviceIntent);
				mCountDownLatch = null;
			}
		}
	}

	private void addTaskToQueue(FFmpegTask task) {

		mTaskQueue.offer(task);

		if (mTaskQueueThread == null || !mTaskQueueThread.isAlive()) {
			mTaskQueueThread = new TaskQueueThread();
			mTaskQueueThread.start();
		}
	}

	private void handleNotice(String notice) {

		if (notice.equals(NOTICE_FILE_START)) {
			if (mCountDownLatch != null) {
				mCountDownLatch.countDown();
			}
		} else {
			mNoticeObserver.stopWatching();

			mCurrentTask.result = notice.equals(NOTICE_FILE_COMPLETION) ? true : false;
			mCurrentTask.done = true;
			waitForServiceShutdown();
			sIsRunning = false;

			synchronized (mTaskQueueThread) {
				mTaskQueueThread.notifyAll();
			}
		}
	}

	private void waitForServiceShutdown() {

		while (true) {

			boolean isAlive = false;
			for (RunningServiceInfo serviceInfo : mActivityManager.getRunningServices(Integer.MAX_VALUE)) {
				if (FFmpegService.class.getName().equals(serviceInfo.service.getClassName())) {
					isAlive = true;
				}
			}
			if (!isAlive) {
				return;
			}
			try {
				Thread.sleep(DEFAULT_THREAD_SLEEP_TIME);
			} catch (InterruptedException e) {
				// Do nothing.
			}
		}
	}

	@Override
	public boolean isSupportedInputFile(String fileName) {
		return AudioInput.isSupported(fileName) || VideoInput.isSupported(fileName);
	}

	@Override
	public boolean isRunning() {
		return sIsRunning || !mTaskQueue.isEmpty();
	}

	// // // // // Inner Class.
	// // // // //
	private static final class FFmpegTask {

		static final Comparator<FFmpegTask> comparator = new Comparator<FFmpegTask>() {

			@Override
			public int compare(FFmpegTask lhs, FFmpegTask rhs) {

				int lhsLevel = lhs.priority.level;
				int rhsLevel = rhs.priority.level;
				
				if (lhsLevel == rhsLevel) {
					return lhs.time > rhs.time ? 1 : -1;
				} else {
					return lhsLevel < rhsLevel ? 1 : -1;
				}
			}
		};

		final String command;
		final FFmpegListener listener;
		
		private final TaskPriority priority;
		private final long time;

		boolean result;
		boolean done;

		FFmpegTask(String command, TaskPriority priority, FFmpegListener listener) {
			
			this.command = command;
			this.priority = priority;
			this.listener = listener;
			this.time = System.currentTimeMillis();
		}
	}

	private final class TaskQueueThread extends Thread {

		@Override
		public synchronized void run() {
			super.run();

			while (!mTaskQueue.isEmpty()) {

				mCurrentTask = mTaskQueue.poll();
				if (startService(mCurrentTask.command, mCurrentTask.listener)) {
					await();
				}

				if (mCurrentTask.listener == null) {
					notifyForSync();
				} else if (mCurrentTask.result) {
					mCurrentTask.listener.onCompletion();
				} else {
					mCurrentTask.listener.onError();
				}
				mCurrentTask = null;
			}
			mTaskQueueThread = null;
		}

		private synchronized void await() {

			try {
				wait();
			} catch (InterruptedException exception) {
				// Do nothing.
			}
		}

		private void notifyForSync() {
			synchronized (FFmpegProcessor.this) {
				FFmpegProcessor.this.notifyAll();
			}
		}
	}

	// // // // // Native.
	// // // // //
	private static native boolean nativeExecute(String[] args, FFmpegListener listener);
}