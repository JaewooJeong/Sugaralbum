package com.kiwiple.scheduler.coordinate.scene;

import com.kiwiple.multimedia.canvas.DrawableImageResource;
import com.kiwiple.multimedia.canvas.Region;

public abstract class DrawableSceneCoordinator extends SceneCoordinator {
	/**
	 * drawble scene 추가.
	 * 
	 * @param regionEditor
	 *            : 현재 편집되고 있는 region editor.<br>
	 * @param drawableId
	 *            : drawable id.
	 * @param duration
	 *            : scene 길이.
	 */
	public abstract void addDrawableScene(Region.Editor regionEditor, DrawableImageResource drawable, int duration);

	/**
	 * drawble scene 추가.
	 * 
	 * @param regionEditor
	 *            : 현재 편집되고 있는 region editor.<br>
	 * @param drawableId
	 *            : drawable id.
	 */
	public abstract void addDrawableScene(Region.Editor regionEditor, DrawableImageResource drawable);

	/**
	 * 후보정에서 drawble scene 추가.
	 * 
	 * @param regionEditor
	 *            : 현재 편집되고 있는 region editor.<br>
	 * @param drawableId
	 *            : drawable id.
	 * @param duration
	 *            : scene 길이.
	 * @param index
	 *            : 삽일할 위치.
	 */
	public abstract void insertDrawableScene(Region.Editor regionEditor, DrawableImageResource drawable, int duration, int index);

	/**
	 * 후보정에서 drawble scene 추가.
	 * 
	 * @param regionEditor
	 *            : 현재 편집되고 있는 region editor.<br>
	 * @param drawableId
	 *            : drawable id.
	 * @param index
	 *            : 삽일할 위치.
	 */
	public abstract void insertDrawableScene(Region.Editor regionEditor, DrawableImageResource drawable, int index);
}
