package com.kiwiple.multimedia.canvas;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Looper;
import android.util.Pair;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.debug.PreconditionException;
import com.kiwiple.imageanalysis.correct.collage.CollageExecuter;
import com.kiwiple.imageanalysis.correct.sticker.StickerExecuter;
import com.kiwiple.imageframework.filter.FilterManagerWrapper;
import com.kiwiple.imageframework.filter.live.LiveFilterController;
import com.kiwiple.multimedia.Version;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.preview.PreviewManager.OnPrepareListener;
import com.kiwiple.multimedia.preview.PreviewManager.OnProgressUpdateListener;
import com.kiwiple.multimedia.util.DebugUtils;
import com.kiwiple.multimedia.util.Size;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 다양한 외부 자원을 통해 의미 있는 영상을 출력하기 위해 여러 {@link VisualizerChild}에 통합된 환경을 제공하며, 이들을 제어하는 데 중추적 역할을 하는
 * 클래스.
 * 
 * @see VisualizerChild
 * @see Editor
 */
public final class Visualizer extends AbstractCanvasUser {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_NAME_VERSION = "version";
	public static final String JSON_NAME_REGIONS = "regions";
	public static final String JSON_NAME_RESOLUTION = Resolution.DEFAULT_JSON_NAME;

	static final int DEFAULT_CLEAR_COLOR = Color.BLACK;

	// // // // // Member variable.
	// // // // //
	private final Context mContext;
	private final Resources mResources;

	private final CacheManager mCacheManager;
	private final LiveFilterController mLiveFilterController;
	private final FilterManagerWrapper mFilterManager;
	private final StickerExecuter mStickerExecuter;
	private final CollageExecuter mCollageExecuter;

	private final IScriptCorrector mScriptCorrector;

	private final Region mRegion;

	private OnPrepareListener mOnPrepareListener;

	private OnProgressUpdateListener mOnProgressUpdateListener;

	private Resolution mResolution;

	private ImageCacheCreator mImageCacheCreator;

	private int mDurationPrepared;

	private boolean mPreviewMode;
	private boolean mEditMode;

	// // // // // Constructor.
	// // // // //
	{
		mScriptCorrector = (Version.current.isDev ? new ScriptCorrectorDev() : new ScriptCorrector());
	}

	public Visualizer(Context context, Resolution resolution) {
		super();
		Precondition.checkNotNull(context, resolution);

		mContext = context.getApplicationContext();
		mResources = mContext.getResources();

		mCacheManager = CacheManager.getInstance(mContext);
		mLiveFilterController = LiveFilterController.getInstance(mContext, false);
		mFilterManager = FilterManagerWrapper.getInstance(mContext);
		mStickerExecuter = StickerExecuter.getInstance(mContext);
		mCollageExecuter = CollageExecuter.getInstance(mContext);

		mResolution = resolution;
		mRegion = new Region(this);
	}

	public Visualizer(Context context, JsonObject jsonObject) throws JSONException {
		this(context, Resolution.createFrom(jsonObject.getJSONObject(JSON_NAME_RESOLUTION)));
		getEditor().start().injectJsonObject(jsonObject).finish();
	}

	// // // // // Method.
	// // // // //
	/**
	 * 현재 시간 위치에 해당하는 이미지를 그립니다.
	 * 
	 * @param dstCanvas
	 *            이미지가 그려질 {@code PixelCanvas} 객체.
	 */
	public synchronized void draw(PixelCanvas dstCanvas) {
		mRegion.draw(dstCanvas);
	}

	/**
	 * 지정한 시간 위치에 해당하는 이미지를 그립니다.
	 * 
	 * @param dstCanvas
	 *            이미지가 그려질 {@code PixelCanvas} 객체.
	 * @param position
	 *            ms 단위의 시간 위치.
	 */
	public synchronized void draw(PixelCanvas dstCanvas, int position) {
		setPosition(position);
		draw(dstCanvas);
	}

