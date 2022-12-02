package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * MagnifyEffect
 * 
 */
public final class MagnifyEffect extends Effect {

	public static final String JSON_VALUE_TYPE = "magnify_effect";

	MagnifyEffect(Scene parent) {
		super(parent);
	}

	@Override
	void onDraw(PixelCanvas dstCanvas) {
		// Do something.
	}

	@Override
	Size[] getCanvasRequirement() {
		return new Size[] { getSize() };
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();
		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);
	}

	public static final class Editor extends Effect.Editor<MagnifyEffect, Editor> {

		private Editor(MagnifyEffect magnifyEffect) {
			super(magnifyEffect);
		}
	}
}
