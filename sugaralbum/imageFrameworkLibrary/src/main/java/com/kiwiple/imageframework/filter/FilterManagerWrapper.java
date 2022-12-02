
package com.kiwiple.imageframework.filter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.text.TextUtils;

import com.kiwiple.imageframework.util.BitmapUtils;
import com.kiwiple.imageframework.util.CacheUtils;
import com.kiwiple.imageframework.util.FilteredThumbnailCache;
import com.kiwiple.imageframework.util.SmartLog;

/**
 * FilterManager를 확장 개발한 class
 * 외부 library로 배포할 때 modifier를 default로 설정하여 확장된 기능은 hide하기 위해서 만듬.
 */
public class FilterManagerWrapper extends FilterManager {
    /**
     * 원본 이미지에 대한 필터 처리 요청시 {@link com.kiwiple.imageframework.filter.FilterManager#mOriginalFile}에 필터를 적용한다.
     */
    public static final int PICTURE_TYPE = 1;
    /**
     * 썸네일 이미지에 대한 필터 처리 요청시 {@link com.kiwiple.imageframework.filter.FilterManager#mThumb}에 필터를 적용한다.
     */
    public static final int THUMBNAIL_TYPE = 2;
    /**
     * 썸네일 이미지에 대한 필터 처리 요청시 {@link com.kiwiple.imageframework.filter.FilterManager#mPreview}에 필터를 적용한다.
     */
    public static final int PREVIEW_TYPE = 3;
    /**
     * Bitmap을 전달하여 필터 처리를 요청한다.
     */
    public static final int DIRECT_BITMAP_REQUEST_TYPE = 4;
    /**
     * File을 전달하여 필터 처리를 요청한다.
     */
    public static final int DIRECT_FILE_REQUEST_TYPE = 5;

    /**
     * 필터 id 1000번 미만은 preload이고, 이상은 마켓.
     */
    public static final int DOWNLOAD_FILTER_ID_START = 1000;
    public static final int FIRST_ID = 1; // Original Image

    // 생성할 필터 이미지의 크기
    static int THUMBNAIL_SIZE = 90;
    static int PREVIEW_SIZE = 320;
    static {
        if(Build.VERSION.SDK_INT >= 14) {
            THUMBNAIL_SIZE = 180;
            PREVIEW_SIZE = 1024;
        } else if(Build.VERSION.SDK_INT < 9) {
            THUMBNAIL_SIZE = 70;
            PREVIEW_SIZE = 200;
        }
    }

    protected FilterManagerWrapper(Context applicationContext) {
        super(applicationContext);
    }

    public synchronized static FilterManagerWrapper getInstance(Context applicationContext) {
        if(sInstance == null) {
            sInstance = new FilterManagerWrapper(applicationContext);
        }
        sInstance.bindService();
        if(sInstance.mGlobalContext == null) {
            sInstance.mGlobalContext = applicationContext;
        }
        return (FilterManagerWrapper)sInstance;
    }

    // 필터 카메라
    public boolean existDownloadFilter() {
        if(mFilterList == null) {
            return false;
        }
        for(FilterData data : mFilterList) {
            if(data.mServerId >= DOWNLOAD_FILTER_ID_START) {
                return true;
            }
        }
        return false;
    }

    /**
     * 이미 다운로드 받은 필터인지 확인
     */
    public boolean isDownloadFilter(int serverId) {
        for(FilterData data : mFilterList) {
            if(data.mServerId == serverId) {
                return true;
            }
        }
        return false;
    }

    /**
     * 즐겨찾기 등록된 필터가 있는지 확인
     */
    public boolean existFavoriteFilter() {
        if(mFilterList == null) {
            return false;
        }
        for(FilterData data : mFilterList) {
            if(data.mFavorite) {
                return true;
            }
        }
        return false;
    }

    /**
     * 즐겨찾기 on/off 설정
     */
    public boolean toggleFavoriteFilterData(int serverId) {
        for(FilterData data : mFilterList) {
            if(data.mServerId == serverId) {
                data.mFavorite = !data.mFavorite;
                if(!data.mFavorite) {
                    data.mFavoriteOrder = -1;
                } else {
                    data.mFavoriteOrder = findLastFavoriteOrderingNumber() + 1;
                }
                return data.mFavorite;
            }
        }
        return false;
    }

    /**
     * 필터목록에 필터가 있는지 확인
     */
    public boolean existFilterData(int serverId) {
        for(FilterData data : mFilterList) {
            if(data.mServerId == serverId) {
                return true;
            }
        }
        return false;
    }

    /**
     * 필터를 적용하기 위한 이미지 등록, 등록된 이미지는 요청의 종류에 따라 사용된다.
     * 
     * @see {@link #PICTURE_TYPE}<br>
     *      {@link #PREVIEW_TYPE}<br>
     *      {@link #THUMBNAIL_TYPE}<br>
     */
    public void setImages(String filename, Bitmap bitmap) {
        cancelAllApplyFilter();
        removeImages();

        mOriginalFile = filename;
        mThumb = BitmapUtils.resizeBitmap(bitmap,
                                          BitmapUtils.getRatioBitmapWidth(bitmap, THUMBNAIL_SIZE),
                                          BitmapUtils.getRatioBitmapHeight(bitmap, THUMBNAIL_SIZE));
        mPreview = BitmapUtils.resizeBitmap(bitmap,
                                            BitmapUtils.getRatioBitmapWidth(bitmap, PREVIEW_SIZE),
                                            BitmapUtils.getRatioBitmapHeight(bitmap, PREVIEW_SIZE));
    }

