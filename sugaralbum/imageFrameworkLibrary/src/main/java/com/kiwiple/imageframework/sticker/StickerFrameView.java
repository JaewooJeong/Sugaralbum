
package com.kiwiple.imageframework.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;

import com.kiwiple.imageframework.Constants;
import com.kiwiple.imageframework.collage.DesignTemplate;
import com.kiwiple.imageframework.collage.ImageFrameInfo;
import com.kiwiple.imageframework.util.CollageRect;
import com.kiwiple.imageframework.util.TransformUtils;
import com.kiwiple.imageframework.view.ImageFrameView;

/**
 * 스티커 프레임 뷰
 */
class StickerFrameView extends ImageFrameView {
    // toolbox info
    protected CollageRect mScaleToolBox = new CollageRect();
    protected CollageRect mDeleteToolBox = new CollageRect();

    // 크기 조절 버튼
    protected Bitmap mScale;
    protected Bitmap mScaleSel;
    // 삭제 버튼
    protected Bitmap mDelete;
    protected Bitmap mDeleteSel;
    /**
     * 크기 조절 버튼 사용 여부
     */
    protected boolean mScaleToolBoxSelection;
    /**
     * 삭제 버튼 사용 여부
     */
    protected boolean mDeleteToolBoxSelection;
    /**
     * 크기 조절 버튼 위치 
     */
    protected int mScaleLocation;
    /**
     * 삭제 버튼 위치
     */
    protected int mDeleteLocation;
    
    protected DesignTemplate mDesignTemplate;
    
    /**
     * 편집 가능 여부, 일반적으로 디자인 템플릿에서는 편집 불가능
     */
    protected boolean mEnable = true;
    protected static Paint mTransparentPaint;

    protected Paint mImagePaint = new Paint();
    protected int mImageAlpha = 255;
    
    protected Bitmap[] mAnimatedImages;

    protected StickerFrameView(int mId, ImageFrameInfo mFrameInfo, Bitmap mFrameImage,
            CollageRect mFrameRect, Matrix mFrameMatrix, float mOriginalFrameScaleFactor,
            float mFrameScaleFactor, float mFrameMinScale, float mFrameMaxScale, Bitmap mImage,
            Matrix mImageMatrix, float mOriginalImageScaleFactor, float mImageScaleFactor,
            float mImageMinScale, float mImageMaxScale, int mOriginalImageRotation,
            float mCurrentImageRotation, boolean mImageFlip, CollageRect mSelectionRect,
            Paint mSelectionPaint, Paint mBackgroundPaint, Context mContext, float mExtraScale,
            CollageRect mScaleToolBox, CollageRect mDeleteToolBox, Bitmap mScale, Bitmap mScaleSel,
            Bitmap mDelete, Bitmap mDeleteSel, int mScaleLocation, int mDeleteLocation,
            DesignTemplate mDesignTemplate, boolean mEnable, Paint mImagePaint, int mImageAlpha,
            int mEditProgress) {
        super(mId, mFrameInfo, mFrameImage, mFrameRect, mFrameMatrix, mOriginalFrameScaleFactor,
              mFrameScaleFactor, mFrameMinScale, mFrameMaxScale, mImage, mImageMatrix,
              mOriginalImageScaleFactor, mImageScaleFactor, mImageMinScale, mImageMaxScale,
              mOriginalImageRotation, mCurrentImageRotation, mImageFlip, mSelectionRect,
              mSelectionPaint, mBackgroundPaint, mContext, mExtraScale, mEditProgress);
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
    }

    public StickerFrameView(Context context, int id, Bitmap scale, Bitmap scaleSel,
            int scaleLocation, Bitmap delete, Bitmap deleteSel, int deleteLocation,
            StickerFrameInfo info, DesignTemplate designTemplate) {
        super(context, id);
        mFrameInfo = info;
        if(mFrameInfo == null) {
            mFrameInfo = new StickerFrameInfo();
        }
        mScale = scale;
        mScaleSel = scaleSel;
        mDelete = delete;
        mDeleteSel = deleteSel;
        mImageMinScale = 0.1f;
        mImageMaxScale = 5.f;
        mScaleLocation = scaleLocation;
        mDeleteLocation = deleteLocation;
        mDesignTemplate = designTemplate;

        if(mTransparentPaint == null) {
            mTransparentPaint = new Paint();
            mTransparentPaint.setStyle(Style.FILL);
            mTransparentPaint.setColorFilter(new PorterDuffColorFilter(Color.RED, Mode.CLEAR));
        }
    }

