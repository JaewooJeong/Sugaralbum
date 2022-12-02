
package com.kiwiple.imageframework.gpuimage.filter.effect;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.Image3x3TextureSamplingFilter;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

public class ImageToonFilter extends Image3x3TextureSamplingFilter {
    private int mThresholdUniform;
    private int mQuantizationLevelsUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "toon_fragment");

        mThresholdUniform = GLES20.glGetUniformLocation(mProgram, "threshold");
        mQuantizationLevelsUniform = GLES20.glGetUniformLocation(mProgram, "quantizationLevels");

        setThreshold(0.2f);
        setQuantizationLevels(10.f);
    }

    public void setThreshold(float newValue) {
        setFloat(newValue, mThresholdUniform, mProgram);
    }

    public void setQuantizationLevels(float newValue) {
        setFloat(newValue, mQuantizationLevelsUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(3);
            progressInfo.add(new ProgressInfo(0.55f, .02f, .47f, 100.f, "Threshold"));
            progressInfo.add(new ProgressInfo(15.f, 4.f, 10.f, 1.f, "Quantization levels"));
            progressInfo.add(new ProgressInfo(3.2f, 1.f, 2.1f, 50.f, ImageFilter.LINEWIDTH, true));
            mArtFilterInfo = new ArtFilterInfo("Toon", progressInfo);
        }
        return mArtFilterInfo;
    }
}
