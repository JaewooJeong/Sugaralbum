
package com.kiwiple.imageframework.burstshot;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.os.Environment;

import com.kiwiple.imageframework.util.BitmapUtils;
import com.kiwiple.imageframework.util.FileUtils;

//TODO: AsyncTask를 상속 받아 구현한 클래스 였지만, 앱에서 Thread를 분리하는 형태로 변경하면서 일반 클래스로 수정하였음. 코드 정리 필요.
class JpegImgSaverAsyncTask {
    private static Bitmap resizedImage;
    private static Canvas canvas;
    public static final String TAG = "ContShooting";
    private static RectF mTempSrc = new RectF();
    private static RectF mTempDst = new RectF();
    private static Matrix imageMatrix = new Matrix();
    private static final Paint sPaint = new Paint(Paint.FILTER_BITMAP_FLAG);

    /**
     * 
     * animated gif로 encoding할 jpeg 파일을 동일한 해상도로 변환한다. 
     * 중앙 정렬하고 남는 영역은 black으로 채운다.
     * 
     */
    public static String execute(Context context, Point size, String jpgFilePath, int index) {
        int width = size.x;
        int height = size.y;

        // convert yuv to bitmap
        Bitmap original;
        try {
            original = FileUtils.decodingImage(jpgFilePath, width > height ? width : height,
                                               Config.ARGB_8888);
            if(resizedImage == null || canvas == null || resizedImage.getWidth() != width
                    || resizedImage.getHeight() != height) {
                resizedImage = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                canvas = new Canvas(resizedImage);
            }

            // Generate the required transform.
            mTempSrc.set(0, 0, original.getWidth(), original.getHeight());
            mTempDst.set(0, 0, resizedImage.getWidth(), resizedImage.getHeight());

            imageMatrix.reset();
            imageMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.CENTER);

            int rotation;
            try {
                rotation = BitmapUtils.getImageRotation(jpgFilePath);
                // rotate bitmap
                if(rotation != 0) {
                    imageMatrix.postRotate(rotation, resizedImage.getWidth() / 2,
                                           resizedImage.getHeight() / 2);
                }
                if(rotation < 0) {
                    rotation += 360;
                } else if(rotation > 360) {
                    rotation -= 360;
                }
                if(rotation == 90 || rotation == 270) {
                    float widthRatio = resizedImage.getWidth() / (float)original.getWidth();
                    float heightRatio = resizedImage.getHeight() / (float)original.getHeight();
                    if(heightRatio > widthRatio) {
                        imageMatrix.postScale(original.getWidth() / (float)original.getHeight(),
                                              original.getWidth() / (float)original.getHeight(),
                                              resizedImage.getWidth() / 2,
                                              resizedImage.getHeight() / 2);
                    } else {
                        imageMatrix.postScale(original.getHeight() / (float)original.getWidth(),
                                              original.getHeight() / (float)original.getWidth(),
                                              resizedImage.getWidth() / 2,
                                              resizedImage.getHeight() / 2);
                    }
                }
            } catch(IOException e) {
            }
            canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(original, imageMatrix, sPaint);

            // save bitmap to jpg
            String path = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    + File.separator + "gif";
            File file = new File(path);
            if(!file.exists()) {
                file.mkdirs();
            }
            return YuvImgSaverAsyncTask.saveData(resizedImage, path + File.separator + index);
        } catch(IOException e1) {
            return null;
        }
    }
}
