package com.kiwiple.scheduler.coordinate.transition;

import static com.kiwiple.multimedia.canvas.Transition.SceneOrder.FORMER;
import static com.kiwiple.multimedia.canvas.Transition.SceneOrder.LATTER;

import java.util.Random;

import com.kiwiple.multimedia.canvas.Region.Editor;
import com.kiwiple.multimedia.canvas.SpinTransition;
import com.kiwiple.multimedia.canvas.SpinTransition.Direction;
import com.kiwiple.multimedia.canvas.Transition.SceneOrder;
import com.kiwiple.scheduler.data.TransitionData;
import com.kiwiple.scheduler.data.uplus.transition.UplusSpinTransitionData;

public class SpinTransitionCoordinator extends TransitionCoordinator {

	public static final SceneOrder[][] sTypes = { {FORMER, FORMER}, {FORMER, FORMER, FORMER, LATTER} };
	public static final Direction[] sDirections = { Direction.LEFT, Direction.UP, Direction.RIGHT, Direction.DOWN };
	
	@Override
	public void applyTransition(Editor regionEditor, int transitionIndex) {
		SpinTransition.Editor spinEditor = regionEditor.replaceTransition(SpinTransition.class, transitionIndex).getEditor();
		spinEditor.setDuration(TRANSITION_DEFAULT_DURATION);
		
		Random random = new Random();
		spinEditor.setSpinOrder(sTypes[random.nextInt(sTypes.length)]);
		spinEditor.setDirection(sDirections[random.nextInt(sDirections.length)]);
		spinEditor.setOvershoot(random.nextBoolean()); 
	}

	@Override
	public void applyTransition(Editor regionEditor, int i, TransitionData transitionData) {
		SpinTransition.Editor editor = regionEditor.replaceTransition(SpinTransition.class, i).getEditor();
		UplusSpinTransitionData spin = (UplusSpinTransitionData) transitionData;
		//각 속성들은 default duration을 가질수 있으므로  duration은 맨 나중에 호출 한다.  
		editor.setSpinOrder(spin.getSceneOrderArray());
		editor.setDirection(spin.getDirection());
		editor.setOvershoot(spin.isOvershoot());
		editor.setBlurredBorder(spin.isBlurredBorder()); 
		editor.setDuration(spin.getDuration());
		editor.setInterpolator(spin.getInterpolator()); 
	}
}
