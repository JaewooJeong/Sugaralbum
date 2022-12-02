package com.kiwiple.multimedia.canvas;

import static android.graphics.Bitmap.Config.ARGB_8888;
import static com.kiwiple.multimedia.canvas.ICanvasUser.BITMAP_CONFIG;

import java.nio.Buffer;
import java.nio.IntBuffer;

import android.graphics.Bitmap;
import android.graphics.Rect;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.util.Size;

/**
 * PixelCanvas.
 */
public class PixelCanvas {

	// // // // // Static variable.
	// // // // //
	private static final int BYTES_PER_PIXEL = BITMAP_CONFIG.equals(ARGB_8888) ? 4 : (int) Precondition.assureUnreachable();

	private static final int MINIMUM_CAPACITY = 1024 * 4;

	// // // // // Member variable.
	// // // // //
	final long id;

	/**
	 * 이미지 데이터의 저장 공간을 감싸고 있는 {@link Buffer} 객체.
	 */
	public final Buffer buffer;

	/**
	 * 이미지 데이터의 저장 공간. {@link #buffer}로부터 획득합니다.
	 * 
	 * @see Buffer#array()
	 */
	public final int[] intArray;

	/**
	 * 저장 공간의 물리적 크기. {@code buffer.capacity()} 혹은 {@code intArray.length}와 동일합니다.
	 */
	private final int mCapacity;

	/**
	 * 이미지의 크기가 고정되어 있는지의 여부.<br />
	 * <br />
	 * {@code true}인 경우, {@code setImageSize()}를 통해 이미지의 크기를 변경할 수 없습니다.
	 */
	private final boolean mIsImageSizeLocked;

	// // // // // Constructor.
	// // // // //
	static {
		System.loadLibrary("PixelCanvas");
		nativeInitializeJNI();
	}

	private PixelCanvas(int capacity, Size imageSize, boolean lockImageSize) {
		Precondition.checkOnlyPositive(capacity);

		// This is a stopgap for prevention of JNI array copy.
		capacity = Math.max(MINIMUM_CAPACITY, capacity);

		IntBuffer intBuffer = IntBuffer.allocate(capacity);
		buffer = intBuffer;
		intArray = intBuffer.array();

		id = nativeInitialize(intArray);
		mCapacity = capacity;

		setImageSize(imageSize == null ? Size.INVALID_SIZE : imageSize);
		mIsImageSizeLocked = lockImageSize;
	}

	/**
	 * 주어진 물리적 크기를 지니는 객체를 생성합니다.
	 * 
	 * @param capacity
	 *            버퍼의 물리적 크기.
	 */
	public PixelCanvas(int capacity) {
		this(capacity, null, false);
	}

	/**
	 * 주어진 물리적 크기를 지니는 객체를 생성하되, 이미지 크기 정보를 동일한 값으로 초기화합니다.
	 * 
	 * @param size
	 *            버퍼의 물리적 크기 정보.
	 * @param lockImageSize
	 *            이미지 크기 정보를 고정하려면 {@code true}.
	 */
	public PixelCanvas(Size size, boolean lockImageSize) {
		this(size.product(), size, lockImageSize);
	}

	// // // // // Static method.
	// // // // //
	public static int measureBytes(int pixelCount) {
		Precondition.checkNotNegative(pixelCount);
		return pixelCount * BYTES_PER_PIXEL;
	}

	public static float measureMegabytes(int pixelCount) {
		return measureBytes(pixelCount) / 1024.0f / 1024.0f;
	}

	// // // // // Method.
	// // // // //
	/**
	 * 이미지 크기 정보를 설정합니다.
	 * 
	 * @param width
	 *            이미지의 가로 크기. 0 이상의 값이어야 합니다.
	 * @param height
	 *            이미지의 세로 크기. 0 이상의 값이어야 합니다.
	 * @throws IllegalArgumentException
	 *             {@code width} 혹은 {@code height}가 {@code 0}보다 작은 경우, 혹은 {@code (width * height)}가
	 *             버퍼의 물리적 크기보다 큰 경우.
	 */
	public void setImageSize(int width, int height) {
		Precondition.checkState(!mIsImageSizeLocked, "Logical size is locked.");
		Precondition.checkNotNegative(width, height);

		nativeSetImageSize(id, width, height);
	}

