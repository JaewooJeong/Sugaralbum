package com.sugarmount.sugarcamera.story.utils;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.sugarmount.sugaralbum.R;

public class WaveProgress extends View {
    private Bitmap mMaskImage;
    private Bitmap mResult;
    private Canvas mResultCanvas;
    private Rect mMaskImageRect;
    private Rect mViewRect;
    private Bitmap mWave1;
    private Bitmap mWave2;

    private Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private Paint mPaintDstIn = new Paint(Paint.FILTER_BITMAP_FLAG);

    private ObjectAnimator mWaveAnimation;

    private float mProgress = 0;
    private float mWaveProgress = 0;

    public WaveProgress(Context context) {
        super(context);
//        init();
    }

    public WaveProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
//        init();
    }

    public WaveProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        init();
    }

    private void init() {
        mMaskImage = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.popup_animation_mask_rectangle);
        mWave1 = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.popup_animation_3);
        mWave2 = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.popup_animation_4);
        mResult = Bitmap.createBitmap(mMaskImage.getWidth(), mMaskImage.getHeight(), Bitmap.Config.ARGB_8888);
        mResultCanvas = new Canvas(mResult);

        mMaskImageRect = new Rect(0, 0, mMaskImage.getWidth(), mMaskImage.getHeight());

        mPaintDstIn.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopProgressAnimation();

        if(mMaskImage != null){
            mMaskImage.recycle();
            mMaskImage = null;
        }
        if(mWave1 != null){
            mWave1.recycle();
            mWave1 = null;
        }
        if(mWave2 != null){
            mWave2.recycle();
            mWave2 = null;
        }
        if(mResult != null){
            mResult.recycle();
            mResult = null;
        }

        System.gc();
    }

    public void startProgressAnimation() {
        if(mMaskImage == null)
            init();

        mWaveAnimation = ObjectAnimator.ofFloat(this, "waveProgress", 0f, 100f);
        mWaveAnimation.setInterpolator(new LinearInterpolator());
        mWaveAnimation.setDuration(10000);
        mWaveAnimation.setRepeatMode(ValueAnimator.RESTART);
        mWaveAnimation.setRepeatCount(ValueAnimator.INFINITE);
        mWaveAnimation.start();
    }

    public void stopProgressAnimation() {
        if (mWaveAnimation != null) {
            mWaveAnimation.end();
            mWaveAnimation = null;
        }
    }

    public void setProgress(float progress) {
        mProgress = progress;
        invalidate();
    }

    public float getProgress() {
        return mProgress;
    }

    public void setWaveProgress(float waveProgress) {
        mWaveProgress = waveProgress;
        invalidate();
    }

    private float getWaveProgress() {
        return mWaveProgress;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mViewRect = new Rect(0, 0, getWidth(), getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (mMaskImage != null && mViewRect != null) {
            //You can change original image here and draw anything you want to be masked on it.

            mResultCanvas.drawColor(Color.WHITE);
            // TODO: Rect 매번 생성하지 않고 재활용 할 수 있도록 수정
            mResultCanvas.drawBitmap(mWave1, getWave1SrcRect(), getWaveDstRect(), mPaint);
            mResultCanvas.drawBitmap(mWave2, getWave2SrcRect(), getWaveDstRect(), mPaint);
            mResultCanvas.drawBitmap(mMaskImage, 0, 0, mPaintDstIn);

            //Draw result after performing masking
            canvas.drawBitmap(mResult, mMaskImageRect, mViewRect, mPaint);
        }
    }

    private Rect getWave1SrcRect() {
        int left = (int) ((mWave1.getWidth() - mWave1.getHeight()) / 100f * getWaveProgress());
        int bottom = (int) (mWave1.getHeight() / 100 * getProgress());
        return new Rect(left, 0, left + mWave1.getHeight(), bottom);
    }

    private Rect getWave2SrcRect() {
        int left = (int) ((mWave2.getWidth() - mWave2.getHeight()) / 100f * (100 - getWaveProgress()));
        int bottom = (int) (mWave2.getHeight() / 100 * getProgress());
        return new Rect(left, 0, left + mWave2.getHeight(), bottom);
    }

    private Rect getWaveDstRect() {
        int top = (int) (mMaskImageRect.height() / 100 * (100 - getProgress()));
        return new Rect(mMaskImageRect.left, top, mMaskImageRect.right, mMaskImageRect.bottom);
    }
}
