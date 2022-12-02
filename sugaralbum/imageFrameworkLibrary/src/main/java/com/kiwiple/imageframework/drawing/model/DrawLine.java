
package com.kiwiple.imageframework.drawing.model;

import android.graphics.Canvas;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.view.MotionEvent;

import com.almeros.android.multitouch.gesturedetectors.MoveGestureDetector;

public class DrawLine extends DrawObject {
    protected PointF mStart;
    protected PointF mEnd;

    @Override
    public void init() {
        super.init();
        mDrawingPaint.setStyle(Style.FILL_AND_STROKE);
    }

    public void updateStartPoint(PointF point) {
        updateStartPoint(point.x, point.y);
    }

    public void updateStartPoint(float x, float y) {
        if(mStart == null) {
            mStart = new PointF(x, y);
        } else {
            mStart.set(x, y);
        }
    }

    public void updateEndPoint(PointF point) {
        updateEndPoint(point.x, point.y);
    }

    public void updateEndPoint(float x, float y) {
        if(mEnd == null) {
            mEnd = new PointF(x, y);
        } else {
            mEnd.set(x, y);
        }
    }

    @Override
    public void onTouchDown(MotionEvent event) {
        updateStartPoint(event.getX(), event.getY());
    }

    @Override
    public void onMove(float x, float y) {
        updateEndPoint(x, y);
    }

    @Override
    public void onDraw(Canvas canvas, boolean output) {
        if(mStart != null && mEnd != null) {
            canvas.drawLine(mStart.x, mStart.y, mEnd.x, mEnd.y, mDrawingPaint);
        }
    }

    @Override
    public boolean isInnterPoint(float x, float y) {
        RectF rect = getBoundRect();
        if(rect.contains(x, y)) {
            return true;
        }
        return false;
    }

    @Override
    public RectF getBoundRect() {
        if(mStart != null && mEnd != null) {
            mBound.set(Math.min(mStart.x, mEnd.x), Math.min(mStart.y, mEnd.y),
                       Math.max(mStart.x, mEnd.x), Math.max(mStart.y, mEnd.y));
            mBound.left -= mDrawingPaint.getStrokeWidth();
            mBound.top -= mDrawingPaint.getStrokeWidth();
            mBound.right += mDrawingPaint.getStrokeWidth();
            mBound.bottom += mDrawingPaint.getStrokeWidth();
        }
        return mBound;
    }
}
