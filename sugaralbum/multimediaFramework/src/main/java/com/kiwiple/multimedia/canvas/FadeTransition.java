package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import com.kiwiple.multimedia.json.JsonObject;

/**
 * 두 장면의 색상 점유율을 조정함으로써 전환 효과를 연출하는 클래스.
 */
public final class FadeTransition extends Transition {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "fade_transition";

	// // // // // Constructor.
	// // // // //
	FadeTransition(Region parent) {
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
		return super.toJsonObject();
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);
	}

	@Override
	void onDraw(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {
		srcCanvasLatter.copy(dstCanvas);
		srcCanvasFormer.blend(dstCanvas, 1.0f - getProgressRatio());
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link FadeTransition}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Transition.Editor<FadeTransition, Editor> {

		private Editor(FadeTransition fadeTransition) {
			super(fadeTransition);
		}
	}
}