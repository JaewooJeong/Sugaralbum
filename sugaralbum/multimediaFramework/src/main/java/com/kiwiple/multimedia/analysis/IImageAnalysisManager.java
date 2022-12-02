package com.kiwiple.multimedia.analysis;

import java.util.List;

import android.content.Context;

import com.kiwiple.imageanalysis.analysis.ImageAnalysis.ImageAutoAnalysisListener;
import com.kiwiple.imageanalysis.database.ImageData;

public interface IImageAnalysisManager {

	public abstract void startAnalysisGallery(Context context, ImageAutoAnalysisListener imageAnalysisListener, int imageCount);

	public abstract List<ImageData> getImageData(Context context, String ruleSetJsonString);

	public abstract void release();
}
