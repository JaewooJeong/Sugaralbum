package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import android.graphics.Color;
import android.widget.ImageView;

import com.kiwiple.multimedia.json.JsonObject;

/**
 * FitCenterScaler.
 * 
 * @see ImageView.ScaleType#FIT_CENTER
 */
public final class FitCenterScaler extends Scaler {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "fit_center_scaler";

	public static final String JSON_NAME_BACKGROUND_COLOR = "background_color";

	public static final int DEFAULT_BACKGROUND_COLOR = Color.TRANSPARENT;

	// // // // // Member variable.
	// // // // //
	private int mBackgroundColor;

	// // // // // Constructor.
	// // // // //
	{
		setBackgroundColor(DEFAULT_BACKGROUND_COLOR);
	}

	FitCenterScaler(Scene parent) {
		super(parent);
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();
		jsonObject.put(JSON_NAME_BACKGROUND_COLOR, mBackgroundColor);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);
		setBackgroundColor(jsonObject.getInt(JSON_NAME_BACKGROUND_COLOR));
	}

	// // // // // Method.
	// // // // //
	@Override
	void onDraw(PixelCanvas srcCanvas, PixelCanvas dstCanvas) {

		float srcWidth = srcCanvas.getImageWidth();
		float srcHeight = srcCanvas.getImageHeight();
		float srcAspectRatio = srcWidth / srcHeight;

		float dstWidth = getWidth();
		float dstHeight = getHeight();
		float dstAspectRatio = dstWidth / dstHeight;

		float scale = srcAspectRatio > dstAspectRatio ? dstWidth / srcWidth : dstHeight / srcHeight;
		float srcWidthScaled = srcWidth * scale;
		float srcHeightScaled = srcHeight * scale;

		int dstX = Math.round(dstWidth / 2.0f - srcWidthScaled / 2.0f);
		int dstY = Math.round(dstHeight / 2.0f - srcHeightScaled / 2.0f);

		if (dstY > 0) {
			dstCanvas.clear(mBackgroundColor, 0, 0, (int) dstWidth, dstY);
			dstCanvas.clear(mBackgroundColor, 0, (int) (dstY + srcHeightScaled), (int) dstWidth, dstY);
		} else if (dstX > 0) {
			dstCanvas.clear(mBackgroundColor, 0, 0, dstX, (int) dstHeight);
			dstCanvas.clear(mBackgroundColor, (int) (dstX + srcWidthScaled), 0, dstX, (int) dstHeight);
		}
		srcCanvas.copyWithScale(dstCanvas, dstX, dstY, scale);
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	void setBackgroundColor(int color) {
		mBackgroundColor = color;
	}

	public int getBackgroundColor() {
		return mBackgroundColor;
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link FitCenterScaler}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Scaler.Editor<FitCenterScaler, Editor> {

		private Editor(FitCenterScaler fitCenterScaler) {
			super(fitCenterScaler);
		}

		public Editor setBackgroundColor(int color) {
			getObject().setBackgroundColor(color);
			return this;
		}
	}
}
