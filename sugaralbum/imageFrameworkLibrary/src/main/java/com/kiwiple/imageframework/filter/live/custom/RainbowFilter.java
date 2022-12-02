
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

import com.kiwiple.imageframework.ScriptC_adjust_opacity;
import com.kiwiple.imageframework.ScriptC_blend_mask;
import com.kiwiple.imageframework.ScriptC_blend_overlay;
import com.kiwiple.imageframework.ScriptC_luminance_threshold;
import com.kiwiple.imageframework.filter.live.color.Hue;

public class RainbowFilter {
    private ScriptC_luminance_threshold mLuminanceThreshold;
    private Hue mHueFilter;
    private ScriptC_blend_mask mMaskBlend;
    private ScriptC_blend_overlay mOverlayBlend;
    private ScriptC_adjust_opacity mAdjustOpacity;

    private Allocation mAllocationTexture1;
    private Allocation mAllocationTexture2;

    public RainbowFilter(RenderScript rs) {
        mLuminanceThreshold = new ScriptC_luminance_threshold(rs);
        mHueFilter = new Hue(rs);
        mMaskBlend = new ScriptC_blend_mask(rs);
        mOverlayBlend = new ScriptC_blend_overlay(rs);
        mAdjustOpacity = new ScriptC_adjust_opacity(rs);
    }

    public void setSubImages(Allocation[] subImages) {
        mAllocationTexture1 = subImages[0];
        mAllocationTexture2 = subImages[1];
    }

    public void setValues(float[] params) {
        mLuminanceThreshold.set_threshold(params[0]);

        mHueFilter.setParams(params[1]);
        mHueFilter.excute(mAllocationTexture1);
    }

    public void excute(Allocation allocation) {
        mLuminanceThreshold.forEach_luminance_threshold(allocation);

        mMaskBlend.forEach_maskBlend(mAllocationTexture1, allocation);

        mOverlayBlend.forEach_overlayBlend(mAllocationTexture2, allocation);

        mAdjustOpacity.forEach_adjust_opacity(allocation);
    }
}
