package com.kiwiple.multimedia.canvas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.graphics.Bitmap;

import com.kiwiple.multimedia.R;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * 광원 효과를 제공하는 클래스 
 * 
 * 광원의 적용 범위, 컬러(레드, 엘로우), 광원수, 광원시작 위치, 광원 종료위 값을 설정하여 적용된다. 광원의 타입에 따라서 타입값이 FULL,
 * CIRCLE, PENTAGON이 될 수 있다..
 *
 */
public final class LightEffect extends Effect {

	/**
	 * 광원의 색상을 지정한다. 지원하는 색상값은 타입에 따라 다르게 적용된다. 기본값은 {@link JSON_VALUE_COLOR_YELLOW } 이다.
	 * 관원 타입이 {@link JSON_VALUE_TYPE_PENTAGON}, {@link JSON_VALUE_TYPE_PENTAGON_A}, {@link JSON_VALUE_TYPE_PENTAGON_B}, {@link JSON_VALUE_TYPE_PENTAGON_C}의 경우, 
	 * 적용 가능한 색상값은 {@link JSON_VALUE_COLOR_YELLOW }, {@link JSON_VALUE_COLOR_RED}, {@link JSON_VALUE_COLOR_BLUE } 이 
	 */
	public static final String JSON_NAME_COLOR = "lighting_color";
	/**
	 * 광원의 크기를 지정한다.  full인경우, 해당 값은 무시된다. 
	 */
	public static final String JSON_NAME_SCALE = "lighting_scale";
	/**
	 * 광원모양을 지정한다. 지원하는 모양 값은 {@link JSON_VALUE_TYPE_FULL}에서  {@link JSON_VALUE_TYPE_BLINGBLING_C}까지의 값이다. 
	 */
	public static final String JSON_NAME_DRAW_TYPE = "lighting_type";
	
	public static final String JSON_NAME_OBJECT = "lighting_object";
	public static final String JSON_NAME_MOVE_FROM_X = "lighting_move_from_x";
	public static final String JSON_NAME_MOVE_FROM_Y = "lighting_move_from_y";
	public static final String JSON_NAME_MOVE_TO_X = "lighting_move_to_x";
	public static final String JSON_NAME_MOVE_TO_Y = "lighting_move_to_y";

	public static final String JSON_VALUE_TYPE = "light_effect";
	public static final String JSON_VALUE_COLOR_RED = "color_red";
	public static final String JSON_VALUE_COLOR_YELLOW = "color_yellow";
	public static final String JSON_VALUE_COLOR_BLUE = "color_blue";
	public static final String JSON_VALUE_COLOR_BROWN = "color_brown";
	public static final String JSON_VALUE_TYPE_FULL = "type_full";
	public static final String JSON_VALUE_TYPE_CIRCLE = "type_circle";
	public static final String JSON_VALUE_TYPE_CIRCLE_A = "type_circle_a";
	public static final String JSON_VALUE_TYPE_CIRCLE_B = "type_circle_b";
	public static final String JSON_VALUE_TYPE_CIRCLE_C = "type_circle_c";
	public static final String JSON_VALUE_TYPE_PENTAGON = "type_pentagon";
	public static final String JSON_VALUE_TYPE_PENTAGON_A = "type_pentagon_a";
	public static final String JSON_VALUE_TYPE_PENTAGON_B = "type_pentagon_b";
	public static final String JSON_VALUE_TYPE_PENTAGON_C = "type_pentagon_c";
	public static final String JSON_VALUE_TYPE_BLINGBLING_A = "type_blingbling_a";
	public static final String JSON_VALUE_TYPE_BLINGBLING_B = "type_blingbling_b";
	public static final String JSON_VALUE_TYPE_BLINGBLING_C = "type_blingbling_c";
	public static final String JSON_VALUE_ALPHA_START = "alpha_start";
	public static final String JSON_VALUE_ALPHA_END = "alpha_end";
	public static final String JSON_VALUE_SET = "set";
	public static final int SCALE_MAX = 250;
	public static final int SCALE_MIN = 200;
	public static final int BOUND_MIN = 50;
	///// local value
	private final int BASE = 128;
	private final float ALPHA_GAP = 0.02f;
	private static final int DURATION = 1;
	private static final int LIGHT_MIN = 50;
	private static final int LIGHT_MAX = 10;
	@CacheCode(indexed = true)
	private List<LightingInfo> lightings = new ArrayList<LightingInfo>();
	
