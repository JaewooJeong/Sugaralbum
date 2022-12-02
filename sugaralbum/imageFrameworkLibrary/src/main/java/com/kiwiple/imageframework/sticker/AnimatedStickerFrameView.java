
package com.kiwiple.imageframework.sticker;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;

import com.kiwiple.imageframework.collage.DesignTemplate;
import com.kiwiple.imageframework.collage.ImageFrameInfo;
import com.kiwiple.imageframework.util.CollageRect;

/**
 * GIF 스티커를 위한 FrameView 클래스
 * 
 * @version 3.0
 */
class AnimatedStickerFrameView extends StickerFrameView {

    // 기본 교체 주기를 330으로 잡는다
    protected int mDuration = 330;
    protected Bitmap[] mAnimatedImages;

    protected AnimatedStickerFrameView(int mId, ImageFrameInfo mFrameInfo, Bitmap mFrameImage,
            CollageRect mFrameRect, Matrix mFrameMatrix, float mOriginalFrameScaleFactor,
            float mFrameScaleFactor, float mFrameMinScale, float mFrameMaxScale, Bitmap mImage,
            Matrix mImageMatrix, float mOriginalImageScaleFactor, float mImageScaleFactor,
            float mImageMinScale, float mImageMaxScale, int mOriginalImageRotation,
            float mCurrentImageRotation, boolean mImageFlip, CollageRect mSelectionRect,
            Paint mSelectionPaint, Paint mBackgroundPaint, Context mContext, float mExtraScale,
            CollageRect mScaleToolBox, CollageRect mDeleteToolBox, Bitmap mScale, Bitmap mScaleSel,
            Bitmap mDelete, Bitmap mDeleteSel, int mScaleLocation, int mDeleteLocation,
            DesignTemplate mDesignTemplate, boolean mEnable, Paint mImagePaint, int mImageAlpha,
            int mEditProgress, Bitmap[] mAnimatedImages) {
        super(mId, mFrameInfo, mFrameImage, mFrameRect, mFrameMatrix, mOriginalFrameScaleFactor,
              mFrameScaleFactor, mFrameMinScale, mFrameMaxScale, mImage, mImageMatrix,
              mOriginalImageScaleFactor, mImageScaleFactor, mImageMinScale, mImageMaxScale,
              mOriginalImageRotation, mCurrentImageRotation, mImageFlip, mSelectionRect,
              mSelectionPaint, mBackgroundPaint, mContext, mExtraScale, mScaleToolBox,
              mDeleteToolBox, mScale, mScaleSel, mDelete, mDeleteSel, mScaleLocation,
              mDeleteLocation, mDesignTemplate, mEnable, mImagePaint, mImageAlpha, mEditProgress);
        this.mScaleToolBox = mScaleToolBox;
        this.mDeleteToolBox = mDeleteToolBox;
        this.mScale = mScale;
        this.mScaleSel = mScaleSel;
        this.mDelete = mDelete;
        this.mDeleteSel = mDeleteSel;
        this.mScaleLocation = mScaleLocation;
        this.mDeleteLocation = mDeleteLocation;
        this.mDesignTemplate = mDesignTemplate;
        this.mEnable = mEnable;
        this.mImagePaint = mImagePaint;
        this.mImageAlpha = mImageAlpha;
        this.mAnimatedImages = mAnimatedImages;
    }

    public AnimatedStickerFrameView(Context context, int id, Bitmap scale, Bitmap scaleSel,
            int scaleLocation, Bitmap delete, Bitmap deleteSel, int deleteLocation,
            StickerFrameInfo info, DesignTemplate designTemplate) {
        super(context, id, scale, scaleSel, scaleLocation, delete, deleteSel, deleteLocation, null,
              null);
        this.mScale = scale;
        this.mScaleSel = scaleSel;
        this.mScaleLocation = scaleLocation;
        this.mDelete = delete;
        this.mDeleteSel = deleteSel;
        this.mDeleteLocation = deleteLocation;
    }

