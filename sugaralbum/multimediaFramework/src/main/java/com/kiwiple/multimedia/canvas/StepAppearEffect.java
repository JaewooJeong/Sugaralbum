package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import android.graphics.Color;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonObject;

/**
 * {@link Scene}이 생성한 이미지가 정해진 시간에 나타나는 효과를 적용하는 클래스.
 */
public final class StepAppearEffect extends Effect {
	
	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "step_appear_effect";
	public static final String JSON_NAME_APPEAR_RATIO = "appear_ratio";
	
	// // // // // Member variable.
	// // // // //
	private float mAppearRatio; 

	// // // // // Constructor.
	// // // // //
	StepAppearEffect(Scene parent) {
		super(parent);
	}
	
	// // // // // Method.
	// // // // //
	@Override
	public Editor getEditor() {
		// TODO Auto-generated method stub
		return (Editor)super.getEditor();
	}
	
	void setStepAppearRatio(float appearRatio){
		Precondition.checkOnlyPositive(appearRatio); 
		
		if(appearRatio > 1.0f){
			appearRatio = 1.0f; 
		}
		mAppearRatio = appearRatio; 
	}

	
	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {
		checkValidity(mAppearRatio > 0.0f, "You must invoke setStepAppearRatio"); 
	}

	@Override
	void onDraw(PixelCanvas dstCanvas) {
		float progressRatio = getProgressRatio();
		if(progressRatio < mAppearRatio){
			dstCanvas.clear(Color.BLACK); 
		}
	}
	
	public int getEffectDuration(){
		return Math.round(getDuration()*mAppearRatio); 
	}
	
	@Override
	public JsonObject toJsonObject() throws JSONException {
		JsonObject jsonObject = super.toJsonObject(); 
		jsonObject.put(JSON_NAME_APPEAR_RATIO, mAppearRatio); 
		return jsonObject; 
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		// TODO Auto-generated method stub
		super.injectJsonObject(jsonObject);
		float appearRatio = jsonObject.getFloat(JSON_NAME_APPEAR_RATIO); 
		setStepAppearRatio(appearRatio); 
	}
	
	// // // // // Inner class.
	// // // // //
	/**
	 * {@link StepAppearEffect}의 일부 기능을 조작하기 위한 클래스. {@link Visualizer}가 편집 모드일 때에만 사용할 수 있습니다.
	 * 
	 * @see Visualizer.Editor
	 */
	public static final class Editor extends Effect.Editor<StepAppearEffect, Editor>{
		
		private Editor(StepAppearEffect stepAppearEffect){
			super(stepAppearEffect); 
		}
		
		/**
		 * 효과가 적용되는 시간을 비율로 설정한다. 
		 * 
		 * @param appearRatio
		 *           {@code 0.0f}에서 {@code 1.0f} 사이, 즉 [{@code 0.0f, 1.0f}]의 값을 가지는 시간 비율.
		 */
		public Editor setAppearRatio(float appearRatio){
			getObject().setStepAppearRatio(appearRatio); 
			return this; 
		}
	}
}
