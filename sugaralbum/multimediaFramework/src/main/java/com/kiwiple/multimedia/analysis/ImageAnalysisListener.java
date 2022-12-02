package com.kiwiple.multimedia.analysis;

public interface ImageAnalysisListener extends com.kiwiple.imageanalysis.analysis.ImageAnalysis.ImageAnalysisListener {

	@Override
	public abstract void onImageAnalysisFinish(int finishCount, int totalCount);

	@Override
	public abstract void onImageAnalysisTotalFinish(boolean isSuccess);
}
