
package com.kiwiple.imageframework.util;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

/**
 * 캐시 관리를 위한 유틸 클래스
 */
public class CacheUtils {
    private static String sCachePath;

    private static void createCachePath(Context context) {
        if(TextUtils.isEmpty(sCachePath)) {
            StringBuilder sb = new StringBuilder();
            sb.append(context.getCacheDir().getAbsolutePath()).append(File.separator)
              .append(context.getPackageName()).append(File.separator);
            sCachePath = sb.toString();
        }
        if(!FileUtils.isExist(sCachePath)) {
            new File(sCachePath).mkdirs();
        }
    }

    /**
     * 비트맵을 캐시(파일)로 저장
     * 
     * @param context
     * @param bitmap 저장 비트맵
     * @param filename 저장 경로
     * @param format 저장 포멧
     * @throws IOException
     */
    public static void saveCacheFile(Context context, Bitmap bitmap, String filename,
            Bitmap.CompressFormat format) throws IOException {
        createCachePath(context);
        filename = new StringBuffer().append(sCachePath).append(File.separator).append(filename)
                                     .toString();

        FileUtils.saveBitmap(bitmap, filename, format);
    }

    /**
     * 캐시된 비트맵을 가져온다
     * 
     * @param context
     * @param filename 가져올 캐시 경로
     * @return
     */
    public static Bitmap readCacheFile(Context context, String filename) {
        createCachePath(context);
        filename = new StringBuffer().append(sCachePath).append(File.separator).append(filename)
                                     .toString();
        return BitmapFactory.decodeFile(filename);
    }

    /**
     * 캐시로 저장된 파일 모두 삭제
     * 
     * @param context
     */
    public static void deleteCacheFileAll(Context context) {
        createCachePath(context);
        File f = new File(sCachePath);
        if(f.isDirectory()) {
            File[] files = f.listFiles();
            int size = files.length - 1;
            for(int i = size; i >= 0; i--) {
                files[i].delete();
            }
        }
    }
}
