
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

import com.kiwiple.imageframework.filter.live.imageprocessing.TiltShiftCircle;
import com.kiwiple.imageframework.filter.live.imageprocessing.UnsharpMask;

public class UnsharpMaskTiltShiftCircle {
    private TiltShiftCircle mImageTiltShiftCircleFilter;
    private UnsharpMask mImageUnsharpMaskFilter;

    public UnsharpMaskTiltShiftCircle(RenderScript rs, int width, int height) {
        mImageTiltShiftCircleFilter = new TiltShiftCircle(rs, width, height);
        mImageUnsharpMaskFilter = new UnsharpMask(rs, width, height);

    }

    public void setValues(float[] params) {
        mImageTiltShiftCircleFilter.setValues(params);
        mImageUnsharpMaskFilter.setValues(new float[] {
            params[5]
        });

    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mImageUnsharpMaskFilter.excute(allocation, mAllocationSub);
        mAllocationSub.copyFrom(allocation);
        mImageTiltShiftCircleFilter.excute(allocation, mAllocationSub);
    }

}
