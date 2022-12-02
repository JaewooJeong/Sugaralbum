package com.kiwiple.multimedia.canvas;

import static com.kiwiple.multimedia.Constants.INVALID_INDEX;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.annotation.Child;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Randomizer;
import com.kiwiple.multimedia.util.Range;
import com.kiwiple.multimedia.util.Size;

/**
 * CollectionScene.
 */
public final class CollectionScene extends Scene {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "collection_scene";

	public static final String JSON_NAME_INDEXES = "indexes";
	public static final String JSON_NAME_TRANSITIONS = "transitions";
	public static final String JSON_NAME_TRANSITION_ORDER = "transition_order";
	public static final String JSON_NAME_CONCURRENT_SCENE_COUNT = "concurrent_scene_count";
	private static final String JSON_NAME_INTERNAL_SEED = "internal_seed";

	public static final List<Class<? extends Scene>> UNSUPPORTED_SCENES;

	private static final int DEFAULT_CONCURRENT_SCENE_COUNT = 3;
	private static final TransitionOrder DEFAULT_TRANSITION_ORDER = TransitionOrder.CIRCULAR_LIST;

	// // // // // Member variable.
	// // // // //
	private final Region mRegion;

	private final CacheManager mCacheManager;

	private final ArrayList<Integer> mSceneIndexes = new ArrayList<>();
	@CacheCode(indexed = true)
	private final ArrayList<Scene> mScenes = new ArrayList<>();
	@CacheCode(indexed = true)
	private final ArrayList<String> mScalerCacheCodeChunks = new ArrayList<>();
	@Child
	private final ArrayList<Transition> mTransitions = new ArrayList<>();

	private HashMap<Scene, PixelCanvas> mSceneCanvasMap = new HashMap<>();
	private ArrayDeque<PixelCanvas> mCanvases = new ArrayDeque<>();
	private ArrayList<Scene> mScenesToDraw = new ArrayList<Scene>();
	private Range[] mSceneRanges;

	private TransitionOrder mTransitionOrder;
	private Randomizer mRandomizer;

	private Size[] mCanvasRequirement;

	private int mConcurrentSceneCount;
	private int mFirstSceneIndex = INVALID_INDEX;
	private int mLastSceneIndex = INVALID_INDEX;
	private int mTempCanvasIndex = INVALID_INDEX;

	// // // // // Constructor.
	// // // // //
	static {
		ArrayList<Class<? extends Scene>> unsupportedScenes = new ArrayList<>();
		unsupportedScenes.add(CollectionScene.class);
		unsupportedScenes.add(VideoFileScene.class);

		UNSUPPORTED_SCENES = Collections.unmodifiableList(unsupportedScenes);
	}

	{
		mCacheManager = getCacheManager();
		mRandomizer = new Randomizer();

		setConcurrentSceneCount(DEFAULT_CONCURRENT_SCENE_COUNT);
		setTransitionOrder(DEFAULT_TRANSITION_ORDER);
	}

	CollectionScene(Region parent) {
		super(parent);
		mRegion = parent;
	}

	CollectionScene(MultiLayerScene parent) {
		super(parent);
		throw new UnsupportedOperationException();
	}

