package com.kiwiple.scheduler.coordinate.transition;

import com.kiwiple.multimedia.canvas.FlashTransition;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.scheduler.data.TransitionData;

public class FlashTransitionCoordinator extends TransitionCoordinator {

	@Override
	public void applyTransition(Editor regionEditor, int transitionIndex) {
		FlashTransition.Editor transitionEditor = regionEditor.replaceTransition(FlashTransition.class, transitionIndex).getEditor();
		transitionEditor.setDuration(TRANSITION_DEFAULT_DURATION);
	}

	@Override
	public void applyTransition(Editor regionEditor, int i, TransitionData transitionData) {
		FlashTransition.Editor transitionEditor = regionEditor.replaceTransition(FlashTransition.class, i).getEditor();
		transitionEditor.setDuration(transitionData.getDuration());
	}
}
