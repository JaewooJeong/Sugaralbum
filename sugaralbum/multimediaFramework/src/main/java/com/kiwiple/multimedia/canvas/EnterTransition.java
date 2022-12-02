package com.kiwiple.multimedia.canvas;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;

import android.graphics.Color;
import android.graphics.Rect;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.annotation.RiValue;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.IJsonConvertible;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * EnterTransition.
 */
public final class EnterTransition extends Transition {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "enter_transition";

	public static final String JSON_NAME_BLOCKS = "blocks";
	public static final String JSON_NAME_VIEWPORT = "viewport";
	public static final String JSON_NAME_DIRECTION = "direction";
	public static final String JSON_NAME_LINE_COLOR = "line_color";
	public static final String JSON_NAME_LINE_THICKNESS = "line_thickness";
	public static final String JSON_NAME_IS_REVERSE = "is_reverse";

	private static final Viewport DEFAULT_VIEWPORT = Viewport.FULL_VIEWPORT;
	private static final Direction DEFAULT_DIRECTION = Direction.ONE_WAY_LEFT;
	private static final float DEFAULT_LINE_THICKNESS = 1.0f;
	private static final int DEFAULT_LINE_COLOR = Color.TRANSPARENT;
	private static final boolean DEFAULT_REVERSE = false;

	// // // // // Member variable.
	// // // // //
	private final List<Block> mBlocks;

	@RiValue
	private float mLineThickness;
	private int mLineColor;
	private boolean mDrawLine;

	private boolean mIsReverse;

	// // // // // Constructor.
	// // // // //
	{
		mBlocks = new ArrayList<>();

		addBlock(DEFAULT_VIEWPORT, DEFAULT_DIRECTION);
		setLineColor(DEFAULT_LINE_COLOR);
		setLineThickness(DEFAULT_LINE_THICKNESS);
		setReverse(DEFAULT_REVERSE);
	}

	EnterTransition(Region parent) {
		super(parent);
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
		jsonObject.put(JSON_NAME_BLOCKS, mBlocks);
		jsonObject.put(JSON_NAME_LINE_COLOR, mLineColor);
		jsonObject.put(JSON_NAME_LINE_THICKNESS, mLineThickness);
		jsonObject.put(JSON_NAME_IS_REVERSE, mIsReverse);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setBlocks(jsonObject.getJSONArrayAsList(JSON_NAME_BLOCKS, Block.class));
		setLineColor(jsonObject.getInt(JSON_NAME_LINE_COLOR));
		setLineThickness(jsonObject.getFloat(JSON_NAME_LINE_THICKNESS));
		setReverse(jsonObject.optBoolean(JSON_NAME_IS_REVERSE));
	}

