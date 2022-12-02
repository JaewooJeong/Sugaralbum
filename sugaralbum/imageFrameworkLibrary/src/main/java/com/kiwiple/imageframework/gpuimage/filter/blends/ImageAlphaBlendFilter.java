
package com.kiwiple.imageframework.gpuimage.filter.blends;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoInputFilter;

public class ImageAlphaBlendFilter extends ImageTwoInputFilter {
    private int mMixUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "alpha_blend_fragment");

        mMixUniform = GLES20.glGetUniformLocation(mProgram, "mixturePercent");
        if(mMixUniform != -1)
            setMix(0.5f);
    }

    public void setMix(float newValue) {
        setFloat(newValue, mMixUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(1.f, 0.5f, .0f, 100.f, "Mix"));
            mArtFilterInfo = new ArtFilterInfo("Alpha Blend", progressInfo);
        }
        return mArtFilterInfo;
    }
}
