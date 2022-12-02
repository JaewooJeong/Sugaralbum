package com.kiwiple.scheduler.data;

import java.util.LinkedList;
import java.util.List;
/**
 * 출력 데이터 클래스. 
 *
 */
public abstract class OutputData {
	
	public static final String TAG_JSON_KEY_SCENE_TYPE = "tag_scene_type"; 
	public static final String TAG_JSON_VALE_SCENE_CONTENT = "tag_scene_content";
	public static final String TAG_JSON_VALE_SCENE_UNKNOWN = "tag_scene_unknown";
	
	public static final int INVALID_FRAME_ID = -1; 
	public static final String FRAME_ID = "frame_id";
    public static final String SCENE_TYPE_NONE = "none";
    
    public static final int FILTER_ID_LITTLE_BABY = 102;
    public static final int FILTER_ID_SNAP_SHOT   =	104;
    public static final int FILTER_ID_COZY_ROOM   = 110;
    public static final int FILTER_ID_WEEK_LOMO   = 142; 
    public static final int FILTER_ID_SLIDE_FILM  = 155;
    
	protected String mThemeName;
	protected String mAudioPath;
	protected boolean isResourceInAsset;
	/**
	 * 전체 데이터에서 imageFileScene, CollageScen, MultiLayerScene 중에 선택된 리스트.
	 */
	protected LinkedList<SelectedOutputData> mSelectedOutputDataList;

	/**
	 * output data 생성자. 
	 */
	public OutputData() {
		mSelectedOutputDataList = new LinkedList<SelectedOutputData>();
	}

	/**
	 * audio path 얻어오기. 
	 * @return : 오디오 path. 
	 */
	public String getAudioPath() {
		return mAudioPath;
	}

	/**
	 * audio path 설정하기. 
	 * @param audioPath : audio path. 
	 */
	public void setAudioPath(String audioPath) {
		this.mAudioPath = audioPath;
	}

	/**
	 * Resource file이 asset으로 설정되어 있는지 확인.  
	 * @return : Resource file이 asset으로 설정 여부  
	 */
	public boolean isResourceInAsset() {
		return isResourceInAsset;
	}

	/**
	 * Resource파 Asset으로 설정하기. 
	 * @param isResourceInAsset : Resource asset data로 설정 여부. 
	 */
	public void setResourceInAsset(boolean isResourceInAsset) {
		this.isResourceInAsset = isResourceInAsset;
	}
	
	/**
	 * output data의 scene type 얻어오기. 
	 * @param location : 얻어올 위치. 
	 * @return : scene type. 
	 */
	public String getOutputDataSceneType(int location){
		return mSelectedOutputDataList.get(location).getSceneType(); 
	}
	
	/**
	 * output data에 name(id) list 추가 하기 
	 * @param location : 추가 할 위치. 
	 * @param names : name list. 
	 */
	public void addOutputDataImageNames(int location, List<String> names){
		mSelectedOutputDataList.get(location).setNameList(names);  
	}

	/**
	 * output data selectedOutputData 추가 히기 
	 * @param selectedOutputData : scene에 대한 정보를 가지고 있다. 
	 */
	public void addOutputDataToOutputDataList(SelectedOutputData selectedOutputData) {
		mSelectedOutputDataList.add(selectedOutputData);
	}
	
	/**
	 * output data의 정해진 위치에 selectedOutputData 추가 하기.
	 * @param location : 추가 할 위치. 
	 * @param selectedOutputData : scene에 대한 정보를 가지고 있다. 
	 */
	public void addOutputDataToOutputDataList(int location, SelectedOutputData selectedOutputData){
		mSelectedOutputDataList.add(location, selectedOutputData); 
	}

	/**
	 * selectedOutput list를 반환한다. 
	 * @return : selectedOutput list
	 */
	public LinkedList<SelectedOutputData> getOutputDataList() {
		return mSelectedOutputDataList;
	}
}
