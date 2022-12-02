package com.kiwiple.scheduler.data;

import com.kiwiple.multimedia.canvas.data.Viewport;

/**
 * 멀티레이아웃 데이터 클래스.
 * 
 */
public abstract class MultiLayerData {

	// 20150226 olive : #10758 멀티 레이어의 종류가 늘어나거나, 형태가 변형될 경우
	// SelectOutputData.java의 isPreferPortraitFrame() 함수도 같이 업데이트 해야 함.

	/**
	 * ------------------<br>
	 * |"     "|"      "|<br>
	 * |"     "|"      "|<br>
	 * |"     "|"      "|<br>
	 * |"     "|"      "|<br>
	 * ------------------<br>
	 */
	public static final int MULTI_LAYER_COLUMN_TWO_PICTURES_ID = 32001;

	/**
	 * ------------------<br>
	 * |"   "|"   "|"   "|<br>
	 * |"   "|"   "|"   "|<br>
	 * |"   "|"   "|"   "|<br>
	 * |"   "|"   "|"   "|<br>
	 * ------------------<br>
	 */
	public static final int MULTI_LAYER_COLUMN_THREE_PICTURES_ID = 33001;

	/**
	 * ------------------<br>
	 * |"     "|"      "|<br>
	 * |"     "|"      "|<br>
	 * | |"-------|<br>
	 * |"     "|"      "|<br>
	 * |"     "|"      "|<br>
	 * ------------------<br>
	 */

	public static final int MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID = 33003;

	/**
	 * ------------------<br>
	 * |"     "|"      "|<br>
	 * |"     "|"      "|<br>
	 * -----------------|<br>
	 * |"     "|"      "|<br>
	 * |"     "|"      "|<br>
	 * ------------------<br>
	 */
	public static final int MULTI_LAYER_REGULAR_FOUR_PICTURES_ID = 34002;

	/**
	 * ------------------<br>
	 * |"  "|"         "|<br>
	 * |"  "|"         "|<br>
	 * -----------------|<br>
	 * |"       "|"    "|<br>
	 * |"       "|"    "|<br>
	 * ------------------<br>
	 */
	public static final int MULTI_LAYER_IRREGULAR_01_FOUR_PICTURES_ID = 34001;

	public static Viewport getScaleSourceViewport(int frameId) {
		float left = 0, top = 0, right = 0, bottom = 0;

		if (frameId == MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID) {
			left = 0.5f;
			top = 0.0f;
			right = 1.0f;
			bottom = 0.5f;
			return new Viewport(left, top, right, bottom);
		} else if (frameId == MULTI_LAYER_REGULAR_FOUR_PICTURES_ID) {
			left = 0.0f;
			top = 0.0f;
			right = 0.5f;
			bottom = 0.5f;
			return new Viewport(left, top, right, bottom);
		} else if (frameId == MULTI_LAYER_IRREGULAR_01_FOUR_PICTURES_ID) {
			left = 0.0f;
			top = 0.5f;
			right = 0.65f;
			bottom = 1.0f;
			return new Viewport(left, top, right, bottom);
		} else {
			return null;
		}
	}
	
	public static boolean canApplyScaleEffect(int frameId) {
		if (frameId == MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID || frameId == MULTI_LAYER_REGULAR_FOUR_PICTURES_ID || frameId == MULTI_LAYER_IRREGULAR_01_FOUR_PICTURES_ID) {
			return true; 
		} else {
			return false;
		}
	}
}
