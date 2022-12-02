package com.kiwiple.multimedia.canvas;

import java.nio.IntBuffer;

import android.graphics.Bitmap;

/**
 * PixelExtractUtils.
 * 
 */
final class PixelExtractUtils {

	static void extractARGB(Bitmap srcBitmap, PixelCanvas dstCanvas, boolean doRecycle) {

		int width = srcBitmap.getWidth();
		int height = srcBitmap.getHeight();

		srcBitmap.getPixels(dstCanvas.intArray, 0, width, 0, 0, width, height);
		dstCanvas.setImageSize(width, height);

		if (doRecycle) {
			srcBitmap.recycle();
		}
	}

	static void extractARGB(Bitmap srcBitmap, int[] dstArray, boolean doRecycle) {

		int width = srcBitmap.getWidth();
		int height = srcBitmap.getHeight();

		srcBitmap.getPixels(dstArray, 0, width, 0, 0, width, height);

		if (doRecycle) {
			srcBitmap.recycle();
		}
	}

	static int[] extractARGB(Bitmap srcBitmap, boolean doRecycle) {

		int width = srcBitmap.getWidth();
		int height = srcBitmap.getHeight();

		int[] pixels = new int[width * height];
		srcBitmap.getPixels(pixels, 0, width, 0, 0, width, height);

		if (doRecycle) {
			srcBitmap.recycle();
		}

		return pixels;
	}

	static void extractABGR(Bitmap srcBitmap, int[] dstArray, boolean doRecycle) {

		IntBuffer intBuffer = getIntBuferABGR(srcBitmap);
		intBuffer.get(dstArray, 0, intBuffer.remaining());

		if (doRecycle) {
			srcBitmap.recycle();
		}
	}

	static int[] extractABGR(Bitmap srcBitmap, boolean doRecycle) {

		IntBuffer intBuffer = getIntBuferABGR(srcBitmap);

		if (doRecycle) {
			srcBitmap.recycle();
		}

		return intBuffer.array();
	}

	private static IntBuffer getIntBuferABGR(Bitmap srcBitmap) {

		int width = srcBitmap.getWidth();
		int height = srcBitmap.getHeight();

		IntBuffer intBuffer = IntBuffer.allocate(width * height);
		srcBitmap.copyPixelsToBuffer(intBuffer);

		intBuffer.position(0);

		return intBuffer;
	}

	private PixelExtractUtils() {
		// Utility classes should have a private constructor.
	}
}
