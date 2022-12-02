package com.kiwiple.scheduler.analysis;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.scheduler.data.AnalyzedInputData;
import com.kiwiple.scheduler.data.InputData;
import com.kiwiple.scheduler.data.OutputData;

/**
 * 입력 이미지 분석을 위한 객체. 
 *
 */
public abstract class ImageAnalysis {
	public static final int ORIENTATION_DEGREE_0 = 0;
	public static final int ORIENTATION_DEGREE_90 = 90;
	public static final int ORIENTATION_DEGREE_180 = 180;
	public static final int ORIENTATION_DEGREE_270 = 270;

	protected Context mContext;
	
	/**
	 * 입력 사진을 시간순으로 배치한 리스트. 
	 */
	protected List<AnalyzedInputData> mInputDataListBasedOnDate;
	/**
	 * 입력 사진의 가로 사진 리스트.
	 */
	protected List<AnalyzedInputData> mInputDataTypeBaseOnWidth;
	/**
	 * 입력 사진의 세로 사진 리스트.
	 */
	protected List<AnalyzedInputData> mInputDataTypeBaseOnHeight;
	
	/**
	 * 인물 사진 리스트. 
	 */
	protected List<AnalyzedInputData> mInputDataTypeBasedOnPerson;
	protected List<AnalyzedInputData> mInputDataTypeBasedOnPersonWidth;
	protected List<AnalyzedInputData> mInputDataTypeBasedAccelerationZoom;
	protected List<AnalyzedInputData> mInputDataTypeBasedOnAccelerationZoomProtagonist;
	protected List<AnalyzedInputData> mInputDataTypeBasedOnPersonHeight;
	protected List<AnalyzedInputData> mInputDataTypeBasedOnMultiFilter; 
	protected List<AnalyzedInputData> mInputDataTypeBasedOnMultiFilterProtagonist; 
	protected List<AnalyzedInputData> mInputDataTypeBasedOnLandscape;
	protected List<AnalyzedInputData> mInputDataTypeBasedOnLandscapeWidth;
	protected List<AnalyzedInputData> mInputDataTypeBasedOnLandscapeHeight;

	/**
	 * 생성자 
	 * @param context Context
	 */
	public ImageAnalysis(Context context) {
		mContext = context;
		mInputDataTypeBaseOnWidth = new ArrayList<AnalyzedInputData>(); 
		mInputDataTypeBaseOnHeight = new ArrayList<AnalyzedInputData>(); 
		mInputDataListBasedOnDate = new ArrayList<AnalyzedInputData>();
		mInputDataTypeBasedOnPerson = new ArrayList<AnalyzedInputData>();
		mInputDataTypeBasedAccelerationZoom = new ArrayList<AnalyzedInputData>();
		mInputDataTypeBasedOnAccelerationZoomProtagonist = new ArrayList<AnalyzedInputData>();
		mInputDataTypeBasedOnPersonWidth = new ArrayList<AnalyzedInputData>();
		mInputDataTypeBasedOnPersonHeight = new ArrayList<AnalyzedInputData>();
		mInputDataTypeBasedOnMultiFilter = new ArrayList<AnalyzedInputData>(); 
		mInputDataTypeBasedOnMultiFilterProtagonist = new ArrayList<AnalyzedInputData>(); 
		mInputDataTypeBasedOnLandscape = new ArrayList<AnalyzedInputData>();
		mInputDataTypeBasedOnLandscapeWidth = new ArrayList<AnalyzedInputData>();
		mInputDataTypeBasedOnLandscapeHeight = new ArrayList<AnalyzedInputData>();
	}

	/**
	 * 사진을 시간, 퀄리티, 가로, 세로, 인물, 풍경 등으로 재 분류 후 각각의 리스트를 생성.<br>
	 */
	public abstract void startInputDataAnalysis(InputData inputData);

	/**
	 * 모든 테마에서 single scene을 생성한다.<br>
	 */
	public abstract void selectImageFileScene();

	/**
	 * 필터, 멀티 테마의 경우 멀티씬을 생성한다. <br>
	 */
	public abstract void selectCollageScene();
	
	/**
	 * frame theme의 경우 collage를 생성한다.<br>
	 */
	public abstract void selectMultiLayerScene();
	
	public abstract void selectBurstShotScene();
}
