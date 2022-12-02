#include <cstring>
#include <cmath>

#include "PixelCanvas.h"
#include "CanvasScope.h"
#include "LogNDK.h"

PixelCanvas::PixelCanvas(JNIEnv * const env, const jintArray jArray) :
		jArray(reinterpret_cast<jintArray>(env->NewGlobalRef(jArray))),
		buffer(env->GetIntArrayElements(jArray, nullptr)),
		bufferSize(env->GetArrayLength(jArray)) {

	if (bufferSize > sharedCanvasBufferSize)
		updateSharedCanvasQueue(bufferSize);
	jniReferenceCount += 1;
}

PixelCanvas::PixelCanvas(const uint bufferSize) :
		jArray(nullptr),
		buffer(new int[bufferSize]),
		bufferSize(bufferSize) {

}

void PixelCanvas::finalize(JNIEnv * const env) const {

	env->ReleaseIntArrayElements(jArray, buffer, JNI_ABORT);
	env->DeleteGlobalRef(jArray);

	jniReferenceCount -= 1;
	if (jniReferenceCount == 0) {
		clearSharedCanvasQueue();
		sharedCanvasBufferSize = 0;
	}
}

void PixelCanvas::setImageSize(const int width, const int height) {
	this->width = width;
	this->height = height;
}

void PixelCanvas::setOffset(const int offset) {
	this->offset = offset;
}

void PixelCanvas::drawPoint(const int color, const int x, const int y) {
	if (!validate(x, y)) {
		return;
	}

	int *ptr = getPointer(x, y);
	*ptr = color;
}

void PixelCanvas::blendPoint(const int color, const int x, const int y) {
	if (!validate(x, y)) {
		return;
	}

	int *ptr = getPointer(x, y);
	*ptr = blend(color, *ptr);
}

void PixelCanvas::blendPoint(const int color, const int alpha, const int x, const int y) {
	if (!validate(x, y)) {
		return;
	}

	int *ptr = getPointer(x, y);
	uint dst = *ptr;
	*ptr = blend(color, alpha, dst, dst >> 24);
}

void PixelCanvas::blendPointWithMultiplier(const int color, const int x, const int y, const float multiplier) {
	if (!validate(x, y)) {
		return;
	}

	int *ptr = getPointer(x, y);
	*ptr = blendWithMultiplier(color, *ptr, multiplier);
}

void PixelCanvas::drawLine(const int color, const int startX, const int startY, const int endX, const int endY, const float thicknessPx) {

	const int dx = abs(endX - startX);
	const int sx = startX < endX ? 1 : -1;
	const int dy = abs(endY - startY);
	const int sy = startY < endY ? 1 : -1;
	const float ed = dx + dy == 0 ? 1 : std::sqrt((float) (dx * dx + dy * dy));
	const float thickness = (thicknessPx + 1.0f) / 2.0f;
	const float thicknessXed = thickness * ed;

	int cx = startX, cy = startY;
	int err = dx - dy;

	while (true) {
		float alpha = min(1.0f, -(abs(err - dx + dy) / ed - thickness));
		blendPointWithMultiplier(color, cx, cy, alpha);

		int err2 = err, cx2 = cx, cy2, count;
		if (err2 * 2 >= -dx) {
			for (cy2 = cy, err2 += dy, count = 0; err2 < thicknessXed && (dx > dy || endY != cy2); err2 += dx, ++count) {
				alpha = min(1.0f, -(abs(err2) / ed - thickness));
				cy2 += sy;
				blendPointWithMultiplier(color, cx, cy2, alpha);
			}

			if (sy == 1) {
				cy2 -= count + 1;
				for (count = (count >= thickness) ? thickness - 1.0f : thickness; count > 0; --count, --cy2)
					if (cy2 < startY)
						blendPointWithMultiplier(color, cx, cy2, count == 1 ? 1.0f - alpha : 1.0f);
			} else {
				cy2 += count + 1;
				for (count = (count >= thickness) ? thickness - 1.0f : thickness; count > 0; --count, ++cy2)
					if (cy2 > startY)
						blendPointWithMultiplier(color, cx, cy2, count == 1 ? 1.0f - alpha : 1.0f);
			}
			if (cx == endX)
				break;
			err2 = err;
			err -= dy;
			cx += sx;
		}
		if (err2 * 2 <= dy) {
			for (err2 = dx - err2, count = 0; err2 < thicknessXed && (dx < dy || endX != cx2); err2 += dy, ++count) {
				alpha = min(1.0f, -(abs(err2) / ed - thickness));
				cx2 += sx;
				blendPointWithMultiplier(color, cx2, cy, alpha);
			}

			if (sx == 1) {
				cx2 -= count + 1;
				for (count = (count >= thickness) ? thickness - 1.0f : thickness; count > 0; --count, --cx2)
					if (cx2 < startX)
						blendPointWithMultiplier(color, cx2, cy, count == 1 ? 1.0f - alpha : 1.0f);
			} else {
				cx2 += count + 1;
				for (count = (count >= thickness) ? thickness - 1.0f : thickness; count > 0; --count, ++cx2)
					if (cx2 > startX)
						blendPointWithMultiplier(color, cx2, cy, count == 1 ? 1.0f - alpha : 1.0f);
			}
			if (cy == endY)
				break;
			err += dx;
			cy += sy;
		}
	}
}

