package com.sugarmount.sugarcamera.story.gallery;

import com.sugarmount.sugarcamera.story.PublicVariable;

import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;


public class MediaData implements Parcelable {
	public int mediaType = PublicVariable.MEDIA_TYPE_IMAGE; // OR VIDEO
	public int headerRefIndex;
	public int listRefIndex;
	public long id;
	public String path;
	public String displayName;
	public String mimeType;
	public long size; // 파일 용량
	public int width;
	public int height;
	public long duration; // 동영상 재생 시간
	public long dateAdded; // 파일 등록일
	public boolean isChecked = false;
	public boolean isNewContent = false;
	public boolean isThumbnailLoadFail = false;
	public int degree = 0;
	public String file_id; // Server에 업로드 된 ID // Magisto Upload 시 화면에서 사용
	
	public MediaData() {
	}
	
	public MediaData(String path) {
		this.path = path;
	}
	
	public MediaData(Parcel in) {
		readFromParcel(in);
	}
	
	public void setMediaSize(String path) {
		if(TextUtils.isEmpty(path)) {
			return;
		}
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		this.width = options.outWidth;
		this.height = options.outHeight;
	}
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(mediaType);
		dest.writeInt(headerRefIndex);
		dest.writeInt(listRefIndex);
		dest.writeLong(id);
		dest.writeString(path);
		dest.writeString(displayName);
		dest.writeString(mimeType);
		dest.writeLong(size);
		dest.writeInt(width);
		dest.writeInt(height);
		dest.writeLong(duration);
		dest.writeLong(dateAdded);
		dest.writeByte((byte)(isNewContent ? 1 : 0));
		dest.writeByte((byte)(isThumbnailLoadFail ? 1 : 0));
		dest.writeInt(degree);
	}
	
	private void readFromParcel(Parcel in) {
		mediaType = in.readInt();
		headerRefIndex = in.readInt();
		listRefIndex = in.readInt();
		id = in.readLong();
		path = in.readString();
		displayName = in.readString();
		mimeType = in.readString();
		size = in.readLong();
		width = in.readInt();
		height = in.readInt();
		duration = in.readLong();
		dateAdded = in.readLong();
		isNewContent = (in.readByte() == 1 ? true : false);
		isThumbnailLoadFail = (in.readByte() == 1 ? true : false);
		degree = in.readInt();
	}
	
	public static final Parcelable.Creator<MediaData> CREATOR = new Parcelable.Creator<MediaData>() {
		@Override
		public MediaData createFromParcel(Parcel source) {
			return new MediaData(source);
		}
		@Override
		public MediaData[] newArray(int size) {
			return new MediaData[size];
		}
	};
}
