
package com.kiwiple.imageframework.cosmetic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

/**
 * 이미지의 일부분을 확대하여 보여주기 위한 class
 * 
 * @version 2.0
 */
public class ZoomImageView extends View {
    private Bitmap mCropImage;
    private boolean mLeftZone = true;
    protected RectF mLeftZoomArea;
    protected RectF mRightZoomArea;

    protected Paint mBrushPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    protected Paint mOutlinePaint;

    private float mDensity;

    private Animation mFadeOutAnimation;

    public ZoomImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public ZoomImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ZoomImageView(Context context) {
        super(context);
        init();
    }

    private void init() {
        if(mOutlinePaint == null) {
            mOutlinePaint = new Paint();
            mOutlinePaint.setStyle(Style.STROKE);
            mOutlinePaint.setColor(Color.WHITE);
            mOutlinePaint.setStrokeWidth(1.5f * getResources().getDisplayMetrics().density);
        }
        mFadeOutAnimation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        mFadeOutAnimation.setAnimationListener(new AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(mIsAnimating) {
                    mIsAnimating = false;
                    mCropImage = null;
                    invalidate();
                }
            }
        });
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(isInEditMode()) {
            return;
        }
        mDensity = getResources().getDisplayMetrics().density;

        // 좌/우 확대 영역의 마진
        float topMargin = 29.5f * mDensity;
        float horizontalMargin = 5 * mDensity;

        // 좌측 확대 영역
        if(mLeftZoomArea == null) {
            mLeftZoomArea = new RectF();
        }
        mLeftZoomArea.left = horizontalMargin;
        mLeftZoomArea.top = topMargin;
        mLeftZoomArea.right = mLeftZoomArea.left + 150f * mDensity;
        mLeftZoomArea.bottom = mLeftZoomArea.top + 150f * mDensity;

        // 우측 확대 영역
        if(mRightZoomArea == null) {
            mRightZoomArea = new RectF();
        }
        mRightZoomArea.right = (right - left) - horizontalMargin;
        mRightZoomArea.left = mRightZoomArea.right - 150f * mDensity;
        mRightZoomArea.top = topMargin;
        mRightZoomArea.bottom = mRightZoomArea.top + 150f * mDensity;
    }

    /**
     * 현재 터치 좌표를 기준으로 표시 영역을 계산한다.
     * 
     * @param x 현재 터치 좌표의 x 값
     * @param y 현재 터치 좌표의 y 값
     * @version 2.0
     */
    public void setLeftZoom(float x, float y) {
        mLeftZone = mLeftZoomArea != null && !mLeftZoomArea.contains(x, y);
    }

    /**
     * 애니메이션이 반복적으로 호출되는 현상을 방지하기 위한 변수.
     */
    private boolean mIsAnimating = false;

    /**
     * 확대 표시할 이미지를 설정한다.
     * 
     * @param image 확대 표시한 이미지
     * @version 2.0
     */
    public void setCropImage(final Bitmap image) {
        if(image == null && mCropImage != null && !mIsAnimating) {
            mIsAnimating = true;
            setAnimation(mFadeOutAnimation);
            // 1초 후에 애니메이션 시작
            mFadeOutAnimation.setStartTime(AnimationUtils.currentAnimationTimeMillis() + 1000);
        } else if(image != null) {
            mIsAnimating = false;
            clearAnimation();
            mCropImage = image;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawMirror(canvas);
    }

    private void drawMirror(Canvas canvas) {
        if(mCropImage != null) {
            if(mLeftZone) {
                canvas.drawBitmap(mCropImage, null, mLeftZoomArea, mBrushPaint);
                canvas.drawRect(mLeftZoomArea, mOutlinePaint);
            } else if(mRightZoomArea != null) {
                canvas.drawBitmap(mCropImage, null, mRightZoomArea, mBrushPaint);
                canvas.drawRect(mRightZoomArea, mOutlinePaint);
            }
        }
    }
}
