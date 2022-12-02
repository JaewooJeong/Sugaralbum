
package com.kiwiple.imageframework.cosmetic;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.PorterDuff.Mode;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.kiwiple.imageframework.cosmetic.FilterManager.FilterInfo;
import com.kiwiple.imageframework.cosmetic.FilterManager.FilterManagerListener;

public class MosaicDrawingImageView extends BeautyEffectImageView {
    private float mBrushSize = 30f;

    // private Matrix mInvertedFrameMatrix = new Matrix();
    // private float[] mInvertedPoint = new float[2];
    // private float mInvertedRadius;

    private ArrayList<Path> queue = new ArrayList<Path>();
    private ArrayList<Paint> paintQueue = new ArrayList<Paint>();

    private Bitmap mMosaicImage;

    private boolean mBlockInvalidate = false;

    public MosaicDrawingImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public MosaicDrawingImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MosaicDrawingImageView(Context context) {
        super(context);
        init();
    }

    @Override
    public void invalidate() {
        if(!mBlockInvalidate) {
            super.invalidate();
        }
    }

    @Override
    protected void init() {
        super.init();
        FilterManager.getInstance(getContext().getApplicationContext())
                     .setFilterManagerListener(mFilterManagerListener);

        // 하드웨어 가속에서는 미지원 또는 API level 제한이 있어 software rendering을 하도록 고정.
        // 참고: http://developer.android.com/guide/topics/graphics/hardware-accel.html
        setLayerType(View.LAYER_TYPE_SOFTWARE, new Paint(Paint.FILTER_BITMAP_FLAG));
        if(mBrushBg != null) {
            mBrushSize = mBrushBg.getWidth() * mBrushScale;
        }
    }

    @Override
    public void reset() {
        super.reset();
        queue.clear();
        paintQueue.clear();
    }

    @Override
    public void setBrushSize(int progress) {
        mBrushScale = (progress + 10) / 200f;

        if(mBrush != null) {
            mBrushSize = mBrush.getWidth() * mBrushScale;
        }

        invalidate();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(isInEditMode()) {
            return;
        }

        // mImageMatrix.invert(mInvertedFrameMatrix);
        invalidate();
    }

