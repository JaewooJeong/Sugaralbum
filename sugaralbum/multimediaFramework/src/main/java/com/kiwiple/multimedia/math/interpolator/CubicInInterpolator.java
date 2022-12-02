package com.kiwiple.multimedia.math.interpolator;

import android.animation.TimeInterpolator;
import android.view.animation.AccelerateInterpolator;

/**
 * 느리게 시작하여 점점 빨라지는 보간법을 제공하는 {@link TimeInterpolator}. {@code factor}가 {@code 1.0f}인
 * {@link AccelerateInterpolator}보다 변량의 편차가 큽니다.
 */
public class CubicInInterpolator implements TimeInterpolator {

	@Override
	public float getInterpolation(float input) {
		return input * input * input;
	}
}
