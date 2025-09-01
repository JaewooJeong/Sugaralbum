package com.kiwiple.multimedia.canvas;

import android.content.Context;
import android.os.AsyncTask;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.preview.PreviewManager;

/**
 * VideoFileFactory.
 * 
 */
public final class VideoFileFactory {

	// // // // // Static variable.
	// // // // //
	private static VideoFileFactory sInstance;

	// // // // // Member variable.
	// // // // //
	private final Context mContext;

	private VideoFileFactoryListener mVideoFileFactoryListener;
	private VideoFileFactoryWorker mVideoFileFactoryWorker;

	// // // // // Static method.
	// // // // //
	public static synchronized VideoFileFactory getInstance(Context context) {

		if (sInstance == null) {
			sInstance = new VideoFileFactory(context);
		}
		return sInstance;
	}

	// // // // // Constructor.
	// // // // //
	private VideoFileFactory(Context context) {
		Precondition.checkNotNull(context);
		mContext = context.getApplicationContext();
	}

	// // // // // Method.
	// // // // //
	public synchronized boolean create(PreviewManager previewManager, Resolution resolution, String outputFilePath) {
		com.kiwiple.debug.L.d("=== VideoFileFactory.create() ENTRY ===");
		com.kiwiple.debug.L.d("Resolution: " + resolution);
		com.kiwiple.debug.L.d("Output file path: " + outputFilePath);
		com.kiwiple.debug.L.d("PreviewManager: " + (previewManager != null ? "OK" : "NULL"));
		com.kiwiple.debug.L.d("Thread: " + Thread.currentThread().getName());
		
		if (isRunning()) {
			com.kiwiple.debug.L.e("VideoFileFactory is already running - returning false");
			return false;
		}
		
		com.kiwiple.debug.L.d("VideoFileFactory not running, proceeding...");
		RequestForm form = null;
		try {
			com.kiwiple.debug.L.d("Creating RequestForm...");
			form = new RequestForm();
			form.resolution = resolution;
			form.factory = this;
			
			com.kiwiple.debug.L.d("Getting visualizer from PreviewManager...");
			form.jsonObject = previewManager.getVisualizer().toJsonObject();
			com.kiwiple.debug.L.d("JsonObject obtained: " + (form.jsonObject != null ? "OK" : "NULL"));
			
			form.outputFilePath = outputFilePath;
			form.audioSourceFilePath = previewManager.getAudioFilePath();
			form.isAssetAudioSource = previewManager.isAssetAudioFile();
			form.listener = mVideoFileFactoryListener;
			
			com.kiwiple.debug.L.d("RequestForm populated:");
			com.kiwiple.debug.L.d("  - Audio source: " + form.audioSourceFilePath + ", isAsset: " + form.isAssetAudioSource);
			com.kiwiple.debug.L.d("  - Listener: " + (form.listener != null ? "OK" : "NULL"));
			
			com.kiwiple.debug.L.d("Creating VideoFileFactoryWorker...");
			try {
				mVideoFileFactoryWorker = new VideoFileFactoryWorker(mContext, form);
				com.kiwiple.debug.L.d("VideoFileFactoryWorker constructor completed successfully");
			} catch (Exception e) {
				com.kiwiple.debug.L.e("CRITICAL: VideoFileFactoryWorker constructor failed: " + e.getMessage(), e);
				throw e;
			}
			
			com.kiwiple.debug.L.d("Executing VideoFileFactoryWorker on thread pool...");
			try {
				mVideoFileFactoryWorker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
				com.kiwiple.debug.L.d("AsyncTask.executeOnExecutor() called successfully");
			} catch (Exception e) {
				com.kiwiple.debug.L.e("CRITICAL: AsyncTask execution failed: " + e.getMessage(), e);
				throw e;
			}
			
			com.kiwiple.debug.L.d("=== VideoFileFactoryWorker started successfully ===");
			return true;
		} catch (Exception exception) {
			com.kiwiple.debug.L.e("VideoFileFactory.create() error: " + exception.getMessage(), exception);
			if (form != null && form.listener != null) {
				form.listener.onError(exception);
			}
			return false;
		}
	}

	public synchronized void cancel() {

		if (isRunning()) {
			mVideoFileFactoryWorker.cancel(true);
			mVideoFileFactoryWorker = null;
		}
	}

	public synchronized boolean isRunning() {
		return mVideoFileFactoryWorker != null && mVideoFileFactoryWorker.isRunning();
	}

	public void setListener(VideoFileFactoryListener videoFileFactoryListener) {
		mVideoFileFactoryListener = videoFileFactoryListener;
	}

	void releaseWorker() {
		mVideoFileFactoryWorker = null;
	}

	// // // // // Inner Class.
	// // // // //
	final class RequestForm {

		VideoFileFactory factory;
		VideoFileFactoryListener listener;
		Resolution resolution;
		JsonObject jsonObject;
		String outputFilePath;
		String audioSourceFilePath;
		boolean isAssetAudioSource;

		private RequestForm() {
			factory = VideoFileFactory.this;
			listener = mVideoFileFactoryListener;
		}
	}
}