package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * 두 {@link Scene} 사이의 자연스러운 이미지 전환 효과를 적용하기 위한 추상 클래스.
 */
@SuppressWarnings("unchecked")
public abstract class Transition extends RegionChild {

	// // // // // Member variable.
	// // // // //
	private int mDuration;
	private int mPosition;

	// // // // // Constructor.
	// // // // //
	protected Transition(Region parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	abstract void onDraw(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas);

	final void draw(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {

		if (isValidated()) {
			float progressRatio = getProgressRatio();

			if (progressRatio == 0.0f)
				srcCanvasFormer.copy(dstCanvas);
			else if (progressRatio >= 1.0f)
				srcCanvasLatter.copy(dstCanvas);
			else
				onDraw(srcCanvasFormer, srcCanvasLatter, dstCanvas);
		} else {
			drawOnInvalid(dstCanvas);
		}
	}

	@Override
	final void drawOnInvalid(PixelCanvas dstCanvas) {
		dstCanvas.clear(COLOR_SYMBOL_INVALID);
		dstCanvas.clear(COLOR_SYMBOL_INVALID_TRANSITION, 0, 20, getWidth(), 4);
		dstCanvas.clear(COLOR_SYMBOL_INVALID_TRANSITION, 0, 25, getWidth(), 2);
	}

	@Override
	public Editor<? extends Transition, ? extends Editor<?, ?>> getEditor() {
		return (Editor<? extends Transition, ? extends Editor<?, ?>>) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();
		jsonObject.put(JSON_NAME_DURATION, mDuration);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);
		setDuration(jsonObject.getInt(JSON_NAME_DURATION));
	}

	@Override
	public final Size getSize() {
		return super.getSize();
	}

	final void setDuration(int duration) {
		Precondition.checkNotNegative(duration);

		mDuration = duration;
		notifyChange(Change.DURATION);
	}

	@Override
	public final int getDuration() {
		return mDuration;
	}

	final void setPosition(int position) {
		Precondition.checkNotNegative(position);

		if (position > mDuration) {
			position = mDuration;
		}
		mPosition = position;
	}

	@Override
	public final int getPosition() {
		return mPosition;
	}

	public final float getProgressRatio() {
		return super.getProgressRatio();
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link Transition}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static abstract class Editor<C extends Transition, E extends Editor<C, E>> extends RegionChild.Editor<C, E> {

		Editor(C transition) {
			super(transition);
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
	}

	/**
	 * {@link Transition}의 영향을 받는 두 {@link Scene}을 순차로써 구분하기 위한 열거형.
	 * 
	 * @see #FORMER
	 * @see #LATTER
	 */
	public static enum SceneOrder {

		/**
		 * 두 {@code Scene} 중 선행하는 {@code Scene}을 의미합니다.
		 */
		FORMER,

		/**
		 * 두 {@code Scene} 중 후행하는 {@code Scene}을 의미합니다.
		 */
		LATTER;

		public static final String DEFAULT_JSON_NAME = "scene_order";
	}
}
