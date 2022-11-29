package com.kiwiple.scheduler.coordinate.transition;

import com.kiwiple.multimedia.canvas.EnterTransition;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.scheduler.data.TransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusEnterTransition;

public class EnterTransitionCoordinator extends TransitionCoordinator {

	@Override
	public void applyTransition(Editor regionEditor, int transitionIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void applyTransition(Editor regionEditor, int i, TransitionData transitionData) {
		EnterTransition.Editor enterEditor = regionEditor.replaceTransition(EnterTransition.class, i).getEditor();
		
		UplusEnterTransition data = (UplusEnterTransition) transitionData;
		enterEditor.setDuration(data.getDuration()); 
		enterEditor.setLineColor(data.getLineColor()); 
		enterEditor.setLineThickness(data.getLineThickness()); 
		enterEditor.setReverse(data.isReverse()); 
		enterEditor.setBlocks(data.getBlockList()); 
	}

}
