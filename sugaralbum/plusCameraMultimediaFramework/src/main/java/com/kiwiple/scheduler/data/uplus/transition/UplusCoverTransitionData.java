package com.kiwiple.scheduler.data.uplus.transition;

import android.content.Context;

import com.kiwiple.multimedia.canvas.CoverTransition.Direction;
import com.kiwiple.multimedia.json.JsonUtils;
import com.kiwiple.scheduler.data.TransitionData;
import com.kiwiple.scheduler.theme.Theme;

/**
 * cover transition 데이터 클래스. 
 *
 */
public class UplusCoverTransitionData extends TransitionData {
	String coverDirection; 
	Theme theme;
	Context context;

	/**
	 * 생성자
	 */
	public UplusCoverTransitionData() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 생성자
	 * @param mContext 
	 * @param duration  트렌지션 길이.
	 * @param type 트렌지션 타입. 
	 * @param direction 트렌지션 방향. 
	 */
	public UplusCoverTransitionData(Context context, int duration, String type, String coverDirection, Theme theme) {
		super(duration, type);
		this.context = context;
		this.coverDirection = coverDirection; 
		this.theme =theme;
	}

	/**
	 * 트렌지션 방향을 반환한다. 
	 * @return : 트렌지션 방향. 
	 */
	public Direction getDirection() {
		return JsonUtils.getEnumByJsonString(coverDirection, Direction.class);
	}

	/**
	 * 트렌지션 방향을 설정한다. 
	 * @param direction : 트렌지션 방향. 
	 */
	public void setDirection(String coverDirection) {
		this.coverDirection = coverDirection;
	}
	
	public String getThemeName(){
		return theme.name;
	}
	
	public Theme getTheme(){
		return theme;
	}

	public Context getContext(){
		return context;
	}
	
}
