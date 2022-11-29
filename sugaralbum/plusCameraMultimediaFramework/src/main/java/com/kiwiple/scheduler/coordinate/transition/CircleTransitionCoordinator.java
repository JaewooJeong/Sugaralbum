package com.kiwiple.scheduler.coordinate.transition;

import com.kiwiple.multimedia.canvas.CircleTransition;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.scheduler.data.TransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusCircleTransitionData;

public class CircleTransitionCoordinator extends TransitionCoordinator {

	@Override
	public void applyTransition(Editor regionEditor, int transitionIndex) {
		CircleTransition.Editor transitionEditor = regionEditor.replaceTransition(CircleTransition.class, transitionIndex).getEditor();
		transitionEditor.setDuration(TRANSITION_DEFAULT_DURATION);
	}

	@Override
	public void applyTransition(Editor regionEditor, int i, TransitionData transitionData) {
		CircleTransition.Editor transitionEditor = regionEditor.replaceTransition(CircleTransition.class, i).getEditor();
		UplusCircleTransitionData circle = (UplusCircleTransitionData)transitionData; 
		transitionEditor.setDuration(circle.getDuration());
		transitionEditor.setZoomIn(circle.isZoomIn());
	}
}
