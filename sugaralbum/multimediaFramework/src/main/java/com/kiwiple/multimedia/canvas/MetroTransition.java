package com.kiwiple.multimedia.canvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

import org.json.JSONException;

import android.animation.TimeInterpolator;
import android.graphics.Rect;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Range;
import com.kiwiple.multimedia.util.Size;

/**
 * 전 {@code Scene}을 후 {@code Scene}으로 덮어버리는 방식의 전환 효과를 연출하는 클래스.
 * <p />
 * 전 {@code Scene}을 바탕에 깔고, 흑백 처리된 후 {@code Scene}의 이미지를 지정된 개수만큼 조각내어 임의의 순서에 따라 순차적으로 진입시킨 후에 흑백
 * 처리되지 않은 본래의 이미지를 진입시킵니다. 조각낼 이미지의 개수뿐 아니라, 진입하는 방향, 순서 배정 방식 등을 지정할 수 있습니다.
 */
public final class MetroTransition extends Transition {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "metro_transition";

	private static final String JSON_NAME_INTERNAL_SEED = "internal_seed";
	public static final String JSON_NAME_SLICE_ORDER = "slice_order";
	public static final String JSON_NAME_SLICE_COUNT = "slice_count";
	public static final String JSON_NAME_LINE_COLOR = "line_color";
	public static final String JSON_NAME_DIRECTION = "direction";

	private static final float GAP_BETWEEN_FIRST_AND_LAST_SLICE = 0.2f;
	private static final float SLICE_DURATION = 0.75f;
	private static final float SLICE_MOVE_DURATION = 0.8f;
	private static final float ORIGINAL_START_POSITION = 1.0f - SLICE_DURATION;

	private static final Direction DEFAULT_DIRECTION = Direction.LEFT;
	private static final SliceOrder DEFAULT_SLICE_ORDER = SliceOrder.NON_SEQUENTIAL_RANDOMIZED;
	private static final int DEFAULT_SLICE_COUNT = 5;
	private static final float DEFAULT_LINE_WIDTH = 3.0f;

	// // // // // Member variable.
	// // // // //
	private final TimeInterpolator mInterpolator = new AccelerateDecelerateInterpolator();

	private final ArrayList<Integer> mRandomSequence = new ArrayList<Integer>();

	private final Rect mRectSrc = new Rect();
	private final Rect mRectDst = new Rect();

	private int mSeed;

	private Direction mDirection;
	private SliceOrder mSliceOrder;

	private int mLineColor;
	private float mLineWidth;

	private int mSliceCount;
	private float mSliceInterval;

	private boolean mNeedToChangeSeed;

	// // // // // Static method.
	// // // // //
	private static void copyPixelCanvasAsTrapezoidByRect(PixelCanvas srcCanvas, PixelCanvas dstCanvas, Rect srcRect, Rect dstRect, float triangleOffsetRatio, Direction direction) {

		int srcStride = srcCanvas.getImageWidth();
		int dstStride = dstCanvas.getImageWidth();

		int rectWidth = srcRect.width();
		int rectHeight = srcRect.height();

		boolean isHorizontal = direction.isHorizontal();
		boolean isNegative = direction.isNegative();

		int triangleOffset = Math.round((isHorizontal ? rectHeight / 2.0f : rectWidth) * triangleOffsetRatio);

		if (isHorizontal) {
			for (int i = 0; i < rectHeight; ++i) {

				int triangleArea = i / 2;
				int copyWidth = rectWidth - triangleArea + triangleOffset;

				int offsetX = isNegative ? triangleArea - triangleOffset : 0;
				if (offsetX < 0) {
					offsetX = 0;
				}

				if (dstRect.left + copyWidth > dstStride) {
					copyWidth = dstStride - dstRect.left;
				}
				if (copyWidth <= 0) {
					continue;
				}

				int srcOffset = srcStride * (i + srcRect.top) + srcRect.left + offsetX;
				int dstOffset = dstStride * (i + dstRect.top) + dstRect.left + offsetX;

				System.arraycopy(srcCanvas.intArray, srcOffset, dstCanvas.intArray, dstOffset, copyWidth);
			}
		} else {
			int endY = rectHeight - 1;
			for (int i = 0; i < rectHeight; ++i) {

				int index = isNegative ? i : endY - i;
				int triangleArea = rectWidth - Math.round(i / 0.5f);
				int copyWidth = rectWidth - triangleArea + triangleOffset;

				int offsetX = isNegative ? triangleArea - triangleOffset : 0;
				if (offsetX < 0) {
					offsetX = 0;
				}

				if (copyWidth > rectWidth) {
					copyWidth = rectWidth;
				}
				if (copyWidth <= 0) {
					continue;
				}

				int srcOffset = srcStride * (index + srcRect.top) + srcRect.left + offsetX;
				int dstOffset = dstStride * (index + dstRect.top) + dstRect.left + offsetX;

				System.arraycopy(srcCanvas.intArray, srcOffset, dstCanvas.intArray, dstOffset, copyWidth);
			}
		}
	}

