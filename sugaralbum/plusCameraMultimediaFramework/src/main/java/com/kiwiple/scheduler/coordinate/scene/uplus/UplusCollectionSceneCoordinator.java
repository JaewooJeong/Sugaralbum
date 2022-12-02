package com.kiwiple.scheduler.coordinate.scene.uplus;

import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.graphics.Color;

import com.kiwiple.multimedia.canvas.CollectionScene;
import com.kiwiple.multimedia.canvas.CollectionScene.Editor;
import com.kiwiple.multimedia.canvas.CollectionScene.TransitionOrder;
import com.kiwiple.multimedia.canvas.EnterTransition;
import com.kiwiple.multimedia.canvas.EnterTransition.Block;
import com.kiwiple.multimedia.canvas.Region;
import com.kiwiple.multimedia.canvas.SplitTransition;
import com.kiwiple.multimedia.canvas.SplitTransition.Direction;
import com.kiwiple.debug.L;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.scheduler.coordinate.scene.CollectionSceneCoordinator;
import com.kiwiple.scheduler.data.OutputData;
import com.kiwiple.scheduler.data.uplus.UplusOutputData;

public class UplusCollectionSceneCoordinator extends CollectionSceneCoordinator {

	private UplusOutputData mUplusOutputData;
	private Context mContext;

	public UplusCollectionSceneCoordinator() {
	}

	public UplusCollectionSceneCoordinator(Context context, OutputData outputData) {
		mContext = context;
		mUplusOutputData = (UplusOutputData) outputData;
	}

	public void addCollectionScene(Region.Editor regionEditor, Integer... indexes) {
		CollectionScene scene = regionEditor.addScene(CollectionScene.class);
		CollectionScene.Editor sceneEditor = scene.getEditor();
		List<Integer> collectableSceneList = Arrays.asList(indexes);
		setCollectionScene(sceneEditor, collectableSceneList);
		setTag(scene, null, OutputData.TAG_JSON_VALE_SCENE_CONTENT);
	}

	public void addCollectionScene(Region.Editor regionEditor, List<Integer> collectableSceneList) {
		CollectionScene scene = regionEditor.addScene(CollectionScene.class);
		CollectionScene.Editor sceneEditor = scene.getEditor();
		setCollectionScene(sceneEditor, collectableSceneList);
		setTag(scene, null, OutputData.TAG_JSON_VALE_SCENE_CONTENT);
	}

	private void setCollectionScene(Editor sceneEditor, List<Integer> collectableSceneList) {
		sceneEditor.setCollection(collectableSceneList);
		// 임시로 설정하엿으나, transition을 추가하는 메소드 추가할 것임
		/**
		 * 20150624 SplitTransition (VERTICAL) SplitTransition (HORIZONTAL) EnterTransition
		 * (TWO_WAY_VERTICAL) EnterTransition (TWO_WAY_HORIZONTAL)
		 */

		boolean isFirstScene = true;
		final int ODD = 1;

		for (int index = 0; index < collectableSceneList.size(); index++) {

			if (isFirstScene) {
				isFirstScene = false;
				SplitTransition splitTransition = sceneEditor.addTransition(SplitTransition.class);
				splitTransition.getEditor().setDirection(Direction.VERTICAL).setLineColor(Color.BLACK);
				splitTransition.getEditor().setWhileLineSplit(false);
			} else {
				EnterTransition enterTransition;
				if (index % 2 == ODD) {   
					enterTransition = sceneEditor.addTransition(EnterTransition.class);
					enterTransition.getEditor().setBlocks(new Block(Viewport.FULL_VIEWPORT, EnterTransition.Direction.TWO_WAY_VERTICAL)).setLineColor(Color.BLACK).setLineThickness(4.0f);
				} else {
					enterTransition = sceneEditor.addTransition(EnterTransition.class);
					enterTransition.getEditor().setBlocks(new Block(Viewport.FULL_VIEWPORT, EnterTransition.Direction.TWO_WAY_HORIZONTAL)).setLineColor(Color.BLACK).setLineThickness(4.0f);
				}
			}
		}

		sceneEditor.setTransitionOrder(TransitionOrder.CIRCULAR_LIST);
	}

	public void updateCollectionScene(CollectionScene.Editor editor, List<Integer> collectableSceneList) {
		CollectionScene.Editor sceneEditor = editor;
		L.e("collectableSceneList.size = " + collectableSceneList.size());
		sceneEditor.setCollection(collectableSceneList);
	}
}
