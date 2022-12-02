
package com.kiwiple.imageframework.gpuimage.filter.colorprocessing;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

public class ImageHueFilter extends ImageFilter {
    private int mHueAdjustUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "hue_fragment");

        mHueAdjustUniform = GLES20.glGetUniformLocation(mProgram, "hueAdjust");
        if(mHueAdjustUniform != -1)
            setHue(90.f);
    }

    public void setHue(float newHue) {
        float hue = (float)((newHue % 360.0f) * Math.PI / 180.f);
        setFloat(hue, mHueAdjustUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(360.f, 0.f, 90.f, 1.f, "Hue"));
            mArtFilterInfo = new ArtFilterInfo("Hue", progressInfo);
        }
        return mArtFilterInfo;
    }
}
