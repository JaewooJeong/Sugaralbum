
package com.kiwiple.imageframework.filter.live;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import androidx.renderscript.RenderScript;
import android.text.TextUtils;

import com.kiwiple.imageframework.filter.CurvesPoint;
import com.kiwiple.imageframework.filter.Filter;
import com.kiwiple.imageframework.filter.NativeImageFilter;
import com.kiwiple.imageframework.gpuimage.ArtFilterManager;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;
import com.kiwiple.imageframework.util.FileUtils;

public class RsYuvNative extends PreviewFiler {
    private Bitmap vignetteBitmap;
    private Bitmap textureBitmap;
    private Bitmap frameBitmap;

    // curve
    private int[] mAllCurves = new int[256];
    private int[] mRCurves = new int[256];
    private int[] mGCurves = new int[256];
    private int[] mBCurves = new int[256];

    private Phase4Handler mPhase4Handler;

    private HandlerThread mHandlerThread;

    private ByteBuffer mOrigByteBuffer;
    private Bitmap mImageBufferRgbFilter;
    private Canvas mCanvas;
    static int[] pixels;
    private Bitmap mImageBufferYuv;
    private ByteBuffer mTextureByteBuffer;

    /**
     * @param rs
     * @param context
     * @version 2.0
     */
    public RsYuvNative(RenderScript rs, Context context) {
        super(rs, context);
        mHandlerThread = new HandlerThread("NativePhaseHandler",
                                           Process.THREAD_PRIORITY_URGENT_AUDIO);
        mHandlerThread.start();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
        mHandlerThread.setPriority(Thread.MAX_PRIORITY);
        mPhase4Handler = new Phase4Handler(mHandlerThread.getLooper());
    }

    @Override
    public void reset(int width, int height) {
        super.reset(width, height);
        mPhase4Handler.removeMessages(0);

        android.util.Log.v("cpa", "reset " + mWidth + ", " + mHeight);

        mOrigByteBuffer = (ByteBuffer)NativeImageFilter.allocByteBuffer(width * height * 4);
        mTextureByteBuffer = (ByteBuffer)NativeImageFilter.allocByteBuffer(width * height * 4);
        mImageBufferRgbFilter = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
        mImageBufferYuv = Bitmap.createBitmap(mWidth, mHeight, Config.ARGB_8888);
        mCanvas = new Canvas(mImageBufferYuv);
        pixels = new int[mWidth * mHeight];

        setFilterData(mFilter);
    }