    @Override
    public void initFrame(int width, int height) {
        super.initFrame(width, height);
        if(mImage != null) {
            mFrameRect.setWidth(mImage.getWidth());
            mFrameRect.setHeight(mImage.getHeight());

            mFrameMatrix.reset();
            if(mDesignTemplate != null) {
                float widthScale = mFrameInfo.mScale * mDesignTemplate.mLayoutWidthScaleFactor;
                float heightScale = mFrameInfo.mScale * mDesignTemplate.mLayoutHeightScaleFactor;
                scaleFrameLeftTop(widthScale < heightScale ? widthScale : heightScale);
                translateFrame(mFrameInfo.mCoordinateX * widthScale, mFrameInfo.mCoordinateY
                        * heightScale);
                rotateFrame(-mFrameInfo.mRotation);
            }

            // setup selection box
            mSelectionRect.set(mFrameRect);
            mSelectionRect.adjustBoundToFloorCeil();

            setScaleToolBoxRect(mScaleLocation);
            setDeleteToolBoxRect(mDeleteLocation);

            adjustFrameMinScale();

            initImage();
        }
    }

    /**
     * 초기 scale값 설정
     * 
     * @param defScale 초기 scale값
     */
    public void setDefaultScale(float defScale) {
        // super.scaleFrame(defScale);
        scaleFrame(defScale);
    }

    /**
     * 스티커 최소 크기 계산
     */
    protected void adjustFrameMinScale() {
        if(Constants.THIRD_PARTY) {
            return;
        }
        float minDiameter;
        if(mScale != null && mScaleSel != null && mDelete != null && mDeleteSel != null) {
            minDiameter = (float)((Math.sqrt(Math.pow(mScale.getWidth(), 2)
                    + Math.pow(mScale.getHeight(), 2)) + Math.sqrt(Math.pow(mDelete.getWidth(), 2)
                    + Math.pow(mDelete.getHeight(), 2))) / 2);
        } else {
            minDiameter = 20;
        }
        float imageDiameter = (float)Math.sqrt(Math.pow(mFrameRect.width(), 2)
                + Math.pow(mFrameRect.height(), 2));
        if(Float.isInfinite(mFrameMinScale) || mFrameMinScale < minDiameter / imageDiameter) {
            mFrameMinScale = minDiameter / imageDiameter;
        }
    }

    @Override
    public void setFrameScale(float minScale, float maxScale) {
        super.setFrameScale(minScale, maxScale);
        adjustFrameMinScale();
    }

    /**
     * 크기 조절 버튼 위치 설정
     */
    public void setScaleToolBoxRect(int location) {
        mScaleLocation = location;
        mScaleToolBox.setEmpty();
        if(mScale == null || mScaleSel == null) {
            return;
        }
        mScaleToolBox.setWidth(mScale.getWidth());
        mScaleToolBox.setHeight(mScale.getHeight());
        translateToolBox(mScaleToolBox, location);
        mScaleToolBox.translate(-mScale.getWidth() / 2, -mScale.getHeight() / 2);
        mScaleToolBox.scale(1 / mFrameScaleFactor, true);
    }

    /**
     * 삭제 버튼 위치 설정
     */
    public void setDeleteToolBoxRect(int location) {
        mDeleteLocation = location;
        mDeleteToolBox.setEmpty();
        if(mDelete == null || mDeleteSel == null) {
            return;
        }
        mDeleteToolBox.setWidth(mDelete.getWidth());
        mDeleteToolBox.setHeight(mDelete.getHeight());
        translateToolBox(mDeleteToolBox, location);
        mDeleteToolBox.translate(-mDelete.getWidth() / 2, -mDelete.getHeight() / 2);
        mDeleteToolBox.scale(1 / mFrameScaleFactor, true);
    }

    protected void translateToolBox(CollageRect toolbox, int location) {
        switch(location) {
            case StickerView.STICKER_BUTTON_LOCATION_LEFT_TOP:
                toolbox.translate(mFrameRect.left, mFrameRect.top);
                break;
            case StickerView.STICKER_BUTTON_LOCATION_RIGHT_TOP:
                toolbox.translate(mFrameRect.right, mFrameRect.top);
                break;
            case StickerView.STICKER_BUTTON_LOCATION_LEFT_BOTTOM:
                toolbox.translate(mFrameRect.left, mFrameRect.bottom);
                break;
            case StickerView.STICKER_BUTTON_LOCATION_RIGHT_BOTTOM:
                toolbox.translate(mFrameRect.right, mFrameRect.bottom);
                break;
        }
    }

