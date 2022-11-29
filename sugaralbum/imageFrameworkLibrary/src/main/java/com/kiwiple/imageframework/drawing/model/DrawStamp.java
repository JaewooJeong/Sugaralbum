
package com.kiwiple.imageframework.drawing.model;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.view.MotionEvent;

import com.almeros.android.multitouch.gesturedetectors.MoveGestureDetector;
import com.kiwiple.imageframework.util.FileUtils;

public class DrawStamp extends DrawObject {
    public static final int STAMP_TYPE_X = 0;
    public static final int STAMP_TYPE_CHECK = 1;
    public static final int STAMP_TYPE_HEART = 2;
    public static final int STAMP_TYPE_QUESTION = 3;
    public static final int STAMP_TYPE_EXCLAMATION = 4;
    /**
     * 객체의 모양에 대한 목록. <br>
     * Use with {@link com.kiwiple.imageframework.drawing.view.DrawingView#mStampType}<br><br>
     * STAMP_TYPE[0] : X<br>
     * STAMP_TYPE[1] : Check<br>
     * STAMP_TYPE[2] : Heart<br>
     * STAMP_TYPE[3] : Question<br>
     * STAMP_TYPE[4] : Exclamation<br>
     */
    public static final String[] STAMP_TYPE = new String[] {
            "X", "Check", "Heart", "Question", "Exclamation"
    };

    private static Bitmap[] sStamp = new Bitmap[5];
    private static Bitmap[] sStampArrow = new Bitmap[5];

    private PointF mStart;
    private PointF mEnd;
    private float mRotation = 0;
    private Bitmap mStamp;
    private Bitmap mArrow;

    private float mDensity;

    public DrawStamp(Context context) {
        super();
        mDensity = context.getResources().getDisplayMetrics().density;
    }

    @Override
    public void init() {
        mDrawingPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    }

    public void setType(int type, Context context) {
        if(sStamp[type] == null) {
            switch(type) {
                case STAMP_TYPE_X:
                    sStamp[type] = BitmapFactory.decodeResource(context.getResources(),
                                                                FileUtils.getBitmapResourceId(context,
                                                                                              "x_mark"));
                    sStampArrow[type] = BitmapFactory.decodeResource(context.getResources(),
                                                                     FileUtils.getBitmapResourceId(context,
                                                                                                   "x_arrow"));
                    break;
                case STAMP_TYPE_CHECK:
                    sStamp[type] = BitmapFactory.decodeResource(context.getResources(),
                                                                FileUtils.getBitmapResourceId(context,
                                                                                              "check_mark"));
                    sStampArrow[type] = BitmapFactory.decodeResource(context.getResources(),
                                                                     FileUtils.getBitmapResourceId(context,
                                                                                                   "check_arrow"));
                    break;
                case STAMP_TYPE_HEART:
                    sStamp[type] = BitmapFactory.decodeResource(context.getResources(),
                                                                FileUtils.getBitmapResourceId(context,
                                                                                              "heart_mark"));
                    sStampArrow[type] = BitmapFactory.decodeResource(context.getResources(),
                                                                     FileUtils.getBitmapResourceId(context,
                                                                                                   "heart_arrow"));
                    break;
                case STAMP_TYPE_QUESTION:
                    sStamp[type] = BitmapFactory.decodeResource(context.getResources(),
                                                                FileUtils.getBitmapResourceId(context,
                                                                                              "question_mark"));
                    sStampArrow[type] = BitmapFactory.decodeResource(context.getResources(),
                                                                     FileUtils.getBitmapResourceId(context,
                                                                                                   "question_arrow"));
                    break;
                case STAMP_TYPE_EXCLAMATION:
                    sStamp[type] = BitmapFactory.decodeResource(context.getResources(),
                                                                FileUtils.getBitmapResourceId(context,
                                                                                              "exclamation_mark"));
                    sStampArrow[type] = BitmapFactory.decodeResource(context.getResources(),
                                                                     FileUtils.getBitmapResourceId(context,
                                                                                                   "exclamation_arrow"));
                    break;
            }
        }
        mStamp = sStamp[type];
        mArrow = sStampArrow[type];
    }

    public void setStamp(Bitmap stamp) {
        mStamp = stamp;
    }

    public void setArrow(Bitmap arrow) {
        mArrow = arrow;
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
            if(Math.abs(x - mStart.x) + Math.abs(y - mStart.y) > 10) {
                mRotation = -(float)Math.toDegrees(3.141592653589793 - (Math.atan2(y - mStart.y, x
                        - mStart.x)));
                mEnd = new PointF(x, y);
            }
        } else {
            mRotation = -(float)Math.toDegrees(3.141592653589793 - (Math.atan2(y - mStart.y, x
                    - mStart.x)));
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
        if(mStart != null && mStamp != null) {
            canvas.drawBitmap(mStamp, mStart.x - mStamp.getWidth() / 2.f,
                              mStart.y - mStamp.getHeight() / 2.f, mDrawingPaint);
            // 스탭프를 찍고 이동이 없을 경우 화살표를 그리지 않는다.
            if(mEnd != null) {
                canvas.save();
                canvas.rotate(mRotation, mStart.x, mStart.y);
                canvas.drawBitmap(mArrow, mStart.x - mStamp.getWidth() / 2.f - mArrow.getWidth()
                        + 25f, mStart.y - mStamp.getHeight() / 2.f, mDrawingPaint);
                canvas.restore();
            }
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
        if(mStart != null) {
            float stampSize = mStamp.getWidth() / 2f + mArrow.getWidth();
            mBound.set(mStart.x, mStart.y, mStart.x, mStart.y);
            mBound.left -= stampSize;
            mBound.top -= stampSize;
            mBound.right += stampSize;
            mBound.bottom += stampSize;
        }
        return mBound;
    }
}
