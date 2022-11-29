package com.kiwiple.scheduler.data.uplus.effect;

import com.kiwiple.multimedia.canvas.EnterEffect;
import com.kiwiple.scheduler.data.EffectData;

/**
 * Entereffect 데이터 클래스.
 *
 */
public class UplusEnterEffectData extends EffectData {
	
	public static final String JSON_VALUE_EFFECT_ARRAY = "effects_array";
	public static final String JSON_VALUE_MULTI_EFFECTS_ARRAY = "multi_effects_array";
	
	private String mDirection;
	private boolean mReverse;
	private int mDuration;
	
	/**
	 * 생성자.
	 * @param direction EnterEffect Direction
	 * @param duration EnterEffect duration 
	 */
	public UplusEnterEffectData(String direction, int duration, boolean reverse) {
		super(EnterEffect.JSON_VALUE_TYPE);
		mDirection = direction; 
		mDuration = duration;
		mReverse = reverse;
	}

	/**
	 * EnterEffect Direction
	 * @return direction
	 */
	public String getDiration() {
		return mDirection;
	}

	/**
	 * EnterEffect Duration
	 * @return duration
	 */
	public int getDuration(){
		return mDuration;
	}
	
	/**
	 * EnterEffect Reverse
	 * @return reverse 
	 */
	public boolean getReverse(){
		//transition(overlay), scene_order latter || scene, enter effect, is_reverse false
		//transition(fade) || scene, enter effect, is_reverse true || transition(overlay), scene_order former 
		return mReverse;
	}
}
