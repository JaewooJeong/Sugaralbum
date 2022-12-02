package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import android.graphics.Color;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.annotation.RiValue;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.json.JsonObject;

/**
 * {@link Scene}이 점유하는 영역의 외곽에 경계선을 그리는 {@code Effect}.
 */
public final class BorderEffect extends Effect {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "border_effect";

	public static final String JSON_NAME_SIDE_TYPE_BIT_FLAG = "side_type_bit_flag";
	public static final String JSON_NAME_LINE_WIDTH = "line_width";
	public static final String JSON_NAME_LINE_COLOR = "line_color";
	public static final String JSON_NAME_LINE_TYPE = "line_type";

	public static final LineType DEFAULT_LINE_TYPE = LineType.SOLID;
	public static final SideType DEFAULT_SIDE_TYPE = SideType.ALL;
	public static final float DEFAULT_LINE_WIDTH_PX = 1.0f;
	public static final int DEFAULT_LINE_COLOR = Color.BLACK;

	// // // // // Member variable.
	// // // // //
	private LineType mLineType;

	@RiValue
	private float mLineWidthPx;
	private int mLineColor;
	private int mSideTypeBitFlag;

	// // // // // Static method.
	// // // // //
	/**
	 * {@link MultiLayerScene}의 관점에서 각 {@link LayerScene} 사이의 내부 경계선만을 그리기 위한 {@code SideType[]}을
	 * 주어진 {@link Viewport}를 기준으로 생성하여 반환합니다. 본 메서드를 통해 반환된 결과를 하나의 {@code MultiLayerScene}에 소속된 모든
	 * {@code LayerScene}의 {@code BorderEffect}에 적용한다면, 결과적으로 해당 {@code MultiLayerScene}의 내부 경계선이
	 * 출력될 것입니다.
	 * 
	 * @param viewport
	 *            내부 경계에 해당하는 테두리를 판단하기 위한 기준 {@code Viewport}.
	 */
	public static SideType[] createSideTypeForInside(Viewport viewport) {
		Precondition.checkNotNull(viewport);

		int bitFlag = 0;
		if (viewport.left > 0.0f) {
			bitFlag |= SideType.LEFT.bitFlag;
		}
		if (viewport.top > 0.0f) {
			bitFlag |= SideType.TOP.bitFlag;
		}
		if (viewport.right < 1.0f) {
			bitFlag |= SideType.RIGHT.bitFlag;
		}
		if (viewport.bottom < 1.0f) {
			bitFlag |= SideType.BOTTOM.bitFlag;
		}
		return SideType.parseBitFlag(bitFlag);
	}

	// // // // // Constructor.
	// // // // //
	{
		setLineType(DEFAULT_LINE_TYPE);
		setSideType(DEFAULT_SIDE_TYPE);
		setLineWidth(DEFAULT_LINE_WIDTH_PX);
		setLineColor(DEFAULT_LINE_COLOR);
	}

