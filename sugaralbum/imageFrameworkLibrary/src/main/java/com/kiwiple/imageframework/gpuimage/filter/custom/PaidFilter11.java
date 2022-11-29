
package com.kiwiple.imageframework.gpuimage.filter.custom;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageOverlayBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageKuwaharaFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageRGBOpeningFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageUnsharpMaskFilter;
import com.kiwiple.imageframework.gpuimage.source.ImagePicture;

public class PaidFilter11 extends ImageFilterGroup {
    private ImageKuwaharaFilter mKuwaharaFilter = new ImageKuwaharaFilter();
    private ImageOverlayBlendFilter imageOverlayBlendFilter = new ImageOverlayBlendFilter();
    private ImageRGBOpeningFilter mImageRGBOpeningFilter = new ImageRGBOpeningFilter();
    private ImageUnsharpMaskFilter imageUnsharpMaskFilter = new ImageUnsharpMaskFilter();
    private ImagePicture textureImage = new ImagePicture();

    private ArtFilterInfo mArtFilterInfo;

    public void initWithImage(Context context, final float blur, final Bitmap texture1) {
        super.init(context);
        imageOverlayBlendFilter.init(context);
        addFilter(imageOverlayBlendFilter);

        if(!ArtFilterUtils.sIsLiteMode) {
            mImageRGBOpeningFilter.initWithRadius(context, 2);
        } else {
            mImageRGBOpeningFilter.initWithRadius(context, 1);
        }
        addFilter(mImageRGBOpeningFilter);

        imageUnsharpMaskFilter.init(context);
        imageUnsharpMaskFilter.setBlur(blur);
        if(!ArtFilterUtils.sIsLiteMode) {
            imageUnsharpMaskFilter.setIntensity(7.f);
        } else {
            imageUnsharpMaskFilter.setIntensity(3.f);
        }
        addFilter(imageUnsharpMaskFilter);

        mKuwaharaFilter.init(context);
        mKuwaharaFilter.setRadius(6);
        addFilter(mKuwaharaFilter);

        // texture
        textureImage.initWithImage(context, texture1);
        addFilter(textureImage);
        textureImage.addTarget(imageOverlayBlendFilter, 1);
        textureImage.processImage();

        // start filtering
        if(!ArtFilterUtils.sIsLiteMode) {
            mImageRGBOpeningFilter.addTarget(imageUnsharpMaskFilter);
            imageUnsharpMaskFilter.addTarget(mKuwaharaFilter);
            mKuwaharaFilter.addTarget(imageOverlayBlendFilter);
        } else {
            mImageRGBOpeningFilter.addTarget(imageOverlayBlendFilter);
        }

        initialFilters.clear();
        initialFilters.add(mImageRGBOpeningFilter);
        terminalFilter = imageOverlayBlendFilter;
    }

    public void setRadius(int newValue) {
        mKuwaharaFilter.setRadius(newValue);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(8.f, 3.f, 5.f, 1.f, "Radius"));
            mArtFilterInfo = new ArtFilterInfo("Oil painting", progressInfo, "0900703831",
                                               "olleh_oil_painting", "item_oil_painting");
        }
        return mArtFilterInfo;
    }
}
