
package com.kiwiple.imageframework.burstshot;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.kiwiple.imageframework.util.thread.PoolWorkerRunnable;
import com.kiwiple.imageframework.util.thread.WorkQueue;

/**
 * jpeg 파일 리스트를 animated gif 형식으로 보여주는 class
 * 
 * @version 2.0
 */
public class BurstShotImageView extends View {
    private int mIndex = 0;
    private boolean mProgress = false;
    private Bitmap mImage;
    private Matrix mDrawMatrix;
    private TextView mCurrentFrame;

    public BurstShotImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public BurstShotImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public BurstShotImageView(Context context) {
        super(context);
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawMatrix = null;
    }

    /**
     * 현재 재생 중인 jpeg 파일 index를 표시해 주는 {@link android.widget#TextView}를 설정한다.
     * 
     * @param currentFrame index가 표시될 {@link android.widget#TextView}
     * @version 2.0
     */
    public void setCurrentFrameTextView(TextView currentFrame) {
        mCurrentFrame = currentFrame;
    }

    /**
     * jpeg 파일 리스트를 초기화 한다.
     * 
     * @version 2.0
     */
    public void init() {
        mIndex = 0;
        WorkQueue.getInstance().execute(new InitializeImageRunnable());
    }

    /**
     * jpeg 파일 리스트의 재생을 중지한다.
     * 
     * @version 2.0
     */
    public void stopAnimation() {
        pause();
        mImage = null;
    }

    /**
     * jpeg 파일 리스트의 재생을 일시 중지한다.
     * 
     * @version 2.0
     */
    public void pause() {
        mProgress = false;
    }

    /**
     * jpeg 파일 리스트의 재생을 시작한다.
     * 
     * @version 2.0
     */
    public void resume() {
        mProgress = true;
        WorkQueue.getInstance().execute(new InvalidateImageRunnable());
    }

    /**
     * 첫 번째 jpeg 파일부터 다시 재생하도록 설정한다.
     * 
     * @version 2.0
     */
    public void reset() {
        mIndex = 0;
        mCurrentFrame.setText(String.valueOf(1));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAnimation();
    }

    private class InitializeImageRunnable extends PoolWorkerRunnable {
        public InitializeImageRunnable() {
            super(null);
        }

        @Override
        public void run() {
            while(true) {
                if(getWidth() != 0 || getHeight() != 0) {
                    break;
                }
            }
            mImage = BurstShotManager.getInstance(getContext().getApplicationContext())
                                     .getSelectedImage(0, Math.max(getWidth(), getHeight()));
            postInvalidate();
        }
    }

    private class InvalidateImageRunnable extends PoolWorkerRunnable {
        private long mLastUpdateTime = 0;

        public InvalidateImageRunnable() {
            super(-1);
        }

        @Override
        public void run() {
            int currentIndex = -1;
            while(true) {
                if(!mProgress) {
                    break;
                }
                if(getWidth() == 0 || getHeight() == 0) {
                    continue;
                }
                // TODO: 방어 코드? 중복 체크? 확인 필요.
                if(mIndex >= BurstShotManager.getInstance(getContext().getApplicationContext())
                                             .getSelectedJpgCount()) {
                    mIndex = 0;
                }
                if(currentIndex != mIndex) {
                    currentIndex = mIndex;
                    mImage = BurstShotManager.getInstance(getContext().getApplicationContext())
                                             .getSelectedImage(currentIndex,
                                                               Math.max(getWidth(), getHeight()));
                }
                if(mLastUpdateTime != 0
                        && System.currentTimeMillis() - mLastUpdateTime < BurstShotManager.sCurrentInterval) {
                    continue;
                }
                mIndex++;
                // 마지막 프레임까지 재생 완료되면 처음으로 되돌린다.
                if(mIndex >= BurstShotManager.getInstance(getContext().getApplicationContext())
                                             .getSelectedJpgCount()) {
                    mIndex = 0;
                }
                mLastUpdateTime = System.currentTimeMillis();
                postInvalidate();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(mImage != null) {
            if(mDrawMatrix == null) {
                RectF mTempSrc = new RectF();
                RectF mTempDst = new RectF();
                mTempSrc.set(0, 0, mImage.getWidth(), mImage.getHeight());
                mTempDst.set(0, 0, getWidth(), getHeight());

                mDrawMatrix = new Matrix();
                mDrawMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.CENTER);
            }
            canvas.drawBitmap(mImage, mDrawMatrix, null);
            // TODO: TextView를 직접 핸들링 하지 않고, InvalidateImageRunnable에서 프레임이 넘어가면 Callback 메서드를 호출해 주는 형태로 변경.
            if(mCurrentFrame != null) {
                mCurrentFrame.setText(String.valueOf(mIndex + 1));
            }
        }
    }
}
