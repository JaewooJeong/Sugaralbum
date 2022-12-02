package com.kiwiple.multimedia.canvas;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.TextUtils;

import com.kiwiple.debug.Precondition;
import com.kiwiple.multimedia.ResourceType;
import com.kiwiple.multimedia.annotation.CacheCode;
import com.kiwiple.multimedia.canvas.DynamicTextureEffect.DynamicTexture;
import com.kiwiple.multimedia.canvas.DynamicTextureEffect.Editor;
import com.kiwiple.multimedia.json.IJsonConvertible;
import com.kiwiple.multimedia.json.JsonArray;
import com.kiwiple.multimedia.json.JsonObject;
import com.kiwiple.multimedia.util.Size;

/**
 * 문자열을 그려주는 효과를 처리한다.
 * 문자열의 색상, 움직일 위치, 폰트, 크기, 회전정도를 설정할 수 있다.
 * 입력된 데이타는 FHD  기준으로 주어진 데이타로 가정하여 사용되어진다.
 * 
 * @author aubergine
 *
 */
public final class TextEffect extends Effect {
	public static final String JSON_VALUE_TYPE = "text_effect";
	
	public static final String JSON_NAME_TEXT = "text";
	public static final String JSON_NAME_SIZE = "size";
	public static final String JSON_NAME_COLOR = "color";
	public static final String JSON_NAME_TYPEFACE_PATH = "typeface_filepath";
	public static final String JSON_NAME_COORDINATE_LEFT_X = "coodinate_left_top_x";
	public static final String JSON_NAME_COORDINATE_LEFT_Y = "coodinate_left_top_y";
	public static final String JSON_NAME_COORDINATE_RIGHT_X = "coodinate_right_bottom_x";
	public static final String JSON_NAME_COORDINATE_RIGHT_Y = "coodinate_right_bottom_y";
	public static final String JSON_NAME_ALIGN = "align";
	public static final String JSON_VALUE_ALIGN_CENTER = "center";
	public static final String JSON_VALUE_ALIGN_LEFT = "left";
	public static final String JSON_VALUE_ALIGN_RIGHT = "right";	
	
	public static final String JSON_NAME_MOTION = "motion";
	public static final String JSON_NAME_MOTION_DURATION = "motion_duration";
	public static final String JSON_NAME_MOTION_MOVE_X = "motion_x";
	public static final String JSON_NAME_MOTION_MOVE_Y = "motion_y";
	
	public static final String JSON_NAME_IMAGE_RESOURCE = ImageResource.DEFAULT_JSON_NAME;
			
	private Typeface mTypeface;
	@CacheCode
	private String mTypefacePath;
	@CacheCode
	private String mText = "";
	@CacheCode 
	private float mSize = 20;
	@CacheCode
	private int mColor = Color.BLUE;
	@CacheCode
	private int mCoordinate_left_x = 0;
	@CacheCode
	private int mCoordinate_left_y = 0;
	@CacheCode
	private int mCoordinate_right_x = 0;
	@CacheCode
	private int mCoordinate_right_y = 0;
	@CacheCode
	private String mAlignString = JSON_VALUE_ALIGN_CENTER;
	@CacheCode
	private float mStringWidth = 0;
	@CacheCode
	private float mStringHeight = 0;
	private Paint.Align mAlign = Paint.Align.CENTER;
	private Resolution mBaseResolution = Resolution.NHD;
	
	private List<TextMotion> mMotions = new ArrayList<TextMotion>();
	private ArrayList <Integer> mMoveIndex = new ArrayList<Integer>();
	private ArrayList <Integer> mMoveTime = new ArrayList<Integer>();
	private int mCurrentMotionIndex = 0;
	private int mNextMotionIndex = 0;
	@CacheCode
	private ImageResource mImageResource;
	
	TextEffect(Scene parent){
		super(parent);
	}

