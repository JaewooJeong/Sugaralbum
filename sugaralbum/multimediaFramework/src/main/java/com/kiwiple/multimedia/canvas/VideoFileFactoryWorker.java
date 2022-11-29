package com.kiwiple.multimedia.canvas;

import android.content.Context;
import android.content.Intent;
import android.media.MediaCodec.BufferInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Looper;
import android.util.Log;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.mediaframework.encoder.VideoEncoderBin;
import com.kiwiple.mediaframework.ffmpeg.FFmpegListener;
import com.kiwiple.mediaframework.ffmpeg.FFmpegProcessor;
import com.kiwiple.mediaframework.ffmpeg.FileFormat.VideoOutput;
import com.kiwiple.mediaframework.ffmpeg.IFFmpegProcessor.TaskPriority;
import com.kiwiple.mediaframework.ffmpeg.VideoMuxer;
import com.kiwiple.multimedia.exception.FileNotFoundException;
import com.kiwiple.multimedia.exception.MultimediaException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.preview.PreviewManager;
import com.kiwiple.multimedia.util.Size;

import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Timer;
import java.util.TimerTask;

/**
 * VideoFileFactoryWorker.
 * 
 */
final class VideoFileFactoryWorker extends AsyncTask<Void, Void, Void> {

	// // // // // Static variable.
	// // // // //
	private static final String TEMP_FILE_PREFIX = "video_file_factory_";

	private static final Resolution NHD_LG_F490L = new Resolution("nHD for LG-F490L", null, new Size(640, 368), 1);
	private static final Resolution HD_LG_F490L = new Resolution("HD for LG-F490L", null, new Size(1280, 720), 2);
	private static final Resolution FHD_LG_F490L = new Resolution("FHD for LG-F490L", null, new Size(1920, 1088), 3);

	private static final int INTERVAL = 33;

	static {
		NHD_LG_F490L.bypassCompatibility = true;
		HD_LG_F490L.bypassCompatibility = true;
		FHD_LG_F490L.bypassCompatibility = true;
	}

	// // // // // Member variable.
	// // // // //
	private final Context mContext;
	private final FFmpegProcessor mFFmpegProcessor;

	private final VideoFileFactory mVideoFileFactory;
	private final VideoFileFactory.RequestForm mRequestForm;
	private final VideoFileFactoryListener mVideoFileFactoryListener;

	private final BufferInfo mBufferInfo;

	private VideoEncoderBin mEncoder;
	private Visualizer mVisualizer;

	private File mTempVideoFile;
	private File mTempAudioFile;

	private int mWidth;
	private int mHeight;

	private int mTotalFrameCount;
	private int mRenderedFrameCount;

	private boolean mIsRunning;
	private final static int SOUND_FADE_MAX = 3000;

	// // // // // Constructor.
	// // // // //
	VideoFileFactoryWorker(Context context, VideoFileFactory.RequestForm form) throws IOException {

		mContext = context.getApplicationContext();
		mFFmpegProcessor = FFmpegProcessor.getInatnace(mContext);

		mVideoFileFactory = form.factory;
		mVideoFileFactoryListener = form.listener;
		mRequestForm = form;

		Resolution tempResolution = form.resolution;
		avoidDeviceDependency(form);
		int offset = form.resolution.height - tempResolution.height;

		mBufferInfo = new BufferInfo();
		mWidth = form.resolution.width;
		mHeight = form.resolution.height;

		mTempVideoFile = File.createTempFile(TEMP_FILE_PREFIX, null, mContext.getCacheDir());

		mEncoder = new VideoEncoderBin();
		mEncoder.initialize(mTempVideoFile.getAbsolutePath(), tempResolution.width, tempResolution.height, offset);
	}

	// // // // // Method.
	// // // // //
	void avoidDeviceDependency(VideoFileFactory.RequestForm requestForm) {

		String deviceName = android.os.Build.MODEL;
		if (deviceName.contains("LG-F490")) {
			if (requestForm.resolution.equals(Resolution.NHD)) {
				requestForm.resolution = NHD_LG_F490L;
			} else if (requestForm.resolution.equals(Resolution.HD)) {
				requestForm.resolution = HD_LG_F490L;
			} else if (requestForm.resolution.equals(Resolution.FHD)) {
				requestForm.resolution = FHD_LG_F490L;
			}

			try {
				JsonObject resolution = requestForm.jsonObject.getJSONObject(Visualizer.JSON_NAME_RESOLUTION);
				resolution.put(Resolution.JSON_NAME_ASPECT_RATIO, requestForm.resolution.aspectRatio);
			} catch (JSONException exception) {
				exception.printStackTrace();
				Precondition.assureUnreachable();
			}
		}
	}

	boolean isRunning() {
		return mIsRunning;
	}

	@Override
	protected void onPreExecute() {
		mIsRunning = true;
	}

