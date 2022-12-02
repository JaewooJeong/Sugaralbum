
package com.kiwiple.imageframework.filter.live.color;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Sampler;

import com.kiwiple.imageframework.ScriptC_adaptive_threshold;
import com.kiwiple.imageframework.ScriptC_adaptive_threshold_lite;
import com.kiwiple.imageframework.ScriptC_gray_scale;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;
import com.kiwiple.imageframework.filter.live.imageprocessing.BoxBlur;

public class AdaptiveThreshold {
    private BoxBlur mBoxBlurFilter;
    private ScriptC_gray_scale mGrayScale;
    private ScriptC_adaptive_threshold adaptiveThresholdFilter;
    private ScriptC_adaptive_threshold_lite adaptiveThresholdFilterLite;

    private RenderScript mRs;

    public AdaptiveThreshold(RenderScript rs, int width, int height) {
        mRs = rs;
        mBoxBlurFilter = new BoxBlur(rs, width, height);
        mGrayScale = new ScriptC_gray_scale(rs);

        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            adaptiveThresholdFilterLite = new ScriptC_adaptive_threshold_lite(rs);
        } else {
            adaptiveThresholdFilter = new ScriptC_adaptive_threshold(rs);
            adaptiveThresholdFilter.set_width(width);
            adaptiveThresholdFilter.set_height(height);
            adaptiveThresholdFilter.set_sampler(Sampler.CLAMP_LINEAR(mRs));
        }
    }

    public void setParams(float[] params) {
        mBoxBlurFilter.setValues(params);
    }

    public void excute(Allocation grayAllocation, Allocation blurAllocation) {
        mGrayScale.forEach_grayScale(grayAllocation);

        mBoxBlurFilter.excute(blurAllocation, grayAllocation);
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            adaptiveThresholdFilterLite.set_grayAllocation(grayAllocation);
            adaptiveThresholdFilterLite.forEach_adaptive_threshold(blurAllocation, grayAllocation);
        } else {
            adaptiveThresholdFilter.set_inAllocation1(blurAllocation);
            adaptiveThresholdFilter.set_inAllocation2(grayAllocation);
            adaptiveThresholdFilter.forEach_adaptive_threshold(grayAllocation);
        }
    }
}
