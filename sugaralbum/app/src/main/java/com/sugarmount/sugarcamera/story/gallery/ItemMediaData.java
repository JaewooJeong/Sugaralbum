package com.sugarmount.sugarcamera.story.gallery;

import android.net.Uri;

public class ItemMediaData {
	
	public Long _id;	// cursor id
	public String displayName;
	public String contentPath;
	public Uri contentUri;
	public String folderName; 
	public long contentSize;
	public boolean isVideo;
	//cursor position
	public int position; 
	/**
	 * 비어있는 기념일 Rowmove 식별자 
	 */
	public boolean isEmptyAnniversary;
	/*
	 * 5초 이하나 2분 이상의 동영상은 사용할 수 없다. 
	 */
	public boolean isEnableVideo; 
	public String mimeType;
	public long duration;	// video or animation gif
	public int degrees;
	public int width;
	public int height; 
	// checked
	public boolean checked;
	public int rowPosition;	// 본 아이템이 포함된 row의 position
	public int groupPosition; // 본 아이템이 포함된 group의 position
	// content validate
	public boolean invalid;
	public long date;
	public boolean isOutOfVideoCondition = false; 
	
	public String personName = ""; 
	public int personId; 
	public String anniversaryName ="";

}