	@Override
	protected Void doInBackground(Void... params) {

		if (Looper.myLooper() == null) {
			Looper.prepare();
		}

		try {
			initializeAudio();
			initializeVisualizer();

			int position = 0;

			int yuvBufferSize = Math.round(mWidth * mHeight * 1.5f);
			ByteBuffer rendererBuffer = ByteBuffer.allocate(yuvBufferSize);
			byte[] arrayWithinRendererBuffer = rendererBuffer.array();

			PixelCanvas frameBuffer = new PixelCanvas(new Size(mWidth, mHeight), true);

			while (!isCancelled() && mVisualizer.hasNextFrame()) {

				mVisualizer.setPosition(position);
				mVisualizer.draw(frameBuffer);

				PixelUtils.convertArgbToYuv420sp(frameBuffer, arrayWithinRendererBuffer, mWidth, mHeight);
				mBufferInfo.set(0, arrayWithinRendererBuffer.length, position * 1000L, 0);
				mEncoder.sampleEncoding(rendererBuffer, mBufferInfo);

				position += INTERVAL;
				publishProgress();

				if (!mTempVideoFile.isFile()) {
					throw new FileNotFoundException();
				}
			}

			mEncoder.finish();
			mEncoder.destory();

			if(isCancelled()) {
				if(rendererBuffer != null)
					rendererBuffer.clear();

				Timer t = new Timer();
				t.schedule(new TimerTask() {
					@Override
					public void run() {
						System.exit(0);
					}
				}, 3000);
			}

			if(!isCancelled()) {

				String srcVideoFilePath = mTempVideoFile.getAbsolutePath();
				String srcAudioFilePath = mRequestForm.audioSourceFilePath;

				VideoMuxer muxer = mFFmpegProcessor.asVideoMuxer();
				muxer.setPriority(TaskPriority.HIGH);
				muxer.setOutputFormat(VideoOutput.MPEG4);
				muxer.setFilePath(srcVideoFilePath, srcAudioFilePath, mRequestForm.outputFilePath);
				muxer.setLoopAudio(true);
				muxer.setFadeAudio(SOUND_FADE_MAX, SOUND_FADE_MAX);

				if (!muxer.execute()) {
					throw new MultimediaException("failed to mux video.");
				}

//				mNoticeFolder = new File(mContext.getCacheDir() + NOTICE_FOLDER_PATH);
//				mNoticeFolder.mkdir();
//				createNoticeFile(NOTICE_FILE_START);
//
//				muxer.execute(this.mFFmpegListener);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			mVideoFileFactoryListener.onError(exception);
			cancel(true);
		}
		Looper.myLooper().quit();
		return null;
	}

	@Override
	protected void onPostExecute(Void result) {
		release();
		mVideoFileFactoryListener.onComplete();
	}

	@Override
	protected void onCancelled(Void result) {
		release();
	}

	@Override
	protected void onProgressUpdate(Void... values) {
		++mRenderedFrameCount;
		mVideoFileFactoryListener.onProgressUpdate(mTotalFrameCount, mRenderedFrameCount);
	}

	private void initializeAudio() throws IOException {

		if (mRequestForm.isAssetAudioSource) {
			mTempAudioFile = createTempFileFromAsset(mRequestForm.audioSourceFilePath);
			mRequestForm.audioSourceFilePath = mTempAudioFile.getAbsolutePath();
		}
	}

	private void initializeVisualizer() throws JSONException {

		PreviewManager.getInstance(mContext).clear();
		mVisualizer = new Visualizer(mContext, mRequestForm.jsonObject);

		if (!mVisualizer.getResolution().isLogicallyEquals(mRequestForm.resolution))
			mVisualizer.getEditor().start().setSize(mRequestForm.resolution).finish();

		mTotalFrameCount = (int) Math.ceil((double) mVisualizer.getDuration() / INTERVAL) + 1;
		mRenderedFrameCount = 0;
	}

	private File createTempFileFromAsset(String assetFilePath) throws IOException {

		File tempFile = File.createTempFile(TEMP_FILE_PREFIX, null, mContext.getCacheDir());

		InputStream inputStream = mContext.getAssets().open(assetFilePath);
		OutputStream outputStream = new FileOutputStream(tempFile);

		byte[] buffer = new byte[1024 * 4];
		int read = 0;
		while ((read = inputStream.read(buffer)) > 0) {
			outputStream.write(buffer, 0, read);
		}

		inputStream.close();
		outputStream.close();
		return tempFile;
	}

	private void release() {

		if (mVisualizer != null) {
			mVisualizer.release();
			mVisualizer = null;
		}

		if (isRunning()) {
			cancel(true);
			mIsRunning = false;
		}

		mTempVideoFile.delete();
		if (mTempAudioFile != null) {
			mTempAudioFile.delete();
		}
		mVideoFileFactory.releaseWorker();
	}

	private File mNoticeFolder;

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
		}
	}

	static final String NOTICE_FOLDER_PATH = "/ffmpeg_service_notice";
	static final String NOTICE_FILE_START = "start";
	static final String NOTICE_FILE_ERROR = "error";
	static final String NOTICE_FILE_COMPLETION = "completion";

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
}
