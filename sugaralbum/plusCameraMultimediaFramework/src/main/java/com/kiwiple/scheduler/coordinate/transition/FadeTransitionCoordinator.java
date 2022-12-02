package com.kiwiple.scheduler.coordinate.transition;

import com.kiwiple.multimedia.canvas.FadeTransition;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.scheduler.data.TransitionData;

public class FadeTransitionCoordinator extends TransitionCoordinator {

	@Override
	public void applyTransition(Editor regionEditor, int transitionIndex) {
		FadeTransition.Editor transitionEditor = regionEditor.replaceTransition(FadeTransition.class, transitionIndex).getEditor();
		transitionEditor.setDuration(TRANSITION_DEFAULT_DURATION);
	}

	@Override
	public void applyTransition(Editor regionEditor, int i, TransitionData transitionData) {
		FadeTransition.Editor transitionEditor = regionEditor.replaceTransition(FadeTransition.class, i).getEditor();
		transitionEditor.setDuration(transitionData.getDuration());
	}
}
