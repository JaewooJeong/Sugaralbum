
package com.kiwiple.imageframework.gpuimage.filter.effect;

import java.util.ArrayList;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageSobelEdgeDetectionFilter;

public class ImageSketchFilter extends ImageSobelEdgeDetectionFilter {
    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "sketch_fragment");
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(2.2f, .2f, .8f, 50.f, ImageFilter.LINEWIDTH, true));
            mArtFilterInfo = new ArtFilterInfo("A/F5", progressInfo);
        }
        return mArtFilterInfo;
    }
}