	@Override
	void onDraw(PixelCanvas dstCanvas) {
		float sMultiplier = getResolution().magnification / mBaseResolution.magnification;

		int dstX = 0;
		int dstY = 0;

		PixelCanvas canvas = getCanvas(0);
		canvas.setImageSize(Math.round(mStringWidth*sMultiplier), Math.round(mStringHeight*sMultiplier ));

		if (mMotions.size() > 0) {
			setCurrentPositon();
			float currentRatio = getCurrentMotionRatio();
			Point current = getMotionPoint(mMotions.get(mCurrentMotionIndex).mMotionP, mMotions.get(mNextMotionIndex).mMotionP, currentRatio);
			dstX = current.x;
			dstY = current.y;
		} else {
			dstX = mCoordinate_left_x;
			dstY = mCoordinate_left_y;
		}

		dstX *= sMultiplier;
		dstY *= sMultiplier;

		canvas.blend(dstCanvas, dstX, dstY);
	}
	

	// // find current& next motion index in getPosition()
	private void setCurrentPositon() {
		float currentTime = getPosition();
		int i = 0;
		for (; i < mMoveTime.size(); i++)
			if (currentTime < mMoveTime.get(i))
				break;

		mCurrentMotionIndex = mMoveIndex.get(i - 1);
		if (i < mMoveIndex.size())
			mNextMotionIndex = mMoveIndex.get(i);
		else
			mNextMotionIndex = mMoveIndex.get(i - 1);
	}

	// / get current motion ratio
	private float getCurrentMotionRatio() {
		float currentTime = getPosition();
		float currentMotionPosition = currentTime - mMoveTime.get(mCurrentMotionIndex);
		float currentMotionDuration = mMotions.get(mCurrentMotionIndex).mMotionDuration;
		return currentMotionPosition / currentMotionDuration;
	}

	// / change current motion point in current motion duration
	private Point getMotionPoint(Point s, Point e, float currentRatio) {
		Point current = new Point();
		current.x = (int) (s.x + ((e.x - s.x) * currentRatio));
		current.y = (int) (s.y + ((e.y - s.y) * currentRatio));
		return current;
	}

