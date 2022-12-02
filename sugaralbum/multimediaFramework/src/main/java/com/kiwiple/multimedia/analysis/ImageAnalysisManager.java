package com.kiwiple.multimedia.analysis;

import java.util.List;

import android.content.Context;

import com.kiwiple.imageanalysis.analysis.ImageAnalysis;
import com.kiwiple.imageanalysis.analysis.ImageAnalysis.ImageAutoAnalysisListener;
import com.kiwiple.imageanalysis.analysis.ImageAnalysisCondition;
import com.kiwiple.imageanalysis.database.ImageData;
import com.kiwiple.imageanalysis.search.ImageSearch;
import com.kiwiple.imageanalysis.search.ImageSearchCondition;
import com.kiwiple.imageanalysis.search.ImageSearchDetailCondition;

public class ImageAnalysisManager implements IImageAnalysisManager {

	// // // // // Static variable.
	// // // // //
	private static ImageAnalysisManager sInstance;

	private static final int IMAGE_SEARCH_COUNT = 0;

	// // // // // Static method.
	// // // // //
	public static synchronized IImageAnalysisManager getInstance() {

		if (sInstance == null) {
			sInstance = new ImageAnalysisManager();
		}
		return sInstance;
	}

	// // // // // Constructor.
	// // // // //
	public ImageAnalysisManager() {
		// Hide constructor and do nothing.
	}

	// // // // // Interface method.
	// // // // //
	@Override
	public void startAnalysisGallery(Context context, ImageAutoAnalysisListener imageAnalysisListener, int imageCount) {
		final ImageAnalysis imageAnalysis = ImageAnalysis.getInstance(context);
		ImageAnalysisCondition imageAnalysisCondition = getImageAnalysisCondition(context, imageCount);
		imageAnalysis.setImageAutoAnalysisListener(imageAnalysisListener);
		imageAnalysis.startAnalysisGallery(null, imageAnalysisCondition);
	}

	@Override
	public List<ImageData> getImageData(Context context, String ruleSetJsonString) {
		ImageSearchCondition imageSearchCondition = getImageSearchCondition(ruleSetJsonString);
		ImageSearch imageSearch = new ImageSearch(context, imageSearchCondition);
		return imageSearch.getImageDatasFromCondition();
	}

	@Override
	public void release() {
		sInstance = null;
	}

	private ImageAnalysisCondition getImageAnalysisCondition(Context context, int imageCount) {
		ImageAnalysisCondition imageAnalysisCondition = new ImageAnalysisCondition(context);
		imageAnalysisCondition.setStartDateAmount(0);
		imageAnalysisCondition.setEndDateAmount(0);
		imageAnalysisCondition.setAnalysisCount(imageCount);
		imageAnalysisCondition.setAnalysisLocationCondition(true);
		imageAnalysisCondition.setAnalysisFaceCondition(true);
		imageAnalysisCondition.setIsAutoFaceRecognition(true, 10);
		imageAnalysisCondition.setAnalysisQualityCondition(true);

		return imageAnalysisCondition;
	}

	private ImageSearchCondition getImageSearchCondition(String jsonString) {
		ImageSearchCondition condition = new ImageSearchCondition(IMAGE_SEARCH_COUNT, 0);
		ImageSearchDetailCondition detailCondition = new ImageSearchDetailCondition();
		detailCondition.setJsonStringCondition(jsonString);
		condition.setImageSearchDetailCondition(detailCondition);

		return condition;
	}
}
