package com.kiwiple.scheduler.data.uplus.effect;

import com.kiwiple.multimedia.canvas.StepAppearEffect;
import com.kiwiple.scheduler.data.EffectData;


public class UplusStepAppearEffectData extends EffectData {
	
	public static final String JSON_NAME_DEFAULT_RATIO = "appear_default_ratio"; 
	public static final String JSON_NAME_STEP_RATIO = "appear_step_ratio"; 
	
	public static final String JSON_NAME_APPEAR_ORDER_1 = "appear_order_1";
	public static final String JSON_NAME_APPEAR_ORDER_2 = "appear_order_2";
	public static final String JSON_NAME_APPEAR_ORDER_3 = "appear_order_3";
	public static final String JSON_NAME_APPEAR_ORDER_4 = "appear_order_4";

	int stepAppearOrder[] = new int[4];
	float defaultRatio; 
	float stepRatio; 
	
	public UplusStepAppearEffectData(String effectType, int []stepAppearOrder, float defaultRatio, float stepRatio) {
		super(StepAppearEffect.JSON_VALUE_TYPE);
		this.stepAppearOrder = stepAppearOrder; 
		this.defaultRatio = defaultRatio; 
		this.stepRatio = stepRatio; 
	}
	
	public int[] getStepAppearOrder(){
		return stepAppearOrder; 
	}

	public float getDefaultRatio() {
		return defaultRatio;
	}

	public float getStepRatio() {
		return stepRatio;
	}
}
