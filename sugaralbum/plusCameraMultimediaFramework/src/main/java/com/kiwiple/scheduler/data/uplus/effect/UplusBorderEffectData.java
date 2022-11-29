package com.kiwiple.scheduler.data.uplus.effect;

import com.kiwiple.multimedia.canvas.BorderEffect;
import com.kiwiple.scheduler.data.EffectData;

public class UplusBorderEffectData extends EffectData {
	
	private int color; 
	private int width; 

	public UplusBorderEffectData(String effectType, int color, int width) {
		super(BorderEffect.JSON_VALUE_TYPE);
		this.color = color; 
		this.width = width; 
	}

	public int getColor() {
		return color;
	}

	public int getWidth() {
		return width;
	}
}
