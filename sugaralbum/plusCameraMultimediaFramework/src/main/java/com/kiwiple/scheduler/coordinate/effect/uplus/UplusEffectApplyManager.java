package com.kiwiple.scheduler.coordinate.effect.uplus;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Point;

import com.kiwiple.debug.L;
import com.kiwiple.imageanalysis.correct.ImageCorrectStickerData;
import com.kiwiple.multimedia.ResourceType;
import com.kiwiple.multimedia.canvas.AnimationEffect;
import com.kiwiple.multimedia.canvas.AnimationEffect.AnimationMotion;
import com.kiwiple.multimedia.canvas.BorderEffect;
import com.kiwiple.multimedia.canvas.BorderEffect.SideType;
import com.kiwiple.multimedia.canvas.DynamicTextureEffect;
import com.kiwiple.multimedia.canvas.EnterEffect;
import com.kiwiple.multimedia.canvas.EnterEffect.Direction;
import com.kiwiple.multimedia.canvas.FogEffect;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.KenBurnsScaler;
import com.kiwiple.multimedia.canvas.LayerScene;
import com.kiwiple.multimedia.canvas.LightEffect;
import com.kiwiple.multimedia.canvas.MultiLayerScene;
import com.kiwiple.multimedia.canvas.NoiseEffect;
import com.kiwiple.multimedia.canvas.OverlayEffect;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.multimedia.canvas.ScaleEffect;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.StepAppearEffect;
import com.kiwiple.multimedia.canvas.StickerEffect;
import com.kiwiple.multimedia.canvas.SwayEffect;
import com.kiwiple.multimedia.canvas.TextEffect;
import com.kiwiple.multimedia.canvas.VideoFileScene;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.json.JsonUtils;
import com.kiwiple.scheduler.R;
import com.kiwiple.scheduler.coordinate.effect.EffectApplyManager;
import com.kiwiple.scheduler.coordinate.scaler.KenBurnsScalerCoordinator;
import com.kiwiple.scheduler.data.EffectData;
import com.kiwiple.scheduler.data.MultiLayerData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.data.uplus.effect.UplusBorderEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusDynamicTextureEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusDynamicTextureEffectData.Motions;
import com.kiwiple.scheduler.data.uplus.effect.UplusDynamicTextureEffectData.Textures;
import com.kiwiple.scheduler.data.uplus.effect.UplusEnterEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusFogEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusLightEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusScaleEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusStepAppearEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusTextEffectData;
import com.kiwiple.scheduler.data.uplus.effect.UplusTextEffectData.TextMotions;
import com.kiwiple.scheduler.tag.UserTag;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.FrameObject;
import com.kiwiple.scheduler.theme.Theme.MotionType;
import com.kiwiple.scheduler.util.ViewportUtils;

public class UplusEffectApplyManager extends EffectApplyManager {
	/**
	 * FogEffect.Editor에 type값을 지정한다.
	 */
	public void applyFogEffect(FogEffect.Editor fogEditor, UplusFogEffectData fogEffectDatas){
		L.d("fog type : " + fogEffectDatas.getType());
		fogEditor.setEffectType(fogEffectDatas.getType()); 
		
	}
	
