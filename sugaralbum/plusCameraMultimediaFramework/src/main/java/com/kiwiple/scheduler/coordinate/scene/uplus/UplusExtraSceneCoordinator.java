package com.kiwiple.scheduler.coordinate.scene.uplus;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import com.kiwiple.debug.L;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.multimedia.canvas.BorderEffect;
import com.kiwiple.multimedia.canvas.DynamicTextureEffect;
import com.kiwiple.multimedia.canvas.Effect;
import com.kiwiple.multimedia.canvas.EnterEffect;
import com.kiwiple.multimedia.canvas.FogEffect;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.ImageResource;
import com.kiwiple.multimedia.canvas.KenBurnsScaler;
import com.kiwiple.multimedia.canvas.MultiLayerScene;
import com.kiwiple.multimedia.canvas.OverlayEffect;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.TextEffect;
import com.kiwiple.multimedia.canvas.VideoFileScene;
import com.kiwiple.multimedia.canvas.ImageResource.ScaleType;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.scheduler.coordinate.effect.uplus.UplusEffectApplyManager;
import com.kiwiple.scheduler.coordinate.scene.ImageFileSceneCoordinator;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.data.uplus.effect.UplusDynamicTextureEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusLightEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusTextEffectData;
import com.kiwiple.scheduler.tag.UserTag;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.FontAlign;
import com.kiwiple.scheduler.theme.Theme.Frame;
import com.kiwiple.scheduler.util.EffectUtil;
import com.kiwiple.scheduler.util.ImageUtil;
import com.kiwiple.scheduler.util.IntroOutroUtils;

public class UplusExtraSceneCoordinator extends ImageFileSceneCoordinator {
	
	public static final String UPLUS_VALUE_TEXT_ELEMENT_DEFAULT_TEXT = "";
	public static final float UPLUS_VALUE_TEXT_ELEMENT_STORY_DEFAULT_SIZE = 18.0f;
	public static final float UPLUS_VALUE_TEXT_ELEMENT_DEFAULT_SIZE = 24.0f;
	public static final int UPLUS_VALUE_SCENE_TEXT_DEFAULT_LINE_SPACE = 20;
	
	protected UplusOutputData mUplusOutputData;
	protected Context mContext;
	protected Theme mTheme;

	public UplusExtraSceneCoordinator(Context context, OutputData outputData) {
		mContext = context;
		mUplusOutputData = (UplusOutputData) outputData;
		mTheme = mUplusOutputData.getTheme();
	}

