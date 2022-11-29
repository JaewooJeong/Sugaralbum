package com.kiwiple.multimedia.canvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.annotation.Child;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * {@link Visualizer}의 가장 기본적인 구성 단위로서, 하나의 장면을 출력하기 위한 추상 클래스.
 */
@SuppressWarnings("unchecked")
public abstract class Scene extends RegionChild {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_NAME_EFFECTS = "effects";
	public static final String JSON_NAME_FILTER_ID = "filter_id";
	public static final String JSON_NAME_IMAGE_ID = "image_id";

	// // // // // Member variable.
	// // // // //
	@Child
	private final ArrayList<Effect> mEffects = new ArrayList<Effect>();

	private final MultiLayerScene mParent;

	private int mDuration;
	private int mPosition;

	// // // // // Constructor.
	// // // // //
	Scene(Region parent) {
		super(parent);
		mParent = null;
	}

	Scene(MultiLayerScene parent) {
		super(parent);
		mParent = parent;
	}

	// // // // // Method.
	// // // // //
	abstract void onDraw(PixelCanvas dstCanvas);

	final void draw(PixelCanvas dstCanvas, boolean withEffect) {

		if (!isValidated()) {
			drawOnInvalid(dstCanvas);
		} else {
			onDraw(dstCanvas);
			if (withEffect) {
				for (Effect effect : mEffects) {
					effect.draw(dstCanvas);
				}
			}
		}
	}

	@Override
	final void drawOnInvalid(PixelCanvas dstCanvas) {
		dstCanvas.clear(COLOR_SYMBOL_INVALID);
		dstCanvas.clear(COLOR_SYMBOL_INVALID_SCENE, 0, 20, getWidth(), 4);
		dstCanvas.clear(COLOR_SYMBOL_INVALID_SCENE, 0, 25, getWidth(), 2);
	}

