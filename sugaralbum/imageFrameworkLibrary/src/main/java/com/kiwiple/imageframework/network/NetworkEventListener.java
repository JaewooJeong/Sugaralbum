
package com.kiwiple.imageframework.network;

/**
 * 서버 API 호출 결과를 전달 받기 위한 콜백함수
 * 
 * @version 1.0
 */
public interface NetworkEventListener {
    /**
     * 서버 API 호출 결과
     * 
     * @param state 서버 API 호출 성공/실패에 대한 정보
     * @param error 서버 API 호출 실패시 실패 원인에 대한 정보
     * @version 1.0
     */
    public void onNetworkEvent(String state, int error);
}
