
package com.kiwiple.imageframework.filter.live;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

public class FilterManagerRenderScript {
    private int mHeight;
    private int mWidth;
    private RenderScript mRS;
    private Allocation mAllocationOut;

    private Context mContext;

    private Phase1Handler mPhase1Handler;

    private HandlerThread mHandlerThread;

    private OnPhaseCompleteListener mOnPhaseCompleteListener;

    public FilterManagerRenderScript(RenderScript rs, Context context) {
        mContext = context;
        mRS = rs;

        mHandlerThread = new HandlerThread("Phase Handler", Process.THREAD_PRIORITY_URGENT_AUDIO);
        mHandlerThread.start();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
        mHandlerThread.setPriority(Thread.MAX_PRIORITY);

        mPhase1Handler = new Phase1Handler(mHandlerThread.getLooper());
        ArtFilterRenderScriptUtils.init(mRS, mContext);
    }

    public void setOnPhaseCompleteListener(OnPhaseCompleteListener listener) {
        mOnPhaseCompleteListener = listener;
    }

    private Bitmap mOutputBitmap;

    private void reset(int width, int height) {
        if(mWidth == width && mHeight == height) {
            return;
        }
        ArtFilterRenderScriptUtils.reset(width, height);
        mPhase1Handler.removeMessages(0);
        mWidth = width;
        mHeight = height;
        mOutputBitmap = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
        android.util.Log.v("cpa", "reset " + mWidth + ", " + mHeight);

        Type.Builder tb;
        if(mAllocationOut != null) {
            mAllocationOut.destroy();
        }
        tb = new Type.Builder(mRS, Element.RGBA_8888(mRS));
        tb.setX(mWidth);
        tb.setY(mHeight);
        mAllocationOut = Allocation.createTyped(mRS, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                                RsYuv.ALLOCATION_USAGE_FULL);
    }

    public void setFilterData(String mCurrentFilter, float[] mSeekBarValue, int width, int height) {
        reset(width, height);

        ArtFilterRenderScriptUtils.initFilter(mContext, mCurrentFilter, mSeekBarValue, width,
                                              height);
    }

    public void execute(Bitmap bitmap) {
        mAllocationOut.copyFrom(bitmap);
        mPhase1Handler.sendEmptyMessage(0);
    }

    private class Phase1Handler extends Handler {
        public Phase1Handler(Looper looper) {
            super(looper);
        }

        public void handleMessage(android.os.Message msg) {
            ArtFilterRenderScriptUtils.processArtFilter(mAllocationOut);

            mAllocationOut.copyTo(mOutputBitmap);
            if(mOnPhaseCompleteListener != null) {
                mOnPhaseCompleteListener.onPhaseComplete(mOutputBitmap);
            }
        }
    }

    public interface OnPhaseCompleteListener {
        public void onPhaseComplete(Bitmap image);
    }
}
