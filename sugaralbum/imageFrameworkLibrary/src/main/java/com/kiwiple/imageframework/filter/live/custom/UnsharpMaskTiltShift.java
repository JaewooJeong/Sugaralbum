
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

import com.kiwiple.imageframework.filter.live.imageprocessing.TiltShift;
import com.kiwiple.imageframework.filter.live.imageprocessing.UnsharpMask;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;

public class UnsharpMaskTiltShift extends ImageFilterGroup {
    private TiltShift mImageTiltShiftFilter;
    private UnsharpMask mImageUnsharpMaskFilter;

    public UnsharpMaskTiltShift(RenderScript rs, int width, int height) {
        mImageTiltShiftFilter = new TiltShift(rs, width, height);
        mImageUnsharpMaskFilter = new UnsharpMask(rs, width, height);

    }

    public void setValues(float[] params) {
        mImageTiltShiftFilter.setValues(params);
        mImageUnsharpMaskFilter.setValues(new float[] {
            params[4]
        });

    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mImageUnsharpMaskFilter.excute(allocation, mAllocationSub);
        mAllocationSub.copyFrom(allocation);
        mImageTiltShiftFilter.excute(allocation, mAllocationSub);
    }

}
