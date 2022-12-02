package com.kiwiple.multimedia.canvas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.content.res.Resources;
import android.graphics.Bitmap;

import com.kiwiple.debug.L;
import com.kiwiple.multimedia.R;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * FogEffect.
 * FogEffect는 세가지 타입을 지원한다.
 * {@link FOG_TYPE_FULL}은 전체 화영에 효과를 적용하고, 
 * {@link FOG_TYPE_HALF}는 화면의 일부 영역에서 전체 영역으로 효과를 적용한다.
 * {@link FOG_TYPE_RANDOM}은 다양한 형태의 효과를 빠른 속도의 움직임으 적용한다.
 * 
 */
public final class FogEffect extends Effect {

	// // // // // Static variable.
	// // // // //
	public static final String JSON_VALUE_TYPE = "fog_effect";
	public static final String JSON_NAME_EFFECT_TYPE = "fog_type";
	public static final String JSON_VALUE_TYPE_FOG_FULL = "fog_type_full";
	public static final String JSON_VALUE_TYPE_FOG_HALF = "fog_type_half";
	public static final String JSON_VALUE_TYPE_FOG_RANDOM = "fog_type_random";
	
	/**
	 * 해당 씬의 전체 화면에 안개효과를 적용한다.
	 */
	public static final int FOG_TYPE_FULL = 0;
	/**
	 * 해당 씬의 일부 영역에 안개효과를 적용한다. 천천히 전체 화면으로 안개효과가 적용되면서 움직인다.
	 */
	public static final int FOG_TYPE_HALF = 1;  
	/**
	 * 해당 씬의 일부 영역에 안개효과를 적용한다. 여러종류의 안개효과가 빠르게 움직이면 적용된다.
	 */
	public static final int FOG_TYPE_RANDOM = 2;
	
	private static final int FOG_LENGTH_FULL = 1;
	private static final int FOG_LENGTH_HALF = 1;
	private static final int FOG_LENGTH_RANDOM = 3;
	
	/**
	 * fog sample data array index를 관리한다.
	 */
	private static final int SAMPLE_FULL_LENGTH = 3;
	private static final int SAMPLE_HALF_LENGTH = 6;
	private static final int SAMPLE_RANDOM_LENGTH= 5;
	
