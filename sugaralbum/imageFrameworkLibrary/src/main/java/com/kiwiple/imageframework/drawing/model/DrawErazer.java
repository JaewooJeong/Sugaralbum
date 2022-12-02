
package com.kiwiple.imageframework.drawing.model;

import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Join;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;

public class DrawErazer extends DrawFreeHand {
    @Override
    public void init() {
        super.init();
        mDrawingPaint.reset();
        mDrawingPaint.setStyle(Style.STROKE);
        mDrawingPaint.setPathEffect(new CornerPathEffect(10));
        mDrawingPaint.setStrokeCap(Cap.ROUND);
        mDrawingPaint.setStrokeJoin(Join.ROUND);
        mDrawingPaint.setColor(Color.TRANSPARENT);
        mDrawingPaint.setAlpha(0x00);
        mDrawingPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
    }

    @Override
    public void setDefaultColor(int color) {
    }
}
