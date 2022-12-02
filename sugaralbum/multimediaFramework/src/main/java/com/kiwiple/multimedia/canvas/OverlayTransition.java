package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;

/**
 * OverlayTransition.
 * 
 */
public final class OverlayTransition extends Transition {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "overlay_transition";

	public static final String JSON_NAME_FRONT_SCENE = SceneOrder.DEFAULT_JSON_NAME;

	private static final SceneOrder DEFAULT_FRONT_SCENE = SceneOrder.FORMER;

	// // // // // Member variable.
	// // // // //
	private SceneOrder mFrontScene;

	// // // // // Constructor.
	// // // // //
	{
		setFrontScene(DEFAULT_FRONT_SCENE);
	}

	OverlayTransition(Region parent) {
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
		jsonObject.put(JSON_NAME_FRONT_SCENE, mFrontScene);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);
		setFrontScene(jsonObject.getEnum(JSON_NAME_FRONT_SCENE, SceneOrder.class));
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {
		checkValidity(mFrontScene != null, "You must invoke setFrontScene()");
	}

	@Override
	void onDraw(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {

		if (mFrontScene.equals(SceneOrder.FORMER)) {
			srcCanvasLatter.copy(dstCanvas);
			srcCanvasFormer.blend(dstCanvas);

		} else if (mFrontScene.equals(SceneOrder.LATTER)) {
			srcCanvasFormer.copy(dstCanvas);
			srcCanvasLatter.blend(dstCanvas);
		}
	}

	void setFrontScene(SceneOrder frontScene) {
		Precondition.checkNotNull(frontScene);
		mFrontScene = frontScene;
	}

	public SceneOrder getFrontScene() {
		return mFrontScene;
	}

	// // // // // Inner Class.
	// // // // //
	/**
	 * {@link OverlayTransition}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Transition.Editor<OverlayTransition, Editor> {

		private Editor(OverlayTransition overlayTransition) {
			super(overlayTransition);
		}

		/**
		 * {@code OverlayTransition}의 영향을 받는 두 {@code Scene} 중 앞에 위치할 {@code Scene}을 지정합니다.
		 * 
		 * @param frontScene
		 *            앞에 위치할 {@code Scene}을 특정하기 위한 {@code SceneOrder} 객체.
		 * @see SceneOrder
		 */
		public Editor setFrontScene(SceneOrder frontScene) {
			getObject().setFrontScene(frontScene);
			return this;
		}
	}
}