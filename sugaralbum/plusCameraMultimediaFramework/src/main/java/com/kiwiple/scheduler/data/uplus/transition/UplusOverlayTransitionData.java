package com.kiwiple.scheduler.data.uplus.transition;

import com.kiwiple.scheduler.data.TransitionData;
/**
 * overlay transition data class
 *
 */
public class UplusOverlayTransitionData extends TransitionData {

	public String order;

	/**
	 * 생성자. 
	 * @param duration transition 길이. 
	 * @param type transition 타입. 
	 * @param order overlay transition 순서. 
	 */
	public UplusOverlayTransitionData(int duration, String type, String order) {
		super(duration, type);
		this.order = order;
	}
	
	/**
	 * overlay transition의 order를 반환한다. 
	 * @return order
	 */
	public String getOrder(){
		return order; 
	}
}
