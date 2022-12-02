
package com.kiwiple.imageframework.drawing.model;

import android.graphics.Canvas;
import android.graphics.Paint.Style;
import android.graphics.RectF;

public class DrawRoundRectangle extends DrawLine {
    private static final float DEFAULT_RADIUS = 10f;
    private RectF mRect = new RectF();

    @Override
    public void init() {
        super.init();
        mDrawingPaint.setStyle(Style.STROKE);
    }

    @Override
    public void onDraw(Canvas canvas, boolean output) {
        if(mStart != null && mEnd != null) {
            mRect.set(Math.min(mStart.x, mEnd.x), Math.min(mStart.y, mEnd.y),
                      Math.max(mStart.x, mEnd.x), Math.max(mStart.y, mEnd.y));
            canvas.drawRoundRect(mRect, DEFAULT_RADIUS, DEFAULT_RADIUS, mDrawingPaint);
        }
    }
}
