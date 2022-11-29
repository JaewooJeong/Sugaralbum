
package com.kiwiple.imageframework.filter.live;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import com.kiwiple.imageframework.filter.CurvesPoint;
import com.kiwiple.imageframework.filter.Filter;

public class NativeUtils {
    private static short[] points;

    public static short[] getCurveData(Filter filter, String CURVES_TYPE) {
        ArrayList<CurvesPoint> curvesPoints = filter.getCurvesPoints(CURVES_TYPE);

        if(curvesPoints == null || curvesPoints.size() == 0) {
            points = new short[] {
                    0, 0, 255, 255
            };
        } else {
            int size = curvesPoints.size();
            points = new short[size * 2];
            int arrayIndex = 0;
            for(int i = 0; i < size; i++) {
                CurvesPoint cp = curvesPoints.get(i);
                points[arrayIndex++] = cp.mX;
                points[arrayIndex++] = cp.mY;
            }
        }
        return points;
    }

    private static Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG
            | Paint.ANTI_ALIAS_FLAG);

    public static void applyImage(Bitmap ori, Bitmap cover, int alpha, PorterDuff.Mode mode) {
        int w = ori.getWidth();
        int h = ori.getHeight();

        BitmapFactory.Options op = new Options();
        op.inPreferredConfig = Config.ARGB_8888;

        Canvas c = new Canvas(ori);

        mPaint.setAlpha(alpha);
        if(mode != null) {
            mPaint.setXfermode(new PorterDuffXfermode(mode));
        } else {
            mPaint.setXfermode(null);
        }

        c.drawBitmap(cover, new Rect(0, 0, cover.getWidth(), cover.getHeight()), new Rect(0, 0, w,
                                                                                          h),
                     mPaint);
    }

    static int frameSize;
    static int uvp, u, v;
    static int i, j, yp;
    static int y1192, r, g, b;

    static int[] decodeYUV420SP(int[] pixels, byte[] yuv420sp, int width, int height) {
        if(pixels == null || pixels.length != width * height) {
            pixels = new int[width * height];
        }
        frameSize = width * height;

        for(j = 0, yp = 0; j < height; j++) {
            uvp = frameSize + (j >> 1) * width;
            u = 0;
            v = 0;
            for(i = 0; i < width; i++, yp++) {
                int y = (0xff & yuv420sp[yp]) - 16;
                if(y < 0)
                    y = 0;
                if((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                y1192 = 1192 * y;
                r = (y1192 + 1634 * v);
                g = (y1192 - 833 * v - 400 * u);
                b = (y1192 + 2066 * u);

                if(r < 0)
                    r = 0;
                else if(r > 262143)
                    r = 262143;
                if(g < 0)
                    g = 0;
                else if(g > 262143)
                    g = 262143;
                if(b < 0)
                    b = 0;
                else if(b > 262143)
                    b = 262143;

                pixels[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00)
                        | ((b >> 10) & 0xff);
            }
        }
        return pixels;
    }
}
