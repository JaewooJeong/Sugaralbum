
package com.kiwiple.imageframework.gpuimage.filter.imageprocessing;

import java.util.ArrayList;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;

public class ImageRGBOpeningFilter extends ImageFilterGroup {
    private ImageRGBErosionFilter erosionFilter = new ImageRGBErosionFilter();
    private ImageRGBDilationFilter dilationFilter = new ImageRGBDilationFilter();

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        initWithRadius(context, 1);
    }

    public void initWithRadius(Context context, int radius) {
        super.init(context);

        erosionFilter.initWithRadius(context, radius);
        addFilter(erosionFilter);

        dilationFilter.initWithRadius(context, radius);
        addFilter(dilationFilter);

        erosionFilter.addTarget(dilationFilter);

        initialFilters.clear();
        initialFilters.add(erosionFilter);
        terminalFilter = dilationFilter;
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(5.f, 1.f, 3.f, 1.f, "Radius"));
            mArtFilterInfo = new ArtFilterInfo("RGB opening", progressInfo);
        }
        return mArtFilterInfo;
    }
}
