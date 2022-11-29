package com.kiwiple.multimedia.canvas;

import static com.kiwiple.multimedia.canvas.Region.JSON_NAME_SCENES;
import static com.kiwiple.multimedia.canvas.Region.JSON_NAME_TRANSITIONS;
import static com.kiwiple.multimedia.canvas.Visualizer.JSON_NAME_REGIONS;
import static com.kiwiple.multimedia.canvas.Visualizer.JSON_NAME_VERSION;
import static com.kiwiple.multimedia.canvas.RegionChild.JSON_NAME_TYPE;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;

import com.kiwiple.multimedia.Version;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.multimedia.json.JsonObject;

/**
 * AbstractScriptCorrector
 */
abstract class AbstractScriptCorrector implements IScriptCorrector {

	// // // // // Static method.
	// // // // //
	protected static Version extractVersion(JsonObject scriptJsonObject) throws JSONException {
		return new Version(scriptJsonObject.getJSONObject(JSON_NAME_VERSION));
	}

	protected static JsonObject getRegionJsonObject(JsonObject scriptJsonObject) throws JSONException {
		return scriptJsonObject.getJSONObject(JSON_NAME_REGIONS);
	}

	protected static List<JsonObject> getSceneJsonObjectList(JsonObject scriptJsonObject) throws JSONException {
		return getRegionJsonObject(scriptJsonObject).getJSONArray(JSON_NAME_SCENES).asList(JsonObject.class);
	}

	protected static List<JsonObject> getTransitionJsonObjectList(JsonObject scriptJsonObject) throws JSONException {
		return getRegionJsonObject(scriptJsonObject).getJSONArray(JSON_NAME_TRANSITIONS).asList(JsonObject.class);
	}

	static ArrayList<JsonObject> pickOutRegionChild(JsonObject jsonObject, String... typeNames) throws JSONException {

		ArrayList<JsonObject> list = new ArrayList<>();
		findRegionChildRecursively(jsonObject, list, typeNames);
		return list;
	}

	private static void findRegionChildRecursively(JsonObject jsonObject, List<JsonObject> dstList, String... typeNames) throws JSONException {

		Iterator<String> iterator = jsonObject.keys();
		while (iterator.hasNext()) {

			String key = iterator.next();
			Object value = jsonObject.get(key);

			if (key.equals(JSON_NAME_TYPE) && value instanceof String) {
				for (String name : typeNames) {
					if (((String) value).endsWith(name)) {
						dstList.add(jsonObject);
						break;
					}
				}
			} else if (value instanceof JsonObject) {
				findRegionChildRecursively((JsonObject) value, dstList, typeNames);
			} else if (value instanceof JsonArray) {
				findRegionChildRecursively((JsonArray) value, dstList, typeNames);
			}
		}
	}

	private static void findRegionChildRecursively(JsonArray jsonArray, List<JsonObject> dstList, String... typeNames) throws JSONException {

		int length = jsonArray.length();
		for (int i = 0; i != length; ++i) {

			Object value = jsonArray.get(i);
			if (value instanceof JsonObject) {
				findRegionChildRecursively((JsonObject) value, dstList, typeNames);
			} else if (value instanceof JsonArray) {
				findRegionChildRecursively((JsonArray) value, dstList, typeNames);
			}
		}
	}
}