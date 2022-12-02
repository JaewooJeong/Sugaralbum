package com.kiwiple.scheduler.coordinate.scene.uplus;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.json.JSONException;

import android.content.Context;

import com.kiwiple.debug.L;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.multimedia.Constants;
import com.kiwiple.multimedia.canvas.ImageFileScene;
import com.kiwiple.multimedia.canvas.ImageResource;
import com.kiwiple.multimedia.canvas.ImageResource.ScaleType;
import com.kiwiple.multimedia.canvas.KenBurnsScaler;
import com.kiwiple.multimedia.canvas.KenBurnsScaler.Editor;
import com.kiwiple.multimedia.canvas.NoiseEffect;
import com.kiwiple.multimedia.canvas.OverlayEffect;
import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.SwayEffect;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.math.interpolator.InterpolatorType;
import com.kiwiple.scheduler.R;
import com.kiwiple.scheduler.coordinate.effect.uplus.UplusEffectApplyManager;
import com.kiwiple.scheduler.coordinate.scaler.KenBurnsScalerCoordinator.KenburnDirection;
import com.kiwiple.scheduler.coordinate.scaler.uplus.UplusKenBurnsScalerCoordinator;
import com.kiwiple.scheduler.coordinate.scene.ImageFileSceneCoordinator;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.data.SelectedOutputData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.tag.UserTag;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.Frame;
import com.kiwiple.scheduler.theme.Theme.FrameType;
import com.kiwiple.scheduler.theme.Theme.ThemeType;

public class UplusImageFileSceneCoordinator extends ImageFileSceneCoordinator {
	public static final int SINGLE_FRAME_ID = 10000;
	public static final int ACCELERATION_ZOOM_ADD_DURATION = 2000;
	private UplusOutputData mUplusOutputData;
	private UplusKenBurnsScalerCoordinator mUplusKenBurnsScalerCoordinator;
	public static final int FRAME_COUNT_SINGLE = 1;
	public static final int FRAME_COUNT_NONE = 0;
	private List<Frame> mFrames;
	private Context mContext;
	private SelectedOutputData mSelectedOutputData;

	public UplusImageFileSceneCoordinator(Context context, OutputData outputData) {
		mContext = context;
		mUplusOutputData = (UplusOutputData) outputData;
		mUplusKenBurnsScalerCoordinator = new UplusKenBurnsScalerCoordinator();

		mFrames = new ArrayList<Frame>();

		mSelectedOutputData = null;

	}

	/**
	 * 새로운 single scene을 추가.<br>
	 * 
	 * @param regionEditor
	 *            : 현재 편집되고 있는 region editor.<br>
	 * @param selectedOutputData
	 *            : 삽입될 data.<br>
	 */
	public void addImageFileScene(Region.Editor regionEditor, SelectedOutputData selectedOutputData) {
		mSelectedOutputData = selectedOutputData;
		ImageFileScene imageFileScene = regionEditor.addScene(ImageFileScene.class);
		ImageFileScene.Editor sceneEditor = imageFileScene.getEditor();
		setImageFileScene(sceneEditor, selectedOutputData, false);
		
		setTag(imageFileScene, selectedOutputData, OutputData.TAG_JSON_VALE_SCENE_CONTENT);
	}

	/**
	 * 후편집에서 새로운 single scene을 추가.<br>
	 * 
	 * @param regionEditor
	 *            : 현재 편집되고 있는 region editor.<br>
	 * @param imageData
	 *            : 삽입될 data.<br>
	 * @param index
	 *            : 삽입될 위치.<br>
	 */
	public void insertImageFileScene(Region.Editor regionEditor, ImageData imageData, int index) {
		ImageFileScene imageFileScene = regionEditor.addScene(ImageFileScene.class, index);
		ImageFileScene.Editor sceneEditor = imageFileScene.getEditor();
		SelectedOutputData selectedOutputData = new SelectedOutputData(ImageFileScene.JSON_VALUE_TYPE, mUplusOutputData.getTheme().filterId, UplusImageFileSceneCoordinator.SINGLE_FRAME_ID);
		selectedOutputData.setAccelerationZoom(false);
		ArrayList<ImageData> images = new ArrayList<ImageData>();
		images.add(imageData);
		selectedOutputData.setImageDatas(images);
		selectedOutputData.setDate(imageData.date);

		mSelectedOutputData = selectedOutputData;
		setImageFileScene(sceneEditor, selectedOutputData, mUplusOutputData.getTheme().isDesignTheme());

		setTag(imageFileScene, selectedOutputData, OutputData.TAG_JSON_VALE_SCENE_CONTENT);

	}

