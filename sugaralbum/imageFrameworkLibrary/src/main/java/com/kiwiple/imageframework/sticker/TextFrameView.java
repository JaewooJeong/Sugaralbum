
package com.kiwiple.imageframework.sticker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.kiwiple.imageframework.collage.DesignTemplate;
import com.kiwiple.imageframework.collage.ImageFrameInfo;
import com.kiwiple.imageframework.util.CollageRect;

/**
 * 텍스트 스티커 프레임 뷰
 */
class TextFrameView extends StickerFrameView {
    private final static int DEFAULT_TEXT_SIZE = 25;
    private final static int DEFAULT_TEXT_STROKE_WIDTH = 2;
    private final static float DEFAULT_FRAME_SCALE = 1.f;
    private final static int DEFAULT_FRAME_WIDTH_MARGIN = 0;
    private final static int DEFAULT_FRAME_HEIGHT_MARGIN = 0;
    private final static float DEFAULT_TEXT_SCALE = 1f;

    protected CollageRect mEditTextToolBox = new CollageRect();
    // 편집 버튼 이미지
    private Bitmap mTextEdit;
    private Bitmap mTextEditSel;
    /**
     * 편집 버튼 사용 여부 
     */
    private boolean mEditTextToolBoxSelection;
    /**
     * 편집 버튼 위치
     */
    private int mTextEditLocation;

    private String mText;
    private Paint mTextFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mTextStrokePaint;

    private float mDensity;

    protected TextFrameView(int mId, ImageFrameInfo mFrameInfo, Bitmap mFrameImage,
            CollageRect mFrameRect, Matrix mFrameMatrix, float mOriginalFrameScaleFactor,
            float mFrameScaleFactor, float mFrameMinScale, float mFrameMaxScale, Bitmap mImage,
            Matrix mImageMatrix, float mOriginalImageScaleFactor, float mImageScaleFactor,
            float mImageMinScale, float mImageMaxScale, int mOriginalImageRotation,
            float mCurrentImageRotation, boolean mImageFlip, CollageRect mSelectionRect,
            Paint mSelectionPaint, Paint mBackgroundPaint, Context mContext, float mExtraScale,
            CollageRect mScaleToolBox, CollageRect mDeleteToolBox, Bitmap mScale, Bitmap mScaleSel,
            Bitmap mDelete, Bitmap mDeleteSel, int mScaleLocation, int mDeleteLocation,
            DesignTemplate mDesignTemplate, boolean mEnable, Paint mImagePaint, int mImageAlpha,
            CollageRect mEditTextToolBox, Bitmap mTextEdit, Bitmap mTextEditSel,
            int mTextEditLocation, String mText, Paint mTextFillPaint, Paint mTextStrokePaint,
            float mDensity, int mEditProgress) {
        super(mId, mFrameInfo, mFrameImage, mFrameRect, mFrameMatrix, mOriginalFrameScaleFactor,
              mFrameScaleFactor, mFrameMinScale, mFrameMaxScale, mImage, mImageMatrix,
              mOriginalImageScaleFactor, mImageScaleFactor, mImageMinScale, mImageMaxScale,
              mOriginalImageRotation, mCurrentImageRotation, mImageFlip, mSelectionRect,
              mSelectionPaint, mBackgroundPaint, mContext, mExtraScale, mScaleToolBox,
              mDeleteToolBox, mScale, mScaleSel, mDelete, mDeleteSel, mScaleLocation,
              mDeleteLocation, mDesignTemplate, mEnable, mImagePaint, mImageAlpha, mEditProgress);
        this.mEditTextToolBox = mEditTextToolBox;
        this.mTextEdit = mTextEdit;
        this.mTextEditSel = mTextEditSel;
        this.mTextEditLocation = mTextEditLocation;
        this.mText = mText;
        this.mTextFillPaint = mTextFillPaint;
        this.mTextStrokePaint = mTextStrokePaint;
        this.mDensity = mDensity;
    }