    @Override
    protected void initImage() {
        mImageScaleFactor = mOriginalImageScaleFactor = 1.f;
        mImageFlip = false;
        mImageMatrix.reset();
    }

    /**
     * 가운데 위치 반환
     * 
     * @return 가운데 위치
     */
    public PointF getCenterPoint() {
        float[] points = new float[] {
                mFrameRect.centerX(), mFrameRect.centerY()
        };
        mFrameMatrix.mapPoints(points);
        return new PointF(points[0], points[1]);
    }

    private float getRadius(float scale) {
        return TransformUtils.getDiameter(mFrameRect.width() * scale, mFrameRect.height() * scale) / 2;
    }

    /**
     * 주어진 (x,y)값이 ScaleToolbox 내부 영역인지 여부 판단
     * 
     * @param x x좌표
     * @param y y좌표
     * @return Toolbox 내부 영역인지 판단
     */
    public boolean isInnerScaleToolboxPoint(float x, float y) {
        if(mScale == null || mScaleSel == null) {
            return false;
        }
        scaleToolBox(mExtraScale);
        try {
            float[] point = invertTransformPoints(mFrameMatrix, x, y);
            if(point != null && mScaleToolBox.contains(point[0], point[1])) {
                return true;
            }
        } finally {
            scaleToolBox(1.f / mExtraScale);
        }
        return false;
    }

    /**
     * 주어진 (x,y)값이 DeleteToolbox 내부 영역인지 여부 판단
     * 
     * @param x x좌표
     * @param y y좌표
     * @return Toolbox 내부 영역인지 판단
     */
    public boolean isInnerDeleteToolboxPoint(float x, float y) {
        if(mDelete == null || mDeleteSel == null) {
            return false;
        }
        scaleToolBox(mExtraScale);
        try {
            float[] point = invertTransformPoints(mFrameMatrix, x, y);
            if(point != null && mDeleteToolBox.contains(point[0], point[1])) {
                return true;
            }
        } finally {
            scaleToolBox(1.f / mExtraScale);
        }
        return false;
    }

    /**
     * 스티커 삭제 버튼 이미지 설정
     * 
     * @param normalImage 보통 이미지
     * @param pressedImage 선택시 이미지
     */
    public void setCloseImage(Bitmap normalImage, Bitmap pressedImage) {
        mDelete = normalImage;
        mDeleteSel = pressedImage;
        setDeleteToolBoxRect(mDeleteLocation);
    }

    /**
     * 스티커 확대 버튼 이미지 설정
     * 
     * @param normalImage 보통 이미지
     * @param pressedImage 선택시 이미지
     */
    public void setScaleImage(Bitmap normalImage, Bitmap pressedImage) {
        mScale = normalImage;
        mScaleSel = pressedImage;
        setScaleToolBoxRect(mScaleLocation);
    }

    /**
     * 크기 조절 버튼 선택 상태 업데이트
     */
    public void setScaleToolboxSelection(boolean selected) {
        mScaleToolBoxSelection = selected;
    }

    /**
     * 크기 조절 버튼 선택 여부
     */
    public boolean isScaleToolboxSelected() {
        return mScaleToolBoxSelection;
    }

    /**
     * 삭제 버튼 선택 상태 업데이트
     */
    public void setDeleteToolboxSelection(boolean selected) {
        mDeleteToolBoxSelection = selected;
    }

    /**
     * 삭제 버튼 선택 여부
     */
    public boolean isDeleteToolboxSelected() {
        return mDeleteToolBoxSelection;
    }

    /**
     * 반지름 길이로 크기 조절
     */
    public void scaleFrameByRadius(float currentRadius) {
        float radius = getRadius(mFrameScaleFactor);
        // if (Math.abs(currentRadius - radius) > 20) {
        // scaleFrame((currentRadius > radius ? radius + 20 : radius - 20) / radius);
        // } else {
        scaleFrame(currentRadius / radius);
        // }
    }

    /**
     * 편집 가능 설정
     */
    public void setEnable(boolean enable) {
        mEnable = enable;
    }

