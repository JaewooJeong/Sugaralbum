
package com.kiwiple.imageframework.gpuimage.filter.colorprocessing;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

public class ImageGrayscaleFilter extends ImageFilter {
    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "luminance_fragment");
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            mArtFilterInfo = new ArtFilterInfo("Grayscale", null);
        }
        return mArtFilterInfo;
    }
}
