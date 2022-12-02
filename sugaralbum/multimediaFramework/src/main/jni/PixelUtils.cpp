#include <cstring>
#include <cstdlib>
#include <cmath>

#include "PixelUtils.h"
#include "PixelCanvas.h"
#include "libyuv.h"

inline static PixelCanvas * getCanvas(jlong id) {
	return reinterpret_cast<PixelCanvas *>(id);
}

void drawLine(int* const pixels, const int width, const int height, int x1, int y1, int x2, int y2, const int color) {

	if (y1 > y2 || (y1 == y2 || x1 > x2)) { // for performance.
		int forSwap;

		forSwap = x1;
		x1 = x2;
		x2 = forSwap;

		forSwap = y1;
		y1 = y2;
		y2 = forSwap;
	}

	int dx = std::abs(x2 - x1);
	int dy = std::abs(y2 - y1);
	int sx = x1 < x2 ? 1 : -1;
	int sy = y1 < y2 ? 1 : -1;

	int err = dx - dy;
	int currentX = x1;
	int currentY = y1;

	while (currentX != x2 || currentY != y2) {

		if (currentX >= 0 && currentX < width && currentY >= 0 && currentY < height) {
			pixels[currentX + currentY * width] = color;
		}

		int err2 = err * 2;
		if (err2 > -1 * dy) {
			err = err - dy;
			currentX = currentX + sx;
		}
		if (err2 < dx) {
			err = err + dx;
			currentY = currentY + sy;
		}
	}
}

JNIEXPORT void JNICALL Java_com_kiwiple_multimedia_canvas_PixelUtils_nativeResizeBilinear
(JNIEnv *env, jclass jobj, jlong srcId, jlong dstId, jint drawWidth, jint drawHeight, jfloat translateX, jfloat translateY, jfloat scale)
{
	PixelCanvas *srcCanvas = getCanvas(srcId);
	PixelCanvas *dstCanvas = getCanvas(dstId);

	int *srcPixelsPtr = srcCanvas->getStartPointer();
	int *dstPixelsPtr = dstCanvas->getStartPointer();
	int srcWidth = srcCanvas->getWidth();
	int srcHeight = srcCanvas->getHeight();
	int dstStride = dstCanvas->getWidth();

	int stride = srcWidth;
	int scaleWidth = (int) (srcWidth * scale + 0.5f);
	int scaleHeight = (int) (srcHeight * scale + 0.5f);

	float ratio = 1.0f / scale;
	float scaleStartX = (translateX < 0.0f) ? -translateX : 0.0f;
	float scaleStartY = (translateY < 0.0f) ? -translateY : 0.0f;

	int x_offset[drawWidth];
	unsigned int x_dif[drawWidth];
	unsigned int x_dif_m[drawWidth];

	for (int i = 0; i < drawWidth; ++i) {

		float iRatio = (i + scaleStartX) * ratio;
		int x = (int) iRatio;

		x_offset[i] = x;
		x_dif[i] = 0xffu * (iRatio - x);
		x_dif_m[i] = 0xffu - x_dif[i];
	}

	unsigned int *srcPtr = reinterpret_cast<unsigned int *>(srcPixelsPtr);
	unsigned int *dstPtr = reinterpret_cast<unsigned int *>(dstPixelsPtr);

	for (unsigned int i = 0; i < drawHeight; ++i) {

		if (i < translateY) continue;
		if (i >= scaleHeight) break;

		int dstIndex = i * dstStride;

		float iRatio = (i + scaleStartY) * ratio;
		int y = (int) iRatio;
		if (y >= srcHeight - 1) {
			y = srcHeight - 2;
		}

		int srcOffset = y * stride;
		unsigned int y_dif = 0xffu * (iRatio - y);
		unsigned int y_dif_m = 0xffu - y_dif;

		for (int j = 0; j < drawWidth; ++j) {

			unsigned int vDif = x_dif[j];
			unsigned int vDifM = x_dif_m[j];

			unsigned int xy_dif = (vDif * y_dif) >> 8u;
			unsigned int xy_dif_m = (vDifM * y_dif_m) >> 8u;
			unsigned int xy_dif_dif_m = (vDif * y_dif_m) >> 8u;
			unsigned int xy_dif_m_dif = (vDifM * y_dif) >> 8u;

			int srcIndex = srcOffset + x_offset[j];
			unsigned int LT = srcPtr[srcIndex++];
			unsigned int RT = srcPtr[srcIndex];
			srcIndex += stride;
			unsigned int RB = srcPtr[srcIndex--];
			unsigned int LB = srcPtr[srcIndex];

			unsigned int outRB = (
					((LT & 0x00ff00ffu) * xy_dif_m) +
					((RT & 0x00ff00ffu) * xy_dif_dif_m) +
					((LB & 0x00ff00ffu) * xy_dif_m_dif) +
					((RB & 0x00ff00ffu) * xy_dif)) & 0xff00ff00u;
			unsigned int outG = (
					((LT & 0x0000ff00u) * xy_dif_m) +
					((RT & 0x0000ff00u) * xy_dif_dif_m) +
					((LB & 0x0000ff00u) * xy_dif_m_dif) +
					((RB & 0x0000ff00u) * xy_dif)) & 0x00ff0000u;

			dstPtr[dstIndex++] = 0xff000000u | (outRB | outG) >> 8u;
		}
	}
}

