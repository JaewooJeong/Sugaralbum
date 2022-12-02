package com.kiwiple.scheduler.coordinate.transition;

import com.kiwiple.multimedia.canvas.ExtendBoxTransition;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.multimedia.canvas.data.Viewport;
import com.kiwiple.scheduler.data.TransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusExtendBoxTransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusExtendBoxTransitionData.Style;

public class ExtendBoxTransitionCoordinator extends TransitionCoordinator {

	@Override
	public void applyTransition(Editor regionEditor, int transitionIndex) {

	}

	@Override
	public void applyTransition(Editor regionEditor, int transitionIndex, TransitionData transitionData) {
		ExtendBoxTransition.Editor transitionEditor = regionEditor.replaceTransition(ExtendBoxTransition.class, transitionIndex).getEditor();
		UplusExtendBoxTransitionData extendBoxTransitionData = (UplusExtendBoxTransitionData) transitionData;

		transitionEditor.setBoxColor(extendBoxTransitionData.getBoxColor());
		transitionEditor.setInterval(extendBoxTransitionData.getInterval());
		transitionEditor.setBoxThickness(extendBoxTransitionData.getTickness());
		transitionEditor.setFadeIn(extendBoxTransitionData.getUseFadeIn());
		Style style = extendBoxTransitionData.getStyle();
		
		transitionEditor.removeAllBox();
		switch (style) {
		// fixes #11722 : keylime_20151030 : blocked the full box style because of crash (ExtendBoxTransition)
		// need to insert the box style of extend box transition into scheduler json file
		case FULL_BOX: // full box
			transitionEditor.addBox(new Viewport(0f, 0f, 1f, 1f));
			break;
		case VERTICAL_TWO_BOXES: // vertical 2 boxes
			transitionEditor.addBox(new Viewport(0f, 0f, 0.5f, 1f));
			transitionEditor.addBox(new Viewport(0.5f, 0f, 1f, 1f));
			break;
		case HORIZONTAL_TWO_BOXES: // horizontal 2 boxes
			transitionEditor.addBox(new Viewport(0f, 0f, 1f, 0.5f));
			transitionEditor.addBox(new Viewport(0f, 0.5f, 1f, 1f));
			break;
		default : // 4 boxes
		case FOUR_BOXES: // horizontal 2 boxes
			transitionEditor.addBox(new Viewport(0f, 0f, 0.5f, 0.5f));
			transitionEditor.addBox(new Viewport(0.5f, 0f, 1f, 0.5f));
			transitionEditor.addBox(new Viewport(0f, 0.5f, 0.5f, 1.0f));
			transitionEditor.addBox(new Viewport(0.5f, 0.5f, 1.0f, 1.0f));
			break;
		}
		
		transitionEditor.setDuration(extendBoxTransitionData.getDuration());
	}
}
