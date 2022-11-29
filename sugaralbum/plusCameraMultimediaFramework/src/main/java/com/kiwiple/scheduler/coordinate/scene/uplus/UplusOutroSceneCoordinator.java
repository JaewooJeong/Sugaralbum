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
import com.kiwiple.multimedia.canvas.ImageResource.ScaleType;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.theme.Theme.Frame;
import com.kiwiple.scheduler.theme.Theme.FrameType;
import com.kiwiple.scheduler.util.ImageUtil;
import com.kiwiple.scheduler.util.IntroOutroUtils;

public class UplusOutroSceneCoordinator extends UplusExtraSceneCoordinator {

	public static final String TAG_JSON_VALE_SCENE_OUTRO = "tag_scene_outro";

	public UplusOutroSceneCoordinator(Context context, OutputData outputData) {
		super(context, outputData);
	}

	public void addOutroScene(Region region, String title) {
		L.d("add outro scene"); 
		
		Region.Editor regionEditor = region.getEditor();
		ImageFileScene scene = regionEditor.addScene(ImageFileScene.class);
		ImageFileScene.Editor editor = scene.getEditor();

		Scene lastScene = null;
		if (mTheme.isUseColloectionScene) {
			lastScene = regionEditor.getObject().getScene(region.getScenes().size() - 3);
		} else {
			lastScene = regionEditor.getObject().getScene(region.getScenes().size() - 2);
		}
		String mediaPath = IntroOutroUtils.getImage(lastScene);
		long videoPosition = IntroOutroUtils.getVideoEndPosition(lastScene);

		Frame outroFrame = null;
		for (Frame frame : mTheme.frameData) {
			if (FrameType.OUTRO.equals(frame.frameType)) {
				outroFrame = frame;
			}
		}
		
		JSONObject dynamicOutroObject = mTheme.getDynamicOutroJson();
		boolean addTitle = dynamicOutroObject == null ? true : false;

		if(dynamicOutroObject == null){
			if (mTheme.hasIntro()) {
				// intro frame을 가진 테마.
				setFrameScene(scene, editor, addTitle, outroFrame, mediaPath, videoPosition, title);
	
			} else {
				// intro frame에 지정된 object image를 사용하고, intro scene은 첫번째 scene의 이미지로 구성.
				setDefaultScene(lastScene, editor, addTitle, mediaPath, videoPosition, title);
			}
		}else{
			try {
				if(dynamicOutroObject.getBoolean("use_default_image")){ 
					String path = dynamicOutroObject.getString("default_image");
					mediaPath = mTheme.combineDowloadImageFilePath(mContext, path);
				}
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (!TextUtils.isEmpty(mediaPath)) {
				if(ImageUtil.isVideoFile(mediaPath)){
					editor.setImageResource(ImageResource.createFromVideoFile(mediaPath, ScaleType.BUFFER, (int)videoPosition));
				}else{
					editor.setImageResource(ImageResource.createFromFile(mediaPath, ScaleType.BUFFER));
				}
			}
			
			setDynamicEffect(scene, mTheme.getDynamicOutroJson(), title);
		}
		
		editor.setDuration(mTheme.frontBackDuration);
		setDefaultKenburn(editor);

		setTag(scene, null, TAG_JSON_VALE_SCENE_OUTRO);
	}

}
