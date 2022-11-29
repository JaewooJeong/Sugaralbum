package com.kiwiple.scheduler.data.uplus.transition;

import com.kiwiple.multimedia.canvas.SplitTransition.Direction;
import com.kiwiple.multimedia.json.JsonUtils;
import com.kiwiple.scheduler.data.TransitionData;

/**
 * split transition 데이터 클래스. 
 *
 */
public class UplusSplitTransitionData extends TransitionData {
	String direction; 
	int line_color; 
	boolean isWhiteLineSplit;

	/**
	 * 생성자. 
	 * @param duration 길이.
	 * @param type 타입.
	 * @param direction 방향.
	 * @param line_color line color. 
	 */
	public UplusSplitTransitionData(int duration, String type, String direction, int line_color ) {
		super(duration, type);
		this.direction = direction; 
		this.line_color = line_color; 
		// TODO Auto-generated constructor stub
	}

	/**
	 * 생성자. 
	 * @param duration 길이.
	 * @param type 타입.
	 * @param direction 방향.
	 * @param line_color line color. 
	 */
	public UplusSplitTransitionData(int duration, String type, String direction, int line_color, boolean isWhiteLineSplit ) {
		super(duration, type);
		this.direction = direction; 
		this.line_color = line_color; 
		this.isWhiteLineSplit = isWhiteLineSplit;
		// TODO Auto-generated constructor stub
	}

	/**
	 * whiteLineSplit enable 상태 반환 
	 * @return 상태
	 */
	public boolean getWhiteLineSplit() {
		return isWhiteLineSplit;
	}

	/**
	 * whiteLineSplit enable 상태 설정
	 * @return 상태
	 */
	public void setWhiteLineSplit(boolean isWhiteLineSplit) {
		this.isWhiteLineSplit = isWhiteLineSplit;
	}

	/**
	 * 방향을 반환한다. 
	 * @return 방향
	 */
	public Direction getDirection() {
		return JsonUtils.getEnumByJsonString(direction, Direction.class);
	}

	/**
	 * 방향 설정. 
	 * @param direction 방향.
	 */
	public void setDirection(String direction) {
		this.direction = direction;
	}

	/**
	 * line color를 반환한다. 
	 * @return line color. 
	 */
	public int getLine_color() {
		return line_color;
	}

	/**
	 *  line color 설정. 
	 * @param line_color
	 */
	public void setLine_color(int line_color) {
		this.line_color = line_color;
	}
}
