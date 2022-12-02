package com.kiwiple.scheduler.data.uplus.transition;

import com.kiwiple.multimedia.json.JsonUtils;
import com.kiwiple.scheduler.data.TransitionData;

/**
 * ExtendBox transition 데이터 클래스.
 * 
 */
public class UplusExtendBoxTransitionData extends TransitionData {

	public static final String JSON_NAME_STYLE = "style";
	
	int boxColor;
	float interval;
	float tickness;
	boolean useFadeIn;
	String style;
	int duration;

	/**
	 * 생성자
	 * 
	 * @param useFadeIn
	 * @param tickness
	 * @param interval
	 * @param boxColor
	 * @param duration
	 * @param viewport
	 */
	public UplusExtendBoxTransitionData(int boxColor, float interval, float tickness, boolean useFadeIn, int duration, String style) {
		this.boxColor = boxColor;
		this.interval = interval;
		this.tickness = tickness;
		this.useFadeIn = useFadeIn;
		this.duration = duration;
		this.style = style;
	}

	public int getDuration() {
		return duration;
	}

	public void setDuration(int duration) {
		this.duration = duration;
	}

	public Style getStyle() {
		return JsonUtils.getEnumByJsonString(style, Style.class);
	}

	public void setStyle(String style) {
		this.style = style;
	}

	public int getBoxColor() {
		return boxColor;
	}

	public void setBoxColor(int boxColor) {
		this.boxColor = boxColor;
	}

	public float getInterval() {
		return interval;
	}

	public void setInterval(float interval) {
		this.interval = interval;
	}

	public float getTickness() {
		return tickness;
	}

	public void setTickness(float tickness) {
		this.tickness = tickness;
	}

	public boolean getUseFadeIn() {
		return useFadeIn;
	}

	public void setUseFadeIn(boolean useFadeIn) {
		this.useFadeIn = useFadeIn;
	}

	public static enum Style {

		FULL_BOX(0),
		VERTICAL_TWO_BOXES(1),
		HORIZONTAL_TWO_BOXES(2),
		FOUR_BOXES(3);
	
		final int style;

		private Style(int style) {
			this.style = style;
		}
	}
}
