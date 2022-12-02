package com.kiwiple.multimedia.canvas;

import static com.kiwiple.multimedia.Constants.INVALID_INDEX;
import static com.kiwiple.multimedia.Constants.INVALID_INTEGER_VALUE;
import static com.kiwiple.multimedia.canvas.FocusState.OFF;
import static com.kiwiple.multimedia.canvas.FocusState.ON;
import static com.kiwiple.multimedia.canvas.FocusState.PRELIMINARY;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.graphics.Color;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.CollectionUtils;
import com.kiwiple.multimedia.util.Size;

/**
 * {@code Region}의 직접적인 제어를 받아야 작동할 수 있는 다양한 {@link RegionChild}를 관리하기 위한 클래스.
 * <p />
 * {@code Region}은 여러 {@code RegionChild}의 조합으로 구성될 수 있습니다. {@link Visualizer}가 표현하는 하나의 화면에 출력할 수
 * 있는 구성물에 한계가 있으므로, 개개의 {@code RegionChild}는 배정된 순서에 따라 특정 시간 위치에서만 출력된다는 것이 기본 규칙입니다.
 * {@code RegionChild} 구현체가 필요로 하는 공간 및 시간 자원을 명시하면, {@code Region}은 이를 효율적으로 배분하고 특정 시간 위치에 어떤 객체가
 * 출력되어야 하는지 산정하는 등의 작업을 수행합니다.
 * 
 * @see RegionChild
 * @see Scene
 * @see Transition
 * @see Effect
 */
public final class Region extends VisualizerChild {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_NAME_LEFT = "left";
	public static final String JSON_NAME_TOP = "top";
	public static final String JSON_NAME_RIGHT = "right";
	public static final String JSON_NAME_BOTTOM = "bottom";

	public static final String JSON_NAME_SCENES = "scenes";
	public static final String JSON_NAME_TRANSITIONS = "transitions";

	// // // // // Member variable.
	// // // // //
	private final ArrayList<Scene> mScenes;
	private final ArrayList<Transition> mTransitions;
	private final NullTransition mNullTransition;

	private final CanvasDispenser mCanvasDispenser;

	private Size mSize;
	private int mDuration;
	private int mPosition;

	private Scene mCurrentScene;
	private Scene mNextScene;
	private Transition mCurrentTransition;

	private int[] mSceneSequence;
	private int[] mTransitionSequence;

	private PixelCanvas mMainCanvas;
	private PixelCanvas mSubCanvas;

	private int mSceneIndex;

	// // // // // Constructor.
	// // // // //
	{
		mScenes = new ArrayList<>();
		mTransitions = new ArrayList<>();
		mNullTransition = new NullTransition(this);
		mCanvasDispenser = new CanvasDispenser(this);
		mSize = Size.INVALID_SIZE;

		clearFocusState();
	}

	Region(Visualizer parent) {
		super(parent);
		setSize(parent.getSize());
	}

	// // // // // Method.
	// // // // //
	void draw(PixelCanvas dstCanvas) {

		if (!isValidated()) {
			drawOnInvalid(dstCanvas);
			return;
		}

		dstCanvas.clear(Visualizer.DEFAULT_CLEAR_COLOR);

		if (!(mCurrentTransition instanceof NullTransition) && mCurrentTransition.getFocusState().equals(ON)) {
			mCurrentScene.draw(mMainCanvas, true);
			mNextScene.draw(mSubCanvas, true);
			mCurrentTransition.onDraw(mMainCanvas, mSubCanvas, dstCanvas);
		} else {
			mCurrentScene.draw(dstCanvas, true);
		}
	}

