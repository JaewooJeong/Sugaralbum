package com.kiwiple.multimedia.math.interpolator;

import android.animation.TimeInterpolator;

/**
 * 느리게 시작하여 절반까지는 점점 빠르게, 이후로는 다시 점점 느려지는 보간법을 제공하는 {@link TimeInterpolator}.
 * {@link QuarticInOutInterpolator}보다 변량의 편차가 큽니다.
 */
public class ExponentialInOutInterpolator implements TimeInterpolator {

	@Override
	public float getInterpolation(float input) {

		double value = input * 2.0f;
		if (value < 1.0) {
			return (float) (0.5 * Math.pow(2.0, 10.0 * (value - 1.0)));
		} else {
			value -= 1.0;
			return (float) (0.5 * (-Math.pow(2.0, -10.0 * value) + 2.0));
		}
	}
}
