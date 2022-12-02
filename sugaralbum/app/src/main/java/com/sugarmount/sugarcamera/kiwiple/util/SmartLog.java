
package com.sugarmount.sugarcamera.kiwiple.util;

import android.util.Log;

import com.sugarmount.sugarcamera.kiwiple.KiwipleConstants;

public class SmartLog {
    private static SmartLog sInstance;

    public static SmartLog getInstance() {
        if(sInstance == null) {
            sInstance = new SmartLog();
        }
        return sInstance;
    }

    private SmartLog() {
        mState = LE_DEBUG;// LE_DEBUG;
    }

    private final int LE_VERVE = 1;
    private final int LE_DEBUG = 2;
    private final int LE_INFO = 3;
    private final int LE_WARNING = 4;
    private final int LE_ERROR = 5;
    private final int LE_FATAL = 6;
    private final int LE_NONE = 10;

    private int mState;

    public void SetLogLevel(String level) {
        if(level == "v")
            mState = LE_VERVE;
        else if(level.equals("V"))
            mState = LE_VERVE;
        else if(level.equals("d"))
            mState = LE_DEBUG;
        else if(level.equals("D"))
            mState = LE_DEBUG;
        else if(level.equals("i"))
            mState = LE_INFO;
        else if(level.equals("I"))
            mState = LE_INFO;
        else if(level.equals("w"))
            mState = LE_WARNING;
        else if(level.equals("W"))
            mState = LE_WARNING;
        else if(level.equals("e"))
            mState = LE_ERROR;
        else if(level.equals("E"))
            mState = LE_ERROR;
        else if(level.equals("f"))
            mState = LE_FATAL;
        else if(level.equals("F"))
            mState = LE_FATAL;
        else if(level.equals("n"))
            mState = LE_NONE;
        else if(level.equals("N"))
            mState = LE_NONE;
    }

    public void v(String tag, String msg) {
        if(!KiwipleConstants.RELEASE_BUILD) {
            if(mState <= LE_VERVE) {
                Log.v("[" + tag + "]", msg);
            }
        }
    }

    public void d(String tag, String msg) {
        if(!KiwipleConstants.RELEASE_BUILD) {
            if(mState <= LE_DEBUG) {
                Log.d("[" + tag + "]", msg);
            }
        }
    }

    public void i(String tag, String msg) {
        if(!KiwipleConstants.RELEASE_BUILD) {
            if(mState <= LE_INFO) {
                Log.i("[" + tag + "]", msg);
            }
        }
    }

    public void w(String tag, String msg) {
        if(!KiwipleConstants.RELEASE_BUILD) {
            if(mState <= LE_WARNING) {
                Log.w("[" + tag + "]", msg);
            }
        }
    }

    public void e(String tag, String msg) {
        if(!KiwipleConstants.RELEASE_BUILD) {
            if(mState <= LE_ERROR) {
                Log.e("[" + tag + "]", msg);
            }
        }
    }

    public void e(String tag, String msg, Throwable e) {
        if(!KiwipleConstants.RELEASE_BUILD) {
            if(mState <= LE_ERROR) {
                Log.e("[" + tag + "]", msg, e);
            }
        }
    }

    public void f(String tag, String msg) {
        if(!KiwipleConstants.RELEASE_BUILD) {
            if(mState <= LE_FATAL) {
                Log.e("[FATAL][" + tag + "]", msg);
            }
        }
    }
}