void PixelCanvas::drawRect(const int color, int startX, int startY, int endX, int endY) {

	if (startX > endX)
		std::swap(startX, endX);
	if (startY > endY)
		std::swap(startY, endY);

	for (int i = startX; i <= endX; ++i) {
		blendPoint(color, i, startY);
		blendPoint(color, i, endY);
	}
	for (int i = startY + 1; i < endY; ++i) {
		blendPoint(color, startX, i);
		blendPoint(color, endX, i);
	}
}

void PixelCanvas::drawRect(const int color, int startX, int startY, int endX, int endY, const float thicknessPx) {

	if (thicknessPx <= 0.0f)
		return;
	if (startX > endX)
		std::swap(startX, endX);
	if (startY > endY)
		std::swap(startY, endY);

	if (thicknessPx <= 1.0f) {
		drawRect(getAlphaMultiplied(color, thicknessPx), startX, startY, endX, endY);
	} else {
		int offset = thicknessPx > 1.0f ? (int) std::ceil((thicknessPx - 1.0f) / 2.0f) : 0;
		float edgeAlpha = thicknessPx > 1.0f ? std::fmod((thicknessPx - 1.0f) / 2.0f, 1.0f) : thicknessPx;

		if (offset > 0 && edgeAlpha != 1.0f) {
			int edgeColor = getAlphaMultiplied(color, edgeAlpha);
			drawRect(edgeColor, startX - offset, startY - offset, endX + offset, endY + offset);
			drawRect(edgeColor, startX + offset, startY + offset, endX - offset, endY - offset);
			offset -= 1;
		}

		startX = startX - offset;
		endX = endX + offset;
		startY = startY - offset;
		endY = endY + offset;
		int innerThickness = offset * 2 + 1;

		for (int y = startY; y <= endY; ++y) {
			if ((y < startY + innerThickness) || (y > endY - innerThickness)) {
				for (int x = startX; x <= endX; ++x) {
					blendPoint(color, x, y);
				}
			} else {
				int rightSideStartX = endX - innerThickness + 1;
				for (int i = 0; i < innerThickness; ++i) {
					blendPoint(color, startX + i, y);
					blendPoint(color, rightSideStartX + i, y);
				}
			}
		}
	}
}

