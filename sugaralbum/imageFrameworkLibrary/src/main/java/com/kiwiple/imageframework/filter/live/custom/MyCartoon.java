
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

import com.kiwiple.imageframework.filter.live.effect.PaidFilter2;
import com.kiwiple.imageframework.filter.live.effect.Toon;

public class MyCartoon {
    private PaidFilter2 mImagePaidFilter2Filter;
    private Toon mImageToonFilter;

    public MyCartoon(RenderScript rs, int width, int height) {
        mImagePaidFilter2Filter = new PaidFilter2(rs);
        mImageToonFilter = new Toon(rs, width, height);

        mImagePaidFilter2Filter.setMin(new float[] {
                0.1f, 1.f, 0.85f
        });
    }

    public void setValues(float[] params) {
        mImagePaidFilter2Filter.setHue(params[0]);
        mImageToonFilter.setValues(new float[] {
                1.02f, 24.f, params[1]
        });
        mImagePaidFilter2Filter.setColorLevels(params[2]);
    }

    public void excute(Allocation allocation, Allocation allocationSub) {
        mImagePaidFilter2Filter.excute(allocation);
        mImageToonFilter.excute(allocation, allocationSub);
    }
}
