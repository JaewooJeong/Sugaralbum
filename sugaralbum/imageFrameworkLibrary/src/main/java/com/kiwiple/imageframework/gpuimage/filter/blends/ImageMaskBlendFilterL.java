
package com.kiwiple.imageframework.gpuimage.filter.blends;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoInputFilter;

/**
 * custom 필터 개발을 위해 추가
 */
public class ImageMaskBlendFilterL extends ImageTwoInputFilter {
    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "mask_blend_fragment");
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            mArtFilterInfo = new ArtFilterInfo("Mask blend", null);
        }
        return mArtFilterInfo;
    }
}