    public TextFrameView(Context context, int id, Bitmap scale, Bitmap scaleSel, int scaleLocation,
            Bitmap delete, Bitmap deleteSel, int deleteLocation, Bitmap textEdit,
            Bitmap textEditSel, int textEditLocation) {
        super(context, id, scale, scaleSel, scaleLocation, delete, deleteSel, deleteLocation, null,
              null);
        mTextEdit = textEdit;
        mTextEditSel = textEditSel;
        mTextEditLocation = textEditLocation;

        mDensity = context.getResources().getDisplayMetrics().density;
        mTextFillPaint.setTextSize(DEFAULT_TEXT_SIZE * mDensity);
        mTextFillPaint.setStrokeWidth(DEFAULT_TEXT_STROKE_WIDTH);
        mTextFillPaint.setStyle(Style.FILL_AND_STROKE);
        mTextFillPaint.setTypeface(Typeface.DEFAULT);
        mTextFillPaint.setColor(Color.BLACK);
        mTextFillPaint.setTextAlign(Align.CENTER);

        mTextStrokePaint = new Paint(mTextFillPaint);
        mTextStrokePaint.setStyle(Style.STROKE);
        mTextStrokePaint.setColor(Color.TRANSPARENT);
        mTextStrokePaint.setStrokeWidth(DEFAULT_TEXT_STROKE_WIDTH * 2);
    }

    /**
     * 텍스트 스티커의 정보를 셋팅
     * 
     * @param info 텍스트 스티커 정보
     */
    public void initWithInfo(TextStickerFrameInfo info) {
        setText(info.mText);
        setTextColor(info.mFontColor);
        // mTextPaint.setTextSize(info.mFontSize * mDensity);
        initFrame(mLayoutWidth, mLayoutHeight);
        scaleFrameLeftTop(info.mFontSize / (DEFAULT_TEXT_SIZE * mDensity));
        translateFrame(info.mCoordinateX, info.mCoordinateY);
        rotateFrame(info.mRotation);
    }

    @Override
    public void initFrame(int width, int height) {
        super.initFrame(width, height);
        mFrameMatrix.reset();
        scaleFrame(DEFAULT_FRAME_SCALE);
    }

    private void initText() {
        float width = 0;
        float height = 0;
        float tempWidth = 0;
        for(String line : mText.split("\n")) {
            tempWidth = mTextFillPaint.measureText(line);
            if(tempWidth > width) {
                width = tempWidth;
            }
            height += mTextFillPaint.descent() - mTextFillPaint.ascent();
        }
        mFrameRect.setWidth(width + DEFAULT_FRAME_WIDTH_MARGIN * mDensity);
        mFrameRect.setHeight(height + DEFAULT_FRAME_HEIGHT_MARGIN * mDensity);

        // setup selection box
        mSelectionRect.set(mFrameRect);
        mSelectionRect.adjustBoundToFloorCeil();

        setScaleToolBoxRect(mScaleLocation);
        setDeleteToolBoxRect(mDeleteLocation);
        setEditTextToolBoxRect(mTextEditLocation);
    }

    protected void setEditTextToolBoxRect(int location) {
        mTextEditLocation = location;
        mEditTextToolBox.setEmpty();
        if(mTextEdit == null || mTextEditSel == null) {
            return;
        }
        mEditTextToolBox.setWidth(mTextEdit.getWidth());
        mEditTextToolBox.setHeight(mTextEdit.getHeight());
        translateToolBox(mEditTextToolBox, location);
        mEditTextToolBox.translate(-mTextEdit.getWidth() / 2, -mTextEdit.getHeight() / 2);
        mEditTextToolBox.scale(1 / mFrameScaleFactor, true);
    }

    /**
     * 편집화면의 이미지 설정
     * 
     * @param normalImage normal시 이미지
     * @param pressedImage press시 이미지
     */
    public void setEditTextImage(Bitmap normalImage, Bitmap pressedImage) {
        mTextEdit = normalImage;
        mTextEditSel = pressedImage;
        setEditTextToolBoxRect(mTextEditLocation);
    }

