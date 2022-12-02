
package com.kiwiple.imageframework.gpuimage.filter.custom;

import java.util.ArrayList;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageColorBurnBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageSaturationFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageSketchFilter;

public class PaidFilter7 extends ImageFilterGroup {
    private ImageSketchFilter mImageSketchFilter = new ImageSketchFilter();
    private ImageSaturationFilter mImageSaturationFilter = new ImageSaturationFilter();
    private ImageColorBurnBlendFilter imageColorBurnBlendFilter = new ImageColorBurnBlendFilter();

    private ArtFilterInfo mArtFilterInfo;

    public void initWithImage(Context context, int width, int height) {
        super.init(context);
        imageColorBurnBlendFilter.init(context);
        addFilter(imageColorBurnBlendFilter);

        mImageSketchFilter.init(context);
        if(!ArtFilterUtils.sIsLiteMode) {
            mImageSketchFilter.setTexelWidth(0.001f);
            mImageSketchFilter.setTexelHeight(0.001f);
        } else {
            mImageSketchFilter.setTexelWidth(1f / (float)width);
            mImageSketchFilter.setTexelHeight(1f / (float)height);
        }
        addFilter(mImageSketchFilter);

        mImageSaturationFilter.init(context);
        addFilter(mImageSaturationFilter);

        // start filtering
        mImageSketchFilter.addTarget(imageColorBurnBlendFilter, 1);
        imageColorBurnBlendFilter.addTarget(mImageSaturationFilter);

        initialFilters.clear();
        initialFilters.add(mImageSketchFilter);
        initialFilters.add(imageColorBurnBlendFilter);
        terminalFilter = mImageSaturationFilter;
    }

    public void setWeight(float newValue) {
        mImageSketchFilter.setWeight(newValue);
    }

    public void setSaturation(float newValue) {
        mImageSaturationFilter.setSaturation(newValue);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(2);
            progressInfo.add(new ProgressInfo(2.2f, .2f, 1.5f, 50.f, ImageFilter.LINEWIDTH, true));
            progressInfo.add(new ProgressInfo(2.f, 0.f, 0.7f, 50.f, "Saturation"));
            mArtFilterInfo = new ArtFilterInfo("Soft toon", progressInfo, "0900703827",
                                               "olleh_soft_toon", "item_soft_toon");
        }
        return mArtFilterInfo;
    }
}
