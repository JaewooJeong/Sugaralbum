
package com.kiwiple.imageframework.cosmetic;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.RadialGradient;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.kiwiple.imageframework.util.FileUtils;
import com.kiwiple.imageframework.view.ScalableViewController;
import com.kiwiple.imageframework.view.ScalableViewController.OnInvalidateListener;

/**
 * 터치, 드래깅, 영역 설정 등의 성형기능 지원을 위한 제스쳐 방법과 세기를 조절할 수 있는 사용자 인터페이스를 제공하는 View 상속 클래스
 * 
 * @version 2.0
 */
public class BeautyEffectImageView extends View {
    protected RectF mLeftZoomArea;
    protected RectF mRightZoomArea;

    protected Paint mOutlinePaint;

    /**
     * 한점을 기준으로 미용 효과를 부여한다. Use with {@link #setBrushType(int)}
     * 
     * @version 2.0
     */
    public static final int BRUSH_TYPE_POINT = 1;
    /**
     * 방향성을 가지는 미용 효과를 부여한다. Use with {@link #setBrushType(int)}
     * 
     * @version 2.0
     */
    public static final int BRUSH_TYPE_2_ARROW = 2;
    /**
     * 특정 영역에 미용 효과를 부여한다. Use with {@link #setBrushType(int)}
     * 
     * @version 2.0
     */
    public static final int BRUSH_TYPE_4_ARROW = 3;
    protected int mBrushType = -1;

    protected Paint mTouchImagePaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    protected Paint mBrushPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    /**
     * 브러시 포인트 이미지
     */
    protected Bitmap mBrush;
    /**
     * 브러시 배경 이미지 
     */
    protected Bitmap mBrushBg;
    protected float mBrushScale = 1.f;

    protected boolean mShowOriginal = false;

    protected Bitmap mImage;
    protected Matrix mImageMatrix = new Matrix();
    private RectF mTempSrc = new RectF();
    private RectF mTempDst = new RectF();

    protected boolean brushing = false;
    protected boolean zooming = false;
    /**
     * 효과 시작 지점
     */
    protected PointF mStartPoint = new PointF();
    /**
     * 효과 종료 지점
     */
    protected PointF mEndPoint = new PointF();
    /**
     * 브러시 방향성 정보
     */
    private float mRotation = 0;

    protected Canvas mCanvas;
    protected Bitmap mSubBitmap;

    /**
     * 효과가 부여된 이미지 목록
     */
    protected ArrayList<Bitmap> queue = new ArrayList<Bitmap>();
    /**
     * 효과가 부여된 이미지들의 좌표
     */
    protected ArrayList<Rect> pointqueue = new ArrayList<Rect>();
    /**
     * 히스토리 관련 기능으로 queue.get(0)~queue.get(mCurrentIndex)까지의 효과가 적용된다. 
     */
    protected int mCurrentIndex = -1;
    
    // TODO: 사용하지 않는 코드. 사용 필요.
    protected boolean mScaleQueue = false;

    private boolean mScalableTouch = false;
    /**
     * 이미지 확대/이동 보기 기능 지원
     */
    protected ScalableViewController mScalableViewController;
    private float[] mScaledPoint = new float[2];
    
    /**
     * 효과를 적용할 영역을 선택 중인지 판단.
     */
    protected boolean mEffectProgress = true;
    
    /**
     * 효과를 적용하지 않도록 설정.
     */
    protected boolean mBlockEffect = false;
    
    
    /*
     * 자동모드시 브러시 및 히스토리 이미지 제어를 위해 추가됨. - aubergine
     */
    protected boolean isAuto = false;

