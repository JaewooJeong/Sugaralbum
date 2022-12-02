
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
public class ImageLuminanceThresholdBlendFilterL extends ImageTwoInputFilter {
    private int mThresholdUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "luminance_threshold_blend_fragment");

        mThresholdUniform = GLES20.glGetUniformLocation(mProgram, "threshold");
        if(mThresholdUniform != -1)
            setThreshold(0.5f);
    }

    public void setThreshold(float newValue) {
        setFloat(newValue, mThresholdUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(1.f, 0.2f, 0.41f, 100.f, "Threshold"));
            mArtFilterInfo = new ArtFilterInfo("Luminance threshold blend", progressInfo);
        }
        return mArtFilterInfo;
    }
}