    @Override
    public void setFilterData(Filter filter) {
        mPhase4Handler.removeMessages(0);
        synchronized(mFilter) {
            mFilter = filter;
            if(vignetteBitmap != null) {
                vignetteBitmap.recycle();
                vignetteBitmap = null;
            }
            if(textureBitmap != null) {
                textureBitmap.recycle();
                textureBitmap = null;
            }
            if(frameBitmap != null) {
                frameBitmap.recycle();
                frameBitmap = null;
            }
            if(mWidth == 0 || mHeight == 0) {
                return;
            }
            ArtFilterUtils.initFilter(mContext, filter.mArtFilter.mFilterName,
                                      filter.mArtFilter.getParams(), mWidth, mHeight);

            if(!(mFilter.mAll == null && mFilter.mRed == null && mFilter.mGreen == null && mFilter.mBlue == null)
                    && !(CurvesPoint.isIdentity(mFilter.mAll)
                            && CurvesPoint.isIdentity(mFilter.mRed)
                            && CurvesPoint.isIdentity(mFilter.mGreen) && CurvesPoint.isIdentity(mFilter.mBlue))) {
                Spine.makeLookupTable(mFilter.mAll, mAllCurves);
                Spine.makeLookupTable(mFilter.mRed, mRCurves);
                Spine.makeLookupTable(mFilter.mGreen, mGCurves);
                Spine.makeLookupTable(mFilter.mBlue, mBCurves);
            }

            if(!ArtFilterRenderScriptUtils.LITE_VERSION || !mFilter.isArtFilter()) {
                if(mFilter.needVignette()) {
                    vignetteBitmap = FileUtils.getBitmapResource(mContext,
                                                                 new StringBuilder().append("vignetting_")
                                                                                    .append(filter.mVignetteName)
                                                                                    .toString(),
                                                                 mWidth, mHeight, Config.ARGB_8888);
                }

                if(filter.needTexture()) {
                    final String resourceName;
                    if(filter.mTextureName.equals("texture06") && mWidth != mHeight) {
                        if(mWidth < mHeight) {
                            resourceName = new StringBuilder().append("texture_")
                                                              .append(filter.mTextureName)
                                                              .append("_ratio34").toString();
                        } else {
                            resourceName = new StringBuilder().append("texture_")
                                                              .append(filter.mTextureName)
                                                              .append("_ratio43").toString();
                        }
                    } else {
                        resourceName = new StringBuilder().append("texture_")
                                                          .append(filter.mTextureName).toString();
                    }
                    textureBitmap = FileUtils.getBitmapResource(mContext, resourceName, mWidth,
                                                                mHeight, Config.ARGB_8888);
                    if(textureBitmap != null) {
                        mTextureByteBuffer.position(0);
                        textureBitmap.copyPixelsToBuffer(mTextureByteBuffer);
                        mTextureByteBuffer.position(0);
                    }
                }

                if(filter.neetFrame()) {
                    // Frame 있으면 적용 - 이것도 java코드로~
                    final String resourceName;
                    if(filter.mFrameName.equals("frame06") && mWidth != mHeight) {
                        if(mWidth < mHeight) {
                            resourceName = new StringBuilder().append("frame_")
                                                              .append(filter.mFrameName)
                                                              .append("_ratio34").toString();
                        } else {
                            resourceName = new StringBuilder().append("frame_")
                                                              .append(filter.mFrameName)
                                                              .append("_ratio43").toString();
                        }
                    } else {
                        resourceName = new StringBuilder().append("frame_")
                                                          .append(filter.mFrameName).toString();
                    }
                    frameBitmap = FileUtils.getBitmapResource(mContext, resourceName, mWidth,
                                                              mHeight, Config.ARGB_8888);
                }
            }
        }
    }

    @Override
    public void execute(byte[] yuv) {
        synchronized(mFilter) {
            if(mPhase4Handler.isProgress(0)) {
                return;
            }

            // NativeUtils.decodeYUV420SP(pixels, yuv, mWidth, mHeight);
            NativeImageFilter.YUVtoRBG(pixels, yuv, mWidth, mHeight);
            mCanvas.drawBitmap(pixels, 0, mWidth, 0, 0, mWidth, mHeight, true, null);

            mPhase4Handler.sendEmptyMessage(0);
        }
    }

    @Override
    public void execute(Bitmap source, Bitmap dest) {
    }

    private class PhaseHandler extends Handler {
        protected AtomicBoolean[] inProgress = new AtomicBoolean[4];

        public PhaseHandler(Looper looper) {
            super(looper);
            for(int i = 0; i < inProgress.length; i++) {
                inProgress[i] = new AtomicBoolean();
            }
        }

        protected void handleStart(int what) {
            inProgress[what].set(true);
        }

        protected void handleEnd(int what) {
            inProgress[what].set(false);
        }

        public boolean isProgress(int what) {
            return hasMessages(what) || inProgress[what].get();
        }
    }

    private class Phase4Handler extends PhaseHandler {
        private ArrayList<String> mBinderName = new ArrayList<String>();
        private boolean mProcessed;

        public Phase4Handler(Looper looper) {
            super(looper);
        }

