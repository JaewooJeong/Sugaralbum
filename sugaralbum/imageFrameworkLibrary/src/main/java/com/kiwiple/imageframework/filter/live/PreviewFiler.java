
package com.kiwiple.imageframework.filter.live;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.ImageFormat;
import android.os.Handler;
import android.os.Message;
import androidx.renderscript.RenderScript;

import com.kiwiple.imageframework.filter.Filter;
import com.kiwiple.imageframework.view.CameraFilterView;

public abstract class PreviewFiler {
    protected Filter mFilter = new Filter();
    protected Bitmap mOutputBitmap;
    protected CameraFilterView mOutputView;
    protected OnPhaseCompleteListener mOnPhaseCompleteListener;
    protected int mHeight;
    protected int mWidth;

    protected RenderScript mRS;
    protected Context mContext;

    // rgb to yuv
    protected int[] mRgbData;
    protected byte[] mYuvData;

    protected boolean mPassOriginal = false;
    protected Bitmap mOriginalBitmap;

    public PreviewFiler(RenderScript rs, Context context) {
        mRS = rs;
        mContext = context;
    }

    // rgb to yuv
    protected boolean mIsYuv;
    
    protected boolean mIsRenderScriptMainThread = false;

    /**
     * 필터 처리 결과를 yuv로 Callback 받을지 여부 
     */
    public void setYuv(boolean isYuv) {
        mIsYuv = isYuv;
        if(mIsYuv && mWidth != 0 && mHeight != 0) {
            mYuvData = new byte[mWidth * mHeight * ImageFormat.getBitsPerPixel(ImageFormat.NV21)
                    / 8];
            mRgbData = new int[mWidth * mHeight];
        } else {
            mYuvData = null;
            mRgbData = null;
        }
    }

    public void setPassOriginal(boolean passOriginal) {
        mPassOriginal = passOriginal;
    }

    /**
     * 필터 처리 결과가 diplay될 View를 설정한다.
     */
    public void setImageView(CameraFilterView view) {
        mOutputView = view;
    }

    /**
     * 필터 처리 결과를 Callback 받을 listener를 설정한다.
     */
    public void setOnPhaseCompleteListener(OnPhaseCompleteListener listener) {
        mOnPhaseCompleteListener = listener;
    }
    
    /**
     * 일부 단말에서는 라이브 필터를 온전하게 thread를 이용할 경우 화면 분할이 일어나 어쩔 수 없이 사용해야할 경우 사용.
     * 
     * @param isUseMainThread
     */
    public void setIsRenderScriptMainThread(boolean isUseMainThread) {
        mIsRenderScriptMainThread = isUseMainThread;
    }

    /**
     * 필터 처리 요청 이미지 가로 크기
     */
    public int getWidth() {
        return mWidth;
    }

    /**
     * 필터 처리 요청 이미지 세로 크기
     */
    public int getHeight() {
        return mHeight;
    }

    protected Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            mOutputView.invalidate();
        }
    };

    /**
     * 필터 처리에 필요한 각종 변수를 초기화한다.
     */
    public void reset(int width, int height) {
        mWidth = width;
        mHeight = height;

        if(mIsYuv && mWidth != 0 && mHeight != 0) {
            mYuvData = new byte[mWidth * mHeight * ImageFormat.getBitsPerPixel(ImageFormat.NV21)
                    / 8];
            mRgbData = new int[mWidth * mHeight];
        } else {
            mYuvData = null;
            mRgbData = null;
        }
        if(mOutputBitmap != null && !mOutputBitmap.isRecycled()) {
            mOutputBitmap.recycle();
            mOutputBitmap = null;
        }
        mOutputBitmap = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
        if(mOutputView != null) {
            mOutputView.setFilterImage(mOutputBitmap);
        }
        if(mPassOriginal) {
            if(mOriginalBitmap != null && !mOriginalBitmap.isRecycled()) {
                mOriginalBitmap.recycle();
                mOriginalBitmap = null;
            }
            mOriginalBitmap = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
            mOutputView.setOriginalImage(mOriginalBitmap);
        }
    }

    /**
     * 필터를 설정한다.
     */
    public abstract void setFilterData(Filter filter);

    /**
     * 현재 설정된 필터를 반환한다.
     */
    public Filter getFilterData() {
        return mFilter;
    }

    /**
     * 필터를 적용한다.
     */
    public abstract void execute(byte[] yuv);

    /**
     * 필터를 적용한다.
     */
    public abstract void execute(Bitmap source, Bitmap dest);

    /**
     * 필터를 적용한다.
     */
    public abstract void execute(int[] source, int[] dest);

    /**
     * 필터 처리된 결과를 Callback 받을 interface
     */
    public interface OnPhaseCompleteListener {
        /**
         * 필터 처리가 완료되면 호출된다.

         * @param data {@link #mIsYuv}가 true로 설정되어 있지 않으며 null.
         */
        public void onPhaseComplete(byte[] data);
    }
}
