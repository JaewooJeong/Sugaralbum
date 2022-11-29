#pragma once

#include <queue>
#include <jni.h>

class CanvasScope;
class PixelCanvas final {

public:
	using uint = unsigned int;

	explicit PixelCanvas() = delete;
	explicit PixelCanvas(JNIEnv * const env, const jintArray jArray);
	explicit PixelCanvas(const PixelCanvas &other) = delete;
	~PixelCanvas() = default;

	void finalize(JNIEnv * const env) const;
	void setImageSize(const int width, const int height);
	void setOffset(const int offset);

	void drawPoint(const int color, const int x, const int y);
	void blendPoint(const int color, const int x, const int y);
	void blendPoint(const int color, const int alpha, const int x, const int y);
	void blendPointWithMultiplier(const int color, const int x, const int y, const float multiplier = 1.0f);
	void drawLine(const int color, const int startX, const int startY, const int endX, const int endY, const float thicknessPx);
	void drawRect(const int color, int startX, int startY, int endX, int endY);
	void drawRect(const int color, int startX, int startY, int endX, int endY, const float thicknessPx);
	void drawOval(const int color, const int x, const int y, const int xRadius, const int yRadius);
	void drawOval(const int color, const int x, const int y, const int xRadius, const int yRadius, const int thickness);
	void fillOval(const int color, const int x, const int y, const int xRadius, const int yRadius);

	void rotate(int degree);

	void clear(const int color);
	void clear(const int color, const CanvasScope &scope);

	void tint(const uint color);
	void tint(const uint color, CanvasScope &scope);
	void blend(PixelCanvas * const dstCanvas, const float multiplier = 1.0f) const;
	void blend(PixelCanvas * const dstCanvas, const int dstX, const int dstY, const float multiplier = 1.0f) const;
	void blend(PixelCanvas * const dstCanvas, CanvasScope &srcScope, CanvasScope &dstScope, const float multiplier = 1.0f) const;
	void blendWithMask(PixelCanvas * const dstCanvas, PixelCanvas * const maskCanvas) const;
	void blendWithMask(PixelCanvas * const dstCanvas, PixelCanvas * const maskCanvas, int x, int y) const;
	void blendWithMask(PixelCanvas * const dstCanvas, PixelCanvas * const maskCanvas, CanvasScope &srcScope, CanvasScope &dstScope, CanvasScope &maskScope) const;
	void copy(PixelCanvas * const dstCanvas) const;
	void copy(PixelCanvas * const dstCanvas, CanvasScope &srcScope, CanvasScope &dstScope) const;
	void copy(PixelCanvas * const dstCanvas, int srcX, int srcY, int dstX, int dstY, int width, int height) const;
	void copyWithScale(PixelCanvas * const dstCanvas, const float dstX, const float dstY, const float scale) const;

	void trimScope(CanvasScope &scope) const;
	void trimScope(CanvasScope &scope, CanvasScope &syncScope) const;

	inline int getWidth() const { return width; };
	inline int getHeight() const { return height; };
	inline int getOffset() const { return offset; };

	inline int * getPointer(const int x, const int y) const { return buffer + offset + x + y * width; };
	inline int * getStartPointer() const { return buffer + offset; };
	inline int * getEndPointer() const { return buffer + offset + getLogicalBufferSize(); };

private:
	explicit PixelCanvas(const uint bufferSize);

	inline static int blend(const uint src, const uint dst);
	inline static int blend(const int src, const int srcAlpha, const uint dst);
	inline static int blend(const int src, const int srcAlpha, const int dst, const int dstAlpha);
	inline static int blendWithMultiplier(const uint src, const uint dst, const float multiplier);

	inline int getLogicalBufferSize() const { return width * height; };

	inline int * getStartPointerOn(const CanvasScope &scope) const;
	inline int * getEndPointerOn(const CanvasScope &scope) const;

	inline bool validate(const int x, const int y) const { return x >= 0 && x < width && y >= 0 && y < height; };
	inline bool validate(const CanvasScope &scope) const;

	inline static int isTransparent(const uint color) { return color <= 0x00ffffffu; };
	inline static int extractAlpha(const uint color) { return color >> 24; };
	inline static int getAlphaMultiplied(const uint color, const float multiplier) { return (round((color >> 24) * multiplier) << 24) | (color & 0x00ffffff); };
	inline static int abs(const int value) { return value >= 0 ? value : -value; };
	inline static float abs(const float value) { return value >= 0.0f ? value : -value; };
	inline static int max(const int value1, const int value2) { return value1 > value2 ? value1 : value2; };
	inline static float max(const float value1, const float value2) { return value1 > value2 ? value1 : value2; };
	inline static int min(const int value1, const int value2) { return value1 < value2 ? value1 : value2; };
	inline static float min(const float value1, const float value2) { return value1 < value2 ? value1 : value2; };
	inline static int round(const float value) { return value + 0.5f; };
	inline static int round(const double value) { return value + 0.5; };

	static void updateSharedCanvasQueue(const int bufferSize);
	static void clearSharedCanvasQueue();
	static void returnSharedCanvas(PixelCanvas * const canvas);
	static PixelCanvas * getSharedCanvas();

	static const int COLOR_TRANSPARENT = 0x0;

	static std::queue<PixelCanvas *> sharedCanvasQueue;
	static int sharedCanvasBufferSize;
	static int jniReferenceCount;

	const jintArray jArray;
	int * const buffer;

	int bufferSize = 0;
	int width = 0;
	int height = 0;
	int offset = 0;
};
