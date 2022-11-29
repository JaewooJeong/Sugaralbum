package com.kiwiple.scheduler.coordinate.transition;

import android.graphics.Color;

import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.multimedia.canvas.SplitTransition;
import com.kiwiple.multimedia.canvas.SplitTransition.Direction;
import com.kiwiple.scheduler.data.TransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusSplitTransitionData;

public class SplitTransitionCoordinator extends TransitionCoordinator {

	public static Direction[] SPLIT_TRANSITION_DIRECTION = { SplitTransition.Direction.HORIZONTAL, SplitTransition.Direction.VERTICAL,
			SplitTransition.Direction.DIAGONAL_LEFT_TOP, SplitTransition.Direction.DIAGONAL_LEFT_BOTTOM };
	
	public static Direction[] SPLIT_TRANSITION_H_V_DIRECTION = { SplitTransition.Direction.HORIZONTAL, SplitTransition.Direction.VERTICAL};

	@Override
	public void applyTransition(Editor regionEditor, int transitionIndex) {
		SplitTransition.Editor transitionEditor = regionEditor.replaceTransition(SplitTransition.class, transitionIndex).getEditor();
		transitionEditor.setDuration(TRANSITION_DEFAULT_DURATION);
		transitionEditor.setDirection(SPLIT_TRANSITION_DIRECTION[(int) (Math.random() * SPLIT_TRANSITION_DIRECTION.length)]);
		transitionEditor.setLineColor(Color.BLACK);
	}


	@Override
	public void applyTransition(Editor regionEditor, int i, TransitionData transitionData) {
		SplitTransition.Editor transitionEditor = regionEditor.replaceTransition(SplitTransition.class, i).getEditor();
		UplusSplitTransitionData splitData = (UplusSplitTransitionData)transitionData;
		
		transitionEditor.setDuration(splitData.getDuration());
		transitionEditor.setDirection(splitData.getDirection());
		transitionEditor.setLineColor(splitData.getLine_color());
		transitionEditor.setWhileLineSplit(splitData.getWhiteLineSplit());
	}
}