	/**
	 * EnterEffect.Editor에 해당 데이타 값을 지정한다. 
	 * MultiLayerScene의 경우, Scene.layers에 각 Editor.Effect의 Editor에 EnterEffect를 적용한다.
	 * 
	 * @param enterEditor Scene에 적용될 Effect의 Editor
	 * @param enterEffectDatas Effect data 
	 * @param scene Current Scene
	 */
	public void applyEnterEffect(EnterEffect.Editor enterEditor, UplusEnterEffectData enterEffectDatas, Scene scene){
		// TODO : reverse control
		// type 1 : reverse false (enter in)
		// type 2 : reverse true (enter out)
		// type 3 : in out mix
	
		String direction = enterEffectDatas.getDiration();
		Direction eDirection = EnterEffect.Direction.TWO_WAY_HORIZONTAL;
		boolean reverse = enterEffectDatas.getReverse();
		int duration = enterEffectDatas.getDuration();
		L.e("applyEnterEffect, direction :"+direction+", reverse: "+reverse+", duration :  "+duration);
		
		
		 if(scene.getClass().equals(MultiLayerScene.class)){ 
			 /**
			  * MultiLayerScene의 경우 Scene.Effect EnterEffect가 적용되지 않고, 
			  * 각 Layer에 EnterEffect가 적용되고,이때,  direction값은  json에 설정된 값이 아닌, kenburn값과 동일하게 적용되도록 한다.
			  * 
			  * @author aubergine
			  */
			 MultiLayerScene multiLayerScene = (MultiLayerScene) scene;
			 
			 for(Scene layerScene : multiLayerScene.getLayers()){
					if(layerScene.getClass().equals(LayerScene.class)){
						LayerScene multiLayerImageScene = (LayerScene)layerScene; 
				 
						 Direction layerDirection = Direction.ONE_WAY_LEFT;
						 
						 //TODO check kenburn viewport
						 Viewport[]  viewport = ((KenBurnsScaler) multiLayerImageScene.getScaler()).getViewports();
						 //L.e("viewport[0] :"+viewport[0].left+", "+viewport[0].top+", "+viewport[0].right+", "+viewport[0].bottom);
						 //L.e("viewport[1] :"+viewport[1].left+", "+viewport[1].top+", "+viewport[1].right+", "+viewport[1].bottom);
						 
						 ViewportUtils.Direction kenburnDirection = ViewportUtils.measureDirection(viewport[0], viewport[1]);
						 
						 if(kenburnDirection == ViewportUtils.Direction.RIGHT) {
							 if(reverse) layerDirection = Direction.ONE_WAY_RIGHT;
							 else layerDirection = Direction.ONE_WAY_LEFT;
						 }
						 else if(kenburnDirection == ViewportUtils.Direction.LEFT)  {
							 if(reverse) layerDirection = Direction.ONE_WAY_LEFT;
							 else layerDirection = Direction.ONE_WAY_RIGHT;
						 }
						 else if(kenburnDirection == ViewportUtils.Direction.UP) {
							 if(reverse) layerDirection = Direction.ONE_WAY_UP;
							 else layerDirection = Direction.ONE_WAY_DOWN;
						 }
						 else if(kenburnDirection == ViewportUtils.Direction.DOWN)  {
							 if(reverse) layerDirection = Direction.ONE_WAY_DOWN;
							 else layerDirection = Direction.ONE_WAY_UP;
						 }
						 
						 EnterEffect.Editor layerEnterEditor = multiLayerImageScene.getEditor().addEffect(EnterEffect.class).getEditor();
						 layerEnterEditor.setDirection(layerDirection);
						 layerEnterEditor.setEffectDuration(duration);
						 layerEnterEditor.setReverse(reverse);
					}else if(layerScene.getClass().equals(VideoFileScene.class)){
						
						VideoFileScene multiLayerVideoFileScene = (VideoFileScene)layerScene;
						EnterEffect.Editor layerEnterEditor = multiLayerVideoFileScene.getEditor().addEffect(EnterEffect.class).getEditor();
						layerEnterEditor.setDirection(Direction.ONE_WAY_RIGHT);
						layerEnterEditor.setEffectDuration(duration);
						layerEnterEditor.setReverse(reverse);
					}
			 }
		 }
		 else {//if(!scene.getClass().equals(BurstShotScene.class)){  
				 /**
				  * BurstShotScene & MultiLayerScene가 아닌 경우, EnterEffect는 Scene.Effect에 적용한다.
				  * 
				  */
				enterEditor.setDirection(JsonUtils.getEnumByJsonString(direction, Direction.class)); 
				enterEditor.setEffectDuration(duration);
				enterEditor.setReverse(reverse);
		 }
	}
	
	public void applyBorderEffect(Scene scene, UplusBorderEffectData borderEffectData){
		L.e("applyBorderEffect, color : " + borderEffectData.getColor() +", width : " + borderEffectData.getWidth());
		 if(scene.getClass().equals(MultiLayerScene.class)){ 
			 MultiLayerScene multiLayerScene = (MultiLayerScene) scene;
			 int index = 0; 
			 for(Scene layerScene : multiLayerScene.getLayers()){
				if(layerScene.getClass().equals(LayerScene.class)){
					LayerScene multiLayerImageScene = (LayerScene)layerScene; 
					 BorderEffect.Editor layerBorderEditor = multiLayerImageScene.getEditor().addEffect(BorderEffect.class).getEditor();
					 layerBorderEditor.setLineColor(borderEffectData.getColor()); 
					 layerBorderEditor.setLineWidth(borderEffectData.getWidth()); 
					 SideType[] sideTypes = BorderEffect.createSideTypeForInside(multiLayerScene.getLayerViewport(index));
					 layerBorderEditor.setSideType(sideTypes);
				}
				index++; 
			 }
		 }
	}
	