	//auto moving light
	private int light_value = 0;
	private boolean light_plus = true;
	private boolean red_light_color = false;
	//auto moving light
	private boolean ml_fwX, ml_fwY = true;
	//private int ml_scale = SCALE_MIN;
	
	LightEffect(Scene parent) {
		super(parent);
		
	}

	@Override
	void onDraw(PixelCanvas dstCanvas) {
		float progress = getProgressRatio();
		int width = getWidth();
		int height = getHeight();
		int effect_size = lightings.size();
		if (effect_size == 0)
			return;
		/**
		 * draw lighting textures..
		 */
		float sMultiplier = getResolution().magnification/lightings.get(0).mBaseResolution.magnification;
		for (int i = 0; i < effect_size; i++) {
			LightingInfo current = lightings.get(i);
			
			if (current.type == LightingInfo.LIGHT_TYPE_FULL) {
				if (light_plus) {
					light_value += DURATION;
					if (current.scale > SCALE_MIN)
						current.scale --;
					if (light_value > LIGHT_MIN)
						light_plus = false;

				} else {
					light_value -= DURATION;
					if (current.scale  < SCALE_MAX)
						current.scale ++;
					if (light_value < LIGHT_MAX)
						light_plus = true;
				}
				if (ml_fwX){
					current.cx += sMultiplier;
				}
				else{
					current.cx -= sMultiplier;
				}
				if (ml_fwY){
					current.cy += sMultiplier;
				}
				else{
					current.cy -= sMultiplier;
				}
				if (current.cx >= width - current.scale ) {
					current.cx -= sMultiplier;
					ml_fwX = false;
				} else if (current.cx <= 0) {
					current.cx += sMultiplier;
					ml_fwX = true;
				}
				if (current.cy >= height - current.scale ) {
					current.cy -= sMultiplier;
					ml_fwY = false;
				} else if (current.cy <= 0) {
					current.cy += sMultiplier;
					ml_fwY = true;
				}
				
				int current_scasle = (int)(current.scale *sMultiplier) ;
				
				if (red_light_color) {
					PixelUtils.applyRedLightingEffect(dstCanvas, dstCanvas, light_value);
					PixelUtils.applyCircleLightingEffect(dstCanvas, dstCanvas, current.cx, current.cy, current_scasle, width, height, BASE);
				} else {
					PixelUtils.applyCircleLightingEffect(dstCanvas, dstCanvas, current.cx + (int) (current_scasle * 0.6f), current.cy,current_scasle, width, height, BASE);
					PixelUtils.applyCircleLightingEffect(dstCanvas, dstCanvas, current.cx - (int) (current_scasle * 0.3f), current.cy + (int) (current_scasle * 0.3f),  (int) (current_scasle - current_scasle * 0.2f), width, height, BASE);
					PixelUtils.applyCircleLightingEffect(dstCanvas, dstCanvas, current.cx, current.cy, (int) (current_scasle + current_scasle* 0.1f), width, height, BASE);
					PixelUtils.applyCircleLightingEffect(dstCanvas, dstCanvas, (int) (current.cx + current_scasle),(int) (current.cy - current_scasle), (int) (current_scasle + current_scasle * 0.2f), width, height, BASE);
				}
			} else {
				/**
				 * change position per duration
				 */
				current.cx = (int) ((current.stX + (current.mx * progress))* sMultiplier); 
				current.cy = (int) ((current.stY + (current.my * progress))* sMultiplier);
				// alpha blend texture...
				PixelCanvas pixelCanvas = getCanvas(i);
				pixelCanvas.blend(dstCanvas, current.cx, current.cy, current.alpha);
				// change texture alpha
				if (current.isShowing)
					current.alpha += ALPHA_GAP;
				else
					current.alpha -= ALPHA_GAP;
				if (current.alpha < current.start_alpha)
					current.isShowing = true;
				if (current.alpha > current.end_alpha)
					current.isShowing = false;
			}
		}
	}
	
