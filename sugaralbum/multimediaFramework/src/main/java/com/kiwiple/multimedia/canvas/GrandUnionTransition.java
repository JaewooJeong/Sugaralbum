package com.kiwiple.multimedia.canvas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.json.JSONException;

import android.animation.TimeInterpolator;
import android.graphics.Color;
import android.graphics.Rect;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.annotation.RiValue;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.math.interpolator.ExponentialInOutInterpolator;
import com.kiwiple.multimedia.util.Range;

/**
 * GrandUnionTransition.
 */
public final class GrandUnionTransition extends Transition {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "grand_union_transition";

	public static final String JSON_NAME_BLOCK_INTERVAL = "block_interval";
	public static final String JSON_NAME_LINE_WIDTH = "line_width";
	public static final String JSON_NAME_LINE_COLOR = "line_color";
	public static final String JSON_NAME_SPREAD_SEQUENCE = "spread_sequence";
	public static final String JSON_NAME_USE_FADE_IN = "use_fade_in";

	private static final float START_MERGE_POSITION = 0.5f;
	private static final float LINE_ENTER_DURATION = 0.4f;
	private static final float SPREAD_BLOCK_DURATION = 1.0f - LINE_ENTER_DURATION;
	private static final float MERGE_BLOCK_DURATION = 0.8f;
	private static final float LINE_EXIT_DURATION = 1.0f - MERGE_BLOCK_DURATION;

	private static final float SIDE_BLOCK_OVERLAP_RATIO = 0.02f;

	public static final int BLOCK_COUNT = 4;

	private static final float DEFAULT_BLOCK_INTERVAL = 0.0f;
	private static final int DEFAULT_LINE_WIDTH_PX = 1;
	private static final int DEFAULT_LINE_COLOR = Color.BLACK;
	private static final boolean DEFAULT_USE_FADE_IN = false;

	private static final int TINT_COLOR = 0x00ffffff;
	private static final float START_TINT_ALPHA = 0.8f;
	private static final float END_TINT_ALPHA = 0.0f;

	// // // // // Member variable.
	// // // // //
	private final ArrayList<Rect> mBlocks;
	private final ArrayList<Integer> mSpreadSequence;
	private final ArrayList<Range> mSpreadProgressRanges;

	private final TimeInterpolator mInterpolator;

	private int mBlockCount;
	private int mLeftSideBlockIndex;
	private int mRightSideBlockIndex;
	private float mInterval;

	@RiValue
	private float mLineWidthPx;
	private int mLineColor;

	private int mOverlapOffset;

	private boolean mUseFadeIn;

	// // // // // Constructor.
	// // // // //
	{
		mBlockCount = BLOCK_COUNT;
		mLeftSideBlockIndex = 0;
		mRightSideBlockIndex = mBlockCount - 1;

		mBlocks = new ArrayList<>(mBlockCount);
		mSpreadSequence = new ArrayList<>(mBlockCount);
		mSpreadProgressRanges = new ArrayList<>(mBlockCount);

		mInterpolator = new ExponentialInOutInterpolator();

		injectPreset(Preset.DEFAULT);
	}

