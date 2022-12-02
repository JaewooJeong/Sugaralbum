
package com.kiwiple.imageframework.gpuimage.filter.colorprocessing;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

public class ImageChromaKeyFilter extends ImageFilter {
    private int mThresholdSensitivityUniform;
    private int mSmoothingUniform;
    private int mColorToReplaceUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "chromakey_fragment");

        mThresholdSensitivityUniform = GLES20.glGetUniformLocation(mProgram, "thresholdSensitivity");
        if(mThresholdSensitivityUniform != -1)
            setThresholdSensitivity(0.4f);

        mSmoothingUniform = GLES20.glGetUniformLocation(mProgram, "smoothing");
        if(mSmoothingUniform != -1)
            setSmoothing(0.1f);

        mColorToReplaceUniform = GLES20.glGetUniformLocation(mProgram, "colorToReplace");
        if(mColorToReplaceUniform != -1)
            setColorToReplaceRed(0.0f, 0.0f, 1.0f);
    }

    public void setColorToReplaceRed(float redComponent, float greenComponent, float blueComponent) {
        Vector3 colorToReplace = new Vector3(redComponent, greenComponent, blueComponent);
        setVec3(colorToReplace, mColorToReplaceUniform, mProgram);
    }

    public void setThresholdSensitivity(float newValue) {
        setFloat(newValue, mThresholdSensitivityUniform, mProgram);
    }

    public void setSmoothing(float newValue) {
        setFloat(newValue, mThresholdSensitivityUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(2);
            progressInfo.add(new ProgressInfo(1.f, 0.4f, .0f, 100.f, "Threshold sensitivity"));
            progressInfo.add(new ProgressInfo(1.f, 0.1f, .0f, 100.f, "Smoothing"));
            mArtFilterInfo = new ArtFilterInfo("ChromaKey", progressInfo);
        }
        return mArtFilterInfo;
    }
}