void PixelCanvas::drawOval(const int color, const int x, const int y, const int xRadius, const int yRadius) {
	if (xRadius <= 0 || yRadius <= 0) {
		return;
	}

	int x0 = x - (xRadius - 1), x1 = x + (xRadius - 1);
	int y0 = y - (yRadius - 1), y1 = y + (yRadius - 1);
	int a = abs(x1 - x0), b = abs(y1 - y0), b1 = b & 1;
	float dx = 4 * (a - 1.0) * b * b, dy = 4 * (b1 + 1) * a * a;
	float ed, i, err = b1 * a * a - dx + dy;
	bool f;

	if (a == 0 || b == 0) {
		return drawLine(color, x0, y0, x1, y1, 1.0f);
	}
	if (x0 > x1) {
		x0 = x1;
		x1 += a;
	}
	if (y0 > y1) {
		y0 = y1;
	}
	y0 += (b + 1) / 2;
	y1 = y0 - b1;
	a = 8 * a * a;
	b1 = 8 * b * b;

	int alpha;
	while (true) {
		i = dx > dy ? dy : dx;
		ed = dx > dy ? dx : dy;
		if (y0 == y1 + 1 && err > dy && a > b1) {
			ed = 255 * 4. / a;
		} else {
			ed = 255 / (ed + 2 * ed * i * i / (4 * ed * ed + i * i));
		}
		alpha = 255 - round(ed * abs(err + dx - dy));
		blendPoint(color, alpha, x0, y0);
		blendPoint(color, alpha, x0, y1);
		blendPoint(color, alpha, x1, y0);
		blendPoint(color, alpha, x1, y1);

		if (f = 2 * err + dy >= 0) {
			if (x0 >= x1)
				break;
			alpha = 255 - round(ed * (err + dx));
			blendPoint(color, alpha, x0, y0 + 1);
			blendPoint(color, alpha, x0, y1 - 1);
			blendPoint(color, alpha, x1, y0 + 1);
			blendPoint(color, alpha, x1, y1 - 1);
		}
		if (2 * err <= dx) {
			alpha = 255 - round(ed * (dy - err));
			blendPoint(color, alpha, x0 + 1, y0);
			blendPoint(color, alpha, x1 - 1, y0);
			blendPoint(color, alpha, x0 + 1, y1);
			blendPoint(color, alpha, x1 - 1, y1);
			y0++;
			y1--;
			err += dy += a;
		}
		if (f) {
			x0++;
			x1--;
			err -= dx -= b1;
		}
	}
	if (--x0 == x1++) {
		while (y0 - y1 < b) {
			alpha = 255 - round(255.0f * 4.0f * abs(err + dx) / b1);
			blendPoint(color, alpha, x0, ++y0);
			blendPoint(color, alpha, x1, y0);
			blendPoint(color, alpha, x0, --y1);
			blendPoint(color, alpha, x1, y1);
			err += dy += a;
		}
	}
}

void PixelCanvas::drawOval(const int color, const int x, const int y, const int xRadius, const int yRadius, const int thickness) {
	if (thickness <= 0)
		return;

	for (int i = 0, offset = thickness / 2; i != thickness; ++i) {
		drawOval(color, x, y, xRadius - offset + i, yRadius - offset + i);
	}
}

void PixelCanvas::fillOval(const int color, const int x, const int y, const int xRadius, const int yRadius) {
	if (xRadius <= 0 || yRadius <= 0) {
		return;
	}

	int width = xRadius * 2 - 1;
	int height = yRadius * 2 - 1;

	PixelCanvas *sharedCanvas = getSharedCanvas();
	sharedCanvas->setImageSize(width, height);
	sharedCanvas->clear(COLOR_TRANSPARENT);
	sharedCanvas->drawOval(color, xRadius - 1, yRadius - 1, xRadius, yRadius);

	int colorQuarterAlpha = extractAlpha(color) / 4;
	int *basePtr = sharedCanvas->getStartPointer() + width;
	int *basePtrEnd = sharedCanvas->getEndPointer() - width;
	int i = 0;
	while (basePtr < basePtrEnd) {

		int *ptr = basePtr;
		int *ptrEnd = ptr + width;

		for (bool passBorder(false), startFill(false); ptr < ptrEnd; ++ptr) {

			if (!passBorder) {
				if (isTransparent(*ptr)) {
					--ptrEnd;
					continue;
				}
				passBorder = true;
			}
			if (!startFill) {
				if (colorQuarterAlpha < extractAlpha(*ptr))
					startFill = true;
				--ptrEnd;
				continue;
			}
			*ptr = color;
		}
		basePtr += width;
	}
	sharedCanvas->blend(this, x - (xRadius - 1), y - (yRadius - 1));
	returnSharedCanvas(sharedCanvas);
}

