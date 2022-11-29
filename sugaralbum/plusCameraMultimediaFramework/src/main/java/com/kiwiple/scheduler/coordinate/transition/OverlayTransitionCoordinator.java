package com.kiwiple.scheduler.coordinate.transition;

import com.kiwiple.multimedia.canvas.OverlayTransition;
import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.multimedia.canvas.Transition.SceneOrder;
import com.kiwiple.multimedia.json.JsonUtils;
import com.kiwiple.scheduler.data.TransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusOverlayTransitionData;

public class OverlayTransitionCoordinator extends TransitionCoordinator {

	public static final SceneOrder[] sOrders = { SceneOrder.FORMER, SceneOrder.LATTER };  
	@Override
	public void applyTransition(Editor regionEditor, int transitionIndex) {
		// TODO Auto-generated method stub

	}

	@Override
	public void applyTransition(Editor regionEditor, int i, TransitionData transitionData) {
		OverlayTransition.Editor editor = regionEditor.replaceTransition(OverlayTransition.class, i).getEditor(); 
		UplusOverlayTransitionData overlay = (UplusOverlayTransitionData)transitionData; 
		editor.setDuration(overlay.getDuration());
//		editor.setFrontScene(Order.byName(overlay.getOrder()));
		int orderNum = 0; 
		if (overlay.getOrder().equals(JsonUtils.toJsonString(SceneOrder.FORMER))) {
			orderNum = 0;
		}else{
			orderNum = 1; 
		}
		editor.setFrontScene(sOrders[orderNum]);
	}

}
