package com.kiwiple.scheduler.select.uplus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;

import com.kiwiple.debug.L;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.multimedia.ResourceType;
import com.kiwiple.multimedia.canvas.AnimationEffect;
import com.kiwiple.multimedia.canvas.BorderEffect;
import com.kiwiple.multimedia.canvas.BurstShotScene;
import com.kiwiple.multimedia.canvas.CircleTransition;
import com.kiwiple.multimedia.canvas.CollageScene;
import com.kiwiple.multimedia.canvas.CollectionScene;
import com.kiwiple.multimedia.canvas.CoverTransition;
import com.kiwiple.multimedia.canvas.DrawableScene;
import com.kiwiple.multimedia.canvas.DummyScene;
import com.kiwiple.multimedia.canvas.DynamicTextureEffect;
import com.kiwiple.multimedia.canvas.Effect;
import com.kiwiple.multimedia.canvas.EnterEffect;
import com.kiwiple.multimedia.canvas.EnterTransition;
import com.kiwiple.multimedia.canvas.ExtendBoxTransition;
import com.kiwiple.multimedia.canvas.FadeTransition;
import com.kiwiple.multimedia.canvas.FlashTransition;
import com.kiwiple.multimedia.canvas.FogEffect;
import com.kiwiple.multimedia.canvas.GrandUnionTransition;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.NoiseEffect;
import com.kiwiple.multimedia.canvas.OverlayEffect;
import com.kiwiple.multimedia.canvas.SwayEffect;
import com.kiwiple.multimedia.canvas.ImageFileScene.Editor;
import com.kiwiple.multimedia.canvas.Transition.SceneOrder;
import com.kiwiple.multimedia.canvas.ImageTextScene;
import com.kiwiple.multimedia.canvas.KenBurnsScaler;
import com.kiwiple.multimedia.canvas.LayerScene;
import com.kiwiple.multimedia.canvas.LightEffect;
import com.kiwiple.multimedia.canvas.MetroTransition;
import com.kiwiple.multimedia.canvas.MultiLayerScene;
import com.kiwiple.multimedia.canvas.OverlayTransition;
import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.multimedia.canvas.ScaleEffect;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.SpinTransition;
import com.kiwiple.multimedia.canvas.SplitTransition;
import com.kiwiple.multimedia.canvas.StepAppearEffect;
import com.kiwiple.multimedia.canvas.TextEffect;
import com.kiwiple.multimedia.canvas.Transition;
import com.kiwiple.multimedia.canvas.VideoFileScene;
import com.kiwiple.multimedia.canvas.Visualizer;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.json.JsonUtils;
import com.kiwiple.multimedia.math.interpolator.InterpolatorType;
import com.kiwiple.scheduler.SchedulerEnvironment;
import com.kiwiple.scheduler.SchedulerVersion;
import com.kiwiple.scheduler.analysis.uplus.UplusImageAnalysis;
import com.kiwiple.scheduler.analysis.uplus.UplusVideoAnalysis;
import com.kiwiple.scheduler.coordinate.effect.uplus.UplusEffectApplyManager;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusBurstShotSceneCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusCollageSceneCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusCollectionSceneCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusDrawableSceneCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusDummySceneCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusEndingLogoSceneCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusImageFileSceneCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusImageTextSceneCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusIntroSceneCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusMultiLayerSceneCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusOutroSceneCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusVideoFileSceneCoordinator;
import com.kiwiple.scheduler.coordinate.transition.uplus.UplusTransitionApplyManager;
import com.kiwiple.scheduler.data.EffectData;
import com.kiwiple.scheduler.data.MultiLayerData;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.data.SelectedOutputData;
import com.kiwiple.scheduler.data.TransitionData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.data.uplus.effect.UplusBorderEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusDynamicTextureEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusEnterEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusFogEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusLightEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusScaleEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusStepAppearEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusTextEffectData;
import com.kiwiple.scheduler.data.uplus.transition.UplusCircleTransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusCoverTransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusCutTransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusEnterTransition;
import com.kiwiple.scheduler.data.uplus.transition.UplusExtendBoxTransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusFadeTransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusFlashTransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusGrandUnionTransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusMetroTransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusOverlayTransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusSpinTransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusSplitTransitionData;
import com.kiwiple.scheduler.select.OutputDataManager;
import com.kiwiple.scheduler.tag.UserTag;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.Frame;
import com.kiwiple.scheduler.theme.Theme.FrameType;
import com.kiwiple.scheduler.theme.ThemeVersion;
import com.kiwiple.scheduler.util.DateUtil;
import com.kiwiple.scheduler.util.EffectUtil;
import com.kiwiple.scheduler.util.ImageUtil;
import com.kiwiple.scheduler.util.IntroOutroUtils;
import com.kiwiple.scheduler.util.TransitionUtil;

/**
 * Uplus 출력 데이터 매니저 객체.
 * 
 */
public class UplusOutputDataManager extends OutputDataManager {

	private List<String> mDurationSet;
	private Context mContext;
	private UplusOutputData mUplusOutputData;

	// outputData에 effect를 추가하기 위해서 coordinator를 만든다.
	private UplusImageFileSceneCoordinator mUplusImageFileSceneCoordinator;
	private UplusCollageSceneCoordinator mUplusCollageSceneCoordinator;
	private UplusMultiLayerSceneCoordinator mUplusMultiLayerSceneCoordinator;
	private UplusVideoFileSceneCoordinator mUplusVideoFileSceneCoordinator;
	private UplusImageTextSceneCoordinator mUplusImageTextSceneCoordinator;
	private UplusDrawableSceneCoordinator mUplusDrawableSceneCoordinator;
	private UplusDummySceneCoordinator mUplusDummySceneCoordinator;
	private UplusTransitionApplyManager mUplusTransitionApplyManager;
	private UplusBurstShotSceneCoordinator mUplusBurstSceneApplyManager;

	private UplusIntroSceneCoordinator mUplusIntroSceneCoordinator;
	private UplusOutroSceneCoordinator mUplusOutroSceneCoordinator;
	private UplusEndingLogoSceneCoordinator mUplusEndingLogoSceneCoordinator;

	private UplusCollectionSceneCoordinator mUplusCollectionSceneCoordinator;
	private String mTitle;

	public UplusOutputDataManager(Context context, OutputData outputData) {
		super(context, outputData);
		mContext = context;
		mUplusOutputData = (UplusOutputData) outputData;
		mUplusImageFileSceneCoordinator = new UplusImageFileSceneCoordinator(context, outputData);
		mUplusCollageSceneCoordinator = new UplusCollageSceneCoordinator(context, outputData);
		mUplusMultiLayerSceneCoordinator = new UplusMultiLayerSceneCoordinator(context, outputData);
		mUplusVideoFileSceneCoordinator = new UplusVideoFileSceneCoordinator(context, outputData);
		mUplusImageTextSceneCoordinator = new UplusImageTextSceneCoordinator(context, outputData);
		mUplusDrawableSceneCoordinator = new UplusDrawableSceneCoordinator();
		mUplusTransitionApplyManager = new UplusTransitionApplyManager(context);
		mUplusDummySceneCoordinator = new UplusDummySceneCoordinator(context, outputData);
		mUplusBurstSceneApplyManager = new UplusBurstShotSceneCoordinator(context, outputData);
		mUplusCollectionSceneCoordinator = new UplusCollectionSceneCoordinator(context, outputData);

		mUplusIntroSceneCoordinator = new UplusIntroSceneCoordinator(context, outputData);
		mUplusOutroSceneCoordinator = new UplusOutroSceneCoordinator(context, outputData);
		mUplusEndingLogoSceneCoordinator = new UplusEndingLogoSceneCoordinator(context, outputData);
	}

	/**
	 * output data의 title을 설정한다.
	 * 
	 * @param title
	 *            : output data의 title.
	 */
	public void setOutputDataTitle(String title) {
		mTitle = title;
	}

	public String getOutputDataSchedulerVersion() {
		String version = SchedulerEnvironment.VERSION.toString();
		return version;
	}

