
package com.kiwiple.imageframework.filter;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;

/**
 * 필터의 구성요소에 대한 정보를 저장하고 있는 class
 * 
 * @version 2.0
 */
public class Filter implements Serializable, Cloneable, Parcelable {
    private static final long serialVersionUID = -1680791608117931353L;

    /**
     * Overlay blend 사용 여부. Overlay blend가 아니면 normal blend로 텍스처를 합성한다. 
     */
    private static final String[] NOT_OVERLAY_TEXTURE = new String[] {
            "texture01", "texture05", "texture06", "texture10"
    };

    public static final String CURVES_TYPE_ALL = "ALL";
    public static final String CURVES_TYPE_RED = "RED";
    public static final String CURVES_TYPE_GREEN = "GREEN";
    public static final String CURVES_TYPE_BLUE = "BLUE";
    private int mVer = 1;

    /**
     * 전체 channel에 대한 curve 설정
     * 
     * @version 2.0
     */
    public ArrayList<CurvesPoint> mAll;
    /**
     * Red channel에 대한 curve 설정
     * 
     * @version 2.0
     */
    public ArrayList<CurvesPoint> mRed;
    /**
     * Red channel에 대한 curve 설정
     * 
     * @version 2.0
     */
    public ArrayList<CurvesPoint> mGreen;
    /**
     * Red channel에 대한 curve 설정
     * 
     * @version 2.0
     */
    public ArrayList<CurvesPoint> mBlue;

    // adjust
    /**
     * 밝기 값으로 -100~100의 값을 가진다.
     * 
     * @remark 0: 기본 값<br>
     *         -100: 가장 어두운 값<br>
     *         100: 가장 밝은 값
     * @version 2.0
     */
    public int mBrightness = 0;
    /**
     * 대비 값으로 0.5~1.5의 값을 가진다.
     * 
     * @remark 1.0: 기본 값<br>
     *         0.5: 낮은 대비 값<br>
     *         1.5: 높은 대비 값
     * @version 2.0
     */
    public float mContrast = 1;
    /**
     * 채도 값으로 0.0~2.0의 값을 가진다.
     * 
     * @remark 1.0: 기본 값<br>
     *         0.0: 낮은 채도 값<br>
     *         1.0: 높은 채도 값
     * @version 2.0
     */
    public float mSaturation = 1;

    /**
     * 비네트 이미지 리소스의 이름
     * 
     * @version 2.0
     */
    public String mVignetteName = "none";
    /**
     * 비네트 이미지의 투명도 0~100의 값을 가진다.
     * 
     * @remark 0: 투명<br>
     *         50: 반 투명<br>
     *         100: 불투명
     * @version 2.0
     */
    public int mVignetteAlpha = 0;
    // texture

    /**
     * 텍스처 이미지 리소스의 이름
     * 
     * @version 2.0
     */
    public String mTextureName = "none";
    /**
     * 텍스처 이미지의 투명도 0~100의 값을 가진다.
     * 
     * @remark 0: 투명<br>
     *         50: 반 투명<br>
     *         100: 불투명
     * @version 2.0
     */
    public int mTextureAlpha = 0;

    /**
     * 프레임 이미지 리소스의 이름
     * 
     * @version 2.0
     */
    public String mFrameName = "none";
    /**
     * U+Camera에서 사용. 상단 프레임 이미지 리소스 이름
     */
    public String mTopFrameName = "none";
    /**
     * U+Camera에서 사용. 하단 프레임 이미지 리소스 이름
     */
    public String mBottomFrameName = "none";

    /**
     * 흑백 모드
     * 
     * @version 2.0
     */
    public boolean mBWMode = false;

    /**
     * 아트 필터 효과에 대한 정보를 저장
     * 
     * @version 2.0
     */
    public ArtFilter mArtFilter = new ArtFilter();

    public Filter() {
        mAll = new ArrayList<CurvesPoint>();
        mRed = new ArrayList<CurvesPoint>();
        mGreen = new ArrayList<CurvesPoint>();
        mBlue = new ArrayList<CurvesPoint>();
    }

