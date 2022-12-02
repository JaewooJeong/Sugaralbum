
package com.kiwiple.imageframework.filter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;

import com.kiwiple.imageframework.util.CacheUtils;
import com.kiwiple.imageframework.util.FileUtils;
import com.kiwiple.imageframework.util.FilteredThumbnailCache;
import com.kiwiple.imageframework.util.SmartLog;

/**
 * 필터 목록 관리 및 필터 적용 프로세스를 담당하는 class. <br>
 * {@link #getInstance} 함수로 instance를 생성하고 singleton으로 동작한다.
 * 
 * @version 2.0
 */
public class FilterManager {
    protected static final String TAG = FilterManager.class.getSimpleName();

    protected static final String CACHE_FILE_NAME = "PREVIEW_";
    protected static final String FILTER_LIST = "FilterList";

    protected static FilterManager sInstance;
    protected static FilterProcess sFilteringThread;

    protected Context mGlobalContext;

    protected IFilterServiceLgu mIFilterService;
    protected NotifyHandler mHandler = new NotifyHandler();

    protected boolean mInitialized = false;

    /**
     * 필터 목록
     */
    protected ArrayList<FilterData> mFilterList = new ArrayList<FilterData>();
    /**
     * U+Camera에서 사용. 프레임 정보만 가지고 있는 필터.
     */
    protected ArrayList<FilterData> mFrameFilterList = new ArrayList<FilterData>();

    // 필터 카메라 용.
    protected Bitmap mThumb;
    protected Bitmap mPreview;
    protected String mOriginalFile;

    /** The maximum amount of time to wait (milliseconds) for a successful binding to the service */
    private static final int SERVICE_CONNECTION_TIMEOUT = 5 * 1000;
    /** Latch used to wait for connection */
    private CountDownLatch mServiceConnectLatch;
    /** Used to synchronize */
    private final Object mServiceLock = new Object();

    /** Used to synchronize */

    protected FilterManager() {
    }

    protected FilterManager(Context applicationContext) {
        clearThread();
        sFilteringThread = new FilterProcess();
        sFilteringThread.start();
        mGlobalContext = applicationContext;
        mInitialized = false;
    }

    /**
     * @param applicationContext
     * @return {@link #FilterManager}의 인스턴스 반환
     * @version 1.0
     */
    public synchronized static FilterManager getInstance(Context applicationContext) {
        if(sInstance == null) {
            sInstance = new FilterManager(applicationContext);
        }
        sInstance.bindService();
        if(sInstance.mGlobalContext == null) {
            sInstance.mGlobalContext = applicationContext;
        }
        return sInstance;
    }

    /**
     * 필터 서비스에 바인딩 시킨다.
     */
    protected void bindService() {
        synchronized(mServiceLock) {
            if(!isServiceBound()) {
                mServiceConnectLatch = new CountDownLatch(1);
                // Service intent must be explicit from LOLLIPOP
                Intent serviceIntent = new Intent(IFilterServiceLgu.class.getName());
                serviceIntent.setPackage("com.sugarmount.sugarcamera");
                //if(!mGlobalContext.bindService(new Intent(IFilterServiceLgu.class.getName()),
                if(!mGlobalContext.bindService(serviceIntent,
                                               serviceConnection, Context.BIND_AUTO_CREATE)) {
                    SmartLog.e(TAG, "Unable to bind to service:" + IFilterServiceLgu.class);
                    mServiceConnectLatch = null;
                } else {
                }
            } else {
            }
        }
    }

    /**
     * 필터 서비스에 바인드 될 때까지 기다리다.
     */
    private boolean waitForServiceConnected() {
        boolean bound;
        synchronized(mServiceLock) {
            if(isServiceBound()) {
                return true;
            }
            if(mServiceConnectLatch == null) {
                return false;
            }
            try {
                mServiceConnectLatch.await(SERVICE_CONNECTION_TIMEOUT, TimeUnit.MILLISECONDS);
            } catch(InterruptedException e) {
                SmartLog.e(TAG, "Connection interrupted", e);
            }
            if(mServiceConnectLatch.getCount() != 0) {
                SmartLog.e(TAG, "Failure waiting for service connection");
                bound = false;
            } else {
                SmartLog.i(TAG, "Bound " + IFilterServiceLgu.class + " to " + mIFilterService);
                bound = true;
            }
        }
        return bound;
    }

