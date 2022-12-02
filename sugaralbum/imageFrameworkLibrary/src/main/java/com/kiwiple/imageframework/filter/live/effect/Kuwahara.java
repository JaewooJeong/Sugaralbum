
package com.kiwiple.imageframework.filter.live.effect;

import androidx.renderscript.Allocation;
import androidx.renderscript.Int2;
import androidx.renderscript.RenderScript;

import com.kiwiple.imageframework.ScriptC_kuwahara;
import com.kiwiple.imageframework.ScriptC_kuwahara_lite;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;

public class Kuwahara {
    private ScriptC_kuwahara mKuwahara;
    private ScriptC_kuwahara_lite mKuwaharaLite;

    private int mWidth;
    private int mHeight;

    public Kuwahara(RenderScript rs, int width, int height) {
        // lite version일때는 경량화한 rs를 사용한다.
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mKuwaharaLite = new ScriptC_kuwahara_lite(rs);
            mKuwaharaLite.set_width(width - 1);
            mKuwaharaLite.set_height(height - 1);
            mWidth = width;
            mHeight = height;
        } else {
            mKuwahara = new ScriptC_kuwahara(rs);
            mKuwahara.set_width(width);
            mKuwahara.set_height(height);
        }
    }

    public void setValues(float[] params) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            int negativeWidth = (int)Math.floor((mWidth / 768.f) * -params[0]);
            int negativeHeight = (int)Math.floor((mHeight / 1024.f) * -params[0]);
            int positiveWidth = (int)Math.floor((mWidth / 768.f) * params[0]);
            int positiveHeight = (int)Math.floor((mHeight / 1024.f) * params[0]);
            mKuwaharaLite.set_negativeFactor(new Int2(negativeWidth, negativeHeight));
            mKuwaharaLite.set_positiveFactor(new Int2(positiveWidth, positiveHeight));
        } else {
            mKuwahara.set_radius((int)params[0]);
        }
    }

    public void excute(Allocation allocation, Allocation allocationSub) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mKuwaharaLite.set_inAllocation(allocationSub);
            mKuwaharaLite.forEach_kuwahara(allocation);
        } else {
            mKuwahara.set_inAllocation(allocationSub);
            mKuwahara.forEach_kuwahara(allocation);
        }
    }
}
