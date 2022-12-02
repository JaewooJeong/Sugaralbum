
package com.kiwiple.imageframework.filter.live.imageprocessing;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.Float2;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Sampler;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_box_blur;
import com.kiwiple.imageframework.ScriptC_box_blur_lite;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;
import com.kiwiple.imageframework.filter.live.RsYuv;

public class BoxBlur {
    private ScriptC_box_blur mBoxBlur;
    private ScriptC_box_blur_lite mBoxBlurLite;

    private int width;
    private int height;

    private Allocation mAllocationOut;

    public BoxBlur(RenderScript rs, int width, int height) {
        this.width = width;
        this.height = height;

        // lite version일때는 경량화한 rs를 사용한다.
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mBoxBlurLite = new ScriptC_box_blur_lite(rs);

            mBoxBlurLite.set_width(width - 1);
            mBoxBlurLite.set_height(height - 1);
        } else {
            mBoxBlur = new ScriptC_box_blur(rs);

            mBoxBlur.set_sampler(Sampler.CLAMP_LINEAR(rs));
            mBoxBlur.set_width(width);
            mBoxBlur.set_height(height);
        }

        if(mAllocationOut != null) {
            mAllocationOut.destroy();
        }
        Type.Builder tb = new Type.Builder(rs, Element.RGBA_8888(rs));
        tb.setX(width);
        tb.setY(height);
        mAllocationOut = Allocation.createTyped(rs, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                                RsYuv.ALLOCATION_USAGE_FULL);
    }

    private Float2 mFirstVerticalOffset;
    private Float2 mSecondVerticalOffset;
    private Float2 mFirstHorizontalOffset;
    private Float2 mSecondHorizontalOffset;

    public void setValues(float[] params) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mFirstVerticalOffset = new Float2(1.5f * 1.0f * params[0], 0.0f);
            mSecondVerticalOffset = new Float2(3.5f * 1.0f * params[0], 0.0f);
            mFirstHorizontalOffset = new Float2(0.0f, 1.5f * 1.0f * params[0]);
            mSecondHorizontalOffset = new Float2(0.0f, 3.5f * 1.0f * params[0]);
        } else {
            mBoxBlur.set_blurSize(params[0]);
        }
    }

    public void excute(Allocation allocation, Allocation mAllocationSub) {
        mAllocationOut.copyFrom(allocation);

        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mBoxBlurLite.set_inAllocation(mAllocationSub);
            mBoxBlurLite.set_firstOffset(mFirstVerticalOffset);
            mBoxBlurLite.set_secondOffset(mSecondVerticalOffset);
            mBoxBlurLite.forEach_boxBlur(mAllocationOut);

            mBoxBlurLite.set_inAllocation(mAllocationOut);
            mBoxBlurLite.set_firstOffset(mFirstHorizontalOffset);
            mBoxBlurLite.set_secondOffset(mSecondHorizontalOffset);
            mBoxBlurLite.forEach_boxBlur(allocation);
        } else {
            mBoxBlur.set_inAllocation(mAllocationSub);
            mBoxBlur.set_texelWidthOffset(1.0f / width);
            mBoxBlur.set_texelHeightOffset(0.0f);
            mBoxBlur.forEach_boxBlur(mAllocationOut);

            mBoxBlur.set_inAllocation(mAllocationOut);
            mBoxBlur.set_texelWidthOffset(0.0f);
            mBoxBlur.set_texelHeightOffset(1.0f / height);
            mBoxBlur.forEach_boxBlur(allocation);
        }
    }
}
