
package com.kiwiple.imageframework.util;

import android.util.Log;

import com.kiwiple.imageframework.Constants;

/**
 * 로그 출력 클래스
 */
public class SmartLog {
    private SmartLog() {
    }

    /**
     * Verbose 로그 출력
     * 
     * @param tag 태그
     * @param msg 출력 내용
     */
    public static void v(String tag, String msg) {
        if(!Constants.RELEASE_BUILD) {
            Log.v("[" + tag + "]", msg);
        }
    }

    /**
     * Debug 로그 출력
     * 
     * @param tag 태그
     * @param msg 출력 내용
     */
    public static void d(String tag, String msg) {
        if(!Constants.RELEASE_BUILD) {
            Log.d("[" + tag + "]", msg);
        }
    }

    /**
     * Infomation 로그 출력
     * 
     * @param tag 태그
     * @param msg 출력 내용
     */
    public static void i(String tag, String msg) {
        if(!Constants.RELEASE_BUILD) {
            Log.i("[" + tag + "]", msg);
        }
    }

    /**
     * Warnings 로그 출력
     * 
     * @param tag 태그
     * @param msg 출력 내용
     */
    public static void w(String tag, String msg) {
        if(!Constants.RELEASE_BUILD) {
            Log.w("[" + tag + "]", msg);
        }
    }

    /**
     * Error 로그 출력
     * 
     * @param tag 태그
     * @param msg 출력 내용
     */
    public static void e(String tag, String msg) {
        if(!Constants.RELEASE_BUILD) {
            Log.e("[" + tag + "]", msg);
        }
    }

    /**
     * Error 로그 출력
     * 
     * @param tag 태그
     * @param msg 출력 내용
     * @param e Throwable
     */
    public static void e(String tag, String msg, Throwable e) {
        if(!Constants.RELEASE_BUILD) {
            Log.e("[" + tag + "]", msg, e);
        }
    }

    /**
     * Fatal 로그 출력
     * 
     * @param tag 태그
     * @param msg 출력 내용
     */
    public static void f(String tag, String msg) {
        if(!Constants.RELEASE_BUILD) {
            Log.e("[FATAL][" + tag + "]", msg);
        }
    }
}
