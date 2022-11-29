package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import com.kiwiple.multimedia.json.JsonObject;


/**
 * FlashTransition
 * @author aubergine
 *
 */
public final class FlashTransition extends Transition {

	public static final String JSON_VALUE_TYPE = "flash_transition";

	/**
	 * fix area 
	 * 0~SHOW_FORMER_TIME : show former 
	 * SHOW_FORMER_TIME ~ FADE_FORMER_TIME : fade out former
	 * FADE_FORMER_TIME ~ FADE_LATTER_TIME : show white
	 * FADE_LATTER_TIME ~ SHOW_LATTER_TIME : fade in latter
	 * SHOW_LATTER_TIME~ 1 : show latter
	 */
	private final float SHOW_LATTER_TIME = 0.62f;
	private final float SHOW_FORMER_TIME = 0.22f;
	private final float FADE_FORMER_TIME = 0.42f;
	private final float FADE_LATTER_TIME = 0.58f;
	
	FlashTransition(Region parent) {
		super(parent);
	}

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
		
		float progressRatio = getProgressRatio();

		for (int i = 0; i < dstCanvas.intArray.length; i++) dstCanvas.intArray[i] = Integer.MAX_VALUE;

		if (progressRatio < SHOW_FORMER_TIME) {
			srcCanvasFormer.copy(dstCanvas);
		} else if (progressRatio > SHOW_LATTER_TIME) {
			srcCanvasLatter.copy(dstCanvas);
		} else if (progressRatio < FADE_FORMER_TIME && progressRatio >= SHOW_FORMER_TIME) {
			float fadeOutRatio = 1.0f - (progressRatio - SHOW_FORMER_TIME) / (1.0f - SHOW_FORMER_TIME);
			srcCanvasFormer.copy(dstCanvas);
			dstCanvas.tint(Math.round(0xff * fadeOutRatio) << 24 | 0x00ffffff);
		} else if (progressRatio > FADE_LATTER_TIME && progressRatio < SHOW_LATTER_TIME) {
			float fadeInRatio = progressRatio / SHOW_LATTER_TIME;
			srcCanvasLatter.copy(dstCanvas);
			dstCanvas.tint(Math.round(0xff * fadeInRatio) << 24 | 0x00ffffff);
		}
	}

	public static final class Editor extends Transition.Editor<FlashTransition, Editor> {

		private Editor(FlashTransition flashTransition) {
			super(flashTransition);
		}
	}
}