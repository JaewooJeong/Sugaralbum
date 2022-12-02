
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_blend_soft_light;
import com.kiwiple.imageframework.ScriptC_opacity;
import com.kiwiple.imageframework.filter.live.RsYuv;
import com.kiwiple.imageframework.filter.live.imageprocessing.GaussianBlur;
import com.kiwiple.imageframework.filter.live.imageprocessing.SmartBlur;

public class Custom14 {

    private SmartBlur mImageSmartBlurFilter;
    private GaussianBlur mImageGaussianBlurFilter;
    private ScriptC_opacity mImageOpacityFilter;
    private ScriptC_blend_soft_light imageSoftLightBlendFilter;

    private Allocation mAllocationOut;

    public Custom14(RenderScript rs, int width, int height) {
        mImageSmartBlurFilter = new SmartBlur(rs, width, height);
        mImageGaussianBlurFilter = new GaussianBlur(rs, width, height);
        mImageOpacityFilter = new ScriptC_opacity(rs);
        imageSoftLightBlendFilter = new ScriptC_blend_soft_light(rs);

        Type.Builder tb;
        if(mAllocationOut != null) {
            mAllocationOut.destroy();
        }
        tb = new Type.Builder(rs, Element.RGBA_8888(rs));
        tb.setX(width);
        tb.setY(height);
        mAllocationOut = Allocation.createTyped(rs, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                                RsYuv.ALLOCATION_USAGE_FULL);

    }

    public void setValues(float[] params) {
        mImageSmartBlurFilter.setValues(params);
        mImageGaussianBlurFilter.setValues(params);
        mImageOpacityFilter.set_alpha(params[2]);
    }

    public void excute(Allocation allocation, Allocation allocationSub) {
        mAllocationOut.copyFrom(allocation);
        mImageGaussianBlurFilter.excute(mAllocationOut, allocationSub);
        mImageOpacityFilter.forEach_opacity(mAllocationOut);

        mImageSmartBlurFilter.excute(allocation, allocationSub);

        imageSoftLightBlendFilter.forEach_softLightBlend(mAllocationOut, allocation);
    }
}