	@Override
	final void drawOnInvalid(PixelCanvas dstCanvas) {
		dstCanvas.clear(COLOR_SYMBOL_INVALID);
		dstCanvas.clear(Color.YELLOW, 20, 20, 10, 10);
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();
		jsonObject.put(JSON_NAME_LEFT, 0.0f);
		jsonObject.put(JSON_NAME_TOP, 0.0f);
		jsonObject.put(JSON_NAME_RIGHT, 1.0f);
		jsonObject.put(JSON_NAME_BOTTOM, 1.0f);
		jsonObject.put(JSON_NAME_SCENES, mScenes);
		jsonObject.put(JSON_NAME_TRANSITIONS, mTransitions);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		mScenes.clear();
		mTransitions.clear();

		for (JsonObject sceneJsonObject : jsonObject.getJSONArrayAsList(JSON_NAME_SCENES, JsonObject.class)) {
			Scene scene = CanvasUserFactory.createScene(sceneJsonObject, this);
			addScene(scene);
		}
		int transitionIndex = 0;
		for (JsonObject transitionJsonObject : jsonObject.getJSONArrayAsList(JSON_NAME_TRANSITIONS, JsonObject.class)) {
			if (transitionIndex >= mTransitions.size())
				break;
			Transition transition = (transitionJsonObject != null ? CanvasUserFactory.createTransition(transitionJsonObject, this) : mNullTransition);
			replaceTransition(transition, transitionIndex++);
		}
	}

	@Override
	final void validate(Changes changes) {

		changes.update(this);

		for (RegionChild child : getRegionChilds())
			child.validate(changes.clone());
		try {
			if (isEmpty()) {
				setValidated(false);
			} else {
				organizeSequence();
				mCanvasDispenser.update();

				mCurrentScene = getScene(0);
				setPosition(0);
				setValidated(true);
			}
		} catch (Exception exception) {
			exception.printStackTrace();
			clearFocusState();
			setValidated(false);
		}
		clearChanges();
	}

	@Override
	void release() {

		clear();
		mMainCanvas = null;
		mSubCanvas = null;
		mSize = Size.INVALID_SIZE;
	}

	@Override
	public final Editor getEditor() {
		return (Editor) super.getEditor();
	}

	void setDuration(int duration) {
		Precondition.checkNotNegative(duration);
		mDuration = duration;
	}

	@Override
	public int getDuration() {
		return mDuration;
	}

	void setPosition(int position) {

		if (isEmpty() || !isValidated())
			return;

		int currentSceneIndex = measureIndex(mSceneSequence, position);
		int nextSceneIndex = currentSceneIndex + 1;
		boolean isLastScene = (nextSceneIndex == mScenes.size());

		if (currentSceneIndex != mSceneIndex) {

			mMainCanvas.clear(Visualizer.DEFAULT_CLEAR_COLOR);
			mCurrentTransition.setFocusState(OFF);

			boolean exCurrentSceneExpired = (mSceneIndex != nextSceneIndex);
			boolean exNextSceneExpired = (mSceneIndex + 1 != currentSceneIndex);

			if (exCurrentSceneExpired)
				mCurrentScene.setFocusState(OFF);
			if (exNextSceneExpired && mNextScene != null)
				mNextScene.setFocusState(OFF);
			if (!exCurrentSceneExpired)
				mCurrentScene.optimizeCanvas();
			if (!exNextSceneExpired && mNextScene != null)
				mNextScene.optimizeCanvas();

			mCurrentScene = mScenes.get(currentSceneIndex);
			mCurrentScene.setFocusState(ON);

			if (isLastScene) {
				mNextScene = null;
			} else {
				mSubCanvas.clear(Visualizer.DEFAULT_CLEAR_COLOR);

				mNextScene = mScenes.get(nextSceneIndex);
				mNextScene.setFocusState(PRELIMINARY);
				mCurrentTransition = mTransitions.get(currentSceneIndex);
				mCurrentTransition.setFocusState(PRELIMINARY);
			}
			mSceneIndex = currentSceneIndex;
		}

		int scenePosition = (currentSceneIndex == 0) ? position : position - mTransitionSequence[currentSceneIndex - 1];
		mCurrentScene.setPosition(scenePosition);

		if (!isLastScene) {

			int priorTransitionStartTime = mTransitionSequence[currentSceneIndex];
			int nextScenePosition = position - priorTransitionStartTime;

			if (nextScenePosition >= 0) {
				if (!mCurrentTransition.getFocusState().equals(ON)) {
					mNextScene.setFocusState(ON);
					mCurrentTransition.setFocusState(ON);
				}
				mNextScene.setPosition(nextScenePosition);
				mCurrentTransition.setPosition(nextScenePosition);
			} else if (mCurrentTransition.getFocusState().equals(ON)) {
				mNextScene.setFocusState(PRELIMINARY);
				mCurrentTransition.setFocusState(PRELIMINARY);
			}
		}
		mPosition = position;
	}

