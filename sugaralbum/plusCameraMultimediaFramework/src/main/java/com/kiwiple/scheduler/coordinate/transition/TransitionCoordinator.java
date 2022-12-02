package com.kiwiple.scheduler.coordinate.transition;

import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.scheduler.data.TransitionData;

public abstract class TransitionCoordinator {
	protected static final int TRANSITION_DEFAULT_DURATION = 2000;
	
	/**
	 * i번째 transition을 삽입.<br>
	 * @param regionEditor : 현재 편집되고 있는 regionEditor.<br>
	 * @param transitionIndex : 삽입 위치.<br>
	 */
	public abstract void applyTransition(Editor regionEditor, int transitionIndex);
	
	/**
	 * i번째 transitionData를 삽입.<br>
	 * @param regionEditor : 현재 편집되고 있는 regionEditor.<br>
	 * @param i : 삽입 위치.<br>
	 * @param transitionData : transition data. <br>
	 */
	public abstract void applyTransition(Editor regionEditor, int i, TransitionData transitionData);
}
