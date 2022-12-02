
package com.kiwiple.imageframework.gpuimage.filter.imageprocessing;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoPassTextureSamplingFilter;

public class ImageSmartBlurFilter extends ImageTwoPassTextureSamplingFilter {
    private int mHorizontalBlurSizeUniform;
    private int mVerticalBlurSizeUniform;
    private int mHorizontalThresholdUniform;
    private int mVerticalThresholdUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        this.initWithFirstStageVertexShaderFromResource(context, null, null, null, null);

        mHorizontalBlurSizeUniform = GLES20.glGetUniformLocation(mProgram, "blurSize");
        mVerticalBlurSizeUniform = GLES20.glGetUniformLocation(mSecondProgram, "blurSize");
        mHorizontalThresholdUniform = GLES20.glGetUniformLocation(mProgram, "threshold");
        mVerticalThresholdUniform = GLES20.glGetUniformLocation(mSecondProgram, "threshold");

        setBlurSize(ArtFilterUtils.getWeightedParam(getOutputWidth(), getOutputHeight(), 1.f));
        setThreshold(0.25f);
    }

    @Override
    public void initWithFirstStageVertexShaderFromResource(Context context,
            String firstStageVertexShaderResName, String firstStageFragmentShaderResName,
            String secondStageVertexShaderResName, String secondStageFragmentShaderResName) {
        super.initWithFirstStageVertexShaderFromResource(context,
                                                         firstStageVertexShaderResName == null ? "gaussian_vertex"
                                                                 : firstStageVertexShaderResName,
                                                         firstStageFragmentShaderResName == null ? "smart_blur_fragment"
                                                                 : firstStageFragmentShaderResName,
                                                         secondStageVertexShaderResName == null ? "gaussian_vertex"
                                                                 : secondStageVertexShaderResName,
                                                         secondStageFragmentShaderResName == null ? "smart_blur_fragment"
                                                                 : secondStageFragmentShaderResName);
    }

    public void setBlurSize(float blurSize) {
        setFloat(blurSize, mHorizontalBlurSizeUniform, mProgram);
        setFloat(blurSize, mVerticalBlurSizeUniform, mSecondProgram);
    }

    public void setThreshold(float threshold) {
        setFloat(threshold, mHorizontalThresholdUniform, mProgram);
        setFloat(threshold, mVerticalThresholdUniform, mSecondProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(2);
            progressInfo.add(new ProgressInfo(6.f, 0.f, .4f, 10.f, ImageFilter.BLURSIZE, true));
            progressInfo.add(new ProgressInfo(1.f, 0.f, 0.1f, 100.f, ImageFilter.THRESHOLD, true));
            mArtFilterInfo = new ArtFilterInfo("Smart blur", progressInfo);
        }
        return mArtFilterInfo;
    }
}
