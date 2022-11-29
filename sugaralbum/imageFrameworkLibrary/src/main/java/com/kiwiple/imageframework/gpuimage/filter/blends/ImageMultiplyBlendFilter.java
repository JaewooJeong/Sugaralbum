
package com.kiwiple.imageframework.gpuimage.filter.blends;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoInputFilter;

public class ImageMultiplyBlendFilter extends ImageTwoInputFilter {
    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "multiply_blend_fragment");
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            mArtFilterInfo = new ArtFilterInfo("Multiply blend", null);
        }
        return mArtFilterInfo;
    }
}
