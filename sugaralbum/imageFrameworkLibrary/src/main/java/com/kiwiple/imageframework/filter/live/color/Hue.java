
package com.kiwiple.imageframework.filter.live.color;

import androidx.renderscript.Allocation;
import androidx.renderscript.Matrix3f;
import androidx.renderscript.RenderScript;

import com.kiwiple.imageframework.ScriptC_hue;
import com.kiwiple.imageframework.ScriptC_hue_lite;
import com.kiwiple.imageframework.filter.live.ArtFilterRenderScriptUtils;

public class Hue {
    public static final Matrix3f mMatrixYIQToRGB = new Matrix3f();
    public static final Matrix3f mMatrixRGBToYIQ = new Matrix3f();
    static {
        mMatrixRGBToYIQ.set(0, 0, 0.299f);
        mMatrixRGBToYIQ.set(1, 0, 0.587f);
        mMatrixRGBToYIQ.set(2, 0, 0.114f);
        mMatrixRGBToYIQ.set(0, 1, 0.595716f);
        mMatrixRGBToYIQ.set(1, 1, -0.274453f);
        mMatrixRGBToYIQ.set(2, 1, -0.321263f);
        mMatrixRGBToYIQ.set(0, 2, 0.211456f);
        mMatrixRGBToYIQ.set(1, 2, -0.522591f);
        mMatrixRGBToYIQ.set(2, 2, 0.31135f);

        mMatrixYIQToRGB.set(0, 0, 1f);
        mMatrixYIQToRGB.set(1, 0, 0.9563f);
        mMatrixYIQToRGB.set(2, 0, 0.6210f);
        mMatrixYIQToRGB.set(0, 1, 1f);
        mMatrixYIQToRGB.set(1, 1, -0.2721f);
        mMatrixYIQToRGB.set(2, 1, -0.6474f);
        mMatrixYIQToRGB.set(0, 2, 1f);
        mMatrixYIQToRGB.set(1, 2, -1.1070f);
        mMatrixYIQToRGB.set(2, 2, 1.7046f);
    }

    private ScriptC_hue mHue;
    private ScriptC_hue_lite mHueLite;

    public Hue(RenderScript rs) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mHueLite = new ScriptC_hue_lite(rs);
            mHueLite.set_rgbToYiq(Hue.mMatrixRGBToYIQ);
            mHueLite.set_YiqToRgb(Hue.mMatrixYIQToRGB);
        } else {
            mHue = new ScriptC_hue(rs);
            mHue.set_rgbToYiq(Hue.mMatrixRGBToYIQ);
            mHue.set_YiqToRgb(Hue.mMatrixYIQToRGB);
        }
    }

    public void setParams(float param) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            float hue = (float)((param % 360.0f) * Math.PI / 180.f);
            mHueLite.set_cosHue((float)Math.cos(-hue));
            mHueLite.set_sinHue((float)Math.sin(-hue));
        } else {
            mHue.set_hueAdjust((float)((param % 360.0f) * Math.PI / 180.f));
        }
    }

    public void excute(Allocation allocation) {
        if(ArtFilterRenderScriptUtils.LITE_VERSION) {
            mHueLite.forEach_hue(allocation);
        } else {
            mHue.forEach_hue(allocation);
        }
    }
}
