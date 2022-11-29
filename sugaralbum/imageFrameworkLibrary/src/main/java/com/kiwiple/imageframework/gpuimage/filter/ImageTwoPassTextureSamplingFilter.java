
package com.kiwiple.imageframework.gpuimage.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ShaderUtils.Size;

/**
 * 처리 영역을 sampling할 때 texel 간격을 변경하기 위한 class 
 */
public class ImageTwoPassTextureSamplingFilter extends ImageTwoPassFilter {
    private int mVerticalPassTexelWidthOffsetUniform;
    private int mVerticalPassTexelHeightOffsetUniform;
    private int mHorizontalPassTexelWidthOffsetUniform;
    private int mHorizontalPassTexelHeightOffsetUniform;

    private float mVerticalPassTexelWidthOffset = 0.0f;
    private float mVerticalPassTexelHeightOffset = 0.0f;
    private float mHorizontalPassTexelWidthOffset = 0.0f;
    private float mHorizontalPassTexelHeightOffset = 0.0f;

    @Override
    public void initWithFirstStageVertexShaderFromResource(Context context,
            String firstStageVertexShaderResName, String firstStageFragmentShaderResName,
            String secondStageVertexShaderResName, String secondStageFragmentShaderResName) {
        super.initWithFirstStageVertexShaderFromResource(context, firstStageVertexShaderResName,
                                                         firstStageFragmentShaderResName,
                                                         secondStageVertexShaderResName,
                                                         secondStageFragmentShaderResName);

        mVerticalPassTexelWidthOffsetUniform = GLES20.glGetUniformLocation(mProgram,
                                                                           "texelWidthOffset");
        mVerticalPassTexelHeightOffsetUniform = GLES20.glGetUniformLocation(mProgram,
                                                                            "texelHeightOffset");

        mHorizontalPassTexelWidthOffsetUniform = GLES20.glGetUniformLocation(mSecondProgram,
                                                                             "texelWidthOffset");
        mHorizontalPassTexelHeightOffsetUniform = GLES20.glGetUniformLocation(mSecondProgram,
                                                                              "texelHeightOffset");
    }

    @Override
    public void setUniformsForProgramAtIndex(int programIndex) {
        if(programIndex == 0) {
            GLES20.glUniform1f(mVerticalPassTexelWidthOffsetUniform, mVerticalPassTexelWidthOffset);
            GLES20.glUniform1f(mVerticalPassTexelHeightOffsetUniform,
                               mVerticalPassTexelHeightOffset);
        } else {
            GLES20.glUniform1f(mHorizontalPassTexelWidthOffsetUniform,
                               mHorizontalPassTexelWidthOffset);
            GLES20.glUniform1f(mHorizontalPassTexelHeightOffsetUniform,
                               mHorizontalPassTexelHeightOffset);
        }
    }

    @Override
    public void setupFilterForSize(Size filterFrameSize) {
        mVerticalPassTexelWidthOffset = 0.0f;
        mVerticalPassTexelHeightOffset = 1.0f / filterFrameSize.height;
        mHorizontalPassTexelWidthOffset = 1.0f / filterFrameSize.width;
        mHorizontalPassTexelHeightOffset = 0.0f;
    }
}
