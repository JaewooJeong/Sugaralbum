
package com.kiwiple.imageframework.filter.live.color;

import androidx.renderscript.Allocation;
import androidx.renderscript.RenderScript;

import com.kiwiple.imageframework.ScriptC_levels;

public class Levels {
    private ScriptC_levels mLevel;
    private float mInBlack = 0.0f;
    private float mInWhite = 1.0f;
    private float mGamma = 1.0f;

    private float mInWMinInB;
    private float mOverInWMinInB;

    public Levels(RenderScript rs) {
        mLevel = new ScriptC_levels(rs);
        setLevels();
    }

    public void setParams(float[] params) {
        mInBlack = params[0];
        mGamma = 1.0f / params[1];
        mInWhite = params[2];
        setLevels();
    }

    private void setLevels() {
        mInWMinInB = mInWhite - mInBlack;
        mOverInWMinInB = 1.f / mInWMinInB;

        mLevel.set_inBlack(mInBlack);
        mLevel.set_overInWMinInB(mOverInWMinInB);
        mLevel.set_gamma(mGamma);
    }

    public void excute(Allocation allocation) {
        mLevel.forEach_levels(allocation);
    }
}
