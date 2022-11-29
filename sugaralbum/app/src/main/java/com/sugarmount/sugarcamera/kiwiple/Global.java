
package com.sugarmount.sugarcamera.kiwiple;

import android.content.Context;
import android.media.ExifInterface;

public class Global {
    private static Global sGlobal;

    // 생성할 필터 이미지 파일의 크기
    public static int PICTURE_SIZE_HIGH = 1600;
    
    public static int STICKER_MARKET_SNAPSHOT_SIZE = 1600;

    // 생성할 필터 이미지의 크기
    // 썸네일 이미지 해상도 상향(130923)
    public static int THUMBNAIL_SIZE = 240;
    public static int STICKER_THUMBNAIL_SIZE = 180;

    /**
     * 폰트가 추가되면 이곳에 Typeface 객체와 폰트의 String 변수를 추가 합니다.
     */
    // 추가 폰트 타입
    public static final String FACE_HELVETICA_BOLD = "HELVETICA_BOLD";
    public static final String FACE_HELVETICA = "HELVETICA";
    public static final String FACE_FONT1 = "FONT1";

    // 원본 이미지의 EXIF 정보
    private ExifInterface mOriginalImgExif;

    private Global(Context ApplicationContext) {
    }

    public static Global getInstance(Context ApplicationContext) {
        if(sGlobal == null) {
            sGlobal = new Global(ApplicationContext);
        }
        return sGlobal;
    }

    public void setOriginalExif(ExifInterface exif) {
        mOriginalImgExif = exif;
    }

    public ExifInterface getOriginalExif() {
        return mOriginalImgExif;
    }
}
