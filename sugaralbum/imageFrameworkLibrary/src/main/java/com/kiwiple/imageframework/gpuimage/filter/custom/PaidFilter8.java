
package com.kiwiple.imageframework.gpuimage.filter.custom;

import java.util.ArrayList;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageOverlayBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageSaturationFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageKuwaharaFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageSketchFilter;

public class PaidFilter8 extends ImageFilterGroup {
    private ImageSaturationFilter mImageSaturationFilter = new ImageSaturationFilter();
    private ImageOverlayBlendFilter imageOverlayBlendFilter = new ImageOverlayBlendFilter();
    private ImageSketchFilter imageSketchFilter = new ImageSketchFilter();
    private ImageKuwaharaFilter imageKuwaharaFilter = new ImageKuwaharaFilter();

    private ArtFilterInfo mArtFilterInfo;

    public void initWithImage(Context context, int width, int height) {
        super.init(context);
        imageOverlayBlendFilter.init(context);
        addFilter(imageOverlayBlendFilter);

        imageSketchFilter.init(context);
        if(!ArtFilterUtils.sIsLiteMode) {
            imageSketchFilter.setTexelWidth(0.001f);
            imageSketchFilter.setTexelHeight(0.001f);
        } else {
            imageSketchFilter.setTexelWidth(1f / (float)width);
            imageSketchFilter.setTexelHeight(1f / (float)height);
        }
        addFilter(imageSketchFilter);

        imageKuwaharaFilter.setRadius(4);
        imageKuwaharaFilter.init(context);
        addFilter(imageKuwaharaFilter);

        mImageSaturationFilter.init(context);
        addFilter(mImageSaturationFilter);

        // start filtering
        imageSketchFilter.addTarget(imageOverlayBlendFilter, 1);
        if(!ArtFilterUtils.sIsLiteMode) {
            imageKuwaharaFilter.addTarget(imageOverlayBlendFilter);
        }
        imageOverlayBlendFilter.addTarget(mImageSaturationFilter);

        initialFilters.clear();
        if(!ArtFilterUtils.sIsLiteMode) {
            initialFilters.add(imageKuwaharaFilter);
        } else {
            initialFilters.add(imageOverlayBlendFilter);
        }
        initialFilters.add(imageSketchFilter);
        terminalFilter = mImageSaturationFilter;
    }

    public void setSaturation(float newValue) {
        mImageSaturationFilter.setSaturation(newValue);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(2.f, 0.f, 0.8f, 50.f, "Saturation"));
            mArtFilterInfo = new ArtFilterInfo("Bright toon", progressInfo, "0900703828",
                                               "olleh_bright_toon", "item_bright_toon2");
        }
        return mArtFilterInfo;
    }
}
