package com.kiwiple.scheduler.coordinate.scene.uplus;

import com.kiwiple.multimedia.canvas.DrawableImageResource;
import com.kiwiple.multimedia.canvas.DrawableScene;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.scheduler.coordinate.scene.DrawableSceneCoordinator;

public class UplusDrawableSceneCoordinator extends DrawableSceneCoordinator {

	@Override
	public void addDrawableScene(Editor regionEditor, DrawableImageResource drawable, int duration) {
		DrawableScene.Editor sceneEditor = regionEditor.addScene(DrawableScene.class).getEditor();
		setDrawableScene(sceneEditor, drawable, duration);
	}

	@Override
	public void addDrawableScene(Editor regionEditor, DrawableImageResource drawable) {
		addDrawableScene(regionEditor, drawable, 5000);
	}

	@Override
	public void insertDrawableScene(Editor regionEditor, DrawableImageResource drawable, int duration, int index) {
		DrawableScene.Editor sceneEditor = regionEditor.addScene(DrawableScene.class, index).getEditor();
		setDrawableScene(sceneEditor, drawable, duration);
	}

	@Override
	public void insertDrawableScene(Editor regionEditor, DrawableImageResource drawable, int index) {
		insertDrawableScene(regionEditor, drawable, 5000, index);
	}

	private void setDrawableScene(DrawableScene.Editor sceneEditor, DrawableImageResource drawable, int duration) {
		sceneEditor.setDrawable(drawable);
		sceneEditor.setDuration(duration);
	}
}