	GrandUnionTransition(Region parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	void onDraw(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {

		float progressRatio = getProgressRatio();
		if (progressRatio < START_MERGE_POSITION) {
			srcCanvasFormer.copy(dstCanvas);
			drawBlockSpreading(srcCanvasFormer, srcCanvasLatter, dstCanvas);
		} else {
			drawBlockMerging(srcCanvasFormer, srcCanvasLatter, dstCanvas);
		}
	}

	void drawBlockSpreading(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {

		int height = getHeight();
		float position = getPosition();

		for (int i = 0; i != mBlockCount; ++i) {
			Range spreadRange = mSpreadProgressRanges.get(i);
			if (spreadRange.start > position) {
				break;
			}

			int blockIndex = mSpreadSequence.get(i);
			Rect block = mBlocks.get(blockIndex);
			int centerX = block.centerX();
			float spreadProgressRatio = Math.min(1.0f, position / spreadRange.end);

			if (spreadProgressRatio < LINE_ENTER_DURATION) {
				float innerRatio = mInterpolator.getInterpolation(spreadProgressRatio / LINE_ENTER_DURATION);
				int centerY = block.centerY();
				int startY = makeProgress(centerY, block.top, innerRatio);
				int endY = makeProgress(centerY, block.bottom, innerRatio);

				dstCanvas.drawLine(mLineColor, centerX, startY, centerX, endY, mLineWidthPx);
			} else {
				float innerRatio = mInterpolator.getInterpolation((spreadProgressRatio - LINE_ENTER_DURATION) / SPREAD_BLOCK_DURATION);
				int leftSideX = makeProgress(centerX, block.left, innerRatio);
				int rightSideX = makeProgress(centerX, block.right, innerRatio);
				int blockWidth = rightSideX - leftSideX;
				int overlapOffset = (blockIndex == mLeftSideBlockIndex) ? mOverlapOffset : (blockIndex == mRightSideBlockIndex) ? -mOverlapOffset : 0;

				srcCanvasLatter.copy(dstCanvas, leftSideX + overlapOffset, block.top, leftSideX, block.top, blockWidth, height);
				if (mUseFadeIn) {
					int tintAlpha = Math.round(0xff * makeProgress(START_TINT_ALPHA, END_TINT_ALPHA, innerRatio)) << 24;
					dstCanvas.tint(tintAlpha | TINT_COLOR, leftSideX, 0, blockWidth, height);
				}

				if (blockIndex == mLeftSideBlockIndex) {
					leftSideX -= Math.round(makeProgress(0.0f, mLineWidthPx, innerRatio));
				} else if (blockIndex == mRightSideBlockIndex) {
					rightSideX += Math.round(makeProgress(0.0f, mLineWidthPx, innerRatio));
				}
				dstCanvas.drawLine(mLineColor, leftSideX, block.top, leftSideX, block.bottom, mLineWidthPx);
				dstCanvas.drawLine(mLineColor, rightSideX, block.top, rightSideX, block.bottom, mLineWidthPx);
			}
		}
	}

	void drawBlockMerging(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {

		int height = getHeight();
		int width = getWidth();
		int halfWidth = Math.round(width / 2.0f);
		float progressRatio = (getProgressRatio() - START_MERGE_POSITION) * 2.0f;

		if (progressRatio < MERGE_BLOCK_DURATION) {
			progressRatio /= MERGE_BLOCK_DURATION;
			{
				int copySrcX = mBlocks.get(mLeftSideBlockIndex + 1).left;
				int copyWidth = mBlocks.get(mRightSideBlockIndex - 1).right - copySrcX;
				srcCanvasLatter.copy(dstCanvas, copySrcX, 0, copySrcX, 0, copyWidth, height);
			}
			float leftSideProgressRatio = mInterpolator.getInterpolation(Math.min(1.0f, progressRatio / 0.5f));
			float rightSideProgressRatio = mInterpolator.getInterpolation(Math.max(0.0f, (progressRatio - 0.5f) / 0.5f));
			{
				Rect leftBlock = mBlocks.get(mLeftSideBlockIndex);
				int leftOverlapOffset = makeProgress(mOverlapOffset, 0, leftSideProgressRatio);
				int leftBlockWidth = makeProgress(leftBlock.width(), halfWidth, leftSideProgressRatio);
				srcCanvasLatter.copy(dstCanvas, 0 + leftOverlapOffset, 0, 0, 0, leftBlockWidth, height);
				dstCanvas.drawLine(mLineColor, leftBlockWidth, 0, leftBlockWidth, height, mLineWidthPx);
			}
			{
				Rect rightBlock = mBlocks.get(mRightSideBlockIndex);
				int rightOverlapOffset = makeProgress(-mOverlapOffset, 0, rightSideProgressRatio);
				int rightBlockWidth = makeProgress(rightBlock.width(), halfWidth, rightSideProgressRatio);
				int rightBlockDstX = width - rightBlockWidth;
				srcCanvasLatter.copy(dstCanvas, rightBlockDstX + rightOverlapOffset, 0, rightBlockDstX, 0, rightBlockWidth, height);
				dstCanvas.drawLine(mLineColor, rightBlockDstX, 0, rightBlockDstX, height, mLineWidthPx);
			}
			dstCanvas.drawLine(mLineColor, halfWidth, 0, halfWidth, height, mLineWidthPx);
		} else {
			progressRatio = mInterpolator.getInterpolation((progressRatio - MERGE_BLOCK_DURATION) / LINE_EXIT_DURATION);

			int centerY = Math.round(height / 2.0f);
			int startY = makeProgress(0, centerY, progressRatio);
			int endY = makeProgress(height, centerY, progressRatio);
			srcCanvasLatter.copy(dstCanvas);
			dstCanvas.drawLine(mLineColor, halfWidth, startY, halfWidth, endY, mLineWidthPx);
		}
	}

	@Override
	Change[] getSensitivities() {
		return new Change[] { Change.DURATION, Change.SIZE };
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {

		mBlocks.clear();

		int duration = Math.round(getDuration() * START_MERGE_POSITION);
		float blockWidth = getWidth() / (float) mBlockCount;

		int spreadDuration = makeProgress(duration / mBlockCount, duration, 1.0f - mInterval);
		int spreadStartPositionOffset = (duration - spreadDuration) / (mBlockCount - 1);

		for (int i = 0; i != mBlockCount; ++i) {
			int left = Math.round(blockWidth * i);
			int right = Math.round(blockWidth * (i + 1));
			mBlocks.add(new Rect(left, 0, right, getHeight()));

			int startPosition = spreadStartPositionOffset * i;
			mSpreadProgressRanges.add(Range.closed(startPosition, startPosition + spreadDuration));
		}
		mOverlapOffset = Math.round(getWidth() * SIDE_BLOCK_OVERLAP_RATIO);
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_BLOCK_INTERVAL, mInterval);
		jsonObject.put(JSON_NAME_LINE_WIDTH, mLineWidthPx);
		jsonObject.put(JSON_NAME_LINE_COLOR, mLineColor);
		jsonObject.put(JSON_NAME_USE_FADE_IN, mUseFadeIn);
		jsonObject.put(JSON_NAME_SPREAD_SEQUENCE, mSpreadSequence);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setBlockInterval(jsonObject.getFloat(JSON_NAME_BLOCK_INTERVAL));
		setLineWidth(jsonObject.getFloat(JSON_NAME_LINE_WIDTH));
		setLineColor(jsonObject.getInt(JSON_NAME_LINE_COLOR));
		setFadeIn(jsonObject.getBoolean(JSON_NAME_USE_FADE_IN));
		setSpreadSequence(jsonObject.getJSONArrayAsList(JSON_NAME_SPREAD_SEQUENCE, Integer.class));
	}

	void setBlockInterval(float interval) {
		Precondition.checkNotNegative(interval);
		mInterval = (interval >= 1.0f ? 1.0f : interval);
	}

	public float getBlockInterval() {
		return mInterval;
	}

	void setLineWidth(float lineWidthPx) {
		Precondition.checkOnlyPositive(lineWidthPx);
		mLineWidthPx = lineWidthPx;
	}

	/**
	 * 경계선의 두께를 픽셀 단위로써 반환합니다.
	 */
	public float getLineWidth() {
		return mLineWidthPx;
	}

	void setLineColor(int lineColor) {
		mLineColor = lineColor;
	}

	/**
	 * 경계선의 색상을 반환합니다.
	 * 
	 * @return {@code #AARRGGBB} 형식의 경계선 색상.
	 */
	public int getLineColor() {
		return mLineColor;
	}

	void setSpreadSequence(Integer... indexes) {
		Precondition.checkArray(indexes).checkLength(mBlockCount, mBlockCount).checkNotContainsNull();
		setSpreadSequence(Arrays.asList(indexes));
	}

	void setSpreadSequence(List<Integer> indexes) {
		Range indexRange = Range.closedOpen(0, mBlockCount);
		Precondition.checkCollection(indexes).checkSize(mBlockCount, mBlockCount).checkNotContainsNull();
		Precondition.checkArgument(indexRange.toList().containsAll(indexes), "indexes must have the numbers 0 to " + mBlockCount);

		mSpreadSequence.clear();
		mSpreadSequence.addAll(indexes);
	}

	public List<Integer> getSpreadSequence() {
		return new ArrayList<Integer>(mSpreadSequence);
	}

	void setFadeIn(boolean useFadeIn) {
		mUseFadeIn = useFadeIn;
	}

	public boolean isUsingFadeIn() {
		return mUseFadeIn;
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link GrandUnionTransition}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Transition.Editor<GrandUnionTransition, Editor> {

		private Editor(GrandUnionTransition grandUnionTransition) {
			super(grandUnionTransition);
		}

		public Editor setBlockInterval(float interval) {
			getObject().setBlockInterval(interval);
			return this;
		}

		/**
		 * 경계선의 두께를 픽셀 단위로써 설정합니다.
		 * 
		 * @param lineWidthPx
		 *            픽셀 단위의 경계선 두께.
		 */
		public Editor setLineWidth(float lineWidthPx) {
			getObject().setLineWidth(lineWidthPx);
			return this;
		}

		/**
		 * 경계선의 색상을 설정합니다.
		 * 
		 * @param lineColor
		 *            {@code #AARRGGBB} 형식의 출력 색상.
		 */
		public Editor setLineColor(int lineColor) {
			getObject().setLineColor(lineColor);
			return this;
		}

		public Editor setSpreadSequence(Integer... indexes) {
			getObject().setSpreadSequence(indexes);
			return this;
		}

		public Editor setSpreadSequence(List<Integer> indexes) {
			getObject().setSpreadSequence(indexes);
			return this;
		}

		public Editor setFadeIn(boolean useFadeIn) {
			getObject().setFadeIn(useFadeIn);
			return this;
		}
	}

	// // // // // Enumeration.
	// // // // //
	public static enum Preset implements IPreset<Editor> {

		DEFAULT,

		BEER_CAN;

		@Override
		public void inject(Editor editor, float magnification) {

			switch (this) {
				case BEER_CAN:
					editor.setBlockInterval(0.4f);
					editor.setLineWidth(4.0f * magnification);
					editor.setLineColor(0xffff9d26);
					editor.setFadeIn(true);
					List<Integer> spreadSequence = Range.closedOpen(0, BLOCK_COUNT).toList();
					Collections.shuffle(spreadSequence);
					editor.setSpreadSequence(spreadSequence);
					break;

				case DEFAULT:
				default:
					editor.setBlockInterval(DEFAULT_BLOCK_INTERVAL);
					editor.setLineWidth(DEFAULT_LINE_WIDTH_PX * magnification);
					editor.setLineColor(DEFAULT_LINE_COLOR);
					editor.setFadeIn(DEFAULT_USE_FADE_IN);
					editor.setSpreadSequence(Range.closedOpen(0, BLOCK_COUNT).toList());
					break;
			}
		}
	}
}