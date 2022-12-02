
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

import com.kiwiple.imageframework.ScriptC_blend_overlay;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;
import com.kiwiple.imageframework.filter.live.effect.Kuwahara;
import com.kiwiple.imageframework.filter.live.imageprocessing.RGBOpening;
import com.kiwiple.imageframework.filter.live.imageprocessing.UnsharpMask;

public class OilPainting {
    private Kuwahara mKuwaharaFilter;// 40
    private ScriptC_blend_overlay imageOverlayBlendFilter;// 10
    private RGBOpening imageRGBOpeningFilter;// 40
    private UnsharpMask imageUnsharpMaskFilter;// 30

    public OilPainting(RenderScript rs, int width, int height) {
        mKuwaharaFilter = new Kuwahara(rs, width, height);
        imageOverlayBlendFilter = new ScriptC_blend_overlay(rs);
        imageRGBOpeningFilter = new RGBOpening(rs, width, height);
        imageUnsharpMaskFilter = new UnsharpMask(rs, width, height);

        // lite version일때는 값을 낮추어 성능을 향상 시킨다.
        if(!ArtFilterRenderScriptUtils.LITE_VERSION) {
            imageRGBOpeningFilter.setValues(new float[] {
                2
            });
            imageUnsharpMaskFilter.setValues(new float[] {
                7.f
            });
        } else {
            imageRGBOpeningFilter.setValues(new float[] {
                1
            });
            imageUnsharpMaskFilter.setValues(new float[] {
                3.f
            });
        }
    }

    public void setValues(float[] params) {
        mKuwaharaFilter.setValues(params);
    }

    public void excute(Allocation tempSubImages, Allocation allocation, Allocation allocationSub) {
        imageRGBOpeningFilter.excute(allocation, allocationSub);
        // lite version일때는 skip
        if(!ArtFilterRenderScriptUtils.LITE_VERSION) {
            allocationSub.copyFrom(allocation);
            imageUnsharpMaskFilter.excute(allocation, allocationSub);

            allocationSub.copyFrom(allocation);
            mKuwaharaFilter.excute(allocation, allocationSub);
        }

        imageOverlayBlendFilter.forEach_overlayBlend(tempSubImages, allocation);
    }
}
