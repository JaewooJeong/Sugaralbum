package com.kiwiple.scheduler.coordinate.scene.uplus;

import java.io.File;

import android.content.Context;
import android.text.TextUtils;

import com.kiwiple.debug.L;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.ImageResource;
import com.kiwiple.multimedia.canvas.ImageResource.ScaleType;
import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.theme.Theme.ResourceType;

public class UplusEndingLogoSceneCoordinator extends UplusExtraSceneCoordinator {

	public static final String TAG_JSON_VALE_SCENE_ENDING = "tag_scene_ending";

	public UplusEndingLogoSceneCoordinator(Context context, OutputData outputData) {
		super(context, outputData);
	}

	public void addEndingLogoScene(Region.Editor regionEditor) {
		L.d("add endging logo scene"); 
		ImageFileScene scene = regionEditor.addScene(ImageFileScene.class);
		ImageFileScene.Editor editor = scene.getEditor();

		String packagePath = mContext.getFilesDir().getAbsolutePath();
		String endingLogFilePath = null;
		if (mTheme.resourceType == ResourceType.ASSET) {
			endingLogFilePath = new StringBuffer().append(packagePath).append(File.separator).append(mTheme.endingLogo).toString();
		} else if (mTheme.resourceType == ResourceType.DOWNLOAD) {
			endingLogFilePath = mTheme.combineDowloadImageFilePath(mContext, mTheme.endingLogo);
		}
		L.d("last ending image file path : " + endingLogFilePath);
		if (!TextUtils.isEmpty(endingLogFilePath)) {
			editor.setImageResource(ImageResource.createFromFile(endingLogFilePath, ScaleType.BUFFER));
			editor.setDuration(mTheme.frontBackDuration);
		}
		editor.setDuration(mTheme.frontBackDuration);
		setDefaultKenburn(editor);
		setTag(scene, null, TAG_JSON_VALE_SCENE_ENDING);
	}

}
