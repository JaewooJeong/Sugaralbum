
package com.kiwiple.imageframework.gpuimage.filter.colorprocessing;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

public class ImageOpacityFilter extends ImageFilter {
    private int mOpacityUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "opacity_fragment");

        mOpacityUniform = GLES20.glGetUniformLocation(mProgram, "opacity");
        if(mOpacityUniform != -1)
            setOpacity(1.f);
    }

    public void setOpacity(float newValue) {
        setFloat(newValue, mOpacityUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(1.f, 0.f, 1.f, 100.f, "Opacity"));
            mArtFilterInfo = new ArtFilterInfo("Opacity", progressInfo);
        }
        return mArtFilterInfo;
    }
}
