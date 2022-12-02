
package com.kiwiple.imageframework.gpuimage.filter.effect;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

/**
 * [U+Camera>겔러리>편집>성형]의 갸름하게 기능을 위해 추가
 */
public class ImageDirectionalShiftFilterL extends ImageFilter {
    private int mScaleUniform;
    private int mDirectionXUniform;
    private int mDirectionYUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "directional_shift_fragment");
        mScaleUniform = GLES20.glGetUniformLocation(mProgram, "scale");
        mDirectionXUniform = GLES20.glGetUniformLocation(mProgram, "directionX");
        mDirectionYUniform = GLES20.glGetUniformLocation(mProgram, "directionY");

        setScale(0.5f);
        setDirection(0.5f, 0.5f);
    }

    public void setScale(float newValue) {
        setFloat(newValue, mScaleUniform, mProgram);
    }

    public void setDirection(float f, float g) {
        setFloat(f, mDirectionXUniform, mProgram);
        setFloat(g, mDirectionYUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(1.f, 0.f, 0.5f, 10.f, "Scale"));
            progressInfo.add(new ProgressInfo(1.f, 0.f, 0.5f, 10.f, "DirectionX"));
            progressInfo.add(new ProgressInfo(1.f, 0.f, 0.5f, 10.f, "DirectionY"));
            mArtFilterInfo = new ArtFilterInfo("Directional shift", progressInfo);
        }
        return mArtFilterInfo;
    }
}
