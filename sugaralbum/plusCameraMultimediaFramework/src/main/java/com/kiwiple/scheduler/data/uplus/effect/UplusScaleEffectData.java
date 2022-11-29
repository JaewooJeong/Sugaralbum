package com.kiwiple.scheduler.data.uplus.effect;

import com.kiwiple.scheduler.data.EffectData;

public class UplusScaleEffectData extends EffectData {
	
	public static final String JSON_NAME_MAX_COUNT = "max_count";  
	int mMaxCount; 

	public UplusScaleEffectData(String effectType) {
		super(effectType);
		this.mMaxCount = 0; 
		// TODO Auto-generated constructor stub
	}
	
	public void setMaxCount(int count){
		this.mMaxCount = count; 
	}
	
	public int getMaxCount(){
		return mMaxCount;  
	}
}
