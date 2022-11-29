
package com.kiwiple.imageframework.filter.live.imageprocessing;

import androidx.renderscript.Allocation;
import androidx.renderscript.Float2;
import androidx.renderscript.RenderScript;

import com.kiwiple.imageframework.ScriptC_tilt_shift_circle;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;

public class TiltShiftCircle extends ImageFilterGroup {
    private GaussianBlur mImageGaussianBlurFilter;
    private ScriptC_tilt_shift_circle mTiltShiftFilter;

    public TiltShiftCircle(RenderScript rs, int width, int height) {
        mImageGaussianBlurFilter = new GaussianBlur(rs, width, height);
        mTiltShiftFilter = new ScriptC_tilt_shift_circle(rs);
        mTiltShiftFilter.set_width(width);
        mTiltShiftFilter.set_height(height);
    }

    public void setValues(float[] params) {
        mImageGaussianBlurFilter.setValues(params);
        mTiltShiftFilter.set_radius(params[1]);
        mTiltShiftFilter.set_center(new Float2(params[2], params[3]));
        mTiltShiftFilter.set_focusFallOffRate(params[4]);

    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mImageGaussianBlurFilter.excute(allocation, mAllocationSub);

        mTiltShiftFilter.set_inAllocation1(mAllocationSub);
        mTiltShiftFilter.set_inAllocation2(allocation);
        mTiltShiftFilter.forEach_tilt_shift(allocation);
    }
}