	@Override
	void onPrepare() {
		if (mMotions.size() <= 0)
			return;
		int effect_duration = getDuration();

		mMoveTime.clear();
		mMoveIndex.clear();

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

	@Override
	public Editor getEditor() {
		return (Editor) super.getEditor();
	}
	
	@Override
	public JsonObject toJsonObject() throws JSONException {
		JsonObject jsonObject = super.toJsonObject();

		jsonObject.put(JSON_NAME_TEXT, this.mText);
		jsonObject.put(JSON_NAME_ALIGN, this.mAlignString);
		jsonObject.put(JSON_NAME_SIZE, this.mSize);
		jsonObject.put(JSON_NAME_COLOR, this.mColor);
		jsonObject.put(JSON_NAME_TYPEFACE_PATH, this.mTypefacePath);
		jsonObject.put(JSON_NAME_COORDINATE_LEFT_X, this.mCoordinate_left_x);
		jsonObject.put(JSON_NAME_COORDINATE_LEFT_Y, this.mCoordinate_left_y);
		jsonObject.put(JSON_NAME_COORDINATE_RIGHT_X, this.mCoordinate_right_x);
		jsonObject.put(JSON_NAME_COORDINATE_RIGHT_Y, this.mCoordinate_right_y);
		jsonObject.put(JSON_NAME_MOTION, mMotions);
		jsonObject.putOpt(JSON_NAME_IMAGE_RESOURCE, mImageResource);
		jsonObject.put(Resolution.DEFAULT_JSON_NAME, mBaseResolution);
		//L.e("toJsonObject is "+jsonObject.toString());
		return jsonObject;
	}
	
	@Override
	void injectJsonObject(JsonObject jsonObject) throws JSONException {
		super.injectJsonObject(jsonObject);
		//L.e("TextEffect jsondata is "+jsonObject.toString());
		setResourceFontName(jsonObject.getString(JSON_NAME_TYPEFACE_PATH));
		setResourceColor(jsonObject.getInt(JSON_NAME_COLOR));
		setResourceSize(jsonObject.getInt(JSON_NAME_SIZE));
		setResourceAlign(jsonObject.getString(JSON_NAME_ALIGN));
		setResoureCoordinate(jsonObject.getInt(JSON_NAME_COORDINATE_LEFT_X),jsonObject.getInt(JSON_NAME_COORDINATE_LEFT_Y),jsonObject.getInt(JSON_NAME_COORDINATE_RIGHT_X),jsonObject.getInt(JSON_NAME_COORDINATE_RIGHT_Y));
		setResourceText(jsonObject.getString(JSON_NAME_TEXT));
		if(!jsonObject.isNull(Resolution.DEFAULT_JSON_NAME))
			mBaseResolution = Resolution.createFrom(jsonObject.getJSONObject(Resolution.DEFAULT_JSON_NAME));
		else 
			mBaseResolution = Resolution.FHD;
		
		if(!jsonObject.isNull(JSON_NAME_MOTION)){
			parseMotions(jsonObject.getJSONArray(JSON_NAME_MOTION));
		}
		
		if (!jsonObject.isNull(JSON_NAME_IMAGE_RESOURCE)) {
			mImageResource = ImageResource.createFromJsonObject(getResources(), jsonObject.getJSONObject(JSON_NAME_IMAGE_RESOURCE));
		}
	}
	
	private void parseMotions(JsonArray jsonArray){
		int size = jsonArray.length();
		mMotions.clear();
		for (int i = 0; i < size; i++) {
			JsonObject jsonMotion;
			try {
				jsonMotion = jsonArray.getJSONObject(i);
				mMotions.add(new TextMotion(jsonMotion));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	@Override
	void prepareCanvasWithCache() throws IOException {
		getCacheManager().decodeImageCache(getCacheCodeChunk(0), getCanvas(0));	
	}
	
	@Override
	void prepareCanvasWithoutCache() throws IOException {
		PixelExtractUtils.extractARGB(createCacheAsBitmap(0), getCanvas(0), true);
	}
	
	@Override
	public int getCacheCount() {
		return 1;
	}
	
	@Override
	Size[] getCanvasRequirement() {
		float sMultiplier =getResolution().magnification/mBaseResolution.magnification;
		return new Size[] { new Size(Math.round(this.mStringWidth*sMultiplier) ,Math.round( this.mStringHeight*sMultiplier)) }; // FIXME: need to optimize.
	}

	/**
	 * set text align
	 * 
	 * @param align
	 */
	void setResourceAlign(String align){
		this.mAlignString = align;
		if(JSON_VALUE_ALIGN_RIGHT.equals(align)) this.mAlign = Paint.Align.RIGHT;
		else if(JSON_VALUE_ALIGN_LEFT.equals(align)) this.mAlign = Paint.Align.LEFT;
		else this.mAlign = Paint.Align.CENTER;
	}
	
	/**
	 * 문자열의 폰트이름을 지정한다.
	 * 파일패스값이 NULL이거나 잘못된 데이타인 경우, TypeFace.DEFAULT 폰트로 지정한다.
	 * 
	 * @param filePath
	 */
	void setResourceFontName(String fontName){
		//L.e("setResourceTypeface..fontName:"+fontName);
		File f = new File("/system/fonts");
		if (f.exists() && f.isDirectory() && !TextUtils.isEmpty(fontName)) {
			File[] files = f.listFiles();
			if (files == null) {
				mTypeface = Typeface.DEFAULT;
				mTypefacePath= "defaultFont";
				return;
			}
			for (File file : files) {
				if (file.getName().endsWith("ttf") && !file.getName().startsWith("AndroidClock") && !file.getName().startsWith("DroidSansFallback") && !file.getName().startsWith("LGE_Dialfont")) {
					if(file.getName().startsWith(fontName)){
						Typeface typeface = Typeface.createFromFile(file);
						if (typeface != null) {
							mTypefacePath = file.getAbsolutePath();
							mTypeface = typeface;
							return;
						}
					}
				}
			}
			//no match font
			//set default
			mTypeface = Typeface.DEFAULT;
			mTypefacePath= "defaultFont";
		}
	}
	
	/**
	 * 문자열을 지정한다.
	 * 문자열이 NULL 인경우 NullPointerException발생.
	 * 
	 * @param text
	 */
	void setResourceText(String text){
		if(text == null) {
			throw new NullPointerException();
		}
		this.mText = text;
	}
	
	/**
	 * 리소스 문자열을 리턴한.
	 * 
	 * @param text
	 */
	public String getResourceText(){
		return this.mText;
	}
	
	/**
	 * 문자열의 컬러값을 지정한다.
	 * 컬러값의 범위는   0xFF000000 -  0xFFFFFFFF
	 * @param color
	 */
	void setResourceColor(int color){
		this.mColor = color;
	}
	
	/**
	 * 문자열의 크기를 지정한다
	 * 사이즈 값은 반드시 0보다 커야한다..
	 * 
	 * @param size
	 */
	void setResourceSize(float size) {
		if (size <= 0)
			throw new IllegalArgumentException("TextEffect size must be >0");

		this.mSize = size;
	}
	
	/**
	 * 문자열의 Bound를 지정한다.
	 * 
	 * @param fx
	 * @param fy
	 * @param tx
	 * @param ty
	 */
	void setResoureCoordinate(int lcx, int lcy, int rcx, int rcy){
		this.mCoordinate_left_x = lcx;
		this.mCoordinate_left_y = lcy;
		this.mCoordinate_right_x = rcx;
		this.mCoordinate_right_y = rcy;
		this.mStringWidth = rcx - lcx;
		this.mStringHeight = rcy - lcy;
	}
	
	/**
	 * 문자열의 움직임을 추가한다. 
	 * @param p
	 * @param type
	 * @param duration
	 * @param alpha
	 */
	void addMotion(Point p, int duration) {
		mMotions.add(new TextMotion(p, duration));
	}
	
	/**
	 * 문자열의 움직임 정보를 초기화 한다. 
	 */
	void clearMotion(){
		mMotions.clear();
	}
	

	void setBaseResolution(Resolution resolution){
		mBaseResolution = resolution;
	}


	void setBackground(String path, ResourceType resourceType) {
		if (ResourceType.ANDROID_RESOURCE == resourceType) {
			int id = getResources().getIdentifier(path, "drawable", getContext().getPackageName());
			Precondition.checkArgument(id > 0, "Invalid texture_path - " + path);
			mImageResource = ImageResource.createFromDrawable(id, getResources(), mBaseResolution);
		} else {
			mImageResource = ImageResource.createFromFile(path, mBaseResolution);
		}
	}

	
	@Override
	Bitmap createCacheAsBitmap(int index) throws IOException {
		float sMultiplier = getResolution().magnification/mBaseResolution.magnification;
		
		Bitmap bitmap = Bitmap.createBitmap(Math.round(this.mStringWidth*sMultiplier),Math.round(this.mStringHeight*sMultiplier), BITMAP_CONFIG);
		Canvas canvas = new Canvas(bitmap);
		
		if(mImageResource != null) {
			Bitmap bg = mImageResource.createBitmap(getResolution());
			Matrix fit = new Matrix();
			fit.setRectToRect(new RectF(0,0,bg.getWidth(),bg.getHeight()), new RectF(0,0,mStringWidth*sMultiplier, mStringHeight*sMultiplier), Matrix.ScaleToFit.FILL);
			canvas.drawBitmap(bg,fit, null);
		}
		
		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setTypeface(mTypeface);
		paint.setColor(this.mColor); 
		paint.setTextSize(mSize*sMultiplier);
		paint.setTextAlign(this.mAlign);
		
		int i = 0;
		for(;i<=mText.length();++i){
			paint.measureText(mText, 0, i);
			if(paint.measureText(mText, 0, i)>=Math.round(this.mStringWidth*sMultiplier)) {
				break;
			}
		}
		i--;
		
		if(this.mAlign == Paint.Align.LEFT) canvas.drawText(mText.substring(0, i), 0, -paint.ascent(), paint);
		else if(this.mAlign == Paint.Align.RIGHT) canvas.drawText(mText.substring(0, i), Math.round(this.mStringWidth*sMultiplier), -paint.ascent(), paint);
		else canvas.drawText(mText.substring(0, i),Math.round(this.mStringWidth*sMultiplier)/2, -paint.ascent(), paint);	
		return bitmap;
	}
		
	public static final class Editor extends Effect.Editor<TextEffect, Editor> {

		private Editor(TextEffect textEffect) {
			super(textEffect);
		}
		
		/**
		 * 문자열의 폰트파일패스를 지정한다.
		 * 
		 * @param filePath
		 */
		public Editor setResourceFontName(String fontName){
			getObject().setResourceFontName(fontName);
			return this;
		}
		
		/**
		 * 문자열을 지정한다.
		 * @param text
		 */
		public Editor setResourceText(String text){
			getObject().setResourceText(text);
			return this;
		}
		
		/**
		 * 문자열의 컬러값을 지정한다.
		 * 컬러값의 범위는   0xFF000000 -  0xFFFFFFFF
		 * @param color
		 */
		public Editor setResourceColor(int color){
			getObject().setResourceColor(color);
			return this;
		}
		
		/**
		 * 문자열의 크기를 지정한다.
		 * 사이즈 값은 반드시 0보다 커야한다.
		 * 
		 * @param size
		 */
		public Editor setResourceSize(float size){
			getObject().setResourceSize(size);
			return this;
		}
		
		/**
		 * 지정한 coordinate내에서 문자열 정렬값을 지정한다.
		 * 
		 * @param align
		 * @return
		 */
		public Editor setResourceAlign(String align){
			getObject().setResourceAlign(align.toLowerCase());
			return this;
		}
		
		/**
		 * 문자열이 Bound를 지정한다.
		 * 
		 * 
		 * @param fx
		 * @param fy
		 * @param tx
		 * @param ty
		 */
		public Editor setResoureCoordinate(int fx, int fy, int tx, int ty){
			getObject().setResoureCoordinate(fx, fy, tx, ty);
			return this;
		}

		
		/**
		 * 문자열의 움직임 정보를 추가한다.
		 * 
		 * @param p
		 * @param duration
		 * @return
		 */
		public Editor addMotion( Point p, int duration) {
			getObject().addMotion(p, duration);
			return this;
		}
		
		/**
		 * 문자열의 움직입 정보를 초기화 한다. 
		 * @return
		 */
		public Editor clearMotion() {
			getObject().clearMotion();
			return this;
		}
		
		/**
		 * set data base resolution
		 * @param name
		 * @return
		 */
		public Editor setBaseResolution(Resolution resoltuion){
			getObject().setBaseResolution(resoltuion);
			return this;
		}
		
		public Editor setBackground(String path, ResourceType resourceType) {
			getObject().setBackground(path, resourceType);
			return this;
		}
	}
	
	/**
	 * 텍스쳐의 포인트, 투명도, 움직임 모양(직선, 곡선), duration값을 가지고 있는 클래스
	 */
	static final class TextMotion implements IJsonConvertible {
		/**
		 * 현재 모션의 위치값.
		 */
		public Point mMotionP = new Point();

		/**
		 * 현재 모션의 진행 시간값. ms기준.(a->b 이동값)
		 */
		public int mMotionDuration;

		public TextMotion(Point p, int duration) {

			this.mMotionP = p;
			this.mMotionDuration = duration;
		}

		public TextMotion(JsonObject jsonMotion) throws JSONException {
			this.mMotionP = new Point(jsonMotion.getInt(JSON_NAME_MOTION_MOVE_X), jsonMotion.getInt(JSON_NAME_MOTION_MOVE_Y));
			this.mMotionDuration = jsonMotion.getInt(JSON_NAME_MOTION_DURATION);
		}

		@Override
		public Object toJsonObject() throws JSONException {
			// TODO Auto-generated method stub
			JsonObject motionObject = new JsonObject();
			motionObject.put(JSON_NAME_MOTION_MOVE_X, mMotionP.x);
			motionObject.put(JSON_NAME_MOTION_MOVE_Y, mMotionP.y);
			motionObject.put(JSON_NAME_MOTION_DURATION, mMotionDuration);
			return motionObject;
		}
	}
}