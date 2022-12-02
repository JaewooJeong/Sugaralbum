
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.Float2;
import androidx.renderscript.RenderScript;

import com.kiwiple.imageframework.ScriptC_pinch_distortion;
import com.kiwiple.imageframework.filter.live.imageprocessing.UnsharpMask;

public class UnsharpMaskPinchDistortion {
    private ScriptC_pinch_distortion mImagePinchDistortionFilter;
    private UnsharpMask mImageUnsharpMaskFilter;

    public UnsharpMaskPinchDistortion(RenderScript rs, int width, int height) {

        mImagePinchDistortionFilter = new ScriptC_pinch_distortion(rs);
        mImageUnsharpMaskFilter = new UnsharpMask(rs, width, height);

        mImagePinchDistortionFilter.set_center(new Float2(width / 2.f, height / 2.f));
        mImagePinchDistortionFilter.set_radius(10.f);

        mImagePinchDistortionFilter.set_width(width);
        mImagePinchDistortionFilter.set_height(height);
    }

    public void setValues(float[] params) {
        mImagePinchDistortionFilter.set_scale(params[0]);
        mImageUnsharpMaskFilter.setValues(new float[] {
            params[1]
        });

    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mImageUnsharpMaskFilter.excute(allocation, mAllocationSub);
        mAllocationSub.copyFrom(allocation);
        mImagePinchDistortionFilter.set_inAllocation(mAllocationSub);
        mImagePinchDistortionFilter.forEach_pinch_distortion(allocation);
    }

}