JNIEXPORT void JNICALL Java_com_kiwiple_multimedia_canvas_PixelUtils_nativeResizeNearestNeighborYUV
(JNIEnv *env, jclass jobj, jobject srcPixels, jbyteArray dstPixels, jint srcWidth, jint srcHeight, jint srcSliceHeight, jint srcStride, jint dstWidth, jint dstHeight)
{
	jbyte *dstPtr = env->GetByteArrayElements(dstPixels, 0);

	char *srcPixelsPtr = reinterpret_cast<char *>(env->GetDirectBufferAddress(srcPixels));
	char *dstPixelsPtr = reinterpret_cast<char *>(dstPtr);

	int srcIndexY;
	int dstIndexY;
	int srcIndexUV = srcSliceHeight * srcStride;
	int dstIndexUV = dstHeight * dstWidth;
	int expandedWidth = (srcWidth << 16) / dstWidth + 1;
	int expandedHeight = (srcHeight << 16) / dstHeight + 1;

	for (int i = 0; i < (dstHeight & ~7); ++i) {

		int srcY = (i * expandedHeight) >> 16;
		srcIndexY = srcY * srcStride;

		if ((i & 1) == 0) {
			int dstOffsetUV = dstIndexUV + (i / 2) * dstWidth;
			int srcOffsetUV = srcIndexUV + (srcY / 2) * srcStride;

			for (int j = 0; j < (dstWidth & ~7); ++j) {
				int srcX = (j * expandedWidth) >> 16;
				dstPixelsPtr[dstIndexY + j] = srcPixelsPtr[srcIndexY + srcX];

				if ((j & 1) == 0) {
					int offset = srcX & ~1;
					dstPixelsPtr[dstOffsetUV + j] = srcPixelsPtr[srcOffsetUV + offset];
					dstPixelsPtr[dstOffsetUV + j + 1] = srcPixelsPtr[srcOffsetUV + offset + 1];
				}
			}
		} else {
			for (int j = 0; j < (dstWidth & ~7); ++j) {
				int srcX = (j * expandedWidth) >> 16;
				dstPixelsPtr[dstIndexY + j] = srcPixelsPtr[srcIndexY + srcX];
			}
		}
		dstIndexY += dstWidth;
	}

	env->ReleaseByteArrayElements(dstPixels, dstPtr, 0);
}

