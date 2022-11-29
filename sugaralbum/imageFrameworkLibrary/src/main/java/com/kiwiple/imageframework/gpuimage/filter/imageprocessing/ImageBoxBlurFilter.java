
package com.kiwiple.imageframework.gpuimage.filter.imageprocessing;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoPassTextureSamplingFilter;

public class ImageBoxBlurFilter extends ImageTwoPassTextureSamplingFilter {
    private int mFirstBlurSizeUniform;
    private int mSecondBlurSizeUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFirstStageVertexShaderFromResource(context, "box_blur_vertex",
                                                         "box_blur_fragment", "box_blur_vertex",
                                                         "box_blur_fragment");

        mFirstBlurSizeUniform = GLES20.glGetUniformLocation(mProgram, "blurSize");
        mSecondBlurSizeUniform = GLES20.glGetUniformLocation(mSecondProgram, "blurSize");

        setBlurSize(ArtFilterUtils.getWeightedParam(getOutputWidth(), getOutputHeight(), 1.f));
    }

    public void setBlurSize(float blurSize) {
        setFloat(blurSize, mFirstBlurSizeUniform, mProgram);
        setFloat(blurSize, mSecondBlurSizeUniform, mSecondProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(7.f, 1.f, 4.f, 1.f, ImageFilter.BLURSIZE, true));
            mArtFilterInfo = new ArtFilterInfo("Box blur", progressInfo);
        }
        return mArtFilterInfo;
    }
}
