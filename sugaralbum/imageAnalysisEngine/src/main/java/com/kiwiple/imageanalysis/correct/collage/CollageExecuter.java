
package com.kiwiple.imageanalysis.correct.collage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Shader;
import android.os.Build;
import android.text.TextUtils;
import android.util.SparseArray;

import com.kiwiple.imageanalysis.correct.ImageCorrectData;
import com.kiwiple.imageanalysis.database.FacePointF;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.utils.BitmapUtils;
import com.kiwiple.imageanalysis.utils.SmartLog;
import com.kiwiple.imageframework.collage.CollageView;
import com.kiwiple.imageframework.collage.DesignTemplateManager;
import com.kiwiple.imageframework.collage.TemplateInfo;
import com.kiwiple.imageframework.sticker.StickerView;
import com.kiwiple.imageframework.util.FileUtils;

/**
 * 콜라주를 적용하기 위한 클래스<br>
 * 이미지 데이터의 보정 데이터를 읽어 콜라주를 생성하여 반환한다.
 */
public class CollageExecuter {

    private static final String TAG = CollageExecuter.class.getSimpleName();

    private static CollageExecuter sInstance;

    private Context mApplicationContext;

    private CollageView mCollageView;
    private TemplateInfo mTemplateInfo;
    private int mDecodeImageSize;

    /**
     * 싱글톤 생성자
     * 
     * @param applicationContext ApplicationContext
     * @param filePath 템플릿 목록 파일 경로
     * @param isAsset Asset폴더 여부 (아닌 경우 일반 파일로 간주)
     */
    public static CollageExecuter getInstance(Context applicationContext) {
        if(sInstance == null) {
            sInstance = new CollageExecuter(applicationContext);
        }
        return sInstance;
    }

    /**
     * 생성자
     * 
     * @param applicationContext ApplicationContext
     * @param filePath 템플릿 목록 파일 경로
     * @param isAsset Asset폴더 여부 (아닌 경우 일반 파일로 간주)
     */
    private CollageExecuter(Context applicationContext) {
        mApplicationContext = applicationContext;
    }

