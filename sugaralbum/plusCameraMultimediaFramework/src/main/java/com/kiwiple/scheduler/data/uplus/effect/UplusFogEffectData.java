package com.kiwiple.scheduler.data.uplus.effect;

import com.kiwiple.multimedia.canvas.FogEffect;
import com.kiwiple.scheduler.data.EffectData;

/**
 * Fog effect 데이터 클래스.
 *
 */
public class UplusFogEffectData extends EffectData {
	
	private String type;
	
	/**
	 * 생성자.
	 * @param effectType 포그 effect 타입. 
	 */
	public UplusFogEffectData(String effectType) {
		super(FogEffect.JSON_VALUE_TYPE);
		type = effectType; 
		// TODO Auto-generated constructor stub
	}

	/**
	 * fog effect 타입을 반환한다. 
	 * @return 포그 effect 타입.
	 */
	public String getType() {
		return type;
	}

}
