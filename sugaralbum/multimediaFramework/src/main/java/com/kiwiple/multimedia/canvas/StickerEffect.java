package com.kiwiple.multimedia.canvas;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.ResourceType;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * Edit> sticker에서 사용되는 기능을 무비 다이어리에서 사용하기 위해서 추가된 효과
 * 싱글 스티커와 에니메이션 스티커에 해당하는 효과를 지원한다.
 * 
 * @author aubergine
 *
 */
public final class StickerEffect extends Effect {

	// // // // // Static variable.
	public static final String JSON_VALUE_TYPE = "sticker_effect";
	public static final String JSON_NAME_OBJECT = "animation_object";
	
	//for image sticker
	public static final String JSON_NAME_FILE_NAME = "file_name";
	public static final String JSON_NAME_RESOURCE_TYPE = ResourceType.DEFAULT_JSON_NAME;
	
	
	//for text sticker
	public static final String JSON_NAME_IMAGE_RESOURCE = ImageResource.DEFAULT_JSON_NAME;
	public static final String JSON_NAME_TEXT = "text";
	public static final String JSON_NAME_TEXT_WIDTH = "text_width";
	public static final String JSON_NAME_TEXT_BORDER_COLOR = "text_border_color";
	public static final String JSON_NAME_TEXT_BORDER_WIDTH = "text_border_width";
	public static final String JSON_NAME_TEXT_STYLE = "text_style";
	public static final String JSON_NAME_FONT_COLOR = "text_font_color";
	public static final String JSON_NAME_TYPE_FACE_FILE_PATH = "type_face_file_path";
	
	public static final String JSON_NAME_COORDINATE_X = "coordinate_x";
	public static final String JSON_NAME_COORDINATE_Y = "coordinate_y";
	public static final String JSON_NAME_SCALE = "scale";
	public static final String JSON_NAME_ROTATE = "rotate";
	public static final String JSON_NAME_STICKER_WIDTH = "sticker_width";
	public static final String JSON_NAME_STICKER_HEIGHT = "sticker_height";
	public static final String JSON_NAME_CATEGORY = "category";
	public static final String JSON_NAME_SUB_CATEGORY = "sub_category";

