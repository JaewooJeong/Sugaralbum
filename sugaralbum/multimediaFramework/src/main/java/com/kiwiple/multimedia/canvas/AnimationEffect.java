package com.kiwiple.multimedia.canvas;



import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.graphics.Bitmap;

import com.kiwiple.multimedia.ResourceType;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.exception.InvalidCanvasUserException;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * AnimationEffect OverlayEffect실행시 해당 프레임에 필요한 애니메이션 효과가 있는 경우 사용된다. 따라서 해당 효과는
 * 독립적으로 사용할 수 없고, OverlayEffect에서 사용중이 프레임의 정보를 참고하여 적용되어야 한다.
 * 
 * @author aubergine
 *
 */
public final class AnimationEffect extends Effect {

	// // // // // Static variable.
	public static final String JSON_VALUE_TYPE = "animation_effect";
	public static final String JSON_NAME_MOTION = "motion";
	public static final String JSON_NAME_IMAGE_RESOURCE = ImageResource.DEFAULT_JSON_NAME;
	public static final String JSON_NAME_RESOURCE_TYPE = ResourceType.DEFAULT_JSON_NAME;
	public static final String JSON_NAME_COORDINATE_X = "coordinate_x";
	public static final String JSON_NAME_COORDINATE_Y = "coordinate_y";
	public static final String JSON_NAME_MOTION_TYPE = "motion_type";
	public static final String JSON_NAME_MOTION_VALUE = "motion_value";
	public static final String JSON_NAME_OBJECT = "animation_object";

	// // // // // Member variable.
	// // // // //
	private static final float FRAME_DURATION = 0.1f;
	private static final int DEFAULT_MOTION_COUNT = 2;
	
	private final List<AnimationInfo> mAnimationObjs = new ArrayList<>();
	@CacheCode(indexed = true)
	private final List<Integer> mCacheCodes = new ArrayList<>();
	
