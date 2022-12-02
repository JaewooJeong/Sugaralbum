package com.kiwiple.multimedia.canvas;

import static com.kiwiple.multimedia.Constants.INVALID_INDEX;

import java.util.Arrays;
import java.util.List;

import org.json.JSONException;

import android.animation.TimeInterpolator;
import android.graphics.RectF;

import com.kiwiple.debug.InvalidArrayException;
import com.kiwiple.debug.InvalidCollectionException;
import com.kiwiple.debug.InvalidNumberException;
import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.math.interpolator.InterpolatorType;
import com.kiwiple.multimedia.util.ArrayUtils;
import com.kiwiple.multimedia.util.Size;

/**
 * 주어진 이미지에 대한 시계(視界)를 자유롭게 설정하여, 이에 해당하는 영역을 확대 및 축소하여 출력하는 클래스.
 * <p />
 * 시계는 {@link Viewport}의 배열로써 설정하며, 시간 축의 흐름에 따라 시계에서 시계로 촬영기가 움직이듯 출력 영역이 조정됩니다.
 * 
 * @see Viewport
 */
public final class KenBurnsScaler extends Scaler {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "ken_burns_scaler";

	public static final String JSON_NAME_VIEWPORTS = "viewports";
	public static final String JSON_NAME_INTERPOLATOR_TYPES = "interpolator_types";
	public static final String JSON_NAME_WEIGHTS = "weights";

	private static final Viewport DEFAULT_VIEWPORT = Viewport.FULL_VIEWPORT;
	private static final InterpolatorType DEFAULT_INTERPOLATOR_TYPE = InterpolatorType.LINEAR;
	private static final int[] DEFAULT_WEIGHTS = { 1 };

	// // // // // Member variable.
	// // // // //
	@CacheCode
	private Viewport[] mViewports;
	private TimeInterpolator[] mInterpolators;
	private InterpolatorType[] mInterpolatorTypes;

	private int[] mWeights;
	private float[] mViewportSequence;
	private float[] mViewportDurations;

	private RectF mManagedViewport = new RectF();
	private RectF mManagedViewportNext = new RectF();
	private TimeInterpolator mInterpolator;
	private int mViewportIndex;

	// // // // // Constructor.
	// // // // //
	{
		setViewports(DEFAULT_VIEWPORT);
		setInterpolator(DEFAULT_INTERPOLATOR_TYPE);
		setWeights(DEFAULT_WEIGHTS);
	}

	KenBurnsScaler(Scene parent) {
		super(parent);
	}

	// // // // // Method.
	// // // // //
	@Override
	void onDraw(PixelCanvas srcCanvas, PixelCanvas dstCanvas) {

		float progressRatio = getProgressRatio();

		int viewportIndex = Arrays.binarySearch(mViewportSequence, 0, mViewportSequence.length, progressRatio);
		if (viewportIndex < 0)
			viewportIndex = -viewportIndex - 2;
		boolean isLastViewport = (viewportIndex == (mViewports.length - 1));

		if (mViewportIndex != viewportIndex) {
			Size srcSize = srcCanvas.getImageSize();

			mManagedViewport = mViewports[viewportIndex].asActualSizeRectF(srcSize);
			mManagedViewportNext = isLastViewport ? null : mViewports[viewportIndex + 1].asActualSizeRectF(srcSize);
			mInterpolator = mInterpolators[viewportIndex % mInterpolators.length];
			mViewportIndex = viewportIndex;
		}
		float innerRatio = mInterpolator.getInterpolation((progressRatio - mViewportSequence[viewportIndex]) / mViewportDurations[viewportIndex]);
		float left, top, srcWidth, srcHeight;

		if (!isLastViewport) {
			left = mManagedViewport.left + (mManagedViewportNext.left - mManagedViewport.left) * innerRatio;
			top = mManagedViewport.top + (mManagedViewportNext.top - mManagedViewport.top) * innerRatio;
			float right = mManagedViewport.right + (mManagedViewportNext.right - mManagedViewport.right) * innerRatio;
			float bottom = mManagedViewport.bottom + (mManagedViewportNext.bottom - mManagedViewport.bottom) * innerRatio;

			srcWidth = right - left;
			srcHeight = bottom - top;

		} else {
			left = mManagedViewport.left;
			top = mManagedViewport.top;
			srcWidth = mManagedViewport.right - left;
			srcHeight = mManagedViewport.bottom - top;
		}

		float dstWidth = getWidth();
		float dstHeight = getHeight();
		float srcAspectRatio = srcWidth / srcHeight;
		float dstAspectRatio = dstWidth / dstHeight;

		float scale = (srcAspectRatio > dstAspectRatio) ? (dstHeight / srcHeight) : (dstWidth / srcWidth);
		float dstX = -left * scale;
		float dstY = -top * scale;
		srcCanvas.copyWithScale(dstCanvas, dstX, dstY, scale);
	}

