package com.kiwiple.multimedia.canvas;

import java.nio.ByteBuffer;

/**
 * PixelUtils.
 * 
 */
final class PixelUtils {

	static {
		System.loadLibrary("PixelUtils");
	}

	private static native void nativeResizeBilinear(long srcId, long dstId, int drawWidth, int drawHeight, float translateX, float translateY, float scale);

	static void resizeBilinear(PixelCanvas srcPixels, PixelCanvas dstPixels, int drawWidth, int drawHeight, float translateX, float translateY, float scale) {
		nativeResizeBilinear(srcPixels.id, dstPixels.id, drawWidth, drawHeight, translateX, translateY, scale);
	}

	private static native void nativeResizeNearestNeighborYUV(ByteBuffer srcPixels, byte[] dstPixels, int srcWidth, int srcHeight, int srcSliceHeight, int srcStride, int dstWidth, int dstHeight);

	static void resizeNearestNeighborYUV(ByteBuffer srcPixels, byte[] dstPixels, int srcWidth, int srcHeight, int srcSliceHeight, int srcStride, int dstWidth, int dstHeight) {
		nativeResizeNearestNeighborYUV(srcPixels, dstPixels, srcWidth, srcHeight, srcSliceHeight, srcStride, dstWidth, dstHeight);
	}

	private static native void nativeApplySplitTransition(long srcFormerId, long srcLatterId, long dstId, int lineWidth, int lineColor, int degree, float progressRatio);

	static void applySplitTransition(PixelCanvas srcFormer, PixelCanvas srcLatter, PixelCanvas dstCanvas, int lineWidth, int lineColor, int degree, float progressRatio) {
		nativeApplySplitTransition(srcFormer.id, srcLatter.id, dstCanvas.id, lineWidth, lineColor, degree, progressRatio);
	}

	private static native void nativeApplyLightingEffect(int[] srcPixels, int[] dstPixels, int current);

	static void applyLightingEffect(PixelCanvas srcPixels, PixelCanvas dstPixels, int current) {
		nativeApplyLightingEffect(srcPixels.intArray, dstPixels.intArray, current);
	}

	private static native void nativeApplyRedLightingEffect(int[] srcPixels, int[] dstPixels, int current);

	static void applyRedLightingEffect(PixelCanvas srcPixels, PixelCanvas dstPixels, int current) {
		nativeApplyRedLightingEffect(srcPixels.intArray, dstPixels.intArray, current);
	}

	private static native void nativeApplyCircleLightingEffect(int[] srcPixels, int[] dstPixels, int cx, int cy, int scale, int width, int height, int light);

	static void applyCircleLightingEffect(PixelCanvas srcPixels, PixelCanvas dstPixels, int cx, int cy, int scale, int width, int height, int light) {
		nativeApplyCircleLightingEffect(srcPixels.intArray, dstPixels.intArray, cx, cy, scale, width, height, light);
	}

	private static native void nativeApplyGrayscale(long id);

	static void applyGrayscale(PixelCanvas pixels) {
		nativeApplyGrayscale(pixels.id);
	}

	private static native void nativeConvertYuv420pToArgb(byte[] pixelsYUV, int[] pixelsARGB, int width, int height);

	static void convertYuv420pToArgb(byte[] pixelsYUV, PixelCanvas pixelsARGB, int width, int height) {
		nativeConvertYuv420pToArgb(pixelsYUV, pixelsARGB.intArray, width, height);
	}

	private static native void nativeConvertYuv420spToArgb(byte[] pixelsYUV, int[] pixelsARGB, int width, int height);

	static void convertYuv420spToArgb(byte[] pixelsYUV, PixelCanvas pixelsARGB, int width, int height) {
		nativeConvertYuv420spToArgb(pixelsYUV, pixelsARGB.intArray, width, height);
	}

	private static native void nativeConvertArgbToYuv420sp(int[] pixelsARGB, byte[] pixelsYUV, int width, int height);

	static void convertArgbToYuv420sp(PixelCanvas pixelsARGB, byte[] pixelsYUV, int width, int height) {
		nativeConvertArgbToYuv420sp(pixelsARGB.intArray, pixelsYUV, width, height);
	}

	private static native void nativeRotate(int[] srcPixels, int[] dstPixels, int width, int height, int rotation);

	static void rotate(PixelCanvas srcPixels, PixelCanvas dstPixels, int width, int height, int rotation) {
		nativeRotate(srcPixels.intArray, dstPixels.intArray, width, height, rotation);
	}

	private static native void nativeCrop(int[] srcPixels, int[] dstPixels, int width, int height, int left, int top, int right, int bottom);

	static void crop(PixelCanvas srcPixels, PixelCanvas dstPixels, int width, int height, int left, int top, int right, int bottom) {
		nativeCrop(srcPixels.intArray, dstPixels.intArray, width, height, left, top, right, bottom);
	}

	private PixelUtils() {
		// Utility classes should have a private constructor.
	}
}