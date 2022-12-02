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

		if (isRunning()) {
			return false;
		}
		RequestForm form = null;
		try {
			form = new RequestForm();
			form.resolution = resolution;
			form.jsonObject = previewManager.getVisualizer().toJsonObject();
			form.outputFilePath = outputFilePath;
			form.audioSourceFilePath = previewManager.getAudioFilePath();
			form.isAssetAudioSource = previewManager.isAssetAudioFile();

			mVideoFileFactoryWorker = new VideoFileFactoryWorker(mContext, form);
			mVideoFileFactoryWorker.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

			return true;
		} catch (Exception exception) {
			form.listener.onError(exception);
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