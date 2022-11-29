package com.sugarmount.sugarcamera.story.gallery;

import android.content.Context;
import android.net.Uri;

import com.kiwiple.debug.L;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.search.ImageSearch;
import com.kiwiple.multimedia.util.ImageUtils;
import com.sugarmount.sugarcamera.story.PublicVariable;
import com.sugarmount.sugarcamera.story.gallery.RULES.Rule;
import com.sugarmount.sugarcamera.story.utils.KiwiplePreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

public class SelectManager {

	private static final String TAG = "ImageResData";
	public static final String SELECTED_ITEMS = "selected_items";
	public static final String SELECTED_RESOLUTION = "resolution";
	public static final String OUTPUT_DIR = "output_dir";
	public static final String ERROR_CODE = "error_code";
	public static final String FILE_URI = "file_uri";
	public static final String SELECTED_IMAGES = "selected_images";
	public static final String SELECTED_VIDEOS = "selected_videos";
	public static final String SELECTED_URL = "selected_url";
	public static final String SELECTED_TITLE = "selected_title";

	public static enum SelectMode {
		NONE(0), MAKE_CLIP(1), SELECT_PHOTO(2), SELECT_VIDEO(3);

		private int _value;

		SelectMode(int value) {
			_value = value;
		}

		public int getValue() {
			return _value;
		}
	}

	public static interface OnSelectManagerCallback {
		public void onRulesChanged(boolean enablePhoto, boolean enableVideo, long minDurationPerVideo, long maxDurationPerVideo,
				long maxSizePerVideo, boolean enableBtnSelectAction);

		public void onDataChanged(SelectMode mode, int totalCount, long totalSize, int photoCount, int videoCount, long totalVideoDuration);
	}

	private static SelectManager mInstance;

