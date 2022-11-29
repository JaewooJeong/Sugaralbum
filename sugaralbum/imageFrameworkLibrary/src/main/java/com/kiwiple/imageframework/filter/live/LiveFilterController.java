
package com.kiwiple.imageframework.filter.live;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.renderscript.RenderScript;
import android.view.View;

import com.kiwiple.imageframework.filter.Filter;
import com.kiwiple.imageframework.filter.FilterData;
import com.kiwiple.imageframework.filter.FilterManager;
import com.kiwiple.imageframework.filter.FilterManagerWrapper;
import com.kiwiple.imageframework.util.SmartLog;
import com.kiwiple.imageframework.view.CameraFilterView;

/**
 * 라이브 필터 효과를 적용하기 위한 class
 * 
 * @version 2.0
 */
public class LiveFilterController {
    private static LiveFilterController sInstance;
    private RenderScript mRS;
    private PreviewFiler mFilterYuv;
    /**
     * 필터 처리 결과가 display될 View
     */
    private CameraFilterView mOutputView;

    /**
     * RenderScript 초기화 여부 확인
     */
    private boolean mIsRenderscriptMode = false;

    /**
     * 현재 설정된 필터
     */
    private Filter mLastFilter;

    private Context mContext;

    public static LiveFilterController getInstance(Context context, boolean useNative) {
        if (sInstance == null) {
            sInstance = new LiveFilterController(context, useNative);
        }
        return sInstance;
    }

    /**
     * @param context
     * @param useNative true 설정시 OpenGL로 동작 한다.
     * @version 2.0
     */
    private LiveFilterController(Context context, boolean useNative) {
        mContext = context;
        if(!useNative) {
            try {
                mRS = RenderScript.create(context);
                mFilterYuv = new RsYuv(mRS, context.getApplicationContext());
                mIsRenderscriptMode = true;
            } catch(Exception e) {
                mFilterYuv = new RsYuvNative(mRS, context.getApplicationContext());
                mIsRenderscriptMode = false;
            }
        } else {
            mFilterYuv = new RsYuvNative(mRS, context.getApplicationContext());
            mIsRenderscriptMode = false;
        }

    }

    /**
     * 라이브 필터 효과가 출력될 {@link com.kiwiple.imageframework.view#CameraFilterView}를 설정한다.
     * 
     * @param view 라이브 필터 효과가 출력될 View
     * @version 2.0
     */
    public void setupView(CameraFilterView view) {
        mOutputView = view;
        mFilterYuv.setImageView(mOutputView);
        if(mOutputView != null && mFilterYuv.mOutputBitmap != null) {
            mOutputView.setFilterImage(mFilterYuv.mOutputBitmap);
        }
    }

    /**
     * 
     * @param passOriginal
     */
    public void setPassOriginal(boolean passOriginal) {
        mFilterYuv.setPassOriginal(passOriginal);
    }

    /**
     * 라이브 필터가 RenderScript로 동작하고 있는지 여부를 반환한다.
     * 
     * @return RenderScript 사용 여부
     * @version 2.0
     */
    public boolean isRenderScriptMode() {
        return mIsRenderscriptMode;
    }

    public PreviewFiler getPreviewFilter() {
        return mFilterYuv;
    }
    
    /**
     * 일부 단말에서는 특정 상황에 메인스레드를 사용해야하는 번거로움이 발생
     * 
     * @param isUseMainThread
     */
    public void setIsRenderScriptMainThread(boolean isUseMainThread) {
        if (mFilterYuv != null) {
            mFilterYuv.setIsRenderScriptMainThread(isUseMainThread);
        }
    }

    /**
     * 적용될 필터 효과를 설정한다.
     * 
     * @param filter
     * @version 2.0
     */
    public void setFilterData(Filter filter) {
        mLastFilter = filter;
        if(filter == null) {
            filter = new Filter();
        }
        mFilterYuv.setFilterData(filter);
    }

    public Filter getFilterData() {
        return mLastFilter;
    }

