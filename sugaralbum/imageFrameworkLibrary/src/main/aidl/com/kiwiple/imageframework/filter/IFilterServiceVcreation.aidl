package com.kiwiple.imageframework.filter;

import android.graphics.Bitmap;
import com.kiwiple.imageframework.filter.Filter;
import com.kiwiple.imageframework.filter.CurvesPoint;
import com.kiwiple.imageframework.filter.ArtFilter;

interface IFilterServiceVcreation {
	String processingImageFile(String filename, int size, in Filter filter, String stickerImageFilePath);
	Bitmap processingImageBitmap(in Bitmap image, in Filter filter, String stickerImageFilePath);
	void stopProcessing(boolean canceled);
}