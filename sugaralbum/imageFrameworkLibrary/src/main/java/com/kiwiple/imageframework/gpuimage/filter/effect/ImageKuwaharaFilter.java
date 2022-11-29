
package com.kiwiple.imageframework.gpuimage.filter.effect;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

public class ImageKuwaharaFilter extends ImageFilter {
    private int mRadiusUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "kuwahara_fragment");

        mRadiusUniform = GLES20.glGetUniformLocation(mProgram, "radius");
        if(mRadiusUniform != -1)
            setRadius(4);
    }

    public void setRadius(int newValue) {
        setInteger(newValue, mRadiusUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(8.f, 3.f, 5.f, 1.f, "Radius"));
            mArtFilterInfo = new ArtFilterInfo("Kuwahara", progressInfo);
        }
        return mArtFilterInfo;
    }
}