    public ArrayList<CurvesPoint> getCurvesPoints(String CURVES_TYPE) {
        if(CURVES_TYPE_ALL.equalsIgnoreCase(CURVES_TYPE)) {
            return mAll;
        } else if(CURVES_TYPE_RED.equalsIgnoreCase(CURVES_TYPE)) {
            return mRed;
        } else if(CURVES_TYPE_GREEN.equalsIgnoreCase(CURVES_TYPE)) {
            return mGreen;
        } else if(CURVES_TYPE_BLUE.equalsIgnoreCase(CURVES_TYPE)) {
            return mBlue;
        } else {
            return null;
        }
    }

    public void parse(JsonParser jp) throws Exception {
        jp.nextToken();
        while(jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();

            if(jp.nextToken() == JsonToken.VALUE_NULL) {
                continue;
            }

            if(fieldName.equalsIgnoreCase("VER")) {
                mVer = jp.getIntValue();
            } else if(fieldName.equalsIgnoreCase("CURVES")) {
                parseFilterCurves(jp);
            } else if(fieldName.equalsIgnoreCase("SATURATION")) {
                mSaturation = Double.valueOf(jp.getDoubleValue()).floatValue();
            } else if(fieldName.equalsIgnoreCase("BRIGHTNESS")) {
                mBrightness = jp.getIntValue();
            } else if(fieldName.equalsIgnoreCase("CONTRAST")) {
                mContrast = jp.getFloatValue();
            } else if(fieldName.equalsIgnoreCase("VIGNETTE")) {
                parseFilterVignette(jp);
            } else if(fieldName.equalsIgnoreCase("TEXTURE")) {
                parseFilterTexture(jp);
            } else if(fieldName.equalsIgnoreCase("FRAMENAME")) {
                mFrameName = jp.getText();
            } else if(fieldName.equalsIgnoreCase("TOP_FRAMENAME")) {
                mTopFrameName = jp.getText();
            } else if(fieldName.equalsIgnoreCase("BOTTOM_FRAMENAME")) {
                mBottomFrameName = jp.getText();
            } else if(fieldName.equalsIgnoreCase("BWMODE")) {
                if(jp.getIntValue() == 1) {
                    mBWMode = true;
                } else {
                    mBWMode = false;
                }
            } else if(fieldName.equalsIgnoreCase("ARTFILTER")) {
                ArtFilter artFilter = new ArtFilter();
                artFilter.parse(jp);
                mArtFilter = artFilter;
            }
        }
        if(mArtFilter == null) {
            mArtFilter = new ArtFilter();
        }
    }

    private void parseFilterCurves(JsonParser jp) throws Exception {
        while(jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();

            if(jp.nextToken() == JsonToken.VALUE_NULL) {
                continue;
            }

            if(fieldName.equalsIgnoreCase("ALL")) {
                mAll = new ArrayList<CurvesPoint>();
                _parseCurves(jp, mAll);
            } else if(fieldName.equalsIgnoreCase("R")) {
                mRed = new ArrayList<CurvesPoint>();
                _parseCurves(jp, mRed);
            } else if(fieldName.equalsIgnoreCase("G")) {
                mGreen = new ArrayList<CurvesPoint>();
                _parseCurves(jp, mGreen);
            } else if(fieldName.equalsIgnoreCase("B")) {
                mBlue = new ArrayList<CurvesPoint>();
                _parseCurves(jp, mBlue);
            }
        }
    }

    private static void _parseCurves(JsonParser jp, ArrayList<CurvesPoint> array) throws Exception {
        int count = 0;
        short pointX = 0;
        short pointY = 0;
        while(jp.nextToken() != JsonToken.END_ARRAY) {
            while(jp.nextToken() != JsonToken.END_ARRAY) {
                if(jp.getCurrentToken() == JsonToken.VALUE_NULL) {
                    continue;
                }

                ++count;
                if(count == 1) {
                    if(jp.getNumberType() == JsonParser.NumberType.DOUBLE) {
                        pointX = Double.valueOf(jp.getDoubleValue()).shortValue();
                    } else if(jp.getNumberType() == JsonParser.NumberType.INT) {
                        pointX = jp.getShortValue();
                    } else {
                        throw new JsonParseException("Curve parsing error", jp.getTokenLocation());
                    }
                } else if(count == 2) {
                    if(jp.getNumberType() == JsonParser.NumberType.DOUBLE) {
                        pointY = Double.valueOf(jp.getDoubleValue()).shortValue();
                    } else if(jp.getNumberType() == JsonParser.NumberType.INT) {
                        pointY = jp.getShortValue();
                    } else {
                        throw new JsonParseException("Curve parsing error", jp.getTokenLocation());
                    }
                    CurvesPoint cp = new CurvesPoint();
                    cp.mX = pointX;
                    cp.mY = pointY;
                    array.add(cp);

                    count = 0;
                    pointX = 0;
                    pointY = 0;
                }
            }
        }
    }

