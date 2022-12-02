package com.kiwiple.scheduler.coordinate.scene.uplus;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;

import com.kiwiple.debug.L;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.multimedia.Constants;
import com.kiwiple.multimedia.canvas.KenBurnsScaler;
import com.kiwiple.multimedia.canvas.LayerScene;
import com.kiwiple.multimedia.canvas.MultiLayerScene;
import com.kiwiple.multimedia.canvas.OverlayEffect;
import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.multimedia.canvas.ScaleEffect;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.StepAppearEffect;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.multimedia.canvas.StepAppearEffect;
import com.kiwiple.multimedia.canvas.VideoFileScene;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.scheduler.analysis.uplus.UplusVideoAnalysis;
import com.kiwiple.scheduler.coordinate.effect.uplus.UplusEffectApplyManager;
import com.kiwiple.scheduler.coordinate.scaler.KenBurnsScalerCoordinator.KenburnDirection;
import com.kiwiple.scheduler.coordinate.scaler.uplus.UplusKenBurnsScalerCoordinator;
import com.kiwiple.scheduler.coordinate.scaler.uplus.UplusMultiLayerKenBurnScalerCoordinator;
import com.kiwiple.scheduler.coordinate.scene.MultiLayerSceneCoordinator;
import com.kiwiple.scheduler.data.MultiLayerData;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.data.SelectedOutputData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.database.uplus.UplusAnalysisConstants;
import com.kiwiple.scheduler.database.uplus.UplusAnalysisPersister;
import com.kiwiple.scheduler.theme.Theme.Frame;
import com.kiwiple.scheduler.theme.Theme.FrameType;
import com.kiwiple.scheduler.theme.Theme.ThemeType;

public class UplusMultiLayerSceneCoordinator extends MultiLayerSceneCoordinator {

	private UplusKenBurnsScalerCoordinator mUplusKenBurnsScalerCoordinator;
	private UplusMultiLayerKenBurnScalerCoordinator mUplusMultiKenBurnScalerCoordinator;
	private UplusOutputData mUplusOutputData;
	private SelectedOutputData mSelectedOutputData;

	private List<Frame> mFrames;
	private Context mContext;

	private boolean mIsFromEdit;

	public UplusMultiLayerSceneCoordinator(Context context, OutputData outputData) {
		mIsFromEdit = false;
		mContext = context;
		mUplusOutputData = (UplusOutputData) outputData;
		mUplusKenBurnsScalerCoordinator = new UplusKenBurnsScalerCoordinator();
		mUplusMultiKenBurnScalerCoordinator = new UplusMultiLayerKenBurnScalerCoordinator();
		mFrames = new ArrayList<Frame>();
	}

