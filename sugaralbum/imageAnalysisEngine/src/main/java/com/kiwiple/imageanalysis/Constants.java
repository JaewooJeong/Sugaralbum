
package com.kiwiple.imageanalysis;

public class Constants {
    /**
     * 빌드에 관한 설정을 한다.<br>
     * true의 경우 로그가 출력되지 않는다.
     */
    public static boolean RELEASE_BUILD = false;
    /**
     * 시작시 캐싱 파일을 지울 것인지 여부
     */
    public static boolean CACHE_REMOVE = true;
    /**
     * 계절별 스티커 추천 막고, 일반적으로 잘 어울릴 센티멘탈 스티커 추천
     */
    public static boolean DEMO_BUILD = true;

    private Constants() {
    }
}
