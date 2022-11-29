
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_blend_alpha;
import com.kiwiple.imageframework.ScriptC_blend_soft_light;
import com.kiwiple.imageframework.ScriptC_opacity;
import com.kiwiple.imageframework.filter.live.RsYuv;
import com.kiwiple.imageframework.filter.live.imageprocessing.GaussianBlur;
import com.kiwiple.imageframework.filter.live.imageprocessing.SmartBlur;

public class Custom15 {

    private GaussianBlur mImageGaussianBlurFilter1;
    private SmartBlur mImageSmartBlurFilter;
    private ScriptC_blend_alpha mImageAlphaBlendFilter;
    private GaussianBlur mImageGaussianBlurFilter2;
    private ScriptC_blend_soft_light mImageSoftLightBlendFilter;
    private ScriptC_opacity mImageOpacityFilter;

    private Allocation mAllocationOut1;
    private Allocation mAllocationOut2;

    public Custom15(RenderScript rs, int width, int height) {
        mImageGaussianBlurFilter1 = new GaussianBlur(rs, width, height);
        mImageSmartBlurFilter = new SmartBlur(rs, width, height);
        mImageAlphaBlendFilter = new ScriptC_blend_alpha(rs);
        mImageGaussianBlurFilter2 = new GaussianBlur(rs, width, height);
        mImageSoftLightBlendFilter = new ScriptC_blend_soft_light(rs);
        mImageOpacityFilter = new ScriptC_opacity(rs);

        Type.Builder tb;
        if(mAllocationOut1 != null) {
            mAllocationOut1.destroy();
        }
        tb = new Type.Builder(rs, Element.RGBA_8888(rs));
        tb.setX(width);
        tb.setY(height);
        mAllocationOut1 = Allocation.createTyped(rs, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                                 RsYuv.ALLOCATION_USAGE_FULL);

        Type.Builder tb1;
        if(mAllocationOut2 != null) {
            mAllocationOut2.destroy();
        }
        tb1 = new Type.Builder(rs, Element.RGBA_8888(rs));
        tb1.setX(width);
        tb1.setY(height);
        mAllocationOut2 = Allocation.createTyped(rs, tb1.create(), RsYuv.MIPMAPCONTROL_FULL,
                                                 RsYuv.ALLOCATION_USAGE_FULL);

    }

    public void setValues(float[] params) {
        mImageGaussianBlurFilter1.setValues(new float[] {
            params[0]
        });

        mImageSmartBlurFilter.setValues(new float[] {
                params[1], params[2]
        });
        mImageGaussianBlurFilter2.setValues(new float[] {
            params[1]
        });
        mImageAlphaBlendFilter.set_mixturePercent(params[3]);
        mImageOpacityFilter.set_alpha(params[4]);
    }

    public void excute(Allocation allocation, Allocation allocationSub) {
        mAllocationOut1.copyFrom(allocation);
        mAllocationOut2.copyFrom(allocation);
        mImageGaussianBlurFilter1.excute(mAllocationOut1, allocationSub);

        mImageGaussianBlurFilter2.excute(mAllocationOut2, allocationSub);

        allocationSub.copyFrom(mAllocationOut1);
        mImageSmartBlurFilter.excute(mAllocationOut1, allocationSub);
        mImageAlphaBlendFilter.forEach_alphaBlend(mAllocationOut1, allocation);

        mImageOpacityFilter.forEach_opacity(mAllocationOut2);

        mImageSoftLightBlendFilter.forEach_softLightBlend(mAllocationOut2, allocation);
    }
}