JNIEXPORT void JNICALL Java_com_kiwiple_multimedia_canvas_PixelUtils_nativeApplySplitTransition
(JNIEnv *env, jclass jobj, jlong srcFormerId, jlong srcLatterId, jlong dstId, jint lineWidth, jint lineColor, jint degree, jfloat progressRatio)
{
	PixelCanvas *srcFormerCanvas = getCanvas(srcFormerId);
	PixelCanvas *srcLatterCanvas = getCanvas(srcLatterId);
	PixelCanvas *dstCanvas = getCanvas(dstId);

	int *srcPixelsFormerPtr = srcFormerCanvas->getStartPointer();
	int *srcPixelsLatterPtr = srcLatterCanvas->getStartPointer();
	int *dstPixelsPtr = dstCanvas->getStartPointer();
	int imageWidth = dstCanvas->getWidth();
	int imageHeight = dstCanvas->getHeight();

	bool drawBorderLine = ((unsigned int) lineColor >= 0x01000000u);
	int lineHalfWidth = lineWidth / 2;

	if (degree == 0) {

		int offset = 0;

		int latterImageWidth = imageWidth * progressRatio + 0.5f;
		int leftOffset = (imageWidth - latterImageWidth) / 2.0f + 0.5f;
		int rightOffset = imageWidth - (leftOffset + latterImageWidth);

		for (int i = 0; i < imageHeight; ++i) {

			std::memcpy(dstPixelsPtr + offset, srcPixelsFormerPtr + offset, sizeof(int) * leftOffset);
			offset += leftOffset;

			std::memcpy(dstPixelsPtr + offset, srcPixelsLatterPtr + offset, sizeof(int) * latterImageWidth);
			offset += latterImageWidth;

			std::memcpy(dstPixelsPtr + offset, srcPixelsFormerPtr + offset, sizeof(int) * rightOffset);
			offset += rightOffset;
		}
		if (drawBorderLine) {
			for (int i = 0; i != lineWidth; ++i) {

				int leftLineX = leftOffset - lineHalfWidth + i;
				int rightLineX = leftLineX + latterImageWidth;

				drawLine(dstPixelsPtr, imageWidth, imageHeight, leftLineX, 0, leftLineX, imageHeight, lineColor);
				drawLine(dstPixelsPtr, imageWidth, imageHeight, rightLineX, 0, rightLineX, imageHeight, lineColor);
			}
		}

	} else if (degree == 90) {

		int latterImageHeight = imageHeight * progressRatio;
		int topOffset = (imageHeight - latterImageHeight) / 2.0f;
		int bottomOffset = imageHeight - (topOffset + latterImageHeight);

		int copySize = imageWidth * topOffset;
		std::memcpy(dstPixelsPtr, srcPixelsFormerPtr, sizeof(int) * copySize);
		int ptrOffset = copySize;

		copySize = imageWidth * latterImageHeight;
		std::memcpy(dstPixelsPtr + ptrOffset, srcPixelsLatterPtr + ptrOffset, sizeof(int) * copySize);
		ptrOffset += copySize;

		copySize = imageWidth * bottomOffset;
		std::memcpy(dstPixelsPtr + ptrOffset, srcPixelsFormerPtr + ptrOffset, sizeof(int) * copySize);

		if (drawBorderLine) {
			for (int i = 0; i != lineWidth; ++i) {

				int topLineY = topOffset - lineHalfWidth + i;
				int bottomLineY = topLineY + latterImageHeight;

				drawLine(dstPixelsPtr, imageWidth, imageHeight, 0, topLineY, imageWidth, topLineY, lineColor);
				drawLine(dstPixelsPtr, imageWidth, imageHeight, 0, bottomLineY, imageWidth, bottomLineY, lineColor);
			}
		}

	} else { // (degree == 45 || degree == 135)

		int latterImageWidth = imageWidth * progressRatio + 0.5f;
		int addendPerY = (degree > 90) ? 1 : -1;

		int offset = 0;
		int centerX = imageWidth / 2.0f;
		int centerY = imageHeight / 2.0f;

		for (int i = 0; i < imageHeight; ++i) {

			int latterCenterX = centerX + (centerY - i) * addendPerY;
			int latterStartX = latterCenterX - latterImageWidth;
			int latterEndX = latterCenterX + latterImageWidth;

			if (latterStartX < 0) {
				latterStartX = 0;
			}
			if (latterEndX > imageWidth) {
				latterEndX = imageWidth;
			}

			std::memcpy(dstPixelsPtr + offset, srcPixelsFormerPtr + offset, sizeof(int) * latterStartX);
			offset += latterStartX;
			std::memcpy(dstPixelsPtr + offset, srcPixelsLatterPtr + offset, sizeof(int) * (latterEndX - latterStartX));
			offset += (latterEndX - latterStartX);
			std::memcpy(dstPixelsPtr + offset, srcPixelsFormerPtr + offset, sizeof(int) * (imageWidth - latterEndX));
			offset += (imageWidth - latterEndX);
		}
		if (drawBorderLine) {
			for (int i = 0; i != lineWidth; ++i) {

				int leftLineX1 = centerX + centerY * addendPerY - latterImageWidth - lineHalfWidth + i;
				int leftLineX2 = leftLineX1 - imageHeight * addendPerY; //centerX - centerY * addendPerY - latterImageWidth - lineHalfWidth + i;
				int rightLineX1 = leftLineX1 + latterImageWidth * 2;
				int rightLineX2 = leftLineX2 + latterImageWidth * 2;

				drawLine(dstPixelsPtr, imageWidth, imageHeight, leftLineX1, 0, leftLineX2, imageHeight, lineColor);
				drawLine(dstPixelsPtr, imageWidth, imageHeight, rightLineX1, 0, rightLineX2, imageHeight, lineColor);
			}
		}
	}
}