    public static final String JSON_NAME_BASE_WIDTH = "base_width";
	// // // // // Member variable.
	// // // // //
	private final static String NULL_STRING = "null_data";
	private final static int DEFAULT_TEXT_SIZE = 24;//TextFrameView.DEFAULT_TEXT_SIZE
	private static final float FRAME_DURATION = 165f;//StickerController.STICKER_ANIMATION_DURATION과 동일하게 적
	@CacheCode(indexed = true)
	private List<StickerInfo> mAnimationObjs = new ArrayList<StickerInfo>();
	private int mMotionIndex = 0;
	// // // // // Constructor.
	// // // // //
	StickerEffect(Scene parent) {
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

		for (StickerInfo animation: mAnimationObjs) {
			JsonObject jsonAnimationObj = new JsonObject();
			
			jsonAnimationObj.put(JSON_NAME_COORDINATE_X, animation.mCoordinateX);
			jsonAnimationObj.put(JSON_NAME_COORDINATE_Y, animation.mCoordinateY);
			jsonAnimationObj.put(JSON_NAME_SCALE, animation.mScale);
			jsonAnimationObj.put(JSON_NAME_ROTATE, animation.mRotate);
			jsonAnimationObj.put(JSON_NAME_FILE_NAME, animation.mFileName);
			jsonAnimationObj.put(JSON_NAME_STICKER_WIDTH, animation.mWidth);
			jsonAnimationObj.put(JSON_NAME_STICKER_HEIGHT, animation.mHeight);
			jsonAnimationObj.put(JSON_NAME_CATEGORY, animation.mCategory);
			jsonAnimationObj.put(JSON_NAME_SUB_CATEGORY, animation.mSubCategory);
			
			jsonAnimationObj.put(JSON_NAME_BASE_WIDTH, animation.mBaseWidth);
			
			jsonAnimationObj.put(JSON_NAME_TEXT, animation.mText);
			jsonAnimationObj.put(JSON_NAME_TEXT_WIDTH, animation.mTextWidth);
			jsonAnimationObj.put(JSON_NAME_TEXT_BORDER_COLOR , animation.mTextBorderColor);
			jsonAnimationObj.put(JSON_NAME_TEXT_BORDER_WIDTH, animation.mTextBorderWidth);
			jsonAnimationObj.put(JSON_NAME_TEXT_STYLE, animation.mTextStyle);
			jsonAnimationObj.put(JSON_NAME_FONT_COLOR, animation.mTextColor);
			jsonAnimationObj.put(Resolution.DEFAULT_JSON_NAME,animation.mBaseResolution);
			if(animation.mTextTypeFacePath != null){
				jsonAnimationObj.put(JSON_NAME_TYPE_FACE_FILE_PATH , animation.mTextTypeFacePath);
			}
			
			if(animation.mImageObjectResource != null){
				jsonAnimationObj.put(JSON_NAME_IMAGE_RESOURCE, animation.mImageObjectResource);
			}
				
			jsonArray.put(jsonAnimationObj);
		}
		jsonObject.put(JSON_NAME_OBJECT, jsonArray);
		
		//L.e(" toJsonObject, "+ jsonObject.toString());
		return jsonObject;
	}
	

	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);
		//L.e("StickerEffect, injectJsonObject, " + jsonObject.toString());

		JsonArray jsonArray = jsonObject.getJSONArray(JSON_NAME_OBJECT);

		if (mAnimationObjs.size() > 0) mAnimationObjs.clear();

		try {
			for (int i = 0; i < jsonArray.length(); i++) {
				JsonObject jsonObj = jsonArray.getJSONObject(i);
				if (jsonObj.isNull(JSON_NAME_FILE_NAME)) {
					String typePath = null;
					if (!jsonObj.isNull(JSON_NAME_TYPE_FACE_FILE_PATH))
						typePath = jsonObj.getString(JSON_NAME_TYPE_FACE_FILE_PATH);

					mAnimationObjs.add(new StickerInfo(jsonObj.getString(JSON_NAME_TEXT), typePath, jsonObj.getInt(JSON_NAME_TEXT_STYLE), jsonObj.getInt(JSON_NAME_FONT_COLOR), (float) jsonObj.getDouble(JSON_NAME_TEXT_WIDTH), (float) jsonObj.getDouble(JSON_NAME_TEXT_BORDER_WIDTH), jsonObj.getInt(JSON_NAME_TEXT_BORDER_COLOR), (float) jsonObj.getDouble(JSON_NAME_COORDINATE_X), (float) jsonObj
							.getDouble(JSON_NAME_COORDINATE_Y), (float) jsonObj.getDouble(JSON_NAME_SCALE), jsonObj.getInt(JSON_NAME_ROTATE), (float) jsonObj.getDouble(JSON_NAME_STICKER_WIDTH), (float) jsonObj.getDouble(JSON_NAME_STICKER_HEIGHT), NULL_STRING, NULL_STRING, jsonObj.getInt(JSON_NAME_BASE_WIDTH), jsonObject.isNull((Resolution.DEFAULT_JSON_NAME)) ? Resolution.NHD : Resolution
							.createFrom(jsonObject.getJSONObject(Resolution.DEFAULT_JSON_NAME))));
				} else {
					mAnimationObjs.add(new StickerInfo(jsonObj, jsonObj.getString(JSON_NAME_FILE_NAME), (float) jsonObj.getDouble(JSON_NAME_COORDINATE_X), (float) jsonObj.getDouble(JSON_NAME_COORDINATE_Y), (float) jsonObj.getDouble(JSON_NAME_SCALE), jsonObj.getInt(JSON_NAME_ROTATE), (float) jsonObj.getDouble(JSON_NAME_STICKER_WIDTH), (float) jsonObj.getDouble(JSON_NAME_STICKER_HEIGHT),
							NULL_STRING, NULL_STRING, jsonObj.getInt(JSON_NAME_BASE_WIDTH), jsonObject.isNull((Resolution.DEFAULT_JSON_NAME)) ? Resolution.NHD : Resolution.createFrom(jsonObject.getJSONObject(Resolution.DEFAULT_JSON_NAME))));
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	/**
	 * StickerView에서 전달 받은 데이타는 원본 데이타의 위치값이다. scale과 rotate 처리를 위해서는 해당 coordinate값으로 다시 계산을 해서 처리해야 한다.
	 * 
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param scale
	 * @param baseScael
	 * @param rotatle
	 * @return
	 */
	private Point getScaleRotatedCoordinate(float x, float y, float width, float height, float scale, float baseScale, float ratate, int logicalW, int logicalH){

		Point newCoordinate = new Point();
		
		float centerX = (x+width/2);
		float centerY = (y+height/2);
		
		newCoordinate.x = (int)(centerX *baseScale - logicalW/2);
		newCoordinate.y = (int)(centerY *baseScale - logicalH/2);
		
		return newCoordinate;
	}


	@Override
	void onDraw(PixelCanvas dstCanvas) {
		if(mAnimationObjs.size()==0) return;

		
		if(mAnimationObjs.get(0).mStickerType == StickerInfo.TEXT_STICKER){
			StickerInfo info = mAnimationObjs.get(0);
			info.mBaseScale  = (float)(getResolution().width)/(float)(info.mBaseWidth);
			Point newP = getScaleRotatedCoordinate(info.mCoordinateX,info.mCoordinateY,info.mWidth,info.mHeight, info.mScale,info.mBaseScale,info.mRotate,
					getCanvas(0).getImageWidth(),getCanvas(0).getImageHeight());;			
			getCanvas(0).blend(dstCanvas,newP.x ,newP.y );
		}
		else {
			
			float progress = getPosition();
			int frameMotionIndex = Math.round(progress / FRAME_DURATION) % mAnimationObjs.size();
			if (this.mMotionIndex != frameMotionIndex) {
				this.mMotionIndex = frameMotionIndex;
			}
			
			StickerInfo animation = mAnimationObjs.get(frameMotionIndex);
			animation.mBaseScale  = (float)(getResolution().width)/(float)(animation.mBaseWidth);
			PixelCanvas pixelCanvas = getCanvas(frameMotionIndex);			
			Point newP = getScaleRotatedCoordinate(animation.mCoordinateX,animation.mCoordinateY,animation.mWidth,animation.mHeight, animation.mScale,animation.mBaseScale,animation.mRotate,
					pixelCanvas.getImageWidth(),pixelCanvas.getImageHeight());
			pixelCanvas.blend(dstCanvas, newP.x, newP.y);
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
		return mAnimationObjs.size();
	}
	
	@Override
	Size[] getCanvasRequirement() {
		Size[] size;
		if (mAnimationObjs.size() == 0) {
			return DO_NOT_NEED_CANVAS;
		} else {
			size = new Size[mAnimationObjs.size()];
			int index = 0;
			for (StickerInfo info : mAnimationObjs) {
				info.mBaseScale  = (float)(getResolution().width)/(float)(info.mBaseWidth);
				if(info.mStickerType == StickerInfo.TEXT_STICKER){
					float sMultiplier =info.mScale*info.mBaseScale;
					size[index] = new Size((int)(info.mWidth * sMultiplier),(int)(info.mHeight * sMultiplier)); // FIXME: need to optimize.
					if(info.mRotate !=0){
						RectF rect = new RectF(0, 0, size[index].width, size[index].height);
						Matrix matrix = new Matrix();
						matrix.postRotate(-info.mRotate);
						matrix.mapRect(rect);
						size[index] =  new Size(Math.round(rect.width()), Math.round(rect.height()));
					}
					index++;
				}
				else {
					float base = (float)(info.mBaseResolution.width)/(float)(info.mBaseWidth);
					size[index++] = info.mImageObjectResource.measureSize(getResolution(),info.mScale * base, info.mRotate*(-1));
				}
			}
		}
		return size;
	}
	

	void  setResource(String text, String typeface,int textStyle,int color,float text_width, float border_width, int border_color, float cx, float cy, float scale, int rotate,float width, float height,String category, String subCategory, int baseWidth, Resolution resolution){
		StickerInfo tmp = new StickerInfo(text,  typeface, textStyle, color, text_width, border_width,  border_color,  cx,  cy,  scale,  rotate, width,  height, category,  subCategory, baseWidth, resolution);
		mAnimationObjs.add(tmp);
	}
	
	void setResource(String path, float cx, float cy, float scale, int rotate,float width, float height,String category, String subCategory, int baseWidth,Resolution resolution,ResourceType resourceType) {
		StickerInfo tmp = new StickerInfo(path,cx, cy,scale, rotate,width, height,category, subCategory, baseWidth,resolution, resourceType);
		mAnimationObjs.add(tmp);
	}
	
	void setResource(int id, float cx, float cy, float scale, int rotate,float width, float height,String category, String subCategory, int baseWidth,Resolution resolution) {
		StickerInfo tmp = new StickerInfo(id,cx, cy, scale, rotate,width, height,category, subCategory, baseWidth,resolution);
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
		StickerInfo info = mAnimationObjs.get(index);
		info.mBaseScale  = (float)(getResolution().width)/(float)(info.mBaseWidth);
		if(info.mStickerType == StickerInfo.IMAGE_STICKER){
			float base = (float)(info.mBaseResolution.width)/(float)(info.mBaseWidth);
			Bitmap bitmap = info.mImageObjectResource.createBitmap(resolution,info.mScale * base, info.mRotate*(-1));
			return bitmap;
		}else {
			//TextSticker
			float sMultiplier =info.mScale * info.mBaseScale;
			float mDensity = getResources().getDisplayMetrics().density;
			
			Paint borderPaint = new Paint();
			borderPaint.setStyle(Style.STROKE);
			borderPaint.setStrokeWidth(info.mTextBorderWidth* sMultiplier);
			borderPaint.setColor(info.mTextBorderColor);
			borderPaint.setTextSize(DEFAULT_TEXT_SIZE * mDensity * sMultiplier);
			borderPaint.setTextAlign(Paint.Align.CENTER);
			borderPaint.setTypeface(info.mTextTypeFace);
			
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setStrokeWidth(2);
			paint.setTypeface(info.mTextTypeFace);
			paint.setStyle(Style.FILL_AND_STROKE);
			paint.setColor(info.mTextColor); 
			paint.setTextSize(DEFAULT_TEXT_SIZE * mDensity * sMultiplier);
			paint.setTextAlign(Paint.Align.CENTER);
			
			Bitmap bitmap = Bitmap.createBitmap((int) (info.mWidth * sMultiplier), (int) (info.mHeight * sMultiplier), BITMAP_CONFIG);
			Canvas canvas = new Canvas(bitmap);
			canvas.drawText(info.mText, bitmap.getWidth() / 2, -paint.ascent(), borderPaint);
			canvas.drawText(info.mText, bitmap.getWidth() / 2, -paint.ascent(), paint);

			if(info.mRotate != 0){
				Matrix matrix = new Matrix();
				matrix.preRotate(-info.mRotate);
				Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0,0,bitmap.getWidth(), bitmap.getHeight(), matrix, true);
				return rotatedBitmap;
			}
			return bitmap;
		}
	}
	
	public List<StickerInfo> getStickerInfos() {
		return deepCopyOf(mAnimationObjs);
	}
	
	public List<StickerInfo> deepCopyOf(List<StickerInfo> original) {

		if (original == null) {
			return new ArrayList<StickerInfo>(0);
		}

		ArrayList<StickerInfo> copy = new ArrayList<>(original.size());

		for (StickerInfo other : original) {
			copy.add(new StickerInfo(other));
		}
		return copy;
	}

	// // // // // Inner class.
	// // // // //
	public static final class Editor extends Effect.Editor<StickerEffect, Editor> {


		private Editor(StickerEffect animationStickerEffect) {
			super(animationStickerEffect);
		}
		
		/**
		 * TextSticker의 리소스를 지정한다.
		 * 
		 * @param text
		 * @param typeface
		 * @param textStyle
		 * @param color
		 * @param text_width
		 * @param border_width
		 * @param border_color
		 * @param cx
		 * @param cy
		 * @param scale
		 * @param rotate
		 * @param width
		 * @param height
		 * @param category
		 * @param subCategory
		 * @param baseWidth
		 * @return
		 */
		public Editor setResource(String text, String typeface,int textStyle,int color,float text_width, float border_width, int border_color, float cx, float cy, float scale, int rotate,float width, float height,String category, String subCategory, int baseWidth,Resolution resolution){
			getObject().setResource( text,  typeface, textStyle, color, text_width, border_width,  border_color,  cx,  cy,  scale,  rotate, width,  height, category,  subCategory, baseWidth,  resolution);
			return this;
		}
		
		/**
		 * 이미지 스티커의 리소스를 지정한다.
		 * 
		 * @param path
		 * @param cx
		 * @param cy
		 * @param scale
		 * @param rotate
		 * @param width
		 * @param height
		 * @param category
		 * @param subCategory
		 * @param baseWidth
		 * @param resolution
		 * @param resourceType
		 * @return
		 */
		public Editor setResource(String path, float cx, float cy, float scale, int rotate,float width, float height,String category, String subCategory, int baseWidth, Resolution resolution, ResourceType resourceType) {
			getObject().setResource(path, cx, cy, scale, rotate,width, height,category, subCategory, baseWidth,resolution,resourceType);
			return this;
		}
		
		/**
		 * drawable 이미지 스티커 리소스를 지정한다.
		 * 
		 * @param id
		 * @param cx
		 * @param cy
		 * @param scale
		 * @param rotate
		 * @param width
		 * @param height
		 * @param category
		 * @param subCategory
		 * @param baseWidth
		 * @param resolution
		 * @return
		 */
		public Editor setResource(int id, float cx, float cy,float scale, int rotate,float width, float height,String category, String subCategory, int baseWidth,Resolution resolution) {
			getObject().setResource(id, cx, cy,scale, rotate,width, height,category, subCategory, baseWidth,resolution);
			return this;
		}
		
		/**
		 * 지정된 스티커 리소스를 모 제거한다.
		 * 
		 * @return
		 */
		public Editor clear() {
			getObject().clear();
			return this;
		}

	}
	
	/**
	 * AnimationStickerEffect Resource Data
	 * @author aubergine
	 *
	 */
	public final class StickerInfo implements ICacheCode {
		public static final int IMAGE_STICKER = 100;
		public static final int TEXT_STICKER = 200;
		
		public int mStickerType = IMAGE_STICKER;
		
		
		public float mCoordinateX;
		public float mCoordinateY;
		public float mScale = 1.0f;
		public float mRotate = 0.0f;
		
		public float mWidth = 0;
		public float mHeight = 0;
		public String mCategory = NULL_STRING;
		public String mSubCategory = NULL_STRING;
		
		//for image sticker
		public String mFileName =null;
		public ImageResource mImageObjectResource;
		
		public float mBaseScale = 1.0f;//FIXME 
		public int mBaseWidth = Resolution.NHD.width;
		public int mBaseHeight = Resolution.NHD.height;
		
		//for text sticker
		public String mText ;
		public float mTextWidth = 0;
		public float mTextHeight = 0;
		public int mTextColor = Color.WHITE;
		public int mTextBorderColor = Color.WHITE;
		public float mTextBorderWidth = 0;
		public int mTextStyle;
		public String mTextTypeFacePath;
		public Typeface mTextTypeFace;
		private int mSize = DEFAULT_TEXT_SIZE;
		private Paint.Align mAlign = Paint.Align.LEFT;

		private Resolution mBaseResolution = Resolution.NHD;
		
		StickerInfo(StickerInfo other) {
			Precondition.checkNotNull(other, "other must not be null.");

			this.mStickerType = other.mStickerType;
			this.mCoordinateX = other.mCoordinateX;
			this.mCoordinateY = other.mCoordinateY;
			this.mScale = other.mScale;
			this.mRotate = other.mRotate;
			this.mWidth = other.mWidth;
			this.mHeight = other.mHeight;
			this.mCategory = other.mCategory;
			this.mSubCategory = other.mSubCategory;
			this.mFileName = other.mFileName;
			this.mImageObjectResource = other.mImageObjectResource;
			this.mText = other.mText;
			this.mTextWidth = other.mTextWidth;
			this.mTextHeight = other.mTextHeight;
			this.mTextColor = other.mTextColor;
			this.mTextBorderColor = other.mTextBorderColor;
			this.mTextBorderWidth = other.mTextBorderWidth;
			this.mTextStyle = other.mTextStyle;
			this.mTextTypeFace = other.mTextTypeFace;
			this.mTextTypeFacePath = other.mTextTypeFacePath;
			this.mSize = other.mSize;
			this.mAlign = other.mAlign;
			this.mBaseScale = other.mBaseScale;
			this.mBaseWidth = other.mBaseWidth;
			this.mBaseResolution = other.mBaseResolution;
		}
		
		/**
		 * text sticker effect 
		 * 
		 * @param text
		 * @param typeface
		 * @param color
		 * @param border_width
		 * @param border_color
		 * @param cx
		 * @param cy
		 * @param scale
		 * @param rotate
		 * @param width
		 * @param height
		 * @param category
		 * @param subCategory
		 * @param resolution
		 */
		public StickerInfo(String text, String typeface,int textStyle,int color,float text_width, float border_width, int border_color, float cx, float cy, float scale, int rotate,float width, float height,String category, String subCategory, int baseWidth, Resolution resolution) {
			this.mStickerType = TEXT_STICKER;
			this.mCoordinateX = cx;
			this.mCoordinateY = cy;
			this.mScale = scale; 
			this.mRotate = rotate;
			this.mCategory = category;
			this.mSubCategory = subCategory;
			
			this.mText = text;
			this.mTextTypeFacePath = typeface;
			this.mTextColor = color;
			this.mTextWidth = text_width;
			this.mWidth = width;
			this.mTextHeight= this.mHeight = height;
			this.mTextBorderColor = border_color;
			this.mTextBorderWidth = border_width;
			this.mTextStyle = textStyle;
			this.mBaseWidth = baseWidth;
			this.mBaseResolution = resolution;
			this.mTextTypeFace = Typeface.DEFAULT;
			if(this.mTextTypeFacePath != null && this.mTextTypeFacePath.length()>0){
				mTextTypeFace = Typeface.createFromFile(this.mTextTypeFacePath);
			}
		}
		
		/**
		 * AnimationStickerInfo생성자 
		 * 
		 * @param path 이미지파일 path
		 * @param cx 이미지 x 좌표 
		 * @param cy 이미지 y 좌표 
		 * @param mt 모션타입으로 {@link MOTION_SCALE}, {@link MOTION_ROTATE}, {@link MOTION_NONE} 중 설정 
		 * @param value 모션값.모션 타입에 따라 해당 값 설정 
		 */
		public StickerInfo(String filePath, float cx, float cy, float scale, int rotate,float width, float height,String category, String subCategory, int baseWidth,Resolution resolution,ResourceType resourceType) {
			this.mCoordinateX = cx;
			this.mCoordinateY = cy;
			this.mScale = scale;
			this.mRotate = rotate;
			this.mCategory = category;
			this.mSubCategory = subCategory;
			this.mFileName = filePath;
			this.mWidth = width;
			this.mHeight = height;
			this.mBaseWidth = baseWidth;
			this.mBaseResolution = resolution;
			if(ResourceType.ANDROID_RESOURCE == resourceType){
				int id = getResources().getIdentifier(filePath, "drawable", getContext().getPackageName());
				this.mImageObjectResource = ImageResource.createFromDrawable(id, getResources(), resolution);
			}
			else this.mImageObjectResource = ImageResource.createFromFile(filePath, resolution);
		}

		/**
		 * AnimationStickerInfo생성자 
		 * 
		 * @param id 이미지파일 drawable ID
		 * @param cx 이미지 x 좌표 
		 * @param cy 이미지 y 좌표 
		 * @param mt 모션타입으로 {@link MOTION_SCALE}, {@link MOTION_ROTATE}, {@link MOTION_NONE} 중 설정 
		 * @param value 모션값.모션 타입에 따라 해당 값 설정 
		 */
		public StickerInfo(int id, float cx, float cy, float scale, int rotate,float width, float height,String category, String subCategory,  int baseWidth,Resolution resolution) {
			this.mCoordinateX = cx;
			this.mCoordinateY = cy;
			this.mScale = scale;
			this.mRotate = rotate;
			this.mCategory = category;
			this.mSubCategory = subCategory;
			this.mWidth = width;
			this.mHeight = height;
			this.mBaseWidth = baseWidth;
			this.mBaseResolution = resolution;
			this.mImageObjectResource = ImageResource.createFromDrawable(id, getResources(), resolution);
		}
		
		public StickerInfo(JsonObject jsonObject, String file_name, float cx, float cy, float scale, int rotate, float width, float height, String category, String subCategory, int baseWidth, Resolution resolution) throws JSONException {
			this.mCoordinateX = cx;
			this.mCoordinateY = cy;
			this.mScale = scale;
			this.mRotate = rotate;
			this.mCategory = category;
			this.mSubCategory = subCategory;
			this.mFileName = file_name;
			this.mWidth = width;
			this.mHeight = height;
			this.mBaseWidth = baseWidth;
			this.mBaseResolution = resolution;		
			
			if (jsonObject.isNull(JSON_NAME_IMAGE_RESOURCE)) {
				if(file_name.startsWith("/data")) {
					this.mImageObjectResource = ImageResource.createFromFile(file_name, resolution);
				}
				else {
					int id = getResources().getIdentifier(file_name, "drawable", getContext().getPackageName());
					this.mImageObjectResource = ImageResource.createFromDrawable(id, getResources(),resolution);
				}
			} else {
				this.mImageObjectResource = ImageResource.createFromJsonObject(getResources(), jsonObject.getJSONObject(JSON_NAME_IMAGE_RESOURCE));
			}
		}
		
		 /**
	     * 텍스트 스타일을 설정
	     * 
	     * @param style 설정할 스타일
	     */
	    public Style getTextStyle(int style) {
	        if(style == 0){ 
	        	return Style.FILL;
	        } else if(style == 1){
	        	return Style.STROKE;
	        } else{// if(style== 2){
	        	return Style.FILL_AND_STROKE;
	        }
	    }
	    
	    @Override
	    public int createCacheCode() {
	    	
	    	StringBuilder builder = new StringBuilder();
	    	
			builder.append(mStickerType);
			builder.append(mCoordinateX);
			builder.append(mCoordinateY);
			builder.append(mScale);
			builder.append(mRotate);
			builder.append(mWidth);
			builder.append(mHeight);
			builder.append(mCategory);
			builder.append(mSubCategory);
			builder.append(mBaseWidth);
			if (mStickerType == IMAGE_STICKER) {
				builder.append(mFileName);
				builder.append(mImageObjectResource.createCacheCode());
			} else if (mStickerType == TEXT_STICKER) {
				builder.append(mText);
				builder.append(mTextWidth);
				builder.append(mTextHeight);
				builder.append(mTextColor);
				builder.append(mTextBorderColor);
				builder.append(mTextBorderWidth);
				builder.append(mTextStyle);
				builder.append(mTextTypeFace);
				builder.append(mTextTypeFacePath);
				builder.append(mSize);
				builder.append(mAlign);
			}
			
	    	return builder.toString().hashCode();
	    }
	}
}
