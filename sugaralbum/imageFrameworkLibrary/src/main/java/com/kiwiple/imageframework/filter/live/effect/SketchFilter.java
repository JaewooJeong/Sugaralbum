
package com.kiwiple.imageframework.filter.live.effect;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Sampler;

import com.kiwiple.imageframework.ScriptC_luminance;
import com.kiwiple.imageframework.ScriptC_sketch;
import com.kiwiple.imageframework.ScriptC_sketch_lite;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;

public class SketchFilter {
    private ScriptC_sketch mSketchFilter;
    private ScriptC_sketch_lite mSketchFilterLite;
    private ScriptC_luminance mLuminance;

    private int width;
    private int height;

    public SketchFilter(RenderScript rs, int width, int height) {
        mLuminance = new ScriptC_luminance(rs);

        this.width = width;
        this.height = height;

        // lite version일때는 경량화한 rs를 사용한다.
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mSketchFilterLite = new ScriptC_sketch_lite(rs);
            mSketchFilterLite.set_width(width - 1);
            mSketchFilterLite.set_height(height - 1);
        } else {
            mSketchFilter = new ScriptC_sketch(rs);
            mSketchFilter.set_width(width);
            mSketchFilter.set_height(height);
            mSketchFilter.set_sampler(Sampler.CLAMP_LINEAR(rs));
        }
    }

    public void setValues(float[] params) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            long texelWidth = (long)Math.ceil((params[0] * width) / (width + 500));
            long texelHeight = (long)Math.ceil((params[0] * height) / (height + 500));
            mSketchFilterLite.set_texelWidth(texelWidth);
            mSketchFilterLite.set_texelHeight(texelHeight);
        } else {
            mSketchFilter.set_texelWidth(params[0] / (width + 500));
            mSketchFilter.set_texelHeight(params[0] / (height + 500));
        }
    }

    public void setValuesOverride(float[] params) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            long texelWidth = (long)Math.ceil((params[0] * width) / 1000f);
            long texelHeight = (long)Math.ceil((params[0] * height) / 1000f);
            mSketchFilterLite.set_texelWidth(texelWidth);
            mSketchFilterLite.set_texelHeight(texelHeight);
        } else {
            mSketchFilter.set_texelWidth(params[0] / 1000f);
            mSketchFilter.set_texelHeight(params[0] / 1000f);
        }
    }

    public void setValuesOverride2(float[] params) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mSketchFilterLite.set_texelWidth((long)params[0]);
            mSketchFilterLite.set_texelHeight((long)params[0]);
        } else {
            mSketchFilter.set_texelWidth(params[0] / 1000f);
            mSketchFilter.set_texelHeight(params[0] / 1000f);
        }
    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mLuminance.forEach_luminance(mAllocationSub);
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mSketchFilterLite.set_inAllocation(mAllocationSub);
            mSketchFilterLite.forEach_sketch(allocation);
        } else {
            mSketchFilter.set_inAllocation(mAllocationSub);
            mSketchFilter.forEach_sketch(allocation);
        }
    }
}
