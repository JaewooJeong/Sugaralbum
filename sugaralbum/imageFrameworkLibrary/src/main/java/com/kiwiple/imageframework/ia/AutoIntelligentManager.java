
package com.kiwiple.imageframework.ia;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;

import android.content.Context;
import android.hardware.Camera;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;

import com.kiwiple.imageframework.filter.CurvesPoint;
import com.kiwiple.imageframework.filter.Filter;
import com.kiwiple.imageframework.filter.live.LiveFilterController;
import com.kiwiple.imageframework.util.FileUtils;
import com.kiwiple.imageframework.util.thread.PoolWorkerRunnable;
import com.kiwiple.imageframework.util.thread.WorkQueue;

/**
 * 카메라 프리뷰 이미지를 분석하여 자동보정 효과를 주기위한 class <br>
 * {@link #getInstance} 함수로 instance를 생성하고 singleton으로 동작한다.
 * 
 * @version 2.0
 */
public class AutoIntelligentManager {
    private static AutoIntelligentManager sInstance;

    private Context mContext;

    private static final int MODE_FACE = 1;// "사람";
    private static final int MODE_DAY = 2;// "형광등 ";
    private static final int MODE_DAYP = 3;// "해님";
    private static final int MODE_NIGHT = 4;// "형광등";
    private static final int MODE_NIGHTP = 5;// "달님";
    private static final int MODE_AUTO = 6;// "자동";
    private int mCamMode = 0;

    private Camera.Size mPreviewSize;
    private Filter mFilter;

    private boolean mFrontCamera;
    private int mNumOfFaces;

    /**
     * 전체 평균 밝기
     */
    private float mLastBrightness = 1000;
    /**
     * left, top, right, bottom, center의 평균 밝기
     */
    private float[] mPatialBrightness = new float[] {
            1000, 1000, 1000, 1000, 1000
    };

    private Handler mUiUpdatehandler = new Handler();

    private int mCounter = 60;

    private LiveFilterController mLiveFilterController;

    private WeakReference<ImageView> mCameraModeViewPortrait;
    private WeakReference<ImageView> mCameraModeViewLandscape;

    private AutoIntelligentManager(Context applicationContext) {
        mContext = applicationContext;
    }

    /**
     * @param applicationContext
     * @return {@link #AutoIntelligentManager}의 인스턴스 반환
     * @version 1.0
     */
    public static AutoIntelligentManager getInstance(Context applicationContext) {
        if(sInstance == null) {
            sInstance = new AutoIntelligentManager(applicationContext);
        }
        return sInstance;
    }

    /**
     * 자동 보정 설정값을 초기화 한다.
     * 
     * @version 2.0
     */
    public void init() {
        mLastBrightness = 1000;
        mPatialBrightness[0] = 1000;
        mPatialBrightness[1] = 1000;
        mPatialBrightness[2] = 1000;
        mPatialBrightness[3] = 1000;
        mPatialBrightness[4] = 1000;
        setCameraMode(MODE_AUTO);
    }

