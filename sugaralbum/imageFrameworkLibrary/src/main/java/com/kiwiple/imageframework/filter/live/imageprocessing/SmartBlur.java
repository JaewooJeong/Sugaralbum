
package com.kiwiple.imageframework.filter.live.imageprocessing;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.Float2;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Sampler;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_smart_blur;
import com.kiwiple.imageframework.ScriptC_smart_blur_lite;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;
import com.kiwiple.imageframework.filter.live.RsYuv;

public class SmartBlur {
    private ScriptC_smart_blur mSmartBlur;
    private ScriptC_smart_blur_lite mSmartBlurLite;

    private int width;
    private int height;
    private Allocation mAllocationOut;
    private float mBlurSize;

    public SmartBlur(RenderScript rs, int width, int height) {
        this.width = width;
        this.height = height;
        // lite version일때는 경량화한 rs를 사용한다.
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mSmartBlurLite = new ScriptC_smart_blur_lite(rs);
            mSmartBlurLite.set_width(width);
            mSmartBlurLite.set_height(height);
        } else {
            mSmartBlur = new ScriptC_smart_blur(rs);
            mSmartBlur.set_width(width);
            mSmartBlur.set_height(height);
            mSmartBlur.set_sampler(Sampler.CLAMP_LINEAR(rs));
        }

        Type.Builder tb;
        if(mAllocationOut != null) {
            mAllocationOut.destroy();
        }
        tb = new Type.Builder(rs, Element.RGBA_8888(rs));
        tb.setX(width);
        tb.setY(height);
        mAllocationOut = Allocation.createTyped(rs, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                                RsYuv.ALLOCATION_USAGE_FULL);

    }

    public void setValues(float[] params) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mBlurSize = params[0];
            mSmartBlurLite.set_threshold(params[1]);
        } else {
            mSmartBlur.set_blurSize(params[0]);
            mSmartBlur.set_threshold(params[1]);
        }
    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mAllocationOut.copyFrom(allocation);
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mSmartBlurLite.set_singleStepOffset(new Float2(0.0f, 1.0f * mBlurSize));
            mSmartBlurLite.set_inAllocation(mAllocationSub);
            mSmartBlurLite.forEach_smart_blur(mAllocationOut);

            mSmartBlurLite.set_singleStepOffset(new Float2(1.0f * mBlurSize, 0.0f));
            mSmartBlurLite.set_inAllocation(mAllocationOut);
            mSmartBlurLite.forEach_smart_blur(allocation);
        } else {
            mSmartBlur.set_texelWidthOffset(0.0f);
            mSmartBlur.set_texelHeightOffset(1.0f / height);
            mSmartBlur.set_inAllocation(mAllocationSub);
            mSmartBlur.forEach_smart_blur(mAllocationOut);

            mSmartBlur.set_texelWidthOffset(1.0f / width);
            mSmartBlur.set_texelHeightOffset(0.0f);
            mSmartBlur.set_inAllocation(mAllocationOut);
            mSmartBlur.forEach_smart_blur(allocation);
        }
    }
}
