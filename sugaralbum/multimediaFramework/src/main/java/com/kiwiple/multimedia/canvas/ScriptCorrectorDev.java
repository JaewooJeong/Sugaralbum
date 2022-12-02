package com.kiwiple.multimedia.canvas;

import static com.kiwiple.multimedia.canvas.Transition.SceneOrder.FORMER;
import static com.kiwiple.multimedia.canvas.Transition.SceneOrder.LATTER;
import static com.kiwiple.multimedia.canvas.Visualizer.JSON_NAME_VERSION;

import java.util.ArrayList;

import org.json.JSONException;

import android.graphics.BitmapFactory;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.R;
import com.kiwiple.multimedia.Version;
import com.kiwiple.multimedia.canvas.Transition.SceneOrder;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.CollectionUtils;
import com.kiwiple.multimedia.util.DebugUtils;

/**
 * ScriptCorrectorDev.
 * 
 */
final class ScriptCorrectorDev extends AbstractScriptCorrector {

	// // // // // Static variable.
	// // // // //
	private static final Version VERSION_1_1_0 = new Version(1, 1, 0);
	private static final Version VERSION_1_2_0 = new Version(1, 2, 0);
	private static final Version VERSION_1_3_0 = new Version(1, 3, 0);
	private static final Version VERSION_1_4_0 = new Version(1, 4, 0);

	// // // // // Method.
	// // // // //
	@Override
	public Compatibility isCompatible(JsonObject scriptJsonObject) {
		Precondition.checkNotNull(scriptJsonObject);

		try {
			Version scriptVersion = extractVersion(scriptJsonObject);
			L.i("JSON script version: " + scriptVersion.toString());

			if (scriptVersion.isAbove(Version.current))
				return Compatibility.INCOMPATIBLE;
			else if (scriptVersion.isBelow(VERSION_1_4_0))
				return Compatibility.COMPATIBLE_WITH_UPGRADE;
		} catch (JSONException exception) {
			exception.printStackTrace();
			return Compatibility.INCOMPATIBLE;
		}
		return Compatibility.COMPATIBLE;
	}

	@Override
	public boolean upgrade(JsonObject scriptJsonObject) {
		Precondition.checkNotNull(scriptJsonObject);

		try {
			Version scriptVersion = extractVersion(scriptJsonObject);
			L.i(String.format("Try to upgrade version: %s -> %s", scriptVersion.toString(), Version.current.toString()));

			if (scriptVersion.isBelow(VERSION_1_1_0))
				scriptVersion = upgradeTo_1_1_0(scriptJsonObject);
			if (scriptVersion.isBelow(VERSION_1_2_0))
				scriptVersion = upgradeTo_1_2_0(scriptJsonObject);
			if (scriptVersion.isBelow(VERSION_1_3_0))
				scriptVersion = upgradeTo_1_3_0(scriptJsonObject);
			if (scriptVersion.isBelow(VERSION_1_4_0))
				scriptVersion = upgradeTo_1_4_0(scriptJsonObject);

			scriptJsonObject.put(JSON_NAME_VERSION, Version.current);
			return true;
		} catch (Exception exception) {
			DebugUtils.dumpJsonObject(scriptJsonObject, "v_error");
			exception.printStackTrace();
			return false;
		}
	}

