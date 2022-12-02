package com.kiwiple.scheduler.coordinate.scene.uplus;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;

import com.kiwiple.debug.L;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.ImageResource;
import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.TextEffect;
import com.kiwiple.multimedia.canvas.ImageResource.ScaleType;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.Frame;
import com.kiwiple.scheduler.theme.Theme.FrameType;
import com.kiwiple.scheduler.util.ImageUtil;
import com.kiwiple.scheduler.util.IntroOutroUtils;

public class UplusIntroSceneCoordinator extends UplusExtraSceneCoordinator {

	public static final String TAG_JSON_VALE_SCENE_INTRO = "tag_scene_intro";

	public UplusIntroSceneCoordinator(Context context, OutputData outputData) {
		super(context, outputData);
		// TODO Auto-generated constructor stub
	}

	public void addIntroScene(Region region, String title, Theme theme) {
		L.d("add intro scene");
		
		Region.Editor regionEditor = region.getEditor();
		ImageFileScene scene = regionEditor.addScene(ImageFileScene.class, 0);
		ImageFileScene.Editor editor = scene.getEditor();
		Scene firstScene = regionEditor.getObject().getScene(1);
		String mediaPath = IntroOutroUtils.getImage(firstScene);
		long videoPosition = IntroOutroUtils.getVideoStartPosition(firstScene);

		Frame introFrame = null;
		for (Frame frame : mTheme.frameData) {
			if (FrameType.INTRO.equals(frame.frameType)) {
				introFrame = frame;
			}
		}
		
		JSONObject dynamicIntroObject = theme.getDynamicIntroJson(); 
		boolean addTitle = theme.getDynamicIntroJson() == null ? true : false; 

		if(dynamicIntroObject == null){
			if (mTheme.hasIntro()) {
				// intro frame을 가진 테마.
				setFrameScene(scene, editor, addTitle, introFrame, mediaPath, videoPosition, title);
	
			} else {
				// intro frame에 지정된 object image를 사용하고, intro scene은 첫번째 scene의 이미지로 구성.
				setDefaultScene(firstScene, editor, addTitle, mediaPath, videoPosition, title);
			}
		}else{
			
			if (!TextUtils.isEmpty(mediaPath)) {
				if(ImageUtil.isVideoFile(mediaPath)){
					editor.setImageResource(ImageResource.createFromVideoFile(mediaPath, ScaleType.BUFFER, (int)videoPosition));
				}else{
					editor.setImageResource(ImageResource.createFromFile(mediaPath, ScaleType.BUFFER));
				}
			}
			
			setDynamicEffect(scene, theme.getDynamicIntroJson(), title);
			
			try {
				if(dynamicIntroObject.getBoolean("use_title")){
					JSONObject titleTextObject = dynamicIntroObject.getJSONObject("title_text"); 
					titleTextObject.put(TextEffect.JSON_NAME_TEXT, title);
					applyTextffect(titleTextObject, scene); 
					
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		editor.setDuration(mTheme.frontBackDuration);

		if (dynamicIntroObject != null) {
			setDynamicKenburn(editor,firstScene);
		}
		else if (mTheme.hasIntro() && !mTheme.isIntroSceneWithUserImage()) {
			setDefaultKenburn(editor);
		}
		else {
			setCenterKenburn(editor,firstScene);
		}
		
		setTag(scene, null, TAG_JSON_VALE_SCENE_INTRO);
	}

}
