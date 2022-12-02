package com.kiwiple.scheduler.data.uplus.effect;

import com.kiwiple.multimedia.canvas.AnimationEffect;
import com.kiwiple.scheduler.data.EffectData;

/**
 * Animation effect 데이터 클래스.
 *
 */
public class UplusAnimationEffectData extends EffectData {
	
	private String filePath = null;
	private int fileId = -1;
	private int coordinateX;
	private int coordinateY;
	private int motionType;
	private Object motionValue;
	
	
	/**
	 * 생성자.
	 * @param effectType Animation effect 타입. 
	 */
	public UplusAnimationEffectData(String filapath, int cx, int cy, int mt, Object value) {
		super(AnimationEffect.JSON_VALUE_TYPE);
		this.filePath = filapath; 
		this.coordinateX = cx;
		this.coordinateY = cy;
		this.motionType = mt;
		this.motionValue = value;
		
	}
	
	public UplusAnimationEffectData(int fileid, int cx, int cy, int mt, int mv) {
		super(AnimationEffect.JSON_VALUE_TYPE);
		this.fileId = fileid; 
		this.coordinateX = cx;
		this.coordinateY = cy;
		this.motionType = mt;
		this.motionValue = mv;
	}

	/**
	 * Animation effect에 적용될 image의 path값을 반환한다.
	 * @return Animation effect imag path.
	 */
	public String getFilePath() {
		return this.filePath;
	}
	
	/**
	 * Animation effect에 적용될 image의 id값을 반환한다.
	 * @return Animation effect imag id.
	 */
	public int getFileIdh() {
		return this.fileId;
	}
	
	public int getCoordinateX(){
		return this.coordinateX;
	}
	
	public int getCoordianteY(){
		return this.coordinateY;
	}
	
	public int getMotionType(){
		return this.motionType;
	}
	
	public Object getMotionValue(){
		return this.motionValue;
	}

}
