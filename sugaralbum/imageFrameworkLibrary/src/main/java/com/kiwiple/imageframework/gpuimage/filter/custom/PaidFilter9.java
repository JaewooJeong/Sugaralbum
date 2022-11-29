
package com.kiwiple.imageframework.gpuimage.filter.custom;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageOpacityMaskBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImagePaidFilter9BlendFilterL;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageAdjustOpacityFilter;
import com.kiwiple.imageframework.gpuimage.source.ImagePicture;

public class PaidFilter9 extends ImageFilterGroup {
    private ImagePaidFilter9BlendFilterL[] imagePaidFilter9BlendFilter1L = new ImagePaidFilter9BlendFilterL[] {
            new ImagePaidFilter9BlendFilterL(), new ImagePaidFilter9BlendFilterL(),
            new ImagePaidFilter9BlendFilterL()
    };
    private ImageOpacityMaskBlendFilter imageOpacityBlendFilter1 = new ImageOpacityMaskBlendFilter();
    private ImageOpacityMaskBlendFilter imageOpacityBlendFilter2 = new ImageOpacityMaskBlendFilter();
    private ImageAdjustOpacityFilter imageAdjustOpacityFilter = new ImageAdjustOpacityFilter();
    private ImagePicture gradientImage = new ImagePicture();

    private ArtFilterInfo mArtFilterInfo1;
    private ArtFilterInfo mArtFilterInfo2;

    public void initWithImage(Context context, final Bitmap gradient, float hueValue) {
        super.init(context);
        // first step filter
        imagePaidFilter9BlendFilter1L[0].init(context);
        imagePaidFilter9BlendFilter1L[0].setThreshold(0.47f + threshold);
        imagePaidFilter9BlendFilter1L[0].setHue(hueValue);
        imagePaidFilter9BlendFilter1L[0].setOpacity(0.20f);
        addFilter(imagePaidFilter9BlendFilter1L[0]);

        imageOpacityBlendFilter1.init(context);
        addFilter(imageOpacityBlendFilter1);

        // second step filter
        imagePaidFilter9BlendFilter1L[1].init(context);
        imagePaidFilter9BlendFilter1L[1].setThreshold(0.37f + threshold);
        imagePaidFilter9BlendFilter1L[1].setHue(hueValue);
        imagePaidFilter9BlendFilter1L[1].setOpacity(0.60f);
        addFilter(imagePaidFilter9BlendFilter1L[1]);

        imageOpacityBlendFilter2.init(context);
        addFilter(imageOpacityBlendFilter2);

        // third step filter
        imagePaidFilter9BlendFilter1L[2].init(context);
        imagePaidFilter9BlendFilter1L[2].setThreshold(0.2f + threshold);
        imagePaidFilter9BlendFilter1L[2].setHue(hueValue);
        imagePaidFilter9BlendFilter1L[2].setOpacity(0.90f);
        addFilter(imagePaidFilter9BlendFilter1L[2]);

        imageAdjustOpacityFilter.init(context);
        addFilter(imageAdjustOpacityFilter);

        // setup gradient image
        gradientImage.initWithImage(context, gradient);
        addFilter(gradientImage);
        gradientImage.addTarget(imagePaidFilter9BlendFilter1L[0], 1);
        gradientImage.addTarget(imagePaidFilter9BlendFilter1L[1], 1);
        gradientImage.addTarget(imagePaidFilter9BlendFilter1L[2], 1);
        gradientImage.processImage();

        // start first step
        imagePaidFilter9BlendFilter1L[0].addTarget(imageOpacityBlendFilter1);

        // start second step
        imagePaidFilter9BlendFilter1L[1].addTarget(imageOpacityBlendFilter1, 1);

        // start third step
        imagePaidFilter9BlendFilter1L[2].addTarget(imageOpacityBlendFilter2, 1);

        // start final step
        imageOpacityBlendFilter1.addTarget(imageOpacityBlendFilter2);
        imageOpacityBlendFilter2.addTarget(imageAdjustOpacityFilter);

        initialFilters.clear();
        initialFilters.add(imagePaidFilter9BlendFilter1L[0]);
        initialFilters.add(imagePaidFilter9BlendFilter1L[1]);
        initialFilters.add(imagePaidFilter9BlendFilter1L[2]);
        terminalFilter = imageAdjustOpacityFilter;
    }

    private float threshold;

    public void setThreshold(float newValue) {
        threshold = newValue - .4f;
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo1 == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(2);
            progressInfo.add(new ProgressInfo(.55f, .25f, .47f, 100.f, "Threshold"));
            progressInfo.add(new ProgressInfo(360.f, 0.f, 207.f, 1.f, "Hue"));
            mArtFilterInfo1 = new ArtFilterInfo("Marshmallow", progressInfo, "0900703829",
                                                "olleh_marshmallow", "item_marshmallow");
        }
        return mArtFilterInfo1;
    }

    @Override
    public ArtFilterInfo getSecondFilterInfo() {
        if(mArtFilterInfo2 == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(2);
            progressInfo.add(new ProgressInfo(.55f, .25f, .40f, 100.f, "Threshold"));
            progressInfo.add(new ProgressInfo(360.f, 0.f, 90.f, 1.f, "Hue"));
            mArtFilterInfo2 = new ArtFilterInfo("Marshmallow", progressInfo, "0900703829",
                                                "olleh_marshmallow", "item_marshmallow");
        }
        return mArtFilterInfo2;
    }
}
