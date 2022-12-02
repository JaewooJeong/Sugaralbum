package com.kiwiple.multimedia.math.interpolator;

import android.animation.TimeInterpolator;
import android.view.animation.DecelerateInterpolator;

/**
 * 빠르게 시작하여 점점 느려지는 보간법을 제공하는 {@link TimeInterpolator}. {@code factor}가 {@code 1.0f}인
 * {@link DecelerateInterpolator}보다 변량의 편차가 큽니다.
 */
public class CubicOutInterpolator implements TimeInterpolator {

	@Override
	public float getInterpolation(float input) {
		input -= 1.0f;
		return input * input * input + 1.0f;
	}
}
