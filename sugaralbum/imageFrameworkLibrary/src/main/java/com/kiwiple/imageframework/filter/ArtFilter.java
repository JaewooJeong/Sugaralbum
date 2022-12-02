
package com.kiwiple.imageframework.filter;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.json.JSONArray;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;

/**
 * 아트 필터 효과에 대한 정보를 저장 하기 위한 class
 * 
 * @version 2.0
 */
public class ArtFilter implements Serializable, Parcelable {
    private static final long serialVersionUID = 3168519639484664647L;

    /**
     * 아트 필터 이름
     * 
     * @version 2.0
     */
    public String mFilterName = "";
    /**
     * 아트 필터에 대한 설명
     * 
     * @version 2.0
     */
    public String mDescription = "";
    public boolean mArtEffectPlus;
    /**
     * 아트 필터에서 사용되는 파라미터 개수
     * 
     * @version 2.0
     */
    public int mParamCount = 0;
    /**
     * 아트 필터에서 사용되는 파라미터 값
     * 
     * @version 2.0
     */
    public ArrayList<String> mParams = new ArrayList<String>();
    /**
     * 필터카메라 인앱 결제용.
     */
    @Deprecated
    public String mTStoreId = "";
    /**
     * 필터카메라 인앱 결제용.
     */
    @Deprecated
    public String mOllehId = "";
    /**
     * 필터카메라 인앱 결제용.
     */
    @Deprecated
    public String mPlayId = "";

    public ArtFilter() {
    }

    public ArtFilter copy() {
        ArtFilter artFilter = new ArtFilter();
        artFilter.mFilterName = this.mFilterName;
        artFilter.mDescription = this.mDescription;
        artFilter.mArtEffectPlus = this.mArtEffectPlus;
        artFilter.mParamCount = this.mParamCount;
        artFilter.mParams = this.mParams;
        artFilter.mParams = new ArrayList<String>();
        for(int i = 0; i < mParamCount; i++) {
            artFilter.mParams.add(this.mParams.get(i));
        }
        artFilter.mTStoreId = this.mTStoreId;
        artFilter.mOllehId = this.mOllehId;
        artFilter.mPlayId = this.mPlayId;
        return artFilter;
    }

    public ArtFilter(Parcel in) {
        mFilterName = (String)in.readValue(String.class.getClassLoader());
        mDescription = (String)in.readValue(String.class.getClassLoader());
        mArtEffectPlus = (Boolean)in.readValue(Boolean.class.getClassLoader());
        mParamCount = in.readInt();
        Bundle b = in.readBundle(String.class.getClassLoader());
        mParams = b.getStringArrayList("params");
        mTStoreId = (String)in.readValue(String.class.getClassLoader());
        mOllehId = (String)in.readValue(String.class.getClassLoader());
        mPlayId = (String)in.readValue(String.class.getClassLoader());
    }

    public void parse(JsonParser jp) throws JsonParseException, IOException {
        while(jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();

            if(jp.nextToken() == JsonToken.VALUE_NULL) {
                continue;
            }

            if(fieldName.equals("TITLE")) {
                mFilterName = jp.getText();
            } else if(fieldName.equals("DESCRIPTION")) {
                mDescription = jp.getText();
            } else if(fieldName.equals("ARTFILTERPLUS")) {
                if(jp.getIntValue() == 1) {
                    mArtEffectPlus = true;
                } else {
                    mArtEffectPlus = false;
                }
            } else if(fieldName.equals("PARAMCOUNT")) {
                mParamCount = jp.getIntValue();
            } else if(fieldName.equals("PARAMS")) {
                if(mParams != null) {
                    try {
                        while(jp.nextToken() != JsonToken.END_ARRAY) {
                            if(jp.getCurrentToken() == JsonToken.VALUE_NULL) {
                                continue;
                            }
                            mParams.add(jp.getText());
                        }
                    } catch(IndexOutOfBoundsException e) {
                    }
                }
            } else if(fieldName.equals("TS_P_ID")) {
                mTStoreId = jp.getText();
            } else if(fieldName.equals("OL_P_ID")) {
                mOllehId = jp.getText();
            } else if(fieldName.equals("PLAY_P_ID")) {
                mPlayId = jp.getText();
            }
        }
    }

