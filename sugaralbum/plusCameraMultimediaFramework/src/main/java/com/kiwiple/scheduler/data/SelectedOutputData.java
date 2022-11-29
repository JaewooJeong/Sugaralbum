package com.kiwiple.scheduler.data;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.multimedia.Constants;
import com.kiwiple.multimedia.canvas.BurstShotScene;
import com.kiwiple.multimedia.canvas.CollageScene;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.MultiLayerScene;
import com.kiwiple.multimedia.canvas.VideoFileScene;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.exception.MultimediaException;
import com.kiwiple.scheduler.coordinate.scaler.KenBurnsScalerCoordinator.KenburnDirection;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;

/**
 * 씬으로 선택된 data 클래스. 
 *
 */
public class SelectedOutputData {
	
	private String mSceneType;//"collage", "image_file", "multi_layer", "video_file"
	private int mDuration;   //최소, 최대 시간은 정해지지 않았다. 현재는 default 5000  
	private int mFilterId;   //108....
	private int mFrameId;    //201, 301....
	private long mDate;       
	private List<String> mNameList;  //name은 id를 나타낸다. single scene인 경우 list은 1이고, multi scene인 경우 list는 복수이다.
	private ArrayList<ImageData> imageDatas; 
	private long mVideoStart; 
	private long mVideoEnd; 
	private boolean mIsAccelerationZoom;
	private boolean mIsMultiFilter;
	private boolean mIsStepAppearEffect; 
	private boolean mIsScaleEffect; 
	private boolean mIsVideoMultiLayer;
	private KenburnDirection mKenburnDirection;
	
	private ArrayList<ArrayList<Viewport>> mViewportList;
	private ArrayList<Viewport> mLayerViewportList;

	/**
	 * 생성
	 */
	public SelectedOutputData() {
		mNameList = new ArrayList<String>();
		this.mSceneType = OutputData.SCENE_TYPE_NONE; 
		this.mFilterId = Constants.INVALID_FILTER_ID; 
		this.mFrameId = OutputData.INVALID_FRAME_ID;
		this.mVideoStart = 0; 
		this.mVideoEnd = 0;
		this.mIsAccelerationZoom = false; 
		this.mIsMultiFilter = false;
		this.mIsStepAppearEffect = false; 
		this.mViewportList = null;
		this.mLayerViewportList = null;
		this.mKenburnDirection = KenburnDirection.NONE; 
		this.mIsScaleEffect = false; 
	}
	/**
	 * 생성자. 
	 * @param sceneType 씬의 타입. 
	 * @param filterId 씬의 필터 아이디.
	 * @param frameId 씬의 프레임 아이디. 
	 */
	public SelectedOutputData(String sceneType, int filterId, int frameId) {
		mNameList = new ArrayList<String>();
		this.mSceneType = sceneType; 
		this.mFilterId = filterId; 
		this.mFrameId = frameId;
		this.mVideoStart = 0; 
		this.mVideoEnd = 0;
		this.mIsAccelerationZoom = false;
		this.mIsStepAppearEffect = false; 
		this.mIsMultiFilter = false;
		this.mViewportList = null;
		this.mLayerViewportList = null;
		this.mKenburnDirection = KenburnDirection.NONE;
		this.mIsScaleEffect = false; 
	}

	public void setSelectedViewportList(ArrayList<ArrayList<Viewport>> viewportList){
		mViewportList = viewportList;
	}

	public ArrayList<ArrayList<Viewport>> getSelectedViewportList(){
		return mViewportList;
	}
	
	public void setSelectedLayerViewportList(ArrayList<Viewport> layerViewportList) {
		mLayerViewportList = layerViewportList;
	}
	
	public ArrayList<Viewport> getSelectedLayerViewportList(){
		return mLayerViewportList;
	}
	/**
	 * output data의 scene type 얻어오기 
	 * @return : scene type; 
	 */
	public String getSceneType() {
		return mSceneType;
	}

	/**
	 * output data sene type 설정. 
	 * @param mSceneType : scene type; 
	 */
	public void setSceneType(String mSceneType) {
		this.mSceneType = mSceneType;
	}

	/**
	 * output data date 정보.<br> 
	 * 싱글씬이 아닐 경우 -1를 반환 한다.<br>
	 * @return : date 정보. 
	 */
	public long getDate() {
		return mDate;
	}

	/**
	 * output data의 date정보 설정. <br>
	 * 싱글씬이 아닐 경우 -1을 설정. <br>
	 * @param date : date 정보. 
	 */
	public void setDate(long date) {
		this.mDate = date;
	}

	/**
	 * data의 id정보를 저장한다. <br>
	 * video data만을 위해 사용. 
	 * @param name : id. 
	 */
	public void addNameToNameList(String name) {
		mNameList.add(name);
	}
	
	/**
	 * data의 id정보 list를 얻어오기. 
	 * @return : id list. 
	 */
	public List<String> getNameList() {
		return mNameList;
	}

	/**
	 * name list(id list)를 설정한다. 
	 * @param names : id list
	 */
	public void setNameList(List<String> names) {
		this.mNameList = names; 
		
	}
	/**
	 * output data의 duration. 
	 * @return : duration. 
	 */
	public int getDuration() {
		return mDuration;
	}
	
	/**
	 * output data의 duration 설정. 
	 * @param duration : duration. 
	 */
	public void setDuration(int duration) {
		this.mDuration = duration;
	}

