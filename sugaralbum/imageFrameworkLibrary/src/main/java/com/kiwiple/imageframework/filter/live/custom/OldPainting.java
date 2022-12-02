
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_blend_color_dodge;
import com.kiwiple.imageframework.ScriptC_blend_dissolve;
import com.kiwiple.imageframework.ScriptC_blend_overlay;
import com.kiwiple.imageframework.ScriptC_blend_soft_light;
import com.kiwiple.imageframework.ScriptC_opacity;
import com.kiwiple.imageframework.filter.live.RsYuv;
import com.kiwiple.imageframework.filter.live.imageprocessing.RGBDilation;
import com.kiwiple.imageframework.filter.live.imageprocessing.RGBErosion;
import com.kiwiple.imageframework.filter.live.imageprocessing.RGBOpening;
import com.kiwiple.imageframework.filter.live.imageprocessing.UnsharpMask;

public class OldPainting {
    private RGBOpening imageRGBOpeningFilter;
    private RGBErosion imageRGBErosionFilter;
    private RGBDilation imageRGBDilationFilter;
    private UnsharpMask imageUnsharpMaskFilter1;
    private UnsharpMask imageUnsharpMaskFilter2;
    private ScriptC_blend_dissolve imageDissolveBlendFilter;
    private ScriptC_blend_soft_light imageSoftLightBlendFilter;
    private ScriptC_opacity imageOpacityFilter1;
    private ScriptC_blend_overlay imageOverlayBlendFilter;
    private ScriptC_blend_color_dodge imageColorDodgeBlendFilter;
    private ScriptC_opacity imageOpacityFilter2;

    private Allocation mAllocation1;
    private Allocation mAllocation2;
    private Allocation mAllocation3;

    public OldPainting(RenderScript rs, int width, int height) {
        imageRGBOpeningFilter = new RGBOpening(rs, width, height);
        imageRGBOpeningFilter.setValues(new float[] {
            5.f
        });

        imageRGBErosionFilter = new RGBErosion(rs, width, height);
        imageRGBErosionFilter.setValues(new float[] {
            2.f
        });

        imageRGBDilationFilter = new RGBDilation(rs, width, height);
        imageRGBDilationFilter.setValues(new float[] {
            3.f
        });

        imageUnsharpMaskFilter1 = new UnsharpMask(rs, width, height);
        imageUnsharpMaskFilter1.setValues(new float[] {
            5.f
        });

        imageUnsharpMaskFilter2 = new UnsharpMask(rs, width, height);
        imageUnsharpMaskFilter2.setValues(new float[] {
            5.f
        });

        imageDissolveBlendFilter = new ScriptC_blend_dissolve(rs);
        imageDissolveBlendFilter.set_mixturePercent(0.2f);

        imageSoftLightBlendFilter = new ScriptC_blend_soft_light(rs);

        imageOpacityFilter1 = new ScriptC_opacity(rs);
        imageOpacityFilter1.set_alpha(0.95f);

        imageOverlayBlendFilter = new ScriptC_blend_overlay(rs);

        imageColorDodgeBlendFilter = new ScriptC_blend_color_dodge(rs);

        imageOpacityFilter2 = new ScriptC_opacity(rs);

        if(mAllocation1 != null) {
            mAllocation1.destroy();
        }
        Type.Builder tb = new Type.Builder(rs, Element.RGBA_8888(rs));
        tb.setX(width);
        tb.setY(height);
        mAllocation1 = Allocation.createTyped(rs, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                              RsYuv.ALLOCATION_USAGE_FULL);

        if(mAllocation2 != null) {
            mAllocation2.destroy();
        }
        tb = new Type.Builder(rs, Element.RGBA_8888(rs));
        tb.setX(width);
        tb.setY(height);
        mAllocation2 = Allocation.createTyped(rs, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                              RsYuv.ALLOCATION_USAGE_FULL);

        if(mAllocation3 != null) {
            mAllocation3.destroy();
        }
        tb = new Type.Builder(rs, Element.RGBA_8888(rs));
        tb.setX(width);
        tb.setY(height);
        mAllocation3 = Allocation.createTyped(rs, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                              RsYuv.ALLOCATION_USAGE_FULL);

    }

    public void setValues(float[] params) {
        imageOpacityFilter2.set_alpha(params[0]);
    }

    public void excute(Allocation tempSubImage1, Allocation tempSubImage2, Allocation allocation,
            Allocation mAllocationSub) {
        mAllocation1.copyFrom(allocation);
        mAllocation2.copyFrom(allocation);
        mAllocation3.copyFrom(allocation);

        imageOpacityFilter2.forEach_opacity(tempSubImage1);

        imageRGBErosionFilter.excute(mAllocation1, mAllocationSub);
        imageUnsharpMaskFilter1.excute(mAllocation1, mAllocationSub);
        mAllocationSub.copyFrom(allocation);

        imageRGBDilationFilter.excute(mAllocation2, mAllocationSub);
        imageUnsharpMaskFilter2.excute(mAllocation2, mAllocationSub);
        mAllocationSub.copyFrom(allocation);

        imageOpacityFilter1.forEach_opacity(mAllocation2);

        imageRGBOpeningFilter.excute(allocation, mAllocation3);
        imageDissolveBlendFilter.forEach_dissolveBlend(mAllocation1, allocation);
        imageSoftLightBlendFilter.forEach_softLightBlend(mAllocation2, allocation);
        imageOverlayBlendFilter.forEach_overlayBlend(tempSubImage1, allocation);
        imageColorDodgeBlendFilter.forEach_colorDodgeBlend(tempSubImage2, allocation);
    }
}
