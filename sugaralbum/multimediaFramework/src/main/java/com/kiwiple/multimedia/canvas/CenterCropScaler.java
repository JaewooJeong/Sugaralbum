package com.kiwiple.multimedia.canvas;

import android.widget.ImageView;

/**
 * CenterCropScaler.
 * 
 * @see ImageView.ScaleType#CENTER_CROP
 */
public final class CenterCropScaler extends Scaler {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "center_crop_scaler";

	// // // // // Constructor.
	// // // // //
	CenterCropScaler(Scene parent) {
		super(parent);
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

		float scale = srcAspectRatio < dstAspectRatio ? dstWidth / srcWidth : dstHeight / srcHeight;
		float srcWidthScaled = srcWidth * scale;
		float srcHeightScaled = srcHeight * scale;

		int dstX = Math.round(dstWidth / 2.0f - srcWidthScaled / 2.0f);
		int dstY = Math.round(dstHeight / 2.0f - srcHeightScaled / 2.0f);

		srcCanvas.copyWithScale(dstCanvas, dstX, dstY, scale);
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link CenterCropScaler}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Scaler.Editor<CenterCropScaler, Editor> {

		private Editor(CenterCropScaler centerCropScaler) {
			super(centerCropScaler);
		}
	}
}