	@Override
	Change[] getSensitivities() {
		return new Change[] { Change.SIZE };
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {

		checkValidity(!mBlocks.isEmpty(), "mBlocks is empty.");

		Size size = getSize();

		int maxBlockSize = 0;
		for (Block block : mBlocks) {
			Rect rect = block.rect;

			rect.set(block.viewport.asActualSizeRect(size));
			maxBlockSize = Math.max(maxBlockSize, Math.round(rect.width() * rect.height()));
		}
		mDrawLine = (mLineColor >>> 24) > 0 && mLineThickness > 0.0f;
	}

	@Override
	void onDraw(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {

		float progressRatio = mIsReverse ? 1.0f - getProgressRatio() : getProgressRatio();
		(mIsReverse ? srcCanvasLatter : srcCanvasFormer).copy(dstCanvas);
		PixelCanvas frontCanvas = mIsReverse ? srcCanvasFormer : srcCanvasLatter;

		for (Block block : mBlocks) {
			switch (block.direction.wayCount) {
				case 1:
					drawOneWay(block, frontCanvas, dstCanvas, progressRatio);
					break;
				case 2:
					drawTwoWay(block, frontCanvas, dstCanvas, progressRatio);
					break;
				default:
					Precondition.assureUnreachable();
			}
		}
	}

	private void drawOneWay(Block block, PixelCanvas srcCanvas, PixelCanvas dstCanvas, float progressRatio) {

		Rect rect = block.rect;
		Direction direction = block.direction;

		int width = rect.width();
		int height = rect.height();

		if (direction.isHorizontal()) {
			int move = Math.round(width * progressRatio);
			int crop = width - move;
			if (direction.isNegative())
				srcCanvas.copy(dstCanvas, rect.left, rect.top, rect.left + crop, rect.top, move, height);
			else
				srcCanvas.copy(dstCanvas, rect.left + crop, rect.top, rect.left, rect.top, move, height);
		} else {
			int move = Math.round(height * progressRatio);
			int crop = height - move;
			if (direction.isNegative())
				srcCanvas.copy(dstCanvas, rect.left, rect.top, rect.left, rect.top + crop, width, move);
			else
				srcCanvas.copy(dstCanvas, rect.left, rect.top + crop, rect.left, rect.top, width, move);
		}
	}

	private void drawTwoWay(Block block, PixelCanvas srcCanvas, PixelCanvas dstCanvas, float progressRatio) {

		Rect rect = block.rect;
		Direction direction = block.direction;

		int width = rect.width();
		int height = rect.height();

		if (direction.isHorizontal()) {

			if (mDrawLine) {
				int x = rect.left + Math.round(width / 2.0f);
				srcCanvas.clear(mLineColor, Math.round(x - mLineThickness), rect.top, Math.round(mLineThickness * 2.0f), rect.bottom);
			}
			int halfWidth = Math.round(width / 2.0f);
			int move = Math.round(halfWidth * progressRatio);
			int crop = halfWidth - move;

			srcCanvas.copy(dstCanvas, rect.left + crop, rect.top, rect.left, rect.top, move, height);
			srcCanvas.copy(dstCanvas, rect.left + halfWidth, rect.top, rect.left + halfWidth + crop, rect.top, move, height);

		} else { // if (mDirection.isVertical())

			if (mDrawLine) {
				int y = rect.top + Math.round(height / 2.0f);
				srcCanvas.clear(mLineColor, rect.left, Math.round(y - mLineThickness), rect.right, Math.round(mLineThickness * 2.0f));
			}
			int halfHeight = Math.round(height / 2.0f);
			int move = Math.round(halfHeight * progressRatio);
			int crop = halfHeight - move;

			srcCanvas.copy(dstCanvas, rect.left, rect.top + crop, rect.left, rect.top, width, move);
			srcCanvas.copy(dstCanvas, rect.left, rect.top + halfHeight, rect.left, rect.top + halfHeight + crop, width, move);
		}
	}

	void addBlock(Viewport viewport, Direction direction) {
		addBlock(new Block(viewport, direction));
	}

	void addBlock(Block block) {
		Precondition.checkNotNull(block);
		mBlocks.add(block);
	}

	void setBlocks(Block... blocks) {
		Precondition.checkArray(blocks).checkNotContainsNull();
		setBlocks(Arrays.asList(blocks));
	}

	void setBlocks(List<Block> blocks) {
		Precondition.checkCollection(blocks).checkNotContainsNull();
		mBlocks.clear();
		mBlocks.addAll(blocks);
	}

	void removeAllBlocks() {
		mBlocks.clear();
	}

	public List<Block> getBlocks() {
		return new ArrayList<>(mBlocks);
	}

	void setLineColor(int lineColor) {
		mLineColor = lineColor;
	}

	public int getLineColor() {
		return mLineColor;
	}

	void setLineThickness(float thicknessPx) {
		Precondition.checkNotNegative(thicknessPx);
		mLineThickness = thicknessPx;
	}

	public float getLineThickness() {
		return mLineThickness;
	}

	void setReverse(boolean reverse) {
		mIsReverse = reverse;
	}

	public boolean isReverse() {
		return mIsReverse;
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link EnterTransition}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Transition.Editor<EnterTransition, Editor> {

		private Editor(EnterTransition enterTransition) {
			super(enterTransition);
		}

		public Editor addBlock(Viewport viewport, Direction direction) {
			getObject().addBlock(viewport, direction);
			return this;
		}

		public Editor addBlock(Block block) {
			getObject().addBlock(block);
			return this;
		}

		public Editor setBlocks(Block... blocks) {
			getObject().setBlocks(blocks);
			return this;
		}

		public Editor setBlocks(List<Block> blocks) {
			getObject().setBlocks(blocks);
			return this;
		}

		public Editor removeAllBlocks() {
			getObject().removeAllBlocks();
			return this;
		}

		public Editor setLineColor(int lineColor) {
			getObject().setLineColor(lineColor);
			return this;
		}

		public Editor setLineThickness(float thicknessPx) {
			getObject().setLineThickness(thicknessPx);
			return this;
		}

		public Editor setReverse(boolean reverse) {
			getObject().setReverse(reverse);
			return this;
		}
	}

	public static final class Block implements IJsonConvertible {

		public final Viewport viewport;
		public final Direction direction;
		private final Rect rect;

		public Block(Viewport viewport, Direction direction) {
			Precondition.checkNotNull(viewport, direction);

			this.viewport = viewport;
			this.direction = direction;
			rect = new Rect();
		}

		public Block(JsonObject jsonObject) throws JSONException {
			Precondition.checkNotNull(jsonObject);

			viewport = jsonObject.getJSONObjectAsConcrete(JSON_NAME_VIEWPORT, Viewport.class);
			direction = jsonObject.getEnum(JSON_NAME_DIRECTION, Direction.class);
			rect = new Rect();
		}

		@Override
		public JsonObject toJsonObject() throws JSONException {

			JsonObject jsonObject = new JsonObject();
			jsonObject.put(JSON_NAME_VIEWPORT, viewport);
			jsonObject.put(JSON_NAME_DIRECTION, direction);

			return jsonObject;
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
