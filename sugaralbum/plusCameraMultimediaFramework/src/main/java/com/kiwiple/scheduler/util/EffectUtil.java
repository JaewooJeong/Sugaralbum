package com.kiwiple.scheduler.util;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.multimedia.canvas.Effect;
import com.kiwiple.multimedia.canvas.EnterEffect;
import com.kiwiple.multimedia.canvas.MultiLayerScene;
import com.kiwiple.multimedia.canvas.ScaleEffect;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.StepAppearEffect;
import com.kiwiple.multimedia.canvas.VideoFileScene;
import com.kiwiple.scheduler.data.EffectData;
import com.kiwiple.scheduler.data.MultiLayerData;
import com.kiwiple.scheduler.data.uplus.UplusMultiLayerData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.tag.UserTag;

public class EffectUtil {

	public static boolean hasScaleEffect(Scene scene) {
		for (Effect effect : scene.getEffects()) {
			if (effect.getClass().equals(ScaleEffect.class)) {
				return true;
			}
		}

		if (scene.getClass().equals(MultiLayerScene.class)) {
			for (Scene layerScene : ((MultiLayerScene) scene).getLayers()) {
				for (Effect effect : layerScene.getEffects()) {
					if (effect.getClass().equals(ScaleEffect.class)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public static boolean hasStepAppearEffect(Scene scene) {

		for (Effect effect : scene.getEffects()) {
			if (effect.getClass().equals(StepAppearEffect.class)) {
				return true;
			}
		}

		if (scene.getClass().equals(MultiLayerScene.class)) {
			for (Scene layerScene : ((MultiLayerScene) scene).getLayers()) {
				for (Effect effect : layerScene.getEffects()) {
					if (effect.getClass().equals(StepAppearEffect.class)) {
						return true;
					}
				}
			}
		}

		return false;
	}

	public static boolean maintainMultiFilterScene(MultiLayerScene scene, ArrayList<ImageData> imageDataList, ArrayList<String> originalImagePathList) {

		boolean isOriginalMultiFilterScene = UserTag.isMaintainFeature(scene.getTagContainer(), UplusOutputData.SCENE_TYPE_MULTI_FILTER);
		boolean isMultiFilterScene = true;
		if (isOriginalMultiFilterScene) {
			for (int i = 0; i < imageDataList.size(); i++) {
				ImageData imageData = imageDataList.get(i);
				if (!imageData.path.equals(originalImagePathList.get(i))) {
					isMultiFilterScene = false;
					break;
				}
			}

			if (imageDataList.get(0).imageCorrectData.collageTempletId != UplusMultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID) {
				isMultiFilterScene = false;
			}

			return isMultiFilterScene;
		} else {
			return false;
		}

	}

	public static boolean maintainScaleEffectScene(MultiLayerScene scene, int newFrameId) {

		boolean isOriginalScaleEffect = UserTag.isMaintainFeature(scene.getTagContainer(), ScaleEffect.JSON_VALUE_TYPE);

		if (isOriginalScaleEffect) {
			if (MultiLayerData.canApplyScaleEffect(newFrameId)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public static boolean sceneHasEnterEffect(Scene scene, boolean reverse) {
		for (Effect effect : scene.getEffects()) {
			if (effect.getClass().equals(EnterEffect.class)) {
				EnterEffect enterEffect = (EnterEffect) effect;
				if (enterEffect.isReverse() == reverse) {
					return true;
				}
			}
		}

		if (scene.getClass().equals(MultiLayerScene.class)) {
			MultiLayerScene multiScene = (MultiLayerScene) scene;
			for (Scene layerScene : multiScene.getLayers()) {
				for (Effect effect : layerScene.getEffects()) {
					if (effect.getClass().equals(EnterEffect.class)) {
						EnterEffect enterEffect = (EnterEffect) effect;
						if (enterEffect.isReverse() == reverse) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	public static void removeEnterEffect(Scene scene) {
		for (Effect effect : scene.getEffects()) {
			if (effect.getClass().equals(EnterEffect.class)) {
				scene.getEditor().removeAllEffects(EnterEffect.class);
				break;
			}
		}

		if (scene.getClass().equals(MultiLayerScene.class)) {
			MultiLayerScene multiScene = (MultiLayerScene) scene;
			for (Scene layerScene : multiScene.getLayers()) {
				for (Effect effect : layerScene.getEffects()) {
					if (effect.getClass().equals(EnterEffect.class)) {
						layerScene.getEditor().removeAllEffects(EnterEffect.class);
						break;
					}
				}
			}
		}
	}

	public static boolean useStepAppearEffect(JSONObject effectJsonObject){
		if (!effectJsonObject.isNull(EffectData.JSON_NAME_EXTRA_EFFECT)) {
			JSONObject extraEffectObject = null;
			try {
				extraEffectObject = effectJsonObject.getJSONObject(EffectData.JSON_NAME_EXTRA_EFFECT);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (extraEffectObject != null && !extraEffectObject.isNull(StepAppearEffect.JSON_VALUE_TYPE)) {
				return true; 
			}else{
				return false; 
			}
		}else{
			return false;
		}
	}

	public static boolean useScaleEffect(JSONObject effectJsonObject) {
		if (!effectJsonObject.isNull(EffectData.JSON_NAME_EXTRA_EFFECT)) {
			JSONObject extraEffectObject = null;
			try {
				extraEffectObject = effectJsonObject.getJSONObject(EffectData.JSON_NAME_EXTRA_EFFECT);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (extraEffectObject != null && !extraEffectObject.isNull(ScaleEffect.JSON_VALUE_TYPE)) {
				return true; 
			}else{
				return false; 
			}
		}else{
			return false;
		}
	}

	public static boolean hasComplexMultiScene(Scene scene) {
		if(scene.getClass().equals(MultiLayerScene.class)){
			for(Scene layerScene : ((MultiLayerScene)scene).getLayers()){
				if(layerScene.getClass().equals(VideoFileScene.class)){
					return true; 
				}
			}
			return false; 
		}else{
			return false;
		}
	}
}
