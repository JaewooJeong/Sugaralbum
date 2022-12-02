
package com.kiwiple.imageframework.gpuimage.filter.custom;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageMaskBlendFilterL;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageOverlayBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageAdjustOpacityFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageHueFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageLuminanceThresholdFilter;
import com.kiwiple.imageframework.gpuimage.source.ImagePicture;

public class PaidFilter1 extends ImageFilterGroup {
    private ImageLuminanceThresholdFilter mImageLuminanceThresholdFilter = new ImageLuminanceThresholdFilter();
    private ImageHueFilter mImageHueFilter = new ImageHueFilter();
    private ImageOverlayBlendFilter imageOverlayBlendFilter = new ImageOverlayBlendFilter();
    private ImageMaskBlendFilterL imageMaskBlendFilterL = new ImageMaskBlendFilterL();
    private ImageAdjustOpacityFilter imageAdjustOpacityFilter = new ImageAdjustOpacityFilter();
    private ImagePicture secondImage = new ImagePicture();
    private ImagePicture textureImage2 = new ImagePicture();

    private ArtFilterInfo mArtFilterInfo;

    public void initWithImage(Context context, final Bitmap texture1, final Bitmap texture2,
            float hueValue) {
        super.init(context);
        imageOverlayBlendFilter.init(context);
        addFilter(imageOverlayBlendFilter);

        imageMaskBlendFilterL.init(context);
        addFilter(imageMaskBlendFilterL);

        mImageHueFilter.init(context);
        mImageHueFilter.setHue(hueValue);
        addFilter(mImageHueFilter);

        mImageLuminanceThresholdFilter.init(context);
        addFilter(mImageLuminanceThresholdFilter);

        imageAdjustOpacityFilter.init(context);
        addFilter(imageAdjustOpacityFilter);

        // texture1
        secondImage.initWithImage(context, texture1);
        addFilter(secondImage);
        mImageHueFilter.addTarget(imageMaskBlendFilterL, 1);
        secondImage.addTarget(mImageHueFilter);
        secondImage.processImage();

        // texture2
        textureImage2.initWithImage(context, texture2);
        addFilter(textureImage2);
        textureImage2.addTarget(imageOverlayBlendFilter, 1);
        textureImage2.processImage();

        // start filtering
        mImageLuminanceThresholdFilter.addTarget(imageMaskBlendFilterL);
        imageMaskBlendFilterL.addTarget(imageOverlayBlendFilter);
        imageOverlayBlendFilter.addTarget(imageAdjustOpacityFilter);

        initialFilters.clear();
        initialFilters.add(mImageLuminanceThresholdFilter);
        terminalFilter = imageAdjustOpacityFilter;
    }

    public void setThreshold(float newValue) {
        mImageLuminanceThresholdFilter.setThreshold(newValue);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(2);
            progressInfo.add(new ProgressInfo(0.55f, 0.25f, .37f, 100.f, "Threshold"));
            progressInfo.add(new ProgressInfo(360.f, 0.f, 61.f, 1.f, "Hue"));
            mArtFilterInfo = new ArtFilterInfo("Rainbow", progressInfo, "0900703802",
                                               "olleh_rainbow", "item_rainbow2");
        }
        return mArtFilterInfo;
    }
}