    /**
     * 문자열을 설정
     * 
     * @param text 문자열
     */
    public void setText(String text) {
        if(text == null) {
            text = "";
        }
        mText = text;
        initText();
    }

    /**
     * 설정된 문자열 반환
     * 
     * @return 설정된 문자열
     */
    public String getText() {
        return mText;
    }

    /**
     * 스타일 설정
     * 
     * @param style 설정할 스타일
     */
    public void setStyle(Style style) {
        mTextFillPaint.setStyle(style);
    }

    /**
     * 설정된 스타일 반환
     * 
     * @return 텍스트 스타일
     */
    public Style getStyle() {
        return mTextFillPaint.getStyle();
    }

    /**
     * Typeface 설정
     * 
     * @param typeface 설정할 폰트
     */
    public void setTextTypeface(Typeface typeface) {
        mTextFillPaint.setTypeface(typeface);
        mTextStrokePaint.setTypeface(typeface);
        initText();
    }

    /**
     * 텍스트 컬러 설정
     * 
     * @param color 텍스트 컬러
     */
    public void setTextColor(int color) {
        mTextFillPaint.setColor(color);
    }

    /**
     * 텍스트 테두리 색상 설정
     * 
     * @param color 텍스트 테두리 색상
     */
    public void setTextBorderColor(int color) {
        mTextStrokePaint.setColor(color);
    }

    /**
     * 텍스트 테두리 두께 설정
     * 
     * @param width 텍스트 테두리 두께
     */
    public void setTextBorderWidth(float width) {
        mTextStrokePaint.setStrokeWidth(width);
    }

    /**
     * 텍스트 두께 설정
     * 
     * @param width 텍스트 두께
     */
    public void setTextWidth(float width) {
        mTextFillPaint.setStrokeWidth(width);
    }

    /**
     * 텍스트 테두리 두께 반환
     * 
     * @return 텍스트 테두리 두께
     */
    public float getTextBorderWidth() {
        return mTextStrokePaint.getStrokeWidth();
    }

    /**
     * 텍스트 두께 반환
     * 
     * @return 텍스트 두께
     */
    public float getTextFillWidth() {
        return mTextFillPaint.getStrokeWidth();
    }

    /**
     * 텍스트 색상 반환
     * 
     * @return 텍스트 색상
     */
    public int getTextColor() {
        return mTextFillPaint.getColor();
    }

    /**
     * 텍스트 테두리 두께 색상을 반환
     * 
     * @return 텍스트 테두리 두께 색상
     */
    public int getTextBorderColor() {
        return mTextStrokePaint.getColor();
    }

    /**
     * 텍스트 배경 이미지 설정
     * 
     * @param background 배경 이미지 비트맵
     */
    public void setTextBackground(Bitmap background) {
        mFrameImage = background;
    }

    /**
     * Toolbox 선택 여부 설정
     * 
     * @param selected 선택 여부
     */
    public void setEditTextToolboxSelection(boolean selected) {
        mEditTextToolBoxSelection = selected;
    }

    /**
     * Toolbox 선택 여부 반환
     * 
     * @return Toolbox 선택 여부
     */
    public boolean isEditTextToolboxSelected() {
        return mEditTextToolBoxSelection;
    }

    /**
     * 텍스트의 가로 길이 반환
     * 
     * @return 텍스트의 가로 길이
     */
    public float getTextWidth() {
        return mFrameRect.width() - DEFAULT_FRAME_WIDTH_MARGIN * mDensity;
    }

    /**
     * 텍스트의 세로 길이 반환
     * 
     * @return 텍스트의 세로 길이
     */
    public float getTextHeight() {
        return mFrameRect.height() - DEFAULT_FRAME_HEIGHT_MARGIN * mDensity;
    }

