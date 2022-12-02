
package com.kiwiple.imageframework.collage;

import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Color;
import android.graphics.RectF;
import android.text.TextUtils;

import com.kiwiple.imageframework.util.SmartLog;

/**
 * 콜라주 프레임 정보를 저장하고 있는 class
 * 
 * @version 2.0
 */
public class CollageFrameInfo extends ImageFrameInfo {
    private static final String TAG = CollageFrameInfo.class.getSimpleName();

    /**
     * 콜라주 프레임의 배경 이미지 경로
     * 
     * @version 2.0
     */
    public String mBackgroundImageUrl = null;
    /**
     * 콜라주 프레임의 svg 파일 경로
     * 
     * @version 1.0
     */
    public String mSvgString = null;

    // added Template version 2
    /**
     * 콜라주 프레임의 배경 색상
     * 
     * @version 2.0
     */
    public int mBackgroundColor = Color.WHITE;
    /**
     * 콜라주 프레임의 테두리 색상
     * 
     * @version 2.0
     */
    public int mBorderColor = Color.WHITE;
    /**
     * 콜라주 프레임의 기본 테두리 두께
     * 
     * @version 2.0
     */
    public float mInitialBorderWidth = DesignTemplate.DEFAULT_OUTLINE_WIDTH;
    /**
     * 콜라주 프레임의 테두리 두께
     * 
     * @version 2.0
     */
    public float mBorderWidth = DesignTemplate.DEFAULT_OUTLINE_WIDTH;

    /**
     * 콜라주 프레임 내에 인물 사진의 얼굴이 들어가기 적당한 영역 정보. 무비 다이어리의 콜라주 추천 기능에서 활용.
     */
    public ArrayList<RectF> mFaceRect = new ArrayList<RectF>();

    public CollageFrameInfo() {
    }

    public CollageFrameInfo(HashMap<String, Object> frameInfo) {
        super(frameInfo);
        mBackgroundImageUrl = (String)frameInfo.get("BackgroundImage");
        mSvgString = (String)frameInfo.get("SVG");
        Object value = null;

        // added Template version 2
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

        try {
            value = frameInfo.get("BorderColor");
            if(value != null && value instanceof String) {
                String hexString = (String)value;
                if(!TextUtils.isEmpty(hexString)) {
                    mBorderColor = (int)Long.parseLong(hexString, 16);
                }
            }
        } catch(NumberFormatException e) {
            SmartLog.e(TAG, "Border color parsing error", e);
        }
        value = frameInfo.get("BorderWidth");
        if(value != null && value instanceof Integer) {
            mInitialBorderWidth = (Integer)value;
        }
        // if(mInitialBorderWidth == 0) {
        // mBorderColor = Color.TRANSPARENT;
        // }

        try {
            value = frameInfo.get("FaceRect");
            if(value != null && value instanceof ArrayList) {
                ArrayList<HashMap<String, Object>> rectArray = (ArrayList<HashMap<String, Object>>)value;
                for(HashMap<String, Object> rect : rectArray) {
                    mFaceRect.add(new RectF((int)rect.get("left"), (int)rect.get("top"),
                                            (int)rect.get("right"), (int)rect.get("bottom")));
                }
            }
        } catch(IllegalArgumentException e) {
            SmartLog.e(TAG, "Border color parsing error", e);
        }
    }

    @Override
    public boolean isValid() {
        if(TextUtils.isEmpty(mSvgString)) {
            return false;
        }
        return super.isValid();
    }
}
