package com.kiwiple.imageanalysis.database;

import java.io.Serializable;

/**
 * Serializable 가능한 PointF 객체
 */
public class FacePointF implements Serializable {

    private static final long serialVersionUID = -6392700555244249706L;
    
    public float x;
    public float y;
    
    public FacePointF() {
        this.x = 0.f;
        this.y = 0.f;
    }
    
    public FacePointF(float x, float y) {
        this.x = x;
        this.y = y;
    }
}
