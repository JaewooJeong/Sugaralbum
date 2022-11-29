package com.kiwiple.scheduler.analysis;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.kiwiple.scheduler.data.AnalyzedInputData;
import com.kiwiple.scheduler.data.InputData;
import com.kiwiple.scheduler.data.OutputData;

public abstract class VideoAnalysis {

	protected Context mContext;
	protected int mMaxVideoCount;
	protected boolean mRequestAnalysis;
	protected List<AnalyzedInputData> mInpuDataList;

	public VideoAnalysis(Context context) {
		mContext = context;
		mInpuDataList = new ArrayList<AnalyzedInputData>();
	}

	/**
	 * 최대 비디오 갯수를 반환한다. 
	 * @return : 최대 비디오 갯수. 
	 */
	public int getMaxVideoCount() {
		return mMaxVideoCount;
	}

	/**
	 * 최대 비디오 갯수를 설정한다. 
	 * @param maxVideoCount : 최대 비디오 갯수. 
	 */
	public void setMaxVideoCount(int maxVideoCount) {
		this.mMaxVideoCount = maxVideoCount;
	}

	/**
	 * 비디오 데이터들의 분석 요청 여부 확인. 
	 * @return 비디오 데이터들의 분석 여부. 
	 */
	public boolean isRequestAnalysis() {
		return mRequestAnalysis;
	}

	/**
	 * 비디오 데이터들의 분석 요청 여부 설정. 
	 * @param requestAnalysis : 비디오 데이터들의 분석 설정. 
	 */
	public void setRequestAnalysis(boolean requestAnalysis) {
		this.mRequestAnalysis = requestAnalysis;
	}

	/**
	 * input, output data를 설정 한다. <br>
	 * 비디오 데이터가 분석 요청이 설정되어 있으면, output data의 사진 데이터의 날짜를 확인하고<br>
	 * 비디오 데이터를 찾아 낸다. <br>
	 * @param inputData : 자동 수동으로 선택된 데이터.  
	 * @param outputData : 스케줄러에 의해서 선택된 데이터. 
	 */
	public abstract void startInputDataAnalysis(InputData inputData, OutputData outputData);

	/**
	 * 비디오 데이터를 선택하고, 날짜순으로 output data list에 삽입. 
	 */
	public abstract void selectVideoFileScene();
}