    public BeautyEffectImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public BeautyEffectImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BeautyEffectImageView(Context context) {
        super(context);
        init();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 뷰의 크기를 이미지 비율에 맞춰 재조정한다.
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if(mImage != null) {
            float calcWidthByHeight = mImage.getWidth() / (float)mImage.getHeight();
            if(width < (int)(height * calcWidthByHeight)) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int)(width * (1 / calcWidthByHeight)),
                                                                MeasureSpec.EXACTLY);
            } else {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int)(height * calcWidthByHeight),
                                                               MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void init() {
        // 하드웨어 가속에서는 미지원 또는 API level 제한이 있어 software rendering을 하도록 고정.
        // 참고: http://developer.android.com/guide/topics/graphics/hardware-accel.html
        setLayerType(View.LAYER_TYPE_SOFTWARE, new Paint(Paint.FILTER_BITMAP_FLAG));
        mBrushBg = BitmapFactory.decodeResource(getResources(),
                                                FileUtils.getBitmapResourceId(getContext(),
                                                                              "img_brush_bg"));
        mScaleQueue = true;
    }

    /**
     * 적용된 미용 효과를 초기화 한다.
     * 
     * @version 2.0
     */
    public void reset() {
        queue.clear();
        pointqueue.clear();
        mCurrentIndex = -1;
    }

    /**
     * Brush의 종류를 선택한다.
     * 
     * @param brushType 다음중 하나의 값을 가진다.
     * @see {@link #BRUSH_TYPE_POINT}<br>
     *      {@link #BRUSH_TYPE_2_ARROW}<br>
     *      {@link #BRUSH_TYPE_4_ARROW}
     * @version 2.0
     */
    public void setBrushType(int brushType) {
        if(mBrushType != brushType) {
            switch(brushType) {
                case BRUSH_TYPE_POINT:
                    mBrush = BitmapFactory.decodeResource(getResources(),
                                                          FileUtils.getBitmapResourceId(getContext(),
                                                                                        "img_brush_01"));
                    break;
                case BRUSH_TYPE_2_ARROW:
                    mBrush = BitmapFactory.decodeResource(getResources(),
                                                          FileUtils.getBitmapResourceId(getContext(),
                                                                                        "img_brush_02"));
                    break;
                case BRUSH_TYPE_4_ARROW:
                    mBrush = BitmapFactory.decodeResource(getResources(),
                                                          FileUtils.getBitmapResourceId(getContext(),
                                                                                        "img_brush_03"));
                    break;
            }
            mBrushType = brushType;
            invalidate();
        }
    }

    /**
     * brush의 크기를 설정한다.
     * 
     * @param progress brush 크기
     * @remark [0~100]의 값을 가지며 아래와 같이 brush의 크기가 설정된다.<br>
     *         0: 기본 brush의 1/2 크기<br>
     *         1: 기본 brush 크기<br>
     *         2: 기본 brush의 2배 크기
     * @version 2.0
     */
    public void setBrushSize(int progress) {
    	float scale = (float)mImage.getWidth() / (float)getWidth();
         // -50~50
        float convert = progress;
        convert -= 50f;
        // -1~1
        convert /= 50f;
        if(convert < 0) {
            // 1/2~1
            mBrushScale = -convert + 1f;
            mBrushScale = 1f / mBrushScale;
        } else if(convert > 0) {
            // 1~2
            mBrushScale = convert + 1f;
        } else {
            mBrushScale = 1f;
        }
        
        mBrushScale *=scale;
        invalidate();
    }

    /**
     * brush guide 를 표시한다.
     * 
     * @param visible true 설정시 brush guide를 표시한다.
     * @version 2.0
     */
    public void setBrushVisibility(boolean visible) {
        brushing = visible;
        invalidate();
    }

    /**
     * 원본 이미지를 보여준다.
     * 
     * @param show true 설정시 원본 이미지를 보여준다.
     * @version 2.0
     */
    public void showOriginal(boolean show) {
        mShowOriginal = show;
        invalidate();
    }

    private float mDensity;

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(isInEditMode()) {
            return;
        }
        mDensity = getResources().getDisplayMetrics().density;

        float topMargin = 29.5f * mDensity;
        // 초기 브러시의 위치는 뷰의 가운데
        float horizontalMargin = 5 * mDensity;
        if(mLeftZoomArea == null) {
            mLeftZoomArea = new RectF();
        }
        mLeftZoomArea.left = horizontalMargin;
        mLeftZoomArea.top = topMargin;
        mLeftZoomArea.right = mLeftZoomArea.left + 150f * mDensity;
        mLeftZoomArea.bottom = mLeftZoomArea.top + 150f * mDensity;

        if(mRightZoomArea == null) {
            mRightZoomArea = new RectF();
        }
        mRightZoomArea.right = (right - left) - horizontalMargin;
        mRightZoomArea.left = mRightZoomArea.right - 150f * mDensity;
        mRightZoomArea.top = topMargin;
        mRightZoomArea.bottom = mRightZoomArea.top + 150f * mDensity;

        if(mOutlinePaint == null) {
            mOutlinePaint = new Paint();
            mOutlinePaint.setStyle(Style.STROKE);
            mOutlinePaint.setColor(Color.WHITE);
            mOutlinePaint.setStrokeWidth(1.5f * getResources().getDisplayMetrics().density);
        }

        float scale = (float)mImage.getWidth() / (float)getWidth();
        mEndPoint.x = (right - left) / 2 * scale;
        mEndPoint.y = (bottom - top) / 2 * scale;

        // Generate the required transform.
        mTempSrc.set(0, 0, mImage.getWidth(), mImage.getHeight());
        mTempDst.set(0, 0, right - left, bottom - top);

        // fit-center
        mImageMatrix.reset();
        mImageMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.CENTER);

        // if(mScalableViewController != null) {
        // mScalableViewController.onLayout(changed, left, top, right, bottom);
        // }

        invalidate();
    }

    /**
     * 미용효과를 부여할 이미지를 설정한다.
     * 
     * @param bm 원본 이미지
     * @version 2.0
     */
    public void setImageBitmap(Bitmap bm) {
        mImage = bm;
        if(mImage != null) {
            mSubBitmap = Bitmap.createBitmap(mImage.getWidth(), mImage.getHeight(),
                                             Config.ARGB_8888);
            mCanvas = new Canvas(mSubBitmap);
            if(mScalableViewController != null) {
                mScalableViewController.setSize(mImage.getWidth(), mImage.getHeight());
            }
        }
        requestLayout();
    }

    protected PointF mLastPoint = new PointF();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float scale = (float)mImage.getWidth() / (float)getWidth();
        mLastPoint.x = event.getX() * scale;
        mLastPoint.y = event.getY() * scale;
        mapScaledPoint(mLastPoint);
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if(!mBlockEffect) {
                    mEffectProgress = true;
                    mStartPoint.x = mLastPoint.x;
                    mStartPoint.y = mLastPoint.y;
                }
            case MotionEvent.ACTION_MOVE:
                if(mEffectProgress) {
                    zooming = true;
                    // 터치 시작 좌표/현재 좌표를 기준으로 degree를 구한다.
                    // TODO: mRotation값은 BRUSH_TYPE_2_ARROW에서만 사용되므로 분기처리하여 불필요한 계산을 최소화 할 수 있다.
                    mRotation = -(float)Math.toDegrees(3.141592653589793 - (Math.atan2(mLastPoint.y
                            - mStartPoint.y, mLastPoint.x - mStartPoint.x))) - 135f;

                    if(mBrushType == BRUSH_TYPE_2_ARROW) {
                        // BRUSH_TYPE_2_ARROW은 drag하여도 위치가 변하지 않는다.
                        mEndPoint = mStartPoint;
                    } else {
                        mEndPoint.x = mLastPoint.x;
                        mEndPoint.y = mLastPoint.y;
                    }
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                invalidate();
            case MotionEvent.ACTION_POINTER_DOWN:
                mEffectProgress = false;
                zooming = false;
                break;
            default:
                break;
        }

        // 멀티터치일 경우 이미지 확대/이동 보기 기능이 동작한다.
        if(mScalableTouch && mScalableViewController != null && !mEffectProgress) {
            return mScalableViewController.onTouchEvent(event);
        }
        return mEffectProgress;
    }

    /**
     * 좌표를 확대/이동하기 이전에서의 좌표로 변환해 준다.
     */
    protected void mapScaledPoint(MotionEvent event) {
        if(mScalableViewController != null) {
            mScaledPoint[0] = event.getX();
            mScaledPoint[1] = event.getY();
            mScalableViewController.getInvertedMatrix().mapPoints(mScaledPoint);
            event.setLocation(mScaledPoint[0], mScaledPoint[1]);
        }
    }

    /**
     * 좌표를 확대/이동하기 이전에서의 좌표로 변환해 준다.
     */
    protected void mapScaledPoint(float[] points) {
        if(mScalableViewController != null) {
            mScalableViewController.getInvertedMatrix().mapPoints(points);
        }
    }

    /**
     * radius값을 확대/이동하기 이전에서의 값으로 변환해 준다.
     */
    protected float mapScaledRadius(float radius) {
        if(mScalableViewController != null) {
            return mScalableViewController.getInvertedMatrix().mapRadius(radius);
        }
        return radius;
    }

    /**
     * 좌표를 확대/이동하기 이전에서의 좌표로 변환해 준다.
     */
    protected void mapScaledPoint(PointF point) {
        if(mScalableViewController != null) {
            mScaledPoint[0] = point.x;
            mScaledPoint[1] = point.y;
            mScalableViewController.getInvertedMatrix().mapPoints(mScaledPoint);
            point.set(mScaledPoint[0], mScaledPoint[1]);
        }
    }

    /**
     * 좌표를 확대/이동했을 때의 좌표로 변환해 준다.
     */
    protected void unmapScaledPoint(MotionEvent event) {
        if(mScalableViewController != null) {
            mScaledPoint[0] = event.getX();
            mScaledPoint[1] = event.getY();
            mScalableViewController.getScaleMatrix().mapPoints(mScaledPoint);
            event.setLocation(mScaledPoint[0], mScaledPoint[1]);
        }
    }

    /**
     * 좌표를 확대/이동했을 때의 좌표로 변환해 준다.
     */
    protected void unmapScaledPoint(PointF point) {
        if(mScalableViewController != null) {
            mScaledPoint[0] = point.x;
            mScaledPoint[1] = point.y;
            mScalableViewController.getScaleMatrix().mapPoints(mScaledPoint);
            point.set(mScaledPoint[0], mScaledPoint[1]);
        }
    }

    /**
     * 미용 효과를 한단계 이전으로 되돌린다.
     * 
     * @version 2.0
     */
    public void undo() {
        if(canUndo()) {
            mCurrentIndex--;
            if(!isAuto) invalidate();
        }
    }

    /**
     * {@link #undo()}로 되돌린 미용 효과를 다시 적용한다.
     * 
     * @version 2.0
     */
    public void redo() {
        if(canRedo()) {
            mCurrentIndex++;
            invalidate();
        }
    }

    /**
     * 되돌릴 수 있는 미용효과가 있는지 확인한다.
     * 
     * @return 되돌릴 수 있는 미용 효과가 있으면 true를 반환
     * @version 2.0
     */
    public boolean canUndo() {
        if(mCurrentIndex >= 0) {
            return true;
        }
        return false;
    }
    
    /**
     * 효과가 적용된 횟수를 반환한다.
     */
    public int getEffectCount() {
        return mCurrentIndex + 1;
    }
    
    /**
     * 효과 적용을 비활성화 한다.
     */
    public void setBlockEvent(boolean isBlock) {
        mBlockEffect = isBlock;
    }
    
    /**
     * 효과 적용이 비활성화 되어 있는지 확인한다.
     */
    public boolean isBlockEvent() {
        return mBlockEffect;
    }

    /**
     * 다시 적용할 수 있는 미용효과가 있는지 확인한다.
     * 
     * @return 다시 적용할 수 있는 미용 효과가 있으면 true를 반환
     * @version 2.0
     */
    public boolean canRedo() {
        if(queue.size() - 1 > mCurrentIndex) {
            return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(isInEditMode()) {
            return;
        }
        onAlternativeDraw(canvas, false);
    }

    /**
     * 미용 효과가 적용된 이미지를 canvas에 그려준다.
     * 
     * @param canvas {@link android.graphics.Canvas}
     * @param output 최종 output일 경우 true로 설장한다.(false로 설정하면 사용자 가이드 요소도 포함된다.)
     * @version 2.0
     */
    public void onAlternativeDraw(Canvas canvas, boolean output) {
        // 이미지 확대/이동 보기를 적용한다.
        if(!output && mScalableViewController != null) {
            mCanvas.save();
            mCanvas.concat(mScalableViewController.getScaleMatrix());
        }
        mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        mCanvas.drawBitmap(mImage, 0, 0, mBrushPaint);
        if(!mShowOriginal) {
            // 효과 적용
            if(!queue.isEmpty() && mCurrentIndex != -1) {
                for(int i = 0; i <= mCurrentIndex; i++) {
                    mCanvas.drawBitmap(queue.get(i), pointqueue.get(i).left, pointqueue.get(i).top,
                                       mTouchImagePaint);
                }
            }
            if(!isAuto && !output) {
                drawMirror(mCanvas);
            }
        }
        if(!output && mScalableViewController != null) {
            mCanvas.restore();
        }

        canvas.save();
        float scale = (float)canvas.getWidth() / (float)mImage.getWidth();
        canvas.scale(scale, scale);
        canvas.drawBitmap(mSubBitmap, 0, 0, mBrushPaint);
        canvas.restore();
    }

    /**
     * 최종적으로 터치한 좌표의 이미지를 확대하여 canvas에 그려준다.
     * 
     * @param canvas 터치 좌표의 이미지를 확대하여 그릴 canvas
     * @version 2.0
     */
    public void drawMirror(Canvas canvas) {
        View zoomImageView = ((ViewGroup)getParent()).findViewById(getResources().getIdentifier("zoom_image_view",
                                                                                                "id",
                                                                                                getContext().getPackageName()));
        if(zooming || brushing) {
            // 브러시를 그려준다.
            if(mBrush != null) {
                canvas.save();
                if(mBrushType == BRUSH_TYPE_2_ARROW) {
                    canvas.rotate(mRotation, mStartPoint.x, mStartPoint.y);
                }
                canvas.scale(mBrushScale * mScalableViewController.getInvertScale(), mBrushScale
                        * mScalableViewController.getInvertScale(), mEndPoint.x, mEndPoint.y);
                canvas.drawBitmap(mBrush, mEndPoint.x - mBrush.getWidth() / 2,
                                  mEndPoint.y - mBrush.getHeight() / 2, mBrushPaint);
                canvas.drawBitmap(mBrushBg, mEndPoint.x - mBrushBg.getWidth() / 2, mEndPoint.y
                        - mBrushBg.getHeight() / 2, mBrushPaint);
                canvas.restore();
            }
        }

        if(zoomImageView != null && zoomImageView instanceof ZoomImageView) {
            if(zooming) {
                cropBitmap();
            }
            if(zooming && mCropImage != null) {
                ((ZoomImageView)zoomImageView).setCropImage(mCropImage);
            } else {
                ((ZoomImageView)zoomImageView).setCropImage(null);
            }
        }
    }

    private Rect mCropRect = new Rect();
    private Bitmap mCropImage;
    private Canvas mCropCanvas;

    /**
     * 좌/우측 확대 뷰에 보여줄 이미지를 가져온다.
     */
    private void cropBitmap() {
        unmapScaledPoint(mEndPoint);
        mCropRect.top = (int)(mEndPoint.y - mDensity * 40);
        mCropRect.bottom = (int)(mEndPoint.y + mDensity * 40);
        mCropRect.left = (int)(mEndPoint.x - mDensity * 40);
        mCropRect.right = (int)(mEndPoint.x + mDensity * 40);
        mapScaledPoint(mEndPoint);

        if(mCropRect.width() < 0 || mCropRect.height() < 0) {
            mCropImage = null;
            return;
        }

        if(mCropCanvas == null || mCropImage == null || mCropRect.width() != mCropImage.getWidth()
                || mCropRect.height() != mCropImage.getHeight()) {
            mCropImage = Bitmap.createBitmap(mCropRect.width(), mCropRect.height(),
                                             Config.ARGB_8888);
            mCropCanvas = new Canvas(mCropImage);
        }
        mCropCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);

        mCropCanvas.drawBitmap(mSubBitmap,
                               new Rect((int)(mCropRect.left), (int)(mCropRect.top),
                                        (int)(mCropRect.right), (int)(mCropRect.bottom)),
                               new Rect(0, 0, mCropImage.getWidth(), mCropImage.getHeight()),
                               mBrushPaint);
    }

    /**
     * 미용효과를 부여할 sub 이미지를 반환한다.
     * 
     * @return sub 이미지
     * @version 2.0
     */
    public Bitmap getTouchBitmap() {
        onAlternativeDraw(mCanvas, true);

        // 효과를 부여할 이미지 영역 계산.
        // 2: img_brush_bg의 반지름
        // mBrushScale: seekbar로 조절한 브러시 크기
        // 이미지 확대 보기 만큼 축소 시켜 준다.
        Rect bound = new Rect();
        if(mBrushType == BRUSH_TYPE_2_ARROW) {
            bound.top = (int)Math.max(0.0f, mStartPoint.y - mBrushBg.getHeight() / 2 * mBrushScale
                    * mScalableViewController.getInvertScale());
            bound.bottom = (int)Math.min(mCanvas.getHeight(), mStartPoint.y + mBrushBg.getHeight()
                    / 2 * mBrushScale * mScalableViewController.getInvertScale());
            bound.left = (int)Math.max(0.0f, mStartPoint.x - mBrushBg.getWidth() / 2 * mBrushScale
                    * mScalableViewController.getInvertScale());
            bound.right = (int)Math.min(mCanvas.getWidth(), mStartPoint.x + mBrushBg.getWidth() / 2
                    * mBrushScale * mScalableViewController.getInvertScale());
        } else {
            bound.top = (int)Math.max(0.0f, mEndPoint.y - mBrushBg.getHeight() / 2 * mBrushScale
                    * mScalableViewController.getInvertScale());
            bound.bottom = (int)Math.min(mCanvas.getHeight(), mEndPoint.y + mBrushBg.getHeight()
                    / 2 * mBrushScale * mScalableViewController.getInvertScale());
            bound.left = (int)Math.max(0.0f, mEndPoint.x - mBrushBg.getWidth() / 2 * mBrushScale
                    * mScalableViewController.getInvertScale());
            bound.right = (int)Math.min(mCanvas.getWidth(), mEndPoint.x + mBrushBg.getWidth() / 2
                    * mBrushScale * mScalableViewController.getInvertScale());
        }
        return getBitmap(bound);
    }

    public Bitmap getBitmap(Rect bound) {
    	return getBitmap(bound, false);
    }
    
    public Bitmap getBitmap(Rect bound, boolean output) {
		onAlternativeDraw(mCanvas, true);

		if (bound.width() < 0 || bound.height() < 0) {
			return null;
		}
		
		Bitmap touchImage = Bitmap.createBitmap(bound.width(), bound.height(), Config.ARGB_8888);
		Canvas canvas = new Canvas(touchImage);
		
		if (output) {
			int width = bound.width();
			int height = bound.height();
			Bitmap sub = Bitmap.createBitmap(mSubBitmap,bound.left, bound.top, bound.width(), bound.height());
			int[] inPixels = new int[width * height];
			int[] outPixels = new int[width * height];
			float alpha_bound = 0.25f;
			sub.getPixels(inPixels,0, width,0, 0, width, height);
			int xa = 0;
			int ya = 0;
			int alpha = 0;
			for (int y = 0; y < height; y++) {
		        for (int x = 0; x < width; x++) {
		        	if(x>(width*alpha_bound) && x<(width*(1.0-alpha_bound))) {
		        		xa = 0xff;
		        	}
		        	else if(x<(width*alpha_bound)) {
		        		xa = (int) (x * 0xff / ((width-1)*alpha_bound ));
		        	}
		        	else if(x>(width*(1.0-alpha_bound))){
		        		xa = (int) (((width-x-1) * 0xff) / ((width-1)*alpha_bound));
		        	}
		        	
		        	if(y>height*alpha_bound && y<(height*(1.0-alpha_bound))){
		        		ya = 0xff;
		        	}
		        	else if(y<(height*alpha_bound)) {
		        		ya = (int) (y * 0xff / ((height-1)*alpha_bound));
		        	}
		        	else if(y>(height*(1.0-alpha_bound))){
		        		ya = (int) ((((height-1)-y) * 0xff) / ((height-1)*alpha_bound ));
		        	}
		           
		            alpha = Math.min(0xff, Math.min(xa, ya));
		            outPixels[y * width + x] = (alpha << 24|0x00ffffff) & inPixels[y*width+x];
		        }
		    }
			sub.setPixels(outPixels, 0,  width, 0,0, width, height);
			canvas.drawBitmap(sub,0, 0,mBrushPaint);
			
		}
		else {
			canvas.drawBitmap(mSubBitmap, new Rect(bound.left, bound.top, bound.right, bound.bottom), new Rect(0, 0, touchImage.getWidth(), touchImage.getHeight()), mBrushPaint);
		}

		// undo 되어 있는 상태일 경우 mCurrentIndex 이후의 객체는 모두 삭제한다.
		for (int i = 0; i < queue.size();) {
			if (i <= mCurrentIndex) {
				i++;
			} else {
				pointqueue.remove(i);
				queue.remove(i);
			}
		}
		pointqueue.add(bound);
		queue.add(touchImage);

		mCurrentIndex = queue.size() - 1;

		return touchImage;
    }
    
    

    /**
     * {@link #BRUSH_TYPE_4_ARROW}로 설정된 경우 미용효과를 부여할 방향을 반환한다.
     * 
     * @return 미용효과를 부여할 방향
     * @remark [0,0]~[10,10]의 값을 가지며, 값에 따른 방향은 아래와 같다.<br>
     *         [0,0]: 좌,상단<br>
     *         [5,5]: 중앙<br>
     *         [10,10]: 우,하단
     * @version 2.0
     */
    public float[] getDirection() {
        float[] direction = new float[2];
        direction[0] = (float)Math.sin(Math.toRadians(mRotation + 45));
        direction[0] += 1;
        direction[0] *= 5;
        direction[1] = (float)Math.sin(Math.toRadians(mRotation - 45));
        direction[1] += 1;
        direction[1] *= 5;
        return direction;
    }

    /**
     * 효과를 적용할 영역을 선택 중인지 확인한다.
     */
    public boolean isEffectProgress() {
        return mEffectProgress;
    }

    /**
     * 이미지 확대/이동 보기 지원 여부를 설정한다.
     */
    public void setScalableTouch(boolean scalableTouch) {
        mScalableTouch = scalableTouch;
    }

    /**
     * ScalableViewController를 설정한다.
     */
    public void setScalableViewController(ScalableViewController controller) {
        mScalableViewController = controller;
    }

    /**
     * 이미지 확대/이동 보기 지원 여부를 설정하고, ScalableViewController을 초기화 한다.
     */
    public void setScalable(boolean scalable) {
        mScalableTouch = scalable;
        if(scalable) {
            mScalableViewController = new ScalableViewController(getContext());
            if(mImage != null) {
                mScalableViewController.setSize(mImage.getWidth(), mImage.getHeight());
            }
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
    
    public void setAuto(boolean isAutoMode){
    	this.isAuto = isAutoMode;
    }
}
