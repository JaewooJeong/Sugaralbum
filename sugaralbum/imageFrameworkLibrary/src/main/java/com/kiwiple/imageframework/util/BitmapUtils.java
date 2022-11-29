
package com.kiwiple.imageframework.util;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.media.ExifInterface;

import com.kiwiple.imageframework.util.ImageDownloadManager.ImageDownloadManagerListener;

/**
 * {@link android.graphics#Bitmap} 관련 기능을 제공하는 클래스
 * 
 * @version 2.0
 */
public class BitmapUtils {
    private static final String TAG = "BitmapUtils";
    private static final int IO_BUFFER_SIZE = 8192;

    public static boolean DownloadBitmap(String url, String local,
            ImageDownloadManagerListener listener, int retryCount) {
        InputStream in = null;
        FileOutputStream fout = null;
        File wfile = null;
        URLConnection connection = null;
        SmartLog.i(TAG, "Request image download : " + url);
        try {
            // image download
            connection = new URL(url).openConnection();
            connection.setConnectTimeout(retryCount * 2000);
            connection.setDoInput(true);
            connection.connect();

            in = new BufferedInputStream(connection.getInputStream(), IO_BUFFER_SIZE);
            wfile = new File(local);
            fout = new FileOutputStream(wfile, false);

            copy(in, fout, connection, listener);
            fout.flush();
        } catch(Exception e) {
            SmartLog.e(TAG, "Could not download Bitmap from: " + url, e);
            return false;
        } finally {
            closeStream(in);
            closeStream(fout);
            if(connection != null) {
                if(connection instanceof HttpURLConnection) {
                    ((HttpURLConnection)connection).disconnect();
                }
            }
        }
        return true;
    }

    private static void closeStream(Closeable stream) {
        if(stream != null) {
            try {
                stream.close();
            } catch(IOException e) {
            }
        }
    }

    private static void copy(InputStream in, OutputStream out, URLConnection downloadStream,
            ImageDownloadManagerListener listener) throws IOException {
        int totalByte = downloadStream.getContentLength();
        int currentByte = 0;
        byte[] b = new byte[IO_BUFFER_SIZE];
        int read;
        while((read = in.read(b)) > 0) {
            out.write(b, 0, read);
            currentByte += read;
            if(listener != null) {
                listener.progressDownload((int)(currentByte / (float)totalByte * 100));
            }
        }
    }

    /**
     * 이미지 크기를 변경한다.
     * 
     * @param orig 원본 이미지
     * @param width 결과 이미지의 가로 길이
     * @param height 결과 이미지의 세로 길이
     * @return 가로/세로 길이가 변경된 이미지
     * @version 2.0
     */
    public static Bitmap resizeBitmap(Bitmap orig, int width, int height) {
        Bitmap b = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        Canvas c = new Canvas(b);
        Paint p = new Paint();
        p.setFilterBitmap(true);

        c.drawBitmap(orig, new Rect(0, 0, orig.getWidth(), orig.getHeight()), new Rect(0, 0, width,
                                                                                       height), p);
        return b;
    }

    /**
     * 이미지를 회전 시킨다.
     * 
     * @param orig 원본 이미지
     * @param rotate 회전 각도
     * @return 회전된 이미지
     * @version 2.0
     */
    public static Bitmap rotateBitmap(Bitmap orig, int rotate) {
        Matrix matrix = new Matrix();

        Bitmap b;
        if(rotate == 0 || rotate == 180) {
            b = Bitmap.createBitmap(orig.getWidth(), orig.getHeight(), Config.ARGB_8888);
            matrix.postTranslate(0, 0);
        } else if(rotate == 90) {
            b = Bitmap.createBitmap(orig.getHeight(), orig.getWidth(), Config.ARGB_8888);
            matrix.postTranslate((orig.getWidth() - orig.getHeight()) / 2,
                                 -(orig.getHeight() - orig.getWidth()) / 2);
        } else {
            b = Bitmap.createBitmap(orig.getHeight(), orig.getWidth(), Config.ARGB_8888);
            matrix.postTranslate(-(orig.getWidth() - orig.getHeight()) / 2,
                                 (orig.getHeight() - orig.getWidth()) / 2);
        }
        matrix.postRotate(rotate, orig.getWidth() / 2, orig.getHeight() / 2);

        Canvas c = new Canvas(b);
        Paint p = new Paint();
        p.setFilterBitmap(true);

        c.drawBitmap(orig, matrix, p);
        return b;
    }

    /**
     * 이미지의 크기를 변경하고 회전 시킨다.
     * 
     * @param orig 원본 이미지
     * @param rotate 회전 각도
     * @param width 결과 이미지의 가로 길이
     * @param height 결과 이미지의 세로 길이
     * @return 가로/세로 길이가 변경되고 회전된 이미지
     * @version 2.0
     */
    public static Bitmap resizeWithRotateBitmap(Bitmap orig, int rotate, int width, int height) {
        Bitmap b;
        if(rotate == 0 || rotate == 180) {
            b = Bitmap.createBitmap(orig.getWidth(), orig.getHeight(), Config.ARGB_8888);
        } else if(rotate == 90) {
            b = Bitmap.createBitmap(orig.getHeight(), orig.getWidth(), Config.ARGB_8888);
        } else {
            b = Bitmap.createBitmap(orig.getHeight(), orig.getWidth(), Config.ARGB_8888);
        }

        Canvas c = new Canvas(b);
        c.rotate(rotate, b.getWidth() / 2, b.getHeight() / 2);
        Paint p = new Paint();
        p.setFilterBitmap(true);

        c.drawBitmap(orig, new Rect(0, 0, orig.getWidth(), orig.getHeight()), new Rect(0, 0, width,
                                                                                       height), p);
        return b;
    }

