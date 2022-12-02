
package com.kiwiple.imageanalysis;

import android.media.ExifInterface;

import com.kiwiple.imageanalysis.analysis.operator.FaceOperator;
import com.kiwiple.imageanalysis.utils.SmartLog;

public class Global {
    private static final String TAG = Global.class.getSimpleName();
    private static Global sGlobal;

    public static int THUMBNAIL_SIZE = 320;
    public static int THUMBNAIL_GOOGLE_SIZE = 640;

    // 원본 이미지의 EXIF 정보
    private ExifInterface mOriginalImgExif;

    public static Global getInstance() {
        if(sGlobal == null) {
            sGlobal = new Global();
        }
        return sGlobal;
    }

    private Global() {

        if(!FaceOperator.IS_SUPPORT_SNAPDRAGON_FACE_PROCESSING) {
            THUMBNAIL_SIZE = THUMBNAIL_GOOGLE_SIZE;
        }

        SmartLog.e(TAG, "thumbnailSize : " + THUMBNAIL_SIZE);
    }

    public void release() {
        sGlobal = null;
    }

    /**
     * String이 null값인지 체크하는 함수. <br>
     * String이 null이거나, 빈 문자열이거나, "null"이라면 true를 반환.
     * 
     * @param string 체크할 문자열
     * @return boolean null이나 빈문자열인지 여부
     */
    public static boolean isNullString(String string) {
        return (string == null || string.isEmpty() || "null".equals(string));
    }

    public void setOriginalExif(ExifInterface exif) {
        mOriginalImgExif = exif;
    }

    public ExifInterface getOriginalExif() {
        return mOriginalImgExif;
    }
}
