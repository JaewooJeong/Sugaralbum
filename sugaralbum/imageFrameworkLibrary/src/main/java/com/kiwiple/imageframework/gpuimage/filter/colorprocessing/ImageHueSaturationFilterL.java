
package com.kiwiple.imageframework.gpuimage.filter.colorprocessing;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

/**
 * 이미지의 특정 색상만을 추출하기 위해서 추가. 필터카메라, 키위카메라에서 사용.
 */
public class ImageHueSaturationFilterL extends ImageFilter {
    private int mRedSaturationUniform;
    private int mYellowSaturationUniform;
    private int mGreenSaturationUniform;
    private int mCyanSaturationUniform;
    private int mBlueSaturationUniform;
    private int mMagentaSaturationUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "partially_grayscale_fragment");

        mRedSaturationUniform = GLES20.glGetUniformLocation(mProgram, "redSaturation");
        if(mRedSaturationUniform != -1)
            setRedSaturation(1.f);
        mYellowSaturationUniform = GLES20.glGetUniformLocation(mProgram, "yellowSaturation");
        if(mYellowSaturationUniform != -1)
            setYellowSaturation(1.f);
        mGreenSaturationUniform = GLES20.glGetUniformLocation(mProgram, "greenSaturation");
        if(mGreenSaturationUniform != -1)
            setGreenSaturation(1.f);
        mCyanSaturationUniform = GLES20.glGetUniformLocation(mProgram, "cyanSaturation");
        if(mCyanSaturationUniform != -1)
            setCyanSaturation(1.f);
        mBlueSaturationUniform = GLES20.glGetUniformLocation(mProgram, "blueSaturation");
        if(mBlueSaturationUniform != -1)
            setBlueSaturation(1.f);
        mMagentaSaturationUniform = GLES20.glGetUniformLocation(mProgram, "magentaSaturation");
        if(mMagentaSaturationUniform != -1)
            setMagentaSaturation(1.f);
    }

    public void setRedSaturation(float newValue) {
        setFloat(newValue, mRedSaturationUniform, mProgram);
    }

    public void setYellowSaturation(float newValue) {
        setFloat(newValue, mYellowSaturationUniform, mProgram);
    }

    public void setGreenSaturation(float newValue) {
        setFloat(newValue, mGreenSaturationUniform, mProgram);
    }

    public void setCyanSaturation(float newValue) {
        setFloat(newValue, mCyanSaturationUniform, mProgram);
    }

    public void setBlueSaturation(float newValue) {
        setFloat(newValue, mBlueSaturationUniform, mProgram);
    }

    public void setMagentaSaturation(float newValue) {
        setFloat(newValue, mMagentaSaturationUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(6);
            progressInfo.add(new ProgressInfo(2.f, 0.f, 1.f, 50.f, "Red"));
            progressInfo.add(new ProgressInfo(2.f, 0.f, 1.f, 50.f, "Yellow"));
            progressInfo.add(new ProgressInfo(2.f, 0.f, 1.f, 50.f, "Green"));
            progressInfo.add(new ProgressInfo(2.f, 0.f, 1.f, 50.f, "Cyan"));
            progressInfo.add(new ProgressInfo(2.f, 0.f, 1.f, 50.f, "Blue"));
            progressInfo.add(new ProgressInfo(2.f, 0.f, 1.f, 50.f, "Magenta"));
            mArtFilterInfo = new ArtFilterInfo("HueSaturation", progressInfo);
        }
        return mArtFilterInfo;
    }
}
