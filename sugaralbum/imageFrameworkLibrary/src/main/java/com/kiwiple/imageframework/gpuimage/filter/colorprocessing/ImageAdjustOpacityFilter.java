
package com.kiwiple.imageframework.gpuimage.filter.colorprocessing;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

public class ImageAdjustOpacityFilter extends ImageFilter {
    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "adjust_opacity_fragment");
    }
}
