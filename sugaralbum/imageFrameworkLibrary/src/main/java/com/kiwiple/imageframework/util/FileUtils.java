
package com.kiwiple.imageframework.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.text.TextUtils;

/**
 * 이미지 파일, 안드로이드 리소스 파일을 {@link android.graphics#Bitmap}로 디코딩하고<br>
 * {@link android.graphics#Bitmap}을 이미지 파일로 저장하는 기능을 지원하기 위한 클래스
 * 
 * @version 2.0
 */
public class FileUtils {

    /**
     * 안드로이드 리소스 파일의 id를 반환한다.
     * 
     * @param context
     * @param resourceName 리소스 이름
     * @return 리소스 id
     * @version 2.0
     */
    public static int getBitmapResourceId(Context context, String resourceName) {
        return context.getResources().getIdentifier(resourceName, "drawable",
                                                    context.getPackageName());
    }

    /**
     * 안드로이드 리소스 파일을 {@link android.graphics#Bitmap}로 디코딩한다.
     * 
     * @param context
     * @param resourceName 리소스 이름
     * @param width 이미지의 가로 길이
     * @param height 이미지의 세로 길이
     * @param config
     * @return 디코딩된 이미지
     * @version 2.0
     */
    public static Bitmap getBitmapResource(Context context, String resourceName, int width,
            int height, Bitmap.Config config) {
        Bitmap cover = getBitmapResourceRatio(context, resourceName, width, height, config);

        if(cover != null && (cover.getWidth() != width || cover.getHeight() != height)) {
            Bitmap resize = BitmapUtils.resizeBitmap(cover, width, height);
            cover.recycle();
            cover = resize;
        }
        return cover;
    }

    /**
     * 이미지를 특정 사이즈로 디코딩한다.<br>
     * 원본 비율을 유지한채 사이즈에 최대한 가까운 크기의 비트맵으로 반환
     * 
     * @param context
     * @param resourceName 리소스 이름
     * @param size 설정할 사이즈
     * @param config Bitmap.Config
     * @return 디코딩된 이미지
     */
    public static Bitmap getBitmapResource(Context context, String resourceName, int size,
            Bitmap.Config config) {
        int resourceId = context.getResources().getIdentifier(resourceName, "drawable",
                                                              context.getPackageName());
        return getBitmapResourceRatio(context, resourceId, size, config);
    }

    /**
     * 안드로이드 리소스 파일을 {@link android.graphics#Bitmap}로 디코딩한다.
     * 
     * @param context
     * @param resourceName 리소스 이름
     * @param width 이미지의 가로 길이
     * @param height 이미지의 세로 길이
     * @param config
     * @return 디코딩된 이미지
     * @remark 결과 이미지의 가로/세로 비율이 원본과 동일하게 유지된다.
     * @version 2.0
     */
    public static Bitmap getBitmapResourceRatio(Context context, String resourceName, int width,
            int height, Bitmap.Config config) {
        int resourceId = context.getResources().getIdentifier(resourceName, "drawable",
                                                              context.getPackageName());
        return getBitmapResourceRatio(context, resourceId, width, height, config);
    }

    /**
     * 안드로이드 리소스 파일을 {@link android.graphics#Bitmap}로 디코딩한다.
     * 
     * @param context
     * @param resourceId 리소스 id
     * @param width 이미지의 가로 길이
     * @param height 이미지의 세로 길이
     * @param config
     * @return 디코딩된 이미지
     * @remark 결과 이미지의 가로/세로 비율이 원본과 동일하게 유지된다.
     * @version 2.0
     */
    public static Bitmap getBitmapResourceRatio(Context context, int resourceId, int width,
            int height, Bitmap.Config config) {
        return getBitmapResourceRatio(context, resourceId, width > height ? width : height, config);
    }

    /**
     * 이미지를 특정 사이즈로 디코딩한다.<br>
     * 원본 비율을 유지한채 사이즈에 최대한 가까운 크기의 비트맵으로 반환
     * 
     * @param context
     * @param resourceId 리소스 아이디
     * @param size 원하는 사이즈
     * @param config Bitmap.Config
     * @return 디코딩된 비트맵
     */
    public static Bitmap getBitmapResourceRatio(Context context, int resourceId, int size,
            Bitmap.Config config) {
        if(resourceId == 0) {
            return null;
        }

        BitmapFactory.Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, size);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = config;
        Bitmap cover = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        return cover;
    }

    /**
     * 이미지 파일을 {@link android.graphics#Bitmap}로 디코딩한다.
     * 
     * @param filename 파일 경로
     * @param size 이미지 크기
     * @param config
     * @return 디코딩된 이미지
     * @throws IOException
     * @version 2.0
     */
    public static Bitmap decodingImage(String filename, int size, Bitmap.Config config)
            throws IOException {
        File ori = new File(filename);
        if(!ori.exists()) {
            throw new FileNotFoundException();
        }

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filename, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, size);
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = config;
        options.inPreferQualityOverSpeed = true;

        // API 11 이상에서는 파일에서 비트맵을 읽어올 시 mutable(수정가능하도록) 가져올 수 있게 됨.
        if(Build.VERSION.SDK_INT >= 11) {
            options.inMutable = true;
        }

        // Decode bitmap with inSampleSize set
        return BitmapFactory.decodeFile(filename, options);
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int size) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if(height > size || width > size) {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = (int)Math.ceil((float)height / (float)size);
            final int widthRatio = (int)Math.ceil((float)width / (float)size);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }

        return inSampleSize;
    }

    /**
     * @link android.graphics#Bitmap}을 이미지 파일로 저장한다.
     * @param bitmap 이미지
     * @param filename 이미지 저장 경로
     * @param format
     * @throws IOException
     * @version 2.0
     */
    public static void saveBitmap(Bitmap bitmap, String filename, Bitmap.CompressFormat format)
            throws IOException {
        BufferedOutputStream bos = null;
        try {
            FileOutputStream fos = new FileOutputStream(filename, false);
            bos = new BufferedOutputStream(fos);
            bitmap.compress(format, 100, bos);
            fos.flush();
        } finally {
            if(bos != null) {
                try {
                    bos.close();
                } catch(IOException e) {
                }
            }
        }
    }

    /**
     * 파일이 있는지 여부
     * 
     * @param filename 파일 이름(경로 포함)
     * @return 파일 존재 여부
     */
    public static boolean isExist(String filename) {
        if(TextUtils.isEmpty(filename)) {
            return false;
        }
        File file = new File(filename);
        return file.exists();
    }

    /**
     * url이 network용 url인지 여부 판단
     * 
     * @param url url
     * @return network용 url인지 여부
     */
    public static boolean isNetworkUrl(String url) {
        return url != null && url.startsWith("http");
    }
}