	// // // // // Method.
	// // // // //
	@Override
	void onDraw(PixelCanvas dstCanvas) {

		try {
			organizeSequence();
		} catch (IOException exception) {
			L.printStackTrace(exception);
			setValidated(false);
			return;
		}

		if (mFirstSceneIndex == mLastSceneIndex) {
			PixelCanvas source = mSceneCanvasMap.get(mScenes.get(mFirstSceneIndex));
			source.copy(dstCanvas);
		} else {

			PixelCanvas former = null;
			PixelCanvas latter = null;
			PixelCanvas tempCanvas = getCanvas(mTempCanvasIndex);

			for (int i = mFirstSceneIndex; i < mLastSceneIndex; ++i) {

				float transitionDuration = mSceneRanges[i].end - mSceneRanges[i + 1].start;
				float transitionPosition = getPosition() - mSceneRanges[i + 1].start;
				int tPosition = Math.round(transitionPosition / transitionDuration * 1000.0f);

				int transitionIndex = (mTransitionOrder.isRandom() ? mRandomizer.randomizeAbs(i) : i) % mTransitions.size();
				Transition transition = mTransitions.get(transitionIndex);
				transition.setPosition(tPosition);

				boolean isFirst = (i == mFirstSceneIndex);
				boolean isLast = (i == mLastSceneIndex - 1);

				former = isFirst ? mSceneCanvasMap.get(mScenes.get(i)) : tempCanvas;
				latter = mSceneCanvasMap.get(mScenes.get(i + 1));
				transition.draw(former, latter, isLast ? dstCanvas : tempCanvas);
			}
		}
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	Change[] getSensitivities() {
		return ALWAYS_SENSITIVE;
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {

		mScenes.clear();
		mScalerCacheCodeChunks.clear();

		checkValidity(!mSceneIndexes.isEmpty(), "You must invoke setCollection()");
		checkValidity(!mTransitions.isEmpty(), "You must invoke setTransitions()");

		List<Scene> scenes = mRegion.getScenes();
		for (int index : mSceneIndexes) {

			Scene scene = scenes.get(index);
			ArrayList<RegionChild> children = scene.createChildList(true, true);

			for (RegionChild child : children) {
				Class<? extends RegionChild> type = child.getClass();

				String message = type.getSimpleName() + " cannot be part of collection.";
				checkValidity(!UNSUPPORTED_SCENES.contains(type), message);
			}
			mScenes.add(scene);

			StringBuilder cacheCodeBuilder = new StringBuilder();
			for (RegionChild child : children)
				if (child instanceof Scaler)
					cacheCodeBuilder.append(child.getCacheCodeChunk(0));
			mScalerCacheCodeChunks.add(cacheCodeBuilder.length() > 0 ? cacheCodeBuilder.toString() : null);
		}

		mCanvasRequirement = new Size[mConcurrentSceneCount + 1];
		Arrays.fill(mCanvasRequirement, getSize());
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_INDEXES, new JsonArray(mSceneIndexes));
		jsonObject.put(JSON_NAME_CONCURRENT_SCENE_COUNT, mConcurrentSceneCount);
		jsonObject.put(JSON_NAME_INTERNAL_SEED, mRandomizer.getSeed());
		jsonObject.put(JSON_NAME_TRANSITIONS, mTransitions);
		jsonObject.put(JSON_NAME_TRANSITION_ORDER, mTransitionOrder);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		for (JsonObject transitionJsonObject : jsonObject.optJSONArrayAsList(JSON_NAME_TRANSITIONS, JsonObject.class)) {
			addTransition(CanvasUserFactory.createTransition(transitionJsonObject, mRegion));
		}
		setCollection(jsonObject.optJSONArrayAsList(JSON_NAME_INDEXES, Integer.class));
		setTransitionOrder(jsonObject.getEnum(JSON_NAME_TRANSITION_ORDER, TransitionOrder.class));
		mRandomizer.setSeed(jsonObject.getInt(JSON_NAME_INTERNAL_SEED));
	}

	@Override
	void onPrepare() {

		for (int i = 0; i != mConcurrentSceneCount; ++i) {
			PixelCanvas canvas = getCanvas(i);
			mCanvases.push(canvas);
		}
	}

	@Override
	void onUnprepare() {

		mFirstSceneIndex = INVALID_INDEX;
		mLastSceneIndex = INVALID_INDEX;
		mCanvases.clear();
		mScenesToDraw.clear();
		mSceneCanvasMap.clear();
	}

	@Override
	public int getCacheCount() {
		return mScenes.size();
	}

	@Override
	Bitmap createCacheAsBitmap(int index) throws IOException {
		Scene scene = mScenes.get(index);
		return extractPixelCanvas(scene).createBitmap();
	}

	@Override
	void prepareCanvasWithCache() throws IOException {

		if (mFirstSceneIndex == INVALID_INDEX) {
			return;
		}
		for (int i = mFirstSceneIndex; i <= mLastSceneIndex; ++i) {
			Scene scene = mScenes.get(i);
			mCacheManager.decodeImageCache(getCacheCodeChunk(i), mSceneCanvasMap.get(scene));
		}
	}

	@Override
	void prepareCanvasWithoutCache() throws IOException {

		if (mFirstSceneIndex == INVALID_INDEX) {
			return;
		}
		for (int i = mFirstSceneIndex; i <= mLastSceneIndex; ++i) {
			Scene scene = mScenes.get(i);
			PixelExtractUtils.extractARGB(createCacheAsBitmap(i), mSceneCanvasMap.get(scene), true);
		}
	}

	private PixelCanvas extractPixelCanvas(Scene scene) throws IOException {

		if (scene instanceof MultiLayerScene) {
			return extractPixelCanvas((MultiLayerScene) scene);
		}

		PixelCanvas srcCanvas = null;

		if (scene.isCacheable()) {
			if (scene.isCacheFilePrepared()) {
				String fileName = scene.getCacheCodeChunk(0);
				srcCanvas = mCacheManager.decodeImageCache(fileName);
			} else {
				Bitmap bitmap = scene.createCacheAsBitmap(0);
				Size size = new Size(bitmap.getWidth(), bitmap.getHeight());
				srcCanvas = new PixelCanvas(size, false);
				srcCanvas.copyFrom(bitmap);
			}

			if (scene instanceof IScalable) {
				PixelCanvas scaledSrcCanvas = new PixelCanvas(scene.getSize(), false);

				Scaler scaler = ((IScalable) scene).getScaler();
				scaler.draw(srcCanvas, scaledSrcCanvas);
				return scaledSrcCanvas;
			}
		} else {
			srcCanvas = new PixelCanvas(getSize(), false);
			scene.onDraw(srcCanvas);
		}
		return srcCanvas;
	}

	private PixelCanvas extractPixelCanvas(MultiLayerScene scene) throws IOException {

		PixelCanvas dstCanvas = new PixelCanvas(getSize(), false);

		for (Scene layer : scene.getLayers()) {
			PixelCanvas srcCanvas = extractPixelCanvas(layer);
			Rect rect = scene.getLayerRect(layer);
			srcCanvas.copy(dstCanvas, 0, 0, rect.left, rect.top, rect.width(), rect.height());
		}
		return dstCanvas;
	}

	private void mapSceneOntoCanvas() {

		for (Scene scene : mScenesToDraw) {
			if (!mSceneCanvasMap.containsKey(scene)) {
				PixelCanvas canvas = mCanvases.poll();
				mSceneCanvasMap.put(scene, canvas);
			}
		}
	}

	private void organizeSequence() throws IOException {
		int position = getPosition();

		for (int i = 0; i != mScenes.size(); ++i) {
			if (mSceneRanges[i].contains(position)) {

				int firstSceneIndex = i;
				int lastSceneIndex = i;
				int boundIndex = Math.min(firstSceneIndex + mConcurrentSceneCount, mScenes.size()) - 1;

				while (lastSceneIndex != boundIndex && mSceneRanges[lastSceneIndex + 1].contains(position)) {
					lastSceneIndex += 1;
				}

				if (mFirstSceneIndex == firstSceneIndex && mLastSceneIndex == lastSceneIndex) {
					break;
				}
				mFirstSceneIndex = firstSceneIndex;
				mLastSceneIndex = lastSceneIndex;

				ArrayList<Scene> scenesToDrawOld = new ArrayList<>(mScenesToDraw);
				mScenesToDraw.clear();

				for (int j = mFirstSceneIndex; j <= mLastSceneIndex; ++j) {
					mScenesToDraw.add(mScenes.get(j));
				}
				for (Scene oldScene : scenesToDrawOld) {
					if (!mScenesToDraw.contains(oldScene)) {
						PixelCanvas removed = mSceneCanvasMap.remove(oldScene);
						mCanvases.push(removed);
					}
				}
				mapSceneOntoCanvas();

				if (isOnPreviewMode()) {
					prepareCanvasWithCache();
				} else {
					prepareCanvasWithoutCache();
				}
				break;
			}
		}
	}

	@Override
	Size[] getCanvasRequirement() {

		Size[] size = new Size[mConcurrentSceneCount + 1];
		Arrays.fill(size, getSize());
		return size;
	}

	void setCollection(Integer... indexes) {
		Precondition.checkArray(indexes).checkMinLength(3).checkNotContainsNull();
		setCollection(Arrays.asList(indexes));
	}

	void setCollection(List<Integer> indexes) {
		Precondition.checkCollection(indexes).checkMinSize(3).checkNotContainsNull();

		mSceneIndexes.clear();
		mSceneIndexes.addAll(indexes);

		makeSceneRanges();
		setDuration(mSceneRanges[indexes.size() - 1].end);
	}

	private void makeSceneRanges() {

		int size = mSceneIndexes.size();
		mSceneRanges = new Range[size];

		for (int i = 0; i != size; ++i) {
			switch (i) {
				case 0:
				case 1: {
					int start = i * 500;
					int end = start + 1000;
					mSceneRanges[i] = Range.closedOpen(start, end);
					break;
				}
				default: {
					int start = i * 250 + 500;
					int end = start + 750;
					mSceneRanges[i] = (i == size - 1) ? Range.closedOpen(start, end) : Range.closed(start, end);
					break;
				}
			}
		}
	}

	public List<Integer> getCollection() {
		return new ArrayList<Integer>(mSceneIndexes);
	}

	void addTransition(Transition transition) {
		addTransition(transition, mTransitions.size());
	}

	void addTransition(Transition transition, int index) {
		Precondition.checkNotNull(transition);
		mTransitions.add(index, transition);
	}

	void removeTransition(int index) {
		mTransitions.remove(index);
	}

	void removeAllTransitions() {
		mTransitions.clear();
	}

	List<Transition> getTransitions() {
		return new ArrayList<Transition>(mTransitions);
	}

	void setTransitionOrder(TransitionOrder transitionOrder) {
		Precondition.checkNotNull(transitionOrder);
		mTransitionOrder = transitionOrder;
	}

	public TransitionOrder getTransitionOrder() {
		return mTransitionOrder;
	}

	void setConcurrentSceneCount(int count) {
		Precondition.checkOnlyPositive(count);

		mConcurrentSceneCount = count;
		mTempCanvasIndex = count;
		mScenesToDraw = new ArrayList<>(count);
	}

	int getConcurrentSceneCount() {
		return mConcurrentSceneCount;
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link CollectionScene}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Scene.Editor<CollectionScene, Editor> {

		private Editor(CollectionScene collectionScene) {
			super(collectionScene);
		}

		@Override
		public Editor setDuration(int duration) {
			throw new UnsupportedOperationException("This method is unsupported.");
		}

		public Editor setCollection(Integer... indexes) {
			getObject().setCollection(indexes);
			return this;
		}

		public Editor setCollection(List<Integer> indexes) {
			getObject().setCollection(indexes);
			return this;
		}

		public <T extends Transition> T addTransition(Class<T> transitionClass) {
			return addTransition(transitionClass, getObject().mTransitions.size());
		}

		public <T extends Transition> T addTransition(Class<T> transitionClass, int index) {

			T transition = createNewTransition(transitionClass);
			getObject().addTransition(transition, index);
			return transition;
		}

		public Editor removeTransition(int index) {
			getObject().removeTransition(index);
			return this;
		}

		public Editor removeAllTransitions() {
			getObject().removeAllTransitions();
			return this;
		}

		public Editor setTransitionOrder(TransitionOrder transitionOrder) {
			getObject().setTransitionOrder(transitionOrder);
			return this;
		}

		private <T extends Transition> T createNewTransition(Class<T> transitionClass) {

			T transition = CanvasUserFactory.createTransition(transitionClass, getObject().mRegion);
			transition.setDuration(1000);
			return transition;
		}
	}

	// // // // // Enumeration.
	// // // // //
	/**
	 * @see #CIRCULAR_LIST
	 * @see #RANDOMIZED
	 */
	public static enum TransitionOrder {

		CIRCULAR_LIST,

		RANDOMIZED;

		public boolean isRandom() {
			return equals(RANDOMIZED);
		}
	}
}