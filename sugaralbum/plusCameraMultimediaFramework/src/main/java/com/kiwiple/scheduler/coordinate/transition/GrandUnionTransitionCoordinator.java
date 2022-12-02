package com.kiwiple.scheduler.coordinate.transition;

import com.kiwiple.multimedia.canvas.GrandUnionTransition;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.scheduler.data.TransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusGrandUnionTransitionData;

public class GrandUnionTransitionCoordinator extends TransitionCoordinator {

	@Override
	public void applyTransition(Editor regionEditor, int transitionIndex) {
	}

	@Override
	public void applyTransition(Editor regionEditor, int i, TransitionData transitionData) {
		GrandUnionTransition.Editor grandUnionEditor = regionEditor.replaceTransition(GrandUnionTransition.class, i).getEditor();
		UplusGrandUnionTransitionData data = (UplusGrandUnionTransitionData) transitionData;
		grandUnionEditor.setDuration(data.getDuration());
		grandUnionEditor.injectPreset(GrandUnionTransition.Preset.BEER_CAN);
		grandUnionEditor.setLineColor(data.getLineColor()); 
	}
}
