package com.kiwiple.scheduler.tag;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import com.kiwiple.multimedia.canvas.CollectionScene;
import com.kiwiple.multimedia.canvas.DummyScene;
import com.kiwiple.multimedia.canvas.ImageTextScene;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.TextEffect;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.scheduler.SchedulerVersion;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusEndingLogoSceneCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusIntroSceneCoordinator;
import com.kiwiple.scheduler.coordinate.scene.uplus.UplusOutroSceneCoordinator;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.data.uplus.effect.UplusTextEffectData;

public class UserTag {

	public static boolean isMaintainFeature(JsonObject tagObject, String targetFeature) {

		try {
			if (tagObject != null) {
				if (!tagObject.isNull(UplusOutputData.JSON_NAME_MAINTAIN_FEATURE)) {
					JsonArray maintainArrayObject = tagObject.getJSONArray(UplusOutputData.JSON_NAME_MAINTAIN_FEATURE);
					for (int i = 0; i < maintainArrayObject.length(); i++) {
						String sourceFeature = maintainArrayObject.getString(i);
						if (sourceFeature.equals(targetFeature)) {
							return true;
						}
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}

	public static void setMaintainFeature(JsonObject tagJsonObject, String maintainFeature) {
		try {
			if (tagJsonObject != null) {

				if (tagJsonObject.isNull(UplusOutputData.JSON_NAME_MAINTAIN_FEATURE)) {
					JsonArray maintainFeatureJsonArray = new JsonArray();
					maintainFeatureJsonArray.put(maintainFeature);
					tagJsonObject.put(UplusOutputData.JSON_NAME_MAINTAIN_FEATURE, maintainFeatureJsonArray);
				} else {
					if(!isMaintainFeature(tagJsonObject, maintainFeature)){
						JsonArray maintainFeatureJsonArray = tagJsonObject.optJSONArray(UplusOutputData.JSON_NAME_MAINTAIN_FEATURE);
						maintainFeatureJsonArray.put(maintainFeature);	
					}
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setTagSceneType(JsonObject tagJsonObject, String tagSceneType) {
		try {
			if (tagJsonObject != null) {
				tagJsonObject.put(OutputData.TAG_JSON_KEY_SCENE_TYPE, tagSceneType);
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static Scene getLastContentSceneExceptionCollectionScene(List<Scene> scenes){
		
		int size = scenes.size(); 
		for(int i = size - 1; i >= 0; i--){
			Scene scene = scenes.get(i); 
			if(!isExtraScene(scene) && !scene.getClass().equals(CollectionScene.class)){
				return scene; 
			}
		}
		
		return null;
	}

	public static boolean isExtraScene(Scene scene) {
		String tagType = getTagSceneType(scene);
		if (tagType.equals(UplusIntroSceneCoordinator.TAG_JSON_VALE_SCENE_INTRO) || tagType.equals(UplusOutroSceneCoordinator.TAG_JSON_VALE_SCENE_OUTRO) || tagType.equals(UplusEndingLogoSceneCoordinator.TAG_JSON_VALE_SCENE_ENDING)) {
			return true;
		} else {
			if (tagType.equals(OutputData.TAG_JSON_VALE_SCENE_UNKNOWN)) {
				if (scene.getClass().equals(ImageTextScene.class) || scene.getClass().equals(DummyScene.class)) {
					return true;
				}
			}
		}
		return false;
	}

	public static String getTagSceneType(Scene scene) {
		try {
			JsonObject tagJsonOject = scene.getTagContainer();
			if (tagJsonOject != null) {
				if (!tagJsonOject.isNull(OutputData.TAG_JSON_KEY_SCENE_TYPE)) {
					return tagJsonOject.getString(OutputData.TAG_JSON_KEY_SCENE_TYPE);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return OutputData.TAG_JSON_VALE_SCENE_UNKNOWN;
	}

	public static void setMaintainFeature(JsonObject tagJsonObject, ArrayList<String> maintainFeatureList) {
		try {
			if (tagJsonObject != null) {

				if (tagJsonObject.isNull(UplusOutputData.JSON_NAME_MAINTAIN_FEATURE)) {
					JsonArray maintainFeatureJsonArray = new JsonArray();
					for (String feature : maintainFeatureList) {
						maintainFeatureJsonArray.put(feature);
					}
					tagJsonObject.put(UplusOutputData.JSON_NAME_MAINTAIN_FEATURE, maintainFeatureJsonArray);
				} else {
					JsonArray maintainFeatureJsonArray = tagJsonObject.optJSONArray(UplusOutputData.JSON_NAME_MAINTAIN_FEATURE);
					for (String feature : maintainFeatureList) {
						if(!isMaintainFeature(tagJsonObject, feature)){
							maintainFeatureJsonArray.put(feature);
						}
					}
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void setTextEffectTagType(JsonObject tagContainer, String type) {
		try {
			if (tagContainer != null) {
				tagContainer.put(UplusTextEffectData.TAG_JSON_KEY_TYPE, type); 
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getTextEffectTagType(TextEffect textEffect){
		JsonObject tagObject = textEffect.getTagContainer(); 
		if(tagObject != null){
			if(!tagObject.isNull(UplusTextEffectData.TAG_JSON_KEY_TYPE)){
				String returnValue = UplusTextEffectData.TAG_JSON_VALUE_TYPE_UNKNOWN; 
				try {
					returnValue = tagObject.getString(UplusTextEffectData.TAG_JSON_KEY_TYPE);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
				return returnValue; 
			}else{
				return UplusTextEffectData.TAG_JSON_VALUE_TYPE_UNKNOWN;
			}
		}else{
			return UplusTextEffectData.TAG_JSON_VALUE_TYPE_UNKNOWN; 
		}
	}
	
	public static SchedulerVersion getSchedulerVersion(JsonObject tagContainer){
		SchedulerVersion version = null; 
		try {
			if (tagContainer != null) {
				if(!tagContainer.isNull(SchedulerVersion.JSON_KEY_SCHEDULER_VERSION)){
					JsonObject scheduerVersionObject = tagContainer.getJSONObject(SchedulerVersion.JSON_KEY_SCHEDULER_VERSION);
					version = new SchedulerVersion(scheduerVersionObject);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return version; 
	}
}
