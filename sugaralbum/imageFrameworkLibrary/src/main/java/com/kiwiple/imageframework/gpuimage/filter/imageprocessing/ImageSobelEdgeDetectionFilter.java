
package com.kiwiple.imageframework.gpuimage.filter.imageprocessing;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.Size;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoPassFilter;

public class ImageSobelEdgeDetectionFilter extends ImageTwoPassFilter {
    private int mTexelWidthUniform;
    private int mTexelHeightUniform;

    private float mTexelWidth;
    private float mTexelHeight;

    private float mWeight;

    private boolean mHasOverriddenImageSizeFactor;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        initWithFragmentShaderFromResource(context, "sobel_edge_detection_fragment");
    }

    @Override
    public void initWithFragmentShaderFromResource(Context context, String fragmentShaderResName) {
        super.initWithFirstStageVertexShaderFromResource(context, "vertex", "luminance_fragment",
                                                         "nearby_texel_sampling_vertex",
                                                         fragmentShaderResName);

        mHasOverriddenImageSizeFactor = false;

        mWeight = 1.f;

        mTexelWidthUniform = GLES20.glGetUniformLocation(mSecondProgram, "texelWidth");
        mTexelHeightUniform = GLES20.glGetUniformLocation(mSecondProgram, "texelHeight");
    }

    @Override
    public void setupFilterForSize(Size filterFrameSize) {
        if(!mHasOverriddenImageSizeFactor) {
            mTexelWidth = 1.f / (filterFrameSize.width + 500);
            mTexelHeight = 1.f / (filterFrameSize.height + 500);
        }
        GLES20.glUseProgram(mSecondProgram);
        GLES20.glUniform1f(mTexelHeightUniform, mTexelHeight * mWeight);
        GLES20.glUniform1f(mTexelWidthUniform, mTexelWidth * mWeight);
        GLES20.glUseProgram(mProgram);
    }

    public void setWeight(float newValue) {
        mWeight = newValue;
    }

    public void setTexelWidth(float newValue) {
        mHasOverriddenImageSizeFactor = true;
        mTexelWidth = newValue;
    }

    public void setTexelHeight(float newValue) {
        mHasOverriddenImageSizeFactor = true;
        mTexelHeight = newValue;
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(2.2f, .2f, .8f, 50.f, ImageFilter.LINEWIDTH, true));
            mArtFilterInfo = new ArtFilterInfo("Sobel edge detection", progressInfo);
        }
        return mArtFilterInfo;
    }
}
