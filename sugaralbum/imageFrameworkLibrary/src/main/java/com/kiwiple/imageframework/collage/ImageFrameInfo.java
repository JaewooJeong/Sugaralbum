
package com.kiwiple.imageframework.collage;

import java.util.HashMap;

import android.text.TextUtils;

/**
 * 콜라주를 구성하는 요소들의 기본 정보를 저장하고 있는 class
 * 
 * @version 1.0
 */
public class ImageFrameInfo {
    /**
     * 콜라주 아이템의 고유 번호
     * 
     * @version 1.0
     */
    public int mId = -1;
    /**
     * 콜라주 아이템의 이름
     * 
     * @version 1.0
     */
    public String mTitle = null;
    /**
     * 콜라주 아이템의 scale 정보
     * 
     * @version 1.0
     */
    public float mScale = 1.f;
    /**
     * 콜라주 아이템의 x 좌표
     * 
     * @version 1.0
     */
    public float mCoordinateX = -1.f;
    /**
     * 콜라주 아이템의 y 좌표
     * 
     * @version 1.0
     */
    public float mCoordinateY = -1.f;
    /**
     * 콜라주 아이템의 rotation 정보
     * 
     * @version 1.0
     */
    public float mRotation = -1;

    protected ImageFrameInfo() {
    }

    public ImageFrameInfo(HashMap<String, Object> frameInfo) {
        mId = (Integer)frameInfo.get("Id");
        mTitle = (String)frameInfo.get("Title");
        Object value = frameInfo.get("Scale");
        if(value != null) {
            mScale = Float.parseFloat(value.toString());
        }
        mCoordinateX = (Integer)frameInfo.get("CoordinateX");
        mCoordinateY = (Integer)frameInfo.get("CoordinateY");
        mRotation = (Integer)frameInfo.get("Rotation");
    }

    public boolean isValid() {
        if(mId == -1 || TextUtils.isEmpty(mTitle) || mCoordinateX == -1.f || mCoordinateY == -1.f
                || mRotation == -1) {
            return false;
        }
        return true;
    }
}
