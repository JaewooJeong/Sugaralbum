
package com.kiwiple.imageframework.filter.live;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.ImageFormat;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import androidx.renderscript.Allocation;
import androidx.renderscript.Allocation.MipmapControl;
import androidx.renderscript.Element;
import androidx.renderscript.Matrix3f;
import androidx.renderscript.RenderScript;
import androidx.renderscript.ScriptIntrinsicBlend;
import androidx.renderscript.ScriptIntrinsicColorMatrix;
import androidx.renderscript.ScriptIntrinsicYuvToRGB;
import androidx.renderscript.Type;
import android.text.TextUtils;

import com.kiwiple.imageframework.ScriptC_blend;
import com.kiwiple.imageframework.ScriptC_blend_overlay;
import com.kiwiple.imageframework.ScriptC_rgb_filter;
import com.kiwiple.imageframework.filter.CurvesPoint;
import com.kiwiple.imageframework.filter.Filter;
import com.kiwiple.imageframework.filter.NativeImageFilter;
import com.kiwiple.imageframework.util.FileUtils;
import com.kiwiple.imageframework.util.SmartLog;

public class RsYuv extends PreviewFiler {
    public static final MipmapControl MIPMAPCONTROL_FULL = Allocation.MipmapControl.MIPMAP_FULL;
    public static final MipmapControl MIPMAPCONTROL = Allocation.MipmapControl.MIPMAP_NONE;
    public static final int ALLOCATION_USAGE_FULL = Allocation.USAGE_SHARED
            | Allocation.USAGE_GRAPHICS_TEXTURE | Allocation.USAGE_SCRIPT;
    public static final int ALLOCATION_USAGE = Allocation.USAGE_SCRIPT;

    private Allocation mAllocationOut;
    private Allocation mAllocationIn;
    private ScriptIntrinsicYuvToRGB mYuv;
    private ScriptIntrinsicBlend mBlend;
    private ScriptC_blend_overlay mOverlayBlend;
    private ScriptC_rgb_filter mRgbFilter;
    private ScriptC_blend mBlendHelper;

    private Allocation mAllocationVignette;
    private Bitmap vignetteBitmap;

    private Allocation mAllocationTexture;
    private Bitmap textureBitmap;

    private Allocation mAllocationFrame;
    private Bitmap frameBitmap;

    // curve
    private int[] mAllCurves = new int[256];
    private int[] mRCurves = new int[256];
    private int[] mGCurves = new int[256];
    private int[] mBCurves = new int[256];

    private Phase4Handler mPhase4Handler;

    private boolean mFilterPrepared = false;

    private HandlerThread mHandlerThread;

    private ScriptIntrinsicColorMatrix mYuvToRgb;

