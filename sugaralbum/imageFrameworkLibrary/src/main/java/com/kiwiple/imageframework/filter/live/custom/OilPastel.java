
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_blend_color_darken;
import com.kiwiple.imageframework.ScriptC_blend_overlay;
import com.kiwiple.imageframework.ScriptC_blend_soft_light;
import com.kiwiple.imageframework.ScriptC_opacity;
import com.kiwiple.imageframework.filter.live.RsYuv;
import com.kiwiple.imageframework.filter.live.imageprocessing.RGBDilation;
import com.kiwiple.imageframework.filter.live.imageprocessing.RGBOpening;
import com.kiwiple.imageframework.filter.live.imageprocessing.UnsharpMask;

public class OilPastel {

    private RGBDilation imageRGBDilationFilter;
    private UnsharpMask imageUnsharpMaskFilter;
    private ScriptC_blend_overlay imageOverlayBlendFilter1;
    private RGBOpening imageRGBOpeningFilter;
    private ScriptC_opacity imageOpacityFilter;
    private ScriptC_blend_color_darken imageScreenBlendFilter;
    private ScriptC_blend_overlay imageOverlayBlendFilter2;
    private ScriptC_opacity imageOpacityFilter2;
    private ScriptC_blend_soft_light imageSoftLightBlendFilter;

    private Allocation mAllocationOut;

    public OilPastel(RenderScript rs, int width, int height) {
        imageRGBDilationFilter = new RGBDilation(rs, width, height);
        imageUnsharpMaskFilter = new UnsharpMask(rs, width, height);
        imageOverlayBlendFilter1 = new ScriptC_blend_overlay(rs);
        imageRGBOpeningFilter = new RGBOpening(rs, width, height);
        imageOpacityFilter = new ScriptC_opacity(rs);
        imageScreenBlendFilter = new ScriptC_blend_color_darken(rs);
        imageOverlayBlendFilter2 = new ScriptC_blend_overlay(rs);
        imageOpacityFilter2 = new ScriptC_opacity(rs);
        imageSoftLightBlendFilter = new ScriptC_blend_soft_light(rs);

        imageRGBDilationFilter.setValues(new float[] {
            3.f
        });
        imageUnsharpMaskFilter.setValues(new float[] {
            7.f
        });
        imageRGBOpeningFilter.setValues(new float[] {
            5.f
        });
        imageOpacityFilter.set_alpha(0.70f);

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
        imageOpacityFilter2.set_alpha(params[0]);
    }

    public void excute(Allocation allocation, Allocation allocationSub, Allocation textureImage1,
            Allocation textureImage2, Allocation textureImage3) {
        mAllocationOut.copyFrom(allocation);

        imageOpacityFilter2.forEach_opacity(textureImage2);

        imageRGBOpeningFilter.excute(mAllocationOut, allocationSub);
        imageOpacityFilter.forEach_opacity(mAllocationOut);

        imageRGBDilationFilter.excute(allocation, allocationSub);
        allocationSub.copyFrom(allocation);
        imageUnsharpMaskFilter.excute(allocation, allocationSub);
        imageOverlayBlendFilter1.forEach_overlayBlend(textureImage1, allocation);
        imageScreenBlendFilter.forEach_colorDarkenBlend(mAllocationOut, allocation);
        imageOverlayBlendFilter2.forEach_overlayBlend(textureImage2, allocation);
        imageSoftLightBlendFilter.forEach_softLightBlend(textureImage3, allocation);

    }
}