    private void parseFilterVignette(JsonParser jp) throws JsonParseException, IOException {
        while(jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();
            if(jp.nextToken() == JsonToken.VALUE_NULL) {
                continue;
            }

            if(fieldName.equalsIgnoreCase("NAME")) {
                mVignetteName = jp.getText();
            } else if(fieldName.equalsIgnoreCase("ALPHA")) {
                mVignetteAlpha = jp.getIntValue();
            }
        }
    }

    private void parseFilterTexture(JsonParser jp) throws JsonParseException, IOException {
        while(jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();
            if(jp.nextToken() == JsonToken.VALUE_NULL) {
                continue;
            }

            if(fieldName.equalsIgnoreCase("NAME")) {
                mTextureName = jp.getText();
            } else if(fieldName.equalsIgnoreCase("ALPHA")) {
                mTextureAlpha = jp.getIntValue();
            }
        }
    }

    /**
     * Desc : Filter 객체를 JSonString 으로 출력
     * 
     * @Method Name : toJsonString
     * @return String(JSon type)
     */
    public String toJsonString() {
        int size = 0;
        int i = 0;

        JSONObject filter = new JSONObject();
        JSONObject object = new JSONObject();
        JSONArray array1;
        JSONArray array2;
        try {
            filter.put("VER", mVer);
            // curve_all
            if(mAll != null && mAll.size() != 0) {
                array1 = new JSONArray();
                size = mAll.size();
                i = 0;
                for(i = 0; i < size; i++) {
                    array2 = new JSONArray();
                    array2.put(mAll.get(i).mX);
                    array2.put(mAll.get(i).mY);
                    array1.put(array2);
                }
                object.put("ALL", array1);
            } else {
                object.put("ALL", getDefaultCurvePoints());
            }

            // curve_red
            if(mRed != null && mRed.size() != 0) {
                array1 = new JSONArray();
                size = mRed.size();
                i = 0;
                for(i = 0; i < size; i++) {
                    array2 = new JSONArray();
                    array2.put(mRed.get(i).mX);
                    array2.put(mRed.get(i).mY);
                    array1.put(array2);
                }
                object.put("R", array1);
            } else {
                object.put("R", getDefaultCurvePoints());
            }

            // curve_green
            if(mGreen != null && mGreen.size() != 0) {
                array1 = new JSONArray();
                size = mGreen.size();
                i = 0;
                for(i = 0; i < size; i++) {
                    array2 = new JSONArray();
                    array2.put(mGreen.get(i).mX);
                    array2.put(mGreen.get(i).mY);
                    array1.put(array2);
                }
                object.put("G", array1);
            } else {
                object.put("G", getDefaultCurvePoints());
            }

            // curve_blue
            if(mBlue != null && mBlue.size() != 0) {
                array1 = new JSONArray();
                size = mBlue.size();
                i = 0;
                for(i = 0; i < size; i++) {
                    array2 = new JSONArray();
                    array2.put(mBlue.get(i).mX);
                    array2.put(mBlue.get(i).mY);
                    array1.put(array2);
                }
                object.put("B", array1);
            } else {
                object.put("B", getDefaultCurvePoints());
            }

            filter.put("CURVES", object);
            filter.put("BRIGHTNESS", mBrightness);
            filter.put("SATURATION", mSaturation);
            filter.put("CONTRAST", mContrast);

            object = new JSONObject();
            object.put("NAME", mVignetteName);
            object.put("ALPHA", mVignetteAlpha);
            filter.put("VIGNETTE", object);

            object = new JSONObject();
            object.put("NAME", mTextureName);
            object.put("ALPHA", mTextureAlpha);
            filter.put("TEXTURE", object);

            filter.put("FRAMENAME", mFrameName);
            filter.put("TOP_FRAMENAME", mTopFrameName);
            filter.put("BOTTOM_FRAMENAME", mBottomFrameName);
            if(mBWMode) {
                filter.put("BWMODE", 1);
            } else {
                filter.put("BWMODE", 0);
            }
            if(!TextUtils.isEmpty(mArtFilter.mFilterName)) {
                filter.put("ARTFILTER", mArtFilter.getJsonObject());
            }
            return filter.toString();
        } catch(Exception e) {
            return null;
        }
    }