	public static SelectManager getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new SelectManager(context);
		}
		return mInstance;
	}

	private Context mContext;
	private SelectMode mSelectMode = SelectMode.MAKE_CLIP;
	private ArrayList<ItemMediaData> mItems = new ArrayList<ItemMediaData>();
	private long mPhotoSize, mVideoSize, mVideoDuration;
	private int mPhotoCount, mVideoCount;
	private ArrayList<OnSelectManagerCallback> mCallbacks = new ArrayList<SelectManager.OnSelectManagerCallback>();
	private Rule mRule;

	// private ShareListDialog.ShareItem mShareAppInfo; // 공유기능에서 선택한 app의 정보를
	// 저장함.

	public SelectManager(Context context) {
		mContext = context;
		
		RULES.init();
		
		//비디오 최대 지원 사이즈 설정. 
		int maxWidth = KiwiplePreferenceManager.getInstance(mContext).getValue("max_width", PublicVariable.BASE_RECORDING_HD_RESOLUTION_WIDTH);
		int maxHeight = KiwiplePreferenceManager.getInstance(mContext).getValue("max_height", PublicVariable.BASE_RECORDING_HD_RESOLUTION_HEIGHT);
		RULES.setVideoMaxSize(maxWidth, maxHeight); 
	}

	public SelectManager init() {
		mVideoCount = 0; 
		mPhotoCount = 0; 
		mSelectMode = SelectMode.MAKE_CLIP;
		mCallbacks.clear();
		clear();
		mRule = RULES.makeClip;
		return mInstance;
	}

	/*
	 * public void setShareAppInfo(ShareListDialog.ShareItem app) {
	 * mShareAppInfo = app; }
	 * 
	 * public ShareListDialog.ShareItem getShareAppInfo() { return
	 * mShareAppInfo; }
	 */

	public void setSelectMode(SelectMode mode) {
		mSelectMode = mode;
		// set current rule
		switch (mSelectMode) {

		case MAKE_CLIP:
			mRule = RULES.makeClip;
			break;
		case SELECT_PHOTO:
			mRule = RULES.selectPhoto;
			break;
		case SELECT_VIDEO:
			mRule = RULES.selectVideo;
			break;
		default:
			mRule = RULES.none;
			break;
		}
		notifyDataSetChanged();
	}

	public SelectMode getSelectMode() {
		return mSelectMode;
	}

	public boolean isSelectMode() {
		return mSelectMode != SelectMode.NONE;
	}

	public boolean isSelectModeForNoRule() {
		return mSelectMode == SelectMode.NONE;
	}

	public void clear() {
		mItems.clear();
		mPhotoSize = mVideoSize = mVideoDuration = 0;
		mPhotoCount = mVideoCount = 0;
	}

	public int getCount() { // for
		return mItems.size();
	}

	public int getPhotoCount() {
		return mPhotoCount;
	}

	public ItemMediaData getItem(int position) { // for
													// class
		if (position < mItems.size()) {
			return mItems.get(position);
		}
		return null;
	}

	public boolean validate(ItemMediaData item) {
		
		if(item.invalid){
			return false; 
		}
		
		if (item.isVideo) {
			if (!mRule.video.used || mRule.video.maxCount < mVideoCount + 1 || mRule.video.maxSize < mVideoSize + item.contentSize
					|| mRule.video.minDuration > item.duration || mRule.video.maxDuration < item.duration) {
				return false;
			}
		} else {
			if (!mRule.photo.used || mRule.photo.maxCount < mPhotoCount) {
				return false;
			}
		}
		return true;
	}

	public boolean testAdd(ItemMediaData item) {
		
		if (item.isVideo) {			
			L.i("select : "+ item.width+", "+ item.height +", limit :"+ mRule.video.maxWidth +", "+ mRule.video.maxHeight);
			if (!mRule.video.used || mRule.video.maxCount < mVideoCount + 1 || mRule.video.maxSize < mVideoSize + item.contentSize
					|| mRule.video.minDuration > item.duration || mRule.video.maxDuration < item.duration || item.invalid 
					|| item.width * item.height  > (mRule.video.maxWidth * mRule.video.maxHeight)) {
				
					// fixes_#11250 (keylime_20150331) : for FHD test
					//|| item.width * item.height  >= (1920 * 1080)
				//	) {
				return false;
			}
			
			mVideoCount++;
			mVideoSize += item.contentSize;
			mVideoDuration += item.duration;
		} else {
			if (!mRule.photo.used || mRule.photo.maxCount < mPhotoCount + 1 || item.invalid) {
				return false;
			}
			mPhotoCount++;
			mPhotoSize += item.contentSize;
		}
		mItems.add(item);
		
		if(mSelectMode == SelectMode.SELECT_PHOTO) {
			if(mRule.photo.maxCount == getCount()) {
			}
		}
		
		return true;
	}

	public void removed(ItemMediaData item) {
		for (int i = 0; i < mItems.size(); i++) {
			if (mItems.get(i)._id == item._id) {
				mItems.remove(i);
				// update total value
				if (item.isVideo) {
					mVideoCount--;
					mVideoSize -= item.contentSize;
					mVideoDuration -= item.duration;
				} else {
					mPhotoCount--;
					mPhotoSize -= item.contentSize;
				}
				break;
			}
		}
	}

	public ItemMediaData[] get() {
		return mItems.toArray(new ItemMediaData[mItems.size()]);
	}

	public ItemMediaData get(int index) {
		return mItems.get(index);
	}
    
    public ItemMediaData get(long id) {
        if(mItems != null) {
            for(ItemMediaData data : mItems) {
                if(data._id == id) {
                    return data;
                }
            }
        }
        return null;
    }

	public ArrayList<MediaData> getByMediaDatas() {
		if (mItems.size() <= 0) {
			return null;
		}

		ArrayList<MediaData> datas = new ArrayList<MediaData>();
		for (int i = 0; i < mItems.size(); i++) {
			ItemMediaData item = mItems.get(i);
			if (item != null) {
				MediaData data = new MediaData();
				data.mediaType = item.isVideo ? PublicVariable.MEDIA_TYPE_VIDEO : PublicVariable.MEDIA_TYPE_IMAGE;
				data.id = item._id;
				data.path = item.contentPath;
				data.displayName = item.displayName;
				data.size = item.contentSize;
				data.duration = item.duration;
				datas.add(data);
			}
		}
		return datas;
	}

	public LinkedList<Long> getVideoMediaIds() {
		if (mItems.size() <= 0) {
			return null;
		}

		LinkedList<Long> videoIds = new LinkedList<Long>();
		for (int i = 0; i < mItems.size(); i++) {
			ItemMediaData item = mItems.get(i);
			if (item != null) {
				if (item.isVideo) {
					videoIds.add(item._id);
				}
			}
		}
		return videoIds;
	}

	public LinkedList<String> getVideoMediaPaths() {
		if (mItems.size() <= 0) {
			return null;
		}

		LinkedList<String> videePaths = new LinkedList<String>();
		for (int i = 0; i < mItems.size(); i++) {
			ItemMediaData item = mItems.get(i);
			if (item != null) {
				if (item.isVideo) {
					videePaths.add(item.contentPath);
				}
			}
		}
		return videePaths;
	}

	public ArrayList<ImageData> getImageDatasForEdit() {
		if (mItems.size() <= 0) {
			return null;
		}

		ArrayList<ImageData> imageDatas = new ArrayList<ImageData>();
		ImageData imageData = null;
		ImageSearch imageSearch = new ImageSearch(mContext, null);
		for (int i = 0; i < mItems.size(); i++) {
			if (!mItems.get(i).isVideo) {
				imageData = imageSearch.getImagaeDataForImageId(mItems.get(i)._id.intValue());
				
				if(imageData == null){
					imageData = new ImageData(); 
					imageData.id = mItems.get(i)._id.intValue();
					imageData.path = mItems.get(i).contentPath;
					imageData.fileName = mItems.get(i).displayName;
					imageData.date = mItems.get(i).date;
					imageData.orientation = mItems.get(i).degrees + "";
					if(mItems.get(i).width == 0 || mItems.get(i).height == 0){
						imageData.width = ImageUtils.measureImageSize(mItems.get(i).contentPath).width; 
						imageData.height = ImageUtils.measureImageSize(mItems.get(i).contentPath).height; 
					}else{
						imageData.width = mItems.get(i).width;
						imageData.height = mItems.get(i).height;	
					}
				}else{
					
					if(imageData.width == 0 || imageData.height == 0){
						imageData.width = ImageUtils.measureImageSize(mItems.get(i).contentPath).width; 
						imageData.height = ImageUtils.measureImageSize(mItems.get(i).contentPath).height; 
					}
				}
				
				if (imageData != null) {
					imageDatas.add(imageData);
				}
			}
		}
		return imageDatas;
	}
	
	public ArrayList<ImageData> getVideoDatasForEdit() {
		if (mItems.size() <= 0) {
			return null;
		}

		ArrayList<ImageData> videoDatas = new ArrayList<ImageData>();
		ImageData videoData = null;
		for (int i = 0; i < mItems.size(); i++) {
			if (mItems.get(i).isVideo) {
				videoData = new ImageData();
				videoData.id = mItems.get(i)._id.intValue();
				videoData.path = mItems.get(i).contentPath;
				videoData.fileName = mItems.get(i).displayName;
				videoData.date = mItems.get(i).date;
				videoData.orientation = mItems.get(i).degrees + "";
				videoData.width = mItems.get(i).width;
				videoData.height = mItems.get(i).height;
				if (videoData != null) {
					videoDatas.add(videoData);
				}
			}
		}
		return videoDatas;
	}

	public LinkedList<Long> getVideoMediaDates() {
		if (mItems.size() <= 0) {
			return null;
		}

		LinkedList<Long> videoDates = new LinkedList<Long>();
		for (int i = 0; i < mItems.size(); i++) {
			ItemMediaData item = mItems.get(i);
			if (item != null) {
				if (item.isVideo) {
					videoDates.add(item.date);
				}
			}
		}
		return videoDates;
	}

	public ArrayList<Uri> getByMediaUri() {
		if (mItems.size() <= 0) {
			return null;
		}

		ArrayList<Uri> uris = new ArrayList<Uri>();
		for (int i = 0; i < mItems.size(); i++) {
			ItemMediaData item = mItems.get(i);
			if (item != null) {
				uris.add(Uri.fromFile(new File(item.contentPath)));
			}
		}
		return uris;
	}

	public void addOnSelectManagerCallback(OnSelectManagerCallback cb) {
		removeOnSelectManagerCallback(cb);
		mCallbacks.add(cb);
		if (cb != null) {
			cb.onDataChanged(mSelectMode, mItems.size(), (mPhotoSize + mVideoSize), mPhotoCount, mVideoCount, mVideoDuration);
		}
	}

	public void removeOnSelectManagerCallback(OnSelectManagerCallback cb) {
		for (int i = 0; i < mCallbacks.size(); i++) {
			if (mCallbacks.get(i) == cb) {
				mCallbacks.remove(i);
				break;
			}
		}
	}

	public void notifyDataSetChanged() {
		for (int i = 0; i < mCallbacks.size(); i++) {
			notifyCallback(mCallbacks.get(i));
		}
	}

	public void notifyCallback(OnSelectManagerCallback cb) {
		if (cb != null) {
			notifyRules(cb);
			cb.onDataChanged(mSelectMode, mItems.size(), (mPhotoSize + mVideoSize), mPhotoCount, mVideoCount, mVideoDuration);
		}
	}

	private void notifyRules(OnSelectManagerCallback cb) {
		cb.onRulesChanged(mRule.photo.used, mRule.video.used, mRule.video.minDuration, mRule.video.maxDuration, mRule.video.maxSize, true);
	}
	
	public int getVideoCount(){
		return mVideoCount; 
	}
}
