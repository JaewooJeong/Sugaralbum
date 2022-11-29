
package com.kiwiple.imageframework.gpuimage.filter.imageprocessing;

import java.util.ArrayList;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoInputFilter;

public class ImageUnsharpMaskFilter extends ImageFilterGroup {
    private ImageTwoInputFilter mUnsharpMaskFilter;
    private ImageGaussianBlurFilter mBlurFilter;
    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.init(context);

        mBlurFilter = new ImageGaussianBlurFilter();
        mBlurFilter.init(context);
        addFilter(mBlurFilter);

        mUnsharpMaskFilter = new ImageTwoInputFilter();
        mUnsharpMaskFilter.initWithFragmentShaderFromResource(context, "unsharp_mask_fragment");
        addFilter(mUnsharpMaskFilter);

        mBlurFilter.addTarget(mUnsharpMaskFilter, 1);

        initialFilters.clear();
        initialFilters.add(mBlurFilter);
        initialFilters.add(mUnsharpMaskFilter);
        terminalFilter = mUnsharpMaskFilter;
    }

    public void setBlur(float newValue) {
        mBlurFilter.setFloat(newValue, "blurSize");
    }

    public void setIntensity(float newValue) {
        mUnsharpMaskFilter.setFloat(newValue, "intensity");
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(7.f, .0f, 1.f, 10.f, ImageFilter.INTENSITY));
            mArtFilterInfo = new ArtFilterInfo("A/F6", progressInfo);
        }
        return mArtFilterInfo;
    }
}
