
package com.kiwiple.imageframework.filter.live.imageprocessing;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Sampler;

import com.kiwiple.imageframework.ScriptC_tilt_shift;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;

public class TiltShift extends ImageFilterGroup {
    private GaussianBlur mImageGaussianBlurFilter;
    private ScriptC_tilt_shift mTiltShiftFilter;

    public TiltShift(RenderScript rs, int width, int height) {
        mImageGaussianBlurFilter = new GaussianBlur(rs, width, height);
        mTiltShiftFilter = new ScriptC_tilt_shift(rs);
        mTiltShiftFilter.set_width(width);
        mTiltShiftFilter.set_height(height);
        mTiltShiftFilter.set_sampler(Sampler.CLAMP_LINEAR(rs));
    }

    public void setValues(float[] params) {
        mImageGaussianBlurFilter.setValues(params);
        mTiltShiftFilter.set_topFocusLevel(params[1]);
        mTiltShiftFilter.set_bottomFocusLevel(params[2]);
        mTiltShiftFilter.set_focusFallOffRate(params[3]);

    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mImageGaussianBlurFilter.excute(allocation, mAllocationSub);

        mTiltShiftFilter.set_inAllocation1(mAllocationSub);
        mTiltShiftFilter.set_inAllocation2(allocation);
        mTiltShiftFilter.forEach_tilt_shift(allocation);
    }
}
