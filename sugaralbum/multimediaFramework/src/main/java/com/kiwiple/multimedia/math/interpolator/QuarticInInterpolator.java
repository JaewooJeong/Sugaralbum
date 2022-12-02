package com.kiwiple.multimedia.math.interpolator;

import android.animation.TimeInterpolator;

/**
 * 느리게 시작하여 점점 빨라지는 보간법을 제공하는 {@link TimeInterpolator}. {@link CubicInInterpolator}보다 변량의 편차가 큽니다.
 */
public class QuarticInInterpolator implements TimeInterpolator {

	@Override
	public float getInterpolation(float input) {
		return input * input * input * input;
	}
}