
package com.kiwiple.imageframework.gpuimage.filter.imageprocessing;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

public class ImageThresholdEdgeDetectionFilter extends ImageSobelEdgeDetectionFilter {
    private int mThresholdUniform;

    @Override
    public void initWithFragmentShaderFromResource(Context context, String fragmentShaderResName) {
        super.initWithFragmentShaderFromResource(context, fragmentShaderResName);

        mThresholdUniform = GLES20.glGetUniformLocation(mSecondProgram, "threshold");
        if(mThresholdUniform != -1)
            setThreshold(0.9f);
    }

    @Override
    public void init(Context context) {
        initWithFragmentShaderFromResource(context, "threshold_edge_detection_fragment");
    }

    public void setThreshold(float newValue) {
        setFloat(newValue, mThresholdUniform, mSecondProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>();
        progressInfo.add(new ProgressInfo(1.f, .3f, .33f, 100.f, "Threshold"));
        progressInfo.add(new ProgressInfo(3.4f, .6f, 1.8f, 50.f, ImageFilter.LINEWIDTH, true));
        return new ArtFilterInfo("Threshold edge detection", progressInfo);
    }
}
