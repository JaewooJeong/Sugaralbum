
package com.kiwiple.imageanalysis.utils;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ExifInterface;

import com.kiwiple.imageanalysis.Global;
import com.kiwiple.imageframework.util.FileUtils;

/**
 * 비트맵 유틸 클래스
 */
public class BitmapUtils {

    /**
     * filename의 비트맵을 비율을 유지한 채 특정 크기로 반환
     * 
     * @param width width
     * @param height height
     * @param filePath 파일 경로
     * @param config BitmapConfig
     * @return 생성된 비트맵
     * @throws IOException
     */
    public static Bitmap decodingThumbnailImage(int width, int height, String filePath,
            Bitmap.Config config) throws IOException {
        // Calculate inSampleSize
        int inSampleSize = 1;
        if(height > Global.THUMBNAIL_SIZE || width > Global.THUMBNAIL_SIZE) {
            // Calculate ratios of height and width to requested height and width
            final int heightRatio = (int)Math.ceil((float)height / (float)Global.THUMBNAIL_SIZE);
            final int widthRatio = (int)Math.ceil((float)width / (float)Global.THUMBNAIL_SIZE);

            // Choose the smallest ratio as inSampleSize value, this will guarantee
            // a final image with both dimensions larger than or equal to the
            // requested height and width.
            inSampleSize = heightRatio > widthRatio ? heightRatio : widthRatio;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = config;

        return BitmapFactory.decodeFile(filePath, options);
    }

    /**
     * 비트맵을 회전시켜 반환한다.
     * 
     * @param orig 원본 비트맵
     * @param rotate 회전 값
     * @param config Bitmap.Config
     * @return 회전된 비트맵
     */
    public static Bitmap rotateBitmap(Bitmap orig, int rotate, Bitmap.Config config) {
        Matrix matrix = new Matrix();

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = config;

        Bitmap b = null;
        if(rotate == 0 || rotate == 180) {
            b = Bitmap.createBitmap(orig.getWidth(), orig.getHeight(), options.inPreferredConfig);
            matrix.postTranslate(0, 0);
        } else if(rotate == 90) {
            b = Bitmap.createBitmap(orig.getHeight(), orig.getWidth(), options.inPreferredConfig);
            matrix.postTranslate((orig.getWidth() - orig.getHeight()) / 2,
                                 -(orig.getHeight() - orig.getWidth()) / 2);
        } else {
            b = Bitmap.createBitmap(orig.getHeight(), orig.getWidth(), options.inPreferredConfig);
            matrix.postTranslate(-(orig.getWidth() - orig.getHeight()) / 2,
                                 (orig.getHeight() - orig.getWidth()) / 2);
        }
        matrix.postRotate(rotate, orig.getWidth() / 2, orig.getHeight() / 2);

        Canvas c = new Canvas(b);
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setDither(true);
        p.setFilterBitmap(true);

        c.drawBitmap(orig, matrix, p);
        return b;
    }

    /**
     * 특정 경로의 비트맵을 원하는 각도로 회전시켜 반환 (0, 90, 180, 270)
     * 
     * @param data 파일 경로
     * @param orientation 원하는 회전 값
     * @param config Bitmap.Config
     * @return 회전된 비트맵
     * @throws IOException
     */
    public static Bitmap getBitmapImage(String data, String orientation, Bitmap.Config config)
            throws IOException {
        Bitmap bmp = FileUtils.decodingImage(data, Global.THUMBNAIL_SIZE, config);
        if(("90".equals(orientation) || "180".equals(orientation) || "270".equals(orientation))
                && bmp != null) {
            int orientationValue = Global.isNullString(orientation) ? 0
                    : Integer.valueOf(orientation);
            bmp = BitmapUtils.rotateBitmap(bmp, orientationValue, config);
        }
        return bmp;
    }

    /**
     * 해당 경로의 사진의 Orientation 값을 가져온다.
     * 
     * @param filename 파일 경로
     * @return Orientation 값
     * @throws IOException
     */
    public static int getImageRotation(String filename) throws IOException {
        ExifInterface exif;
        exif = new ExifInterface(filename);
        int rotate = 0;
        switch(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
            case 1:
                rotate = 0;
                break;
            case 6:
                rotate = 90;
                break;
            case 8:
                rotate = -90;
                break;
            case 3:
                rotate = 180;
                break;
        }
        return rotate;
    }
}
