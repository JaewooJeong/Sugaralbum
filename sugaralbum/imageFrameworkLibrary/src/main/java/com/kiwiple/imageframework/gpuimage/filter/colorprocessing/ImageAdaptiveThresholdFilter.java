
package com.kiwiple.imageframework.gpuimage.filter.colorprocessing;

import java.util.ArrayList;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoInputFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageBoxBlurFilter;

public class ImageAdaptiveThresholdFilter extends ImageFilterGroup {
    private ImageBoxBlurFilter mBoxBlurFilter = new ImageBoxBlurFilter();
    private ImageGrayscaleFilter luminanceFilter = new ImageGrayscaleFilter();
    private ImageTwoInputFilter adaptiveThresholdFilter = new ImageTwoInputFilter();

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.init(context);
        // First pass: reduce to luminance
        luminanceFilter.init(context);
        addFilter(luminanceFilter);

        // Second pass: perform a box blur
        mBoxBlurFilter.init(context);
        addFilter(mBoxBlurFilter);

        // Third pass: compare the blurred background luminance to the local value
        adaptiveThresholdFilter.initWithFragmentShaderFromResource(context,
                                                                   "adaptive_threshold_fragment");
        addFilter(adaptiveThresholdFilter);

        luminanceFilter.addTarget(mBoxBlurFilter);

        mBoxBlurFilter.addTarget(adaptiveThresholdFilter);
        // To prevent double updating of this filter, disable updates from the sharp luminance image
        // side
        luminanceFilter.addTarget(adaptiveThresholdFilter);

        initialFilters.clear();
        initialFilters.add(luminanceFilter);
        terminalFilter = adaptiveThresholdFilter;
    }

    public void setBlurSize(float newValue) {
        mBoxBlurFilter.setBlurSize(newValue);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(7.f, 1.f, 4.f, 1.f, ImageFilter.BLURSIZE, true));
            mArtFilterInfo = new ArtFilterInfo("A/F1", progressInfo);
        }
        return mArtFilterInfo;
    }
}