    /**
     * 등록된 이미지 리소스를 해제, 저장된 캐시 이미지를 삭제한다.
     */
    public void removeImages() {
        if(mThumb != null) {
            mThumb.recycle();
            mThumb = null;
        }
        if(mPreview != null) {
            mPreview.recycle();
            mPreview = null;
        }
        mOriginalFile = null;
        CacheUtils.deleteCacheFileAll(mGlobalContext);
        FilteredThumbnailCache.getInstance().clear();
        System.gc();
    }

    /**
     * 등록된 프리뷰 이미지를 반환한다.
     */
    public Bitmap getPriviewBitmap() {
        return mPreview;
    }

    /**
     * base 필터 목록과 {@link com.kiwiple.imageframework.filter.FilterManager#mFilterList}를 merge 시켜준다.
     */
    public boolean mergeFilter(ArrayList<FilterData> base) {
        boolean changedFilter = false;
        boolean hasData;
        int favoriteOrder = findLastFavoriteOrderingNumber() + 1;
        int presetOrder = findLastPresetOrderingNumber() + 1;
        int downloadOrder = findLastDownlaodOrderingNumber() + 1;
        for(FilterData target : mFilterList) {
            hasData = false;
            for(FilterData data : base) {
                if(data.mServerId == target.mServerId) {
                    hasData = true;
                    changedFilter = true;
                    break;
                }
            }
            if(!hasData) {
                base.add(mFilterList.indexOf(target), target);
                if(target.mFavorite) {
                    target.mFavoriteOrder = favoriteOrder++;
                }
                if(target.mServerId < DOWNLOAD_FILTER_ID_START) {
                    target.mPresetOrder = presetOrder++;
                } else {
                    target.mDownloadOrder = downloadOrder++;
                }
            }
        }
        mFilterList = base;
        return changedFilter;
    }

    /**
     * 필터 목록을 반환한다.
     */
    public ArrayList<FilterData> getFilterData() {
        return mFilterList;
    }

    /** 마켓 필터 다운로드 완료. */
    public void addFilterData(FilterData data) {
        if(data != null) {
            if(!existFilterData(data.mServerId)) {
                data.mDownloadOrder = findLastDownlaodOrderingNumber() + 1;
                mFilterList.add(data);
            }
        }
    }

    /**
     * 마지막 즐겨찾기 필터 순서번호를 반환한다.
     */
    private int findLastFavoriteOrderingNumber() {
        int order = -1;
        for(FilterData filterData : mFilterList) {
            if(filterData.mFavoriteOrder > order) {
                order = filterData.mFavoriteOrder;
            }
        }
        return order;
    }

    /**
     * 마지막 기본 필터 순서번호를 반환한다.
     */
    private int findLastPresetOrderingNumber() {
        int order = -1;
        for(FilterData filterData : mFilterList) {
            if(filterData.mPresetOrder > order) {
                order = filterData.mPresetOrder;
            }
        }
        return order;
    }

    /**
     * 마지막 다운로드 필터 순서번호를 반환한다.
     */
    private int findLastDownlaodOrderingNumber() {
        int order = -1;
        for(FilterData filterData : mFilterList) {
            if(filterData.mDownloadOrder > order) {
                order = filterData.mDownloadOrder;
            }
        }
        return order;
    }

    /**
     * 필터를 삭제한다.
     */
    public void removeFilterData(FilterData data) {
        mFilterList.remove(data);
    }

    /**
     * 필터 순서를 변경한다.
     * type 0: favorite 1: preset 2: download
     */
    public void changeFilterOrder(FilterData from, FilterData to, int type) {
        int fromOrder;
        int toOrder;
        switch(type) {
            case 0:
                fromOrder = from.mFavoriteOrder;
                toOrder = to.mFavoriteOrder;
                for(FilterData filterData : mFilterList) {
                    if(fromOrder < toOrder && filterData.mFavoriteOrder <= toOrder
                            && filterData.mFavoriteOrder > fromOrder) {
                        filterData.mFavoriteOrder--;
                    } else if(fromOrder > toOrder && filterData.mFavoriteOrder >= toOrder
                            && filterData.mFavoriteOrder < fromOrder) {
                        filterData.mFavoriteOrder++;
                    }
                }
                from.mFavoriteOrder = toOrder;
                break;
            case 1:
                fromOrder = from.mPresetOrder;
                toOrder = to.mPresetOrder;
                for(FilterData filterData : mFilterList) {
                    if(fromOrder < toOrder && filterData.mPresetOrder <= toOrder
                            && filterData.mPresetOrder > fromOrder) {
                        filterData.mPresetOrder--;
                    } else if(fromOrder > toOrder && filterData.mPresetOrder >= toOrder
                            && filterData.mPresetOrder < fromOrder) {
                        filterData.mPresetOrder++;
                    }
                }
                from.mPresetOrder = toOrder;
                break;
            case 2:
                fromOrder = from.mDownloadOrder;
                toOrder = to.mDownloadOrder;
                for(FilterData filterData : mFilterList) {
                    if(fromOrder < toOrder && filterData.mDownloadOrder <= toOrder
                            && filterData.mDownloadOrder > fromOrder) {
                        filterData.mDownloadOrder--;
                    } else if(fromOrder > toOrder && filterData.mDownloadOrder >= toOrder
                            && filterData.mDownloadOrder < fromOrder) {
                        filterData.mDownloadOrder++;
                    }
                }
                from.mDownloadOrder = toOrder;
                break;
        }
        for(FilterData filterData : mFilterList) {
            SmartLog.d(TAG, String.format("Id:%d,\tFavorate:%d,\t Preset:%d,\t Download:%d\t",
                                          filterData.mServerId, filterData.mFavoriteOrder,
                                          filterData.mPresetOrder, filterData.mDownloadOrder));
        }
    }

