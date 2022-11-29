
package com.kiwiple.imageframework.view;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.opengl.GLES20;
import android.os.Build;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;

import com.almeros.android.multitouch.gesturedetectors.MoveGestureDetector;
import com.almeros.android.multitouch.gesturedetectors.RotateGestureDetector;
import com.kiwiple.imageframework.Constants;
import com.kiwiple.imageframework.util.BitmapUtils;
import com.kiwiple.imageframework.util.CollageRect;
import com.kiwiple.imageframework.view.ScalableViewController.OnInvalidateListener;

/**
 * ImageView를 상속 받은 View 클래스.<br>
 * Gesture 이벤트를 이용하여 이미지를 Crop, Zoom In/Out, Rotate 시킬 수 있다.
 * 
 * @version 1.0
 */
public class ZoomAndCropImageView extends ImageView {
    /**
     * Gesture 이벤트를 모두 사용한다.
     * 
     * @version 1.0
     */
    public static final int GESTURE_ALL = ~0;
    /**
     * Gesture 이벤트를 사용하지 않는다.
     * 
     * @version 1.0
     */
    public static final int GESTURE_CLEAR = 0;
    /**
     * Double tap 초기화 Gesture 이벤트를 사용한다.
     * 
     * @version 1.0
     */
    public static final int GESTURE_DOUBLE_TAP_INITIALIZE = 1;
    /**
     * Move Gesture 이벤트를 사용한다.
     * 
     * @version 1.0
     */
    public static final int GESTURE_MOVE = 2;
    /**
     * Scale Gesture 이벤트를 사용한다.
     * 
     * @version 1.0
     */
    public static final int GESTURE_SCALE = 4;
    /**
     * Rotate Gesture 이벤트를 사용한다.
     * 
     * @version 1.0
     */
    public static final int GESTURE_ROTATE = 8;

    /**
     * 가로 세로 원본 비율. Use with {@link #setImageRatio}
     * 
     * @version 1.0
     */
    public static final int ASPECT_RATIO_ORIGINAL = 0;
    /**
     * 가로 세로 비율 1:1. Use with {@link #setImageRatio}
     * 
     * @version 1.0
     */
    public static final int ASPECT_RATIO_1_1 = 1;
    /**
     * 가로 세로 비율 3:4. Use with {@link #setImageRatio}
     * 
     * @version 1.0
     */
    public static final int ASPECT_RATIO_3_4 = 2;
    /**
     * 가로 세로 비율 4:3. Use with {@link #setImageRatio}
     * 
     * @version 1.0
     */
    public static final int ASPECT_RATIO_4_3 = 3;

    private float mCurrentZoom;
    private float mOriginalRotation;
    private float mBaseRotation;
    private float mCurrentRotation;

    private PointF mCurrentPosition = new PointF();
    private float mBaseZoom;

    private float mImageWidth;
    private float mImageHeight;

    private Matrix mMatrix;
    private Timer mMoveTimer;

    private boolean mMoveGesture = false;

    private GestureDetector mGestureDetector;
    private RotateGestureDetector mRotateDetector;
    private MoveGestureDetector mMoveDetector;
    private ScaleGestureDetector mScaleDetector;

    private boolean mMirrorY;
    private boolean mInit = false;

    private Path mImagePath;
    private Path mImagePathTransformed = new Path();
    private CollageRect mImageRect = new CollageRect();

    private Bitmap mGridBitmap;
    // grid
    private Matrix mGridMatrix;
    private Bitmap mHorizontalLine;
    private Bitmap mHorizontalLineSelected;
    private Matrix mHorizontalLineMatrix;
    private Bitmap mHorizontalLineGuide;
    private Bitmap mHorizontalLineSelectedGuide;
    private Matrix mHorizontalLineGuideMatrix;
    private Paint mRotateDegreePaint = new Paint();
    private Paint mRotateDegreeStrokePaint = new Paint();
    private boolean mGuideLineVisibility = false;

    private int mGestureFlag = GESTURE_CLEAR;
    private int mAspectRatio = ASPECT_RATIO_3_4;

    private int[] mMaxTextureSize = new int[1];

    private boolean mScalableTouch = false;
    private ScalableViewController mScalableViewController;

