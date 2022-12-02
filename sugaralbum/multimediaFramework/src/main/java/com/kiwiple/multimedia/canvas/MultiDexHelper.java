package com.kiwiple.multimedia.canvas;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import com.kiwiple.debug.Precondition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dalvik.system.DexFile;

import static com.kiwiple.multimedia.Constants.CANVAS_PACKAGE_NAME;

/**
 * Created by Jaewoo on 2017-03-16.
 */
public class MultiDexHelper {

    private static final String EXTRACTED_NAME_EXT = ".classes";
    private static final String EXTRACTED_SUFFIX = ".zip";

    private static final String SECONDARY_FOLDER_NAME = "code_cache" + File.separator + "secondary-dexes";

    private static final String PREFS_FILE = "multidex.version";
    private static final String KEY_DEX_NUMBER = "dex.number";

    private static SharedPreferences getMultiDexPreferences(Context context) {
        return context.getSharedPreferences(PREFS_FILE, Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB ?
                Context.MODE_PRIVATE :
                Context.MODE_PRIVATE | Context.MODE_MULTI_PROCESS);
    }

    /**
     * get all the dex path
     *
     * @param context the application context
     * @return all the dex path
     * @throws PackageManager.NameNotFoundException
     * @throws IOException
     */
    public static List<String> getSourcePaths(Context context) throws PackageManager.NameNotFoundException, IOException {
        ApplicationInfo applicationInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), 0);
        File sourceApk = new File(applicationInfo.sourceDir);
        File dexDir = new File(applicationInfo.dataDir, SECONDARY_FOLDER_NAME);

        if (LogUtil.isDebugModeEnable()) {
            LogUtil.d("MultiDexHelper",
                    "getSourcePaths sourceDir=" + applicationInfo.sourceDir + ", dataDir=" + applicationInfo.dataDir);
        }

        List<String> sourcePaths = new ArrayList<String>();
        sourcePaths.add(applicationInfo.sourceDir); //add the default apk path

        //the prefix of extracted file, ie: test.classes
        String extractedFilePrefix = sourceApk.getName() + EXTRACTED_NAME_EXT;
        //the total dex numbers
        int totalDexNumber = getMultiDexPreferences(context).getInt(KEY_DEX_NUMBER, 1);

        if (LogUtil.isDebugModeEnable()) {
            LogUtil.d("MultiDexHelper", "getSourcePaths totalDexNumber=" + totalDexNumber);
        }

        for (int secondaryNumber = 2; secondaryNumber <= totalDexNumber; secondaryNumber++) {
            //for each dex file, ie: test.classes2.zip, test.classes3.zip...
            String fileName = extractedFilePrefix + secondaryNumber + EXTRACTED_SUFFIX;
            File extractedFile = new File(dexDir, fileName);
            if (extractedFile.isFile()) {
                sourcePaths.add(extractedFile.getAbsolutePath());
                //we ignore the verify zip part
            } else {
                throw new IOException("Missing extracted secondary dex file '" +
                        extractedFile.getPath() + "'");
            }
        }
        try {
            // handle dex files built by instant run
            File instantRunFilePath = new File(applicationInfo.dataDir,
                    "files" + File.separator + "instant-run" + File.separator + "dex");
            if (LogUtil.isDebugModeEnable()) {
                LogUtil.d("MultiDexHelper", "getSourcePaths instantRunFile exists=" + instantRunFilePath.exists() + ", isDirectory="
                        + instantRunFilePath.isDirectory() + ", getAbsolutePath=" + instantRunFilePath.getAbsolutePath());
            }
            if (instantRunFilePath.exists() && instantRunFilePath.isDirectory()) {
                File[] sliceFiles = instantRunFilePath.listFiles();
                for (File sliceFile : sliceFiles) {
                    if (null != sliceFile && sliceFile.exists() && sliceFile.isFile() && sliceFile.getName().endsWith(".dex")) {
                        sourcePaths.add(sliceFile.getAbsolutePath());
                    }
                }
            }
        } catch (Throwable e) {
            LogUtil.e("MultiDexHelper", "getSourcePaths parse instantRunFilePath exception", e);
        }

        return sourcePaths;
    }

    /**
     * get all the classes name in "classes.dex", "classes2.dex", ....
     *
     * @param context the application context
     * @return all the classes name
     * @throws PackageManager.NameNotFoundException
     * @throws IOException
     */
    public static ArrayList<Class<? extends AbstractCanvasUser>> getAllClasses(Context context) throws PackageManager.NameNotFoundException, IOException {
        ArrayList<Class<? extends AbstractCanvasUser>> list = new ArrayList<>();

        for (String path : getSourcePaths(context)) {
            try {
                DexFile dexfile = null;
                if (path.endsWith(EXTRACTED_SUFFIX)) {
                    //NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
                    dexfile = DexFile.loadDex(path, path + ".tmp", 0);
                } else {
                    dexfile = new DexFile(path);
                }
                Enumeration<String> dexEntries = dexfile.entries();
                String name = null;
                while (dexEntries.hasMoreElements()) {
                    name = dexEntries.nextElement();
                    if (!name.startsWith(CANVAS_PACKAGE_NAME))
                        continue;

                    Class<?> type = Class.forName(name);
                    if (!AbstractCanvasUser.class.isAssignableFrom(type))
                        continue;
                    if (ReflectionUtils.isAbstractClass(type))
                        continue;
                    list.add((Class<? extends AbstractCanvasUser>) type);
                }
            } catch (IOException e) {
                throw new IOException("Error at loading dex file '" + path + "'");
            } catch (ClassNotFoundException exception) {
                Precondition.assureUnreachable(exception.getMessage());
            }
        }

        return list;
    }

    /**
     * scan parent class's sub classes
     *
     * @param context
     * @param packageName
     * @param parentClass
     * @param <T>
     * @return
     */
    public static <T> Set<Class<? extends T>> scanClasses(Context context, String packageName, Class<T> parentClass) {
        Set<Class<? extends T>> classes = new HashSet<Class<? extends T>>();
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            for (String path : getSourcePaths(context)) {
                if (LogUtil.isDebugModeEnable()) {
                    LogUtil.d("MultiDexHelper", "scanClasses path=" + path);
                }
                try {
                    DexFile dexfile = null;
                    if (path.endsWith(EXTRACTED_SUFFIX)) {
                        //NOT use new DexFile(path), because it will throw "permission error in /data/dalvik-cache"
                        dexfile = DexFile.loadDex(path, path + ".tmp", 0);
                    } else {
                        dexfile = new DexFile(path);
                    }
                    Enumeration<String> dexEntries = dexfile.entries();
                    while (dexEntries.hasMoreElements()) {
                        String className = dexEntries.nextElement();
                        if (LogUtil.isDebugModeEnable()) {
                            LogUtil.d("MultiDexHelper", "scanClasses className=" + className);
                        }
                        if (className.toLowerCase().startsWith(packageName.toLowerCase())) {
                            Class clazz = classLoader.loadClass(className);
                            if (LogUtil.isDebugModeEnable()) {
                                LogUtil.d("MultiDexHelper",
                                        "scanClasses clazz=" + clazz + ", parentClass=" + parentClass + ", equals=" + clazz
                                                .getSuperclass().equals(parentClass));
                            }
                            if (clazz.getSuperclass().equals(parentClass)) {
                                classes.add(clazz);
                            }
                        }
                    }
                } catch (Throwable e) {
                    LogUtil.e("MultiDexHelper", "scanClasses Error at loading dex file '" +
                            path + "'", e);
                }
            }
        } catch (Throwable e) {
            LogUtil.e("MultiDexHelper", "scanClasses exception", e);
        }
        return classes;
    }

    public static class LogUtil {

        public static boolean isDebugModeEnable(){
            return true;
        }


        public static void e(String a, String b, Throwable c){
            Log.e(a, b);
        }

        public static void d(String a, String b){
            Log.d(a, b);
        }
    }
}