    public static ArtFilter parse(ArtFilterInfo filterInfo) {
        ArtFilter artFilter = new ArtFilter();
        if(filterInfo == null) {
            return artFilter;
        }
        artFilter.mFilterName = filterInfo.filterName;
        artFilter.mArtEffectPlus = filterInfo.customFilter;
        artFilter.mParamCount = filterInfo.progressInfo != null ? filterInfo.progressInfo.size()
                : 0;
        artFilter.mParams = new ArrayList<String>();
        for(int i = 0; i < artFilter.mParamCount; i++) {
            artFilter.mParams.add(String.valueOf(filterInfo.progressInfo.get(i).progressDefaut));
        }
        artFilter.mTStoreId = filterInfo.tstoreId;
        artFilter.mOllehId = filterInfo.ollehId;
        artFilter.mPlayId = filterInfo.playId;
        return artFilter;
    }

    /**
     * Desc : Filter 객체를 JSonString 으로 출력
     * 
     * @Method Name : toJsonString
     * @return String(JSon type)
     */
    public JSONObject getJsonObject() {
        JSONObject artFilter = new JSONObject();
        try {
            artFilter.put("TITLE", mFilterName);
            artFilter.put("DISCRIPTION", mDescription);
            if(mArtEffectPlus) {
                artFilter.put("ARTFILTERPLUS", 1);
            } else {
                artFilter.put("ARTFILTERPLUS", 0);
            }
            artFilter.put("PARAMCOUNT", mParams.size());

            JSONArray object = new JSONArray();
            if(mParams != null && mParams.size() != 0) {
                int size = mParams.size();
                for(int i = 0; i < size; i++) {
                    object.put(mParams.get(i));
                }
            }
            artFilter.put("PARAMS", object);
            artFilter.put("TS_P_ID", mTStoreId);
            artFilter.put("OL_P_ID", mOllehId);
            artFilter.put("PLAY_P_ID", mPlayId);
            return artFilter;
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(mFilterName);
        dest.writeValue(mDescription);
        dest.writeValue(mArtEffectPlus);
        dest.writeInt(mParamCount);
        Bundle b = new Bundle();
        b.putStringArrayList("params", mParams);
        dest.writeBundle(b);
        dest.writeValue(mTStoreId);
        dest.writeValue(mOllehId);
        dest.writeValue(mPlayId);
    }

    public static final Parcelable.Creator<ArtFilter> CREATOR = new Creator<ArtFilter>() {
        @Override
        public ArtFilter[] newArray(int size) {
            return new ArtFilter[size];
        }

        @Override
        public ArtFilter createFromParcel(Parcel source) {
            return new ArtFilter(source);
        }
    };

    @Override
    public boolean equals(Object o) {
        if(o == null || !(o instanceof ArtFilter)) {
            return false;
        }
        ArtFilter artFilter = (ArtFilter)o;
        if(!mFilterName.equals(artFilter.mFilterName))
            return false;
        if(!mDescription.equals(artFilter.mDescription))
            return false;
        if(mArtEffectPlus != artFilter.mArtEffectPlus)
            return false;
        if(artFilter.mParams == null)
            return false;
        if(mParamCount != artFilter.mParamCount)
            return false;
        if(!mTStoreId.equals(artFilter.mTStoreId))
            return false;
        if(!mOllehId.equals(artFilter.mOllehId))
            return false;
        if(!mPlayId.equals(artFilter.mPlayId))
            return false;

        for(int i = 0; i < mParams.size(); i++) {
            String objcp = artFilter.mParams.get(i);
            String oriCp = mParams.get(i);
            if(!objcp.equals(oriCp))
                return false;
        }
        return super.equals(o);
    }

    @Override
    public int hashCode() {
        return mFilterName.hashCode();
    }

    public float[] getParams() {
        if(mParams.size() == 0) {
            return null;
        }
        int size = mParams.size();
        float[] params = new float[size];
        for(int i = 0; i < size; i++) {
            params[i] = Float.valueOf(mParams.get(i));
        }
        return params;
    }
}
