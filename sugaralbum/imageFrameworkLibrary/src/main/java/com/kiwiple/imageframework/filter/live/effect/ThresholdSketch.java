
package com.kiwiple.imageframework.filter.live.effect;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Sampler;

import com.kiwiple.imageframework.ScriptC_luminance;
import com.kiwiple.imageframework.ScriptC_threshold_sketch;
import com.kiwiple.imageframework.ScriptC_threshold_sketch_lite;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;

public class ThresholdSketch {
    private ScriptC_threshold_sketch_lite mThresholdSketchLite;
    private ScriptC_threshold_sketch mThresholdSketch;
    private ScriptC_luminance mLuminance;

    private int width;
    private int height;

    public ThresholdSketch(RenderScript rs, int width, int height) {
        mLuminance = new ScriptC_luminance(rs);

        this.width = width;
        this.height = height;

        // lite version일때는 경량화한 rs를 사용한다.
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mThresholdSketchLite = new ScriptC_threshold_sketch_lite(rs);
            mThresholdSketchLite.set_width(width - 1);
            mThresholdSketchLite.set_height(height - 1);
        } else {
            mThresholdSketch = new ScriptC_threshold_sketch(rs);
            mThresholdSketch.set_width(width);
            mThresholdSketch.set_height(height);
            mThresholdSketch.set_sampler(Sampler.CLAMP_LINEAR(rs));
        }
    }

    public void setValues(float[] params) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mThresholdSketchLite.set_texelWidth((long)Math.ceil((params[1] * width) / (width + 500)));
            mThresholdSketchLite.set_texelHeight((long)Math.ceil((params[1] * height)
                    / (height + 500)));
            mThresholdSketchLite.set_threshold(params[0]);
        } else {
            mThresholdSketch.set_texelWidth(params[1] / (width + 500));
            mThresholdSketch.set_texelHeight(params[1] / (height + 500));
            mThresholdSketch.set_threshold(params[0]);
        }
    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mLuminance.forEach_luminance(mAllocationSub);
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mThresholdSketchLite.set_inAllocation(mAllocationSub);
            mThresholdSketchLite.forEach_threshold_sketch(allocation);
        } else {
            mThresholdSketch.set_inAllocation(mAllocationSub);
            mThresholdSketch.forEach_threshold_sketch(allocation);
        }
    }
}
