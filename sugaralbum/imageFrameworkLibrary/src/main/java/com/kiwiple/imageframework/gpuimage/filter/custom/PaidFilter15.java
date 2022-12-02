
package com.kiwiple.imageframework.gpuimage.filter.custom;

import java.util.ArrayList;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageAlphaBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageSoftLightBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageOpacityFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageGaussianBlurFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageSmartBlurFilter;

public class PaidFilter15 extends ImageFilterGroup {
    private ImageGaussianBlurFilter mImageGaussianBlurFilter1 = new ImageGaussianBlurFilter();
    private ImageSmartBlurFilter mImageSmartBlurFilter = new ImageSmartBlurFilter();
    private ImageAlphaBlendFilter mImageAlphaBlendFilter = new ImageAlphaBlendFilter();
    private ImageGaussianBlurFilter mImageGaussianBlurFilter2 = new ImageGaussianBlurFilter();
    private ImageSoftLightBlendFilter mImageSoftLightBlendFilter = new ImageSoftLightBlendFilter();
    private ImageOpacityFilter mImageOpacityFilter = new ImageOpacityFilter();

    private ArtFilterInfo mArtFilterInfo;

    public void initWithImage(Context context, float opacity) {
        super.init(context);
        mImageGaussianBlurFilter1.init(context);
        addFilter(mImageGaussianBlurFilter1);

        mImageSmartBlurFilter.init(context);
        addFilter(mImageSmartBlurFilter);

        mImageAlphaBlendFilter.init(context);
        addFilter(mImageAlphaBlendFilter);

        mImageGaussianBlurFilter2.init(context);
        addFilter(mImageGaussianBlurFilter2);

        mImageSoftLightBlendFilter.init(context);
        addFilter(mImageSoftLightBlendFilter);

        mImageOpacityFilter.init(context);
        mImageOpacityFilter.setOpacity(opacity);
        addFilter(mImageOpacityFilter);

        mImageGaussianBlurFilter1.addTarget(mImageSmartBlurFilter);
        mImageSmartBlurFilter.addTarget(mImageAlphaBlendFilter, 1);
        mImageAlphaBlendFilter.addTarget(mImageSoftLightBlendFilter);

        mImageGaussianBlurFilter2.addTarget(mImageOpacityFilter);
        mImageOpacityFilter.addTarget(mImageSoftLightBlendFilter, 1);

        initialFilters.clear();
        initialFilters.add(mImageGaussianBlurFilter1);
        initialFilters.add(mImageAlphaBlendFilter);
        initialFilters.add(mImageGaussianBlurFilter2);
        terminalFilter = mImageSoftLightBlendFilter;
    }

    public void setBlur1(float newValue) {
        mImageGaussianBlurFilter1.setBlurSize(newValue);
    }

    public void setBlur2(float newValue) {
        mImageSmartBlurFilter.setBlurSize(newValue);
        mImageGaussianBlurFilter2.setBlurSize(newValue);
    }

    public void setThreshold(float newValue) {
        mImageSmartBlurFilter.setThreshold(newValue);
    }

    public void setOpacity(float newValue) {
        mImageAlphaBlendFilter.setMix(newValue);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(5);
            progressInfo.add(new ProgressInfo(6.f, 0.f, .2f, 10.f, ImageFilter.BLURSIZE + "1", true));
            progressInfo.add(new ProgressInfo(6.f, 0.f, .4f, 10.f, ImageFilter.BLURSIZE + "2", true));
            progressInfo.add(new ProgressInfo(1.f, 0.f, 0.1f, 100.f, ImageFilter.THRESHOLD, true));
            progressInfo.add(new ProgressInfo(1.f, 0.f, 0.6f, 100.f, "Opacity"));
            progressInfo.add(new ProgressInfo(1.f, 0.f, 0.3f, 100.f, "Blend"));
            mArtFilterInfo = new ArtFilterInfo("Custom15", progressInfo, null, "olleh_Custom15",
                                               "item_Custom15");
        }
        return mArtFilterInfo;
    }
}
