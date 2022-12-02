
package com.kiwiple.imageframework.gpuimage.filter.blends;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoInputFilter;

public class ImageSoftLightBlendFilter extends ImageTwoInputFilter {
    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "soft_light_blend_fragment");
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            mArtFilterInfo = new ArtFilterInfo("Soft light blend", null);
        }
        return mArtFilterInfo;
    }
}