	public void applyStepAppearEffect(Scene scene, UplusStepAppearEffectData stepAppearData){
		if(scene.getClass().equals(MultiLayerScene.class)){ 
			 MultiLayerScene multiLayerScene = (MultiLayerScene) scene;
			 int layerCount = multiLayerScene.getLayers().size(); 
			 int sceneOrder[] = stepAppearData.getStepAppearOrder();
			 int applySceneOrder[] = stepAppearData.getStepAppearOrder();
			 int maxCount = 0;  
			 int index = 0;
			 
			 if(layerCount == 2){
				 if(sceneOrder[0]>sceneOrder[1]){
					 applySceneOrder[0] = 2; 
					 applySceneOrder[1] = 1; 
				 }else{
					 applySceneOrder[0] = 1; 
					 applySceneOrder[1] = 2;
				 }
			 }else if(layerCount == 3){
				 
				 for(int i = 0; i < layerCount ; i++){
					 int order = sceneOrder[i];
					 maxCount = 0; 
					 for(int j = 0 ; j < layerCount ; j++){
						 if(i == j){
							 continue; 
						 }
						 if(order > sceneOrder[j]){
							 maxCount++;  
						 }
					 }
					 if(maxCount == 2){
						 applySceneOrder[i] = 3; 
					 }else if(maxCount == 1){
						 applySceneOrder[i] = 2; 
					 }else if(maxCount == 0){
						 applySceneOrder[i] = 1; 
					 }
				 }
			 }
			 for (Scene layerScene : multiLayerScene.getLayers()){
				 layerScene.getEditor().removeAllEffects(StepAppearEffect.class); 
			}
			 index = 0; 
			 for(Scene layerScene : multiLayerScene.getLayers()){
				 
				 layerScene.getEditor().removeAllEffects(EnterEffect.class); 
				 
				 if(layerScene.getClass().equals(LayerScene.class)){
					 LayerScene multiLayerImageScene = (LayerScene)layerScene;  
					 StepAppearEffect.Editor StepAppearEditor = multiLayerImageScene.getEditor().addEffect(StepAppearEffect.class).getEditor();
					 StepAppearEditor.setAppearRatio(stepAppearData.getDefaultRatio() + stepAppearData.getStepRatio()*applySceneOrder[index]);
				}
				index++; 
			 }
		}
		
		UserTag.setMaintainFeature(scene.getTagContainer(), StepAppearEffect.JSON_VALUE_TYPE);
	}
	
