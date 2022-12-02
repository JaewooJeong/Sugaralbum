
package com.kiwiple.imageanalysis;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.opengl.GLES20;
import android.os.Build;
import android.text.TextUtils;

import com.kiwiple.imageanalysis.utils.SmartLog;

public class PreferenceManager {
    private static final String SHARED_PREFERENCES_ID = "IMAGEANALYSISSDK";

    private static PreferenceManager mInstance;
    private SharedPreferences mPreferences;

    private int[] mMaxTextureSize = new int[1];

    /**
     * PreferenceManager 싱글톤 생성자.
     * 
     * @param ctx Context
     * @return PreferenceManager 생성 객체
     */
    public static PreferenceManager getInstance(Context ctx) {
        if(mInstance == null) {
            mInstance = new PreferenceManager(ctx);
        }
        return mInstance;
    }

    private PreferenceManager(Context ctx) {
        mPreferences = ctx.getSharedPreferences(SHARED_PREFERENCES_ID, Context.MODE_PRIVATE);
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, mMaxTextureSize, 0);
    }

    /**
     * 특정 태그를 처음 수행하는지 여부를 반환한다.
     * 
     * @param tag 태그
     * @return boolean tag에 해당되는 기능이나 행동이 처음 수행되는지 여부
     */
    public boolean isFirstExcution(String tag) {
        if(mPreferences.getBoolean(tag, true)) {
            SharedPreferences.Editor editor = mPreferences.edit();
            editor.putBoolean(tag, false);
            commit(editor);
            return true;
        }
        return false;
    }

    /**
     * 특정 태깅(tagging)이 된 기능이나 행동이 오늘 처음되었는지 여부를 반환한다.
     * 
     * @param tag 태그
     * @return boolean tag에 해당되는 기능이나 행동이 오늘 처음 수행되었는지 여부
     */
    public boolean isFirstExcutionToday(String tag) {
        boolean result = false;

        String lastLoginDate = mPreferences.getString(tag, null);

        Calendar rightNow = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        String strdate = sdf.format(rightNow.getTime());

        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(tag, strdate);

        if(TextUtils.isEmpty(lastLoginDate)) {
            result = true;
        } else {
            Calendar create = Calendar.getInstance();
            Date createData;
            try {
                createData = sdf.parse(lastLoginDate);
                create.setTime(createData);
                if(isSameDay(rightNow, create)) {
                    result = false;
                } else {
                    result = true;
                }
            } catch(ParseException e) {
                SmartLog.e("MhkPreferenceManager", "Data Parse Error", e);
            }
        }
        commit(editor);
        return result;
    }

    private static boolean isSameDay(Calendar cal1, Calendar cal2) {
        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA)
                && cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
    }

    /**
     * 모든 Preference 값을 초기화 한다.
     */
    public void clear() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.clear();
        commit(editor);
    }

    private static void commit(SharedPreferences.Editor editor) {
        if(Build.VERSION.SDK_INT >= 9) {
            editor.apply();
        } else {
            editor.commit();
        }
    }
}
