
package com.kiwiple.imageframework.drawing.model;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.almeros.android.multitouch.gesturedetectors.MoveGestureDetector;

public class DrawTransparentFreeHand extends DrawObject {
    private static final int DEFAULT_TRANSPARENCY = 0x70ffffff;
    private List<PointF> mPoints = new ArrayList<PointF>();
    private Path mPath;

    @Override
    public void init() {
        super.init();
        mDrawingPaint.setColor(mDrawingPaint.getColor() & DEFAULT_TRANSPARENCY);
        mDrawingPaint.setStyle(Style.STROKE);
    }

    public void addPoint(float x, float y) {
        addPoint(new PointF(x, y));
    }

    public void addPoint(PointF point) {
        mPoints.add(point);
        if(mPath == null) {
            mPath = new Path();
            mPath.moveTo(point.x, point.y);
        } else {
            mPath.lineTo(point.x, point.y);
        }
    }

    @Override
    public void onTouchDown(MotionEvent event) {
        addPoint(event.getX(), event.getY());
    }

    @Override
    public void onMove(float x, float y) {
        addPoint(x, y);
    }

    @Override
    public void onDraw(Canvas canvas, boolean output) {
        if(mPath != null) {
            canvas.drawPath(mPath, mDrawingPaint);
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
        if(mPoints.size() != 0) {
            mBound.set(Float.MAX_VALUE, Float.MAX_VALUE, Float.MIN_VALUE, Float.MIN_VALUE);
            for(PointF point : mPoints) {
                mBound.set(Math.min(mBound.left, point.x), Math.min(mBound.top, point.y),
                           Math.max(mBound.right, point.x), Math.max(mBound.bottom, point.y));
            }
            mBound.left -= mDrawingPaint.getStrokeWidth();
            mBound.top -= mDrawingPaint.getStrokeWidth();
            mBound.right += mDrawingPaint.getStrokeWidth();
            mBound.bottom += mDrawingPaint.getStrokeWidth();
        }
        return mBound;
    }
}
