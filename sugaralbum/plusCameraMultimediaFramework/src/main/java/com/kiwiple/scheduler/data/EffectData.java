package com.kiwiple.scheduler.data;
/**
 * effect 데이터 클래스 
 */
public abstract class EffectData {
	
	public static final String JSON_NAME_EFFECT_MULTI_SCENE = "MultiLayer";
	public static final String JSON_NAME_MAX_COUNT = "max_count"; 
	public static final String JSON_NAME_EXTRA_EFFECT = "extra_effects"; 
	public static final String JSON_NAME_APPLY_SCENE = "apply_scene"; 
	public static final String JSON_NAME_ACTIVE_START_RATIO = "active_start_ratio"; 
	public static final String JSON_NAME_ACTIVE_END_RATIO = "active_end_ratio";
	public static final String JSON_NAME_DRAW_ONLY_ACTIVE_RATIO = "draw_only_active_ratio";
	
	protected String effectType;
	protected int maxCount;
	protected String applySceneType; 
	protected float activeStartRatio; 
	protected float activeEndRatio; 
	protected boolean drawOnlyActvieRatio; 

	/**
	 * UplusEffectData 생성자.   
	 * @param effectType : effect type. 
	 */
	public EffectData(String effectType) {
		super();
		this.effectType = effectType;
	}

	/**
	 * effect type 반환.
	 * @return effect type. 
	 */
	public String getEffectType() {
		return effectType;
	}

	/**
	 * effect type 설정. 
	 * @param effectType : effect type. 
 	 */
	public void setEffectType(String effectType) {
		this.effectType = effectType;
	}  
	
	public int getMaxCount(){
		return maxCount; 
	}
	
	public void setMaxCount(int maxCount){
		this.maxCount = maxCount;
	}
	
	public void setApplySceneType(String applySceneType){
		this.applySceneType = applySceneType; 
	}
	
	public String getApplySceneType(){
		return applySceneType;  
	}

	public float getActiveStartRatio() {
		return activeStartRatio;
	}

	public void setActiveStartRatio(float activeStartRatio) {
		this.activeStartRatio = activeStartRatio;
	}

	public float getActiveEndRatio() {
		return activeEndRatio;
	}

	public void setActiveEndRatio(float activeEndRatio) {
		this.activeEndRatio = activeEndRatio;
	}

	public boolean isDrawOnlyActvieRatio() {
		return drawOnlyActvieRatio;
	}

	public void setDrawOnlyActvieRatio(boolean drawOnlyActvieRatio) {
		this.drawOnlyActvieRatio = drawOnlyActvieRatio;
	}
}
