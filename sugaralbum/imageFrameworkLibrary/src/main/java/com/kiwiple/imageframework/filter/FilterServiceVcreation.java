
package com.kiwiple.imageframework.filter;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.kiwiple.imageframework.Constants;
import com.kiwiple.imageframework.gpuimage.ArtFilterManager;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;
import com.kiwiple.imageframework.util.BitmapUtils;
import com.kiwiple.imageframework.util.FileUtils;
import com.kiwiple.imageframework.util.SmartLog;

public class FilterServiceVcreation extends Service {
    private static final String TAG = FilterServiceVcreation.class.getSimpleName();

    private ByteBuffer mOrigByteBuffer;
    private ByteBuffer mTextureBuffer;

    private Paint mPaint = new Paint();

    private ArtFilterManager mArtFilterManager;
    private ArrayList<String> mBinderName = new ArrayList<String>();

    // 생성할 필터 이미지 파일의 크기
    public static int PICTURE_SIZE_MAX = 1600;
    public static int PICTURE_SIZE_ARTFILTER_MAX = 1600;

    @Override
    public void onCreate() {
        super.onCreate();
        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(true);
        mPaint.setDither(true);
    }

    @Override
    public void onDestroy() {
        if(mArtFilterManager != null) {
            mArtFilterManager.deinitGL();
        }

        SmartLog.i(TAG, "Filter Service is destoryed.");
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mFilterSerivceStub;
    }

    // private void swapBuffer() {
    // ByteBuffer temp = mResultByteBuffer;
    // mResultByteBuffer = mOrigByteBuffer;
    // mOrigByteBuffer = temp;
    //
    // mOrigByteBuffer.position(0);
    // mResultByteBuffer.position(0);
    // }

    private static short[] getCurveData(Filter filter, String CURVES_TYPE) {
        ArrayList<CurvesPoint> curvesPoints = filter.getCurvesPoints(CURVES_TYPE);
        short[] points;

        if(curvesPoints == null || curvesPoints.size() == 0) {
            points = new short[] {
                    0, 0, 255, 255
            };
        } else {
            int size = curvesPoints.size();
            points = new short[size * 2];
            int arrayIndex = 0;
            for(int i = 0; i < size; i++) {
                CurvesPoint cp = curvesPoints.get(i);
                points[arrayIndex++] = cp.mX;
                points[arrayIndex++] = cp.mY;
            }
        }
        return points;
    }

    private void applyImage(Bitmap ori, Bitmap cover, int alpha, PorterDuff.Mode mode) {
        int w = ori.getWidth();
        int h = ori.getHeight();

        BitmapFactory.Options op = new Options();
        op.inPreferredConfig = Config.ARGB_8888;

        Canvas c = new Canvas(ori);

        mPaint.setAlpha(alpha);
        if(mode != null) {
            mPaint.setXfermode(new PorterDuffXfermode(mode));
        } else {
            mPaint.setXfermode(null);
        }

        c.drawBitmap(cover, new Rect(0, 0, cover.getWidth(), cover.getHeight()), new Rect(0, 0, w,
                                                                                          h),
                     mPaint);

        cover.recycle();
        cover = null;
    }

    private void applyTopFrame(Bitmap ori, Bitmap cover, int alpha, PorterDuff.Mode mode) {
        int w = ori.getWidth();

        BitmapFactory.Options op = new Options();
        op.inPreferredConfig = Config.ARGB_8888;

        Canvas c = new Canvas(ori);

        mPaint.setAlpha(alpha);
        if(mode != null) {
            mPaint.setXfermode(new PorterDuffXfermode(mode));
        } else {
            mPaint.setXfermode(null);
        }

        c.drawBitmap(cover, null,
                     new Rect(0, 0, w, (int)(cover.getHeight() * (w / (float)cover.getWidth()))),
                     mPaint);

        cover.recycle();
        cover = null;
    }

