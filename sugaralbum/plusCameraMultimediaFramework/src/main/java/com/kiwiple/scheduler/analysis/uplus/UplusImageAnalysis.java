package com.kiwiple.scheduler.analysis.uplus;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Rect;
import android.graphics.RectF;
import android.provider.MediaStore;

import com.kiwiple.debug.L;
import com.kiwiple.imageanalysis.correct.collage.CollageCorrectAdviser;
import com.kiwiple.imageanalysis.correct.collage.FaceInfomation;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.database.ImageFaceData;
import com.kiwiple.imageanalysis.search.ImageSearch;
import com.kiwiple.imageframework.collage.DesignTemplateManager;
import com.kiwiple.imageframework.collage.TemplateInfo;
import com.kiwiple.multimedia.Constants;
import com.kiwiple.multimedia.canvas.BurstShotScene;
import com.kiwiple.multimedia.canvas.CollageScene;
import com.kiwiple.multimedia.canvas.DrawableScene;
import com.kiwiple.multimedia.canvas.DummyScene;
import com.kiwiple.multimedia.canvas.FileImageResource;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.ImageTextScene;
import com.kiwiple.multimedia.canvas.KenBurnsScaler;
import com.kiwiple.multimedia.canvas.KenBurnsScaler.Editor;
import com.kiwiple.multimedia.canvas.LayerScene;
import com.kiwiple.multimedia.canvas.MultiLayerScene;
import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.multimedia.canvas.ScaleEffect;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.StepAppearEffect;
import com.kiwiple.multimedia.canvas.VideoFileScene;
import com.kiwiple.multimedia.canvas.Visualizer;
import com.kiwiple.multimedia.canvas.data.CollageElement;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.preview.PreviewManager;
import com.kiwiple.multimedia.util.ImageUtils;
import com.kiwiple.multimedia.util.Size;
import com.kiwiple.scheduler.analysis.ImageAnalysis;
import com.kiwiple.scheduler.coordinate.scaler.KenBurnsScalerCoordinator.KenburnDirection;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusImageFileSceneCoordinator;
import com.kiwiple.scheduler.data.AnalyzedInputData;
import com.kiwiple.scheduler.data.InputData;
import com.kiwiple.scheduler.data.MultiLayerData;
import com.kiwiple.scheduler.data.SelectedOutputData;
import com.kiwiple.scheduler.data.uplus.UplusInputData;
import com.kiwiple.scheduler.data.uplus.UplusMultiLayerData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.database.uplus.UplusAnalysisPersister;
import com.kiwiple.scheduler.tag.UserTag;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.ThemeType;
import com.kiwiple.scheduler.util.EffectUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;

public class UplusImageAnalysis extends ImageAnalysis {

	public static final int MAXIMUM_LIMITATION_LOOP_COUNT = 2;
	
	public static final int MAXIMUM_IMAGE_SCENE_COUNT = 26;
	public static final int MAXIMUM_LIMITATION_IMAGE_SCENE_COUNT = 49;

	public static final int MINIMUM_IMAGE_COUNT = 5;
	public static final int MINIMUM_AUTO_IMAGE_COUNT = 10;
	
	public static final int MAXIMUM_IMAGE_COUNT = 40;
	public static final int MAXIMUM_LIMITATION_IMAGE_COUNT = 81;
	private static final int COLLAGE_DEFAULT_IMAGE_SIZE = 1200;

	public static final int MAXIMUM_ENDING_SCENE_COUNT = 15;
	
	private Context mContext;

	private UplusInputData mUplusInputData;
	private UplusOutputData mUplusOutputData;

	private List<AnalyzedInputData> mInputDataListBasedOnQualityScore;

	private HashMap<String, ImageData> mUplusInputDataHashMap;
	// key : 이미지의 id, value : 이미지의 scene type
	private HashMap<String, String> mSelectedImageMap;

	private int mMultiSceneCount; // multi scene 갯수.
	private int mTotalMultiImageCount; // multi scene에 사용되 총 이미지 갯수.
	private int mSingleSceneCount; // single scene 갯수.
	private int mTotalImageSceneCount; // 전체 image scene의 갯수.

	private DesignTemplateManager templateManager;

	//burst shot
	private final int BURST_SHOT_SIMIRARITY_TIME_SLOT  = 5000; 
	private final int BURST_SHOT_SIMIRARITY_SCORE      = 90;
	public static final int BURST_SHOT_MIN_COUNT             = 4;
	public static final int BURST_SHOT_MAX_COUNT             = 10; 
	private final int BURST_SHOT_ENABLE_MINIMUM_DATA        = 20; 	
	private boolean mIsBurstShot = false;
	private int mBurstShotCount = 0;
	
	//acceleration zoom
	public final static int ACCELERATION_ZOOM_MAX_COUNT = 3; 
	public final static int ACCELERATION_ZOOM_MAX_PERSON_NUM = 2; 
	public final static float ACCELERATION_ZOOM_MIN_PERCENT = 0.1f;
	public final static float ACCELERATION_ZOOM_MAX_PERCENT = 0.8f;
	
	//multi filter
	public final static int MULTI_FILTER_ZOOM_MAX_COUNT = 1;
	public final static int MULTI_FILTER_ZOOM_MIN_SIZE = 30;
	