	// // // // // Member variaΩle.
    private String mEffectType = JSON_VALUE_TYPE_FOG_FULL;
    private int mDataIndex = 0;
    @CacheCode(indexed = true)
	private List<FogResourceInfo> mFogResourceInfos = new ArrayList<FogResourceInfo>();
	// // // // // Constructor.
	// // // // //
	FogEffect(Scene parent) {
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
		jsonObject.put(JSON_NAME_EFFECT_TYPE, mEffectType+mDataIndex);
		return jsonObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);
		makeInfoByType(jsonObject.getString(JSON_NAME_EFFECT_TYPE));
	}
	
	@Override
	void onDraw(PixelCanvas dstCanvas) {
		Resolution srcResolution = mFogResourceInfos.get(0).mImageResource.getResolution();
		Resolution dstResolution = getResolution();

		int dstX = 0;
		int dstY = 0;
		
		float currentPosition = getProgressRatio();
		for(int i=0;i<mFogResourceInfos.size();i++){
			dstX = mFogResourceInfos.get(i).mDatas.mStartX + (int)(mFogResourceInfos.get(i).mDatas.mMovingDirectionX * mFogResourceInfos.get(i).mDatas.mMovingSpeedX * currentPosition) ;
			dstY = mFogResourceInfos.get(i).mDatas.mStartY + (int)(mFogResourceInfos.get(i).mDatas.mMovingDirectionY * mFogResourceInfos.get(i).mDatas.mMovingSpeedY * currentPosition) ;
			
			if (!srcResolution.equals(dstResolution)) {
				float multiplier = dstResolution.magnification / srcResolution.magnification;
				dstX *= multiplier;
				dstY *= multiplier;
			}
			
			PixelCanvas pixelCanvas = getCanvas(i);
			pixelCanvas.blend(dstCanvas, dstX, dstY, mFogResourceInfos.get(i).mAlpha);
		}
	}
	

	@Override
	Size[] getCanvasRequirement() {
		if ( mFogResourceInfos.size() == 0) {
			return DO_NOT_NEED_CANVAS;
		} else {
			Size[] tmpSize = new Size[mFogResourceInfos.size()];
			for(int i =0;i<mFogResourceInfos.size();i++){
				tmpSize[i] = mFogResourceInfos.get(i).mImageResource.measureSize(getResolution()) ;
			}
			return tmpSize;
		}
	}

	@Override
	void prepareCanvasWithCache() throws IOException {

		for (int i = 0; i != mFogResourceInfos.size(); ++i) {
			getCacheManager().decodeImageCache(getCacheCodeChunk(i), getCanvas(i));
		}
	}
	
	@Override
	void prepareCanvasWithoutCache() throws IOException {

		for (int i = 0; i != mFogResourceInfos.size(); ++i) {
			PixelExtractUtils.extractARGB(createCacheAsBitmap(i), getCanvas(i), true);
		}
	}
	
	@Override
	Bitmap createCacheAsBitmap(int index) throws IOException {

		Resolution resolution = getResolution();
		if(mFogResourceInfos.size() > 0){
			Bitmap tmp =  mFogResourceInfos.get(index).mImageResource.createBitmap(resolution);
			return tmp;
		}
		return null;
	}

	@Override
	public int getCacheCount() {
		return mFogResourceInfos.size();
	}
	
	/**
	 * 설정된 효과의 타입에 따라 사용되는 리소스로 FogInfo를 생성한다.
	 * @param type
	 */
	void setEffectType(String type){
		makeInfoByType(type);
	}
	
	private void makeInfoByType(String type){
		int fogSize = 1;
		int plusIndex = 0;
		int maxSampleLength = 3;
		mFogResourceInfos.clear();
		
		this.mEffectType =  JSON_VALUE_TYPE_FOG_FULL;
		
		if(type.startsWith(JSON_VALUE_TYPE_FOG_RANDOM)) {
			this.mEffectType =  JSON_VALUE_TYPE_FOG_RANDOM;
			fogSize = FOG_LENGTH_RANDOM;
			maxSampleLength  = fogTempletsRandom.length;
		}
		else if(type.startsWith(JSON_VALUE_TYPE_FOG_HALF)) {
			this.mEffectType =  JSON_VALUE_TYPE_FOG_HALF;
			maxSampleLength  = fogTempletsHalf.length;
		}

		try{
			mDataIndex = Integer.parseInt(type.substring(type.length()-1));
		}catch(NumberFormatException e){
			L.e("=====NumberFormatException, type:"+type);
			mDataIndex = 0;
		}
		
		for(int i = 0;i<fogSize;i++) 
		{
			mFogResourceInfos.add(new FogResourceInfo(changeTypeToInt(), getResources(), mDataIndex+plusIndex));
			plusIndex++;
			if(maxSampleLength <= plusIndex) plusIndex = 0;
		}
	}
	
	private int changeTypeToInt(){
		if(JSON_VALUE_TYPE_FOG_FULL.equals(mEffectType)) return FOG_TYPE_FULL;
		else if(JSON_VALUE_TYPE_FOG_HALF.equals(mEffectType)) return FOG_TYPE_HALF;
		return FOG_TYPE_RANDOM;
	}
	                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                   
	// // // // // Inner class.
	// // // // //
	public static final class Editor extends Effect.Editor<FogEffect, Editor> {

		private Editor(FogEffect fogEffect) {
			super(fogEffect);
		}
		
		public Editor setEffectType(String type) {
			getObject().setEffectType(type);
			return this;
		}
	}
	

	/**
	 * Fog Data Samples
	 */
	static final int NONE = 1;
	
	static final int SPPED_SLOW = 50;
	static final int SPPED_NORMAL = 100;
	static final int SPPED_FAST= 400;
	static final int LEFT = -1;
	static final int RIGHT = 1; 
	static final int UP = -1;
	static final int DOWN = 1;
	
		
	/** FogSample {mFogResID,mStartX,mStartY,mMovingSpeedX,mMovingSpeedY,mMovingDirectionX,mMovingDirectionY}**/
	final static int[][] fogTempletsFull = {
		{R.drawable.fog_t3_07, 5,-10,SPPED_SLOW,NONE,LEFT,NONE},
		{R.drawable.fog_t3_09, -60,-50,SPPED_SLOW,NONE,RIGHT,NONE},
		{R.drawable.fog_t3_10, -40,-40,SPPED_SLOW,NONE,LEFT,NONE}
	};//length 3
	
	final static int[][] fogTempletsHalf = {
		{R.drawable.fog_t3_01, 5,-10,SPPED_NORMAL,NONE,LEFT,NONE},
		{R.drawable.fog_t3_08, -20,50,SPPED_NORMAL,NONE,RIGHT,NONE},
		{R.drawable.fog_t3_11, 5,-10,SPPED_NORMAL,NONE,LEFT,NONE},
		{R.drawable.fog_t3_12, 10,-10,SPPED_NORMAL,NONE,LEFT,NONE},
		{R.drawable.fog_t3_13, 5,-10,SPPED_NORMAL,NONE,LEFT,NONE},
		{R.drawable.fog_t3_14, -30,100,SPPED_NORMAL,SPPED_SLOW,RIGHT,UP},
		{R.drawable.fog_t3_15, 5,-10,SPPED_NORMAL,NONE,LEFT,NONE}
	};//length 6
	
	final static int[][] fogTempletsRandom = {
		{R.drawable.fog_t3_02, 400,-100,SPPED_FAST,SPPED_NORMAL,LEFT,DOWN},
		{R.drawable.fog_t3_03, 20, 0,SPPED_FAST,SPPED_FAST,LEFT,UP},
		{R.drawable.fog_t3_04, 30,-10,SPPED_FAST,SPPED_SLOW,LEFT,UP},
		{R.drawable.fog_t3_05, -20,10,SPPED_FAST,SPPED_SLOW,RIGHT,UP},
		{R.drawable.fog_t3_06, 50,-10,SPPED_FAST,NONE,LEFT,NONE}
	};//length 5
	
	/**
	 * FogEffect resource 정보를 가지고 있는 클래스
	 * 사용자가 지정한 타입에 따라서 사용할 리소스와 움직입 값을 렌덤하게 생성하여 저장한다.
	 * 저장된 값은 해당 씬에서는 항상 동일하게 사용될 수 있도록 한다.
	 * 
	 * @author aubergine
	 *
	 */
	static final class FogResourceInfo implements ICacheCode {
		
		final FogSample mDatas;
		final int mEffectType;
		final ImageResource mImageResource;
		final float mAlpha = 0.4f;
		
		public FogResourceInfo(int effectType, Resources resource, int dataIndex) {
			this.mEffectType = effectType;
			
			if(this.mEffectType==FOG_TYPE_FULL ){
				if(dataIndex >=fogTempletsFull.length) dataIndex = 0;
				mDatas = new FogSample(fogTempletsFull[dataIndex]);
			}
			else if(this.mEffectType == FOG_TYPE_HALF){
				if(dataIndex >=fogTempletsHalf.length) dataIndex = 0;
				mDatas = new FogSample(fogTempletsHalf[dataIndex]);
			}
			else {
				if(dataIndex >=fogTempletsRandom.length) dataIndex = 0;
				mDatas = new FogSample(fogTempletsRandom[dataIndex]);
			}
			this.mImageResource = ImageResource.createFromDrawable(mDatas.mFogResID, resource, Resolution.NHD);
		}
		
		@Override
		public int createCacheCode() {
			return mDatas.mFogResID;
		}
	}
	
	/**
	 * Fog resource id, position, moving data class
	 * 
	 * @author aubergine
	 *
	 */
	static final class FogSample {
		int mFogResID;
		int mStartX;
		int mStartY;
		int mMovingSpeedX;
		int mMovingSpeedY;
		int mMovingDirectionX;
		int mMovingDirectionY;
		
		///data [mFogResID,mStartX,mStartY,mMovingSpeedX,mMovingSpeedY,mMovingDirectionX,mMovingDirectionY]
		public FogSample(int[] data){
			this.mFogResID =  data[0];
			this.mStartX = data[1];
			this.mStartY = data[2];
			this.mMovingSpeedX =  data[3];
			this.mMovingSpeedY = data[4];
			this.mMovingDirectionX = data[5];
			this.mMovingDirectionY = data[6];
		}
	}
}
