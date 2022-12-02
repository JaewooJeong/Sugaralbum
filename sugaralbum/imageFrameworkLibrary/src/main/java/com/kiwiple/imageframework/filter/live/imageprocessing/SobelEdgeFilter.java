
package com.kiwiple.imageframework.filter.live.imageprocessing;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Sampler;

import com.kiwiple.imageframework.ScriptC_luminance;
import com.kiwiple.imageframework.ScriptC_sobel_edge_detection;
import com.kiwiple.imageframework.ScriptC_sobel_edge_detection_lite;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;

public class SobelEdgeFilter {
    private ScriptC_sobel_edge_detection_lite mSobelEdgeDetectionLite;
    private ScriptC_sobel_edge_detection mSobelEdgeDetection;
    private ScriptC_luminance mLuminance;

    private int width;
    private int height;

    public SobelEdgeFilter(RenderScript rs, int width, int height) {
        mLuminance = new ScriptC_luminance(rs);

        this.width = width;
        this.height = height;

        // lite version일때는 경량화한 rs를 사용한다.
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mSobelEdgeDetectionLite = new ScriptC_sobel_edge_detection_lite(rs);
            mSobelEdgeDetectionLite.set_width(width - 1);
            mSobelEdgeDetectionLite.set_height(height - 1);
        } else {
            mSobelEdgeDetection = new ScriptC_sobel_edge_detection(rs);
            mSobelEdgeDetection.set_width(width);
            mSobelEdgeDetection.set_height(height);
            mSobelEdgeDetection.set_sampler(Sampler.CLAMP_LINEAR(rs));
        }
    }

    public void setValues(float[] params) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mSobelEdgeDetectionLite.set_texelWidth((long)Math.ceil((params[0] * width)
                    / (width + 500)));
            mSobelEdgeDetectionLite.set_texelHeight((long)Math.ceil((params[0] * height)
                    / (height + 500)));
        } else {
            mSobelEdgeDetection.set_texelWidth(params[0] / (width + 500));
            mSobelEdgeDetection.set_texelHeight(params[0] / (height + 500));
        }
    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mLuminance.forEach_luminance(mAllocationSub);
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mSobelEdgeDetectionLite.set_inAllocation(mAllocationSub);
            mSobelEdgeDetectionLite.forEach_sobelEdgeDetection(allocation);
        } else {
            mSobelEdgeDetection.set_inAllocation(mAllocationSub);
            mSobelEdgeDetection.forEach_sobelEdgeDetection(allocation);
        }
    }
}
