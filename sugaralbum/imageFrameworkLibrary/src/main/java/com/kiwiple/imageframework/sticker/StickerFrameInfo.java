
package com.kiwiple.imageframework.sticker;

import java.util.HashMap;

import com.kiwiple.imageframework.collage.ImageFrameInfo;

/**
 * 콜라주 스티커 정보를 저장하는 class
 * 
 * @version 2.0
 */
public class StickerFrameInfo extends ImageFrameInfo {
    /**
     * 콜라주 스티커의 이미지 경로
     * 
     * @version 2.0
     */
    public String mImageUrl;
    /**
     * 콜라주 스티커의 투명도
     * 
     * @version 2.0
     */
    public int mAlpha;
    /**
     * Design Template에서 사용. 프레임 영역에서 배경을 지운다.
     * 
     * @version 2.0
     */
    public boolean mClipBackground;

    public StickerFrameInfo() {
        mId = 0;
        mTitle = "sticker";
        mScale = 1.f;
        mCoordinateX = 0.f;
        mCoordinateY = 0.f;
        mRotation = 0;
        mAlpha = 255;
        mClipBackground = false;
    }

    public StickerFrameInfo(HashMap<String, Object> frameInfo) {
        super(frameInfo);
        // added Template version 2
        mImageUrl = (String)frameInfo.get("Image");
        mAlpha = (Integer)frameInfo.get("Alpha");
        Object value = frameInfo.get("ClipBackground");
        if(value instanceof Integer) {
            mClipBackground = (Integer)frameInfo.get("ClipBackground") == 1 ? true : false;
        }
    }
}