        public void handleMessage(android.os.Message msg) {
            handleStart(msg.what);
            ByteBuffer image = mOrigByteBuffer;

            // phase 1
            if(!mBinderName.contains(Thread.currentThread().getName())) {
                ArtFilterManager.getInstance().initGL(1, 1);
                mBinderName.add(Thread.currentThread().getName());
            }
            if(!TextUtils.isEmpty(mFilter.mArtFilter.mFilterName)) {
                ArtFilterUtils.processArtFilter(mContext, mImageBufferYuv);
            }
            image.position(0);
            mImageBufferYuv.copyPixelsToBuffer(image);
            image.position(0);

            // phase2
            // todo rgb vin, fram, texture
            if(mFilter.mBWMode) {
                NativeImageFilter.grayProcessing(image, mWidth, mHeight);
                image.position(0);
            }

            if(!(mFilter.mAll == null && mFilter.mRed == null && mFilter.mGreen == null && mFilter.mBlue == null)
                    && !(CurvesPoint.isIdentity(mFilter.mAll)
                            && CurvesPoint.isIdentity(mFilter.mRed)
                            && CurvesPoint.isIdentity(mFilter.mGreen) && CurvesPoint.isIdentity(mFilter.mBlue))) {
                NativeImageFilter.curveProcessing(image,
                                                  mWidth,
                                                  mHeight,
                                                  NativeUtils.getCurveData(mFilter,
                                                                           Filter.CURVES_TYPE_ALL),
                                                  NativeUtils.getCurveData(mFilter,
                                                                           Filter.CURVES_TYPE_RED),
                                                  NativeUtils.getCurveData(mFilter,
                                                                           Filter.CURVES_TYPE_GREEN),
                                                  NativeUtils.getCurveData(mFilter,
                                                                           Filter.CURVES_TYPE_BLUE));
                image.position(0);
            }

            if(mFilter.mSaturation <= 2.0f && mFilter.mSaturation >= .0f
                    && mFilter.mSaturation != 1) {
                NativeImageFilter.saturationProcessing(image, mWidth, mHeight, mFilter.mSaturation);
                image.position(0);
            }

            if(mFilter.mBrightness >= -100 && mFilter.mBrightness <= 100
                    && mFilter.mBrightness != 0) {
                NativeImageFilter.brightnessProcessing(image, mWidth, mHeight, mFilter.mBrightness);
                image.position(0);
            }

            if(mFilter.mContrast >= 0.5 && mFilter.mContrast <= 1.5 && mFilter.mContrast != 1) {
                NativeImageFilter.contrastProcessing(image, mWidth, mHeight, mFilter.mContrast);
                image.position(0);
            }

            mProcessed = false;

            if(!ArtFilterRenderScriptUtils.LITE_VERSION || !mFilter.isArtFilter()) {
                if(vignetteBitmap != null) {
                    mImageBufferRgbFilter.copyPixelsFromBuffer(image);
                    image.position(0);
                    NativeUtils.applyImage(mImageBufferRgbFilter, vignetteBitmap,
                                           mFilter.mVignetteAlpha, null);
                    mProcessed = true;
                }
                //
                if(textureBitmap != null) {
                    if(Build.VERSION.SDK_INT >= 11) {
                        if(!mProcessed) {
                            mImageBufferRgbFilter.copyPixelsFromBuffer(image);
                            image.position(0);
                        }
                        NativeUtils.applyImage(mImageBufferRgbFilter, textureBitmap,
                                               mFilter.mTextureAlpha,
                                               mFilter.isOverlayTexture() ? PorterDuff.Mode.OVERLAY
                                                       : null);
                        mProcessed = true;
                    } else {
                        if(mProcessed) {
                            mImageBufferRgbFilter.copyPixelsToBuffer(image);
                            image.position(0);
                        }
                        NativeImageFilter.textureProcessing(image, mTextureByteBuffer, mWidth,
                                                            mHeight, mFilter.mTextureAlpha);
                        image.position(0);
                        mTextureByteBuffer.position(0);
                        mProcessed = false;
                    }
                }

                if(frameBitmap != null) {
                    if(!mProcessed) {
                        mImageBufferRgbFilter.copyPixelsFromBuffer(image);
                        image.position(0);
                    }
                    NativeUtils.applyImage(mImageBufferRgbFilter, frameBitmap, 255, null);
                    mProcessed = true;
                }
            }

            if(mProcessed) {
                mImageBufferRgbFilter.copyPixelsToBuffer(image);
                image.position(0);
            }

            mOutputBitmap.copyPixelsFromBuffer(image);
            image.position(0);

            if(mIsYuv && mRgbData != null) {
                mOutputBitmap.getPixels(mRgbData, 0, mWidth, 0, 0, mWidth, mHeight);
                // image.get(mRgbData);
                NativeImageFilter.RGBtoYUV(mRgbData, mYuvData, mWidth, mHeight);
            }

            mOutputView.postInvalidate();

            if(mOnPhaseCompleteListener != null) {
                mOnPhaseCompleteListener.onPhaseComplete(mYuvData);
            }

            handleEnd(msg.what);
        }
    }

    @Override
    public void execute(int[] source, int[] dest) {
        // TODO Auto-generated method stub
        
    }
}