	// // // // // Constructor.
	// // // // //
	AnimationEffect(Scene parent) {
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
		JsonArray jsonArray = new JsonArray();

		for (AnimationInfo animation: mAnimationObjs) {
			JsonObject jsonAnimationObj = new JsonObject();
			jsonAnimationObj.put(JSON_NAME_COORDINATE_X, animation.mCoordinateX);
			jsonAnimationObj.put(JSON_NAME_COORDINATE_Y, animation.mCoordinateY);
			jsonAnimationObj.put(JSON_NAME_IMAGE_RESOURCE, animation.mImageResource);
			jsonAnimationObj.put(JSON_NAME_MOTION_TYPE, animation.mMotion.type);
			jsonAnimationObj.put(JSON_NAME_MOTION_VALUE, animation.mMotion.value);
			jsonArray.put(jsonAnimationObj);
		}
		jsonObject.put(JSON_NAME_OBJECT, jsonArray);
		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);
		JsonArray jsonArray = jsonObject.getJSONArray(JSON_NAME_OBJECT);
		parseAnimationInfo(jsonArray);
	}

	@Override
	void onDraw(PixelCanvas dstCanvas) {
		
		Resolution dstResolution = getResolution();

		float progressRatio = getProgressRatio();
		int index = 0;
		for (AnimationInfo animation : mAnimationObjs) {
			Resolution srcResolution = animation.mImageResource.getResolution();
			int frameMotionIndex = Math.round(progressRatio / FRAME_DURATION) % animation.mAnimationCount;
			if (animation.motionIndex!= frameMotionIndex) {
				animation.motionIndex = frameMotionIndex;
			}
			
			PixelCanvas pixelCanvas ;
			int dstX = animation.mCoordinateX;
			int dstY = animation.mCoordinateY;
			
			if (animation.motionIndex !=0) { 
				pixelCanvas = getCanvas(index+animation.motionIndex);
				
				int imageW = pixelCanvas.getImageWidth();
				int imageH = pixelCanvas.getImageHeight();
				
				int orgW = getCanvas(index).getImageWidth();
				int orgH = getCanvas(index).getImageHeight();
				
				if(animation.mMotion.type == AnimationMotion.MOTION_ROTATE )
				{
					//TODO
					dstX = dstX - (imageW - orgW)/2;
					dstY = dstY - (imageH - orgH)/2;
				}
				else if(animation.mMotion.type == AnimationMotion.MOTION_SCALE)
				{
					dstX = dstX - (imageW - orgW)/2;
					dstY = dstY - (imageH - orgH)/2;
				}
				index+=DEFAULT_MOTION_COUNT;
			}
			else {
				pixelCanvas = getCanvas(index);
				index+=DEFAULT_MOTION_COUNT;
			}
			
			if (!srcResolution.equals(dstResolution)) {
				float multiplier = dstResolution.magnification / srcResolution.magnification;
				dstX *= multiplier;
				dstY *= multiplier;
			}

			pixelCanvas.blend(dstCanvas, dstX, dstY);
		}
	}

	@Override
	void onValidate(Changes changes) throws InvalidCanvasUserException {
		
		mCacheCodes.clear();
		for (AnimationInfo info : mAnimationObjs) {
			mCacheCodes.add(info.createCacheCode());
			mCacheCodes.add(info.createCacheCode() + info.mMotion.createCacheCode());
		}
	}
	
	@Override
	void prepareCanvasWithCache() throws IOException {
		
		for (int i = 0; i < getCacheCount(); i++) {
			getCacheManager().decodeImageCache(getCacheCodeChunk(i), getCanvas(i));
		}
	}
	
	@Override
	void prepareCanvasWithoutCache() throws IOException {
		
		for (int i = 0; i < getCacheCount(); i++) {
			PixelExtractUtils.extractARGB(createCacheAsBitmap(i), getCanvas(i), true);
		}
	}

	@Override
	public int getCacheCount() {
		return mAnimationObjs.size() * DEFAULT_MOTION_COUNT;
	}
	
	@Override
	Size[] getCanvasRequirement() {
		
		if (mAnimationObjs.size() == 0) {
			return DO_NOT_NEED_CANVAS;
		} else {
			Size[] size = new Size[mAnimationObjs.size()*DEFAULT_MOTION_COUNT];
			int index = 0;
			for(AnimationInfo animationInfo :mAnimationObjs){
				size[index++] = animationInfo.mImageResource.measureSize(getResolution());
				if (animationInfo.mMotion.type == AnimationMotion.MOTION_REPLACE) {
					size[index++] =animationInfo.mMotion.mImageObjectResource.measureSize(getResolution());
				} else if (animationInfo.mMotion.type == AnimationMotion.MOTION_SCALE) {
					size[index++] = animationInfo.mImageResource.measureSize(getResolution(), animationInfo.mMotion.value_float, null);
				} else if (animationInfo.mMotion.type == AnimationMotion.MOTION_ROTATE) {
					size[index++] = animationInfo.mImageResource.measureSize(getResolution(), null, animationInfo.mMotion.value_float);
				}
			}
			return size;
		}
	}
	
	void setResource(String path, int cx, int cy, int mt, Object value,Resolution resolution) {
		AnimationInfo tmp = new AnimationInfo(path,cx, cy, mt,value,resolution);
		mAnimationObjs.add(tmp);
	}
	
	void setResource(int id, int cx, int cy, int mt, Object value,Resolution resolution) {
		AnimationInfo tmp = new AnimationInfo(id,cx, cy, mt,value,resolution);
		mAnimationObjs.add(tmp);
	}

	void clear() {
		if (mAnimationObjs != null && mAnimationObjs.size() > 0)
			mAnimationObjs.clear();
	}

	@Override
	Bitmap createCacheAsBitmap(int index) throws IOException {
		Resolution resolution = getResolution();
		if(mAnimationObjs.size()==0) return null;
		
		int infoIndex = index/DEFAULT_MOTION_COUNT;
		int motionIndex = index%DEFAULT_MOTION_COUNT;
		if(motionIndex == 0) {
			return mAnimationObjs.get(infoIndex).mImageResource.createBitmap(resolution);
		}
		
		AnimationMotion motion = mAnimationObjs.get(infoIndex).mMotion;
		if (motion.type == AnimationMotion.MOTION_ROTATE){
			return  mAnimationObjs.get(infoIndex).mImageResource.createBitmap(resolution,null, motion.value_float);
		}
		else if (motion.type == AnimationMotion.MOTION_SCALE){
			return mAnimationObjs.get(infoIndex).mImageResource.createBitmap(resolution, motion.value_float, null);
		}
		else{
			return motion.mImageObjectResource.createBitmap(resolution);
		}
	}


	private void parseAnimationInfo(JsonArray info) {
		// TODO parse info & set animationInfo object
		if(mAnimationObjs.size()>0) mAnimationObjs.clear();
		try {
			for (int i = 0; i < info.length(); i++) {
				JsonObject jsonObj = info.getJSONObject(i);
				AnimationMotion motion = new AnimationMotion( jsonObj.getInt(JSON_NAME_MOTION_TYPE), jsonObj.get(JSON_NAME_MOTION_VALUE),Resolution.FHD);
				mAnimationObjs.add(new AnimationInfo(jsonObj, jsonObj.getInt(JSON_NAME_COORDINATE_X), jsonObj.getInt(JSON_NAME_COORDINATE_Y), motion));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// // // // // Inner class.
	// // // // //
	public static final class Editor extends Effect.Editor<AnimationEffect, Editor> {

		private Editor(AnimationEffect animationEffect) {
			super(animationEffect);
		}

		public Editor setResource(String path, int cx, int cy, int mt, Object value, Resolution resolution) {
			getObject().setResource(path, cx, cy, mt, value,resolution);
			return this;
		}
		
		public Editor setResource(int id, int cx, int cy, int mt, Object value,Resolution resolution) {
			getObject().setResource(id, cx, cy, mt, value,resolution);
			return this;
		}

		public Editor clear() {
			getObject().clear();
			return this;
		}
	}
	
	
	/**
	 * AnimationEffect Info Resource Motion Data class
	 * 
	 * @author aubergine
	 *
	 */
	public final class AnimationMotion implements ICacheCode {

		public static final int MOTION_NOTHING = 0;
		public static final int MOTION_SCALE = 1;
		public static final int MOTION_ROTATE = 2;
		public static final int MOTION_REPLACE = 3;
		
		public int type;
		public float value_float;
		public String value_string;
		public Object value;
		public ImageResource mImageObjectResource;
		
		public AnimationMotion( int type, Object value, Resolution resolution){
			this.type = type;
			this.value = value;
			switch(this.type){
				case MOTION_SCALE: 
					this.value_float = ((Double)value).floatValue(); 
					break;
				case MOTION_ROTATE:
					try{
						this.value_float = ((Double)value).floatValue();
					}catch(Exception e){
						this.value_float = ((Integer)value).floatValue();
					}
					break;
				case MOTION_REPLACE:
					this.value_string = ((String)value); 
					mImageObjectResource = ImageResource.createFromFile(value_string, resolution);
					break;
			}
		}
		
		@Override
		public int createCacheCode() {
			
			StringBuilder builder = new StringBuilder();
			builder.append(type);
			builder.append(value_float);
			if (value_string != null)
				builder.append(value_string);
			if (mImageObjectResource != null)
				builder.append(mImageObjectResource.createCacheCode());
			return builder.toString().hashCode();
		}
	}
			

	/**
	 * AnimationEffect Resource Data
	 * @author aubergine
	 *
	 */
	public final class AnimationInfo implements ICacheCode {
		public ImageResource mImageResource;
		public AnimationMotion mMotion;
		public int mCoordinateX;
		public int mCoordinateY;
		public int mAnimationCount = DEFAULT_MOTION_COUNT;
		public int motionIndex = 0;
		/**
		 * AnimationInfo생성자 
		 * 
		 * @param path 이미지파일 path
		 * @param cx 이미지 x 좌표 
		 * @param cy 이미지 y 좌표 
		 * @param mt 모션타입으로 {@link MOTION_SCALE}, {@link MOTION_ROTATE}, {@link MOTION_REPLACE} 중 설정 
		 * @param value 모션값.모션 타입에 따라 해당 값 설정 
		 */
		public AnimationInfo(String filePath, int cx, int cy, int mt, Object value,Resolution resolution) {
			this.mCoordinateX = cx;
			this.mCoordinateY = cy;
			this.mMotion = new AnimationMotion(mt, value, resolution);
			try{
				this.mImageResource = ImageResource.createFromFile(filePath, resolution);
			}catch(Exception e){
				e.printStackTrace();
				this.mImageResource = null;
			}
		}

		/**
		 * AnimationInfo생성자 
		 * 
		 * @param id 이미지파일 drawable ID
		 * @param cx 이미지 x 좌표 
		 * @param cy 이미지 y 좌표 
		 * @param mt 모션타입으로 {@link MOTION_SCALE}, {@link MOTION_ROTATE}, {@link MOTION_REPLACE} 중 설정 
		 * @param value 모션값.모션 타입에 따라 해당 값 설정 
		 */
		public AnimationInfo(int id, int cx, int cy, int mt, Object value, Resolution resolution) {
			this.mCoordinateX = cx;
			this.mCoordinateY = cy;
			this.mMotion = new AnimationMotion(mt, value, resolution);
			this.mImageResource = ImageResource.createFromDrawable(id, getResources(), resolution);
		}
		
		public AnimationInfo(JsonObject jsonObject , int cx, int cy, AnimationMotion motion) throws JSONException {
			this.mCoordinateX = cx;
			this.mCoordinateY = cy;
			this.mMotion  = motion;
			mImageResource = ImageResource.createFromJsonObject(getResources(), jsonObject.getJSONObject(JSON_NAME_IMAGE_RESOURCE));
		}
		
		@Override
		public int createCacheCode() {
			return mImageResource.createCacheCode();
		}
	}
}
