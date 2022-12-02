
package com.kiwiple.imageframework.filter.live.imageprocessing;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Sampler;

import com.kiwiple.imageframework.ScriptC_luminance;
import com.kiwiple.imageframework.ScriptC_threshold_edge_detection;
import com.kiwiple.imageframework.ScriptC_threshold_edge_detection_lite;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;

public class ThresholdEdgeDetection {
    private ScriptC_threshold_edge_detection_lite mThresholdEdgeDetectionLite;
    private ScriptC_threshold_edge_detection mThresholdEdgeDetection;
    private ScriptC_luminance mLuminance;

    private int width;
    private int height;

    public ThresholdEdgeDetection(RenderScript rs, int width, int height) {
        mLuminance = new ScriptC_luminance(rs);

        this.width = width;
        this.height = height;

        // lite version일때는 경량화한 rs를 사용한다.
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mThresholdEdgeDetectionLite = new ScriptC_threshold_edge_detection_lite(rs);
            mThresholdEdgeDetectionLite.set_width(width - 1);
            mThresholdEdgeDetectionLite.set_height(height - 1);
        } else {
            mThresholdEdgeDetection = new ScriptC_threshold_edge_detection(rs);
            mThresholdEdgeDetection.set_width(width);
            mThresholdEdgeDetection.set_height(height);
            mThresholdEdgeDetection.set_sampler(Sampler.CLAMP_LINEAR(rs));
        }
    }

    public void setValues(float[] params) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mThresholdEdgeDetectionLite.set_texelWidth((long)Math.ceil((params[1] * width)
                    / (width + 500)));
            mThresholdEdgeDetectionLite.set_texelHeight((long)Math.ceil((params[1] * height)
                    / (height + 500)));
            mThresholdEdgeDetectionLite.set_threshold(params[0]);
        } else {
            mThresholdEdgeDetection.set_texelWidth(params[1] / (width + 500));
            mThresholdEdgeDetection.set_texelHeight(params[1] / (height + 500));
            mThresholdEdgeDetection.set_threshold(params[0]);
        }
    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mLuminance.forEach_luminance(mAllocationSub);
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mThresholdEdgeDetectionLite.set_inAllocation(mAllocationSub);
            mThresholdEdgeDetectionLite.forEach_threshold_edge_detection(allocation);
        } else {
            mThresholdEdgeDetection.set_inAllocation(mAllocationSub);
            mThresholdEdgeDetection.forEach_threshold_edge_detection(allocation);
        }
    }
}
