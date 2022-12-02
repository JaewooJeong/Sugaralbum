
package com.kiwiple.imageframework.gpuimage.filter.effect;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.PointF;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

public class ImagePinchDistortionFilter extends ImageFilter {
    private int mRadiusUniform;
    private int mScaleUniform;
    private int mCenterUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "pinch_distortion_fragment");
        mRadiusUniform = GLES20.glGetUniformLocation(mProgram, "radius");
        mScaleUniform = GLES20.glGetUniformLocation(mProgram, "scale");
        mCenterUniform = GLES20.glGetUniformLocation(mProgram, "center");

        setRadius(1.f);
        setScale(0.5f);
        setCenter(new PointF(0.5f, 0.5f));
    }

    public void setRadius(float newValue) {
        setFloat(newValue, mRadiusUniform, mProgram);
    }

    public void setScale(float newValue) {
        setFloat(newValue, mScaleUniform, mProgram);
    }

    public void setCenter(PointF newValue) {
        setPoint(newValue, mCenterUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(1.f, -1.f, -.2f, 10.f, "Scale"));
            mArtFilterInfo = new ArtFilterInfo("A/F3", progressInfo);
        }
        return mArtFilterInfo;
    }
}
