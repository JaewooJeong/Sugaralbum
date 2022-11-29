
package com.kiwiple.imageframework.gpuimage.filter.colorprocessing;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

/**
 * [U+Camera>겔러리>편집>성형]의 색감보정 기능을 위해 추가
 */
public class ImageColorTemperatureFilter extends ImageFilter {
    private int mScaleAdjustUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "color_temperature_fragment");

        mScaleAdjustUniform = GLES20.glGetUniformLocation(mProgram, "scale");
        if(mScaleAdjustUniform != -1)
            setTemperature(0.f);
    }

    public void setTemperature(float scale) {
        setFloat(scale, mScaleAdjustUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(.25f, -.25f, 0.f, 100.f, "Scale"));
            mArtFilterInfo = new ArtFilterInfo("Color temperature", progressInfo);
        }
        return mArtFilterInfo;
    }
}
