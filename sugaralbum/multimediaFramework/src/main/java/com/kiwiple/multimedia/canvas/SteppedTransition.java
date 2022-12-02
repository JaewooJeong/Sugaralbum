package com.kiwiple.multimedia.canvas;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.json.JSONException;

import android.animation.TimeInterpolator;
import android.graphics.Rect;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.json.JsonObject;

/**
 * SteppedTransition.
 * 
 */
@Deprecated
final class SteppedTransition extends Transition {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "stepped_transition";

	public static final String JSON_NAME_PRESET = "preset";
	public static final String JSON_NAME_DIRECTION = "direction";

	private static final TimeInterpolator DEFAULT_INTERPOLATOR = new OvershootInterpolator(2.0f);
	private static final SlideDirection DEFAULT_SLIDE_DIRECTION = SlideDirection.RANDOM;
	private static final DisappearingStyle DEFAULT_DISAPPEARING_STYLE = DisappearingStyle.ADHERE;

	private static final int DEFAULT_INTERVAL_BETWEEN_STEPS = 150;
	private static final int DEFAULT_STEP_COUNT = 6;
	private static final int DEFAULT_STEP_DURATION = 600;

	// // // // // Member variable.
	// // // // //
	private final Random mRandom = new Random();

	private List<Integer> mSequence = new ArrayList<Integer>(DEFAULT_STEP_COUNT);
	private List<Integer> mSequenceShuffled = new ArrayList<Integer>(DEFAULT_STEP_COUNT);

	private TimeInterpolator mInterpolator = DEFAULT_INTERPOLATOR;
	private SlideDirection mSlideDirection = DEFAULT_SLIDE_DIRECTION;
	private DisappearingStyle mDisappearingStyle = DEFAULT_DISAPPEARING_STYLE;

	private Preset mLastAppliedPreset = Preset.DEFAULT;

	private final Rect mRectDst = new Rect();
	private final Rect mRectSrc = new Rect();

	private int mStepCount = DEFAULT_STEP_COUNT;
	private float mStepDuration = DEFAULT_STEP_DURATION;
	private float mStepInterval = DEFAULT_INTERVAL_BETWEEN_STEPS;

	private boolean mIsSequential = true;
	private boolean mIsReverse = false;

	private boolean mIsHorizontal = true;
	private boolean mIsMixedDirection = false;
	private boolean mIsNegativeDirection = false;

	// // // // // Constructor.
	// // // // //
	SteppedTransition(Region parent) {
		super(parent);

		mInterpolator = DEFAULT_INTERPOLATOR;
		setSlideDirection(DEFAULT_SLIDE_DIRECTION);
		setStepCount(DEFAULT_STEP_COUNT);
	}

	// // // // // Method.
	// // // // //
	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	void __setDuration__(int duration) {

		float formerDuration = getDuration();

		super.setDuration(duration);

		float ratio = getDuration() / formerDuration;
		mStepDuration *= ratio;
		mStepInterval *= ratio;
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();
		jsonObject.put(JSON_NAME_PRESET, mLastAppliedPreset.name);
		jsonObject.put(JSON_NAME_DIRECTION, mSlideDirection.name);

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		String presetName = jsonObject.getString(JSON_NAME_PRESET);
		applyPreset(Preset.byName(presetName));

		String directionName = jsonObject.getString(JSON_NAME_DIRECTION);
		setSlideDirection(SlideDirection.byName(directionName));
	}

