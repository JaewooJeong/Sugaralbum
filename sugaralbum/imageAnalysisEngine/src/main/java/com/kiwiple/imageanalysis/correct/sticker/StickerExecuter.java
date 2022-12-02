
package com.kiwiple.imageanalysis.correct.sticker;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.kiwiple.imageanalysis.correct.ImageCorrectStickerData;
import com.kiwiple.imageanalysis.correct.collage.CollageCorrectCondition;
import com.kiwiple.imageanalysis.database.FacePointF;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.utils.BitmapUtils;
import com.kiwiple.imageanalysis.utils.SmartLog;
import com.kiwiple.imageframework.sticker.StickerView;
import com.kiwiple.imageframework.util.FileUtils;

/**
 * 스티커를 적용하기 위한 클래스
 */
public class StickerExecuter {

    private static final String TAG = StickerExecuter.class.getSimpleName();

    private static StickerExecuter sInstance;
    
    private Context mApplicationContext;

    public static StickerExecuter getInstance(Context applicationContext) {
        if (sInstance == null) {
            sInstance = new StickerExecuter(applicationContext);
        }
        return sInstance;
    }
    
    /**
     * 생성자
     * 
     * @param applicationContext ApplicationContext
     */
    private StickerExecuter(Context applicationContext) {
        mApplicationContext = applicationContext;
    }

    /**
     * 스티커를 적용하여 이미지를 반환한다. <br>
     * 이미지 데이터에 스티커 정보가 들어있을 경우 적용하여 반환한다. <br>
     * 없을 경우 null이나 이미지 데이터 원본을 반환한다.
     * 
     * @param imageData 스티커를 적용할 이미지 (스티커 정보가 반드시 포함되어있어야함)
     * @param targetSize 적용할 이미지의 크기
     * @return Bitmap 스티커가 적용된 비트맵
     */
    public synchronized Bitmap getStickerImageWithImageData(ImageData imageData, int targetSize)
            throws IOException {
        // 데이터가 없거나 path가 없다면
        if(imageData == null || imageData.path == null) {
            return null;
        }

        // 사이즈가 입력되어있지 않다면 기본 사이즈를 정하자
        if(targetSize < 1) {
            targetSize = CollageCorrectCondition.COLLAGE_DEFAULT_IMAGE_SIZE;
        }

        // path를 바탕으로 size에 맞춰 대상이 될 이미지를 디코딩하고 회전시킨다.
        Bitmap.Config config = Bitmap.Config.ARGB_8888;

        Bitmap imageBitmap = FileUtils.decodingImage(imageData.path, targetSize, config);

        // 회전 정도에 따라 이미지를 회전시킨다.
        if("90".equals(imageData.orientation) || "180".equals(imageData.orientation)
                || "270".equals(imageData.orientation)) {
            imageBitmap = BitmapUtils.rotateBitmap(imageBitmap,
                                                   Integer.parseInt(imageData.orientation), config);
        }

        if(imageBitmap == null) {
            SmartLog.e(TAG, "image Decoding Fail");
            return null;
        }

        if(imageData.imageCorrectData == null
                || imageData.imageCorrectData.stickerCorrectDataArray == null) {
            return imageBitmap;
        }

        // StickerView를 사이즈에 맞춰서 생성
        StickerView stickerView = new StickerView(mApplicationContext, imageBitmap.getWidth(),
                                                  imageBitmap.getHeight());
        // selection box color, width 변경
        stickerView.setStickerSelectionColorWithWidth(Color.WHITE, 5);
        stickerView.setTextStickerLimitLength(50);
        stickerView.setTextInputBorderColorWithWidth(Color.WHITE, 5);
        stickerView.setStickerScale(0.001f, 10.f);
        stickerView.setStickerDefaultScale(1.f);

        // 스티커 이미지를 가져오자
        for(int i = 0; i < imageData.imageCorrectData.stickerCorrectDataArray.size(); i++) {
            ImageCorrectStickerData stickerData = imageData.imageCorrectData.stickerCorrectDataArray.get(i);

            int stickerImageWidth = stickerData.imageWidth;
            // if("90".equals(imageData.orientation) || "270".equals(imageData.orientation)) {
            // stickerImageWidth = stickerData.imageHeight;
            // }

            if(stickerData.isTextSticker()) {
                String text = stickerData.text;

                int index = stickerView.addTextStickerRestore(text);
                // 보정데이터에 맞게 적용
                // 스티커 추천 보정시의 이미지 크기와 뷰에 뿌릴 때의 이미지 크기가 다르므로 비율을 맞춰준다.
                float scaleRatio = (float)imageBitmap.getWidth() / stickerImageWidth;

                stickerView.setFrameImageBaseRotation(index, stickerData.stickerRotate);
                stickerView.setFrameImageBaseScale(index, stickerData.stickerScale * scaleRatio);
                FacePointF translate = stickerData.getStickerCoordinate(scaleRatio);
                stickerView.setFrameImageBaseTranslate(index, translate.x, translate.y);
                stickerView.setTextStickerTextColor(index, stickerData.fontColor);
                stickerView.setTextStickerTextBorderColor(index, stickerData.textBorderColor);
                stickerView.setTextStickerTextWidth(index, stickerData.textWidth);
                stickerView.setTextStickerTextBorderWidth(index, stickerData.textBorderWidth);
                stickerView.setTextStickerStyle(index, stickerData.getTextStyle());
                String path = stickerData.typeFaceFilePath;
                if(!TextUtils.isEmpty(path)) {
                    Typeface typeFace = Typeface.createFromFile(path);
                    stickerView.setTextStickerFont(index, typeFace);
                }
            } else {
                String[] splitName = stickerData.stickerFileName.split("\\.");
                Bitmap stickerBitmap = null;
                if(splitName.length == 1) {
                    int stickerResId = mApplicationContext.getResources()
                                                          .getIdentifier(splitName[0],
                                                                         "drawable",
                                                                         mApplicationContext.getPackageName());
                    SmartLog.e(TAG, "resId : " + stickerResId);
                    stickerBitmap = BitmapFactory.decodeResource(mApplicationContext.getResources(),
                                                                 stickerResId);
                } else {
                    File file = new File(stickerData.stickerFileName);
                    if(file.exists()) {
                        stickerBitmap = BitmapFactory.decodeFile(stickerData.stickerFileName);
                    } else {
                        SmartLog.e(TAG, "not sticker file");
                    }
                }

                if(stickerBitmap != null) {

                    float layoutScale = imageBitmap.getWidth() / (float)stickerImageWidth;
                    int stickerIndex = stickerView.addStickerRestore(stickerBitmap);
                    stickerView.setFrameImageBaseRotation(stickerIndex, stickerData.stickerRotate);
                    stickerView.setFrameImageBaseScale(stickerIndex, stickerData.stickerScale
                            * layoutScale);

                    // FacePointF point = stickerData.getStickerCoordinate(layoutScale);
                    FacePointF point = new FacePointF(0.f, 0.f);
                    point.x = stickerData.stickerCoordinate.x * layoutScale
                            - stickerBitmap.getWidth()
                            * (1f - stickerData.stickerScale * layoutScale) / 2f;
                    point.y = stickerData.stickerCoordinate.y * layoutScale
                            - stickerBitmap.getHeight()
                            * (1f - stickerData.stickerScale * layoutScale) / 2f;

                    stickerView.setFrameImageBaseTranslate(stickerIndex, point.x, point.y);
                }
            }
        }

        Canvas canvas = new Canvas(imageBitmap);
        canvas.save();
        stickerView.onAlternativeDraw(canvas, true);
        canvas.restore();
        return imageBitmap;
    }

