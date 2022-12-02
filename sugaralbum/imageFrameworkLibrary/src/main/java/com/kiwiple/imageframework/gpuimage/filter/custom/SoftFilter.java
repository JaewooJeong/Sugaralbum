
package com.kiwiple.imageframework.gpuimage.filter.custom;

import java.util.ArrayList;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.ImageInput;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageAlphaBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageGaussianBlurFilter;

public class SoftFilter extends ImageFilterGroup {
    private ImageGaussianBlurFilter mImageGaussianBlurFilter1 = new ImageGaussianBlurFilter();
    private ImageAlphaBlendFilter mImageAlphaBlendFilter = new ImageAlphaBlendFilter();

    private ArtFilterInfo mArtFilterInfo;

    public void initWithImage(Context context, float alpha) {
        super.init(context);
        mImageGaussianBlurFilter1.init(context);
        addFilter(mImageGaussianBlurFilter1);

        mImageAlphaBlendFilter.init(context);
        mImageAlphaBlendFilter.setMix(alpha);
        addFilter(mImageAlphaBlendFilter);

        mImageGaussianBlurFilter1.addTarget(mImageAlphaBlendFilter, 1);

        initialFilters = new ArrayList<ImageInput>();
        initialFilters.add(mImageGaussianBlurFilter1);
        initialFilters.add(mImageAlphaBlendFilter);
        terminalFilter = mImageAlphaBlendFilter;
    }

    public void setBlur(float newValue) {
        mImageGaussianBlurFilter1.setBlurSize(newValue);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(1.f, 0.f, 0.3f, 100.f, "Opacity"));
            progressInfo.add(new ProgressInfo(6.f, 0.f, 1.f, 10.f, ImageFilter.BLURSIZE, true));
            mArtFilterInfo = new ArtFilterInfo("Soft", progressInfo, null, null, null);
        }
        return mArtFilterInfo;
    }
}
