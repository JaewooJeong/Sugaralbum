package com.kiwiple.multimedia.math.interpolator;

import android.animation.TimeInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import com.kiwiple.debug.Precondition;

/**
 * 라이브러리 사용자가 임의로 선택 가능한 {@link TimeInterpolator}의 종류를 구분하기 위한 열거형.
 */
public enum InterpolatorType {

	/**
	 * {@link LinearInterpolator}에 해당하는 {@code InterpolatorType}입니다.
	 */
	LINEAR(LinearInterpolator.class),

	/**
	 * {@link AccelerateInterpolator}에 해당하는 {@code InterpolatorType}입니다. {@code factor}는
	 * {@code 1.0f} 고정입니다.
	 */
	ACCELERATE(AccelerateInterpolator.class),

	/**
	 * {@link DecelerateInterpolator}에 해당하는 {@code InterpolatorType}입니다. {@code factor}는
	 * {@code 1.0f} 고정입니다.
	 */
	DECELERATE(DecelerateInterpolator.class),

	/**
	 * {@link AccelerateDecelerateInterpolator}에 해당하는 {@code InterpolatorType}입니다.
	 */
	ACCELERATE_DECELERATE(AccelerateDecelerateInterpolator.class),

	/**
	 * {@link OvershootInterpolator}에 해당하는 {@code InterpolatorType}입니다. {@code tension}은
	 * {@code 2.0f} 고정입니다.
	 */
	OVERSHOOT(OvershootInterpolator.class),

	/**
	 * {@link BounceInterpolator}에 해당하는 {@code InterpolatorType}입니다.
	 */
	BOUNCE(BounceInterpolator.class),

	/**
	 * {@link CubicInInterpolator}에 해당하는 {@code InterpolatorType}입니다.
	 */
	CUBIC_IN(CubicInInterpolator.class),

	/**
	 * {@link CubicOutInterpolator}에 해당하는 {@code InterpolatorType}입니다.
	 */
	CUBIC_OUT(CubicOutInterpolator.class),

	/**
	 * {@link CubicInOutInterpolator}에 해당하는 {@code InterpolatorType}입니다.
	 */
	CUBIC_IN_OUT(CubicInOutInterpolator.class),

	/**
	 * {@link QuarticInInterpolator}에 해당하는 {@code InterpolatorType}입니다.
	 */
	QUARTIC_IN(QuarticInInterpolator.class),

	/**
	 * {@link QuarticOutInterpolator}에 해당하는 {@code InterpolatorType}입니다.
	 */
	QUARTIC_OUT(QuarticOutInterpolator.class),

	/**
	 * {@link QuarticInOutInterpolator}에 해당하는 {@code InterpolatorType}입니다.
	 */
	QUARTIC_IN_OUT(QuarticInOutInterpolator.class),

	/**
	 * {@link ExponentialInInterpolator}에 해당하는 {@code InterpolatorType}입니다.
	 */
	EXPONENTIAL_IN(ExponentialInInterpolator.class),

	/**
	 * {@link ExponentialOutInterpolator}에 해당하는 {@code InterpolatorType}입니다.
	 */
	EXPONENTIAL_OUT(ExponentialOutInterpolator.class),

	/**
	 * {@link ExponentialInOutInterpolator}에 해당하는 {@code InterpolatorType}입니다.
	 */
	EXPONENTIAL_IN_OUT(ExponentialInOutInterpolator.class);

	public static final String DEFAULT_JSON_NAME = "interpolator_type";

	// // // // // Member variable.
	// // // // //
	private final Class<? extends TimeInterpolator> interpolatorClass;

	private InterpolatorType(Class<? extends TimeInterpolator> interpolatorClass) {
		Precondition.checkNotNull(interpolatorClass);
		this.interpolatorClass = interpolatorClass;
	}

	public static TimeInterpolator createInterpolator(InterpolatorType interpolatorType) {

		if (interpolatorType == null)
			return null;
		try {
			return interpolatorType.interpolatorClass.newInstance();
		} catch (Exception exception) {
			return Precondition.assureUnreachable();
		}
	}

	public TimeInterpolator createInterpolator() {

		try {
			return interpolatorClass.newInstance();
		} catch (Exception exception) {
			return Precondition.assureUnreachable();
		}
	}
}