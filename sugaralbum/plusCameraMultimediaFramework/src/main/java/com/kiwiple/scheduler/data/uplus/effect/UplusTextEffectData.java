package com.kiwiple.scheduler.data.uplus.effect;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Point;

import com.kiwiple.multimedia.canvas.ImageResource;
import com.kiwiple.multimedia.canvas.Resolution;
import com.kiwiple.multimedia.canvas.TextEffect;
import com.kiwiple.scheduler.data.EffectData;

/**
 * DynamicTextureEffect 데이터 클래스.
 *
 */
public class UplusTextEffectData extends EffectData {
	
	public static final String TAG_JSON_KEY_TYPE = "type"; 
	public static final String TAG_JSON_VALUE_TYPE_NORMAL = "normal";
	public static final String TAG_JSON_VALUE_TYPE_TITLE = "title";
	public static final String TAG_JSON_VALUE_TYPE_UNKNOWN = "unknown";
	
	private String mText;
	private String mAlign;
	private int mSize;
	private int mColor;
	private String mTypefacePath;
	private Point mCL = new Point();
	private Point mCR = new Point();
	ArrayList<TextMotions> motions = new ArrayList<TextMotions>();
	private Resolution mBaseResoltuion ;
	private String mBackgroundPath;
	
	/**
	 * 생성자.
	 * @param effectType DynamicTextureEffect 타입. 
	 * @throws JSONException 
	 */
	public UplusTextEffectData(String effectType, JSONObject textObject) throws JSONException {
		super(TextEffect.JSON_VALUE_TYPE);
		mTypefacePath = textObject.getString(TextEffect.JSON_NAME_TYPEFACE_PATH);
		mColor = (textObject.getInt(TextEffect.JSON_NAME_COLOR));
		mSize = (textObject.getInt(TextEffect.JSON_NAME_SIZE));
		mAlign = (textObject.getString(TextEffect.JSON_NAME_ALIGN));
		setResoureCoordinate(textObject.getInt(TextEffect.JSON_NAME_COORDINATE_LEFT_X),textObject.getInt(TextEffect.JSON_NAME_COORDINATE_LEFT_Y),textObject.getInt(TextEffect.JSON_NAME_COORDINATE_RIGHT_X),textObject.getInt(TextEffect.JSON_NAME_COORDINATE_RIGHT_Y));
		mText = (textObject.getString(TextEffect.JSON_NAME_TEXT));
		if(!textObject.isNull(Resolution.DEFAULT_JSON_NAME)){
			mBaseResoltuion =  findResoltuion(textObject.getString(Resolution.DEFAULT_JSON_NAME));
		}
		if(!textObject.isNull(TextEffect.JSON_NAME_MOTION)){
			parseMotions(textObject.getJSONArray(TextEffect.JSON_NAME_MOTION));
		}
		if(!textObject.isNull(ImageResource.DEFAULT_JSON_NAME)){
			mBackgroundPath = textObject.getString(ImageResource.DEFAULT_JSON_NAME);
		}
		
		if(!textObject.isNull(EffectData.JSON_NAME_ACTIVE_START_RATIO)){
			activeStartRatio = (float)textObject.getDouble(EffectData.JSON_NAME_ACTIVE_START_RATIO); 
		}else{
			activeStartRatio = 0.0f; 
		}
		
		if(!textObject.isNull(EffectData.JSON_NAME_ACTIVE_END_RATIO)){
			activeEndRatio = (float)textObject.getDouble(EffectData.JSON_NAME_ACTIVE_END_RATIO); 
		}else{
			activeEndRatio = 1.0f; 
		}
		
		if(!textObject.isNull(EffectData.JSON_NAME_DRAW_ONLY_ACTIVE_RATIO)){
			drawOnlyActvieRatio = textObject.getBoolean(EffectData.JSON_NAME_DRAW_ONLY_ACTIVE_RATIO); 
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
	
	private void parseMotions(JSONArray jsonArray){
		int size = jsonArray.length();
		motions.clear();
		for (int i = 0; i < size; i++) {
			JSONObject jsonMotion;
			try {
				jsonMotion = jsonArray.getJSONObject(i);
				Point p = new Point(jsonMotion.getInt(TextEffect.JSON_NAME_MOTION_MOVE_X), jsonMotion.getInt(TextEffect.JSON_NAME_MOTION_MOVE_Y));
				int duration = jsonMotion.getInt(TextEffect.JSON_NAME_MOTION_DURATION);

				motions.add(new TextMotions(p,duration));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}
	
	private void setResoureCoordinate(int lx, int ly, int rx, int ry){
		mCL.x = lx;
		mCL.y = ly;
		mCR.x = rx;
		mCR.y = ry;
	}
	
	public String getAlign(){
		return this.mAlign;
	}
	
	public Point getLeftCoordinate(){
		return this.mCL;
	}
	
	public Point getRightCoordinate(){
		return this.mCR;
	}
	
	public void setText(String text){
		this.mText = text; 
	}
	
	public String getText(){
		return this.mText;
	}
	
	public String getTypeFacePath(){
		return this.mTypefacePath;
	}
	
	public int getColor(){
		return this.mColor;
	}
	
	public int getSize(){
		return this.mSize;
	}
	
	public Resolution getBaseResolution(){
		return this.mBaseResoltuion;
	}
	public ArrayList<TextMotions> getMotions(){
		return motions;
	}
	
	public String getBackgourndPath(){
		return this.mBackgroundPath;
	}
	
	public class TextMotions{
		public Point mMotionP = new Point();
		public int mMotionDuration;
		
		TextMotions(Point p,int duration){
			this.mMotionP = p;
			this.mMotionDuration = duration;
		}
	}
}
