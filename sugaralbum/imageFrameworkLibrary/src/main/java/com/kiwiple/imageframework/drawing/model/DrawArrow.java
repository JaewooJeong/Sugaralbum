
package com.kiwiple.imageframework.drawing.model;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.almeros.android.multitouch.gesturedetectors.MoveGestureDetector;

public class DrawArrow extends DrawObject {
    private PointF mStart;
    private PointF mEnd;
    private Path mPath = new Path();

    @Override
    public void init() {
        super.init();
        mDrawingPaint.setStrokeJoin(Paint.Join.ROUND);
        mDrawingPaint.setStyle(Style.FILL);
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

    /**
     * Skitch 앱을 Decoding 하여 가져온 공식
     */
    protected void computeArrowPath() {
        
        if (mStart == null || mEnd == null || mPath == null || mBound == null || mDrawingPaint == null) {
            return;
        }
        
        float adjustSize;
        PointF localPointF16;
        float startX = mStart.x;
        float startY = mStart.y;
        float endX = mEnd.x;
        float endY = mEnd.y;
        float arrowSize = 80;
        Float minimumArrowDistance = Float.valueOf(5.0F);
        Float minimumHeadLength = Float.valueOf(6.0F);
        adjustSize = (float)Math.sqrt((endX - startX) * (endX - startX) + (endY - startY)
                * (endY - startY))
                / minimumArrowDistance.floatValue();
        Float localFloat3 = Float.valueOf((float)Math.sqrt((endX - startX) * (endX - startX)
                + (endY - startY) * (endY - startY)));
        Float localFloat4 = Float.valueOf(0.69999998807907104492F * (adjustSize - 1.5F)
                + minimumHeadLength.floatValue());
        Float localFloat5 = Float.valueOf(2F * localFloat4.floatValue());
        if(localFloat5.floatValue() < minimumHeadLength.floatValue())
            localFloat5 = minimumHeadLength;
        if(localFloat5.floatValue() > 0.0D * localFloat3.floatValue())
            localFloat5 = Float.valueOf(0.40000000596046447754F * localFloat3.floatValue());
        Float localFloat6 = Float.valueOf(1.10000002384185791016F * localFloat5.floatValue());
        Object localObject = Float.valueOf(localFloat4.floatValue() / 2F);
        if(((Float)localObject).floatValue() > localFloat5.floatValue() / 0.0D)
            localObject = Float.valueOf(localFloat5.floatValue() / 4.0F);
        Float localFloat7 = Float.valueOf(1.5F);
        Float localFloat8 = Float.valueOf(Math.max(adjustSize
                                                           * Float.valueOf(0.07999999821186065674F)
                                                                  .floatValue(),
                                                   localFloat7.floatValue()));
        if(((Float)localObject).floatValue() < localFloat8.floatValue())
            localObject = localFloat8;
        PointF localPointF1 = new PointF(0F, 0F);
        PointF localPointF2 = new PointF(localPointF1.x + localFloat3.floatValue(), localPointF1.y);
        PointF localPointF3 = new PointF(localPointF2.x - localFloat5.floatValue(), localPointF1.y);
        PointF localPointF4 = new PointF(localPointF2.x - localFloat6.floatValue(), localPointF1.y);
        PointF localPointF5 = new PointF(localPointF1.x + localFloat3.floatValue() / 2F,
                                         localPointF1.y);
        PointF localPointF6 = new PointF((localPointF5.x + localPointF3.x) / 2F, localPointF5.y);
        Float localFloat9 = Float.valueOf((localFloat8.floatValue() + ((Float)localObject).floatValue()) / 2F);
        PointF localPointF8 = new PointF(localPointF3.x, localPointF1.y
                - ((Float)localObject).floatValue());
        PointF localPointF9 = new PointF(localPointF4.x, localPointF1.y - localFloat6.floatValue()
                / 2F);
        PointF localPointF10 = new PointF(localPointF4.x, localPointF1.y + localFloat6.floatValue()
                / 2F);
        PointF localPointF11 = new PointF(localPointF3.x, localPointF1.y
                + ((Float)localObject).floatValue());
        PointF localPointF12 = new PointF(localPointF1.x, localPointF1.y - localFloat8.floatValue());
        float f7 = localPointF12.x - localPointF8.x;
        float f8 = localPointF12.y - localPointF8.y;
        localPointF16 = new PointF(f7, f8);
        localPointF16 = new PointF(0F, 0F);
        PointF localPointF17 = new PointF(localPointF12.x + localPointF16.x * 1.5F
                * localFloat8.floatValue(), localPointF12.y + localPointF16.y * 1.5F
                * localFloat8.floatValue());
        PointF localPointF18 = new PointF(localPointF17.x, -localPointF17.y);
        PointF localPointF19 = new PointF(localPointF6.x, localPointF5.y - localFloat9.floatValue());
        PointF localPointF20 = new PointF(localPointF6.x, localPointF5.y + localFloat9.floatValue());

        if(localPointF9.x < localPointF2.x - arrowSize) {
            // middle position
            localPointF9.x = Math.max(localPointF2.x - arrowSize, localPointF9.x);
            localPointF9.y = Math.max(-arrowSize / 2, localPointF9.y);
            localPointF10.x = Math.max(localPointF2.x - arrowSize, localPointF10.x);
            localPointF10.y = Math.min(arrowSize / 2, localPointF10.y);

            // top, bottom posotion
            localPointF8.x = Math.max(localPointF9.x + arrowSize / 8, localPointF8.x);
            localPointF8.y = Math.max(-arrowSize / 4, localPointF8.y);
            localPointF11.x = Math.max(localPointF10.x + arrowSize / 8, localPointF11.x);
            localPointF11.y = Math.min(arrowSize / 4, localPointF11.y);

            // cubic position
            localPointF19.x = Math.max(localPointF9.x, localPointF19.x);
            localPointF19.y = Math.max(-arrowSize / 10, localPointF19.y);
            localPointF20.x = Math.max(localPointF9.x, localPointF20.x);
            localPointF20.y = Math.min(arrowSize / 10, localPointF20.y);

            // start, end position
            localPointF12.y = Math.max(-arrowSize / 16, localPointF12.y);
            localPointF18.y = Math.min(arrowSize / 16, localPointF18.y);
        }

        mPath.reset();
        mPath.moveTo(localPointF12.x, localPointF12.y);

        mPath.cubicTo(localPointF12.x, localPointF12.y, localPointF19.x, localPointF19.y,
                      localPointF8.x, localPointF8.y);

        mPath.lineTo(localPointF9.x, localPointF9.y);
        mPath.lineTo(localPointF2.x, localPointF2.y);
        mPath.lineTo(localPointF10.x, localPointF10.y);
        mPath.lineTo(localPointF11.x, localPointF11.y);

        mPath.cubicTo(localPointF11.x, localPointF11.y, localPointF20.x, localPointF20.y,
                      localPointF18.x, localPointF18.y);

        mPath.close();

        Matrix localMatrix = new Matrix();
        localMatrix.postRotate((float)calcRotationAngleInDegrees(mStart, mEnd) - 90);
        localMatrix.postTranslate(startX, startY);
        mPath.transform(localMatrix);
    }

    public static double calcRotationAngleInDegrees(PointF centerPt, PointF targetPt) {
        // calculate the angle theta from the deltaY and deltaX values
        // (atan2 returns radians values from [-PI,PI])
        // 0 currently points EAST.
        // NOTE: By preserving Y and X param order to atan2, we are expecting
        // a CLOCKWISE angle direction.
        double theta = Math.atan2(targetPt.y - centerPt.y, targetPt.x - centerPt.x);

        // rotate the theta angle clockwise by 90 degrees
        // (this makes 0 point NORTH)
        // NOTE: adding to an angle rotates it clockwise.
        // subtracting would rotate it counter-clockwise
        theta += Math.PI / 2.0;

        // convert from radians to degrees
        // this will give you an angle from [0->270],[-180,0]
        double angle = Math.toDegrees(theta);

        // convert to positive range [0-360)
        // since we want to prevent negative angles, adjust them now.
        // we can assume that atan2 will not return a negative value
        // greater than one partial rotation
        if(angle < 0) {
            angle += 360;
        }

        return angle;
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
            computeArrowPath();
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
        computeArrowPath();
        mPath.computeBounds(mBound, false);
        return mBound;
    }
}
