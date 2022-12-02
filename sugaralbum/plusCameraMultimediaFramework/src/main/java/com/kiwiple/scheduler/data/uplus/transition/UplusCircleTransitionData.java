package com.kiwiple.scheduler.data.uplus.transition;

import com.kiwiple.scheduler.data.TransitionData;
/**
 * circle transition data class
 *
 */
public class UplusCircleTransitionData extends TransitionData {

	private boolean mZoomin;

	/**
	 * 생성자. 
	 * @param duration transition 길이. 
	 * @param type transition 타입. 
	 * @param zoomin circle transition 방 
	 */
	public UplusCircleTransitionData(int duration, String type, boolean zoomin) {
		super(duration, type);
		this.mZoomin = zoomin;
	}
	
	/**
	 * circle transition의 zoomin 상태를 반환한. 
	 * @return mZoomin
	 */
	public boolean isZoomIn(){
		return mZoomin; 
	}
}
