package com.kiwiple.scheduler.coordinate.scene.uplus;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.kiwiple.debug.L;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.multimedia.Constants;
import com.kiwiple.multimedia.canvas.BurstShotScene;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.NoiseEffect;
import com.kiwiple.multimedia.canvas.OverlayEffect;
import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.multimedia.canvas.SwayEffect;
import com.kiwiple.scheduler.R;
import com.kiwiple.scheduler.coordinate.effect.uplus.UplusEffectApplyManager;
import com.kiwiple.scheduler.coordinate.scene.BurstShotSceneCoordinator;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.data.SelectedOutputData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.Frame;
import com.kiwiple.scheduler.theme.Theme.FrameType;
import com.kiwiple.scheduler.theme.Theme.ThemeType;

public class UplusBurstShotSceneCoordinator extends BurstShotSceneCoordinator {

	private UplusOutputData mUplusOutputData;

	private Context mContext;

	private boolean mIsFromEdit;
	private List<Frame> mFrames;

	public UplusBurstShotSceneCoordinator(Context context, OutputData outputData) {
		mIsFromEdit = false;
		mContext = context;
		mUplusOutputData = (UplusOutputData) outputData;
		mFrames = new ArrayList<Frame>();
	}

	/**
	 * 새로운 BurstShot scene을 추가.<br>
	 * 
	 * @param regionEditor
	 *            : 현재 편집되고 있는 region editor.<br>
	 * @param selectedOutputData
	 *            : 삽입될 data.<br>
	 */
	public void addBurstShotScene(Editor regionEditor, SelectedOutputData selectedOutputData) {
		mIsFromEdit = false;
		BurstShotScene scene = regionEditor.addScene(BurstShotScene.class);
		BurstShotScene.Editor sceneEditor = scene.getEditor();

		Theme theme = mUplusOutputData.getTheme();
		ThemeType themeType = theme.themeType;

		if (themeType == ThemeType.FILTER) {
			sceneEditor.setFilterId(mUplusOutputData.getTheme().filterId);
		} else {

			sceneEditor.setFilterId(Constants.INVALID_FILTER_ID);
		}

		if (themeType != ThemeType.FILTER) {
			setFrameEffect(sceneEditor);
		}

		ArrayList<ImageData> imageDatas = selectedOutputData.getImageDatas();
		ArrayList<String> imageFileNames = new ArrayList<String>();
		ArrayList<Integer> imageIds = new ArrayList<Integer>();

		for (int i = 0; i < imageDatas.size(); i++) {
			imageFileNames.add(imageDatas.get(i).path);
			imageIds.add(imageDatas.get(i).id);
		}

		sceneEditor.setImageFilePath(imageFileNames);
		sceneEditor.setImageId(imageIds);

		sceneEditor.setDuration(mUplusOutputData.getTheme().contentDuration); // TODO duration 검토.

		setTag(scene, null, OutputData.TAG_JSON_VALE_SCENE_CONTENT);

	}

	/**
	 * 후보정에서 새로운 BurstShot scene을 추가.<br>
	 * 
	 * @param regionEditor
	 *            : 현재 편집되고 있는 region editor.<br>
	 * @param imageDataList
	 *            : 삽입될 data.<br>
	 * @param scenePosition
	 *            : 삽입될 data position.<br>
	 */
	public void updateBurstShotScene(Region.Editor regionEditor, ArrayList<ImageData> imageDataList, int scenePosition) {

		mIsFromEdit = true;
		BurstShotScene scene = regionEditor.addScene(BurstShotScene.class, scenePosition);
		BurstShotScene.Editor sceneEditor = scene.getEditor();

		int index = 0;

		int templateId = imageDataList.get(0).imageCorrectData.collageTempletId;
		sceneEditor.setDuration(mUplusOutputData.getTheme().contentDuration);
		editBurstShotScene(sceneEditor, templateId, imageDataList);
		
		setTag(scene, null, OutputData.TAG_JSON_VALE_SCENE_CONTENT);
	}

	/**
	 * 현재의 테마에 맞게 scene을 구성한다. <br>
	 * 
	 * @param multiScneEditor
	 *            : 현재 편집되고 있는 scene editor.<br>
	 * @param templateId
	 *            : 멀티 레이아웃 template id. <br>
	 * @param imageDataList
	 *            : 삽입될 data.<br>
	 */
	private void editBurstShotScene(BurstShotScene.Editor sceneEditor, int templateId, ArrayList<ImageData> imageDatas) {

		ThemeType themeType = mUplusOutputData.getTheme().themeType;

		if (themeType == ThemeType.FILTER) {
			sceneEditor.setFilterId(mUplusOutputData.getTheme().filterId);
		} else {

			sceneEditor.setFilterId(Constants.INVALID_FILTER_ID);
		}

		ArrayList<String> imageFileNames = new ArrayList<String>();
		ArrayList<Integer> imageIds = new ArrayList<Integer>();

		for (int i = 0; i < imageDatas.size(); i++) {
			imageFileNames.add(imageDatas.get(i).path);
			imageIds.add(imageDatas.get(i).id);
		}

		sceneEditor.setImageFilePath(imageFileNames);
		sceneEditor.setImageId(imageIds);
		// if (themeName.equals(Theme.THEME_NAME_OLDMOVIE)) {
		// setOldMovieEffect(sceneEditor);
		// }

		sceneEditor.setDuration(mUplusOutputData.getTheme().contentDuration);

	}

	/**
	 * 현재 테마에 맞게 각각의 frame에 effect가 있다면 sceneEditor에 설정한다.<br>
	 * 
	 * @param sceneEditor
	 *            : 현재 편집되고 있는 scene editor.<br>
	 */
	private void setFrameEffect(BurstShotScene.Editor sceneEditor) {

		Frame frame = null;
		String overlayPath = null;

		if (mUplusOutputData.getFrames() == null) {
			L.d("Frame is null");
			return;
		}
		for (Frame _frame : mUplusOutputData.getFrames()) {
			if (_frame.frameCount == 1 && _frame.frameType == FrameType.CONTENT) {
				mFrames.add(_frame);
			}
		}

		if (mFrames == null || mFrames.isEmpty()) {
			return;
		}

		frame = mFrames.get((int) (Math.random() * mFrames.size()));
		L.d("frame type : " + frame.frameType + ", count : " + frame.frameCount);

		if (frame.frameImageName != null) {
			OverlayEffect.Editor overlayEditor = sceneEditor.addEffect(OverlayEffect.class).getEditor();
			overlayPath = mUplusOutputData.getTheme().combineDowloadImageFilePath(mContext, frame.frameImageName, "png");
			L.d("frame name " + frame.frameImageName + ", overlay effect path : " + overlayPath);
			overlayEditor.setImageFile(overlayPath, Resolution.FHD);
		} else {
			L.d("frame name is null");
		}

		if (frame.objects != null) {
			UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
			effectApplyManager.applyFrameEffect(mContext, mUplusOutputData, frame.objects, sceneEditor);
		} else {
			L.d("Object is null");
		}
	}
}
