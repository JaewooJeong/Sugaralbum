package com.kiwiple.mediaframework;

/**
 * Contents 의 type 이나 Track의 Type에대한 정의
 */
public enum MediaType {
	/** Track Type is Audio  */
	Audio(0), 

	/** Track Type is Video */
	Video(1), 
	
	/**  Track Type is Image */
	Image(2),

	/**  Track Type is Subtitle */
	SubTitle(3), 

	/** Unknow Type */
	Unknown(4);

	String str = "";
	int key;

	/**
	 * 생성자
	 * @param _key contents 타입에 대한 번호
	 */	
	private MediaType(int _key) {
		key = _key;
	}
	
	/**
	 * contents 의 번호를 반환
	 * @return content의 key 
	 */		
	public int	getKey()
	{
		return key;
	}

	/**
	 * contents 의 MediaType을 String으로 반환
	 * @return content의 이름 
	 */			
	public String getToString() {
		switch (key) {
		case 0:
			return "Audio Track";
		case 1:
			return "Video Track";
		case 2:
			return "Image Data";
		case 3:
			return "Sub Title Track";
		default:
			return "Unknown";
		}
	}
}
