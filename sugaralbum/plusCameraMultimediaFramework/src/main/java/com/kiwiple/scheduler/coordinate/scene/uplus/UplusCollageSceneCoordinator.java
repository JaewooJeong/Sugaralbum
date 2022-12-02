package com.kiwiple.scheduler.coordinate.scene.uplus;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.kiwiple.debug.L;
import com.kiwiple.imageanalysis.database.FacePointF;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.multimedia.Constants;
import com.kiwiple.multimedia.canvas.CollageScene;
import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.multimedia.canvas.data.CollageElement;
import com.kiwiple.scheduler.coordinate.effect.uplus.UplusEffectApplyManager;
import com.kiwiple.scheduler.coordinate.scene.CollageSceneCoordinator;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.data.SelectedOutputData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.theme.Theme.Frame;

public class UplusCollageSceneCoordinator extends CollageSceneCoordinator {
	private UplusOutputData mUplusOutputData;

	private List<Frame> mFrames;
	private Context mContext;

	public UplusCollageSceneCoordinator() {
	}

	public UplusCollageSceneCoordinator(Context context, OutputData outputData) {
		mContext = context;
		mUplusOutputData = (UplusOutputData) outputData;
		mFrames = new ArrayList<Frame>();

	}

	/**
	 * 현재 테마의 각각의 frame에 effect가 있다면 sceneEditor에 설정한다.<br>
	 * 
	 * @param sceneEditor
	 *            : 현재 편집되고 있는 scene editor.<br>
	 * @param templatId
	 *            : collage template id. <br>
	 */
	private void setFrameEffect(CollageScene.Editor sceneEditor, int templatId) {

		Frame frame = null;
		if (mUplusOutputData.getFrames() == null) {
			L.d("Frame is null");
			return;
		}
		for (Frame _frame : mUplusOutputData.getFrames()) {
			if (_frame.id == templatId) {
				frame = _frame;
			}
		}
		L.d("frame type : " + frame.frameType + ", count : " + frame.frameCount);

		if (frame.objects != null) {
			UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
			effectApplyManager.applyFrameEffect(mContext, mUplusOutputData, frame.objects, sceneEditor);
		}
	}
	/**
	 * 새로운 collage scene을 추가.<br>
	 * 
	 * @param regionEditor
	 *            : 현재 편집되고 있는 region editor.<br>
	 * @param selectedOutputData
	 *            : 삽입될 data.<br>
	 */
	public void addCollageScene(Editor regionEditor, SelectedOutputData selectedOutputData) {
		CollageScene scene = regionEditor.addScene(CollageScene.class);
		CollageScene.Editor sceneEditor = scene.getEditor();
		setCollageScene(sceneEditor, selectedOutputData);
		
		setTag(scene, selectedOutputData, OutputData.TAG_JSON_VALE_SCENE_CONTENT);
	}

	/**
	 * 후보정시에 collage scene을 추가.<br>
	 * 
	 * @param regionEditor
	 *            : 현재 편집되고 있는 region editor.<br>
	 * @param imageDataList
	 *            : 삽입될 data.<br>
	 * @param index
	 *            : 삽입될 위치. <br>
	 */
	public void insertCollageScene(Region.Editor regionEditor, ArrayList<ImageData> imageDataList, int index) {
		CollageScene scene = regionEditor.addScene(CollageScene.class, index);
		CollageScene.Editor sceneEditor = scene.getEditor();
		setCollageScene(sceneEditor, imageDataList);
		setTag(scene, null, OutputData.TAG_JSON_VALE_SCENE_CONTENT);
	}

	/**
	 * Collage의 imageData를 CollageElement로 변환.
	 * 
	 * @param imageData
	 *            : 현재 사용되고 있는 data. <br>
	 * @return collageElement. <br>
	 */
	public CollageElement createCollageData(ImageData imageData) {
		CollageElement collageData = new CollageElement();

		collageData.id = imageData.id;
		collageData.width = imageData.width;
		collageData.height = imageData.height;
		collageData.orientation = imageData.orientation;
		collageData.path = imageData.path;

		collageData.imageCorrectData.collageTempletId = imageData.imageCorrectData.collageTempletId;
		L.d("collage templet id : " + imageData.imageCorrectData.collageTempletId);
		collageData.imageCorrectData.collageCoordinate = new FacePointF(imageData.imageCorrectData.collageCoordinate.x, imageData.imageCorrectData.collageCoordinate.y);
		collageData.imageCorrectData.collageRotate = imageData.imageCorrectData.collageRotate;
		collageData.imageCorrectData.collageScale = imageData.imageCorrectData.collageScale;

		collageData.imageCorrectData.collageWidth = imageData.imageCorrectData.collageWidth;
		collageData.imageCorrectData.collageHeight = imageData.imageCorrectData.collageHeight;
		collageData.imageCorrectData.collageFrameBorderWidth = imageData.imageCorrectData.collageFrameBorderWidth;
		collageData.imageCorrectData.collageFrameCornerRadius = imageData.imageCorrectData.collageFrameCornerRadius;
		collageData.imageCorrectData.collageBackgroundColor = imageData.imageCorrectData.collageBackgroundColor;
		collageData.imageCorrectData.collageBackgroundColorTag = imageData.imageCorrectData.collageBackgroundColorTag;
		collageData.imageCorrectData.collageBackgroundImageFileName = imageData.imageCorrectData.collageBackgroundImageFileName;
		return collageData;
	}

	/**
	 * 현재의 테마에 맞게 collage scene을 구성한다.
	 * 
	 * @param sceneEditor
	 *            : 현재 편집되는 scene editor. <br>
	 * @param imageDataList
	 *            : 삽입되는 data. <br>
	 */
	private void setCollageScene(CollageScene.Editor sceneEditor, ArrayList<ImageData> imageDataList) {
		List<CollageElement> collageDataList = new ArrayList<CollageElement>(imageDataList.size());
		for (ImageData imageData : imageDataList) {
			collageDataList.add(createCollageData(imageData));
		}
		sceneEditor.setCollageElements(collageDataList);
		sceneEditor.setFilterId(Constants.INVALID_FILTER_ID);
		setFrameEffect(sceneEditor, imageDataList.get(0).imageCorrectData.collageTempletId);
		sceneEditor.setDuration(mUplusOutputData.getTheme().contentDuration);
	}

	/**
	 * 현재의 테마에 맞게 collage scene을 구성한다.
	 * 
	 * @param sceneEditor
	 *            : 현재 편집되는 scene editor. <br>
	 * @param selectedOutputData
	 *            : 삽입되는 data. <br>
	 */
	private void setCollageScene(CollageScene.Editor sceneEditor, SelectedOutputData selectedOutputData) {
		setCollageScene(sceneEditor, selectedOutputData.getImageDatas());
	}
}
