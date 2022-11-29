package com.kiwiple.scheduler.data.uplus.transition;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.kiwiple.multimedia.canvas.EnterTransition;
import com.kiwiple.multimedia.canvas.EnterTransition.Block;
import com.kiwiple.multimedia.canvas.EnterTransition.Direction;
import com.kiwiple.multimedia.canvas.KenBurnsScaler;
import com.kiwiple.multimedia.canvas.LayerScene;
import com.kiwiple.multimedia.canvas.MultiLayerScene;
import com.kiwiple.multimedia.canvas.Scene;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.multimedia.json.JsonUtils;
import com.kiwiple.scheduler.data.TransitionData;
import com.kiwiple.scheduler.util.ViewportUtils;

public class UplusEnterTransition extends TransitionData {

	String direction;
	int lineColor;
	float lineThickness;
	boolean isReverse;
	Scene frontScene; 
	Scene backScene; 

	public UplusEnterTransition(JSONObject transitionJsonObject,int duration, Scene frontScene, Scene backScene) throws JSONException {
		super(duration, EnterTransition.JSON_VALUE_TYPE);
		this.direction = transitionJsonObject.getString(EnterTransition.JSON_NAME_DIRECTION);
		this.lineColor = transitionJsonObject.getInt(EnterTransition.JSON_NAME_LINE_COLOR);
		this.lineThickness = (float) transitionJsonObject.getDouble(EnterTransition.JSON_NAME_LINE_THICKNESS);
		this.isReverse = transitionJsonObject.getBoolean(EnterTransition.JSON_NAME_IS_REVERSE);
		this.frontScene = frontScene; 
		this.backScene = backScene;
	}
	
	public EnterTransition.Direction getDirection() {
		return JsonUtils.getEnumByJsonString(direction, EnterTransition.Direction.class);
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public int getLineColor() {
		return lineColor;
	}

	public void setLineColor(int lineColor) {
		this.lineColor = lineColor;
	}

	public float getLineThickness() {
		return lineThickness;
	}

	public void setLineThickness(float lineThickness) {
		this.lineThickness = lineThickness;
	}

	public boolean isReverse() {
		return isReverse;
	}

	public void setReverse(boolean isReverse) {
		this.isReverse = isReverse;
	}

	public List<Block> getBlockList() {
		List<Block> blockList = new ArrayList<Block>();
		if (isReverse()) {
			// n씬이 나가는 효과. n+1씬이 바닥에 깔린다. n씬에 따라서 block을 구성해야 함.
			if (frontScene.getClass().equals(MultiLayerScene.class)) {
				blockList = getMultiLayerBlockList((MultiLayerScene) frontScene, isReverse());
			} else {
				Block block = new Block(Viewport.FULL_VIEWPORT, getDirection());
				blockList.add(block);
			}
		} else {
			if (backScene.getClass().equals(MultiLayerScene.class)) {
				blockList = getMultiLayerBlockList((MultiLayerScene) backScene, isReverse());
			} else {
				Block block = new Block(Viewport.FULL_VIEWPORT, getDirection());
				blockList.add(block);
			}
		}
		return blockList;
	}

	private List<Block> getMultiLayerBlockList(MultiLayerScene multiLayerScene, boolean reverse) {

		List<Block> blockList = new ArrayList<Block>();
		for (Scene layerScene : multiLayerScene.getLayers()) {
			if (layerScene.getClass().equals(LayerScene.class)) {
				LayerScene multiLayerImageScene = (LayerScene) layerScene;

				Direction layerDirection = Direction.ONE_WAY_LEFT;

				// TODO check kenburn viewport
				Viewport[] viewport = ((KenBurnsScaler) multiLayerImageScene.getScaler()).getViewports();
				// L.e("viewport[0] :"+viewport[0].left+", "+viewport[0].top+", "+viewport[0].right+", "+viewport[0].bottom);
				// L.e("viewport[1] :"+viewport[1].left+", "+viewport[1].top+", "+viewport[1].right+", "+viewport[1].bottom);

				ViewportUtils.Direction kenburnDirection = ViewportUtils.measureDirection(viewport[0], viewport[1]);

				if (kenburnDirection == ViewportUtils.Direction.RIGHT) {
					if (reverse)
						layerDirection = Direction.ONE_WAY_RIGHT;
					else
						layerDirection = Direction.ONE_WAY_LEFT;
				} else if (kenburnDirection == ViewportUtils.Direction.LEFT) {
					if (reverse)
						layerDirection = Direction.ONE_WAY_LEFT;
					else
						layerDirection = Direction.ONE_WAY_RIGHT;
				} else if (kenburnDirection == ViewportUtils.Direction.UP) {
					if (reverse)
						layerDirection = Direction.ONE_WAY_UP;
					else
						layerDirection = Direction.ONE_WAY_DOWN;
				} else if (kenburnDirection == ViewportUtils.Direction.DOWN) {
					if (reverse)
						layerDirection = Direction.ONE_WAY_DOWN;
					else
						layerDirection = Direction.ONE_WAY_UP;
				}
				Viewport layerViewPort = multiLayerScene.getLayerViewport(multiLayerImageScene);
				blockList.add(new Block(layerViewPort, layerDirection));
			}
		}
		return blockList;
	}
}
