
package com.kiwiple.imageframework.filter.live.effect;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Sampler;

import com.kiwiple.imageframework.ScriptC_toon;
import com.kiwiple.imageframework.ScriptC_toon_lite;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;

public class Toon {
    private ScriptC_toon mToon;
    private ScriptC_toon_lite mToonLite;

    private int width;
    private int height;

    public Toon(RenderScript rs, int width, int height) {
        this.width = width;
        this.height = height;
        // lite version일때는 경량화한 rs를 사용한다.
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mToonLite = new ScriptC_toon_lite(rs);
            mToonLite.set_width(width - 1);
            mToonLite.set_height(height - 1);
        } else {
            mToon = new ScriptC_toon(rs);
            mToon.set_width(width);
            mToon.set_height(height);
            mToon.set_sampler(Sampler.CLAMP_LINEAR(rs));
        }
    }

    public void setValues(float[] params) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mToonLite.set_threshold(params[0]);
            mToonLite.set_quantizationLevels(params[1]);
            mToonLite.set_texelWidth((long)Math.ceil(params[2]));
            mToonLite.set_texelHeight((long)Math.ceil(params[2]));
        } else {
            mToon.set_threshold(params[0] * 255.f);
            mToon.set_quantizationLevels(params[1]);
            mToon.set_texelWidth(params[2] / width);
            mToon.set_texelHeight(params[2] / height);
        }
    }

    public void setTexel(float value) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mToonLite.set_texelWidth((long)Math.ceil(value));
            mToonLite.set_texelHeight((long)Math.ceil(value));
        } else {
            mToon.set_texelWidth(value / width);
            mToon.set_texelHeight(value / height);
        }
    }

    public void setThreshold(float value) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mToonLite.set_threshold(value);
        } else {
            mToon.set_threshold(value * 255.f);
        }
    }

    public void setQuantizationLevels(float value) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mToonLite.set_quantizationLevels(value);
        } else {
            mToon.set_quantizationLevels(value);
        }
    }

    public void excute(Allocation allocation, Allocation allocationSub) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mToonLite.set_inAllocation(allocationSub);
            mToonLite.forEach_toon(allocation);
        } else {
            mToon.set_inAllocation(allocationSub);
            mToon.forEach_toon(allocation);
        }
    }
}
