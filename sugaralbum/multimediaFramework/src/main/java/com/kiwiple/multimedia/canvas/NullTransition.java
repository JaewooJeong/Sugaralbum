package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import com.kiwiple.multimedia.json.JsonObject;

/**
 * NullTransition.
 * 
 * Internal only.
 */
final class NullTransition extends Transition {

	public static final String JSON_VALUE_TYPE = "null_transition";

	NullTransition(Region parent) {
		super(parent);
	}

	@Override
	void onDraw(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {
		throw new UnsupportedOperationException("Do not use this class.");
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {
		return null;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		return;
	}

	public static final class Editor extends Transition.Editor<NullTransition, Editor> {

		private Editor(NullTransition nullTransition) {
			super(nullTransition);
		}

		@Override
		public Editor setDuration(int duration) {
			return this;
		}
	}
}
