
package com.kiwiple.imageframework.collage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import android.graphics.Color;
import android.text.TextUtils;

import com.kiwiple.imageframework.sticker.StickerFrameInfo;
import com.kiwiple.imageframework.sticker.TextStickerFrameInfo;
import com.kiwiple.imageframework.util.SmartLog;

/**
 * 콜라주 템플릿을 구성하는 요소들을 저장하고 있는 클래스입니다.
 * 
 * @version 2.0
 */
public class DesignTemplate implements Serializable {
    private static final String TAG = DesignTemplate.class.getSimpleName();

    private static final long serialVersionUID = 1012L;
    static final float DEFAULT_OUTLINE_BASELINE = 800.f;
    /**
     * U+Story<br>
     * fixes #8392 : 20141001_keylime - to remove collage border of clean theme<br>
     * the default border value changed from 3.f to 0.f
     */
    static final float DEFAULT_OUTLINE_WIDTH = 0.f;

    /**
     * 콜라주 템플릿의 종류(기본 템플릿). Use with {@link #mTemplateType}
     * 
     * @version 1.0
     */
    public static final int TEMPLATE_TYPE_DEFAULT = 1;
    /**
     * 콜라주 템플릿의 종류(다각형 템플릿). Use with {@link #mTemplateType}
     * 
     * @version 2.0
     */
    public static final int TEMPLATE_TYPE_POLYGON = 2;
    /**
     * 콜라주 템플릿의 종류(디자인 템플릿). Use with {@link #mTemplateType}
     * 
     * @version 2.0
     */
    public static final int TEMPLATE_TYPE_DESIGN = 3;

    /**
     * 콜라주 템플릿의 고유 번호
     * 
     * @version 1.0
     */
    public int mId = -1;
    /**
     * 콜라주 템플릿의 이름
     * 
     * @version 1.0
     */
    public String mTitle = null;
    /**
     * 콜라주 템플릿의 썸네일 이미지
     * 
     * @version 1.0
     */
    public String mTemplateThumb = null;
    /**
     * 콜라주 템플릿의 배경 이미지 경로
     * 
     * @version 2.0
     */
    public String mBackgroundImageUrl = null;
    /**
     * 콜라주 템플릿의 가로 길이
     * 
     * @version 2.0
     */
    public int mWidth = -1;
    /**
     * 콜라주 템플릿의 세로 길이
     * 
     * @version 2.0
     */
    public int mHeight = -1;
    /**
     * 콜라주 템플릿의 가로 세로 비율
     * 
     * @version 2.0
     */
    public float mAspectRatio = -1;
    /**
     * 콜라주 템플릿을 구성하는 프레임 목록
     * 
     * @version 2.0
     */
    public ArrayList<CollageFrameInfo> mFrameInfos = null;
    /**
     * U+Story<br>
     * 콜라주 템플릿의 종류 다음 중 하나의 값을 가진다.
     * 
     * @see {@link #TEMPLATE_TYPE_DEFAULT}<br>
     *      {@link #TEMPLATE_TYPE_POLYGON}<br>
     *      {@link #TEMPLATE_TYPE_DESIGN}
     * @version 2.0
     */
    public int mTemplateType = 1;
    /**
     * 콜라주 템플릿의 배경 색상
     * 
     * @version 2.0
     */
    public int mBackgroundColor = Color.WHITE;

    public float mLayoutWidthScaleFactor = 1.f;
    public float mLayoutHeightScaleFactor = 1.f;
    public float mOutlineWidth = DEFAULT_OUTLINE_WIDTH;
    /**
     * 콜라주 템플릿의 스티커 목록
     * 
     * @version 2.0
     */
    public ArrayList<StickerFrameInfo> mStickerInfos = null;
    /**
     * 콜라주 템플릿의 텍스트 스티커 목록
     * 
     * @version 2.0
     */
    public ArrayList<TextStickerFrameInfo> mTextStickerInfos = null;
    /**
     * U+Story<br>
     * 테마 이름
     * 
     * @version 3.0
     */
    public String mTheme;
    public boolean mIsThemeTemplate = false;
    
    /**
     * U+Story<br>
     * 버전
     * 
     * @version 3.0
     */
    public int mVersion;

    @SuppressWarnings("unchecked")
    public void parse(HashMap<String, Object> data) {
        mId = (Integer)data.get("Id");
        mTitle = (String)data.get("Title");
        mTemplateThumb = (String)data.get("ThumbnailImage");
        mBackgroundImageUrl = (String)data.get("BackgroundImage");
        mWidth = (Integer)data.get("Width");
        mHeight = (Integer)data.get("Height");
        mFrameInfos = new ArrayList<CollageFrameInfo>();
        for(HashMap<String, Object> frameInfo : (ArrayList<HashMap<String, Object>>)data.get("Frames")) {
            mFrameInfos.add(new CollageFrameInfo(frameInfo));
        }
        if(mWidth != -1 && mHeight != -1) {
            mAspectRatio = mHeight / (float)mWidth;
        }
        // added Template version 2
        Object value = data.get("TemplateType");
        if(value instanceof Integer) {
            mTemplateType = (Integer)value;
        }
        try {
            value = data.get("BackgroundColor");
            if(value instanceof String) {
                String hexString = (String)value;
                if(!TextUtils.isEmpty(hexString)) {
                    mBackgroundColor = (int)Long.parseLong(hexString, 16);
                }
            }
        } catch(NumberFormatException e) {
            SmartLog.e(TAG, "Background color parsing error", e);
        }
        mStickerInfos = new ArrayList<StickerFrameInfo>();
        for(HashMap<String, Object> stickerInfo : (ArrayList<HashMap<String, Object>>)data.get("Stickers")) {
            mStickerInfos.add(new StickerFrameInfo(stickerInfo));
        }
        mTextStickerInfos = new ArrayList<TextStickerFrameInfo>();
        value = data.get("TextStickers");
        if(value instanceof ArrayList) {
            for(HashMap<String, Object> stickerInfo : (ArrayList<HashMap<String, Object>>)value) {
                mTextStickerInfos.add(new TextStickerFrameInfo(stickerInfo));
            }
        }

        /**
         * U+Story<br>
         * 추가 변수 파싱 추가
         */
        // ------------------------- 여기부터 변경 -------------------------------------------
        value = data.get("Theme");
        if (value instanceof String) {
            mTheme = (String)data.get("Theme");    
        }
        
        value = data.get("Version");
        if (value instanceof Integer) {
            mVersion = (Integer)data.get("Version");    
        }
        // ------------------------- 여기까지 변경 -------------------------------------------
    }

    public boolean isValid() {
        /**
         * U+Story<br>
         * 변수 추가에 따른 조건 값 추가
         */
        if(mId == -1
                || TextUtils.isEmpty(mTitle)
                || TextUtils.isEmpty(mTemplateThumb)
                || mWidth == -1
                || mHeight == -1
                || mAspectRatio == -1
                || mFrameInfos == null
                || (mTemplateType != TEMPLATE_TYPE_DEFAULT
                        && mTemplateType != TEMPLATE_TYPE_POLYGON && mTemplateType != TEMPLATE_TYPE_DESIGN)) {
            return false;
        }
        for(CollageFrameInfo info : mFrameInfos) {
            if(!info.isValid()) {
                return false;
            }
        }
        return true;
    }
}