	@Override
	void onDraw(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {

		int width = getWidth();
		int height = getHeight();

		int position = getPosition();
		int completedStepCount = measureCompletedStepCount(position);
		float stepSize = (mIsHorizontal ? (float) height : (float) width) / mStepCount;

		List<Integer> sequence = mIsSequential ? mSequence : mSequenceShuffled;

		if (mDisappearingStyle.equals(DisappearingStyle.ADHERE)) {
			// Do nothing.
		} else if (mDisappearingStyle.equals(DisappearingStyle.OVERLAY) || mDisappearingStyle.equals(DisappearingStyle.WIPE)) {
			srcCanvasFormer.copy(dstCanvas);
		} else if (mDisappearingStyle.equals(DisappearingStyle.VANISH)) {
			dstCanvas.clear(Visualizer.DEFAULT_CLEAR_COLOR);
		}

		for (int i = completedStepCount; i != mStepCount; ++i) {

			int stepIndex = sequence.get(i);
			int startTime = Math.round(mStepInterval * i);
			float innerRatio = (position - startTime) / mStepDuration;

			if (innerRatio < 0.0f) {

				if (mDisappearingStyle.equals(DisappearingStyle.ADHERE)) {

					if (mIsHorizontal) {
						mRectSrc.left = 0;
						mRectSrc.top = Math.round(stepSize * stepIndex);
						mRectSrc.right = width;
						mRectSrc.bottom = Math.round(stepSize * (stepIndex + 1));
					} else {
						mRectSrc.left = Math.round(stepSize * stepIndex);
						mRectSrc.top = 0;
						mRectSrc.right = Math.round(stepSize * (stepIndex + 1));
						mRectSrc.bottom = height;
					}

					copyRect(srcCanvasFormer.intArray, dstCanvas.intArray, width, height, mRectSrc, mRectSrc);
					continue;
				}
				break;
			}

			if (mInterpolator != null) {
				innerRatio = mInterpolator.getInterpolation(innerRatio);
			}

			boolean flagNegative = (mIsMixedDirection && i % 2 != 0) || (!mIsMixedDirection && mIsNegativeDirection);

			if (mIsHorizontal) {

				int stepWidth = Math.round(width * innerRatio);

				mRectSrc.top = Math.round(stepSize * stepIndex);
				mRectSrc.bottom = Math.round(stepSize * (stepIndex + 1));
				mRectDst.top = mRectSrc.top;
				mRectDst.bottom = mRectSrc.bottom;

				if (mDisappearingStyle.equals(DisappearingStyle.ADHERE)) {

					mRectSrc.left = flagNegative ? stepWidth : 0;
					mRectSrc.right = flagNegative ? width : width - stepWidth;
					mRectDst.left = flagNegative ? 0 : stepWidth;
					mRectDst.right = flagNegative ? width - stepWidth : width;

					dstCanvas.clear(Visualizer.DEFAULT_CLEAR_COLOR, 0, mRectSrc.top, width, mRectSrc.height());
				}

				mRectSrc.left = flagNegative ? 0 : width - stepWidth;
				mRectSrc.right = flagNegative ? stepWidth : width;
				mRectDst.left = flagNegative ? width - stepWidth : 0;
				mRectDst.right = flagNegative ? width : stepWidth;
			} else {

				int stepHeight = Math.round(height * innerRatio);

				mRectSrc.left = Math.round(stepSize * stepIndex);
				mRectSrc.right = Math.round(stepSize * (stepIndex + 1));
				mRectDst.left = mRectSrc.left;
				mRectDst.right = mRectSrc.right;

				if (mDisappearingStyle.equals(DisappearingStyle.ADHERE)) {

					mRectSrc.top = flagNegative ? stepHeight : 0;
					mRectSrc.bottom = flagNegative ? height : height - stepHeight;
					mRectDst.top = flagNegative ? 0 : stepHeight;
					mRectDst.bottom = flagNegative ? height - stepHeight : height;

					dstCanvas.clear(Visualizer.DEFAULT_CLEAR_COLOR, mRectSrc.left, 0, mRectSrc.width(), height);
				}

				mRectSrc.top = flagNegative ? 0 : height - stepHeight;
				mRectSrc.bottom = flagNegative ? stepHeight : height;
				mRectDst.top = flagNegative ? height - stepHeight : 0;
				mRectDst.bottom = flagNegative ? height : stepHeight;
			}

			if (mDisappearingStyle.equals(DisappearingStyle.WIPE)) {
				copyRect(srcCanvasLatter.intArray, dstCanvas.intArray, width, height, mRectSrc, mRectSrc);
			} else {
				copyRect(srcCanvasLatter.intArray, dstCanvas.intArray, width, height, mRectSrc, mRectDst);
			}
		}

		mRectSrc.left = 0;
		mRectSrc.top = 0;
		mRectSrc.right = width;
		mRectSrc.bottom = height;

		for (int i = 0; i != completedStepCount; ++i) {

			int stepIndex = sequence.get(i);
			if (mIsHorizontal) {
				mRectSrc.top = Math.round(stepSize * stepIndex);
				mRectSrc.bottom = Math.round(stepSize * (stepIndex + 1));
			} else {
				mRectSrc.left = Math.round(stepSize * stepIndex);
				mRectSrc.right = Math.round(stepSize * (stepIndex + 1));
			}

			copyRect(srcCanvasLatter.intArray, dstCanvas.intArray, width, height, mRectSrc, mRectSrc);
		}
	}

	private void computeDuration() {
		super.setDuration(Math.round(mStepDuration + mStepInterval * (mStepCount - 1)));
	}

	private void randomize() {

		mIsHorizontal = mRandom.nextBoolean();
		mIsNegativeDirection = mRandom.nextBoolean();
	}

	private int measureCompletedStepCount(int time) {

		time -= mStepDuration;

		if (time < 0) {
			return 0;
		} else {
			return (int) (time / mStepInterval) + 1;
		}
	}

	void applyPreset(Preset preset) {

		if (preset == null) {
			preset = Preset.DEFAULT;
		}

		switch (preset) {
			case DEFAULT:
				setStepCount(DEFAULT_STEP_COUNT);
				setStepDuration(DEFAULT_STEP_DURATION);
				setStepInterval(DEFAULT_INTERVAL_BETWEEN_STEPS);
				setSlideDirection(DEFAULT_SLIDE_DIRECTION);
				setSequential(true);
				setMixed(false);
				setDisappearingStyle(DEFAULT_DISAPPEARING_STYLE);
				setInterpolator(DEFAULT_INTERPOLATOR);
				break;

			case SWIFT_BIRD:
				setStepCount(80);
				setStepDuration(40);
				setStepInterval(1.0f);
				setSequential(false);
				setMixed(false);
				setSlideDirection(SlideDirection.RANDOM);
				setDisappearingStyle(DisappearingStyle.ADHERE);
				setInterpolator(new OvershootInterpolator(2.0f));
				break;

			case PAGE_TURNING:
				setStepCount(200);
				setStepDuration(1600);
				setStepInterval(4.0f);
				setSequential(true);
				setMixed(false);
				setSlideDirection(SlideDirection.DOWN);
				setDisappearingStyle(DisappearingStyle.OVERLAY);
				setInterpolator(new DecelerateInterpolator(2.0f));
				break;

			case NET_CRAFT:
				setStepCount(50);
				setStepDuration(1200);
				setStepInterval(80.0f);
				setSequential(true);
				setMixed(true);
				setSlideDirection(SlideDirection.LEFT);
				setDisappearingStyle(DisappearingStyle.ADHERE);
				setInterpolator(new OvershootInterpolator(2.0f));
				break;

			case SOMETHING_NOISY:
				setStepCount(500);
				setStepDuration(1000);
				setStepInterval(8.0f);
				setSequential(false);
				setMixed(true);
				setSlideDirection(SlideDirection.LEFT);
				setDisappearingStyle(DisappearingStyle.OVERLAY);
				setInterpolator(new OvershootInterpolator(2.0f));
				break;

			case MAGIC_EYE:
				setStepCount(250);
				setStepDuration(3500);
				setStepInterval(4.0f);
				setSequential(true);
				setMixed(true);
				setSlideDirection(SlideDirection.RIGHT);
				setDisappearingStyle(DisappearingStyle.ADHERE);
				setInterpolator(new BounceInterpolator());
				break;

			case COUNTER_ATTACK:
				setStepCount(25);
				setStepDuration(1200);
				setStepInterval(300.0f);
				setSequential(true);
				setMixed(true);
				setSlideDirection(SlideDirection.LEFT);
				setDisappearingStyle(DisappearingStyle.VANISH);
				setInterpolator(new BounceInterpolator());
				break;

			case PETIT_EARTHQUAKE:
				setStepCount(10);
				setStepDuration(600);
				setStepInterval(1.0f);
				setSequential(false);
				setMixed(false);
				setSlideDirection(SlideDirection.RANDOM);
				setDisappearingStyle(DisappearingStyle.ADHERE);
				setInterpolator(new BounceInterpolator());
				break;

			case JUST_PUSH:
				setStepCount(1);
				setStepInterval(0.0f);
				setMixed(false);
				setSlideDirection(SlideDirection.RANDOM);
				setDisappearingStyle(DisappearingStyle.ADHERE);
				setInterpolator(new OvershootInterpolator(2.0f));
				break;

			default:
				break;
		}
		computeDuration();
		mLastAppliedPreset = preset;
	}

	void setStepCount(int stepCount) {
		Precondition.checkOnlyPositive(stepCount);

		mStepCount = stepCount;

		mSequence.clear();
		for (int i = 0; i != stepCount; ++i) {
			mSequence.add(i);
		}

		mSequenceShuffled = new ArrayList<Integer>(mSequence);
		Collections.shuffle(mSequenceShuffled, new Random());

		computeDuration();
	}

	public int getStepCount() {
		return mStepCount;
	}

	void setStepDuration(float stepDuration) {
		Precondition.checkOnlyPositive(stepDuration);

		mStepDuration = stepDuration;
		computeDuration();
	}

	public float getStepDuration() {
		return mStepDuration;
	}

	void setStepInterval(float stepInterval) {
		Precondition.checkNotNegative(stepInterval);

		mStepInterval = stepInterval;
		computeDuration();
	}

	public float getStepInterval() {
		return mStepInterval;
	}

	void setInterpolator(TimeInterpolator interpolator) {
		mInterpolator = interpolator;
	}

	public TimeInterpolator getInterpolator() {
		return mInterpolator;
	}

	void setSequential(boolean isSequential) {
		mIsSequential = isSequential;
	}

	public boolean isSequential() {
		return mIsSequential;
	}

	void setReverse(boolean isReverse) {
		mIsReverse = isReverse;
	}

	public boolean isReverse() {
		return mIsReverse;
	}

	void setMixed(boolean isMixed) {
		mIsMixedDirection = isMixed;
	}

	public boolean isMixed() {
		return mIsMixedDirection;
	}

	void setDisappearingStyle(DisappearingStyle style) {
		Precondition.checkNotNull(style);
		mDisappearingStyle = style;
	}

	public DisappearingStyle getDisappearingStyle() {
		return mDisappearingStyle;
	}

	void setSlideDirection(SlideDirection slideDirection) {
		Precondition.checkNotNull(slideDirection);

		mSlideDirection = slideDirection;
		if (mSlideDirection == SlideDirection.RANDOM) {
			randomize();
		} else {
			mIsHorizontal = (mSlideDirection == SlideDirection.LEFT) || (mSlideDirection == SlideDirection.RIGHT);
			mIsNegativeDirection = (mSlideDirection == SlideDirection.LEFT) || (mSlideDirection == SlideDirection.UP);
		}
	}

	public SlideDirection getSlideDirection() {
		return mSlideDirection;
	}

	// // // // // Inner Class.
	// // // // //
	public static final class Editor extends Transition.Editor<SteppedTransition, Editor> {

		private Editor(SteppedTransition steppedTransition) {
			super(steppedTransition);
		}

		public Editor applyPreset(Preset preset) {
			getObject().applyPreset(preset);
			return this;
		}

		public Editor setDirection(SlideDirection direction) {
			getObject().setSlideDirection(direction);
			return this;
		}
	}

	// // // // // Enumeration.
	// // // // //
	public static enum SlideDirection {

		LEFT("left"), UP("up"), RIGHT("right"), DOWN("down"), RANDOM("random");

		public final String name;

		public static SlideDirection byName(String name) {

			for (SlideDirection direction : values()) {
				if (name.equals(direction.name)) {
					return direction;
				}
			}
			return null;
		}

		private SlideDirection(String name) {
			this.name = name;
		}
	}

	public static enum DisappearingStyle {
		VANISH, OVERLAY, ADHERE, WIPE;
	}

	public static enum Preset {

		DEFAULT("default"),

		SWIFT_BIRD("swift_bird"),

		PAGE_TURNING("page_turning"),

		NET_CRAFT("net_craft"),

		SOMETHING_NOISY("something_noisy"),

		MAGIC_EYE("magic_eye"),

		COUNTER_ATTACK("counter_attack"),

		PETIT_EARTHQUAKE("petit_earthquake"),

		JUST_PUSH("just_push");

		static private final HashMap<String, Preset> sPresetNameMap;

		static {
			sPresetNameMap = new HashMap<String, Preset>(Preset.values().length);
			for (Preset preset : Preset.values()) {
				sPresetNameMap.put(preset.name, preset);
			}
		}

		public static Preset byName(String name) {
			return sPresetNameMap.get(name);
		}

		public final String name;

		private Preset(String name) {
			this.name = name;
		}
	}

	private static void copyRect(int[] srcPixels, int[] dstPixels, int imageWidth, int imageHeight, Rect srcRect, Rect dstRect) {

		adjustRectBound(srcRect, dstRect, imageWidth, imageHeight);

		int rectWidth = srcRect.width();
		int rectHeight = srcRect.height();

		if (rectWidth < 1 || rectHeight < 1) {
			return;
		}

		for (int i = 0; i < rectHeight; ++i) {

			int srcOffset = imageWidth * (i + srcRect.top) + srcRect.left;
			int dstOffset = imageWidth * (i + dstRect.top) + dstRect.left;

			System.arraycopy(srcPixels, srcOffset, dstPixels, dstOffset, rectWidth);
		}
		// or can use just nativeCopyPixels(srcPixels, srcPixels, imageWidth, imageHeight,
		// rectWidth, rectHeight, srcRect.left, srcRect.top, dstRect.left, dstRect.top).
		// however, notice there is no appreciable performance difference! - 2014. 07. 03.
		// amaranth@kiwiple.com.
	}

	private static void adjustRectBound(Rect srcRect, Rect dstRect, int width, int height) {

		if (srcRect.left < 0 || dstRect.left < 0) {
			int dif = Math.min(srcRect.left, dstRect.left);
			srcRect.left -= dif;
			dstRect.left -= dif;
		}

		if (srcRect.top < 0 || dstRect.top < 0) {
			int dif = Math.min(srcRect.top, dstRect.top);
			srcRect.top -= dif;
			dstRect.top -= dif;
		}

		if (srcRect.right > width || dstRect.right > width) {
			int dif = Math.max(srcRect.right, dstRect.right) - width;
			srcRect.right -= dif;
			dstRect.right -= dif;
		}

		if (srcRect.bottom > height || dstRect.bottom > height) {
			int dif = Math.max(srcRect.bottom, dstRect.bottom) - height;
			srcRect.bottom -= dif;
			dstRect.bottom -= dif;
		}
	}
}
