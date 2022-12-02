package com.kiwiple.scheduler.coordinate.transition;

import java.util.Random;

import android.graphics.Color;

import com.kiwiple.multimedia.canvas.MetroTransition;
import com.kiwiple.multimedia.canvas.MetroTransition.Direction;
import com.kiwiple.multimedia.canvas.MetroTransition.SliceOrder;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.multimedia.json.JsonUtils;
import com.kiwiple.scheduler.data.TransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusMetroTransitionData;

public class MetrotransitionCoordinator extends TransitionCoordinator {
	
	public static final SliceOrder[] sSliceOrders = { SliceOrder.NON_SEQUENTIAL_RANDOMIZED, SliceOrder.SEQUENTIAL_FROM_LEFT_OR_TOP, SliceOrder.SEQUENTIAL_FROM_RIGHT_OR_BOTTOM};
	public static final Direction[] sDirections = { Direction.DOWN, Direction.LEFT, Direction.RIGHT, Direction.UP};
	public static final Integer[] sSliceCount = { 3,4,5,6}; 
	public static final Integer[] sLineColor = { Color.YELLOW, Color.RED, Color.BLACK}; 

	@Override
	public void applyTransition(Editor regionEditor, int transitionIndex) {
		MetroTransition.Editor transitionEditor = regionEditor.replaceTransition(MetroTransition.class, transitionIndex).getEditor();
		Random random = new Random();
		transitionEditor.setDuration(TRANSITION_DEFAULT_DURATION);		
		transitionEditor.setDirection(sDirections[random.nextInt(sDirections.length)]); 
		transitionEditor.setSliceCount(sSliceCount[random.nextInt(sSliceCount.length)]); 
		transitionEditor.setSliceOrder(sSliceOrders[random.nextInt(sSliceOrders.length)]);
		transitionEditor.setLineColor(sLineColor[random.nextInt(sLineColor.length)]); 
	}

	@Override
	public void applyTransition(Editor regionEditor, int i, TransitionData transitionData) {
		MetroTransition.Editor transitionEditor = regionEditor.replaceTransition(MetroTransition.class, i).getEditor();
		UplusMetroTransitionData metroData = (UplusMetroTransitionData)transitionData; 
		transitionEditor.setDuration(metroData.getDuration()); 
		transitionEditor.setDirection(JsonUtils.getEnumByJsonString(metroData.getDirection(), Direction.class)); 
		transitionEditor.setSliceCount(metroData.getSliceCount()); 
		transitionEditor.setSliceOrder(JsonUtils.getEnumByJsonString(metroData.getSliceOrder(), SliceOrder.class)); 
		transitionEditor.setLineColor(metroData.getLineColor()); 

	}

}
