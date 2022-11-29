package com.kiwiple.scheduler.data.uplus.effect;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Point;

import com.kiwiple.multimedia.ResourceType;
import com.kiwiple.debug.L;
import com.kiwiple.multimedia.canvas.DynamicTextureEffect;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.scheduler.data.EffectData;

/**
 * DynamicTextureEffect 데이터 클래스.
 *
 */
public class UplusDynamicTextureEffectData extends EffectData {
	
	private String type;
	ArrayList<Textures> mTextures;
	ArrayList<Motions> motions = new ArrayList<Motions>();
	public String mReverse;
	public Resolution mResolution;
	/**
	 * 생성자.
	 * @param effectType DynamicTextureEffect 타입. 
	 * @throws JSONException 
	 */
	public UplusDynamicTextureEffectData(String effectType, JSONObject dynamicTextureObject) throws JSONException {
		super(DynamicTextureEffect.JSON_VALUE_TYPE);
		L.d("dynamicTexture.dynamicTextureObject:"+dynamicTextureObject.toString());
		mTextures  = new ArrayList<Textures>();
		type = effectType; 
		mReverse = dynamicTextureObject.getString(DynamicTextureEffect.JSON_NAME_MOTION_REVERSE);
		mResolution = findResoltuion(dynamicTextureObject.getString(Resolution.DEFAULT_JSON_NAME));
		JSONArray textures = dynamicTextureObject.getJSONArray(DynamicTextureEffect.JSON_NAME_TEXTURE);
		
		for (int dy = 0; dy < textures.length(); dy++) {
			JSONObject dynamicObject = textures.getJSONObject(dy);
			String path = dynamicObject.getString(DynamicTextureEffect.JSON_NAME_TEXTURE_PATH);
			ResourceType resourcetype = ResourceType.ANDROID_RESOURCE;
			if (path.contains("/data")) resourcetype = ResourceType.FILE;
			mTextures.add(new Textures(path, (float) dynamicObject.getDouble(DynamicTextureEffect.JSON_NAME_TEXTURE_SCALE), (float) dynamicObject.getDouble(DynamicTextureEffect.JSON_NAME_TEXTURE_ROTATE), resourcetype));

		}

		JSONArray jsonArray = dynamicTextureObject.getJSONArray(DynamicTextureEffect.JSON_NAME_MOTION);
		for (int dy = 0; dy < jsonArray.length(); dy++) {
			JSONObject motionObject = jsonArray.getJSONObject(dy);
			addMotion(new Motions(new Point(motionObject.getInt(DynamicTextureEffect.JSON_NAME_MOTION_MOVE_X), 
					motionObject.getInt(DynamicTextureEffect.JSON_NAME_MOTION_MOVE_Y)), 
					motionObject.getString(DynamicTextureEffect.JSON_NAME_MOTION_MOVE_TYPE), 
					motionObject.getInt(DynamicTextureEffect.JSON_NAME_MOTION_DURATION), 
					(float) motionObject.getDouble(DynamicTextureEffect.JSON_NAME_MOTION_ALPHA),
					motionObject.optBoolean(DynamicTextureEffect.JSON_NAME_MOTION_ANIMATED)));
		}
		
		if(!dynamicTextureObject.isNull(EffectData.JSON_NAME_ACTIVE_START_RATIO)){
			activeStartRatio = (float)dynamicTextureObject.getDouble(EffectData.JSON_NAME_ACTIVE_START_RATIO); 
		}else{
			activeStartRatio = 0.0f; 
		}
		
		if(!dynamicTextureObject.isNull(EffectData.JSON_NAME_ACTIVE_END_RATIO)){
			activeEndRatio = (float)dynamicTextureObject.getDouble(EffectData.JSON_NAME_ACTIVE_END_RATIO); 
		}else{
			activeEndRatio = 1.0f; 
		}
		
		if(!dynamicTextureObject.isNull(EffectData.JSON_NAME_DRAW_ONLY_ACTIVE_RATIO)){
			drawOnlyActvieRatio = dynamicTextureObject.getBoolean(EffectData.JSON_NAME_DRAW_ONLY_ACTIVE_RATIO); 
		}else{
			drawOnlyActvieRatio = false;
		}
	}
	
	public Resolution findResoltuion(String name) {
		if(Resolution.NHD.name.equals(name)) return Resolution.NHD;
		if(Resolution.HD.name.equals(name)) return Resolution.HD;
		if(Resolution.FHD.name.equals(name)) return Resolution.FHD;
		return null;
	}

	/**
	 * DynamicTextureEffect 타입을 반환한다. 
	 * @return DynamicTextureEffect타입.
	 */
	public String getType() {
		return type;
	}
	
	public ArrayList<Textures> getTextures(){
		return mTextures;
	}
	
	public void addMotion(Motions motion){
		motions.add(motion);
	}
	
	public ArrayList<Motions> getMotions(){
		return motions;
	}
	
	public class Textures {
		
		public String mPath;
		public float mScale;
		public float mRotate;
		public ResourceType mRType;
		
		public Textures(String path, float scale, float rotate, ResourceType resourceType){
			this.mPath = path;
			this.mScale = scale;
			this.mRotate = rotate;
			this.mRType = resourceType;
		}
	}
	
	public class Motions{
		public Point mMotionP = new Point();
		public float mAlpha ;
		public int mMotionDuration;
		public String mMotionType;
		public boolean mAnimated = false;
		
		Motions(Point p, String type, int duration, float alpha, boolean animated){
			this.mMotionP = p;
			this.mAlpha = alpha;
			this.mMotionDuration = duration;
			this.mMotionType = type;
			this.mAnimated = animated;
		}
	}
}
