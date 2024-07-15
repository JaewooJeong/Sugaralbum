package com.sugarmount.common.utils;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.storage.StorageManager;
import android.text.format.DateFormat;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

//import com.sugarmount.common.model.DataTextSize;
import com.sugarmount.common.env.MvConfig;
import com.sugarmount.sugaralbum.GlobalApplication;
import com.sugarmount.sugaralbum.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

/**
 * Created by Jaewoo on 2018-11-29.
 */
public final class Utils {
    private static final String[] units = {
            "bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"
    };

    private Utils() {
    }

    /**
     * change dp to px
     * @param context
     * @param dpValue
     * @return
     */
    public static int dp2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * change sp to px
     * @param spValue
     * @return
     */
    public static int sp2px(Context context, float spValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (spValue * fontScale + 0.5f);
    }
    
    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return  dp * scale + 0.5f;
    }

    public static float sp2px(Resources resources, float sp){
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }

    public static float px2dp(Resources resources, float px){
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts dp unit to equivalent pixels, depending on device density.
     *
     * @param dp A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float convertDpToPixel(Context context, float dp){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(Context context, float px){
        return px / ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }

    @SuppressLint("DefaultLocale")
    public static String switchSToM(Integer s){
        return String.format("%02d:%02d:%02d",(s/3600), ((s % 3600)/60), (s % 60));
    }

    @SuppressLint("DefaultLocale")
    public static String switchBToUnit(Long bytes){
        double unit = Math.floor(Math.log(bytes)/ Math.log(1024));
        double calcBytes = (bytes/ Math.pow(1024, unit));
        if(Double.isNaN(calcBytes)) {
            unit = 0;
            return String.format("0 %s", units[(int) unit]);
        }else {
            return String.format("%02d %s", (int) calcBytes, units[(int) unit]);
        }
    }

    public static String getCurrentTime(){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(new Date(System.currentTimeMillis()));
    }

    private static String getDateCheck(Locale locale, int n, String p1, String p2, String p3){
        return String.format(locale, "%d %s %s", n, n <= 1 ? p1 : p2, p3);
    }

    @SuppressLint("SimpleDateFormat")
    public static String switchLastTimeCheck(Context context, String s){
        try {
            if(!s.equals("")) {
                Locale locale = Locale.getDefault();
                if(context.getResources().getConfiguration().getLocales().size() > 0){
                    locale = context.getResources().getConfiguration().getLocales().get(0);
                }
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", locale);
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

                long a = Objects.requireNonNull(sdf.parse(s)).getTime();
                long b = System.currentTimeMillis();
                String datec = getCurrentTime();

//                slog.i("lastTime: %d, %s", a, s);
//                slog.i("currTime: %d, %s", b, datec);

                if(b > a) {
                    float c = (b - a) / 1000L;
                    int d = (int)c % 60; // 초
                    int e = (int)c / 60 % 60; // 분
                    int f = (int)c / 3600; // 시
                    int g = (int)c / (24 * 60 * 60); // 일
                    int h = (int)(c / (24L * 60L * 60L * 31L)); // 월
                    int i = (int)(c / (24L * 60L * 60L * 31L * 12)); // 년

                    Resources resources = context.getResources();
                    if(e <= 0){
                        return getDateCheck(locale, d, resources.getString(R.string.string_time_second),
                                resources.getString(R.string.string_time_seconds),
                                resources.getString(R.string.string_time_ago));
                    }else if(f <= 0){
                        return getDateCheck(locale, e, resources.getString(R.string.string_time_minute),
                                resources.getString(R.string.string_time_minutes),
                                resources.getString(R.string.string_time_ago));
                    }else if(g <= 0){
                        return getDateCheck(locale, f, resources.getString(R.string.string_time_hour),
                                resources.getString(R.string.string_time_hours),
                                resources.getString(R.string.string_time_ago));
                    }else if(h <= 0){
                        return getDateCheck(locale, g, resources.getString(R.string.string_time_day),
                                resources.getString(R.string.string_time_days),
                                resources.getString(R.string.string_time_ago));
                    }else if(i <= 0){
                        return getDateCheck(locale, h, resources.getString(R.string.string_time_month),
                                resources.getString(R.string.string_time_months),
                                resources.getString(R.string.string_time_ago));
                    }else{
                        return getDateCheck(locale, i, resources.getString(R.string.string_time_year),
                                resources.getString(R.string.string_time_years),
                                resources.getString(R.string.string_time_ago));
                    }
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return s;
    }

    @SuppressLint("SimpleDateFormat")
    public static String switchTypeFromString(String s){
        try {
            if(!s.equals("")) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date newDate = sdf.parse(s);
                sdf = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyy-MM-dd'T'hh:mm:ss"));
                s = sdf.format(newDate);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return s;
    }

    public static void setAnimation(View view) {
        boolean scrollEvent = true;
        ObjectAnimator animator = ObjectAnimator.ofFloat(view, "translationY", scrollEvent == true ? 300 : -300, 0);
        animator.setDuration(350);
        animator.start();
    }

    public static String getCurrentDateString(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.KOREA);

        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        return sdf.format(date);
    }


    public static String getDefaultCurrentTime(){
        Calendar c = Calendar.getInstance(Locale.getDefault());
        c.setTimeZone(TimeZone.getTimeZone("UTC"));

        return c.getTime().toString();
    }

    /**
     * Get external sd card path using reflection
     * @param mContext
     * @param is_removable is external storage removable
     * @return
     */
    public static File getExternalStoragePath(Context mContext, boolean is_removable) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method getPathFile = storageVolumeClazz.getMethod("getPathFile");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);

                if (is_removable == removable) {
                    //return path;
                    return (File) getPathFile.invoke(storageVolumeElement);
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void getVersion(Context context, TextView v){
        PackageInfo pInfo;
        try {
            pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);

            v.setText(new StringBuffer("Version ").append(pInfo.versionName).append("(").append("AVPL3.0SX.3.2").append(")").toString());
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static boolean isDownloadContentsSDCard(){

        StoragePath storagePath;
        storagePath = new StoragePath(GlobalApplication.getInstance().getExternalFilesDirs(null));

        String[] storages = storagePath.getDeviceStorages();

        File[] fs = GlobalApplication.getInstance().getExternalFilesDirs(null);
        return fs.length > 1;
    }

    public static String checkAllStorageContents(int id){
        File[] fs = GlobalApplication.getInstance().getExternalFilesDirs(null);
        for (File f: fs) {
            StringBuffer sb = new StringBuffer();
            sb.append(f.getPath()).append("/");
            sb.append(GlobalApplication.getInstance().getString(R.string.app_name)).append("/");
            sb.append("Contents_").append(id);

            if(new File(sb.toString()).exists()){
                return sb.toString();
            }
        }

        return null;
    }

    public static String downloadContentsRoot(MvConfig.CONTENTS_DOWNLOAD_TYPE type, int id){
        StringBuffer sb = new StringBuffer();
        File[] fs = GlobalApplication.getInstance().getExternalFilesDirs(null);
        sb.append(type == MvConfig.CONTENTS_DOWNLOAD_TYPE.INTERNAL ? fs[0].getPath() : fs[1].getPath()).append("/");
        sb.append(GlobalApplication.getInstance().getString(R.string.app_name)).append("/");
        sb.append("Contents_").append(id);
        return sb.toString();
    }

    public static void getTopActivity() {

//        PackageManager pkgMgr = GlobalApplication.getGlobalApplicationContext().getPackageManager();
//        List<ResolveInfo> mApps;
//        String[] arrayPkgName;
//
//        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
//        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
//        mApps = pkgMgr.queryIntentActivities(mainIntent, 0); // 실행가능한 Package만 추출.
//
//        arrayPkgName = new String[mApps.size()];
//
//        for (int i = 0; i < mApps.size(); i++)
//        {
//            slog.e("%s: ", mApps.get(i).activityInfo.packageName);
//            slog.e("%s: ", mApps.get(i).activityInfo.loadLabel(pkgMgr).toString());
//
//
//        }


        //String topActivityCmd = "dumpsys activity | grep top-activity";
//        String topActivityCmd = "dumpsys window";
        String command[] = new String[]{"sh", "-c",
            "dumpsys window"};

        //String topActivityCmd = "dumpsys -l";
        Runtime runtime = Runtime.getRuntime();
        try {
            Process p = runtime.exec(command);
            BufferedReader subProcessInputReader =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));

            BufferedReader errorResult =
                    new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String line = null;
            while ((line = subProcessInputReader.readLine()) != null)
                log.e("read:%s", line);

            while ((line = errorResult.readLine()) != null)
                log.e("read error:%s", line);

            subProcessInputReader.close();

        } catch (Exception e) {
            log.e("Exception deleteDir:%s", e);
        }



//        String packageName = "";
//        try {
//            String command[] = new String[]{"sh", "-c",
//                    "dumpsys activity | grep top-activity"};
//            //"dumpsys window | grep mCurrentFocus"
//            Process process = Runtime.getRuntime().exec(command);
//            int code = process.waitFor();
//            slog.e("code ： %d", code);
//            StringBuilder successMsg = new StringBuilder();
//            StringBuilder errorMsg = new StringBuilder();
//            BufferedReader successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
//            BufferedReader errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
//            String s;
//            while ((s = successResult.readLine()) != null) {
//                successMsg.append(s);
//            }
//            while ((s = errorResult.readLine()) != null) {
//                errorMsg.append(s);
//            }
//            slog.e("successMsg ： %s", successMsg);
//            slog.e("errorMsg ： %s", errorMsg);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }

    public static void deleteDir(int dirName) {
        StringBuffer sb;
        File[] fs = GlobalApplication.getInstance().getExternalFilesDirs(null);
        for (File f: fs) {
            sb = new StringBuffer();
            sb.append(f.getPath()).append("/");
            sb.append(GlobalApplication.getInstance().getString(R.string.app_name)).append("/");
            sb.append("Contents_").append(dirName);

            if(new File(sb.toString()).exists()){
                String deleteCmd = "rm -r " + sb.toString();
                Runtime runtime = Runtime.getRuntime();
                try {
                    runtime.exec(deleteCmd);
                } catch (Exception e) {
                    log.e("Exception deleteDir");
                }
            }
        }
    }

    public static long getFreeInternalBytes(MvConfig.CONTENTS_DOWNLOAD_TYPE type){
        File[] fs = GlobalApplication.getInstance().getExternalFilesDirs(null);
        return (type == MvConfig.CONTENTS_DOWNLOAD_TYPE.INTERNAL ? fs[0] : fs[1]).getFreeSpace();
    }

    public static boolean isNetworkConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)GlobalApplication.getInstance().getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    public static Point getNavigationBarSize(Context context) {
        Point appUsableSize = getAppUsableScreenSize(context);
        Point realScreenSize = getRealScreenSize(context);

        // navigation bar on the right
        if (appUsableSize.x < realScreenSize.x) {
            return new Point(realScreenSize.x - appUsableSize.x, appUsableSize.y);
        }

        // navigation bar at the bottom
        if (appUsableSize.y < realScreenSize.y) {
            return new Point(appUsableSize.x, realScreenSize.y - appUsableSize.y);
        }

        // navigation bar is not present
        return new Point();
    }

    public static Point getAppUsableScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size;
    }

    public static Point getRealScreenSize(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();

        if (Build.VERSION.SDK_INT >= 17) {
            display.getRealSize(size);
        } else if (Build.VERSION.SDK_INT >= 14) {
            try {
                size.x = (Integer) Display.class.getMethod("getRawWidth").invoke(display);
                size.y = (Integer) Display.class.getMethod("getRawHeight").invoke(display);
            } catch (IllegalAccessException e) {} catch (InvocationTargetException e) {} catch (NoSuchMethodException e) {}
        }

        return size;
    }

    public static Point statusBar(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return new Point(0, context.getResources().getDimensionPixelSize(resourceId));
        }else{
            return new Point(0,0);
        }
    }

    public static int getNavigationBarHeight(WindowManager windowManager) {
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;

        windowManager.getDefaultDisplay().getMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight)
            return realHeight - usableHeight;
        else
            return 0;
    }

    public static String[] split(String value) {
        //int supportedFlags = 46 | 63 | 33;
        byte supportedFlagsA = 46;
        byte supportedFlagsB = 63;
        byte supportedFlagsC = 33;
        byte supportedFlagsD = 10;
        byte[] bytes = value.getBytes();
        byte[] temp;
        List<String> list = new ArrayList<>();
        int check = 0;
        for(int i=0; i<bytes.length; i++){
            if ((bytes[i] == supportedFlagsA ||
                    bytes[i] == supportedFlagsB ||
                    bytes[i] == supportedFlagsC ||
                    bytes[i] == supportedFlagsD) &&
                    i > 0 &&
                    bytes[i] != bytes[i-1] ||
                    bytes.length == i+1) {
                temp = new byte[i - check + 1];
                for(int n=0; n<temp.length; n++){
                    temp[n] += bytes[check++];
                }
                check = i + 1;
                list.add(new String(temp));
//                System.out.println(new String(temp));
            }
        }

        if(list.size() == 0){
            return new String[] { value };
        } else {
//            int n = value.indexOf(list.get(list.size()));
//            int length = list.get(list.size()).length();
//            if(n != -1){
//
//            }

            return Arrays.copyOf(list.toArray(), list.size(), String[].class);
        }
    }

}
