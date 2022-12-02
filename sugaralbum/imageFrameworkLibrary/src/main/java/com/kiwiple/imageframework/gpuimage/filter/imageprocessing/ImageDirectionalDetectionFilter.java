
package com.kiwiple.imageframework.gpuimage.filter.imageprocessing;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.Image3x3TextureSamplingFilter;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

import java.util.ArrayList;

public class ImageDirectionalDetectionFilter extends Image3x3TextureSamplingFilter {
    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context,
                                                 "directional_sobel_edge_detection_fragment");
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(4.f, .0f, 1.f, 50.f, ImageFilter.LINEWIDTH, true));
            mArtFilterInfo = new ArtFilterInfo("Directional sobel edge detection", progressInfo);
        }
        return mArtFilterInfo;
    }
}