    /**
     * 편집 가능 여부
     */
    public boolean isEnable() {
        return mEnable;
    }

    /**
     * 이미지 투명도 설정
     * 
     * @param alpha 이미지 투명도
     */
    public void setImageAlpha(int alpha) {
        mImageAlpha = alpha;
    }

    /**
     * 애니메이션 이미지 설정
     * 
     * @param images 애니메이션 이미지 배열
     */
    public void setAnimatedImage(Bitmap[] images) {
        mAnimatedImages = images;
    }


    /**
     * 이미지의 투명도 반환
     * 
     * @return 이미지의 투명도
     */
    public int getImageAlpha() {
        return mImageAlpha;
    }

    @Override
    public boolean scaleFrame(float scale) {
        if(super.scaleFrame(scale)) {
            scaleToolBox(1 / scale);
            return true;
        }
        return false;
    }

    @Override
    public boolean scaleFrameLeftTop(float scale) {
        if(super.scaleFrameLeftTop(scale)) {
            scaleToolBox(1 / scale);
            return true;
        }
        return false;
    }

    /**
     * ToolBox의 크기 조절
     * 
     * @param scale 크기 값
     */
    public void scaleToolBox(float scale) {
        mScaleToolBox.scale(scale, true);
        mDeleteToolBox.scale(scale, true);
    }

    @Override
    public void drawBackground(Canvas canvas, boolean isOutput) {
    }

    @Override
    public void drawImage(Canvas canvas, Paint bitmapPaint, boolean isOutput) {
        if(mImage != null) {
            mImagePaint.set(bitmapPaint);
            mImagePaint.setAlpha(mImageAlpha);

            canvas.save();
            canvas.concat(mFrameMatrix);
            // 프레임영역에는 배경을 지운다.
            if(((StickerFrameInfo)mFrameInfo).mClipBackground) {
                canvas.save();
                canvas.scale(0.95f, 0.95f, mFrameRect.centerX(), mFrameRect.centerY());
                canvas.clipRect(mSelectionRect);
                canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
                canvas.restore();
                // bitmapPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
            }
            bitmapPaint.setAlpha(((StickerFrameInfo)mFrameInfo).mAlpha);
            canvas.drawBitmap(mImage, mImageMatrix, mImagePaint);
            canvas.restore();
        }
    }

    /**
     * Toolbox를 그림
     * 
     * @param canvas
     * @param paint
     */
    public void drawToolbox(Canvas canvas, Paint paint) {
        scaleToolBox(mExtraScale);
        if(mSelected) {
            canvas.save();
            canvas.concat(mFrameMatrix);
            if(mScale != null && mScaleSel != null) {
                canvas.save();
                if(mScaleToolBoxSelection) {
                    canvas.drawBitmap(mScaleSel, null, mScaleToolBox, paint);
                } else {
                    canvas.drawBitmap(mScale, null, mScaleToolBox, paint);
                }
                canvas.restore();
            }
            if(mDelete != null && mDeleteSel != null) {
                canvas.save();
                if(mDeleteToolBoxSelection) {
                    canvas.drawBitmap(mDeleteSel, null, mDeleteToolBox, paint);
                } else {
                    canvas.drawBitmap(mDelete, null, mDeleteToolBox, paint);
                }
                canvas.restore();
            }
            canvas.restore();
        }
        scaleToolBox(1.f / mExtraScale);
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public StickerFrameView copy() {
        return new StickerFrameView(mId, mFrameInfo, mFrameImage, mFrameRect,
                                    new Matrix(mFrameMatrix), mOriginalFrameScaleFactor,
                                    mFrameScaleFactor, mFrameMinScale, mFrameMaxScale, mImage,
                                    new Matrix(mImageMatrix), mOriginalImageScaleFactor,
                                    mImageScaleFactor, mImageMinScale, mImageMaxScale,
                                    mOriginalImageRotation, mCurrentImageRotation, mImageFlip,
                                    mSelectionRect, new Paint(mSelectionPaint),
                                    new Paint(mBackgroundPaint), mContext, mExtraScale,
                                    new CollageRect(mScaleToolBox),
                                    new CollageRect(mDeleteToolBox), mScale, mScaleSel, mDelete,
                                    mDeleteSel, mScaleLocation, mDeleteLocation, mDesignTemplate,
                                    mEnable, new Paint(mImagePaint), mImageAlpha, mEditProgress);
    }
}
