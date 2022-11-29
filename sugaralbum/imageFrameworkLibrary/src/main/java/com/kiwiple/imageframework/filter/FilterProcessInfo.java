
package com.kiwiple.imageframework.filter;

import android.graphics.Bitmap;

import com.kiwiple.imageframework.filter.FilterManager.FilterProcessListener;

class FilterProcessInfo {
    public FilterData mFilterData;
    /**
     * 필터 처리 결과를 반환해줄 Callback listener
     */
    public FilterProcessListener mListener;
    /**
     * 필터 처리 타입.
     * 
     * @see {@link com.kiwiple.imageframework.filter.FilterManagerWrapper#PICTURE_TYPE}<br>
     *      {@link com.kiwiple.imageframework.filter.FilterManagerWrapper#THUMBNAIL_TYPE}<br>
     *      {@link com.kiwiple.imageframework.filter.FilterManagerWrapper#PREVIEW_TYPE}<br>
     *      {@link com.kiwiple.imageframework.filter.FilterManagerWrapper#DIRECT_BITMAP_REQUEST_TYPE} <br>
     *      {@link com.kiwiple.imageframework.filter.FilterManagerWrapper#DIRECT_FILE_REQUEST_TYPE}<br>
     */
    public int mFilterType = -1;
    /**
     * 필터 처리 결과 이미지 크기
     */
    public int mSize;
    /**
     * 필터 처리 결과 캐쉬 여부
     */
    public boolean mCacheable = false;
    /**
     * 필터 처리 결과 이미지
     */
    public Bitmap mResultBitmap;
    /**
     * 필터 처리 결과 이미지 파일
     */
    public String mResultFilePath;
    /**
     * 필터 처리를 요청한 객체 확이용
     */
    public Object mUserInfo;
    /**
     * 스티커 이미지 파일
     */
    public String mStickerImageFilePath;

    public FilterProcessInfo(FilterData mFilterData, FilterProcessListener mListener,
            int mFilterType, int mSize) {
        super();
        this.mFilterData = mFilterData;
        this.mListener = mListener;
        this.mFilterType = mFilterType;
        this.mSize = mSize;
    }

    public int getSize() {
        return mSize;
    }

    public FilterData getFilterData() {
        return mFilterData;
    }

    public void setCacheable(boolean cacheable) {
        mCacheable = cacheable;
    }

    public boolean isCacheable() {
        return mCacheable;
    }

    public boolean isValid() {
        if(mFilterData == null || mFilterData.mFilter == null || mFilterType == -1
                || (mFilterType == FilterManagerWrapper.PICTURE_TYPE && mSize == 0)) {
            return false;
        }
        return true;
    }
}
