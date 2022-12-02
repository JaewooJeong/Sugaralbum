
package com.larvalabs.svgandroid;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;

/**
 * Describes a vector Picture object, and optionally its bounds.
 */
public class SVG {
    public static final int TYPE_UNKNOWN = -1;
    public static final int TYPE_RECT = 0;
    public static final int TYPE_CIRCLE = 1;
    public static final int TYPE_OVAL = 2;
    public static final int TYPE_POLYGON = 3;
    public static final int TYPE_PATH = 4;

    // svg info
    private int mType = TYPE_UNKNOWN;
    private int mWidth;
    private int mHeight;
    private Matrix mMatrix;

    // for rect
    private float mLeft;
    private float mTop;
    private float mRight;
    private float mBottom;

    // for circle
    private float mCenterX;
    private float mCenterY;
    private float mRadius;

    // for oval
    private RectF mOval;

    // for polygon
    private Path mPolygon;

    // for path
    private Path mPath;

    public int getType() {
        return mType;
    }

    public void setType(int type) {
        mType = type;
    }

    public int getWidth() {
        return mWidth;
    }

    public void setWidth(int width) {
        mWidth = width;
    }

    public int getHeight() {
        return mHeight;
    }

    public void setHeight(int height) {
        mHeight = height;
    }

    public void setTransform(Matrix matrix) {
        mMatrix = matrix;
    }

    public Matrix getTransform() {
        return mMatrix;
    }

    public void setRect(float left, float top, float right, float bottom) {
        mLeft = left;
        mTop = top;
        mRight = right;
        mBottom = bottom;
    }

    public float[] getRect() {
        return new float[] {
                mLeft, mTop, mRight, mBottom
        };
    }

    public void setCircle(float centerX, float centerY, float radius) {
        mCenterX = centerX;
        mCenterY = centerY;
        mRadius = radius;
    }

    public float[] getCircle() {
        return new float[] {
                mCenterX, mCenterY, mRadius
        };
    }

    public void setOval(RectF oval) {
        mOval = oval;
    }

    public RectF getOval() {
        return mOval;
    }

    public void setPolygone(Path polygon) {
        mPolygon = polygon;
    }

    public Path getPolygon() {
        return mPolygon;
    }

    public void setPath(Path path) {
        mPath = path;
    }

    public Path getPath() {
        return mPath;
    }
}
