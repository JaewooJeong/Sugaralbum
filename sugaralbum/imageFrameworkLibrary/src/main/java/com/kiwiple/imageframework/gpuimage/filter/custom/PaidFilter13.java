
package com.kiwiple.imageframework.gpuimage.filter.custom;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageDarkenBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageOverlayBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageSoftLightBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageOpacityFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageRGBDilationFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageRGBOpeningFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageUnsharpMaskFilter;
import com.kiwiple.imageframework.gpuimage.source.ImagePicture;

public class PaidFilter13 extends ImageFilterGroup {
    private ImageRGBDilationFilter imageRGBDilationFilter = new ImageRGBDilationFilter();
    private ImageUnsharpMaskFilter imageUnsharpMaskFilter = new ImageUnsharpMaskFilter();
    private ImageOverlayBlendFilter imageOverlayBlendFilter1 = new ImageOverlayBlendFilter();
    private ImageRGBOpeningFilter imageRGBOpeningFilter = new ImageRGBOpeningFilter();
    private ImageOpacityFilter imageOpacityFilter = new ImageOpacityFilter();
    private ImageDarkenBlendFilter imageScreenBlendFilter = new ImageDarkenBlendFilter();
    private ImageOverlayBlendFilter imageOverlayBlendFilter2 = new ImageOverlayBlendFilter();
    private ImageOpacityFilter imageOpacityFilter2 = new ImageOpacityFilter();
    private ImageSoftLightBlendFilter imageSoftLightBlendFilter = new ImageSoftLightBlendFilter();
    private ImagePicture textureImage1 = new ImagePicture();
    private ImagePicture textureImage2 = new ImagePicture();
    private ImagePicture textureImage3 = new ImagePicture();

    private ArtFilterInfo mArtFilterInfo;

    public void initWithImage(Context context, int width, int height, final Bitmap texture1,
            final Bitmap texture2, final Bitmap texture3, float opacity) {
        super.init(context);
        imageRGBDilationFilter.initWithRadius(context, 3);
        addFilter(imageRGBDilationFilter);

        imageUnsharpMaskFilter.init(context);
        imageUnsharpMaskFilter.setBlur(ArtFilterUtils.getWeightedParam(width, height, 1.f));
        imageUnsharpMaskFilter.setIntensity(7.f);
        addFilter(imageUnsharpMaskFilter);

        imageOverlayBlendFilter1.init(context);
        addFilter(imageOverlayBlendFilter1);

        imageRGBOpeningFilter.initWithRadius(context, 5);
        addFilter(imageRGBOpeningFilter);

        imageOpacityFilter.init(context);
        imageOpacityFilter.setOpacity(0.70f);
        addFilter(imageOpacityFilter);

        imageScreenBlendFilter.init(context);
        addFilter(imageScreenBlendFilter);

        imageOverlayBlendFilter2.init(context);
        addFilter(imageOverlayBlendFilter2);

        imageOpacityFilter2.init(context);
        imageOpacityFilter2.setOpacity(opacity);
        addFilter(imageOpacityFilter2);

        imageSoftLightBlendFilter.init(context);
        addFilter(imageSoftLightBlendFilter);

        // texture 1
        textureImage1.initWithImage(context, texture1);
        addFilter(textureImage1);
        textureImage1.addTarget(imageOverlayBlendFilter1, 1);
        textureImage1.processImage();

        // original 2
        // ImagePicture originalImage = new ImagePicture();
        // originalImage.initWithImage(context, original);
        // addFilter(originalImage);
        imageRGBOpeningFilter.addTarget(imageOpacityFilter);
        imageOpacityFilter.addTarget(imageScreenBlendFilter, 1);
        // originalImage.addTarget(imageRGBOpeningFilter, 1);
        // originalImage.processImage();

        // texture 2
        textureImage2.initWithImage(context, texture2);
        addFilter(textureImage2);
        imageOpacityFilter2.addTarget(imageOverlayBlendFilter2, 1);
        textureImage2.addTarget(imageOpacityFilter2);
        textureImage2.processImage();

        // texture 3
        textureImage3.initWithImage(context, texture3);
        addFilter(textureImage3);
        textureImage3.addTarget(imageSoftLightBlendFilter, 1);
        textureImage3.processImage();

        // start filtering
        imageRGBDilationFilter.addTarget(imageUnsharpMaskFilter);
        imageUnsharpMaskFilter.addTarget(imageOverlayBlendFilter1);
        imageOverlayBlendFilter1.addTarget(imageScreenBlendFilter);
        imageScreenBlendFilter.addTarget(imageOverlayBlendFilter2);
        imageOverlayBlendFilter2.addTarget(imageSoftLightBlendFilter);

        initialFilters.clear();
        initialFilters.add(imageRGBDilationFilter);
        initialFilters.add(imageRGBOpeningFilter);
        terminalFilter = imageSoftLightBlendFilter;
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(1.f, 0.f, .4f, 100.f, "Opacity"));
            mArtFilterInfo = new ArtFilterInfo("Oil pastel", progressInfo, "0900703832",
                                               "olleh_oil_pastel", "item_oil_pastel");
        }
        return mArtFilterInfo;
    }
}
