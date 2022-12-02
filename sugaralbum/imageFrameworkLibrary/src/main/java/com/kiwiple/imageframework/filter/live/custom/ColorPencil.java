
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_blend_color_burn;
import com.kiwiple.imageframework.ScriptC_blend_multiply;
import com.kiwiple.imageframework.ScriptC_blend_normal;
import com.kiwiple.imageframework.ScriptC_blend_soft_light;
import com.kiwiple.imageframework.filter.live.RsYuv;
import com.kiwiple.imageframework.filter.live.color.Levels;
import com.kiwiple.imageframework.filter.live.effect.SketchFilter;

public class ColorPencil {
    private Levels mImageLevelsFilter;
    private ScriptC_blend_color_burn imageColorBurnBlendFilter;
    private SketchFilter imageSketchFilter;
    private ScriptC_blend_multiply imageMultiplyBlendFilter;
    private ScriptC_blend_soft_light imageSoftLightBlendFilter;
    private ScriptC_blend_normal imageNormalBlendFilter;

    private Allocation mAllocationOut;

    public ColorPencil(RenderScript rs, int width, int height) {
        mImageLevelsFilter = new Levels(rs);
        imageColorBurnBlendFilter = new ScriptC_blend_color_burn(rs);
        imageSketchFilter = new SketchFilter(rs, width, height);
        imageSketchFilter.setValues(new float[] {
            0.5f
        });
        imageMultiplyBlendFilter = new ScriptC_blend_multiply(rs);
        imageSoftLightBlendFilter = new ScriptC_blend_soft_light(rs);
        imageNormalBlendFilter = new ScriptC_blend_normal(rs);

        if(mAllocationOut != null) {
            mAllocationOut.destroy();
        }
        Type.Builder tb = new Type.Builder(rs, Element.RGBA_8888(rs));
        tb.setX(width);
        tb.setY(height);
        mAllocationOut = Allocation.createTyped(rs, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                                RsYuv.ALLOCATION_USAGE_FULL);
    }

    public void excute(Allocation allocation, Allocation mAllocationSub, final Allocation texture1,
            final Allocation texture2, final Allocation texture3) {
        mAllocationOut.copyFrom(allocation);

        // start filtering
        imageSketchFilter.excute(allocation, mAllocationSub);
        mImageLevelsFilter.excute(allocation);
        imageColorBurnBlendFilter.forEach_colorBurnBlend(mAllocationOut, allocation);
        imageMultiplyBlendFilter.forEach_multiplyBlend(texture1, allocation);
        imageSoftLightBlendFilter.forEach_softLightBlend(texture2, allocation);
        imageNormalBlendFilter.forEach_normalBlend(texture3, allocation);

    }

    public void setParam(float min) {
        mImageLevelsFilter.setParams(new float[] {
                min, 1.0f, 1.0f
        });
    }
}
