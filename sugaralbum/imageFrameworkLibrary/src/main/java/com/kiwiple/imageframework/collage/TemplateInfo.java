
package com.kiwiple.imageframework.collage;

import java.util.HashMap;

/**
 * 콜라주 템플릿 정보
 * 
 * @version 2.0
 */
public class TemplateInfo {
    // get from json file
    private int mId = -1;
    private String mTitle = null;
    private String mThumbnail = null;
    private float mAspectRatio = -1;
    private int mFrameCount = -1;
    /**
     * U+Story<br>
     * 디자인 프레임을 포함한 일반(1), 다각형(2), 디자인(3)으로 설정
     */
    private int mTemplateType = 1;
    /**
     * U+Story<br>
     * 버전을 관리하기 위한 값
     */
    private int mVersion = 1;
    /**
     * U+Story<br>
     * 테마 값
     */
    private String mTheme = null;
    private HashMap<String, Object> mData = null;

    TemplateInfo(DesignTemplate template) {
        mId = template.mId;
        mTitle = template.mTitle;
        mThumbnail = template.mTemplateThumb;
        mAspectRatio = template.mAspectRatio;
        mFrameCount = template.mFrameInfos.size();
        /**
         * U+Story<br>
         * 초기값 설정
         */
        // ------------------------- 여기부터 변경 -------------------------------------------
        mTemplateType = template.mTemplateType;
        mTheme = template.mTheme;
        mVersion = template.mVersion;
        // ------------------------- 여기까지 변경 -------------------------------------------
    }

    /**
     * @return 템플릿 고유 식별자
     * @version 1.0
     */
    public int getId() {
        return mId;
    }

    /**
     * @return 템플릿 제목
     * @version 1.0
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * @return 템플릿 썸네일 이미지
     * @version 1.0
     */
    public String getThumbnail() {
        return mThumbnail;
    }

    /**
     * @return 템플릿 가로 세로 비율
     * @version 2.0
     */
    public float getAspectRatio() {
        return mAspectRatio;
    }

    /**
     * @return 프레임 개수
     * @version 1.0
     */
    public int getFrameCount() {
        return mFrameCount;
    }

    /**
     * @return Template 전체 data를 갖는 HashMap을 반환한다. <br>
     *         HashMap의 value는 json파일 상에서의 value type에 따라 String, int, ArrayList, HashMap 등이 된다.
     * @version 1.0
     */
    public HashMap<String, Object> getTemplateInfo() {
        return mData;
    }

    /**
     * U+Story<br>
     * 콜라주 템플릿 버전을 반환한다.
     * 
     * @version 2.0
     */
    public int getVersion() {
        return mVersion;
    }

    /**
     * U+Story<br>
     * 콜라주 템플릿 타입을 반환한다.
     * 
     * @see {@link DesignTemplate#TEMPLATE_TYPE_DEFAULT}<br>
     *      {@link DesignTemplate#TEMPLATE_TYPE_POLYGON}<br>
     *      {@link DesignTemplate#TEMPLATE_TYPE_DESIGN}
     * @version 2.0
     */
    public int getTemplateType() {
        return mTemplateType;
    }

    /**
     * U+Story<br>
     * 콜라주 템플릿 테마 이름을 반환한다.
     * 
     * @return 템플릿 테마 이름
     * @version 3.0
     */
    public String getTheme() {
        return mTheme;
    }
}
