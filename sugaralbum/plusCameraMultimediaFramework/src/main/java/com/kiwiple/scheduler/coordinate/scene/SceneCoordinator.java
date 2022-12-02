package com.kiwiple.scheduler.coordinate.scene;

import java.util.ArrayList;

import org.json.JSONException;

import com.kiwiple.debug.L;
import com.kiwiple.multimedia.canvas.ScaleEffect;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.StepAppearEffect;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.scheduler.data.MultiLayerData;
import com.kiwiple.scheduler.data.SelectedOutputData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.tag.UserTag;

public class SceneCoordinator {

	protected void setTag(Scene scene, SelectedOutputData selectedOutputData, String tagSceneType) {
		JsonObject tagJsonObject = scene.getTagContainer();
		
		ArrayList<String> maintainFeatureList = new ArrayList<String>();
		if(selectedOutputData != null){
			if (selectedOutputData.getStepAppearEffect()) {
				maintainFeatureList.add(StepAppearEffect.JSON_VALUE_TYPE);
			}
			if (selectedOutputData.getMultiFilter()) {
				maintainFeatureList.add(UplusOutputData.SCENE_TYPE_MULTI_FILTER);
			}
			if (selectedOutputData.getScaleEffect() && MultiLayerData.canApplyScaleEffect(selectedOutputData.getFrameId())) {
				maintainFeatureList.add(ScaleEffect.JSON_VALUE_TYPE);
			}
			if (selectedOutputData.getAccelerationZoom()) {
				maintainFeatureList.add(UplusOutputData.SCENE_TYPE_ACCELERATION);
			}
		}
		
		if (maintainFeatureList != null && !maintainFeatureList.isEmpty()) {
			UserTag.setMaintainFeature(tagJsonObject, maintainFeatureList);
		}
		
		UserTag.setTagSceneType(tagJsonObject, tagSceneType);

	}

}
