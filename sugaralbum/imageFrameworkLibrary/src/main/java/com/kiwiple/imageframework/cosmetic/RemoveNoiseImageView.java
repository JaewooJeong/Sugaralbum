
package com.kiwiple.imageframework.cosmetic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.util.AttributeSet;

/**
 * 얼굴 사진의 잡티 제거 기능 지원을 위한BeautyEffectImageView 상속 클래스
 * 
 * @version 2.0
 */
public class RemoveNoiseImageView extends RemoveDarkImageView {
    public RemoveNoiseImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RemoveNoiseImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RemoveNoiseImageView(Context context) {
        super(context);
    }

    @Override
    public Bitmap getTouchBitmap() {
        if(mPoints == null || mPoints.isEmpty()) {
            return null;
        }
        Bitmap touchImage = null;
        Rect dstBound = new Rect();
        // 10: img_brush_01에서 점의 반지름
        // mBrushScale: seekbar로 조절한 브러시 크기
        // 이미지 확대 보기 만큼 축소 시켜 준다.
        float brushRadius = mBrush.getHeight() / 10f * mBrushScale
                * mScalableViewController.getInvertScale();

        if(!mDragMode) {
            dstBound.top = (int)Math.max(0.0f, mEndPoint.y - brushRadius);
            dstBound.bottom = (int)Math.min(mCanvas.getHeight(), mEndPoint.y + brushRadius);
            dstBound.left = (int)Math.max(0.0f, mEndPoint.x - brushRadius);
            dstBound.right = (int)Math.min(mCanvas.getWidth(), mEndPoint.x + brushRadius);

            if(dstBound.width() == 0 || dstBound.height() == 0 || dstBound.left < 0
                    || dstBound.right > mCanvas.getWidth() || dstBound.top < 0
                    || dstBound.bottom > mCanvas.getHeight()) {
                return null;
            }

            Rect srcBound = new Rect();
            srcBound.top = (int)Math.max(0.0f, mEndPoint.y - brushRadius);
            srcBound.bottom = (int)Math.min(mImage.getHeight(), mEndPoint.y + brushRadius);
            srcBound.left = (int)Math.max(0.0f, mEndPoint.x - brushRadius);
            srcBound.right = (int)Math.min(mImage.getWidth(), mEndPoint.x + brushRadius);

            // 터치한 영역 주변 색상 값을 가져온다.
            int[] srcColors = new int[4];
            try {
                srcColors[0] = mImage.getPixel(srcBound.centerX(), srcBound.top);
                srcColors[1] = mImage.getPixel(srcBound.centerX(), srcBound.bottom);
                srcColors[2] = mImage.getPixel(srcBound.left, srcBound.centerY());
                srcColors[3] = mImage.getPixel(srcBound.right, srcBound.centerY());
            } catch(IllegalArgumentException e) {
                // x, y exceed the bitmap's bounds, skip
                return null;
            }
            int tmp;
            for(int i = 0; i < 4; i++) {
                for(int j = i; j < 4; j++) {
                    if(isBright(srcColors[j], srcColors[i])) {
                        tmp = srcColors[i];
                        srcColors[i] = srcColors[j];
                        srcColors[j] = tmp;
                    }
                }
            }
            int centerColor = Color.argb(0x45,
                                         (Color.red(srcColors[1]) + Color.red(srcColors[2])) / 2,
                                         (Color.green(srcColors[1]) + Color.green(srcColors[2])) / 2,
                                         (Color.blue(srcColors[1]) + Color.blue(srcColors[2])) / 2);
            int edgeColor = 0x00ffffff & centerColor;

            touchImage = Bitmap.createBitmap(dstBound.right - dstBound.left, dstBound.bottom
                    - dstBound.top, Config.ARGB_8888);
            Canvas canvas = new Canvas(touchImage);

            // 중심의 alpha 값 0x45, 원의 바깥으로 갈수록 0x00으로 희미해 진다.(효과 영역에 경계선이 생기지 않도록)
            RadialGradient gradient = new RadialGradient(dstBound.width() / 2f,
                                                         dstBound.height() / 2f,
                                                         dstBound.width() / 2f, centerColor,
                                                         edgeColor,
                                                         android.graphics.Shader.TileMode.CLAMP);
            mSkinPaint.reset();
            mSkinPaint.setShader(gradient);
            canvas.drawCircle(dstBound.width() / 2f, dstBound.height() / 2f, dstBound.width() / 2f,
                              mSkinPaint);
        } else {
            dstBound.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
            for(PointF point : mPoints) {
                dstBound.set((int)Math.min(dstBound.left, point.x),
                             (int)Math.min(dstBound.top, point.y),
                             (int)Math.max(dstBound.right, point.x),
                             (int)Math.max(dstBound.bottom, point.y));
            }
            dstBound.left -= brushRadius;
            dstBound.top -= brushRadius;
            dstBound.right += brushRadius;
            dstBound.bottom += brushRadius;

            dstBound.top = (int)Math.max(0.0f, dstBound.top);
            dstBound.bottom = (int)Math.min(mCanvas.getHeight(), dstBound.bottom);
            dstBound.left = (int)Math.max(0.0f, dstBound.left);
            dstBound.right = (int)Math.min(mCanvas.getWidth(), dstBound.right);

            if(dstBound.width() == 0 || dstBound.height() == 0 || dstBound.left < 0
                    || dstBound.right > mCanvas.getWidth() || dstBound.top < 0
                    || dstBound.bottom > mCanvas.getHeight()) {
                return null;
            }

            Rect srcBound = new Rect();
            srcBound.top = (int)Math.max(0.0f, mStartPoint.y - brushRadius);
            srcBound.bottom = (int)Math.min(mImage.getHeight(), mStartPoint.y + brushRadius);
            srcBound.left = (int)Math.max(0.0f, mStartPoint.x - brushRadius);
            srcBound.right = (int)Math.min(mImage.getWidth(), mStartPoint.x + brushRadius);

            int[] srcColors = new int[4];
            try {
                srcColors[0] = mImage.getPixel(srcBound.centerX(), srcBound.top);
                srcColors[1] = mImage.getPixel(srcBound.centerX(), srcBound.bottom);
                srcColors[2] = mImage.getPixel(srcBound.left, srcBound.centerY());
                srcColors[3] = mImage.getPixel(srcBound.right, srcBound.centerY());
            } catch(IllegalArgumentException e) {
                // x, y exceed the bitmap's bounds, skip
                return null;
            }
            int tmp;
            for(int i = 0; i < 4; i++) {
                for(int j = i; j < 4; j++) {
                    if(isBright(srcColors[j], srcColors[i])) {
                        tmp = srcColors[i];
                        srcColors[i] = srcColors[j];
                        srcColors[j] = tmp;
                    }
                }
            }
            // 좌우위아래 4가지 색상 값 중 중간 2개 생생 값의 평균
            int centerColor = Color.argb(0x45,
                                         (Color.red(srcColors[1]) + Color.red(srcColors[2])) / 2,
                                         (Color.green(srcColors[1]) + Color.green(srcColors[2])) / 2,
                                         (Color.blue(srcColors[1]) + Color.blue(srcColors[2])) / 2);
            int edgeColor = 0x00ffffff & centerColor;

            touchImage = Bitmap.createBitmap(dstBound.right - dstBound.left, dstBound.bottom
                    - dstBound.top, Config.ARGB_8888);
            Canvas canvas = new Canvas(touchImage);

            mSkinPaint.reset();
            mSkinPaint.setStyle(Style.STROKE);
            mSkinPaint.setPathEffect(new CornerPathEffect(brushRadius));
            mSkinPaint.setStrokeCap(Cap.ROUND);
            mSkinPaint.setStrokeJoin(Join.ROUND);
            mSkinPaint.setMaskFilter(new BlurMaskFilter(brushRadius / 2,
                                                   BlurMaskFilter.Blur.NORMAL));
            mSkinPaint.setStrokeWidth(brushRadius);
            mSkinPaint.setColor(centerColor);
            
            Path path = null;
            for(PointF point : mPoints) {
                if(path == null) {
                    path = new Path();
                    path.moveTo(point.x - dstBound.left, point.y - dstBound.top);
                } else {
                    path.lineTo(point.x - dstBound.left, point.y - dstBound.top);
                }
            }
            canvas.drawPath(path, mSkinPaint);
        }
        // undo 되어 있는 상태일 경우 mCurrentIndex 이후의 객체는 모두 삭제한다.
        if(touchImage != null) {
            for(int i = 0; i < queue.size();) {
                if(i <= mCurrentIndex) {
                    i++;
                } else {
                    pointqueue.remove(i);
                    queue.remove(i);
                }
            }
            pointqueue.add(dstBound);
            queue.add(touchImage);

            mCurrentIndex = queue.size() - 1;
        }
        return touchImage;
    }

    private boolean isBright(int color1, int color2) {
        return Color.red(color1) + Color.green(color1) + Color.blue(color1) > Color.red(color2)
                + Color.green(color2) + Color.blue(color2);
    }
}
