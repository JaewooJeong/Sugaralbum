
package com.kiwiple.imageframework.gpuimage.filter.custom;

import java.util.ArrayList;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImagePaidFilter2Filter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageToonFilter;

public class PaidFilter2 extends ImageFilterGroup {
    private ImagePaidFilter2Filter mImagePaidFilter2Filter = new ImagePaidFilter2Filter();
    private ImageToonFilter mImageToonFilter = new ImageToonFilter();

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.init(context);
        mImagePaidFilter2Filter.init(context);
        mImagePaidFilter2Filter.setMin(0.1f, 1.f, 0.85f);
        addFilter(mImagePaidFilter2Filter);

        mImageToonFilter.init(context);
        mImageToonFilter.setThreshold(1.02f);
        mImageToonFilter.setQuantizationLevels(24);
        addFilter(mImageToonFilter);

        // start filtering
        mImagePaidFilter2Filter.addTarget(mImageToonFilter);

        initialFilters.clear();
        initialFilters.add(mImagePaidFilter2Filter);
        terminalFilter = mImageToonFilter;
    }

    public void setHue(float newHue) {
        mImagePaidFilter2Filter.setHue(newHue);
    }

    public void setWeight(float newValue) {
        mImageToonFilter.setWeight(newValue);// 1
    }

    public void setColorLevels(int newValue) {
        mImagePaidFilter2Filter.setColorLevels(newValue);// 2
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(3);
            progressInfo.add(new ProgressInfo(360.f, .0f, 360.f, 1.f, "Hue"));
            progressInfo.add(new ProgressInfo(2.8f, .2f, 1.31f, 50.f, ImageFilter.LINEWIDTH, true));
            progressInfo.add(new ProgressInfo(15.f, 8.f, 15.f, 1.f, "Posterize"));
            mArtFilterInfo = new ArtFilterInfo("My cartoon", progressInfo, "0900703803",
                                               "olleh_my_cartoon", "item_my_cartoon");
        }
        return mArtFilterInfo;
    }
}