    /**
     * 필터 서비스에 바인드 되었는지 여부를 판단한다.
     */
    private boolean isServiceBound() {
        synchronized(mServiceLock) {
            return mIFilterService != null;
        }
    }

    protected ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            SmartLog.i(TAG, "Service" + IFilterServiceLgu.class + "disconnected");
            mIFilterService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SmartLog.i(TAG, "Service" + IFilterServiceLgu.class + "connected");
            mIFilterService = IFilterServiceLgu.Stub.asInterface(service);
            if(mServiceConnectLatch != null) {
                mServiceConnectLatch.countDown();
            }
        }
    };

    /**
     * @return 필터 목록이 초기화 되었으면 true 아니면 false
     * @version 1.0
     */
    public boolean isInitialized() {
        return mInitialized;
    }

    /**
     * JSON 형식의 필터 데이터 파일을 전달 받는 메소드입니다. <br>
     * 내부적으로 필터 데이터를 해석하여 필터 목록을 생성합니다. <br>
     * 일반 데이터 파일의 전체 경로를 기반으로 파일을 읽는 메소드입니다
     * 
     * @param dataPath 필터 목록 파일 경로
     * @throws IOException 필터 목록 파일을 불러오지 못했을 경우
     */
    public void setFilterFile(String dataPath) throws IOException {
        InputStream in = new FileInputStream(dataPath);
        setFilter(mFilterList, in);
    }

    /**
     * JSON 형식의 필터 데이터 파일을 전달 받는 메소드입니다. <br>
     * 내부적으로 필터 데이터를 해석하여 필터 목록을 생성합니다. <br>
     * 안드로이드 에셋리소스에 기반한 경로에서 파일을 읽는 메소드입니다.
     * 
     * @param assetPath 필터 목록 파일 경로(Asset 폴더에서의 경로)
     * @throws IOException 필터 목록 파일을 불러오지 못했을 경우
     * @version 1.0
     */
    public void setFilterAsset(String assetPath) throws IOException {
        InputStream in = mGlobalContext.getResources().getAssets().open(assetPath);
        setFilter(mFilterList, in);
    }

    /**
     * U+Camera에서 사용. 프레임 정보만 있는 필터 목록을 생성한다.
     * 
     * @param assetPath 필터 목록 파일 경로(Asset 폴더에서의 경로)
     * @throws IOException 필터 목록 파일을 불러오지 못했을 경우
     */
    public void setFrameFilterAsset(String assetPath) throws IOException {
        InputStream in = mGlobalContext.getResources().getAssets().open(assetPath);
        setFilter(mFrameFilterList, in);
    }

    /**
     * JSON 형식 문자열의 필터 데이타를 전달받아 파싱해 필터 목록을 생성합니다.
     * 
     * @param filterList 필터 목록 문자열
     * @throws IOException 필터 목록 파일을 불러오지 못했을 경우
     * @version 2.0
     */
    public void setFilterJsonString(String filterJsonString) throws IOException {
        mFilterList.clear();
        mInitialized = true;
        JsonParser jp = null;
        try {
            jp = new JsonFactory().createJsonParser(filterJsonString);
            parseFilterJson(mFilterList, jp);
        } finally {
            if(jp != null) {
                try {
                    jp.close();
                } catch(IOException e) {
                }
            }
        }
    }

    protected void setFilter(ArrayList<FilterData> filterList, InputStream in) throws IOException {
        filterList.clear();
        mInitialized = true;
        JsonParser jp = null;
        try {
            jp = new JsonFactory().createJsonParser(in);
            parseFilterJson(filterList, jp);
        } finally {
            if(in != null) {
                try {
                    in.close();
                } catch(IOException e) {
                }
            }
            if(jp != null) {
                try {
                    jp.close();
                } catch(IOException e) {
                }
            }
        }
    }

    private void parseFilterJson(ArrayList<FilterData> filterList, JsonParser jp)
            throws JsonParseException, IOException {
        jp.nextToken();
        while(jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();
            jp.nextToken();
            if(FILTER_LIST.equals(fieldName)) {
                while(jp.nextToken() != JsonToken.END_ARRAY) {
                    FilterData data = new FilterData();
                    data.parse(jp);
                    if(data.mFilter != null) {
                        filterList.add(data);
                    }
                }
            }
        }
    }

    /**
     * 이미 JSON 형식의 필터 데이타를 파싱해 전달된 필터 목록을 파라메타로 받아 설정하는 메소드입니다.
     * 
     * @param data 설정할 필터 목록
     * @version 2.0
     */
    public void setFilterData(ArrayList<FilterData> data) {
        if(mFilterList != null) {
            mFilterList.clear();
        } else {
            mFilterList = new ArrayList<FilterData>();
        }

        mFilterList.addAll(data);
    }

    /**
     * @return 필터 목록 반환
     * @version 1.0
     */
    public ArrayList<FilterInfo> getFilterArray() {
        ArrayList<FilterInfo> list = new ArrayList<FilterInfo>();
        for(FilterData data : mFilterList) {
            list.add(new FilterInfo(data.mTitle, data.mDescription, data.mServerId));
        }
        return list;
    }

    /**
     * 필터 매니저가 관리 중인 필터 전체를 얻습니다.
     * 
     * @return 필터 목록
     * @version 2.0
     */
    public ArrayList<FilterData> getFilterdataList() {
        if(mFilterList != null) {
            return mFilterList;
        }
        return new ArrayList<FilterData>();
    }

    /**
     * U+Camera에서 사용. 프레임 정보만 있는 필터 목록을 반환한다. 
     */
    public ArrayList<FilterData> getFrameFilterdataList() {
        if(mFrameFilterList != null) {
            return mFrameFilterList;
        }
        return new ArrayList<FilterData>();
    }

    /**
     * serverId에 해당하는 필터 반환.
     */
    protected FilterData getFilterData(int serverId) {
        for(FilterData data : mFilterList) {
            if(data.mServerId == serverId) {
                return data;
            }
        }
        for(FilterData data : mFrameFilterList) {
            if(data.mServerId == serverId) {
                return data;
            }
        }
        return null;
    }

    /**
     * 필터 목록을 초기화한다.
     */
    protected void clearFilterList() {
        if(mFilterList != null) {
            mFilterList.clear();
        }
    }

    /**
     * 필터링 된 결과를 전달 받기 위한 Listener
     * 
     * @version 1.0
     */
    public interface FilterProcessListener {
        /**
         * 필터링 된 결과에 대한 callback 함수
         * 
         * @param image 필터링 된 이미지(필터링이 취소 또는 실패한 경우 null)
         * @param filePath 사용하지 않음
         * @param filterId 필터 id
         * @param userInfo {@link FilterManager#applyFilterImage} 호출시 전달한 사용자 정의 class
         * @version 1.0
         */
        public void onCompleteFilterProcess(final Bitmap image, final String filePath,
                final int filterId, Object userInfo);

        /**
         * 필터 적용이 실패 했을 때의 callback 함수
         * 
         * @param filterId 필터 id
         * @param userInfo {@link FilterManager#applyFilterImage} 호출시 전달한 사용자 정의 class
         * @version 2.0
         */
        public void onFailureFilterProcess(final int filterId, Object userInfo);
    }

    /**
     * @param image 필터를 적용할 이미지
     * @param filterId 적용할 필터 id
     * @param listener 필터 적용 결과에 대한 callback
     * @param userInfo 사용자 정의 class로 {@link FilterProcessListener} callback 함수의 parameter로 전달된다.
     * @version 1.0
     */
    public void applyFilterImage(Bitmap image, int filterId, FilterProcessListener listener,
            Object userInfo) {
        FilterData data = getFilterData(filterId);
        if(data == null) {
            SmartLog.d(TAG, "Invalid filterId");
            listener.onFailureFilterProcess(filterId, userInfo);
            return;
        }
        FilterProcessInfo filterInfo = new FilterProcessInfo(
                                                             data,
                                                             listener,
                                                             FilterManagerWrapper.DIRECT_BITMAP_REQUEST_TYPE,
                                                             0);
        filterInfo.mResultBitmap = image;
        filterInfo.mUserInfo = userInfo;
        sFilteringThread.addFilterData(filterInfo);
    }

    /**
     * @param image 필터를 적용할 이미지
     * @param filter 적용할 필터
     * @param listener 필터 적용 결과에 대한 callback
     * @param userInfo 사용자 정의 class로 {@link FilterProcessListener} callback 함수의 parameter로 전달된다.
     */
    public void applyFilterImage(Bitmap image, Filter filter, FilterProcessListener listener,
            Object userInfo) {
        FilterData data = new FilterData();
        data.mFilter = filter;
        FilterProcessInfo filterInfo = new FilterProcessInfo(
                                                             data,
                                                             listener,
                                                             FilterManagerWrapper.DIRECT_BITMAP_REQUEST_TYPE,
                                                             0);
        filterInfo.mResultBitmap = image;
        filterInfo.mUserInfo = userInfo;
        sFilteringThread.addFilterData(filterInfo);
    }

    /**
     * @param path 필터를 적용할 이미지 경로
     * @param filterId 적용할 필터 id
     * @param listener 필터 적용 결과에 대한 callback
     * @param userInfo 사용자 정의 class로 {@link FilterProcessListener} callback 함수의 parameter로 전달된다.
     * @version 1.0
     */
    public void applyFilterImage(String path, int size, int filterId,
            FilterProcessListener listener, Object userInfo) {
        FilterData data = getFilterData(filterId);
        if(data == null) {
            SmartLog.d(TAG, "Invalid filterId");
            listener.onFailureFilterProcess(filterId, userInfo);
            return;
        }
        FilterProcessInfo filterInfo = new FilterProcessInfo(
                                                             data,
                                                             listener,
                                                             FilterManagerWrapper.DIRECT_FILE_REQUEST_TYPE,
                                                             0);
        filterInfo.mResultFilePath = path;
        filterInfo.mUserInfo = userInfo;
        filterInfo.mSize = size;
        sFilteringThread.addFilterData(filterInfo);
    }

    /**
     * @param path 필터를 적용할 이미지 경로
     * @param filter 적용할 필터
     * @param listener 필터 적용 결과에 대한 callback
     * @param userInfo 사용자 정의 class로 {@link FilterProcessListener} callback 함수의 parameter로 전달된다.
     */
    public void applyFilterImage(String path, int size, Filter filter,
            FilterProcessListener listener, Object userInfo) {
        FilterData data = new FilterData();
        data.mFilter = filter;
        FilterProcessInfo filterInfo = new FilterProcessInfo(
                                                             data,
                                                             listener,
                                                             FilterManagerWrapper.DIRECT_FILE_REQUEST_TYPE,
                                                             0);
        filterInfo.mResultFilePath = path;
        filterInfo.mUserInfo = userInfo;
        filterInfo.mSize = size;
        sFilteringThread.addFilterData(filterInfo);
    }

    /**
     * 동기식 필터 처리를 위한 함수로, main thread에서 호출 하면 안된다.
     * 
     * @param image 필터를 적용할 이미지
     * @param filterId 적용할 필터 id
     * @return 필터가 적용된 이미지
     * @throws RemoteException 필터 서비스 에러
     */
    public Bitmap applyFilterImage(Bitmap image, int filterId) throws RemoteException {
        if(!waitForServiceConnected()) {
            return null;
        }
        FilterData data = getFilterData(filterId);
        if(data == null || data.mFilter == null) {
            SmartLog.d(TAG, "Invalid filterId");
            return null;
        }

        SmartLog.d(TAG, "Processing Direct request image start: " + data.mTitle);
        // 이미지가 큰 경우 파일로 출력후 filter service에서 처리한다.
        Bitmap result = null;
        // prevent TransactionTooLargeException: max size 1MB
        if(image.getWidth() * image.getHeight() > 1024 * 800) {
            String tmpPath = new StringBuffer().append(mGlobalContext.getFilesDir()
                                                                     .getAbsolutePath())
                                               .append(File.separator)
                                               .append("filter_target_temp_image.jpg").toString();
            try {
                FileUtils.saveBitmap(image, tmpPath, Bitmap.CompressFormat.PNG);
                int size;
                if(image.getWidth() > image.getHeight()) {
                    size = image.getWidth();
                } else {
                    size = image.getHeight();
                }

                String fileName = mIFilterService.processingImageFile(tmpPath, size, data.mFilter,
                                                                      null);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inMutable = true;
                result = BitmapFactory.decodeFile(fileName, options);
            } catch(IOException e) {
            }
        } else {
            try {
                result = mIFilterService.processingImageBitmap(image, data.mFilter, null);
            } catch(DeadObjectException e) {
                // 서비스 process가 죽은 경우 다시 bind하도록 처리
                SmartLog.e(TAG, "DeadObjectException");
                mGlobalContext.unbindService(serviceConnection);
                mIFilterService = null;
                bindService();
                if(waitForServiceConnected()) {
                    if (mIFilterService != null) {
                        result = mIFilterService.processingImageBitmap(image, data.mFilter, null);    
                    } else {
                        SmartLog.e(TAG, "mIFilterService still Null");
                    }
                }
            }
        }
        SmartLog.d(TAG, "Processing Direct request image end: " + data.mTitle);
        return result;
    }

    /**
     * 동기식 필터 처리를 위한 함수로, main thread에서 호출 하면 안된다.
     * 
     * @param path 필터를 적용할 이미지 경로
     * @param size 이미지 최대 크기
     * @param filterId 적용할 필터 id
     * @return 필터가 적용된 이미지 경로
     * @throws RemoteException 필터 서비스 에러
     */
    public String applyFilterImage(String path, int size, int filterId) throws RemoteException {
        if(!waitForServiceConnected()) {
            return null;
        }
        FilterData data = getFilterData(filterId);
        if(data == null) {
            SmartLog.d(TAG, "Invalid filterId");
            return null;
        }
        return mIFilterService.processingImageFile(path, size, data.mFilter, null);
    }

    /**
     * 필터 적용 요청 리스트를 초기화한다.
     * 
     * @version 1.0
     */
    @SuppressWarnings("static-method")
    public void cancelAllApplyFilter() {
        sFilteringThread.cancelFiltering();
    }

    /**
     * @param userInfo 필터 적용을 중지하고자 하는 대상에 대한 정보
     * @return 필터 적용이 중지 되었으면 true 아니면 false
     * @version 1.0
     */
    @SuppressWarnings("static-method")
    public boolean cancelApplyFilter(Object userInfo) {
        return sFilteringThread.cancelFiltering(userInfo);
    }

    protected class FilterProcess extends Thread {
        private ArrayList<FilterProcessInfo> mThreadQueue;
        private boolean mStopProcessing = false;
        private FilterProcessInfo data;

        public FilterProcess() {
            mThreadQueue = new ArrayList<FilterProcessInfo>();
        }

        public void addFilterData(FilterProcessInfo data) {
            synchronized(mThreadQueue) {
                mThreadQueue.add(data);
                mThreadQueue.notify();
            }
        }

        public void cancelFiltering() {
            synchronized(mThreadQueue) {
                mThreadQueue.clear();
            }
        }

        public boolean cancelFiltering(Object userInfo) {
            boolean cancled = false;
            if(data != null && data.mUserInfo == userInfo) {
                try {
                    mIFilterService.stopProcessing(true);
                } catch(RemoteException e) {
                }
            } else {
                synchronized(mThreadQueue) {
                    for(int index = mThreadQueue.size() - 1; index >= 0; index--) {
                        FilterProcessInfo info = mThreadQueue.get(index);
                        if(info.mUserInfo == userInfo) {
                            mThreadQueue.remove(index);
                            cancled = true;
                        }
                    }
                }
            }
            return cancled;
        }

        public void clearThread() {
            synchronized(mThreadQueue) {
                mStopProcessing = true;
                mThreadQueue.notify();
            }
        }

        @Override
        public void run() {
            while(true) {
                if(mStopProcessing) {
                    break;
                }

                synchronized(mThreadQueue) {
                    if(mThreadQueue.size() != 0) {
                        data = mThreadQueue.get(0);
                        mThreadQueue.remove(0);
                    } else {
                        data = null;
                    }
                }

                boolean retryProcess = false;

                try {
                    if(data != null) {
                        // Original Filter
                        if(data.mFilterType == FilterManagerWrapper.PICTURE_TYPE) {
                            SmartLog.d(TAG, "Processing Picture image start: "
                                    + data.mFilterData.mTitle);
                            String fileName = mIFilterService.processingImageFile(mOriginalFile,
                                                                                  data.mSize,
                                                                                  data.mFilterData.mFilter,
                                                                                  data.mStickerImageFilePath);
                            SmartLog.d(TAG, "Processing Picture image end: "
                                    + data.mFilterData.mTitle);
                            data.mResultFilePath = fileName;
                        } else if(data.mFilterType == FilterManagerWrapper.PREVIEW_TYPE) {
                            // 캐시 확인
                            data.mResultBitmap = CacheUtils.readCacheFile(mGlobalContext,
                                                                          CACHE_FILE_NAME
                                                                                  + data.mFilterData.mServerId);
                            if(data.mResultBitmap == null) {
                                SmartLog.d(TAG, "Processing Preview image start: "
                                        + data.mFilterData.mTitle);
                                data.mResultBitmap = mIFilterService.processingImageBitmap(mPreview,
                                                                                           data.mFilterData.mFilter,
                                                                                           data.mStickerImageFilePath);
                                SmartLog.d(TAG, "Processing Preview image end: "
                                        + data.mFilterData.mTitle);
                                // 캐시 저장
                                if(data.mCacheable) {
                                    CacheUtils.saveCacheFile(mGlobalContext, data.mResultBitmap,
                                                             CACHE_FILE_NAME
                                                                     + data.mFilterData.mServerId,
                                                             Bitmap.CompressFormat.JPEG);
                                }
                            }
                        } else if(data.mFilterType == FilterManagerWrapper.THUMBNAIL_TYPE) {
                            // 캐시 확인
                            data.mResultBitmap = FilteredThumbnailCache.getInstance()
                                                                       .get(data.mFilterData.mServerId);
                            if(data.mResultBitmap == null) {
                                SmartLog.d(TAG, "Processing Thumbnail image start: "
                                        + data.mFilterData.mTitle);
                                data.mResultBitmap = mIFilterService.processingImageBitmap(mThumb,
                                                                                           data.mFilterData.mFilter,
                                                                                           data.mStickerImageFilePath);
                                SmartLog.d(TAG, "Processing Thumbnail image end: "
                                        + data.mFilterData.mTitle);
                                // save cache
                                if(data.mResultBitmap != null) {
                                    Bitmap b = data.mResultBitmap.copy(Config.ARGB_8888, true);
                                    FilteredThumbnailCache.getInstance()
                                                          .put(data.mFilterData.mServerId, b);
                                }
                            }
                        } else if(data.mFilterType == FilterManagerWrapper.DIRECT_BITMAP_REQUEST_TYPE) {
                            SmartLog.d(TAG, "Processing Direct request image start: "
                                    + data.mFilterData.mTitle);
                            // 이미지가 큰 경우 파일로 출력후 filter service에서 처리한다.
                            // 이미지가 큰 경우 서비스로 넘길 수 없다.
                            if(data.mResultBitmap.getWidth() > FilterManagerWrapper.PREVIEW_SIZE
                                    || data.mResultBitmap.getHeight() > FilterManagerWrapper.PREVIEW_SIZE) {
                                String tmpPath = new StringBuffer().append(mGlobalContext.getFilesDir()
                                                                                         .getAbsolutePath())
                                                                   .append(File.separator)
                                                                   .append("filter_target_temp_image.jpg")
                                                                   .toString();
                                FileUtils.saveBitmap(data.mResultBitmap, tmpPath,
                                                     Bitmap.CompressFormat.JPEG);

                                if(data.mResultBitmap.getWidth() > data.mResultBitmap.getHeight()) {
                                    data.mSize = data.mResultBitmap.getWidth();
                                } else {
                                    data.mSize = data.mResultBitmap.getHeight();
                                }

                                String fileName = mIFilterService.processingImageFile(tmpPath,
                                                                                      data.mSize,
                                                                                      data.mFilterData.mFilter,
                                                                                      data.mStickerImageFilePath);
                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inMutable = true;
                                data.mResultBitmap = BitmapFactory.decodeFile(fileName, options);
                            } else {
                                Config config = data.mResultBitmap.getConfig();
                                boolean needRecycle = false;
                                if(config == null || !config.equals(Config.ARGB_8888)) {
                                    data.mResultBitmap = data.mResultBitmap.copy(Config.ARGB_8888,
                                                                                 true);
                                    needRecycle = true;
                                }
                                Bitmap result = mIFilterService.processingImageBitmap(data.mResultBitmap,
                                                                                      data.mFilterData.mFilter,
                                                                                      data.mStickerImageFilePath);
                                if(needRecycle) {
                                    if(data.mResultBitmap != null
                                            && !data.mResultBitmap.isRecycled()) {
                                        data.mResultBitmap.recycle();
                                    }
                                }
                                data.mResultBitmap = result;
                            }
                            SmartLog.d(TAG, "Processing Direct request image end: "
                                    + data.mFilterData.mTitle);
                        } else if(data.mFilterType == FilterManagerWrapper.DIRECT_FILE_REQUEST_TYPE) {
                            data.mResultFilePath = mIFilterService.processingImageFile(data.mResultFilePath,
                                                                                       data.mSize,
                                                                                       data.mFilterData.mFilter,
                                                                                       data.mStickerImageFilePath);
                        }
                    }
                } catch(DeadObjectException e) {
                    // 서비스 process가 죽은 경우 다시 bind하도록 처리
                    SmartLog.e(TAG, "dead Object Exception");
                    mIFilterService = null;
                    bindService();
                    if(waitForServiceConnected()) {
                        retryProcess = true;
                        mThreadQueue.add(0, data);
                    }
                } catch(Throwable e) {
                    SmartLog.e(TAG, "FilterProcess", e);
                } finally {
                    try {
                        mIFilterService.stopProcessing(false);
                    } catch(Exception e) {
                    }
                }
                if(!retryProcess) {
                    notifyResult(data);
                }

                synchronized(mThreadQueue) {
                    try {
                        if(mThreadQueue.size() == 0) {
                            mThreadQueue.wait();
                        }
                    } catch(InterruptedException e) {
                    }
                }
            }
        }
    }

    protected void notifyResult(FilterProcessInfo filterInfo) {
        if(filterInfo != null && filterInfo.mListener != null) {
            Message msg = new Message();
            msg.what = NotifyHandler.HANDLER_MESSAGE_NOTIFY_RESULT;
            msg.obj = filterInfo;
            mHandler.sendMessage(msg);
        }
    }

    protected static class NotifyHandler extends Handler {
        public static final int HANDLER_MESSAGE_NOTIFY_RESULT = 1;

        @Override
        public void handleMessage(Message msg) {
            if(msg.what == HANDLER_MESSAGE_NOTIFY_RESULT) {
                if(msg.obj instanceof FilterProcessInfo) {
                    FilterProcessInfo filterInfo = (FilterProcessInfo)msg.obj;
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
                    filterInfo = null;
                }
            }
        }
    }

    @SuppressWarnings("static-method")
    protected void clearThread() {
        if(sFilteringThread != null) {
            sFilteringThread.clearThread();
        }
    }

    /**
     * {@link FilterManager} 리소스 반환
     * 
     * @version 1.0
     */
    public void release() {
        clearThread();
        clearFilterList();

        if(mIFilterService != null) {
            try {
                mGlobalContext.unbindService(serviceConnection);
            } catch(Exception e) {
            }
        }

        if(mGlobalContext != null) {
            mGlobalContext = null;
        }

        if(sInstance != null) {
            sInstance = null;
        }
    }
}
