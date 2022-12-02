
package com.kiwiple.imageframework.burstshot;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.kiwiple.imageframework.filter.NativeImageFilter;

//TODO: AsyncTask를 상속 받아 구현한 클래스 였지만, 앱에서 Thread를 분리하는 형태로 변경하면서 일반 클래스로 수정하였음. 코드 정리 필요.
class YuvImgSaverAsyncTask {
    public static final String TAG = "ContShooting";

    private Point mSize;

    // TODO: gc호출을 최소화 하기 위해 메모리 재사용. release 해주는 코드 필요.
    private byte[] mYuvData;
    private int[] mRgbData;
    private Bitmap mBitmap1;
    private Bitmap mBitmap2;

    private int mRotation;
    private boolean mFlip;

    public YuvImgSaverAsyncTask(Point size, int rotation, boolean flip) {
        mSize = size;
        mRotation = rotation;
        mFlip = flip;
    }

    /**
     * 
     * yuv 파일을 jpeg로 변환하여 저장한다.
     * 
     * @param yuvFilePath jpeg로 변환하여 저장할 yuv 파일
     * @return 저장된 jpeg 파일 path
     */
    public String execute(String yuvFilePath) {
        if(mYuvData == null) {
            mYuvData = new byte[(mSize.x * mSize.y * 4)];
        }
        if(mRgbData == null) {
            mRgbData = new int[(mSize.x * mSize.y)];
        }
        int width = mSize.x;
        int height = mSize.y;

        // read yuv file
        readData(yuvFilePath);

        // convert yuv to bitmap
        NativeImageFilter.YUVtoRBG(mRgbData, mYuvData, width, height);
        if(mBitmap1 == null || mBitmap1.getWidth() != width || mBitmap1.getHeight() != height) {
            mBitmap1 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        mBitmap1.setPixels(mRgbData, 0, width, 0, 0, width, height);

        // rotate bitmap
        if(mRotation == 90 || mRotation == 270) {
            int tmp = width;
            width = height;
            height = tmp;
        }
        if(mBitmap2 == null || mBitmap2.getWidth() != width || mBitmap2.getHeight() != height) {
            mBitmap2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        rotateBitmap(mBitmap1, mBitmap2, mRotation, mFlip);

        // save bitmap to jpg
        return saveData(mBitmap2, yuvFilePath);
    }

    private static Bitmap rotateBitmap(Bitmap orig, Bitmap target, int rotate, boolean flip) {
        Matrix matrix = new Matrix();

        if(rotate == 0 || rotate == 180) {
            matrix.postTranslate(0, 0);
        } else if(rotate == 90) {
            matrix.postTranslate((orig.getWidth() - orig.getHeight()) / 2,
                                 -(orig.getHeight() - orig.getWidth()) / 2);
        } else {
            matrix.postTranslate(-(orig.getWidth() - orig.getHeight()) / 2,
                                 (orig.getHeight() - orig.getWidth()) / 2);
        }
        matrix.postRotate(rotate, orig.getWidth() / 2, orig.getHeight() / 2);

        if(flip) {
            matrix.preTranslate(orig.getWidth(), 0);
            matrix.preScale(-1.0f, 1.0f);
        }

        Canvas c = new Canvas(target);
        Paint p = new Paint();
        p.setFilterBitmap(true);

        c.drawBitmap(orig, matrix, p);
        return target;
    }

    private void readData(String filePath) {
        BufferedInputStream bis = null;
        try {
            FileInputStream fis = new FileInputStream(filePath);
            bis = new BufferedInputStream(fis);
            // if (mYuvData == null) mYuvData = new byte[fis.available()];
            while(bis.read(mYuvData) != -1) {
            }
            fis.close();
            Log.d(TAG, "Yuv Image Read : " + filePath);
        } catch(IOException e) {
            Log.e(TAG, "IOException in readData");
        } finally {
            if(bis != null) {
                try {
                    bis.close();
                } catch(IOException e1) {
                    // do nothing
                }
            }
        }

        // configuration 변경 등으로 인하여 activity가 재시작 될 때 다시 읽어야 한다.
        // delete the yuv image file after read data from it
        // try {
        // File file = new File(filePath);
        // file.delete();
        // } catch(NullPointerException e) {
        // Log.e(TAG, "NullPointerException in deleteFile");
        // }
    }

    public static String saveData(final Bitmap image, String filePath) {
        String jpgPath = filePath + ".jpg";

        try {
            BufferedOutputStream bos = null;
            try {
                FileOutputStream fos = new FileOutputStream(jpgPath, false);
                bos = new BufferedOutputStream(fos);
                image.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                fos.flush();
            } finally {
                if(bos != null) {
                    try {
                        bos.close();
                    } catch(IOException e) {
                    }
                }
            }
        } catch(IOException e) {
            jpgPath = null;
        }
        return jpgPath;
    }
}
