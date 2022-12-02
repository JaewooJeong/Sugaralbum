
package com.kiwiple.imageframework.burstshot;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.hardware.Camera;
import android.text.TextUtils;

import com.kiwiple.imageframework.util.BitmapUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * {@link android.hardware.Camera.PreviewCallback#onPreviewFrame}으로 입력받은 yuv data(byte[])를 <br>
 * animated gif 파일로 변환해 주는 역할을 하는 class. <br>
 * {@link #getInstance} 함수로 instance를 생성하고 singleton으로 동작한다.
 * 
 * @version 2.0
 */
public class BurstShotManager {
    /**
     * 저장 가능한 최대 프레임의 기본 개수
     * 
     * @version 2.0
     */
    public static final int DEFAULT_MAX_SHOOT_NUMBER = 21;
    /**
     * 프레임 사이의 기본 간격(ms)
     * 
     * @version 2.0
     */
    public static final int DEAULT_INTERVAL = 330;
    /**
     * 저장할 gif파일의 프레임 사이 간격(ms)
     * 
     * @version 2.0
     */
    public static int sCurrentInterval = DEAULT_INTERVAL;
    /**
     * 저장할 gif파일의 기본 크기(%)
     * 
     * @version 2.0
     */
    public static final int DEAULT_SCALE = 100;

    private static BurstShotManager sInstance;

    private ArrayList<String> mYuvPathList = new ArrayList<String>();
    protected ArrayList<String> mJpgPathList = new ArrayList<String>();
    protected ArrayList<String> mSelectedJpgPathList = new ArrayList<String>();
    protected HashMap<Integer, Boolean> mSelectedJpgPathListIndex = new HashMap<Integer, Boolean>();
    private long mLastPreviewFrame;
    private Point mSize = null;

    private Context mContext;

    private boolean mProgress = false;
    private int mRotation = 0;
    private boolean mFlip = false;

    private int mMaxShot = DEFAULT_MAX_SHOOT_NUMBER;

    private BurstShotManager(Context applicationContext) {
        mContext = applicationContext;
    }

    /**
     * @param applicationContext
     * @return {@link #BurstShotManager}의 인스턴스 반환
     * @version 2.0
     */
    public synchronized static BurstShotManager getInstance(Context applicationContext) {
        if(sInstance == null) {
            sInstance = new BurstShotManager(applicationContext);
        }
        return sInstance;
    }

    /**
     * yuv data(byte[])의 이미지 회전 정보를 설정한다.
     * 
     * @param rotation 이미지 회전 정보 <br>
     *            (일반적으로 {@link android.hardware.Camera#setDisplayOrientation(int)}의 값과 동일하다.)
     * @version 2.0
     */
    public void setRotation(int rotation) {
        if(rotation < 0) {
            rotation += 360;
        } else if(rotation >= 360) {
            rotation -= 360;
        }
        mRotation = rotation;
    }

    /**
     * yuv data(byte[])의 이미지 flip 여부를 설정한다.
     * 
     * @param isFlip 이미지 flip 여부(일반적으로 전면카메라의 경우 true로 설정한다.)
     * @version 2.0
     */
    public void setFlip(boolean isFlip) {
        mFlip = isFlip;
    }

    /**
     * {@link android.hardware.Camera.PreviewCallback#onPreviewFrame}으로 입력받은 yuv data를 저장한다.
     * 
     * @param data yuv byte array
     * @param camera {@link android.hardware.Camera}
     * @return yuv data 저장 여부
     * @version 2.0
     */
    public boolean onPreviewFrame(byte[] data, Camera camera) {
        long current = System.currentTimeMillis();
        if(mLastPreviewFrame != 0 && (current - mLastPreviewFrame) - DEAULT_INTERVAL < -15) {
            return false;
        }
        if(!mProgress) {
            return false;
        }
        if(mYuvPathList.size() >= mMaxShot) {
            mProgress = false;
            return true;
        }
        if(mYuvPathList.size() == 0) {
            mSize = new Point(camera.getParameters().getPreviewSize().width,
                              camera.getParameters().getPreviewSize().height);
        }
        mLastPreviewFrame = current;

        String yuvFilePath = BurstShotUtils.saveYuvPreview(mContext,
                                                           String.valueOf(mYuvPathList.size())
                                                                   + ".yuv", data);
        if(!TextUtils.isEmpty(yuvFilePath)) {
            mYuvPathList.add(yuvFilePath);
        }
        return false;
    }

    /**
     * 저장할 yuv data의 최대 개수를 설정한다.
     * 
     * @param max 저장할 yuv data의 최대 개수
     * @version 2.0
     */
    public void setMaxShot(int max) {
        mMaxShot = max;
    }

    /**
     * 저장된 yuv data, 설정 값을 초기화 한다.
     * 
     * @version 2.0
     */
    public void reset() {
        mYuvPathList.clear();
        mJpgPathList.clear();
        mSelectedJpgPathList.clear();
        mSelectedJpgPathListIndex.clear();
        mLastPreviewFrame = 0;
        mMaxShot = DEFAULT_MAX_SHOOT_NUMBER;
        mProgress = false;
    }

    /**
     * {@link #onPreviewFrame}로 전될되는 yuv data의 저장을 시작한다.
     * 
     * @version 2.0
     */
    public void start() {
        reset();
        mProgress = true;
    }

    /**
     * {@link #onPreviewFrame}로 전될되는 yuv data의 저장을 중지한다.
     * 
     * @version 2.0
     */
    public void stop() {
        mProgress = false;
    }

    /**
     * {@link #onPreviewFrame}로 전될되는 yuv data의 저장이 진행되고 있는지 여부를 확인한다.
     * 
     * @return yuv data 저장 진행 여부
     * @version 2.0
     */
    public boolean isProgress() {
        return mProgress;
    }

    /**
     * 저장된 yuv data의 개수를 반환한다.
     * 
     * @return 저장된 yuv data 개수
     * @version 2.0
     */
    public int getCurrentYuvCount() {
        return mYuvPathList.size();
    }

    /**
     * 저장된 jpeg 파일의 개수를 반환한다.
     * 
     * @return 저장된 jpeg 파일 개수
     * @version 2.0
     */
    public int getCurrentJpgCount() {
        return mJpgPathList.size();
    }

    /**
     * 선택된 jpeg 파일의 개수를 반환한다.
     * 
     * @return 선택된 jpeg 파일 개수
     * @version 2.0
     */
    public int getSelectedJpgCount() {
        return mSelectedJpgPathList.size();
    }

    /**
     * 저장된 jpeg 파일 목록을 반환한다.
     * 
     * @return jpeg 파일 목록
     * @version 2.0
     */
    public ArrayList<String> getJpgList() {
        return mJpgPathList;
    }

    /**
     * 선택된 jpeg 파일 목록을 설정한다.
     * 
     * @return jpeg 파일 목록
     * @version 2.0
     */
    public void setJpgList(ArrayList<String> jpgList) {
        mJpgPathList = jpgList;
        mSelectedJpgPathList.clear();
        for(int i = 0; i < mJpgPathList.size(); i++) {
            if(mSelectedJpgPathListIndex.get(i)) {
                mSelectedJpgPathList.add(mJpgPathList.get(i));
            }
        }
    }

    /**
     * 저장된 yuv data를 jpeg 파일로 변환한다.
     * 
     * @version 2.0
     */
    public void saveYuvToJpg() {
        mJpgPathList.clear();
        mSelectedJpgPathList.clear();
        mSelectedJpgPathListIndex.clear();
        int listLen = mYuvPathList.size();
        YuvImgSaverAsyncTask task = new YuvImgSaverAsyncTask(mSize, mRotation, mFlip);
        for(int i = 0; i < listLen; i++) {
            countSavedImage(task.execute(mYuvPathList.get(i)));
        }
    }

    /**
     * 
     * jpeg 파일 목록을 추가한다.
     * 
     * @param jpgPathList 추가할 jpeg 파일 목록.
     */
    public void addJpegPath(ArrayList<String> jpgPathList) {
        if(jpgPathList == null || jpgPathList.isEmpty()) {
            return;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        int widthCount = 0;
        int heightCount = 0;
        ArrayList<String> badImageList = new ArrayList<String>();
        for(String jpgPath : jpgPathList) {
            BitmapFactory.decodeFile(jpgPath, options);

            if(options.outWidth == 0 || options.outHeight == 0) {
                badImageList.add(jpgPath);
                continue;
            }

            try {
                int rotation = BitmapUtils.getImageRotation(jpgPath);
                if(rotation == 90 || rotation == 270) {
                    int tmp = options.outWidth;
                    options.outWidth = options.outHeight;
                    options.outHeight = tmp;
                }
            } catch(IOException e) {
            }
            if(options.outWidth > options.outHeight) {
                widthCount++;
            } else {
                heightCount++;
            }
        }
        // 디코딩 할 수 없는 이미지는 목록에서 제거한다.
        for(String badImage : badImageList) {
            jpgPathList.remove(badImage);
        }

        // 해상도가 서로 다른 이미지들로 animated gif 만들 때, gif의 해상도 결정 기준은 U+측에서 정해줌.
        if(widthCount > heightCount) {
            mSize = new Point(960, 720);
        } else {
            mSize = new Point(720, 960);
        }
        for(String jpgPath : jpgPathList) {
            addJpegPath(jpgPath);
        }
    }

    /**
     * 
     * jpeg 파일을 추가한다.
     * 
     * @param jpgPath 추가할 jpeg 파일.
     */
    public void addJpegPath(String jpgPath) {
        if(TextUtils.isEmpty(jpgPath) || !new File(jpgPath).exists()) {
            return;
        }
        if(mSize == null) {
            mSize = new Point(960, 960);
        }
        countSavedImage(JpegImgSaverAsyncTask.execute(mContext, mSize, jpgPath, mJpgPathList.size()));

    }

    private void countSavedImage(String jpgPath) {
        if(TextUtils.isEmpty(jpgPath)) {
            return;
        }
        mJpgPathList.add(jpgPath);
        mSelectedJpgPathList.add(jpgPath);
        mSelectedJpgPathListIndex.put(mJpgPathList.size() - 1, true);
    }

    /**
     * 저장된 jpeg 파일 목록 index에 해당하는 jpeg를 선택/해제한다.
     * 
     * @param index 선택/해제할 jpeg의 index
     * @param select jpeg의 선택 여부(true면 선택, false면 해제)
     * @version 2.0
     */
    public void selectedJpg(int index, boolean select) {
        mSelectedJpgPathListIndex.put(index, select);
        mSelectedJpgPathList.clear();
        for(int i = 0; i < mJpgPathList.size(); i++) {
            if(mSelectedJpgPathListIndex.get(i)) {
                mSelectedJpgPathList.add(mJpgPathList.get(i));
            }
        }
    }

    /**
     * 저장된 jpeg 파일 목록 index에 해당하는 jpeg의 선택 여부를 토글한다.
     * 
     * @param index 선택/해제할 jpeg의 index
     * @version 2.0
     */
    public void toggleSelectedJpg(int index) {
        selectedJpg(index, !mSelectedJpgPathListIndex.get(index));
    }

    /**
     * 특정 jpeg의 선택 여부를 확인한다.
     * 
     * @param index jpeg의 index
     * @return jpeg 선택 여부
     * @version 2.0
     */
    public boolean isSelected(int index) {
        return mSelectedJpgPathListIndex.get(index);
    }

    /**
     * 저장된 jpeg 파일 목록의 index를 선택된 jpeg 파일 목록의 index로 변환해준다. <br>
     * 
     * @param index 저장된 jpeg 파일 목록에서의 index
     * @return 선택된 jpeg 파일 목록에서의 index
     * @version 2.0
     */
    public int convertSelectkedIndex(int index) {
        int convertedIndex = 0;
        for(int i = 0; i < index + 1; i++) {
            if(mSelectedJpgPathListIndex.get(i)) {
                convertedIndex++;
            }
        }
        return convertedIndex;
    }

    /**
     * 저장된 jpeg 파일을 {@link android.graphics.Bitmap}으로 변환한다.
     * 
     * @param index 저장된 jpeg 파일의 index
     * @param size {@link android.graphics.Bitmap}의 크기
     * @return {@link android.graphics.Bitmap}
     * @version 2.0
     */
    public Bitmap getImage(int index, int size) {
        if(mJpgPathList.size() <= index) {
            return null;
        }
        Bitmap image = null;

        try {
            image = BurstShotUtils.decodingImage(mJpgPathList.get(index), size, Config.RGB_565);
        } catch(IOException e) {
        }
        return image;
    }

    /**
     * 선택된 jpeg 파일을 {@link android.graphics.Bitmap}으로 변환한다.
     * 
     * @param index 선택된 jpeg 파일의 index
     * @param size {@link android.graphics.Bitmap}의 크기
     * @return {@link android.graphics.Bitmap}
     * @version 2.0
     */
    public Bitmap getSelectedImage(int index, int size) {
        if(mSelectedJpgPathList.size() <= index) {
            return null;
        }
        Bitmap image = null;

        try {
            image = BurstShotUtils.decodingImage(mSelectedJpgPathList.get(index), size,
                                                 Config.RGB_565);
        } catch(IOException e) {
        }
        return image;
    }

}
