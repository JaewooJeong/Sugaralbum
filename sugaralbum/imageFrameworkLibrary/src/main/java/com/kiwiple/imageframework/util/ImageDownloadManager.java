
package com.kiwiple.imageframework.util;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Handler;

import com.kiwiple.imageframework.util.thread.PoolWorkerRunnable;
import com.kiwiple.imageframework.util.thread.WorkQueue;

/**
 * 원격 이미지를 다운로드하는 매니저 클래스
 */
public class ImageDownloadManager {

    private static final String TAG = ImageDownloadManager.class.getSimpleName();
    public final static String STATE_IMAGE_DOWNLOAD_COMPLETE = "IMAGE_MANAGER_IMAGE_DOWNLOAD_COMPLETE";
    public final static String STATE_IMAGE_DOWNLOAD_ERROR = "IMAGE_MANAGER_IMAGE_DOWNLOAD_ERROR";
    private static ImageDownloadManager sInstance;
    private final String mPersistancePath;

    private Handler mHandler = new Handler();

    private Context mContext = null;
    private BitmapFactory.Options mConfigOptions = new Options();

    private boolean mPause;

    private ImageDownloadManager(Context ctx, String persistancePath) {
        mContext = ctx;
        mConfigOptions.inPreferredConfig = Config.RGB_565;
        mPersistancePath = persistancePath;
    }

    public static ImageDownloadManager getInstance(Context ctx, String persistancePath) {
        if(sInstance == null) {
            sInstance = new ImageDownloadManager(ctx, persistancePath);
        }
        return sInstance;
    }

    /**
     * 다운로드를 중지한다
     */
    public void pause() {
        mPause = true;
        cancelAllDownload();
    }

    /**
     * 다운로드를 재개
     */
    public void resume() {
        mPause = false;
    }

    /**
     * 중지 여부 반환
     * 
     * @return 중지 여부
     */
    public boolean isPause() {
        return mPause;
    }

    /**
     * Url을 통해 이미지를 캐시에서 가져온다
     * 
     * @param url 이미지 url
     * @return 캐싱된 이미지
     */
    public static Bitmap getCacheImage(String url) {
        return FilteredThumbnailCache.getInstance().get("downloadmanager" + url);
    }

    /**
     * 이미지를 캐시에 저장한다
     * 
     * @param key 저장 키(url)
     * @param image 저장 비트맵
     */
    private static void addCacheImage(String key, Bitmap image) {
        FilteredThumbnailCache.getInstance().put("downloadmanager" + key, image);
    }

    private static void notifyResult(String state, ImageInfo info) {
        if(info.listener != null) {
            if(!info.canceled) {
                info.listener.onImageDownloadComplete(state, info);
            } else {
                if(info.bitmap != null && !info.caching) {
                    info.bitmap.recycle();
                    info.bitmap = null;
                }
            }
        }
    }

    /**
     * 이미지 다운로드 요청
     * 
     * @param info 이미지 정보
     */
    public void ReqDownload(ImageInfo info) {
        if(mPause) {
            return;
        }
        WorkQueue.getInstance().execute(new DownloadProc(info));
    }

    /**
     * 모든 이미지 다운로드 취소
     */
    public static void cancelAllDownload() {
        WorkQueue.getInstance().removeAll();
    }

    private class DownloadProc extends PoolWorkerRunnable {
        private ImageInfo mCurrentDownloadInfo = null;
        private int mIndex;

        public DownloadProc(ImageInfo info) {
            super(info.mId);
            mCurrentDownloadInfo = info;
        }

