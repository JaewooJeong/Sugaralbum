
package com.kiwiple.imageframework.filter.live.custom;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

import com.kiwiple.imageframework.ScriptC_blend_alpha;
import com.kiwiple.imageframework.ScriptC_blend_alpha_mask;
import com.kiwiple.imageframework.ScriptC_blend_luminance_threshold;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;
import com.kiwiple.imageframework.filter.live.effect.SketchFilter;

public class PostGray {
    private SketchFilter mImageSketchFilter;
    private ScriptC_blend_alpha imageAlphaBlendFilter;
    private ScriptC_blend_luminance_threshold imageLuminanceThresholdBlendFilterL;
    private ScriptC_blend_alpha_mask imageAlphaMaskBlendFilterL;

    public PostGray(RenderScript rs, int width, int height) {
        mImageSketchFilter = new SketchFilter(rs, width, height);
        imageAlphaBlendFilter = new ScriptC_blend_alpha(rs);
        imageLuminanceThresholdBlendFilterL = new ScriptC_blend_luminance_threshold(rs);
        imageAlphaMaskBlendFilterL = new ScriptC_blend_alpha_mask(rs);

        imageAlphaBlendFilter.set_mixturePercent(0.2f);
        imageAlphaMaskBlendFilterL.set_mixturePercent(0.5f);
    }

    public void setValues(float[] params) {
        imageLuminanceThresholdBlendFilterL.set_threshold(params[1]);// 1
        // lite version일때는 값을 낮추어 성능을 향상 시킨다.
        if(!ArtFilterRenderScriptUtils.LITE_VERSION) {
            mImageSketchFilter.setValues(params);
        } else {
            mImageSketchFilter.setValuesOverride2(new float[] {
                1f
            });
        }
    }

    public void excute(Allocation tempSubImages, Allocation allocation, Allocation allocationSub) {
        mImageSketchFilter.excute(allocationSub, allocation);
        imageAlphaBlendFilter.forEach_alphaBlend(tempSubImages, allocationSub);
        imageLuminanceThresholdBlendFilterL.forEach_luminanceThresholdBlend(allocationSub,
                                                                            allocation);
        imageAlphaMaskBlendFilterL.forEach_alphaMaskBlend(tempSubImages, allocation);
    }
}
