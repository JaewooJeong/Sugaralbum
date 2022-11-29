
package com.kiwiple.imageframework.drawing.view;

import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.almeros.android.multitouch.gesturedetectors.MoveGestureDetector;
import com.kiwiple.imageframework.drawing.model.DrawArrow;
import com.kiwiple.imageframework.drawing.model.DrawErazer;
import com.kiwiple.imageframework.drawing.model.DrawFreeHand;
import com.kiwiple.imageframework.drawing.model.DrawLine;
import com.kiwiple.imageframework.drawing.model.DrawObject;
import com.kiwiple.imageframework.drawing.model.DrawOval;
import com.kiwiple.imageframework.drawing.model.DrawRectangle;
import com.kiwiple.imageframework.drawing.model.DrawRoundRectangle;
import com.kiwiple.imageframework.drawing.model.DrawStamp;
import com.kiwiple.imageframework.drawing.model.DrawTransparentFreeHand;

public class DrawingView extends View {
    private GestureDetector mGestureDetector;
    private MoveGestureDetector mMoveDetector;

    /**
     * 현재 선택된 객체
     */
    private DrawObject mCurrentObject;
    private List<DrawObject> mObjects = new ArrayList<DrawObject>();

    /**
     * 객체의 모양. 
     * 
     * @see {@link com.kiwiple.imageframework.drawing.model.DrawObject#DRAWING_TYPE}
     */
    private int mDrawingType = -1;
    private Bitmap mStampImage = null;
    private Bitmap mStampArrow = null;
    /**
     * 스탬프의 모양. 
     * 
     * @see {@link com.kiwiple.imageframework.drawing.model.DrawStamp#STAMP_TYPE}
     */
    private int mStampType = -1;

    private int mColor = DrawObject.DEFAULT_COLOR;
    private float mStrokeWidth = DrawObject.DEFAULT_STORKE_WIDTH;

    private OnDrawingObjectAddListener mOnDrawingObjectAddListener;