JNIEXPORT void JNICALL Java_com_kiwiple_multimedia_canvas_PixelUtils_nativeApplyLightingEffect
(JNIEnv *env, jclass jobj, jintArray srcPixels, jintArray dstPixels, jint current)
{
	jint *srcPixelsPtr = env->GetIntArrayElements(srcPixels, 0);
	jint *dstPixelsPtr = env->GetIntArrayElements(dstPixels, 0);

	float whiteB = 1.f / (current+ 127.0f);
	float r,g,b = 0.f;
	int length = env->GetArrayLength(srcPixels);
	for (int i = length; i--;) {
		r = (float) (srcPixelsPtr[i] & 0xff) * whiteB* 255.0f;
		g = (float) ((srcPixelsPtr[i] >> 8) & 0xff) * whiteB* 255.0f;
		b = (float) ((srcPixelsPtr[i] >> 16) & 0xff) * whiteB* 255.0f;

		if (r < 0.f) r = 0.f;
		if (r > 255.f) r = 255.f;
		if (g < 0.f) g = 0.f;
		if (g > 255.f) g = 255.f;
		if (b < 0.f) b = 0.f;
		if (b > 255.f) b = 255.f;
		dstPixelsPtr[i] = ((int) r) + (((int) g) << 8) + (((int) b) << 16) + (srcPixelsPtr[i] & 0xff000000);
	}

	env->ReleaseIntArrayElements(srcPixels, srcPixelsPtr, 0);
	env->ReleaseIntArrayElements(dstPixels, dstPixelsPtr, 0);
}


JNIEXPORT void JNICALL Java_com_kiwiple_multimedia_canvas_PixelUtils_nativeApplyRedLightingEffect
(JNIEnv *env, jclass jobj, jintArray srcPixels, jintArray dstPixels, jint current)
{
	jint *srcPixelsPtr = env->GetIntArrayElements(srcPixels, 0);
	jint *dstPixelsPtr = env->GetIntArrayElements(dstPixels, 0);

	float whiteB = 1.f / (current+ 127.0f);
	float r,g,b = 0.f;
	int length = env->GetArrayLength(srcPixels);
	for (int i = length; i--;) {
		r = (float) (srcPixelsPtr[i] & 0xff) * whiteB* 255.0f;
		g = (float) ((srcPixelsPtr[i] >> 8) & 0xff) * whiteB* 255.0f;
		b = (float) ((srcPixelsPtr[i] >> 16) & 0xff) * whiteB* 255.0f+100.0f;

		if (r < 0.f) r = 0.f;
		if (r > 255.f) r = 255.f;
		if (g < 0.f) g = 0.f;
		if (g > 255.f) g = 255.f;
		if (b < 0.f) b = 0.f;
		if (b > 255.f) b = 255.f;
		dstPixelsPtr[i] = ((int) r) + (((int) g) << 8) + (((int) b) << 16) + (srcPixelsPtr[i] & 0xff000000);
	}

	env->ReleaseIntArrayElements(srcPixels, srcPixelsPtr, 0);
	env->ReleaseIntArrayElements(dstPixels, dstPixelsPtr, 0);
}