	/**
	 * 이미지 크기 정보를 설정합니다.
	 * 
	 * @param size
	 *            이미지의 크기 정보.
	 * @throws IllegalArgumentException
	 *             {@link Size#product()}가 버퍼의 물리적 크기보다 큰 경우.
	 */
	public void setImageSize(Size size) {
		Precondition.checkNotNull(size);
		setImageSize(size.width, size.height);
	}

	/**
	 * 물리적 크기를 반환합니다.
	 * 
	 * @return 버퍼의 물리적 크기. {@code buffer.capacity()} 혹은 {@code intArray.length}와 동일합니다.
	 */
	public int getCapacity() {
		return mCapacity;
	}

	/**
	 * 이미지 크기 정보를 반환합니다.
	 */
	public Size getImageSize() {
		return new Size(nativeGetImageWidth(id), nativeGetImageHeight(id));
	}

	/**
	 * 이미지의 가로 크기를 반환합니다.
	 */
	public int getImageWidth() {
		return nativeGetImageWidth(id);
	}

	/**
	 * 이미지의 세로 크기를 반환합니다.
	 */
	public int getImageHeight() {
		return nativeGetImageHeight(id);
	}

	/**
	 * 물리적 저장 공간에서 이미지가 실제로 저장되어 있는 위치를 산정하기 위한 offset 값을 설정합니다.
	 * 
	 * @param offset
	 *            {@code 0} 이상의 값을 가지는 offset.
	 */
	public void setOffset(int offset) {
		Precondition.checkNotNegative(offset);
		nativeSetOffset(id, offset);
	}

	/**
	 * offset 값을 반환합니다.
	 */
	public int getOffset() {
		return nativeGetOffset(id);
	}

	/**
	 * 이미지를 {@link Bitmap}으로 변환하여 반환합니다.
	 */
	public Bitmap createBitmap() {

		int width = nativeGetImageWidth(id);
		int height = nativeGetImageHeight(id);

		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(intArray, 0, width, 0, 0, width, height);
		return bitmap;
	}

	void drawPoint(int color, int x, int y) {
		nativeDrawPoint(id, color, x, y);
	}

	void drawLine(int color, int startX, int startY, int endX, int endY, float thickness) {
		nativeDrawLine(id, color, startX, startY, endX, endY, thickness);
	}

	void drawRect(int color, int startX, int startY, int endX, int endY, float thickness) {
		nativeDrawRect(id, color, startX, startY, endX, endY, thickness);
	}

	void drawRect(int color, Rect rect, float thickness) {
		drawRect(color, rect.left, rect.top, rect.right, rect.bottom, thickness);
	}

	void drawOval(int color, int x, int y, int xRadius, int yRadius, int thickness) {
		nativeDrawOval(id, color, x, y, xRadius, yRadius, thickness);
	}

	void fillOval(int color, int x, int y, int xRadius, int yRadius) {
		nativeFillOval(id, color, x, y, xRadius, yRadius);
	}

	void rotate(int degree) {

		if (degree % 90 != 0) {
			throw new UnsupportedOperationException("unsupported degree: " + degree);
		}
		nativeRotate(id, degree);
	}

	void clear(int color) {
		nativeClear(id, color);
	}

	void clear(int color, int x, int y, int width, int height) {
		nativeClear(id, color, x, y, width, height);
	}

	void deepCopy(PixelCanvas dstCanvas) {
		dstCanvas.setImageSize(getImageSize());
		copy(dstCanvas);
	}

	void tint(int color) {
		nativeTint(id, color);
	}

	void tint(int color, int x, int y, int width, int height) {
		nativeTint(id, color, x, y, width, height);
	}

	void blend(PixelCanvas dstCanvas) {
		nativeBlend(id, dstCanvas.id, 1.0f);
	}

	void blend(PixelCanvas dstCanvas, float multiplier) {
		nativeBlend(id, dstCanvas.id, multiplier);
	}

	void blend(PixelCanvas dstCanvas, int dstX, int dstY) {
		nativeBlend(id, dstCanvas.id, dstX, dstY, 1.0f);
	}

	void blend(PixelCanvas dstCanvas, int dstX, int dstY, float multiplier) {
		nativeBlend(id, dstCanvas.id, dstX, dstY, multiplier);
	}

	void blend(PixelCanvas dstCanvas, int srcX, int srcY, int dstX, int dstY, int width, int height) {
		nativeBlend(id, dstCanvas.id, srcX, srcY, dstX, dstY, width, height, 1.0f);
	}