void PixelCanvas::rotate(int degree) {

	degree %= 360;
	if (degree == 0) {
		return;
	} else if (degree < 0) {
		degree += 360;
	}

	PixelCanvas *sharedCanvas = getSharedCanvas();
	sharedCanvas->setImageSize(width, height);
	copy(sharedCanvas);

	const int size = getLogicalBufferSize();
	int *srcPtr = sharedCanvas->getStartPointer();
	int *srcPtrEnd = sharedCanvas->getEndPointer();

	if (degree == 90) {
		int *dstPtrEnd = getEndPointer();
		for (int *dstPtr = getStartPointer() + height - 1; srcPtr != srcPtrEnd; ++srcPtr, dstPtr += height) {
			if (dstPtr >= dstPtrEnd) {
				dstPtr -= (size + 1);
			}
			*dstPtr = *srcPtr;
		}
		std::swap(width, height);
	} else if (degree == 180) {
		for (int *dstPtr = getEndPointer() - 1; srcPtr != srcPtrEnd; ++srcPtr, --dstPtr) {
			*dstPtr = *srcPtr;
		}
	} else if (degree == 270) {
		int *dstPtrStart = getStartPointer();
		for (int *dstPtr = getEndPointer() - height; srcPtr != srcPtrEnd; ++srcPtr, dstPtr -= height) {
			if (dstPtr < dstPtrStart) {
				dstPtr += (size + 1);
			}
			*dstPtr = *srcPtr;
		}
		std::swap(width, height);
	}
	returnSharedCanvas(sharedCanvas);
}

void PixelCanvas::clear(const int color) {

	int *ptr = getStartPointer();
	int *ptrEnd = getEndPointer();

	while (ptr != ptrEnd) {
		*ptr = color;
		++ptr;
	}
}

void PixelCanvas::clear(const int color, const CanvasScope &scope) {

	int *basePtr = getStartPointerOn(scope);
	int *basePtrEnd = getEndPointerOn(scope);
	int clearWidth = scope.getWidth();

	while (basePtr < basePtrEnd) {

		int *ptr = basePtr;
		int *ptrEnd = ptr + clearWidth;
		while (ptr != ptrEnd) {
			*ptr = color;
			++ptr;
		}
		basePtr += width;
	}
}

void PixelCanvas::tint(const uint color) {

	int *ptr = getStartPointer();
	int *ptrEnd = getEndPointer();
	int alpha = extractAlpha(color);

	while (ptr != ptrEnd) {
		*ptr = blend(color, alpha, *ptr);
		++ptr;
	}
}

void PixelCanvas::tint(const uint color, CanvasScope &scope) {

	int *basePtr = getStartPointerOn(scope);
	int *basePtrEnd = getEndPointerOn(scope);
	int tintWidth = scope.getWidth();
	int alpha = extractAlpha(color);

	while (basePtr < basePtrEnd) {

		int *ptr = basePtr;
		int *ptrEnd = ptr + tintWidth;
		while (ptr != ptrEnd) {
			*ptr = blend(color, alpha, *ptr);
			++ptr;
		}
		basePtr += width;
	}
}

void PixelCanvas::blend(PixelCanvas * const dstCanvas, const float multiplier) const {

	CanvasScope srcScope(this);
	CanvasScope dstScope(dstCanvas);
	blend(dstCanvas, srcScope, dstScope, multiplier);
}

void PixelCanvas::blend(PixelCanvas * const dstCanvas, const int dstX, const int dstY, const float multiplier) const {

	CanvasScope srcScope(this);
	CanvasScope dstScope(dstX, dstY, width, height, true);
	blend(dstCanvas, srcScope, dstScope, multiplier);
}

