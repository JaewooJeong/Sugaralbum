
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_blend_alpha;
import com.kiwiple.imageframework.filter.live.RsYuv;
import com.kiwiple.imageframework.filter.live.imageprocessing.GaussianBlur;

public class Soft {
    private GaussianBlur mImageGaussianBlurFilter;
    private ScriptC_blend_alpha mImageAlphaBlendFilter;

    private Allocation mAllocationOut;

    public Soft(RenderScript rs, int width, int height) {
        mImageGaussianBlurFilter = new GaussianBlur(rs, width, height);
        mImageAlphaBlendFilter = new ScriptC_blend_alpha(rs);

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
        mImageGaussianBlurFilter.setValues(new float[] {
            params[1]
        });
        mImageAlphaBlendFilter.set_mixturePercent(params[0]);
    }

    public void excute(Allocation allocation, Allocation allocationSub) {
        mAllocationOut.copyFrom(allocation);
        mImageGaussianBlurFilter.excute(mAllocationOut, allocationSub);
        mImageAlphaBlendFilter.forEach_alphaBlend(mAllocationOut, allocation);
    }
}
