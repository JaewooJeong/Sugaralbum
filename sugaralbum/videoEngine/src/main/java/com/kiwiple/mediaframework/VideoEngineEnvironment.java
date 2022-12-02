package com.kiwiple.mediaframework;

public final class VideoEngineEnvironment {

	public static final boolean IS_DEV = true;

	public static final String EMPTY_STRING = "";

	public static final int INVALID_INDEX = -1;
	public static final int INVALID_INTEGER_VALUE = -1;
	public static final long INVALID_LONG_VALUE = -1L;

	public static final long DEFAULT_THREAD_SLEEP_TIME = 2L;

	public static final long DEFAULT_UPDATE_INTERVAL_MILLIS = 33L;
	public static final long DEFAULT_UPDATE_INTERVAL_MICROS = 33000L;
	
	/*
	 * jhshin
	 * 단말별로 디코딩 시 버퍼 크기 조절을 위함 >> 녹색줄 문제 
	 */
	public static final String G_FLEX2 = "F510";
	public static final String G_500L = "F500";

	private VideoEngineEnvironment() {
		// Hide constructor and do nothing.
	}
}