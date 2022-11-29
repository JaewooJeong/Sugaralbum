
package com.kiwiple.imageframework.filter.live.imageprocessing;

import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_blend_overlay;
import com.kiwiple.imageframework.ScriptC_color_invert;
import com.kiwiple.imageframework.ScriptC_opacity;
import com.kiwiple.imageframework.ScriptC_remove_opacity;
import com.kiwiple.imageframework.filter.live.RsYuv;

public class HighPass {
    private GaussianBlur mImageGaussianBlurFilter;
    private ScriptC_color_invert mImageColorInvertFilter;
    private ScriptC_opacity mImageOpacityFilter;
    private ScriptC_blend_overlay mImageOverlayBlendFilter;
    private ScriptC_remove_opacity mImageRemoveOpacity;

    private Allocation mAllocationOut;

    public HighPass(RenderScript rs, int width, int height) {

        mImageGaussianBlurFilter = new GaussianBlur(rs, width, height);
        mImageColorInvertFilter = new ScriptC_color_invert(rs);
        mImageOpacityFilter = new ScriptC_opacity(rs);
        mImageOverlayBlendFilter = new ScriptC_blend_overlay(rs);
        mImageRemoveOpacity = new ScriptC_remove_opacity(rs);

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
        mImageOpacityFilter.set_alpha(params[0]);
        mImageGaussianBlurFilter.setValues(new float[] {
            params[1]
        });
    }

    public void excute(Allocation allocation, Allocation allocationSub) {
        mAllocationOut.copyFrom(allocation);

        mImageGaussianBlurFilter.excute(mAllocationOut, allocationSub);
        mImageColorInvertFilter.forEach_color_invert(mAllocationOut);

        mImageOpacityFilter.forEach_opacity(allocation);

        mImageOverlayBlendFilter.forEach_overlayBlend(mAllocationOut, allocation);

        mImageRemoveOpacity.forEach_remove_opacity(allocation);

    }

    public void subExcute(Allocation allocation, Allocation allocationSub) {
        mAllocationOut.copyFrom(allocation);

        mImageGaussianBlurFilter.excute(mAllocationOut, allocationSub);
        mImageColorInvertFilter.forEach_color_invert(mAllocationOut);

        mImageOpacityFilter.forEach_opacity(allocation);

        mImageOverlayBlendFilter.forEach_overlayBlend(mAllocationOut, allocation);

    }
}
