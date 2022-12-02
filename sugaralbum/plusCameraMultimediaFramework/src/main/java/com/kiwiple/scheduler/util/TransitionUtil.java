package com.kiwiple.scheduler.util;

import org.json.JSONException;
import org.json.JSONObject;

import com.kiwiple.multimedia.canvas.EnterTransition;
import com.kiwiple.multimedia.canvas.OverlayTransition;
import com.kiwiple.multimedia.canvas.Transition;
import com.kiwiple.multimedia.canvas.Transition.SceneOrder;
import com.kiwiple.multimedia.json.JsonUtils;

public class TransitionUtil {

	public static boolean isEnterTransitionReverse(JSONObject jsonObject) {
		try {
			if(jsonObject.getString(Transition.JSON_NAME_TYPE).equals(EnterTransition.JSON_VALUE_TYPE)){
				return jsonObject.getBoolean(EnterTransition.JSON_NAME_IS_REVERSE); 
				
			}else{
				return false; 
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false; 
		}
		
	}
}