    /**
     * 콜라주 이미지를 Bitmap으로 반환. <br>
     * 인자인 ImageData 갯수 = 프레임 갯수. <br>
     * 이미지 보정데이터가 ImageData에 포함되어 있다면 해당 이미지 보정데이터를 적용한다. <br>
     * 콜라주 가로 길이에 따라 결정된 프레임에 맞게 세로 길이가 결정된다. <br>
     * 이미지 데이터가 없거나 1개 이하라면 null을 반환함.<br>
     * Thread 사용 불가능함수
     * 
     * @param collageWidth 가로 길이
     * @return Bitmap 완성된 콜라주 이미지
     */
    public synchronized Bitmap getCollageImage(ArrayList<ImageData> imageDatas, int collageWidth) {

        // 이미지 데이터가 부족하면 null을 반환해야지
        if(imageDatas == null || imageDatas.isEmpty() || collageWidth < 1) {
            SmartLog.e(TAG, "no image data");
            return null;
        }

        ImageCorrectData imageCorrectData = imageDatas.get(0).imageCorrectData;
        // 보정데이터에서 템플릿 Id를 읽어온다.
        int templateId = imageCorrectData.collageTempletId;

        mTemplateInfo = DesignTemplateManager.getInstance(mApplicationContext)
                                             .getTemplateInfo(templateId);

        // 설정된 템플릿 Id가 없다면 template을 랜덤으로 설정한다.
        if(mTemplateInfo == null) {
            SmartLog.e(TAG, "Not Match TemplateInfo");
            // try {
            // DesignTemplateManager.getInstance(mApplicationContext).setTemplateAsset(mFilePath);
            // } catch(IOException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            //
            // mTemplateInfo = DesignTemplateManager.getInstance(mApplicationContext)
            // .getTemplateInfo(templateId);

            // if(mTemplateInfo == null) {
            return null;
            // }
        }

        int collageHeight = (int)(collageWidth * mTemplateInfo.getAspectRatio());

        // 콜라주 생성
        mCollageView = new CollageView(mApplicationContext, mTemplateInfo, collageWidth,
                                       collageHeight);
        mDecodeImageSize = collageWidth > collageHeight ? collageWidth : collageHeight;
        mCollageView.setFrameImageScale(0.001f, 10.f);
        mCollageView.setTemplateBackgroundColor(Color.WHITE);

        StickerView stickerView = new StickerView(mApplicationContext, collageWidth, collageHeight,
                                                  mTemplateInfo);
        stickerView.setStickerSelectionColorWithWidth(Color.WHITE, 5);
        stickerView.setStickerScale(0.001f, 10.f);

        // 이미지 셋팅
        decodeFilledImageFiles(imageDatas);

        // 테두리 두께
        if(imageCorrectData.collageFrameBorderWidth > 0) {
            mCollageView.setFrameBorderWidth(imageCorrectData.collageFrameBorderWidth);
        }

        // 테두리 원형
        if(imageCorrectData.collageFrameCornerRadius > 0) {
            mCollageView.setFrameCornerRadius(imageCorrectData.collageFrameCornerRadius);
        }

        // 프레임 배경 색상
        if(imageCorrectData.collageBackgroundColor != 0
                && imageCorrectData.collageBackgroundColorTag != null
                && !imageCorrectData.collageBackgroundColorTag.isEmpty()) {

            mCollageView.setTemplateBackgroundPattern(null);
            mCollageView.setTemplateBackgroundImage(null);
            mCollageView.setTemplateBackgroundColor(imageCorrectData.collageBackgroundColor);
            for(int j = 0; j < imageDatas.size(); j++) {
                mCollageView.setFrameBorderColor(j, imageCorrectData.collageBackgroundColor);
            }
        }

        // 프레임 배경 이미지
        if(imageCorrectData.collageBackgroundImageFileName != null
                && !imageCorrectData.collageBackgroundImageFileName.isEmpty()) {

            Bitmap pattern = BitmapFactory.decodeResource(mApplicationContext.getResources(),
                                                          FileUtils.getBitmapResourceId(mApplicationContext,
                                                                                        imageCorrectData.collageBackgroundImageFileName));
            if(pattern != null) {
                BitmapShader shader = new BitmapShader(pattern, Shader.TileMode.REPEAT,
                                                       Shader.TileMode.REPEAT);
                mCollageView.setTemplateBackgroundImage(null);
                for(int i = 0; i < imageDatas.size(); i++) {
                    mCollageView.setFrameBorderColor(i, Color.TRANSPARENT);
                }
                mCollageView.setTemplateBackgroundPattern(shader);
            }
        }

        // 완성된 이미지 반환
        Bitmap bitmap = Bitmap.createBitmap(collageWidth, collageHeight,
                                            Build.VERSION.SDK_INT < 9 ? Config.ARGB_4444
                                                    : Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.save();
        canvas.scale(1, 1);
        mCollageView.onAlternativeDraw(canvas, true);
        SmartLog.e(TAG, "stickerCount : " + stickerView.getStickerCount());
        if(stickerView.getStickerCount() > 0) {
            stickerView.onAlternativeDraw(canvas, true);
            Bitmap sticker;
            for(int i = 0 ; i < stickerView.getStickerCount() ; i++) {
                sticker = stickerView.getImage(i);
                if(sticker != null && !sticker.isRecycled()) {
                    sticker.recycle();
                    sticker = null;
                }
            }
        }
        SparseArray<Bitmap> images =  mCollageView.getFrameImages();
        Bitmap frame;
        for(int i = 0 ; i < images.size() ; i++) {
            frame = images.valueAt(i);
            if(frame != null && !frame.isRecycled()) {
                frame.recycle();
                frame = null;
            }
        }
        canvas.restore();

        return bitmap;
    }

    private CountDownLatch mCountDownLatch;
    private StickerView mStickerView;

    /**
     * 콜라주 이미지를 Bitmap으로 반환. <br>
     * 인자인 ImageData 갯수 = 프레임 갯수. <br>
     * 이미지 보정데이터가 ImageData에 포함되어 있다면 해당 이미지 보정데이터를 적용한다. <br>
     * 콜라주 가로 길이에 따라 결정된 프레임에 맞게 세로 길이가 결정된다. <br>
     * 이미지 데이터가 없거나 1개 이하라면 null을 반환함.<br>
     * Thread 사용 가능함수
     * 
     * @param activity Activity
     * @param imageDatas 콜라주에 들어갈 이미지 정보
     * @param collageWidth 가로 길이
     * @return Bitmap 완성된 콜라주 이미지
     */
    public synchronized Bitmap getCollageImage(Activity activity, ArrayList<ImageData> imageDatas,
            int collageWidth) {

        // 이미지 데이터가 부족하면 null을 반환해야지
        if(imageDatas == null || imageDatas.isEmpty() || collageWidth < 1) {
            SmartLog.e(TAG, "no image data");
            return null;
        }

        mCountDownLatch = new CountDownLatch(1);

        ImageCorrectData imageCorrectData = imageDatas.get(0).imageCorrectData;
        // 보정데이터에서 템플릿 Id를 읽어온다.
        int templateId = imageCorrectData.collageTempletId;
        mTemplateInfo = DesignTemplateManager.getInstance(mApplicationContext)
                                             .getTemplateInfo(templateId);

        // 설정된 템플릿 Id가 없다면 template을 랜덤으로 설정한다.
        if(mTemplateInfo == null) {
            SmartLog.e(TAG, "Not Match TemplateInfo");
            // try {
            // DesignTemplateManager.getInstance(mApplicationContext).setTemplateAsset(mFilePath);
            // } catch(IOException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            //
            // mTemplateInfo = DesignTemplateManager.getInstance(mApplicationContext)
            // .getTemplateInfo(templateId);

            // if(mTemplateInfo == null) {
            return null;
            // }
        }

        int collageHeight = (int)(collageWidth * mTemplateInfo.getAspectRatio());

        final ArrayList<ImageData> inputImageDatas = new ArrayList<ImageData>(imageDatas);
        final int collageWidthValue = collageWidth;
        final int collageHeightValue = collageHeight;

        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // 콜라주 생성
                mCollageView = new CollageView(mApplicationContext, mTemplateInfo,
                                               collageWidthValue, collageHeightValue);
                mDecodeImageSize = collageWidthValue > collageHeightValue ? collageWidthValue
                        : collageHeightValue;
                mCollageView.setFrameImageScale(0.001f, 10.f);
                mCollageView.setTemplateBackgroundColor(Color.WHITE);

                mStickerView = new StickerView(mApplicationContext, collageWidthValue,
                                               collageHeightValue, mTemplateInfo);
                mStickerView.setStickerSelectionColorWithWidth(Color.WHITE, 5);
                mStickerView.setStickerScale(0.001f, 10.f);

                // 이미지 셋팅
                decodeFilledImageFiles(inputImageDatas);

                mCountDownLatch.countDown();
            }
        });

        try {
            mCountDownLatch.await(3, TimeUnit.SECONDS);
        } catch(InterruptedException e) {
        }

        if(mStickerView == null) {
            return null;
        }

        // 테두리 두께
        if(imageCorrectData.collageFrameBorderWidth > 0) {
            mCollageView.setFrameBorderWidth(imageCorrectData.collageFrameBorderWidth);
        }

        // 테두리 원형
        if(imageCorrectData.collageFrameCornerRadius > 0) {
            mCollageView.setFrameCornerRadius(imageCorrectData.collageFrameCornerRadius);
        }

        // 프레임 배경 색상
        if(imageCorrectData.collageBackgroundColor != 0
                && imageCorrectData.collageBackgroundColorTag != null
                && !imageCorrectData.collageBackgroundColorTag.isEmpty()) {

            mCollageView.setTemplateBackgroundPattern(null);
            mCollageView.setTemplateBackgroundImage(null);
            mCollageView.setTemplateBackgroundColor(imageCorrectData.collageBackgroundColor);
            for(int j = 0; j < imageDatas.size(); j++) {
                mCollageView.setFrameBorderColor(j, imageCorrectData.collageBackgroundColor);
            }
        }

        // 프레임 배경 이미지
        if(imageCorrectData.collageBackgroundImageFileName != null
                && !imageCorrectData.collageBackgroundImageFileName.isEmpty()) {

            Bitmap pattern = BitmapFactory.decodeResource(mApplicationContext.getResources(),
                                                          FileUtils.getBitmapResourceId(mApplicationContext,
                                                                                        imageCorrectData.collageBackgroundImageFileName));
            if(pattern != null) {
                BitmapShader shader = new BitmapShader(pattern, Shader.TileMode.REPEAT,
                                                       Shader.TileMode.REPEAT);
                mCollageView.setTemplateBackgroundImage(null);
                for(int i = 0; i < imageDatas.size(); i++) {
                    mCollageView.setFrameBorderColor(i, Color.TRANSPARENT);
                }
                mCollageView.setTemplateBackgroundPattern(shader);
            }
        }

        // 완성된 이미지 반환
        Bitmap bitmap = Bitmap.createBitmap(collageWidth, collageHeight,
                                            Build.VERSION.SDK_INT < 9 ? Config.ARGB_4444
                                                    : Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.save();
        canvas.scale(1, 1);
        mCollageView.onAlternativeDraw(canvas, true);
        if(mStickerView.getStickerCount() > 0) {
            mStickerView.onAlternativeDraw(canvas, true);
        }
        canvas.restore();

        return bitmap;
    }
    
    /**
     * 콜라주 이미지를 이미지 없는 빈 프레임만 Bitmap으로 반환. <br>
     * 인자인 ImageData 갯수 = 프레임 갯수. <br>
     * 이미지 보정데이터가 ImageData에 포함되어 있다면 해당 이미지 보정데이터를 적용한다. <br>
     * 콜라주 가로 길이에 따라 결정된 프레임에 맞게 세로 길이가 결정된다. <br>
     * 이미지 데이터가 없거나 1개 이하라면 null을 반환함.<br>
     * Thread 사용 가능함수
     * 
     * @param activity Activity
     * @param imageDatas 콜라주에 들어갈 이미지 정보
     * @param collageWidth 가로 길이
     * @return Bitmap 완성된 콜라주 이미지
     */
    public synchronized Bitmap getCollageCoverImage(Activity activity, ArrayList<ImageData> imageDatas,
            int collageWidth) {

        // 이미지 데이터가 부족하면 null을 반환해야지
        if(imageDatas == null || imageDatas.isEmpty() || collageWidth < 1) {
            SmartLog.e(TAG, "no image data");
            return null;
        }

        mCountDownLatch = new CountDownLatch(1);

        ImageCorrectData imageCorrectData = imageDatas.get(0).imageCorrectData;
        // 보정데이터에서 템플릿 Id를 읽어온다.
        int templateId = imageCorrectData.collageTempletId;
        mTemplateInfo = DesignTemplateManager.getInstance(mApplicationContext)
                                             .getTemplateInfo(templateId);

        // 설정된 템플릿 Id가 없다면 template을 랜덤으로 설정한다.
        if(mTemplateInfo == null) {
            SmartLog.e(TAG, "Not Match TemplateInfo");
            // try {
            // DesignTemplateManager.getInstance(mApplicationContext).setTemplateAsset(mFilePath);
            // } catch(IOException e) {
            // // TODO Auto-generated catch block
            // e.printStackTrace();
            // }
            //
            // mTemplateInfo = DesignTemplateManager.getInstance(mApplicationContext)
            // .getTemplateInfo(templateId);

            // if(mTemplateInfo == null) {
            return null;
            // }
        }

        int collageHeight = (int)(collageWidth * mTemplateInfo.getAspectRatio());

        final ArrayList<ImageData> inputImageDatas = new ArrayList<ImageData>(imageDatas);
        final int collageWidthValue = collageWidth;
        final int collageHeightValue = collageHeight;

        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                // 콜라주 생성
                mCollageView = new CollageView(mApplicationContext, mTemplateInfo,
                                               collageWidthValue, collageHeightValue);
                mDecodeImageSize = collageWidthValue > collageHeightValue ? collageWidthValue
                        : collageHeightValue;
                mCollageView.setFrameImageScale(0.001f, 10.f);
                mCollageView.setTemplateBackgroundColor(Color.WHITE);

                mStickerView = new StickerView(mApplicationContext, collageWidthValue,
                                               collageHeightValue, mTemplateInfo);
                mStickerView.setStickerSelectionColorWithWidth(Color.WHITE, 5);
                mStickerView.setStickerScale(0.001f, 10.f);

                // 이미지 셋팅
//                decodeFilledImageFiles(inputImageDatas);

                mCountDownLatch.countDown();
            }
        });

        try {
            mCountDownLatch.await(3, TimeUnit.SECONDS);
        } catch(InterruptedException e) {
        }

        if(mStickerView == null) {
            return null;
        }

        // 테두리 두께
        if(imageCorrectData.collageFrameBorderWidth > 0) {
            mCollageView.setFrameBorderWidth(imageCorrectData.collageFrameBorderWidth);
        }

        // 테두리 원형
        if(imageCorrectData.collageFrameCornerRadius > 0) {
            mCollageView.setFrameCornerRadius(imageCorrectData.collageFrameCornerRadius);
        }

        // 프레임 배경 색상
        if(imageCorrectData.collageBackgroundColor != 0
                && imageCorrectData.collageBackgroundColorTag != null
                && !imageCorrectData.collageBackgroundColorTag.isEmpty()) {

            mCollageView.setTemplateBackgroundPattern(null);
            mCollageView.setTemplateBackgroundImage(null);
            mCollageView.setTemplateBackgroundColor(imageCorrectData.collageBackgroundColor);
            for(int j = 0; j < imageDatas.size(); j++) {
                mCollageView.setFrameBorderColor(j, imageCorrectData.collageBackgroundColor);
            }
        }

        // 프레임 배경 이미지
        if(imageCorrectData.collageBackgroundImageFileName != null
                && !imageCorrectData.collageBackgroundImageFileName.isEmpty()) {

            Bitmap pattern = BitmapFactory.decodeResource(mApplicationContext.getResources(),
                                                          FileUtils.getBitmapResourceId(mApplicationContext,
                                                                                        imageCorrectData.collageBackgroundImageFileName));
            if(pattern != null) {
                BitmapShader shader = new BitmapShader(pattern, Shader.TileMode.REPEAT,
                                                       Shader.TileMode.REPEAT);
                mCollageView.setTemplateBackgroundImage(null);
                for(int i = 0; i < imageDatas.size(); i++) {
                    mCollageView.setFrameBorderColor(i, Color.TRANSPARENT);
                }
                mCollageView.setTemplateBackgroundPattern(shader);
            }
        }

        // 완성된 이미지 반환
        Bitmap bitmap = Bitmap.createBitmap(collageWidth, collageHeight,
                                            Build.VERSION.SDK_INT < 9 ? Config.ARGB_4444
                                                    : Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.save();
        canvas.scale(1, 1);
        mCollageView.onAlternativeDraw(canvas, true);
        if(mStickerView.getStickerCount() > 0) {
            mStickerView.onAlternativeDraw(canvas, true);
        }
        canvas.restore();

        return bitmap;
    }

    /**
     * 이미지 데이터 배열로 콜라주에 이미지를 채우는 함수<br>
     * 
     * @param imageDatas 콜라주에 들어갈 이미지 데이터 배열
     */
    private void decodeFilledImageFiles(ArrayList<ImageData> imageDatas) {
        try {
            int index = 0;
            for(ImageData imageData : imageDatas) {
                setFilledImage(index++, imageData);
            }
        } catch(Exception e) {
            SmartLog.e("", "decodeFilledImageFiles Exception", e);
        } catch(OutOfMemoryError e) {
            SmartLog.e("", "decodeFilledImageFiles OutOfMemoryError", e);
        }
    }

    private void setFilledImage(int index, ImageData imageData) {
        if(!TextUtils.isEmpty(imageData.path)) {
            ImageCorrectData imageCorrectData = imageData.imageCorrectData;
            setFilledImage(index, imageData.path, imageCorrectData.collageRotate,
                           imageCorrectData.collageScale, imageCorrectData.collageCoordinate);
        } else {
            SmartLog.e("", "file Path null");
        }
    }

    private void setFilledImage(int index, String filePath, float collageRotate,
            float collageScale, FacePointF collageCoordinate) {
        if(!TextUtils.isEmpty(filePath)) {
            float scale = 1.f / mTemplateInfo.getFrameCount();
            Bitmap bitmap = null;
            int imageOrientation = 0;
            try {
                bitmap = FileUtils.decodingImage(filePath, (int)(mDecodeImageSize * scale),
                                                 Build.VERSION.SDK_INT < 9 ? Config.ARGB_4444
                                                         : Config.ARGB_8888);
                imageOrientation = BitmapUtils.getImageRotation(filePath);
            } catch(IOException e) {
                SmartLog.e("", "image load fail : " + filePath);
            }

            if(bitmap != null) {
                setFilledImage(index, bitmap, imageOrientation, collageRotate, collageScale,
                               collageCoordinate);
            } else {
                SmartLog.e("", "image load fail : " + filePath);
            }
        } else {
            SmartLog.e("", "file Path null");
        }
    }

    private void setFilledImage(int index, Bitmap bitmap, int imageOrientation,
            float frameImageRotate, float frameImageScale, FacePointF frameImageCoordinate) {
        try {

            if(bitmap == null) {
                return;
            }

            if(frameImageCoordinate == null) {
                frameImageCoordinate = new FacePointF(0.f, 0.f);
            }

            mCollageView.setFrameImageDefaultRotation(index, imageOrientation);
            mCollageView.setFrameImage(index, bitmap);
            mCollageView.setFrameImageBaseRotation(index, (int)frameImageRotate * -1);
            mCollageView.setFrameImageBaseScale(index, frameImageScale);
            mCollageView.setFrameImageBaseTranslate(index, frameImageCoordinate.x,
                                                    frameImageCoordinate.y);
        } catch(Exception e) {
            // Do nothing. May be activity state is finishing.
            SmartLog.e("", "decodeFilledImageFiles", e);
        } catch(OutOfMemoryError e2) {
            SmartLog.e("", "decodeFilledImageFiles", e2);
            System.gc();
            throw new OutOfMemoryError();
        }
    }
}