    /**
     * 스티커를 적용하여 이미지를 반환한다. <br>
     * 이미지 데이터에 스티커 정보가 들어있을 경우 적용하여 반환한다. <br>
     * 없을 경우 null을 반환<br>
     * 인자의 비트맵 크기 그대로 스티커가 적용되어 반환.<br>>
     * 비트맵의 orientation은 계산하지 않는다.
     * 
     * @param bitmap 스티커를 적용할 비트맵
     * @param imageData 스티커를 적용할 이미지 데이터 (스티커 정보가 반드시 포함되어있어야함)
     * @return Bitmap 스티커가 적용된 비트맵
     */
    public synchronized Bitmap getStickerImageWithImageDataAndBitmap(Bitmap bitmap, ImageData imageData)
            throws IOException {
        // 데이터가 없거나 path가 없다면
        if(imageData == null || imageData.path == null || bitmap == null) {
            return null;
        }

        Bitmap imageBitmap = Bitmap.createBitmap(bitmap);

        if(imageBitmap == null) {
            SmartLog.e(TAG, "image Decoding Fail");
            return null;
        }

        if(imageData.imageCorrectData == null
                || imageData.imageCorrectData.stickerCorrectDataArray == null) {
            return imageBitmap;
        }

        // StickerView를 사이즈에 맞춰서 생성
        StickerView stickerView = new StickerView(mApplicationContext, imageBitmap.getWidth(),
                                                  imageBitmap.getHeight());
        // selection box color, width 변경
        stickerView.setStickerSelectionColorWithWidth(Color.WHITE, 5);
        stickerView.setTextStickerLimitLength(50);
        stickerView.setTextInputBorderColorWithWidth(Color.WHITE, 5);
        stickerView.setStickerScale(0.001f, 10.f);
        stickerView.setStickerDefaultScale(1.f);

        // 스티커 이미지를 가져오자
        for(int i = 0; i < imageData.imageCorrectData.stickerCorrectDataArray.size(); i++) {
            ImageCorrectStickerData stickerData = imageData.imageCorrectData.stickerCorrectDataArray.get(i);

            int stickerImageWidth = stickerData.imageWidth;

            if(stickerData.isTextSticker()) {
                String text = stickerData.text;

                int index = stickerView.addTextStickerRestore(text);
                // 보정데이터에 맞게 적용
                // 스티커 추천 보정시의 이미지 크기와 뷰에 뿌릴 때의 이미지 크기가 다르므로 비율을 맞춰준다.
                float scaleRatio = (float)imageBitmap.getWidth() / stickerImageWidth;

                stickerView.setFrameImageBaseRotation(index, stickerData.stickerRotate);
                stickerView.setFrameImageBaseScale(index, stickerData.stickerScale * scaleRatio);
                FacePointF translate = stickerData.getStickerCoordinate(scaleRatio);
                stickerView.setFrameImageBaseTranslate(index, translate.x, translate.y);
                stickerView.setTextStickerTextColor(index, stickerData.fontColor);
                stickerView.setTextStickerTextBorderColor(index, stickerData.textBorderColor);
                stickerView.setTextStickerTextWidth(index, stickerData.textWidth);
                stickerView.setTextStickerTextBorderWidth(index, stickerData.textBorderWidth);
                stickerView.setTextStickerStyle(index, stickerData.getTextStyle());
                String path = stickerData.typeFaceFilePath;
                if(!TextUtils.isEmpty(path)) {
                    Typeface typeFace = Typeface.createFromFile(path);
                    stickerView.setTextStickerFont(index, typeFace);
                }
            } else {
                String[] splitName = stickerData.stickerFileName.split("\\.");
                Bitmap stickerBitmap = null;
                if(splitName.length == 1) {
                    int stickerResId = mApplicationContext.getResources()
                                                          .getIdentifier(splitName[0],
                                                                         "drawable",
                                                                         mApplicationContext.getPackageName());
                    SmartLog.e(TAG, "resId : " + stickerResId);
                    stickerBitmap = BitmapFactory.decodeResource(mApplicationContext.getResources(),
                                                                 stickerResId);
                } else {
                    File file = new File(stickerData.stickerFileName);
                    if(file.exists()) {
                        stickerBitmap = BitmapFactory.decodeFile(stickerData.stickerFileName);
                    } else {
                        SmartLog.e(TAG, "not sticker file");
                    }
                }

                if(stickerBitmap != null) {

                    float layoutScale = imageBitmap.getWidth() / (float)stickerImageWidth;
                    int stickerIndex = stickerView.addStickerRestore(stickerBitmap);
                    stickerView.setFrameImageBaseRotation(stickerIndex, stickerData.stickerRotate);
                    stickerView.setFrameImageBaseScale(stickerIndex, stickerData.stickerScale
                            * layoutScale);

                    // FacePointF point = stickerData.getStickerCoordinate(layoutScale);
                    FacePointF point = new FacePointF(0.f, 0.f);
                    point.x = stickerData.stickerCoordinate.x * layoutScale
                            - stickerBitmap.getWidth()
                            * (1f - stickerData.stickerScale * layoutScale) / 2f;
                    point.y = stickerData.stickerCoordinate.y * layoutScale
                            - stickerBitmap.getHeight()
                            * (1f - stickerData.stickerScale * layoutScale) / 2f;

                    stickerView.setFrameImageBaseTranslate(stickerIndex, point.x, point.y);
                }
            }
        }

        Canvas canvas = new Canvas(imageBitmap);
        canvas.save();
        stickerView.onAlternativeDraw(canvas, true);
        canvas.restore();
        return imageBitmap;
    }