	public UplusImageAnalysis(Context context) {
		super(context);
		mContext = context;
		templateManager = DesignTemplateManager.getInstance(mContext);
		mMultiSceneCount = 0; // multi scene 갯수.
		mTotalMultiImageCount = 0; // multi scene에 사용되 총 이미지 갯수.
		mSingleSceneCount = 0; // single scene 갯수.
		mTotalImageSceneCount = 0; // 전체 scene의 갯수.
		mIsBurstShot = false; 
		mBurstShotCount = 0;

		mSelectedImageMap = new HashMap<String, String>();
		mInputDataListBasedOnQualityScore = new ArrayList<AnalyzedInputData>();
	}
	
	
	/**
	 * 선택된 jsonObject와 사진의 갯수를 통해 각각의 scene의 구성.<br>
	 * @param jsonObject : 선택된 스케중링 json data. <br>
	 * @param inputImageCount : 입력받은 이미지 데이터 갯수.<br>
	 */
	public void setSchduleFromJsonObject(JSONObject jsonObject, int inputImageCount){

		JSONObject jsonRegionObject = null;
		JSONArray jsonSceneArray = null;
		mSingleSceneCount = 0;
		mMultiSceneCount = 0;
		mTotalMultiImageCount = 0;
		mTotalImageSceneCount = 0;

		int totalImageCount = 0;
		int filterId = 0;
		int frameId;
		String sceneType;
		int acclerationZoomCount = 0; 
		
		// 각 씬 단위로 몇장의 사진이 필요한지 체크.
		try {
			jsonRegionObject = jsonObject.getJSONObject(Visualizer.JSON_NAME_REGIONS);
			jsonSceneArray = jsonRegionObject.getJSONArray(Region.JSON_NAME_SCENES);
			L.d("input Image count : " + inputImageCount +", json array size : " + jsonSceneArray.length());
			for (int i = 0; i < jsonSceneArray.length(); i++) {
				
				if (totalImageCount >= inputImageCount) {
					break;
				}
				
				JSONObject sceneObject = jsonSceneArray.getJSONObject(i);
				
				sceneType = sceneObject.getString(Scene.JSON_NAME_TYPE);
				if(sceneType.equals(DrawableScene.JSON_VALUE_TYPE) ||
						sceneType.equals(DummyScene.JSON_VALUE_TYPE) ||
						sceneType.equals(ImageTextScene.JSON_VALUE_TYPE)){
					continue;
				}
				
				if(mUplusOutputData.getTheme().themeType == ThemeType.FRAME && sceneType.equals(MultiLayerScene.JSON_VALUE_TYPE)){
					sceneType = CollageScene.JSON_VALUE_TYPE; 
				}
				
				filterId = sceneObject.optInt(Scene.JSON_NAME_FILTER_ID, Constants.INVALID_FILTER_ID);
				frameId = sceneObject.getInt(UplusOutputData.FRAME_ID);
				
				int needImageCount = getNeedImageCount(frameId);
				ArrayList<ImageData> standardImageDatas = new ArrayList<ImageData>();
				for(int j = 0; j < needImageCount; j++){
					String selectedSceneType = mSelectedImageMap.get(mInputDataListBasedOnDate.get(totalImageCount).getName());
					
					if(selectedSceneType.equals(UplusOutputData.SCENE_TYPE_ACCELERATION) ||
					   selectedSceneType.equals(UplusOutputData.SCENE_TYPE_MULTI_FILTER)){
						
						KenburnDirection kenburnDirection = KenburnDirection.NONE;
						if(selectedSceneType.equals(UplusOutputData.SCENE_TYPE_ACCELERATION)){
							if(acclerationZoomCount == 0){
								kenburnDirection = kenburnDirection.IN; 
							}else{
								kenburnDirection = kenburnDirection.OUT;
							}
							acclerationZoomCount++;
						}
						
						totalImageCount = addSpecialScene(selectedSceneType, filterId, frameId, totalImageCount, kenburnDirection);
						j -= 1; 
					}else if(selectedSceneType.equals(BurstShotScene.JSON_VALUE_TYPE)){
						j -= 1; 
						totalImageCount++; 
					}else{
					    standardImageDatas.add(mUplusInputDataHashMap.get(mInputDataListBasedOnDate.get(totalImageCount).getName()));
						totalImageCount++; 
					}
					
					if(totalImageCount >= inputImageCount){
						break; 
					}
				}

				if(needImageCount == standardImageDatas.size()){
					addExpectedScene(standardImageDatas, sceneType, filterId, frameId); 
				}else{
					addUnExpectedScene(standardImageDatas, filterId); 
				}
			}
			
			addBurstShotSelecteOutputData(filterId);

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int index = 0; 
		for (SelectedOutputData selectedOutputData : mUplusOutputData.getOutputDataList()) {
			L.d("index : " + index +", type : " + selectedOutputData.getSceneType() +", date : " + selectedOutputData.getDate() +", acceleration : " + selectedOutputData.getAccelerationZoom() +", multi filter : " + selectedOutputData.getMultiFilter());
			index++; 
		}
	}
	private void addBurstShotSelecteOutputData(int filterId) {
		SelectedOutputData burstSelectOutputData = getBurstShotImageList(filterId); 
		if(burstSelectOutputData != null){
			int index = 0;
			boolean addedBurstShot = false; 
			for (SelectedOutputData selectedOutputData : mUplusOutputData.getOutputDataList()) {
				if(selectedOutputData.getDate() > burstSelectOutputData.getDate()){
					mUplusOutputData.addOutputDataToOutputDataList(index, burstSelectOutputData);
					addedBurstShot = true; 
					break; 
				}
				index++; 
			}
			if(!addedBurstShot){
				mUplusOutputData.addOutputDataToOutputDataList(burstSelectOutputData); 
			}
		}
		
	}

	private SelectedOutputData getBurstShotImageList(int filterId) {
		int frameId = 0; 
		ArrayList<ImageData> burstShotImageDataList = new ArrayList<ImageData>();
		SelectedOutputData burstShotSelectedOutputData = new SelectedOutputData(); 
		for(int i = 0; i < mInputDataListBasedOnDate.size(); i++){
			if(mSelectedImageMap.get(mInputDataListBasedOnDate.get(i).getName()).equals(BurstShotScene.JSON_VALUE_TYPE)){
				burstShotImageDataList.add(mUplusInputDataHashMap.get(mInputDataListBasedOnDate.get(i).getName()));
			}
		}
		if(!burstShotImageDataList.isEmpty()){
			burstShotSelectedOutputData.setSceneType(BurstShotScene.JSON_VALUE_TYPE);
			burstShotSelectedOutputData.setFilterId(filterId); 
			frameId =  30000  + (burstShotImageDataList.size() * 1000);
			burstShotSelectedOutputData.setFrameId(frameId); 
			burstShotSelectedOutputData.setImageDatas(burstShotImageDataList);
			burstShotSelectedOutputData.setDate(burstShotImageDataList.get(0).date);
			return burstShotSelectedOutputData; 
		}else{
			return null; 
		}
	}


	int addSpecialScene(String sceneType, int filterId, int frameId, int totalImageCount,KenburnDirection kenburnDirection){
		
		SelectedOutputData specialSelectedOutputData = new SelectedOutputData(); 
		ArrayList<ImageData> specialSceneImageDatas = new ArrayList<ImageData>();
		int needImageCount = 1;
		boolean isAccelerationZoom = false;
		boolean isMultiFilter = false; 
		
		if(sceneType.equals(BurstShotScene.JSON_VALUE_TYPE)){
			frameId =  30000  + (mBurstShotCount * 1000);
			needImageCount = mBurstShotCount;
		}else if(sceneType.equals(UplusOutputData.SCENE_TYPE_ACCELERATION)){
			sceneType = ImageFileScene.JSON_VALUE_TYPE;
			isAccelerationZoom = true; 
		}else if(sceneType.equals(UplusOutputData.SCENE_TYPE_MULTI_FILTER)){
			frameId = UplusMultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID; 
			isMultiFilter = true;
		}
		
		if(!sceneType.equals(UplusOutputData.SCENE_TYPE_MULTI_FILTER)){
			L.d("scheduler need count :" +needImageCount +", total count : " + totalImageCount);
			for(int i = 0; i < needImageCount; i++){
				specialSceneImageDatas.add(mUplusInputDataHashMap.get(mInputDataListBasedOnDate.get(totalImageCount).getName()));
				totalImageCount++;
			}
		}else{
			sceneType = MultiLayerScene.JSON_VALUE_TYPE;
			specialSceneImageDatas.add(mUplusInputDataHashMap.get(mInputDataListBasedOnDate.get(totalImageCount).getName()));
			specialSceneImageDatas.add(mUplusInputDataHashMap.get(mInputDataListBasedOnDate.get(totalImageCount).getName()));
			specialSceneImageDatas.add(mUplusInputDataHashMap.get(mInputDataListBasedOnDate.get(totalImageCount).getName()));
			totalImageCount++;
		}
		
		specialSelectedOutputData.setSceneType(sceneType);
		specialSelectedOutputData.setFilterId(filterId); 
		specialSelectedOutputData.setFrameId(frameId); 
		specialSelectedOutputData.setImageDatas(specialSceneImageDatas);
		specialSelectedOutputData.setDate(specialSceneImageDatas.get(0).date);
		specialSelectedOutputData.setAccelerationZoom(isAccelerationZoom); 
		specialSelectedOutputData.setMultiFilter(isMultiFilter); 
		if(isAccelerationZoom){
			specialSelectedOutputData.setKenburnDirection(kenburnDirection); 
		}
		
		mUplusOutputData.addOutputDataToOutputDataList(specialSelectedOutputData);
		
		return totalImageCount; 
	}
	void addExpectedScene(ArrayList<ImageData> imageDatas, String sceneType, int filterId, int frameId){
		SelectedOutputData standardSelectedOutputData = new SelectedOutputData(sceneType, filterId, frameId);
		standardSelectedOutputData.setImageDatas(imageDatas); 
		standardSelectedOutputData.setDate(imageDatas.get(0).date); 
		
		if(sceneType.equals(MultiLayerScene.JSON_VALUE_TYPE)){
			//3개 멀티씬에서는 사진의 가로 세로 여부에 따라서 frame을 설정한다. 
			if(frameId == UplusMultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID ||
					frameId == UplusMultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID){
				
				int verticalImageCount = 0; 
				for(ImageData _imageData : imageDatas){
					if(_imageData.isPotraitImage()){
						verticalImageCount++; 
					}
				}
				if(verticalImageCount == 3){
					standardSelectedOutputData.setFrameId(UplusMultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID);
				}else{
					standardSelectedOutputData.setFrameId(UplusMultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID);
				}
			}
		}
		
		mUplusOutputData.addOutputDataToOutputDataList(standardSelectedOutputData);

	}
	void addUnExpectedScene(ArrayList<ImageData> imageDatas, int filterId){
		int imageCount = imageDatas.size(); 		
		switch(imageCount){
		case 1:
			SelectedOutputData imageFileSceneSelectedOutputData = new SelectedOutputData(ImageFileScene.JSON_VALUE_TYPE, filterId, UplusImageFileSceneCoordinator.SINGLE_FRAME_ID);
			ArrayList<ImageData> imageFileSceneImages = new ArrayList<ImageData>(); 
			imageFileSceneImages.add(imageDatas.get(0)); 
			imageFileSceneSelectedOutputData.setImageDatas(imageFileSceneImages);
			imageFileSceneSelectedOutputData.setDate(imageFileSceneImages.get(0).date); 
			mUplusOutputData.addOutputDataToOutputDataList(imageFileSceneSelectedOutputData);
			break;
		case 2:
			SelectedOutputData twoLayerSelectedOutputData = new SelectedOutputData(MultiLayerScene.JSON_VALUE_TYPE, filterId, MultiLayerData.MULTI_LAYER_COLUMN_TWO_PICTURES_ID);
			twoLayerSelectedOutputData.setImageDatas(imageDatas); 
			twoLayerSelectedOutputData.setDate(imageDatas.get(0).date); 
			mUplusOutputData.addOutputDataToOutputDataList(twoLayerSelectedOutputData);
			break;
		case 3:
			SelectedOutputData threeLayerSelectedOutputData = new SelectedOutputData(MultiLayerScene.JSON_VALUE_TYPE, filterId, MultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID);
			threeLayerSelectedOutputData.setImageDatas(imageDatas); 
			threeLayerSelectedOutputData.setDate(imageDatas.get(0).date);
			
			int verticalImageCount = 0; 
			for(ImageData _imageData : imageDatas){
				if(_imageData.isPotraitImage()){
					verticalImageCount++; 
				}
			}
			if(verticalImageCount == 3){
				threeLayerSelectedOutputData.setFrameId(UplusMultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID);
			}else{
				threeLayerSelectedOutputData.setFrameId(UplusMultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID);
			}
			
			mUplusOutputData.addOutputDataToOutputDataList(threeLayerSelectedOutputData);
			
		}
		
	}
	/**
	 * 선택된 jsonObject와 사진의 갯수를 통해 각각의 scene의 구성.<br>
	 * @param jsonObject : 선택된 스케중링 json data. <br>
	 * @param inputImageCount : 입력받은 이미지 데이터 갯수.<br>
	 */
	/*
	public void setScheduleJsonObject(JSONObject jsonObject, int inputImageCount) {
		JSONObject jsonRegionObject = null;
		JSONArray jsonSceneArray = null;
		mSingleSceneCount = 0;
		mMultiSceneCount = 0;
		mTotalMultiImageCount = 0;
		mTotalImageSceneCount = 0;

		int totalImageCount = 0;
		int restImageCount = 0;
		int duration;
		int filterId;
		int frameId;
		String sceneType;

		L.d("input Image count : " + inputImageCount);

		// 각 씬 단위로 몇장의 사진이 필요한지 체크.
		try {
			jsonRegionObject = jsonObject.getJSONObject(UplusJsonTemplateName.REGIONS);
			jsonSceneArray = jsonRegionObject.getJSONArray(UplusJsonTemplateName.SCENES);
			// 로고는 제외 한다.
			// 0번은 표지이기 때문에 1번부터 시작한다.
			for (int i = 1; i < jsonSceneArray.length(); i++) {
				JSONObject object = jsonSceneArray.getJSONObject(i);
				sceneType = object.getString(UplusJsonTemplateName.SCENE_TYPE);
				duration = object.getInt(UplusJsonTemplateName.DURATION);
				filterId = object.getInt(UplusJsonTemplateName.FILTER_ID);
				frameId = object.getInt(UplusJsonTemplateName.FRAME_ID);

				L.d("count : " + i + "type : " + sceneType + "frameid : " + frameId);

				if (sceneType.equals(ImageFileScene.JSON_VALUE_TYPE)) {
					totalImageCount++;
					if (totalImageCount > inputImageCount) {
						break;
					}

					SelectedOutputData selectedOutputData = new SelectedOutputData(sceneType, duration, filterId, frameId);
					mUplusOutputData.addOutputDataToOutputDataList(selectedOutputData);

				} else if (sceneType.equals(MultiLayerScene.JSON_VALUE_TYPE)) {
					// 멀티씬이기 때문에 몇개의 사진이 포함되는 frame인지 체크 한다.
					int multiImageNum = getMultiFrameImageCount(frameId);
					restImageCount = inputImageCount - totalImageCount;
					totalImageCount += multiImageNum;

					if (restImageCount == 0) {
						break;
					}
					// 마지막이 멀티씬이고 현재 남아 있는 사진의 갯수보다 씬 count가 많은 상황.
					if (multiImageNum > restImageCount) {

						for (int j = 0; j < restImageCount; j++) {
							SelectedOutputData selectedOutputData = new SelectedOutputData(UplusJsonTemplateName.SCENE_TYPE_SINGLE, duration, filterId, 10000);
							mUplusOutputData.addOutputDataToOutputDataList(selectedOutputData);
						}
						break;
					} else {
						SelectedOutputData selectedOutputData = new SelectedOutputData(sceneType, duration, filterId, frameId);
						mUplusOutputData.addOutputDataToOutputDataList(selectedOutputData);
					}

				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		for (SelectedOutputData selectedOutputData : mUplusOutputData.getOutputDataList()) {
			if (selectedOutputData.getSceneType().equals(UplusJsonTemplateName.SCENE_TYPE_SINGLE)) {
				mTotalImageSceneCount += 1;
				mSingleSceneCount += 1;
				L.d("total image : " + mTotalImageSceneCount + "single : " + mSingleSceneCount);
			} else if (selectedOutputData.getSceneType().equals(MultiLayerScene.JSON_VALUE_TYPE)) {
				int multiImageNum = getMultiFrameImageCount(selectedOutputData.getFrameId());
				mTotalImageSceneCount += multiImageNum;
				mMultiSceneCount += 1;
				mTotalMultiImageCount += multiImageNum;
				L.d("total image : " + mTotalImageSceneCount + "multi : " + mMultiSceneCount + "total multi : " + mTotalMultiImageCount);
			}

		}

		L.d("total Image scene count : " + mTotalImageSceneCount + ", single count : " + mSingleSceneCount + ", multi count : " + mMultiSceneCount
				+ ", total multi image count : " + mTotalMultiImageCount);
	}*/

	/**
	 * frame id를 통해 사용될 사진을 갯수를 구한다. <br>
	 * @param frameId 
	 * @return 사용될 사진의 갯수.<br>
	 */	
	private int getNeedImageCount(int frameId){
		if(frameId == UplusImageFileSceneCoordinator.SINGLE_FRAME_ID){
			return 1; 
		}else{
			return (int) ((frameId / 1000) % 10);
		}
	}

	
	@Override
	public void startInputDataAnalysis(InputData inputData) {

		mUplusInputData = (UplusInputData) inputData;

		L.d("mUplusInputData.getImageDataList().size = " + mUplusInputData.getImageDataList().size());
		makeUplusInputDataHashMap(mUplusInputData.getImageDataList());
		uplusInputDataDetermination(mUplusInputData.getImageDataList());
		analysisBasedOnDate(mUplusInputData.getImageDataList());
		analysisBasedOnQualityScore(mUplusInputData.getImageDataList());
	}

	/**
	 * outputData를 설정. 
	 * @param uplusOutputData : output data. <br>
	 */
	public void setUplusOutputData(UplusOutputData uplusOutputData) {
		this.mUplusOutputData = (UplusOutputData) uplusOutputData;
	}


	/**
	 * 테마 변경시 현재 사용되고 있는 json data에서 각각의 Scene을<br>
	 * 현재 테마에 맞게 재 설정한다.<br>
	 * @param jsonObject : 현재 사용되고 있는 json data. <br>
	 */
	public void setSelectedOutputDataFromNewTheme(JSONObject jsonObject, JSONObject effectJsonObject) {
		
		String sceneType;
		int filterId = -1;
		int frameId = -1; 
		String MultiType = null;
		int imageId = -1; 
		String filePath = new String();
		
		ArrayList<TemplateInfo> templateInfos = new ArrayList<TemplateInfo>();
		
		Visualizer visualizer = PreviewManager.getInstance(mContext).getVisualizer();
		Visualizer.Editor vEditor = visualizer.getEditor(); 
		if(!PreviewManager.getInstance(mContext).getVisualizer().isOnEditMode()){
			vEditor.start(); 
		}
		
		List<Scene> scenes = visualizer.getRegion().getScenes(); 
		
		for(Scene scene : scenes){
			
			if(UserTag.isExtraScene(scene)){
				continue; 
			}
			
			SelectedOutputData selectedOutputData = new SelectedOutputData();
			ArrayList<ImageData> imageDatas = new ArrayList<ImageData>();
			ArrayList<ArrayList<Viewport>> viewportLists = new ArrayList<ArrayList<Viewport>>();
			ArrayList<Viewport> layerViewportList = new ArrayList<Viewport>();
			
			boolean isAccelerationScene = UserTag.isMaintainFeature(scene.getTagContainer(), UplusOutputData.SCENE_TYPE_ACCELERATION);  
			boolean isStepAppearEffect = UserTag.isMaintainFeature(scene.getTagContainer(), StepAppearEffect.JSON_VALUE_TYPE) && EffectUtil.useStepAppearEffect(effectJsonObject); 
			boolean isMultiFilterScene = UserTag.isMaintainFeature(scene.getTagContainer(), UplusOutputData.SCENE_TYPE_MULTI_FILTER);
			boolean isScaleEffect = UserTag.isMaintainFeature(scene.getTagContainer(), ScaleEffect.JSON_VALUE_TYPE) && EffectUtil.useScaleEffect(effectJsonObject); 
			
			L.d("accelerationScene : " + isAccelerationScene +", stepAppear effect : " + isStepAppearEffect); 
			
			if(scene.getClass().equals(ImageFileScene.class)){
				sceneType = ImageFileScene.JSON_VALUE_TYPE; 
				ImageFileScene imageFileScene = (ImageFileScene)scene; 
				filterId = imageFileScene.getFilterId();
				
				frameId = UplusImageFileSceneCoordinator.SINGLE_FRAME_ID;
				FileImageResource imageFileResource = (FileImageResource)imageFileScene.getImageResource();
				imageId = imageFileScene.getImageId();
				filePath = imageFileResource.getFilePath(); 
				imageDatas.add(getImageData(imageId, filePath));
				
				ImageFileScene.Editor imageFileSceneEditor = imageFileScene.getEditor(); 
				KenBurnsScaler.Editor kenburnScaler = (KenBurnsScaler.Editor) imageFileSceneEditor.getObject().getScaler().getEditor();
				Viewport[] viewPorts = kenburnScaler.getObject().getViewports();
				ArrayList<Viewport> viewportList = new ArrayList<Viewport>(); 
				for(Viewport viewport : viewPorts){
					viewportList.add(viewport); 
				}
				viewportLists.add(viewportList); 
				 
				selectedOutputData.setAccelerationZoom(isAccelerationScene); 
				
				selectedOutputData.setSceneType(sceneType); 
				selectedOutputData.setImageDatas(imageDatas); 
				selectedOutputData.setDate(imageDatas.get(0).date); 
				selectedOutputData.setFilterId(filterId); 
				selectedOutputData.setFrameId(frameId); 
				selectedOutputData.setSelectedViewportList(viewportLists); 
				mUplusOutputData.addOutputDataToOutputDataList(selectedOutputData);
				
			}else if(scene.getClass().equals(MultiLayerScene.class)){
				
				if (mUplusOutputData.getTheme().themeType != ThemeType.FRAME) {
					sceneType = MultiLayerScene.JSON_VALUE_TYPE;
				}else{
					sceneType = CollageScene.JSON_VALUE_TYPE;
				}
				
				MultiLayerScene multiLayerScene = (MultiLayerScene)scene; 
				Scene firstScene = multiLayerScene.getLayer(0); 
				if(firstScene.getClass().equals(LayerScene.class)){
					LayerScene firstLayerImageScene = (LayerScene)firstScene; 
					filterId = firstLayerImageScene.getFilterId(); 
				}else if(firstScene.getClass().equals(VideoFileScene.class)){
					VideoFileScene firstLayerVideoScene = (VideoFileScene)firstScene; 
					filterId = firstLayerVideoScene.getFilterId(); 
				}
				frameId = multiLayerScene.getTemplateId();
				
				//20151111 : 멀티에서 디자인으로 변경 시 VideoScene 포함 유무확인
				boolean hasVideoScene = false;
				if(sceneType.equalsIgnoreCase(CollageScene.JSON_VALUE_TYPE)){
					for(Scene layerScene : multiLayerScene.getLayers()){
						if(layerScene instanceof VideoFileScene){
							hasVideoScene = true;
							break;
						}
					}
				}
				
				//20151111 : 기존 멀티가 Video를 가진 혼합 콜라주 일 경우, layer 갯수에 따라서 콜라주 + 비디오  or 싱글 비디오 + 싱글 이미지로 구성해야 한다
				if(hasVideoScene){
					int imageLayerSize = multiLayerScene.getLayers().size() - 1;
					makeImageDatas(multiLayerScene, imageDatas);
					
					switch (imageLayerSize) {
					case 3:
						frameId = getMultiLayerThreeFrameId(imageDatas);
						//싱글 비디오 씬 생성  
						makeSingleVideoOutputData(multiLayerScene, filterId);
						break;
					case 2:
						frameId = MultiLayerData.MULTI_LAYER_COLUMN_TWO_PICTURES_ID;
						//싱글 비디오 씬 생성  
						makeSingleVideoOutputData(multiLayerScene, filterId);
						break;
					case 1:
						//싱글 이미지 생성 
						makeSingleImageOutputData(imageDatas, filterId, isAccelerationScene);
						//싱글 비디오 씬 생성  
						makeSingleVideoOutputData(multiLayerScene, filterId);
						
						break;
					}
					layerViewportList = null;
				}else{
					int index = 0; 
					for(Scene layerScene : multiLayerScene.getLayers()){
						ArrayList<Viewport> viewportList = new ArrayList<Viewport>(); 
						
						if(layerScene.getClass().equals(LayerScene.class)){
							LayerScene multiLayerImageScene = (LayerScene)layerScene; 
							imageId = multiLayerImageScene.getImageId();
							filePath = multiLayerImageScene.getImageFilePath();
							ImageData imageData = getImageData(imageId, filePath);
							imageDatas.add(imageData);
							
							LayerScene.Editor LayerSceneEditor = multiLayerImageScene.getEditor(); 
							KenBurnsScaler.Editor kenburnScaler = (Editor) LayerSceneEditor.getObject().getScaler().getEditor();
							Viewport[] viewPorts = kenburnScaler.getObject().getViewports();
							
							viewportList.add(viewPorts[0]); 
							viewportList.add(viewPorts[1]);
							
							viewportLists.add(viewportList);
							
						} else if (layerScene.getClass().equals(VideoFileScene.class)) {
						    VideoFileScene videoFileScene = (VideoFileScene)layerScene; 
			                filterId = videoFileScene.getFilterId(); 
			                imageId = videoFileScene.getVideoId(); 
			                
			                ImageData imageData = getVideoData(imageId, videoFileScene.getVideoFilePath());
			                imageDatas.add(imageData);

			                List<String> names = new ArrayList<String>(); 
			                names.add(imageId +""); 
			                
			                selectedOutputData.setSceneType(sceneType); 
			                selectedOutputData.setNameList(names); 
			                selectedOutputData.setVideoStart(videoFileScene.getVideoStartPosition()); 
			                selectedOutputData.setVideoEnd(videoFileScene.getVideoEndPosition()); 
			                selectedOutputData.setDate(imageDatas.get(0).date); 
			                selectedOutputData.setFilterId(filterId); 
			                selectedOutputData.setFrameId(frameId);
			                
			                imageData.imageCorrectData.videoStartPosition = videoFileScene.getVideoStartPosition();
			                imageData.imageCorrectData.videoEndPosition = videoFileScene.getVideoEndPosition();
			                imageData.imageCorrectData.videoDuration = (int)(videoFileScene.getVideoEndPosition() - videoFileScene.getVideoStartPosition());

			                viewportLists.add(new ArrayList<Viewport>());
						}
						layerViewportList.add(multiLayerScene.getLayerViewport(index));
						index++; 
					}
				}
				if (mUplusOutputData.getTheme().themeType == ThemeType.FRAME) {
					L.d("image data size : " + imageDatas.size());
					ArrayList<ImageData> collageDatas = imageDatas; 
					imageDatas = makeCollageData(collageDatas);
				}
				
				selectedOutputData.setSceneType(sceneType); 
				selectedOutputData.setImageDatas(imageDatas); 
				selectedOutputData.setDate(imageDatas.get(0).date);
				selectedOutputData.setScaleEffect(isScaleEffect); 
				selectedOutputData.setFilterId(filterId); 
				selectedOutputData.setMultiFilter(isMultiFilterScene); 
				selectedOutputData.setFrameId(frameId); 
				selectedOutputData.setSelectedViewportList(viewportLists); 
				selectedOutputData.setSelectedLayerViewportList(layerViewportList);
				selectedOutputData.setStepAppearEffect(isStepAppearEffect); 
				mUplusOutputData.addOutputDataToOutputDataList(selectedOutputData);
				
			}else if(scene.getClass().equals(BurstShotScene.class)){
				sceneType = BurstShotScene.JSON_VALUE_TYPE;
				BurstShotScene burstShotScene = (BurstShotScene)scene; 
				filterId = burstShotScene.getFilterId(); 
				int imageCount = burstShotScene.getImageSize(); 
				frameId =  30000  + (imageCount * 1000);
				for(int i = 0; i < burstShotScene.getImageSize(); i++){
					imageId = burstShotScene.getImageId(i); 
					filePath = burstShotScene.getImageFilePath(i); 
					ImageData burstImageData = getImageData(imageId, filePath); 
					imageDatas.add(burstImageData); 
				}
				
				selectedOutputData.setSceneType(sceneType); 
				selectedOutputData.setImageDatas(imageDatas); 
				selectedOutputData.setDate(imageDatas.get(0).date); 
				selectedOutputData.setFilterId(filterId); 
				selectedOutputData.setFrameId(frameId); 
				mUplusOutputData.addOutputDataToOutputDataList(selectedOutputData);
				
			}else if(scene.getClass().equals(CollageScene.class)){

				if (mUplusOutputData.getTheme().themeType != ThemeType.FRAME) {
					sceneType = MultiLayerScene.JSON_VALUE_TYPE;
				}else{
					sceneType = CollageScene.JSON_VALUE_TYPE;
				}
				
				CollageScene collageScene = (CollageScene)scene; 
				filterId = collageScene.getFilterId();
				for(CollageElement collageElement : collageScene.getCollageElements()){
					ImageData imageData = (ImageData)collageElement;
					imageDatas.add(imageData); 
				}
				
				if (mUplusOutputData.getTheme().themeType != ThemeType.FRAME) {
					templateInfos = templateManager.getTemplateArray(imageDatas.size(), Theme.THEME_NAME_CLEAN);
				} else {
					templateInfos = templateManager.getTemplateArray(imageDatas.size(), mUplusOutputData.getTheme().name);
				}
				
				TemplateInfo info = templateInfos.get(((int) Math.random() * templateInfos.size()));
				
				if(mUplusOutputData.getTheme().themeType == ThemeType.FRAME){
					frameId = info.getId();
				}else{
					boolean isColumnThreeTemplate = false; 
					if(imageDatas.size() == 3){
						int verticalCount  = 0; 
						for(ImageData _imageData : imageDatas){
							if(_imageData.isPotraitImage()){
								verticalCount++; 
							}
						}
						if(verticalCount == 3){
							isColumnThreeTemplate = true; 
						}
						
						if(isColumnThreeTemplate){
							frameId = MultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID;
						}else{
							frameId = MultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID;
						}
					}else{
						frameId = info.getId();
					}
					
				}
				L.d("frameId : " + frameId);
				
				if (mUplusOutputData.getTheme().themeType == ThemeType.FRAME) {
					L.d("image data size : " + imageDatas.size());
					ArrayList<ImageData> collageDatas = imageDatas; 
					imageDatas = makeCollageData(collageDatas);
				}
				
				selectedOutputData.setSceneType(sceneType); 
				selectedOutputData.setImageDatas(imageDatas); 
				selectedOutputData.setDate(imageDatas.get(0).date);
				selectedOutputData.setScaleEffect(isScaleEffect);
				selectedOutputData.setFilterId(filterId); 
				selectedOutputData.setMultiFilter(isMultiFilterScene);
				selectedOutputData.setFrameId(frameId); 
				selectedOutputData.setSelectedViewportList(viewportLists);
				selectedOutputData.setStepAppearEffect(isStepAppearEffect);
				mUplusOutputData.addOutputDataToOutputDataList(selectedOutputData);
				
			}else if(scene.getClass().equals(VideoFileScene.class)){
				sceneType = VideoFileScene.JSON_VALUE_TYPE;
				VideoFileScene videoFileScene = (VideoFileScene)scene; 
				filterId = videoFileScene.getFilterId(); 
				frameId = -1;
				
				imageId = videoFileScene.getVideoId(); 

				List<String> names = new ArrayList<String>(); 
				names.add(imageId +""); 
				
				selectedOutputData.setSceneType(sceneType); 
				selectedOutputData.setNameList(names); 
				selectedOutputData.setVideoStart(videoFileScene.getVideoStartPosition()); 
				selectedOutputData.setVideoEnd(videoFileScene.getVideoEndPosition()); 
				selectedOutputData.setFilterId(filterId); 
				selectedOutputData.setFrameId(frameId);
				mUplusOutputData.addOutputDataToOutputDataList(selectedOutputData);
			}
		}
		
		if (vEditor != null) {
			vEditor.finish();
		}
	}
	
	//20151111 :  멀티레이어 2frame(video + image) > 콜라주 변환 시 single video 생성을 위한 outputData 구성 
	private void makeSingleVideoOutputData(MultiLayerScene multiLayerScene, int filterId) {
		String sceneType;
		int frameId = -1; 
		int imageId = -1; 
		sceneType = VideoFileScene.JSON_VALUE_TYPE;
		frameId = -1;
		
		for(Scene layerScene : multiLayerScene.getLayers()){
			if(layerScene instanceof VideoFileScene){
				VideoFileScene videoFileScene = (VideoFileScene) layerScene;
				imageId = videoFileScene.getVideoId(); 
				SelectedOutputData selectedOutputData = new SelectedOutputData();
				List<String> names = new ArrayList<String>(); 
				names.add(imageId +""); 
				
				selectedOutputData.setSceneType(sceneType); 
				selectedOutputData.setNameList(names); 
				selectedOutputData.setVideoStart(videoFileScene.getVideoStartPosition()); 
				selectedOutputData.setVideoEnd(videoFileScene.getVideoEndPosition()); 
				selectedOutputData.setFilterId(filterId); 
				selectedOutputData.setFrameId(frameId);
				mUplusOutputData.addOutputDataToOutputDataList(selectedOutputData);
				break;
			}
		}
	}

	//20151111 :  멀티레이어 2frame(video + image) > 콜라주 변환 시 single image 생성을 위한 outputData 구성 
	private void makeSingleImageOutputData(ArrayList<ImageData> imageDatas, int filterId, boolean isAccelerationScene) {
		String sceneType;
		int frameId = -1; 
		int imageId = -1; 
		String filePath = new String();
		SelectedOutputData selectedOutputData = new SelectedOutputData();
		
		sceneType = ImageFileScene.JSON_VALUE_TYPE; 
		frameId = UplusImageFileSceneCoordinator.SINGLE_FRAME_ID;
		
		imageId = imageDatas.get(0).id;
		filePath = imageDatas.get(0).path;
		imageDatas.add(getImageData(imageId, filePath));
		
		selectedOutputData.setAccelerationZoom(isAccelerationScene); 
		selectedOutputData.setSceneType(sceneType); 
		selectedOutputData.setImageDatas(imageDatas); 
		selectedOutputData.setDate(imageDatas.get(0).date); 
		selectedOutputData.setFilterId(filterId); 
		selectedOutputData.setFrameId(frameId); 
		selectedOutputData.setSelectedViewportList(null); 
		mUplusOutputData.addOutputDataToOutputDataList(selectedOutputData);
	}


	//20151111 : 멀티레이어 >> 콜라주 변환 시 이미지가 3장일 경우 3열 Frame 사용 유무 설정
	private int getMultiLayerThreeFrameId(ArrayList<ImageData> imageDatas) {
		boolean isColumnThreeTemplate = false; 
		int frameId;
		int verticalCount  = 0; 
		for(ImageData _imageData : imageDatas){
			if(_imageData.isPotraitImage()){
				verticalCount++; 
			}
		}
		if(verticalCount == 3){
			isColumnThreeTemplate = true; 
		}
		if(isColumnThreeTemplate){
			frameId = MultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID;
		}else{
			frameId = MultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID;
		}
		return frameId;
	}

	//20151111 : 멀티레이어에 포함된 video를 제외한 Image data 구성
	private void makeImageDatas(MultiLayerScene multiLayerScene, ArrayList<ImageData> imageDatas) {
		for(Scene layerScene : multiLayerScene.getLayers()){
			if(layerScene.getClass().equals(LayerScene.class)){
				LayerScene multiLayerImageScene = (LayerScene)layerScene; 
				int imageId = multiLayerImageScene.getImageId();
				String filePath = multiLayerImageScene.getImageFilePath();
				ImageData imageData = getImageData(imageId, filePath);
				imageDatas.add(imageData);
			}
		}
	}


	/**
	 * 멀티씬과 collage scen,BurstShotScene에서 이미지 데이터 list를 만든다. <br>
	 * @param arrayType : source scene type<br> 
	 * @param targetType :target scene type<br> 
	 * @param object :  json object.<br> 
	 * @return : imageData list<br>
	 */
	private ArrayList<ImageData> getImageDatasFromMultiScene(String arrayType, String targetType, JSONObject object) {

		
		ArrayList<ImageData> imageDatas = new ArrayList<ImageData>();
		// collage scene id는 collage scene으로 변경 한다.
		JSONArray multiImages;
		try {
			multiImages = object.getJSONArray(arrayType);

			for (int k = 0; k < multiImages.length(); k++) {
				JSONObject multiObject = multiImages.getJSONObject(k);
				int imageId = 0;
				String path = new String(); 
				if(arrayType.equals(MultiLayerScene.JSON_NAME_LAYERS)){
					imageId = multiObject.getInt(LayerScene.JSON_NAME_IMAGE_ID);
					path = multiObject.getString(LayerScene.JSON_NAME_FILE_PATH);
				}else if(arrayType.equals(CollageScene.JSON_NAME_COLLAGE_ELEMENTS)){
					imageId = multiObject.getInt(CollageElement.JSON_NAME_IMAGE_ID);
					path = multiObject.getString(CollageElement.JSON_NAME_FILE_PATH);

				}else if(arrayType.equals(BurstShotScene.JSON_NAME_OBJECT)){
					imageId = multiObject.getInt(BurstShotScene.JSON_NAME_IMAGE_ID);
					path = multiObject.getString(BurstShotScene.JSON_NAME_FILE_PATH);

				}
				mSelectedImageMap.put(imageId + "", targetType);
				imageDatas.add(getImageData(imageId, path));
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return imageDatas;
	}
	
	/**
	 * BurstShotScene에서 이미지 데이터 list를 만든다. <br>
	 * @param arrayType : source scene type<br> 
	 * @param targetType :target scene type<br> 
	 * @param object :  json object.<br> 
	 * @return : imageData list<br>
	 */
	private ArrayList<ImageData> getImageDatasFromBurstScene(String arrayType, String targetType, JSONObject object) {

		
		ArrayList<ImageData> imageDatas = new ArrayList<ImageData>();
		// collage scene id는 collage scene으로 변경 한다.
		JSONArray burstShotImages;
		try {
			burstShotImages = object.getJSONArray(arrayType);

			for (int k = 0; k < burstShotImages.length(); k++) {
				JSONObject burstShotObject = burstShotImages.getJSONObject(k);
				int imageId = burstShotObject.getInt(BurstShotScene.JSON_NAME_IMAGE_ID);
				String path = burstShotObject.getString(BurstShotScene.JSON_NAME_FILE_PATH);
				mSelectedImageMap.put(imageId + "", targetType);
				imageDatas.add(getImageData(imageId, path));
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return imageDatas;
	}
	
	/**
	 * 전체 scene을 시간순으로 배치. 
	 */
	public void rulsetAllScenesByTime(){
		
		int i = 0; 
		int imageCount = 0; 
		for (SelectedOutputData selectedOutputData : mUplusOutputData.getOutputDataList()) {
			if (selectedOutputData.getSceneType().equals(ImageFileScene.JSON_VALUE_TYPE)) {
				
				ArrayList<ImageData> imageDatas = new ArrayList<ImageData>();				
				ImageData imageData = mUplusInputDataHashMap.get(mInputDataListBasedOnDate.get(i++).getName());
				imageDatas.add(imageData);
				selectedOutputData.setImageDatas(imageDatas);
				selectedOutputData.setDate(imageData.date);
				imageDatas = null;  
				
				L.d( selectedOutputData.getSceneType() + ", i : " + i +", path : " + imageData.path +", acceleration zoom : " + selectedOutputData.getAccelerationZoom()); 

				
			}else if(selectedOutputData.getSceneType().equals(MultiLayerScene.JSON_VALUE_TYPE)){
				imageCount = (int) ((selectedOutputData.getFrameId() / 1000) % 10);
				ArrayList<ImageData> multiImageDatas = new ArrayList<ImageData>();
				
				//멀티씬의 들어갈 사진의 갯수 만큼을 구한다. 
				for(int j = 0; j < imageCount ; j ++){
					L.d("multiLayer, i : " + i +", path : " + mUplusInputDataHashMap.get(mInputDataListBasedOnDate.get(i).getName()).path); 
					multiImageDatas.add(mUplusInputDataHashMap.get(mInputDataListBasedOnDate.get(i++).getName())); 
				}
				
				//3개 멀티씬에서는 사진의 가로 세로 여부에 따라서 frame을 설정한다. 
				if(selectedOutputData.getFrameId() == UplusMultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID ||
						selectedOutputData.getFrameId() == UplusMultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID){
					
					int verticalImageCount = 0; 
					for(ImageData _imageData : multiImageDatas){
						if(_imageData.isPotraitImage()){
							verticalImageCount++; 
						}
					}
					if(verticalImageCount == 3){
						selectedOutputData.setFrameId(UplusMultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID);
					}else{
						selectedOutputData.setFrameId(UplusMultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID);
					}
				}
				selectedOutputData.setImageDatas(multiImageDatas); 
				selectedOutputData.setDate(multiImageDatas.get(0).date); 
				multiImageDatas = null; 
				
			}else if(selectedOutputData.getSceneType().equals(CollageScene.JSON_VALUE_TYPE)){
				imageCount = (int) ((selectedOutputData.getFrameId() / 1000) % 10);
				ArrayList<ImageData> collageImageDatas = new ArrayList<ImageData>();
				
				//콜라쥬씬의 들어갈 사진의 갯수 만큼을 구한다. 
				for(int j = 0; j < imageCount ; j ++){
					L.d("collage, i : " + i +", path : " + mUplusInputDataHashMap.get(mInputDataListBasedOnDate.get(i).getName()).path);
					collageImageDatas.add(mUplusInputDataHashMap.get(mInputDataListBasedOnDate.get(i++).getName())); 
				}
				selectedOutputData.setImageDatas(makeCollageData(collageImageDatas)); 
				selectedOutputData.setDate(collageImageDatas.get(0).date); 
				collageImageDatas = null; 
			}else if(selectedOutputData.getSceneType().equals(BurstShotScene.JSON_VALUE_TYPE)){
				int imageCount0 = ((selectedOutputData.getFrameId() / 10000) - 3)*10 ;
				imageCount = (int) ((selectedOutputData.getFrameId() / 1000) % 10) + imageCount0;
				L.d("image count : " + imageCount); 
				ArrayList<ImageData> burstImageDatas = new ArrayList<ImageData>();
				
				//버스트 씬에 들어 갈 사진의 갯 수 만큼 만든다.  
				for(int j = 0; j < imageCount ; j ++){
					L.d("burst, i : " + i +", path : " + mUplusInputDataHashMap.get(mInputDataListBasedOnDate.get(i).getName()).path);
					burstImageDatas.add(mUplusInputDataHashMap.get(mInputDataListBasedOnDate.get(i++).getName())); 
				}
				selectedOutputData.setImageDatas(burstImageDatas); 
				selectedOutputData.setDate(burstImageDatas.get(0).date); 
				burstImageDatas = null; 
			}
		}
	}
	
	@Override
	public void selectImageFileScene() {

		ArrayList<AnalyzedInputData> selectedImageFiles = new ArrayList<AnalyzedInputData>();

		selectedImageFiles = getUnSelectedInputDatasForSingleScene(mSingleSceneCount, ImageFileScene.JSON_VALUE_TYPE);
		applyOutputDataSingle(selectedImageFiles, ImageFileScene.JSON_VALUE_TYPE);
	}

	@Override
	public void selectMultiLayerScene() {

		int imageCount = 0;
		int verticalImageCount = 0;
		List<ArrayList<ImageData>> selectedMultiFiles = new ArrayList<ArrayList<ImageData>>();

		if ((mMultiSceneCount != 0) && (mUplusOutputData.getTheme().themeType != ThemeType.FRAME)) {
			L.d("start SelectMultiLayerScene");
			ArrayList<AnalyzedInputData> selectedImageFiles = new ArrayList<AnalyzedInputData>();
			selectedImageFiles = getUnSelectedInputDatasForMultiScene(mTotalMultiImageCount, MultiLayerScene.JSON_VALUE_TYPE);
			L.d("selected image count : " + selectedImageFiles.size() + " mMultiCount : " + mMultiSceneCount);

			for (SelectedOutputData selectedOutputData : mUplusOutputData.getOutputDataList()) {

				if (selectedOutputData.getSceneType().equals(MultiLayerScene.JSON_VALUE_TYPE)||
						selectedOutputData.getSceneType().equals(BurstShotScene.JSON_VALUE_TYPE)) {
					imageCount = (int) ((selectedOutputData.getFrameId() / 1000) % 10);
					L.d("imageCount : " + imageCount);

					ArrayList<ImageData> multiImages = new ArrayList<ImageData>();
					ImageData imageData = null;
					AnalyzedInputData analyzedInputData = null;
					boolean preferPortrait = false;
					
					
					for (int j = 0; j < imageCount; j++) {
						verticalImageCount = 0;
						// 20150226 olive : #10758 각 프레임의 우선순위에 맞는 이미지를 선택한다.
						preferPortrait = selectedOutputData.isPreferPortraitFrame(j);
						for (int i = 0; i < selectedImageFiles.size(); i++) {
							analyzedInputData = selectedImageFiles.get(i);
							imageData = mUplusInputDataHashMap.get(analyzedInputData.getName());
							if (imageData.isPotraitImage()) {
								// 세로 사진
								if (preferPortrait) {
									L.i("match portrait");
									break;
								}
							} else {
								// 가로 사진
								if (!preferPortrait) {
									L.i("match landscape");
									break;
								}
							}
							imageData = null;
						}
						// 20150226 olive : #10758 프레임의 우선순위에 맞는 사진이 없는 경우 첫 번째
						// 사진(시간순, #10758 반영하기 이전의 rule)을 선택한다.
						if (imageData == null && selectedImageFiles.size() > 0) {
							L.i("missmatch");
							analyzedInputData = selectedImageFiles.get(0);
							imageData = mUplusInputDataHashMap.get(analyzedInputData.getName());
						}
						selectedImageFiles.remove(analyzedInputData);
						multiImages.add(imageData);
					}
					
					if(selectedOutputData.getFrameId() == UplusMultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID ||
							selectedOutputData.getFrameId() == UplusMultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID){
						
						verticalImageCount = 0; 
						for(ImageData _imageData : multiImages){
							if(_imageData.isPotraitImage()){
								verticalImageCount++; 
							}
						}
						if(verticalImageCount == 3){
							selectedOutputData.setFrameId(UplusMultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID);
						}else{
							selectedOutputData.setFrameId(UplusMultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID);
						}
					}
					selectedMultiFiles.add(multiImages);
					multiImages = null;
				}
			}

			applyOutputDataMulti(selectedMultiFiles, MultiLayerScene.JSON_VALUE_TYPE);
		}
	}
	
	@Override
	public void selectBurstShotScene() {

	}
	
	public void selectMultiFilterScene(){
		
		int totalSelectedCount = 0;
		
		if(mInputDataTypeBasedOnMultiFilterProtagonist != null && !mInputDataTypeBasedOnMultiFilterProtagonist.isEmpty()){
			//remove already selected image. 
			for(int i = 0; i < mInputDataTypeBasedOnMultiFilterProtagonist.size(); i++){
				AnalyzedInputData inputData = mInputDataTypeBasedOnMultiFilterProtagonist.get(i); 
				if (!mSelectedImageMap.get(inputData.getName()).equals(UplusOutputData.SCENE_TYPE_NONE)) {
					mInputDataTypeBasedOnMultiFilterProtagonist.remove(i); 
				}
			}
		}
		
		
		if(mInputDataTypeBasedOnMultiFilterProtagonist != null && !mInputDataTypeBasedOnMultiFilterProtagonist.isEmpty()){
			L.d("protagonist height size : " + mInputDataTypeBasedOnMultiFilterProtagonist.size());
			
			for(int i = 0; i < mInputDataTypeBasedOnMultiFilterProtagonist.size(); i++){
				AnalyzedInputData inputData = mInputDataTypeBasedOnMultiFilterProtagonist.get(i); 
				if (mSelectedImageMap.get(inputData.getName()).equals(UplusOutputData.SCENE_TYPE_NONE)) {
					mSelectedImageMap.put(inputData.getName(), UplusOutputData.SCENE_TYPE_MULTI_FILTER);
					totalSelectedCount++;
					if(totalSelectedCount == MULTI_FILTER_ZOOM_MAX_COUNT){
						break; 
					}
				}
			}	
		}
		
		if(totalSelectedCount < MULTI_FILTER_ZOOM_MAX_COUNT){
			for(int i = 0; i < mInputDataTypeBasedOnMultiFilter.size(); i++){
				AnalyzedInputData inputData = mInputDataTypeBasedOnMultiFilter.get(i); 
				if (mSelectedImageMap.get(inputData.getName()).equals(UplusOutputData.SCENE_TYPE_NONE)) {
					mSelectedImageMap.put(inputData.getName(), UplusOutputData.SCENE_TYPE_MULTI_FILTER);
					totalSelectedCount++; 
					if(totalSelectedCount == MULTI_FILTER_ZOOM_MAX_COUNT){
						break; 
					}
				}
			}
		}
		
		
	}
	
	public void selectAccelerationZoomScene(){
		
		int totalSelectedCount = 0;
		
		if(mInputDataTypeBasedOnAccelerationZoomProtagonist != null && !mInputDataTypeBasedOnAccelerationZoomProtagonist.isEmpty()){
			//remove already selected image. 
			for(int i = 0; i < mInputDataTypeBasedOnAccelerationZoomProtagonist.size(); i++){
				AnalyzedInputData inputData = mInputDataTypeBasedOnAccelerationZoomProtagonist.get(i); 
				if (!mSelectedImageMap.get(inputData.getName()).equals(UplusOutputData.SCENE_TYPE_NONE)) {
					mInputDataTypeBasedOnAccelerationZoomProtagonist.remove(i); 
				}
			}
		}
		
		
		if(mInputDataTypeBasedOnAccelerationZoomProtagonist != null && !mInputDataTypeBasedOnAccelerationZoomProtagonist.isEmpty()){
			L.d("protagonist with size : " + mInputDataTypeBasedOnAccelerationZoomProtagonist.size());
			
			for(int i = 0; i < mInputDataTypeBasedOnAccelerationZoomProtagonist.size(); i++){
				AnalyzedInputData inputData = mInputDataTypeBasedOnAccelerationZoomProtagonist.get(i); 
				if (mSelectedImageMap.get(inputData.getName()).equals(UplusOutputData.SCENE_TYPE_NONE)) {
					mSelectedImageMap.put(inputData.getName(), UplusOutputData.SCENE_TYPE_ACCELERATION);
					totalSelectedCount++;
					if(totalSelectedCount == ACCELERATION_ZOOM_MAX_COUNT){
						break; 
					}
				}
			}	
		}
		
		if(totalSelectedCount < ACCELERATION_ZOOM_MAX_COUNT){
			for(int i = 0; i < mInputDataTypeBasedAccelerationZoom.size(); i++){
				AnalyzedInputData inputData = mInputDataTypeBasedAccelerationZoom.get(i); 
				if (mSelectedImageMap.get(inputData.getName()).equals(UplusOutputData.SCENE_TYPE_NONE)) {
					mSelectedImageMap.put(inputData.getName(), UplusOutputData.SCENE_TYPE_ACCELERATION);
					totalSelectedCount++; 
					if(totalSelectedCount == ACCELERATION_ZOOM_MAX_COUNT){
						break; 
					}
				}
			}
		}
	}
	
	@Override
	public void selectCollageScene() {

		int imageCount = 0; 
		
		if (mMultiSceneCount > 0 && mUplusOutputData.getTheme().themeType == ThemeType.FRAME) {
			L.d("start selectCollageScene");
			
			ArrayList<ImageData> selectedImageDatas = new ArrayList<ImageData>();
			List<ArrayList<ImageData>> selectedImageDataList = new ArrayList<ArrayList<ImageData>>(); 
			selectedImageDatas = getUnSelectedInputDatasForCollage(mTotalMultiImageCount, CollageScene.JSON_VALUE_TYPE);
			L.d("selected image count : " + selectedImageDatas.size() + " mMultiCount : " + mMultiSceneCount);
			
			int count = 0; 
			
			for (SelectedOutputData selectedOutputData : mUplusOutputData.getOutputDataList()) {
				if(selectedOutputData.getSceneType().equals(CollageScene.JSON_VALUE_TYPE)){
					imageCount = (int) ((selectedOutputData.getFrameId() / 1000) % 10);
					L.d("imageCount : " + imageCount);
					
					ArrayList<ImageData> multiImages = new ArrayList<ImageData>();
					
					for (int i = 0; i < imageCount; i++) {
						L.d("selected image count : " + count); 
						multiImages.add(selectedImageDatas.get(count)); 
						count++; 
					}
					selectedImageDataList.add(makeCollageData(multiImages)); 
				}
			}
			applyOutputDataCollage(selectedImageDataList, CollageScene.JSON_VALUE_TYPE); 
		}
	}

	/**
	 * collage scene으로 선택된 사진 outputDataList에 적용.<br>
	 * @param selectedImageFiles : 선택된 이미지 파일. <br>
	 * @param sceneType : 적용할 scene tyep. <br>
	 */
	private void applyOutputDataCollage(List<ArrayList<ImageData>> selectedImageFiles, String sceneType) {

		int i = 0;
		for (SelectedOutputData selectedOutputData : mUplusOutputData.getOutputDataList()) {
			if (selectedOutputData.getSceneType().equals(CollageScene.JSON_VALUE_TYPE)) {
				selectedOutputData.setFrameId(selectedImageFiles.get(i).get(0).imageCorrectData.collageTempletId);
				// selectedOutputData.setSceneType(SCENE_TYPE_COLLAGE);
				selectedOutputData.setImageDatas(selectedImageFiles.get(i));
				selectedOutputData.setDate(-1);
				i++;
				if (i == selectedImageFiles.size()) {
					break;
				}
			}
		}
	}

	/**
	 * single scene으로 선택된 사진 outputDataList에 적용.<br>
	 * @param selectedImageFiles : 선택된 이미지 파일. <br>
	 * @param sceneType : 적용할 scene tyep. <br>
	 */
	private void applyOutputDataSingle(ArrayList<AnalyzedInputData> selectedImageFiles, String sceneType) {

		int i = 0;

		L.d("Total selected count : " + selectedImageFiles.size() + " total single scene count : " + mSingleSceneCount);

		for (SelectedOutputData selectedOutputData : mUplusOutputData.getOutputDataList()) {
			if (selectedOutputData.getSceneType().equals(sceneType)) {

				ArrayList<ImageData> imageDatas = new ArrayList<ImageData>();
				ImageData imageData = mUplusInputDataHashMap.get(selectedImageFiles.get(i).getName());
				L.d("i : " + i + ", name : " + selectedImageFiles.get(i).getName() + ", date : " + imageData.date);
				i++;

				imageDatas.add(imageData);
				selectedOutputData.setImageDatas(imageDatas);
				selectedOutputData.setDate(imageData.date);

				imageDatas = null;
			}
		}

	}

	/**
	 * multi scene으로 선택된 사진을 outputDataList에 적용.<br>
	 * @param selectedImageFiles : 선택된 이미지 파일. <br>
	 * @param sceneType : 적용할 scene tyep. <br>
	 */
	private void applyOutputDataMulti(List<ArrayList<ImageData>> selectedImageFiles, String sceneType) {

		int i = 0;

		L.d("Total Multi count : " + selectedImageFiles.size() + " mMultiCount : " + mMultiSceneCount);

		for (SelectedOutputData selectedOutputData : mUplusOutputData.getOutputDataList()) {
			if (selectedOutputData.getSceneType().equals(sceneType)) {
				selectedOutputData.setImageDatas(selectedImageFiles.get(i));
				selectedOutputData.setDate(-1);
				i++;
			}
		}

	}
	
	/**
	 * 멀티씬에서 사용할 사진 선택<br>
	 * @param count : 선택해야 하는 사진 갯수.<br>
	 * @param sceneType : 선택해야 하는 사진 타입<br>
	 * @return : 선택한 imageData list.<br>
	 */
	private ArrayList<AnalyzedInputData> getUnSelectedInputDatasForMultiScene(int count, String sceneType) {

		ArrayList<AnalyzedInputData> selectedImageFiles = new ArrayList<AnalyzedInputData>();
		int multiSceneCount = 0;
		List<AnalyzedInputData> mInputDataList = mInputDataListBasedOnQualityScore;
		
		L.d("count : " + count + ", scene type : " + sceneType);

		for (AnalyzedInputData analyzedInputData : mInputDataList) {
			// 이미 선택되어 있다면 제외한다.
			if (mSelectedImageMap.get(analyzedInputData.getName()).equals(UplusOutputData.SCENE_TYPE_NONE)) {
				multiSceneCount++;
				selectedImageFiles.add(analyzedInputData);
				mSelectedImageMap.put(analyzedInputData.getName(), sceneType);
				if (multiSceneCount == count) {
					break;
				}
			}
		}
		// 선택된 사진을 시간순으로 배치 한다.
		Comparator<AnalyzedInputData> comparatorDate = new Comparator<AnalyzedInputData>() {

			@Override
			public int compare(AnalyzedInputData lhs, AnalyzedInputData rhs) {
				long lhsDate = lhs.getDate(); 
				long rhsDate = rhs.getDate(); 
				if(lhsDate < rhsDate){
					return -1; 
				}else if (lhsDate == rhsDate){
					return 0; 
				}else{
					return 1; 
				}
			}
		};
		Collections.sort(selectedImageFiles, comparatorDate);
		/*
 		for (int i = 0; i < selectedImageFiles.size(); i++) {
			L.d("unSelected id : " + selectedImageFiles.get(i).getName() +", date : " + selectedImageFiles.get(i).getDate());
		}*/		
		return selectedImageFiles;
	}

	/**
	 * single scene에서 사용될 사진을 선택.<br>
	 * @param count : 선택해야 하는 사진 갯수.<br>
	 * @param sceneType : 선택해야 하는 scene type<br>
	 * @return 선택한 imageData list.<br>
	 */

	private ArrayList<AnalyzedInputData> getUnSelectedInputDatasForSingleScene(int count, String sceneType) {

		ArrayList<AnalyzedInputData> selectedImageFiles = new ArrayList<AnalyzedInputData>();

		int selectCount = 0;

		L.d("count : " + count + ", scene type : " + sceneType);

		// 가로 사진에서 선택. 
		for (AnalyzedInputData analyzedInputData : mInputDataTypeBaseOnWidth) {

			// 이미 선택되어 있다면 제외한다.
			if (mSelectedImageMap.get(analyzedInputData.getName()).equals(UplusOutputData.SCENE_TYPE_NONE)) {
				selectCount++;
				selectedImageFiles.add(analyzedInputData);
				mSelectedImageMap.put(analyzedInputData.getName(), sceneType);
				if (selectCount == count) {
					break;
				}
			}
		}

		L.d("selected count : " + selectCount + "total need count : " + count);
		
		if(selectCount < count){
		//세로 사진에서 선택한다. 
			for (AnalyzedInputData analyzedInputData : mInputDataTypeBaseOnHeight) {
				// 이미 선택되어 있다면 제외한다.
				if (mSelectedImageMap.get(analyzedInputData.getName()).equals(UplusOutputData.SCENE_TYPE_NONE)) {
					selectCount++;
					selectedImageFiles.add(analyzedInputData);
					mSelectedImageMap.put(analyzedInputData.getName(), sceneType);
					if (selectCount == count) {
						break;
					}
				}
			}
			L.d("selected count : " + selectCount + "total need count : " + count);
		}

		// 선택된 사진을 시간순으로 배치 한다.
		Comparator<AnalyzedInputData> comparatorDate = new Comparator<AnalyzedInputData>() {

			@Override
			public int compare(AnalyzedInputData lhs, AnalyzedInputData rhs) {
				long lhsDate = lhs.getDate(); 
				long rhsDate = rhs.getDate(); 
				if(lhsDate < rhsDate){
					return -1; 
				}else if (lhsDate == rhsDate){
					return 0; 
				}else{
					return 1; 
				}
			}
		};
		Collections.sort(selectedImageFiles, comparatorDate);

/*		for (int i = 0; i < selectedImageFiles.size(); i++) {
			L.d("unSelected id : " + selectedImageFiles.get(i).getName() +", date : " + selectedImageFiles.get(i).getDate());
		}*/
		return selectedImageFiles;
	}

	/**
	 * collage를 만들 사진을 선택한다. <br>
	 * @param count : 선택해야 할 사진 갯수.<br> 
	 * @param sceneType : 선택 해야 할 scene type.<br>
	 * @return 선택된 imageData list.<br>
	 */
	private ArrayList<ImageData> getUnSelectedInputDatasForCollage(int count, String sceneType) {

		ArrayList<AnalyzedInputData> selectedImageFiles = new ArrayList<AnalyzedInputData>();

		int selectedImageCount = 0;
		int needImageCount = 0;

		L.d("count : " + count + ", scene type : " + sceneType);

		// 1. 한명의 얼굴이 포함된 사진을 선택한다. 
		for (AnalyzedInputData analyzedInputData : mInputDataTypeBasedOnPerson) {
			ImageData imageData = mUplusInputDataHashMap.get(analyzedInputData.getName());
			// 이미 선택되어 있다면 제외한다.
			if ((imageData.faceDataItems.size() == 1) && (mSelectedImageMap.get(analyzedInputData.getName()).equals(UplusOutputData.SCENE_TYPE_NONE))) {
				selectedImageCount++;
				selectedImageFiles.add(analyzedInputData);
				mSelectedImageMap.put(analyzedInputData.getName(), sceneType);
				if (selectedImageCount == count) {
					break;
				}
			}
		}
		// 2. 풍경 사진을 선택한다.  
		if (count > selectedImageCount) {
			needImageCount = count - selectedImageCount; 

			for (AnalyzedInputData analyzedInputData : mInputDataTypeBasedOnLandscape) {
				
				// 이미 선택되어 있다면 제외한다.
				if (mSelectedImageMap.get(analyzedInputData.getName()).equals(UplusOutputData.SCENE_TYPE_NONE)) {
					selectedImageCount++;
					selectedImageFiles.add(analyzedInputData);
					mSelectedImageMap.put(analyzedInputData.getName(), sceneType);
					if (count == selectedImageCount) {
						break;
					}
				}
			}
		}
		
		List<AnalyzedInputData> analyzedInputDatas = mInputDataListBasedOnQualityScore; 
		
		//3. 시간이나 퀄리티순으로 사진을 선택한다. 
		if (count > selectedImageCount) {
			needImageCount = count - selectedImageCount; 

			for (AnalyzedInputData analyzedInputData : analyzedInputDatas) {
				
				// 이미 선택되어 있다면 제외한다.
				if (mSelectedImageMap.get(analyzedInputData.getName()).equals(UplusOutputData.SCENE_TYPE_NONE)) {
					selectedImageCount++;
					selectedImageFiles.add(analyzedInputData);
					mSelectedImageMap.put(analyzedInputData.getName(), sceneType);
					if (count == selectedImageCount) {
						break;
					}
				}
			}
		}

		// 4. 선택된 사진을 시간순으로 배치 한다.
		Comparator<AnalyzedInputData> comparatorDate = new Comparator<AnalyzedInputData>() {

			@Override
			public int compare(AnalyzedInputData lhs, AnalyzedInputData rhs) {
				long lhsDate = lhs.getDate(); 
				long rhsDate = rhs.getDate(); 
				if(lhsDate < rhsDate){
					return -1; 
				}else if (lhsDate == rhsDate){
					return 0; 
				}else{
					return 1; 
				}
			}
		};
		Collections.sort(selectedImageFiles, comparatorDate);

		ArrayList<ImageData> imageDatas = new ArrayList<ImageData>(); 
		
/*		for (int i = 0; i < selectedImageFiles.size(); i++) {
			L.d("unSelected id : " + selectedImageFiles.get(i).getName());
			imageDatas.add(mUplusInputDataHashMap.get(selectedImageFiles.get(i).getName()));
		}*/
		return imageDatas;
	}

	/**
	 * mUplusInputDataHashMap 얻어오기.<br>
	 * @return mUplusInputDataHashMap <br>
	 */
	public HashMap<String, ImageData> getUplusInputDataHashMap() {
		return mUplusInputDataHashMap;
	}

	/**
	 * mUplusInputDataHashMap 데이터 초기화.<br>
	 * 
	 * @param imageDataList
	 */
	private void makeUplusInputDataHashMap(List<ImageData> imageDataList) {
		// key : id, value : imagedata
		mUplusInputDataHashMap = new HashMap<String, ImageData>(imageDataList.size());

		List<ImageData> tempImageDataList = imageDataList;
		for (ImageData imageData : tempImageDataList) {
			if (!mUplusInputDataHashMap.containsKey(String.valueOf(imageData.id))) {
				mUplusInputDataHashMap.put(String.valueOf(imageData.id), imageData);
			}
		}
	}
	

	/**
	 * 사진 데이터를 인물이 있는 가로/세로 사진, 풍경 가로/세로, 인물사진, 풍경 사진으로 분류.<br> 
	 * @param imageDataList
	 */
	private void uplusInputDataDetermination(List<ImageData> imageDataList) {
		
		//주인공 이미지 아이디 설정.
		int protagonistId = new ImageSearch(mContext, null).getMainProtagonistPersonId();
		L.d("set protagonist id : " + protagonistId);
		mUplusOutputData.setProtagonistId(protagonistId);

		Comparator<ImageData> comparatorScore = new Comparator<ImageData>() {
	
			@Override
			public int compare(ImageData lhs, ImageData rhs) {
				long lhsScore = lhs.qualityScore; 
				long rhsScore = rhs.qualityScore; 
				if(lhsScore > rhsScore){
					return -1; 
				}else if (lhsScore == rhsScore){
					return 0; 
				}else{
					return 1; 
				}		
			}
		};
		Collections.sort(imageDataList, comparatorScore);
		
		Size dstSize = PreviewManager.DEFAULT_PREVIEW_RESOLUTION.getSize();
		
		for (ImageData imageData : imageDataList) {
			Size srcSize = new Size(imageData.width, imageData.height); 
			double scale = getScaleValue(srcSize, dstSize);
			
			if (imageData.faceDataItems != null) {
				// 인물이 있는 사진.
				if (!imageData.isPotraitImage()) {
					// 인물이 있는 가로 모드
					mInputDataTypeBasedOnPersonWidth.add(new AnalyzedInputData(String.valueOf(imageData.id), imageData.date));
					if(imageData.width != imageData.height){
						if(imageData.faceDataItems.size() <= ACCELERATION_ZOOM_MAX_PERSON_NUM ){
							
							boolean validSize = true; 
							for(ImageFaceData faceData : imageData.faceDataItems){
								Rect faceRect = getFaceRect(imageData, faceData);
								double faceHeight = faceRect.height()*scale;
								double faceWidth = (faceHeight/dstSize.height)*dstSize.width; 
								Size faceSize = new Size((int)faceHeight, (int)faceWidth);
								double faceScale = getScaleValue(dstSize, faceSize);
								L.d("aronia name : " + imageData.fileName + ", faceScale : " + faceScale); 
								if(faceScale < ACCELERATION_ZOOM_MIN_PERCENT || faceScale > ACCELERATION_ZOOM_MAX_PERCENT){
									validSize = false; 
									break; 
								}
							}
							if(validSize){
								mInputDataTypeBasedAccelerationZoom.add(new AnalyzedInputData(String.valueOf(imageData.id), imageData.date));
								
								for(ImageFaceData faceData : imageData.faceDataItems){
									if(faceData.personId == protagonistId){
										mInputDataTypeBasedOnAccelerationZoomProtagonist.add(new AnalyzedInputData(String.valueOf(imageData.id), imageData.date));
										break; 
									}
								}
							}
						}
					}
					
				} else {
					// 인물이 있는 세로 모드
					mInputDataTypeBasedOnPersonHeight.add(new AnalyzedInputData(String.valueOf(imageData.id), imageData.date));
				
					boolean isOverMinSize = false; 
					for(ImageFaceData faceData : imageData.faceDataItems){
						Rect faceRect = getFaceRect(imageData, faceData); 
						if(faceRect.width() * scale > MULTI_FILTER_ZOOM_MIN_SIZE || faceRect.height() * scale > MULTI_FILTER_ZOOM_MIN_SIZE){
							isOverMinSize = true; 
							break; 
						}
					}
					if(isOverMinSize){
						mInputDataTypeBasedOnMultiFilter.add(new AnalyzedInputData(String.valueOf(imageData.id), imageData.date));
						
						for(ImageFaceData faceData : imageData.faceDataItems){
							if(faceData.personId == protagonistId){
								mInputDataTypeBasedOnMultiFilterProtagonist.add(new AnalyzedInputData(String.valueOf(imageData.id), imageData.date));
								break; 
							}
						}
					}
				}
				
				
				// 인물 사진 total.
				mInputDataTypeBasedOnPerson.add(new AnalyzedInputData(String.valueOf(imageData.id), imageData.date));
			} else {
				// 풍경 사진.
				if (imageData.orientation.equals(String.valueOf(ImageAnalysis.ORIENTATION_DEGREE_0))
						|| imageData.orientation.equals(String.valueOf(ImageAnalysis.ORIENTATION_DEGREE_180))) {
					//풍경 가로 모드
					mInputDataTypeBasedOnLandscapeWidth.add(new AnalyzedInputData(String.valueOf(imageData.id), imageData.date));
				} else {
					// 풍경 세로 모드
					mInputDataTypeBasedOnLandscapeHeight.add(new AnalyzedInputData(String.valueOf(imageData.id), imageData.date));
				}
				// 풍경 total
				mInputDataTypeBasedOnLandscape.add(new AnalyzedInputData(String.valueOf(imageData.id), imageData.date));
			}
			
			if (imageData.isPotraitImage()) {
				// 세로 사진
				mInputDataTypeBaseOnHeight.add(new AnalyzedInputData(String.valueOf(imageData.id), imageData.date)); 
			}else{
				//가로 사진.
				mInputDataTypeBaseOnWidth.add(new AnalyzedInputData(String.valueOf(imageData.id), imageData.date)); 
			}
		}
		
		// 선택된 사진을 시간순으로 배치 한다.
		Comparator<AnalyzedInputData> comparatorDate = new Comparator<AnalyzedInputData>() {

			@Override
			public int compare(AnalyzedInputData lhs, AnalyzedInputData rhs) {
				long lhsDate = lhs.getDate(); 
				long rhsDate = rhs.getDate(); 
				if(lhsDate < rhsDate){
					return -1; 
				}else if (lhsDate == rhsDate){
					return 0; 
				}else{
					return 1; 
				}
			}
		};
		Collections.sort(mInputDataTypeBaseOnHeight, comparatorDate);
		Collections.sort(mInputDataTypeBaseOnWidth, comparatorDate);
		
	}
	
	private Rect getFaceRect(ImageData imageData, ImageFaceData faceData){
		
		float faceScaleValue = imageData.faceBitmapScale; 
		RectF faceRectF = FaceInfomation.getFaceRect((int)(imageData.width / faceScaleValue), 
				   (int)(imageData.height/faceScaleValue), 
					faceData.leftEyePoint, 
					faceData.rightEyePoint, 
					faceData.mouthPoint);
		float temp; 
		if(faceRectF.left > faceRectF.right){
		temp = faceRectF.right; 
		faceRectF.right = faceRectF.left; 
		faceRectF.left = temp; 
		}
		if(faceRectF.top > faceRectF.bottom){
		temp = faceRectF.bottom; 
		faceRectF.bottom = faceRectF.top; 
		faceRectF.top = temp; 
		}
		
		Rect faceRect = new Rect();
		faceRect.left = (int)(faceRectF.left * faceScaleValue); 
		faceRect.right = (int)(faceRectF.right * faceScaleValue); 
		faceRect.bottom = (int)(faceRectF.bottom * faceScaleValue); 
		faceRect.top = (int)(faceRectF.top * faceScaleValue);
		
		return faceRect; 
	}
	
	private double getScaleValue(Size srcSize, Size dstSize){
		return Math.sqrt((double) dstSize.product() / srcSize.product());
	}

	/**
	 * Data를 날짜 기반으로 리스트를 작성. mInpuDataListBasedOnDate<br>
	 * mSelectedImageMap 초기화.<br> 
	 * @param imageDataList
	 */
	private void analysisBasedOnDate(List<ImageData> imageDataList) {
		List<ImageData> tempImageDataList = imageDataList;

		Comparator<ImageData> comparatorDate = new Comparator<ImageData>() {

			@Override
			public int compare(ImageData lhs, ImageData rhs) {
				long lhsDate = lhs.date; 
				long rhsDate = rhs.date; 
				if(lhsDate < rhsDate){
					return -1; 
				}else if (lhsDate == rhsDate){
					return 0; 
				}else{
					return 1; 
				}
			}
		};
		Collections.sort(tempImageDataList, comparatorDate);

		for (ImageData imageData : tempImageDataList) {

			AnalyzedInputData analyzedInputData = new AnalyzedInputData();
			analyzedInputData.setName(String.valueOf(imageData.id));
			analyzedInputData.setDate(imageData.date);
			mInputDataListBasedOnDate.add(analyzedInputData);
			mSelectedImageMap.put(imageData.id + "", UplusOutputData.SCENE_TYPE_NONE);
		}
	}

	/**
	 * Data를 QualityScore별 list를 작성. mInpuDataListBasedOnQualityScore<br>
	 * 
	 * @param imageDataList : 전체 image data list. <br>
	 */
	private void analysisBasedOnQualityScore(List<ImageData> imageDataList) {
		List<ImageData> tempImageDataList = imageDataList;

		Comparator<ImageData> comparatorDate = new Comparator<ImageData>() {

			@Override
			public int compare(ImageData lhs, ImageData rhs) {
				
				long lhsScore = lhs.qualityScore; 
				long rhsScore = rhs.qualityScore; 
				if(lhsScore > rhsScore){
					return -1; 
				}else if (lhsScore == rhsScore){
					return 0; 
				}else{
					return 1; 
				}
			}
		};
		Collections.sort(tempImageDataList, comparatorDate);

		for (ImageData imageData : tempImageDataList) {

			AnalyzedInputData analyzedInputData = new AnalyzedInputData();
			analyzedInputData.setName(String.valueOf(imageData.id));
			analyzedInputData.setDate(imageData.date);
			mInputDataListBasedOnQualityScore.add(analyzedInputData);

		}
	}

	/**
	 * 주어진 이미지 데이터 리스트에서 한장의 collage를 추천 받음.<br> 
	 * @param multiImages : collage를 만들 image data list. <br>
	 * @return : collage로 생성된 image data list. <br>
	 */
	private ArrayList<ImageData> makeCollageData(ArrayList<ImageData> multiImages) {
		
		String themeName ="";
		if(mUplusOutputData.getTheme().themeType != ThemeType.FRAME){
			themeName = Theme.THEME_NAME_CLEAN;  
		}else{
			themeName = mUplusOutputData.getTheme().name;
		}
		ArrayList<ImageData> imageDatas = new ArrayList<ImageData>(); 
		if(multiImages != null && !multiImages.isEmpty()){
			CollageCorrectAdviser collageCorrectAdviser = new CollageCorrectAdviser(mContext); 
			imageDatas = collageCorrectAdviser.getMatchedImageDatas(multiImages, multiImages.size(), COLLAGE_DEFAULT_IMAGE_SIZE, themeName);
			if(imageDatas == null || imageDatas.isEmpty()){
				//collage를 생성하지 못했을 경우 강제 생성. 
				imageDatas = makeCollageDataEnForce(multiImages); 
			}
		}
		return imageDatas;
	}

	/**
	 * 주어진 이미지 데이터에서 한장의 collage를 강제 생성.<br> 
	 * @param inputImageDatas : collage를 만들 image data list. <br>
	 * @return : collage로 생성된 image data list. <br>
	 */
	private ArrayList<ImageData> makeCollageDataEnForce(ArrayList<ImageData> inputImageDatas) {

		String collageThemeName ="";
		if(mUplusOutputData.getTheme().themeType != ThemeType.FRAME){
			collageThemeName = Theme.THEME_NAME_CLEAN;  
		}else{
			collageThemeName = mUplusOutputData.getTheme().name;
		}
		ArrayList<ImageData> resultCollageDataList = new ArrayList<ImageData>(); 
		L.d(" input datas size :  " + inputImageDatas.size() + ", theme name : " + collageThemeName);
		if (inputImageDatas != null) {
			CollageCorrectAdviser collageCorrectAdviser = new CollageCorrectAdviser(mContext);
			
			ArrayList<TemplateInfo> templateInfos = templateManager.getTemplateArray(inputImageDatas.size(),collageThemeName);
			int templateId = templateInfos.get((int)Math.random()*templateInfos.size()).getId(); 
			
			L.d("templat id : " + templateId);

			resultCollageDataList = collageCorrectAdviser.getCollageImageDatasWithTemplateId(inputImageDatas, templateId,
					COLLAGE_DEFAULT_IMAGE_SIZE);

			for (ImageData imageData : resultCollageDataList) {
				L.d("templet id : " + imageData.imageCorrectData.collageTempletId);
			}

		}
		return resultCollageDataList; 
	}

	/**
	 * id와 path로 image data 생성.<br> 
	 * @param id : data id. <br>
	 * @param path : data path. <br>
	 * @return : image data. <br>
	 */
	private ImageData getImageData(int id, String path) {
		ImageSearch imageSearch = new ImageSearch(mContext, null);
		ImageData imageData = null;
		imageData = imageSearch.getImagaeDataForImageId(id);
		if (imageData == null) {
			imageData = new ImageData();
			imageData.id = id;
			imageData.path = path;
			imageData.fileName = getFileName(path); 
			imageData.orientation = ImageUtils.getOrientation(path) + "";
			Size size = ImageUtils.measureImageSize(path);
			imageData.width = size.width;
			imageData.height = size.height;

		}
		return imageData;
	}
	
	/**
	 * 파일의 경로에서 파일 이름 생성.<br> 
	 * @param path : data path.<br>
	 * @return : file name.<br>
	 */
	private String getFileName(String path) {
        String fileName = null;
        StringTokenizer st = new StringTokenizer(path, "/");
        while(st.hasMoreTokens()) {
        	fileName = st.nextToken();
        }
        StringTokenizer st2 = new StringTokenizer(fileName, ".");
        while(st2.hasMoreTokens()) {
            fileName = st2.nextToken();
            break;
        }

        return fileName;
	}

	/**
	 * id와 path로 video data 생성.<br> 
	 * @param id : data id. <br>
	 * @param path : data path. <br>
	 * @return : video data. <br>
	 */
	private ImageData getVideoData(int id, String path) {
		
		ImageData imageData = null;
		long date = 0; 
		
		UplusAnalysisPersister persister = UplusAnalysisPersister.getAnalysisPersister(mContext.getApplicationContext());
		Cursor cursor = persister.getVideoDataCursorInGallery(id);
		
		if(cursor != null){
			cursor.moveToNext(); 
			date = cursor.getLong(cursor.getColumnIndexOrThrow("datetaken"));
			if(date == 0){
				date = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATE_ADDED)) * 1000; 
			}
		}
				
		imageData = new ImageData();
		imageData.id = id;
		imageData.path = path;
		imageData.date = date;
		imageData.orientation = "0";

		return imageData;
	}

	public boolean getIsBurstShot(){
		return mIsBurstShot;
	}
}
