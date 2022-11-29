
package com.kiwiple.imageframework.drawing2.view;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
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

    private DrawObject mSelectedObject;
    private DrawObject mCurrentObject;
    private List<DrawObject> mObjects = new ArrayList<DrawObject>();

    private int mDrawingType = -1;
    private Bitmap mStampImage = null;
    private int mColor = DrawObject.DEFAULT_COLOR;
    private float mStrokeWidth = DrawObject.DEFAULT_STORKE_WIDTH;

    private final static int HANDLER_MESSAGE_INVALIDATE = 0;
    private final static int HANDLER_MESSAGE_SELECT_FRAME = 1;
    private final static int HANDLER_MESSAGE_DESELECT_FRAME = 2;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case HANDLER_MESSAGE_INVALIDATE:
                    mHandler.removeMessages(HANDLER_MESSAGE_INVALIDATE);
                    invalidate();
                    break;
                case HANDLER_MESSAGE_SELECT_FRAME:
                    selectSticker((DrawObject)msg.obj);
                    break;
                case HANDLER_MESSAGE_DESELECT_FRAME:
                    deselectSticker();
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
        if(Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, new Paint(Paint.FILTER_BITMAP_FLAG));
        }

        // Setup Gesture Detectors
        mGestureDetector = new GestureDetector(context.getApplicationContext(),
                                               new SimpleGestureListener());
        mMoveDetector = new MoveGestureDetector(context.getApplicationContext(), new MoveListener());
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isEnabled()) {
            return false;
        }
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mCurrentObject = createNewDrawingObject();
                if(mCurrentObject != null) {
                    mCurrentObject.onTouchDown(event);

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
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mCurrentObject = null;
                break;
        }

        try {
            mGestureDetector.onTouchEvent(event);
            if(mCurrentObject != null) {
                mMoveDetector.onTouchEvent(event);
            }
        } catch(Exception e) {
            // do nothing
        }
        if(mCurrentObject != null) {
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
        return mCurrentObject != null;
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            if(mCurrentObject != null) {
                mCurrentObject.onMove(detector.getCurrMotionEvent().getX(), detector.getCurrMotionEvent().getY());
            }
            return true;
        }
    }

    private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // 스티커 선택
            DrawObject drawObject = findInnterPoint(e.getX(), e.getY());

            // 스티커 삭제
            if(mSelectedObject != null && mSelectedObject.isDeleteToolboxSelected()) {
                removeSticker(mObjects.indexOf(mSelectedObject));
                mSelectedObject = null;
            } else if(drawObject != null && mSelectedObject != drawObject) {
                Message selectFrameMessage = new Message();
                selectFrameMessage.what = HANDLER_MESSAGE_SELECT_FRAME;
                selectFrameMessage.obj = drawObject;
                mHandler.sendMessage(selectFrameMessage);
            }
            // 스티커 선택 해제
            else {
                if(mSelectedObject != null && (drawObject == null || mSelectedObject == drawObject)) {
                    mHandler.sendEmptyMessage(HANDLER_MESSAGE_DESELECT_FRAME);
                }
            }
            return true;
        }
    }

    private DrawObject findInnterPoint(float x, float y) {
        for(int i = mObjects.size() - 1; i >= 0; i--) {
            if(mObjects.get(i).isInnterPoint(x, y)) {
                return mObjects.get(i);
            }
        }
        return null;
    }

    public void removeSticker(int index) {
        if(index > mObjects.size() - 1) {
            return;
        }
        DrawObject sticker = mObjects.get(index);
        mObjects.remove(sticker);
        if(mSelectedObject == sticker) {
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_DESELECT_FRAME);
        } else {
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    private void selectSticker(DrawObject selecteView) {
        if(selecteView == null) {
            return;
        }
        mSelectedObject = selecteView;
        for(DrawObject frameView : mObjects) {
            if(frameView.isSelected()) {
                frameView.setSelected(false);
            }
        }
        mSelectedObject.setSelected(true);
        mObjects.remove(mSelectedObject);
        mObjects.add(mSelectedObject);
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    private void deselectSticker() {
        for(DrawObject imageView : mObjects) {
            imageView.setSelected(false);
        }
        mSelectedObject = null;
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.TRANSPARENT, Mode.CLEAR);
        if(!mObjects.isEmpty() && mCurrentIndex != -1) {
            for(int i = 0; i <= mCurrentIndex; i++) {
                mObjects.get(i).onDraw(canvas, true);
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
                    newObject = new DrawStamp(getContext());
                    ((DrawStamp)newObject).setStamp(mStampImage);
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

    public void setDrawingType(int type) {
        mDrawingType = type;
    }

    public int getDrawingType() {
        return mDrawingType;
    }

    public void setStampImage(Bitmap stamp) {
        mStampImage = stamp;
    }

    public Bitmap getStampImage() {
        return mStampImage;
    }

    public void setStokeWidth(float width) {
        mStrokeWidth = width;
        if(mCurrentObject != null) {
            mCurrentObject.setDefaultStrokeWidth(width);
        }
    }

    public void setColor(int color) {
        mColor = color;
        if(mCurrentObject != null) {
            mCurrentObject.setDefaultColor(color);
        }
    }

    public void clear() {
        mObjects.clear();
        mCurrentIndex = -1;
        if(mCurrentObject != null) {
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
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
}
