
package com.kiwiple.imageframework.filter.live.imageprocessing;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Sampler;

import com.kiwiple.imageframework.ScriptC_unsharp_mask;
import com.kiwiple.imageframework.ScriptC_unsharp_mask_lite;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;

public class UnsharpMask {
    private ScriptC_unsharp_mask mUnsharpMaskFilter;
    private ScriptC_unsharp_mask_lite mUnsharpMaskFilterLite;
    private GaussianBlur mBlurFilter;

    public UnsharpMask(RenderScript rs, int width, int height) {
        // lite version일때는 경량화한 rs를 사용한다.
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mUnsharpMaskFilterLite = new ScriptC_unsharp_mask_lite(rs);
        } else {
            mUnsharpMaskFilter = new ScriptC_unsharp_mask(rs);
            mUnsharpMaskFilter.set_width(width);
            mUnsharpMaskFilter.set_height(height);
            mUnsharpMaskFilter.set_sampler(Sampler.CLAMP_LINEAR(rs));
        }

        mBlurFilter = new GaussianBlur(rs, width, height);
        mBlurFilter.setValues(new float[] {
            ArtFilterUtils.getWeightedParam(width, height, 1.f)
        });
    }

    public void setValues(float[] params) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mUnsharpMaskFilterLite.set_intensity(params[0]);
        } else {
            mUnsharpMaskFilter.set_intensity(params[0]);
        }
    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mBlurFilter.excute(allocation, mAllocationSub);

        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mUnsharpMaskFilterLite.set_blurAllocation(allocation);
            mUnsharpMaskFilterLite.forEach_unsharp_mask(mAllocationSub, allocation);
        } else {
            mUnsharpMaskFilter.set_inAllocation1(mAllocationSub);
            mUnsharpMaskFilter.set_inAllocation2(allocation);
            mUnsharpMaskFilter.forEach_unsharp_mask(allocation);
        }
    }
}
