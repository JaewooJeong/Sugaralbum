
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_blend_multiply;
import com.kiwiple.imageframework.filter.live.RsYuv;
import com.kiwiple.imageframework.filter.live.effect.Kuwahara;
import com.kiwiple.imageframework.filter.live.effect.ThresholdSketch;

public class WashDrawing {
    private ScriptC_blend_multiply imageMultiplyBlendFilter;
    private ThresholdSketch mThresholdSketchFilter;
    private Kuwahara mKuwaharaFilter;

    private Allocation mAllocationOut;

    public WashDrawing(RenderScript rs, int width, int height) {
        imageMultiplyBlendFilter = new ScriptC_blend_multiply(rs);
        mThresholdSketchFilter = new ThresholdSketch(rs, width, height);
        mKuwaharaFilter = new Kuwahara(rs, width, height);

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
        mThresholdSketchFilter.setValues(params);
        mKuwaharaFilter.setValues(new float[] {
            params[2]
        });
    }

    public void excute(Allocation tempSubImages, Allocation allocation, Allocation allocationSub) {
        mThresholdSketchFilter.excute(allocation, allocationSub);
        mAllocationOut.copyFrom(allocation);
        mKuwaharaFilter.excute(allocation, mAllocationOut);
        imageMultiplyBlendFilter.forEach_multiplyBlend(tempSubImages, allocation);
    }
}
