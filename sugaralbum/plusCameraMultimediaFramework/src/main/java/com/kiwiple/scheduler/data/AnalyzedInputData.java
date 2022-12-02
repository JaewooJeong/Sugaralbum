package com.kiwiple.scheduler.data;

/**
 * 분석된 입력 데이터 클래스. 
 *
 */
public class AnalyzedInputData {
	/**
	 * 이미지의 id를 name으로 설정. 
	 */
	private String mName;
	private long mDate;

	public AnalyzedInputData() {
	}

	public AnalyzedInputData(String name, long date) {
		setName(name);
		setDate(date);
	}

	/**
	 * name (id)를 반환.
	 * @return : name(id)를 반환. 
	 */
	public String getName() {
		return mName;
	}

	/**
	 * name(id)를 설정. 
	 * @param name : id. 
	 */
	public void setName(String name) {
		this.mName = name;
	}

	/**
	 * 날짜 정보를 반환. 
	 * @return : 날짜 정보. 
	 */
	public long getDate() {
		return mDate;
	}

	/**
	 * 날짜 정보를 설정. 
	 * @param date : 날짜 정보. 
	 */
	public void setDate(long date) {
		this.mDate = date;
	}
}
