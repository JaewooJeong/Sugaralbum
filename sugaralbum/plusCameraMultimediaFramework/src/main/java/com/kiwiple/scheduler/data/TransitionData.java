package com.kiwiple.scheduler.data;

/**
 * 트렌지션 데이터 클래스.
 *
 */
public class TransitionData {
	int duration; 
	String type;
	
	/**
	 * 생성자.
	 */
	public TransitionData() {
		super();
		
	}
	/**
	 * 생성자. 
	 * @param duration : 트렌지션 길이.  
	 * @param type  : 트렌지션 타입. 
	 */
	public TransitionData(int duration, String type) {
		super();
		this.duration = duration;
		this.type = type;
	}
	/**
	 * 트렌지션 길이를 반환한다. 
	 * @return  트렌지션 길이. 
	 */
	public int getDuration() {
		return duration;
	}
	/**
	 * 트렌지션 길이를 설정한다. 
	 * @param duration 트렌지션 길이.
	 */
	public void setDuration(int duration) {
		this.duration = duration;
	}
	/**
	 * 트렌지션 타입을 반환한다.  
	 * @return 트렌지션 타입. 
	 */
	public String getType() {
		return type;
	}
	/**
	 * 트렌지션 타입을 설정한다. 
	 * @param type : 트렌지션 타입. 
	 */
	public void setType(String type) {
		this.type = type;
	} 
	
}