    @Override
    public void setImageBitmap(Bitmap image) {
        super.setImageBitmap(image);
        reset();
        // 모자이크 효과가 적용된 이미지를 준비한다.
        mMosaicImage = mImage.copy(Config.ARGB_8888, true);
        FilterManager.getInstance(getContext().getApplicationContext())
                     .addFilterData(new FilterInfo("Pixellate", mMosaicImage, new float[] {
                         2
                     }, null));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mBlockInvalidate = true;
        boolean retVal = super.onTouchEvent(event);
        mBlockInvalidate = false;

        // mInvertedPoint[0] = event.getX();
        // mInvertedPoint[1] = event.getY();
        // mInvertedFrameMatrix.mapPoints(mInvertedPoint);
        // mInvertedRadius = mInvertedFrameMatrix.mapRadius(mBrushSize);
        // mapScaledPoint(mInvertedPoint);
        // mInvertedRadius = mapScaledRadius(mInvertedRadius);
        Path path;
        Paint paint;
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if(!mBlockEffect) {
                    // action_move 없이 action_up이 될 경우를 대비해 원형으로 모자이크 효과를 준다.
                    path = new Path();
                    path.setFillType(FillType.EVEN_ODD);
                    path.addCircle(mLastPoint.x, mLastPoint.y,
                                   mBrushSize * mScalableViewController.getInvertScale() / 2,
                                   Path.Direction.CCW);
    
                    paint = newPaint(Style.FILL);
                    addPath(path, paint);
    
                    path = new Path();
                    path.moveTo(mLastPoint.x, mLastPoint.y);
                    paint = newPaint(Style.STROKE);
                    addPath(path, paint);
                }
            case MotionEvent.ACTION_MOVE:
                if(mEffectProgress) {
                    queue.get(queue.size() - 1).lineTo(mLastPoint.x, mLastPoint.y);
                    retVal = true;
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // TODO: 불필요 한 코드... BeautiEffectImageView.onTouchEvent에서 mEffectProgress가 무조건 false로 세팅된다. 삭제 필요.
                if(mEffectProgress) {
                    path = new Path();
                    path.setFillType(FillType.EVEN_ODD);
                    path.addCircle(mLastPoint.x, mLastPoint.y,
                                   mBrushSize * mScalableViewController.getInvertScale() / 2,
                                   Path.Direction.CCW);

                    paint = newPaint(Style.FILL);
                    addPath(path, paint);
                    retVal = true;
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // 멀티터치가 시작되면 진행 중이던 효과를 제거한다.
                if(!mBlockEffect) {
                    if(mCurrentIndex >= 0) {
                        queue.remove(mCurrentIndex);
                        paintQueue.remove(mCurrentIndex);
                        mCurrentIndex--;
                    }
                    if(mCurrentIndex >= 0) {
                        queue.remove(mCurrentIndex);
                        paintQueue.remove(mCurrentIndex);
                        mCurrentIndex--;
                    }
                }
                break;
        }
        if(retVal) {
            invalidate();
        }
        return retVal;
    }

    /**
     * Paint 인스턴스 생성
     */
    private Paint newPaint(Style style) {
        Paint paint = new Paint();
        paint.setStyle(Style.STROKE);
        // 선을 전체적으로 둥글게 만들어 준다.
        paint.setPathEffect(new CornerPathEffect(10));
        paint.setStrokeCap(Cap.ROUND);
        paint.setStrokeJoin(Join.ROUND);
        BitmapShader bs;
        if(mMosaicImage != null) {
            bs = new BitmapShader(mMosaicImage, TileMode.CLAMP, TileMode.CLAMP);
        } else {
            bs = new BitmapShader(mImage, TileMode.CLAMP, TileMode.CLAMP);

        }
        paint.setShader(bs);
        paint.setStyle(style);
        if(mBrush != null) {
            // 이미지 확대 보기에 따라 브러시 크기가 달라지지 않도록 해준다. 
            paint.setStrokeWidth(mBrushSize * mScalableViewController.getInvertScale());
        }
        return paint;
    }

    /**
     * 모자이크 영역을 추가해준다.
     */
    private void addPath(Path path, Paint paint) {
        // undo 되어 있는 상태일 경우 mCurrentIndex 이후의 객체는 모두 삭제한다.
        for(int i = 0; i < queue.size();) {
            if(i <= mCurrentIndex) {
                i++;
            } else {
                paintQueue.remove(i);
                queue.remove(i);
            }
        }

        paintQueue.add(paint);
        queue.add(path);
        mCurrentIndex = queue.size() - 1;
    }

    @Override
    public void onAlternativeDraw(Canvas canvas, boolean output) {
        // 이미지 확대/이동 보기를 적용한다.
        if(!output && mScalableViewController != null) {
            mCanvas.save();
            mCanvas.concat(mScalableViewController.getScaleMatrix());
        }
        mCanvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        mCanvas.drawBitmap(mImage, 0, 0, mBrushPaint);
        if(!mShowOriginal) {
            // 모자이크 효과 적용
            if(mMosaicImage != null) {
                if(!queue.isEmpty() && mCurrentIndex != -1) {
                    for(int i = 0; i <= mCurrentIndex; i++) {
                        mCanvas.drawPath(queue.get(i), paintQueue.get(i));
                    }
                }
            }
            if(!output) {
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

    private FilterManagerListener mFilterManagerListener = new FilterManagerListener() {
        @Override
        public void onImageFilteringComplete(final Bitmap data, Object object) {
            // 원본 이미지에 대한 모자이크 처리가 완료되어도 별다른 처리는 하지 않는다.
        }
    };

    @Override
    public void undo() {
        // 효과 시작점에 원형 효과를 추가하였기 때문에 2번 undo 해주어야 한다.
        if(canUndo()) {
            mCurrentIndex--;
        }
        if(canUndo()) {
            mCurrentIndex--;
        }
        invalidate();
    }

    @Override
    public void redo() {
        // undo와 마찬가지 이유로 2번 redo해 주어야 한다.
        if(canRedo()) {
            mCurrentIndex++;
        }
        if(canRedo()) {
            mCurrentIndex++;
        }
        invalidate();
    }

    @Override
    public boolean canRedo() {
        // queue가 BeautyEffectImageView와 다르기 때문에 상속
        if(queue.size() - 1 > mCurrentIndex) {
            return true;
        }
        return false;
    }

    @Override
    public int getEffectCount() {
        // 효과 1개에 Bitmap이 2개이므로...
        return (mCurrentIndex + 1) / 2;
    }
}
