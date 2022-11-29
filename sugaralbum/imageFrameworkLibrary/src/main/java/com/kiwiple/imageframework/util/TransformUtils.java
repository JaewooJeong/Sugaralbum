
package com.kiwiple.imageframework.util;

/**
 * Transform 관련 계산 유틸 클래스
 */
public class TransformUtils {

    /**
     * 회전 후 x값 계산
     * 
     * @param x 기존 x좌표
     * @param y 기존 y좌표
     * @param degree 회전 값
     * @return 회전 후 x값
     */
    public static float rotateX(float x, float y, float degree) {
        return (float)(x * Math.cos(Math.toRadians(degree)) - y * Math.sin(Math.toRadians(degree)));
    }

    /**
     * 회전 후 y값 계산
     * 
     * @param x 기존 x좌표
     * @param y 기존 y좌표
     * @param degree 회전 값
     * @return 회전 후 y값
     */
    public static float rotateY(float x, float y, float degree) {
        return (float)(x * Math.sin(Math.toRadians(degree)) + y * Math.cos(Math.toRadians(degree)));
    }

    /**
     * 삼각형의 대각선 길이를 반환
     * 
     * @param width 가로 길이
     * @param height 세로 길이
     * @return 대각선 길이 반환
     */
    public static float getDiameter(float width, float height) {
        return (float)Math.sqrt(Math.pow(width, 2) + Math.pow(height, 2));
    }
}
