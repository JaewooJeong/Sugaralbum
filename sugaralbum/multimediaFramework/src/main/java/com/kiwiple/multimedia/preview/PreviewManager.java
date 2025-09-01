package com.kiwiple.multimedia.preview;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.net.Uri;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.ResourceType;
import com.kiwiple.multimedia.canvas.CacheManager;
import com.kiwiple.multimedia.canvas.PixelCanvas;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.multimedia.canvas.Visualizer;
import com.kiwiple.multimedia.json.JsonObject;

import org.json.JSONException;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * {@link Visualizer}가 생성하는 이미지를 시간 주기에 맞추어 화면에 재생하고, 음원의 동기화를 제어하기 위한 클래스.
 * 
 * @see PreviewSurfaceView
 */
public class PreviewManager {

	// // // // // Static variable.
	// // // // //
	public static final Resolution DEFAULT_PREVIEW_RESOLUTION = Resolution.NHD;

	public static final String JSON_NAME_AUDIO = "audio";
	public static final String JSON_NAME_AUDIO_FILE_PATH = "file_path";
	public static final String JSON_NAME_AUDIO_IS_ASSET = "is_asset";
	public static final String JSON_NAME_AUDIO_RESOURCE_TYPE = ResourceType.DEFAULT_JSON_NAME;

	public static final int UPDATE_INTERVAL = 33;

	private static final int SOUND_FADE_MAX = 3000;
	private static final int INT_VOLUME_MAX = 100;
	private static final int INT_VOLUME_MIN = 0;
	private static final float FLOAT_VOLUME_MAX = 1;
	private static final float FLOAT_VOLUME_MIN = 0;

	private static PreviewManager sInstance;

	// // // // // Member variable.
	// // // // //
	private Context mContext;

	private final Visualizer mVisualizer;
	private final BufferPool mPreviewBufferPool;
	private final SyncTimer mSyncTimer;

	private final PreviewController mPreviewController = new PreviewController();

	private PreviewSurfaceView mPreview;

	private MediaPlayer mAudioPlayer;
	private String mAudioFilePath;
	private ResourceType mAudioResourceType;
	private int mAudioDuration;

	private boolean mIsVisualizerPrepared = false;

	private int mVolumeGauge;
	private Timer mFadeTimer;

	private boolean mIsRunningFadeOut;
	private boolean mIsFadeSoundEnabled;

	// // // // // Static method.
	// // // // //
	/**
	 * 유일한 {@code PreviewManager} 객체를 반환합니다.
	 * 
	 * @param context
	 *            애플리케이션 전역 정보를 참조를 위한 {@code Context} 객체.
	 */
	public static synchronized PreviewManager getInstance(Context context) {
 		Precondition.checkNotNull(context);

		if (sInstance == null) {
			sInstance = new PreviewManager(context);
		}
		return sInstance;
	}

	// // // // // Constructor.
	// // // // //
	private PreviewManager(Context context) {

		mContext = context.getApplicationContext();

		mVisualizer = new Visualizer(context, DEFAULT_PREVIEW_RESOLUTION);
		mVisualizer.getEditor().start().setPreviewMode(true).finish();

		mPreviewBufferPool = new BufferPool(DEFAULT_PREVIEW_RESOLUTION.getSize());
		mSyncTimer = new SyncTimer(UPDATE_INTERVAL);
	}

	// // // // // Method.
	// // // // //
	/**
	 * 객체의 상태를 {@link JsonObject}의 형태로 변환합니다.
	 * 
	 * @throws JSONException
	 *             org.json API 사용 중에 오류가 발생했을 때.
	 * @see #injectJsonObject(JsonObject)
	 */
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = mVisualizer.toJsonObject();

