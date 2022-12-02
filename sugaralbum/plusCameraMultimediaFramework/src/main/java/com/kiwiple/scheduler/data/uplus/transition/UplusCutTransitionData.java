package com.kiwiple.scheduler.data.uplus.transition;

import com.kiwiple.scheduler.data.TransitionData;

/**
 * 컷트렌지션 데이터 클래스.  
 *
 */
public class UplusCutTransitionData extends TransitionData {
	
	public static final String JSON_VALUE_CUT_TRANSITION_TYPE = "cut_transition"; 

	/**
	 * 생성자
	 */
	public UplusCutTransitionData() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 생성자
	 * @param duration  트렌지션 길이.
	 * @param type 트렌지션 타입. 
	 */
	public UplusCutTransitionData(int duration, String type) {
		super(duration, type);
		// TODO Auto-generated constructor stub
	}

}
