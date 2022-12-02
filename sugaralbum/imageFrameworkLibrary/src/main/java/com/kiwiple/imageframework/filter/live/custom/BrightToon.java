
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_blend_overlay;
import com.kiwiple.imageframework.ScriptC_saturation;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;
import com.kiwiple.imageframework.filter.live.RsYuv;
import com.kiwiple.imageframework.filter.live.effect.Kuwahara;
import com.kiwiple.imageframework.filter.live.effect.SketchFilter;

public class BrightToon {
    private ScriptC_saturation imageSaturationFilter;
    private ScriptC_blend_overlay imageOverlayBlendFilter;
    private SketchFilter imageSketchFilter;
    private Kuwahara imageKuwaharaFilter;

    private Allocation mAllocationOut;

    public BrightToon(RenderScript rs, int width, int height) {
        imageSketchFilter = new SketchFilter(rs, width, height);
        imageSaturationFilter = new ScriptC_saturation(rs);
        imageOverlayBlendFilter = new ScriptC_blend_overlay(rs);
        imageKuwaharaFilter = new Kuwahara(rs, width, height);

        if(!ArtFilterRenderScriptUtils.LITE_VERSION) {
            imageSketchFilter.setValuesOverride(new float[] {
                1.f
            });
            imageKuwaharaFilter.setValues(new float[] {
                4f
            });
        } else {
            imageSketchFilter.setValuesOverride2(new float[] {
                1f
            });
        }

        if(mAllocationOut != null) {
            mAllocationOut.destroy();
        }
        Type.Builder tb = new Type.Builder(rs, Element.RGBA_8888(rs));
        tb.setX(width);
        tb.setY(height);
        mAllocationOut = Allocation.createTyped(rs, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                                RsYuv.ALLOCATION_USAGE_FULL);
    }

    public void setValues(float[] params) {
        imageSaturationFilter.set_saturation(params[0]);
    }

    public void excute(Allocation allocation, Allocation allocationSub) {
        mAllocationOut.copyFrom(allocation);

        if(!ArtFilterRenderScriptUtils.LITE_VERSION) {
            imageKuwaharaFilter.excute(allocation, mAllocationOut);
        }

        imageSketchFilter.excute(allocationSub, mAllocationOut);
        imageOverlayBlendFilter.forEach_overlayBlend(allocationSub, allocation);
        imageSaturationFilter.forEach_saturationFilter(allocation, allocation);
    }
}
