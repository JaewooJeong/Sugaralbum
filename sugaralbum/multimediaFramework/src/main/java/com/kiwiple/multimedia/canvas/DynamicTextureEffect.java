package com.kiwiple.multimedia.canvas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.Point;

import com.kiwiple.debug.L;
import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.ResourceType;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.json.IJsonConvertible;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * DynamicTextureEffect
 * 
 * 텍스쳐에 N가지 모션을 지정하여 사용할 수 있는 효과. 모션{@link DynamicTextureMotion}은 이동 좌표값, 이동 형태(직선, 곡선),이동 속도, 텍스쳐의
 * 투명도, 투명도 속도 값을 가진다. . 텍스쳐는 원본 대비 스케일값을 지정할 수 있다.
 * 
 * @author aubergine
 *
 */
public final class DynamicTextureEffect extends Effect {
	public static final String JSON_VALUE_TYPE = "dynamic_texture_effect";
	// public static final String JSON_NAME_TEXTURE = "dynamic_texture";
	public static final String JSON_NAME_IMAGE_RESOURCE = ImageResource.DEFAULT_JSON_NAME;
	public static final String JSON_NAME_TEXTURE = "texture";
	public static final String JSON_NAME_TEXTURE_PATH = "texture_path";
	public static final String JSON_NAME_TEXTURE_SCALE = "texture_scale";
	public static final String JSON_NAME_TEXTURE_ROTATE = "texture_rotate";
	public static final String JSON_NAME_MOTION_REVERSE = "motion_reverse";
	public static final String JSON_NAME_MOTION = "motion";
	public static final String JSON_NAME_MOTION_DURATION = "motion_duration";
	public static final String JSON_NAME_MOTION_ALPHA = "motion_alpha";
	public static final String JSON_NAME_MOTION_MOVE_X = "motion_x";
	public static final String JSON_NAME_MOTION_MOVE_Y = "motion_y";
	public static final String JSON_NAME_MOTION_MOVE_TYPE = "move_type";
	public static final String JSON_NAME_MOTION_ANIMATED = "motion_animated";
	public static final String JSON_VALUE_MOTION_REVERSE_BACK = "motion_reverse_back";//motion index 값이 0-1-2-1-0-1-... 순으롤 변경된다.
	public static final String JSON_VALUE_MOTION_REVERSE_INIT = "motion_reverse_init";//motion index 값이 0-1-2-0-1-2-... 순으로 변경된다.
	public static final String JSON_VALUE_MOTION_REVERSE_STOP = "motion_reverse_stop";//motion index 값이 0-1-2 로 변경된다.
	// // // // // Member variable.
	// // // // //
	@CacheCode(indexed = true)
	private ArrayList<DynamicTexture> mTextures = new ArrayList<DynamicTexture>();
	private List<DynamicTextureMotion> mMotions = new ArrayList<DynamicTextureMotion>();
	// local
	private boolean mCanReverse = false;// motion reverse 여부 설
	private boolean mMotionReversed = false;
	private String mMotonReverseType = JSON_VALUE_MOTION_REVERSE_STOP;
	private int mCMIndex = 0;//current motion index
	private int mNMIndex = 0;//next motion index
	private boolean isReversed = false;
	private Resolution mBaseResoltuion = Resolution.NHD;
	private ArrayList <Integer> mMoveIndex = new ArrayList<Integer>();
	private ArrayList <Integer> mMoveTime = new ArrayList<Integer>();
	private static final float FRAME_DURATION = 100f;
	private int mAnimatioinIndex = 0;
	// // // // // Constructor.
	// // // // //
	DynamicTextureEffect(Scene parent) {
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

		JsonObject jsonTextureObject = super.toJsonObject();
		jsonTextureObject.put(JSON_NAME_TEXTURE, mTextures);
		jsonTextureObject.put(JSON_NAME_MOTION_REVERSE, mMotonReverseType);
		jsonTextureObject.put(Resolution.DEFAULT_JSON_NAME, mBaseResoltuion);
		jsonTextureObject.put(JSON_NAME_MOTION, mMotions);

		return jsonTextureObject;
	}

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);

		clearTextures();
		JsonArray textures = jsonObject.getJSONArray(JSON_NAME_TEXTURE);
		for(int i=0;i<textures.length();i++){
			JsonObject texture = textures.getJSONObject(i);
			addTexture(texture.getJSONObject(JSON_NAME_IMAGE_RESOURCE), texture.getString(JSON_NAME_TEXTURE_PATH), texture.getFloat(JSON_NAME_TEXTURE_SCALE), texture.getFloat(JSON_NAME_TEXTURE_ROTATE));
			
		}
		setState(jsonObject.getString(JSON_NAME_MOTION_REVERSE),Resolution.createFrom(jsonObject.optJSONObject(Resolution.DEFAULT_JSON_NAME)));
		parseMotions(jsonObject.getJSONArray(JSON_NAME_MOTION));
	}

	private void parseMotions(JsonArray jsonArray) {
		
		//ArrayList< DynamicTextureMotion> tmp = new ArrayList< DynamicTextureMotion>();
		int size = jsonArray.length();
		for (int i = 0; i < size; i++) {
			JsonObject jsonMotion;
			try {
				jsonMotion = jsonArray.getJSONObject(i);
				addMotion(jsonMotion);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	@Override
	Size[] getCanvasRequirement() {
		if(mTextures.size()==0) 
			return DO_NOT_NEED_CANVAS;
		Size[] size = new Size[mTextures.size()];
		int i = 0;
		for(DynamicTexture texture:mTextures){
			size[i++] = texture.mImageResource.measureSize(getResolution(), texture.mImageScale, texture.mImageRotate);
		}
		return size;
	}

	@Override
	public int getCacheCount() {
		// TODO Auto-generated method stub
		if(mTextures==null) 
			return 0;
		return mTextures.size();
	}

	@Override
	Bitmap createCacheAsBitmap(int index) throws IOException {
		if (mTextures.get(index).mImageResource == null) return null;
		Bitmap bitmap = mTextures.get(index).mImageResource.createBitmap(getResolution(), mTextures.get(index).mImageScale, mTextures.get(index).mImageRotate);
		return bitmap;
	}

	@Override
	void prepareCanvasWithCache() throws IOException {
		if (getCacheCount() == 0)
			return;
		for (int i = 0; i < getCacheCount(); i++) {
			getCacheManager().decodeImageCache(getCacheCodeChunk(i), getCanvas(i));
		}
	}

	@Override
	void prepareCanvasWithoutCache() throws IOException {
		if (getCacheCount() == 0)
			return;
		for (int i = 0; i < getCacheCount(); i++) {
			PixelExtractUtils.extractARGB(createCacheAsBitmap(i), getCanvas(i), true);
		}
	}

	@Override
	void onDraw(PixelCanvas dstCanvas) {
		
		if (mTextures == null || mTextures.size() == 0) return;
		
		PixelCanvas pixelCanvas;
		float progress = getPosition();
		
		// / 1. find current& next motion index
		int timeIndex = setCurrentPositon();
		// / 2. check ratio
		float currentRatio = getCurrentMotionRatio(timeIndex);

		// / 3. change point & alpha
		float currentAlpha = getMotionAlpha(mMotions.get(mCMIndex).mAlpha, mMotions.get(mNMIndex).mAlpha, currentRatio);

		int roundDirection = (mCMIndex % 2) == 0 ? -1 : 1;
		Point current = getMotionPoint(mMotions.get(mCMIndex).mMotionP, mMotions.get(mNMIndex).mMotionP, mMotions.get(mCMIndex).mMotionType, currentRatio, roundDirection);
		// / 4. blend texture
		
		float sMultiplier = getResolution().magnification / mBaseResoltuion.magnification;
		if(mMotions.get(mCMIndex).mAnimated){
			int frameMotionIndex = Math.round(progress / FRAME_DURATION) % mTextures.size();
			if (this.mAnimatioinIndex != frameMotionIndex) {
				this.mAnimatioinIndex = frameMotionIndex;
			}
			pixelCanvas = getCanvas(frameMotionIndex);
			pixelCanvas.setOffset(0);
		}
		else {
			pixelCanvas = getCanvas(0);
		}
		pixelCanvas.setOffset(0);
		pixelCanvas.blend(dstCanvas, Math.round(current.x * sMultiplier), Math.round(current.y * sMultiplier), currentAlpha);
	}
	
	// / 1. find current& next motion index in getPosition()
	private int setCurrentPositon() {
		float currentTime = getPosition();
		int i = 0;

		for (; i < mMoveTime.size(); i++) {
			if (currentTime < mMoveTime.get(i))
				break;
		}

		mCMIndex = mMoveIndex.get(i - 1);
		if (i < mMoveIndex.size())
			mNMIndex = mMoveIndex.get(i);
		else
			mNMIndex = mMoveIndex.get(i - 1);
		return i-1;
	}

	// / 2. get current motion ratio
	private float getCurrentMotionRatio(int timeIndex) {
		
		float currentTime = getPosition();
		float currentMotionPosition = currentTime - mMoveTime.get(timeIndex);
		float currentMotionDuration = mMotions.get(mCMIndex).mMotionDuration;
		return currentMotionPosition / currentMotionDuration;
	}

	// / 3-1. change current motion alpha in current motion duration
	private float getMotionAlpha(float start, float next, float currentRatio) {
		boolean getDown = (start > next) ? true : false;
		float gap = ((getDown) ? (start - next) : (next - start)) * currentRatio;
		return (getDown) ? (start - gap) : (start + gap);
	}

	// / 3-2. change current motion point in current motion duration
	private Point getMotionPoint(Point s, Point e, boolean type, float currentRatio, int direction) {
		Point current = new Point();
		if (type) {
			// linear point
			current.x = (int) (s.x + ((e.x - s.x) * currentRatio));
			current.y = (int) (s.y + ((e.y - s.y) * currentRatio));
		} else {
			// circle point
			Point center = new Point(s.x + (e.x - s.x) / 2, s.y + (e.y - s.y) / 2);
			int radius = (int) (Math.sqrt((e.x - s.x) * (e.x - s.x) + (e.y - s.y) * (e.y - s.y)) / 2);
			double angle = 180 * currentRatio * direction;
			angle += getCurrentAngle(s.x, s.y, e.x, e.y);
			current.x = (int) (radius * Math.cos(angle)) + center.x;
			current.y = (int) (radius * Math.sin(angle)) + center.y;

		}
		return current;
	}

	private double getCurrentAngle(int x1, int y1, int x2, int y2) {
		int dx = x2 - x1;
		int dy = y2 - y1;

		double rad = Math.atan2(dx, dy);
		double degree = rad / Math.PI * 180;
		return degree;
	}
	
	
	@Override
	void onPrepare() {
		int effect_duration = getDuration();

		mMoveTime.clear();
		mMoveIndex.clear();
		if (mMotonReverseType.equals(JSON_VALUE_MOTION_REVERSE_INIT)) {
			int duration = 0;
			int i = 0;
			while (duration < effect_duration) {
				mMoveTime.add(duration);
				duration += mMotions.get(i).mMotionDuration;
				mMoveIndex.add(i++);
				if (i >= mMotions.size())
					i = 0;
			}
		} else if (mMotonReverseType.equals(JSON_VALUE_MOTION_REVERSE_BACK)) {
			int duration = 0;
			int i = 0;
			boolean reversed = false;
			while (duration < effect_duration) {
				mMoveTime.add(duration);
				duration += mMotions.get(i).mMotionDuration;
				mMoveIndex.add(i);
				if (reversed)
					i--;
				else
					i++;

				if (i >= mMotions.size()) {
					reversed = true;
					i -= 2;
				}
				if (i < 0) {
					reversed = false;
					i += 2;
				}
			}
		} else {
			int duration = 0;
			int i = 0;
			while (duration < effect_duration) {
				mMoveTime.add(duration);
				duration += mMotions.get(i).mMotionDuration;
				mMoveIndex.add(i++);
				if (i >= mMotions.size())
					break;
			}
		}
	}

	// call from editor
	void addTexture(String path,float scale, float roate, ResourceType resourceType) {
		DynamicTexture texture = new DynamicTexture();
		texture.mImagePath = path;
		
		texture.mImageScale = scale;
		texture.mImageRotate = roate;
		if (ResourceType.ANDROID_RESOURCE == resourceType) {
			int id = getResources().getIdentifier(path, "drawable", getContext().getPackageName());
			Precondition.checkArgument(id>0,"Invalid texture_path - "+path);
			texture.mImageResource = ImageResource.createFromDrawable(id, getResources(), mBaseResoltuion);
		} else{
			texture.mImageResource = ImageResource.createFromFile(path, mBaseResoltuion);
		}
		mTextures.add(texture);
	}

	void setState( String reverse, Resolution resolution){
		mCanReverse = !(reverse.equals(JSON_VALUE_MOTION_REVERSE_STOP))? true:false;
		mMotonReverseType = reverse;
		mBaseResoltuion = resolution;
	}
	
	// call from injectJsonObject
	void addTexture(JsonObject jsonObject, String path, float scale, float roate) {
		DynamicTexture texture = new DynamicTexture();
		texture.mImageScale = scale;
		texture.mImageRotate = roate;
		texture.mImagePath = path;
		
		try {
			texture.mImageResource = ImageResource.createFromJsonObject(getResources(), jsonObject);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mTextures.add(texture);
	}

	void addMotion(Point p, String type, int duration, float alpha,boolean animated) {
		mMotions.add(new DynamicTextureMotion(p, type, duration, alpha,animated));
	}
	
	void addMotion(JsonObject motion) {
		try {
			mMotions.add(new DynamicTextureMotion(motion));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	void clearTextures() {
		mTextures.clear();
		mMotions.clear();
	}
	

	// // // // // Inner class.
	// // // // //
	public static final class Editor extends Effect.Editor<DynamicTextureEffect, Editor> {

		private Editor(DynamicTextureEffect effect) {
			super(effect);
		}

		public Editor setState( String reverse,Resolution resolution){
			getObject().setState( reverse,resolution);
			return this;
		}
		public Editor addTexture(String path, float scale, float rotate, ResourceType resourceType) {
			getObject().addTexture(path, scale, rotate, resourceType);
			return this;
		}

		public Editor addMotion(Point p, String type, int duration, float alpha, boolean animated) {
			getObject().addMotion(p, type, duration, alpha, animated);
			return this;
		}

		public Editor clearTextures() {
			getObject().clearTextures();
			return this;
		}
		
	}

	// // // // // Inner class.
	// // // // //

	static final class DynamicTexture implements IJsonConvertible, ICacheCode {
		
		public ImageResource mImageResource;
		public String mImagePath;
		public float mImageScale = 1.0f;
		public float mImageRotate = 0.0f;
		
		@Override
		public Object toJsonObject() throws JSONException {
			// TODO Auto-generated method stub
			JsonObject jsonTextureObject = new JsonObject();
			jsonTextureObject.putOpt(JSON_NAME_IMAGE_RESOURCE, mImageResource);			
			jsonTextureObject.put(JSON_NAME_TEXTURE_PATH, mImagePath);
			jsonTextureObject.put(JSON_NAME_TEXTURE_SCALE, mImageScale);
			jsonTextureObject.put(JSON_NAME_TEXTURE_ROTATE, mImageRotate);
			return jsonTextureObject;
		}
		
		@Override
		public int createCacheCode() {

			StringBuilder builder = new StringBuilder();
			builder.append(mImagePath);
			builder.append(mImageScale);
			builder.append(mImageRotate);

			return builder.toString().hashCode() ^ mImageResource.createCacheCode();
		}
	}
	
	
	/**
	 * 텍스쳐의 포인트, 투명도, 움직임 모양(직선, 곡선), duration값을 가지고 있는 클래스
	 */
	static final class DynamicTextureMotion implements IJsonConvertible{

		public static final String MOTION_LINEAR = "motion_linear";
		public static final String MOTION_ROUND = "motion_round";
		public boolean mMotionType = false;
		/**
		 * 현재 모션의 위치값.
		 */
		public Point mMotionP = new Point();
		/**
		 * 현재 모션의 투명도
		 */
		public float mAlpha = 1.0f;
		/**
		 * 현재 모션의 진행 시간값. ms기준.(a->b 이동값)
		 */
		public int mMotionDuration;
		/**
		 * 해당 모션에 Image 애니메이션이 필요한지 설정한다.
		 * @param p
		 * @param type
		 * @param duration
		 * @param alpha
		 */
		public boolean mAnimated = false;
		
		public DynamicTextureMotion(Point p, String type, int duration, float alpha, boolean animated) {

			this.mMotionP = p;
			this.mAlpha = alpha;
			this.mMotionType = type.equals(MOTION_LINEAR)?true:false;
			this.mMotionDuration = duration;
			this.mAnimated = animated;
		}
		
		public DynamicTextureMotion(JsonObject jsonMotion) throws JSONException {
			String type =  jsonMotion.getString(JSON_NAME_MOTION_MOVE_TYPE);
			this.mMotionP = new Point(jsonMotion.getInt(JSON_NAME_MOTION_MOVE_X), jsonMotion.getInt(JSON_NAME_MOTION_MOVE_Y));
			this.mAlpha = jsonMotion.getFloat(JSON_NAME_MOTION_ALPHA);
			this.mMotionType = type.equals(MOTION_LINEAR)?true:false;
			this.mMotionDuration = jsonMotion.getInt(JSON_NAME_MOTION_DURATION);
			this.mAnimated = jsonMotion.optBoolean(JSON_NAME_MOTION_ANIMATED);
		}

		@Override
		public Object toJsonObject() throws JSONException {
			// TODO Auto-generated method stub
			JsonObject motionObject = new JsonObject();
			motionObject.put(JSON_NAME_MOTION_MOVE_X, mMotionP.x);
			motionObject.put(JSON_NAME_MOTION_MOVE_Y, mMotionP.y);
			motionObject.put(JSON_NAME_MOTION_MOVE_TYPE, mMotionType? MOTION_LINEAR:MOTION_ROUND);
			motionObject.put(JSON_NAME_MOTION_DURATION, mMotionDuration);
			motionObject.put(JSON_NAME_MOTION_ALPHA, mAlpha);
			motionObject.put(JSON_NAME_MOTION_ANIMATED, mAnimated);
			return motionObject;
		}
	}
}