JNIEXPORT void JNICALL Java_com_kiwiple_multimedia_canvas_PixelUtils_nativeApplyCircleLightingEffect
(JNIEnv *env, jclass jobj, jintArray srcPixels, jintArray dstPixels, jint cx, jint cy, jint scale, jint width, jint height, jint light)
{
	jint *srcPixelsPtr = env->GetIntArrayElements(srcPixels, 0);
	jint *dstPixelsPtr = env->GetIntArrayElements(dstPixels, 0);

	float r,g,b = 0.f;
	int i, j;
	int ylight = -1;
	int u = scale;
	int m = scale/2;
	int pos = 0;
	unsigned c1 = (u * u)/4.0;
	unsigned c2 = u/2;
	unsigned c3 = 0.5;
	float basel = light + 127.0f;
	float whiteB = 1.f /basel;

	//light = light/2;

	if(cx<0) cx = 0;
	else if(cx>width) cx = width-1;
	if(cy<0) cy = 0;
	else if(cy>height) cy = height-1;

	for (i = u; i--;) {
		c3 = i + 0.5;
		for (j = u; j--;) {
			if (c1 > (c3 - c2) * (c3 - c2) + (j + 0.5 - c2) * (j + 0.5 - c2)) {
				pos =  cx + i + (j + cy) * width;
				if(pos >= width * height) {
					continue;
				}
				r = (float) (srcPixelsPtr[pos] & 0xff) * whiteB * 255.0f;
				g = (float) ((srcPixelsPtr[pos] >> 8) & 0xff) * whiteB * 255.0f;
				b = (float) ((srcPixelsPtr[pos] >> 16) & 0xff) * whiteB * 255.0f;

				if (r < 0.f) r = 0.f;
				if (r > 255.f) r = 255.f;
				if (g < 0.f) g = 0.f;
				if (g > 255.f) g = 255.f;
				if (b < 0.f) b = 0.f;
				if (b > 255.f) b = 255.f;

				dstPixelsPtr[pos] = ((int) r) + (((int) g) << 8) + (((int) b) << 16) + (srcPixelsPtr[pos] & 0xff000000);

				if (j >= m) {
					ylight -= 1;
				} else {
					ylight += 1;
				}

				if(ylight>-64 && ylight<0) whiteB = 1.f / (ylight + basel);

			}
		}

		if(j <= 0) ylight = -1;

		if (i >= m) {
			ylight -= 1;
		} else {
			ylight += 1;
		}

		if(ylight>-64 && ylight<0) whiteB = 1.f / (ylight + basel);
	}

	env->ReleaseIntArrayElements(srcPixels, srcPixelsPtr, 0);
	env->ReleaseIntArrayElements(dstPixels, dstPixelsPtr, 0);
}

JNIEXPORT void JNICALL Java_com_kiwiple_multimedia_canvas_PixelUtils_nativeApplyGrayscale
(JNIEnv *env, jclass jobj, jlong id)
{
	PixelCanvas *canvas = getCanvas(id);
	int *ptr = canvas->getStartPointer();
	int * const ptrEnd = canvas->getEndPointer();

	while (ptr != ptrEnd) {
		int pixel = *ptr;
		int a = (pixel & 0xff000000);
		int r = (pixel & 0x00ff0000) >> 16;
		int g = (pixel & 0x0000ff00) >> 8;
		int b = (pixel & 0x000000ff);
		int gray = (r + g + b) / 3;
		*ptr = a + (gray << 16) + (gray << 8) + gray;

		++ptr;
	}
}

JNIEXPORT void JNICALL Java_com_kiwiple_multimedia_canvas_PixelUtils_nativeConvertYuv420pToArgb
(JNIEnv *env, jclass jobj, jbyteArray arrayYUV, jintArray arrayARGB, jint width, jint height)
{
	jbyte *ptrYUV = env->GetByteArrayElements(arrayYUV, 0);
	jint *ptrARGB = env->GetIntArrayElements(arrayARGB, 0);

	int size = width * height;
	int halfWidth = width / 2;

	uint8 *srcY = reinterpret_cast<uint8 *>(ptrYUV);
	uint8 *srcU = srcY + size;
	uint8 *srcV = srcU + size / 4;
	uint8 *dstARGB = reinterpret_cast<uint8 *>(ptrARGB);

	libyuv::I420ToARGB(srcY, width, srcU, halfWidth, srcV, halfWidth, dstARGB, width * 4, width, height);

	env->ReleaseIntArrayElements(arrayARGB, ptrARGB, 0);
	env->ReleaseByteArrayElements(arrayYUV, ptrYUV, 0);
}

