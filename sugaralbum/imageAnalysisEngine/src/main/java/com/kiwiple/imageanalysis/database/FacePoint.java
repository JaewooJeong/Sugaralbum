package com.kiwiple.imageanalysis.database;

import java.io.Serializable;

/**
 * Serializable 가능한 Point 객체
 */
public class FacePoint implements Serializable {

    private static final long serialVersionUID = -6392700555244249706L;
    
    public int x;
    public int y;
    
    public FacePoint() {
        this.x = 0;
        this.y = 0;
    }
    
    public FacePoint(int x, int y) {
        this.x = x;
        this.y = y;
    }
}