    public RsYuv(RenderScript rs, Context context) {
        super(rs, context);
        mYuv = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs));
        mYuvToRgb = ScriptIntrinsicColorMatrix.create(rs, Element.RGBA_8888(rs));
        mYuvToRgb.setYUVtoRGB();
        mBlend = ScriptIntrinsicBlend.create(mRS, Element.U8_4(mRS));
        mBlendHelper = new ScriptC_blend(mRS);
        mBlendHelper.set_alpha((short)255);
        mRgbFilter = new ScriptC_rgb_filter(mRS);
        mOverlayBlend = new ScriptC_blend_overlay(mRS);
        ArtFilterRenderScriptUtils.init(mRS, mContext);

        mHandlerThread = new HandlerThread("Phase Handler", Process.THREAD_PRIORITY_URGENT_AUDIO);
        mHandlerThread.start();
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_DISPLAY);
        mHandlerThread.setPriority(Thread.MAX_PRIORITY);
        mPhase4Handler = new Phase4Handler(mHandlerThread.getLooper());
    }

    @Override
    public void reset(int width, int height) {
        super.reset(width, height);
        ArtFilterRenderScriptUtils.reset(width, height);

        mPhase4Handler.removeMessages(0);

        android.util.Log.v("cpa", "reset " + mWidth + ", " + mHeight);

        if(mAllocationOut != null) {
            mAllocationOut.destroy();
        }
        if(mAllocationVignette != null) {
            mAllocationVignette.destroy();
            mAllocationVignette = null;
        }
        if(mAllocationTexture != null) {
            mAllocationTexture.destroy();
            mAllocationTexture = null;
        }
        if(mAllocationFrame != null) {
            mAllocationFrame.destroy();
            mAllocationFrame = null;
        }
        
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

        Type.Builder tb = new Type.Builder(mRS, Element.RGBA_8888(mRS));
        tb.setX(mWidth);
        tb.setY(mHeight);
        mAllocationOut = Allocation.createTyped(mRS, tb.create(), MIPMAPCONTROL_FULL,
                                                ALLOCATION_USAGE_FULL);
        mAllocationVignette = Allocation.createTyped(mRS, tb.create(), MIPMAPCONTROL_FULL,
                                                     ALLOCATION_USAGE_FULL);
        mAllocationTexture = Allocation.createTyped(mRS, tb.create(), MIPMAPCONTROL_FULL,
                                                    ALLOCATION_USAGE_FULL);
        mAllocationFrame = Allocation.createTyped(mRS, tb.create(), MIPMAPCONTROL_FULL,
                                                  ALLOCATION_USAGE_FULL);

        if(mAllocationIn != null) {
            mAllocationIn.destroy();
        }
        tb = new Type.Builder(mRS, Element.U8(mRS));
        tb.setX(mWidth * mHeight * ImageFormat.getBitsPerPixel(ImageFormat.NV21) / 8);
        mAllocationIn = Allocation.createTyped(mRS, tb.create(), MIPMAPCONTROL_FULL,
                                               ALLOCATION_USAGE_FULL);
        mYuv.setInput(mAllocationIn);

        mFilterPrepared = false;
        setFilterData(mFilter);
    }

    @Override
    public void setFilterData(Filter filter) {
        if(mFilterPrepared && mFilter != null && mFilter.equals(filter)) {
            return;
        }
        mPhase4Handler.removeMessages(0);
        synchronized(mFilter) {
            mFilterPrepared = false;
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

            if(!TextUtils.isEmpty(mFilter.mArtFilter.mFilterName)) {
                ArtFilterRenderScriptUtils.initFilter(mContext, mFilter.mArtFilter.mFilterName,
                                                      filter.mArtFilter.getParams(), mWidth,
                                                      mHeight);
            }
            mRgbFilter.invoke_setGreyscale(mFilter.mBWMode);

            mRgbFilter.invoke_setCurve(false);
            if(!(mFilter.mAll == null && mFilter.mRed == null && mFilter.mGreen == null && mFilter.mBlue == null)
                    && !(CurvesPoint.isIdentity(mFilter.mAll)
                            && CurvesPoint.isIdentity(mFilter.mRed)
                            && CurvesPoint.isIdentity(mFilter.mGreen) && CurvesPoint.isIdentity(mFilter.mBlue))) {
                Spine.makeLookupTable(mFilter.mAll, mAllCurves);
                Spine.makeLookupTable(mFilter.mRed, mRCurves);
                Spine.makeLookupTable(mFilter.mGreen, mGCurves);
                Spine.makeLookupTable(mFilter.mBlue, mBCurves);
                mRgbFilter.set_allCurves(mAllCurves);
                mRgbFilter.set_rCurves(mRCurves);
                mRgbFilter.set_gCurves(mGCurves);
                mRgbFilter.set_bCurves(mBCurves);
                mRgbFilter.invoke_setCurve(true);
            }

            setSaturation(mFilter.mSaturation);
            mRgbFilter.invoke_setSaturation(mFilter.mSaturation);
            mRgbFilter.invoke_setBrightness(mFilter.mBrightness);
            mRgbFilter.invoke_setContrast(mFilter.mContrast);

            if(!ArtFilterRenderScriptUtils.LITE_VERSION || !mFilter.isArtFilter()) {
                if(mFilter.needVignette()) {
                    vignetteBitmap = FileUtils.getBitmapResource(mContext,
                                                                 new StringBuilder().append("vignetting_")
                                                                                    .append(filter.mVignetteName)
                                                                                    .toString(),
                                                                 mWidth, mHeight, Config.ARGB_8888);
                    if(vignetteBitmap != null) {
                        mAllocationVignette.copyFrom(vignetteBitmap);
                        if(mFilter.mVignetteAlpha != 255) {
                            mBlendHelper.set_alpha((short)mFilter.mVignetteAlpha);
                            mBlendHelper.forEach_setImageAlpha(mAllocationVignette);
                        }
                    }
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
                        mAllocationTexture.copyFrom(textureBitmap);
                        if(mFilter.mTextureAlpha != 255) {
                            mBlendHelper.set_alpha((short)mFilter.mTextureAlpha);
                            mBlendHelper.forEach_setImageAlpha(mAllocationTexture);
                        }
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

                    if(frameBitmap != null) {
                        mAllocationFrame.copyFrom(frameBitmap);
                    }
                }
            }
            mFilterPrepared = true;
        }
    }

    private Matrix3f satMatrix = new Matrix3f();
    private static final float rWeight = 0.299f;
    private static final float gWeight = 0.587f;
    private static final float bWeight = 0.114f;
    private float oneMinusS;

    private void setSaturation(float saturation) {
        oneMinusS = 1.0f - saturation;

        satMatrix.set(0, 0, oneMinusS * rWeight + saturation);
        satMatrix.set(0, 1, oneMinusS * rWeight);
        satMatrix.set(0, 2, oneMinusS * rWeight);
        satMatrix.set(1, 0, oneMinusS * gWeight);
        satMatrix.set(1, 1, oneMinusS * gWeight + saturation);
        satMatrix.set(1, 2, oneMinusS * gWeight);
        satMatrix.set(2, 0, oneMinusS * bWeight);
        satMatrix.set(2, 1, oneMinusS * bWeight);
        satMatrix.set(2, 2, oneMinusS * bWeight + saturation);
        mRgbFilter.set_colorMat(satMatrix);
    }

    @Override
    public void execute(byte[] yuv) {
        synchronized(mFilter) {
            if(!mFilterPrepared || mPhase4Handler.isProgress(0)) {
                return;
            }
            mAllocationIn.copyFrom(yuv);
            mPhase4Handler.sendEmptyMessage(0);
        }
    }

    @Override
    public void execute(Bitmap source, Bitmap dest) {
        synchronized(mFilter) {
            if(!mFilterPrepared) {
                return;
            }
            Allocation allocation = mAllocationOut;
            allocation.copyFrom(source);
            applyFilter(allocation);
            // phase4
            allocation.copyTo(dest);
        }
    }
    
    @Override
    public void execute(int[] source, int[] dest) {
        synchronized(mFilter) {
            if(!mFilterPrepared) {
                return;
            }

            mOutputBitmap.setPixels(source, 0, mWidth, 0, 0, mWidth, mHeight);
            Allocation allocation = mAllocationOut;
            allocation.copyFrom(mOutputBitmap);
            applyFilter(allocation);
            // phase4
            allocation.copyTo(mOutputBitmap);
            mOutputBitmap.getPixels(dest, 0, mWidth, 0, 0, mWidth, mHeight);
        }
    }
    

    private void applyFilter(Allocation allocation) {
        // phase2
        if(!TextUtils.isEmpty(mFilter.mArtFilter.mFilterName)) {
            try {
                ArtFilterRenderScriptUtils.processArtFilter(allocation);
            } catch(Exception e) {
                SmartLog.e(RsYuv.class.getSimpleName(), "applyFilter failed", e);
            }
        }

        // phase3
        mRgbFilter.forEach_rgbFilter(allocation, allocation);

        if(!ArtFilterRenderScriptUtils.LITE_VERSION || !mFilter.isArtFilter()) {
            if(mAllocationVignette != null && mFilter.needVignette()) {
                mBlend.forEachSrcOver(mAllocationVignette, allocation);
            }

            if(mAllocationTexture != null && mFilter.needTexture()) {
                if(mFilter.isOverlayTexture()) {
                    mOverlayBlend.forEach_overlayBlend(mAllocationTexture, allocation);
                } else {
                    mBlend.forEachSrcOver(mAllocationTexture, allocation);
                }
            }

            if(mAllocationFrame != null && mFilter.neetFrame()) {
                mBlend.forEachSrcOver(mAllocationFrame, allocation);
            }            
        }
    }

    private class PhaseHandler extends Handler {
        protected boolean inProgress = false;

        public PhaseHandler(Looper looper) {
            super(looper);
        }

        public boolean isProgress(int what) {
            return hasMessages(what) || inProgress;
        }
    }

    private class Phase4Handler extends PhaseHandler {
        public Phase4Handler(Looper looper) {
            super(looper);
        }

        public void handleMessage(android.os.Message msg) {
            if(!mFilterPrepared) {
                return;
            }
            inProgress = true;

            Allocation allocation = mAllocationOut;

            // phase1
            mYuv.forEach(allocation);

            applyFilter(allocation);

            if(mIsYuv && mRgbData != null) {
                // allocation.copyTo(mRgbData);
                mOutputBitmap.getPixels(mRgbData, 0, mOutputBitmap.getWidth(), 0, 0,
                                        mOutputBitmap.getWidth(), mOutputBitmap.getHeight());
                NativeImageFilter.RGBtoYUV(mRgbData, mYuvData, mOutputBitmap.getWidth(),
                                           mOutputBitmap.getHeight());
            }

            // 2015.09.09 F620L 프리뷰 깨지는 현상으로 인함
            if (mIsRenderScriptMainThread) {
                mOutputHandler.sendEmptyMessage(0);    
            } else {
//              mHandler.sendEmptyMessage(0);
                mAllocationOut.copyTo(mOutputBitmap);
                mOutputView.postInvalidate();
            }

            if(mOnPhaseCompleteListener != null) {
                mOnPhaseCompleteListener.onPhaseComplete(mYuvData);
            }

            inProgress = false;
        }
    }
    
    private Handler mOutputHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            // phase4
            mAllocationOut.copyTo(mOutputBitmap);
            mOutputView.invalidate();
        };
    };
}
