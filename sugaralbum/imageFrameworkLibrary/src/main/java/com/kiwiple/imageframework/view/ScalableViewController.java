
package com.kiwiple.imageframework.view;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * ScalableImageView 관련 설정 클래스
 */
public class ScalableViewController {
    private ScaleGestureDetector mScaleDetector;
    private Matrix mMatrix = new Matrix();
    private Matrix mInvertedMatrix = new Matrix();
    private float mScaleFactor = 1.f;
    private PointF mPivot = new PointF(0, 0);
    private PointF mTranslate = new PointF(0, 0);
    private PointF mTranslateMax = new PointF(0, 0);

    private boolean mInitialized = false;
    private int mWidth;
    private int mHeight;
    private OnInvalidateListener mOnInvalidateListener;

    public ScalableViewController(Context context) {
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mWidth = right - left;
        mHeight = bottom - top;
        mInitialized = true;
    }

    /**
     * 크기 설정
     * 
     * @param width 가로
     * @param height 세로
     */
    public void setSize(int width, int height) {
        mWidth = width;
        mHeight = height;
        mInitialized = true;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        if(!mInitialized) {
            return false;
        }
        // Let the ScaleGestureDetector inspect all events.
        boolean retVal = mScaleDetector.onTouchEvent(ev);

        switch(ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(mScaleFactor <= 1f) {
                    ObjectAnimator scaleAnim = ObjectAnimator.ofFloat(this, "scale", mScaleFactor,
                                                                      1.0f);
                    ObjectAnimator translateXAnim = ObjectAnimator.ofFloat(this, "translateX",
                                                                           mTranslate.x, 0.0f);
                    ObjectAnimator translateYAnim = ObjectAnimator.ofFloat(this, "translateY",
                                                                           mTranslate.y, 0.0f);
                    AnimatorSet animatorSet = new AnimatorSet();
                    animatorSet.playTogether(scaleAnim, translateXAnim, translateYAnim);
                    animatorSet.start();
                }
                setPivotX(0);
                setPivotY(0);
                break;
            case MotionEvent.ACTION_MOVE:
                if(ev.getPointerCount() > 1) {
                    if(mScaleFactor >= 1.f) {
                        float currentCenterX = (ev.getX(0) + ev.getX(1)) / 2.f;
                        float currentCenterY = (ev.getY(0) + ev.getY(1)) / 2.f;

                        if(mPivot.x != 0 && mPivot.y != 0) {
                            translate(currentCenterX - mPivot.x, currentCenterY - mPivot.y);
                            retVal = true;
                        }

                        setPivotX(currentCenterX);
                        setPivotY(currentCenterY);
                    }
                }
                break;
        }
        if(retVal) {
            invalidate();
        }
        return retVal;
    }

    /**
     * 크기 설정
     * 
     * @param scale 크기
     */
    public void setScale(float scale) {
        if(!mInitialized) {
            return;
        }
        mScaleFactor = scale;
        mMatrix.setScale(mScaleFactor, mScaleFactor, mPivot.x, mPivot.y);
        mTranslateMax.set(mWidth * (mScaleFactor - 1) / 2f, mHeight * (mScaleFactor - 1) / 2f);
        invalidate();
    }

    /**
     * Pivot의 x값 설정
     * 
     * @param x x값
     */
    public void setPivotX(float x) {
        if(!mInitialized) {
            return;
        }
        mPivot.x = x;
    }

    /**
     * Pivot의 y값 설정
     * 
     * @param y y값
     */
    public void setPivotY(float y) {
        if(!mInitialized) {
            return;
        }
        mPivot.y = y;
    }

    float[] mMatrixValues = new float[9];

    /**
     * 이동 설정
     * 
     * @param x x 이동 거리
     * @param y y 이동 거리
     */
    public void translate(float x, float y) {

        if(!mInitialized) {
            return;
        }
        mMatrix.getValues(mMatrixValues);
        if(mScaleFactor > 1 && mMatrixValues[Matrix.MTRANS_X] + x < -mTranslateMax.x) {
            x = -(mMatrixValues[Matrix.MTRANS_X] + mTranslateMax.x);
        } else if(mScaleFactor > 1 && mMatrixValues[Matrix.MTRANS_X] + x > 0) {
            x = -mMatrixValues[Matrix.MTRANS_X];
        }
        if(mScaleFactor > 1 && mMatrixValues[Matrix.MTRANS_Y] + y < -mTranslateMax.y) {
            y = -(mMatrixValues[Matrix.MTRANS_Y] + mTranslateMax.y);
        } else if(mScaleFactor > 1 && mMatrixValues[Matrix.MTRANS_Y] + y > 0) {
            y = -mMatrixValues[Matrix.MTRANS_Y];
        }
        mTranslate.x += x;
        mTranslate.y += y;
        mMatrix.postTranslate(x, y);
        Log.d("test", "Translate x: " + mTranslate.x + ", y: " + mTranslate.y);
    }

    /**
     * x축 이동 설정
     * 
     * @param x x 이동 거리
     */
    public void setTranslateX(float x) {
        if(!mInitialized) {
            return;
        }
        mTranslate.x = x;
        mMatrix.setTranslate(mTranslate.x, mTranslate.y);
        invalidate();
    }

    /**
     * y축 이동 설정
     * 
     * @param y y 이동 거리
     */
    public void setTranslateY(float y) {
        if(!mInitialized) {
            return;
        }
        mTranslate.y = y;
        mMatrix.setTranslate(mTranslate.x, mTranslate.y);
        invalidate();
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if(mScaleFactor * detector.getScaleFactor() > 1.0f
                    && mScaleFactor * detector.getScaleFactor() < 5.0f) {
                mScaleFactor *= detector.getScaleFactor();

                // Don't let the object get too small or too large.
                // mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
                mMatrix.preScale(detector.getScaleFactor(), detector.getScaleFactor(), mPivot.x,
                                 mPivot.y);

                mTranslateMax.set(mWidth * (mScaleFactor - 1), mHeight * (mScaleFactor - 1));
                translate(0, 0);
            }
            return true;
        }
    }

    public void invalidate() {
        if(mOnInvalidateListener != null) {
            mOnInvalidateListener.onInvalidate();
        }
    }

    /**
     * OnInvalidateListener 설정
     * 
     * @param listener OnInvalidateListener
     */
    public void setOnInvalidateListener(OnInvalidateListener listener) {
        mOnInvalidateListener = listener;
    }

    /**
     * Scale Matrix 반환
     * 
     * @return Scale Matrix
     */
    public Matrix getScaleMatrix() {
        return mMatrix;
    }

    /**
     * Scale Invert Matrix 반환
     * 
     * @return Scale Invert Matrix
     */
    public Matrix getInvertedMatrix() {
        mMatrix.invert(mInvertedMatrix);
        return mInvertedMatrix;
    }

    /**
     * 크기 값 반환. Default 1.0
     * 
     * @return 크기 값
     */
    public float getScale() {
        return mScaleFactor;
    }

    /**
     * 크기 값의 역수 반환.
     * 
     * @return 크기 값의 역수
     */
    public float getInvertScale() {
        return 1.f / mScaleFactor;
    }

    public interface OnInvalidateListener {
        public void onInvalidate();
    }
}