	protected void setDynamicEffect(ImageFileScene scene, JSONObject dynamicIntroOutroJson, String title) {

		try {
			JSONArray dynamicJsonArray = dynamicIntroOutroJson.getJSONArray("dynamic_array"); 
			for (int i = 0; i < dynamicJsonArray.length(); i++) {
				JSONObject effectObject = dynamicJsonArray.getJSONObject(i);
				L.d("extra scene effect, index : " + i + ", type : " + effectObject.getString(Effect.JSON_NAME_TYPE) );
				if(effectObject != null){
					String type = effectObject.getString(Effect.JSON_NAME_TYPE);
					
					if (type.equals(DynamicTextureEffect.JSON_VALUE_TYPE)) {
						applyDynamicTextureEffect(effectObject, scene, mTheme);
						
					} else if (type.equals(TextEffect.JSON_VALUE_TYPE)) {
						applyTextffect(effectObject, scene);
						
					}
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void applyTextffect(JSONObject effectObject, Scene scene) throws JSONException {

		UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
		Scene.Editor<?, ?> editor = scene.getEditor();

		TextEffect.Editor textEditor = editor.addEffect(TextEffect.class).getEditor();
		effectApplyManager.applyTextEffect(textEditor, new UplusTextEffectData(TextEffect.JSON_VALUE_TYPE, effectObject));

	}

	private void applyDynamicTextureEffect(JSONObject effectObject, Scene scene, Theme theme) throws JSONException {

		UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
		Scene.Editor<?, ?> editor = scene.getEditor();

		DynamicTextureEffect.Editor dynamicEditor = editor.addEffect(DynamicTextureEffect.class).getEditor();
		effectApplyManager.applyDynamicTextureEffect(mContext, mUplusOutputData,dynamicEditor, new UplusDynamicTextureEffectData(DynamicTextureEffect.JSON_VALUE_TYPE, effectObject));

	}

	protected void setFrameScene(ImageFileScene scene, ImageFileScene.Editor editor, boolean addTitle, Frame extraFrame, String mediaPath, long videoPosition, String title) {
		if (extraFrame == null) {
			throw new IllegalArgumentException("Intro/Outro frame is missing");
		}
		
		String overlayPath = null; 
		if (extraFrame.frameImageName != null) {
			overlayPath = mTheme.combineDowloadImageFilePath(mContext, extraFrame.frameImageName, "png");
		}
		
		L.d("frame type " + extraFrame.frameType + ", back gound image : " + extraFrame.useUserImageBackground);
		if (extraFrame.useUserImageBackground) {
			if (!TextUtils.isEmpty(mediaPath)) {
				
				if(ImageUtil.isVideoFile(mediaPath)){
					editor.setImageResource(ImageResource.createFromVideoFile(mediaPath, ScaleType.BUFFER, (int)videoPosition));
				}else{
					editor.setImageResource(ImageResource.createFromFile(mediaPath, ScaleType.BUFFER));
				}
			}

			if (extraFrame.frameImageName != null) {
				OverlayEffect.Editor overlayEditor = editor.addEffect(OverlayEffect.class).getEditor();
				overlayEditor.setImageFile(overlayPath, Resolution.FHD);
			}
		} else {
			if (extraFrame.frameImageName != null) {
				editor.setImageResource(ImageResource.createFromFile(overlayPath, ScaleType.BUFFER)); 
			}
		}

		if (extraFrame.fontSize > 0 && addTitle) {

			TextEffect textEffect = editor.addEffect(TextEffect.class); 
			TextEffect.Editor textEffectEditor = textEffect.getEditor();
			textEffectEditor.setResourceAlign(extraFrame.fontAlign == FontAlign.CENTER ? TextEffect.JSON_VALUE_ALIGN_CENTER : (extraFrame.fontAlign == FontAlign.RIGHT ? TextEffect.JSON_VALUE_ALIGN_RIGHT : TextEffect.JSON_VALUE_ALIGN_LEFT));
			textEffectEditor.setResourceColor(extraFrame.fontColor);
			textEffectEditor.setResourceFontName("DroidSans"/* inoutroFrame.fontName */);
			textEffectEditor.setResourceSize(extraFrame.fontSize);
			textEffectEditor.setResoureCoordinate(extraFrame.fontCoordinateLeft, extraFrame.fontCoordinateTop, extraFrame.fontCoordinateRight, extraFrame.fontCoordinateBottom);
			textEffectEditor.setResourceText(title);
			textEffectEditor.setBaseResolution(Resolution.FHD);
			UserTag.setTextEffectTagType(textEffect.getTagContainer(), UplusTextEffectData.TAG_JSON_VALUE_TYPE_TITLE);
		}

		if (extraFrame.objects != null) {
			UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
			effectApplyManager.applyFrameEffect(mContext, mUplusOutputData, extraFrame.objects, editor);
		}
	}

	
	protected void setDefaultScene(Scene scene, ImageFileScene.Editor editor, boolean addTitle, String mediaPath, long videoPosition, String title) {

		if (!TextUtils.isEmpty(mediaPath)) {
			if(ImageUtil.isVideoFile(mediaPath)){
				editor.setImageResource(ImageResource.createFromVideoFile(mediaPath, ScaleType.BUFFER, (int)videoPosition));
			}else{
				editor.setImageResource(ImageResource.createFromFile(mediaPath, ScaleType.BUFFER));
			}
		}

		if (addTitle) {
			// text effect without dynamic intro
			
			TextEffect textEffect = editor.addEffect(TextEffect.class); 
			TextEffect.Editor textEffectEditor = textEffect.getEditor();
			textEffectEditor.setResourceAlign(TextEffect.JSON_VALUE_ALIGN_CENTER);
			textEffectEditor.setResourceColor(Color.WHITE);
			textEffectEditor.setResourceFontName("DroidSans"/* inoutroFrame.fontName */);
			textEffectEditor.setResourceSize(UPLUS_VALUE_TEXT_ELEMENT_STORY_DEFAULT_SIZE);
			textEffectEditor.setResoureCoordinate(0, 125, 640, 145);
			textEffectEditor.setResourceText(UPLUS_VALUE_TEXT_ELEMENT_DEFAULT_TEXT);
			textEffectEditor.setBaseResolution(Resolution.NHD);
			UserTag.setTextEffectTagType(textEffect.getTagContainer(), UplusTextEffectData.TAG_JSON_VALUE_TYPE_NORMAL);
			
			TextEffect textEffectTitle = editor.addEffect(TextEffect.class); 
			TextEffect.Editor textEffectTitleEditor = textEffectTitle.getEditor();
			textEffectTitleEditor.setResourceAlign(TextEffect.JSON_VALUE_ALIGN_CENTER);
			textEffectTitleEditor.setResourceColor(Color.WHITE);
			textEffectTitleEditor.setResourceFontName("DroidSans"/* inoutroFrame.fontName */);
			textEffectTitleEditor.setResourceSize(UPLUS_VALUE_TEXT_ELEMENT_DEFAULT_SIZE);
			textEffectTitleEditor.setResoureCoordinate(0, 160, 640, 185);
			textEffectTitleEditor.setResourceText(title);
			textEffectTitleEditor.setBaseResolution(Resolution.NHD);
			UserTag.setTextEffectTagType(textEffectTitle.getTagContainer(), UplusTextEffectData.TAG_JSON_VALUE_TYPE_TITLE); 
		}

	}

	protected void setDefaultKenburn(ImageFileScene.Editor editor) {
		KenBurnsScaler.Editor scalerEditor = editor.setScaler(KenBurnsScaler.class).getEditor();
		Viewport[] viewportArray = new Viewport[2];
		viewportArray[0] = new Viewport(0, 0, 1, 1);
		viewportArray[1] = new Viewport(0, 0, 1, 1);
		scalerEditor.setViewports(viewportArray);
	}
	
	/**
	 * scene( intro / outro )에 viewport를 첫번째 씬의 viewport를 기준으로 지정한다. 
	 * 첫 씬이 ImageFileScene의 경우 , first_scene_viewport[0] -> intro_scene_viewport[0], ,intro_scene_viewport[1]
	 * 그 외의 경우,  Center Crop으로 설정된다.
	 * 
	 * @param editor 
	 * @param scene  
	 */
	protected void setCenterKenburn(ImageFileScene.Editor sceneEditor, Scene copyFrom) {
		KenBurnsScaler.Editor scalerEditor = sceneEditor.setScaler(KenBurnsScaler.class).getEditor();
		Viewport[] viewportArray = new Viewport[2];
		ImageData imageData = IntroOutroUtils.getImageData(mContext,copyFrom);
		
		if (copyFrom.getClass().equals(ImageFileScene.class)) {
		
			ImageFileScene.Editor sEditor = (ImageFileScene.Editor) copyFrom.getEditor();
			KenBurnsScaler.Editor kenburnScaler = (KenBurnsScaler.Editor) sEditor.getObject().getScaler().getEditor();

			Viewport[] firstViewport = kenburnScaler.getObject().getViewports();
			
			viewportArray[0] = firstViewport[0];
			viewportArray[1] = firstViewport[0];
		} else{

			viewportArray[0] = IntroOutroUtils.getCenterFullViewPort( imageData.path, imageData.width, imageData.height);
			viewportArray[1] = viewportArray[0];
		}

		scalerEditor.setViewports(viewportArray);
	}
	
	/**
	 * dynamicEffectJson을 가지는 경우, scene( intro / outro )에 viewport를 첫번째 씬의 viewport를 기준으로 지정한다. 
	 * 첫 씬이 ImageFileScene의 경우 , first_scene_viewport[0] -> intro_scene_viewport[1], first_scene_viewport[1] -> intro_scene_viewport[0]
	 * 그 외의 경우,  Center Zoom in으로 설정된다.
	 * 
	 * @param editor
	 * @param scene
	 */
	protected void setDynamicKenburn(ImageFileScene.Editor sceneEditor, Scene copyFrom) {
		Viewport[] viewportArray = new Viewport[2];
		KenBurnsScaler.Editor scalerEditor = sceneEditor.setScaler(KenBurnsScaler.class).getEditor();
		ImageData imageData = IntroOutroUtils.getImageData(mContext,copyFrom);
		
		if (copyFrom.getClass().equals(ImageFileScene.class)) {

			ImageFileScene.Editor sEditor = (ImageFileScene.Editor) copyFrom.getEditor();
			KenBurnsScaler.Editor kenburnScaler = (KenBurnsScaler.Editor) sEditor.getObject().getScaler().getEditor();

			Viewport[] firstViewport = kenburnScaler.getObject().getViewports();
			viewportArray[0] = new Viewport(firstViewport[1].left,firstViewport[1].top,firstViewport[1].right,firstViewport[1].bottom);
			viewportArray[1] = new Viewport(firstViewport[0].left,firstViewport[0].top,firstViewport[0].right,firstViewport[0].bottom);
		} else {
			viewportArray[0] = IntroOutroUtils.getCenterFullViewPort(imageData.path, imageData.width, imageData.height);
			viewportArray[1] = IntroOutroUtils.getZoomInViewPort(viewportArray[0]);
		}
		scalerEditor.setViewports(viewportArray);
	}
}
