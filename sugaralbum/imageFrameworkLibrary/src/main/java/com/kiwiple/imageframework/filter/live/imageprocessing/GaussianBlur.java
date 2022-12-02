
package com.kiwiple.imageframework.filter.live.imageprocessing;

import androidx.renderscript.Allocation;
import androidx.renderscript.Long2;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Sampler;

import com.kiwiple.imageframework.ScriptC_gaussian;
import com.kiwiple.imageframework.ScriptC_gaussian_lite;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;

public class GaussianBlur {
    private ScriptC_gaussian mGaussian;
    private ScriptC_gaussian_lite mGaussianLite;

    private int width;
    private int height;
    private Long2[] mVBlurSize = new Long2[4];
    private Long2[] mHBlurSize = new Long2[4];

    private RenderScript mRs;

    public GaussianBlur(RenderScript rs, int width, int height) {
        mRs = rs;
        this.width = width;
        this.height = height;
        // lite version일때는 경량화한 rs를 사용한다.
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mGaussianLite = new ScriptC_gaussian_lite(rs);
            mGaussianLite.set_width(width - 1);
            mGaussianLite.set_height(height - 1);
        } else {
            mGaussian = new ScriptC_gaussian(rs);
            mGaussian.set_width(width);
            mGaussian.set_height(height);
        }
    }

    public void setValues(float[] params) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mVBlurSize[0] = new Long2(0, (long)params[0]);
            mVBlurSize[1] = new Long2(0, (long)(params[0] * 2f));
            mVBlurSize[2] = new Long2(0, (long)(params[0] * 3f));
            mVBlurSize[3] = new Long2(0, (long)(params[0] * 4f));
            mHBlurSize[0] = new Long2((long)params[0], 0);
            mHBlurSize[1] = new Long2((long)(params[0] * 2f), 0);
            mHBlurSize[2] = new Long2((long)(params[0] * 3f), 0);
            mHBlurSize[3] = new Long2((long)(params[0] * 4f), 0);
        } else {
            mGaussian.set_blurSize(params[0]);
            mGaussian.set_sampler(Sampler.CLAMP_LINEAR(mRs));
        }
    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mGaussianLite.set_firstStepOffset(mVBlurSize[0]);
            mGaussianLite.set_secondStepOffset(mVBlurSize[1]);
            mGaussianLite.set_thirdStepOffset(mVBlurSize[2]);
            mGaussianLite.set_firthStepOffset(mVBlurSize[3]);
            mGaussianLite.set_inAllocation(allocation);
            mGaussianLite.forEach_gaussian(mAllocationSub);

            mGaussianLite.set_firstStepOffset(mHBlurSize[0]);
            mGaussianLite.set_secondStepOffset(mHBlurSize[1]);
            mGaussianLite.set_thirdStepOffset(mHBlurSize[2]);
            mGaussianLite.set_firthStepOffset(mHBlurSize[3]);
            mGaussianLite.set_inAllocation(mAllocationSub);
            mGaussianLite.forEach_gaussian(allocation);
        } else {
            mGaussian.set_texelWidthOffset(0.0f);
            mGaussian.set_texelHeightOffset(1.0f / height);
            mGaussian.set_inAllocation(allocation);
            mGaussian.forEach_gaussian(mAllocationSub);

            mGaussian.set_texelWidthOffset(1.0f / width);
            mGaussian.set_texelHeightOffset(0.0f);
            mGaussian.set_inAllocation(mAllocationSub);
            mGaussian.forEach_gaussian(allocation);
        }
    }
}