	/**
	 * 현재 테마에 맞게 각각의 frame에 effect가 있다면 sceneEditor에 설정한다.<br>
	 * 
	 * @param sceneEditor
	 *            : 현재 편집되고 있는 scene editor.<br>
	 */
	private void setFrameEffect(MultiLayerScene.Editor sceneEditor) {

		Frame frame = null;
		String overlayPath = null;

		if (mUplusOutputData.getFrames() == null) {
			L.d("Frame is null");
			return;
		}
		for (Frame _frame : mUplusOutputData.getFrames()) {
			if (_frame.frameCount >= UplusImageFileSceneCoordinator.FRAME_COUNT_SINGLE && _frame.frameType == FrameType.CONTENT) {
				mFrames.add(_frame);
			}
		}

		// 20150217 olive : #10507 (Frame이 없는 경우 예외처리 ex>Travel)
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
	 * 새로운 multi scene을 추가.<br>
	 * 
	 * @param regionEditor
	 *            : 현재 편집되고 있는 region editor.<br>
	 * @param selectedOutputData
	 *            : 삽입될 data.<br>
	 */
	public void addMultiLayerScene(Editor regionEditor, SelectedOutputData selectedOutputData) {
		mIsFromEdit = false;
		mSelectedOutputData = selectedOutputData;
		MultiLayerScene multiLayerScene = regionEditor.addScene(MultiLayerScene.class);
		MultiLayerScene.Editor sceneEditor = multiLayerScene.getEditor();

		L.d("frame id : " + selectedOutputData.getFrameId());
		sceneEditor.getObject().setTemplateId(selectedOutputData.getFrameId());

		sceneEditor.setDuration(mUplusOutputData.getTheme().contentDuration);
		setFrameEffect(sceneEditor);

		ArrayList<ImageData> imageDataList = selectedOutputData.getImageDatas();

		L.d("image data size : " + imageDataList.size());
		ArrayList<ArrayList<Viewport>> multilayerViewportLists = selectedOutputData.getSelectedViewportList();
		ArrayList<Viewport> layerViewportList = selectedOutputData.getSelectedLayerViewportList();
		ArrayList<Viewport[]> multilayerViewportList = null;

		if (multilayerViewportLists != null && multilayerViewportLists.size() > 0) {
			multilayerViewportList = new ArrayList<Viewport[]>();
			Viewport[] viewports;
			for (int m = 0; m < multilayerViewportLists.size(); m++) {
				ArrayList<Viewport> vPortList = multilayerViewportLists.get(m);
				viewports = new Viewport[vPortList.size()];
				for (int i = 0; i < vPortList.size(); i++) {
					viewports[i] = (Viewport) vPortList.get(i);
				}
				multilayerViewportList.add(viewports);
			}
		}
		int frameId = selectedOutputData.getFrameId(); 
		if (multilayerViewportList == null) {
			// 신규 생성시
			applyKenburnEffect(sceneEditor, frameId, getReorderImageDataList(imageDataList, frameId));
		} else {
			// 테마 변경시
			applyKenburnEffect(sceneEditor, frameId, multilayerViewportList, layerViewportList, imageDataList);
		}
		
		// fixes #12400 : 비디오가 포함된 멀티레이어씬의 길이를 재조절한다. (비디오 분량 확보)
		int movieMaxDuration = 0; 
		for (Scene layer : multiLayerScene.getLayers()) {
            if (layer.isInstanceOf(VideoFileScene.class)) {
                VideoFileScene videoLayer = (VideoFileScene)layer;
                VideoFileScene.Editor videoEditor = videoLayer.getEditor();
                videoEditor.setIsVideoMultiLayer(true);
                int videoDuration = (int)(videoLayer.getVideoEndPosition() - videoLayer.getVideoStartPosition());
                if (movieMaxDuration < videoDuration) {
                    movieMaxDuration = videoDuration;
                }
            }
        }
		if (movieMaxDuration > multiLayerScene.getDuration()) {
		    sceneEditor.setDuration(movieMaxDuration);
		}
	
		setTag(multiLayerScene, selectedOutputData, OutputData.TAG_JSON_VALE_SCENE_CONTENT);
	}
	
	private ArrayList<ImageData> getReorderImageDataList(ArrayList<ImageData> imageDataList, int frameId){
		ArrayList<ImageData> reOrderImageDataList = new ArrayList<ImageData>();
		if(frameId == MultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID){
			if(imageDataList.get(1).isPotraitImage()){
				if(!imageDataList.get(0).isPotraitImage()){
					reOrderImageDataList.add(0, imageDataList.get(1));
					reOrderImageDataList.add(1, imageDataList.get(0)); 
					reOrderImageDataList.add(2, imageDataList.get(2));
					return reOrderImageDataList;
				}else if(!imageDataList.get(2).isPotraitImage()){
					reOrderImageDataList.add(0, imageDataList.get(0));
					reOrderImageDataList.add(1, imageDataList.get(2)); 
					reOrderImageDataList.add(2, imageDataList.get(1));
					return reOrderImageDataList;
				}else{
					return imageDataList; 
				}
			}else{
				return imageDataList;
			}
		}else{
			return imageDataList; 
		}
	}
	

	// /**
	// * 후보정에서 multi scene update시에 kenburn effect를 재 설정한다.<br>
	// * @param regionEditor : 현재 편집되고 있는 region editor.<br>
	// * @param frameId : layout. <br>
	// * @param imageDataList : image data list. <br>
	// */
	// public void updateKenburnEffect(MultiLayerScene.Editor sceneEditor, int frameId,
	// ArrayList<ImageData> imageDataList){
	// if (frameId == MultiLayerData.MULTI_LAYER_COLUMN_TWO_PICTURES_ID) {
	// L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_TWO_PICTURES");
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 0),
	// imageDataList.get(0), 8.0f, 9.0f, 0, KenburnDirection.UP);// left
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 1),
	// imageDataList.get(1), 8.0f, 9.0f, 1, KenburnDirection.DOWN);// right
	// } else if (frameId == MultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID) {
	// L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID");
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 0),
	// imageDataList.get(0), 5.28f, 9.0f, 0, KenburnDirection.UP);// left
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 1),
	// imageDataList.get(1), 5.44f, 9.0f, 1, KenburnDirection.DOWN);// center
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 2),
	// imageDataList.get(2), 5.28f, 9.0f, 2, KenburnDirection.UP);// bottom
	// } else if (frameId == MultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID) {
	// L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID");
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 0),
	// imageDataList.get(0), 8.0f, 9.0f, 0, KenburnDirection.UP);// left
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 1),
	// imageDataList.get(1), 8.0f, 4.5f, 1, KenburnDirection.RIGHT); // right_top
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 2),
	// imageDataList.get(2), 8.0f, 4.5f, 2, KenburnDirection.LEFT);// right_bottom
	// } else if (frameId == MultiLayerData.MULTI_LAYER_REGULAR_FOUR_PICTURES_ID) {
	// L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_REGULAR_FOUR_PICTURES_ID");
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 0),
	// imageDataList.get(0), 8.0f, 4.5f, 0, KenburnDirection.RIGHT);// left_top
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 1),
	// imageDataList.get(1), 8.0f, 4.5f, 1, KenburnDirection.UP);// left_bottom
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 2),
	// imageDataList.get(2), 8.0f, 4.5f, 2, KenburnDirection.DOWN);// right_top
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 3),
	// imageDataList.get(3), 8.0f, 4.5f, 3, KenburnDirection.LEFT);// right_bottom
	// } else if (frameId == MultiLayerData.MULTI_LAYER_IRREGULAR_01_FOUR_PICTURES_ID) {
	// L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_IRREGULAR_01_FOUR_PICTURES_ID");
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 0),
	// imageDataList.get(0), 5.6f, 4.5f, 0, KenburnDirection.RIGHT);// left_top
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 1),
	// imageDataList.get(1), 10.4f, 4.5f, 1, KenburnDirection.LEFT);// left_bottom
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 2),
	// imageDataList.get(2), 10.4f, 4.5f, 2, KenburnDirection.RIGHT);// right_top
	// updateLayer(sceneEditor,
	// mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 3),
	// imageDataList.get(3), 5.6f, 4.5f, 3, KenburnDirection.LEFT);// right_bottom
	// }
	// }

	/**
	 * multi scene에 각각의 레이어를 추가한다. <br>
	 * 
	 * @param sceneEditor
	 *            : 현재 편집되고 있는 scene editor.<br>
	 * @param imageData
	 *            : 삽입될 data. <br>
	 * @param viewports
	 *            : 각 layer의 이미지가 가지는 viewport 영역. <br>
	 * @param layerViewPort
	 *            : scene의 일부인 layer의 영역. <br>
	 */

	private void addLayer(MultiLayerScene.Editor sceneEditor, ImageData imageData, Viewport[] viewports, Viewport layerViewPort, int count) {

		if (UplusVideoAnalysis.isMovieData(imageData)) {
			VideoFileScene.Editor videoEditor = sceneEditor.addLayer(VideoFileScene.class, layerViewPort).getEditor();
			setVideoFileScene(videoEditor, imageData);
		} else {
			LayerScene.Editor layerEditor = sceneEditor.addLayer(LayerScene.class, layerViewPort).getEditor();
			layerEditor.setImageFilePath(imageData.path);
			layerEditor.getObject().setImageId(imageData.id);

			if (mSelectedOutputData.getMultiFilter() && mUplusOutputData.getTheme().themeType != ThemeType.FILTER) {
				switch (count) {
					case 0:
						layerEditor.setFilterId(mUplusOutputData.getTheme().filterId);
						break;
					case 1:
						layerEditor.setFilterId(OutputData.FILTER_ID_LITTLE_BABY);
						break;
					case 2:
						layerEditor.setFilterId(OutputData.FILTER_ID_COZY_ROOM);
						break;
				}
			} else {
				if (mUplusOutputData.getTheme().themeType == ThemeType.FILTER) {
					layerEditor.setFilterId(mUplusOutputData.getTheme().filterId);
				} else {
					layerEditor.setFilterId(Constants.INVALID_FILTER_ID);
				}
			}
			
			for(Viewport viewport : viewports){
				L.d("multiLayer view port ("+viewport.left + ", " +viewport.top +") (" +viewport.right +", "+ viewport.bottom +")"); 
			}

			((KenBurnsScaler) layerEditor.getObject().getScaler()).getEditor().setViewports(viewports);
		}
	}

	/**
	 * video의 id를 가지고 scene editor에 추가한다. <br>
	 * start, end position의 0이면 분석된 값을 사용한다. <br>
	 * 
	 * @param sceneEditor
	 *            : 편집할 scene editor.
	 * @param videoId
	 *            : video data id.
	 * @param videoStart
	 *            : video start position.
	 * @param videoEnd
	 *            : video end position.
	 */
	private void setVideoFileScene(VideoFileScene.Editor sceneEditor, ImageData videoData) {
		UplusAnalysisPersister persister = UplusAnalysisPersister.getAnalysisPersister(mContext.getApplicationContext());
		Cursor cursor = persister.getVideoDataCursorInAnalysis(videoData.id);
		String videoPath = null;
		if (cursor != null) {
			L.d("Analyzed Video id = " + videoData.id);
			cursor.moveToNext();
			videoPath = cursor.getString(cursor.getColumnIndexOrThrow(UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_PATH));
			long startPosition;
			long endPosition;
			if (videoData.imageCorrectData.videoStartPosition >= 0 
			        && videoData.imageCorrectData.videoEndPosition > videoData.imageCorrectData.videoStartPosition) {
				startPosition = videoData.imageCorrectData.videoStartPosition;
				endPosition = videoData.imageCorrectData.videoEndPosition;
			} else {
				startPosition = cursor.getLong(cursor.getColumnIndexOrThrow(UplusAnalysisConstants.VideoAnalysisDatabaseField.START_POSITION));
				endPosition = cursor.getLong(cursor.getColumnIndexOrThrow(UplusAnalysisConstants.VideoAnalysisDatabaseField.END_POSITION));
			}
			int duration = (int) (endPosition - startPosition);
			L.e("video Duration : " + duration);
			sceneEditor.setVideoFilePath(videoPath);
			sceneEditor.setVideoClip(startPosition, endPosition);
			sceneEditor.setDuration(duration);
			sceneEditor.setVideoId(videoData.id);
			cursor.close();
			
			videoData.imageCorrectData.videoStartPosition = startPosition;
			videoData.imageCorrectData.videoEndPosition = endPosition;
			videoData.imageCorrectData.videoDuration = duration;
		} else {
			cursor = persister.getVideoDataCursorInGallery(videoData.id);
			if (cursor != null) {
				L.d("Video id = " + videoData.id);
				cursor.moveToNext();
				long duration = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DURATION));

				videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.VideoColumns.DATA));

				if (duration <= 0) {
					MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();

					try {
						mediaMetadataRetriever.setDataSource(videoPath);
						duration = Integer.parseInt(mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));
						mediaMetadataRetriever.release();
						mediaMetadataRetriever = null;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				sceneEditor.setVideoFilePath(videoPath);

				long durationDivider = UplusVideoFileSceneCoordinator.DURATION_DIVIDER;
				int position = (int) (duration / durationDivider);
				long startPosition;
				long endPosition;
				if (position > 0) {
					if (videoData.imageCorrectData.videoStartPosition >= 0 
					        && videoData.imageCorrectData.videoEndPosition > videoData.imageCorrectData.videoStartPosition) {
						startPosition = videoData.imageCorrectData.videoStartPosition;
						endPosition = videoData.imageCorrectData.videoEndPosition;
					} else {
						startPosition = (long) ((int) (Math.random() * position) * durationDivider);
						endPosition = startPosition + durationDivider > duration ? duration : startPosition + durationDivider;
					}
					sceneEditor.setVideoClip(startPosition, endPosition);
					sceneEditor.setVideoId(videoData.id);
					sceneEditor.setDuration((int) (endPosition - startPosition));
					L.e("video Duration : " + (int) (endPosition - startPosition));
					videoData.imageCorrectData.videoStartPosition = startPosition;
		            videoData.imageCorrectData.videoEndPosition = endPosition;
		            videoData.imageCorrectData.videoDuration = (int) (endPosition - startPosition);
				} else {
					if (videoData.imageCorrectData.videoStartPosition >= 0 
					        && videoData.imageCorrectData.videoEndPosition > videoData.imageCorrectData.videoStartPosition) {
						startPosition = videoData.imageCorrectData.videoStartPosition;
						endPosition = videoData.imageCorrectData.videoEndPosition;
					} else {
						startPosition = 0;
						endPosition = duration;
					}
					sceneEditor.setVideoId(videoData.id);
					sceneEditor.setVideoClip(startPosition, endPosition);
					sceneEditor.setDuration((int) (endPosition - startPosition));
					L.e("video Duration : " + (int) (endPosition - startPosition));
					videoData.imageCorrectData.videoStartPosition = startPosition;
                    videoData.imageCorrectData.videoEndPosition = endPosition;
                    videoData.imageCorrectData.videoDuration = (int) (endPosition - startPosition);
				}
				cursor.close();
			}
		}
	}

	/**
	 * 테마 변경 시 multi scene이 가지는 kenburn effect를 재 설정한다.<br>
	 * 
	 * @param regionEditor
	 *            : 현재 편집되고 있는 region editor.<br>
	 * @param frameId
	 *            : layout. <br>
	 * @param viewportArrayList
	 *            : 기존 멀티레이어가 가지는 viewport list. <br>
	 * @param layerViewportList
	 *            : 기존 멀티레이어가 가지는 layer viewport list. <br>
	 * @param imageDataList
	 *            : image data list. <br>
	 */

	private void applyKenburnEffect(MultiLayerScene.Editor sceneEditor, int frameId, ArrayList<Viewport[]> viewportArrayList, ArrayList<Viewport> layerViewportList, ArrayList<ImageData> imageDataList) {

		if (frameId == MultiLayerData.MULTI_LAYER_COLUMN_TWO_PICTURES_ID) {
			L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_TWO_PICTURES");
			addLayer(sceneEditor, imageDataList.get(0), viewportArrayList.get(0), layerViewportList.get(0), 0);
			addLayer(sceneEditor, imageDataList.get(1), viewportArrayList.get(1), layerViewportList.get(1), 1);
		} else if (frameId == MultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID) {
			L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID");
			addLayer(sceneEditor, imageDataList.get(0), viewportArrayList.get(0), layerViewportList.get(0), 0);
			addLayer(sceneEditor, imageDataList.get(1), viewportArrayList.get(1), layerViewportList.get(1), 1);
			addLayer(sceneEditor, imageDataList.get(2), viewportArrayList.get(2), layerViewportList.get(2), 2);
		} else if (frameId == MultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID) {
			L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID");
			addLayer(sceneEditor, imageDataList.get(0), viewportArrayList.get(0), layerViewportList.get(0), 0);
			addLayer(sceneEditor, imageDataList.get(1), viewportArrayList.get(1), layerViewportList.get(1), 1);
			addLayer(sceneEditor, imageDataList.get(2), viewportArrayList.get(2), layerViewportList.get(2), 2);
		} else if (frameId == MultiLayerData.MULTI_LAYER_REGULAR_FOUR_PICTURES_ID) {
			L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_REGULAR_FOUR_PICTURES_ID");
			addLayer(sceneEditor, imageDataList.get(0), viewportArrayList.get(0), layerViewportList.get(0), 0);
			addLayer(sceneEditor, imageDataList.get(1), viewportArrayList.get(1), layerViewportList.get(1), 1);
			addLayer(sceneEditor, imageDataList.get(2), viewportArrayList.get(2), layerViewportList.get(2), 2);
			addLayer(sceneEditor, imageDataList.get(3), viewportArrayList.get(3), layerViewportList.get(3), 3);
		} else if (frameId == MultiLayerData.MULTI_LAYER_IRREGULAR_01_FOUR_PICTURES_ID) {
			L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_IRREGULAR_01_FOUR_PICTURES_ID");
			addLayer(sceneEditor, imageDataList.get(0), viewportArrayList.get(0), layerViewportList.get(0), 0);
			addLayer(sceneEditor, imageDataList.get(1), viewportArrayList.get(1), layerViewportList.get(1), 1);
			addLayer(sceneEditor, imageDataList.get(2), viewportArrayList.get(2), layerViewportList.get(2), 2);
			addLayer(sceneEditor, imageDataList.get(3), viewportArrayList.get(3), layerViewportList.get(3), 3);
		}
	}

	/**
	 * multi layout의 kenburn effect 설정. <br>
	 * 
	 * @param sceneEditor
	 *            : 현재 편집되고 있는 scene editor.<br>
	 * @param frameId
	 *            : layout. <br>
	 * @param imageDataList
	 *            : image data list. <br>
	 */
	public void applyKenburnEffect(MultiLayerScene.Editor sceneEditor, int frameId, ArrayList<ImageData> imageDataList) {
		if (frameId == MultiLayerData.MULTI_LAYER_COLUMN_TWO_PICTURES_ID) {
			L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_TWO_PICTURES");
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 0), imageDataList.get(0), 8.0f, 9.0f, KenburnDirection.UP, 0);// left
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 1), imageDataList.get(1), 8.0f, 9.0f, KenburnDirection.DOWN, 1);// right
		} else if (frameId == MultiLayerData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID) {
			L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_COLUMN_THREE_PICTURES_ID");
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 0), imageDataList.get(0), 5.28f, 9.0f, KenburnDirection.UP, 0);// left
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 1), imageDataList.get(1), 5.44f, 9.0f, KenburnDirection.DOWN, 1);// center
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 2), imageDataList.get(2), 5.28f, 9.0f, KenburnDirection.UP, 2);// bottom
		} else if (frameId == MultiLayerData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID) {
			L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_LEFT_ONE_RIGHT_TWO_PICTURES_ID");
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 0), imageDataList.get(0), 8.0f, 9.0f, KenburnDirection.UP, 0);// left
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 1), imageDataList.get(1), 8.0f, 4.5f, KenburnDirection.RIGHT, 1); // right_top
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 2), imageDataList.get(2), 8.0f, 4.5f, KenburnDirection.LEFT, 2);// right_bottom
		} else if (frameId == MultiLayerData.MULTI_LAYER_REGULAR_FOUR_PICTURES_ID) {
			L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_REGULAR_FOUR_PICTURES_ID");
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 0), imageDataList.get(0), 8.0f, 4.5f, KenburnDirection.RIGHT, 0);// left_top
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 1), imageDataList.get(1), 8.0f, 4.5f, KenburnDirection.UP, 1);// left_bottom
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 2), imageDataList.get(2), 8.0f, 4.5f, KenburnDirection.DOWN, 2);// right_top
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 3), imageDataList.get(3), 8.0f, 4.5f, KenburnDirection.LEFT, 3);// right_bottom
		} else if (frameId == MultiLayerData.MULTI_LAYER_IRREGULAR_01_FOUR_PICTURES_ID) {
			L.d("MultiLayoutType = " + "MultiLayoutData.MULTI_LAYER_IRREGULAR_01_FOUR_PICTURES_ID");
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 0), imageDataList.get(0), 5.6f, 4.5f, KenburnDirection.RIGHT, 0);// left_top
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 1), imageDataList.get(1), 10.4f, 4.5f, KenburnDirection.LEFT, 1);// left_bottom
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 2), imageDataList.get(2), 10.4f, 4.5f, KenburnDirection.RIGHT, 2);// right_top
			addLayer(sceneEditor, mUplusMultiKenBurnScalerCoordinator.getTemplateLayerViewport(frameId, 3), imageDataList.get(3), 5.6f, 4.5f, KenburnDirection.LEFT, 3);// right_bottom
		}
	}

	/**
	 * 후보정에서 새로운 multi scene을 추가.<br>
	 * 
	 * @param regionEditor
	 *            : 현재 편집되고 있는 region editor.<br>
	 * @param imageDataList
	 *            : 삽입될 data.<br>
	 * @param scenePosition
	 *            : 삽입될 data position.<br>
	 */
	public void insertMultiLayerScene(Region.Editor regionEditor, SelectedOutputData selectedOutputData, int scenePosition) {

		mIsFromEdit = true;
		MultiLayerScene multiLayerScene = regionEditor.addScene(MultiLayerScene.class, scenePosition);
		MultiLayerScene.Editor sceneEditor = multiLayerScene.getEditor();

		int templateId = selectedOutputData.getImageDatas().get(0).imageCorrectData.collageTempletId;
		mSelectedOutputData = selectedOutputData;

		sceneEditor.setDuration(mUplusOutputData.getTheme().contentDuration);
		editMultiLayerScene(sceneEditor, templateId, selectedOutputData.getImageDatas());
		applyKenburnEffect(sceneEditor, templateId, selectedOutputData.getImageDatas());
		
		// fixes #12400 : 비디오가 포함된 멀티레이어씬의 길이를 재조절한다. (비디오 분량 확보)
        int movieMaxDuration = 0; 
        for (Scene layer : multiLayerScene.getLayers()) {
            if (layer.isInstanceOf(VideoFileScene.class)) {
                VideoFileScene videoLayer = (VideoFileScene)layer;
                int videoDuration = (int)(videoLayer.getVideoEndPosition() - videoLayer.getVideoStartPosition());
                if (movieMaxDuration < videoDuration) {
                    movieMaxDuration = videoDuration;
                }
            }
        }
        if (movieMaxDuration > multiLayerScene.getDuration()) {
            sceneEditor.setDuration(movieMaxDuration);
        }

		setTag(multiLayerScene, selectedOutputData, OutputData.TAG_JSON_VALE_SCENE_CONTENT);
	}

	// /**
	// * 후보정에서 새로운 multi scene을 update한다.<br>
	// * @param multiLayerSceneEditor : 현재 편집되고 있는 scene editor.<br>
	// * @param imageDataList : 삽입될 data.<br>
	// */
	// public void updateMultiLayerScene(MultiLayerScene.Editor multiLayerSceneEditor,
	// ArrayList<ImageData> imageDataList){
	// mIsFromEdit = true;
	// updateKenburnEffect(multiLayerSceneEditor,
	// imageDataList.get(0).imageCorrectData.collageTempletId, imageDataList);
	// }

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
	private void editMultiLayerScene(MultiLayerScene.Editor multiScneEditor, int templateId, ArrayList<ImageData> imageDataList) {

		multiScneEditor.getObject().setTemplateId(templateId);
		L.e("imageDataLis.size = " + imageDataList.size());

		setFrameEffect(multiScneEditor);
	}

	/**
	 * multi scene에 각각의 레이어를 추가한다. <br>
	 * 
	 * @param sceneEditor
	 *            : 현재 편집되고 있는 scene editor.<br>
	 * @param layerViewPort
	 *            : scene의 일부인 layer의 영역. <br>
	 * @param imageData
	 *            : 삽입될 data. <br>
	 * @param previewRatioWidth
	 *            : preview에서 차지 하는 width 영역. <br>
	 * @param previewRatioHeight
	 *            :preview에서 차지 하는 height 영역. <br>
	 * @param direction
	 *            : kenburn의 방향. <br>
	 * @param speed
	 *            : kenburn의 speed. <br>
	 */
	private void addLayer(MultiLayerScene.Editor sceneEditor, Viewport layerViewPort, ImageData imageData, float previewRatioWidth, float previewRatioHeight, KenburnDirection direction, int count) {

		if (UplusVideoAnalysis.isMovieData(imageData)) {
			VideoFileScene.Editor videoEditor = sceneEditor.addLayer(VideoFileScene.class, layerViewPort).getEditor();
			videoEditor.setIsVideoMultiLayer(true);
			setVideoFileScene(videoEditor, imageData);
		} else {
			Viewport[] viewports = null;
			LayerScene.Editor layerEditor = sceneEditor.addLayer(LayerScene.class, layerViewPort).getEditor();
			layerEditor.setImageFilePath(imageData.path);
			layerEditor.getObject().setImageId(imageData.id);

			if (mSelectedOutputData.getMultiFilter() && mUplusOutputData.getTheme().themeType != ThemeType.FILTER) {
				switch (count) {
					case 0:
						layerEditor.setFilterId(mUplusOutputData.getTheme().filterId);
						break;
					case 1:
						layerEditor.setFilterId(OutputData.FILTER_ID_LITTLE_BABY);
						break;
					case 2:
						layerEditor.setFilterId(OutputData.FILTER_ID_COZY_ROOM);
						break;
				}
			} else {
				if (mUplusOutputData.getTheme().themeType == ThemeType.FILTER) {
					layerEditor.setFilterId(mUplusOutputData.getTheme().filterId);
				} else {
					layerEditor.setFilterId(Constants.INVALID_FILTER_ID);
				}
			}

			L.d("imageData name : " + imageData.fileName + " ( " + previewRatioWidth + ", " + previewRatioHeight + ")");
			// layout에서 주어진 영역으로 Kenburn영역을 정한다.
			if (!mIsFromEdit) {
				viewports = mUplusMultiKenBurnScalerCoordinator.applyKenBurnEffectPicture(imageData, previewRatioWidth, previewRatioHeight, direction);
			} else {
				Viewport startViewport = mUplusMultiKenBurnScalerCoordinator.makeViewportFromCollagePosition(imageData, previewRatioWidth, previewRatioHeight, layerViewPort);
				viewports = mUplusMultiKenBurnScalerCoordinator.applyKenBurnEffectFromStartViewPort(imageData, startViewport, direction);
			}
			for(Viewport viewport : viewports){
				L.d("multiLayer view port ("+viewport.left + ", " +viewport.top +") (" +viewport.right +", "+ viewport.bottom +")"); 
			}
			((KenBurnsScaler) layerEditor.getObject().getScaler()).getEditor().setViewports(viewports);
		}
	}

	// /**
	// * multi scene에 각각의 레이어를 수정한다. <br>
	// * @param sceneEditor : 현재 편집되고 있는 scene editor.<br>
	// * @param layerViewPort : scene의 일부인 layer의 영역. <br>
	// * @param imageData : 삽입될 data. <br>
	// * @param previewRatioWidth : preview에서 차지 하는 width 영역. <br>
	// * @param previewRatioHeight :preview에서 차지 하는 height 영역. <br>
	// * @param index : 수정되는 layer 위치. <br>
	// * @param direction : kenburn의 방향. <br>
	// * @param speed : kenburn의 speed. <br>
	// */
	// private void updateLayer(MultiLayerScene.Editor sceneEditor, Viewport layerViewPort,
	// ImageData imageData, float previewRatioWidth,
	// float previewRatioHeight, int index, KenburnDirection direction) {
	//
	// Viewport[] viewports = null;
	// LayerScene imageLayerScene = (LayerScene)sceneEditor.getObject().getLayer(index);
	// LayerScene.Editor layerEditor = imageLayerScene.getEditor()
	// layerEditor.setViewport(layerViewPort);
	//
	// L.d("imageData name : " + imageData.fileName + " ( " + previewRatioWidth + ", " +
	// previewRatioHeight + ")");
	// // layout에서 주어진 영역으로 Kenburn영역을 정한다.
	// Viewport startViewport =
	// mUplusMultiKenBurnScalerCoordinator.makeViewportFromCollagePosition(imageData,
	// previewRatioWidth, previewRatioHeight, layerViewPort);
	// viewports =
	// mUplusMultiKenBurnScalerCoordinator.applyKenBurnEffectFromStartViewPort(imageData,
	// startViewport, direction);
	// ((KenBurnsScaler) layerEditor.getObject().getScaler()).getEditor().setViewports(viewports);
	// }

}
