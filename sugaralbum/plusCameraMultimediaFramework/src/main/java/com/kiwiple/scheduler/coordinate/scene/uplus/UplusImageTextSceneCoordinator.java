package com.kiwiple.scheduler.coordinate.scene.uplus;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;

import com.kiwiple.multimedia.canvas.ImageTextScene;
import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.data.TextElement;
import com.kiwiple.scheduler.coordinate.scene.ImageTextSceneCoordinator;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.theme.Theme.FrameType;
import com.kiwiple.scheduler.util.IntroOutroUtils;

public class UplusImageTextSceneCoordinator extends ImageTextSceneCoordinator {

	public static final String UPLUS_VALUE_TEXT_ELEMENT_DEFAULT_TEXT = "";
	public static final float UPLUS_VALUE_TEXT_ELEMENT_STORY_DEFAULT_SIZE = 18.0f;
	public static final float UPLUS_VALUE_TEXT_ELEMENT_DEFAULT_SIZE = 24.0f;
	public static final int UPLUS_VALUE_SCENE_TEXT_DEFAULT_LINE_SPACE = 20;

	private UplusOutputData mUplusOutputData;
	private Context mContext;

	public UplusImageTextSceneCoordinator() {
	}

	public UplusImageTextSceneCoordinator(Context context, OutputData outputData) {
		mContext = context;
		mUplusOutputData = (UplusOutputData) outputData;
	}

	/**
	 * filter theme에서 사용되는 image text scne에 대한 구성.
	 * 
	 * @param regionEditor
	 *            : 편집중인 editor.
	 * @param frameType
	 *            : intro, outro, content로 구별.
	 * @param textElementList
	 *            : scene에 들어갈 title과 content.
	 */
	public void addImageTextScene(Region.Editor regionEditor, FrameType frameType, List<TextElement> textElementList, boolean addTitle) {
		ImageTextScene.Editor sceneEditor = regionEditor.addScene(ImageTextScene.class).getEditor();
		setImageTextScene(sceneEditor, frameType, regionEditor, textElementList, addTitle);
	}

	/**
	 * 후보정에서 filter theme에서 사용되는 image text scne에 대한 추가.
	 * 
	 * @param regionEditor
	 *            : 편집중인 editor.
	 * @param frameType
	 *            : intro, outro, content로 구별.
	 * @param textElementList
	 *            : scene에 들어갈 title과 content.
	 * @param duration
	 *            : scene의 길이.
	 * @param index
	 *            : 삽일될 위치.
	 */
	public ImageTextScene insertImageTextScene(Editor regionEditor, FrameType frameType, List<TextElement> textElementList, int index, boolean addTitle) {
		ImageTextScene imageTextScene = regionEditor.addScene(ImageTextScene.class, index);
		ImageTextScene.Editor sceneEditor = imageTextScene.getEditor();
		setImageTextScene(sceneEditor, frameType, regionEditor, textElementList, addTitle);
		return imageTextScene;
	}

	/**
	 * string data를 가지고 textElement를 생성.
	 * 
	 * @param date
	 *            : string형 날짜 데이터.
	 * @return : TextElement list.
	 */
	public List<TextElement> getTextElementList(String date) {
		List<TextElement> textElementList = new ArrayList<TextElement>();
		textElementList.add(makeTextElement(UPLUS_VALUE_TEXT_ELEMENT_DEFAULT_TEXT, UPLUS_VALUE_TEXT_ELEMENT_STORY_DEFAULT_SIZE, Color.WHITE));
		textElementList.add(makeTextElement(date, UPLUS_VALUE_TEXT_ELEMENT_DEFAULT_SIZE, Color.WHITE));
		return textElementList;
	}

	public List<TextElement> getEmptyTextElementList() {
		List<TextElement> textElementList = new ArrayList<TextElement>();
		textElementList.add(makeTextElement("", UPLUS_VALUE_TEXT_ELEMENT_DEFAULT_SIZE, Color.WHITE));
		return textElementList;
	}

	/**
	 * TextElement를 생성.
	 * 
	 * @param text
	 *            : TextElement의 text.
	 * @param size
	 *            : TextElement의 size.
	 * @param color
	 *            : TextElement의 color
	 * @return : TextElement.
	 */
	private TextElement makeTextElement(String text, float size, int color) {
		return new TextElement(text, size, color);
	}

	/**
	 * ImageText scene editor를 통해서 세팅하기.
	 * 
	 * @param sceneEditor
	 *            : 편집중인 scene editor.
	 * @param frameType
	 *            : intro, outro, content로 구별.
	 * @param regionEditor
	 *            편집중인 region editor.
	 * @param textElementList
	 *            : scene에 들어갈 title과 content.
	 * @param duration
	 *            : scene의 길이.
	 */

	private void setImageTextScene(ImageTextScene.Editor sceneEditor, FrameType frameType, Region.Editor regionEditor, List<TextElement> textElementList, boolean addTitle) {

		if (addTitle) {
			sceneEditor.setTextElements(textElementList);
		} else {
			sceneEditor.setTextElements(getEmptyTextElementList());
		}

		sceneEditor.setLineSpace(UPLUS_VALUE_SCENE_TEXT_DEFAULT_LINE_SPACE);
		sceneEditor.setDuration(mUplusOutputData.getTheme().frontBackDuration);

		String mediaPath = null;
		long position = 0;
		if (FrameType.INTRO.equals(frameType)) {
			Scene sceneClass = regionEditor.getObject().getScene(1);
			mediaPath = IntroOutroUtils.getImage(sceneClass);
			position = IntroOutroUtils.getVideoStartPosition(sceneClass);
		} else {
			Region region = regionEditor.getObject();
			Scene sceneClass = null;
			if (mUplusOutputData.getTheme().name.equalsIgnoreCase("Mix up")) {
				sceneClass = region.getScene(region.getScenes().size() - 3);
			} else {
				sceneClass = region.getScene(region.getScenes().size() - 2);
			}
			mediaPath = IntroOutroUtils.getImage(sceneClass);
			position = IntroOutroUtils.getVideoEndPosition(sceneClass);
		}
		if (!TextUtils.isEmpty(mediaPath)) {
			sceneEditor.setBackgroundFilePath(mediaPath);
			sceneEditor.setVideoFramePosition(position);
		}
	}
}