	@Override
	public int getPosition() {
		return mPosition;
	}

	void setSize(Size size) {
		Precondition.checkNotNull(size);
		Precondition.checkArgument(size.isValid(), "Invalid size.");

		if (!mSize.equals(size)) {
			mMainCanvas = new PixelCanvas(size, true);
			mSubCanvas = new PixelCanvas(size, true);
			mSize = size;
		}
	}

	@Override
	public Size getSize() {
		return mSize;
	}

	/**
	 * 객체를 구성하는 {@link Scene} 목록에서 지정된 첨자에 해당하는 {@code Scene}을 반환합니다.
	 * 
	 * @param index
	 *            지정할 첨자.
	 */
	public Scene getScene(int index) {
		return mScenes.get(index);
	}

	/**
	 * 객체를 구성하는 {@link Scene} 목록의 사본을 반환합니다.
	 */
	public List<Scene> getScenes() {
		return new ArrayList<Scene>(mScenes);
	}

	/**
	 * 객체를 구성하는 {@link Transition}의 목록에서 지정된 첨자에 해당하는 {@code Transition}을 반환합니다.
	 * 
	 * @param index
	 *            지정할 첨자.
	 */
	public Transition getTransition(int index) {
		return mTransitions.get(index);
	}

	/**
	 * 객체를 구성하는 {@link Transition} 목록의 사본을 반환합니다.
	 */
	public List<Transition> getTransitions() {
		return new ArrayList<Transition>(mTransitions);
	}

	/**
	 * 객체가 비어 있는 상태인지의 여부를 반환합니다.
	 * <p />
	 * 객체를 구성하는 {@link Scene}이 없다면 자연스럽게 {@link Transition}도 존재할 수 없으므로, 어떤 {@code Scene}도 포함되어 있지
	 * 않은 상태를 비어 있는 상태로 정의합니다.
	 * 
	 * @return 객체를 비어 있다면 {@code true}.
	 */
	public boolean isEmpty() {
		return mScenes.isEmpty();
	}

	void organizeSequence() {

		mSceneSequence = new int[mScenes.size()];
		mTransitionSequence = new int[mTransitions.size()];

		for (int index = 0, size = mScenes.size(); index != size; ++index) {

			boolean isFirstScene = (index == 0);
			boolean isLastScene = (index == mScenes.size() - 1);
			int priorIndex = isFirstScene ? INVALID_INDEX : (index - 1);

			Scene scene = mScenes.get(index);
			Transition transition = isLastScene ? mNullTransition : mTransitions.get(index);
			Transition priorTransition = isFirstScene ? mNullTransition : mTransitions.get(priorIndex);

			int priorTransitionStartPosition = isFirstScene ? 0 : mTransitionSequence[priorIndex];
			int priorTransitionDuration = priorTransition.getDuration();
			int priorSceneEndPosition = priorTransitionStartPosition + priorTransitionDuration;

			int sceneStartPosition = priorTransitionStartPosition;
			int sceneEndPosition = sceneStartPosition + scene.getDuration();
			int transitionStartPosition = sceneEndPosition - transition.getDuration();

			mSceneSequence[index] = priorSceneEndPosition;

			if (isLastScene)
				mDuration = sceneEndPosition;
			else
				mTransitionSequence[index] = transitionStartPosition;
		}
	}