	/**
	 * 객체의 상태를 {@link JsonObject}의 형태로 변환합니다.
	 * <p />
	 * 유효한 Json 데이터가 생성된다는 것을 보장하기 위해, 본 메서드는 편집 모드 중에 사용할 수 없습니다.
	 * 
	 * @throws PreconditionException
	 *             메서드를 편집 모드 중에 호출했을 때.
	 */
	@Override
	public JsonObject toJsonObject() throws JSONException {
		Precondition.checkState(!mEditMode, "You must invoke Editor.finish() before access to toJsonObject().");

		JsonObject jsonObject = new JsonObject();
		jsonObject.put(JSON_NAME_VERSION, Version.current);
		jsonObject.put(JSON_NAME_REGIONS, mRegion);
		jsonObject.put(JSON_NAME_RESOLUTION, mResolution);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		Precondition.checkNotNull(jsonObject);

		try {
			clear();
			validateJson(jsonObject);
			updateJson(jsonObject);

			setSize(Resolution.createFrom(jsonObject.getJSONObject(JSON_NAME_RESOLUTION)));
			mRegion.injectJsonObject(jsonObject.getJSONObject(JSON_NAME_REGIONS));

		} catch (Exception exception) {
			exception.printStackTrace();
			throw exception;
		} finally {
			DebugUtils.dumpJsonObject(jsonObject, null);
		}
	}

	private void validateJson(JsonObject jsonObject) throws JSONException {

		JSONException exception = new JSONException("invalid JsonObject");
		Precondition.check(!jsonObject.isNull(JSON_NAME_VERSION), exception);
		Precondition.check(!jsonObject.isNull(JSON_NAME_RESOLUTION), exception);
		Precondition.check(!jsonObject.isNull(JSON_NAME_REGIONS), exception);

		Resolution injectedResolution = Resolution.createFrom(jsonObject.getJSONObject(JSON_NAME_RESOLUTION));
		if (!mResolution.isCompatibleWith(injectedResolution)) {
			String message = "Injected JSON script is not compatible with resolution: " + mResolution;
			throw new JSONException(message);
		}
	}

	public void updateJson(JsonObject jsonObject) throws JSONException {

		switch (mScriptCorrector.isCompatible(jsonObject)) {
			case COMPATIBLE:
				break;

			case COMPATIBLE_WITH_UPGRADE:
				mScriptCorrector.upgrade(jsonObject);
				break;

			case INCOMPATIBLE:
			default:
				throw new JSONException("Injected JSON script is not compatible with MultimediaFramework version.");
		}
	}

	@Override
	public Size getSize() {
		return mResolution.getSize();
	}

	/**
	 * 크기 정보를 {@link Resolution}으로써 반환합니다.
	 */
	public Resolution getResolution() {
		return mResolution;
	}

	@Override
	public int getDuration() {
		return mRegion.getDuration();
	}

