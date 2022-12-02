
package com.kiwiple.imageframework.gpuimage.filter.custom;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageAlphaBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageAlphaMaskBlendFilterL;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageLuminanceThresholdBlendFilterL;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageSketchFilter;
import com.kiwiple.imageframework.gpuimage.source.ImagePicture;

public class PaidFilter5 extends ImageFilterGroup {
    private ImageSketchFilter mImageSketchFilter = new ImageSketchFilter();
    private ImageAlphaBlendFilter imageAlphaBlendFilter = new ImageAlphaBlendFilter();
    private ImageLuminanceThresholdBlendFilterL imageLuminanceThresholdBlendFilterL = new ImageLuminanceThresholdBlendFilterL();
    private ImageAlphaMaskBlendFilterL imageAlphaMaskBlendFilterL = new ImageAlphaMaskBlendFilterL();
    private ImagePicture textureImage = new ImagePicture();

    private ArtFilterInfo mArtFilterInfo1;
    private ArtFilterInfo mArtFilterInfo2;

    public void initWithImage(Context context, Bitmap texture, float threshold) {
        super.init(context);
        mImageSketchFilter.init(context);
        addFilter(mImageSketchFilter);

        imageAlphaBlendFilter.init(context);
        imageAlphaBlendFilter.setMix(0.2f);
        addFilter(imageAlphaBlendFilter);

        imageLuminanceThresholdBlendFilterL.init(context);
        imageLuminanceThresholdBlendFilterL.setThreshold(threshold);
        addFilter(imageLuminanceThresholdBlendFilterL);

        imageAlphaMaskBlendFilterL.init(context);
        imageAlphaMaskBlendFilterL.setMix(0.5f);
        addFilter(imageAlphaMaskBlendFilterL);

        // texture
        textureImage.initWithImage(context, texture);
        addFilter(textureImage);
        textureImage.addTarget(imageAlphaBlendFilter, 1);
        textureImage.addTarget(imageAlphaMaskBlendFilterL, 1);
        textureImage.processImage();

        // start filtering
        mImageSketchFilter.addTarget(imageAlphaBlendFilter);
        imageAlphaBlendFilter.addTarget(imageLuminanceThresholdBlendFilterL, 1);
        imageLuminanceThresholdBlendFilterL.addTarget(imageAlphaMaskBlendFilterL);

        initialFilters.clear();
        initialFilters.add(mImageSketchFilter);
        initialFilters.add(imageLuminanceThresholdBlendFilterL);
        terminalFilter = imageAlphaMaskBlendFilterL;
    }

    public void setWeight(float newValue, int width, int height) {
        if(!ArtFilterUtils.sIsLiteMode) {
            mImageSketchFilter.setWeight(newValue);
        } else {
            mImageSketchFilter.setTexelWidth(1f / (float)width);
            mImageSketchFilter.setTexelHeight(1f / (float)height);
        }
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo1 == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(2);
            progressInfo.add(new ProgressInfo(2.2f, .2f, .43f, 50.f, ImageFilter.LINEWIDTH, true));
            progressInfo.add(new ProgressInfo(0.5f, 0.2f, 0.33f, 100.f, "Threshold"));
            mArtFilterInfo1 = new ArtFilterInfo("Post gray", progressInfo, "0900703805",
                                                "olleh_post_gray", "item_post_gray");
        }
        return mArtFilterInfo1;
    }

    @Override
    public ArtFilterInfo getSecondFilterInfo() {
        if(mArtFilterInfo2 == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(2);
            progressInfo.add(new ProgressInfo(2.2f, .2f, .74f, 50.f, ImageFilter.LINEWIDTH, true));
            progressInfo.add(new ProgressInfo(0.5f, 0.2f, 0.355f, 100.f, "Threshold"));
            mArtFilterInfo2 = new ArtFilterInfo("Post gray", progressInfo, "0900703805",
                                                "olleh_post_gray", "item_post_gray");
        }
        return mArtFilterInfo2;
    }
}