    private void applyBottomFrame(Bitmap ori, Bitmap cover, int alpha, PorterDuff.Mode mode) {
        int w = ori.getWidth();
        int h = ori.getHeight();

        BitmapFactory.Options op = new Options();
        op.inPreferredConfig = Config.ARGB_8888;

        Canvas c = new Canvas(ori);

        mPaint.setAlpha(alpha);
        if(mode != null) {
            mPaint.setXfermode(new PorterDuffXfermode(mode));
        } else {
            mPaint.setXfermode(null);
        }

        c.drawBitmap(cover,
                     null,
                     new Rect(0, h - (int)(cover.getHeight() * (w / (float)cover.getWidth())), w, h),
                     mPaint);

        cover.recycle();
        cover = null;
    }

    private void releaseBuffer() {
        if(mOrigByteBuffer != null) {
            NativeImageFilter.freeByteBuffer(mOrigByteBuffer);
            mOrigByteBuffer = null;
        }
    }

    private static void causeGC(int width, int height) {
        if(width >= 1600 || height >= 1600) {
            System.gc();
        }
    }

    private IBinder mFilterSerivceStub = new IFilterServiceVcreation.Stub() {
        private boolean mCanceled = false;

        private Bitmap processing(Bitmap src, Filter filter) {
            if(src == null) {
                return null;
            }
            causeGC(src.getWidth(), src.getHeight());

            if(mArtFilterManager == null || !mBinderName.contains(Thread.currentThread().getName())) {
                mArtFilterManager = ArtFilterManager.getInstance();
                mArtFilterManager.initGL(1, 1);
                mBinderName.add(Thread.currentThread().getName());
            }

            try {
                if(mCanceled) {
                    return null;
                }
                final int width = src.getWidth();
                final int height = src.getHeight();

                SmartLog.d(TAG, "Filtering start");
                // art filter
                if(!TextUtils.isEmpty(filter.mArtFilter.mFilterName)) {
                    SmartLog.d(TAG, "Filtering artfilter start");
                    // result = ArtFilterUtils.processArtFilter(FilterServiceLgu.this, src);
                    ArtFilterUtils.initFilter(FilterServiceVcreation.this, filter.mArtFilter.mFilterName,
                                              filter.mArtFilter.getParams(), width, height);
                    ArtFilterUtils.processArtFilter(FilterServiceVcreation.this, src);
                    SmartLog.d(TAG, "Filtering artfilter end");
                }
                if(mCanceled) {
                    return null;
                }

                SmartLog.d(TAG, "Filtering setup start");

                mOrigByteBuffer = (ByteBuffer)NativeImageFilter.allocByteBuffer(width * height * 4);

                mOrigByteBuffer.position(0);
                src.copyPixelsToBuffer(mOrigByteBuffer);
                mOrigByteBuffer.position(0);

                SmartLog.d(TAG, "Filtering setup end");

                // gray 세피아를 위해 흑백을 최 우선 작업으로 이동
                if(mCanceled) {
                    return null;
                }

                if(filter.mBWMode) {
                    SmartLog.d(TAG, "Filtering gray start");
                    NativeImageFilter.grayProcessing(mOrigByteBuffer, width, height);
                    mOrigByteBuffer.position(0);
                    // swapBuffer();
                    SmartLog.d(TAG, "Filtering gray end");
                }

                if(mCanceled) {
                    return null;
                }

                if(!(filter.mAll == null && filter.mRed == null && filter.mGreen == null && filter.mBlue == null)
                        && !(CurvesPoint.isIdentity(filter.mAll)
                                && CurvesPoint.isIdentity(filter.mRed)
                                && CurvesPoint.isIdentity(filter.mGreen) && CurvesPoint.isIdentity(filter.mBlue))) {
                    SmartLog.d(TAG, "Filtering curve start");
                    // curve 테스트 용 값.
                    short[] all = getCurveData(filter, Filter.CURVES_TYPE_ALL);
                    short[] r = getCurveData(filter, Filter.CURVES_TYPE_RED);
                    short[] g = getCurveData(filter, Filter.CURVES_TYPE_GREEN);
                    short[] b = getCurveData(filter, Filter.CURVES_TYPE_BLUE);

                    // Curve 적용
                    NativeImageFilter.curveProcessing(mOrigByteBuffer, width, height, all, r, g, b);
                    mOrigByteBuffer.position(0);
                    SmartLog.d(TAG, "Filtering curve end");
                }

                if(mCanceled) {
                    return null;
                }

                if(filter.mSaturation <= 2.0f && filter.mSaturation >= .0f
                        && filter.mSaturation != 1 /* 1이면 건너 뛰자~ */) {
                    SmartLog.d(TAG, "Filtering saturation start");
                    // Saturation가 있으면 적용 (0.0 ~ 2.0)
                    NativeImageFilter.saturationProcessing(mOrigByteBuffer, width, height,
                                                           filter.mSaturation);
                    mOrigByteBuffer.position(0);
                    SmartLog.d(TAG, "Filtering saturation end");
                }

                if(mCanceled) {
                    return null;
                }

                if(filter.mBrightness >= -100 && filter.mBrightness <= 100
                        && filter.mBrightness != 0 /* 0이면 건너 뛰자~ */) {
                    SmartLog.d(TAG, "Filtering brightness start");
                    // Brightness가 있으면 적용 (-100 ~ 100 사이던가??)
                    NativeImageFilter.brightnessProcessing(mOrigByteBuffer, width, height,
                                                           filter.mBrightness);
                    mOrigByteBuffer.position(0);
                    SmartLog.d(TAG, "Filtering brightness end");
                }

                if(mCanceled) {
                    return null;
                }

                if(filter.mContrast >= 0.5 && filter.mContrast <= 1.5 && filter.mContrast != 1/*
                                                                                               * 1이면
                                                                                               * 건너
                                                                                               * 뛰자~
                                                                                               */) {
                    SmartLog.d(TAG, "Filtering contrast start");
                    // contrast가 있으면 적용 (0.5 ~ 1.5 사이 값)
                    NativeImageFilter.contrastProcessing(mOrigByteBuffer, width, height,
                                                         filter.mContrast);
                    mOrigByteBuffer.position(0);
                    SmartLog.d(TAG, "Filtering contrast end");
                }

                if(mCanceled) {
                    return null;
                }

                src.copyPixelsFromBuffer(mOrigByteBuffer);
                mOrigByteBuffer.position(0);

                if(filter.needVignette() && !filter.isLightArtFilter()) {
                    SmartLog.d(TAG, "Filtering vignette start");
                    Bitmap vignetteBitmap = FileUtils.getBitmapResource(getApplicationContext(),
                                                                        new StringBuilder().append("vignetting_")
                                                                                           .append(filter.mVignetteName)
                                                                                           .toString(),
                                                                        width, height,
                                                                        Config.ARGB_8888);

                    if(vignetteBitmap != null) {
                        applyImage(src, vignetteBitmap, filter.mVignetteAlpha, null);
                        vignetteBitmap = null;
                        causeGC(width, height);
                    } else {
                        SmartLog.e(TAG, "VignetteImage null");
                    }
                    SmartLog.d(TAG, "Filtering vignette end");
                }

                if(mCanceled) {
                    return null;
                }

                if(filter.needTexture() && !filter.isLightArtFilter()) {
                    SmartLog.d(TAG, "Filtering texture start");
                    final String resourceName;
                    if(filter.mTextureName.equals("texture06") && width != height) {
                        if(width < height) {
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
                    Bitmap textureBitmap = FileUtils.getBitmapResource(getApplicationContext(),
                                                                       resourceName, width, height,
                                                                       Config.ARGB_8888);

                    if(textureBitmap != null) {
                        if(Build.VERSION.SDK_INT >= 11) {
                            applyImage(src, textureBitmap, filter.mTextureAlpha,
                                       filter.isOverlayTexture() ? PorterDuff.Mode.OVERLAY : null);
                        } else {
                            mTextureBuffer = (ByteBuffer)NativeImageFilter.allocByteBuffer(width
                                    * height * 4);
                            mTextureBuffer.position(0);
                            textureBitmap.copyPixelsToBuffer(mTextureBuffer);
                            textureBitmap.recycle();
                            mTextureBuffer.position(0);

                            if(filter.needVignette()) {
                                src.copyPixelsToBuffer(mOrigByteBuffer);
                                mOrigByteBuffer.position(0);
                            }
                            NativeImageFilter.textureProcessing(mOrigByteBuffer, mTextureBuffer,
                                                                width, height, filter.mTextureAlpha);
                            NativeImageFilter.freeByteBuffer(mTextureBuffer);
                            mOrigByteBuffer.position(0);
                            src.copyPixelsFromBuffer(mOrigByteBuffer);
                            mTextureBuffer = null;
                        }
                        textureBitmap = null;
                        causeGC(width, height);
                    } else {
                        SmartLog.e(TAG, "TextureImage null");
                    }
                    SmartLog.d(TAG, "Filtering texture end");
                }

                if(mCanceled) {
                    return null;
                }

                if(filter.neetFrame() && !filter.isLightArtFilter()) {
                    SmartLog.d(TAG, "Filtering frame start");
                    // Frame 있으면 적용 - 이것도 java코드로~
                    final String resourceName;
                    if(filter.mFrameName.equals("frame06") && width != height) {
                        if(width < height) {
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
                    Bitmap frameBitmap = FileUtils.getBitmapResource(getApplicationContext(),
                                                                     resourceName, width, height,
                                                                     Config.ARGB_8888);

                    if(frameBitmap != null) {
                        applyImage(src, frameBitmap, 255, null);
                        frameBitmap = null;
                        causeGC(width, height);
                    } else {
                        SmartLog.e(TAG, "FrameImage null");
                    }
                    SmartLog.d(TAG, "Filtering frame end");
                }
                if(filter.neetTopFrame() && !filter.isLightArtFilter()) {
                    SmartLog.d(TAG, "Filtering top frame start: " + filter.mTopFrameName);
                    // Frame 있으면 적용 - 이것도 java코드로~
                    Bitmap frameBitmap = FileUtils.getBitmapResource(getApplicationContext(),
                                                                     filter.mTopFrameName, width,
                                                                     Config.ARGB_8888);

                    if(frameBitmap != null) {
                        applyTopFrame(src, frameBitmap, 255, null);
                        frameBitmap = null;
                        causeGC(width, height);
                    } else {
                        SmartLog.e(TAG, "FrameImage null");
                    }
                    SmartLog.d(TAG, "Filtering top frame end");
                }
                if(filter.neetBottomFrame() && !filter.isLightArtFilter()) {
                    SmartLog.d(TAG, "Filtering bottom frame start");
                    // Frame 있으면 적용 - 이것도 java코드로~
                    Bitmap frameBitmap = FileUtils.getBitmapResource(getApplicationContext(),
                                                                     filter.mBottomFrameName,
                                                                     width, Config.ARGB_8888);

                    if(frameBitmap != null) {
                        applyBottomFrame(src, frameBitmap, 255, null);
                        frameBitmap = null;
                        causeGC(width, height);
                    } else {
                        SmartLog.e(TAG, "FrameImage null");
                    }
                    SmartLog.d(TAG, "Filtering bottom frame end");
                }

                if(mCanceled) {
                    return null;
                }
            } catch(OutOfMemoryError e) {
                SmartLog.e(TAG, "Filter processing", e);
                return null;
            } catch(Throwable e) {
                SmartLog.e(TAG, "Filter processing", e);
                return null;
            } finally {
                releaseBuffer();
                if(mCanceled) {
                    if(src != null && !src.isRecycled()) {
                        src.recycle();
                        src = null;
                    }
                    if(src != null && !src.isRecycled()) {
                        src.recycle();
                        src = null;
                    }
                }
            }
            SmartLog.d(TAG, "Filtering finished");
            return src;
        }

        @SuppressWarnings("unused")
        @Override
        public String processingImageFile(String filename, int size, Filter filter,
                String stickerImageFilePath) throws RemoteException {
            // art filter 사용할 경우 최대 해상도는 중간값으로 설정.
            if(filter.isArtFilter() && size > PICTURE_SIZE_ARTFILTER_MAX) {
                size = PICTURE_SIZE_ARTFILTER_MAX;
            }
            if(size > PICTURE_SIZE_MAX) {
                size = PICTURE_SIZE_MAX;
            }

            Bitmap image = null;
            try {
                // File에서 Bitmap Decoding시 Bitmap은 기본적으로 unMutable (수정할 수 없음)
                // API 11 (OS 3.0) 이상에서는 decoding시 Bitmap의 속성을 mutable로 설정하여 가져올 수 있게 됨
                Bitmap decodingBitmap = FileUtils.decodingImage(filename, size,
                                                                Bitmap.Config.ARGB_8888);

                // 따라서 API 11이상에서는 해당 decodingBitmap으로 바로 이미지 처리를 할 수 있게됨.
                // 아래는 API 11미만인 경우엔 decodingBitmap을 mutable한 비트맵으로 새로 생성해주는 코드 (예전 프로세스 방식)
                if(!decodingBitmap.isMutable()) {
                    Bitmap mutableImage = decodingBitmap.copy(Config.ARGB_8888, true);
                    decodingBitmap.recycle();
                    decodingBitmap = mutableImage;
                }

                image = processing(decodingBitmap, filter);
            } catch(IOException e) {
            }

            String tempImagePath = null;
            if(image != null) {
                if(!mCanceled) {
                    image = mergeStickerImage(image, stickerImageFilePath, size);
                }
                if(!mCanceled && Constants.DEMO_VERSION) {
                    BitmapUtils.applyWaterMarkImage(getBaseContext(), image);
                }
                if(!mCanceled) {
                    tempImagePath = new StringBuffer().append(getApplicationContext().getFilesDir()
                                                                                     .getAbsolutePath())
                                                      .append(File.separator)
                                                      .append("filter_temp_image.jpg").toString();
                    try {
                        FileUtils.saveBitmap(image, tempImagePath, Bitmap.CompressFormat.JPEG);
                    } catch(IOException e) {
                        tempImagePath = null;
                    }
                }
                image.recycle();
                causeGC(image.getWidth(), image.getHeight());
                image = null;
            }
            return tempImagePath;
        }

        @SuppressWarnings("unused")
        @Override
        public Bitmap processingImageBitmap(Bitmap src, Filter filter, String stickerImageFilePath)
                throws RemoteException {
            Bitmap target = src;
            int size = src.getWidth() > src.getHeight() ? src.getWidth() : src.getHeight();
            if(filter.isArtFilter() && size > PICTURE_SIZE_ARTFILTER_MAX) {
                target = BitmapUtils.resizeBitmap(src,
                                                  BitmapUtils.getRatioBitmapWidth(src,
                                                                                  PICTURE_SIZE_ARTFILTER_MAX),
                                                  BitmapUtils.getRatioBitmapHeight(src,
                                                                                   PICTURE_SIZE_ARTFILTER_MAX));
                src.recycle();
                causeGC(src.getWidth(), src.getHeight());
            }
            if(size > PICTURE_SIZE_MAX) {
                target = BitmapUtils.resizeBitmap(src,
                                                  BitmapUtils.getRatioBitmapWidth(src,
                                                                                  PICTURE_SIZE_MAX),
                                                  BitmapUtils.getRatioBitmapHeight(src,
                                                                                   PICTURE_SIZE_MAX));
                src.recycle();
                causeGC(src.getWidth(), src.getHeight());
            }

            // API 11미만인 경우엔 decodingBitmap을 mutable한 비트맵으로 새로 생성해주는 코드 (예전 프로세스 방식)
            if(!target.isMutable()) {
                Bitmap mutableImage = target.copy(Config.ARGB_8888, true);
                target.recycle();
                target = mutableImage;
            }

            Bitmap image = processing(target, filter);
            if(!mCanceled) {
                mergeStickerImage(image, stickerImageFilePath, size);
            }
            if(!mCanceled && Constants.DEMO_VERSION) {
                BitmapUtils.applyWaterMarkImage(getBaseContext(), image);
            }
            return image;
        }

        private Bitmap mergeStickerImage(Bitmap filteredImage, String stickerImageFilePath, int size) {
            if(!TextUtils.isEmpty(stickerImageFilePath)) {
                try {
                    Bitmap stickerImage = FileUtils.decodingImage(stickerImageFilePath, size,
                                                                  Bitmap.Config.ARGB_8888);
                    if(stickerImage != null) {
                        BitmapUtils.mergeImage(filteredImage, stickerImage, null);
                        stickerImage.recycle();
                    }
                } catch(IOException e) {
                }
            }
            return filteredImage;
        }

        @Override
        public void stopProcessing(boolean canceled) {
            mCanceled = canceled;
        }
    };
}
