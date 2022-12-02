
package com.kiwiple.imageframework.cosmetic;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.almeros.android.multitouch.gesturedetectors.MoveGestureDetector;
import com.kiwiple.imageframework.util.FileUtils;
import com.kiwiple.imageframework.util.TransformUtils;

/**
 * 얼굴 인식이 실패할 때 얼굴의 눈, 입, 턱의 위치를 수동으로 조정하는 기능을 지원하는
 * {@link com.kiwiple.imageframework.cosmetic#BeautyEffectImageView} 상속 클래스
 * 
 * @version 2.0
 */
public class SelectFaceImageView extends BeautyEffectImageView {
    /**
     * 눈(좌/우), 입, 턱 가이드 이미지
     */
    private Bitmap[] mObject = new Bitmap[4];
    /**
     * mObject의 위치/크기/회전 정보
     */
    private Matrix[] mObjectMatrix = new Matrix[4];
    /**
     * mObjectMatrix에서 크기 정보만 별도로 저장. 회전/크기 버튼을 그릴 때 사용한다.
     */
    private float[] mScale = new float[4];

    /**
     * 선택된 객체의 사각 테두리 모양
     */
    private Path[] mObjectPath = new Path[4];

    private Bitmap mRotationToolNormal;
    private Bitmap mRotationToolPress;

    /**
     * mRotationToolNormal, mRotationToolPress의 위치/크기/회전 정보
     */
    private Matrix[] mToolBoxMatrix = new Matrix[4];

    /**
     * mObjectPath의 위치/크기/회전 정보
     */
    private Matrix[] mSelectionMatrix = new Matrix[4];
    /**
     * 선택된 객체의 사각 테두리 크기
     */
    private RectF mSelectionRect = new RectF();
    private boolean mToolBoxSelected = false;

    private Paint mImagePaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private Paint mOutlinePaint;

    private static final int OBJECT_LEFT_EYE = 0;
    private static final int OBJECT_RIGHT_EYE = 1;
    private static final int OBJECT_MOUSE = 2;
    private static final int OBJECT_CHIN = 3;
    private int mSelectedObject = -1;

    private GestureDetector mGestureDetector;
    private MoveGestureDetector mMoveDetector;

    private Face mFace;

    public SelectFaceImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public SelectFaceImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SelectFaceImageView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        mObject[OBJECT_LEFT_EYE] = BitmapFactory.decodeResource(getResources(),
                                                                FileUtils.getBitmapResourceId(getContext(),
                                                                                              "img_face_setting_01"));
        mObject[OBJECT_RIGHT_EYE] = mObject[OBJECT_LEFT_EYE];
        mObject[OBJECT_MOUSE] = BitmapFactory.decodeResource(getResources(),
                                                             FileUtils.getBitmapResourceId(getContext(),
                                                                                           "img_face_setting_02"));
        mObject[OBJECT_CHIN] = BitmapFactory.decodeResource(getResources(),
                                                            FileUtils.getBitmapResourceId(getContext(),
                                                                                          "img_face_setting_03"));
        mRotationToolNormal = BitmapFactory.decodeResource(getResources(),
                                                           FileUtils.getBitmapResourceId(getContext(),
                                                                                         "sticker_rotate_nor"));
        mRotationToolPress = BitmapFactory.decodeResource(getResources(),
                                                          FileUtils.getBitmapResourceId(getContext(),
                                                                                        "sticker_rotate_sel"));

        mGestureDetector = new GestureDetector(getContext().getApplicationContext(),
                                               new SimpleGestureListener());
        mMoveDetector = new MoveGestureDetector(getContext().getApplicationContext(),
                                                new MoveListener());

