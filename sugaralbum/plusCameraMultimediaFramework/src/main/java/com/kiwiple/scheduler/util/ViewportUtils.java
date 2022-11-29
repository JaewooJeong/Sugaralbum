package com.kiwiple.scheduler.util;

import com.kiwiple.multimedia.canvas.data.Viewport;

/**
 * ViewportUtils.
 */
public final class ViewportUtils {

	public static Direction measureDirection(Viewport from, Viewport to) {

		if (from.top == to.top && from.bottom == to.bottom) {
			return (from.left > to.left && from.right > to.right) ? Direction.LEFT : Direction.RIGHT;
		} else if (from.left == to.left && from.right == to.right) {
			return (from.top > to.top && from.bottom > to.bottom) ? Direction.UP : Direction.DOWN;
		} else {
			return from.contains(to) ? Direction.ZOOM_IN : to.contains(from) ? Direction.ZOOM_OUT : null;
		}
	}

	public static enum Direction {

		LEFT,

		UP,

		RIGHT,

		DOWN,

		ZOOM_IN,

		ZOOM_OUT;
	}
	
	private ViewportUtils() {
		// Utility classes should have a private constructor.
	}
}