    public ZoomAndCropImageView(Context context) {
        super(context);
        init();
    }

    public ZoomAndCropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        if(isInEditMode()) {
            return;
        }
        if(Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, new Paint(Paint.FILTER_BITMAP_FLAG));
        }
        GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, mMaxTextureSize, 0);

        setScaleType(ScaleType.MATRIX);
        mMatrix = new Matrix();
        mGestureDetector = new GestureDetector(getContext(), new SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                resetMatrix();
                return super.onDoubleTapEvent(e);
            }
        });
        mRotateDetector = new RotateGestureDetector(getContext(), new RotateListener());
        mMoveDetector = new MoveGestureDetector(getContext(), new MoveListener());
        mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());

        mGridBitmap = BitmapFactory.decodeResource(getResources(),
                                                   getResources().getIdentifier("img_photo_grid",
                                                                                "drawable",
                                                                                getContext().getApplicationContext()
                                                                                            .getPackageName()));
        mHorizontalLine = BitmapFactory.decodeResource(getResources(),
                                                       getResources().getIdentifier("img_horizontal_line_off",
                                                                                    "drawable",
                                                                                    getContext().getApplicationContext()
                                                                                                .getPackageName()));
        mHorizontalLineSelected = BitmapFactory.decodeResource(getResources(),
                                                               getResources().getIdentifier("img_horizontal_line_on",
                                                                                            "drawable",
                                                                                            getContext().getApplicationContext()
                                                                                                        .getPackageName()));
        mHorizontalLineGuide = BitmapFactory.decodeResource(getResources(),
                                                            getResources().getIdentifier("img_angle_on",
                                                                                         "drawable",
                                                                                         getContext().getApplicationContext()
                                                                                                     .getPackageName()));
        mHorizontalLineSelectedGuide = BitmapFactory.decodeResource(getResources(),
                                                                    getResources().getIdentifier("img_angle_on",
                                                                                                 "drawable",
                                                                                                 getContext().getApplicationContext()
                                                                                                             .getPackageName()));

        mRotateDegreePaint.setStyle(Style.FILL);
        mRotateDegreePaint.setTextSize(16 * getResources().getDisplayMetrics().density);
        mRotateDegreePaint.setTextAlign(Paint.Align.CENTER);
        mRotateDegreePaint.setTypeface(Typeface.DEFAULT_BOLD);
        mRotateDegreePaint.setColor(Color.argb(0xb3, 0xff, 0xff, 0xff));

        mRotateDegreeStrokePaint.setStyle(Style.STROKE);
        mRotateDegreeStrokePaint.setTextSize(16 * getResources().getDisplayMetrics().density);
        mRotateDegreeStrokePaint.setTextAlign(Paint.Align.CENTER);
        mRotateDegreeStrokePaint.setTypeface(Typeface.DEFAULT_BOLD);
        mRotateDegreeStrokePaint.setStrokeWidth(2);
        mRotateDegreeStrokePaint.setColor(Color.argb(0x6f, 0x00, 0x00, 0x00));
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        if(bm != null && mMaxTextureSize[0] != 0
                && (bm.getWidth() > mMaxTextureSize[0] || bm.getHeight() > mMaxTextureSize[0])) {
            float scale = bm.getWidth() > bm.getHeight() ? (float)mMaxTextureSize[0]
                    / bm.getWidth() : (float)mMaxTextureSize[0] / bm.getHeight();
            Bitmap scaledBitmap = BitmapUtils.resizeBitmap(bm, (int)(bm.getWidth() * scale),
                                                           (int)(bm.getHeight() * scale));
            bm.recycle();
            System.gc();
            bm = scaledBitmap;
        }
        super.setImageBitmap(bm);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        super.setImageDrawable(drawable);
        mInit = false;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // if (mInitialWidthMeasureSpec == -1 || mInitialHeightMeasureSpec == -1) {
        // mInitialWidthMeasureSpec = widthMeasureSpec;
        // mInitialHeightMeasureSpec = heightMeasureSpec;
        // }
        // int width = MeasureSpec.getSize(mInitialWidthMeasureSpec);
        // int height = MeasureSpec.getSize(mInitialHeightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if(mAspectRatio == ASPECT_RATIO_ORIGINAL && getDrawable() != null) {
            float calcWidthByHeight;
            if(mBaseRotation == 90 || mBaseRotation == 270) {
                calcWidthByHeight = getDrawable().getIntrinsicHeight()
                        / (float)getDrawable().getIntrinsicWidth();
            } else {
                calcWidthByHeight = getDrawable().getIntrinsicWidth()
                        / (float)getDrawable().getIntrinsicHeight();
            }
            if(width < (int)(height * calcWidthByHeight)) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int)(width * (1 / calcWidthByHeight)),
                                                                MeasureSpec.EXACTLY);
            } else {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int)(height * calcWidthByHeight),
                                                               MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            }
        } else if(mAspectRatio == ASPECT_RATIO_4_3) {
            if(width < (int)(height * (4.0f / 3.0f))) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int)(width * (3.0f / 4.0f)),
                                                                MeasureSpec.EXACTLY);
            } else {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int)(height * (4.0f / 3.0f)),
                                                               MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            }
        } else if(mAspectRatio == ASPECT_RATIO_3_4) {
            if(width < (int)(height * (3.0f / 4.0f))) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int)(width * (4.0f / 3.0f)),
                                                                MeasureSpec.EXACTLY);
            } else {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int)(height * (3.0f / 4.0f)),
                                                               MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            }
        } else {
            if(width < height) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            } else {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if((!mInit || changed) && getDrawable() != null) {
            mInit = true;
            initMatrix();
            initGridMatrix(right - left, bottom - top);

            if(mScalableViewController != null) {
                mScalableViewController.onLayout(changed, left, top, right, bottom);
            }
        }
    }

    private float initBaseZoom() {
        float wz = (mBaseRotation == 90 || mBaseRotation == 270 ? getHeight() : getWidth())
                / mImageWidth;
        float hz = (mBaseRotation == 90 || mBaseRotation == 270 ? getWidth() : getHeight())
                / mImageHeight;
        float z = 1.f;

        if(wz < hz) {
            z = wz;
        } else {
            z = hz;
        }
        return z;
    }

    private void initMatrix() {
        Drawable d = getDrawable();
        mImageWidth = d.getIntrinsicWidth();
        mImageHeight = d.getIntrinsicHeight();

        mBaseZoom = initBaseZoom();
        mCurrentZoom = mBaseZoom;

        mCurrentPosition.x = -(int)(mImageWidth - getWidth()) / 2;
        mCurrentPosition.y = -(int)(mImageHeight - getHeight()) / 2;

        mImagePath = new Path();
        mImagePath.addRect(new RectF(0, 0, mImageWidth, mImageHeight), Direction.CW);

        mCurrentRotation = 0;

        mMirrorY = false;

        updateImageMatrix();
        if(adjustBoundImagePosition(true)) {
            updateImageMatrix();
        }
        invalidate();
    }

    /**
     * 이미지의 이동/회전/크기 정보를 초기화 한다.
     * 
     * @version 2.0
     */
    public void resetMatrix() {
        mBaseRotation = mOriginalRotation;
        mInit = false;
        requestLayout();
    }

    private void moveMatrix() {
        if(mMoveTimer != null) {
            return;
        }

        mMoveTimer = new Timer();
        if(mCurrentZoom > 15) {
            mCurrentZoom = 15;
        }

        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                post(new Runnable() {
                    @Override
                    public void run() {
                        if(!adjustBoundImagePosition(false)) {
                            if(mMoveTimer != null) {
                                mMoveTimer.cancel();
                                mMoveTimer = null;
                            }
                        }
                        updateImageMatrix();
                    }
                });
            }
        };
        mMoveTimer.schedule(task, 10, 10);
    }

    private void initGridMatrix(float vwidth, float vheight) {
        if(mGridBitmap != null) {
            mGridMatrix = initCenterCropImageMatrix(mGridBitmap, vwidth, vheight);
        }
        if(mHorizontalLine != null) {
            mHorizontalLineMatrix = initFitCenterImageMatrix(mHorizontalLine, vwidth, vheight);
        }
        if(mHorizontalLineGuide != null && mHorizontalLineMatrix != null) {
            mHorizontalLineGuideMatrix = initImageDependentImageMatrix(mHorizontalLineGuide,
                                                                       mHorizontalLineMatrix,
                                                                       vwidth, vheight);
            setMatrixCenterRotate(mHorizontalLineGuideMatrix, mHorizontalLineGuide,
                                  mCurrentRotation);
        }
    }

    private static Matrix initCenterCropImageMatrix(Bitmap image, float vwidth, float vheight) {
        Matrix matrix = new Matrix();
        float scale;
        float dx = 0, dy = 0;
        float dwidth = image.getWidth();
        float dheight = image.getHeight();

        if(dwidth * vheight > vwidth * dheight) {
            scale = vheight / dheight;
            dx = (vwidth - dwidth * scale) * 0.5f;
        } else {
            scale = vwidth / dwidth;
            dy = (vheight - dheight * scale) * 0.5f;
        }

        matrix.setScale(scale, scale);
        matrix.postTranslate((int)(dx + 0.5f), (int)(dy + 0.5f));
        return matrix;
    }

    private static Matrix initFitCenterImageMatrix(Bitmap image, float vwidth, float vheight) {
        Matrix matrix = new Matrix();
        RectF src = new RectF();
        src.set(0, 0, image.getWidth(), image.getHeight());
        RectF dest = new RectF();
        dest.set(0, 0, vwidth, vheight);

        matrix.setRectToRect(src, dest, ScaleToFit.CENTER);
        return matrix;
    }

    private static Matrix initImageDependentImageMatrix(Bitmap image, Matrix dependentMatrix,
            float vwidth, float vheight) {
        Matrix matrix = new Matrix();
        float[] values = new float[9];
        dependentMatrix.getValues(values);
        float scale = values[Matrix.MSCALE_X];

        float dx = (vwidth - image.getWidth() * scale) * 0.5f;
        float dy = (vheight - image.getHeight() * scale) * 0.5f;

        matrix.setScale(scale, scale);
        matrix.postTranslate(dx, dy);
        return matrix;
    }

    private int distanceFromCenterX;
    private int distanceFromCenterY;

    private boolean adjustBoundImagePosition(boolean noAnimation) {
        mImagePath.transform(mMatrix, mImagePathTransformed);
        mImagePathTransformed.computeBounds(mImageRect, false);
        mImageRect.format();

        boolean adjust = false;
        // 이미지 넓이가 프레임보다 작을때.
        if(mImageRect.right - mImageRect.left < getWidth()) {
            distanceFromCenterX = (int)((getWidth() - (mImageRect.right - mImageRect.left)) / 2 - mImageRect.left);
            if(distanceFromCenterX != 0) {
                adjust = true;
                if(Math.abs(distanceFromCenterX) > 5 && !noAnimation) {
                    mCurrentPosition.x += distanceFromCenterX * 0.1f;
                } else {
                    mCurrentPosition.x += distanceFromCenterX;
                }

            }
        } else {
            // 왼쪽 경계로 붙이기
            if(mImageRect.left > 0) {
                adjust = true;
                if(mImageRect.left > 5 && !noAnimation) {
                    mCurrentPosition.x -= Math.ceil(mImageRect.left * 0.1f);
                } else {
                    mCurrentPosition.x -= mImageRect.left;
                }
            } else if(mImageRect.right < getWidth()) {
                adjust = true;
                if(getWidth() - mImageRect.right > 5 && !noAnimation) {
                    mCurrentPosition.x += Math.ceil((getWidth() - mImageRect.right) * 0.1f);
                } else {
                    mCurrentPosition.x += getWidth() - mImageRect.right;
                }
            }
        }
        // 이미지 높이가 프레임보다 작을때.
        if(mImageRect.bottom - mImageRect.top < getHeight()) {
            distanceFromCenterY = (int)((getHeight() - (mImageRect.bottom - mImageRect.top)) / 2 - mImageRect.top);
            if(distanceFromCenterY != 0) {
                adjust = true;
                if(Math.abs(distanceFromCenterY) > 5 && !noAnimation) {
                    mCurrentPosition.y += distanceFromCenterY * 0.1f;
                } else {
                    mCurrentPosition.y += distanceFromCenterY;
                }

            }
        } else {
            // 위쪽 경계로 붙이기
            if(mImageRect.top > 0) {
                adjust = true;
                if(mImageRect.top > 5 && !noAnimation) {
                    mCurrentPosition.y -= Math.ceil(mImageRect.top * 0.1f);
                } else {
                    mCurrentPosition.y -= mImageRect.top;
                }
            } else if(mImageRect.bottom < getHeight()) {
                adjust = true;
                if(getHeight() - mImageRect.bottom > 5 && !noAnimation) {
                    mCurrentPosition.y += Math.ceil((getHeight() - mImageRect.bottom) * 0.1f);
                } else {
                    mCurrentPosition.y += getHeight() - mImageRect.bottom;
                }
            }
        }
        return adjust;
    }

    /**
     * 이미지의 가로 세로 비율을 설정한다.
     * 
     * @param aspectRatio 다음 중 하나의 값을 가진다.
     * @see {@link #ASPECT_RATIO_ORIGINAL}<br>
     *      {@link #ASPECT_RATIO_1_1}<br>
     *      {@link #ASPECT_RATIO_3_4}<br>
     *      {@link #ASPECT_RATIO_4_3}
     * @version 1.0
     */
    public void setImageRatio(int aspectRatio) {
        mAspectRatio = aspectRatio;
        mInit = false;
        requestLayout();
    }

    /**
     * @return 이미지의 가로 세로 비율을 반환한다.
     * @see {@link #ASPECT_RATIO_ORIGINAL}<br>
     *      {@link #ASPECT_RATIO_1_1}<br>
     *      {@link #ASPECT_RATIO_3_4}<br>
     *      {@link #ASPECT_RATIO_4_3}
     * @version 1.0
     */
    public int getImageRatio() {
        return mAspectRatio;
    }

    /**
     * 편집할 이미지의 기본 각도를 설정한다. 범위 0 ~360도
     * 
     * @param degree 기본 orientation
     * @version 2.0
     */
    public void setBaseRotation(int degree) {
        mBaseRotation = degree;
        mOriginalRotation = degree;
    }

    /**
     * 이미지를 90도 회전 시킨다.
     * 
     * @version 1.0
     */
    public void rotateImage() {
        rotateImage(90);
    }

    /**
     * 파라메타로 받은 각도만큼 이미지를 회전 시킨다.
     * 
     * @param angle 회전 시킬 각도
     * @version 2.0
     */
    public void rotateImage(int angle) {
        mBaseRotation += angle;
        if(mBaseRotation < 0) {
            mBaseRotation += 360;
        }
        if(mBaseRotation >= 360) {
            mBaseRotation %= 360;
        }
        mInit = false;
        requestLayout();
    }

    private void setRotate(float rotate) {
        mCurrentRotation += rotate;
        if(mCurrentRotation < 0) {
            mCurrentRotation += 360;
        }
        if(mCurrentRotation >= 360) {
            mCurrentRotation %= 360;
        }
        if(mHorizontalLineGuide != null) {
            setMatrixCenterRotate(mHorizontalLineGuideMatrix, mHorizontalLineGuide, rotate);
        }
        updateImageMatrix();
    }

    private static void setMatrixCenterRotate(Matrix matrix, Bitmap source, float rotate) {
        RectF rectF = new RectF(0, 0, source.getWidth(), source.getHeight());
        matrix.mapRect(rectF);
        matrix.postRotate(rotate, rectF.centerX(), rectF.centerY());
    }

    /**
     * 이미지를 좌우 대칭 시킨다.
     * 
     * @version 1.0
     */
    public void flipImage() {
        mMirrorY = !mMirrorY;
        updateImageMatrix();
    }

    private void translate(float offsetX, float offsetY) {
        mCurrentPosition.x += offsetX;
        mCurrentPosition.y += offsetY;
        updateImageMatrix();
    }

    private void scale(float scale) {
        if(mCurrentZoom * scale < mBaseZoom || mCurrentZoom * scale > mBaseZoom * 4.f) {
            return;
        }
        mCurrentZoom = mCurrentZoom * scale;
        updateImageMatrix();
    }

    private void updateImageMatrix() {
        mMatrix.reset();
        mMatrix.postTranslate(mCurrentPosition.x, mCurrentPosition.y);
        mMatrix.postRotate(mBaseRotation + mCurrentRotation, mCurrentPosition.x + mImageWidth / 2,
                           mCurrentPosition.y + mImageHeight / 2);
        mMatrix.postScale(mCurrentZoom, mCurrentZoom, mCurrentPosition.x + mImageWidth / 2,
                          mCurrentPosition.y + mImageWidth / 2);
        if(mMirrorY) {
            if(mBaseRotation == 0 || mBaseRotation == 180) {
                mMatrix.preTranslate(mImageWidth, 0);
                mMatrix.preScale(-1.0f, 1.0f);
            } else {
                mMatrix.preTranslate(0, mImageHeight);
                mMatrix.preScale(1.0f, -1.0f);
            }
        }
        setImageMatrix(mMatrix);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isEnabled()) {
            if(mScalableTouch && mScalableViewController != null) {
                return mScalableViewController.onTouchEvent(event);
            }
            return false;
        }
        if(!mInit) {
            return false;
        }
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mMoveGesture = true;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mMoveGesture = false;
                break;
            case MotionEvent.ACTION_UP:
                moveMatrix();
            case MotionEvent.ACTION_POINTER_UP:
                mMoveGesture = false;
                break;
        }
        if((mGestureFlag & GESTURE_DOUBLE_TAP_INITIALIZE) != 0) {
            mGestureDetector.onTouchEvent(event);
        }
        if((mGestureFlag & GESTURE_ROTATE) != 0) {
            mRotateDetector.onTouchEvent(event);
        }
        if((mGestureFlag & GESTURE_MOVE) != 0) {
            mMoveDetector.onTouchEvent(event);
        }
        if((mGestureFlag & GESTURE_SCALE) != 0) {
            mScaleDetector.onTouchEvent(event);
        }
        return true; // indicate event was handled
    }

    private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            if(Math.abs(detector.getRotationDegreesDelta()) < 1) {
                return false;
            }
            setRotate((int)-detector.getRotationDegreesDelta());
            return true;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            if(mMoveGesture) {
                PointF d = detector.getFocusDelta();
                translate(d.x, d.y);
            }
            return true;
        }
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale(detector.getScaleFactor());
            return true;
        }
    }

    /**
     * Crop, zoom, rotate가 적용 된 Bitmap 이미지를 생성한다.
     * 
     * @param size Bitmap의 크기
     * @return Crop, zoom, rotate가 적용 된 이미지
     * @version 1.0
     */
    public Bitmap cropBitmap(int size) {
        int targetWidth = size;
        int targetHeight = size;
        if(getWidth() < getHeight()) {
            targetWidth *= getWidth() / (float)getHeight();
        } else if(getWidth() > getHeight()) {
            targetHeight *= getHeight() / (float)getWidth();
        }
        Bitmap result = Bitmap.createBitmap(targetWidth, targetHeight, Config.ARGB_8888);

        Canvas canvas = new Canvas(result);
        canvas.drawColor(Color.BLACK);
        canvas.save();
        canvas.scale(targetWidth / (float)getWidth(), targetHeight / (float)getHeight());
        onAlternativeDraw(canvas, true);
        canvas.restore();
        if(Constants.DEMO_VERSION) {
            BitmapUtils.applyWaterMarkImage(getContext(), result);
        }
        return result;
    }

    /**
     * 가이드 라인의 표시 여부를 설정한다.
     * 
     * @param visibility 가이드 라인 표시 여뷰
     * @version 1.0
     */
    public void setGuideLineVisibility(boolean visibility) {
        if(visibility != mGuideLineVisibility) {
            mGuideLineVisibility = visibility;
            if(mGuideLineVisibility) {
                invalidate();
            }
        }
    }

    /**
     * 제스쳐 이벤트를 이용하여 이미지를 Crop, Zoom In/Out, Rotate 시킨다.
     * 
     * @param Gesture 이벤트 사용 여부에 대한 flag(비트 연산)
     * @see {@link #GESTURE_ALL} <br>
     *      {@link #GESTURE_CLEAR}<br>
     *      {@link #GESTURE_DOUBLE_TAP_INITIALIZE} <br>
     *      {@link #GESTURE_MOVE} <br>
     *      {@link #GESTURE_SCALE} <br>
     *      {@link #GESTURE_ROTATE}
     * @version 1.0
     */
    public void setGestureEnabled(int gestureFlag) {
        mGestureFlag |= gestureFlag;
    }

    /**
     * 제스쳐 이벤트를 이용하지 않도록 설정한다.
     * 
     * @version 2.0
     */
    public void setGestureEnabledClear() {
        mGestureFlag = GESTURE_CLEAR;
    }

    /**
     * 현재 설정된 제스쳐 이벤트 정보를 반환한다.
     * 
     * @return Gesture 이벤트 정보
     * @version 2.0
     */
    public int getGestureEnabled() {
        return mGestureFlag;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        onAlternativeDraw(canvas, false);
    }

    /**
     * 편집된 이미지를 캔버스에 그려준다.
     * 
     * @param canvas 편집된 이미지를 그릴 canvas
     * @param isOutput 최종 output일 경우 true로 설장한다.(false로 설정하면 사용자 가이드 요소도 포함된다.)
     * @version 2.0
     */
    public void onAlternativeDraw(Canvas canvas, boolean output) {
        if(!output && mScalableViewController != null) {
            canvas.save();
            canvas.concat(mScalableViewController.getScaleMatrix());
        }
        super.onDraw(canvas);
        if(mGuideLineVisibility && !output) {
            if(mGridBitmap != null && mGridMatrix != null) {
                canvas.drawBitmap(mGridBitmap, mGridMatrix, null);
            }
            if(mCurrentRotation == 0 || mCurrentRotation == 359 || mCurrentRotation == 1
                    || mCurrentRotation == 180 || mCurrentRotation == 179
                    || mCurrentRotation == 181) {
                if(mHorizontalLineSelected != null && mHorizontalLineMatrix != null) {
                    canvas.drawBitmap(mHorizontalLineSelected, mHorizontalLineMatrix, null);
                }
                if(mHorizontalLineSelectedGuide != null && mHorizontalLineGuideMatrix != null) {
                    canvas.drawBitmap(mHorizontalLineSelectedGuide, mHorizontalLineGuideMatrix,
                                      null);
                }
            } else {
                if(mHorizontalLine != null && mHorizontalLineMatrix != null) {
                    canvas.drawBitmap(mHorizontalLine, mHorizontalLineMatrix, null);
                }
                if(mHorizontalLineGuide != null && mHorizontalLineGuideMatrix != null) {
                    canvas.drawBitmap(mHorizontalLineGuide, mHorizontalLineGuideMatrix, null);
                }
            }
            if(mRotateDegreePaint != null && mRotateDegreeStrokePaint != null) {
                canvas.drawText(String.valueOf((int)mCurrentRotation), getWidth() / 2, getHeight()
                        / 2 + mRotateDegreePaint.getTextSize() * 0.4f, mRotateDegreePaint);
                canvas.drawText(String.valueOf((int)mCurrentRotation), getWidth() / 2, getHeight()
                        / 2 + mRotateDegreePaint.getTextSize() * 0.4f, mRotateDegreeStrokePaint);
            }
        }
        if(!output && mScalableViewController != null) {
            canvas.restore();
        }
    }

    /**
     * Scale 가능 터치 여부를 설정
     * 
     * @param scalableTouch Scale 가능 터치 여부
     */
    public void setScalableTouch(boolean scalableTouch) {
        mScalableTouch = scalableTouch;
    }

    public void setScalableViewController(ScalableViewController controller) {
        mScalableViewController = controller;
    }

    /**
     * Scale 가능 여부를 설정
     * 
     * @param scalable Scale 가능 터치 여부
     */
    public void setScalable(boolean scalable) {
        mScalableTouch = scalable;
        if(scalable) {
            mScalableViewController = new ScalableViewController(getContext());
            mScalableViewController.setOnInvalidateListener(new OnInvalidateListener() {
                @Override
                public void onInvalidate() {
                    invalidate();
                }
            });
        } else if(mScalableViewController != null) {
            mScalableViewController.setOnInvalidateListener(null);
            mScalableViewController = null;
        }
    }
}
