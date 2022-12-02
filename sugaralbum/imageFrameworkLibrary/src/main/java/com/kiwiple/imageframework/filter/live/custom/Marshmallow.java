
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_blend_opacity_mask;
import com.kiwiple.imageframework.ScriptC_blend_paid_9;
import com.kiwiple.imageframework.filter.live.color.Hue;

public class Marshmallow {
    private ScriptC_blend_paid_9 sImagePaidFilter9BlendFilter1L[];
    private ScriptC_blend_opacity_mask sImageOpacityBlendFilter1;
    private ScriptC_blend_opacity_mask sImageOpacityBlendFilter2;

    private Allocation mOriginalAllocation1;
    private Allocation mOriginalAllocation2;
    private Allocation mOriginalAllocation3;

    private Allocation mAllocationTexture1;

    float threshold;

    public Marshmallow(RenderScript rs, int width, int height) {
        sImagePaidFilter9BlendFilter1L = new ScriptC_blend_paid_9[] {
                new ScriptC_blend_paid_9(rs), new ScriptC_blend_paid_9(rs),
                new ScriptC_blend_paid_9(rs)
        };
        sImageOpacityBlendFilter1 = new ScriptC_blend_opacity_mask(rs);
        sImageOpacityBlendFilter2 = new ScriptC_blend_opacity_mask(rs);

        if(mOriginalAllocation2 != null) {
            mOriginalAllocation2.destroy();
        }

        if(mOriginalAllocation3 != null) {
            mOriginalAllocation3.destroy();
        }
        Type.Builder tb;
        tb = new Type.Builder(rs, Element.RGBA_8888(rs));
        tb.setX(width);
        tb.setY(height);
        mOriginalAllocation2 = Allocation.createTyped(rs, tb.create(), Allocation.USAGE_SCRIPT);
        mOriginalAllocation3 = Allocation.createTyped(rs, tb.create(), Allocation.USAGE_SCRIPT);
    }

    public void setSubImages(Allocation subImages) {
        mAllocationTexture1 = subImages;
    }

    // params[0] => threshold, params[1] => hue
    public void setValues(float[] params) {
        threshold = params[0] - 0.4f;

        // TODO: hue lite version
        sImagePaidFilter9BlendFilter1L[0].set_rgbToYiq(Hue.mMatrixRGBToYIQ);
        sImagePaidFilter9BlendFilter1L[0].set_YiqToRgb(Hue.mMatrixYIQToRGB);
        sImagePaidFilter9BlendFilter1L[0].set_threshold(0.47f + threshold);
        sImagePaidFilter9BlendFilter1L[0].set_hueAdjust((float)((params[1] % 360.0f) * Math.PI / 180.f));
        sImagePaidFilter9BlendFilter1L[0].set_opacity(0.2f);

        sImagePaidFilter9BlendFilter1L[1].set_rgbToYiq(Hue.mMatrixRGBToYIQ);
        sImagePaidFilter9BlendFilter1L[1].set_YiqToRgb(Hue.mMatrixYIQToRGB);
        sImagePaidFilter9BlendFilter1L[1].set_threshold(0.37f + threshold);
        sImagePaidFilter9BlendFilter1L[1].set_hueAdjust((float)((params[1] % 360.0f) * Math.PI / 180.f));
        sImagePaidFilter9BlendFilter1L[1].set_opacity(0.6f);

        sImagePaidFilter9BlendFilter1L[2].set_rgbToYiq(Hue.mMatrixRGBToYIQ);
        sImagePaidFilter9BlendFilter1L[2].set_YiqToRgb(Hue.mMatrixYIQToRGB);
        sImagePaidFilter9BlendFilter1L[2].set_threshold(0.2f + threshold);
        sImagePaidFilter9BlendFilter1L[2].set_hueAdjust((float)((params[1] % 360.0f) * Math.PI / 180.f));
        sImagePaidFilter9BlendFilter1L[2].set_opacity(0.9f);
    }

    public void excute(Allocation allocation) {
        mOriginalAllocation1 = allocation;
        mOriginalAllocation2.copyFrom(allocation);
        mOriginalAllocation3.copyFrom(allocation);

        // first step
        sImagePaidFilter9BlendFilter1L[0].forEach_blendPaid9(mAllocationTexture1,
                                                             mOriginalAllocation1);

        sImagePaidFilter9BlendFilter1L[1].forEach_blendPaid9(mAllocationTexture1,
                                                             mOriginalAllocation2);

        sImagePaidFilter9BlendFilter1L[2].forEach_blendPaid9(mAllocationTexture1,
                                                             mOriginalAllocation3);

        sImageOpacityBlendFilter1.forEach_opacityMaskBlend(mOriginalAllocation2,
                                                           mOriginalAllocation1);

        sImageOpacityBlendFilter2.forEach_opacityMaskBlend(mOriginalAllocation3,
                                                           mOriginalAllocation1);
    }
}
