
package com.kiwiple.imageanalysis.utils;

import android.util.Log;

import com.kiwiple.imageanalysis.Constants;

/**
 * 로그 출력 클래스
 */
public class SmartLog {
    /**
     * verbose 로그 출력
     *
     * @param tag
     *         태그
     * @param msg
     *         메세지
     */
    public static void v(String tag, String msg) {
        if (!Constants.RELEASE_BUILD && tag != null && msg != null) {
            Log.v("[" + tag + "]", msg);
        }
    }

    /**
     * debug 로그 출력
     *
     * @param tag
     *         태그
     * @param msg
     *         메세지
     */
    public static void d(String tag, String msg) {
        if (!Constants.RELEASE_BUILD && tag != null && msg != null) {
            Log.d("[" + tag + "]", msg);
        }
    }

    /**
     * verbose 로그 출력
     *
     * @param tag
     *         태그
     * @param msg
     *         메세지
     */
    public static void i(String tag, String msg) {
        if (!Constants.RELEASE_BUILD && tag != null && msg != null) {
            Log.i("[" + tag + "]", msg);
        }
    }

    /**
     * warning 로그 출력
     *
     * @param tag
     *         태그
     * @param msg
     *         메세지
     */
    public static void w(String tag, String msg) {
        if (!Constants.RELEASE_BUILD && tag != null && msg != null) {
            Log.w("[" + tag + "]", msg);
        }
    }

    /**
     * error 로그 출력
     *
     * @param tag
     *         태그
     * @param msg
     *         메세지
     */
    public static void e(String tag, String msg) {
        if (!Constants.RELEASE_BUILD && tag != null && msg != null) {
            Log.e("[" + tag + "]", msg);
        }
    }

    /**
     * error 로그 출력
     *
     * @param tag
     *         태그
     * @param msg
     *         메세지
     * @param e
     *         Throwable
     */
    public static void e(String tag, String msg, Throwable e) {
        if (!Constants.RELEASE_BUILD && tag != null && msg != null) {
            Log.e("[" + tag + "]", msg, e);
        }
    }

    /**
     * fatal 로그 출력
     *
     * @param tag
     *         태그
     * @param msg
     *         메세지
     */
    public static void f(String tag, String msg) {
        if (!Constants.RELEASE_BUILD && tag != null && msg != null) {
            Log.e("[FATAL][" + tag + "]", msg);
        }
    }
}
