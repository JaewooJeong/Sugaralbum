
package com.sugarmount.sugarcamera.kiwiple.util;

import android.graphics.Bitmap;

public class ThumbnailImageCache {
    private static ImagePickerLRUCache<String, Bitmap> sCache = new ImagePickerLRUCache<String, Bitmap>();
    static {
        sCache.init(100);
    }

    public static void setCacheImage(String id, Bitmap image) {
        sCache.put(id, image);
    }

    public static Bitmap getCacheImage(String id) {
        return sCache.get(id);
    }

    public static void removeCacheImage(String id) {
        sCache.remove(id);
    }
}
