package com.kiwiple.scheduler.coordinate.scene.uplus;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.provider.MediaStore;

import com.kiwiple.debug.L;
import com.kiwiple.multimedia.Constants;
import com.kiwiple.multimedia.canvas.OverlayEffect;
import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.multimedia.canvas.VideoFileScene;
import com.kiwiple.scheduler.coordinate.effect.uplus.UplusEffectApplyManager;
import com.kiwiple.scheduler.coordinate.scene.VideoFileSceneCoordinator;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.data.SelectedOutputData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;
import com.kiwiple.scheduler.database.uplus.UplusAnalysisConstants;
import com.kiwiple.scheduler.database.uplus.UplusAnalysisPersister;
import com.kiwiple.scheduler.theme.Theme;
import com.kiwiple.scheduler.theme.Theme.Frame;
import com.kiwiple.scheduler.theme.Theme.FrameType;
import com.kiwiple.scheduler.theme.Theme.ThemeType;

public class UplusVideoFileSceneCoordinator extends VideoFileSceneCoordinator {
	private UplusOutputData mUplusOutputData;

	public static final long DURATION_DIVIDER = 10000;

	private Context mContext;

	public UplusVideoFileSceneCoordinator(Context context, OutputData outputData) {
		mContext = context;
		mUplusOutputData = (UplusOutputData) outputData;

	}

	@Override
	public void addVideoFileScene(Region.Editor regionEditor, SelectedOutputData selectedOutputData) {
		VideoFileScene scene = regionEditor.addScene(VideoFileScene.class);
		VideoFileScene.Editor sceneEditor = scene.getEditor();
		setVideoFileScene(sceneEditor, Integer.parseInt(selectedOutputData.getNameList().get(0)), selectedOutputData.getVideoStart(), selectedOutputData.getVideoEnd());

		setTag(scene, null, OutputData.TAG_JSON_VALE_SCENE_CONTENT);
	}