    private static JSONArray getDefaultCurvePoints() {
        JSONArray array1 = new JSONArray();
        JSONArray array2 = new JSONArray();

        array2.put(0);
        array2.put(0);
        array1.put(array2);

        array2 = new JSONArray();
        array2.put(255);
        array2.put(255);
        array1.put(array2);

        return array1;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        Object obj = null;
        try {
            obj = super.clone();
        } catch(Exception e) {
        }
        return obj;
    }

    public Filter(Parcel in) {
        mAll = new ArrayList<CurvesPoint>();
        Object[] p = in.readParcelableArray(CurvesPoint.class.getClassLoader());
        for(Object po : p) {
            mAll.add((CurvesPoint)po);
        }

        mRed = new ArrayList<CurvesPoint>();
        p = in.readParcelableArray(CurvesPoint.class.getClassLoader());
        for(Object po : p) {
            mRed.add((CurvesPoint)po);
        }

        mGreen = new ArrayList<CurvesPoint>();
        p = in.readParcelableArray(CurvesPoint.class.getClassLoader());
        for(Object po : p) {
            mGreen.add((CurvesPoint)po);
        }

        mBlue = new ArrayList<CurvesPoint>();
        p = in.readParcelableArray(CurvesPoint.class.getClassLoader());
        for(Object po : p) {
            mBlue.add((CurvesPoint)po);
        }

        mBrightness = in.readInt();
        mContrast = in.readFloat();
        mSaturation = in.readFloat();
        mVignetteName = in.readString();
        mVignetteAlpha = in.readInt();
        mTextureName = in.readString();
        mTextureAlpha = in.readInt();
        mFrameName = in.readString();
        mTopFrameName = in.readString();
        mBottomFrameName = in.readString();
        mBWMode = (Boolean)in.readValue(Boolean.class.getClassLoader());

        mArtFilter = (ArtFilter)in.readParcelable(ArtFilter.class.getClassLoader());
        if(mArtFilter == null) {
            mArtFilter = new ArtFilter();
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelableArray(mAll.toArray(new CurvesPoint[mAll.size()]),
                                  Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelableArray(mRed.toArray(new CurvesPoint[mRed.size()]),
                                  Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelableArray(mGreen.toArray(new CurvesPoint[mGreen.size()]),
                                  Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        dest.writeParcelableArray(mBlue.toArray(new CurvesPoint[mBlue.size()]),
                                  Parcelable.PARCELABLE_WRITE_RETURN_VALUE);

        dest.writeInt(mBrightness);
        dest.writeFloat(mContrast);
        dest.writeFloat(mSaturation);
        dest.writeString(mVignetteName);
        dest.writeInt(mVignetteAlpha);
        dest.writeString(mTextureName);
        dest.writeInt(mTextureAlpha);
        dest.writeString(mFrameName);
        dest.writeString(mTopFrameName);
        dest.writeString(mBottomFrameName);
        dest.writeValue(mBWMode);

        dest.writeParcelable(mArtFilter, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
        if(mArtFilter == null) {
            mArtFilter = new ArtFilter();
        }
    }

    public static final FilterCreator CREATOR = new FilterCreator();

    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof Filter))
            return false;
        if(o.getClass() != this.getClass())
            return false;
        Filter obj = (Filter)o;

        if(obj.mAll == null)
            return false;
        if(obj.mRed == null)
            return false;
        if(obj.mGreen == null)
            return false;
        if(obj.mBlue == null)
            return false;
        if(obj.mAll.size() != mAll.size())
            return false;
        if(obj.mRed.size() != mRed.size())
            return false;
        if(obj.mGreen.size() != mGreen.size())
            return false;
        if(obj.mBlue.size() != mBlue.size())
            return false;
        if(obj.mBrightness != mBrightness)
            return false;
        if(obj.mContrast != mContrast)
            return false;
        if(obj.mSaturation != mSaturation)
            return false;
        if(!obj.mVignetteName.equals(mVignetteName))
            return false;
        if(obj.mVignetteAlpha != mVignetteAlpha)
            return false;
        if(!obj.mTextureName.equals(mTextureName))
            return false;
        if(obj.mTextureAlpha != mTextureAlpha)
            return false;
        if(!obj.mFrameName.equals(mFrameName))
            return false;
        if(!obj.mTopFrameName.equals(mTopFrameName))
            return false;
        if(!obj.mBottomFrameName.equals(mBottomFrameName))
            return false;
        if(obj.mBWMode != mBWMode)
            return false;
        if(obj.mArtFilter == null)
            return false;
        if(!obj.mArtFilter.equals(mArtFilter))
            return false;

        int allSize = mAll.size();
        for(int i = 0; i < allSize; i++) {
            CurvesPoint objcp = obj.mAll.get(i);
            CurvesPoint oriCp = mAll.get(i);
            if(objcp.mX != oriCp.mX)
                return false;
            if(objcp.mY != oriCp.mY)
                return false;
        }

        int redSize = mRed.size();
        for(int i = 0; i < redSize; i++) {
            CurvesPoint objcp = obj.mRed.get(i);
            CurvesPoint oriCp = mRed.get(i);
            if(objcp.mX != oriCp.mX)
                return false;
            if(objcp.mY != oriCp.mY)
                return false;
        }

        int greenSize = mGreen.size();
        for(int i = 0; i < greenSize; i++) {
            CurvesPoint objcp = obj.mGreen.get(i);
            CurvesPoint oriCp = mGreen.get(i);
            if(objcp.mX != oriCp.mX)
                return false;
            if(objcp.mY != oriCp.mY)
                return false;
        }

        int blueSize = mBlue.size();
        for(int i = 0; i < blueSize; i++) {
            CurvesPoint objcp = obj.mBlue.get(i);
            CurvesPoint oriCp = mBlue.get(i);
            if(objcp.mX != oriCp.mX)
                return false;
            if(objcp.mY != oriCp.mY)
                return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    public boolean isArtFilter() {
        if(mArtFilter != null && !TextUtils.isEmpty(mArtFilter.mFilterName)) {
            return true;
        }
        return false;
    }

    public boolean isLightArtFilter() {
        return isArtFilter() && ArtFilterUtils.sIsLiteMode;
    }

    public boolean isOverlayTexture() {
        for(String notOverlayTexture : NOT_OVERLAY_TEXTURE) {
            if(notOverlayTexture.equals(mTextureName)) {
                return false;
            }
        }
        return true;
    }

    public boolean needVignette() {
        return !TextUtils.isEmpty(mVignetteName) && !mVignetteName.equalsIgnoreCase("none")
                && mVignetteAlpha > 0 && mVignetteAlpha < 256;
    }

    public boolean needTexture() {
        return !TextUtils.isEmpty(mTextureName) && !mTextureName.equalsIgnoreCase("none")
                && mTextureAlpha > 0 && mTextureAlpha < 256;
    }

    public boolean neetFrame() {
        return !TextUtils.isEmpty(mFrameName) && !mFrameName.equalsIgnoreCase("none");
    }

    public boolean neetTopFrame() {
        return !TextUtils.isEmpty(mTopFrameName) && !mTopFrameName.equalsIgnoreCase("none");
    }

    public boolean neetBottomFrame() {
        return !TextUtils.isEmpty(mBottomFrameName) && !mBottomFrameName.equalsIgnoreCase("none");
    }
}
