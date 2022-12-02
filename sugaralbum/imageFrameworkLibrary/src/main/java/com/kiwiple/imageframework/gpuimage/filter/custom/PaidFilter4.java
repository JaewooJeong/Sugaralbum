
package com.kiwiple.imageframework.gpuimage.filter.custom;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageMultiplyBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageKuwaharaFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageThresholdSketchFilter;
import com.kiwiple.imageframework.gpuimage.source.ImagePicture;

public class PaidFilter4 extends ImageFilterGroup {
    private ImageMultiplyBlendFilter imageMultiplyBlendFilter = new ImageMultiplyBlendFilter();
    private ImageThresholdSketchFilter mSketchFilter = new ImageThresholdSketchFilter();
    private ImageKuwaharaFilter mKuwaharaFilter = new ImageKuwaharaFilter();
    private ImagePicture textureImage = new ImagePicture();

    private ArtFilterInfo mArtFilterInfo;

    public void initWithImage(Context context, final Bitmap texture1, int width, int height) {
        super.init(context);
        imageMultiplyBlendFilter.init(context);
        addFilter(imageMultiplyBlendFilter);

        mSketchFilter.init(context);
        addFilter(mSketchFilter);

        mKuwaharaFilter.init(context);
        addFilter(mKuwaharaFilter);

        // setup texture
        textureImage.setOverrideSize(width, height);
        textureImage.initWithImage(context, texture1, true);
        addFilter(textureImage);
        textureImage.addTarget(imageMultiplyBlendFilter);
        textureImage.processImage();

        // start filtering
        mSketchFilter.addTarget(mKuwaharaFilter);
        mKuwaharaFilter.addTarget(imageMultiplyBlendFilter);

        initialFilters.clear();
        initialFilters.add(mSketchFilter);
        terminalFilter = imageMultiplyBlendFilter;
    }

    public void setThreshold(float newValue) {
        mSketchFilter.setThreshold(newValue);
    }

    public void setWeight(float newValue) {
        mSketchFilter.setWeight(newValue);
    }

    public void setRadius(int newValue) {
        mKuwaharaFilter.setRadius(newValue);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(3);
            progressInfo.add(new ProgressInfo(1.f, .45f, .5f, 100.f, "Threshold"));
            progressInfo.add(new ProgressInfo(3.2f, 0.6f, 2.f, 50.f, ImageFilter.LINEWIDTH, true));
            progressInfo.add(new ProgressInfo(11.f, 2.f, 5.f, 1.f, "Radius"));
            mArtFilterInfo = new ArtFilterInfo("Wash drawing", progressInfo, "0900703804",
                                               "olleh_wash_drawing", "item_wash_drawing");
        }
        return mArtFilterInfo;
    }
}