    /**
     * 캔버스에 텍스트를 그린다
     * 
     * @param canvas
     */
    public void drawText(Canvas canvas) {
        if(!TextUtils.isEmpty(mText)) {
            canvas.save();
            canvas.concat(mFrameMatrix);
            canvas.scale(DEFAULT_TEXT_SCALE, DEFAULT_TEXT_SCALE, mFrameRect.centerX(),
                         mFrameRect.centerY());
            float y = -mTextFillPaint.ascent();
            float x = 0;
            if(mTextFillPaint.getTextAlign() == Align.CENTER) {
                x = mFrameRect.width() / 2;
            } else if(mTextFillPaint.getTextAlign() == Align.RIGHT) {
                x = mFrameRect.width();
            } else {
                x = 0;
            }
            for(String line : mText.split("\n")) {
                canvas.drawText(line, x, y, mTextStrokePaint);
                canvas.drawText(line, x, y, mTextFillPaint);
                y += mTextFillPaint.descent() - mTextFillPaint.ascent();
            }
            canvas.restore();
        }
    }

    @Override
    public void drawFrame(Canvas canvas, boolean isOutput) {
        if(mFrameImage != null) {
            canvas.save();
            canvas.concat(mFrameMatrix);
            canvas.scale(mFrameRect.width() / mFrameImage.getWidth(), mFrameRect.height()
                    / mFrameImage.getHeight());
            canvas.drawBitmap(mFrameImage, 0, 0, null);
            canvas.restore();
        }
    }

    /**
     * 주어진 좌표가 TextToolbox 내부인지 여부 판단
     * 
     * @param x 주어진 x좌표
     * @param y 주어진 y좌표
     * @return Toolbox 내부 여부
     */
    public boolean isInnerTextEditToolboxPoint(float x, float y) {
        if(mTextEdit == null || mTextEditSel == null) {
            return false;
        }
        float[] point = invertTransformPoints(mFrameMatrix, x, y);
        if(point != null && mEditTextToolBox.contains(point[0], point[1])) {
            return true;
        }
        return false;
    }

    @Override
    public boolean scaleFrame(float scale) {
        if(super.scaleFrame(scale)) {
            mEditTextToolBox.scale(1 / scale, true);
            return true;
        }
        return false;
    }

    @Override
    public void drawBackground(Canvas canvas, boolean isOutput) {
    }

    @Override
    public void drawImage(Canvas canvas, Paint bitmapPaint, boolean isOutput) {
    }

    @Override
    public void drawToolbox(Canvas canvas, Paint paint) {
        super.drawToolbox(canvas, paint);
        if(mSelected) {
            if(mTextEdit != null && mTextEditSel != null) {
                canvas.save();
                canvas.concat(mFrameMatrix);
                if(mEditTextToolBoxSelection) {
                    canvas.drawBitmap(mTextEditSel, null, mEditTextToolBox, paint);
                } else {
                    canvas.drawBitmap(mTextEdit, null, mEditTextToolBox, paint);
                }
                canvas.restore();
            }
        }
    }

    @Override
    public TextFrameView copy() {
        return new TextFrameView(mId, mFrameInfo, mFrameImage, mFrameRect,
                                 new Matrix(mFrameMatrix), mOriginalFrameScaleFactor,
                                 mFrameScaleFactor, mFrameMinScale, mFrameMaxScale, mImage,
                                 new Matrix(mImageMatrix), mOriginalImageScaleFactor,
                                 mImageScaleFactor, mImageMinScale, mImageMaxScale,
                                 mOriginalImageRotation, mCurrentImageRotation, mImageFlip,
                                 mSelectionRect, new Paint(mSelectionPaint),
                                 new Paint(mBackgroundPaint), mContext, mExtraScale,
                                 new CollageRect(mScaleToolBox), new CollageRect(mDeleteToolBox),
                                 mScale, mScaleSel, mDelete, mDeleteSel, mScaleLocation,
                                 mDeleteLocation, mDesignTemplate, mEnable, new Paint(mImagePaint),
                                 mImageAlpha, new CollageRect(mEditTextToolBox), mTextEdit,
                                 mTextEditSel, mTextEditLocation, new String(mText),
                                 new Paint(mTextFillPaint), new Paint(mTextStrokePaint), mDensity,
                                 mEditProgress);
    }
}