	private Version upgradeTo_1_1_0(JsonObject scriptJsonObject) throws JSONException {
		L.i();

		float aspectRatioForHD = 1920.0f / 1080.0f;
		scriptJsonObject.put("aspect_ratio", aspectRatioForHD);

		if (!scriptJsonObject.isNull("audio")) {
			JsonObject audio = scriptJsonObject.getJSONObject("audio");
			audio.put("resource_type", audio.getString("file_path").startsWith("audio") ? "asset_file" : "file");
			audio.remove("is_asset");
		}

		for (JsonObject textEffect : pickOutRegionChild(scriptJsonObject, "textEffect")) {
			textEffect.put("type", "text");
			textEffect.put("align", "center");
		}

		JsonObject resolution = new JsonObject();
		resolution.put("name", "FHD").put("width", 1920).put("height", 1080).put("magnification", 3).put("aspect_ratio", aspectRatioForHD);

		for (JsonObject jsonObject : pickOutRegionChild(scriptJsonObject, "animation")) {

			jsonObject.remove("image_resource_type");
			for (JsonObject animationObject : jsonObject.getJSONArray("animation_object").asList(JsonObject.class)) {
				JsonObject imageResource = new JsonObject();
				imageResource.put("resource_type", "file");
				imageResource.put("file_path", animationObject.remove("image_file_path"));
				imageResource.put("base_resolution", resolution);
				animationObject.put("image_resource", imageResource);
			}
		}
		for (JsonObject jsonObject : pickOutRegionChild(scriptJsonObject, "overlay", "drawable")) {

			JsonObject imageResource = jsonObject.getJSONObject("image_resource");
			imageResource.put("resource_type", imageResource.remove("image_resource_type"));
			imageResource.put("base_resolution", resolution);
			if (imageResource.getString("resource_type").equals("internal_drawable")) {
				imageResource.put("resource_type", "drawable");
				imageResource.put("drawable_id", R.drawable.vignette_blur);
				imageResource.remove("internal_drawable");
			}
		}
		scriptJsonObject.put(JSON_NAME_VERSION, VERSION_1_1_0);
		return VERSION_1_1_0;
	}

	private Version upgradeTo_1_2_0(JsonObject scriptJsonObject) throws JSONException {
		L.i();

		for (JsonObject imageFileScene : pickOutRegionChild(scriptJsonObject, "image_file")) {
			if (!imageFileScene.isNull("sticker_elements")) {

				JsonArray effects;
				if (imageFileScene.isNull("effects")) {
					effects = new JsonArray();
					imageFileScene.put("effects", effects);
				} else {
					effects = imageFileScene.getJSONArray("effects");
				}

				JsonArray stickerElements = (JsonArray) imageFileScene.remove("sticker_elements");
				for (JsonObject stickerElement : stickerElements.asList(JsonObject.class)) {
					stickerElement.put("base_width", stickerElement.remove("image_width"));

					JsonArray animations = new JsonArray();
					animations.put(stickerElement);

					JsonObject stickerEffect = new JsonObject();
					stickerEffect.put("type", "sticker");
					stickerEffect.put("animation_object", animations);
					effects.put(stickerEffect);
				}
			}
		}
		scriptJsonObject.put(JSON_NAME_VERSION, VERSION_1_2_0);
		return VERSION_1_2_0;
	}