		if (mAudioFilePath != null && !mAudioFilePath.isEmpty()) {
			JsonObject audioJsonObject = new JsonObject();
			audioJsonObject.put(JSON_NAME_AUDIO_FILE_PATH, mAudioFilePath);
			audioJsonObject.put(JSON_NAME_AUDIO_RESOURCE_TYPE, mAudioResourceType);
			jsonObject.put(JSON_NAME_AUDIO, audioJsonObject);
		}
		return jsonObject;
	}

	/**
	 * 이미지를 출력할 {@link PreviewSurfaceView}를 등록하고, 프리뷰의 재생 및 준비 상태에 대한 신호를 받기 위한 리스너를 설정합니다.
	 * 
	 * @param preview
	 *            이미지를 출력할 {@code PreviewSurfaceView} 객체.
	 * @param onUpdateListener
	 *            프리뷰의 재생 상태에 대한 신호를 받기 위한 리스너.
	 * @param onPrepareListener
	 *            프리뷰의 준비 상태에 대한 신호를 받기 위한 리스너.
	 * @see PreviewManager.OnUpdateListener
	 * @see PreviewManager.OnPrepareListener
	 */
	public void bindPreview(PreviewSurfaceView preview, OnUpdateListener onUpdateListener, OnPrepareListener onPrepareListener, OnProgressUpdateListener onProgressUpdateListener) {
		Precondition.checkNotNull(preview);

		mPreview = preview;
		mPreview.bindBufferPool(mPreviewBufferPool);

		setOnUpdateListener(onUpdateListener);
		mVisualizer.setPrepareListener(onPrepareListener);
		mVisualizer.setProgressUpdateListener(onProgressUpdateListener);

		mPreviewBufferPool.initializeBuffer();
		mPreviewBufferPool.getWriteBufferWithLock();
		mPreviewBufferPool.unlockWrittenBuffer();
		mPreview.requestInvalidate();
	}

	/**
	 * Json 데이터를 문자열로 담고 있는 {@link String}을 통해 객체를 구성합니다.
	 * 
	 * @param jsonScript
	 *            객체의 상태 정보를 Json의 형태로 담은 {@code String} 객체.
	 * @throws JSONException
	 *             org.json API 사용 중에 오류가 발생했을 때.
	 * @see #toJsonObject()
	 */
	public void injectJsonScript(String jsonScript) throws JSONException {
		Precondition.checkNotNull(jsonScript);
		injectJsonObject(new JsonObject(jsonScript));
	}

	/**
	 * {@link JsonObject}를 통해 객체를 구성합니다.
	 * 
	 * @param jsonObject
	 *            객체의 상태 정보를 담은 {@code JsonObject} 객체.
	 * @throws JSONException
	 *             org.json API 사용 중에 오류가 발생했을 때.
	 * @see #toJsonObject()
	 */
	public void injectJsonObject(JsonObject jsonObject) throws JSONException {
		Precondition.checkState(mVisualizer.isOnEditMode(), "Invoke Visualizer.Editor.start() first.");
		Precondition.checkNotNull(jsonObject);

		mPreviewController.pausePreview();
		mVisualizer.updateJson(jsonObject);

		if (!jsonObject.isNull(JSON_NAME_AUDIO)) {

			JsonObject audioJsonObject = jsonObject.getJSONObject(JSON_NAME_AUDIO);
			String audioFilePath = audioJsonObject.getString(JSON_NAME_AUDIO_FILE_PATH);
			ResourceType audioResourceType = audioJsonObject.getEnum(JSON_NAME_AUDIO_RESOURCE_TYPE, ResourceType.class);

			setAudioFile(audioFilePath, audioResourceType.equals(ResourceType.ANDROID_ASSET));
		}

		mVisualizer.getEditor().injectJsonObject(jsonObject);
		mIsVisualizerPrepared = true;
	}

	public void setAudioPathAndResourceType(String audioFilePath, ResourceType rType) {
		mAudioFilePath = audioFilePath;
		mAudioResourceType = rType;
	}

	/**
	 * 프리뷰와 동기화할 음원 파일의 경로를 설정합니다.
	 * 
	 * @param audioFilePath
	 *            음원 파일의 경로.
	 * @param isAssetAudio
	 *            안드로이드 패키지의 Assets에 소속되어 있는 음원 파일이라면 {@code true}.
	 */
	public void setAudioFile(String audioFilePath, boolean isAssetAudio) {

		mAudioFilePath = audioFilePath;
		if (mAudioFilePath == null || mAudioFilePath.isEmpty()) {
			return;
		}

		try {
			if (mAudioPlayer == null) {
				mAudioPlayer = new MediaPlayer();
			}
			mAudioPlayer.reset();

			if (isAssetAudio) {
				AssetFileDescriptor assetFileDescriptor = mContext.getAssets().openFd(audioFilePath);
				FileDescriptor fd = assetFileDescriptor.getFileDescriptor();
				mAudioPlayer.setDataSource(fd, assetFileDescriptor.getStartOffset(), assetFileDescriptor.getDeclaredLength());
				assetFileDescriptor.close();
				mAudioResourceType = ResourceType.ANDROID_ASSET;
			} else {
				File audioFile = new File(mAudioFilePath);
				mAudioPlayer.setDataSource(mContext, Uri.fromFile(audioFile));
				mAudioResourceType = ResourceType.FILE;
			}

			mAudioPlayer.setLooping(true);
			mAudioPlayer.prepare();
			mAudioDuration = mAudioPlayer.getDuration();
			
			// Ensure mAudioDuration is valid
			if (mAudioDuration <= 0) {
				mAudioDuration = 1; // Set minimum duration to prevent divide by zero
			}

		} catch (IOException exception) {
			exception.printStackTrace();
			// Set default duration if audio loading fails
			mAudioDuration = 1000; // 1 second default
		}
	}

	/**
	 * 이미지의 생성을 담당하는 {@code Visualizer}를 반환합니다.
	 */
	public Visualizer getVisualizer() {
		return mVisualizer;
	}

	/**
	 * 프리뷰를 제어하기 위한 {@link PreviewController}를 반환합니다.
	 */
	public PreviewController getPreviewController() {
		return mPreviewController;
	}

	/**
	 * 프리뷰와 동기화한 음원 파일의 경로를 반환합니다.
	 */
	public String getAudioFilePath() {
		return mAudioFilePath;
	}

	/**
	 * 프리뷰와 동기화한 음원 파일이 안드로이드 패키지의 Assets에 소속되어 있는지의 여부를 반환합니다.
	 * 
	 * @return 안드로이드 패키지의 Assets에 소속되어 있다면 {@code true}.
	 */
	public boolean isAssetAudioFile() {
		return mAudioResourceType == ResourceType.ANDROID_ASSET;
	}

	public void setAudioFade(boolean fade) {
		mIsFadeSoundEnabled = fade;
	}

	public boolean isUsingAudioFade() {
		return mIsFadeSoundEnabled;
	}

	/**
	 * 프리뷰를 중단하고, 모든 설정 정보를 초기화합니다.
	 */
	public void clear() {

		mIsRunningFadeOut = false;
		mSyncTimer.stop();
		mVisualizer.clear();
		if (mAudioPlayer != null) {
			mAudioPlayer.stop();
			mAudioPlayer.release();
			mAudioPlayer = null;
		}
	}

	/**
	 * 명시적으로 자원을 해제합니다.
	 */
	public void release() {

		clear();
		mVisualizer.release();
		CacheManager.getInstance(mContext).release();
		sInstance = null;
	}

	private void setOnUpdateListener(final OnUpdateListener onUpdateListener) {

		SyncTimer.OnUpdateListener timerUpdateListener = new SyncTimer.OnUpdateListener() {

			@Override
			public void onUpdate() {

				if (mVisualizer.isOnEditMode() || !mVisualizer.hasNextFrame()) {
					mSyncTimer.stop();
					if (mAudioPlayer != null && mAudioPlayer.isPlaying())
						mAudioPlayer.pause();
					onUpdateListener.onEnd();
				}

				if (mIsFadeSoundEnabled)
					checkSoundFadeOut(mVisualizer.getPosition(), mVisualizer.getDuration());

				PixelCanvas buffer = mPreviewBufferPool.getWriteBufferWithLock();

				if (buffer != null) { // FIXME: something.
					synchronized (buffer) {
						mVisualizer.offsetPosition(UPDATE_INTERVAL);
						mVisualizer.draw(buffer);
						mPreviewBufferPool.unlockWrittenBuffer();
						mPreview.requestInvalidate();
						onUpdateListener.onUpdate();
					}
				} else {
					L.w("buffer is null");
					mPreviewBufferPool.unlockWrittenBuffer();
					mPreviewBufferPool.unlockReadBuffer();
				}

			}
		};

		mSyncTimer.setOnUpdateListener(timerUpdateListener);
	}

	private void cancelSoundFadeTimer() {
		if (mFadeTimer != null) {
			mFadeTimer.cancel();
			mFadeTimer.purge();
			mFadeTimer = null;
		}
		mIsRunningFadeOut = false;
	}

	private void checkSoundFadeIn(int currentPositionMs, int durationMs) {

		if (currentPositionMs >= 0 && currentPositionMs < SOUND_FADE_MAX) {
			int fadeDuration = SOUND_FADE_MAX - mVisualizer.getPosition();
			playSoundFadeIn(fadeDuration);
		}
	}

	private void checkSoundFadeOut(int currentPositionMs, int durationMs) {

		if (mIsRunningFadeOut)
			return;

		// Ending scene에 fade 적용 후 100ms 이내의 duration 남을 경우 skip
		if (currentPositionMs >= durationMs - SOUND_FADE_MAX && currentPositionMs < durationMs) {
			mIsRunningFadeOut = true;
			int fadeDuration = durationMs - currentPositionMs;
			if (fadeDuration < 100)
				return;
			playSoundFadeOut(fadeDuration);
		}
	}

	private void restoreDefaultVolume() {
		if (mAudioPlayer != null) {
			mAudioPlayer.setVolume(1.f, 1.f);
		}
	}

	private void playSoundFadeIn(int fadeDuration) {
		if (fadeDuration > 0)
			mVolumeGauge = INT_VOLUME_MIN;
		else
			mVolumeGauge = INT_VOLUME_MAX;

		updateVolume(0);

		if (fadeDuration > 0) {
			mFadeTimer = new Timer(false);
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					updateVolume(1);
					if (mVolumeGauge == INT_VOLUME_MAX) {
						cancelSoundFadeTimer();
					}
				}
			};

			// 타이머 스케쥴링 시 delay 추가
			int delay = fadeDuration / INT_VOLUME_MAX;
			if (delay == 0)
				delay = 1;

			mFadeTimer.schedule(timerTask, delay, delay);
		}
	}

	private void playSoundFadeOut(int fadeDuration) {
		if (fadeDuration > 0)
			mVolumeGauge = INT_VOLUME_MAX;
		else
			mVolumeGauge = INT_VOLUME_MIN;

		updateVolume(0);

		if (fadeDuration > 0) {
			mFadeTimer = new Timer(false);
			TimerTask timerTask = new TimerTask() {
				@Override
				public void run() {
					updateVolume(-1);
					if (mVolumeGauge == INT_VOLUME_MIN) {
						cancelSoundFadeTimer();
					}
				}
			};

			int delay = fadeDuration / INT_VOLUME_MAX;
			if (delay == 0)
				delay = 1;

			mFadeTimer.schedule(timerTask, delay, delay);
		}
	}

	private void updateVolume(int change) {
		mVolumeGauge += change;

		if (mVolumeGauge < INT_VOLUME_MIN)
			mVolumeGauge = INT_VOLUME_MIN;
		else if (mVolumeGauge > INT_VOLUME_MAX)
			mVolumeGauge = INT_VOLUME_MAX;

		float fVolume = 1 - ((float) Math.log(INT_VOLUME_MAX - mVolumeGauge) / (float) Math.log(INT_VOLUME_MAX));

		if (fVolume < FLOAT_VOLUME_MIN)
			fVolume = FLOAT_VOLUME_MIN;
		else if (fVolume > FLOAT_VOLUME_MAX)
			fVolume = FLOAT_VOLUME_MAX;

		if (mAudioPlayer != null && mAudioPlayer.isPlaying())
			mAudioPlayer.setVolume(fVolume, fVolume);
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * 프리뷰를 제어하기 위한 클래스.
	 */
	public final class PreviewController {

		PreviewController() {
			// Hide constructor and do nothing.
		}

		/**
		 * 프리뷰 화면을 새롭게 그립니다.
		 */
		public void invalidate() {

			if (isEnabled()) {
				setPosition(mVisualizer.getPosition());
			}
		}

		/**
		 * 특정 시간 위치에 해당하는 프리뷰 화면을 그립니다.
		 * 
		 * @param positionMs
		 *            ms 단위의 시간 위치.
		 */
		public void setPosition(int positionMs) {

			if (isEnabled()) {

				mVisualizer.setPosition(positionMs);
				PixelCanvas buffer = mPreviewBufferPool.getWriteBufferWithLock();

				if (buffer != null) { // FIXME: at sometime.
					mVisualizer.draw(buffer);
					mPreviewBufferPool.unlockWrittenBuffer();
					mPreview.requestRender();
				} else {
					L.w("buffer is null");
					mPreviewBufferPool.unlockWrittenBuffer();
					mPreviewBufferPool.unlockReadBuffer();
				}
			}
		}

		/**
		 * 프리뷰를 재생합니다.
		 */
		public void startPreview() {
			if (isEnabled()) {
				if (mAudioPlayer != null && mAudioDuration > 0) {
					mAudioPlayer.seekTo(mVisualizer.getPosition() % mAudioDuration);
					mAudioPlayer.start();
				}

				if (mIsFadeSoundEnabled) {
					checkSoundFadeIn(mVisualizer.getPosition(), mVisualizer.getDuration());
					checkSoundFadeOut(mVisualizer.getPosition(), mVisualizer.getDuration());
				}

				mSyncTimer.start();
			}
		}

		/**
		 * 프리뷰 재생을 중단합니다.
		 */
		public void pausePreview() {
			if (isEnabled()) {

				try { // 임시 예외처리.. illegalException
					if (mAudioPlayer != null && mAudioPlayer.isPlaying())
						mAudioPlayer.pause();
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (mIsFadeSoundEnabled) {
					cancelSoundFadeTimer();
					restoreDefaultVolume();
				}

				mSyncTimer.stop();
			}
		}

		private boolean isEnabled() {
			return mIsVisualizerPrepared && !mVisualizer.isOnEditMode() && sInstance != null;
		}
	}

	// // // // // Interface
	// // // // //
	/**
	 * 프리뷰의 준비 상태에 대한 신호를 받기 위한 리스너 클래스.
	 */
	public static interface OnPrepareListener {

		/**
		 * 프리뷰의 준비가 부분적으로 완료되었을 때 호출됩니다.
		 * 
		 * @param index
		 *            준비가 완료된 부분에 해당하는 첨자.
		 * @param durationPreparedMs
		 *            준비가 완료된 ms 단위의 시간 위치.
		 */
		public abstract void onPrepare(int index, int durationPreparedMs);

		/**
		 * 프리뷰의 준비가 완료되었을 때 호출됩니다.
		 */
		public abstract void onComplete();
	}

	/**
	 * 프리뷰의 재생 상태에 대한 신호를 받기 위한 리스너 클래스.
	 *
	 */
	public static interface OnUpdateListener {

		/**
		 * 프리뷰 재생 중, 다음 시간 위치로 이동하여 화면이 갱신되었을 때 호출됩니다.
		 */
		public abstract void onUpdate();

		/**
		 * 시간 축의 끝에 도달하여 프리뷰 재생이 완료되었을 때 호출됩니다.
		 */
		public abstract void onEnd();
	}


	public static interface OnProgressUpdateListener {

		public abstract void onFinish();
	}
}