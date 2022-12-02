
package com.kiwiple.imageframework.gpuimage.filter.effect;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.Size;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

public class ImagePixellateFilter extends ImageFilter {
    private int mFractionalWidthOfAPixelUniform;
    private int mAspectRatioUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        initWithFragmentShaderFromResource(context, "pixellation_fragment");
    }

    @Override
    public void initWithFragmentShaderFromResource(Context context, String fragmentShaderResName) {
        super.initWithFragmentShaderFromResource(context, fragmentShaderResName);
        mFractionalWidthOfAPixelUniform = GLES20.glGetUniformLocation(mProgram,
                                                                      "fractionalWidthOfPixel");
        mAspectRatioUniform = GLES20.glGetUniformLocation(mProgram, "aspectRatio");
    }

    @Override
    public void setInputSize(Size newSize, int textureIndex) {
        Size oldInputSize = mInputTextureSize;
        super.setInputSize(newSize, textureIndex);

        if(!oldInputSize.equals(mInputTextureSize) && !newSize.equals(new Size(0.f, 0.f))) {
            setAspectRatio(mInputTextureSize.height / mInputTextureSize.width);
        }
    }

    // TODO: mInputTextureSize always 0
    public void setFractionalWidthOfAPixel(float newValue) {
        float singlePixelSpacing;
        float fractionalWidthOfAPixel;
        if(mInputTextureSize.width != 0.0) {
            singlePixelSpacing = 1.0f / mInputTextureSize.width;
        } else {
            singlePixelSpacing = 1.0f / 2048.0f;
        }

        if(newValue < singlePixelSpacing) {
            fractionalWidthOfAPixel = singlePixelSpacing;
        } else {
            fractionalWidthOfAPixel = newValue;
        }

        setFloat(fractionalWidthOfAPixel, mFractionalWidthOfAPixelUniform, mProgram);
    }

    public void setAspectRatio(float newValue) {
        setFloat(newValue, mAspectRatioUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(.2f, .0f, .03f, 100.f, "Fractional width"));
            mArtFilterInfo = new ArtFilterInfo("Pixellate", progressInfo);
        }
        return mArtFilterInfo;
    }
}
