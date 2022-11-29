package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import android.graphics.Color;

import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.json.JsonObject;

/**
 * 지정된 색상을 출력하는 클래스.
 */
public final class ColorScene extends Scene {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "color_scene";

	public static final String JSON_NAME_COLOR = "color";

	/**
	 * 기본으로 지정되는 출력 색상.
	 */
	public static final int DEFAULT_COLOR = Color.BLACK;

	// // // // // Member variable.
	// // // // //
	@CacheCode
	private int mColor;

	// // // // // Constructor.
	// // // // //
	{
		setColor(DEFAULT_COLOR);
	}

	ColorScene(Region parent) {
		super(parent);
	}

	ColorScene(MultiLayerScene parent) {
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
		jsonObject.put(JSON_NAME_COLOR, mColor);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setColor(jsonObject.getInt(JSON_NAME_COLOR));
	}

	@Override
	void onDraw(PixelCanvas dstCanvas) {
		dstCanvas.clear(mColor);
	}

	void setColor(int color) {
		mColor = color;
	}

	/**
	 * 출력하는 색상을 반환합니다.
	 * 
	 * @return {@code #AARRGGBB} 형식의 출력 색상.
	 */
	public int getColor() {
		return mColor;
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link ColorScene}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Scene.Editor<ColorScene, Editor> {

		private Editor(ColorScene colorScene) {
			super(colorScene);
		}

		/**
		 * 출력할 색상을 설정합니다.
		 * 
		 * @param color
		 *            {@code #AARRGGBB} 형식의 출력 색상.
		 */
		public Editor setColor(int color) {
			getObject().setColor(color);
			return this;
		}
	}
}
