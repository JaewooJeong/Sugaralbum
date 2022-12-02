#pragma once

#include <initializer_list>

class PixelCanvas;
class CanvasScope final {

public:
	friend class PixelCanvas;

	explicit CanvasScope();
	explicit CanvasScope(int left, int top, int right, int bottom, bool useRelative);
	explicit CanvasScope(const PixelCanvas * const canvas);
	explicit CanvasScope(const CanvasScope &other) = delete;
	~CanvasScope() = default;

	static void minimize(CanvasScope &scope1, CanvasScope &scope2);
	static void minimize(std::initializer_list<CanvasScope *> scopes);

	inline int getWidth() const { return right - left; };
	inline int getHeight() const { return bottom - top; };
	inline bool isValid() const { return right - left > 0 && bottom - top > 0; };

private:
	int left;
	int top;
	int right;
	int bottom;

};
