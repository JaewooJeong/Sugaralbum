package com.kiwiple.scheduler.coordinate.scene;

import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.scheduler.data.SelectedOutputData;

public abstract class VideoFileSceneCoordinator extends SceneCoordinator {
	/**
	 * video data를 region editor에 추가 한다.
	 * 
	 * @param regionEditor
	 *            : 현재 편집되는 region editor.
	 * @param selectedOutputData
	 *            : 삽입될 data.
	 */
	public abstract void addVideoFileScene(Region.Editor regionEditor, SelectedOutputData selectedOutputData);

	/**
	 * 후보정에서 video data를 region editor에 추가 한다.
	 * 
	 * @param regionEditor
	 *            : 현재 편집되는 region editor.
	 * @param videoId
	 *            : 삽입될 video data id.
	 * @param index
	 *            : 삽입될 위치.
	 */
	public abstract void insertVideoFileScene(Region.Editor regionEditor, int videoId, int index);
}
