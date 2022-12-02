
package com.kiwiple.imageframework.drawing.model;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.almeros.android.multitouch.gesturedetectors.MoveGestureDetector;

public abstract class DrawObject {
    protected RectF mBound = new RectF();
    protected Paint mDrawingPaint;
    public static final int DEFAULT_COLOR = Color.BLACK;
    public static final float DEFAULT_STORKE_WIDTH = 10f;
    public static final int DRAWING_TYPE_LINE = 0;
    public static final int DRAWING_TYPE_RECTANGLE = 1;
    public static final int DRAWING_TYPE_ROUND_RECT = 2;
    public static final int DRAWING_TYPE_OVAL = 3;
    public static final int DRAWING_TYPE_FREEHAND = 4;
    public static final int DRAWING_TYPE_ALPHA_FREEHAND = 5;
    public static final int DRAWING_TYPE_ARROW = 6;
    public static final int DRAWING_TYPE_STAMP = 7;
    public static final int DRAWING_TYPE_ERAZER = 8;
    /**
     * 객체의 모양에 대한 목록. <br>
     * Use with {@link com.kiwiple.imageframework.drawing.view.DrawingView#mDrawingType}<br><br>
     * DRAWING_TYPE[0] : Line<br>
     * DRAWING_TYPE[1] : Rectangle<br>
     * DRAWING_TYPE[2] : Rounded Rectangle<br>
     * DRAWING_TYPE[3] : Oval<br>
     * DRAWING_TYPE[4] : Free hand line<br>
     * DRAWING_TYPE[5] : Free hand line(with alpha)<br>
     * DRAWING_TYPE[6] : Arrow<br>
     * DRAWING_TYPE[7] : Stamp<br>
     * DRAWING_TYPE[8] : Erazer<br>
     */
    public static final String[] DRAWING_TYPE = new String[] {
            "Line", "Rectangle", "Round rect", "Oval", "Freehand", "Freehand(alpha)", "Arrow",
            "Stamp", "Erazer"
    };

    private boolean mIsSelected = false;
    private boolean mDeleteToolBoxSelection;

    private Bitmap mDelete;
    private Bitmap mDeleteSel;

    public void init() {
        mDrawingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDrawingPaint.setColor(DEFAULT_COLOR);
        mDrawingPaint.setStrokeWidth(DEFAULT_STORKE_WIDTH);
    }

    public void setDefaultStrokeWidth(float width) {
        mDrawingPaint.setStrokeWidth(width);
    }

    public void setDefaultColor(int color) {
        mDrawingPaint.setColor(color);
    }

    public abstract void onTouchDown(MotionEvent event);

    public abstract void onMove(float x, float y);

    public abstract void onDraw(Canvas canvas, boolean output);

    public abstract boolean isInnterPoint(float x, float y);
    public abstract RectF getBoundRect();

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean isSelected) {
        mIsSelected = isSelected;
    }

    public void setDeleteToolboxSelection(boolean selected) {
        mDeleteToolBoxSelection = selected;
    }

    public boolean isDeleteToolboxSelected() {
        return mDeleteToolBoxSelection;
    }
}
