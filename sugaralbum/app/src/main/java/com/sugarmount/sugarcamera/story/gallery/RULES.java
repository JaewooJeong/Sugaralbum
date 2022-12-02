package com.sugarmount.sugarcamera.story.gallery;

import com.kiwiple.scheduler.analysis.uplus.UplusImageAnalysis;
import com.sugarmount.sugarcamera.story.PublicVariable;
import com.sugarmount.sugarcamera.story.utils.KiwiplePreferenceManager;
import com.sugarmount.sugarcamera.utils.DeviceHelper;

public class RULES {
	
	public static final long MIN_DURATION_PER_VIDEO = 10000;	
	public static final long MAX_DURATION_PER_VIDEO = 120000;
	
	public static final int FULL_HD_WIDTH = 1920; 
	public static final int FULL_HD_HEIGHT = 1080; 
	
	public static final Rule none = new Rule();
	public static final Rule makeClip = new Rule(); 
	public static final Rule selectPhoto = new Rule();
	public static final Rule selectVideo = new Rule();
	
	
	
	
	public static void init() {
		setNone();
		setMakeMovie(); 
		setEditClip(); 
		setSelectVideoClip(); 
	}
	
	private static void setSelectVideoClip(){
		selectVideo.photo.used = false; 
		selectVideo.video.used = true;
		selectVideo.video.minCount = 0;
		selectVideo.video.maxCount = 1;
		selectVideo.video.maxSize = Long.MAX_VALUE;
		selectVideo.video.minDuration = 10000;
		selectVideo.video.maxDuration = 120000;
		selectVideo.video.maxWidth =  FULL_HD_WIDTH; 
		selectVideo.video.maxHeight = FULL_HD_HEIGHT;
	}

	public static int getSelectVideoMaxCount(){
		return selectVideo.video.maxCount; 
	}
	public static void setSelectVideoMaxCount(int max){
		selectVideo.video.maxCount = max;
	}
	
	private static void setMakeMovie() {
		makeClip.photo.used = true; 
		makeClip.photo.minCount = 0; 
		if(DeviceHelper.isMaxiumLimitationReleaseDevice())
			makeClip.photo.maxCount = UplusImageAnalysis.MAXIMUM_LIMITATION_IMAGE_COUNT; 
		else 
			makeClip.photo.maxCount = UplusImageAnalysis.MAXIMUM_IMAGE_COUNT; 
			
		makeClip.photo.maxSize = Long.MAX_VALUE;
		makeClip.video.used = true;
		makeClip.video.minCount = 0;
		makeClip.video.maxCount = DeviceHelper.MAX_VIDEO_COUNT;
		makeClip.video.maxSize = Long.MAX_VALUE;
		makeClip.video.minDuration = 10000;
		makeClip.video.maxDuration = 120000;
		makeClip.video.maxWidth =  FULL_HD_WIDTH; 
		makeClip.video.maxHeight = FULL_HD_HEIGHT; 
		
	}
	
	public static void setVideoMaxSize(int maxWidth, int maxHeight){
		makeClip.video.maxWidth = maxWidth; 
		makeClip.video.maxHeight = maxHeight; 
		
		selectVideo.video.maxWidth =  maxWidth; 
		selectVideo.video.maxHeight = maxHeight;
	}
	
	public static int getClipVideoMaxWidth(){
		return makeClip.video.maxWidth; 
	}
	
	public static int getClipVideoMaxHeight(){
		return makeClip.video.maxHeight; 
	}

	public static void setMakeClipMaxVideoCount(int max){
		makeClip.video.maxCount = max; 
	}
	public static int getMakeClipMaxVideoCount(){
		return makeClip.video.maxCount; 
	}
	
	public static int getMakeClipMaxPhotoCount(){
		return makeClip.photo.maxCount; 
	}
	
	private static void setEditClip(){
		selectPhoto.photo.used = true; 
		selectPhoto.photo.minCount = 1; 
		selectPhoto.photo.maxCount = 9;
		selectPhoto.photo.maxSize = Long.MAX_VALUE;
		selectPhoto.video.used = false; 
	}
	
	public static void setEditClipMinCount(int min){
		selectPhoto.photo.minCount = min; 
	}
	public static void setEditClipMaxCount(int max){
		selectPhoto.photo.maxCount = max; 
	}
	public static int getEditClipMaxCount(){
		return selectPhoto.photo.maxCount; 
	}

	private static void setNone() {
		none.photo.used = true;
		none.photo.minCount = 0;
		none.photo.maxCount = Integer.MAX_VALUE;
		none.photo.maxSize = Long.MAX_VALUE;
		none.video.used = true;
		none.video.minCount = 0;
		none.video.maxCount = Integer.MAX_VALUE;
		none.video.maxSize = Long.MAX_VALUE;
		none.video.minDuration = 0;
		none.video.maxDuration = Long.MAX_VALUE;
	}


	
	public static class Rule {
		public Photo photo = new Photo();
		public Video video = new Video();
		
		public static class Photo {
			public boolean used;
			public int minCount;
			public int maxCount;
			public long maxSize;
		}
		
		public static class Video {
			public boolean used;
			public int minCount;
			public int maxCount;
			public long maxSize;
			public long minDuration;
			public long maxDuration;
			public int maxWidth;
			public int maxHeight; 
		}
	}
	
}