        for(int i = 0; i < mObjectMatrix.length; i++) {
            mObjectMatrix[i] = new Matrix();
        }
        for(int i = 0; i < mToolBoxMatrix.length; i++) {
            mToolBoxMatrix[i] = new Matrix();
        }
        for(int i = 0; i < mSelectionMatrix.length; i++) {
            mSelectionMatrix[i] = new Matrix();
        }
        for(int i = 0; i < mObjectPath.length; i++) {
            mObjectPath[i] = new Path();
        }
        for(int i = 0; i < mScale.length; i++) {
            mScale[i] = 1f;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(isInEditMode()) {
            return;
        }
        float density = getResources().getDisplayMetrics().density;
        if(mOutlinePaint == null) {
            mOutlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mOutlinePaint.setStyle(Style.STROKE);
            mOutlinePaint.setColor(Color.WHITE);
            mOutlinePaint.setStrokeWidth(1.5f * getResources().getDisplayMetrics().density);
            mSelectionRect.set(0, 0, 79 * density, 79 * density);
        }

        mRotation = 0;
        for(int i = 0; i < mObjectMatrix.length; i++) {
            mObjectMatrix[i].reset();
        }
        float leftEyeScale = 1f;
        float rightEyeScale = 1f;
        float mouseScale = 1f;
        float chinScale = 1f;
        if(mFace != null) {
            mFaceBound.set(mFace.mFaceBound);
            mChinBound.set(mFace.mChinBound);
            mCheekBound.set(mFace.mCheekBound);
            mLeftEyeBound.set(mFace.mLeftEyeBound);
            mRightEyeBound.set(mFace.mRightEyeBound);
            mMouseBound.set(mFace.mMouseBound);

            //얼굴 인식에서 사용한 이미지 크기 대비 원본 이미지의 크기에 대한 비율을 반영한다.
            float mainScale = mSubBitmap.getWidth() / mFace.mWidth;
            scaleRect(mainScale, mFaceBound);
            scaleRect(mainScale, mCheekBound);
            scaleRect(mainScale, mChinBound);
            scaleRect(mainScale, mLeftEyeBound);
            scaleRect(mainScale, mRightEyeBound);
            scaleRect(mainScale, mMouseBound);

			// 가이드 이미지 크기 대비 눈/입/턱 크기에 대한 비율을 반영한다.
			leftEyeScale = mLeftEyeBound.width() / mObject[OBJECT_LEFT_EYE].getWidth();
			rightEyeScale = mRightEyeBound.width() / mObject[OBJECT_RIGHT_EYE].getWidth();
			mouseScale = mMouseBound.width() / mObject[OBJECT_MOUSE].getWidth();
			chinScale = mChinBound.width()/ mObject[OBJECT_CHIN].getWidth();

			mObjectMatrix[OBJECT_LEFT_EYE].preTranslate(mFace.mLeftEyeCenterX * mainScale - mObject[OBJECT_LEFT_EYE].getWidth() / 2f * leftEyeScale, mFace.mLeftEyeCenterY * mainScale - mObject[OBJECT_LEFT_EYE].getHeight() / 2f * leftEyeScale);
			mObjectMatrix[OBJECT_RIGHT_EYE].preTranslate(mFace.mRightEyeCenterX * mainScale - mObject[OBJECT_RIGHT_EYE].getWidth() / 2f * rightEyeScale, mFace.mRightEyeCenterY * mainScale - mObject[OBJECT_RIGHT_EYE].getHeight() / 2f * rightEyeScale);
			mObjectMatrix[OBJECT_MOUSE].preTranslate(mFace.mMouseCenterX * mainScale - mObject[OBJECT_MOUSE].getWidth() / 2f * mouseScale, mFace.mMouseCenterY * mainScale - mObject[OBJECT_MOUSE].getHeight() / 2f * mouseScale);
			mObjectMatrix[OBJECT_CHIN].preTranslate(mFace.mChinCenterX * mainScale - mObject[OBJECT_CHIN].getWidth() / 2f * chinScale, mFace.mChinCenterY * mainScale + mObject[OBJECT_CHIN].getHeight() / 2f * chinScale);

            mObjectMatrix[OBJECT_LEFT_EYE].preScale(leftEyeScale, leftEyeScale);
            mObjectMatrix[OBJECT_RIGHT_EYE].preScale(rightEyeScale, rightEyeScale);
            mObjectMatrix[OBJECT_MOUSE].preScale(mouseScale, mouseScale);
            mObjectMatrix[OBJECT_CHIN].preScale(chinScale, chinScale);
            rotate(mFace.mRotation);
        } else {
        	float scale = (float)mImage.getWidth() / (float)getWidth();
            // 얼굴 정보가 없을 경우 임의의 고정된 위치에 표시
        	/**
        	 * 20150612 aubergine : 이미지에 따른 적용 오류 수정 : 이미지 사이즈에 맞게 scale적용하고 위치 조정 
        	 */
            for(int i = 0; i < mObjectMatrix.length; i++) {
                mObjectMatrix[i].preTranslate((getWidth() /2)*scale,(getHeight() /2)*scale);
            }
            
 			mObjectMatrix[OBJECT_LEFT_EYE].preTranslate(-(mObject[OBJECT_LEFT_EYE].getWidth() + mObject[OBJECT_LEFT_EYE].getWidth()/2)*scale, -((getHeight() /4)*scale +mObject[OBJECT_LEFT_EYE].getHeight()*scale));
			mObjectMatrix[OBJECT_RIGHT_EYE].preTranslate((mObject[OBJECT_RIGHT_EYE].getWidth()/2)*scale, -((getHeight() /4)*scale +mObject[OBJECT_RIGHT_EYE].getHeight()*scale));
			mObjectMatrix[OBJECT_MOUSE].preTranslate(-(mObject[OBJECT_MOUSE].getWidth()/2)*scale,0);
			mObjectMatrix[OBJECT_CHIN].preTranslate(-(mObject[OBJECT_CHIN].getWidth()/2)*scale, (getHeight() /4 + mObject[OBJECT_CHIN].getHeight())*scale);
            
            mObjectMatrix[OBJECT_LEFT_EYE].preScale(scale, scale);
            mObjectMatrix[OBJECT_RIGHT_EYE].preScale(scale, scale);
            mObjectMatrix[OBJECT_MOUSE].preScale(scale, scale);
            mObjectMatrix[OBJECT_CHIN].preScale(scale, scale);
            
            leftEyeScale = rightEyeScale = mouseScale = chinScale = scale;
        }

        for(int i = 0; i < mSelectionMatrix.length; i++) {
            mSelectionMatrix[i].set(mObjectMatrix[i]);
            mSelectionMatrix[i].preTranslate(-(mSelectionRect.width() - mObject[i].getWidth()) / 2f,
                                             -(mSelectionRect.height() - mObject[i].getHeight()) / 2f);
        }
        for(int i = 0; i < mToolBoxMatrix.length; i++) {
            mToolBoxMatrix[i].set(mSelectionMatrix[i]);
            mToolBoxMatrix[i].preTranslate(mSelectionRect.width() - mRotationToolNormal.getWidth()
                    / 2f, mSelectionRect.height() - mRotationToolNormal.getHeight() / 2f);

            //if(mFace != null) {
                mScale[OBJECT_LEFT_EYE] = leftEyeScale;
                mScale[OBJECT_RIGHT_EYE] = rightEyeScale;
                mScale[OBJECT_MOUSE] = mouseScale;
                mScale[OBJECT_CHIN] = chinScale;
            //}
            
        }
    }