	@Override
	public Editor<? extends Scene, ? extends Editor<?, ?>> getEditor() {
		return (Editor<? extends Scene, ? extends Editor<?, ?>>) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_DURATION, mDuration);
		jsonObject.putOpt(JSON_NAME_EFFECTS, mEffects);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		// prefer optInt() to getInt() because Layer have no duration.
		setDuration(jsonObject.optInt(JSON_NAME_DURATION, mDuration));
		parseEffectJsonObject(jsonObject);
	}

	@Override
	public final Size getSize() {
		return (mParent == null ? super.getSize() : mParent.getLayerSize(this));
	}

	final void setDuration(int duration) {
		Precondition.checkNotNegative(duration);

		mDuration = duration;
		notifyChange(Change.DURATION);
	}

	@Override
	public final int getDuration() {
		return (mParent == null ? mDuration : mParent.getDuration());
	}

	final void setPosition(int position) {
		Precondition.checkNotNegative(position);

		if (position > mDuration)
			position = mDuration;
		mPosition = position;
	}

	@Override
	public final int getPosition() {
		return (mParent == null ? mPosition : mParent.getPosition());
	}

	public final float getProgressRatio() {
		return super.getProgressRatio();
	}

	void addEffect(Effect effect) {
		addEffect(effect, mEffects.size());
	}

	void addEffect(Effect effect, int index) {
		Precondition.checkNotNull(effect);
		mEffects.add(index, effect);
	}

	void removeEffect(int index) {
		Precondition.checkNotNegative(index);
		mEffects.remove(index).release();
	}

	void removeAllEffect(Class<?> type) {
		Precondition.checkNotNull(type);

		for (Effect effect : new ArrayList<>(mEffects)) {
			if (effect.getClass().equals(type)) {
				effect.release();
				mEffects.remove(effect);
			}
		}
	}

	void removeAllEffects() {

		for (Effect effect : mEffects)
			effect.release();
		mEffects.clear();
	}

	void replaceEffect(Effect effect, int index) {
		Precondition.checkNotNull(effect);

		removeEffect(index);
		addEffect(effect, index);
	}

	void swapEffects(int index1, int index2) {
		Collections.swap(mEffects, index1, index2);
	}

	/**
	 * 이미지를 생성할 때 적용하는 {@link Effect} 목록의 사본을 반환합니다.
	 */
	public List<Effect> getEffects() {
		return new ArrayList<>(mEffects);
	}

	private void parseEffectJsonObject(JsonObject jsonObject) throws JSONException {

		if (!jsonObject.isNull(JSON_NAME_EFFECTS)) {
			for (JsonObject effectJsonObject : jsonObject.getJSONArray(JSON_NAME_EFFECTS).asList(JsonObject.class)) {
				addEffect(CanvasUserFactory.createEffect(effectJsonObject, this));
			}
		}
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link Scene}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static abstract class Editor<C extends Scene, E extends Editor<C, E>> extends RegionChild.Editor<C, E> {

		Editor(C scene) {
			super(scene);
		}

		/**
		 * 지정한 시간 위치에 해당하는 이미지를 그립니다.
		 * <p />
		 * <b>주의</b>: 본 메서드는 {@code Scene}에 포함된 {@link Effect}를 온전히 적용하지만, {@link Transition}은 적용하지
		 * 않습니다.
		 * 
		 * @param dstCanvas
		 *            이미지가 그려질 {@code PixelCanvas} 객체.
		 * @param position
		 *            ms 단위의 시간 위치.
		 */
		public E draw(PixelCanvas dstCanvas, int position) {

			Scene scene = getObject();

			try {
				Changes changes = new Changes();
				changes.update(scene);
				scene.validate(changes);

				scene.setFocusState(FocusState.ON);
				scene.setPosition(position);
				scene.draw(dstCanvas, true);

			} catch (Exception exception) {
				scene.drawOnInvalid(dstCanvas);
			} finally {
				scene.setFocusState(FocusState.OFF);
			}
			return (E) this;
		}

		/**
		 * 객체가 유지되는 시간 길이를 ms 단위로써 설정합니다.
		 * 
		 * @param duration
		 *            ms 단위의 시간 길이.
		 */
		public E setDuration(int duration) {
			getObject().setDuration(duration);
			return (E) this;
		}

		/**
		 * {@code Scene}의 {@link Effect} 목록에 새로운 {@code Effect}를 생성하여 추가합니다.
		 * 
		 * @param type
		 *            추가할 {@code Effect}의 {@code Class} 객체.
		 * @return 추가된 {@code Effect} 객체.
		 */
		public <T extends Effect> T addEffect(Class<T> type) {

			T effect = CanvasUserFactory.createEffect(type, getObject());
			getObject().addEffect(effect);
			return effect;
		}

		/**
		 * {@code Scene}의 {@link Effect} 목록에서 특정 위치에 새로운 {@code Effect}를 생성하여 추가합니다.
		 * 
		 * @param type
		 *            추가할 {@code Effect}의 {@code Class} 객체.
		 * @param index
		 *            추가할 위치를 해당하는 첨자.
		 * @return 추가된 {@code Effect} 객체.
		 */
		public <T extends Effect> T addEffect(Class<T> type, int index) {

			T effect = CanvasUserFactory.createEffect(type, getObject());
			getObject().addEffect(effect, index);
			return effect;
		}

		/**
		 * {@code Scene}의 {@link Effect} 목록에서 특장 위치에 해당하는 {@code Effect}를 제거한 후에, 새로운 {@code Effect}
		 * 를 생성하여 대체합니다.
		 * 
		 * @param type
		 *            새로운 {@code Effect}의 {@code Class} 객체.
		 * @param index
		 *            제거할 {@code Effect}에 해당하는 첨자.
		 * @return 새롭게 생성된 {@code Effect} 객체.
		 */
		public <T extends Effect> T replaceEffect(Class<T> type, int index) {

			T effect = CanvasUserFactory.createEffect(type, getObject());
			getObject().replaceEffect(effect, index);
			return effect;
		}

		/**
		 * {@code Scene}의 {@link Effect} 목록에서 특정 위치에 해당하는 {@code Effect}를 제거합니다.
		 * 
		 * @param index
		 *            제거할 {@code Effect}에 해당하는 첨자.
		 */
		public E removeEffect(int index) {
			getObject().removeEffect(index);
			return (E) this;
		}

		/**
		 * {@code Scene}의 {@link Effect} 목록에서 특정 클래스에 해당하는 모든 {@code Effect}를 제거합니다.
		 * 
		 * @param type
		 *            제거할 {@code Effect}의 {@code Class} 객체.
		 */
		public E removeAllEffects(Class<? extends Effect> type) {
			getObject().removeAllEffect(type);
			return (E) this;
		}

		/**
		 * {@code Scene}의 {@link Effect} 목록에 포함된 모든 객체를 제거합니다.
		 */
		public E removeAllEffects() {
			getObject().removeAllEffects();
			return (E) this;
		}

		/**
		 * {@code Scene}의 {@link Effect} 목록에서 특정 위치에 해당하는 두 {@code Scene}의 위치를 맞바꿉니다.
		 * 
		 * @param index1
		 *            위치를 교환할 첫 번째 {@code Effect}에 해당하는 첨자.
		 * @param index2
		 *            위치를 교환할 두 번째 {@code Effect}에 해당하는 첨자.
		 */
		public E swapEffects(int index1, int index2) {
			getObject().swapEffects(index1, index2);
			return (E) this;
		}
	}
}