void PixelCanvas::blend(PixelCanvas * const dstCanvas, CanvasScope &srcScope, CanvasScope &dstScope, const float multiplier) const {

	trimScope(srcScope);
	CanvasScope::minimize(srcScope, dstScope);
	dstCanvas->trimScope(dstScope, srcScope);

	int *srcBasePtr = getStartPointerOn(srcScope);
	int *srcBasePtrEnd = getEndPointerOn(srcScope);
	int *dstBasePtr = dstCanvas->getStartPointerOn(dstScope);
	int blendWidth = srcScope.getWidth();

	while (srcBasePtr < srcBasePtrEnd) {

		int *srcPtr = srcBasePtr;
		int *srcPtrEnd = srcPtr + blendWidth;
		int *dstPtr = dstBasePtr;

		if (multiplier == 1.0f) {
			while (srcPtr != srcPtrEnd) {
				*dstPtr = blend(*srcPtr, *dstPtr);
				++srcPtr;
				++dstPtr;
			}
		} else {
			while (srcPtr != srcPtrEnd) {
				*dstPtr = blendWithMultiplier(*srcPtr, *dstPtr, multiplier);
				++srcPtr;
				++dstPtr;
			}
		}
		srcBasePtr += width;
		dstBasePtr += dstCanvas->width;
	}
}

void PixelCanvas::blendWithMask(PixelCanvas * const dstCanvas, PixelCanvas * const maskCanvas) const {

	CanvasScope srcScope(this);
	CanvasScope dstScope(dstCanvas);
	CanvasScope maskScope(maskCanvas);
	blendWithMask(dstCanvas, maskCanvas, srcScope, dstScope, maskScope);
}

void PixelCanvas::blendWithMask(PixelCanvas * const dstCanvas, PixelCanvas * const maskCanvas, int x, int y) const {

	CanvasScope srcScope(x, y, maskCanvas->width, maskCanvas->height, true);
	CanvasScope maskScope(maskCanvas);
	trimScope(srcScope, maskScope);

	blendWithMask(dstCanvas, maskCanvas, srcScope, srcScope, maskScope);
}

void PixelCanvas::blendWithMask(PixelCanvas * const dstCanvas, PixelCanvas * const maskCanvas, CanvasScope &srcScope, CanvasScope &dstScope, CanvasScope &maskScope) const {

	CanvasScope::minimize({&srcScope, &dstScope, &maskScope});

	int *srcBasePtr = getStartPointerOn(srcScope);
	int *srcBasePtrEnd = getEndPointerOn(srcScope);
	int *dstBasePtr = dstCanvas->getStartPointerOn(dstScope);
	int *maskBasePtr = maskCanvas->getStartPointerOn(maskScope);
	int blendWidth = srcScope.getWidth();

	while (srcBasePtr < srcBasePtrEnd) {

		int *srcPtr = srcBasePtr;
		int *srcPtrEnd = srcPtr + blendWidth;
		int *dstPtr = dstBasePtr;
		int *maskPtr = maskBasePtr;
		while (srcPtr != srcPtrEnd) {
			int maskAlpha = extractAlpha(*maskPtr);
			*dstPtr = blend(*srcPtr, maskAlpha, *dstPtr);
			++srcPtr;
			++dstPtr;
			++maskPtr;
		}
		srcBasePtr += width;
		dstBasePtr += dstCanvas->width;
		maskBasePtr += maskCanvas->width;
	}
}

void PixelCanvas::copy(PixelCanvas * const dstCanvas) const {

	CanvasScope srcScope(this);
	CanvasScope dstScope(dstCanvas);
	copy(dstCanvas, srcScope, dstScope);
}

void PixelCanvas::copy(PixelCanvas * const dstCanvas, CanvasScope &srcScope, CanvasScope &dstScope) const {

	trimScope(srcScope);
	CanvasScope::minimize(srcScope, dstScope);
	dstCanvas->trimScope(dstScope, srcScope);

	int *srcPtr = getStartPointerOn(srcScope);
	int *srcPtrEnd = getEndPointerOn(srcScope);
	int *dstPtr = dstCanvas->getStartPointerOn(dstScope);
	int copyBytes = srcScope.getWidth() * 4;

	while (srcPtr < srcPtrEnd) {
		std::memcpy(dstPtr, srcPtr, copyBytes);
		srcPtr += width;
		dstPtr += dstCanvas->width;
	}
}

void PixelCanvas::copy(PixelCanvas * const dstCanvas, int srcX, int srcY, int dstX, int dstY, int width, int height) const {

	CanvasScope srcScope(srcX, srcY, width, height, true);
	CanvasScope dstScope(dstX, dstY, width, height, true);
	copy(dstCanvas, srcScope, dstScope);
}

