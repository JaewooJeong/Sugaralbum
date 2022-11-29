
package com.kiwiple.imageframework.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import com.kiwiple.imageframework.collage.ImageFrameInfo;
import com.kiwiple.imageframework.util.CollageRect;
import com.kiwiple.imageframework.util.TransformUtils;

public abstract class ImageFrameView {
    protected static final String TAG = ImageFrameView.class.getSimpleName();
    protected int mId;
    protected List<Integer> mRelativeIds = new ArrayList<Integer>();
    protected int mEditProgress;
    // frame layout info
    protected ImageFrameInfo mFrameInfo;
    protected Bitmap mFrameImage;
    protected CollageRect mFrameRect = new CollageRect();
    protected CollageRect mComputedFrameRect = new CollageRect();
    protected Matrix mFrameMatrix = new Matrix();
    protected Matrix mInvertedFrameMatrix = new Matrix();
    protected float mOriginalFrameScaleFactor = 1.f;
    protected float mFrameScaleFactor = 1.f;
    /**
     * U+Story<br>
     * 스티커의 이동 값을 저장하기 위한 변수
     */
    protected float mFrameTranslateXFactor = 0.f;
    /**
     * U+Story<br>
     * 스티커의 이동 값을 저장하기 위한 변수
     */
    protected float mFrameTranslateYFactor = 0.f;
    /**
     * U+Story<br>
     * 스티커의 회전 값을 저장하기 위한 변수
     */
    protected float mFrameRotateFactor = 0.f;
    protected float mFrameMinScale = .2f;
    protected float mFrameMaxScale = 4.0f;

    // image transform info
    protected Bitmap mImage;
    protected Matrix mImageMatrix = new Matrix();
    protected float mOriginalImageScaleFactor = 1.f;
    protected float mImageScaleFactor = 1.f;
    protected float mImageMinScale = 1.f;
    protected float mImageMaxScale = 2.5f;
    protected int mOriginalImageRotation = 0;
    protected float mCurrentImageRotation = 0;
    protected float mImageTranslateXFactor = 0.f;
    protected float mImageTranslateYFactor = 0.f;
    protected boolean mImageFlip;

    // selection box
    protected boolean mSelected;
    protected boolean mSubSelected;
    protected CollageRect mSelectionRect = new CollageRect();
    protected Paint mSelectionPaint = new Paint();

    protected int mLayoutWidth;
    protected int mLayoutHeight;

