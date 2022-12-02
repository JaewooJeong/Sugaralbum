package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * {@link Scene}이 생성한 이미지에 가볍게 흔들리는 효과를 적용하는 클래스.
 */
public final class SwayEffect extends Effect {

	// // // // // Static variable.
	// // // // //
	private static final double MINUS_PI = -Math.PI;

	public static final String JSON_VALUE_TYPE = "sway_effect";

	public static final String JSON_NAME_DIRECTION = "direction";

	public static final Direction DEFAULT_DIRECTION = Direction.VERTICAL;

	private static final float SWAY_SIZE_RATIO = 0.05f;
	private static final int SWAY_INTERVAL_MS = 2500;

	// // // // // Member variable.
	// // // // //
	private Direction mDirection;

	// // // // // Static method.
	// // // // //
	private static float getInterpolation(float input) {
		return (float) Math.sin((input + 1.0) * MINUS_PI);
	}

	// // // // // Constructor.
	// // // // //
	SwayEffect(Scene parent) {
		super(parent);
		mDirection = DEFAULT_DIRECTION;
	}

	// // // // // Method.
	// // // // //
	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();
		jsonObject.put(JSON_NAME_DIRECTION, mDirection);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setDirection(jsonObject.getEnum(JSON_NAME_DIRECTION, Direction.class));
	}

	@Override
	void onDraw(PixelCanvas dstCanvas) {

		int position = getPosition();
		int height = getHeight();

		boolean isNegativeDirection = (position / SWAY_INTERVAL_MS % 2 == 0);
		float swayRatio = getInterpolation((float) position % SWAY_INTERVAL_MS / SWAY_INTERVAL_MS);

		if (mDirection.equals(Direction.VERTICAL)) {

			int swaySize = Math.round(height * SWAY_SIZE_RATIO * swayRatio);
			if (swaySize == 0) {
				return;
			}

			PixelCanvas copyCanvas = getCanvas(0);
			dstCanvas.deepCopy(copyCanvas);

			if (isNegativeDirection) {
				copyCanvas.copy(dstCanvas, 0, -swaySize);
				copyCanvas.copy(dstCanvas, 0, height - swaySize);
			} else {
				copyCanvas.copy(dstCanvas, 0, swaySize);
				copyCanvas.copy(dstCanvas, 0, swaySize - height);
			}
		} else {
			Precondition.assureUnreachable();
		}
	}

	@Override
	Size[] getCanvasRequirement() {
		return new Size[] { getSize() };
	}

	void setDirection(Direction direction) {
		Precondition.checkNotNull(direction);
		mDirection = direction;
	}

	/**
	 * 이미지가 흔들리는 방향을 반환합니다.
	 */
	public Direction getDirection() {
		return mDirection;
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link SwayEffect}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Effect.Editor<SwayEffect, Editor> {

		private Editor(SwayEffect swayEffect) {
			super(swayEffect);
		}

		/**
		 * 이미지가 흔들릴 방향을 설정합니다.
		 * 
		 * @param type
		 *            흔들리는 방향에 대한 Direction 객체.
		 * @see Direction
		 */
		public Editor setDirection(Direction type) {
			getObject().setDirection(type);
			return this;
		}
	}

	// // // // // Enumeration.
	// // // // //
	/**
	 * 이미지가 흔들리는 방향을 구분하기 위한 열거형.
	 * 
	 * @see #VERTICAL
	 */
	public static enum Direction {

		/**
		 * 이미지가 수직으로 흔들림을 의미합니다.
		 */
		VERTICAL;
	}
}
