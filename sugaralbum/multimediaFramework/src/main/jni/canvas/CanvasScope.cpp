#include <limits>

#include "CanvasScope.h"
#include "PixelCanvas.h"
#include "LogNDK.h"

CanvasScope::CanvasScope() :
		left(0),
		top(0),
		right(0),
		bottom(0) {

}

CanvasScope::CanvasScope(int left, int top, int right, int bottom, bool useRelative) :
		left(left),
		top(top),
		right(useRelative ? left + right : right),
		bottom(useRelative ? top + bottom : bottom) {

	if (!isValid()) {
		left = top = right = bottom = 0;
	}
}

CanvasScope::CanvasScope(const PixelCanvas * const canvas) :
		left(0),
		top(0),
		right(canvas->getWidth()),
		bottom(canvas->getHeight()) {

}

void CanvasScope::minimize(CanvasScope &scope1, CanvasScope &scope2) {

	int width1 = scope1.getWidth();
	int width2 = scope2.getWidth();
	if (width1 > width2) {
		scope1.right = scope1.left + width2;
	} else if (width1 < width2) {
		scope2.right = scope2.left + width1;
	}

	int height1 = scope1.getHeight();
	int height2 = scope2.getHeight();
	if (height1 > height2) {
		scope1.bottom = scope1.top + height2;
	} else if (height1 < height2) {
		scope2.bottom = scope2.top + height1;
	}
}

void CanvasScope::minimize(std::initializer_list<CanvasScope *> scopes) {

	int minWidth = std::numeric_limits<int>::max();
	int minHeight = minWidth;

	for (CanvasScope *scope : scopes) {
		int temp;
		if ((temp = scope->getWidth()) < minWidth)
			minWidth = temp;
		if ((temp = scope->getHeight()) < minHeight)
			minHeight = temp;
	}

	for (CanvasScope *scope : scopes) {
		if (scope->getWidth() > minWidth)
			scope->right = scope->left + minWidth;
		if (scope->getHeight() > minHeight)
			scope->bottom = scope->top + minHeight;
	}
}
