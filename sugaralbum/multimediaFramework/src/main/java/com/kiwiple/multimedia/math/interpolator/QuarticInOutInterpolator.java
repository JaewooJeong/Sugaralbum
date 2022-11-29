package com.kiwiple.multimedia.math.interpolator;

import android.animation.TimeInterpolator;

/**
 * 느리게 시작하여 절반까지는 점점 빠르게, 이후로는 다시 점점 느려지는 보간법을 제공하는 {@link TimeInterpolator}.
 * {@link CubicInOutInterpolator}보다 변량의 편차가 큽니다.
 */
public class QuarticInOutInterpolator implements TimeInterpolator {

	@Override
	public float getInterpolation(float input) {

		input *= 2.0f;
		if (input < 1.0f) {
			return 0.5f * input * input * input * input;
		} else {
			input -= 2.0f;
			return -0.5f * (input * input * input * input - 2.0f);
		}
	}
}
