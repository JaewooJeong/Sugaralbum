
package com.kiwiple.imageframework.gpuimage.filter.effect;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

public class ImageCrosshatchFilter extends ImageFilter {
    private int mCrossHatchSpacingUniform;
    private int mLineWidthUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        initWithFragmentShaderFromResource(context, "crosshatch_fragment");

        mCrossHatchSpacingUniform = GLES20.glGetUniformLocation(mProgram, "crossHatchSpacing");
        mLineWidthUniform = GLES20.glGetUniformLocation(mProgram, "lineWidth");
    }

    public void setCrossHatchSpacing(float newValue) {
        float singlePixelSpacing;
        float crossHatchSpacing;

        if(mInputTextureSize.width != 0.0) {
            singlePixelSpacing = 1.0f / mInputTextureSize.width;
        } else {
            singlePixelSpacing = 1.0f / 2048.0f;
        }

        if(newValue < singlePixelSpacing) {
            crossHatchSpacing = singlePixelSpacing;
        } else {
            crossHatchSpacing = newValue;
        }
        setFloat(crossHatchSpacing, mCrossHatchSpacingUniform, mProgram);
    }

    public void setLineWidth(float newValue) {
        setFloat(newValue, mLineWidthUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(2);
            progressInfo.add(new ProgressInfo(.07f, .01f, .02f, 100.f, "Line spacing"));
            progressInfo.add(new ProgressInfo(.009f, .003f, .006f, 1000.f, ImageFilter.LINEWIDTH,
                                              true));
            mArtFilterInfo = new ArtFilterInfo("Crosshatch", progressInfo);
        }
        return mArtFilterInfo;
    }
}
