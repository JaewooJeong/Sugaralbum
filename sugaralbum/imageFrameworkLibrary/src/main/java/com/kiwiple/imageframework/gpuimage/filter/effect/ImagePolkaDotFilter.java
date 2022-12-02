
package com.kiwiple.imageframework.gpuimage.filter.effect;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;

public class ImagePolkaDotFilter extends ImagePixellateFilter {
    private int mDotScalingUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "polka_dot_fragment");
        mDotScalingUniform = GLES20.glGetUniformLocation(mProgram, "dotScaling");
        setDotScaling(0.9f);
    }

    public void setDotScaling(float newValue) {
        setFloat(newValue, mDotScalingUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(2);
            progressInfo.add(new ProgressInfo(.15f, .01f, .02f, 100.f, "Fractional width"));
            progressInfo.add(new ProgressInfo(1.f, .5f, .66f, 100.f, "Dot scaling"));
            mArtFilterInfo = new ArtFilterInfo("Polka dot", progressInfo);
        }
        return mArtFilterInfo;
    }
}