	/**
	 * 후편집에서 single scene에 대해서 update할 경우, kenburn의 위치를 설정한다.<br>
	 * 
	 * @param regionEditor
	 *            : 현재 편집되고 있는 region editor.<br>
	 * @param imageData
	 *            : update된 viewports <br>
	 */
	public void updateImageFileScene(ImageFileScene.Editor imageFileSceneEditor, ImageData imageData, Viewport[] viewports, boolean updateEndPoint) {
		SelectedOutputData selectedOutputData = new SelectedOutputData(ImageFileScene.JSON_VALUE_TYPE, mUplusOutputData.getTheme().filterId, UplusImageFileSceneCoordinator.SINGLE_FRAME_ID);
		KenBurnsScaler.Editor kenburnScaler = (Editor) imageFileSceneEditor.getObject().getScaler().getEditor();

		boolean isInterpolator = false;
		if (kenburnScaler.getObject().getInterpolators().length > 0) {
			isInterpolator = true;
			selectedOutputData.setAccelerationZoom(true);
		} else {
			isInterpolator = false;
			selectedOutputData.setAccelerationZoom(false);
		}

		if (updateEndPoint) {
			setKenBurnsScaler(imageFileSceneEditor, viewports, isInterpolator);
		} else {
			Viewport[] newViewports = kenburnScaler.getObject().getViewports();
			newViewports[0] = viewports[0];
			setKenBurnsScaler(imageFileSceneEditor, newViewports, isInterpolator);
		}
		mSelectedOutputData = selectedOutputData;

	}

	/**
	 * 현재의 테마에 맞게 single scene을 구성한다.
	 * 
	 * @param sceneEditor
	 *            : 현재 편집되는 scene editor. <br>
	 * @param selectedOutputData
	 *            : 삽입되는 data. <br>
	 */
	private void setImageFileScene(ImageFileScene.Editor sceneEditor, SelectedOutputData selectedOutputData, boolean isFixedFrame) {

		int duration = mUplusOutputData.getTheme().contentDuration;
		L.d("setImageFileScene, duration : " + duration);
		ThemeType themeType = mUplusOutputData.getTheme().themeType;
		boolean isAccelerationZoom = selectedOutputData.getAccelerationZoom();

		ImageData imageData = selectedOutputData.getImageDatas().get(0);

		// if (imageData.imageCorrectData.stickerCorrectDataArray != null) {
		// sceneEditor.setStickerElements(getStickerInfos(imageData));
		// }

		if (themeType == ThemeType.FILTER) {
			sceneEditor.setFilterId(mUplusOutputData.getTheme().filterId);
		} else {
			sceneEditor.setFilterId(Constants.INVALID_FILTER_ID);
		}

		L.d("template id : " + imageData.imageCorrectData.collageTempletId);

		if (themeType != ThemeType.FILTER) { // love,
			if (isFixedFrame) {
				setFrameEffect(sceneEditor, imageData.imageCorrectData.collageTempletId);
			} else {
				setFrameEffect(sceneEditor);
			}
		}

		if (selectedOutputData.getAccelerationZoom()) {
			sceneEditor.setDuration(duration + ACCELERATION_ZOOM_ADD_DURATION);
		} else {
			sceneEditor.setDuration(duration);
		}
		ArrayList<ArrayList<Viewport>> viewportLists = selectedOutputData.getSelectedViewportList();
		ArrayList<Viewport> viewportList;
		Viewport[] viewports = null;
		KenburnDirection direction;

		if (viewportLists != null && viewportLists.size() > 0) {
			viewportList = viewportLists.get(0);

			if (viewportList != null && viewportList.size() > 0) {
				viewports = new Viewport[viewportList.size()];
				for (int i = 0; i < viewportList.size(); i++) {
					viewports[i] = viewportList.get(i);
				}
			}
		}

		if (viewports != null) {
			if (mUplusKenBurnsScalerCoordinator.isSameViewPort(viewports[0], viewports[1])) {
				direction = mUplusKenBurnsScalerCoordinator.getSingleLayerViewPortDirection(imageData, UplusKenBurnsScalerCoordinator.PREVIEW_RATIO_WIDTH, UplusKenBurnsScalerCoordinator.PREVIEW_RATIO_HEIGHT, viewports[0]);
				Viewport[] newViewports = mUplusKenBurnsScalerCoordinator.applyKenBurnEffectFromStartViewPort(imageData, viewports[0], direction);
				selectedOutputData.setAccelerationZoom(false);
				setKenBurnsScaler(sceneEditor, newViewports, false);

			} else {
				setKenBurnsScaler(sceneEditor, viewports, mSelectedOutputData.getAccelerationZoom());
			}
		} else {
			setKenBurnsScaler(sceneEditor, imageData, isAccelerationZoom, selectedOutputData.getKenburnDirection());
		}

		sceneEditor.getObject().setImageId(imageData.id);
		sceneEditor.setImageResource(ImageResource.createFromFile(imageData.path, ScaleType.BUFFER));

	}

