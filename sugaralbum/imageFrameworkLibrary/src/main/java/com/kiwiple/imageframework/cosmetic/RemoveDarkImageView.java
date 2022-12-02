
package com.kiwiple.imageframework.cosmetic;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * 얼굴 사진의 다크서클 제거 기능 지원을 위한BeautyEffectImageView 상속 클래스
 * 
 * @version 2.0
 */
public class RemoveDarkImageView extends BeautyEffectImageView {
    protected boolean mDragMode = false;
    protected List<PointF> mPoints = new ArrayList<PointF>();
    private Path mPath;
    private Paint mDragPaint;

    public RemoveDarkImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public RemoveDarkImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RemoveDarkImageView(Context context) {
        super(context);
    }

    @Override
    protected void init() {
        super.init();
        mTouchImagePaint.setXfermode(new PorterDuffXfermode(Mode.LIGHTEN));

        mSkinPaint = new Paint();

        mDragPaint = new Paint();
        mDragPaint.setStyle(Style.STROKE);
        // 선을 전체적으로 둥글게 만들어 준다.
        mDragPaint.setPathEffect(new CornerPathEffect(10));
        mDragPaint.setStrokeCap(Cap.ROUND);
        mDragPaint.setStrokeJoin(Join.ROUND);
        mDragPaint.setARGB(0x35, 0xff, 0xff, 0xff);
        mScaleQueue = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float scale = (float)mImage.getWidth() / (float)getWidth();
        mLastPoint.x = event.getX() * scale;
        mLastPoint.y = event.getY() * scale;
        mapScaledPoint(mLastPoint);
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if(!mBlockEffect) {
                    if(mBrush != null) {
                        mDragPaint.setStrokeWidth(mBrush.getHeight() / 10f * mBrushScale
                                * mScalableViewController.getInvertScale());
                    }
                    mPoints.clear();
                    addPoint(mLastPoint.x, mLastPoint.y);
                    mPath = new Path();
                    mPath.moveTo(mLastPoint.x, mLastPoint.y);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if(mPath != null) {
                    mDragMode = true;
                    addPoint(mLastPoint.x, mLastPoint.y);
                    mPath.lineTo(mLastPoint.x, mLastPoint.y);
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mPoints.clear();
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mPath = null;
                mDragMode = false;
                break;
        }
        return super.onTouchEvent(event);
    }

    public void addPoint(float x, float y) {
        addPoint(new PointF(x, y));
    }

    public void addPoint(PointF point) {
        mPoints.add(point);
    }

    protected Paint mSkinPaint;

    @Override
    public Bitmap getTouchBitmap() {
        if(mPoints == null || mPoints.isEmpty()) {
            return null;
        }
        Bitmap touchImage = null;
        Rect dstBound = new Rect();
        // 10: img_brush_01에서 점의 반지름
        // mBrushScale: seekbar로 조절한 브러시 크기
        // 이미지 확대 보기 만큼 축소 시켜 준다.
        float brushRadius = mBrush.getHeight() / 10f * mBrushScale
                * mScalableViewController.getInvertScale();
        int srcColor;
        try {
            // 터치영역 하단의 색상 값을 가져온다.
            srcColor = mImage.getPixel((int)(mStartPoint.x), (int)(mStartPoint.y + brushRadius));
        } catch(IllegalArgumentException e) {
            // x, y exceed the bitmap's bounds, skip
            return null;
        }
        int centerColor = srcColor;
        centerColor = 0x00ffffff & centerColor;
        centerColor = 0x35000000 | centerColor;
        int edgeColor = 0x00ffffff & centerColor;

        if(!mDragMode) {
            dstBound.top = (int)Math.max(0.0f, mEndPoint.y - brushRadius);
            dstBound.bottom = (int)Math.min(mCanvas.getHeight(), mEndPoint.y + brushRadius);
            dstBound.left = (int)Math.max(0.0f, mEndPoint.x - brushRadius);
            dstBound.right = (int)Math.min(mCanvas.getWidth(), mEndPoint.x + brushRadius);

            if(dstBound.width() == 0 || dstBound.height() == 0 || dstBound.left < 0
                    || dstBound.right > mCanvas.getWidth() || dstBound.top < 0
                    || dstBound.bottom > mCanvas.getHeight()) {
                return null;
            }

            touchImage = Bitmap.createBitmap(dstBound.right - dstBound.left, dstBound.bottom
                    - dstBound.top, Config.ARGB_8888);
            Canvas canvas = new Canvas(touchImage);

            // 중심의 alpha 값 0x35, 원의 바깥으로 갈수록 0x00으로 희미해 진다.(효과 영역에 경계선이 생기지 않도록)
            RadialGradient gradient = new RadialGradient(dstBound.width() / 2f,
                                                         dstBound.height() / 2f,
                                                         dstBound.width() / 2f, centerColor,
                                                         edgeColor,
                                                         android.graphics.Shader.TileMode.CLAMP);
            mSkinPaint.reset();
            mSkinPaint.setShader(gradient);
            canvas.drawCircle(dstBound.width() / 2f, dstBound.height() / 2f, dstBound.width() / 2f,
                              mSkinPaint);

        } else {
            dstBound.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
            for(PointF point : mPoints) {
                dstBound.set((int)Math.min(dstBound.left, point.x),
                             (int)Math.min(dstBound.top, point.y),
                             (int)Math.max(dstBound.right, point.x),
                             (int)Math.max(dstBound.bottom, point.y));
            }
            dstBound.left -= brushRadius;
            dstBound.top -= brushRadius;
            dstBound.right += brushRadius;
            dstBound.bottom += brushRadius;

            dstBound.top = (int)Math.max(0.0f, dstBound.top);
            dstBound.bottom = (int)Math.min(mCanvas.getHeight(), dstBound.bottom);
            dstBound.left = (int)Math.max(0.0f, dstBound.left);
            dstBound.right = (int)Math.min(mCanvas.getWidth(), dstBound.right);

            if(dstBound.width() == 0 || dstBound.height() == 0 || dstBound.left < 0
                    || dstBound.right > mCanvas.getWidth() || dstBound.top < 0
                    || dstBound.bottom > mCanvas.getHeight()) {
                return null;
            }

            touchImage = Bitmap.createBitmap(dstBound.right - dstBound.left, dstBound.bottom
                    - dstBound.top, Config.ARGB_8888);
            Canvas canvas = new Canvas(touchImage);

            mSkinPaint.reset();
            mSkinPaint.setStyle(Style.STROKE);
            mSkinPaint.setPathEffect(new CornerPathEffect(brushRadius));
            mSkinPaint.setStrokeCap(Cap.ROUND);
            mSkinPaint.setStrokeJoin(Join.ROUND);
            mSkinPaint.setMaskFilter(new BlurMaskFilter(brushRadius / 2,
            // TODO: beauty-11353-dev에서 코드 수정하였음. 반영해야 됨. 원을 반복적으로 그리지 않고 path 하나로 처리.
                                                   BlurMaskFilter.Blur.NORMAL));
            mSkinPaint.setStrokeWidth(brushRadius);
            mSkinPaint.setColor(centerColor);
            
            Path path = null;
            for(PointF point : mPoints) {
                if(path == null) {
                    path = new Path();
                    path.moveTo(point.x - dstBound.left, point.y - dstBound.top);
                } else {
                    path.lineTo(point.x - dstBound.left, point.y - dstBound.top);
                }
            }
            canvas.drawPath(path, mSkinPaint);
        }
        if(touchImage != null) {
            for(int i = 0; i < queue.size();) {
                if(i <= mCurrentIndex) {
                    i++;
                } else {
                    pointqueue.remove(i);
                    queue.remove(i);
                }
            }
            pointqueue.add(dstBound);
            queue.add(touchImage);

            mCurrentIndex = queue.size() - 1;
        }
        return touchImage;
    }

    public Bitmap getBitmap(List<PointF> points) {
        if(points == null || points.isEmpty()) {
            return null;
        }
        
        mapScaledPoint(points.get(0));
        
        Bitmap touchImage = null;
        Rect dstBound = new Rect();
        float brushRadius = mBrush.getHeight() / 10f * mBrushScale
                * mScalableViewController.getInvertScale();
        int srcColor;
        try {
            srcColor = mImage.getPixel((int)(mStartPoint.x), (int)(mStartPoint.y + brushRadius));
        } catch(IllegalArgumentException e) {
            // x, y exceed the bitmap's bounds, skip
            return null;
        }
        int centerColor = srcColor;
        centerColor = 0x00ffffff & centerColor;
        centerColor = 0x35000000 | centerColor;
        dstBound.set(Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE);
        for(PointF point : points) {
            dstBound.set((int)Math.min(dstBound.left, point.x),
                         (int)Math.min(dstBound.top, point.y),
                         (int)Math.max(dstBound.right, point.x),
                         (int)Math.max(dstBound.bottom, point.y));
        }
        dstBound.left -= brushRadius;
        dstBound.top -= brushRadius;
        dstBound.right += brushRadius;
        dstBound.bottom += brushRadius;

        dstBound.top = (int)Math.max(0.0f, dstBound.top);
        dstBound.bottom = (int)Math.min(mCanvas.getHeight(), dstBound.bottom);
        dstBound.left = (int)Math.max(0.0f, dstBound.left);
        dstBound.right = (int)Math.min(mCanvas.getWidth(), dstBound.right);

        if(dstBound.width() == 0 || dstBound.height() == 0 || dstBound.left < 0
                || dstBound.right > mCanvas.getWidth() || dstBound.top < 0
                || dstBound.bottom > mCanvas.getHeight()) {
            return null;
        }
        touchImage = Bitmap.createBitmap(dstBound.right - dstBound.left, dstBound.bottom
                - dstBound.top, Config.ARGB_8888);
        Canvas canvas = new Canvas(touchImage);
        mSkinPaint.reset();
        mSkinPaint.setStyle(Style.FILL_AND_STROKE);
        mSkinPaint.setPathEffect(new CornerPathEffect(brushRadius));
        mSkinPaint.setStrokeCap(Cap.ROUND);
        mSkinPaint.setStrokeJoin(Join.ROUND);
        mSkinPaint.setMaskFilter(new BlurMaskFilter(brushRadius / 2, BlurMaskFilter.Blur.NORMAL));
        mSkinPaint.setStrokeWidth(brushRadius);
        mSkinPaint.setColor(centerColor);//to test change color 0xffff0000);//
        
        Paint mPaintFace = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaintFace.setStyle(Paint.Style.STROKE);
        mPaintFace.setColor(Color.TRANSPARENT);

        Path path = null;
        for(PointF point : points) {
            if(path == null) {
                path = new Path();
                path.moveTo(point.x - dstBound.left, point.y - dstBound.top);
            } else {
                path.lineTo(point.x - dstBound.left, point.y - dstBound.top);
            }
        }
        canvas.drawPath(path, mSkinPaint);
        canvas.drawRect(dstBound, mPaintFace);
        // undo 되어 있는 상태일 경우 mCurrentIndex 이후의 객체는 모두 삭제한다.
        if(touchImage != null) {
            for(int i = 0; i < queue.size();) {
                if(i <= mCurrentIndex) {
                    i++;
                } else {
                    pointqueue.remove(i);
                    queue.remove(i);
                }
            }
            pointqueue.add(dstBound);
            queue.add(touchImage);

            mCurrentIndex = queue.size() - 1;
        }
        
       
        
        return touchImage;
    }

    @Override
    public void drawMirror(Canvas canvas) {
        // drag한 영역을 반투명 흰색으로 표시해준다.
        if(zooming) {
            if(mDragMode && mPath != null) {
                canvas.drawPath(mPath, mDragPaint);
            }
        }
        super.drawMirror(canvas);
    }
}
