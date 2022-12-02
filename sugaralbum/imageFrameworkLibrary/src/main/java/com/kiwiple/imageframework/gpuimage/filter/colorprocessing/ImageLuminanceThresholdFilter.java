
package com.kiwiple.imageframework.gpuimage.filter.colorprocessing;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

public class ImageLuminanceThresholdFilter extends ImageFilter {
    private int mThresholdUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "luminance_threshold_fragment");

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
            progressInfo.add(new ProgressInfo(0.5f, 0.2f, 0.41f, 100.f, "Threshold"));
            mArtFilterInfo = new ArtFilterInfo("Luminance threshold", progressInfo);
        }
        return mArtFilterInfo;
    }
}