	// // // // // Constructor.
	// // // // //
	{
		setDirection(DEFAULT_DIRECTION);
		setSliceOrder(DEFAULT_SLICE_ORDER);
		setSliceCount(DEFAULT_SLICE_COUNT);
	}

	MetroTransition(Region parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	void onDraw(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {

		float progressRatio = getProgressRatio();

		PixelCanvas grayPixelsLatter = getCanvas(0);
		srcCanvasLatter.deepCopy(grayPixelsLatter);
		PixelUtils.applyGrayscale(grayPixelsLatter);
		srcCanvasFormer.copy(dstCanvas);

		int width = getWidth();
		int height = getHeight();
		boolean isHorizontal = mDirection.isHorizontal();
		boolean isNegative = mDirection.isNegative();

		drawGraySlice(grayPixelsLatter, dstCanvas, progressRatio, width, height, isHorizontal, isNegative);
		if (progressRatio >= ORIGINAL_START_POSITION) {
			drawNextScene(srcCanvasLatter, dstCanvas, progressRatio, width, height, isHorizontal, isNegative);
		}
	}

	private void drawGraySlice(PixelCanvas srcCanvas, PixelCanvas dstCanvas, float progressRatio, int width, int height, boolean isHorizontal, boolean isNegative) {

		float sliceSize = (isHorizontal ? (float) height : (float) width) / mSliceCount;
		boolean isSequential = mSliceOrder.isSequential();

		for (int i = 0; i != mSliceCount; ++i) {

			float sliceStartPosition = i * mSliceInterval;
			if (progressRatio < sliceStartPosition) {
				continue;
			}

			float innerRatio = (progressRatio - sliceStartPosition) / SLICE_DURATION;
			innerRatio = mInterpolator.getInterpolation(innerRatio);

			float innerRatioSlice = Math.min(innerRatio / SLICE_MOVE_DURATION, 1.0f);
			float innerRatioTrapezoid = 0.0f;
			if (innerRatio > SLICE_MOVE_DURATION) {
				innerRatioTrapezoid = Math.min((innerRatio - SLICE_MOVE_DURATION) / 0.2f, 1.0f);
			}

			int stepIndex = i;
			if (!isSequential) {
				stepIndex = mRandomSequence.get(i);
			}

			if (isHorizontal) {
				int stepWidth = Math.round(width * innerRatioSlice);

				mRectSrc.top = mRectDst.top = Math.round(sliceSize * stepIndex);
				mRectSrc.bottom = mRectDst.bottom = Math.round(sliceSize * (stepIndex + 1));

				if (isNegative) {
					mRectSrc.left = 0;
					mRectSrc.right = stepWidth;
					mRectDst.left = width - stepWidth;
					mRectDst.right = width;
				} else {
					mRectSrc.left = width - stepWidth;
					mRectSrc.right = width;
					mRectDst.left = 0;
					mRectDst.right = stepWidth;
				}
			} else {
				int stepHeight = Math.round(height * innerRatioSlice);

				mRectSrc.left = mRectDst.left = Math.round(sliceSize * stepIndex);
				mRectSrc.right = mRectDst.right = Math.round(sliceSize * (stepIndex + 1));

				if (isNegative) {
					mRectSrc.top = 0;
					mRectSrc.bottom = stepHeight;
					mRectDst.top = height - stepHeight;
					mRectDst.bottom = height;
				} else {
					mRectSrc.top = height - stepHeight;
					mRectSrc.bottom = height;
					mRectDst.top = 0;
					mRectDst.bottom = stepHeight;
				}
			}
			copyPixelCanvasAsTrapezoidByRect(srcCanvas, dstCanvas, mRectSrc, mRectDst, innerRatioTrapezoid, mDirection);
		}
	}

	private void drawNextScene(PixelCanvas srcCanvas, PixelCanvas dstCanvas, float progressRatio, int width, int height, boolean isHorizontal, boolean isNegative) {

		float innerRatio = (progressRatio - ORIGINAL_START_POSITION) / SLICE_DURATION;
		innerRatio = mInterpolator.getInterpolation(innerRatio);

		float innerRatioSlice = Math.min(innerRatio / SLICE_MOVE_DURATION, 1.0f);

		float innerRatioTrapezoid = 0.0f;
		if (innerRatio > SLICE_MOVE_DURATION) {
			innerRatioTrapezoid = Math.min((innerRatio - SLICE_MOVE_DURATION) / 0.2f, 1.0f);
		}

		if (isHorizontal) {
			int sliceWidth = Math.round(width * innerRatioSlice);

			mRectSrc.top = mRectDst.top = 0;
			mRectSrc.bottom = mRectDst.bottom = height;

			if (isNegative) {
				mRectSrc.left = 0;
				mRectSrc.right = sliceWidth;
				mRectDst.left = width - sliceWidth;
				mRectDst.right = width;
			} else {
				mRectSrc.left = width - sliceWidth;
				mRectSrc.right = width;
				mRectDst.left = 0;
				mRectDst.right = sliceWidth;
			}

		} else {
			int sliceHeight = Math.round(height * innerRatioSlice);

			mRectSrc.left = mRectDst.left = 0;
			mRectSrc.right = mRectDst.right = width;

			if (isNegative) {
				mRectSrc.top = 0;
				mRectSrc.bottom = sliceHeight;
				mRectDst.top = height - sliceHeight;
				mRectDst.bottom = height;
			} else {
				mRectSrc.top = height - sliceHeight;
				mRectSrc.bottom = height;
				mRectDst.top = 0;
				mRectDst.bottom = sliceHeight;
			}
		}
		copyPixelCanvasAsTrapezoidByRect(srcCanvas, dstCanvas, mRectSrc, mRectDst, innerRatioTrapezoid, mDirection);
	}

	@SuppressWarnings("unused")
	private void drawLine(PixelCanvas dstCanvas, int width, int height, float innerRatioTrapezoid, boolean isHorizontal, boolean isNegative) {

		int triangleArea = isHorizontal ? mRectSrc.height() / 2 : mRectSrc.width() / 2;
		int triangleOffset = Math.round(triangleArea * innerRatioTrapezoid);

		if (isHorizontal) {
			int startX = isNegative ? mRectDst.left - triangleOffset : mRectDst.right + triangleOffset;
			int endX = startX + (isNegative ? triangleArea : -triangleArea);
			dstCanvas.drawLine(mLineColor, startX, 0, endX, height, mLineWidth);
		} else {

			if (isNegative) {
				int endY = mRectDst.top - triangleOffset;
				int startY = endY + triangleArea;
				dstCanvas.drawLine(mLineColor, 0, startY, width, endY, mLineWidth);
			} else {
				int startY = mRectDst.bottom + triangleOffset;
				int endY = startY - triangleArea;
				dstCanvas.drawLine(mLineColor, 0, startY, width, endY, mLineWidth);
			}
		}
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	void onPrepare() {
		mLineWidth = DEFAULT_LINE_WIDTH * getResolution().magnification;
	}

	@Override
	Size[] getCanvasRequirement() {
		return new Size[] { getSize() };
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_SLICE_ORDER, mSliceOrder);
		jsonObject.put(JSON_NAME_SLICE_COUNT, mSliceCount);
		jsonObject.put(JSON_NAME_INTERNAL_SEED, mSeed);
		jsonObject.put(JSON_NAME_DIRECTION, mDirection);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		mSeed = jsonObject.getInt(JSON_NAME_INTERNAL_SEED);
		setSliceCount(jsonObject.getInt(JSON_NAME_SLICE_COUNT));
		setSliceOrder(jsonObject.getEnum(JSON_NAME_SLICE_ORDER, SliceOrder.class));
		setDirection(jsonObject.getEnum(JSON_NAME_DIRECTION, Direction.class));
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {
		if (!mSliceOrder.isSequential()) {
			createRandomSequence();
		}
	}

	void setDirection(Direction direction) {
		Precondition.checkNotNull(direction);
		mDirection = direction;
	}

	/**
	 * 후 {@link Scene}의 이미지의 진입 방향을 반환합니다.
	 */
	public Direction getDirection() {
		return mDirection;
	}

	void setSliceOrder(SliceOrder sliceOrder) {
		Precondition.checkNotNull(sliceOrder);
		mSliceOrder = sliceOrder;
	}

	/**
	 * 흑백 처리된 후 {@link Scene}의 이미지 조각의 진입 순서 배정 방식을 반환합니다.
	 */
	public SliceOrder getSliceOrder() {
		return mSliceOrder;
	}

	void setSliceCount(int sliceCount) {
		Precondition.checkOnlyPositive(sliceCount);

		mSliceCount = sliceCount;
		mSliceInterval = GAP_BETWEEN_FIRST_AND_LAST_SLICE / mSliceCount;
	}

	/**
	 * 흑백 처리된 후 {@link Scene}의 이미지 조각 개수를 반환합니다.
	 */
	public int getSliceCount() {
		return mSliceCount;
	}

	void setLineColor(int lineColor) {
		mLineColor = lineColor;
	}

	/**
	 * 흑백 처리된 후 {@link Scene}의 이미지와 원본 이미지 사이의 경계선 색상을 반환합니다.
	 */
	public int getLineColor() {
		return mLineColor;
	}

	private void createRandomSequence() {

		mRandomSequence.clear();
		for (Integer index : Range.closedOpen(0, mSliceCount)) {
			mRandomSequence.add(index);
		}
		if (mNeedToChangeSeed) {
			mSeed = new Random().nextInt();
			mNeedToChangeSeed = false;
		}
		Collections.shuffle(mRandomSequence, new Random(mSeed));
	}

	private MetroTransition signalToChangeSeed() {
		mNeedToChangeSeed = true;
		return this;
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link MetroTransition}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Transition.Editor<MetroTransition, Editor> {

		private Editor(MetroTransition metroTransition) {
			super(metroTransition);
		}

		/**
		 * 후 {@link Scene}의 이미지가 진입할 방향을 설정합니다.
		 * 
		 * @param direction
		 *            설정할 {@code Direction} 객체.
		 * @see Direction
		 */
		public Editor setDirection(Direction direction) {
			getObject().signalToChangeSeed().setDirection(direction);
			return this;
		}

		/**
		 * 흑백 처리된 후 {@link Scene}의 이미지 조각의 진입 순서 배정 방식을 설정합니다.
		 * 
		 * @param sliceOrder
		 *            설정할 {@code SliceOrder} 객체.
		 * @see SliceOrder
		 */
		public Editor setSliceOrder(SliceOrder sliceOrder) {
			getObject().signalToChangeSeed().setSliceOrder(sliceOrder);
			return this;
		}

		/**
		 * 흑백 처리된 후 {@link Scene}의 이미지를 몇 개로 조각낼 것인지 설정합니다.
		 * 
		 * @param sliceCount
		 *            이미지 조각의 개수.
		 */
		public Editor setSliceCount(int sliceCount) {
			getObject().signalToChangeSeed().setSliceCount(sliceCount);
			return this;
		}

		/**
		 * 흑백 처리된 후 {@link Scene}의 이미지와 원본 이미지 사이의 경계선 색상을 설정합니다.
		 * 
		 * @param lineColor
		 *            {@code #AARRGGBB} 형식의 출력 색상.
		 */
		public Editor setLineColor(int lineColor) {
			getObject().setLineColor(lineColor);
			return this;
		}
	}

	// // // // // Enumeration.
	// // // // //
	/**
	 * 흑백 처리된 후 {@link Scene}의 이미지 조각의 진입 순서 배정 방식을 구분하기 위한 열거형.
	 * 
	 * @see #SEQUENTIAL_FROM_LEFT_OR_TOP
	 * @see #SEQUENTIAL_FROM_RIGHT_OR_BOTTOM
	 * @see #NON_SEQUENTIAL_RANDOMIZED
	 */
	public static enum SliceOrder {

		/**
		 * 이미지 조각의 순서를 좌상단을 기준으로 하여 우하단까지 순차적으로 배정함을 의미합니다.
		 */
		SEQUENTIAL_FROM_LEFT_OR_TOP,

		/**
		 * 이미지 조각의 순서를 우하단을 기준으로 하여 좌상단까지 순차적으로 배정함을 의미합니다.
		 */
		SEQUENTIAL_FROM_RIGHT_OR_BOTTOM,

		/**
		 * 이미지 조각의 순서를 무작위적으로 배정함을 의미합니다.
		 */
		NON_SEQUENTIAL_RANDOMIZED;

		/**
		 * 이미지 조각의 순서가 순차적인지의 여부를 반환합니다.
		 * 
		 * @return 순차적일 때 true.
		 */
		public boolean isSequential() {
			return !equals(NON_SEQUENTIAL_RANDOMIZED);
		}
	}

	/**
	 * 후 {@link Scene}의 이미지의 진입 방향을 구분하기 위한 열거형.
	 * 
	 * @see #LEFT
	 * @see #TOP
	 * @see #RIGHT
	 * @see #BOTTOM
	 */
	public static enum Direction {

		LEFT(180),

		UP(270),

		RIGHT(0),

		DOWN(90);

		final int degree;

		private Direction(int degree) {
			this.degree = degree;
		}

		boolean isHorizontal() {
			return equals(LEFT) || equals(RIGHT);
		}

		boolean isNegative() {
			return equals(LEFT) || equals(UP);
		}
	}
}
