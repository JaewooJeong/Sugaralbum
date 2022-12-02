
package com.kiwiple.imageframework.gpuimage.filter.colorprocessing;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

public class ImageSaturationFilter extends ImageFilter {
    private int mSaturationUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "saturation_fragment");

        mSaturationUniform = GLES20.glGetUniformLocation(mProgram, "saturation");
        if(mSaturationUniform != -1)
            setSaturation(1.f);
    }

    public void setSaturation(float newValue) {
        setFloat(newValue, mSaturationUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(2.f, 0.f, 1.f, 50.f, "Saturation"));
            mArtFilterInfo = new ArtFilterInfo("Saturation", progressInfo);
        }
        return mArtFilterInfo;
    }
}
