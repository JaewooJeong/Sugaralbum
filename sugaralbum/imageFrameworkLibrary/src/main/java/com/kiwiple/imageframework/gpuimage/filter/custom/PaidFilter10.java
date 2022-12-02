
package com.kiwiple.imageframework.gpuimage.filter.custom;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageColorDodgeBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageDissolveBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageOverlayBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageSoftLightBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageOpacityFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageRGBDilationFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageRGBErosionFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageRGBOpeningFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageUnsharpMaskFilter;
import com.kiwiple.imageframework.gpuimage.source.ImagePicture;

public class PaidFilter10 extends ImageFilterGroup {
    private ImageRGBOpeningFilter imageRGBOpeningFilter = new ImageRGBOpeningFilter();
    private ImageRGBErosionFilter imageRGBErosionFilter = new ImageRGBErosionFilter();
    private ImageRGBDilationFilter imageRGBDilationFilter = new ImageRGBDilationFilter();
    private ImageUnsharpMaskFilter imageUnsharpMaskFilter1 = new ImageUnsharpMaskFilter();
    private ImageUnsharpMaskFilter imageUnsharpMaskFilter2 = new ImageUnsharpMaskFilter();
    private ImageDissolveBlendFilter imageDissolveBlendFilter = new ImageDissolveBlendFilter();
    private ImageSoftLightBlendFilter imageSoftLightBlendFilter = new ImageSoftLightBlendFilter();
    private ImageOpacityFilter imageOpacityFilter1 = new ImageOpacityFilter();
    private ImageOverlayBlendFilter imageOverlayBlendFilter = new ImageOverlayBlendFilter();
    private ImageColorDodgeBlendFilter imageColorDodgeBlendFilter = new ImageColorDodgeBlendFilter();
    private ImageOpacityFilter imageOpacityFilter2 = new ImageOpacityFilter();
    private ImagePicture textureImage1 = new ImagePicture();
    private ImagePicture textureImage2 = new ImagePicture();

    private ArtFilterInfo mArtFilterInfo;

    public void initWithImage(Context context, final float blur, final Bitmap texture1,
            final Bitmap texture2, final float opacity) {
        super.init(context);
        imageRGBOpeningFilter.initWithRadius(context, 5);
        addFilter(imageRGBOpeningFilter);

        imageRGBErosionFilter.initWithRadius(context, 2);
        addFilter(imageRGBErosionFilter);

        imageRGBDilationFilter.initWithRadius(context, 3);
        addFilter(imageRGBDilationFilter);

        imageUnsharpMaskFilter1.init(context);
        imageUnsharpMaskFilter1.setBlur(blur);
        imageUnsharpMaskFilter1.setIntensity(5.f);
        addFilter(imageUnsharpMaskFilter1);

        imageUnsharpMaskFilter2.init(context);
        imageUnsharpMaskFilter2.setBlur(blur);
        imageUnsharpMaskFilter2.setIntensity(5.f);
        addFilter(imageUnsharpMaskFilter2);

        imageDissolveBlendFilter.init(context);
        imageDissolveBlendFilter.setMix(0.2f);
        addFilter(imageDissolveBlendFilter);

        imageSoftLightBlendFilter.init(context);
        addFilter(imageSoftLightBlendFilter);

        imageOpacityFilter1.init(context);
        imageOpacityFilter1.setOpacity(0.95f);
        addFilter(imageOpacityFilter1);

        imageOverlayBlendFilter.init(context);
        addFilter(imageOverlayBlendFilter);

        imageColorDodgeBlendFilter.init(context);
        addFilter(imageColorDodgeBlendFilter);

        imageOpacityFilter2.init(context);
        imageOpacityFilter2.setOpacity(opacity);
        addFilter(imageOpacityFilter2);

        // texture1
        textureImage1.initWithImage(context, texture1);
        addFilter(textureImage1);
        textureImage1.addTarget(imageOpacityFilter2);
        imageOpacityFilter2.addTarget(imageOverlayBlendFilter, 1);
        textureImage1.processImage();

        // texture2
        textureImage2.initWithImage(context, texture2);
        addFilter(textureImage2);
        textureImage2.addTarget(imageColorDodgeBlendFilter, 1);
        textureImage2.processImage();

        // start first step
        imageRGBErosionFilter.addTarget(imageUnsharpMaskFilter1);
        imageUnsharpMaskFilter1.addTarget(imageDissolveBlendFilter, 1);

        // start second step
        imageRGBDilationFilter.addTarget(imageUnsharpMaskFilter2);
        imageUnsharpMaskFilter2.addTarget(imageOpacityFilter1);
        imageOpacityFilter1.addTarget(imageSoftLightBlendFilter, 1);

        // start final step
        imageRGBOpeningFilter.addTarget(imageDissolveBlendFilter);
        imageDissolveBlendFilter.addTarget(imageSoftLightBlendFilter);
        imageSoftLightBlendFilter.addTarget(imageOverlayBlendFilter);
        imageOverlayBlendFilter.addTarget(imageColorDodgeBlendFilter);

        initialFilters.clear();
        initialFilters.add(imageRGBErosionFilter);
        initialFilters.add(imageRGBDilationFilter);
        initialFilters.add(imageRGBOpeningFilter);
        terminalFilter = imageColorDodgeBlendFilter;
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(1.f, 0.f, .5f, 100.f, "Opacity"));
            mArtFilterInfo = new ArtFilterInfo("Old painting", progressInfo, "0900703830",
                                               "olleh_old_painting", "item_old_painting");
        }
        return mArtFilterInfo;
    }
}
