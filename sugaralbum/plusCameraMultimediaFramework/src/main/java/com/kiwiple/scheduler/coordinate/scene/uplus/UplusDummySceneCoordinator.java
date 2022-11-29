package com.kiwiple.scheduler.coordinate.scene.uplus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.kiwiple.multimedia.canvas.AnimationEffect;
import com.kiwiple.multimedia.canvas.AnimationEffect.AnimationInfo;
import com.kiwiple.multimedia.canvas.DummyScene;
import com.kiwiple.multimedia.canvas.OverlayEffect;
import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.TextEffect;
import com.kiwiple.debug.L;
import com.kiwiple.scheduler.coordinate.effect.uplus.UplusEffectApplyManager;
import com.kiwiple.scheduler.coordinate.scene.DummySceneCoordinator;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.data.uplus.effect.UplusAnimationEffectData;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.FontAlign;
import com.kiwiple.scheduler.theme.Theme.Frame;
import com.kiwiple.scheduler.theme.Theme.FrameObject;
import com.kiwiple.scheduler.theme.Theme.FrameType;
import com.kiwiple.scheduler.theme.Theme.MotionType;
import com.kiwiple.scheduler.theme.Theme.ResourceType;
import com.kiwiple.scheduler.util.IntroOutroUtils;

public class UplusDummySceneCoordinator extends DummySceneCoordinator {
	private UplusOutputData mUplusOutputData;
	private Context mContext;

	public UplusDummySceneCoordinator() {
	}

	public UplusDummySceneCoordinator(Context context, OutputData outputData) {
		mContext = context;
		mUplusOutputData = (UplusOutputData) outputData;
	}

	/**
	 * frame, multi theme에서 사용되는 dummy scne을 추가한다.
	 * 
	 * @param regionEditor
	 *            : 편집중인 editor.
	 * @param frameType
	 *            : intro, outro, content로 구별.
	 * @param title
	 *            : scene의 title.
	 */
	public DummyScene addDummyScene(Region.Editor regionEditor, FrameType frameType, String title, boolean addTitle) {
		DummyScene dummyScene;
		DummyScene.Editor sceneEditor;
		if (frameType != null && FrameType.INTRO.equals(frameType)) {
			dummyScene = regionEditor.addScene(DummyScene.class, 0);
			sceneEditor = dummyScene.getEditor();
			setDummyScene(regionEditor, sceneEditor, frameType, title, addTitle);
		} else if (frameType != null && FrameType.OUTRO.equals(frameType)) {
			dummyScene = regionEditor.addScene(DummyScene.class);
			sceneEditor = dummyScene.getEditor();
			setDummyScene(regionEditor, sceneEditor, frameType, title, addTitle);
		} else {
			dummyScene = regionEditor.addScene(DummyScene.class);
			sceneEditor = dummyScene.getEditor();
			setLastOutroDummyScene(regionEditor, sceneEditor);
		}

		return dummyScene;
	}

	private void setLastOutroDummyScene(Editor regionEditor, DummyScene.Editor sceneEditor) {
		Theme theme = mUplusOutputData.getTheme();
		String packagePath = mContext.getFilesDir().getAbsolutePath();
		String endingLogFilePath = null;
		if (theme.resourceType == ResourceType.ASSET) {
			endingLogFilePath = new StringBuffer().append(packagePath).append(File.separator).append(theme.endingLogo).toString();
		} else if (theme.resourceType == ResourceType.DOWNLOAD) {
			endingLogFilePath = theme.combineDowloadImageFilePath(mContext, theme.endingLogo);
		}
		L.d("last ending image file path : " + endingLogFilePath);
		sceneEditor.setBackgroundFilePath(endingLogFilePath);
		sceneEditor.setDuration(theme.frontBackDuration);
	}

	/**
	 * frame, multi theme에서 사용되는 dummy scne에 대한 구성.
	 * 
	 * @param regionEditor
	 *            : 편집중인 editor.
	 * @param frameType
	 *            : intro, outro, content로 구별.
	 * @param title
	 *            : scene의 title.
	 */
	private void setDummyScene(Region.Editor regionEditor, DummyScene.Editor sceneEditor, FrameType frameType, String title, boolean addTitle) {
		Frame inoutroFrame = null;
		for (Frame frame : mUplusOutputData.getTheme().frameData) {
			if (frameType.equals(frame.frameType)) {
				inoutroFrame = frame;
			}
		}
		if (inoutroFrame == null) {
			throw new IllegalArgumentException("Intro or Outro frame is missing");
		}

		if (inoutroFrame.useUserImageBackground) {
			String mediaPath = null;
			long position = 0;
			if (FrameType.INTRO.equals(frameType)) {
				Scene sceneClass = regionEditor.getObject().getScene(1);
				mediaPath = IntroOutroUtils.getImage(sceneClass);
				position = IntroOutroUtils.getVideoStartPosition(sceneClass);
			} else {
				Region region = regionEditor.getObject();
				Scene sceneClass = region.getScene(region.getScenes().size() - 2);
				mediaPath = IntroOutroUtils.getImage(sceneClass);
				position = IntroOutroUtils.getVideoEndPosition(sceneClass);
			}
			if (!TextUtils.isEmpty(mediaPath)) {
				L.d("background path : " + mediaPath);
				sceneEditor.setBackgroundFilePath(mediaPath);
				sceneEditor.setVideoFramePosition(position);
			}
		}

		if (inoutroFrame.frameImageName != null) {
			OverlayEffect.Editor overlayEditor = sceneEditor.addEffect(OverlayEffect.class).getEditor();
			String overlayPath = mUplusOutputData.getTheme().combineDowloadImageFilePath(mContext, inoutroFrame.frameImageName, "png");
			L.d("overlay effect path : " + overlayPath);
			overlayEditor.setImageFile(overlayPath, Resolution.FHD);
		}

		if (addTitle && inoutroFrame.fontSize > 0) {
			/**
			 * aubergine : 테마영역값은 FHD기준으로 전달받는다. TextEffect는 NHD를 기준 해상도록 처리되도록 변경되어서 multiplier를
			 * 추가함.
			 */
			TextEffect.Editor textEffect = sceneEditor.addEffect(TextEffect.class).getEditor();
			textEffect.setResourceAlign(inoutroFrame.fontAlign == FontAlign.CENTER ? TextEffect.JSON_VALUE_ALIGN_CENTER : (inoutroFrame.fontAlign == FontAlign.RIGHT ? TextEffect.JSON_VALUE_ALIGN_RIGHT : TextEffect.JSON_VALUE_ALIGN_LEFT));
			textEffect.setResourceColor(inoutroFrame.fontColor);
			textEffect.setResourceFontName("DroidSans"/* inoutroFrame.fontName */);
			textEffect.setResourceSize(inoutroFrame.fontSize);
			textEffect.setResoureCoordinate(inoutroFrame.fontCoordinateLeft, inoutroFrame.fontCoordinateTop, inoutroFrame.fontCoordinateRight, inoutroFrame.fontCoordinateBottom);
			textEffect.setResourceText(title);
			textEffect.setBaseResolution(Resolution.FHD);
		}

		if (inoutroFrame.objects != null) {
			UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
			effectApplyManager.applyFrameEffect(mContext, mUplusOutputData, inoutroFrame.objects, sceneEditor);
		}

		sceneEditor.setDuration(mUplusOutputData.getTheme().frontBackDuration);
	}
}