	void addScene(Scene scene) {
		addScene(scene, mScenes.size());
	}

	void addScene(Scene scene, int index) {
		Precondition.checkNotNull(scene);

		mScenes.add(index, scene);

		int scenesSize = mScenes.size();
		if (scenesSize > 1) {
			if (index == (scenesSize - 1))
				mTransitions.add(mNullTransition);
			else
				mTransitions.add(index, mNullTransition);
		}
	}

	void removeScene(int index) {

		mScenes.remove(index).release();
		if (!mTransitions.isEmpty())
			mTransitions.remove(index == mScenes.size() ? mTransitions.size() - 1 : index).release();
	}

	void replaceScene(Scene scene, int index) {
		Precondition.checkNotNull(scene);

		mScenes.remove(index).release();
		mScenes.add(index, scene);
	}

	void swapScenes(int index1, int index2) {

		Scene scene1 = mScenes.get(index1);
		Scene scene2 = mScenes.get(index2);
		mScenes.add(index1, scene2);
		mScenes.remove(index1 + 1);
		mScenes.add(index2, scene1);
		mScenes.remove(index2 + 1);
	}

	void reorderScenes(int srcIndex, int dstIndex) {

		Scene scene = mScenes.remove(srcIndex);
		mScenes.add(dstIndex, scene);
	}

	void replaceTransition(Transition transition, int index) {
		mTransitions.remove(index).release();
		mTransitions.add(index, transition != null ? transition : mNullTransition);
	}

	int getSceneEndPosition(int sceneIndex) {

		if (sceneIndex >= mScenes.size() || mTransitionSequence == null)
			return INVALID_INTEGER_VALUE;
		return (sceneIndex == mScenes.size() - 1) ? mDuration : Math.max(mTransitionSequence[sceneIndex] - 1, 0);
	}

	CanvasDispenser getCanvasDispenser() {
		return mCanvasDispenser;
	}

	void clear() {

		clearFocusState();

		for (RegionChild child : getRegionChilds())
			child.release();

		mCanvasDispenser.clear();
		mScenes.clear();
		mTransitions.clear();
		mSceneSequence = null;
		mTransitionSequence = null;
	}

	void clearFocusState() {

		mPosition = INVALID_INTEGER_VALUE;
		mSceneIndex = INVALID_INDEX;
		mCurrentScene = null;
		mNextScene = null;
		mCurrentTransition = mNullTransition;

		for (RegionChild child : getRegionChilds())
			child.setFocusState(OFF);
		setValidated(false);
	}

	private int measureIndex(int[] sequence, int position) {

		for (int i = sequence.length - 1; i >= 0; --i)
			if (sequence[i] <= position)
				return i;
		return Precondition.assureUnreachable(String.format("sequence.length : %s, position : %s", sequence.length, position));
	}

	private ArrayList<RegionChild> getRegionChilds() {

		ArrayList<RegionChild> list = new ArrayList<>();
		list.addAll(mScenes);
		list.addAll(mTransitions);

		return CollectionUtils.removeAll(list, mNullTransition);
	}