    public void createFilterImage(int filterId, int filterType, int size,
            String stickerImageFilePath, FilterProcessListener listener, Object userInfo) {
        FilterData data = getFilterData(filterId);
        if(data == null) {
            listener.onFailureFilterProcess(filterId, userInfo);
            return;
        }
        FilterProcessInfo filterInfo = new FilterProcessInfo(data, listener, filterType, size);
        filterInfo.mStickerImageFilePath = stickerImageFilePath;
        filterInfo.mUserInfo = userInfo;
        if(filterInfo.mFilterData.mServerId == FIRST_ID && TextUtils.isEmpty(stickerImageFilePath)) {
            if(filterInfo.mFilterType == THUMBNAIL_TYPE) {
                filterInfo.mResultBitmap = mThumb;
            } else if(filterInfo.mFilterType == PREVIEW_TYPE) {
                filterInfo.mResultBitmap = mPreview;
            } else if(filterInfo.mFilterType == PICTURE_TYPE) {
                filterInfo.mResultFilePath = mOriginalFile;
            }
        } else {
            if(filterInfo.mFilterType == PREVIEW_TYPE) {
                filterInfo.mResultBitmap = CacheUtils.readCacheFile(mGlobalContext, CACHE_FILE_NAME
                        + filterInfo.mFilterData.mServerId);
                filterInfo.mCacheable = true;
            } else if(filterInfo.mFilterType == THUMBNAIL_TYPE) {
                filterInfo.mResultBitmap = FilteredThumbnailCache.getInstance()
                                                                 .get(filterInfo.mFilterData.mServerId);
            }
        }
        if(!TextUtils.isEmpty(filterInfo.mResultFilePath) || (filterInfo.mResultBitmap != null)) {
            // not use handler for quickly load
            if(filterInfo.mListener != null) {
                if(filterInfo.mResultBitmap == null
                        && TextUtils.isEmpty(filterInfo.mResultFilePath)) {
                    filterInfo.mListener.onFailureFilterProcess(filterInfo.mFilterData.mServerId,
                                                                filterInfo.mUserInfo);
                } else {
                    filterInfo.mListener.onCompleteFilterProcess(filterInfo.mResultBitmap,
                                                                 filterInfo.mResultFilePath,
                                                                 filterInfo.mFilterData.mServerId,
                                                                 filterInfo.mUserInfo);
                }
            }
            filterInfo.mListener = null;
            filterInfo.mResultBitmap = null;
            filterInfo.mUserInfo = null;
            filterInfo.mStickerImageFilePath = null;
            filterInfo = null;
        } else {
            sFilteringThread.addFilterData(filterInfo);
        }
    }

    /**
     * 필터 목록을 재정렬한다.
     * type 0: favorite 1: preset 2: download
     */
    public void sortFilterList(ArrayList<FilterData> filterList, int type) {
        switch(type) {
            case 0:
                Collections.sort(filterList, filterFavoriteComparator);
                break;
            case 1:
                Collections.sort(filterList, filterPresetComparator);
                break;
            case 2:
                Collections.sort(filterList, filterDownloadComparator);
                break;
        }
    }

    private final Comparator<FilterData> filterFavoriteComparator = new Comparator<FilterData>() {
        @Override
        public int compare(FilterData object1, FilterData object2) {
            return object1.mFavoriteOrder - object2.mFavoriteOrder;
        }
    };
    private final Comparator<FilterData> filterPresetComparator = new Comparator<FilterData>() {
        @Override
        public int compare(FilterData object1, FilterData object2) {
            return object1.mPresetOrder - object2.mPresetOrder;
        }
    };
    private final Comparator<FilterData> filterDownloadComparator = new Comparator<FilterData>() {
        @Override
        public int compare(FilterData object1, FilterData object2) {
            return object1.mDownloadOrder - object2.mDownloadOrder;
        }
    };

    @Override
    public void release() {
        removeImages();
        super.release();
    }
}