	/**
	 * StickerEffect
	 * @param stickerEditor
	 * @param id
	 * @param scene
	 */
	public void applyStickerEffect(StickerEffect.Editor stickerEditor,ImageCorrectStickerData sticker){
		/**
		 * StickerEffect
		 */
		//L.e("sticker.imageWidth:"+sticker.imageWidth+", baseScale:"+baseScale);
		if(sticker.isTextSticker()){
			stickerEditor.setResource(sticker.text, sticker.typeFaceFilePath,sticker.getTextStyleValue(),
					sticker.fontColor,sticker.textWidth,sticker.textBorderWidth, sticker.textBorderColor,
					sticker.stickerCoordinate.x,sticker.stickerCoordinate.y,  
					sticker.stickerScale,sticker.stickerRotate, 
					sticker.stickerWidth, sticker.stickerHeight,
					sticker.stickerCategory,sticker.stickerSubCategory,sticker.imageWidth,Resolution.NHD);
		}
		else if(sticker.stickerAnimatedFileNames!= null && !sticker.stickerAnimatedFileNames.isEmpty()){
			ArrayList<String> names = sticker.stickerAnimatedFileNames;
			
			ResourceType resourcetype = ResourceType.ANDROID_RESOURCE;
			if(names.get(0).contains("/data")) resourcetype = ResourceType.FILE;
			
			
			for(int i = 0;i<names.size();i++){
				stickerEditor.setResource(names.get(i),
						sticker.stickerCoordinate.x,sticker.stickerCoordinate.y,  
						sticker.stickerScale,sticker.stickerRotate, 
						sticker.stickerWidth,sticker.stickerHeight,
						sticker.stickerCategory,sticker.stickerSubCategory,sticker.imageWidth,
						Resolution.NHD, resourcetype);
			}
		}
		else if(!sticker.stickerFileName.isEmpty()){
			ResourceType resourcetype = ResourceType.ANDROID_RESOURCE;
			if(sticker.stickerFileName.contains("/data")) resourcetype = ResourceType.FILE;
			stickerEditor.setResource(sticker.stickerFileName,
					sticker.stickerCoordinate.x,sticker.stickerCoordinate.y,  
					sticker.stickerScale,sticker.stickerRotate, 
					sticker.stickerWidth,sticker.stickerHeight,
					sticker.stickerCategory,sticker.stickerSubCategory,sticker.imageWidth,
					Resolution.NHD,resourcetype);
		}
		
	}
	
	
	public void applyFrameEffect(Context mContext,UplusOutputData mUplusOutputData, List<FrameObject> objects, Scene.Editor<?, ?> sceneEditor){
		
		String aniPath = null;
		
		for (FrameObject frameObject : objects) {
			aniPath = mUplusOutputData.getTheme().combineDowloadImageFilePath(mContext, frameObject.imageName, "png");
			
			if (frameObject.motion.type == MotionType.SCALE) {
				AnimationEffect.Editor animationEditor = sceneEditor.addEffect(AnimationEffect.class).getEditor();
				animationEditor.setResource(aniPath, frameObject.coordinate.x, frameObject.coordinate.y, AnimationMotion.MOTION_SCALE, frameObject.motion.value, Resolution.FHD);
			} else if (frameObject.motion.type == MotionType.REPLACE) {
				AnimationEffect.Editor animationEditor = sceneEditor.addEffect(AnimationEffect.class).getEditor();
				animationEditor.setResource(aniPath, frameObject.coordinate.x, frameObject.coordinate.y, AnimationMotion.MOTION_REPLACE, mUplusOutputData.getTheme()
						.combineDowloadImageFilePath(mContext, (String) frameObject.motion.value, "png"),Resolution.FHD);
				
			} else if (frameObject.motion.type == MotionType.ROTATE) {
				AnimationEffect.Editor animationEditor = sceneEditor.addEffect(AnimationEffect.class).getEditor();
				animationEditor.setResource(aniPath, frameObject.coordinate.x, frameObject.coordinate.y, AnimationMotion.MOTION_ROTATE, frameObject.motion.value,Resolution.FHD);
				
			}else if(frameObject.motion.type == MotionType.NOTHING){
				OverlayEffect.Editor overlayEditor = sceneEditor.addEffect(OverlayEffect.class).getEditor();
				overlayEditor.setImageFile(aniPath, Resolution.FHD);
				/*
				 * 현재 FrameObject의 모든 좌표 값은 FHD 해상도 기준으로 설정된 것입니다.
				 * 하지만 ICanvasUser의 모든 구현체는 주어진 모든 수치를 현재 해상도를 기준으로 적용되는 것이기 때문에 주의할 필요가 있습니다.
				 * 현재 Scheduler가 사용하는 Visualizer는 nHD 해상도를 사용하기 때문에, 이 수치를 nHD에 맞게 변환해야 합니다. 
				 */
				overlayEditor.setCoordinate(frameObject.coordinate.x / 3.0f, frameObject.coordinate.y / 3.0f);
			}
		}

	}
	
	public void applyDynamicTextureEffect(Context mContext,UplusOutputData mUplusOutputData,DynamicTextureEffect.Editor  dynamicEditor,  UplusDynamicTextureEffectData dynamicTextureObject) {
			dynamicEditor.clearTextures();
			dynamicEditor.setState(dynamicTextureObject.mReverse, dynamicTextureObject.mResolution);
			
			ArrayList<Textures> textures = dynamicTextureObject.getTextures();
			for(int dy = 0;dy<textures.size();dy++){
				Textures texture = textures.get(dy);
				String path = texture.mPath;
				
				int id = mContext.getResources().getIdentifier(path, "drawable",mContext.getPackageName());
				ResourceType resourceType = texture.mRType;
				if(id<=0) {
					path = mUplusOutputData.getTheme().combineDowloadImageFilePath(mContext, texture.mPath, "png");
					resourceType = ResourceType.FILE;
				}
				
				dynamicEditor.addTexture(path, texture.mScale, texture.mRotate, resourceType);
			}
			ArrayList<Motions> motions = dynamicTextureObject.getMotions();
			for(int d = 0;d<motions.size();d++){
				Motions motion = motions.get(d);
				dynamicEditor.addMotion(new Point(motion.mMotionP.x, motion.mMotionP.y), motion.mMotionType, motion.mMotionDuration, motion.mAlpha, motion.mAnimated);
			}
			
			dynamicEditor.setActiveTimeRange(dynamicTextureObject.getActiveStartRatio(), dynamicTextureObject.getActiveEndRatio());
			dynamicEditor.setDrawOnlyWhileActiveTime(dynamicTextureObject.isDrawOnlyActvieRatio());
	}
	