    protected Paint mBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG
            | Paint.FILTER_BITMAP_FLAG);

    protected Context mContext;

    protected float mExtraScale = 1.f;
    
    protected float mFrameWidthScale;
    protected float mFrameHeightScale;
    
    protected ImageFrameView(int mId, ImageFrameInfo mFrameInfo, Bitmap mFrameImage,
            CollageRect mFrameRect, Matrix mFrameMatrix, float mOriginalFrameScaleFactor,
            float mFrameScaleFactor, float mFrameMinScale, float mFrameMaxScale, Bitmap mImage,
            Matrix mImageMatrix, float mOriginalImageScaleFactor, float mImageScaleFactor,
            float mImageMinScale, float mImageMaxScale, int mOriginalImageRotation,
            float mCurrentImageRotation, boolean mImageFlip, CollageRect mSelectionRect,
            Paint mSelectionPaint, Paint mBackgroundPaint, Context mContext, float mExtraScale,
            int mEditProgress) {
        super();
        this.mId = mId;
        this.mFrameInfo = mFrameInfo;
        this.mFrameImage = mFrameImage;
        this.mFrameRect = mFrameRect;
        this.mFrameMatrix = mFrameMatrix;
        this.mOriginalFrameScaleFactor = mOriginalFrameScaleFactor;
        this.mFrameScaleFactor = mFrameScaleFactor;
        this.mFrameMinScale = mFrameMinScale;
        this.mFrameMaxScale = mFrameMaxScale;
        this.mImage = mImage;
        this.mImageMatrix = mImageMatrix;
        this.mOriginalImageScaleFactor = mOriginalImageScaleFactor;
        this.mImageScaleFactor = mImageScaleFactor;
        this.mImageMinScale = mImageMinScale;
        this.mImageMaxScale = mImageMaxScale;
        this.mOriginalImageRotation = mOriginalImageRotation;
        this.mCurrentImageRotation = mCurrentImageRotation;
        this.mImageFlip = mImageFlip;
        this.mSelectionRect = mSelectionRect;
        this.mSelectionPaint = mSelectionPaint;
        this.mBackgroundPaint = mBackgroundPaint;
        this.mContext = mContext;
        this.mExtraScale = mExtraScale;
        this.mEditProgress = mEditProgress;
    }

    public ImageFrameView(Context context, int id) {
        mId = id;
        mContext = context;
    }

    public void initFrame(int width, int height) {
        mLayoutWidth = width;
        mLayoutHeight = height;
    }

    protected abstract void initImage();

    /**
     * 프레임의 Rect를 반환한다.
     * 
     * @return 프레임의 Rect
     */
    public Rect getFrameRect() {
        mFrameMatrix.mapRect(mComputedFrameRect, mFrameRect);
        return mComputedFrameRect.getRect();
    }

    /**
     * 프레임의 Bounds를 반환
     * 
     * @return 프레임의 Bounds
     */
    public Rect getFrameBounds() {
        return mFrameRect.getRect();
    }
    
    /**
     * 이미지를 설정
     * 
     * @param image 설정할 비트맵 이미지
     */
    public void setImage(Bitmap image) {
        mImage = image;
        if(mImage != null) {
            initImage();
        }
    }

    /**
     * 이미지 교체
     * 
     * @param image 설정할 비트맵 이미지
     */
    public void changeImage(Bitmap image) {
        mImage = image;
    }

    /**
     * 이미지의 기본 회전 값을 설정한다.
     * 
     * @param rotation 회전 각도 (0, 90, 180, 270)
     */
    public void setBaseImageRotation(int rotation) {
        mOriginalImageRotation = rotation;
    }

    /**
     * 설정된 이미지의 기본 회전 값을 반환
     * 
     * @return 이미지의 회전 값
     */
    public int getBaseImageRotation() {
        return mOriginalImageRotation;
    }

    /**
     * 이미지가 설정되어있는지 여부 체크
     * 
     * @return 이미지의 설정 여부
     */
    public boolean hasImage() {
        return mImage != null;
    }

    /**
     * 설정된 이미지의 반환
     * 
     * @return 이미지 비트맵
     */
    public Bitmap getImage() {
        return mImage;
    }

    /**
     * 설정된 이미지를 설정 값 만큼 이동한다.
     * 
     * @param distanceX x 이동 거리
     * @param distanceY y 이동 거리
     */
    public void translateImage(float distanceX, float distanceY) {
        mImageTranslateXFactor += TransformUtils.rotateX(distanceX, distanceY,
                                                         -(mFrameInfo.mRotation));
        mImageTranslateYFactor += TransformUtils.rotateY(distanceX, distanceY,
                                                         -(mFrameInfo.mRotation));
        mImageMatrix.postTranslate(TransformUtils.rotateX(distanceX, distanceY,
                                                          -(mFrameInfo.mRotation)),
                                   TransformUtils.rotateY(distanceX, distanceY,
                                                          -(mFrameInfo.mRotation)));
    }

    /**
     * 현재 적용되어 있는 x축의 이동 값을 반환
     * 
     * @return x 좌표값
     */
    public float getCurrentTranslateXImage() {
        return mImageTranslateXFactor;
    }

    /**
     * 현재 적용되어 있는 y축의 이동 값을 반환
     * 
     * @return
     */
    public float getCurrentTranslateYImage() {
        return mImageTranslateYFactor;
    }

    /**
     * 최소, 최대 스케일 값을 설정
     * 
     * @param minScale 최소 스케일 값
     * @param maxScale 최대 스케일 값
     */
    public void setImageScale(float minScale, float maxScale) {
        mImageMinScale = minScale;
        mImageMaxScale = maxScale;
    }

    /**
     * 이미지를 Scale시킨다
     * 
     * @param scale Scale값
     */
    public void scaleImage(float scale) {
        if(mImageScaleFactor * scale < mImageMinScale || mImageScaleFactor * scale > mImageMaxScale) {
            return;
        }
        mImageScaleFactor *= scale;
        mImageMatrix.preScale(scale, scale, mImage.getWidth() / 2, mImage.getHeight() / 2);
    }

    /**
     * U+Story<br>
     * 초기 scale 설정 값을 가져오기 위한 메소드 추가
     * 
     * @return float 초기 scale값
     */
    public float getOritinalScaleImage() {
        return mOriginalImageScaleFactor;
    }

    /**
     * 현재 Scale 설정 값 반환
     * 
     * @return 현재 scale값
     */
    public float getCurrentScaleImage() {
        return mImageScaleFactor;
    }
    
    public float[] getFrameScale() {
        return new float[]{mFrameWidthScale * mFrameScaleFactor, mFrameHeightScale * mFrameScaleFactor};
    }

    /**
     * 이미지를 rotate
     * 
     * @param degree rotate 각도
     */
    public void rotateImage(float degree) {
        mCurrentImageRotation -= degree;
        if(mCurrentImageRotation < 0) {
            mCurrentImageRotation += 360;
        }
        if(mCurrentImageRotation >= 360) {
            mCurrentImageRotation %= 360;
        }
        mImageMatrix.preRotate(mImageFlip ? degree : -degree, mImage.getWidth() / 2,
                               mImage.getHeight() / 2);
    }

    /**
     * 현재 rotate 값을 반환
     * 
     * @return rotate값
     */
    public float getCurrentRotateImage() {
        return mCurrentImageRotation;
    }

    /**
     * 이미지를 좌우로 반전
     */
    public void flipImage() {
        if(mOriginalImageRotation == 0 || mOriginalImageRotation == 180) {
            mImageMatrix.preTranslate(mImage.getWidth(), 0);
            mImageMatrix.preScale(-1.0f, 1.0f);
        } else {
            mImageMatrix.preTranslate(0, mImage.getHeight());
            mImageMatrix.preScale(1.0f, -1.0f);
        }
        mImageFlip = !mImageFlip;
    }

    /**
     * 프레임을 이동 시킨다.
     * 
     * @param distanceX x축 이동 값
     * @param distanceY y축 이동 값
     */
    public void translateFrame(float distanceX, float distanceY) {
        /**
         * U+Story<br>
         * 변수에 이동 거리 값 저장
         */
        mFrameTranslateXFactor += distanceX;
        mFrameTranslateYFactor += distanceY;
        mFrameMatrix.postTranslate(distanceX, distanceY);
    }

    /**
     * 프레임을 이동 시킨다.
     * 
     * @param dx x축 이동 값
     * @param dy y축 이동 값
     */
    public void setTranslateFrame(float dx, float dy) {
        mFrameMatrix.setTranslate(dx, dy);
    }

    /**
     * U+Story<br>
     * 스티커의 이동 좌표를 반환하기 위한 메소드(x값)
     * 
     * @return float 스티커 이동 값
     */
    public float getCurrentFrameTranslateX() {
        return mFrameTranslateXFactor;
    }

    /**
     * U+Story<br>
     * 스티커의 이동 좌표를 반환하기 위한 메소드(y값)
     * 
     * @return float 스티커 이동 값
     */
    public float getCurrentFrameTranslateY() {
        return mFrameTranslateYFactor;
    }

    /**
     * 프레임의 최소, 최대 scale값을 설정
     * 
     * @param minScale 최소 scale
     * @param maxScale 최대 scale
     */
    public void setFrameScale(float minScale, float maxScale) {
        mFrameMinScale = minScale;
        mFrameMaxScale = maxScale;
    }

    /**
     * 프레임을 확대/축소 시킨다
     * 
     * @param scale scale
     * @return 확대/축소 가능 여부
     */
    public boolean scaleFrame(float scale) {
        if(mFrameScaleFactor * scale < mOriginalFrameScaleFactor * mFrameMinScale
                || mFrameScaleFactor * scale > mOriginalFrameScaleFactor * mFrameMaxScale) {
            return false;
        }
        // mFrameTranslateXFactor += mFrameRect.width()
        // * (mFrameScaleFactor - mFrameScaleFactor * scale) / 2;
        // mFrameTranslateYFactor += mFrameRect.height()
        // * (mFrameScaleFactor - mFrameScaleFactor * scale) / 2;

        mFrameScaleFactor *= scale;
        mFrameMatrix.preScale(scale, scale, mFrameRect.width() / 2, mFrameRect.height() / 2);
        return true;
    }

    /**
     * 좌상단 기준으로 scale
     */
    protected boolean scaleFrameLeftTop(float scale) {
        if(mFrameScaleFactor * scale < mOriginalFrameScaleFactor * mFrameMinScale
                || mFrameScaleFactor * scale > mOriginalFrameScaleFactor * mFrameMaxScale) {
            return false;
        }
        mFrameScaleFactor *= scale;
        mFrameMatrix.preScale(scale, scale);
        return true;
    }

    /**
     * 현재 프레임의 scale값을 반환
     * 
     * @return 현재 프레임의 scale값
     */
    public float getCurrentFrameScale() {
        return mFrameScaleFactor;
    }

    /**
     * 초기 프레임의 scale값을 반환
     * 
     * @return 초기 프레임의 scale값
     */
    public float getDefaultFrameScale() {
        return mOriginalFrameScaleFactor;
    }

    public void setExtraScale(float scale) {
        mExtraScale = scale;
    }

    /**
     * 프레임을 회전시킨다
     * 
     * @param degree 회전 값
     */
    public void rotateFrame(float degree) {
        /**
         * U+Story<br>
         * rotation 값 저장
         */
        mFrameRotateFactor += degree;
        mFrameMatrix.preRotate(-degree, mFrameRect.width() / 2, mFrameRect.height() / 2);
    }

    /**
     * U+Story<br>
     * 회전의 축이 좌측 상단에 맞춰서 회전하도록
     * 
     * @param degree
     */
    public void rotateFrameLeftTop(float degree) {
        mFrameRotateFactor += degree;
        mFrameMatrix.preRotate(-degree);
    }

    /**
     * U+Story<br>
     * 스티커의 회전 값을 반환하기 위한 메소드
     * 
     * @return float 스티커의 회전 값
     */
    public float getCurrentFrameRotate() {
        return mFrameRotateFactor;
    }

    /**
     * 현재 프레임의 매트릭스를 배열로 반환
     * 
     * @return 프레임 매트릭스 값 배열
     */
    public float[] getFrameMatrixValues() {
        float[] values = new float[9];
        mFrameMatrix.getValues(values);
        return values;
    }

    /**
     * 설정된 (x,y) 지점이 프레임의 내부영역인지 여부 판단
     * 
     * @param x x지점
     * @param y y지점
     * @return 프레임 내부 영역 여부
     */
    public boolean isInnterPoint(float x, float y) {
        float[] point = invertTransformPoints(mFrameMatrix, x, y);
        if(point != null && mFrameRect.contains(point[0], point[1])) {
            return true;
        }
        return false;
    }

    /**
     * x,y좌표를 역행렬에 매핑
     */
    protected float[] invertTransformPoints(Matrix origin, float x, float y) {
        if(origin.invert(mInvertedFrameMatrix)) {
            float[] points = new float[] {
                    x, y
            };
            mInvertedFrameMatrix.mapPoints(points);
            return points;
        }
        return null;
    }

    /**
     * 선택
     */
    public void setSelected(boolean selected) {
        mSelected = selected;
    }

    /**
     * 선택 여부
     */
    public boolean isSelected() {
        return mSelected;
    }

    /**
     * 연관 프레임으로 선택
     */
    public void setSubSelected(int relativeId) {
        if(mRelativeIds.contains(relativeId)) {
            setSubSelected(true);
        } else {
            setSubSelected(false);
        }
    }

    /**
     * 연관 프레임으로 선택
     */
    public void setSubSelected(boolean selected) {
        if(selected) {
            setSelected(false);
        }
        mSubSelected = selected;
    }

    /**
     * 연관 프레임으로 선택되어 있는지 여부
     */
    public boolean isSubSelected() {
        return mSubSelected;
    }

    /**
     * 연관 프레이미 추가
     */
    public void addRelativeSticker(int relativeId) {
        mRelativeIds.add(relativeId);
    }

    /**
     * 아이디를 반환
     * 
     * @return 아이디
     */
    public int getId() {
        return mId;
    }

    /**
     * 프레임 그리기
     */
    public void drawFrame(Canvas canvas, boolean isOutput) {
        if(mFrameImage != null) {
            canvas.drawBitmap(mFrameImage, mFrameMatrix, null);
        }
    }

    /**
     * 배경 그리기
     */
    public abstract void drawBackground(Canvas canvas, boolean isOutput);

    /**
     * 이미지 그리기
     */
    public abstract void drawImage(Canvas canvas, Paint bitmapPaint, boolean isOutput);

    /**
     * 선택 사각 박스 그리기
     */
    public void drawSelection(Canvas canvas, Paint selectionPaint) {
        canvas.save();
        canvas.concat(mFrameMatrix);
        mSelectionPaint.set(selectionPaint);
        mSelectionPaint.setStrokeWidth(mSelectionPaint.getStrokeWidth() / mFrameScaleFactor
                * mExtraScale);
        canvas.drawRect(mSelectionRect, mSelectionPaint);
        canvas.restore();
    }

    /**
     * 이미지를 초기화한다.
     */
    public void clear() {
        if(mFrameImage != null && !mFrameImage.isRecycled()) {
            mFrameImage.recycle();
            mFrameImage = null;
        }
        mSelected = false;
    }

    @Override
    public int hashCode() {
        return mId;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ImageFrameView && ((ImageFrameView)o).mId == mId;
    }

    public abstract ImageFrameView copy();

    /**
     * 편집 index
     */
    public void setEditProgress(int editProgress) {
        mEditProgress = editProgress;
    }

    public int getEditProgress() {
        return mEditProgress;
    }
}
