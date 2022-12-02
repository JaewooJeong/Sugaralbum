
package com.kiwiple.imageframework.gpuimage.filter.blends;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoInputFilter;

/**
 * custom 필터 개발을 위해 추가. 여러 필터를 하나 shader code로 합쳐서 메모리 사용을 최소하 하기 위함.
 * LuminanceThreshold + Hue + MaskBlendL + Opacity
 */
public class ImagePaidFilter9BlendFilterL extends ImageTwoInputFilter {
    private int mThresholdUniform;
    private int mHueAdjustUniform;
    private int mOpacityUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "paid_filter_9_blend_fragment");

        mThresholdUniform = GLES20.glGetUniformLocation(mProgram, "threshold");
        if(mThresholdUniform != -1)
            setThreshold(0.5f);

        mHueAdjustUniform = GLES20.glGetUniformLocation(mProgram, "hueAdjust");
        if(mHueAdjustUniform != -1)
            setHue(90.f);

        mOpacityUniform = GLES20.glGetUniformLocation(mProgram, "opacity");
        if(mOpacityUniform != -1)
            setOpacity(1.f);
    }

    public void setThreshold(float newValue) {
        setFloat(newValue, mThresholdUniform, mProgram);
    }

    public void setHue(float newHue) {
        float hue = (float)((newHue % 360.0f) * Math.PI / 180.f);
        setFloat(hue, mHueAdjustUniform, mProgram);
    }

    public void setOpacity(float newValue) {
        setFloat(newValue, mOpacityUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(3);
            progressInfo.add(new ProgressInfo(0.5f, 0.2f, 0.41f, 100.f, "Threshold"));
            progressInfo.add(new ProgressInfo(360.f, 0.f, 90.f, 1.f, "Hue"));
            progressInfo.add(new ProgressInfo(1.f, 0.f, 1.f, 100.f, "Opacity"));
            mArtFilterInfo = new ArtFilterInfo("PaidFilter9 BlendFilter1L", progressInfo);
        }
        return mArtFilterInfo;
    }
}
