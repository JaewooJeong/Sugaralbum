
package com.kiwiple.imageframework.gpuimage.filter.effect;

import java.util.ArrayList;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageThresholdEdgeDetectionFilter;

public class ImageThresholdSketchFilter extends ImageThresholdEdgeDetectionFilter {
    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "threshold_sketch_fragment");
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>();
            progressInfo.add(new ProgressInfo(1.f, .3f, .5f, 100.f, "Threshold"));
            progressInfo.add(new ProgressInfo(3.4f, .6f, 2.9f, 50.f, ImageFilter.LINEWIDTH, true));
            mArtFilterInfo = new ArtFilterInfo("Threshold sketch", progressInfo);
        }
        return mArtFilterInfo;
    }
}
