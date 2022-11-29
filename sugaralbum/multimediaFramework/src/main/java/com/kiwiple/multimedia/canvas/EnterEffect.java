package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import android.graphics.Color;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * {@link Scene}이 생성한 이미지가 외부로부터 진입하는 효과를 적용하는 클래스.
 * 
 * @see OverlayTransition
 */
public final class EnterEffect extends Effect {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "enter_effect";

	public static final String JSON_NAME_DIRECTION = "direction";
	public static final String JSON_NAME_EFFECT_DURATION = "effect_duration";
	public static final String JSON_NAME_BACKGROUND_COLOR = "background_color";
	public static final String JSON_NAME_IS_REVERSE = "is_reverse";

	public static final Direction DEFAULT_DIRECTION = Direction.ONE_WAY_LEFT;
	public static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;

	// // // // // Member variable.
	// // // // //
	private Direction mDirection = DEFAULT_DIRECTION;

	private int mEffectDuration;

	private int mBackgroundColor = DEFAULT_BACKGROUND_COLOR;

	private boolean mIsReverse;

	// // // // // Constructor.
	// // // // //
	EnterEffect(Scene parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	Size[] getCanvasRequirement() {
		return new Size[] { getSize() };
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_DIRECTION, mDirection);
		jsonObject.put(JSON_NAME_EFFECT_DURATION, mEffectDuration);
		jsonObject.put(JSON_NAME_BACKGROUND_COLOR, mBackgroundColor);
		jsonObject.put(JSON_NAME_IS_REVERSE, mIsReverse);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setDirection(jsonObject.getEnum(JSON_NAME_DIRECTION, Direction.class));
		setEffectDuration(jsonObject.getInt(JSON_NAME_EFFECT_DURATION));
		setBackgroundColor(jsonObject.getInt(JSON_NAME_BACKGROUND_COLOR));
		setReverse(jsonObject.getBoolean(JSON_NAME_IS_REVERSE));
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {
		checkValidity(mEffectDuration > 0, "You must invoke setEffectDuration().");
	}

	@Override
	void onDraw(PixelCanvas dstCanvas) {

		float progressRatio = getInternalProgressRatio();

		if (progressRatio == 0.0f) {
			dstCanvas.clear(mBackgroundColor);
			return;
		} else if (progressRatio == 1.0f) {
			return;
		}

		PixelCanvas tempCanvas = getCanvas(0);
		dstCanvas.deepCopy(tempCanvas);
		dstCanvas.clear(mBackgroundColor);

		switch (mDirection.wayCount) {
			case 1:
				drawOneWay(tempCanvas, dstCanvas, progressRatio);
				return;
			case 2:
				drawTwoWay(tempCanvas, dstCanvas, progressRatio);
				return;
			case 4:
				drawFourWay(tempCanvas, dstCanvas, progressRatio);
				return;
			default:
				return;
		}
	}

	private float getInternalProgressRatio() {

		int position = getPosition();
		if (mIsReverse) {
			int duration = getDuration();
			int effectStartPosition = duration - mEffectDuration;
			if (position < effectStartPosition) {
				return 1.0f;
			} else if (position >= duration) {
				return 0.0f;
			} else {
				position -= effectStartPosition;
				return 1.0f - (float) position / mEffectDuration;
			}
		} else {
			if (position <= 0) {
				return 0.0f;
			} else if (position >= mEffectDuration) {
				return 1.0f;
			} else {
				return (float) position / mEffectDuration;
			}
		}
	}

	private void drawOneWay(PixelCanvas srcCanvas, PixelCanvas dstCanvas, float progressRatio) {

		int width = getWidth();
		int height = getHeight();

		int dstX, dstY;

		if (mDirection.isHorizontal()) {

			dstY = 0;
			if (mDirection.isNegative()) {
				dstX = width - Math.round(width * progressRatio);
			} else {
				dstX = Math.round(width * progressRatio) - width;
			}
		} else {

			dstX = 0;
			if (mDirection.isNegative()) {
				dstY = height - Math.round(height * progressRatio);
			} else {
				dstY = Math.round(height * progressRatio) - height;
			}
		}
		srcCanvas.copy(dstCanvas, 0, 0, dstX, dstY, width, height);
	}

	private void drawTwoWay(PixelCanvas srcCanvas, PixelCanvas dstCanvas, float progressRatio) {

		int width = getWidth();
		int height = getHeight();

		if (mDirection.isHorizontal()) {

			int halfWidth = Math.round(width / 2.0f);
			int moveWidth = Math.round(halfWidth * progressRatio);

			srcCanvas.copy(dstCanvas, 0, 0, -halfWidth + moveWidth, 0, halfWidth, height);
			srcCanvas.copy(dstCanvas, halfWidth, 0, width - moveWidth, 0, halfWidth, height);

		} else { // if (mDirection.isVertical())

			int halfHeight = Math.round(height / 2.0f);
			int moveHeight = Math.round(halfHeight * progressRatio);

			srcCanvas.copy(dstCanvas, 0, 0, 0, -halfHeight + moveHeight, width, halfHeight);
			srcCanvas.copy(dstCanvas, 0, halfHeight, 0, height - moveHeight, width, halfHeight);
		}
	}

	private void drawFourWay(PixelCanvas srcCanvas, PixelCanvas dstCanvas, float progressRatio) {

	}

	void setDirection(Direction direction) {
		Precondition.checkNotNull(direction);
		mDirection = direction;
	}

	/**
	 * 이미지의 진입 방향을 반환합니다.
	 */
	public Direction getDirection() {
		return mDirection;
	}

	void setEffectDuration(int duration) {
		Precondition.checkOnlyPositive(duration);
		mEffectDuration = duration;
	}

	/**
	 * 효과가 적용되는 시간 길이를 ms 단위로써 반환합니다.
	 */
	public int getEffectDuration() {
		return mEffectDuration;
	}

	void setBackgroundColor(int color) {
		mBackgroundColor = color;
	}

	/**
	 * 효과가 적용되는 중에 발생하는 빈 공간에 출력하는 색상을 반환합니다.
	 * 
	 * @return {@code #AARRGGBB} 형식의 출력 색상.
	 */
	public int getBackgroundColor() {
		return mBackgroundColor;
	}

	void setReverse(boolean reverse) {
		mIsReverse = reverse;
	}

	/**
	 * 적용 효과가 역전되었는지의 여부를 반환합니다.
	 * 
	 * @return 적용 효과가 역전되었을 때 {@code true}.
	 */
	public boolean isReverse() {
		return mIsReverse;
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link EnterEffect}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Effect.Editor<EnterEffect, Editor> {

		private Editor(EnterEffect enterEffect) {
			super(enterEffect);
		}

		/**
		 * 이미지의 진입 방향을 설정합니다.
		 * 
		 * @param direction
		 *            설정할 {@code Direction} 객체.
		 * @see Direction
		 */
		public Editor setDirection(Direction direction) {
			getObject().setDirection(direction);
			return this;
		}

		/**
		 * 효과가 적용되는 시간 길이를 ms 단위로써 설정합니다.
		 * 
		 * @param duration
		 *            ms 단위의 시간 길이.
		 */
		public Editor setEffectDuration(int duration) {
			getObject().setEffectDuration(duration);
			return this;
		}

		/**
		 * 효과가 적용되는 중에 발생하는 빈 공간에 출력할 색상을 설정합니다.
		 * 
		 * @param color
		 *            {@code #AARRGGBB} 형식의 출력 색상.
		 */
		public Editor setBackgroundColor(int color) {
			getObject().setBackgroundColor(color);
			return this;
		}

		/**
		 * 적용 효과를 역전시킬지의 여부를 설정합니다. 역전된 경우에는 효과의 시작점에서 진입하는 것이 아닌, 끝점에서 퇴장하는 효과가 연출됩니다.
		 * 
		 * @param reverse
		 *            적용 효과를 역전시키려면 {@code true}.
		 */
		public Editor setReverse(boolean reverse) {
			getObject().setReverse(reverse);
			return this;
		}
	}

	// // // // // Enumeration.
	// // // // //
	/**
	 * 이미지가 진입하는 방향을 구분하기 위한 열거형.
	 * 
	 * @see #ONE_WAY_LEFT
	 * @see #ONE_WAY_UP
	 * @see #ONE_WAY_RIGHT
	 * @see #ONE_WAY_DOWN
	 * @see #TWO_WAY_HORIZONTAL
	 * @see #TWO_WAY_VERTICAL
	 */
	public static enum Direction {

		/**
		 * 이미지가 왼쪽으로 진입함을 의미합니다.
		 */
		ONE_WAY_LEFT(1),

		/**
		 * 이미지가 위로 진입함을 의미합니다.
		 */
		ONE_WAY_UP(1),

		/**
		 * 이미지가 오른쪽으로 진입함을 의미합니다.
		 */
		ONE_WAY_RIGHT(1),

		/**
		 * 이미지가 아래로 진입함을 의미합니다.
		 */
		ONE_WAY_DOWN(1),

		/**
		 * 이미지가 양쪽에서 수평으로 진입함을 의미합니다.
		 */
		TWO_WAY_HORIZONTAL(2),

		/**
		 * 이미지가 양쪽에서 수직으로 진입함을 의미합니다.
		 */
		TWO_WAY_VERTICAL(2);

		final int wayCount;

		private Direction(int wayCount) {
			this.wayCount = wayCount;
		}

		boolean isHorizontal() {
			return equals(ONE_WAY_LEFT) || equals(ONE_WAY_RIGHT) || equals(TWO_WAY_HORIZONTAL);
		}

		boolean isNegative() {
			return equals(ONE_WAY_LEFT) || equals(ONE_WAY_UP);
		}
	}
}