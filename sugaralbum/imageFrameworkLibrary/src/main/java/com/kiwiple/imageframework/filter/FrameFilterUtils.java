
package com.kiwiple.imageframework.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;

import com.kiwiple.imageframework.util.FileUtils;

public class FrameFilterUtils {
    private static Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    /**
     * 상/하단 프레임을 적용한다.
     * 
     * @param ori 원본 이미지
     * @param data 상/하단 프레임 정보
     */
    public static void applyFrame(Context context, Bitmap ori, FilterData data) {
        if(data.mFilter.neetTopFrame() && !data.mFilter.isLightArtFilter()) {
            Bitmap frameBitmap = null;
            // 가로/세로 이미지가 나뉘어져 있다.
            if(ori.getHeight() < ori.getWidth()) {
                frameBitmap = BitmapFactory.decodeResource(context.getResources(),
                                                           FileUtils.getBitmapResourceId(context,
                                                                                         data.mFilter.mTopFrameName
                                                                                                 + "_w"),
                                                           null);
            }
            if(frameBitmap == null) {
                frameBitmap = BitmapFactory.decodeResource(context.getResources(),
                                                           FileUtils.getBitmapResourceId(context,
                                                                                         data.mFilter.mTopFrameName),
                                                           null);
            }

            if(frameBitmap != null) {
                applyTopFrame(ori, frameBitmap, 255, null);
                frameBitmap = null;
            } else {
            }
        }
        if(data.mFilter.neetBottomFrame() && !data.mFilter.isLightArtFilter()) {
            Bitmap frameBitmap = null;
            // 가로/세로 이미지가 나뉘어져 있다.
            if(ori.getHeight() < ori.getWidth()) {
                frameBitmap = BitmapFactory.decodeResource(context.getResources(),
                                                           FileUtils.getBitmapResourceId(context,
                                                                                         data.mFilter.mBottomFrameName
                                                                                                 + "_w"),
                                                           null);
            }
            if(frameBitmap == null) {
                frameBitmap = BitmapFactory.decodeResource(context.getResources(),
                                                           FileUtils.getBitmapResourceId(context,
                                                                                         data.mFilter.mBottomFrameName),
                                                           null);
            }

            if(frameBitmap != null) {
                applyBottomFrame(ori, frameBitmap, 255, null);
                frameBitmap = null;
            } else {
            }
        }
    }

    /**
     * 두개의 이미지를 합성한다.
     * 
     * @param ori 원본 이미지
     * @param cover 합성할 이미지
     * @param alpha 합성할 이미지의 투명도
     * @param mode 합성 방법
     */
    private static void applyTopFrame(Bitmap ori, Bitmap cover, int alpha, PorterDuff.Mode mode) {
        int w = ori.getWidth();

        BitmapFactory.Options op = new Options();
        op.inPreferredConfig = Config.ARGB_8888;

        Canvas c = new Canvas(ori);

        mPaint.setAlpha(alpha);
        if(mode != null) {
            mPaint.setXfermode(new PorterDuffXfermode(mode));
        } else {
            mPaint.setXfermode(null);
        }

        c.drawBitmap(cover, null,
                     new Rect(0, 0, w, (int)(cover.getHeight() * (w / (float)cover.getWidth()))),
                     mPaint);

        cover.recycle();
        cover = null;
    }

    /**
     * 두개의 이미지를 합성한다.
     * cover 이미지를 ori 이미지 상단에 꽉차도록 배치한다.
     * 
     * @param ori 원본 이미지
     * @param cover 합성할 이미지
     * @param alpha 합성할 이미지의 투명도
     * @param mode 합성 방법
     */
    private static void applyBottomFrame(Bitmap ori, Bitmap cover, int alpha, PorterDuff.Mode mode) {
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

        c.drawBitmap(cover,
                     null,
                     new Rect(0, h - (int)(cover.getHeight() * (w / (float)cover.getWidth())), w, h),
                     mPaint);

        cover.recycle();
        cover = null;
    }
}
