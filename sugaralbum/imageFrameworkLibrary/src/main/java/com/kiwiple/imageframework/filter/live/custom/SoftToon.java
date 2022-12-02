
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_blend_color_burn;
import com.kiwiple.imageframework.ScriptC_saturation;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;
import com.kiwiple.imageframework.filter.live.RsYuv;
import com.kiwiple.imageframework.filter.live.effect.SketchFilter;

public class SoftToon {
    private SketchFilter mImageSketchFilter;
    private ScriptC_saturation mImageSaturationFilter;
    private ScriptC_blend_color_burn imageColorBurnBlendFilter;

    private Allocation mAllocationOut;

    public SoftToon(RenderScript rs, int width, int height) {
        mImageSketchFilter = new SketchFilter(rs, width, height);
        mImageSaturationFilter = new ScriptC_saturation(rs);
        imageColorBurnBlendFilter = new ScriptC_blend_color_burn(rs);

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
        mImageSaturationFilter.set_saturation(params[1]);// 1

        // lite version일때는 값을 낮추어 성능을 향상 시킨다.
        if(!ArtFilterRenderScriptUtils.LITE_VERSION) {
            mImageSketchFilter.setValuesOverride(params);
        } else {
            mImageSketchFilter.setValuesOverride2(new float[] {
                1f
            });
        }
    }

    public void excute(Allocation allocation, Allocation allocationSub) {
        mAllocationOut.copyFrom(allocation);
        mImageSketchFilter.excute(allocationSub, mAllocationOut);
        imageColorBurnBlendFilter.forEach_colorBurnBlend(allocationSub, allocation);
        mImageSaturationFilter.forEach_saturationFilter(allocation, allocation);
    }
}