    private final static int HANDLER_MESSAGE_INVALIDATE = 0;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case HANDLER_MESSAGE_INVALIDATE:
                    mHandler.removeMessages(HANDLER_MESSAGE_INVALIDATE);
                    invalidate();
                    break;
            }
        }
    };

    public DrawingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DrawingView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        // 하드웨어 가속에서는 미지원 또는 API level 제한이 있어 software rendering을 하도록 고정.
        // 참고: http://developer.android.com/guide/topics/graphics/hardware-accel.html
        if(Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, new Paint(Paint.FILTER_BITMAP_FLAG));
        }

        // Setup Gesture Detectors
        mGestureDetector = new GestureDetector(context.getApplicationContext(),
                                               new SimpleGestureListener());
        mMoveDetector = new MoveGestureDetector(context.getApplicationContext(), new MoveListener());
    }

    @SuppressLint("WrongCall")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isEnabled()) {
            return false;
        }
        mGestureDetector.onTouchEvent(event);
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                if (mCurrentObject == null) {
                    mCurrentObject = createNewDrawingObject();
                    if(mCurrentObject != null) {
                        mCurrentObject.onTouchDown(event);

                        // undo 되어 있는 상태일 경우 mCurrentIndex 이후의 객체는 모두 삭제한다.
                        for(int i = 0; i < mObjects.size();) {
                            if(i <= mCurrentIndex) {
                                i++;
                            } else {
                                mObjects.remove(i);
                            }
                        }
                        mObjects.add(mCurrentObject);
                        mCurrentIndex = mObjects.size() - 1;
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                finishCurrentObject();
                break;
        }

        try {
            if(mCurrentObject != null) {
                mMoveDetector.onTouchEvent(event);
            }
        } catch(Exception e) {
            // do nothing
        }
        return mCurrentObject != null;
    }

    public void finishCurrentObject() {
        if(mOnDrawingObjectAddListener != null && mCurrentObject != null) {
            RectF rect = mCurrentObject.getBoundRect();
            if((int)rect.width() > 0 && (int)rect.height() > 0) {
                Bitmap image = Bitmap.createBitmap((int)rect.width(), (int)rect.height(),
                                                   Config.ARGB_8888);
                Canvas canvas = new Canvas(image);
                canvas.translate(-rect.left, -rect.top);
                mCurrentObject.onDraw(canvas, true);
                mOnDrawingObjectAddListener.onDrawingObjectAdd((int)rect.left, (int)rect.top, image);
            }
        }
        clear();
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            if(mCurrentObject != null) {
                mCurrentObject.onMove(detector.getCurrMotionEvent().getX(),
                                      detector.getCurrMotionEvent().getY());
                mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
            }
            return true;
        }
    }

    private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // onSingleTapUp일 경우 스탬프만 그려주고 나머지는 그리지 않도록 해준다.
            if(mCurrentObject != null && mCurrentObject instanceof DrawStamp) {
                return false;
            }
            clear();
            return true;
        }
    }

    @SuppressLint("WrongCall")
    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        if(!mObjects.isEmpty() && mCurrentIndex != -1) {
            for(int i = 0; i <= mCurrentIndex; i++) {
                mObjects.get(i).onDraw(canvas, false);
            }
        }
    }

    private DrawObject createNewDrawingObject() {
        DrawObject newObject = null;
        switch(mDrawingType) {
            case DrawObject.DRAWING_TYPE_LINE:
                newObject = new DrawLine();
                break;
            case DrawObject.DRAWING_TYPE_RECTANGLE:
                newObject = new DrawRectangle();
                break;
            case DrawObject.DRAWING_TYPE_ROUND_RECT:
                newObject = new DrawRoundRectangle();
                break;
            case DrawObject.DRAWING_TYPE_OVAL:
                newObject = new DrawOval();
                break;
            case DrawObject.DRAWING_TYPE_FREEHAND:
                newObject = new DrawFreeHand();
                break;
            case DrawObject.DRAWING_TYPE_ERAZER:
                newObject = new DrawErazer();
                break;
            case DrawObject.DRAWING_TYPE_ALPHA_FREEHAND:
                newObject = new DrawTransparentFreeHand();
                break;
            case DrawObject.DRAWING_TYPE_ARROW:
                newObject = new DrawArrow();
                break;
            case DrawObject.DRAWING_TYPE_STAMP:
                if(mStampImage != null) {
                    // 사용자가 설정한 스탬프 이미지
                    newObject = new DrawStamp(getContext());
                    ((DrawStamp)newObject).setStamp(mStampImage);
                    ((DrawStamp)newObject).setArrow(mStampArrow);
                } else if(mStampType != -1) {
                    // 타입별로 미리 정의된 스탬프 이미지
                    newObject = new DrawStamp(getContext());
                    ((DrawStamp)newObject).setType(mStampType, getContext());
                }
                break;
            default:
                break;
        }
        if(newObject != null) {
            newObject.init();
            newObject.setDefaultStrokeWidth(mStrokeWidth);
            newObject.setDefaultColor(mColor);
        }
        return newObject;
    }

    /**
     * 브러시 종류를 설정한다.
     * @param type 브러시 종류
     */
    public void setDrawingType(int type) {
        mDrawingType = type;
    }

    /**
     * 현재 설정된 브러시 종류를 반화한다.
     * @return 브러시 종류
     */
    public int getDrawingType() {
        return mDrawingType;
    }

    /**
     * 스탬프 종류 설정한다.
     * @param type 스탬프 종류
     * 
     * @see {@link com.kiwiple.imageframework.drawing.model.DrawStamp#STAMP_TYPE}
     */
    public void setStampType(int type) {
        mStampType = type;
        if(mStampType != -1) {
            mStampImage = null;
            mStampArrow = null;
        }
    }

    /**
     * 현재 설정된 스탬프의 종류를 반환한다.
     * @return 스탬프 종류
     * 
     * @see {@link com.kiwiple.imageframework.drawing.model.DrawStamp#STAMP_TYPE}
     */
    public int getStampType() {
        return mStampType;
    }

    /**
     * {@link com.kiwiple.imageframework.drawing.model.DrawStamp#STAMP_TYPE}로 미리 정의된 스탬프가 아닌 사용자 정의 스탬프로 설정한다.
     * 
     * @param stamp 스탬프 이미지
     * @param arrow 화살표 이미지
     */
    public void setStampImage(Bitmap stamp, Bitmap arrow) {
        mStampImage = stamp;
        mStampArrow = arrow;
        if(mStampImage != null) {
            mStampType = -1;
        }
    }

    /**
     * 현재 설정된 사용자 정의 스탬프 이미지를 반환한다.
     * @return 스탬프 이미지
     */
    public Bitmap getStampIcon() {
        return mStampImage;
    }

    /**
     * 현재 설정된 사용자 정의 스탬프 화살표 이미지를 반환한다.
     * @return 화살표 이미지
     */
    public Bitmap getStampArrowImage() {
        return mStampArrow;
    }

    /**
     * 브러시 두께를 설정한다.
     * @param width 브러시 두께
     */
    public void setStokeWidth(float width) {
        mStrokeWidth = width;
        if(mCurrentObject != null) {
            mCurrentObject.setDefaultStrokeWidth(width);
        }
    }

    /**
     * 브래시 색상을 설정한다.
     * @param color 색상
     */
    public void setColor(int color) {
        mColor = color;
        if(mCurrentObject != null) {
            mCurrentObject.setDefaultColor(color);
        }
    }

    /**
     * 리소스를 반환한다.
     */
    public void clear() {
        mCurrentObject = null;
        mObjects.clear();
        mCurrentIndex = -1;
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    protected int mCurrentIndex = -1;

    /**
     * 미용 효과를 한단계 이전으로 되돌린다.
     * 
     * @version 2.0
     */
    public void undo() {
        if(canUndo()) {
            mCurrentIndex--;
            invalidate();
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
     * 다시 적용할 수 있는 미용효과가 있는지 확인한다.
     * 
     * @return 다시 적용할 수 있는 미용 효과가 있으면 true를 반환
     * @version 2.0
     */
    public boolean canRedo() {
        if(mObjects.size() - 1 > mCurrentIndex) {
            return true;
        }
        return false;
    }

    
    /**
     * {@link #OnDrawingObjectAddListener}<br>
     * 브러시 객체 추가를 감지하는 리스너를 등록한다.
     * 
     * @param listener 브러시 객체 추가 리스너
     */
    public void setOnDrawingObjectAddListener(OnDrawingObjectAddListener listener) {
        mOnDrawingObjectAddListener = listener;
    }

    /**
     * 브러시 객체가 추가되면 호출되는 class
     */
    public interface OnDrawingObjectAddListener {
        /**
         * 브러시 객체가 추가되면 호출된다.
         * @param x 객체의 x축 좌표
         * @param y 객체의 y축 좌표
         * @param drawing 추가된 객체 이미지
         */
        public void onDrawingObjectAdd(int x, int y, Bitmap drawing);
    }
}