	@Override
	public int getPosition() {
		return mRegion.getPosition();
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	public void release() {

		mOnPrepareListener = null;
		mOnProgressUpdateListener = null;
		clear();
		mRegion.release();
	}

	/**
	 * {@code Visualizer}를 구성하는 {@link Region}을 반환합니다.
	 */
	public Region getRegion() {
		return mRegion;
	}

	DetailChange<String> d = new DetailChange<>(Change.DURATION);

	void setSize(Resolution resolution) {
		Precondition.checkNotNull(resolution);
		Precondition.checkState(mResolution.isCompatibleWith(resolution), "resolution is not compatible with mResolution.");

		float changeRatio = resolution.magnification / mResolution.magnification;
		mResolution = resolution;
		mRegion.setSize(resolution.getSize());

		notifyChange(Change.SIZE);
		notifyChange(Change.RESOLUTION, Pair.create(DetailChange.MAGNIFICATION_CHANGE_RATIO, changeRatio));
	}

	/**
	 * 출력할 이미지의 시간 위치를 ms 단위로써 설정합니다.
	 * <p />
	 * 본 메서드는 편집 모드 중에 사용할 수 없습니다.
	 * 
	 * @param position
	 *            ms 단위의 시간 위치.
	 * @throws PreconditionException
	 *             메서드를 편집 모드 중에 호출했을 때.
	 */
	public synchronized void setPosition(int position) {
		Precondition.checkState(!mEditMode, "You must invoke Editor.finish() before access to drawing stuff.");

		if (position < 0) {
			position = 0;
		} else if (mPreviewMode && position > mDurationPrepared) {
			position = mDurationPrepared;
		}

		int duration = getDuration();
		if (position > duration) {
			position = duration;
		}
		mRegion.setPosition(position);
	}

	public synchronized void offsetPosition(int offset) {
		setPosition(getPosition() + offset);
	}

	/**
	 * 현재 시간 위치로부터 시간 축의 진행이 더 가능한지의 여부를 반환합니다.
	 * 
	 * @return 진행이 가능하다면 {@code true}.
	 */
	public boolean hasNextFrame() {
		return getPosition() < (mPreviewMode ? mDurationPrepared : getDuration());
	}

	public boolean isEmpty() {
		return mRegion.isEmpty();
	}

	/**
	 * 프리뷰 모드일 때, 프리뷰 사전 준비 진척도를 전달받기 위한 리스너를 설정합니다.
	 * 
	 * @param onPrepareListener
	 *            설정할 {@code OnPrepareListener} 객체.
	 */
	public void setPrepareListener(OnPrepareListener onPrepareListener) {

		mOnPrepareListener = onPrepareListener;

		if (onPrepareListener == null) {
			return;
		}
		if (!mRegion.isEmpty() && getDuration() == mDurationPrepared) {
			onPrepareListener.onComplete();
		}
	}

	public void setProgressUpdateListener(OnProgressUpdateListener onProgressUpdateListener) {

		mOnProgressUpdateListener = onProgressUpdateListener;

		if (onProgressUpdateListener == null) {
			return;
		}
	}



	void setEditMode(boolean editMode) {
		Precondition.checkState(editMode != mEditMode, "Visualizer is " + (editMode ? "on Edit mode already." : "not on edit mode."));

		if (editMode) {
			if (mPreviewMode)
				interruptImageCacheCreator();
			mRegion.clearFocusState();
			mEditMode = true;

		} else {
			Changes changes = new Changes();
			changes.update(this);
			mRegion.validate(changes);

			clearChanges();
			mEditMode = false;

			if (mPreviewMode && mRegion.isValidated())
				prepareImageCache();
		}
	}

	void reportVaidationError(VisualizerChild visualizerChild, String message) {
		String reporter = visualizerChild.getClass().getSimpleName();
		L.e(reporter + " : " + message);
	}

	@Override
	public boolean isOnEditMode() {
		return mEditMode;
	}

	void setPreviewMode(boolean previewMode) {
		mPreviewMode = previewMode;
	}

	/**
	 * 프리뷰에 최적화된 상태인지의 여부를 반환합니다.
	 * 
	 * @return 프리뷰 모드일 때 true.
	 */
	public boolean isOnPreviewMode() {
		return mPreviewMode;
	}

	/**
	 * {@code Visualizer}를 구성하는 모든 {@link VisualizerChild} 정보를 제거합니다.
	 */
	public void clear() {

		interruptImageCacheCreator();
		mDurationPrepared = 0;
		mRegion.clear();
	}

	public void prepareImageCache() {

		interruptImageCacheCreator();
		mImageCacheCreator = new ImageCacheCreator();
		mImageCacheCreator.start();
	}

	public void interruptImageCacheCreator() {

		if (mImageCacheCreator != null) {
			try {
				mImageCacheCreator.interrupt();
				mImageCacheCreator.join();
			} catch (InterruptedException exception) {
				exception.printStackTrace();
			}
		}
	}


	Context getContext() {
		return mContext;
	}

	Resources getResources() {
		return mResources;
	}

	CacheManager getCacheManager() {
		return mCacheManager;
	}

	LiveFilterController getLiveFilterController() {
		return mLiveFilterController;
	}

	FilterManagerWrapper getFilterManager() {
		return mFilterManager;
	}

	StickerExecuter getStickerExecuter() {
		return mStickerExecuter;
	}

	CollageExecuter getCollageExecuter() {
		return mCollageExecuter;
	}

	/**
	 * 새로운 {@link FilterApplier}를 생성하여 반환합니다.
	 */
	FilterApplier createFilterApplier(int filterId) {
		return FilterApplier.create(mLiveFilterController, mFilterManager, filterId);
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link Visualizer}의 일부 기능을 조작하기 위한 클래스.
	 * <p />
	 * {@code Visualizer} 및 {@link VisualizerChild}의 {@code Editor}가 제공하는 모든 메서드는 {@code Visualizer}
	 * 가 편집 모드일 때에만 사용할 수 있으며, 그렇지 않은 경우에 접근을 시도했을 때에는 {@link PreconditionException}이 발생하게 되어 있습니다.
	 * 한 가지 예외로, {@link #start()}는 편집 모드가 아닐 때에만 사용할 수 있으며, 이를 통해 {@code Visualizer}를 편집 모드로 전환할 수
	 * 있습니다.
	 * <p />
	 * {@code Visualizer}가 편집 모드일 때에는 다음 기능에 대한 사용이 제한됩니다.
	 * <ul>
	 * <li>{@link Visualizer#draw(PixelCanvas)}</li>
	 * <li>{@link Visualizer#draw(PixelCanvas, int)}</li>
	 * <li>{@link Visualizer#setPosition(int)}</li>
	 * </ul>
	 * 한편, 모든 편집 내용을 완전히 반영하고, 제한되었던 {@code Visualizer}의 기능을 다시 사용하기 위해서는 {@link #finish()}를 호출함으로써
	 * 편집 모드를 종료해야 합니다.
	 */
	public static final class Editor extends AbstractCanvasUser.Editor<Visualizer, Editor> {

		private static interface Starter {
			abstract void power();
		}

		private final Starter mStarter;

		private Editor(final Visualizer visualizer) {
			super(visualizer);

			mStarter = new Starter() {

				@Override
				public void power() {
					visualizer.setEditMode(true);
				}
			};
		}

		/**
		 * {@code Visualizer}의 프리뷰 모드를 설정합니다.
		 * 
		 * @param previewMode
		 *            프리뷰 모드를 사용한다면 true.
		 */
		public Editor setPreviewMode(boolean previewMode) {
			getObject().setPreviewMode(previewMode);
			return this;
		}

		/**
		 * Json 형식의 문자열을 통해 {@code Visualizer}를 구성합니다.
		 * 
		 * @param jsonScript
		 *            객체의 상태 정보를 담은 Json 형식의 문자열.
		 * @throws JSONException
		 *             org.json API 사용 중에 오류가 발생했을 때.
		 * @see Visualizer#toJsonObject()
		 */
		public Editor injectJsonScript(String jsonScript) throws JSONException {
			getObject().injectJsonObject(new JsonObject(jsonScript));
			return this;
		}

		/**
		 * {@link JsonObject}를 통해 {@code Visualizer}를 구성합니다.
		 * 
		 * @param jsonObject
		 *            객체의 상태 정보를 담은 {@code JsonObject} 객체.
		 * @throws JSONException
		 *             org.json API 사용 중에 오류가 발생했을 때.
		 * @see Visualizer#toJsonObject()
		 */
		public Editor injectJsonObject(JsonObject jsonObject) throws JSONException {
			getObject().injectJsonObject(jsonObject);
			return this;
		}

		/**
		 * {@link Resolution}을 통해 크기를 조정합니다.
		 * 
		 * @param resolution
		 *            크기 정보를 담은 {@code Resolution} 객체.
		 */
		Editor setSize(Resolution resolution) {
			getObject().setSize(resolution);
			return this;
		}

		/**
		 * 편집 모드를 시작합니다.
		 */
		public Editor start() {
			mStarter.power();
			return this;
		}

		/**
		 * 편집 모드를 종료합니다.
		 */
		public void finish() {
			getObject().setEditMode(false);
		}


	}

	private class ImageCacheCreator extends Thread {

		private final List<Scene> scenes;
		private final List<Transition> transitions;
		private final int lastSceneIndex;

		{
			scenes = mRegion.getScenes();
			transitions = mRegion.getTransitions();
			lastSceneIndex = scenes.size() - 1;
		}

		@Override
		public void run() {
			super.run();

			if (Looper.myLooper() == null)
				Looper.prepare();
			for (int index = 0; index <= lastSceneIndex; ++index) {

				if (isInterrupted())
					break;
				for (RegionChild child : createListOfBoundTo(index)) {
					if (child.isCacheable() && child.isValidated()) {
						try {
							child.prepareCacheFile();
						} catch (IOException exception) {
							exception.printStackTrace();
							child.setValidated(false);
						}
					}
				}
				mDurationPrepared = mRegion.getSceneEndPosition(index);
				if (mOnPrepareListener != null) {
					mOnPrepareListener.onPrepare(index, mDurationPrepared);
				}
			}

			if (!isInterrupted()) {
//				Log.e("###### finish", "Finish progress, scene size:" + lastSceneIndex);
				mOnProgressUpdateListener.onFinish();
			}
			Looper.myLooper().quit();
		}

		private ArrayList<RegionChild> createListOfBoundTo(int index) {

			ArrayList<RegionChild> list = scenes.get(index).createChildList(true, true);
			if (index != lastSceneIndex)
				list.addAll(transitions.get(index).createChildList(true, true));
			return list;
		}
	}
}