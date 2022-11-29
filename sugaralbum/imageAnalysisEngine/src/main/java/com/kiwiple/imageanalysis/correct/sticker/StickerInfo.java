
package com.kiwiple.imageanalysis.correct.sticker;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import android.text.TextUtils;

/**
 * 스티커 정보 클래스
 */
public class StickerInfo implements Serializable {
    private static final long serialVersionUID = 6917999888065720245L;

    private static final String STICKER_ID = "id";
    private static final String STICKER_FILE_NAME = "file_name";
    private static final String STICKER_ANI_FILE_NAMES = "ani_names";
    private static final String STICKER_CATEGORY = "category";
    private static final String STICKER_SUB_CATEGORY = "sub_category";
    private static final String STICKER_WIDTH = "width";
    private static final String STICKER_HEIGHT = "height";
    private static final String STICKER_IS_FIT = "isFit";
    private static final String STICKER_LEFT_PADDING = "leftPadding";
    private static final String STICKER_TOP_PADDING = "topPadding";

    /**
     * 스티커 고유 아이디 값
     */
    public String mId;
    /**
     * 스티커 파일 이름
     */
    public String mFileName;
    /**
     * 스티커 대분류
     */
    public String mCategory;
    /**
     * 스티커 소분류
     */
    public String mSubCategory;
    /**
     * 스티커 가로 길이
     */
    public int mWidth = -1;
    /**
     * 스티커 세로 길이
     */
    public int mHeight = -1;
    /**
     * 스티커를 Fit할지 여부
     */
    public boolean mIsFit = false;
    /**
     * 스티커의 좌측 여백 크기
     */
    public int mLeftPadding = 0;
    /**
     * 스티커의 상단 여백 크기
     */
    public int mTopPadding = 0;
    /**
     * 애니메이션 스티커 파일 이름
     */
    public ArrayList<String> mAniFileNames = new ArrayList<String>();

    public StickerInfo() {

    }

    public void parse(JsonParser jp) throws JsonParseException, IOException {
        while(jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();

            if(jp.nextToken() == JsonToken.VALUE_NULL) {
                continue;
            }

            if(STICKER_ID.equals(fieldName)) {
                mId = jp.getText();
            } else if(STICKER_FILE_NAME.equals(fieldName)) {                
                mFileName = jp.getText();
            } else if(STICKER_ANI_FILE_NAMES.equals(fieldName)) {                
                while(jp.nextToken() != JsonToken.END_ARRAY) {
                    mAniFileNames.add(jp.getText());
                }
            } else if(STICKER_CATEGORY.equals(fieldName)) {
                mCategory = jp.getText();
            } else if(STICKER_SUB_CATEGORY.equals(fieldName)) {
                mSubCategory = jp.getText();
            } else if(STICKER_WIDTH.equals(fieldName)) {
                mWidth = jp.getIntValue();
            } else if(STICKER_HEIGHT.equals(fieldName)) {
                mHeight = jp.getIntValue();
            } else if(STICKER_IS_FIT.equals(fieldName)) {
                mIsFit = jp.getBooleanValue();
            } else if(STICKER_LEFT_PADDING.equals(fieldName)) {
                mLeftPadding = jp.getIntValue();
            } else if(STICKER_TOP_PADDING.equals(fieldName)) {
                mTopPadding = jp.getIntValue();
            }
        }
    }

    /**
     * HashMap에서 스티커 데이터를 파싱한다.
     * 
     * @param data 대상 HashMap
     */
    @SuppressWarnings("unchecked")
    public void parse(HashMap<String, Object> data) {
        if(data == null) {
            return;
        }

        mId = (String)data.get(STICKER_ID);
        mFileName = (String)data.get(STICKER_FILE_NAME);
        mAniFileNames = (ArrayList<String>)data.get(STICKER_ANI_FILE_NAMES);
        mCategory = (String)data.get(STICKER_CATEGORY);
        mSubCategory = (String)data.get(STICKER_SUB_CATEGORY);
        mWidth = (Integer)data.get(STICKER_WIDTH);
        mHeight = (Integer)data.get(STICKER_HEIGHT);
        mIsFit = (Boolean)data.get(STICKER_IS_FIT);
        mLeftPadding = (Integer)data.get(STICKER_LEFT_PADDING);
        mTopPadding = (Integer)data.get(STICKER_TOP_PADDING);
    }

    /**
     * 스티커가 유효한지 여부를 반환
     * 
     * @return 스티커 유효 여부
     */
    public boolean isValid() {
        if(TextUtils.isEmpty(mId)) {
            return false;
        }
        if(TextUtils.isEmpty(mFileName)) {
            if (mAniFileNames.isEmpty()) {
                return false;
            }
        }
        if(TextUtils.isEmpty(mCategory)
                || TextUtils.isEmpty(mSubCategory) || mWidth == -1 || mHeight == -1) {
            return false;
        }

        return true;
    }
}
