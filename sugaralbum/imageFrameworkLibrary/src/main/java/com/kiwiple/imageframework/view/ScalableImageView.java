
package com.kiwiple.imageframework.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.kiwiple.imageframework.view.ScalableViewController.OnInvalidateListener;

/**
 * 화면 확대/축소가 가능하도록 구현한 ImageView.
 * [U+Camera>편집>뷰티>스킨 톤]에서 사용
 */
public class ScalableImageView extends ImageView {
    public ScalableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScalableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScalableImageView(Context context) {
        super(context);
    }

    private boolean mScalableTouch = false;
    private ScalableViewController mScalableViewController;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(isInEditMode()) {
            return;
        }

        if(mScalableViewController != null) {
            mScalableViewController.onLayout(changed, left, top, right, bottom);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isEnabled()) {
            return false;
        }
        if(mScalableTouch && mScalableViewController != null) {
            return mScalableViewController.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mScalableViewController != null) {
            canvas.save();
            canvas.concat(mScalableViewController.getScaleMatrix());
        }
        super.onDraw(canvas);
        if(mScalableViewController != null) {
            canvas.restore();
        }
    }

    /**
     * Scale터치 가능 여부 설정
     * 
     * @param scalableTouch Scale터치 가능 여부
     */
    public void setScalableTouch(boolean scalableTouch) {
        mScalableTouch = scalableTouch;
    }

    public void setScalableViewController(ScalableViewController controller) {
        mScalableViewController = controller;
    }

    /**
     * Scale 가능 여부 설정
     * 
     * @param scalable Scale 가능 여부
     */
    public void setScalable(boolean scalable) {
        mScalableTouch = scalable;
        if(scalable) {
            mScalableViewController = new ScalableViewController(getContext());
            mScalableViewController.setOnInvalidateListener(new OnInvalidateListener() {
                @Override
                public void onInvalidate() {
                    invalidate();
                }
            });
        } else if(mScalableViewController != null) {
            mScalableViewController.setOnInvalidateListener(null);
            mScalableViewController = null;
        }
    }
}
