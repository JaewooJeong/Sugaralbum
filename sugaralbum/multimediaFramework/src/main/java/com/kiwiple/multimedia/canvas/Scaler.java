package com.kiwiple.multimedia.canvas;

import com.kiwiple.multimedia.util.Size;

/**
 * Scaler.
 */
@SuppressWarnings("unchecked")
public abstract class Scaler extends RegionChild {

	// // // // // Member variable.
	// // // // //
	private final Scene mParent;

	// // // // // Constructor.
	// // // // //
	protected Scaler(Scene parent) {
		super(parent);
		mParent = parent;
	}

	// // // // // Method.
	// // // // //
	abstract void onDraw(PixelCanvas srcCanvas, PixelCanvas dstCanvas);

	final void draw(PixelCanvas srcCanvas, PixelCanvas dstCanvas) {

		if (!isValidated()) {
			drawOnInvalid(dstCanvas);
		} else {
			onDraw(srcCanvas, dstCanvas);
		}
	}

	@Override
	final void drawOnInvalid(PixelCanvas dstCanvas) {
		dstCanvas.clear(COLOR_SYMBOL_INVALID);
		dstCanvas.clear(COLOR_SYMBOL_INVALID_SCALER, 0, 20, getWidth(), 4);
		dstCanvas.clear(COLOR_SYMBOL_INVALID_SCALER, 0, 25, getWidth(), 2);
	}

	@Override
	public Editor<? extends Scaler, ? extends Editor<?, ?>> getEditor() {
		return (Editor<? extends Scaler, ? extends Editor<?, ?>>) super.getEditor();
	}

	@Override
	public final Size getSize() {
		return mParent.getSize();
	}

	@Override
	public final int getDuration() {
		return mParent.getDuration();
	}

	@Override
	public final int getPosition() {
		return mParent.getPosition();
	}

	public final float getProgressRatio() {
		return super.getProgressRatio();
	}

	// // // // // Inner class.
	// // // // //
	public static abstract class Editor<C extends Scaler, E extends Editor<C, E>> extends RegionChild.Editor<C, E> {

		Editor(C scaler) {
			super(scaler);
		}
	}
}