    private CountDownLatch mCountDownLatch;
    private StickerView mStickerView;

    /**
     * 스티커를 적용하여 이미지를 반환한다. <br>
     * 이미지 데이터에 스티커 정보가 들어있을 경우 적용하여 반환한다. <br>
     * 없을 경우 null을 반환<br>
     * 인자의 비트맵 크기 그대로 스티커가 적용되어 반환.<br>>
     * 비트맵의 orientation은 계산하지 않는다.<br>
     * Thread 사용 가능 함수
     * 
     * @param activity 액티비티
     * @param bitmap 스티커를 적용할 비트맵
     * @param imageData 스티커를 적용할 이미지 데이터 (스티커 정보가 반드시 포함되어있어야함)
     * @return Bitmap 스티커가 적용된 비트맵
     */
    public synchronized Bitmap getStickerImageWithImageDataAndBitmap(Activity activity, Bitmap bitmap,
            ImageData imageData) throws IOException {
        // 데이터가 없거나 path가 없다면
        if(imageData == null || imageData.path == null || bitmap == null) {
            return null;
        }

        mCountDownLatch = new CountDownLatch(1);

        Bitmap imageBitmap = Bitmap.createBitmap(bitmap);

        if(imageBitmap == null) {
            SmartLog.e(TAG, "image Decoding Fail");
            return null;
        }

        if(imageData.imageCorrectData == null
                || imageData.imageCorrectData.stickerCorrectDataArray == null) {
            return imageBitmap;
        }

        final int imageWidth = imageBitmap.getWidth();
        final int imageHeight = imageBitmap.getHeight();

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // StickerView를 사이즈에 맞춰서 생성
                mStickerView = new StickerView(mApplicationContext, imageWidth, imageHeight);
                // selection box color, width 변경
                mStickerView.setStickerSelectionColorWithWidth(Color.WHITE, 5);
                mStickerView.setTextStickerLimitLength(50);
                mStickerView.setTextInputBorderColorWithWidth(Color.WHITE, 5);
                mStickerView.setStickerScale(0.001f, 10.f);
                mStickerView.setStickerDefaultScale(1.f);
            }
        });

        try {
            mCountDownLatch.await(3, TimeUnit.SECONDS);
        } catch(InterruptedException e) {
        }

        if(mStickerView == null) {
            return null;
        }

        // 스티커 이미지를 가져오자
        for(int i = 0; i < imageData.imageCorrectData.stickerCorrectDataArray.size(); i++) {
            ImageCorrectStickerData stickerData = imageData.imageCorrectData.stickerCorrectDataArray.get(i);

            int stickerImageWidth = stickerData.imageWidth;

            if(stickerData.isTextSticker()) {
                String text = stickerData.text;

                int index = mStickerView.addTextStickerRestore(text);
                // 보정데이터에 맞게 적용
                // 스티커 추천 보정시의 이미지 크기와 뷰에 뿌릴 때의 이미지 크기가 다르므로 비율을 맞춰준다.
                float scaleRatio = (float)imageBitmap.getWidth() / stickerImageWidth;

                mStickerView.setFrameImageBaseRotation(index, stickerData.stickerRotate);
                mStickerView.setFrameImageBaseScale(index, stickerData.stickerScale * scaleRatio);
                FacePointF translate = stickerData.getStickerCoordinate(scaleRatio);
                mStickerView.setFrameImageBaseTranslate(index, translate.x, translate.y);
                mStickerView.setTextStickerTextColor(index, stickerData.fontColor);
                mStickerView.setTextStickerTextBorderColor(index, stickerData.textBorderColor);
                mStickerView.setTextStickerTextWidth(index, stickerData.textWidth);
                mStickerView.setTextStickerTextBorderWidth(index, stickerData.textBorderWidth);
                mStickerView.setTextStickerStyle(index, stickerData.getTextStyle());
                String path = stickerData.typeFaceFilePath;
                if(!TextUtils.isEmpty(path)) {
                    Typeface typeFace = Typeface.createFromFile(path);
                    mStickerView.setTextStickerFont(index, typeFace);
                }
            } else {
                String[] splitName = stickerData.stickerFileName.split("\\.");
                Bitmap stickerBitmap = null;
                if(splitName.length == 1) {
                    int stickerResId = mApplicationContext.getResources()
                                                          .getIdentifier(splitName[0],
                                                                         "drawable",
                                                                         mApplicationContext.getPackageName());
                    SmartLog.e(TAG, "resId : " + stickerResId);
                    stickerBitmap = BitmapFactory.decodeResource(mApplicationContext.getResources(),
                                                                 stickerResId);
                } else {
                    File file = new File(stickerData.stickerFileName);
                    if(file.exists()) {
                        stickerBitmap = BitmapFactory.decodeFile(stickerData.stickerFileName);
                    } else {
                        SmartLog.e(TAG, "not sticker file");
                    }
                }

                if(stickerBitmap != null) {

                    float layoutScale = imageBitmap.getWidth() / (float)stickerImageWidth;
                    int stickerIndex = mStickerView.addStickerRestore(stickerBitmap);
                    mStickerView.setFrameImageBaseRotation(stickerIndex, stickerData.stickerRotate);
                    mStickerView.setFrameImageBaseScale(stickerIndex, stickerData.stickerScale
                            * layoutScale);

                    // FacePointF point = stickerData.getStickerCoordinate(layoutScale);
                    FacePointF point = new FacePointF(0.f, 0.f);
                    point.x = stickerData.stickerCoordinate.x * layoutScale
                            - stickerBitmap.getWidth()
                            * (1f - stickerData.stickerScale * layoutScale) / 2f;
                    point.y = stickerData.stickerCoordinate.y * layoutScale
                            - stickerBitmap.getHeight()
                            * (1f - stickerData.stickerScale * layoutScale) / 2f;

                    mStickerView.setFrameImageBaseTranslate(stickerIndex, point.x, point.y);
                }
            }
        }

        Canvas canvas = new Canvas(imageBitmap);
        canvas.save();
        mStickerView.onAlternativeDraw(canvas, true);
        canvas.restore();
        return imageBitmap;
    }

    /**
     * 스티커를 적용하여 이미지를 반환한다. <br>
     * 스티커 한장만 적용할 수 있다. <br>
     * 일반 스티커만 적용할 수 있다. (텍스트 스티커 제외)
     * 
     * @param imageBitmap 스티커를 적용할 이미지 비트맵
     * @param orientation 이미지 파일의 회전 정보 (무조건 Portrait로 변경됨)
     * @param stickerFileName 적용할 스티커의 파일 이름 (파일의 경우 Asset에 저장되어있어야함)
     * @param stickerCoordinate 스티커의 위치 정보
     * @param stickerScale 스티커의 크기 정보
     * @param stickerRotate 스티커의 회전 정보
     * @return Bitmap 이미지와 스티커의 합성 이미지
     */
    public synchronized Bitmap getStickerImageWithBitmap(Bitmap imageBitmap, String orientation,
            String stickerFileName, FacePointF stickerCoordinate, float stickerScale,
            int stickerRotate, int originalImageWidth, int originalImageHeight) throws IOException {
        // 데이터가 없거나 path가 없다면
        if(imageBitmap == null || stickerFileName == null || originalImageWidth < 0
                || originalImageHeight < 0) {
            return null;
        }

        // 이미지 회전정보에 따라 처리
        if("90".equals(orientation) || "270".equals(orientation)) {
            originalImageWidth = originalImageHeight;
        }

        // StickerView를 사이즈에 맞춰서 생성
        StickerView stickerView = new StickerView(mApplicationContext, imageBitmap.getWidth(),
                                                  imageBitmap.getHeight());
        stickerView.setStickerScale(0.001f, 10.f);

        // 스티커 이미지를 가져오자
        String[] splitName = stickerFileName.split("\\.");
        Bitmap stickerBitmap = null;
        if(splitName.length == 1) {
            int stickerResId = mApplicationContext.getResources()
                                                  .getIdentifier(splitName[0],
                                                                 "drawable",
                                                                 mApplicationContext.getPackageName());
            SmartLog.e(TAG, "resId : " + stickerResId);
            stickerBitmap = BitmapFactory.decodeResource(mApplicationContext.getResources(),
                                                         stickerResId);
        } else {
            File file = new File(stickerFileName);
            if(file.exists()) {
                stickerBitmap = BitmapFactory.decodeFile(stickerFileName);
            } else {
                SmartLog.e(TAG, "not sticker file");
            }
        }
        if(stickerBitmap != null) {
            float layoutScale = imageBitmap.getWidth() / (float)originalImageWidth;
            int stickerIndex = stickerView.addStickerRestore(stickerBitmap);
            stickerView.setFrameImageBaseRotation(stickerIndex, stickerRotate);
            stickerView.setFrameImageBaseScale(stickerIndex, stickerScale * layoutScale);

            FacePointF point = new FacePointF(0.f, 0.f);
            point.x = stickerCoordinate.x * layoutScale - stickerBitmap.getWidth()
                    * (1f - stickerScale * layoutScale) / 2f;
            point.y = stickerCoordinate.y * layoutScale - stickerBitmap.getHeight()
                    * (1f - stickerScale * layoutScale) / 2f;

            stickerView.setFrameImageBaseTranslate(stickerIndex, point.x, point.y);

            Canvas canvas = new Canvas(imageBitmap);
            canvas.save();
            stickerView.onAlternativeDraw(canvas, true);
            canvas.restore();
        } else {
            SmartLog.e(TAG, "No exist Sticker Bitmap");
        }

        return imageBitmap;
    }

}
