package com.kiwiple.multimedia.audio;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import android.content.Context;

import com.kiwiple.debug.Precondition;
import com.kiwiple.mediaframework.ffmpeg.AudioConverter;
import com.kiwiple.mediaframework.ffmpeg.FFmpegProcessor;
import com.kiwiple.mediaframework.ffmpeg.FileFormat.AudioInput;
import com.kiwiple.mediaframework.ffmpeg.FileFormat.AudioOutput;
import com.kiwiple.mediaframework.ffmpeg.FileFormat.VideoInput;

/**
 * BeatTracker.
 */
public final class BeatTracker {

	// // // // // Static variable.
	// // // // //
	private static final int SAMPLING_RATE = 22050;
	private static final int CHANNEL_COUNT = 1;

	private static WeakReference<BeatTracker> sWeakInstance;

	// // // // // Member variable.
	// // // // //
	private final Context mContext;
	private final FFmpegProcessor mFFmpegProcessor;

	// // // // // Static method.
	// // // // //
	public static synchronized BeatTracker getInatnace(Context context) {

		if (sWeakInstance == null || sWeakInstance.get() == null) {
			sWeakInstance = new WeakReference<BeatTracker>(new BeatTracker(context));
		}
		return sWeakInstance.get();
	}

	// // // // // Constructor.
	// // // // //
	static {
		System.loadLibrary("BeatTracker");
	}

	private BeatTracker(Context context) {
		Precondition.checkNotNull(context);

		mContext = context.getApplicationContext();
		mFFmpegProcessor = FFmpegProcessor.getInatnace(mContext);
	}

	// // // // // Method.
	// // // // //
	public synchronized double[] track(String filePath) {
		return track(filePath, (Float) null);
	}

	public synchronized double[] track(String filePath, Float dstDurationS) {
		Precondition.checkFile(filePath).checkExist();
		Precondition.checkArgument(isSupportedAudioFile(filePath), "not supported file format.");
		if (dstDurationS != null) {
			Precondition.checkOnlyPositive(dstDurationS);
		}

		File file = new File(filePath);
		WavFile wavFile = null;
		boolean isTempFileCreated = false;
		try {
			if (!filePath.endsWith(".wav")) {
				file = createTempWavFile(filePath, dstDurationS);
				isTempFileCreated = true;
			}

			wavFile = WavFile.openWavFile(file);
			if (!isTempFileCreated && isNeedToConvert(wavFile, dstDurationS)) {
				file = createTempWavFile(filePath, dstDurationS);
				isTempFileCreated = true;

				wavFile.close();
				wavFile = WavFile.openWavFile(file);
			}

			int frameCount = (int) wavFile.getNumFrames();
			int samplingRate = (int) wavFile.getSampleRate();
			short[] frames = new short[frameCount];

			wavFile.readFrames(frames, 0, frameCount);
			wavFile.close();

			if (isTempFileCreated) {
				file.delete();
			}
			return nativeTrack(frames, samplingRate);

		} catch (IOException | WavFileException exception) {
			exception.printStackTrace();

			if (isTempFileCreated && file.isFile()) {
				file.delete();
			}
			return null;
		}
	}

	public void track(final String filePath, final Listener listener) {
		track(filePath, null, listener);
	}

	public void track(final String filePath, final Float dstDurationS, final Listener listener) {
		Precondition.checkNotNull(listener);

		new Thread(new Runnable() {

			@Override
			public void run() {
				double[] result = track(filePath, dstDurationS);
				if (result != null) {
					listener.onCompletion(result);
				} else {
					listener.onError();
				}
			}
		}).start();
	}

	private synchronized File createTempWavFile(String filePath, Float durationS) throws IOException {

		AudioConverter converter = mFFmpegProcessor.asAudioConverter();
		File tempFile = File.createTempFile("beat_tracker_", ".wav", mContext.getCacheDir());

		converter.setFilePath(filePath, tempFile.getAbsolutePath());
		converter.setOutputFormat(AudioOutput.WAV);
		converter.setSamplingRate(SAMPLING_RATE);
		converter.setChannelCount(CHANNEL_COUNT);
		if (durationS != null) {
			converter.setDuration(durationS);
		}

		boolean result = converter.execute();
		if (!result) {
			throw new IOException("failed to create temp wav file.");
		}
		return tempFile;
	}

	private boolean isNeedToConvert(WavFile wavFile, Float dstDurationS) {

		int frameCount = (int) wavFile.getNumFrames();
		int samplingRate = (int) wavFile.getSampleRate();
		int channelCount = wavFile.getNumChannels();
		float srcDurationS = (float) frameCount / (float) samplingRate;

		return channelCount != CHANNEL_COUNT || samplingRate != SAMPLING_RATE || (dstDurationS != null && srcDurationS > dstDurationS);
	}

	public boolean isSupportedAudioFile(String fileName) {
		Precondition.checkNotNull(fileName, "fileName must not be null.");
		return AudioInput.isSupported(fileName) || VideoInput.isSupported(fileName);
	}

	public boolean isEnabled() {
		return !mFFmpegProcessor.isRunning();
	}

	// // // // // Interface
	// // // // //
	public static interface Listener {

		public abstract void onCompletion(double[] result);

		public abstract void onError();
	}

	// // // // // Native.
	// // // // //
	private static native double[] nativeTrack(short[] pcm, int samplingRate);
}