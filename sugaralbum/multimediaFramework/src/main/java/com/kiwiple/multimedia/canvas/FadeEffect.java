package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;

/**
 * {@link Scene}이 생성한 이미지가 서서히 사라지거나 서서히 나타나는 효과를 적용하는 클래스.
 */
public final class FadeEffect extends Effect {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "fade_effect";

	public static final String JSON_NAME_USE_FADE_IN = "use_fade_in";
	public static final String JSON_NAME_USE_FADE_OUT = "use_fade_out";
	public static final String JSON_NAME_DURATION_RATIO_NO_FADE = "duration_ratio_no_fade";

	// // // // // Member variable.
	// // // // //
	private boolean mUseFadeIn;
	private boolean mUseFadeOut;

	private float mDurationRatioNoFade;
	private float mFadeInEndTime;
	private float mFadeOutStartTime;

	// // // // // Constructor.
	// // // // //
	FadeEffect(Scene parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_USE_FADE_IN, mUseFadeIn);
		jsonObject.put(JSON_NAME_USE_FADE_OUT, mUseFadeOut);
		jsonObject.put(JSON_NAME_DURATION_RATIO_NO_FADE, mDurationRatioNoFade);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		float durationRatioNoFade = jsonObject.getFloat(JSON_NAME_DURATION_RATIO_NO_FADE);
		setDurationRatioNoFade(durationRatioNoFade);

		boolean useFadeIn = jsonObject.getBoolean(JSON_NAME_USE_FADE_IN);
		boolean useFadeOut = jsonObject.getBoolean(JSON_NAME_USE_FADE_OUT);
		setFade(useFadeIn, useFadeOut);
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {

		checkValidity(mDurationRatioNoFade > 0.0f, "You must invoke setDurationRatioNoFade()");
		checkValidity(mUseFadeIn || mUseFadeOut, "You must invoke setFade()");

		mFadeInEndTime = mUseFadeIn ? (1.0f - mDurationRatioNoFade) / (mUseFadeOut ? 2.0f : 1.0f) : 0.0f;
		mFadeOutStartTime = mUseFadeOut ? (mUseFadeIn ? mFadeInEndTime + mDurationRatioNoFade / 2.0f : mDurationRatioNoFade) : 1.0f;
	}

	@Override
	void onDraw(PixelCanvas dstCanvas) {

		float progressRatio = getProgressRatio();

		if (mUseFadeIn && progressRatio < mFadeInEndTime) {
			float fadeInRatio = progressRatio / mFadeInEndTime;
			dstCanvas.tint(Math.round(0xff * fadeInRatio) << 24);
		} else if (mUseFadeOut && progressRatio >= mFadeOutStartTime) {
			float fadeOutRatio = 1.0f - (progressRatio - mFadeOutStartTime) / (1.0f - mFadeOutStartTime);
			dstCanvas.tint(Math.round(0xff * fadeOutRatio) << 24);
		}
	}

	void setFade(boolean useFadeIn, boolean useFadeOut) {
		Precondition.checkArgument(useFadeIn || useFadeOut, "Don't try to set false either");

		mUseFadeIn = useFadeIn;
		mUseFadeOut = useFadeOut;
	}

	/**
	 * 서서히 나타나는 효과의 적용 여부를 반환합니다.
	 * 
	 * @return 적용 중일 때 {@code true}.
	 */
	public boolean isUsingFadeIn() {
		return mUseFadeIn;
	}

	/**
	 * 서서히 사라지는 효과의 적용 여부를 반환합니다.
	 * 
	 * @return 적용 중일 때 {@code true}.
	 */
	public boolean isUsingFadeOut() {
		return mUseFadeOut;
	}

	void setDurationRatioNoFade(float durationRatio) {
		Precondition.checkOnlyPositive(durationRatio);

		if (durationRatio > 1.0f) {
			durationRatio = 1.0f;
		}
		mDurationRatioNoFade = durationRatio;
	}

	/**
	 * 전체 시간 길이 중, 효과가 적용되지 않는 구간의 길이를 비율로써 반환합니다.
	 */
	public float getDurationRatioNoFade() {
		return mDurationRatioNoFade;
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link FadeEffect}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Effect.Editor<FadeEffect, Editor> {

		private Editor(FadeEffect fadeEffect) {
			super(fadeEffect);
		}

		/**
		 * 전체 시간 길이 중, 효과가 적용되지 않는 구간의 길이를 비율로써 설정합니다.
		 * 
		 * @param durationRatio
		 *            {@code 0.0f}에서 {@code 1.0f} 사이, 즉 [{@code 0.0f, 1.0f}]의 값을 가지는 시간 비율.
		 * 
		 */
		public Editor setDurationRatioNoFade(float durationRatio) {
			getObject().setDurationRatioNoFade(durationRatio);
			return this;
		}

		/**
		 * 효과 적용의 시작점에서 서서히 나타나는 효과를 적용할 것인지, 그리고 끝점에서 서서히 사라지는 효과를 적용할 것인지의 여부를 설정합니다.
		 * 
		 * @param useFadeIn
		 *            서서히 나타나는 효과를 적용하려면 {@code true}.
		 * @param useFadeOut
		 *            서서히 사라지는 효과를 적용하려면 {@code true}.
		 */
		public Editor setFade(boolean useFadeIn, boolean useFadeOut) {
			getObject().setFade(useFadeIn, useFadeOut);
			return this;
		}
	}
}
