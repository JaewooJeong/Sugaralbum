package com.kiwiple.scheduler.data.uplus.transition;

import com.kiwiple.scheduler.data.TransitionData;
import com.kiwiple.scheduler.theme.Theme;

/**
 * cover transition 데이터 클래스.
 * 
 */
public class UplusGrandUnionTransitionData extends TransitionData {

	float blockInterval;
	int lineWidth;
	int lineColor;
	boolean useFadeIn;
	
	int duration;

	/**
	 * 생성자
	 */
	public UplusGrandUnionTransitionData() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 생성자
	 * 
	 * @param blockInterval
	 * @param lineWidth
	 * @param lineColor
	 * @param useFadeIn
	 */
	public UplusGrandUnionTransitionData(float blockInterval, int lineWidth, int lineColor, boolean useFadeIn) {
		super();
		this.blockInterval = blockInterval;
		this.lineWidth = lineWidth;
		this.lineColor = lineColor;
		this.useFadeIn = useFadeIn;
	}

	public UplusGrandUnionTransitionData(int duration, String type) {
		super(duration, type);
		this.duration = duration;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public float getBlockInterval() {
		return blockInterval;
	}

	public void setBlockInterval(float blockInterval) {
		this.blockInterval = blockInterval;
	}

	public int getLineWidth() {
		return lineWidth;
	}

	public void setLineWidth(int lineWidth) {
		this.lineWidth = lineWidth;
	}

	public int getLineColor() {
		return lineColor;
	}

	public void setLineColor(int lineColor) {
		this.lineColor = lineColor;
	}

	public boolean isUseFadeIn() {
		return useFadeIn;
	}

	public void setUseFadeIn(boolean useFadeIn) {
		this.useFadeIn = useFadeIn;
	}

}
