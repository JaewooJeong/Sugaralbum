package com.sugarmount.sugaralbum;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.multidex.MultiDexApplication;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.sugarmount.common.ads.AppOpenManager;
import com.sugarmount.common.env.MvConfig;
import com.sugarmount.common.utils.log;

import java.util.Collections;
import java.util.List;


/**
 * Created by Jaewoo on 2020-12-16.
 */
public class GlobalApplication extends MultiDexApplication implements MvConfig {
    /// @ 주요 앱 저장 변수
    public static SharedPreferences pref;
    public static SharedPreferences.Editor editor;

    /// @ Global 변수
    private static int default_Color = 0;
    public static volatile GlobalApplication instance = null;
    private static volatile InputMethodManager mImm;
    private static Point point;
    private static boolean refresh = false;
    private static Toast toast;
    private static AppOpenManager appOpenManager;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    public static synchronized GlobalApplication getInstance(){
        if(instance == null){
            instance = new GlobalApplication();
        }
        return instance;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        //xx
        log.e("support abis: %s", Build.SUPPORTED_ABIS);
        log.e("os.arch: %s", System.getProperty("os.arch"));

        // ADS SDK
//        MobileAds.initialize(this);
//        GoogleAds.Companion.loadInterstitialAd(this);

        appOpenManager = new AppOpenManager();
        appOpenManager.initialize(this);

        if(MvConfig.debug) {
            List<String> testDeviceIds = Collections.singletonList("1AAA21F530BFD426F7E5EB8B127D4796");
            RequestConfiguration configuration =
                    new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
            MobileAds.setRequestConfiguration(configuration);
        }

        mImm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        pref = getSharedPreferences("pref", MODE_PRIVATE);
        editor = pref.edit();

        point = new Point();
        point.x = pref.getInt("point.x", 0);
        point.y = pref.getInt("point.y", 0);

        refresh = pref.getBoolean("opt.refresh", false);

    }

    /**
     * Interface definition for a callback to be invoked when an app open ad is complete
     * (i.e. dismissed or fails to show).
     */
    public interface OnShowAdCompleteListener {
        void onShowAdComplete();
    }

    /**
     * Shows an app open ad.
     *
     * @param activity the activity that shows the app open ad
     * @param onShowAdCompleteListener the listener to be notified when an app open ad is complete
     */
    public void showAdIfAvailable(
            @NonNull Activity activity,
            @NonNull OnShowAdCompleteListener onShowAdCompleteListener) {
        // We wrap the showAdIfAvailable to enforce that other classes only interact with MyApplication
        // class.
        appOpenManager.showAdIfAvailable(activity, onShowAdCompleteListener);
    }

    /**
     * singleton 애플리케이션 객체를 얻는다.
     * @return singleton 애플리케이션 객체
     */
    public static GlobalApplication getGlobalApplicationContext() {
        if(instance == null)
            throw new IllegalStateException("The Material Design color system uses an organized approach to applying color to your UI. In this system, a primary and a secondary color are typically selected to represent your brand. Dark and light variants of each color can then be applied to your UI in different ways.");
        return instance;
    }

    /**
     * 애플리케이션 종료시 singleton 어플리케이션 객체 초기화한다.
     */
    @Override
    public void onTerminate() {
        super.onTerminate();
        instance = null;
    }

    /**
     * 키보드 보이기
     * @param v
     */
    public static void showSoftInputKeyboard(final View v){
        new Handler().postDelayed(new Runnable() {
            public void run() {
                v.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN , 0, 0, 0));
                v.dispatchTouchEvent(MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP , 0, 0, 0));
            }
        }, 100);
    }

    /**
     * 키보드 숨기기
     * @param v
     */
    public static void hideSoftInputKeyboard(final View v){
        if (mImm.isAcceptingText()) {
            mImm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
        }
    }

    /**
     * 키보드 숨기기
     * @param a
     */
    public static void hideSoftInputKeyboardActivity(final Activity a){
        View view = a.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(a);
        }
        mImm.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
    }

    public static void setStatusColor(Window window, int color){
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(color);
        default_Color = color;
        if(color != Color.WHITE)
            window.getDecorView().setSystemUiVisibility(0);
    }

    public static void setNavigationColor(Window window, int color){
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setNavigationBarColor(color);
        default_Color = color;
    }

    public static Point getPoint() {
        return point;
    }

    public static void setPoint(Point point) {
        GlobalApplication.point = point;
        editor.putInt("point.x", point.x);
        editor.putInt("point.y", point.y);
        editor.commit();
    }

    public static Toast getToast() {
        return toast;
    }

    public static void setToast(Toast toast) {
        GlobalApplication.toast = toast;
    }

    public static boolean isWifiConnect(){
        ConnectivityManager connManager = (ConnectivityManager) getGlobalApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (mWifi.isConnected()) {
            return true;
        }else{
            return false;
        }
    }

    public static boolean isRefresh() {
        return refresh;
    }

    public static void setRefresh(boolean refresh) {
        GlobalApplication.refresh = refresh;
        editor.putBoolean("opt.refresh", refresh);
        editor.commit();
    }

}
