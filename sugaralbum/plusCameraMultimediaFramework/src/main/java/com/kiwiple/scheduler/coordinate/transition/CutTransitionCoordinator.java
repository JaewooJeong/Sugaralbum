package com.kiwiple.scheduler.coordinate.transition;

import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.scheduler.data.TransitionData;

public class CutTransitionCoordinator extends TransitionCoordinator {


	@Override
	public void applyTransition(Editor regionEditor, int transitionIndex) {
		regionEditor.replaceTransition(null, transitionIndex);
	}

	@Override
	public void applyTransition(Editor regionEditor, int i, TransitionData transitionData) {
		regionEditor.replaceTransition(null, i);

	}

}
