package com.kiwiple.multimedia.canvas;

import org.json.JSONException;

import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * center에서 현재 씬이 zoom in되거나 다음씬이 zoom out 되면서 화면 전환이 이루어 지는 효과를 적용하기 위해서 사용한다.
 * zoom in / out 상태는 설정할 수 있다.
 * @author aubergine
 *
 */
public final class CircleTransition extends Transition{
	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "circle_transition";
	public static final String JSON_NAME_CIRCLE_DIRECTION = "zoom_in"; 
	
	
	// // // // //local cariable
	// // // // // 
	private boolean mZoomIn = false; // false = zoom out (former base, zoom out latter) , true= zoom in( latter base,  zoom in former)
	private final int mask_color = 0xff000000;
	private final int mask_clear = 0x00000000;
	
	CircleTransition(Region parent) {
		super(parent);
	}
	
	// // // // // Method.
	// // // // //
	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}
	
	@Override
	public JsonObject toJsonObject() throws JSONException {
		JsonObject jsonObject = super.toJsonObject();
		jsonObject.put(JSON_NAME_CIRCLE_DIRECTION, mZoomIn);
		return jsonObject;
	}
	
	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);
		this.mZoomIn = jsonObject.getBoolean(JSON_NAME_CIRCLE_DIRECTION);
	}

	@Override
	void onDraw(PixelCanvas srcCanvasFormer, PixelCanvas srcCanvasLatter, PixelCanvas dstCanvas) {
		float ratio = (mZoomIn)? getProgressRatio():1.0f - getProgressRatio();
		//make mask canvas
		PixelCanvas maskCanvas = getCanvas(0);
		maskCanvas.clear(mask_clear);
		//draw circle 
		int width = dstCanvas.getImageWidth();
		int height = dstCanvas.getImageHeight();
		int x= width/2;
		int y = height/2;
		// get radius from progress state
		int radius = width/2;
		if (width < height)
			radius = height/2;
		radius *= ratio;		
		maskCanvas.fillOval(mask_color, x, y, radius, radius);
		//blend image
		if(!mZoomIn){
			srcCanvasLatter.copy(dstCanvas);
			srcCanvasFormer.blendWithMask(dstCanvas, maskCanvas);
			//dstCanvas.fillOval(mask_color, x, y, radius, radius);//test fillOval
		}
		else {
			srcCanvasFormer.copy(dstCanvas);
			srcCanvasLatter.blendWithMask(dstCanvas, maskCanvas);
			//dstCanvas.fillOval(mask_color, x, y, radius, radius);//test fillOval
		}
	}

	@Override
	Size[] getCanvasRequirement() {
		Resolution resolution = getResolution();
		Size tmp = resolution.getSize();
		Size maskSize;
		if(tmp.width>tmp.height) maskSize = new Size(tmp.width,tmp.width);
		else maskSize = new Size(tmp.height,tmp.height);
		return new Size[]{maskSize};
	}

	void setZoomIn(boolean zoomin){
		this.mZoomIn = zoomin;
	}
	// // // // // Inner Class.
	// // // // //
	public static final class Editor extends Transition.Editor<CircleTransition, Editor>{
		private Editor(CircleTransition circleTrasition) {
			super(circleTrasition);
		}
		
		public Editor setZoomIn(boolean zoomin){
			getObject().setZoomIn(zoomin);
			return this;
		}
	}
}