    /**
     * 원본 이미지를 target 크기로 변경하기 위한 배율을 구함 (가로)
     * 
     * @param original 원본 이미지
     * @param target target 크기
     * @return 배율 값
     */
    public static int getRatioBitmapWidth(Bitmap original, int target) {
        float xRatio = 1.0f;
        if(original.getWidth() < original.getHeight()) {
            xRatio = original.getWidth() / (float)original.getHeight();
        }
        return (int)(target * xRatio);
    }

    /**
     * 원본 이미지를 target 크기로 변경하기 위한 배율을 구함 (세로)
     * 
     * @param original 원본 이미지
     * @param target target 크기
     * @return 배율 값
     */
    public static int getRatioBitmapHeight(Bitmap original, int target) {
        float yRatio = 1.0f;
        if(original.getWidth() > original.getHeight()) {
            yRatio = original.getHeight() / (float)original.getWidth();
        }
        return (int)(target * yRatio);
    }

    /**
     * 파일 경로에서 Exif 데이터를 읽어 회전 값을 반환한다
     * 
     * @param filename 파일 경로
     * @return 해당 파일의 회전 값
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

    /**
     * 2개의 비트맵을 합성한다
     * 
     * @param src 합성 대상 비트맵
     * @param dst 합성할 비트맵
     * @param dstRect 합성시 영역크기
     */
    public static void mergeImage(Bitmap src, Bitmap dst, Rect dstRect) {
        Canvas c = new Canvas(src);
        Paint p = new Paint();
        p.setFilterBitmap(true);

        c.drawBitmap(dst, dstRect, new Rect(0, 0, src.getWidth(), src.getHeight()), p);
    }

    private static final String DEMO_VERSION_WATER_MARK = " L G   U + ";

    /**
     * WaterMark를 합성한다
     * 
     * @param context
     * @param image 합성할 이미지
     */
    public static void applyWaterMarkImage(Context context, Bitmap image) {
        Canvas canvas = new Canvas(image);
        float density = context == null ? 1.5f : context.getResources().getDisplayMetrics().density;

        Paint paint = new Paint();
        paint.setTextSize(100 * density);
        paint.setColor(Color.argb(0xbb, 0xff, 0xff, 0xff));
        paint.setFakeBoldText(true);
        paint.setStyle(Style.FILL);
        float width = paint.measureText(DEMO_VERSION_WATER_MARK);

        Paint shadowPaint = new Paint(paint);
        shadowPaint.setColor(Color.argb(0xbb, 0x00, 0x00, 0x00));

        canvas.save();
        canvas.scale(image.getWidth() / (width * 1.1f), image.getWidth() / (width * 1.1f),
                     image.getWidth() / 2, image.getHeight() / 2);
        canvas.drawText(DEMO_VERSION_WATER_MARK, (image.getWidth() - width) / 2 + 1 * density,
                        (image.getHeight() + shadowPaint.getTextSize()) / 2 + 1 * density,
                        shadowPaint);
        canvas.drawText(DEMO_VERSION_WATER_MARK, (image.getWidth() - width) / 2,
                        (image.getHeight() + shadowPaint.getTextSize()) / 2, paint);
        canvas.restore();
    }

    /**
     * 비트맵을 복사한다
     * 
     * @param src 복사 대상 비트맵
     * @param dest 복사한 비트맵
     */
    public static void copyBitmap(Bitmap src, Bitmap dest) {
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(src, null, new Rect(0, 0, dest.getWidth(), dest.getHeight()), null);
    }

    /**
     * 썸네일 이미지로 만들기 위한 함수
     * 
     * @param ori 원본 이미지
     * @param size 원하는 크기
     * @return 원하는 크기로 변경된 비트맵
     */
    public static Bitmap createThumbnailImage(Bitmap ori, int size) {
        Bitmap thumbnail = Bitmap.createBitmap(size, size, Config.ARGB_8888);
        Canvas canvas = new Canvas(thumbnail);

        int shortSize = ori.getWidth() < ori.getHeight() ? ori.getWidth() : ori.getHeight();
        Rect originRect = new Rect((ori.getWidth() - shortSize) / 2,
                                   (ori.getHeight() - shortSize) / 2,
                                   (ori.getWidth() + shortSize) / 2,
                                   (ori.getHeight() + shortSize) / 2);
        Rect dsetRect = new Rect(0, 0, thumbnail.getWidth(), thumbnail.getHeight());
        Paint p = new Paint(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(ori, originRect, dsetRect, p);

        return thumbnail;
    }
}
