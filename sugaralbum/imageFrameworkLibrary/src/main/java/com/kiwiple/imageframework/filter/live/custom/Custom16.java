
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_blend_overlay;
import com.kiwiple.imageframework.ScriptC_blend_soft_light;
import com.kiwiple.imageframework.ScriptC_color_invert;
import com.kiwiple.imageframework.ScriptC_opacity1;
import com.kiwiple.imageframework.ScriptC_opacity2;
import com.kiwiple.imageframework.ScriptC_remove_opacity;
import com.kiwiple.imageframework.filter.live.RsYuv;
import com.kiwiple.imageframework.filter.live.imageprocessing.HighPass;
import com.kiwiple.imageframework.filter.live.imageprocessing.SmartBlur;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;

public class Custom16 {

    private SmartBlur mImageSmartBlurFilter;
    private ScriptC_color_invert mImageColorInvertFilter;
    private HighPass mImageHighPassFilter;
    private ScriptC_opacity1 mImageOpacityFilter;
    private ScriptC_blend_overlay mImageOverlayBlendFilter;
    private ScriptC_opacity2 mImageOpacityFilter3;
    private ScriptC_blend_soft_light mImageSoftLightBlendFilter;
    private ScriptC_remove_opacity mImageRemoveOpacity;

    private Allocation mAllocationOut1;
    private Allocation mAllocationOut2;

    int width;
    int height;

    public Custom16(RenderScript rs, int width, int height) {

        this.width = width;
        this.height = height;

        mImageSmartBlurFilter = new SmartBlur(rs, width, height);
        mImageColorInvertFilter = new ScriptC_color_invert(rs);
        mImageHighPassFilter = new HighPass(rs, width, height);
        mImageOpacityFilter = new ScriptC_opacity1(rs);
        mImageOverlayBlendFilter = new ScriptC_blend_overlay(rs);
        mImageOpacityFilter3 = new ScriptC_opacity2(rs);
        mImageSoftLightBlendFilter = new ScriptC_blend_soft_light(rs);
        mImageRemoveOpacity = new ScriptC_remove_opacity(rs);

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
        mImageSmartBlurFilter.setValues(new float[] {
                params[0], params[1]
        });
        mImageHighPassFilter.setValues(new float[] {
                params[2], ArtFilterUtils.getWeightedParam(width, height, 1.f)
        });
        mImageOpacityFilter.set_alpha(params[3]);
        mImageOpacityFilter3.set_alpha(params[4]);
    }

    public void excute(Allocation allocation, Allocation allocationSub) {
        mAllocationOut1.copyFrom(allocation);
        mAllocationOut2.copyFrom(allocation);

        mImageSmartBlurFilter.excute(mAllocationOut1, allocationSub);
        mImageColorInvertFilter.forEach_color_invert(mAllocationOut1);
        allocationSub.copyFrom(mAllocationOut1);
        mImageHighPassFilter.subExcute(mAllocationOut1, allocationSub);

        mImageOpacityFilter.forEach_opacity(mAllocationOut1);
        // allocation.copyFrom(mAllocationOut1);

        mImageOverlayBlendFilter.forEach_overlayBlend(mAllocationOut1, allocation);

        mImageOpacityFilter3.forEach_opacity(mAllocationOut2);
        mImageSoftLightBlendFilter.forEach_softLightBlend(mAllocationOut2, allocation);

        mImageRemoveOpacity.forEach_remove_opacity(allocation);

    }
}