        @Override
        public void run() {
            SmartLog.i(TAG, "Request image download[" + mIndex + "] : "
                    + mCurrentDownloadInfo.imageURL);
            String url = null;
            File tmpDir;
            if(!mCurrentDownloadInfo.persistance) {
                tmpDir = mContext.getCacheDir();
            } else {
                tmpDir = new File(mPersistancePath);
                if(!tmpDir.exists()) {
                    tmpDir.mkdirs();
                }
            }
            url = mCurrentDownloadInfo.imageURL;

            if(url != null) {
                boolean isOK = false;

                String filename;
                if(mCurrentDownloadInfo.targetName == null) {
                    filename = Base64Coder.getMD5HashString(url);
                } else {
                    filename = mCurrentDownloadInfo.targetName;
                }

                String filePath;
                if(mCurrentDownloadInfo.targetDir == null) {
                    // 주어진 경로가 없으면 defalut
                    filePath = tmpDir.getAbsolutePath() + "/" + filename;
                } else {
                    File dir = new File(mCurrentDownloadInfo.targetDir);

                    if(!dir.exists()) {
                        dir.mkdir();
                    }

                    filePath = mCurrentDownloadInfo.targetDir + "/" + filename;
                }

                if(checkCacheFile(filePath)) {
                    mCurrentDownloadInfo.localPath = filePath;
                    isOK = true;
                } else {
                    int count = 0;

                    while(count < 3) {
                        count++;
                        if(BitmapUtils.DownloadBitmap(url, filePath, mCurrentDownloadInfo.listener,
                                                      count)) {
                            if(checkValid(filePath)) {
                                mCurrentDownloadInfo.localPath = filePath;
                                isOK = true;

                                SmartLog.i(TAG, "Image download complete : " + filePath);
                                break;
                            }
                        }
                    }
                }

                if(isOK) {
                    // set state.

                    mCurrentDownloadInfo.localPath = filePath;

                    if(mCurrentDownloadInfo.decoding) {
                        if(mCurrentDownloadInfo.caching) {
                            mCurrentDownloadInfo.bitmap = getCacheImage(url);
                        }

                        if(mCurrentDownloadInfo.bitmap == null) {
                            try {
                                mCurrentDownloadInfo.bitmap = FileUtils.decodingImage(filePath,
                                                                                      mCurrentDownloadInfo.size,
                                                                                      mCurrentDownloadInfo.targetBitmapConfig);
                                // decodingImage(filePath, info.width, info.height);
                            } catch(Throwable e) {
                                mCurrentDownloadInfo.bitmap = null;
                            }
                        }

                        if(mCurrentDownloadInfo.bitmap == null) {
                            File file = new File(filePath);
                            file.delete();

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    notifyResult(STATE_IMAGE_DOWNLOAD_ERROR, mCurrentDownloadInfo);
                                }
                            });
                        } else {

                            SmartLog.d(TAG,
                                       "Image Size : " + mCurrentDownloadInfo.bitmap.getWidth()
                                               + "x" + mCurrentDownloadInfo.bitmap.getHeight());

                            if(mCurrentDownloadInfo.caching) {
                                addCacheImage(url, mCurrentDownloadInfo.bitmap);
                            }

                            if(mCurrentDownloadInfo.deleteAfterDecoding) {
                                File file = new File(filePath);

                                file.delete();
                            }

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    notifyResult(STATE_IMAGE_DOWNLOAD_COMPLETE,
                                                 mCurrentDownloadInfo);
                                }
                            });
                        }
                    } else {
                        try {
                            mCurrentDownloadInfo.bitmap = BitmapFactory.decodeFile(mCurrentDownloadInfo.localPath,
                                                                                   mConfigOptions);
                        } catch(Throwable e) {
                            SmartLog.e(TAG, "Download image decode error", e);
                        }
                        if(mCurrentDownloadInfo.bitmap == null) {
                            File file = new File(filePath);
                            file.delete();
                        } else {
                            if(mCurrentDownloadInfo.caching) {
                                addCacheImage(url, mCurrentDownloadInfo.bitmap);
                            }
                        }
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                notifyResult(STATE_IMAGE_DOWNLOAD_COMPLETE, mCurrentDownloadInfo);
                            }
                        });
                    }
                } else {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            notifyResult(STATE_IMAGE_DOWNLOAD_ERROR, mCurrentDownloadInfo);
                        }
                    });
                }
            }
        }
    }

    private static boolean checkCacheFile(String path) {
        File file = new File(path);
        if(file.exists() && file.length() != 0) {
            SmartLog.i(TAG, path + " file exist in cache dir");
            return true;
        }
        return false;
    }

    private static boolean checkValid(String path) {
        BitmapFactory.Options options = new Options();
        options.inJustDecodeBounds = true;
        File file = new File(path);
        if(file.exists() && file.length() != 0) {
            BitmapFactory.decodeFile(path, options);
            if(options.outWidth > 0 && options.outHeight > 0) {
                return true;
            }
            SmartLog.i(TAG, "Bitmap image crashed");
            return false;
        }
        return false;
    }

    /**
     * 이미지 다운로드 리스너
     */
    public interface ImageDownloadManagerListener {
        public void progressDownload(int progress);

        public void onImageDownloadComplete(String state, ImageInfo info);
    }

    public static class ImageInfo extends Object {
        public Object mId;
        public boolean persistance;
        public String imageURL;
        public String localPath;
        public boolean decoding;
        public int size;
        public Bitmap bitmap;
        public boolean deleteAfterDecoding = false;
        public boolean caching = false;
        public ImageDownloadManagerListener listener;
        public String targetDir = null;
        public String targetName = null;
        public boolean canceled = false;
        public Config targetBitmapConfig = Config.RGB_565;
    }
}