	/**
	 * 음원 분석 정보를 scene duration에 적용 <br>
	 * 
	 * @param jsonObject
	 *            음원 분석 정보(scene/ transition duration)
	 * @return
	 */
	public void setOutputDataAnalysisScene(JSONObject analysisDataObject) {

		if (analysisDataObject == null)
			return;

		Visualizer.Editor vEditor = null;
		if (!mVisualizer.isOnEditMode()) {
			vEditor = mVisualizer.getEditor().start();
		}

		L.w("transition : " + analysisDataObject.isNull("transitions"));
		L.w("scenes :   " + analysisDataObject.isNull("scenes"));
		L.w("defaultscene : " + analysisDataObject.isNull("defaultscene"));
		int loop;
		try {

			if (!analysisDataObject.isNull("scenes")) {

				JSONArray sceneDurationArray = analysisDataObject.getJSONArray("scenes");

				List<Scene> sceneList = mVisualizer.getRegion().getScenes();
				L.i("sceneList size : " + sceneList.size() + ", sceneDurationArray size : " + sceneDurationArray.length());
				JSONArray expandableSceneDurationArray = null;

				if (sceneList.size() > sceneDurationArray.length()) {
					loop = sceneList.size() / sceneDurationArray.length() + 1;
					expandableSceneDurationArray = new JSONArray();
					for (int i = 0; i < loop; i++) {
						for (int index = 0; index < sceneDurationArray.length(); index++) {
							try {
								expandableSceneDurationArray.put(sceneDurationArray.get(index));
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				}

				for (int index = 0; index < sceneList.size(); index++) {
					try {
						int duration;

						if (expandableSceneDurationArray == null) {
							duration = sceneDurationArray.getJSONObject(index).getInt("duration");
						} else {
							duration = expandableSceneDurationArray.getJSONObject(index).getInt("duration");
						}

						Scene scene = sceneList.get(index);
						if (!(scene instanceof CollectionScene)) {
							scene.getEditor().setDuration(duration);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}

			if (!analysisDataObject.isNull("defaultscene")) {
				JSONArray defaultSceneDurationArray = analysisDataObject.getJSONArray("defaultscene");

				int contentDuration = defaultSceneDurationArray.getJSONObject(0).getInt("ContentDuration");
				int frontBackDuration = defaultSceneDurationArray.getJSONObject(1).getInt("FrontBackDuration");

				List<Scene> sceneList = mVisualizer.getRegion().getScenes();

				for (int index = 0; index < sceneList.size(); index++) {
					Scene scene = sceneList.get(index);
					if (scene instanceof ImageTextScene || scene instanceof DummyScene || scene instanceof DrawableScene) {
						scene.getEditor().setDuration(frontBackDuration);
						L.d("index : " + index + ", duration : " + frontBackDuration);
					} else if (!(scene instanceof CollectionScene)) {
						// 분석되지 않은 음원 적용 시 비디오 씬은 원래 duration 유지 되도록.
						if (scene instanceof VideoFileScene) {
							continue;
						}
						L.d("index : " + index + ", duration : " + frontBackDuration);
						scene.getEditor().setDuration(contentDuration);
					}
				}
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (vEditor != null) {
			vEditor.finish();
		}
	}

	/**
	 * 테마가 가지는 effect/ transition을 적용 <br>
	 * 
	 * @param jsonObject
	 *            현재 테마가 가지는 effect 정보
	 * @param jsonObject
	 *            현재 테마가 가지는 transition 정보
	 * @return
	 */
	public void setOutputDataEffectTransition(JSONObject effectObject, JSONObject transitionObject, boolean isChangeTheme, boolean isChangeOrder) {
		setOutputDataEffect(effectObject, isChangeTheme, isChangeOrder);
		setOutputDataTransition(transitionObject);
	}

	/**
	 * 음원 분석 정보를 transition duration 적용 <br>
	 * 
	 * @param jsonObject
	 *            음원 분석 정보(scene/ transition duration)
	 * @return
	 */

	public void setOutputDataAnalysisTransition(JSONObject analysisDataObject) {

		if (analysisDataObject == null)
			return;

		Visualizer.Editor vEditor = null;
		if (!mVisualizer.isOnEditMode()) {
			vEditor = mVisualizer.getEditor().start();
		}

		L.w("transition : " + analysisDataObject.isNull("transitions"));
		L.w("scenes :   " + analysisDataObject.isNull("scenes"));
		L.w("defaultscene : " + analysisDataObject.isNull("defaultscene"));
		int loop;
		try {
			if (!analysisDataObject.isNull("transitions")) {

				JSONArray transitionDurationArray = analysisDataObject.getJSONArray("transitions");

				List<Transition> transitionList = mVisualizer.getRegion().getTransitions();
				L.i("transition size : " + transitionList.size() + ", analysis transition size : " + transitionDurationArray.length());
				JSONArray expandlableTransitionDurationArray = null;

				if (transitionList.size() > transitionDurationArray.length()) {
					loop = transitionList.size() / transitionDurationArray.length() + 1;
					expandlableTransitionDurationArray = new JSONArray();
					for (int i = 0; i < loop; i++) {
						for (int index = 0; index < transitionDurationArray.length(); index++) {
							try {
								expandlableTransitionDurationArray.put(transitionDurationArray.get(index));
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				}

				for (int index = 0; index < transitionList.size(); index++) {
					try {
						int duration;
						if (expandlableTransitionDurationArray == null) {
							duration = transitionDurationArray.getJSONObject(index).getInt("duration");
						} else {
							duration = expandlableTransitionDurationArray.getJSONObject(index).getInt("duration");
						}

						Transition transition = transitionList.get(index);
						transition.getEditor().setDuration(duration);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (vEditor != null) {
			vEditor.finish();
		}
	}

	/**
	 * 음원 분석 정보를 통해 EnterEffect(==OverlayTransition) duration 적용 <br>
	 * 
	 * @param jsonObject
	 *            음원 분석 정보
	 * @return
	 */

	public void setOutputDataAnalysisEffect(JSONObject analysisDataObject) {

		if (analysisDataObject == null)
			return;

		Visualizer.Editor vEditor = null;
		if (!mVisualizer.isOnEditMode()) {
			vEditor = mVisualizer.getEditor().start();
		}

		int loop;
		try {
			if (!analysisDataObject.isNull("transitions")) {

				JSONArray transitionDurationArray = analysisDataObject.getJSONArray("transitions");

				List<Transition> transitionList = mVisualizer.getRegion().getTransitions();
				List<Scene> sceneList = mVisualizer.getRegion().getScenes();

				L.i("transition size : " + transitionList.size() + ", transitionDurationArray size : " + transitionDurationArray.length());
				JSONArray expandlableTransitionDurationArray = null;

				if (transitionList.size() > transitionDurationArray.length()) {
					loop = transitionList.size() / transitionDurationArray.length() + 1;
					expandlableTransitionDurationArray = new JSONArray();
					for (int i = 0; i < loop; i++) {
						for (int index = 0; index < transitionDurationArray.length(); index++) {
							try {
								expandlableTransitionDurationArray.put(transitionDurationArray.get(index));
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					}
				}

				ArrayList<List<com.kiwiple.multimedia.canvas.Effect>> effectList = new ArrayList<List<com.kiwiple.multimedia.canvas.Effect>>();

				for (int index = 0; index < transitionList.size(); index++) {
					try {
						int duration;
						if (expandlableTransitionDurationArray == null) {
							duration = transitionDurationArray.getJSONObject(index).getInt("duration");
						} else {
							duration = expandlableTransitionDurationArray.getJSONObject(index).getInt("duration");
						}

						Transition transition = transitionList.get(index);

						if (transition instanceof OverlayTransition) {
							JSONObject transitionObject = ((OverlayTransition) transition).toJsonObject();
							if (!transitionObject.isNull("scene_order")) {
								String sceneOrder = (String) transitionObject.get("scene_order");
								int effectIndex;
								if (sceneOrder.equalsIgnoreCase("latter")) {
									effectIndex = index + 1;
								} else {
									effectIndex = index;
								}
								Scene scene = sceneList.get(effectIndex);

								effectList.clear();
								if (scene instanceof ImageFileScene || scene instanceof VideoFileScene) {
									effectList.add(scene.getEffects());
								} else if (scene instanceof MultiLayerScene) {
									MultiLayerScene multiLayerScene = (MultiLayerScene) scene;
									for (Scene layerScene : multiLayerScene.getLayers()) {
										if (layerScene.getClass().equals(LayerScene.class)) {
											LayerScene multiLayerImageScene = (LayerScene) layerScene;
											effectList.add(multiLayerImageScene.getEffects());
										}
									}
								}
							}
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				effectList.clear();
				effectList = null;
			}
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (vEditor != null) {
			vEditor.finish();
		}
	}

	@Override
	public synchronized void setOutputDataTransition(JSONObject transitionJsonObject) {

		Visualizer.Editor vEditor = null;
		if (!mVisualizer.isOnEditMode()) {
			vEditor = mVisualizer.getEditor().start();
		}
		Region.Editor regionEditor = mVisualizer.getRegion().getEditor();
		String type = null;
		int duration = 0;
		int direction = 0;
		int line_color = 0;
		Theme theme = mUplusOutputData.getTheme();
		int totalValidSceneCount = 0;

		try {
			JSONArray jsonTransitionArray;
			totalValidSceneCount = getValidSceneCount();
			L.i("validSceneCount = " + totalValidSceneCount);

			if (totalValidSceneCount == 0) {
				totalValidSceneCount = mVisualizer.getRegion().getScenes().size();
			}

			if (totalValidSceneCount > UplusImageAnalysis.MAXIMUM_IMAGE_SCENE_COUNT) {
				JSONArray jsonTransitionTempArray = transitionJsonObject.getJSONArray("transitions");
				jsonTransitionArray = new JSONArray();

				for (int loop = 0; loop < UplusImageAnalysis.MAXIMUM_LIMITATION_LOOP_COUNT; loop++) {
					for (int cnt = 0; cnt < jsonTransitionTempArray.length(); cnt++) {
						jsonTransitionArray.put(jsonTransitionTempArray.getJSONObject(cnt));
					}
				}
			} else {
				jsonTransitionArray = transitionJsonObject.getJSONArray("transitions");
			}

			List<Scene> scenes = mVisualizer.getRegion().getScenes();

			TransitionData transitionData = null;
			L.i("transition size : " + jsonTransitionArray.length() + ", scenes.size() : " + scenes.size());
			int jsonIndex = 0;  
			int jsonArrayLength = jsonTransitionArray.length(); 
			for (int i = 0; i < scenes.size() - 1; i++) {

				Scene frontScene = scenes.get(i);
				Scene backScene = scenes.get(i + 1);
				JSONObject jsonObject = jsonTransitionArray.getJSONObject(jsonIndex);

				if (!jsonObject.isNull("multi_scene") && backScene.getClass().equals(MultiLayerScene.class)) {
					jsonObject = jsonTransitionArray.getJSONObject(jsonIndex).getJSONObject("multi_scene");
				}
				
				jsonIndex++; 
				if(jsonArrayLength == jsonIndex){
					jsonIndex = 0; 
				}

				if (i != 0 && (backScene.getClass().equals(CollectionScene.class) || UserTag.isExtraScene(backScene))) {
					// if next scene is collection, outro and ending scene, apply fade transition.
					jsonObject = getFadeTransition();
				}

				if (jsonObject.getString(Transition.JSON_NAME_TYPE).equals(EnterTransition.JSON_VALUE_TYPE)) {
					//overlay transition can not apply without enter effect.
					//So, change to fade transition. 
					jsonObject = changeEnterTransitionForSpecialEffect(frontScene, jsonObject, backScene);
					if (jsonObject.getString(Transition.JSON_NAME_TYPE).equals(EnterTransition.JSON_VALUE_TYPE)) {
						jsonObject = changeEnterTransitionForComplexMultiScene(frontScene, jsonObject, backScene);
					}
				}
				
				if(jsonObject.getString(Transition.JSON_NAME_TYPE).equals(CircleTransition.JSON_VALUE_TYPE)){
					//circle transition can not apply with step appear effect.
					//So, change to fade transition.
					jsonObject = changeCircleTransitionForStepAppearEffect(jsonObject, backScene); 
				}


				type = jsonObject.getString(Transition.JSON_NAME_TYPE);
				duration = jsonObject.getInt(Transition.JSON_NAME_DURATION);

				if (!type.contains("_transition")) {
					type = type + "_transition";
				}
				if (type.equals(FadeTransition.JSON_VALUE_TYPE)) {
					transitionData = new UplusFadeTransitionData(duration, type);
				} else if (type.equals(SplitTransition.JSON_VALUE_TYPE)) {
					L.d("transitio json : " + jsonObject);
					String splitDirection = jsonObject.getString(SplitTransition.JSON_NAME_DIRECTION);
					line_color = jsonObject.getInt(SplitTransition.JSON_NAME_LINE_COLOR);

					if (!jsonObject.isNull(SplitTransition.JSON_NAME_WHITE_LINE_SPLIT)) {
						boolean isWhiteLineSplit = jsonObject.getBoolean(SplitTransition.JSON_NAME_WHITE_LINE_SPLIT);
						transitionData = new UplusSplitTransitionData(duration, type, splitDirection, line_color, isWhiteLineSplit);
					} else {
						transitionData = new UplusSplitTransitionData(duration, type, splitDirection, line_color);
					}
				} else if (type.equals(SpinTransition.JSON_VALUE_TYPE)) {
					ArrayList<String> spinSceneOrderList = new ArrayList<String>();
					JSONArray spinOrderArray = jsonObject.getJSONArray(SpinTransition.JSON_NAME_SPIN_ORDER);
					for (int j = 0; j < spinOrderArray.length(); j++) {
						spinSceneOrderList.add(spinOrderArray.getString(j));
					}
					String spinDirection = jsonObject.getString(SpinTransition.JSON_NAME_DIRECTION);
					boolean overshot = false;
					if (!jsonObject.isNull(SpinTransition.JSON_NAME_USE_OVERSHOOT)) {
						overshot = jsonObject.getBoolean(SpinTransition.JSON_NAME_USE_OVERSHOOT);
					}
					boolean blurredBorder = false;
					if (!jsonObject.isNull(SpinTransition.JSON_NAME_USE_BLURRED_BORDER)) {
						blurredBorder = jsonObject.getBoolean(SpinTransition.JSON_NAME_USE_BLURRED_BORDER);
					}
					String interpolator = InterpolatorType.EXPONENTIAL_IN_OUT.toString(); 
					if (!jsonObject.isNull(SpinTransition.JSON_NAME_INTERPOLATOR_TYPE)) {
						interpolator = jsonObject.getString(SpinTransition.JSON_NAME_INTERPOLATOR_TYPE);
					}
					transitionData = new UplusSpinTransitionData(duration, type, spinSceneOrderList, overshot, spinDirection, blurredBorder, interpolator);
				} else if (type.equals(CoverTransition.JSON_VALUE_TYPE)) {
					String coverDirection = jsonObject.getString(CoverTransition.JSON_NAME_DIRECTION);
					transitionData = new UplusCoverTransitionData(mContext, duration, type, coverDirection, theme);

				} else if (type.equals(GrandUnionTransition.JSON_VALUE_TYPE)) {
					// float blockInterval =
					// jsonObject.getLong(GrandUnionTransition.JSON_NAME_BLOCK_INTERVAL);
					// int lineWidth = jsonObject.getInt(GrandUnionTransition.JSON_NAME_LINE_WIDTH);
					// int lineColor = jsonObject.getInt(GrandUnionTransition.JSON_NAME_LINE_COLOR);
					// boolean useFadeIn =
					// jsonObject.getBoolean(GrandUnionTransition.JSON_NAME_USE_FADE_IN);
					// transitionData = new UplusGrandUnionTransitionData(blockInterval, lineWidth,
					// lineColor, useFadeIn);

					// 1030 : GrandunionTransition의 비어캔 프리셋 사용
					transitionData = new UplusGrandUnionTransitionData(duration, type);
					int lineColor = 0xffff9d26; 
					if(!jsonObject.isNull(GrandUnionTransition.JSON_NAME_LINE_COLOR)){
						lineColor = jsonObject.getInt(GrandUnionTransition.JSON_NAME_LINE_COLOR); 
					}
					((UplusGrandUnionTransitionData)transitionData).setLineColor(lineColor); 

				} else if (type.equals(ExtendBoxTransition.JSON_VALUE_TYPE)) {
					boolean useFadeIn = jsonObject.getBoolean(ExtendBoxTransition.JSON_NAME_USE_FADE_IN);
					float tickness = jsonObject.getLong(ExtendBoxTransition.JSON_NAME_BOX_THICKNESS);
					float interval = jsonObject.getLong(ExtendBoxTransition.JSON_NAME_INTERVAL);
					int boxColor = jsonObject.getInt(ExtendBoxTransition.JSON_NAME_BOX_COLOR);
					String style = jsonObject.getString(UplusExtendBoxTransitionData.JSON_NAME_STYLE);
					duration = jsonObject.getInt(ExtendBoxTransition.JSON_NAME_DURATION);
					transitionData = new UplusExtendBoxTransitionData(boxColor, interval, tickness, useFadeIn, duration, style);

				} else if (type.equals(FlashTransition.JSON_VALUE_TYPE)) {
					transitionData = new UplusFlashTransitionData(duration, type);
				} else if (type.equals(UplusCutTransitionData.JSON_VALUE_CUT_TRANSITION_TYPE)) {
					transitionData = new UplusCutTransitionData(duration, type);
				} else if (type.equals(MetroTransition.JSON_VALUE_TYPE)) {
					String sDirection = jsonObject.getString(MetroTransition.JSON_NAME_DIRECTION);
					String sSliceOrder = jsonObject.getString(MetroTransition.JSON_NAME_SLICE_ORDER);
					int sliceCount = jsonObject.getInt(MetroTransition.JSON_NAME_SLICE_COUNT);
					line_color = jsonObject.getInt(MetroTransition.JSON_NAME_LINE_COLOR);
					transitionData = new UplusMetroTransitionData(duration, type, sliceCount, sSliceOrder, sDirection, line_color);
				} else if (type.equals(OverlayTransition.JSON_VALUE_TYPE)) {
					String order = jsonObject.getString(OverlayTransition.JSON_NAME_FRONT_SCENE);
					transitionData = new UplusOverlayTransitionData(duration, type, order);
				} else if (type.equals(CircleTransition.JSON_VALUE_TYPE)) {
					boolean zoomin = jsonObject.getBoolean(CircleTransition.JSON_NAME_CIRCLE_DIRECTION);
					transitionData = new UplusCircleTransitionData(duration, type, zoomin);
				}else if(type.equals(EnterTransition.JSON_VALUE_TYPE)){
					transitionData = new UplusEnterTransition(jsonObject, duration, frontScene, backScene); 
				}

				mUplusTransitionApplyManager.applyTransition(regionEditor, i, transitionData, type);
				L.d("i : " + i + ", type : " + type + ", duration : " + duration);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (vEditor != null) {
			vEditor.finish();
		}
	}

	private JSONObject changeCircleTransitionForStepAppearEffect(JSONObject jsonObject, Scene backScene) throws JSONException{
		if(EffectUtil.hasStepAppearEffect(backScene)){
			return getFadeTransition(); 
		}else{
			return jsonObject; 
		}
	}

	private JSONObject changeEnterTransitionForComplexMultiScene(Scene frontScene, JSONObject jsonObject, Scene backScene) throws JSONException {
		boolean frontComplexMultiScene = EffectUtil.hasComplexMultiScene(frontScene);
		boolean backComplexMultiScene = EffectUtil.hasComplexMultiScene(backScene);

		boolean reverse = jsonObject.getBoolean(EnterTransition.JSON_NAME_IS_REVERSE); 

		if (reverse) {
			if (frontComplexMultiScene) {
				return getFadeTransition();
			} else {
				return jsonObject;
			}
		} else {
			if (backComplexMultiScene) {
				return getFadeTransition();
			} else {
				return jsonObject;
			}
		}

	}

	private JSONObject changeEnterTransitionForSpecialEffect(Scene frontScene, JSONObject enterTransitionObject, Scene backScene) throws JSONException {

		boolean frontScaleEffect = EffectUtil.hasScaleEffect(frontScene);
		boolean frontStepApperEffect = EffectUtil.hasStepAppearEffect(frontScene);

		boolean backScaleEffect = EffectUtil.hasScaleEffect(backScene);
		boolean backStepApperEffect = EffectUtil.hasStepAppearEffect(backScene);

		L.d("front has scale effect : " + frontScaleEffect + ", step effect : " + frontStepApperEffect);
		L.d("back has scale effect : " + backScaleEffect + ", step effect : " + backStepApperEffect);

		boolean reverse = enterTransitionObject.getBoolean(EnterTransition.JSON_NAME_IS_REVERSE);

		if (reverse) {
			if (frontScaleEffect || backStepApperEffect) {
				return getFadeTransition();
			}
		} else {
			if (backStepApperEffect) {
				return getFadeTransition();
			}
		}
		return enterTransitionObject;
	}

	private JSONObject getNullTransition() throws JSONException {
		JSONObject cutTransitionObject = new JSONObject();
		cutTransitionObject.put(Transition.JSON_NAME_TYPE, UplusCutTransitionData.JSON_VALUE_CUT_TRANSITION_TYPE);
		cutTransitionObject.put(Transition.JSON_NAME_DURATION, 1500);
		return cutTransitionObject;
	}

	private JSONObject getFadeTransition() throws JSONException {
		JSONObject fadeTransitionObject = new JSONObject();
		fadeTransitionObject.put(Transition.JSON_NAME_TYPE, FadeTransition.JSON_VALUE_TYPE);
		fadeTransitionObject.put(Transition.JSON_NAME_DURATION, 1500);
		return fadeTransitionObject;
	}

	public synchronized void setOutputDataMakeCacheData(boolean bMakeCache) {
		Visualizer.Editor vEditor = null;
		if (!mVisualizer.isOnEditMode()) {
			vEditor = mVisualizer.getEditor().start();
			vEditor.setPreviewMode(bMakeCache);
		} else {
			mVisualizer.getEditor().setPreviewMode(bMakeCache);
		}

		if (vEditor != null) {
			vEditor.finish();
		}
	}

	@Override
	public synchronized void setOutputDataEffect(JSONObject jsonEffectObject, boolean isChangeTheme, boolean isChangeOrder) {

		Theme oldTheme = mUplusOutputData.getOldTheme();
		Theme theme = mUplusOutputData.getTheme();
		boolean isNewMake = false;

		if (oldTheme == null && !isChangeTheme && !isChangeOrder) {
			isNewMake = true;
		}

		Visualizer.Editor vEditor = null;
		if (!mVisualizer.isOnEditMode()) {
			vEditor = mVisualizer.getEditor().start();
		}

		int i = 0;
		int countStepAppearEffect = 0;
		int countScaleEffect = 0;
		try {

			JSONArray jsonEffectArray = jsonEffectObject.getJSONArray(Scene.JSON_NAME_EFFECTS);
			List<Scene> scenes = mVisualizer.getRegion().getScenes();

			boolean applyStepAppearEffect = EffectUtil.useStepAppearEffect(jsonEffectObject);
			boolean applyScaleEffect = EffectUtil.useScaleEffect(jsonEffectObject);
			int jsonEffectLength = jsonEffectArray.length(); 

			for (Scene scene : scenes) {

				JSONObject effectTotalObject = jsonEffectArray.getJSONObject(i++);
				if(i == jsonEffectLength){
					i = 0; 
				}
				
				JSONArray effectsArray = effectTotalObject.getJSONArray(UplusEnterEffectData.JSON_VALUE_EFFECT_ARRAY);
				JSONArray multiEffectsArray = effectTotalObject.getJSONArray(UplusEnterEffectData.JSON_VALUE_MULTI_EFFECTS_ARRAY);

				if (!UserTag.getTagSceneType(scene).equals(OutputData.TAG_JSON_VALE_SCENE_CONTENT)) {
					// only apply effect on tag content scene.
					if (UserTag.getTagSceneType(scene).equals(OutputData.TAG_JSON_VALE_SCENE_UNKNOWN)) {
						// if(scene.getClass().equals(ImageTextScene.class) ||
						// scene.getClass().equals(DummyScene.class)){
						if (UserTag.isExtraScene(scene)) {
							// check old extra scene type.
							continue;
						}
					} else {
						continue;
					}
				}

				// remove effects
				scene.getEditor().removeAllEffects(BorderEffect.class);
				scene.getEditor().removeAllEffects(LightEffect.class);
				scene.getEditor().removeAllEffects(FogEffect.class);
				scene.getEditor().removeAllEffects(EnterEffect.class);
				scene.getEditor().removeAllEffects(StepAppearEffect.class);
				scene.getEditor().removeAllEffects(ScaleEffect.class);
				scene.getEditor().removeAllEffects(SwayEffect.class);
				scene.getEditor().removeAllEffects(NoiseEffect.class);
				scene.getEditor().removeAllEffects(TextEffect.class);
				scene.getEditor().removeAllEffects(DynamicTextureEffect.class);

				if (scene.getClass().equals(MultiLayerScene.class)) {
					MultiLayerScene multiLayerScene = (MultiLayerScene) scene;
					for (Scene layerScene : multiLayerScene.getLayers()) {
						layerScene.getEditor().removeAllEffects(StepAppearEffect.class);
						layerScene.getEditor().removeAllEffects(EnterEffect.class);
						layerScene.getEditor().removeAllEffects(BorderEffect.class);
					}
				}

				int index = i - 1;

				boolean isStepAppear = UserTag.isMaintainFeature(scene.getTagContainer(), StepAppearEffect.JSON_VALUE_TYPE);
				boolean isScaleEffect = UserTag.isMaintainFeature(scene.getTagContainer(), ScaleEffect.JSON_VALUE_TYPE);
				L.d("index : " + (index) + ", stepApper : " + isStepAppear + ", scale effect : " + isScaleEffect);

				for (int j = 0; j < effectsArray.length(); j++) {
					JSONObject effectObject = effectsArray.getJSONObject(j);
					L.d(" single scene effect, index : " + j + ", type : " + effectObject.getString(Effect.JSON_NAME_TYPE) );
					if(effectObject != null){
						applyEffectObject(effectObject, scene);
					}
				}
				
				for (int j = 0; j < multiEffectsArray.length(); j++) {
					JSONObject effectObject = multiEffectsArray.getJSONObject(j);
					if(effectObject != null){
						applyEffectObject(effectObject, scene);
					}
				}

				if (isStepAppear && scene.getClass().equals(MultiLayerScene.class) && applyStepAppearEffect) {
					L.d("effect, index : " + index + ", " + StepAppearEffect.JSON_VALUE_TYPE);
					applyStepApperEffect(jsonEffectObject, scene);
					countStepAppearEffect++;
				}
				if (isScaleEffect && scene.getClass().equals(MultiLayerScene.class) && MultiLayerData.canApplyScaleEffect(((MultiLayerScene) scene).getTemplateId()) && applyScaleEffect) {
					L.d("effect, index : " + index + ", " + ScaleEffect.JSON_VALUE_TYPE);
					applyScaleEffect(jsonEffectObject, scene, theme);
					countScaleEffect++;
				}

				if (theme.name.equals(Theme.THEME_NAME_OLDMOVIE)) {
					L.d("effect, index : " + index + ", " + "old movie effect");
					applyOldMovieEffect(scene);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (isNewMake || (isChangeTheme && countStepAppearEffect == 0)) {
			setOutputDataExtraStepApperEffect(jsonEffectObject);
		}

		if (isNewMake || (isChangeTheme && countScaleEffect == 0)) {
			setOutputDataExtraScaleEffect(jsonEffectObject, theme);
		}

		if (vEditor != null) {
			vEditor.finish();
		}

	}

	private void applyLightEffectArrayObject(JSONArray lightEffectArray, Scene scene) throws JSONException {
		
		UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
		ArrayList<UplusLightEffectData> lightEffectDataList = new ArrayList<UplusLightEffectData>(); 
		for(int i = 0; i < lightEffectArray.length(); i++){
			JSONObject jsonObject = lightEffectArray.getJSONObject(i); 
			lightEffectDataList.add(getLightEffect(jsonObject)); 
		}
		effectApplyManager.applyLightEffect(scene, lightEffectDataList); 
	}

	private void applyEffectObject(JSONObject effectObject, Scene scene) throws JSONException {
		// apply effects
		String type = effectObject.getString(Effect.JSON_NAME_TYPE);
		
		if (type.equals(BorderEffect.JSON_VALUE_TYPE)) {
			applyBoaderEffect(effectObject, scene);
			
		}  else if (type.equals(FogEffect.JSON_VALUE_TYPE)) {
			applyFogEffect(effectObject, scene);
			
		} else if (type.equals(DynamicTextureEffect.JSON_VALUE_TYPE)) {
			applyDynamicTextureEffect(effectObject, scene);
			
		} else if (type.equals(TextEffect.JSON_VALUE_TYPE)) {
			applyTextffect(effectObject, scene);
			
		} else if (type.equals(EnterEffect.JSON_VALUE_TYPE) && !EffectUtil.hasComplexMultiScene(scene)) {
			applyEnterEffect(effectObject, scene);
			
		} else if(type.equals(UplusLightEffectData.JSON_VALUE_LIGHT_EFFECT_ARRAY)){
			JSONArray lightEffectArray = effectObject.getJSONArray(UplusLightEffectData.JSON_VALUE_LIGHT_EFFECT_ARRAY); 
			applyLightEffectArrayObject(lightEffectArray, scene); 
		}

	}

	private void applyScaleEffect(JSONObject effectObject, Scene scene, Theme theme) throws JSONException {
		UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
		UplusScaleEffectData scaleEffectData = getScaleEffectData(effectObject);
		if (scaleEffectData != null) {
			effectApplyManager.applyScaleEffect(scene, scaleEffectData, theme);
		}
	}

	private void applyEnterEffect(JSONObject effectObject, Scene scene) throws JSONException {

		UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
		Scene.Editor<?, ?> editor = scene.getEditor();

		String enterDirection = effectObject.getString(EnterEffect.JSON_NAME_DIRECTION);
		boolean enterReverse = effectObject.getBoolean(EnterEffect.JSON_NAME_IS_REVERSE);
		int enterDuretion = effectObject.getInt(EnterEffect.JSON_NAME_EFFECT_DURATION);

		EffectData enterEffectData = new UplusEnterEffectData(enterDirection, enterDuretion, enterReverse);
		EnterEffect.Editor enterEditor = null;
		if (enterEffectData != null) {
			if (!scene.getClass().equals(MultiLayerScene.class)) {
				enterEditor = editor.addEffect(EnterEffect.class).getEditor();
			}
			effectApplyManager.applyEnterEffect(enterEditor, (UplusEnterEffectData) enterEffectData, scene);
		}

	}

	private void applyOldMovieEffect(Scene scene) {
		UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
		effectApplyManager.applyOldMovieEffect(scene);
	}

	private void applyStepApperEffect(JSONObject effectObject, Scene scene) throws JSONException {
		UplusStepAppearEffectData stepAppearEffectData = getStepAppearEffectData(effectObject);
		UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
		if (stepAppearEffectData != null) {
			effectApplyManager.applyStepAppearEffect(scene, stepAppearEffectData);
		}

	}

	private void applyTextffect(JSONObject effectObject, Scene scene) throws JSONException {

		UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
		Scene.Editor<?, ?> editor = scene.getEditor();

		TextEffect.Editor textEditor = editor.addEffect(TextEffect.class).getEditor();
		effectApplyManager.applyTextEffect(textEditor, new UplusTextEffectData(TextEffect.JSON_VALUE_TYPE, effectObject));

	}

	private void applyDynamicTextureEffect(JSONObject effectObject, Scene scene) throws JSONException {

		UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
		Scene.Editor<?, ?> editor = scene.getEditor();

		DynamicTextureEffect.Editor dynamicEditor = editor.addEffect(DynamicTextureEffect.class).getEditor();
		effectApplyManager.applyDynamicTextureEffect(mContext, mUplusOutputData,dynamicEditor, new UplusDynamicTextureEffectData(DynamicTextureEffect.JSON_VALUE_TYPE, effectObject));

	}

	private void applyFogEffect(JSONObject effectObject, Scene scene) throws JSONException {

		FogEffect.Editor fogEditor = null;
		UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
		ArrayList<EffectData> fogEffects = new ArrayList<EffectData>();
		
		String fogType = effectObject.getString(FogEffect.JSON_NAME_EFFECT_TYPE);
		Scene.Editor<?, ?> editor = scene.getEditor();

		fogEffects.add(new UplusFogEffectData(fogType));

		if (fogEffects != null && !fogEffects.isEmpty()) {
			fogEditor = editor.addEffect(FogEffect.class).getEditor();
			effectApplyManager.applyFogEffect(fogEditor, (UplusFogEffectData) fogEffects.get(0));
		}
	}

	private void applyBoaderEffect(JSONObject effectObject, Scene scene) throws JSONException {
		UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
		int color = effectObject.getInt(BorderEffect.JSON_NAME_LINE_COLOR);
		int width = effectObject.getInt(BorderEffect.JSON_NAME_LINE_WIDTH);
		EffectData borderEffectData = new UplusBorderEffectData(BorderEffect.JSON_VALUE_TYPE, color, width);
		effectApplyManager.applyBorderEffect(scene, (UplusBorderEffectData) borderEffectData);
	}

	private void setOutputDataExtraScaleEffect(JSONObject jsonEffectObject, Theme theme) {

		UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
		Random random = new Random();
		try {
			UplusScaleEffectData scaleEffectData = getScaleEffectData(jsonEffectObject);
			if (scaleEffectData != null) {
				L.d("effect apply extra scale effect");
				ArrayList<MultiLayerScene> applySceneList = getMultiLayerSceneForScaleEffect();
				int maxCount = scaleEffectData.getMaxCount();

				if (applySceneList != null && !applySceneList.isEmpty()) {
					int applySceneCount = applySceneList.size();
					if (applySceneCount + 1 <= maxCount) {
						for (MultiLayerScene scene : applySceneList) {
							effectApplyManager.applyScaleEffect(scene, scaleEffectData, theme);
						}
					} else {

						for (int i = 0; i < maxCount; i++) {
							int randomNum = random.nextInt(applySceneList.size());
							MultiLayerScene multiScene = applySceneList.get(randomNum);
							effectApplyManager.applyScaleEffect(multiScene, scaleEffectData, theme);
							applySceneList.remove(randomNum);
						}
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ArrayList<MultiLayerScene> getMultiLayerSceneForScaleEffect() {
		ArrayList<MultiLayerScene> multiSceneList = new ArrayList<MultiLayerScene>();
		for (MultiLayerScene scene : getMultiLayerSceneForSpecialEffect()) {
			if (MultiLayerData.canApplyScaleEffect(scene.getTemplateId())) {
				multiSceneList.add(scene);
			}
		}
		return multiSceneList;
	}

	private UplusScaleEffectData getScaleEffectData(JSONObject jsonEffectObject) throws JSONException {
		if (!jsonEffectObject.isNull(EffectData.JSON_NAME_EXTRA_EFFECT)) {
			JSONObject extraEffectObject = jsonEffectObject.getJSONObject(EffectData.JSON_NAME_EXTRA_EFFECT);
			if (!extraEffectObject.isNull(ScaleEffect.JSON_VALUE_TYPE)) {
				JSONObject scaleEffectObject = extraEffectObject.getJSONObject(ScaleEffect.JSON_VALUE_TYPE);
				UplusScaleEffectData uplusScaleEffectData = new UplusScaleEffectData(ScaleEffect.JSON_VALUE_TYPE);
				uplusScaleEffectData.setMaxCount(scaleEffectObject.getInt(UplusScaleEffectData.JSON_NAME_MAX_COUNT));

				return uplusScaleEffectData;
			}
			return null;
		} else {
			return null;
		}
	}

	private int getValidSceneCount() {
		int totalValidSceneCount = 0;
		for (Scene scene : mVisualizer.getRegion().getScenes()) {
			if (scene.getClass().equals(ImageFileScene.class) || scene.getClass().equals(CollageScene.class) || scene.getClass().equals(MultiLayerScene.class) || scene.getClass().equals(BurstShotScene.class))
				totalValidSceneCount++;
		}
		return totalValidSceneCount;
	}

	private ArrayList<MultiLayerScene> getMultiLayerSceneForSpecialEffect() {
		// make multilayer list except step appear effect, scale effect, video file.
		ArrayList<MultiLayerScene> multiLayerSceneList = new ArrayList<MultiLayerScene>();

		for (Scene scene : mVisualizer.getRegion().getScenes()) {
			if (scene.getClass().equals(MultiLayerScene.class)) {
				if (!UserTag.isMaintainFeature(scene.getTagContainer(), StepAppearEffect.JSON_VALUE_TYPE) && !UserTag.isMaintainFeature(scene.getTagContainer(), ScaleEffect.JSON_VALUE_TYPE) && !isContainVideoFileScene(scene)) {
					multiLayerSceneList.add((MultiLayerScene) scene);
				}
			}
		}
		return multiLayerSceneList;
	}

	private boolean isContainVideoFileScene(Scene scene) {
		if (scene.getClass().equals(MultiLayerScene.class)) {
			for (Scene layerScene : ((MultiLayerScene) scene).getLayers()) {
				if (layerScene.getClass().equals(VideoFileScene.class)) {
					return true;
				}
			}
		} else {
			if (scene.getClass().equals(VideoFileScene.class)) {
				return true;
			}
		}
		return false;
	}

	public synchronized void setOutputDataExtraStepApperEffect(JSONObject jsonEffectObject) {

		Random random = new Random();
		UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
		try {
			UplusStepAppearEffectData stepAppearEffectData = getStepAppearEffectData(jsonEffectObject);
			if (stepAppearEffectData != null) {
				L.d("effect apply extra step appear effect");
				ArrayList<MultiLayerScene> applySceneList = null;
				String applySceneType = stepAppearEffectData.getApplySceneType();
				int maxCount = stepAppearEffectData.getMaxCount();
				if (applySceneType.equals(MultiLayerScene.JSON_VALUE_TYPE)) {
					applySceneList = getMultiLayerSceneForSpecialEffect();
				}

				if (applySceneList != null && !applySceneList.isEmpty()) {
					int applySceneCount = applySceneList.size();
					if (applySceneCount + 1 <= maxCount) {
						for (Scene scene : applySceneList) {
							effectApplyManager.applyStepAppearEffect(scene, (UplusStepAppearEffectData) stepAppearEffectData);

						}
					} else {

						for (int i = 0; i < maxCount; i++) {
							int randomNum = random.nextInt(applySceneList.size());
							MultiLayerScene multiScene = applySceneList.get(randomNum);
							effectApplyManager.applyStepAppearEffect(multiScene, (UplusStepAppearEffectData) stepAppearEffectData);
							applySceneList.remove(randomNum);
						}
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private UplusStepAppearEffectData getStepAppearEffectData(JSONObject jsonEffectObject) throws JSONException {
		if (!jsonEffectObject.isNull(EffectData.JSON_NAME_EXTRA_EFFECT)) {
			JSONObject extraEffectObject = jsonEffectObject.getJSONObject(EffectData.JSON_NAME_EXTRA_EFFECT);
			if (!extraEffectObject.isNull(StepAppearEffect.JSON_VALUE_TYPE)) {
				JSONObject stepAppearEffect = extraEffectObject.getJSONObject(StepAppearEffect.JSON_VALUE_TYPE);

				int stepAppearOrder[] = new int[4];
				stepAppearOrder[0] = stepAppearEffect.getInt(UplusStepAppearEffectData.JSON_NAME_APPEAR_ORDER_1);
				stepAppearOrder[1] = stepAppearEffect.getInt(UplusStepAppearEffectData.JSON_NAME_APPEAR_ORDER_2);
				stepAppearOrder[2] = stepAppearEffect.getInt(UplusStepAppearEffectData.JSON_NAME_APPEAR_ORDER_3);
				stepAppearOrder[3] = stepAppearEffect.getInt(UplusStepAppearEffectData.JSON_NAME_APPEAR_ORDER_4);
				float defaultRatio = (float) stepAppearEffect.getDouble(UplusStepAppearEffectData.JSON_NAME_DEFAULT_RATIO);
				float stepRatio = (float) stepAppearEffect.getDouble(UplusStepAppearEffectData.JSON_NAME_STEP_RATIO);
				int maxCount = (int) stepAppearEffect.getInt(UplusStepAppearEffectData.JSON_NAME_MAX_COUNT);
				String applySceneType = (String) stepAppearEffect.getString(UplusStepAppearEffectData.JSON_NAME_APPLY_SCENE);

				EffectData stepAppearEffectData = new UplusStepAppearEffectData(StepAppearEffect.JSON_VALUE_TYPE, stepAppearOrder, defaultRatio, stepRatio);
				stepAppearEffectData.setMaxCount(maxCount);
				stepAppearEffectData.setApplySceneType(applySceneType);

				return (UplusStepAppearEffectData) stepAppearEffectData;
			}
			return null;
		} else {
			return null;
		}
	}
	@Override
	public synchronized void setOutputDataScene() {

		// 각 coordinator에 선택된 이미지 add.

		Visualizer.Editor vEditor = null;
		if (!mVisualizer.isOnEditMode()) {
			vEditor = mVisualizer.getEditor().start();
		}

		Region region = mVisualizer.getRegion();
		Region.Editor regionEditor = region.getEditor();
		regionEditor.clear();
		mPreviewManager.setAudioPathAndResourceType(mOutputData.getAudioPath(), (mOutputData.isResourceInAsset() ? ResourceType.ANDROID_ASSET : ResourceType.FILE));

		if (mTitle == null) {
			mTitle = getTitleBaseByDate();
		}

		List<Integer> collectableSceneList = new ArrayList<Integer>();
		Theme theme = mUplusOutputData.getTheme();
		
		try {
			region.getTagContainer().put(ThemeVersion.JSON_KEY_THEME_VERSION, theme.version.toJsonObject());
			region.getTagContainer().put(SchedulerVersion.JSON_KEY_SCHEDULER_VERSION, SchedulerEnvironment.VERSION.toJsonObject()); 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		int sceneCount = 0;
		L.i("use collection scene = " + theme.isUseColloectionScene);
		for (int i = 0; i < mOutputData.getOutputDataList().size(); i++) {
			SelectedOutputData selectedOutputData = mOutputData.getOutputDataList().get(i);
			String sceneType = selectedOutputData.getSceneType();
			L.d("sceneType = " + sceneType + " frame id : " + selectedOutputData.getFrameId());

			if (sceneType.equals(ImageFileScene.JSON_VALUE_TYPE)) {
				ImageData imageData = selectedOutputData.getImageDatas().get(0);
				if (imageData != null) {
					L.d("imageData = " + imageData.id);
					mUplusImageFileSceneCoordinator.addImageFileScene(regionEditor, selectedOutputData);
					sceneCount = i + 1;
					if (theme.isUseColloectionScene && collectableSceneList.size() < UplusImageAnalysis.MAXIMUM_ENDING_SCENE_COUNT)
						collectableSceneList.add(sceneCount);
				}

			} else if (sceneType.equals(CollageScene.JSON_VALUE_TYPE)) {
				ArrayList<ImageData> collageDataList = selectedOutputData.getImageDatas();

				if (collageDataList != null) {
					for (ImageData imageData : collageDataList) {
						L.d("collageDataList = " + imageData.id);
					}
					sceneCount = i + 1;
					if (theme.isUseColloectionScene && collectableSceneList.size() < UplusImageAnalysis.MAXIMUM_ENDING_SCENE_COUNT)
						collectableSceneList.add(sceneCount);
					mUplusCollageSceneCoordinator.addCollageScene(regionEditor, selectedOutputData);
				}

			} else if (sceneType.equals(MultiLayerScene.JSON_VALUE_TYPE)) {

				int videoIndex = UplusVideoAnalysis.isContainVideoData(selectedOutputData.getImageDatas());
				if (theme.isUseColloectionScene && videoIndex == -1 && collectableSceneList.size() < UplusImageAnalysis.MAXIMUM_ENDING_SCENE_COUNT){
					sceneCount = i + 1;
					collectableSceneList.add(sceneCount);
				}
				mUplusMultiLayerSceneCoordinator.addMultiLayerScene(regionEditor, selectedOutputData);

			} else if (sceneType.equals(BurstShotScene.JSON_VALUE_TYPE)) {

				sceneCount = i + 1;
				// if(theme.isUseColloectionScene && collectableSceneList.size() <
				// UplusImageAnalysis.MAXIMUM_ENDING_SCENE_COUNT)
				// collectableSceneList.add(sceneCount);
				mUplusBurstSceneApplyManager.addBurstShotScene(regionEditor, selectedOutputData);

			} else if (sceneType.equals(VideoFileScene.JSON_VALUE_TYPE)) {
				L.d("videoData = " + selectedOutputData.getNameList().get(0));
				mUplusVideoFileSceneCoordinator.addVideoFileScene(regionEditor, selectedOutputData);
			}
		}

		// 맨 처음에 타이틀 삽입, 맨 마중에 로그 삽입.
		if (mOutputData.getOutputDataList().size() > 0) {
			addExtraScenes(region, theme, mTitle, collectableSceneList);
		}
		if (vEditor != null) {
			vEditor.finish();
		}
	}

	private void addExtraScenes(Region region, Theme theme, String title, List<Integer> collectableSceneList) {
		Region.Editor regionEditor = region.getEditor();

		mUplusIntroSceneCoordinator.addIntroScene(region, mTitle, theme);

		if (theme.isUseColloectionScene) {
			mUplusCollectionSceneCoordinator.addCollectionScene(regionEditor, collectableSceneList);
			for (Integer integer : collectableSceneList) {
				L.i("check collection scene index : " + integer);
			}

			if (collectableSceneList != null && collectableSceneList.size() > 0)
				L.i("check collection scene size : " + collectableSceneList.size());

			mUplusOutputData.getOutputDataList().add(new SelectedOutputData(CollectionScene.JSON_NAME_TYPE, -1, 10000));
		}

		mUplusOutroSceneCoordinator.addOutroScene(region, mTitle);
		// endinglogo delete
//		mUplusEndingLogoSceneCoordinator.addEndingLogoScene(regionEditor);

	}

	public void setOutputDataIntroOutroViewPort() {

		Visualizer.Editor vEditor = null;
		if (!mVisualizer.isOnEditMode()) {
			vEditor = mVisualizer.getEditor().start();
		}

		List<Scene> scenes = mVisualizer.getRegion().getScenes();
		if (scenes.size() <= 1) {
			return;
		}

		Theme theme = mUplusOutputData.getTheme();
		Frame introFrame = null;
		Frame outroFrame = null;

		for (Frame frame : theme.frameData) {
			if (FrameType.INTRO.equals(frame.frameType)) {
				introFrame = frame;
			}

			if (FrameType.OUTRO.equals(frame.frameType)) {
				outroFrame = frame;
			}
		}

		boolean useUserImage = theme.isIntroSceneWithUserImage();
//		boolean useDynamicOutroDefaultImage = theme.isDynamicOutroDefaultImage();
		boolean useDynamicOutroDefaultImage = true;

		Viewport[] firstViewPort = new Viewport[2];
		Viewport lastViewPort = null;
		boolean isDynamicIntro = theme.getDynamicIntroJson() != null ? true : false;  

		Scene firstContentScene = scenes.get(1);
		if (theme.hasIntro() && !useUserImage) {
			firstViewPort[0] = new Viewport(0, 0, 1, 1);
			firstViewPort[1] = new Viewport(0, 0, 1, 1);
		} else if (firstContentScene.getClass().equals(ImageFileScene.class)) {
			ImageFileScene.Editor sEditor = (Editor) firstContentScene.getEditor();
			KenBurnsScaler.Editor kenburnScaler = (KenBurnsScaler.Editor) sEditor.getObject().getScaler().getEditor();
			Viewport[] viewPortArray = kenburnScaler.getObject().getViewports();
			if (isDynamicIntro) {
				// TODO aubergine 20151104 : 현재는 dynamicIntroJsonName의 유무로만 체크하고 있지만, 차후 관리 필
				firstViewPort[0] = viewPortArray[1];
				firstViewPort[1] = viewPortArray[0];
			} else {
				firstViewPort[0] = viewPortArray[0];
				firstViewPort[1] = viewPortArray[0];
			}
		} else {
			ImageData imageData = IntroOutroUtils.getImageData(mContext, firstContentScene);
			firstViewPort[0] = IntroOutroUtils.getCenterFullViewPort(imageData.path, imageData.width, imageData.height);
			firstViewPort[1] = firstViewPort[0];
			if (isDynamicIntro) {
				// TODO aubergine 20151104 : 현재는 dynamicIntroJsonName의 유무로만 체크하고 있지만, 차후 관리 필요
				firstViewPort[1] = IntroOutroUtils.getZoomInViewPort(firstViewPort[0]);
			}
		}

		Scene lastContentScene = UserTag.getLastContentSceneExceptionCollectionScene(scenes);
		if (lastContentScene.getClass().equals(ImageFileScene.class)) {

			ImageFileScene.Editor sEditor = (Editor) lastContentScene.getEditor();
			KenBurnsScaler.Editor kenburnScaler = (KenBurnsScaler.Editor) sEditor.getObject().getScaler().getEditor();
			Viewport[] viewPortArray = kenburnScaler.getObject().getViewports();
			for (Viewport viewport : viewPortArray) {
				lastViewPort = viewport;
			}
		}

		Scene introScene = scenes.get(0);
		Scene outroScene = scenes.get(scenes.size() - 2);

		KenBurnsScaler.Editor scalerEditor = null;

		if (useUserImage && firstViewPort != null) {
			if (introScene.getClass().equals(ImageFileScene.class)) {
				scalerEditor = ((ImageFileScene.Editor) introScene.getEditor()).setScaler(KenBurnsScaler.class).getEditor();
				Viewport[] viewportArray = new Viewport[2];
				viewportArray = firstViewPort;
				scalerEditor.setViewports(viewportArray);
			} else if (introScene.getClass().equals(ImageTextScene.class)) {
				ImageTextScene firstImageTextScene = (ImageTextScene) introScene;
				ImageTextScene.Editor firstImageTextSceneEditor = firstImageTextScene.getEditor();
				firstImageTextSceneEditor.setBackgroundViewport(firstViewPort[0]);
			} else if (introScene.getClass().equals(DummyScene.class)) {
				DummyScene firstDummyScene = (DummyScene) introScene;
				DummyScene.Editor firstDummySceneEditor = firstDummyScene.getEditor();
				firstDummySceneEditor.setBackgroundViewport(firstViewPort[0]);
			}
			L.d("update intro view port 0: (" + firstViewPort[0].left + ", " + firstViewPort[0].top + ")(" + firstViewPort[0].right + ", " + firstViewPort[0].bottom + ")");
			L.d("update intro view port 1: (" + firstViewPort[1].left + ", " + firstViewPort[1].top + ")(" + firstViewPort[1].right + ", " + firstViewPort[1].bottom + ")");
		}

		if (useUserImage && lastViewPort != null && !useDynamicOutroDefaultImage) {
			if (outroScene.getClass().equals(ImageFileScene.class)) {
				scalerEditor = ((ImageFileScene.Editor) outroScene.getEditor()).setScaler(KenBurnsScaler.class).getEditor();
				Viewport[] viewportArray = new Viewport[2];
				viewportArray[0] = lastViewPort;
				viewportArray[1] = lastViewPort;
				scalerEditor.setViewports(viewportArray);
			} else if (outroScene.getClass().equals(ImageTextScene.class)) {
				ImageTextScene lastImageTextScene = (ImageTextScene) outroScene;
				ImageTextScene.Editor lastImageTextSceneEditor = lastImageTextScene.getEditor();
				lastImageTextSceneEditor.setBackgroundViewport(lastViewPort);
			} else if (outroScene.getClass().equals(DummyScene.class)) {
				DummyScene lastDummyScene = (DummyScene) outroScene;
				DummyScene.Editor lastDummySceneEditor = lastDummyScene.getEditor();
				lastDummySceneEditor.setBackgroundViewport(lastViewPort);
			}
			L.d("update outro view port : (" + lastViewPort.left + ", " + lastViewPort.top + ")(" + lastViewPort.right + ", " + lastViewPort.bottom + ")");
		}

		if (vEditor != null) {
			vEditor.finish();
		}
	}

	/**
	 * movie diary의 title을 데이터의 날짜 기준으로 설정한다.
	 * 
	 * @return : movie diary title
	 */
	private String getTitleBaseByDate() {
		String title = "";
		long date = mOutputData.getOutputDataList().get(0).getDate();
		String path = mOutputData.getOutputDataList().get(0).getImageDatas().get(0).path;
		if (date == 0) {
			title = DateUtil.getDayStringFromPath(mContext, path);
		} else {
			title = DateUtil.getDayStringFromDate(mContext, date);
		}
		mTitle = title;
		L.d("title : " + title);
		return title;
	}

	public String getTitle() {
		return mTitle;
	}
	
	/**
	 * json object에서 lighting effect data를 생성한다. <br>
	 * 
	 * @param jsonObject
	 * @return
	 */
	private UplusLightEffectData getLightEffect(JSONObject jsonObject) throws JSONException {
		String type = jsonObject.getString(LightEffect.JSON_NAME_DRAW_TYPE);
		String color = jsonObject.getString(LightEffect.JSON_NAME_COLOR);
		int scale = 0, startX = 0, startY = 0, endX = 0, endY = 0;
		int startAlpha = 0;
		int endAlpha = 100;
		String resolution = Resolution.NHD.name;

		if (!jsonObject.isNull(LightEffect.JSON_NAME_SCALE)) {
			scale = jsonObject.getInt(LightEffect.JSON_NAME_SCALE);
			startX = jsonObject.getInt(LightEffect.JSON_NAME_MOVE_FROM_X);
			startY = jsonObject.getInt(LightEffect.JSON_NAME_MOVE_FROM_Y);
			endX = jsonObject.getInt(LightEffect.JSON_NAME_MOVE_TO_X);
			endY = jsonObject.getInt(LightEffect.JSON_NAME_MOVE_TO_Y);
		} else {
			scale = LightEffect.SCALE_MIN + (int) (Math.random() * (LightEffect.SCALE_MAX - LightEffect.SCALE_MIN));
			startX = LightEffect.BOUND_MIN + (int) (Math.random() * (Resolution.NHD.width - LightEffect.BOUND_MIN));
			startY = LightEffect.BOUND_MIN + (int) (Math.random() * (Resolution.NHD.height - LightEffect.BOUND_MIN));
			endX = LightEffect.BOUND_MIN + (int) (Math.random() * (Resolution.NHD.width - LightEffect.BOUND_MIN));
			endY = LightEffect.BOUND_MIN + (int) (Math.random() * (Resolution.NHD.height - LightEffect.BOUND_MIN));
		}
		if (!jsonObject.isNull(LightEffect.JSON_VALUE_ALPHA_START)) {
			startAlpha = jsonObject.getInt(LightEffect.JSON_VALUE_ALPHA_START);
			endAlpha = jsonObject.getInt(LightEffect.JSON_VALUE_ALPHA_END);
		}

		if (!jsonObject.isNull(Resolution.DEFAULT_JSON_NAME))
			resolution = jsonObject.getString(Resolution.DEFAULT_JSON_NAME);
		
		return new UplusLightEffectData(type, color, scale, startX, startY, endX, endY, startAlpha, endAlpha, resolution);
	}

}
