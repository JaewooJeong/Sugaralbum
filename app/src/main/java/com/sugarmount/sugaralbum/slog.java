package com.sugarmount.sugaralbum;

import android.util.Log;


/**
 * Created by Jaewoo on 2016-09-29.
 */
public class slog {


    private static final String TAG = "SugarAlbum";

    public static void v(String msg) {
        if ( BuildConfig.DEBUG )
            Log.v(TAG, msg);
    }

    public static void d(String msg) {
        if ( BuildConfig.DEBUG )
            Log.d(TAG, msg);
    }

    public static void i(String msg) {
        if ( BuildConfig.DEBUG )
            Log.i(TAG, msg);
    }

    public static void e(String msg) {
        Log.e(TAG, msg);
    }

    public static void v(String format, Object... msg) {
        if ( BuildConfig.DEBUG )
            Log.v(TAG, String.format(format, msg));
    }

    public static void d(String format, Object... msg) {
        if ( BuildConfig.DEBUG )
            Log.d(TAG, String.format(format, msg));
    }

    public static void i(String format, Object... msg) {
        if ( BuildConfig.DEBUG )
            Log.i(TAG, String.format(format, msg));
    }

    public static void e(String format, Object... msg) {
        Log.e(TAG, String.format(format, msg));
    }
}