    /**
     * 자동 보정 모드 표시 여부를 설정한다.
     * 
     * @param visible 자동 보정 모드 표시 여부
     * @version 2.0
     */
    public void setVisibility(boolean visible) {
        if(mCameraModeViewPortrait.get() != null) {
            mCameraModeViewPortrait.get().setVisibility(visible ? View.VISIBLE : View.GONE);
        }
        if(mCameraModeViewLandscape.get() != null) {
            mCameraModeViewLandscape.get().setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * {@link android.hardware.Camera.PreviewCallback#onPreviewFrame}으로 입력받은 yuv data를 분석하여 자동 보정
     * 모드를 설정한다.
     * 
     * @param data yuv byte array
     * @param camera {@link android.hardware.Camera}
     * @return yuv data 저장 여부
     * @version 2.0
     */
    public void onPreviewFrame(byte[] data, Camera camera) {
        mCounter--;
        if(mCounter <= 0) {
            try {
                setPreviewSize(camera.getParameters().getPreviewSize());
                WorkQueue.getInstance().execute(new AnalysisPreviewRunnable(data));
            } catch(RuntimeException e) {
                // Method called after release()
            }
        }
    }

    private class AnalysisPreviewRunnable extends PoolWorkerRunnable {
        private byte[] mData;

        public AnalysisPreviewRunnable(byte[] data) {
            super("AnalysisPreviewRunnable");
            mData = data;
        }

        @Override
        public void run() {
            getBrightness(mData);
            mData = null;
        }
    }

    /**
     * 자동 보정 모드에 따라 필터 효과를 설정하기 위한
     * {@link com.kiwiple.imageframework.filter.live#LiveFilterController}를 설정한다.
     * 
     * @param liveFilterController
     * @version 2.0
     */
    public void setLiveFilterController(LiveFilterController liveFilterController) {
        mLiveFilterController = liveFilterController;
    }

    /**
     * 자동 보정 모드를 표시할 {@link android.widget#ImageView}를 설정한다.
     * 
     * @param portraitView 가로 UI에서의 자동 보정 모드 표시
     * @param landscapeView 세로 UI에서의 자동 보정 모드 표시
     * @version 2.0
     */
    public void setCameraModeView(ImageView portraitView, ImageView landscapeView) {
        mCameraModeViewPortrait = new WeakReference<ImageView>(portraitView);
        mCameraModeViewLandscape = new WeakReference<ImageView>(landscapeView);
    }

    private void setPreviewSize(Camera.Size size) {
        mPreviewSize = size;
    }

    private boolean getBrightness(byte[] pix) {
        if(mFrontCamera || mNumOfFaces > 0) {
            return setCameraMode(MODE_FACE);
        }
        boolean handled = false;
        
        // 영역별 현재 밝기 값 계산
        final float brightness = AutoIntelligentUtils.getCurrentBrightness(pix, 0,
                                                                           mPreviewSize.width,
                                                                           mPreviewSize.height);
        final float[] patialBrightness = new float[] {
                AutoIntelligentUtils.getCurrentBrightness(pix, 1, mPreviewSize.width,
                                                          mPreviewSize.height),
                AutoIntelligentUtils.getCurrentBrightness(pix, 2, mPreviewSize.width,
                                                          mPreviewSize.height),
                AutoIntelligentUtils.getCurrentBrightness(pix, 3, mPreviewSize.width,
                                                          mPreviewSize.height),
                AutoIntelligentUtils.getCurrentBrightness(pix, 4, mPreviewSize.width,
                                                          mPreviewSize.height),
                AutoIntelligentUtils.getCurrentBrightness(pix, 5, mPreviewSize.width,
                                                          mPreviewSize.height)
        };

        // 밝기 값 변경 폭이 작으면 스킵
        if(Math.abs(mLastBrightness - brightness) < 10
                && Math.abs(mPatialBrightness[0] - patialBrightness[0]) < 10
                && Math.abs(mPatialBrightness[1] - patialBrightness[1]) < 10
                && Math.abs(mPatialBrightness[2] - patialBrightness[2]) < 10
                && Math.abs(mPatialBrightness[3] - patialBrightness[3]) < 10
                && Math.abs(mPatialBrightness[4] - patialBrightness[4]) < 10) {
            return false;
        }

        // 현재 밝기값, 현재 시간을 기준으로 모드를 변경한다.
        float highScore = AutoIntelligentUtils.isHighBrightness(brightness, false);
        float highPatialScore = AutoIntelligentUtils.max(AutoIntelligentUtils.isHighBrightness(patialBrightness[0],
                                                                                               true),
                                                         AutoIntelligentUtils.isHighBrightness(patialBrightness[1],
                                                                                               true),
                                                         AutoIntelligentUtils.isHighBrightness(patialBrightness[2],
                                                                                               true),
                                                         AutoIntelligentUtils.isHighBrightness(patialBrightness[3],
                                                                                               true));
        float lowScore = AutoIntelligentUtils.isLowBrightness(brightness, false);
        float maxScore = AutoIntelligentUtils.max(highScore, highPatialScore, lowScore);
        if(brightness < 100
                && (AutoIntelligentUtils.isSunLight(patialBrightness[0], patialBrightness[2])
                        || AutoIntelligentUtils.isSunLight(patialBrightness[1], patialBrightness[3]) || AutoIntelligentUtils.isSunLight(patialBrightness[4],
                                                                                                                                        brightness))) {
            int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            if(hour > 9 && hour < 18) {
                // sun light
                handled = setCameraMode(MODE_DAYP);
            } else {
                // moon light
                handled = setCameraMode(MODE_NIGHTP);
            }
        } else if(maxScore == 0) {
            // 중간
            handled = setCameraMode(MODE_AUTO);
        } else if(maxScore == highScore
                && AutoIntelligentUtils.isHighBrightness(patialBrightness[4], true) != 0
                && AutoIntelligentUtils.isHighBrightness(patialBrightness[3], true) != 0
                && AutoIntelligentUtils.isHighBrightness(patialBrightness[2], true) != 0
                && AutoIntelligentUtils.isHighBrightness(patialBrightness[1], true) != 0
                && AutoIntelligentUtils.isHighBrightness(patialBrightness[0], true) != 0) {
            // 전체 밝음
            handled = setCameraMode(MODE_DAY);
        } else if(maxScore == lowScore
                && AutoIntelligentUtils.isLowBrightness(patialBrightness[4], true) != 0
                && AutoIntelligentUtils.isLowBrightness(patialBrightness[3], true) != 0
                && AutoIntelligentUtils.isLowBrightness(patialBrightness[2], true) != 0
                && AutoIntelligentUtils.isLowBrightness(patialBrightness[1], true) != 0
                && AutoIntelligentUtils.isLowBrightness(patialBrightness[0], true) != 0) {
            // 전체 어두움
            handled = setCameraMode(MODE_NIGHT);
        } else if(brightness > 70 && brightness < 120) {
            handled = setCameraMode(MODE_AUTO);
        }

        // 모드가 변경 되었으면 60 프레임 후에, 아니면 10 프레임 후에 다시 밝기를 측정한다.
        if(handled) {
            mLastBrightness = brightness;
            mPatialBrightness = patialBrightness;
            mCounter = 60;
        } else {
            mCounter = 10;
        }
        return handled;
    }

    /**
     * 전면 카메라 여부를 설정한다.
     * 
     * @param isFrontCamera 전면 카메라 여부
     * @version 2.0
     */
    public void setFrontCamera(boolean isFrontCamera) {
        mFrontCamera = isFrontCamera;
        if(mFrontCamera) {
            setCameraMode(MODE_FACE);
        }
    }

    /**
     * 카메라 프리뷰에서 인식된 얼굴의 개수를 설정한다.
     * 
     * @param faceCount 얼굴의 개수
     * @version 2.0
     */
    public void setFaceCount(int faceCount) {
        mNumOfFaces = faceCount;
        if(mNumOfFaces > 0) {
            setCameraMode(MODE_FACE);
        }
    }

    private boolean setCameraMode(int mode) {
        if(mCamMode != mode) {
            mCamMode = mode;
            setLuminance();
            return true;
        }
        return false;
    }

    /**
     * 현재 모드에 따라 필터를 생성한다.
     */
    private void setLuminance() {
        mFilter = new Filter();
        mFilter.mRed = new ArrayList<CurvesPoint>();
        mFilter.mGreen = new ArrayList<CurvesPoint>();
        mFilter.mBlue = new ArrayList<CurvesPoint>();
        switch(mCamMode) {
            case MODE_DAYP:
                mFilter.mAll = new ArrayList<CurvesPoint>();
                mFilter.mAll.add(new CurvesPoint(0, 0));
                mFilter.mAll.add(new CurvesPoint(20, 30));
                mFilter.mAll.add(new CurvesPoint(240, 230));
                mFilter.mAll.add(new CurvesPoint(255, 255));
                break;
            case MODE_DAY:
                mFilter.mAll = new ArrayList<CurvesPoint>();
                mFilter.mAll.add(new CurvesPoint(0, 0));
                mFilter.mAll.add(new CurvesPoint(127, 100));
                mFilter.mAll.add(new CurvesPoint(255, 255));
                break;
            case MODE_AUTO:
                break;
            case MODE_NIGHTP:
                mFilter.mAll = new ArrayList<CurvesPoint>();
                mFilter.mAll.add(new CurvesPoint(0, 0));
                mFilter.mAll.add(new CurvesPoint(20, 30));
                mFilter.mAll.add(new CurvesPoint(240, 230));
                mFilter.mAll.add(new CurvesPoint(255, 255));
                break;
            case MODE_NIGHT:
                mFilter.mAll = new ArrayList<CurvesPoint>();
                mFilter.mAll.add(new CurvesPoint(0, 0));
                mFilter.mAll.add(new CurvesPoint(127, 150));
                mFilter.mAll.add(new CurvesPoint(255, 255));
                break;
            case MODE_FACE:
                mFilter.mAll = new ArrayList<CurvesPoint>();
                mFilter.mAll.add(new CurvesPoint(0, 0));
                mFilter.mAll.add(new CurvesPoint(160, 180));
                mFilter.mAll.add(new CurvesPoint(255, 255));
                mFilter.mArtFilter.mFilterName = "Soft";
                mFilter.mArtFilter.mParamCount = 2;
                mFilter.mArtFilter.mParams.add("30");
                mFilter.mArtFilter.mParams.add("40");
                break;
            default:
                break;
        }
        applyAutoIntelligent();
    }

    /**
     * 필터를 적용하고, UI를 업데이트해준다.
     */
    private void applyAutoIntelligent() {
        mUiUpdatehandler.post(new Runnable() {
            @Override
            public void run() {
                if(mLiveFilterController != null) {
                    mLiveFilterController.setFilterData(mFilter);
                }
                switch(mCamMode) {
                // 해님
                    case MODE_DAYP:
                        if(mCameraModeViewPortrait.get() != null) {
                            mCameraModeViewPortrait.get().setImageResource(FileUtils.getBitmapResourceId(mContext,
                                                                                                   "icon_mode_01"));
                        }
                        if(mCameraModeViewLandscape.get() != null) {
                            mCameraModeViewLandscape.get().setImageResource(FileUtils.getBitmapResourceId(mContext,
                                                                                                    "icon_mode_01"));
                        }
                        break;
                    // 형광등
                    case MODE_DAY:
                        if(mCameraModeViewPortrait.get() != null) {
                            mCameraModeViewPortrait.get().setImageResource(FileUtils.getBitmapResourceId(mContext,
                                                                                                   "icon_mode_04"));
                        }
                        if(mCameraModeViewLandscape.get() != null) {
                            mCameraModeViewLandscape.get().setImageResource(FileUtils.getBitmapResourceId(mContext,
                                                                                                    "icon_mode_04"));
                        }
                        break;
                    // 자동
                    case MODE_AUTO:
                        if(mCameraModeViewPortrait.get() != null) {
                            mCameraModeViewPortrait.get().setImageResource(FileUtils.getBitmapResourceId(mContext,
                                                                                                   "icon_mode_00"));
                        }
                        if(mCameraModeViewLandscape.get() != null) {
                            mCameraModeViewLandscape.get().setImageResource(FileUtils.getBitmapResourceId(mContext,
                                                                                                    "icon_mode_00"));
                        }
                        break;
                    // 달님
                    case MODE_NIGHTP:
                        if(mCameraModeViewPortrait.get() != null) {
                            mCameraModeViewPortrait.get().setImageResource(FileUtils.getBitmapResourceId(mContext,
                                                                                                   "icon_mode_02"));
                        }
                        if(mCameraModeViewLandscape.get() != null) {
                            mCameraModeViewLandscape.get().setImageResource(FileUtils.getBitmapResourceId(mContext,
                                                                                                    "icon_mode_02"));
                        }
                        break;
                    // 형광등
                    case MODE_NIGHT:
                        if(mCameraModeViewPortrait != null) {
                            mCameraModeViewPortrait.get().setImageResource(FileUtils.getBitmapResourceId(mContext,
                                                                                                   "icon_mode_04"));
                        }
                        if(mCameraModeViewLandscape.get() != null) {
                            mCameraModeViewLandscape.get().setImageResource(FileUtils.getBitmapResourceId(mContext,
                                                                                                    "icon_mode_04"));
                        }
                        break;
                    // 사람
                    case MODE_FACE:
                        if(mCameraModeViewPortrait.get() != null) {
                            mCameraModeViewPortrait.get().setImageResource(FileUtils.getBitmapResourceId(mContext,
                                                                                                   "icon_mode_03"));
                        }
                        if(mCameraModeViewLandscape.get() != null) {
                            mCameraModeViewLandscape.get().setImageResource(FileUtils.getBitmapResourceId(mContext,
                                                                                                    "icon_mode_03"));
                        }
                        break;
                    default:
                        if(mCameraModeViewPortrait.get() != null) {
                            mCameraModeViewPortrait.get().setImageResource(FileUtils.getBitmapResourceId(mContext,
                                                                                                   "icon_mode_00"));
                        }
                        if(mCameraModeViewLandscape.get() != null) {
                            mCameraModeViewLandscape.get().setImageResource(FileUtils.getBitmapResourceId(mContext,
                                                                                                    "icon_mode_00"));
                        }
                        break;
                }
            }
        });
    }
}
