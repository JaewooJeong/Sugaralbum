package com.kiwiple.scheduler;

import com.kiwiple.scheduler.theme.ThemeVersion;

/**
 * 라이브러리의 일부 기능을 일괄적으로 제어하기 위한 환경 변수를 담고 있는 클래스.
 * 
 */
public final class SchedulerEnvironment {

	public static final int INVALIDATE_INDEX = -1;
	public static final int INVALIDATE_FRAME_ID = -1;
	public static final boolean IS_DEV;
	public static final boolean ENABLE_CHECK_BURSTSHOT;
	public static final boolean IS_TEST_THEME_URL;
	public static final boolean IS_HIDDEN_MENU_ENABLE;
	public static final SchedulerVersion VERSION;
	public static final SchedulerVersion VERSION_1_0_0;
	public static final SchedulerVersion VERSION_1_0_1;
	public static final SchedulerVersion VERSION_1_0_2;
	/**
	 * 기본 asset 음악이 free and lucky에서 Inspire the World로 변경. 
	 *	1. asset으로 설정된 음악 path 변경. 
	 *  2. 새로운 음악에 맞게 duration 변경. 
	 */
	public static final SchedulerVersion VERSION_1_0_3;
	public static final ThemeVersion THEME_VERSION_0_0_0;  
    public static final ThemeVersion THEME_VERSION_1_0_0;
    public static final ThemeVersion THEME_VERSION_1_0_1;
 	static {
 		IS_DEV = false; 
 		ENABLE_CHECK_BURSTSHOT = false; 
 		VERSION = new SchedulerVersion(1, 0, 3); 
 		VERSION_1_0_0 = new SchedulerVersion(1, 0, 0); 
 		VERSION_1_0_1 = new SchedulerVersion(1, 0, 1);
 		VERSION_1_0_2 = new SchedulerVersion(1, 0, 2);
 		VERSION_1_0_3 = new SchedulerVersion(1, 0, 3);
 		THEME_VERSION_0_0_0 = new ThemeVersion(0, 0, 0);
 		THEME_VERSION_1_0_0 = new ThemeVersion(1, 0, 0);
 		THEME_VERSION_1_0_1 = new ThemeVersion(1, 0, 1);
  		IS_TEST_THEME_URL = true;
 		IS_HIDDEN_MENU_ENABLE = true; 
 	}

	public SchedulerEnvironment() {
		// TODO Auto-generated constructor stub
	}

}