    /**
     * 각각의 GIF 스티커를 이룰 비트맵 배열을 설정
     */
    public void setAnimatedImage(Bitmap[] images) {
        mAnimatedImages = images;
        if(images != null && images.length > 0) {
            mImage = images[0];
        }
    }

    /**
     * GIF 스티커의 시간을 설정한다.
     * 
     * @param duration
     */
    public void setAnimationDuration(int duration) {
        mDuration = duration;
    }

    public int getAnimationImageCount() {
        return mAnimatedImages != null ? mAnimatedImages.length : 0;
    }

    public void startAnimation() {
        if(mAnimatedImages != null && mAnimatedImages.length > 0) {
            if(mAnimatedTimer != null) {
                mAnimatedTimer = null;
            }

            mCurrentBitmapIndex = 0;

            mAnimatedTimer = new Timer();
            mAnimatedTimer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mCurrentBitmapIndex++;
                    if(mCurrentBitmapIndex == mAnimatedImages.length) {
                        mCurrentBitmapIndex = 0;
                    }

                    mImage = mAnimatedImages[mCurrentBitmapIndex];
                }
            }, 0, mDuration);
        }
    }

    public void stopAnimation() {
        if (mAnimatedTimer != null) {
            mAnimatedTimer.cancel();    
        }
    }

    public void drawImageWithIndex(int index, Canvas canvas, Paint bitmapPaint, boolean isOutput) {
        if(mAnimatedImages != null && index >= 0 && index < mAnimatedImages.length) {
            
//            int currentImageIndex = mCurrentBitmapIndex; 
//            for (int i = 0; i < index; i++) {
//                currentImageIndex++;
//                if (currentImageIndex >= mAnimatedImages.length) {
//                    currentImageIndex = 0;
//                }
//            }
            int currentImageIndex = index;
            
            Bitmap image = mAnimatedImages[currentImageIndex];
            if(image != null) {
                mImagePaint.set(bitmapPaint);
                mImagePaint.setAlpha(mImageAlpha);

                canvas.save();
                canvas.concat(mFrameMatrix);
                if(((StickerFrameInfo)mFrameInfo).mClipBackground) {
                    canvas.save();
                    canvas.scale(0.95f, 0.95f, mFrameRect.centerX(), mFrameRect.centerY());
                    canvas.clipRect(mSelectionRect);
                    canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
                    canvas.restore();
                    // bitmapPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
                }
                bitmapPaint.setAlpha(((StickerFrameInfo)mFrameInfo).mAlpha);
                canvas.drawBitmap(image, mImageMatrix, mImagePaint);
                canvas.restore();
            }
        }
    }

    private int mCurrentBitmapIndex = 0;
    private Timer mAnimatedTimer = null;

    @Override
    public void clear() {
        super.clear();
        stopAnimation();
        mAnimatedImages = null;
    }

    @Override
    public AnimatedStickerFrameView copy() {
        return new AnimatedStickerFrameView(mId, mFrameInfo, mFrameImage, mFrameRect,
                                            new Matrix(mFrameMatrix), mOriginalFrameScaleFactor,
                                            mFrameScaleFactor, mFrameMinScale, mFrameMaxScale,
                                            mImage, new Matrix(mImageMatrix),
                                            mOriginalImageScaleFactor, mImageScaleFactor,
                                            mImageMinScale, mImageMaxScale, mOriginalImageRotation,
                                            mCurrentImageRotation, mImageFlip, mSelectionRect,
                                            new Paint(mSelectionPaint),
                                            new Paint(mBackgroundPaint), mContext, mExtraScale,
                                            new CollageRect(mScaleToolBox),
                                            new CollageRect(mDeleteToolBox), mScale, mScaleSel,
                                            mDelete, mDeleteSel, mScaleLocation, mDeleteLocation,
                                            mDesignTemplate, mEnable, new Paint(mImagePaint),
                                            mImageAlpha, mEditProgress, mAnimatedImages);
    }

}