	/**
	 * 현재 테마에 맞게 각각의 frame에 effect가 있다면 sceneEditor에 설정한다.<br>
	 * 
	 * @param sceneEditor
	 *            : 현재 편집되고 있는 scene editor.<br>
	 */
	private void setFrameEffect(ImageFileScene.Editor sceneEditor, int templateId) {

		Frame frame = null;
		String overlayPath = null;

		if (mUplusOutputData.getFrames() == null) {
			L.d("Frame is null");
			return;
		}
		for (Frame _frame : mUplusOutputData.getFrames()) {
			if (_frame.frameCount == UplusImageFileSceneCoordinator.FRAME_COUNT_SINGLE && _frame.frameType == FrameType.CONTENT) {
				mFrames.add(_frame);
			}
		}

		if (mFrames == null || mFrames.isEmpty()) {
			return;
		}

		for (Frame _frame : mFrames) {
			if (_frame.id == templateId) {
				frame = _frame;
			}

		}

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

	/**
	 * 현재 테마에 맞게 각각의 frame에 effect가 있다면 sceneEditor에 설정한다.<br>
	 * 
	 * @param sceneEditor
	 *            : 현재 편집되고 있는 scene editor.<br>
	 */
	private void setFrameEffect(ImageFileScene.Editor sceneEditor) {

		Frame frame = null;
		String overlayPath = null;

		if (mUplusOutputData.getFrames() == null) {
			L.d("Frame is null");
			return;
		}
		for (Frame _frame : mUplusOutputData.getFrames()) {
			if (_frame.frameCount == UplusImageFileSceneCoordinator.FRAME_COUNT_SINGLE && _frame.frameType == FrameType.CONTENT) {
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

	/**
	 * kenburn effect 설정.
	 * 
	 * @param sceneEditor
	 *            : 현재 편집되고 있는 scene editor.<br>
	 * @param imageData
	 *            : 현재 사용되고 있는 data. <br>
	 */
	private void setKenBurnsScaler(ImageFileScene.Editor sceneEditor, ImageData imageData, boolean isAccelerationZoom, KenburnDirection direction) {
		KenBurnsScaler.Editor scalerEditor = sceneEditor.setScaler(KenBurnsScaler.class).getEditor();
		L.d("imageData name : " + imageData.fileName + " ( 16 : 9 )");
		Viewport[] viewports = mUplusKenBurnsScalerCoordinator.applyKenBurnEffectPicture(imageData, UplusKenBurnsScalerCoordinator.PREVIEW_RATIO_WIDTH, UplusKenBurnsScalerCoordinator.PREVIEW_RATIO_HEIGHT, isAccelerationZoom, direction);
		scalerEditor.setViewports(viewports);
		if (isAccelerationZoom) {

			List<InterpolatorType> interpolatorTypeList = new ArrayList<InterpolatorType>();
			List<Integer> weightList = new ArrayList<Integer>();

			for (int i = 0; i < viewports.length - 2; i++) {
				interpolatorTypeList.add(InterpolatorType.CUBIC_IN);
			}
			interpolatorTypeList.add(InterpolatorType.LINEAR);
			scalerEditor.setInterpolators(interpolatorTypeList);

			for (int i = 0; i < viewports.length - 2; i++) {
				weightList.add(1);
			}
			weightList.add(2);
			scalerEditor.setWeights(weightList);

		} else {
			scalerEditor.setInterpolator(null);
			scalerEditor.setWeights(1);
		}
	}

	/**
	 * kenburn effect 설정.
	 * 
	 * @param sceneEditor
	 *            : 현재 편집되고 있는 scene editor.<br>
	 * @param viewport
	 *            : 후보정에서 update된 viewport . <br>
	 */
	private void setKenBurnsScaler(ImageFileScene.Editor sceneEditor, Viewport[] viewports, boolean isAcceleration) {
		KenBurnsScaler.Editor scalerEditor = sceneEditor.setScaler(KenBurnsScaler.class).getEditor();
		scalerEditor.setViewports(viewports);
		if (isAcceleration) {
			List<InterpolatorType> interpolatorTypeList = new ArrayList<InterpolatorType>();
			List<Integer> weightList = new ArrayList<Integer>();

			for (int i = 0; i < viewports.length - 2; i++) {
				interpolatorTypeList.add(InterpolatorType.CUBIC_IN);
			}
			interpolatorTypeList.add(InterpolatorType.LINEAR);
			scalerEditor.setInterpolators(interpolatorTypeList);

			for (int i = 0; i < viewports.length - 2; i++) {
				weightList.add(1);
			}
			weightList.add(2);
			scalerEditor.setWeights(weightList);
		} else {
			scalerEditor.setInterpolator(null);
			scalerEditor.setWeights(1);
		}
	}

}