	/**
	 * 필터 테마의 경우 필터 id를 얻어온다. <br>
	 * 필터 테마가 아닌 경우는 -1로 설정되어 있다. <br>
	 * @return : filter id. 
	 */
	public int getFilterId() {
		return mFilterId;
	}
	/**
 	 * 필터 테마의 경우 필터 id를 설정한다. <br>
	 * 필터 테마가 아닌 경우는 -1로 설정한다. <br>
	 * @param filterId : filter id. 
	 */
	public void setFilterId(int filterId) {
		this.mFilterId = filterId;
	}

	/**
	 * output data의 frame id를 얻어온다. 
	 * @return : frame id. 
	 */
	public int getFrameId() {
		return mFrameId;
	}

	/**
	 * output data의 frame id를 설정한다. 
	 * @param frameId : frame id. 
	 */
	public void setFrameId(int frameId) {
		this.mFrameId = frameId;
	}
	
	/**
	 * output data에 들어 가는 image data list를 설정한다. 
	 * @param imageDatas : image data list. 
	 */
	public void setImageDatas(ArrayList<ImageData> imageDatas){
		this.imageDatas = imageDatas; 
	}
	
	/**
	 * output data에서 image data list를 얻어온다. 
	 * @return : image data list. 
	 */
	public ArrayList<ImageData> getImageDatas(){
		return this.imageDatas; 
	}
	

	/**
	 * 멀티씬의 경우 frame의 index에 따라서 가로/세로 배치의 우선순위를 정한다. 
	 * @param frameIndex : frame의 index. 
	 * @return : 우선순위 배치 여부.
	 */
    // 20150226 olive : #10758 각 프레임의 우선순위를 지정한다. 프레임 아이디로 우선순위를 가져올 수 없으므로 하드코딩; 추후 프레임의 종류가 추가될 경우 업데이트 필요.
	public boolean isPreferPortraitFrame(int frameIndex) {
	    boolean preferPortrait = false;
	    if (getFrameId() == MultiLayerData.MULTI_LAYER_COLUMN_TWO_PICTURES_ID) {
            preferPortrait = true;
        } else if (getFrameId() == MultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID) {
            preferPortrait = true;
        } else if (getFrameId() == MultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID) {
            switch(frameIndex) {
                case 1: // left
                    preferPortrait = true;
                    break;
                case 2: // right top
                    preferPortrait = false;
                    break;
                case 3: // right bottom
                    preferPortrait = false;
                    break;
                default:
                    preferPortrait = false;
                    break;
            }
        } else if (getFrameId() == MultiLayerData.MULTI_LAYER_REGULAR_FOUR_PICTURES_ID) {
            preferPortrait = false;
        } else if (getFrameId() == MultiLayerData.MULTI_LAYER_IRREGULAR_01_FOUR_PICTURES_ID) {
            switch(frameIndex) {
                case 1: // left top
                    preferPortrait = true;
                    break;
                case 2: // left bottom
                    preferPortrait = false;
                    break;
                case 3: // right top
                    preferPortrait = true;
                    break;
                case 4: // right bottom
                    preferPortrait = false;
                    break;
                default:
                    preferPortrait = false;
                    break;
            }
        }
	    return preferPortrait;
	}
	
	/**
	 * 비디오 데이터의 시작위치를 얻어온다. 
	 * @return : 비디오 데이터의 시작 위치. 
	 */
	public long getVideoStart() {
		return mVideoStart;
	}
	/**
	 * 비디오 데이터의 시작 위치를 설정한다. 
	 * @param videoStart : 비디오 데이터의 시작 위치. 
	 */
	public void setVideoStart(long videoStart) {
		this.mVideoStart = videoStart;
	}
	/**
	 * 비디오 데이터의 끝 위치를 얻어온다. 
	 * @return : 비디오 데이터의 끝 위치. 
	 */
	public long getVideoEnd() {
		return mVideoEnd;
	}
	/**
	 * 비디오 데이터의 끝 위치를 설정한다. 
	 * @param videoEnd : 비디오 데이터의 끝 위치. 
	 */
	public void setVideoEnd(long videoEnd) {
		this.mVideoEnd = videoEnd;
	}
	
	public void setAccelerationZoom(boolean isAccelerationZoom){
		this.mIsAccelerationZoom = isAccelerationZoom; 
	}
	
	public boolean getAccelerationZoom(){
		return mIsAccelerationZoom; 
	}
	
	public void setMultiFilter(boolean isMultiFilter){
		this.mIsMultiFilter = isMultiFilter; 
	}
	
	public boolean getMultiFilter(){
		return mIsMultiFilter; 
	}
	
	public void setKenburnDirection(KenburnDirection kenburnDirection){
		mKenburnDirection = kenburnDirection; 
	}
	
	public KenburnDirection getKenburnDirection(){
		return mKenburnDirection; 
	}
	
	public void setStepAppearEffect(boolean isStepAppearEffect){
		this.mIsStepAppearEffect = isStepAppearEffect; 
	}
	
	public boolean getStepAppearEffect(){
		return mIsStepAppearEffect; 
	}
	
	public void setScaleEffect(boolean isScaleEffect){
		this.mIsScaleEffect = isScaleEffect; 
	}
	
	public boolean getScaleEffect(){
		return mIsScaleEffect; 
	}
}
