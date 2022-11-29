
package com.kiwiple.imageframework.gpuimage.filter.blends;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoInputFilter;

public class ImageOpacityMaskBlendFilter extends ImageTwoInputFilter {
    private int mOpacity;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "opacity_mask_blend_fragment");

        mOpacity = GLES20.glGetUniformLocation(mProgram, "mixturePercent");
        if(mOpacity != -1)
            setOpacity(0.5f);
    }

    public void setOpacity(float newValue) {
        setFloat(newValue, mOpacity, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(1.f, 0.5f, .0f, 100.f, "Opacity"));
            mArtFilterInfo = new ArtFilterInfo("Opacity mask blend", progressInfo);
        }
        return mArtFilterInfo;
    }
}