	BorderEffect(Scene parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	void onDraw(PixelCanvas dstCanvas) {

		int width = getWidth();
		int height = getHeight();
		int lineWidth = Math.round(mLineWidthPx);

		if (SideType.LEFT.isCoveredBy(mSideTypeBitFlag)) {
			dstCanvas.clear(mLineColor, 0, 0, lineWidth, height);
		}
		if (SideType.TOP.isCoveredBy(mSideTypeBitFlag)) {
			dstCanvas.clear(mLineColor, 0, 0, width, lineWidth);
		}
		if (SideType.RIGHT.isCoveredBy(mSideTypeBitFlag)) {
			dstCanvas.clear(mLineColor, width - lineWidth, 0, lineWidth, height);
		}
		if (SideType.BOTTOM.isCoveredBy(mSideTypeBitFlag)) {
			dstCanvas.clear(mLineColor, 0, height - lineWidth, width, lineWidth);
		}
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {
		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_SIDE_TYPE_BIT_FLAG, mSideTypeBitFlag);
		jsonObject.put(JSON_NAME_LINE_WIDTH, mLineWidthPx);
		jsonObject.put(JSON_NAME_LINE_COLOR, mLineColor);
		jsonObject.put(JSON_NAME_LINE_TYPE, mLineType);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setSideType(SideType.parseBitFlag(jsonObject.getInt(JSON_NAME_SIDE_TYPE_BIT_FLAG)));
		setLineWidth(jsonObject.getFloat(JSON_NAME_LINE_WIDTH));
		setLineColor(jsonObject.getInt(JSON_NAME_LINE_COLOR));
		setLineType(jsonObject.getEnum(JSON_NAME_LINE_TYPE, LineType.class));
	}

	void setLineType(LineType lineType) {
		Precondition.checkNotNull(lineType);
		mLineType = lineType;
	}

	/**
	 * 경계선의 종류를 반환합니다.
	 * 
	 * @see LineType
	 */
	public LineType getLineType() {
		return mLineType;
	}

	void setSideType(SideType... sideTypes) {
		Precondition.checkNotNull((Object) sideTypes);
		Precondition.checkArgument(sideTypes.length > 0, "sideTypes must have at least one element.");

		int bitFlag = 0;
		for (SideType type : sideTypes) {
			bitFlag |= type.bitFlag;
		}
		mSideTypeBitFlag = bitFlag;
	}

	/**
	 * 경계선이 출력되는 테두리의 종류를 {@code SideType}의 배열로써 반환합니다.
	 * 
	 * @see SideType
	 */
	public SideType[] getSideType() {
		return SideType.parseBitFlag(mSideTypeBitFlag);
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

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link BorderEffect}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Effect.Editor<BorderEffect, Editor> {

		private Editor(BorderEffect borderEffect) {
			super(borderEffect);
		}

		/**
		 * 경계선의 종류를 설정합니다.
		 * 
		 * @param lineType
		 *            설정할 경계선에 해당하는 {@code LineType}.
		 * @see LineType
		 */
		public Editor setLineType(LineType lineType) {
			getObject().setLineType(lineType);
			return this;
		}

		/**
		 * 사각 영역을 이루는 네 개의 테두리 중 경계선을 그릴 테두리를 설정합니다.
		 * 
		 * @param sideTypes
		 *            설정할 테두리에 해당하는 {@code SideType}의 배열.
		 * @see SideType
		 */
		public Editor setSideType(SideType... sideTypes) {
			getObject().setSideType(sideTypes);
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
	}

	// // // // // Enumeration.
	// // // // //
	/**
	 * 사각 영역을 이루는 테두리의 종류를 정의한 클래스.
	 * 
	 * @see #LEFT
	 * @see #TOP
	 * @see #RIGHT
	 * @see #BOTTOM
	 * @see #ALL
	 */
	public static enum SideType {

		/**
		 * 좌측 테두리를 의미합니다.
		 */
		LEFT("left", 1 << 0),

		/**
		 * 상단 테두리를 의미합니다.
		 */
		TOP("top", 1 << 1),

		/**
		 * 우측 테두리를 의미합니다.
		 */
		RIGHT("right", 1 << 2),

		/**
		 * 하단 테두리를 의미합니다.
		 */
		BOTTOM("bottom", 1 << 3),

		/**
		 * 전체 테두리를 의미합니다.
		 */
		ALL("all", LEFT.bitFlag | TOP.bitFlag | RIGHT.bitFlag | BOTTOM.bitFlag);

		final String name;
		final int bitFlag;

		static SideType[] parseBitFlag(int bitFlag) {

			int bitCount = Integer.bitCount(bitFlag);
			boolean useAllSide = (bitCount == 4);

			if (useAllSide) {
				return new SideType[] { ALL };
			} else {
				SideType types[] = new SideType[bitCount];
				int index = 0;
				if (LEFT.isCoveredBy(bitFlag)) {
					types[index++] = LEFT;
				}
				if (TOP.isCoveredBy(bitFlag)) {
					types[index++] = TOP;
				}
				if (RIGHT.isCoveredBy(bitFlag)) {
					types[index++] = RIGHT;
				}
				if (BOTTOM.isCoveredBy(bitFlag)) {
					types[index++] = BOTTOM;
				}
				return types;
			}
		}

		private SideType(String name, int bitFlag) {
			this.name = name;
			this.bitFlag = bitFlag;
		}

		boolean isCoveredBy(int bitFlag) {
			return (bitFlag & this.bitFlag) != 0;
		}
	}

	/**
	 * 경계선의 종류를 구분하기 위한 열거형.
	 * 
	 * @see #SOLID
	 */
	public static enum LineType {

		/**
		 * 단순 실선을 의미합니다.
		 */
		SOLID;
	}
}