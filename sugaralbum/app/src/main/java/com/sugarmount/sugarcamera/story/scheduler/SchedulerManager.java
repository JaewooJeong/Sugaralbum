package com.sugarmount.sugarcamera.story.scheduler;

import android.content.Context;
import android.content.res.AssetManager;
import android.net.Uri;

import com.kiwiple.debug.L;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.scheduler.analysis.uplus.UplusImageAnalysis;
import com.kiwiple.scheduler.analysis.uplus.UplusVideoAnalysis;
import com.kiwiple.scheduler.data.uplus.UplusInputData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.scenario.uplus.UplusInputDataManager;
import com.kiwiple.scheduler.select.uplus.UplusOutputDataManager;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.Frame;
import com.kiwiple.scheduler.theme.Theme.FrameObject;
import com.kiwiple.scheduler.theme.Theme.ResourceType;
import com.kiwiple.scheduler.theme.Theme.ThemeType;
import com.kiwiple.scheduler.util.JsonReader;
import com.sugarmount.sugarcamera.story.database.StoryJsonPersister;
import com.sugarmount.sugarcamera.story.music.StoryMusicAnalysisManager;
import com.sugarmount.sugarcamera.story.theme.ThemeManager;
import com.sugarmount.sugarcamera.story.utils.KiwiplePreferenceManager;
import com.sugarmount.sugarcamera.utils.DeviceHelper;
import com.sugarmount.sugarcamera.utils.FileCopyUtil;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class SchedulerManager {
	private Context mContext;
	public static final String ASSET_FILTER_THEME_MUSIC_PATH = "audio/Inspire the World.mp3";

	private static final String ASSET_SCHEDULE_TEMPLATE_HORIZONTAL = "schedule_template/horizontal";
	private static final String ASSET_SCHEDULE_TEMPLATE_VERTICAL = "schedule_template/vertical";
	private static final String ASSET_SCHEDULE_NO_MULTI_TEMPLATE = "schedule_template_no_multi";

	private ThemeType mThemeType;

	private String mTitle;
	private Theme mTheme;

	private static final int FROM_MULTI_IMAGE_START = 8;
	private static final long ONE_DAY = 1000 * 60 * 60 * 24;

	public SchedulerManager(Context context) {
		mContext = context;
		mTheme = null;
	}

	public Uri makeJsonString(ArrayList<ImageData> mImageDataList, ArrayList<ImageData> mVideoDataList, String title, boolean isFromUplusBox) {
		
		if(!FileCopyUtil.isFileExits(mContext, Theme.OUTRO_ASSET_FILE_NAME)){
			FileCopyUtil.copyFile(mContext, Theme.OUTRO_ASSET_FILE_NAME);
		}
		
		L.i("실제 이미지 씬, 비디오 씬을 구성해서 Transition, Effect 구성 ");
        int maxImageCount = 0;
        if (DeviceHelper.isMaxiumLimitationReleaseDevice()) {
            maxImageCount = UplusImageAnalysis.MAXIMUM_LIMITATION_IMAGE_COUNT;
        } else {
            maxImageCount = UplusImageAnalysis.MAXIMUM_IMAGE_COUNT;
        }
        UplusInputDataManager uplusInputDataManager  = new UplusInputDataManager(mContext, maxImageCount);
		UplusInputData uplusInputData = uplusInputDataManager.setUplusInputData(mImageDataList, mVideoDataList);

		UplusImageAnalysis uplusImageAnalysis = new UplusImageAnalysis(mContext);

		UplusOutputData uplusOutputData = null;
		if (mTheme == null) {
			uplusOutputData = makeUplusOutputData();
		} else {
			uplusOutputData = makeUplusOutputData(mTheme);
		}
		uplusOutputData.setOldTheme(null); 
		uplusImageAnalysis.setUplusOutputData(uplusOutputData);
		uplusImageAnalysis.startInputDataAnalysis(uplusInputData);  //input data -> burst check -> schedule -> ruleset scene
//		uplusImageAnalysis.selectBurstShotScene();
		uplusImageAnalysis.selectAccelerationZoomScene();
		uplusImageAnalysis.selectMultiFilterScene(); 
		uplusImageAnalysis.setSchduleFromJsonObject(getJsonSchedule(mTheme, mImageDataList), mImageDataList.size());

		UplusVideoAnalysis uplusVideoAnalysis = new UplusVideoAnalysis(mContext, KiwiplePreferenceManager.getInstance(mContext.getApplicationContext()).getValue(KiwiplePreferenceManager.KEY_KIWIPLE_MAX_ATTACHED_VIDEO_COUNT, 0), false);
		uplusVideoAnalysis.startInputDataAnalysis(uplusInputData, uplusOutputData);
		uplusVideoAnalysis.selectVideoFileScene();

		StoryJsonPersister storyJsonPersister = StoryJsonPersister.getStoryJsonPersister(mContext.getApplicationContext());

		UplusOutputDataManager uplusOutputDataManager = new UplusOutputDataManager(mContext, uplusOutputData);
		uplusOutputDataManager.setOutputDataTitle(title);
		uplusOutputDataManager.setOutputDataMakeCacheData(false);
		uplusOutputDataManager.setOutputDataScene();
		uplusOutputDataManager.setOutputDataEffect(getJsonObject(mTheme, mTheme.effectJsonName),false, false);
		uplusOutputDataManager.setOutputDataTransition(getJsonObject(mTheme, mTheme.transitionJsonName));

		JSONObject analysisObject = StoryMusicAnalysisManager.getInstance(mContext).getPostCorrectionDurationForTransition(mTheme);
		uplusOutputDataManager.setOutputDataAnalysisScene(analysisObject);
		uplusOutputDataManager.setOutputDataAnalysisTransition(analysisObject);
		//uplusOutputDataManager.setOutputDataAnalysisEffect(analysisObject);
		
		uplusOutputDataManager.setOutputDataIntroOutroViewPort();

		JSONObject jsonObject = uplusOutputDataManager.makeJSONObject();

		if (jsonObject != null) {
			Uri jsonUri = storyJsonPersister.insertJsonData(jsonObject.toString(), mTheme.name, uplusOutputDataManager.getOutputDataSchedulerVersion(), isFromUplusBox, uplusOutputDataManager.getTitle());
			
			if (jsonUri != null) {
				storyJsonPersister.saveJsonThumbnail(jsonUri);
			}
			return jsonUri;
		} else {
			return null;
		}
	}
	


	private JSONObject getJsonSchedule(Theme theme, ArrayList<ImageData> imageDatas) {
		AssetManager assetManager = mContext.getAssets();
		String selectedJsonFile;
		String[] templetList = null;
		JSONObject jsonObject = null;

		int inputImageCount = imageDatas.size();
		int verticalImageCount = getVerticalImageCount(imageDatas); // 세로 이미지
																	// 숫자.
		int horizontalImageCount = inputImageCount - verticalImageCount;
		boolean isTotalVertical = verticalImageCount > horizontalImageCount;

		try {
			if (inputImageCount >= FROM_MULTI_IMAGE_START) {
				if (isTotalVertical) {
					templetList = assetManager.list(ASSET_SCHEDULE_TEMPLATE_VERTICAL);
				} else {
					templetList = assetManager.list(ASSET_SCHEDULE_TEMPLATE_HORIZONTAL);
				}
			} else {
				// 8장 아래의 사진이 들어오면 multi scene이 없는 json을 선택한다.
				templetList = assetManager.list(ASSET_SCHEDULE_NO_MULTI_TEMPLATE);
			}
			selectedJsonFile = templetList[(int) (Math.random() * templetList.length)];
			L.d("selected file name : " + selectedJsonFile);
			InputStream inputStream = null;
			if (inputImageCount >= FROM_MULTI_IMAGE_START) {
				if (isTotalVertical) {
					inputStream = assetManager.open(ASSET_SCHEDULE_TEMPLATE_VERTICAL + "/" + selectedJsonFile);
				} else {
					inputStream = assetManager.open(ASSET_SCHEDULE_TEMPLATE_HORIZONTAL + "/" + selectedJsonFile);
				}
			} else {
				inputStream = assetManager.open(ASSET_SCHEDULE_NO_MULTI_TEMPLATE + "/" + selectedJsonFile);
			}

			jsonObject = JsonReader.readJsonFile(mContext, inputStream);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// L.d("json : " + jsonObject);

		return jsonObject;
	}

	private int getVerticalImageCount(ArrayList<ImageData> imageDatas) {
		int verticalImageCount = 0;
		for (ImageData imageData : imageDatas) {
			if (imageData.isPotraitImage()) {
				verticalImageCount++;
			}
		}
		return verticalImageCount;
	}

	private UplusOutputData makeUplusOutputData(Theme theme) {
		UplusOutputData uplusOutputData = new UplusOutputData();

		L.d("selected Theme name : " + theme.name);

		mThemeType = theme.themeType;
		uplusOutputData.setTheme(theme);
		uplusOutputData.setFrames(getFrameData(theme));

		if (theme.resourceType == ResourceType.ASSET) {
			L.d("asset audio file name : " + theme.audioFileName);
			uplusOutputData.setAudioPath(theme.audioFileName);
			uplusOutputData.setResourceInAsset(true);
		} else {
			L.d("download audio file name : " + theme.audioFileName);
			String audioPath = theme.combineDowloadImageFilePath(mContext, theme.audioFileName);
			L.d("audio path : " + audioPath);
			uplusOutputData.setAudioPath(audioPath);
			uplusOutputData.setResourceInAsset(false);
		}

		return uplusOutputData;
	}

	private UplusOutputData makeUplusOutputData() {
		UplusOutputData uplusOutputData = new UplusOutputData();

		Theme theme = randomThemeList();
		mTheme = theme;

		uplusOutputData.setTheme(theme);
		mThemeType = theme.themeType;

		L.d("selected Theme name : " + theme.name + ", type : " + theme.themeType + ", filter id : " + theme.filterId);
		uplusOutputData.setFrames(getFrameData(theme));

		if (theme.resourceType == ResourceType.ASSET) {
			L.d("asset audio file name : " + theme.audioFileName);
			uplusOutputData.setAudioPath(theme.audioFileName);
			uplusOutputData.setResourceInAsset(true);
		} else {
			L.d("download audio file name : " + theme.audioFileName);
			String audioPath = theme.combineDowloadImageFilePath(mContext, theme.audioFileName);
			L.d("audio path : " + audioPath);
			uplusOutputData.setAudioPath(audioPath);
			uplusOutputData.setResourceInAsset(false);
		}
		return uplusOutputData;
	}

	private List<Frame> getFrameData(Theme theme) {
		List<Frame> frames = new ArrayList<Theme.Frame>();
		String objectPath = null;
		if (theme.frameData != null) {
			L.d("theme : " + theme.name + ", frame data size : " + theme.frameData.size());
			for (Frame frameData : theme.frameData) {

				if (!(theme.name.equals(Theme.THEME_NAME_DAILY) && frameData.frameCount > 1)) {

					L.d("Frame type : " + frameData.frameType + ", count : " + frameData.frameCount);
					if (frameData.frameImageName != null) {
						L.d("frame.frameImageName : " + frameData.frameImageName);
					}
					if (frameData.objects != null) {
						for (FrameObject frameObject : frameData.objects) {
							objectPath = theme.combineDowloadImageFilePath(mContext, frameObject.imageName, "png");
							L.d("object name : " + objectPath + ", x :" + frameObject.coordinate.x + ", y : " + frameObject.coordinate.y + ", motionType : " + frameObject.motion.type);
							objectPath = null;
						}
					}
					frames.add(frameData);
				}
			}
		} else {
			frames = null;
		}
		return frames;
	}

	private Theme randomThemeList() {
		Random random = new Random();
		ArrayList<Theme> themes = ThemeManager.getInstance(mContext).getAvailableThemeListForMaking();
		Theme theme = themes.get(random.nextInt(themes.size())); 
		if(theme == null){
			L.d("theme is null"); 
		}else{
			L.d("theme name : " + theme.name); 
		}
		return theme; 
	}

	/**
	 * path에 따른 jsonObject를 반환.
	 * 
	 * @param theme
	 *            : 현재 설정되어 있는 테마.
	 * @param path
	 *            : json 파일 path.
	 * @return : jsonObject.
	 */
	private JSONObject getJsonObject(Theme theme, String path) {

		AssetManager assetManager = mContext.getAssets();
		InputStream inputStream = null;
		JSONObject jsonObject = null;
		
		if(path == null){
			return null; 
		}
		
		try {
			inputStream = assetManager.open(path);
			jsonObject = JsonReader.readJsonFile(mContext, inputStream);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return jsonObject;
	}
}