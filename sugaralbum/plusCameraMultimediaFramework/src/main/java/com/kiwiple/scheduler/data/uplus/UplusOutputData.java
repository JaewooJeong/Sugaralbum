package com.kiwiple.scheduler.data.uplus;

import java.util.List;

import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.Frame;

/**
 * uplus 출력 데이터 클래스. 
 *
 */
public class UplusOutputData extends OutputData {
	
	
    public static final String SCENE_TYPE_ACCELERATION = "acceleration_zoom_scene";
    public static final String SCENE_TYPE_MULTI_FILTER = "multi_filter_scene"; 
    
    public static final String JSON_NAME_MAINTAIN_FEATURE = "maintain_feature";
    
    public static final int INVALID_PROTAGONIST_ID = -1;
    
	private Theme mTheme;
	private Theme mOldTheme; 
	private List<String> mAssetImagePathList;
	private List<Frame> mFrames;
	private int protagonistId; 

	public UplusOutputData() {
		super();
		protagonistId = INVALID_PROTAGONIST_ID; 
	}
	
	public void setProtagonistId(int protagonistId){
		this.protagonistId = protagonistId;  
	}
	
	public int getProtagonistId(){
		return protagonistId; 
	}

	/**
	 * asset image path list 반환. 
	 * @return : asset image path list
	 */
	public List<String> getAssetImagePathList() {
		return mAssetImagePathList;
	}

	/**
	 * asset image path list 서정. 
	 * @param assetImagePathList : asset image path list. 
	 */
	public void setAssetImagePathList(List<String> assetImagePathList) {
		this.mAssetImagePathList = assetImagePathList;
	}

	/**
	 * frame data list를 반환. 
	 * @return : frame list. 
	 */
	public List<Frame> getFrames() {
		return mFrames;
	}

	/**
	 * frame data list 설정. 
	 * @param frames : 각 scene이 가지고 있어야 할 데이터 list. 
	 */
	public void setFrames(List<Frame> frames) {
		this.mFrames = frames;
	}

	/**
	 *  theme 반환. 
	 * @return : theme. 
	 */
	public Theme getTheme() {
		return mTheme;
	}

	/**
	 * theme 설정. 
	 * @param theme : 테마. 
	 */
	public void setTheme(Theme theme) {
		this.mTheme = theme;
    }
	
	/**
	 *  old theme 반환. 
	 * @return : theme. 
	 */
	public Theme getOldTheme() {
		return mOldTheme;
	}

	/**
	 * old theme 설정. 
	 * @param theme : 테마. 
	 */
	public void setOldTheme(Theme theme) {
		this.mOldTheme = theme;
    }
}
