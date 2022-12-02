
package com.kiwiple.imageframework.filter;

import java.io.IOException;
import java.io.Serializable;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 필터의 상세 정보를 관리하는 클래스입니다.
 * 
 * @version 2.0
 */
public class FilterData implements Serializable, Parcelable {
    private static final long serialVersionUID = 3375772804006175213L;
    /**
     * 필터의 고유 번호
     * 
     * @version 2.0
     */
    public int mServerId = 0; // local & server

    /**
     * 필터가 적용되지 않은 원본 이미지에 대한 주소
     * 
     * @version 2.0
     */
    public String mOriginImageURL; // used for local & server, ImageUrl
    /**
     * 필터가 적용된 이미지에 대한 주소
     * 
     * @version 2.0
     */
    public String mFilterImageURL; // used for local & server, ImageUrl
    /**
     * 필터 이름
     * 
     * @version 2.0
     */
    public String mTitle; // local & server
    /**
     * 필터에 대한 설명
     * 
     * @version 2.0
     */
    public String mDescription; // local & server
    /**
     * 필터 제작자
     * 
     * @version 2.0
     */
    public String mSignature; // local & server

    /**
     * 필터 구성 요소에 대한 정보
     * 
     * @version 2.0
     */
    public Filter mFilter;
    /**
     * 필터 마켓에서의 다운로드 건수
     * 
     * @version 2.0
     */
    public int mDownloadCount;

    /**
     * 필터 제작일
     * 
     * @version 2.0
     */
    public String mCreateTime;

    /**
     * 필터카메라 친구가 사용한 필터 기능에서 사용. 친구의 카카오 아이디.
     */
    public String mFriendId;

    /**
     * 매직아워에서 사용, 즐겨찾기에 추가된 필터인지 여부
     */
    public boolean mFavorite;

    /**
     * 필터카메라에서 사용, 즐겨찾기 필터 순서 정보
     */
    public int mFavoriteOrder = -1;
    /**
     * 필터카메라에서 사용, 기본 필터 순서 정보
     */
    public int mPresetOrder = -1;
    /**
     * 필터카메라에서 사용, 다운로드 필터 순서 정보
     */
    public int mDownloadOrder = -1;

    public void parse(JsonParser jp) throws JsonParseException, IOException {
        while(jp.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jp.getCurrentName();

            if(jp.nextToken() == JsonToken.VALUE_NULL) {
                continue;
            }

            if(fieldName.equalsIgnoreCase("Id")) {
                mServerId = jp.getIntValue();
            } else if(fieldName.equalsIgnoreCase("Title")) {
                mTitle = jp.getText();
            } else if(fieldName.equalsIgnoreCase("Description")) {
                mDescription = jp.getText();
            } else if(fieldName.equalsIgnoreCase("Signature")) {
                mSignature = jp.getText();
            } else if(fieldName.equalsIgnoreCase("OriImage")) {
                mOriginImageURL = jp.getText();
            } else if(fieldName.equalsIgnoreCase("FilterImage")) {
                mFilterImageURL = jp.getText();
            } else if(fieldName.equalsIgnoreCase("Filter")) {
                String txt = jp.getText();
                JsonFactory f = new JsonFactory();
                try {
                    JsonParser jp2 = f.createJsonParser(txt);
                    Filter filter = new Filter();
                    filter.parse(jp2);
                    mFilter = filter;
                } catch(Exception e) {
                }
            } else if(fieldName.equalsIgnoreCase("DownloadCount")) {
                mDownloadCount = jp.getIntValue();
            } else if(fieldName.equalsIgnoreCase("CreateTime")) {
                mCreateTime = jp.getText();
            } else if(fieldName.equalsIgnoreCase("kakao_id")) {
                mFriendId = jp.getText();
            }
        }
    }

    public FilterData() {
        mFilter = new Filter();
    }

    public FilterData(Parcel in) {

        mServerId = in.readInt();

        mOriginImageURL = in.readString();
        mFilterImageURL = in.readString();
        mTitle = in.readString();
        mDescription = in.readString();
        mSignature = in.readString();

        mFilter = (Filter)in.readParcelable(Filter.class.getClassLoader());

        mDownloadCount = in.readInt();

        mCreateTime = in.readString();
        mFriendId = in.readString();

        mFavorite = (Boolean)in.readValue(Boolean.class.getClassLoader());

        mFavoriteOrder = in.readInt();
        mPresetOrder = in.readInt();
        mDownloadOrder = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(mServerId);

        dest.writeString(mOriginImageURL);
        dest.writeString(mFilterImageURL);
        dest.writeString(mTitle);
        dest.writeString(mDescription);
        dest.writeString(mSignature);

        dest.writeParcelable(mFilter, Parcelable.PARCELABLE_WRITE_RETURN_VALUE);

        dest.writeInt(mDownloadCount);

        dest.writeString(mCreateTime);
        dest.writeString(mFriendId);

        dest.writeValue(mFavorite);

        dest.writeInt(mFavoriteOrder);
        dest.writeInt(mPresetOrder);
        dest.writeInt(mDownloadOrder);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<FilterData> CREATOR = new Creator<FilterData>() {

        @Override
        public FilterData createFromParcel(Parcel source) {
            return new FilterData(source);
        }

        @Override
        public FilterData[] newArray(int size) {
            return new FilterData[size];
        }
    };
}
