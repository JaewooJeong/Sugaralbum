package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import android.graphics.Bitmap;

import com.kiwiple.debug.Precondition;
import com.kiwiple.imageframework.filter.FilterManager;
import com.kiwiple.imageframework.filter.live.LiveFilterController;
import com.kiwiple.multimedia.Constants;
import com.kiwiple.multimedia.json.IJsonConvertible;
import com.kiwiple.multimedia.util.Size;

/**
 * {@link Bitmap}에 필터 효과를 적용하기 위한 편의성 클래스.
 * <p />
 * 실질적인 기능 구현은 {@link LiveFilterController} 및 {@link FilterManager}에 종속되어 있습니다.
 */
final class FilterApplier implements ICacheCode, IJsonConvertible {

	// // // // // Static variable.
	// // // // //
	/**
	 * 정상적으로 필터 효과를 사용할 수 없는 상태를 의도적으로 나타내기 위한 필터 식별자.
	 */
	static final int INVALID_FILTER_ID = Constants.INVALID_FILTER_ID;

	/**
	 * 아무런 동작도 취하지 않는 객체.
	 */
	static final FilterApplier INVALID_OBJECT = new FilterApplier();

	// // // // // Member variable.
	// // // // //
	private final LiveFilterController mLiveFilterController;
	private final FilterManager mFilterManager;

	private int mFilterId = INVALID_FILTER_ID;

	// // // // // Static method.
	// // // // //
	/**
	 * 주어진 필터의 식별자가 유효한 경우, 새로운 {@code FilterApplier}를 생성하여 반환합니다. 유효하지 않은 경우에는
	 * {@link #INVALID_OBJECT}를 반환합니다.
	 * 
	 * @param liveFilterController
	 *            LiveFilterController 객체.
	 * @param filterManager
	 *            FilterManager 객체.
	 * @param filterId
	 *            {@value #INVALID_FILTER_ID} 혹은 1 이상의 값을 가지는 필터의 식별자.
	 */
	static FilterApplier create(LiveFilterController liveFilterController, FilterManager filterManager, int filterId) {

		if (filterId == INVALID_FILTER_ID || filterId <= 0) {
			return INVALID_OBJECT;
		}
		return new FilterApplier(liveFilterController, filterManager, filterId);
	}

	// // // // // Constructor.
	// // // // //
	/**
	 * {@link #INVALID_OBJECT}를 생성하기 위한 내부 생성자.
	 */
	private FilterApplier() {

		mLiveFilterController = null;
		mFilterManager = null;
		mFilterId = INVALID_FILTER_ID;
	}

	/**
	 * {@link LiveFilterController}와 {@link FilterManager}, 필터의 식별자를 통해 {@code FilterApplier}를
	 * 생성합니다.
	 * 
	 * @param liveFilterController
	 *            필터 효과 적용에 사용할 LiveFilterController 객체.
	 * @param filterManager
	 *            필터 효과 적용에 사용할 FilterManager 객체.
	 * @param filterId
	 *            {@value #INVALID_FILTER_ID} 혹은 1 이상의 값을 가지는 필터의 식별자.
	 */
	private FilterApplier(LiveFilterController liveFilterController, FilterManager filterManager, int filterId) {
		Precondition.checkNotNull(liveFilterController, filterManager);

		mLiveFilterController = liveFilterController;
		mFilterManager = filterManager;
		mFilterId = filterId;
	}

	// // // // // Method.
	// // // // //
	/**
	 * 적용할 필터의 식별자를 반환합니다.
	 */
	int getFilterId() {
		return mFilterId;
	}

	/**
	 * 적용할 필터의 식별자가 유효한지의 여부를 반환합니다.
	 * 
	 * @return 유효한 식별자일 때 {@code true}.
	 */
	boolean isVaild() {
		return mFilterId != INVALID_FILTER_ID;
	}

	/**
	 * {@link Bitmap}에 필터 효과를 적용합니다. 만약 필터 식별자가 유효하지 않은 값으로 설정되어 있다면, 아무런 동작도 취하지 않습니다.
	 * 
	 * @param bitmap
	 *            필터 효과를 적용할 {@code Bitmap} 객체.
	 */
	void apply(Bitmap bitmap) {
		Precondition.checkNotNull(bitmap);

		if (!isVaild()) {
			return;
		}
		mLiveFilterController.applyFilter(mFilterManager, mFilterId, bitmap, bitmap);
	}

	/**
	 * {@code int} 배열에 담긴 이미지 데이터에 필터 효과를 적용합니다. 만약 필터 식별자가 유효하지 않은 값으로 설정되어 있다면, 아무런 동작도 취하지 않습니다.
	 * 
	 * @param srcPixels
	 *            이미지의 픽셀 데이터가 담긴 {@code int} 배열.
	 * @param dstPixels
	 *            효과가 적용된 픽셀 데이터를 담을 {@code int} 배열.
	 * @param width
	 *            이미지의 가로 크기.
	 * @param height
	 *            이미지의 세로 크기.
	 */
	void apply(int[] srcPixels, int[] dstPixels, int width, int height) {
		Precondition.checkNotNull(srcPixels, dstPixels);
		Precondition.checkArgument(new Size(width, height) != null, "size must be created.");

		if (!isVaild()) {
			return;
		}
		mLiveFilterController.applyFilter(mFilterManager, mFilterId, width, height, srcPixels, dstPixels);
	}

	@Override
	public Integer toJsonObject() throws JSONException {
		return isVaild() ? mFilterId : null;
	}

	@Override
	public int createCacheCode() {
		return mFilterId;
	}
}