	private void printCanvasUsage() {

		StringBuilder builder = new StringBuilder(512);
		builder.append(mCanvasDispenser).append(" CanvasRequirement {\n");

		for (int i = 0, end = mScenes.size(); i != end; ++i)
			builder.append("\t[").append(i).append("] ").append(mScenes.get(i).createCanvasRequirementLog(1)).append('\n');
		for (int i = 0, end = mTransitions.size(); i != end; ++i)
			builder.append("\t[").append(i).append("] ").append(mTransitions.get(i).createCanvasRequirementLog(1)).append('\n');
		builder.append('}');

		L.d(builder);
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link Region}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends VisualizerChild.Editor<Region, Editor> {

		private Editor(Region region) {
			super(region);
		}

		/**
		 * {@code Region}을 구성하는 {@link Scene} 목록에 새로운 {@code Scene} 객체를 생성하여 추가합니다.
		 * 
		 * @param type
		 *            추가할 {@code Scene}의 {@code Class} 객체.
		 * @return 추가된 {@code Scene} 객체.
		 */
		public <T extends Scene> T addScene(Class<T> type) {

			T scene = CanvasUserFactory.createScene(type, getObject());
			getObject().addScene(scene);
			return scene;
		}

		/**
		 * {@code Region}을 구성하는 {@link Scene} 목록에서 특정 위치에 새로운 {@code Scene}을 생성하여 추가합니다.
		 * 
		 * @param type
		 *            추가할 {@code Scene}의 {@code Class} 객체.
		 * @param index
		 *            추가할 위치에 해당하는 첨자.
		 * @return 추가된 {@code Scene} 객체.
		 */
		public <T extends Scene> T addScene(Class<T> type, int index) {

			T scene = CanvasUserFactory.createScene(type, getObject());
			getObject().addScene(scene, index);
			return scene;
		}

		/**
		 * {@code Region}을 구성하는 {@link Scene} 목록에서 특정 위치에 해당하는 {@code Scene}을 제거합니다.
		 * 
		 * @param index
		 *            제거할 {@code Scene}에 해당하는 첨자.
		 */
		public Editor removeScene(int index) {
			getObject().removeScene(index);
			return this;
		}

		/**
		 * {@code Region}을 구성하는 {@link Scene} 목록에서 특정 위치에 해당하는 {@code Scene}을 제거한 후, 새로운
		 * {@code Scene}을 생성하여 대체합니다.
		 * 
		 * @param type
		 *            새로운 {@code Scene}의 {@code Class} 객체.
		 * @param index
		 *            제거할 {@code Scene}에 해당하는 첨자.
		 * @return 새롭게 생성된 {@code Scene} 객체.
		 */
		public <T extends Scene> T replaceScene(Class<T> type, int index) {

			T scene = CanvasUserFactory.createScene(type, getObject());
			getObject().replaceScene(scene, index);
			return scene;
		}

		/**
		 * {@code Region}을 구성하는 {@link Scene} 목록에서 특정 위치에 해당하는 두 {@code Scene}의 위치를 맞바꿉니다.
		 * 
		 * @param index1
		 *            위치를 교환할 첫 번째 {@code Scene}에 해당하는 첨자.
		 * @param index2
		 *            위치를 교환할 두 번째 {@code Scene}에 해당하는 첨자.
		 */
		public Editor swapScenes(int index1, int index2) {
			getObject().swapScenes(index1, index2);
			return this;
		}

		/**
		 * {@code Region}을 구성하는 {@link Scene} 목록에서 특정 위치에 해당하는 {@code Scene}의 위치를 새롭게 배정합니다.
		 * 
		 * @param srcIndex
		 *            위치를 옮길 {@code Scene}에 해당하는 첨자.
		 * @param dstIndex
		 *            새로 배정할 위치에 해당하는 첨자.
		 */
		public Editor reorderScenes(int srcIndex, int dstIndex) {
			getObject().reorderScenes(srcIndex, dstIndex);
			return this;
		}

		/**
		 * {@code Region}을 구성하는 {@link Transition} 목록에서 특정 위치에 해당하는 {@code Transition}을 제거한 후에, 새로운
		 * {@code Transition}을 생성하여 대체합니다.
		 * 
		 * @param type
		 *            새로운 {@code Transition}의 {@code Class} 객체.
		 * @param index
		 *            제거할 {@code Transition}에 해당하는 첨자.
		 * @return 새롭게 생성된 {@code Transition} 객체.
		 */
		public <T extends Transition> T replaceTransition(Class<T> type, int index) {

			T transition = (type != null) ? CanvasUserFactory.createTransition(type, getObject()) : null;
			getObject().replaceTransition(transition, index);
			return transition;
		}

		/**
		 * {@code Region}을 구성하는 모든 {@link RegionChild} 정보를 제거합니다.
		 */
		public Editor clear() {
			getObject().clear();
			return this;
		}
	}
}