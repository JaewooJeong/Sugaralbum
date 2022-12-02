package com.kiwiple.multimedia.math.interpolator;

import android.animation.TimeInterpolator;

/**
 * 빠르게 시작하여 점점 느려지는 보간법을 제공하는 {@link TimeInterpolator}. {@link QuarticOutInterpolator}보다 변량의 편차가
 * 큽니다.
 */
public class ExponentialOutInterpolator implements TimeInterpolator {

	@Override
	public float getInterpolation(float input) {
		return (float) (-Math.pow(2.0, -10.0 * input) + 1.0);
	}
}
