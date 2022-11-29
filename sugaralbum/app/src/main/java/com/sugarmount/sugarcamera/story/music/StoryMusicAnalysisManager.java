package com.sugarmount.sugarcamera.story.music;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;

import com.kiwiple.debug.L;
import com.kiwiple.imageframework.util.SmartLog;
import com.kiwiple.multimedia.canvas.BurstShotScene;
import com.kiwiple.multimedia.canvas.CollectionScene;
import com.kiwiple.multimedia.canvas.MultiLayerScene;
import com.kiwiple.multimedia.canvas.OverlayTransition;
import com.kiwiple.multimedia.canvas.ScaleEffect;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.Transition;
import com.kiwiple.multimedia.canvas.VideoFileScene;
import com.kiwiple.multimedia.canvas.VideoFileScene.Editor;
import com.kiwiple.multimedia.canvas.Visualizer;
import com.kiwiple.multimedia.preview.PreviewManager;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.tag.UserTag;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.ResourceType;
import com.kiwiple.scheduler.util.JsonReader;
import com.sugarmount.sugarcamera.story.service.StoryAnalysisMusicForegroundService;
import com.sugarmount.sugarcamera.story.utils.StoryUtils;
import com.sugarmount.sugarcamera.story.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class StoryMusicAnalysisManager {

	private static StoryMusicAnalysisManager sInstance = null;
	private Context mContext;

	public final static String ASSET_ANALYSIS_DEFAULT_DIRECTORY = "audio/analysis";
	private final int DEFAULT_COUNT = 1;
	public final static int DEFAULT_AUTO_ANALYSIS_COUNT = 50;
	private int mAnalysisDoneCount = DEFAULT_COUNT;

	public static final float LIMIT_EXTERNAL_AUDIO_DURATION_SEC = 5 * 60; // 5분
	public static final long MS = 1000L;
	public static final float LIMIT_EXTERNAL_AUDIO_DURATION_MS = LIMIT_EXTERNAL_AUDIO_DURATION_SEC * MS; // 300 000ms

	public static final long LIMIT_SLOW_VIDEO_DURATION_MIN = 6 * MS;
	public static final long LIMIT_SLOW_VIDEO_DURATION_MAX = 10 * MS;
	public static final int LIMIT_AVERAGE_TIMESTAMP = 3;
	private static final float SLOW_MOTION_DURATION = 4.0f;
	private static final int SLOW_MOTION_VELOCITY = 2;
	private static final float SLOW_MOTION_DURATION_MS = SLOW_MOTION_DURATION * MS;

	public StoryMusicAnalysisManager(Context context) {
		mContext = context.getApplicationContext();
	}

	public static synchronized StoryMusicAnalysisManager getInstance(Context context) {
		if (sInstance == null) {
			sInstance = new StoryMusicAnalysisManager(context.getApplicationContext());
		}
		return sInstance;
	}


	// 무비다이어리 생성시(자동, 수동, 테마 변경/ 랜덤 테마 변경) 분석된 디폴트 음원을 가지고 스케쥴링
	public JSONObject getPostCorrectionDurationForTransition(Theme mTheme) {

		JSONObject analysisDataObject = null;
		try {
			String title;
			if(mTheme.resourceType != ResourceType.ASSET){
				String audioPath = mTheme.combineDowloadImageFilePath(mContext, mTheme.audioFileName);
				title = Utils.getAudioName(audioPath);
			}else{
				title = Utils.getAssetAudioName(mTheme.audioFileName, mContext);
			}
			L.i("title : "+ title +", resType : "+ mTheme.resourceType);
			Cursor cursor = StoryMusicAnalysisHelper.getInstance(mContext).getExistAudioFileInDBWithTitle(title);
			if (cursor != null && cursor.getCount() > 0) {
				cursor.moveToFirst();
				String analysisData = cursor.getString(cursor.getColumnIndexOrThrow(StoryMusicAnalysisConstants.MusicDatabaseField.MUSIC_ANALYSIS_DATA));
				int audioDuration = cursor.getInt(cursor.getColumnIndexOrThrow(StoryMusicAnalysisConstants.MusicDatabaseField.MUSIC_DURAION));

				analysisDataObject = new JSONObject(analysisData);
				analysisDataObject = makePostCorrectionDurationForTransition(analysisDataObject.getJSONArray("scenes"), mTheme, audioDuration);

				L.i("title = "+ title +", audioDuration = " + audioDuration + "\nanalysisDataObject = " + analysisDataObject);
			}else{
				setDefaultSlowMotionRatio();
			}

			if (cursor != null) {
				cursor.close();
				cursor = null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return analysisDataObject;
	}

	private JSONObject makePostCorrectionDurationForTransition(JSONArray analysisDataArray, Theme mTheme, int audioDuration) throws JSONException {
		JSONArray sceneDurationArray = new JSONArray();
		JSONArray transitionDurationArray = new JSONArray();
		JSONObject durationsObject = new JSONObject();

		JSONObject transitionDurationObject = getJsonObject(mTheme, mTheme.transitionJsonName);
		JSONArray jsonTransitionArray = transitionDurationObject.getJSONArray("transitions");

		List<Scene> sceneList = PreviewManager.getInstance(mContext).getVisualizer().getRegion().getScenes();
		List<Transition> transitionList = PreviewManager.getInstance(mContext).getVisualizer().getRegion().getTransitions();

		int tempTransitionIndex = -1;
		final int OVERLAY_TRANSITION_DURATION = 350;

//		int themeTransitionIndex = 0;
		int themeTransitionDuration = 0;

		int headIndex = 0;
		int tailIndex = 0;
		int oldIndex = 0;

		int validNextIntervalTime = 0;

		int sceneDuration = 0;
		int dynamicSlowMotionPosition = 0;
		final int FIRST_SCENE_INDEX = 0;
		
		int currentPickPosition;
		int nextPickPosition = 0;
		int oldPickPosition = 0;
		boolean isOverFlowIndexOfImage = false;
		boolean isOverFlowIndexOfExceptScene = false;
//		boolean isDynamicSlowMotionEnable = false;

		ArrayList<Integer> analysisDataList = new ArrayList<Integer>();

//		float averageInterval = getAverageInterval(analysisDataArray);
		for (int i = 0; i < analysisDataArray.length(); i++) {
			analysisDataList.add((int) (analysisDataArray.getJSONObject(i).getDouble(StoryMusicAnalysisConstants.DURATION) * MS));
			L.i("duration = " + analysisDataList.get(i));
		}
		int maxAnalysisDataIndex = analysisDataList.size() - 1;
		int abnormalPickPosition = 0;

		for (int sceneIndex = 0; sceneIndex < sceneList.size(); sceneIndex++) {
			Scene scene = sceneList.get(sceneIndex);
			tailIndex = headIndex + 1;

			/**
			 * Mixup 테마의 경우 Ending 씬 추가로 인해서 기존 Theme가 가지는 Transition list가 아닌, Region 객체가 가지는 실제 적용된 Transition type, duration을 가지고 적용 또한 싱글, 멀티레이어 씬과 Overlay 타입만 적용
			 */
			Transition transition = null;

			boolean isAcceleratationActiveScene = UserTag.isMaintainFeature(scene.getTagContainer(), UplusOutputData.SCENE_TYPE_ACCELERATION);
			boolean isScaleEffectScene = UserTag.isMaintainFeature(scene.getTagContainer(), ScaleEffect.JSON_VALUE_TYPE);
			boolean isVideoMultiScene = false;
			boolean isDynamicIntroScene = StoryUtils.isValidDynamicIntroType(scene, mTheme);
			if (scene instanceof MultiLayerScene) {
			    for (Scene layer : ((MultiLayerScene)scene).getLayers()) {
			        if (layer instanceof VideoFileScene) {
			            isVideoMultiScene = true;
			            break;
			        }
			    }
			}

			L.i("isAcceleratationActiveScene = "+ isAcceleratationActiveScene);
			if ((!isAcceleratationActiveScene && StoryUtils.isValidContentImageFileScene(scene)) || (scene instanceof MultiLayerScene && !isVideoMultiScene) || (!isScaleEffectScene && scene instanceof MultiLayerScene)) {

				if (tempTransitionIndex < 0) {
					transition = transitionList.get(sceneIndex);
					if (transition instanceof OverlayTransition) {
						themeTransitionDuration = OVERLAY_TRANSITION_DURATION;
					} else {
						themeTransitionDuration = transition.getDuration();
					}
				} else {
					transition = transitionList.get(tempTransitionIndex++);
				}

			} else if (scene instanceof CollectionScene) {
				tempTransitionIndex = sceneIndex;

			} else {
				// imageText, dummy, drawable
				if (tempTransitionIndex < 0) {
					if (sceneIndex == transitionList.size()) {
						continue;
					} else {
						if (sceneIndex == 0) {
							themeTransitionDuration = transitionList.get(sceneIndex).getDuration();
						} else {
							transition = transitionList.get(sceneIndex);
						}
					}
				} else {
					transition = transitionList.get(tempTransitionIndex);
				}

				if (transition != null) {
					L.d("transition = " + transition);
					if (transition instanceof OverlayTransition) {
						themeTransitionDuration = OVERLAY_TRANSITION_DURATION;
					} else {
						themeTransitionDuration = transition.getDuration();
					}
				}
			}

			if (transition != null) {
				L.i("themeTransitionDuration = " + themeTransitionDuration + ", scene = " + scene + ", transition type : " + transition);
			} else {
				L.i("themeTransitionDuration = " + themeTransitionDuration + ", scene = " + scene);
			}

			if (scene instanceof VideoFileScene || scene instanceof BurstShotScene || isAcceleratationActiveScene || isVideoMultiScene || isScaleEffectScene || isDynamicIntroScene) {
				long exceptSceneDuration = 0;

				if(scene instanceof VideoFileScene){
					//slow motion적용 시 ex) N(6~10)초 비디오중 앞에 N/2 기준으로 1.7초 구간 3배 느리게 재생.. video 전체 duration은 N초 + 1.7*2초
					VideoFileScene videoScene = ((VideoFileScene)scene);
					boolean isSlowMotionEnable = videoScene.getSlowMotionEnable();
					int videoDuration = videoScene.getDuration();
					boolean isFastMode = videoScene.getFastPlayMode();
					//이미 slowmotion이 적용되었을 경우 추가된 1.7*2초를 제거한 duration이 실제  video duration
					//20151127 : 0.8초 * 1
					//20151203 : 3~4 초 구간(1초)를 2배 빠르게로 변경
					if(isSlowMotionEnable){
						if(isFastMode){
							videoDuration += SLOW_MOTION_DURATION_MS / SLOW_MOTION_VELOCITY  ;
						}else{
							videoDuration -= SLOW_MOTION_DURATION_MS * (SLOW_MOTION_VELOCITY - 1);
						}
					}
					
					L.i("sceneIndex : "+sceneIndex +", isSlowMotionEnable : "+ isSlowMotionEnable +", videoDuration : "+ videoDuration);
					//슬로우 모션 조건에 부합 유무 체크 
					//1203 : timeStamp 조건 제거 
					if((videoDuration >= LIMIT_SLOW_VIDEO_DURATION_MIN && videoDuration <= LIMIT_SLOW_VIDEO_DURATION_MAX)){
						//1203 : 음원 N/2기점으로 timestamp 계산 부분 제거 
//						isDynamicSlowMotionEnable = true;
						setSlowMotionDuration(scene, SLOW_MOTION_VELOCITY);
						setSlowMotionConversionPosition(scene, (int)(3 * MS));
					}else{
						setDefaultSlowMotionRatio();
					}
					
					exceptSceneDuration = videoScene.getDuration();
					
				}else if (isVideoMultiScene) {
				    for (Scene layer : ((MultiLayerScene)scene).getLayers()) {
				        if (layer.isInstanceOf(VideoFileScene.class)) {
							exceptSceneDuration = ((VideoFileScene)layer).getDuration();
				            SmartLog.e("sceneDuration", "sceneDuration : " + exceptSceneDuration);
				        }
				    }
				}else{
					exceptSceneDuration = scene.getDuration();
				}
				
				L.i("headIndex = " + headIndex + ", tailIndex = " + tailIndex + ", oldIndex = " + oldIndex + ", exceptSceneDuration = " + exceptSceneDuration);

				int loopSumDuration;

				while (true) {
					if (tailIndex > maxAnalysisDataIndex) {
						L.d("exceptScene index overflow....initialize index");
						isOverFlowIndexOfExceptScene = true;
						tailIndex = 0;

						//슬로우 모션 적용할 비디오 duration 계산 시 audio duration을 초과했을 경우 처리
						//1203 : 음원 N/2기점으로 timestamp 계산 부분 제거 
//						if(isDynamicSlowMotionEnable){
//							int tmpHeadIndex = headIndex;
//							// 비디오 중간 지점을 기점으로 timestamp 발생 구간 계산 후 slowmotion position 적용
//							while(true){
//								int halfVideoDuration = (int) ((exceptSceneDuration - SLOW_MOTION_DURATION_MS * (SLOW_MOTION_VELOCITY - 1)) / 2);
//								dynamicSlowMotionPosition = (int) (oldPickPosition + halfVideoDuration);
//	
//								if(tmpHeadIndex > maxAnalysisDataIndex){
//									int startVideoPosition = audioDuration - oldPickPosition;
//									tmpHeadIndex = 0;
//									while(true){
//										int timestamp = startVideoPosition + analysisDataList.get(tmpHeadIndex);
//										if(timestamp > halfVideoDuration){
//											isDynamicSlowMotionEnable = false;
//											setSlowMotionConversionPosition(scene, timestamp);
//											break;
//										}
//										tmpHeadIndex++;
//									}
//									if(!isDynamicSlowMotionEnable)
//										break;
//									
//								}else{
//									if(analysisDataList.get(tmpHeadIndex) > dynamicSlowMotionPosition){
//										int conversionPosition = halfVideoDuration + analysisDataList.get(tmpHeadIndex) - dynamicSlowMotionPosition;
//										isDynamicSlowMotionEnable = false;
//										setSlowMotionConversionPosition(scene, conversionPosition);
//										break;
//									}
//									tmpHeadIndex++;
//									continue;
//								}
//							}
//						}
						
						
						sceneDuration = (int) (exceptSceneDuration + analysisDataList.get(oldIndex));
						loopSumDuration = audioDuration - analysisDataList.get(oldIndex);
						L.i("headIndex = " + headIndex + ", tailIndex = " + tailIndex + ", sceneDuration = " + sceneDuration);

						int sumDuration;
						while (true) {
							sumDuration = loopSumDuration + analysisDataList.get(tailIndex);
							if (sumDuration > exceptSceneDuration) {
								break;
							}
							tailIndex++;
							if (tailIndex > maxAnalysisDataIndex)
								tailIndex = 0;
						}

						L.d("sumDuration = " + sumDuration + ", exceptSceneDuration = " + exceptSceneDuration + ", headIndex = " + headIndex + ", tailIndex = " + tailIndex);

						abnormalPickPosition = sceneDuration - themeTransitionDuration;

						headIndex = tailIndex;

						sceneDurationArray.put(new JSONObject().put("duration", exceptSceneDuration));
						transitionDurationArray.put(new JSONObject().put("duration", themeTransitionDuration));
						L.i("timestamp position = " + analysisDataList.get(headIndex) + ", scene duration = " + exceptSceneDuration + ", transition duration = " + themeTransitionDuration + ", next pick " + analysisDataList.get(tailIndex));
						L.i("current headIndex = " + headIndex + ", current  tailIndex = " + tailIndex + ", oldIndex = " + oldIndex);
						break;

					} else {
						isOverFlowIndexOfExceptScene = false;

						//첫번째 씬이 음원 분석 미적용 씬일경우 예외처리
						if(sceneIndex == FIRST_SCENE_INDEX){
							oldPickPosition = 0;
						}else{
							if (abnormalPickPosition != 0) {
								oldPickPosition = abnormalPickPosition;
							} else {
								oldPickPosition = analysisDataList.get(oldIndex);
							}
						}
						L.i("abnormalPickPosition = " + abnormalPickPosition + ", headIndex = " + headIndex + ", tailIndex = " + tailIndex);

						sceneDuration = (int) (oldPickPosition + exceptSceneDuration);
						 
						//슬로우 모션 duration 계산 시 audio duration 내에서 계산 할 경우 
						//1203 : 음원 N/2기점으로 timestamp 계산 부분 제거 
//						if(isDynamicSlowMotionEnable){
//							int halfVideoDuration = (int) ((exceptSceneDuration - SLOW_MOTION_DURATION_MS * (SLOW_MOTION_VELOCITY - 1)) / 2);
//							dynamicSlowMotionPosition = (int) (oldPickPosition + halfVideoDuration);
//
//							if(analysisDataList.get(headIndex) > dynamicSlowMotionPosition){
//								int conversionPosition = halfVideoDuration + analysisDataList.get(headIndex) - dynamicSlowMotionPosition;
//								isDynamicSlowMotionEnable = false;
//								L.i("slowmotion conversion position : "+ conversionPosition);
//								setSlowMotionConversionPosition(scene, conversionPosition);
//							}
//						}

						if (analysisDataList.get(headIndex) > sceneDuration && ((sceneIndex == FIRST_SCENE_INDEX) ? analysisDataList.get(headIndex) - sceneDuration > themeTransitionDuration : true)){
							abnormalPickPosition = sceneDuration - themeTransitionDuration;
							sceneDurationArray.put(new JSONObject().put("duration", exceptSceneDuration));
							transitionDurationArray.put(new JSONObject().put("duration", themeTransitionDuration));
							tailIndex = headIndex;
							L.d("timestamp position = " + analysisDataList.get(headIndex) + ", scene duration = " + exceptSceneDuration + ", transition duration = " + themeTransitionDuration);
							L.d("current headIndex = " + headIndex + ", current tailIndex = " + tailIndex + ", oldIndex = " + oldIndex);
							break;
						} else {
							headIndex++;
							tailIndex = headIndex + 1;
						}
					}
				}// while
			} else {

				if (scene instanceof CollectionScene)
					continue;

				while (true) {

					L.i("headIndex = " + headIndex + ", tailIndex = " + tailIndex + ", oldIndex = " + oldIndex + ", audioDuration  =" + audioDuration);

					if (tailIndex > maxAnalysisDataIndex) {
						L.d("scene index overflow....initialize index");
						isOverFlowIndexOfImage = true;
						tailIndex = 0;

						while (true) {
							if (audioDuration + analysisDataList.get(tailIndex) - analysisDataList.get(headIndex) > themeTransitionDuration) {
								nextPickPosition = audioDuration + analysisDataList.get(tailIndex);
								break;
							} else {
								tailIndex++;
							}
						}

					} else {
						isOverFlowIndexOfImage = false;
						nextPickPosition = analysisDataList.get(tailIndex);
					}

					if (abnormalPickPosition != 0) {
						oldPickPosition = abnormalPickPosition;
					} else {
						oldPickPosition = analysisDataList.get(oldIndex);
					}

					currentPickPosition = analysisDataList.get(headIndex);
					validNextIntervalTime = nextPickPosition - currentPickPosition;

					L.i("abnormalPickPosition = " + abnormalPickPosition + ", currentPickPosition = " + currentPickPosition + ",nextPickPosition = " + nextPickPosition + ", oldPickPosition = " + oldPickPosition);
					if (validNextIntervalTime > themeTransitionDuration) {
						abnormalPickPosition = 0;

						L.i("sceneIndex = " + sceneIndex + ", validNextIntervalTime = " + validNextIntervalTime + ", nextPickPosition = " + analysisDataList.get(tailIndex));
						if (sceneIndex == 0) {
							sceneDuration = currentPickPosition + themeTransitionDuration;
							oldIndex = headIndex;
						} else {
							if (isOverFlowIndexOfImage) {
								sceneDuration = currentPickPosition + themeTransitionDuration - oldPickPosition;
								oldIndex = headIndex;
								// 인덱스 초과로 첫번째 Pick time 사용했으니 다음 pick은 next
							} else {
								if (isOverFlowIndexOfExceptScene) {
									isOverFlowIndexOfExceptScene = false;
									sceneDuration = audioDuration - oldPickPosition + analysisDataList.get(headIndex) + themeTransitionDuration;
									oldIndex = headIndex;
								} else {
									// 다음 pickTime이 분석 정보의 처음으로 돌아갈 경우
									if (oldIndex > headIndex) {
										sceneDuration = audioDuration - oldPickPosition + currentPickPosition + themeTransitionDuration;
										oldIndex = headIndex;

									} else {
										sceneDuration = currentPickPosition - oldPickPosition + themeTransitionDuration;
										oldIndex = headIndex;
									}
								}
							}
						}

						sceneDurationArray.put(new JSONObject().put("duration", sceneDuration));
						transitionDurationArray.put(new JSONObject().put("duration", themeTransitionDuration));

						L.d("timeStamp position =" + analysisDataList.get(headIndex) + ", scene duration = " + (sceneDuration) + ", transition duration = " + themeTransitionDuration);
						L.d("current headIndex = " + headIndex + ", current tailIndex = " + tailIndex + ", oldIndex = " + oldIndex + ", next pick = " + analysisDataList.get(tailIndex));
						headIndex = tailIndex;
						break;
					} else {
						L.d("interval timeStamp is smaller than themeDuration.. ");
						tailIndex++;
						continue;
					}
				}
			}

		} // for

		durationsObject.put("scenes", sceneDurationArray);
		durationsObject.put("transitions", transitionDurationArray);
		return durationsObject;
	}

	//ex>비디오 기본 속도조절(1X) 설정
	public void setDefaultSlowMotionRatio(){
		List<Scene> sceneList = PreviewManager.getInstance(mContext).getVisualizer().getRegion().getScenes();

		PreviewManager pManager = PreviewManager.getInstance(mContext.getApplicationContext());
		Visualizer visualizer = pManager.getVisualizer();
		Visualizer.Editor visualizerEditor = null;
		

		for(Scene scene : sceneList){
			if(scene instanceof VideoFileScene){
				if (!visualizer.isOnEditMode()) {
					visualizerEditor = visualizer.getEditor().start();
				}
				VideoFileScene.Editor vEditor =  (Editor) scene.getEditor();
				VideoFileScene vScene = (VideoFileScene)scene;
				
				long startPos = vScene.getVideoStartPosition();
				long endPos = vScene.getVideoEndPosition();
				vEditor.setSlowMotionRatio(VideoFileScene.DEFAULT_SLOW_MOTION_RATIO);
				vEditor.setSlowMotionVelocity(1);
				vEditor.setDuration((int)(endPos - startPos));
				
				if(visualizerEditor != null){
					visualizerEditor.finish();
				}
			}
		}
	}
	
	private void setSlowMotionDuration(Scene scene, int velocity){
		PreviewManager pManager = PreviewManager.getInstance(mContext.getApplicationContext());
		Visualizer visualizer = pManager.getVisualizer();
		Visualizer.Editor visualizerEditor = null;
		
		if (!visualizer.isOnEditMode()) {
			visualizerEditor = visualizer.getEditor().start();
		}
		VideoFileScene.Editor vEditor = (Editor) scene.getEditor();
		VideoFileScene vScene = (VideoFileScene)scene;

		long startPos = vScene.getVideoStartPosition();
		long endPos = vScene.getVideoEndPosition();

		final int videoDuration = (int)(endPos - startPos);
		vEditor.setFastPlayMode(true);
		vEditor.setSlowMotionVelocity(velocity);
		vEditor.setSlowMotionRatio(SLOW_MOTION_DURATION);
		
		//20151117 : ex> 슬로우 모션 구간 1.7초 / x3 슬로우 모션 적용 / 전체 5.1초 / 추가된 시간 3.4초 
		//20151127 : 구간 0.8초 / x2 슬로우 모션 적용 / 전체 1.6초 / 추가된 시간 0.8초 
		//20151203 : 구간 1초, x2 Fast 모드, 전체 duration은 N - 0.5초
//		if(true){
//			// slow mode
//			vEditor.setFastPlayMode(false);
//			vEditor.setDurationForSlowMotion((int) (videoDuration + SLOW_MOTION_DURATION_MS * (SLOW_MOTION_VELOCITY - 1)));
//		}else{
			// fast mode  mode
			vEditor.setDurationForSlowMotion((int) (videoDuration - SLOW_MOTION_DURATION_MS / SLOW_MOTION_VELOCITY ));
//		}
		
		if(visualizerEditor != null){
			visualizerEditor.finish();
		}
	}
	
	public void setSlowMotionConversionPosition(Scene scene, int conversionPosition) {
		PreviewManager pManager = PreviewManager.getInstance(mContext.getApplicationContext());
		Visualizer visualizer = pManager.getVisualizer();
		Visualizer.Editor visualizerEditor = null;
		
		if (!visualizer.isOnEditMode()) {
			visualizerEditor = visualizer.getEditor().start();
		}
		VideoFileScene.Editor vEditor = (Editor) scene.getEditor();
		vEditor.setCoversionPosition(conversionPosition);
		
		if(visualizerEditor != null){
			visualizerEditor.finish();
		}
	}

	private JSONObject getJsonObject(Theme theme, String path) {

		AssetManager assetManager = mContext.getAssets();
		InputStream inputStream = null;
		JSONObject jsonObject = null;

		try {
			inputStream = assetManager.open(path);
			jsonObject = JsonReader.readJsonFile(mContext, inputStream);

		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return jsonObject;
	}


	public boolean isAnalyzingMusicTask() {
		return StoryAnalysisMusicForegroundService.getAnalysisMusicServiceStatus();
	}



}
