package com.kiwiple.multimedia.canvas;

import static com.kiwiple.multimedia.canvas.Transition.SceneOrder.FORMER;
import static com.kiwiple.multimedia.canvas.Transition.SceneOrder.LATTER;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;

import android.animation.TimeInterpolator;
import android.graphics.Bitmap;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.R;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.math.interpolator.InterpolatorType;
import com.kiwiple.multimedia.util.Size;

/**
 * 전 {@code Scene}을 후 {@code Scene}으로 밀어내는 방식의 전환 효과를 연출하는 클래스.
 * <p />
 * 양 {@code Scene}이 회전하듯 특정 방향으로 반복적으로 밀어내기를 반복하다가 후 {@code Scene}이 최종적으로 진입합니다.
 */
public final class SpinTransition extends Transition {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "spin_transition";

	public static final String JSON_NAME_SPIN_ORDER = "spin_order";
	public static final String JSON_NAME_DIRECTION = "direction";
	public static final String JSON_NAME_INTERPOLATOR_TYPE = "interpolator_type";
	public static final String JSON_NAME_USE_OVERSHOOT = "use_overshoot";
	public static final String JSON_NAME_USE_BLURRED_BORDER = "use_blurred_border";

	private static final float FADE_DURATION = 0.05f;
	private static final float CRITICAL_VALUE_FADE_IN_BORDER = 0.0f + FADE_DURATION;
	private static final float CRITICAL_VALUE_FADE_OUT_BORDER = 1.0f - FADE_DURATION;
	private static final float OVERSHOOT_WEIGHT_MULTIPLIER = 3.0f;

	private static final SceneOrder[] DEFAULT_SCENE_ORDER = { FORMER, LATTER };
	private static final Direction DEFAULT_DIRECTION = Direction.LEFT;
	private static final InterpolatorType DEFAULT_INTERPOLATOR_TYPE = InterpolatorType.EXPONENTIAL_IN_OUT;
	private static final boolean DEFAULT_OVERSHOOT = false;
	private static final boolean DEFAULT_BLURRED_BORDER = false;

	// // // // // Member variable.
	// // // // //
	private final TimeInterpolator mOvershootInterpolator = new TimeInterpolator() {

		@Override
		public float getInterpolation(float input) {
			return (input * input - 1.0f) * (input - 0.8f) * (4.5f * input - 1.25f) + 1.0f;
		}
	};

	private SceneOrder[] mSpinOrder;
	private float[] mSpinSequence;
	private float mSpinBaseWeight;
	private int mSpinCount;

	@CacheCode
	private Direction mDirection;
	private InterpolatorType mProgressInterpolatorType;
	private TimeInterpolator mProgressInterpolator;

	@CacheCode
	private ImageResource mBlurredBorder;
	private int mBlurredBorderOffset;

	private boolean mUseOvershoot;
	private boolean mUseBlurredBorder;

	// // // // // Constructor.
	// // // // //
	{
		setSpinOrder(DEFAULT_SCENE_ORDER);
		setDirection(DEFAULT_DIRECTION);
		setInterpolator(DEFAULT_INTERPOLATOR_TYPE);
		setOvershoot(DEFAULT_OVERSHOOT);
		setBlurredBorder(DEFAULT_BLURRED_BORDER);
	}