	@Override
	public void insertVideoFileScene(Region.Editor regionEditor, int videoId, int index) {
		VideoFileScene scene = regionEditor.addScene(VideoFileScene.class, index);
		VideoFileScene.Editor sceneEditor = scene.getEditor();
		setVideoFileScene(sceneEditor, videoId, 0, 0);

		setTag(scene, null, OutputData.TAG_JSON_VALE_SCENE_CONTENT);
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
	private void setVideoFileScene(VideoFileScene.Editor sceneEditor, int videoId, long videoStart, long videoEnd) {
		UplusAnalysisPersister persister = UplusAnalysisPersister.getAnalysisPersister(mContext.getApplicationContext());
		Cursor cursor = persister.getVideoDataCursorInAnalysis(videoId);
		String videoPath = null;
		if (cursor != null) {
			L.d("Analyzed Video id = " + videoId);
			cursor.moveToNext();
			videoPath = cursor.getString(cursor.getColumnIndexOrThrow(UplusAnalysisConstants.VideoAnalysisDatabaseField.VIDEO_PATH));
			long startPosition;
			long endPosition;
			if (videoStart != 0 && videoEnd != 0) {
				startPosition = videoStart;
				endPosition = videoEnd;
			} else {
				startPosition = cursor.getLong(cursor.getColumnIndexOrThrow(UplusAnalysisConstants.VideoAnalysisDatabaseField.START_POSITION));
				endPosition = cursor.getLong(cursor.getColumnIndexOrThrow(UplusAnalysisConstants.VideoAnalysisDatabaseField.END_POSITION));
			}
			sceneEditor.setVideoFilePath(videoPath);
			sceneEditor.setVideoClip(startPosition, endPosition);
			sceneEditor.setDuration((int) (endPosition - startPosition));
			sceneEditor.setVideoId(videoId);
			cursor.close();
		} else {
			cursor = persister.getVideoDataCursorInGallery(videoId);
			if (cursor != null) {
				L.d("Video id = " + videoId);
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
				int position = (int) (duration / DURATION_DIVIDER);
				long startPosition;
				long endPosition;
				if (position > 0) {
					if (videoStart != 0 && videoEnd != 0) {
						startPosition = videoStart;
						endPosition = videoEnd;
					} else {
						startPosition = (long) ((int) (Math.random() * position) * DURATION_DIVIDER);
						endPosition = startPosition + DURATION_DIVIDER > duration ? duration : startPosition + DURATION_DIVIDER;
					}
					sceneEditor.setVideoClip(startPosition, endPosition);
					sceneEditor.setVideoId(videoId);
					sceneEditor.setDuration((int) (endPosition - startPosition));
				} else {
					if (videoStart != 0 && videoEnd != 0) {
						startPosition = videoStart;
						endPosition = videoEnd;
					} else {
						startPosition = 0;
						endPosition = duration;
					}
					sceneEditor.setVideoId(videoId);
					sceneEditor.setVideoClip(startPosition, endPosition);
					sceneEditor.setDuration((int) (endPosition - startPosition));
				}
				cursor.close();
			}
		}
		// 20150225 olive : #10672 VideoFileScene에 필터 적용 안되도록 임시 주석 처리
		// fixes_#11257 (keylime_20150331) : for video filter test
		if (mUplusOutputData.getTheme().themeType == ThemeType.FILTER) {
			sceneEditor.setFilterId(mUplusOutputData.getTheme().filterId);
		} else {
			setFrameEffect(sceneEditor);
			sceneEditor.setFilterId(Constants.INVALID_FILTER_ID);
		}
	}

	/**
	 * 현재 테마에 맞게 frame에 effect가 있다면 VideoFileScene editor에 설정한다.<br>
	 * 
	 * @param sceneEditor
	 *            : 비디오 scene editor.<br>
	 */
	private void setFrameEffect(VideoFileScene.Editor sceneEditor) {

		Frame selectedFrame = null;
		String overlayPath = null;
		ArrayList<Frame> allFrames = new ArrayList<Frame>();

		if (mUplusOutputData.getFrames() == null) {
			L.d("Frame is null");
			return;
		}
		for (Frame _frame : mUplusOutputData.getFrames()) {
			// fixes #12298 비디오씬 디자인 theme overlay시 love, birthday, baby는 특정 Frame만 설정되도록 적용(비디오씬이
			// 가려져서 보이는 문제)
			if (_frame.frameCount == UplusImageFileSceneCoordinator.FRAME_COUNT_SINGLE && _frame.frameType == FrameType.CONTENT) {
				if (mUplusOutputData.getTheme().name.equalsIgnoreCase(Theme.THEME_NAME_LOVE)) {
					if (_frame.frameImageName.equalsIgnoreCase("love_image_frame7") || _frame.frameImageName.equalsIgnoreCase("love_image_frame3")) {
						allFrames.add(_frame);
					}
				} else if (mUplusOutputData.getTheme().name.equalsIgnoreCase(Theme.THEME_NAME_BABY)) {
					if (_frame.frameImageName.equalsIgnoreCase("baby_img_frame_g")) {
						allFrames.add(_frame);
					}
				} else if (mUplusOutputData.getTheme().name.equalsIgnoreCase(Theme.THEME_NAME_BIRTHDAY)) {
					if (_frame.frameImageName.equalsIgnoreCase("birthday_img_frame3") || _frame.frameImageName.equalsIgnoreCase("birthday_img_frame4")) {
						allFrames.add(_frame);
					}
				} else {
					allFrames.add(_frame);
				}
			}
		}
		L.d("allFrames.size = " + allFrames.size());

		if (allFrames == null || allFrames.isEmpty()) {
			return;
		}

		selectedFrame = allFrames.get((int) (Math.random() * allFrames.size()));
		L.d("frame type : " + selectedFrame.frameType + ", count : " + selectedFrame.frameCount);

		if (selectedFrame.frameImageName != null) {
			OverlayEffect.Editor overlayEditor = sceneEditor.addEffect(OverlayEffect.class).getEditor();
			overlayPath = mUplusOutputData.getTheme().combineDowloadImageFilePath(mContext, selectedFrame.frameImageName, "png");
			L.d("selectedFrame name " + selectedFrame.frameImageName + ", overlay effect path : " + overlayPath);
			overlayEditor.setImageFile(overlayPath, Resolution.FHD);
		} else {
			L.d("selectedFrame name is null");
		}

		if (selectedFrame.objects != null) {
			UplusEffectApplyManager effectApplyManager = new UplusEffectApplyManager();
			effectApplyManager.applyFrameEffect(mContext, mUplusOutputData, selectedFrame.objects, sceneEditor);
		} else {
			L.d("Object is null");
		}
	}

}