JNIEXPORT void JNICALL Java_com_kiwiple_multimedia_canvas_PixelUtils_nativeConvertYuv420spToArgb
(JNIEnv *env, jclass jobj, jbyteArray arrayYUV, jintArray arrayARGB, jint width, jint height)
{
	jbyte *ptrYUV = env->GetByteArrayElements(arrayYUV, 0);
	jint *ptrARGB = env->GetIntArrayElements(arrayARGB, 0);

	uint8 *srcY = reinterpret_cast<uint8 *>(ptrYUV);
	uint8 *srcVU = srcY + width * height;
	uint8 *dstARGB = reinterpret_cast<uint8 *>(ptrARGB);

	libyuv::NV12ToARGB(srcY, width, srcVU, width, dstARGB, width * 4, width, height);

	env->ReleaseIntArrayElements(arrayARGB, ptrARGB, 0);
	env->ReleaseByteArrayElements(arrayYUV, ptrYUV, 0);
}

JNIEXPORT void JNICALL Java_com_kiwiple_multimedia_canvas_PixelUtils_nativeConvertArgbToYuv420sp
(JNIEnv *env, jclass jobj, jintArray arrayARGB, jbyteArray arrayYUV, jint width, jint height)
{
	jbyte *ptrYUV = env->GetByteArrayElements(arrayYUV, 0);
	jint *ptrARGB = env->GetIntArrayElements(arrayARGB, 0);

	uint8 *srcARGB = reinterpret_cast<uint8 *>(ptrARGB);
	uint8 *dstY = reinterpret_cast<uint8 *>(ptrYUV);
	uint8 *dstVU = dstY + width * height;

	libyuv::ARGBToNV12(srcARGB, width * 4, dstY, width, dstVU, width, width, height);

	env->ReleaseIntArrayElements(arrayARGB, ptrARGB, 0);
	env->ReleaseByteArrayElements(arrayYUV, ptrYUV, 0);
}

JNIEXPORT void JNICALL Java_com_kiwiple_multimedia_canvas_PixelUtils_nativeRotate
(JNIEnv *env, jclass jobj, jintArray srcArray, jintArray dstArray, jint width, jint height, jint rotation)
{
	jint *ptrsrc = env->GetIntArrayElements(srcArray, 0);
	jint *ptrdst = env->GetIntArrayElements(dstArray, 0);

	const int size = width * height;
	const int sizePlusOne = size + 1;

	if (rotation == 90) {

		for (int from = 0, to = height - 1; from != size; ++from, to += height) {
			if (to >= size) {
				to -= sizePlusOne;
			}
			ptrdst[to] = ptrsrc[from];
		}
	} else if (rotation == 180) {
		for (int from = 0, to = size - 1; from != size; ++from, --to) {
			ptrdst[to] = ptrsrc[from];
		}
	} else if (rotation == 270) {
		for (int from = 0, to = size - height; from != size; ++from, to -= height) {
			if (to < 0) {
				to += sizePlusOne;
			}
			ptrdst[to] = ptrsrc[from];
		}
	}
	env->ReleaseIntArrayElements(srcArray, ptrsrc, 0);
	env->ReleaseIntArrayElements(dstArray, ptrdst, 0);
}

JNIEXPORT void JNICALL Java_com_kiwiple_multimedia_canvas_PixelUtils_nativeCrop
(JNIEnv *env, jclass jobj, jintArray srcArray, jintArray dstArray, jint width, jint height, jint left, jint top, jint right, jint bottom)
{
	jint *ptrsrc = env->GetIntArrayElements(srcArray, 0);
	jint *ptrdst = env->GetIntArrayElements(dstArray, 0);

	int oldWidth = width;
	int newWidth = right - left;
	int newHeight = bottom - top;
	int copyLength = sizeof(jint) * newWidth;

	jint* whereToGet = ptrsrc + left + top * oldWidth;
	jint* whereToPut = ptrdst;

	for (int y = top; y < bottom; ++y)
	{
		memcpy(whereToPut, whereToGet, copyLength);
		whereToGet += oldWidth;
		whereToPut += newWidth;
	}

	env->ReleaseIntArrayElements(srcArray, ptrsrc, 0);
	env->ReleaseIntArrayElements(dstArray, ptrdst, 0);
}
