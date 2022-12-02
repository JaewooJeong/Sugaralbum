package com.kiwiple.scheduler.data.uplus.transition;

import com.kiwiple.scheduler.data.TransitionData;

/**
 * Metro transition 데이터 클래스. 
 *
 */
public class UplusMetroTransitionData extends TransitionData {
	int sliceCount; 
	String sliceOrder; 
	String direction;
	int lineColor; 
	
	/**
	 * 생성자. 
	 * @param duration 길이. 
	 * @param type 타입.
	 * @param sliceCount 슬라이스 갯수. 
	 * @param sliceOrder 슬라이스 순서. 
	 * @param direction 슬라이스 방향. 
	 */
	public UplusMetroTransitionData(int duration, String type, int sliceCount, String sliceOrder, String direction, int lineColor) {
		super(duration, type);
		
		this.sliceCount = sliceCount;
		this.sliceOrder = sliceOrder;
		this.direction = direction;
		this.lineColor = lineColor; 
	}
	
	/**
	 * 슬라이스 갯수 반환.  
	 * @return 슬라이스 갯수. 
	 */
	public int getSliceCount() {
		return sliceCount;
	}
	
	/**
	 * 슬라이스 순서 반환. 
	 * @return 슬라이스 순서. 
	 */
	public String getSliceOrder() {
		return sliceOrder;
	}

	/**
	 * 슬라이스 방향 반환. 
	 * @return 슬라이스 방향. 
	 */
	public String getDirection() {
		return direction;
	}

	/**
	 * 경계선 line 색 반환. 
	 * @return 경계선 line 색. 
	 */
	public int getLineColor() {
		return lineColor;
	}

}