	public void applyTextEffect(TextEffect.Editor  textEditor,  UplusTextEffectData textObject) {
		textEditor.setResourceColor(textObject.getColor());
		textEditor.setResourceAlign(textObject.getAlign());
		textEditor.setResourceFontName(textObject.getTypeFacePath());
		textEditor.setResourceSize(textObject.getSize());
		textEditor.setResourceText(textObject.getText());
		textEditor.setResoureCoordinate(textObject.getLeftCoordinate().x, textObject.getLeftCoordinate().y,
				textObject.getRightCoordinate().x, textObject.getRightCoordinate().y);
		textEditor.clearMotion();
		textEditor.setBaseResolution(textObject.getBaseResolution());
		String bgPath = textObject.getBackgourndPath();
		if(bgPath != null){
			ResourceType resourcetype = ResourceType.ANDROID_RESOURCE;
			if(bgPath.contains("/data")) resourcetype = ResourceType.FILE;
			textEditor.setBackground(bgPath, resourcetype);
		}
		ArrayList<TextMotions> motions = textObject.getMotions();
		for(int d = 0;d<motions.size();d++){
			textEditor.addMotion(motions.get(d).mMotionP, motions.get(d).mMotionDuration);
		}
		
		textEditor.setActiveTimeRange(textObject.getActiveStartRatio(), textObject.getActiveEndRatio());
		textEditor.setDrawOnlyWhileActiveTime(textObject.isDrawOnlyActvieRatio());
	}
	
	public void applyOldMovieEffect(Scene scene){
		Scene.Editor<?, ?> editor = scene.getEditor();
		SwayEffect.Editor swayEditor = editor.addEffect(SwayEffect.class).getEditor();
		NoiseEffect.Editor noiseEditor = editor.addEffect(NoiseEffect.class).getEditor();
		OverlayEffect.Editor overlayEditor = editor.addEffect(OverlayEffect.class).getEditor();
		overlayEditor.setImageDrawable(R.drawable.vignette_blur, Resolution.FHD);
	}
	
	public void applyScaleEffect(Scene scene, UplusScaleEffectData scaleEffectData, Theme theme){
		if(scene.getClass().equals(MultiLayerScene.class)){
			
			MultiLayerScene multiLayerScene = (MultiLayerScene)scene; 
			MultiLayerScene.Editor editor = multiLayerScene.getEditor();
			
			// 2 multiply default content duration, 2 multiply viewport.
			editor.setDuration(theme.contentDuration + theme.contentDuration);
			for(Scene multiScene : multiLayerScene.getLayers()){
				List<Viewport> viewportList = new ArrayList<Viewport>(); 
				LayerScene layerScene = (LayerScene)multiScene; 
				
				KenBurnsScaler scaler = (KenBurnsScaler)layerScene.getScaler(); 
				Viewport[] viewportArray = scaler.getViewports();
				viewportList.add(viewportArray[0]);
				viewportList.add(viewportArray[1]);
				viewportList.add(viewportArray[0]);
				
				KenBurnsScaler.Editor scalerEditor = scaler.getEditor(); 
				scalerEditor.setViewports(viewportList); 
				
				multiScene.getEditor().removeAllEffects(EnterEffect.class); 
			}
			
			int templateId = multiLayerScene.getTemplateId();
			ScaleEffect.Editor scaleEffectEditor = editor.addEffect(ScaleEffect.class, 0).getEditor(); 
			Viewport srcViewport = MultiLayerData.getScaleSourceViewport(templateId);
			Viewport dstViewport = new Viewport(0.0f,0.0f,1.0f,1.0f);
			scaleEffectEditor.setViewport(srcViewport, dstViewport);
			scaleEffectEditor.setActiveTimeRange(0.5f, 0.55f); 
			
			UserTag.setMaintainFeature(scene.getTagContainer(), ScaleEffect.JSON_VALUE_TYPE); 
		}
	}

	public void applyLightEffect(Scene scene, ArrayList<UplusLightEffectData> lightEffectDataList) {
		LightEffect.Editor editor = scene.getEditor().addEffect(LightEffect.class).getEditor();
		for(int i = 0; i < lightEffectDataList.size(); i++){
			UplusLightEffectData lightEffectData = lightEffectDataList.get(i); 
			editor.setResource(lightEffectData.getType(), lightEffectData.getColor(), lightEffectData.getScale(), lightEffectData.getStartX(), lightEffectData.getStartY(), lightEffectData.getEndX(), lightEffectData.getEndY(), lightEffectData.getStartAlpha(), lightEffectData.getEndAlpha(),lightEffectData.getResolution());
		}
	}

}
