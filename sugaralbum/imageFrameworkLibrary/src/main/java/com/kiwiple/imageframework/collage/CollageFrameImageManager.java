
package com.kiwiple.imageframework.collage;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;

/**
 * 
 * 콜라주 프레임을 그리기 위한 Canvas와 Bitmap을 관리하는 클래스로, 모든 CollageFrameView에서 공동으로 사용한다. 
 *
 */
public class CollageFrameImageManager {
    private static CollageFrameImageManager sImageManager;
    private Bitmap mImage;
    private Canvas mCanvas;

    public static synchronized CollageFrameImageManager getInstance() {
        if(sImageManager == null) {
            sImageManager = new CollageFrameImageManager();
        }
        return sImageManager;
    }

    private synchronized void makeSubImageIfNeeded(int width, int height) {
        if(mCanvas == null || mImage == null || mImage.isRecycled() || mImage.getWidth() != width
                || mImage.getHeight() != height) {
            mImage = Bitmap.createBitmap(width, height, Config.ARGB_8888);
            mCanvas = new Canvas(mImage);
        }
    }

    public synchronized Bitmap getImage(int width, int height) {
        makeSubImageIfNeeded(width, height);
        return mImage;
    }

    public synchronized Canvas getCanvas(int width, int height) {
        makeSubImageIfNeeded(width, height);
        return mCanvas;
    }

    public synchronized void release() {
        if(mImage != null) {
            mImage.recycle();
        }
        mCanvas = null;
    }
}
