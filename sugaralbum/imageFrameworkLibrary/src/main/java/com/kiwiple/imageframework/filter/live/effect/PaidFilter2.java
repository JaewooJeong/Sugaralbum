
package com.kiwiple.imageframework.filter.live.effect;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

import com.kiwiple.imageframework.ScriptC_posterize;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;
import com.kiwiple.imageframework.filter.live.color.Hue;
import com.kiwiple.imageframework.filter.live.color.Levels;

public class PaidFilter2 {
    private Hue mHueFilter;
    private ScriptC_posterize mPosterize;
    private Levels mlevels;

    public PaidFilter2(RenderScript rs) {
        mHueFilter = new Hue(rs);
        mPosterize = new ScriptC_posterize(rs);
        mlevels = new Levels(rs);
    }

    public void setMin(float[] params) {
        mlevels.setParams(params);
    }

    public void setHue(float params) {
        mHueFilter.setParams(params);
    }

    public void setColorLevels(float param) {
        mPosterize.set_colorLevels(param);
    }

    public void setValues(float[] params) {
        mlevels.setParams(params);
        mPosterize.set_colorLevels(params[3]);
        mHueFilter.setParams(params[4]);
    }

    public void excute(Allocation allocation) {
        mlevels.excute(allocation);
        mPosterize.forEach_posterize(allocation);
        // lite version일때는 skip
        if(!ArtFilterRenderScriptUtils.LITE_VERSION) {
            mHueFilter.excute(allocation);
        }
    }
}
