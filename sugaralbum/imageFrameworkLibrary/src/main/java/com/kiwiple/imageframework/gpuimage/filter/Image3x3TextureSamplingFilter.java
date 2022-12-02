
package com.kiwiple.imageframework.gpuimage.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.Size;

public class Image3x3TextureSamplingFilter extends ImageFilter {
    private int mTexelWidthUniform;
    private int mTexelHeightUniform;

    private float mTexelWidth;
    private float mTexelHeight;

    private float mWeight;

    @Override
    public void initWithFragmentShaderFromResource(Context context, String fragmentShaderResName) {
        super.initWithVertexShaderFromResource(context, "nearby_texel_sampling_vertex",
                                               fragmentShaderResName);

        mWeight = ArtFilterUtils.getWeightedParam(getOutputWidth(), getOutputHeight(), 1.f);

        mTexelWidthUniform = GLES20.glGetUniformLocation(mProgram, "texelWidth");
        mTexelHeightUniform = GLES20.glGetUniformLocation(mProgram, "texelHeight");
    }

    @Override
    public void setupFilterForSize(Size filterFrameSize) {
        mTexelWidth = 1.f / filterFrameSize.width;
        mTexelHeight = 1.f / filterFrameSize.height;
        GLES20.glUseProgram(mProgram);
        GLES20.glUniform1f(mTexelHeightUniform, mTexelHeight * mWeight);
        GLES20.glUniform1f(mTexelWidthUniform, mTexelWidth * mWeight);
    }

    public void setWeight(float newValue) {
        mWeight = newValue;
    }
}