	void blend(PixelCanvas dstCanvas, int srcX, int srcY, int dstX, int dstY, int width, int height, float multiplier) {
		nativeBlend(id, dstCanvas.id, srcX, srcY, dstX, dstY, width, height, multiplier);
	}

	void blendWithMask(PixelCanvas dstCanvas, PixelCanvas maskCanvas) {
		nativeBlendWithMask(id, dstCanvas.id, maskCanvas.id);
	}

	void blendWithMask(PixelCanvas dstCanvas, PixelCanvas maskCanvas, int x, int y) {
		nativeBlendWithMask(id, dstCanvas.id, maskCanvas.id, x, y);
	}

	void copy(PixelCanvas dstCanvas) {
		nativeCopy(id, dstCanvas.id);
	}

	void copy(PixelCanvas dstCanvas, int dstX, int dstY) {
		nativeCopy(id, dstCanvas.id, 0, 0, dstX, dstY, nativeGetImageWidth(id), nativeGetImageHeight(id));
	}

	void copy(PixelCanvas dstCanvas, int srcX, int srcY, int dstX, int dstY, int width, int height) {
		nativeCopy(id, dstCanvas.id, srcX, srcY, dstX, dstY, width, height);
	}

	void copy(PixelCanvas dstCanvas, int length) {
		Precondition.checkArgument(mCapacity >= length && dstCanvas.mCapacity >= length, "mCapacity must be greater than length.");
		System.arraycopy(intArray, 0, dstCanvas.intArray, 0, length);
	}

	void copyWithScale(PixelCanvas dstCanvas, float dstX, float dstY, float scale) {
		nativeCopyWithScale(id, dstCanvas.id, dstX, dstY, scale);
	}

	void copyFrom(Bitmap bitmap) {
		Precondition.checkArgument(bitmap.getConfig().equals(ICanvasUser.BITMAP_CONFIG), "copyFrom() support only: " + ICanvasUser.BITMAP_CONFIG);
		Precondition.checkArgument(bitmap.getWidth() * bitmap.getHeight() <= mCapacity, "mCapacity must be greater than bitmap size.");
		PixelExtractUtils.extractARGB(bitmap, this, false);
	}

	@Override
	protected void finalize() throws Throwable {
		try {
			nativeFinalize(id);
		} finally {
			super.finalize();
		}
	}

	// // // // // Native.
	// // // // //
	private static native void nativeInitializeJNI();

	private native long nativeInitialize(int[] buffer);

	private native void nativeFinalize(long id);

	private native void nativeSetImageSize(long id, int width, int height);

	private native int nativeGetImageWidth(long id);

	private native int nativeGetImageHeight(long id);

	private native void nativeSetOffset(long id, int offset);

	private native int nativeGetOffset(long id);

	private native void nativeDrawPoint(long id, int color, int x, int y);

	private native void nativeDrawLine(long id, int color, int startX, int startY, int endX, int endY, float thickness);

	private native void nativeDrawRect(long id, int color, int startX, int startY, int endX, int endY, float thickness);

	private native void nativeDrawOval(long id, int color, int x, int y, int xRadius, int yRadius, int thickness);

	private native void nativeFillOval(long id, int color, int x, int y, int xRadius, int yRadius);

	private native void nativeRotate(long id, int degree);

	private native void nativeClear(long id, int color);

	private native void nativeClear(long id, int color, int x, int y, int width, int height);

	private native void nativeTint(long id, int color);

	private native void nativeTint(long id, int color, int x, int y, int width, int height);

	private native void nativeBlend(long srcId, long dstId, float multiplier);

	private native void nativeBlend(long srcId, long dstId, int dstX, int dstY, float multiplier);

	private native void nativeBlend(long srcId, long dstId, int srcX, int srcY, int dstX, int dstY, int width, int height, float multiplier);

	private native void nativeBlendWithMask(long srcId, long dstId, long maskId);

	private native void nativeBlendWithMask(long srcId, long dstId, long maskId, int x, int y);

	private native void nativeCopy(long srcId, long dstId);

	private native void nativeCopy(long srcId, long dstId, int srcX, int srcY, int dstX, int dstY, int width, int height);

	private native void nativeCopyWithScale(long srcId, long dstId, float dstX, float dstY, float scale);
}