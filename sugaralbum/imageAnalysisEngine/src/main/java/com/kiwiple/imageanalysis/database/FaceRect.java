
package com.kiwiple.imageanalysis.database;

import java.io.Serializable;

import android.graphics.Rect;

/**
 * Serializable 가능한 Rect 객체
 */
public class FaceRect implements Serializable {

    private static final long serialVersionUID = -6392700555244249706L;

    public int left;
    public int top;
    public int right;
    public int bottom;

    public FaceRect() {
        this.left = 0;
        this.top = 0;
        this.right = 0;
        this.bottom = 0;
    }

    public FaceRect(Rect f) {
        this.left = f.left;
        this.top = f.top;
        this.right = f.right;
        this.bottom = f.bottom;
    }

    public FaceRect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public int width() {
        return this.right - this.left;
    }

    public int height() {
        return this.bottom - this.top;
    }

}
