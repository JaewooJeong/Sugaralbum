package com.kiwiple.scheduler.data.uplus.effect;

import com.kiwiple.multimedia.canvas.LightEffect;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.scheduler.data.EffectData;

/**
 * 유플러스 lighting effect 데이터 클래스. 
 *
 */
public class UplusLightEffectData extends EffectData {
	
	public static final String JSON_VALUE_LIGHT_EFFECT_ARRAY = "light_effect_array";
	private String type;
	private String color;
	private int scale;
	private int startX;
	private int startY;
	private int endX;
	private int endY;
	private int startAlpha = 0;
	private int endalpha = 50;
	private Resolution baseResolution = Resolution.NHD;

	
	public UplusLightEffectData(String type, String color, int scale, int startX, int startY, int endX, int endY, int startAlpha, int endAlpha, String resolution) {
		super(LightEffect.JSON_VALUE_TYPE);
		this.type = type;
		this.color = color;
		this.scale = scale;
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		this.startAlpha = startAlpha;
		this.endalpha = endAlpha;
		this.baseResolution = findResoltuion(resolution);
	}
	
	public Resolution findResoltuion(String name) {
		if(Resolution.NHD.name.equals(name)) return Resolution.NHD;
		if(Resolution.HD.name.equals(name)) return Resolution.HD;
		if(Resolution.FHD.name.equals(name)) return Resolution.FHD;
		return null;
	}
	
	public Resolution getResolution(){
		return baseResolution;
	}

	/**
	 * lighting effect type 반환
	 * @return : type 반환. 
	 */
	public String getType() {
		return type;
	}

	/**
	 * lighting effect color 반환
	 * @return : color 반환. 
	 */
	public String getColor() {
		return color;
	}

	/**
	 * lighting effect scale 반환
	 * @return : scale 반환. 
	 */
	public int getScale() {
		return scale;
	}

	/**
	 * lighting effect start x 반환
	 * @return : start x 반환. 
	 */
	public int getStartX() {
		return startX;
	}

	/**
	 * lighting effect start y 반환
	 * @return : start y 반환. 
	 */
	public int getStartY() {
		return startY;
	}

	/**
	 * lighting effect end x 반환
	 * @return : end x 반환. 
	 */
	public int getEndX() {
		return endX;
	}

	/**
	 * lighting effect end y 반환
	 * @return : type 반환. 
	 */
	public int getEndY() {
		return endY;
	}
	
	/**
	 * lighting effect end x 반환
	 * @return : end x 반환. 
	 */
	public int getStartAlpha() {
		return startAlpha;
	}

	/**
	 * lighting effect end y 반환
	 * @return : type 반환. 
	 */
	public int getEndAlpha() {
		return endalpha;
	}
}
