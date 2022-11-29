package com.kiwiple.scheduler.data.uplus.transition;

import java.util.ArrayList;

import com.kiwiple.multimedia.canvas.SpinTransition.Direction;
import com.kiwiple.multimedia.canvas.Transition.SceneOrder;
import com.kiwiple.multimedia.json.JsonUtils;
import com.kiwiple.multimedia.math.interpolator.InterpolatorType;
import com.kiwiple.scheduler.data.TransitionData;

/**
 * spin transition 데이터 클래스. 
 *
 */
public class UplusSpinTransitionData extends TransitionData {
	ArrayList<String> sceneOrderList; 
	String direction; 
	boolean overshoot; 
	boolean blurredBorder;
	String interploator; 

	/**
	 * 생성자. 
	 * @param duration 길이.
	 * @param type 타입.
	 * @param spinType 스핀타입.
	 * @param direction 방향.
	 */
	public UplusSpinTransitionData(int duration, String type, ArrayList<String> sceneOrderList, boolean overshoot, String direction, boolean blurredBorder, String interpolator) {
		super(duration, type);
		this.sceneOrderList = sceneOrderList;
		this.overshoot = overshoot; 
		this.direction = direction; 
		this.blurredBorder = blurredBorder; 
		this.interploator = interpolator; 
	}

	/**
	 * Scene order를 반환. 
	 * @return scene order.
	 */
	public SceneOrder[] getSceneOrderArray() {
		int sceneOrderNum = sceneOrderList.size(); 
		SceneOrder [] orderArray = new SceneOrder[sceneOrderNum]; 
		for(int i = 0; i < sceneOrderNum; i++){
			orderArray[i] = JsonUtils.getEnumByJsonString(sceneOrderList.get(i), SceneOrder.class); 
		}
		return orderArray;
	}

	/**
	 * 스핀 타입 설정.
	 * @param spinType 스핀타입.
	 */
	public void setSpinType(ArrayList<String> sceneOrderList) {
		this.sceneOrderList = sceneOrderList;
	}

	/**
	 * direction 반환. 
	 * @return direction
	 */
	public Direction getDirection() {
		return JsonUtils.getEnumByJsonString(direction, Direction.class);
	}

	/**
	 * direction 설정.
	 * @param direction : 방향. 
	 */
	public void setDirection(String direction) {
		
		this.direction = direction;
	}
	
	public InterpolatorType getInterpolator(){
		return JsonUtils.getEnumByJsonString(interploator, InterpolatorType.class); 
	}

	public boolean isOvershoot() {
		return overshoot;
	}

	public void setOvershoot(boolean overshoot) {
		this.overshoot = overshoot;
	}
	
	public boolean isBlurredBorder(){
		return blurredBorder; 
	}
}
