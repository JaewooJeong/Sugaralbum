
package com.kiwiple.imageframework.util;

import android.graphics.Bitmap;
import android.os.Build;

import androidx.collection.LruCache;

import com.kiwiple.imageframework.Constants;

/**
 * 썸네일 이미지를 캐시해두기 위한 클래스<br>
 * 캐시 가능한 이미지는 최대 개수는 30개, 최대 크기는 2MB이다.
 * 
 * @version 2.0
 */
public class FilteredThumbnailCache {
    // Thumbnail Memory Cache Size
    private static final int BITMAP_CACHE_SIZE = 1024 * 1024 * 20; // 20MB
    private static final int BITMAP_CACHE_SIZE_LEGACY = 30; // 2MB

    private static FilteredThumbnailCache sInstance;

    private LruCache<Object, Bitmap> mCache;

    /**
     * @return {@link #FilteredThumbnailCache}의 인스턴스 반환
     * @version 2.0
     */
    public static FilteredThumbnailCache getInstance() {
        if(sInstance == null)
            sInstance = new FilteredThumbnailCache();
        return sInstance;
    }

    private FilteredThumbnailCache() {
        if(Build.VERSION.SDK_INT >= 12) {
            mCache = new LruCache<Object, Bitmap>((BITMAP_CACHE_SIZE)) {
                @Override
                protected int sizeOf(Object key, Bitmap value) {
                    return value.getByteCount();
                }

                @Override
                protected void entryRemoved(boolean evicted, Object key, Bitmap oldValue,
                        Bitmap newValue) {
                }
            };
        } else {
            mCache = new LruCache<Object, Bitmap>((BITMAP_CACHE_SIZE_LEGACY)) {
                @Override
                protected int sizeOf(Object key, Bitmap value) {
                    return 1;
                }

                @Override
                protected void entryRemoved(boolean evicted, Object key, Bitmap oldValue,
                        Bitmap newValue) {
                }
            };
        }
    }

    /**
     * 캐시에 저장된 썸네일 이미지를 반환
     * 
     * @param key 썸네일 이미지 고유 번호
     * @return 캐시된 이미지 또는 null
     * @version 2.0
     */
    public synchronized Bitmap get(Object key) {
        if(!Constants.RELEASE_BUILD && mCache.get(key) != null) {
            SmartLog.d("cache", "hit: " + key);
        }
        return mCache.get(key);
    }

    /**
     * 썸네일 이미지를 캐시에 저장
     * 
     * @param key 썸네일 이미지 고유 번호
     * @param value 썸네일 이미지
     * @version 2.0
     */
    public synchronized void put(Object key, Bitmap value) {
        if(key == null || value == null) {
            return;
        }
        mCache.put(key, value);
    }

    /**
     * 캐시에 저장된 썸네일 이미지를 삭제한다.
     * 
     * @param key 썸네일 이미지
     * @version 2.0
     */
    public synchronized void remove(Object key) {
        mCache.remove(key);
    }

    /**
     * 캐시를 초기화한다.
     * 
     * @version 2.0
     */
    public synchronized void clear() {
        if(mCache != null) {
            mCache.evictAll();
            System.gc();
        }
    }

    /**
     * {@link #FilteredThumbnailCache의 모든 리소스를 반환한다.
     * 
     * @version 2.0
     */
    public void release() {
        clear();
        if(sInstance != null) {
            sInstance = null;
        }
    }
}