	@Override
	void prepareCanvasWithCache() throws IOException {
		if(getCacheCount()==0) return;
		for(int i = 0;i<getCacheCount();i++){
			getCacheManager().decodeImageCache(getCacheCodeChunk(i), getCanvas(i));	
		}
	}
	
	
	@Override
	void prepareCanvasWithoutCache() throws IOException {
		if(getCacheCount()==0) return;
		for(int i = 0;i<getCacheCount();i++){
			PixelExtractUtils.extractARGB(createCacheAsBitmap(i), getCanvas(i), true);
		}
	}
	
	@Override
	public int getCacheCount() {
		if (lightings.size() == 1 && lightings.get(0).type == LightingInfo.LIGHT_TYPE_FULL) {
			return 0;
		}
		return lightings.size();
	}
	
	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}
	
	@Override
	Bitmap createCacheAsBitmap(int index) throws IOException {
		Resolution dstResolution = getResolution();

		int type = lightings.get(index).type;
		if (type == LightingInfo.LIGHT_TYPE_FULL){
			return null;
		}
		
		Bitmap drawable;
		if (type <= LightingInfo.LIGHT_TYPE_BLINGBLING_C) {
			drawable = lightings.get(index).mImageResource.createBitmap(dstResolution);
		} else {
			drawable = lightings.get(index).mImageResource.createBitmap(dstResolution, lightings.get(index).scale, null);
		}

		return drawable;
	}
	
	@Override
	Size[] getCanvasRequirement() {
		if (lightings.size() == 1 && lightings.get(0).type == LightingInfo.LIGHT_TYPE_FULL) {
			return DO_NOT_NEED_CANVAS;
		}
		Size[] size = new Size[lightings.size()];
		for (int i = 0; i < lightings.size(); i++) {
			if (lightings.get(i).type == LightingInfo.LIGHT_TYPE_FULL) {
				size[i] = new Size(0,0);
			}
			else {
				size[i] = new Size(lightings.get(i).mImageResource.measureSize(getResolution()).product(), 1);
			}
		}
		return size;
	}

	
	/**
	 * LightingEffect 리소스를 추가한다.
	 * 
	 * 광원은 지정된 위치값의 시작값과 끝값을 Scene duration동아 반복해서 이동한다.
	 * 
	 * 
	 * @param type
	 *            JSON_VALUE_TYPE_FULL, JSON_VALUE_TYPE_CIRCLE,
	 *            JSON_VALUE_TYPE_PENTAGON
	 * @param colortype
	 *            JSON_VALUE_COLOR_RED, JSON_VALUE_COLOR_YELLOW
	 * @param scale
	 *            JSON_VALUE_TYPE_FULL의 경우 값이 의미가 없고, 그 외의 타입에서 사용된다. 이 값은 영역의
	 *            지름값으로 사용된다.
	 * @param fromx
	 *            광원의 이동최소위치
	 * @param fromy
	 *            광원의 이동최소위치
	 * @param tox
	 *            광원의 이동최대위치
	 * @param toy
	 *            이동최대위치
	 */
	void setResource(String type, String colortype, int scale, int fromx, int fromy, int tox, int toy, float startAlpha, float endAlpha, Resolution resolution) {
		lightings.add(new LightingInfo(castTypeValue(type), castColorTypeVaule(colortype), scale, fromx, fromy, tox, toy, startAlpha, endAlpha, resolution));
	}
	
	/**
	 * clear all light resource
	 */
	void clear(){
		lightings.clear();
	}

	/**
	 * change  json value to lighting color
	 * @param color
	 * @return
	 */
	private int castColorTypeVaule(String color) {

		if (color == null) return LightingInfo.LIGHT_COLOR_YELLOW;

		switch (color) {
			case JSON_VALUE_COLOR_RED:
				return LightingInfo.LIGHT_COLOR_RED;
			case JSON_VALUE_COLOR_YELLOW:
				return LightingInfo.LIGHT_COLOR_YELLOW;
			case JSON_VALUE_COLOR_BLUE:
				return LightingInfo.LIGHT_COLOR_BLUE;
			case JSON_VALUE_COLOR_BROWN:
				return LightingInfo.LIGHT_COLOR_BROWN;
		}
		return LightingInfo.LIGHT_COLOR_YELLOW;
	}

	/**
	 * change json value to lighting type
	 * @param type
	 * @return
	 */
	private int castTypeValue(String type) {
		if (type == null)
			return LightingInfo.LIGHT_TYPE_FULL;

		switch (type) {
		case JSON_VALUE_TYPE_FULL:
			return LightingInfo.LIGHT_TYPE_FULL;
		case JSON_VALUE_TYPE_CIRCLE_A:
			return LightingInfo.LIGHT_TYPE_CIRCLE_A;
		case JSON_VALUE_TYPE_CIRCLE_B:
			return LightingInfo.LIGHT_TYPE_CIRCLE_B;
		case JSON_VALUE_TYPE_CIRCLE_C:
			return LightingInfo.LIGHT_TYPE_CIRCLE_C;
		case JSON_VALUE_TYPE_PENTAGON:
			return LightingInfo.LIGHT_TYPE_PENTAGON;
		case JSON_VALUE_TYPE_PENTAGON_A:
			return LightingInfo.LIGHT_TYPE_PENTAGON_A;
		case JSON_VALUE_TYPE_PENTAGON_B:
			return LightingInfo.LIGHT_TYPE_PENTAGON_B;
		case JSON_VALUE_TYPE_PENTAGON_C:
			return LightingInfo.LIGHT_TYPE_PENTAGON_C;
		case JSON_VALUE_TYPE_BLINGBLING_A:
			return LightingInfo.LIGHT_TYPE_BLINGBLING_A;
		case JSON_VALUE_TYPE_BLINGBLING_B:
			return LightingInfo.LIGHT_TYPE_BLINGBLING_B;
		case JSON_VALUE_TYPE_BLINGBLING_C:
			return LightingInfo.LIGHT_TYPE_BLINGBLING_C;
		case JSON_VALUE_TYPE_CIRCLE:
			return LightingInfo.LIGHT_TYPE_CIRCLE;
		}
		return LightingInfo.LIGHT_TYPE_FULL;
	}

	/**
	 * change lighting color to json value
	 * @param color
	 * @return
	 */
	private String castColorTypeVaule(int color) {

		switch (color) {
		case LightingInfo.LIGHT_COLOR_RED:
			return JSON_VALUE_COLOR_RED;
		case LightingInfo.LIGHT_COLOR_YELLOW:
			return JSON_VALUE_COLOR_YELLOW;
		case LightingInfo.LIGHT_COLOR_BLUE:
			return JSON_VALUE_COLOR_BLUE;
		case LightingInfo.LIGHT_COLOR_BROWN:
			return JSON_VALUE_COLOR_BROWN;
		}
		return JSON_VALUE_COLOR_YELLOW;
	}

	/**
	 * change lighting type to json value
	 * @param type
	 * @return
	 */
	private String castTypeValue(int type) {

		switch (type) {
		case LightingInfo.LIGHT_TYPE_FULL:
			return JSON_VALUE_TYPE_FULL;
		case LightingInfo.LIGHT_TYPE_CIRCLE_A:
			return JSON_VALUE_TYPE_CIRCLE_A;
		case LightingInfo.LIGHT_TYPE_CIRCLE_B:
			return JSON_VALUE_TYPE_CIRCLE_B;
		case LightingInfo.LIGHT_TYPE_CIRCLE_C:
			return JSON_VALUE_TYPE_CIRCLE_C;
		case LightingInfo.LIGHT_TYPE_PENTAGON:
			return JSON_VALUE_TYPE_PENTAGON;
		case LightingInfo.LIGHT_TYPE_PENTAGON_A:
			return JSON_VALUE_TYPE_PENTAGON_A;
		case LightingInfo.LIGHT_TYPE_PENTAGON_B:
			return JSON_VALUE_TYPE_PENTAGON_B;
		case LightingInfo.LIGHT_TYPE_PENTAGON_C:
			return JSON_VALUE_TYPE_PENTAGON_C;
		case LightingInfo.LIGHT_TYPE_BLINGBLING_A:
			return JSON_VALUE_TYPE_BLINGBLING_A;
		case LightingInfo.LIGHT_TYPE_BLINGBLING_B:
			return JSON_VALUE_TYPE_BLINGBLING_B;
		case LightingInfo.LIGHT_TYPE_BLINGBLING_C:
			return JSON_VALUE_TYPE_BLINGBLING_C;
		case LightingInfo.LIGHT_TYPE_CIRCLE:
			return JSON_VALUE_TYPE_CIRCLE;
		}
		return JSON_VALUE_TYPE_FULL;
	}
	 
	@Override
	public JsonObject toJsonObject() throws JSONException {
		JsonObject jsonObject = super.toJsonObject();
		JsonArray jsonArray = new JsonArray();
		for (int i = 0; i < lightings.size(); i++) {
			JsonObject jsonLightingObj = new JsonObject();
			jsonLightingObj.put(JSON_NAME_COLOR, castColorTypeVaule(lightings.get(i).colorType));
			jsonLightingObj.put(JSON_NAME_DRAW_TYPE, castTypeValue(lightings.get(i).type));
			jsonLightingObj.put(JSON_NAME_SCALE,lightings.get(i).realSize);
			jsonLightingObj.put(JSON_NAME_MOVE_FROM_X, lightings.get(i).stX);
			jsonLightingObj.put(JSON_NAME_MOVE_FROM_Y, lightings.get(i).stY);
			jsonLightingObj.put(JSON_NAME_MOVE_TO_X, lightings.get(i).endX);
			jsonLightingObj.put(JSON_NAME_MOVE_TO_Y, lightings.get(i).endY);
			jsonLightingObj.put(JSON_VALUE_ALPHA_START, (lightings.get(i).start_alpha * 100));
			jsonLightingObj.put(JSON_VALUE_ALPHA_END, (lightings.get(i).end_alpha * 100));
			jsonLightingObj.put(JSON_VALUE_SET, true);
			jsonObject.put(Resolution.DEFAULT_JSON_NAME, lightings.get(i).mBaseResolution);
			jsonArray.put(jsonLightingObj);
		}

		jsonObject.put(JSON_NAME_OBJECT, jsonArray);
		//L.e("toJsonObject, jsonObject:"+jsonObject.toString());
		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);
		//L.e("injectJsonobject, jsonObject:"+jsonObject.toString());
		JsonArray jsonArray = jsonObject.getJSONArray(JSON_NAME_OBJECT);
		int size = jsonArray.length();
		lightings.clear();
		
		for (int i = 0; i < size; i++) {
			JsonObject single = jsonArray.getJSONObject(i);
			String type = single.getString(JSON_NAME_DRAW_TYPE);
			String colortype = single.getString(JSON_NAME_COLOR);
			int scale = single.getInt(JSON_NAME_SCALE);
			int fromx = single.getInt(JSON_NAME_MOVE_FROM_X);
			int fromy = single.getInt(JSON_NAME_MOVE_FROM_Y);
			int tox = single.getInt(JSON_NAME_MOVE_TO_X);
			int toy = single.getInt(JSON_NAME_MOVE_TO_Y);
			float alphaStart = LightingInfo.DEFAULT_ALPHA_START;
			float alphaEnd = LightingInfo.DEFAULT_ALPHA_END;
			Resolution resolution = Resolution.NHD;
			if(!jsonObject.isNull(JSON_VALUE_ALPHA_START)){
				alphaStart = single.getInt(JSON_VALUE_ALPHA_START) * 0.01f;
				alphaEnd = single.getInt(JSON_VALUE_ALPHA_END) * 0.01f;
			}
			
			if(!jsonObject.isNull(Resolution.DEFAULT_JSON_NAME))
				resolution = Resolution.createFrom(jsonObject.getJSONObject(Resolution.DEFAULT_JSON_NAME));
			
			if(JSON_VALUE_TYPE_FULL.equals(type) && single.isNull(JSON_VALUE_SET)){
				scale = SCALE_MIN  + (int)(Math.random() * (SCALE_MAX - SCALE_MIN)); 
				fromx = BOUND_MIN + (int)(Math.random() * (resolution.width-BOUND_MIN)); 
				fromy = BOUND_MIN + (int)(Math.random() *(resolution.height-BOUND_MIN)); 
				tox = BOUND_MIN + (int)(Math.random() * (resolution.width-BOUND_MIN)); 
				toy= BOUND_MIN + (int)(Math.random() * (resolution.height-BOUND_MIN)); 
			}
			
			setResource(type, colortype, scale, fromx, fromy, tox, toy,alphaStart,alphaEnd,resolution);

		}
	}

	// // // // // Inner class.
	// // // // //
	public static final class Editor extends Effect.Editor<LightEffect, Editor> {

		private Editor(LightEffect effect) {
			super(effect);
		}
		
		public Editor setResource(String type, String color, int scale, int fromx, int fromy, int tox, int toy, int startAlpha, int endAlpha, Resolution resolution) {
			getObject().setResource(type, color, scale, fromx, fromy, tox, toy,(float)(startAlpha)* 0.01f,(float)(endAlpha)* 0.01f, resolution);
			return this;
		}
		
		public Editor clear(){
			getObject().clear();
			return this;
		}
	}
	
	
	/**
	 * resource set by type & color
	 */
	private static final int[] mLightingTexturesCircleYellow =  {
		R.drawable.lightingeffect_innercircle_a,
		R.drawable.lightingeffect_innercircle_b,
		R.drawable.lightingeffect_innercircle_c,
		R.drawable.lightingeffect_innercircle
	};
		
	private static final int[] mLightingTexturesPentagonRed=  {
		R.drawable.lightingeffect_pentagon_summer_a,
		R.drawable.lightingeffect_pentagon_summer_b,
		R.drawable.lightingeffect_pentagon_summer_c,		
		R.drawable.lightingeffect_pentagon_summer
	};
		
	private static final int[] mLightingTexturesPentagonYellow=  {
		R.drawable.lightingeffect_pentagon_spring_a,
		R.drawable.lightingeffect_pentagon_spring_b,
		R.drawable.lightingeffect_pentagon_spring_c,		
		R.drawable.lightingeffect_pentagon_spring
	};
		
	private static final int[] mLightingTexturesPentagonBlue=  {
		R.drawable.lightingeffect_pentagon_winter_a,
		R.drawable.lightingeffect_pentagon_winter_b,
		R.drawable.lightingeffect_pentagon_winter_c,		
		R.drawable.lightingeffect_pentagon_winter
	};
		
	private static final int[] mLightingTexturesPentagonbrown=  {
		R.drawable.lightingeffect_pentagon_fall_a,
		R.drawable.lightingeffect_pentagon_fall_b,
		R.drawable.lightingeffect_pentagon_fall_c,		
		R.drawable.lightingeffect_pentagon_fall
	};
		
		
	private static final int[] mLightingTexturesBlingBlingYellow =  {
		R.drawable.lightingeffect_blingbling_a,
		R.drawable.lightingeffect_blingbling_b,
		R.drawable.lightingeffect_blingbling_c
	};
		

	/**
	 * 광원효과정보-광원컬러, 타입, 위치, 크기-를 가지고 있는 클래스이다. 
	 * 
	 */
	private final class LightingInfo implements ICacheCode{
		/**
		 * texture color index
		 */
		public static final int LIGHT_COLOR_RED = 0;
		public static final int LIGHT_COLOR_YELLOW = 1;
		public static final int LIGHT_COLOR_BLUE = 2;
		public static final int LIGHT_COLOR_BROWN = 3;
		public static final int LIGHT_TYPE_FULL = 12;
		/**
		 * texture index
		 */
		public static final int LIGHT_TYPE_CIRCLE_A = 0;
		public static final int LIGHT_TYPE_CIRCLE_B = 1;
		public static final int LIGHT_TYPE_CIRCLE_C = 2;
		public static final int LIGHT_TYPE_PENTAGON_A = 3;
		public static final int LIGHT_TYPE_PENTAGON_B = 4;
		public static final int LIGHT_TYPE_PENTAGON_C = 5;
		public static final int LIGHT_TYPE_BLINGBLING_A = 6;
		public static final int LIGHT_TYPE_BLINGBLING_B = 7;
		public static final int LIGHT_TYPE_BLINGBLING_C = 8;
		public static final int LIGHT_TYPE_CIRCLE = 9;
		public static final int LIGHT_TYPE_PENTAGON = 10;
		/**
		 * local value
		 */
		private final float SCALE_RATE = 0.01f;
		private final static float DEFAULT_ALPHA_START = 0.0f;
		private final static float DEFAULT_ALPHA_END = 1.0f;
		private int resId = 0;
		private int stX = 0;// start x
		private int stY = 0;// start y
		private int endX = 0;// end x
		private int endY = 0;// end y
		private int realSize = 0;
		private float scale = 0.0f;// fix light size- it could be 0 - 255
		private int colorType = LIGHT_COLOR_YELLOW;// default color yellow
		private int type = LIGHT_TYPE_FULL; // default type full
		private float alpha = 0.0f; // 0.0 - 1.0;
		private float start_alpha = DEFAULT_ALPHA_START;
		private float end_alpha = DEFAULT_ALPHA_END;
		private int cx, cy = 0;// moving control
		private float mx;
		private float my;
		private boolean isShowing = true;
		private ImageResource mImageResource;
		private Resolution mBaseResolution = Resolution.NHD;
		 
		LightingInfo(int type, int color, int size, int stx, int sty, int endx, int endy, float startAlpha,float endAlpha, Resolution resolution) {
			
			this.type = type;
			this.colorType = color;
			this.realSize = size;	
			this.stX = stx;
			this.stY = sty;
			this.endX = endx;
			this.endY = endy;
			
			// draw position init
			this.mx = (this.endX - this.stX);
			this.my = (this.endY - this.stY); 
			this.cx = stX;
			this.cy = stY;
			this.start_alpha = startAlpha;
			this.end_alpha = endAlpha;
			this.alpha = start_alpha;
			this.resId = getResourceId(type, color);
			this.mBaseResolution = resolution;
			if (this.resId != 0) {
				mImageResource = ImageResource.createFromDrawable(this.resId, getResources(), resolution);
				if (this.type > LIGHT_TYPE_BLINGBLING_C) {
					this.scale = this.realSize * SCALE_RATE;
				}
			} else { // this.type == LIGHT_TYPE_FULL
				this.scale = this.realSize;
			}
		}
		
		/**
		 * change texture type & color to resource id
		 * 
		 * @param type
		 * @param color
		 * @return
		 */
		private int getResourceId(int type, int color){
			switch (type) {
			case LIGHT_TYPE_CIRCLE_A:
				return mLightingTexturesCircleYellow[0];
			case LIGHT_TYPE_CIRCLE_B:
				return mLightingTexturesCircleYellow[1];
			case LIGHT_TYPE_CIRCLE_C:
				return mLightingTexturesCircleYellow[2];
			case LIGHT_TYPE_BLINGBLING_A:
				return mLightingTexturesBlingBlingYellow[0];
			case LIGHT_TYPE_BLINGBLING_B:
				return mLightingTexturesBlingBlingYellow[1];
			case LIGHT_TYPE_BLINGBLING_C:
				return mLightingTexturesBlingBlingYellow[2];
			case LIGHT_TYPE_CIRCLE:
				return mLightingTexturesCircleYellow[3];
			case LIGHT_TYPE_PENTAGON_A:
				if(color==LIGHT_COLOR_YELLOW) return mLightingTexturesPentagonYellow[0];
				if(color==LIGHT_COLOR_BLUE) return mLightingTexturesPentagonBlue[0];
				if(color==LIGHT_COLOR_BROWN) return mLightingTexturesPentagonbrown[0];
				return mLightingTexturesPentagonRed[0];
			case LIGHT_TYPE_PENTAGON_B:
				if(color==LIGHT_COLOR_YELLOW) return mLightingTexturesPentagonYellow[1];
				if(color==LIGHT_COLOR_BLUE) return mLightingTexturesPentagonBlue[1];
				if(color==LIGHT_COLOR_BROWN) return mLightingTexturesPentagonbrown[1];
				return mLightingTexturesPentagonRed[1];
			case LIGHT_TYPE_PENTAGON_C:
				if(color==LIGHT_COLOR_YELLOW) return mLightingTexturesPentagonYellow[2];
				if(color==LIGHT_COLOR_BLUE) return mLightingTexturesPentagonBlue[2];
				if(color==LIGHT_COLOR_BROWN) return mLightingTexturesPentagonbrown[2];
				return mLightingTexturesPentagonRed[2];
			case LIGHT_TYPE_PENTAGON:
				if(color==LIGHT_COLOR_YELLOW) return mLightingTexturesPentagonYellow[3];
				if(color==LIGHT_COLOR_BLUE) return mLightingTexturesPentagonBlue[3];
				if(color==LIGHT_COLOR_BROWN) return mLightingTexturesPentagonbrown[3];
				return mLightingTexturesPentagonRed[3];
			}
			return 0;
		}

		@Override
		public int createCacheCode() {
			StringBuilder builder = new StringBuilder();

			builder.append(type);
			builder.append(colorType);
			builder.append(realSize);
			builder.append(mBaseResolution);
			
	    	return builder.toString().hashCode();
		}
	}
}