	@Override
	void onUnprepare() {
		mViewportIndex = INVALID_INDEX;
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {

		mViewportIndex = INVALID_INDEX;

		int weightLength = Math.max(1, mViewports.length - 1);
		if (mWeights.length < weightLength) {
			int originalLength = mWeights.length;
			mWeights = Arrays.copyOf(mWeights, weightLength);
			Arrays.fill(mWeights, originalLength, weightLength, 1);
		}

		int[] weights = Arrays.copyOf(mWeights, weightLength);

		if (weights.length == 1) {
			mViewportSequence = new float[] { 0.0f };
			mViewportDurations = new float[] { 1.0f };
		} else {
			mViewportSequence = new float[weightLength];
			mViewportDurations = new float[weightLength];

			int weightSum = ArrayUtils.sum(weights);
			float sequence = 0.0f;
			for (int i = 0; i != weightLength; ++i) {
				mViewportSequence[i] = sequence;
				mViewportDurations[i] = (float) weights[i] / weightSum;
				sequence += mViewportDurations[i];
			}
		}
	}

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}

	@Override
	public JsonObject toJsonObject() throws JSONException {

		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_VIEWPORTS, mViewports);
		jsonObject.put(JSON_NAME_INTERPOLATOR_TYPES, mInterpolatorTypes);
		jsonObject.put(JSON_NAME_WEIGHTS, new JsonArray(mWeights));

		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		setViewports(jsonObject.getJSONArrayAsList(JSON_NAME_VIEWPORTS, Viewport.class));
		setInterpolators(jsonObject.getJSONArrayAsList(JSON_NAME_INTERPOLATOR_TYPES, InterpolatorType.class));
		setWeights(jsonObject.getJSONArrayAsList(JSON_NAME_WEIGHTS, Integer.class));
	}

	void setViewports(Viewport... viewports) {
		Precondition.checkArray(viewports).checkNotEmpty().checkNotContainsNull();
		mViewports = ArrayUtils.copy(viewports);
	}

	void setViewports(List<Viewport> viewports) {
		Precondition.checkCollection(viewports).checkNotEmpty().checkNotContainsNull();
		setViewports(viewports.toArray(new Viewport[viewports.size()]));
	}

	/**
	 * 출력할 영역 정보의 사본을 반환합니다.
	 */
	public Viewport[] getViewports() {
		return ArrayUtils.copy(mViewports);
	}

	/**
	 * @see Editor#setInterpolator(InterpolatorType)
	 */
	void setInterpolator(InterpolatorType interpolatorType) {

		if (interpolatorType == null) {
			interpolatorType = InterpolatorType.LINEAR;
		}
		setInterpolators(interpolatorType);
	}

	/**
	 * @see Editor#setInterpolators(InterpolatorType...)
	 */
	void setInterpolators(InterpolatorType... interpolatorTypes) {
		Precondition.checkArray(interpolatorTypes).checkNotEmpty().checkNotContainsNull();

		mInterpolatorTypes = Arrays.copyOf(interpolatorTypes, interpolatorTypes.length);
		mInterpolators = new TimeInterpolator[mInterpolatorTypes.length];
		for (int i = 0; i != mInterpolatorTypes.length; ++i) {
			mInterpolators[i] = mInterpolatorTypes[i].createInterpolator();
		}
	}

	/**
	 * @see Editor#setInterpolators(List)
	 */
	void setInterpolators(List<InterpolatorType> interpolatorTypes) {
		Precondition.checkCollection(interpolatorTypes).checkNotEmpty().checkNotContainsNull();
		setInterpolators(interpolatorTypes.toArray(new InterpolatorType[0]));
	}

	/**
	 * 사용할 {@link InterpolatorType}의 목록의 사본을 반환합니다.
	 */
	public InterpolatorType[] getInterpolators() {
		return Arrays.copyOf(mInterpolatorTypes, mInterpolatorTypes.length);
	}

	/**
	 * @see Editor#setWeights(int...)
	 */
	void setWeights(int... weights) {
		Precondition.checkOnlyPositive(weights);

		if (weights == null || weights.length == 0) {
			mWeights = DEFAULT_WEIGHTS;
		} else {
			mWeights = Arrays.copyOf(weights, weights.length);
		}
	}

	/**
	 * @see Editor#setWeights(List)
	 */
	void setWeights(List<Integer> weights) {
		setWeights(weights != null ? ArrayUtils.unboxCollectionInteger(weights) : null);
	}

	/**
	 * 각각의 전환 경로가 가지는 시간 길이에 대한 가중치의 사본을 반환합니다.
	 */
	public int[] getWeights() {
		return Arrays.copyOf(mWeights, mWeights.length);
	}

	// // // // // Inner class.
	// // // // //
	/**
	 * {@link KenBurnsScaler}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Scaler.Editor<KenBurnsScaler, Editor> {

		private Editor(KenBurnsScaler kenBurnsScaler) {
			super(kenBurnsScaler);
		}

		/**
		 * {@code Viewport}의 배열로써 출력할 영역을 설정합니다. 만약 배열이 하나의 요소만을 가진다면, 고정된 영역을 출력하게 됩니다.
		 * 
		 * @param viewports
		 *            출력할 영역 정보의 목록.
		 * @throws InvalidArrayException
		 *             주어진 목록이 비어 있거나 {@code null}을 가질 때.
		 * @see Viewport
		 */
		public Editor setViewports(Viewport... viewports) {
			getObject().setViewports(viewports);
			return this;
		}

		public Editor setViewports(List<Viewport> viewports) {
			getObject().setViewports(viewports);
			return this;
		}

		/**
		 * 시간 축의 흐름에 따른 출력 영역을 조정에 사용할 {@code TimeInterpolator}를 {@code InterpolatorType}으로써 설정합니다.
		 * 만약 인자로 {@code null}을 전달한다면 자동적으로 {@code InterpolatorType#LINEAR}가 사용됩니다.
		 * 
		 * @param interpolatorType
		 *            사용할 {@code TimeInterpolator}에 해당하는 {@code InterpolatorType}.
		 * @see InterpolatorType
		 * @see TimeInterpolator
		 */
		public Editor setInterpolator(InterpolatorType interpolatorType) {
			getObject().setInterpolator(interpolatorType);
			return this;
		}

		/**
		 * 시간 축의 흐름에 따른 출력 영역의 조정에 사용할 {@code TimeInterpolator}를 {@code InterpolatorType}의 목록으로써
		 * 설정합니다. 만약 인자로 전달된 {@code InterpolatorType}이 필요 이상으로 많은 경우에는 목록은 유지하되 사용하지는 않으며, 필요한 것보다
		 * 적은 경우에는 순환적으로, 즉 주어진 목록의 첫 번째 요소부터 필요한 만큼 재사용합니다.
		 * 
		 * @param interpolatorTypes
		 *            사용할 {@code TimeInterpolator}에 해당하는 {@code InterpolatorType}의 목록.
		 * @throws InvalidArrayException
		 *             주어진 목록이 비어 있거나 {@code null}을 가질 때.
		 * @see InterpolatorType
		 * @see TimeInterpolator
		 */
		public Editor setInterpolators(InterpolatorType... interpolatorTypes) {
			getObject().setInterpolators(interpolatorTypes);
			return this;
		}

		/**
		 * 시간 축의 흐름에 따른 출력 영역의 조정에 사용할 {@code TimeInterpolator}를 {@code InterpolatorType}의 목록으로써
		 * 설정합니다. 만약 인자로 전달된 {@code InterpolatorType}이 필요 이상으로 많은 경우에는 목록은 유지하되 사용하지는 않으며, 필요한 것보다
		 * 적은 경우에는 순환적으로, 즉 주어진 목록의 첫 번째 요소부터 필요한 만큼 재사용합니다.
		 * 
		 * @param interpolatorTypes
		 *            사용할 {@code TimeInterpolator}에 해당하는 {@code InterpolatorType}의 목록.
		 * @throws InvalidCollectionException
		 *             주어진 목록이 비어 있거나 {@code null}을 가질 때.
		 * @see InterpolatorType
		 * @see TimeInterpolator
		 */
		public Editor setInterpolators(List<InterpolatorType> interpolatorTypes) {
			getObject().setInterpolators(interpolatorTypes);
			return this;
		}

		/**
		 * 각 출력 영역 간의 전환에 걸리는 시간 길이, 다시 말해 각각의 전환 경로가 가지는 시간 길이를 산정하기 위한 가중치를 설정합니다. 기본적으로는 모든 출력 영역
		 * 전환에 동등한 시간 길이를 배분하게 되는데, 이때 각 전환 경로가 가지는 가중치는 {@code 1}입니다.<br />
		 * <br />
		 * 예컨대, 출력할 영역 정보로 3개를 설정했다면 전환 경로는 2개이며, 가중치를 각각 {@code 2}, {@code 1}로 설정했을 때 각 전환 경로에서
		 * 점유하는 시간 길이는 총 시간 길이를 2:1의 비율로 나눈 값이 됩니다. 만약 인자로 전달된 가중치의 개수가 필요 이상으로 많은 경우에는 목록은 유지하되
		 * 사용하지는 않으며, 필요한 것보다 적은 경우에는 {@code 1}의 가중치를 자동적으로 추가하여 적용합니다.<br />
		 * <br />
		 * 만약 {@code null} 혹은 빈 목록이 인자로 주어진다면 모든 전환 경로의 가중치는 {@code 1}로 초기화됩니다.
		 * 
		 * @param weights
		 *            적용할 가중치 목록.
		 * @throws InvalidNumberException
		 *             주어진 목록에 양의 정수가 아닌 숫자가 포함되어 있을 때.
		 */
		public Editor setWeights(int... weights) {
			getObject().setWeights(weights);
			return this;
		}

		/**
		 * 각 출력 영역 간의 전환에 걸리는 시간 길이, 다시 말해 각각의 전환 경로가 가지는 시간 길이를 산정하기 위한 가중치를 설정합니다. 기본적으로는 모든 출력 영역
		 * 전환에 동등한 시간 길이를 배분하게 되는데, 이때 각 전환 경로가 가지는 가중치는 {@code 1}입니다.<br />
		 * <br />
		 * 예컨대, 출력할 영역 정보로 3개를 설정했다면 전환 경로는 2개이며, 가중치를 각각 {@code 2}, {@code 1}로 설정했을 때 각 전환 경로에서
		 * 점유하는 시간 길이는 총 시간 길이를 2:1의 비율로 나눈 값이 됩니다. 만약 인자로 전달된 가중치의 개수가 필요 이상으로 많은 경우에는 목록은 유지하되
		 * 사용하지는 않으며, 필요한 것보다 적은 경우에는 {@code 1}의 가중치를 자동적으로 추가하여 적용합니다.<br />
		 * <br />
		 * 만약 {@code null} 혹은 빈 목록이 인자로 주어진다면 모든 전환 경로의 가중치는 {@code 1}로 초기화됩니다.<br />
		 * <br />
		 * 만약 주어진 목록에 {@code null} 요소가 포함되어 있다면 이 요소는 {@code 1}로 변환됩니다.
		 * 
		 * 
		 * @param weights
		 *            적용할 가중치 목록.
		 * @throws InvalidNumberException
		 *             주어진 목록에 양의 정수가 아닌 숫자가 포함되어 있을 때.
		 */
		public Editor setWeights(List<Integer> weights) {
			getObject().setWeights(weights);
			return this;
		}
	}
}