    private boolean isConsistanceFrameSize(int width, int height) {
        if((width != mFilterYuv.getWidth()) || (height != mFilterYuv.getHeight())) {
            return false;
        }
        return true;
    }

    private void reset(int width, int height) {
        mFilterYuv.reset(width, height);
    }

    private void execute(byte[] data) {
        mFilterYuv.execute(data);
    }

    /**
     * yuv data(byte[])의 이미지 회전 정보를 설정한다.
     * 
     * @param orientation 이미지 회전 정보 <br>
     *            (일반적으로 {@link android.hardware.Camera#setDisplayOrientation(int)}의 값과 동일하다.)
     * @version 2.0
     */
    public void setDisplayOrientation(int orientation) {
        mOutputView.setOrientation(orientation);
    }

    /**
     * 라이브 필터 효과가 적용된 이미지의 좌/우 반전 여부
     * 
     * @param flip true이면 좌/우가 반전된 이미지
     * @version 2.0
     */
    public void setFlip(boolean flip) {
        mOutputView.setFlip(flip);
    }

    /**
     * {@link android.hardware.Camera.PreviewCallback#onPreviewFrame}으로 입력받은 yuv data에 라이브 필터 효과를
     * 적용한다.
     * 
     * @param width yuv data의 가로 길이
     * @param height yuv data의 세로 길이
     * @param data yuv data
     * @param cameraId 카메라 프리뷰가 동작하고 있는 Camera의 id
     * @return 라이브 필터 적용 여부
     * @version 2.0
     */
    public boolean onPreviewFrame(int width, int height, byte[] data, int cameraId) {
        if(mLastFilter == null || !mIsRenderscriptMode) {
            if(mOutputView.getVisibility() != View.GONE) {
                mOutputView.setVisibility(View.GONE);
            }
            return false;
        }
        if(mOutputView.getVisibility() != View.VISIBLE) {
            mOutputView.setVisibility(View.VISIBLE);
        }

        if(!isConsistanceFrameSize(width, height)) {
            reset(width, height);
        }

        execute(data);

        setFlip(cameraId != 0);
        return true;
    }

    public synchronized void applyFilter(FilterManager filterManager, int id, Bitmap source, Bitmap dest) {
        if((mFilterYuv.getWidth() != source.getWidth())
                || (mFilterYuv.getHeight() != source.getHeight())) {
            mFilterYuv.reset(source.getWidth(), source.getHeight());
        }

        SmartLog.e("liveController", "id : " + id);
        SmartLog.e("liveController", "initailized : "
                + FilterManagerWrapper.getInstance(mContext).isInitialized());

        Filter filter = null;
        for(FilterData data : filterManager.getFilterdataList()) {
            if(data.mServerId == id) {
                filter = data.mFilter;
            }
        }

        SmartLog.e("liveController", "filterData : " + filter);

        mFilterYuv.setFilterData(filter);

        mFilterYuv.execute(source, dest);
    }

    public synchronized void applyFilter(FilterManager filterManager, Filter filter, Bitmap source, Bitmap dest) {
        if((mFilterYuv.getWidth() != source.getWidth())
                || (mFilterYuv.getHeight() != source.getHeight())) {
            mFilterYuv.reset(source.getWidth(), source.getHeight());
        }

        mFilterYuv.setFilterData(filter);

        mFilterYuv.execute(source, dest);
    }

    public synchronized void applyFilter(FilterManager filterManager, int id, int width, int height, int[] source, int[] dest) {
        if((mFilterYuv.getWidth() != width)
                || (mFilterYuv.getHeight() != height)) {
            mFilterYuv.reset(width, height);
        }

        SmartLog.e("liveController", "id : " + id);
        SmartLog.e("liveController", "initailized : "
                + FilterManagerWrapper.getInstance(mContext).isInitialized());

        Filter filter = null;
        for(FilterData data : filterManager.getFilterdataList()) {
            if(data.mServerId == id) {
                filter = data.mFilter;
            }
        }

        SmartLog.e("liveController", "filterData : " + filter);

        mFilterYuv.setFilterData(filter);

        mFilterYuv.execute(source, dest);
    }
}