void PixelCanvas::copyWithScale(PixelCanvas * const dstCanvas, const float dstX, const float dstY, const float scale) const {

	if (scale <= 0.0f)
		return;

	int scaledWidth = round(width * scale);
	int scaledHeight = round(height * scale);
	if (width == scaledWidth && height == scaledHeight)
		return copy(dstCanvas, 0, 0, dstX, dstY, width, height);

	int drawWidth = round(dstX < 0.0f ? min((float) dstCanvas->width, dstX + scaledWidth) : min(dstCanvas->width - dstX, (float) scaledWidth));
	int drawHeight = round(dstY < 0.0f ? min((float) dstCanvas->height, dstY + scaledHeight) : min(dstCanvas->height - dstY, (float) scaledHeight));
	if (drawWidth <= 0 || drawHeight <= 0)
		return;

	float ratioPerPixel = 1.0f / scale;
	float srcStartX = (dstX < 0.0f) ? -dstX : 0.0f;
	float srcStartY = (dstY < 0.0f) ? -dstY : 0.0f;

	PixelCanvas *sharedCanvas = getSharedCanvas();
	int *xOffsets = sharedCanvas->buffer;
	int *shareXs = xOffsets + drawWidth;

	for (int i = 0; i < drawWidth; ++i) {
		float xFloat = (i + srcStartX) * ratioPerPixel;
		int x = (int) xFloat;
		xOffsets[i] = x;

		int shareNextX = 0xff * (xFloat - x);
		shareXs[i] = 0xff - shareNextX;
	}
	{
		int lastIndex = drawWidth - 1;
		if (xOffsets[lastIndex] == width - 1) {
			shareXs[lastIndex] = 0xff;
		}
	}

	int *srcPtr = getStartPointer();
	int *dstPtr = dstCanvas->getStartPointer();
	int dstStartX = round(max(0.0f, dstX));
	int dstStartY = round(max(0.0f, dstY));

	for (int i = 0, lastY = height - 1; i < drawHeight; ++i) {

		int dstIndex = (i + dstStartY) * dstCanvas->width + dstStartX;
		float yFloat = (i + srcStartY) * ratioPerPixel;
		int y = (int) yFloat;

		int srcOffset = y * width;
		int srcOffsetForNextLine = (y != lastY ? width : 0);
		int shareNextY = (y != lastY ? 0xff * (yFloat - y) : 0);
		int shareY = 0xff - shareNextY;

		for (int j = 0; j < drawWidth; ++j) {

			int shareX = shareXs[j];
			int shareLT = (shareX * shareY) >> 8;
			int shareRT = shareY - shareLT;
			int shareLB = (shareX * shareNextY) >> 8;
			int shareRB = shareNextY - shareLB;

			int srcIndex = srcOffset + xOffsets[j];
			int LT = srcPtr[srcIndex++];
			int RT = srcPtr[srcIndex];
			srcIndex += srcOffsetForNextLine;
			int RB = srcPtr[srcIndex--];
			int LB = srcPtr[srcIndex];

			int outRB = 0xff00ff00 & (
					((LT & 0x00ff00ff) * shareLT) +
					((RT & 0x00ff00ff) * shareRT) +
					((LB & 0x00ff00ff) * shareLB) +
					((RB & 0x00ff00ff) * shareRB));
			int outG = 0x00ff0000 & (
					((LT & 0x0000ff00) * shareLT) +
					((RT & 0x0000ff00) * shareRT) +
					((LB & 0x0000ff00) * shareLB) +
					((RB & 0x0000ff00) * shareRB));

			dstPtr[dstIndex++] = 0xff000000 | (outRB | outG) >> 8;
		}
	}
	returnSharedCanvas(sharedCanvas);
}

void PixelCanvas::trimScope(CanvasScope &scope) const {
	trimScope(scope, scope);
}

