
package com.kiwiple.imageframework.sticker;

import java.util.HashMap;

import android.graphics.Color;
import android.text.TextUtils;

import com.kiwiple.imageframework.collage.ImageFrameInfo;
import com.kiwiple.imageframework.util.SmartLog;

/**
 * 콜라주 텍스트의 정보를 저장하는 class
 * 
 * @version 2.0
 */
public class TextStickerFrameInfo extends ImageFrameInfo {
    private static final String TAG = ImageFrameInfo.class.getSimpleName();

    /**
     * 콜라주 텍스트의 문자열
     * 
     * @version 2.0
     */
    public String mText;
    /**
     * 콜라주 텍스트의 글꼴 경로
     * 
     * @version 2.0
     */
    public String mFontUrl;
    /**
     * 콜라주 텍스트의 글자 크기
     * 
     * @version 2.0
     */
    public int mFontSize;
    /**
     * 콜라주 텍스트의 글자 색상
     * 
     * @version 2.0
     */
    public int mFontColor;
    /**
     * 콜라주 텍스트의 배경 색상
     * 
     * @version 2.0
     */
    public int mBackgroundColor;
    /**
     * 콜라주 텍스트의 배경 이미지 경로
     * 
     * @version 2.0
     */
    public String mBackgroundImageUrl;

    public TextStickerFrameInfo() {
        mId = 0;
        mTitle = "text";
        mScale = 1.f;
        mCoordinateX = 0.f;
        mCoordinateY = 0.f;
        mRotation = 0;
        mText = null;
        mFontUrl = null;
        mFontSize = 33;
        mFontColor = Color.BLACK;
        mBackgroundColor = -1;
        mBackgroundImageUrl = null;
    }

    public TextStickerFrameInfo(HashMap<String, Object> frameInfo) {
        super(frameInfo);
        // added Template version 2
        mText = (String)frameInfo.get("Text");
        mFontUrl = (String)frameInfo.get("Font");
        mFontSize = (Integer)frameInfo.get("FontSize");
        Object value = null;

        try {
            value = frameInfo.get("FontColor");
            if(value != null && value instanceof String) {
                String hexString = (String)value;
                if(!TextUtils.isEmpty(hexString)) {
                    mFontColor = (int)Long.parseLong(hexString, 16);
                }
            }
        } catch(NumberFormatException e) {
            SmartLog.e(TAG, "Background color parsing error", e);
        }

        try {
            value = frameInfo.get("BackgroundColor");
            if(value != null && value instanceof String) {
                String hexString = (String)value;
                if(!TextUtils.isEmpty(hexString)) {
                    mBackgroundColor = (int)Long.parseLong(hexString, 16);
                }
            }
        } catch(NumberFormatException e) {
            SmartLog.e(TAG, "Background color parsing error", e);
        }

        mBackgroundImageUrl = (String)frameInfo.get("BackgroundImage");
    }
}