	private Version upgradeTo_1_3_0(JsonObject scriptJsonObject) throws JSONException {
		L.i();

		JsonObject resolution = new JsonObject();
		Object aspectRatio = scriptJsonObject.remove("aspect_ratio");
		resolution.put("name", "nHD").put("width", 640).put("height", 360).put("magnification", 1).put("aspect_ratio", aspectRatio);
		scriptJsonObject.put("resolution", resolution);

		for (JsonObject scene : scriptJsonObject.find(JsonArray.class, "scenes").asList(JsonObject.class)) {
			scene.put("type", scene.remove("type") + "_scene");

			if (scene.getString("type").equals("multi_layer_scene")) {
				for (JsonObject layer : scene.optJSONArrayAsList("layers", JsonObject.class)) {
					layer.put("type", "layer_scene");
				}
			}
		}
		for (JsonArray transitions : scriptJsonObject.findAll(JsonArray.class, "transitions")) {
			for (JsonObject transition : CollectionUtils.removeAllNull(transitions.asList(JsonObject.class))) {
				transition.put("type", transition.remove("type") + "_transition");
			}
		}
		for (JsonArray effects : scriptJsonObject.findAll(JsonArray.class, "effects")) {
			for (JsonObject effect : effects.asList(JsonObject.class)) {
				effect.put("type", effect.remove("type") + "_effect");
			}
		}
		for (JsonObject scaler : scriptJsonObject.findAll(JsonObject.class, "scaler")) {
			scaler.put("type", scaler.remove("type") + "_scaler");
		}

		for (JsonObject spinTransition : pickOutRegionChild(scriptJsonObject, "spin_transition")) {
			switch ((String) spinTransition.remove("spin_type")) {
				case "step_two_one":
					spinTransition.put("spin_order", new SceneOrder[] { FORMER, FORMER });
					break;
				case "step_three_two":
					spinTransition.put("spin_order", new SceneOrder[] { FORMER, FORMER, FORMER, LATTER });
					break;
				default:
					Precondition.assureUnreachable();
			}
			spinTransition.put("use_overshoot", false);
			spinTransition.put("use_blurred_border", false);
		}
		for (JsonObject imageFileScene : pickOutRegionChild(scriptJsonObject, "image_file_scene")) {
			JsonObject imageResource = new JsonObject();

			imageResource.put("resource_type", "file");
			imageResource.put("scale_type", "buffer");
			imageResource.put("file_path", imageFileScene.remove("file_path"));
			imageFileScene.put("image_resource", imageResource);
		}

		ArrayList<JsonObject> containingResourceType = new ArrayList<>();
		for (JsonObject jsonObject : pickOutRegionChild(scriptJsonObject, "animation_effect", "overlay_effect", "sticker_effect", "drawable_scene")) {
			if (!jsonObject.isNull("image_resource")) {
				containingResourceType.add(jsonObject.getJSONObject("image_resource"));
			}
		}
		containingResourceType.add(scriptJsonObject.getJSONObject("audio"));

		for (JsonObject jsonObject : containingResourceType) {
			switch (jsonObject.getString("resource_type")) {
				case "asset_file":
					jsonObject.put("resource_type", "android_asset");
					break;
				case "drawable":
					jsonObject.put("resource_type", "android_resource");
					break;
				case "file":
				default:
					break;
			}
		}

		for (JsonObject kenBurnsScaler : pickOutRegionChild(scriptJsonObject, "ken_burns_scaler")) {
			String interpolatorTypeName = (String) kenBurnsScaler.remove("interpolator_type");
			kenBurnsScaler.put("interpolator_types", new JsonArray().put(interpolatorTypeName == null ? "linear" : interpolatorTypeName));
			kenBurnsScaler.put("weights", new JsonArray().put(1));
		}
		scriptJsonObject.put(JSON_NAME_VERSION, VERSION_1_3_0);
		return VERSION_1_3_0;
	}

	private Version upgradeTo_1_4_0(JsonObject scriptJsonObject) throws JSONException {
		L.i();

		for (JsonObject multiLayerScene : pickOutRegionChild(scriptJsonObject, "multi_layer_scene")) {
			JsonArray layers = new JsonArray();

			for (JsonObject layerScene : multiLayerScene.getJSONArrayAsList("layers", JsonObject.class)) {

				JsonObject layer = new JsonObject();
				layer.put("layer", layerScene).put("layer_viewport", (JsonObject) layerScene.remove("layer_viewport"));
				layers.put(layer);
			}
			multiLayerScene.put("layers", layers);
		}
		for (JsonArray elements : scriptJsonObject.findAll(JsonArray.class, "collage_elements")) {
			for (JsonObject element : elements.asList(JsonObject.class)) {
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;

				BitmapFactory.decodeFile(element.getString("file_path"), options);

				element.put("collage_width", element.getInt("width"));
				element.put("collage_height", element.getInt("height"));
				element.put("width", options.outWidth);
				element.put("height", options.outHeight);
			}
		}
		for (JsonObject overlayEffect : pickOutRegionChild(scriptJsonObject, "overlay_effect")) {
			overlayEffect.put("coordinate_x", overlayEffect.getFloat("coordinate_x") / 3.0f);
			overlayEffect.put("coordinate_y", overlayEffect.getFloat("coordinate_y") / 3.0f);
		}
		for (JsonObject enterTransition : pickOutRegionChild(scriptJsonObject, "enter_transition")) {
			JsonObject block = new JsonObject();
			block.put("viewport", Viewport.FULL_VIEWPORT);
			block.put("direction", enterTransition.remove("direction"));

			enterTransition.put("blocks", new JsonArray().put(block));
			enterTransition.put("line_thickness", 1.0f);
			enterTransition.put("is_reverse", false);
		}
		for (JsonObject spinTransition : pickOutRegionChild(scriptJsonObject, "spin_transition")) {
			spinTransition.put("interpolator_type", "exponential_in_out");
		}

		scriptJsonObject.put(JSON_NAME_VERSION, VERSION_1_4_0);
		return VERSION_1_4_0;
	}

	// // // // // Constructor.
	// // // // //
	ScriptCorrectorDev() {
		// Do nothing.
	}
}