    private RectF mLeftEyeBound = new RectF();
    private RectF mRightEyeBound = new RectF();
    private RectF mCheekBound = new RectF();
    private RectF mFaceBound = new RectF();
    private RectF mChinBound = new RectF();
    private RectF mMouseBound = new RectF();

    private void scaleRect(float scale, RectF rect) {
        rect.left *= scale;
        rect.top *= scale;
        rect.right *= scale;
        rect.bottom *= scale;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float scale = (float)mImage.getWidth() / (float)getWidth();
        mLastPoint.x = event.getX() * scale;
        mLastPoint.y = event.getY() * scale;
        mapScaledPoint(mLastPoint);
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mToolBoxSelected = isInnterToolPoint(mLastPoint.x, mLastPoint.y);
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mSelectedObject = -1;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mToolBoxSelected = false;
                break;
            default:
                break;
        }

        mGestureDetector.onTouchEvent(event);
        if(mSelectedObject != -1) {
            mMoveDetector.onTouchEvent(event);
        }
        invalidate();
        return super.onTouchEvent(event);
    }

    private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // 객체 선택
            float scale = (float)mImage.getWidth() / (float)getWidth();
            mLastPoint.x = e.getX() * scale;
            mLastPoint.y = e.getY() * scale;
            mapScaledPoint(mLastPoint);
            mSelectedObject = findInnterPoint(mLastPoint.x, mLastPoint.y);
            return true;
        }
    }

    protected PointF mPrevPoint = new PointF();

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            float scale = (float)mImage.getWidth() / (float)getWidth();
            mLastPoint.x = detector.getCurrMotionEvent().getX() * scale;
            mLastPoint.y = detector.getCurrMotionEvent().getY() * scale;
            mapScaledPoint(mLastPoint);
            mPrevPoint.x = detector.getPrevMotionEvent().getX() * scale;
            mPrevPoint.y = detector.getPrevMotionEvent().getY() * scale;
            mapScaledPoint(mPrevPoint);
            if(mToolBoxSelected && mSelectedObject != -1) {

                PointF centerPoint = getSelectedCenterPoint(mSelectedObject);
                PointF originalCenterPoint = getOriginalSelectedCenterPoint(mSelectedObject);
                float degree = getDegree(mPrevPoint.x, mPrevPoint.y, mLastPoint.x, mLastPoint.y,
                                         centerPoint);
                rotate(degree);
                // 스케일
                float afterDistance = TransformUtils.getDiameter(mLastPoint.x - centerPoint.x,
                                                                 mLastPoint.y - centerPoint.y);
                float beforeDistance = TransformUtils.getDiameter(mPrevPoint.x - centerPoint.x,
                                                                  mPrevPoint.y - centerPoint.y);
                scale = afterDistance / beforeDistance;
                mScale[mSelectedObject] *= scale;
                mObjectMatrix[mSelectedObject].preScale(scale, scale, originalCenterPoint.x,
                                                        originalCenterPoint.y);
            } else if(mSelectedObject != -1) {
                mObjectMatrix[mSelectedObject].postTranslate(mLastPoint.x - mPrevPoint.x,
                                                             mLastPoint.y - mPrevPoint.y);
            }
            return true;
        }
    }

    private float mRotation = 0f;

    private void rotate(float degree) {
        mRotation += degree;
        for(int i = 0; i < mObjectMatrix.length; i++) {
            PointF originalCenterPoint = getOriginalSelectedCenterPoint(i);
            mObjectMatrix[i].preRotate(degree, originalCenterPoint.x, originalCenterPoint.y);
        }
    }

    /**
     * 좌표 시동시 회전 각도
     */
    private float getDegree(float preX, float preY, float curX, float curY, PointF centerPoint) {
        return -(float)Math.toDegrees(Math.atan2(preY - centerPoint.y, preX - centerPoint.x)
                - (Math.atan2(curY - centerPoint.y, curX - centerPoint.x)));
    }

    private PointF getOriginalSelectedCenterPoint(int object) {
        return new PointF(mObject[object].getWidth() / 2, mObject[object].getHeight() / 2);
    }

    private PointF getSelectedCenterPoint(int object) {
        PointF point = null;
        Matrix matrix = mObjectMatrix[mSelectedObject];
        if(matrix != null) {
            PointF originalPoint = getOriginalSelectedCenterPoint(object);
            float[] points = new float[] {
                    originalPoint.x, originalPoint.y
            };
            matrix.mapPoints(points);
            return new PointF(points[0], points[1]);
        }
        return point;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(isInEditMode()) {
            return;
        }
        onAlternativeDraw(canvas, false);
    }

    @Override
    public void onAlternativeDraw(Canvas canvas, boolean output) {
        if(!output && mScalableViewController != null) {
            mCanvas.save();
            mCanvas.concat(mScalableViewController.getScaleMatrix());
        }
        mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        mCanvas.drawBitmap(mImage, 0, 0, mImagePaint);
        mCanvas.drawBitmap(mObject[OBJECT_LEFT_EYE], mObjectMatrix[OBJECT_LEFT_EYE], mImagePaint);
        mCanvas.drawBitmap(mObject[OBJECT_RIGHT_EYE], mObjectMatrix[OBJECT_RIGHT_EYE], mImagePaint);
        mCanvas.drawBitmap(mObject[OBJECT_MOUSE], mObjectMatrix[OBJECT_MOUSE], mImagePaint);
        mCanvas.drawBitmap(mObject[OBJECT_CHIN], mObjectMatrix[OBJECT_CHIN], mImagePaint);

        float scale = (float)canvas.getWidth() / (float)mImage.getWidth();
        if(mSelectedObject != -1) {
            mSelectionMatrix[mSelectedObject].set(mObjectMatrix[mSelectedObject]);
            mSelectionMatrix[mSelectedObject].preTranslate(-(mSelectionRect.width() - mObject[mSelectedObject].getWidth()) / 2f,
                                                           -(mSelectionRect.height() - mObject[mSelectedObject].getHeight()) / 2f);
            mObjectPath[mSelectedObject].reset();
            mObjectPath[mSelectedObject].addRect(mSelectionRect, Direction.CW);
            mObjectPath[mSelectedObject].transform(mSelectionMatrix[mSelectedObject]);
            mOutlinePaint.setStrokeWidth(1.5f * getResources().getDisplayMetrics().density
                    * mScalableViewController.getInvertScale());
            mCanvas.drawPath(mObjectPath[mSelectedObject], mOutlinePaint);

            mToolBoxMatrix[mSelectedObject].set(mSelectionMatrix[mSelectedObject]);
            mToolBoxMatrix[mSelectedObject].preTranslate(mSelectionRect.width()
                    - mRotationToolNormal.getWidth() / 2f, mSelectionRect.height()
                    - mRotationToolNormal.getHeight() / 2f);
            mToolBoxMatrix[mSelectedObject].preScale((1f / scale)
                                                             * (1f / mScale[mSelectedObject])
                                                             * mScalableViewController.getInvertScale(),
                                                     (1f / scale)
                                                             * (1f / mScale[mSelectedObject])
                                                             * mScalableViewController.getInvertScale(),
                                                     mRotationToolPress.getWidth() / 2f,
                                                     mRotationToolPress.getHeight() / 2f);
            mCanvas.drawBitmap(mToolBoxSelected ? mRotationToolPress : mRotationToolNormal,
                               mToolBoxMatrix[mSelectedObject], mImagePaint);
        }
        if(!output) {
            drawMirror(mCanvas);
        }
        if(!output && mScalableViewController != null) {
            mCanvas.restore();
        }

        canvas.save();
        canvas.scale(scale, scale);
        canvas.drawBitmap(mSubBitmap, 0, 0, mImagePaint);
        canvas.restore();
    }

    private RectF mImageBound = new RectF();

    /**
     * x, y좌표에 있는 객체의 인덱스를 반환한다.
     */
    private Integer findInnterPoint(float x, float y) {
        for(int i = 0; i < mObject.length; i++) {
            mImageBound.set(0, 0, mObject[i].getWidth(), mObject[i].getHeight());
            if(isInnterPoint(mImageBound, mObjectMatrix[i], x, y)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 크기/회전 버튼이 선택 되었는지 확인한다.
     */
    private boolean isInnterToolPoint(float x, float y) {
        if(mSelectedObject != -1) {
            mImageBound.set(0, 0, mRotationToolNormal.getWidth(), mRotationToolNormal.getHeight());
            return isInnterPoint(mImageBound, mToolBoxMatrix[mSelectedObject], x, y);
        }
        return false;
    }

    private Matrix mInvertedFrameMatrix = new Matrix();

    private boolean isInnterPoint(RectF rect, Matrix matrix, float x, float y) {
        float[] point = invertTransformPoints(matrix, x, y);
        if(point != null && rect.contains(point[0], point[1])) {
            return true;
        }
        return false;
    }

    private float[] invertTransformPoints(Matrix origin, float x, float y) {
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
     * 이미지의 얼굴 영역 정보를 가져온다.
     * 
     * @param face 얼굴 영역 정보를 저장할 Face
     * @version 2.0
     */
    public void getFace(Face face) {
        float rotate = mRotation;
        rotate(-mRotation);
        face.mWidth = mSubBitmap.getWidth();
        face.mHeight = mSubBitmap.getHeight();

        RectF leftEye = new RectF();
        leftEye.right = mObject[OBJECT_LEFT_EYE].getWidth();
        leftEye.bottom = mObject[OBJECT_LEFT_EYE].getHeight();
        mObjectMatrix[OBJECT_LEFT_EYE].mapRect(leftEye);

        RectF rightEye = new RectF();
        rightEye.right = mObject[OBJECT_RIGHT_EYE].getWidth();
        rightEye.bottom = mObject[OBJECT_RIGHT_EYE].getHeight();
        mObjectMatrix[OBJECT_RIGHT_EYE].mapRect(rightEye);

        RectF mouse = new RectF();
        mouse.right = mObject[OBJECT_MOUSE].getWidth();
        mouse.bottom = mObject[OBJECT_MOUSE].getHeight();
        mObjectMatrix[OBJECT_MOUSE].mapRect(mouse);

        RectF chin = new RectF();
        chin.right = mObject[OBJECT_CHIN].getWidth();
        chin.bottom = mObject[OBJECT_CHIN].getHeight();
        mObjectMatrix[OBJECT_CHIN].mapRect(chin);

        face.mEyesDist = (float)Math.hypot(leftEye.centerX() - rightEye.centerX(),
                                           leftEye.centerY() - rightEye.centerY());

        face.mLeftEyeCenterX = leftEye.centerX();
        face.mLeftEyeCenterY = leftEye.centerY();
        face.mLeftEyeSize = leftEye.width() / 2f;
        face.mRightEyeCenterX = rightEye.centerX();
        face.mRightEyeCenterY = rightEye.centerY();
        face.mRightEyeSize = rightEye.width() / 2f;
        face.mMouseCenterX = mouse.centerX();
        face.mMouseCenterY = mouse.centerY();
        face.mMouseWidth = mouse.width() / 2f;
        face.mMouseHeight = mouse.height() / 2f;

        face.mChinWidth = chin.width() / 2f;
        face.mChinHeight = chin.height() / 2f;
        face.mChinCenterX = chin.centerX();
        face.mChinCenterY = chin.centerY();

        face.mDistanceEyeToMouse = face.mMouseCenterY - face.mRightEyeCenterY;
        face.mMidPointX = face.mLeftEyeCenterX + face.mEyesDist / 2f;
        face.mMidPointY = face.mLeftEyeCenterY;

        face.mRotation = rotate;
        face.calculateFaceBound();
    }

    /**
     * 이미지의 얼굴 영역 정보를 서정한다.
     * 
     * @param face 설정할 얼굴 영역 정보
     * @version 2.0
     */
    public void setFace(Face face) {
        mFace = face;

        // 눈,코,입의 위치를 레이아웃에 맞춰주기 위해 다시 초기화 해 준다.
        requestLayout();
    }
}
