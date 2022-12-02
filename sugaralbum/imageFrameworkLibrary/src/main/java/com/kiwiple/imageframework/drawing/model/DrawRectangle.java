
package com.kiwiple.imageframework.drawing.model;

import android.graphics.Canvas;
import android.graphics.Paint.Style;

public class DrawRectangle extends DrawLine {
    @Override
    public void init() {
        super.init();
        mDrawingPaint.setStyle(Style.STROKE);
    }

    @Override
    public void onDraw(Canvas canvas, boolean output) {
        if(mStart != null && mEnd != null) {
            canvas.drawRect(Math.min(mStart.x, mEnd.x), Math.min(mStart.y, mEnd.y),
                            Math.max(mStart.x, mEnd.x), Math.max(mStart.y, mEnd.y), mDrawingPaint);
        }
    }
}
