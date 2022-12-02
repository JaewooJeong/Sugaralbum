package com.kiwiple.multimedia;

/**
 * 멀티미디어 자원의 형태를 구분하기 위한 열거형.
 * 
 * @see #FILE
 * @see #ANDROID_ASSET
 * @see #ANDROID_RESOURCE
 */
public enum ResourceType {

	/**
	 * 일반적인 파일 시스템의 파일 경로로써 접근할 수 있는 멀티미디어 자원.
	 */
	FILE,

	/**
	 * 안드로이드 응용 프로그램 패키지의 assets 디렉토리에 소속되어 있는 멀티미디어 자원.
	 */
	ANDROID_ASSET,

	/**
	 * 안드로이드 응용 프로그램 패키지의 res 디렉토리에 소속되어 있는 멀티미디어 자원.
	 */
	ANDROID_RESOURCE;

	public static final String DEFAULT_JSON_NAME = "resource_type";
}