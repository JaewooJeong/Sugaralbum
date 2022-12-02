package com.sugarmount.sugarcamera.story.story;

import android.net.Uri;

public class StoryData {
	private long mDate;
	private Uri mStoryUri;
	private Integer mStoryId;
	private String mStorySchedulerVersion;
	private String mStoryTitle;
	private Integer mStoryDuration;
	private Integer mStoryOrientation;
	private String mStoryJsonString;
	private String mStoryThemeName;
	
	public StoryData(long date, Uri uri, int id, String version, String title, int duration, int orientation, String json, String themeName){
		this.mDate = date; 
		this.mStoryUri = uri; 
		this.mStoryId = id; 
		this.mStorySchedulerVersion = version; 
		this.mStoryTitle = title; 
		this.mStoryDuration = duration; 
		this.mStoryOrientation = orientation; 
		this.mStoryJsonString = json; 
		this.mStoryThemeName = themeName; 
	}

	public Uri getStoryUri() {
		return mStoryUri;
	}

	public Integer getStoryId() {
		return mStoryId;
	}

	public String getStoryTitle() {
		return mStoryTitle;
	}

	public Integer getStoryDuration() {
		return mStoryDuration;
	}
}