	SpinTransition(Region parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	Change[] getSensitivities() {
		return new Change[] { Change.SIZE };
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {

		if (mUseBlurredBorder)
			mBlurredBorderOffset = Math.round(mBlurredBorder.measureSize(getResolution()).height / 2.0f);

		mSpinBaseWeight = 1.0f / (mUseOvershoot ? mSpinCount - 1 + OVERSHOOT_WEIGHT_MULTIPLIER : mSpinCount);
		for (int i = 0; i != mSpinSequence.length; ++i)
			mSpinSequence[i] = mSpinBaseWeight * i;
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_DIRECTION, mDirection);
		jsonObject.put(JSON_NAME_SPIN_ORDER, Arrays.copyOfRange(mSpinOrder, 1, mSpinOrder.length - 1));
		jsonObject.put(JSON_NAME_INTERPOLATOR_TYPE, mProgressInterpolatorType);
		jsonObject.put(JSON_NAME_USE_OVERSHOOT, mUseOvershoot);
		jsonObject.put(JSON_NAME_USE_BLURRED_BORDER, mUseBlurredBorder);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setSpinOrder(jsonObject.getJSONArrayAsList(JSON_NAME_SPIN_ORDER, SceneOrder.class));
		setDirection(jsonObject.getEnum(JSON_NAME_DIRECTION, Direction.class));
		setInterpolator(jsonObject.getEnum(JSON_NAME_INTERPOLATOR_TYPE, InterpolatorType.class));
		setOvershoot(jsonObject.getBoolean(JSON_NAME_USE_OVERSHOOT));
		setBlurredBorder(jsonObject.getBoolean(JSON_NAME_USE_BLURRED_BORDER));
	}

	@Override
	void onDraw(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {

		float progressRatio = mProgressInterpolator.getInterpolation(getProgressRatio());
		int index = measureIndex(progressRatio);

		PixelCanvas mainSrcCanvas = (mSpinOrder[index].equals(FORMER) ? srcCanvasFormer : srcCanvasLatter);
		PixelCanvas subSrcCanvas = (mSpinOrder[index + 1].equals(FORMER) ? srcCanvasFormer : srcCanvasLatter);
		boolean isHorizontal = mDirection.isHorizontal();
		boolean isNegative = mDirection.isNegative();
		boolean overshoot = mUseOvershoot && (index == mSpinCount - 1);

		int baseAxisLength = isHorizontal ? getWidth() : getHeight();
		float moveRatio = (progressRatio - mSpinSequence[index]) / mSpinBaseWeight;
		if (overshoot) {
			moveRatio /= OVERSHOOT_WEIGHT_MULTIPLIER;
			moveRatio = mOvershootInterpolator.getInterpolation(moveRatio);
		}
		int move = Math.round(baseAxisLength * moveRatio);
		int dstPoint = isNegative ? baseAxisLength - move : -baseAxisLength + move;

		if (isHorizontal) {
			mainSrcCanvas.copy(dstCanvas, isNegative ? -move : move, 0);
			subSrcCanvas.copy(dstCanvas, dstPoint, 0);

			if (overshoot)
				subSrcCanvas.copy(dstCanvas, isNegative ? baseAxisLength + dstPoint : -baseAxisLength + dstPoint, 0);
		} else {
			mainSrcCanvas.copy(dstCanvas, 0, isNegative ? -move : move);
			subSrcCanvas.copy(dstCanvas, 0, dstPoint);

			if (overshoot)
				subSrcCanvas.copy(dstCanvas, 0, isNegative ? baseAxisLength + dstPoint : -baseAxisLength + dstPoint);
		}

		if (mUseBlurredBorder) {
			PixelCanvas blurredBorder = getCanvas(0);
			int dst = (isNegative ? dstPoint : move) - mBlurredBorderOffset;
			float alpha = computeBlurredBorderAlphaMultiplier();

			if (isHorizontal) {
				blurredBorder.blend(dstCanvas, dst, 0, alpha);
				if (dst < 0)
					blurredBorder.blend(dstCanvas, dst + baseAxisLength, 0, alpha);
				else if (dst + mBlurredBorderOffset * 2 > baseAxisLength)
					blurredBorder.blend(dstCanvas, dst - baseAxisLength, 0, alpha);
			} else {
				getCanvas(0).blend(dstCanvas, 0, dst, alpha);
				if (dst < 0)
					blurredBorder.blend(dstCanvas, 0, dst + baseAxisLength, alpha);
				else if (dst + mBlurredBorderOffset * 2 > baseAxisLength)
					blurredBorder.blend(dstCanvas, 0, dst - baseAxisLength, alpha);
			}
		}
	}

	private int measureIndex(float progressRatio) {

		for (int i = mSpinSequence.length - 1; i >= 0; --i)
			if (mSpinSequence[i] <= progressRatio)
				return i;
		return Precondition.assureUnreachable();
	}

	private float computeBlurredBorderAlphaMultiplier() {

		float progressRatio = getProgressRatio();
		if (progressRatio < CRITICAL_VALUE_FADE_IN_BORDER) {
			return progressRatio / FADE_DURATION;
		} else if (progressRatio >= CRITICAL_VALUE_FADE_OUT_BORDER) {
			return 1.0f - (progressRatio - CRITICAL_VALUE_FADE_OUT_BORDER) / FADE_DURATION;
		}
		return 1.0f;
	}

	@Override
	Size[] getCanvasRequirement() {

		if (mUseBlurredBorder) {
			return new Size[] { mBlurredBorder.measureSize(getResolution(), null, mDirection.isHorizontal() ? 90.0f : null) };
		} else {
			return DO_NOT_NEED_CANVAS;
		}
	}

	@Override
	int getCacheCount() {
		return mUseBlurredBorder ? 1 : 0;
	}

	@Override
	void prepareCanvasWithCache() throws IOException {
		getCacheManager().decodeImageCache(getCacheCodeChunk(0), getCanvas(0));
	}

	@Override
	void prepareCanvasWithoutCache() throws IOException {
		PixelExtractUtils.extractARGB(createCacheAsBitmap(0), getCanvas(0), true);
	}

	@Override
	Bitmap createCacheAsBitmap(int index) throws IOException {

		if (mDirection.isHorizontal()) {
			return mBlurredBorder.createBitmap(getResolution(), null, 90.0f);
		} else {
			return mBlurredBorder.createBitmap(getResolution());
		}
	}

	void setSpinOrder(SceneOrder... spinOrder) {
		Precondition.checkArray(spinOrder).checkNotContainsNull();

		mSpinOrder = new SceneOrder[spinOrder.length + 2];
		mSpinOrder[0] = FORMER;
		mSpinOrder[mSpinOrder.length - 1] = LATTER;
		for (int i = 0; i != spinOrder.length; ++i) {
			mSpinOrder[i + 1] = spinOrder[i];
		}
		mSpinCount = mSpinOrder.length - 1;
		mSpinSequence = new float[mSpinCount];
	}

	void setSpinOrder(List<SceneOrder> spinOrder) {
		Precondition.checkCollection(spinOrder).checkNotContainsNull();
		setSpinOrder(spinOrder.toArray(new SceneOrder[0]));
	}

	/**
	 * 회전 순서를 정의한 배열을 반환합니다.
	 */
	public SceneOrder[] getSpinOrder() {
		return Arrays.copyOf(mSpinOrder, mSpinOrder.length);
	}

	void setDirection(Direction direction) {
		Precondition.checkNotNull(direction);
		mDirection = direction;
	}

	/**
	 * 회전 방향을 반환합니다.
	 */
	public Direction getDirection() {
		return mDirection;
	}

	void setInterpolator(InterpolatorType interpolatorType) {

		if (interpolatorType == null)
			interpolatorType = InterpolatorType.LINEAR;
		mProgressInterpolatorType = interpolatorType;
		mProgressInterpolator = interpolatorType.createInterpolator();
	}

	public InterpolatorType getInterpolator() {
		return mProgressInterpolatorType;
	}

	void setOvershoot(boolean useOvershoot) {
		mUseOvershoot = useOvershoot;
	}

	public boolean isUsingOvershoot() {
		return mUseOvershoot;
	}

	void setBlurredBorder(boolean useBlurredBorder) {
		mBlurredBorder = useBlurredBorder ? ImageResource.createFromDrawable(R.drawable.blurred_line, getResources(), Resolution.FHD) : null;
		mUseBlurredBorder = useBlurredBorder;
	}

	public boolean isUsingBlurredBorder() {
		return mUseBlurredBorder;
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link SpinTransition}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Transition.Editor<SpinTransition, Editor> {

		private Editor(SpinTransition spinTransition) {
			super(spinTransition);
		}

		/**
		 * 회전이 진행되며 나타날 {@code Scene}의 순서를 설정합니다.<br />
		 * <br />
		 * 단, 고정적으로 처음에 나타나는 {@code SceneOrder.FORMER}와 마지막에 나타나는 {@code SceneOrder.LATTER}를 별개로 하고,
		 * 설정한 순서가 그 중간에 삽입되는 구조로 작동합니다. 즉, 다음과 같이 설정했을 때,
		 * 
		 * <pre>
		 * transition.setSpinOrder(LATTER, FORMER);
		 * </pre>
		 * 
		 * 실제로 해당 {@code SpinTransition}은 다음과 같은 순서로 {@code Scene}이 노출되는 회전 효과를 적용할 것입니다.<br />
		 * - FORMER -> LATTER -> FORMER -> LATTER
		 * 
		 * @param spinOrder
		 *            설정할 순서를 나타내기 위한 목록.
		 * @see SceneOrder
		 */
		public Editor setSpinOrder(SceneOrder... spinOrder) {
			getObject().setSpinOrder(spinOrder);
			return this;
		}

		/**
		 * @see #setSpinOrder(SceneOrder...)
		 */
		public Editor setSpinOrder(List<SceneOrder> spinOrder) {
			getObject().setSpinOrder(spinOrder);
			return this;
		}

		/**
		 * 회전 방향을 설정합니다.
		 * 
		 * @param direction
		 *            설정할 {@code Direction} 객체.
		 * @see Direction
		 */
		public Editor setDirection(Direction direction) {
			getObject().setDirection(direction);
			return this;
		}

		public Editor setInterpolator(InterpolatorType interpolatorType) {
			getObject().setInterpolator(interpolatorType);
			return this;
		}

		public Editor setOvershoot(boolean useOvershoot) {
			getObject().setOvershoot(useOvershoot);
			return this;
		}

		public Editor setBlurredBorder(boolean useBlurredBorder) {
			getObject().setBlurredBorder(useBlurredBorder);
			return this;
		}
	}

	/**
	 * 회전 방향을 구분하기 위한 열거형.
	 * 
	 * @see #LEFT
	 * @see #UP
	 * @see #RIGHT
	 * @see #DOWN
	 */
	public static enum Direction {

		LEFT,

		UP,

		RIGHT,

		DOWN;

		boolean isHorizontal() {
			return equals(LEFT) || equals(RIGHT);
		}

		boolean isNegative() {
			return equals(LEFT) || equals(UP);
		}
	}
}
