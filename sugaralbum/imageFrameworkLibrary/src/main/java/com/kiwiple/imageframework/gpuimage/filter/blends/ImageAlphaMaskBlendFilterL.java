
package com.kiwiple.imageframework.gpuimage.filter.blends;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoInputFilter;

/**
 * custom 필터 개발을 위해 추가
 */
public class ImageAlphaMaskBlendFilterL extends ImageTwoInputFilter {
    private int mMixUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "alpha_mask_blend_fragment");

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
            mArtFilterInfo = new ArtFilterInfo("Alpha mask blend", progressInfo);
        }
        return mArtFilterInfo;
    }
}
