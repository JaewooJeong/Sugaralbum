
package com.kiwiple.imageframework.gpuimage.filter.imageprocessing;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoPassTextureSamplingFilter;

public class ImageGaussianBlurFilter extends ImageTwoPassTextureSamplingFilter {
    private int mHorizontalBlurSizeUniform;
    private int mVerticalBlurSizeUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        this.initWithFirstStageVertexShaderFromResource(context, null, null, null, null);

        mHorizontalBlurSizeUniform = GLES20.glGetUniformLocation(mProgram, "blurSize");
        mVerticalBlurSizeUniform = GLES20.glGetUniformLocation(mSecondProgram, "blurSize");

        setBlurSize(ArtFilterUtils.getWeightedParam(getOutputWidth(), getOutputHeight(), 1.f));
    }

    @Override
    public void initWithFirstStageVertexShaderFromResource(Context context,
            String firstStageVertexShaderResName, String firstStageFragmentShaderResName,
            String secondStageVertexShaderResName, String secondStageFragmentShaderResName) {
        super.initWithFirstStageVertexShaderFromResource(context,
                                                         firstStageVertexShaderResName == null ? "gaussian_vertex"
                                                                 : firstStageVertexShaderResName,
                                                         firstStageFragmentShaderResName == null ? "gaussian_fragment"
                                                                 : firstStageFragmentShaderResName,
                                                         secondStageVertexShaderResName == null ? "gaussian_vertex"
                                                                 : secondStageVertexShaderResName,
                                                         secondStageFragmentShaderResName == null ? "gaussian_fragment"
                                                                 : secondStageFragmentShaderResName);
    }

    public void setBlurSize(float blurSize) {
        setFloat(blurSize, mHorizontalBlurSizeUniform, mProgram);
        setFloat(blurSize, mVerticalBlurSizeUniform, mSecondProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(6.f, 1.f, 2.f, 10.f, ImageFilter.BLURSIZE, true));
            mArtFilterInfo = new ArtFilterInfo("Gaussian blur", progressInfo);
        }
        return mArtFilterInfo;
    }
}