void PixelCanvas::trimScope(CanvasScope &scope, CanvasScope &syncScope) const {

	bool synchronize = (&scope != &syncScope);

	if (scope.left < 0) {
		if (synchronize)
			syncScope.left -= scope.left;
		scope.left = 0;
	}
	if (scope.top < 0) {
		if (synchronize)
			syncScope.top -= scope.top;
		scope.top = 0;
	}
	if (scope.right > width) {
		if (synchronize)
			syncScope.right -= scope.right - width;
		scope.right = width;
	}
	if (scope.bottom > height) {
		if (synchronize)
			syncScope.bottom -= scope.bottom - height;
		scope.bottom = height;
	}
}

int * PixelCanvas::getStartPointerOn(const CanvasScope &scope) const {
	return validate(scope) ? getStartPointer() + scope.top * width + scope.left : 0;
}

int * PixelCanvas::getEndPointerOn(const CanvasScope &scope) const {
	return validate(scope) ? getStartPointer() + (scope.bottom - 1) * width + scope.right : 0;
}

bool PixelCanvas::validate(const CanvasScope &scope) const {
	return scope.isValid() && scope.left >= 0 && scope.top >= 0 && scope.right <= width && scope.bottom <= height;
}

int PixelCanvas::blend(const uint src, const uint dst) {
	return blend(src, src >> 24, dst, dst >> 24);
}

int PixelCanvas::blend(const int src, const int srcAlpha, const uint dst) {
	return blend(src, srcAlpha, dst, dst >> 24);
}

int PixelCanvas::blend(const int src, const int srcAlpha, const int dst, const int dstAlpha) {

	if (srcAlpha >= 255) {
		return 0xff000000 | src;
	} else if (dstAlpha <= 0) {
		return srcAlpha > 0 ? (srcAlpha << 24) | (src & 0x00ffffff) : COLOR_TRANSPARENT;
	} else if (srcAlpha <= 0) {
		return dstAlpha > 0 ? (dstAlpha << 24) | (dst & 0x00ffffff) : COLOR_TRANSPARENT;
	}

	int srcRB = src & 0x00ff00ff;
	int srcG = src & 0x0000ff00;
	int dstRB = dst & 0x00ff00ff;
	int dstG = dst & 0x0000ff00;

	int dstMultiplier = dstAlpha * (0xff - srcAlpha) / 0xff;
	int outA = srcAlpha + dstAlpha - ((srcAlpha * dstAlpha) / 0xff);
	int outRB = (srcRB * srcAlpha + dstRB * dstMultiplier) & 0xff00ff00;
	int outG = (srcG * srcAlpha + dstG * dstMultiplier) & 0x00ff0000;

	return (outA << 24) | (outRB | outG) >> 8;
}

int PixelCanvas::blendWithMultiplier(const uint src, const uint dst, const float multiplier) {
	return blend(src, (src >> 24) * multiplier, dst);
}

void PixelCanvas::updateSharedCanvasQueue(const int bufferSize) {

	if (bufferSize > sharedCanvasBufferSize) {
		sharedCanvasBufferSize = bufferSize;

		if (sharedCanvasQueue.empty())
			return;

		int count = sharedCanvasQueue.size();
		clearSharedCanvasQueue();

		for (int i = 0; i != count; ++i)
			sharedCanvasQueue.push(new PixelCanvas(bufferSize));
	}
}

void PixelCanvas::clearSharedCanvasQueue() {

	while (!sharedCanvasQueue.empty()) {
		PixelCanvas *canvas = sharedCanvasQueue.front();
		sharedCanvasQueue.pop();

		delete [] canvas->buffer;
		delete canvas;
	}
}

PixelCanvas * PixelCanvas::getSharedCanvas() {

	if (sharedCanvasQueue.empty())
		sharedCanvasQueue.push(new PixelCanvas(sharedCanvasBufferSize));
	PixelCanvas *canvas = sharedCanvasQueue.front();
	sharedCanvasQueue.pop();

	return canvas;
}

void PixelCanvas::returnSharedCanvas(PixelCanvas * const canvas) {
	sharedCanvasQueue.push(canvas);
}

std::queue<PixelCanvas *> PixelCanvas::sharedCanvasQueue;
int PixelCanvas::sharedCanvasBufferSize = 0;
int PixelCanvas::jniReferenceCount = 0;
