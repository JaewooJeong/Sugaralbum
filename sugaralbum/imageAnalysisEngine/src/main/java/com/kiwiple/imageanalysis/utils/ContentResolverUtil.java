
package com.kiwiple.imageanalysis.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * 갤러리 앨범에서 필요 Cursor를 쉽게 꺼내기 위한 메소드
 */
public class ContentResolverUtil {

    /**
     * 앨범에서 특정 조건에 만족하는 Cursor 반환
     * 
     * @param ctx Context
     * @param select Select 컬럼 배열
     * @param where 조건
     * @param groupBy Group By 조건
     * @param orderBy Order By 조건
     * @return 조건에 맞는 Cursor
     */
    public static Cursor getImageCursor(Context ctx, String[] select, String where, String groupBy,
            String orderBy) {
        if(groupBy != null) {
            where = where + ") GROUP BY (" + groupBy;
        }

        return ctx.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, select,
                                              where, null, orderBy);
    }

    /**
     * 앨범에서 특정 조건에 만족하는 Cursor 반환
     * 
     * @param ctx Context
     * @param uri Uri
     * @param select Select 컬럼 배열
     * @param where 조건
     * @param groupBy Group By 조건
     * @param orderBy Order By 조건
     * @return 조건에 맞는 Cursor
     */
    public static Cursor getImageCursor(Context ctx, Uri uri, String[] select, String where,
            String groupBy, String orderBy) {

        if(uri == null) {
            return null;
        }

        if(groupBy != null) {
            where = where + ") GROUP BY (" + groupBy;
        }

        return ctx.getContentResolver().query(uri, select, where, null, orderBy);
    }
}
