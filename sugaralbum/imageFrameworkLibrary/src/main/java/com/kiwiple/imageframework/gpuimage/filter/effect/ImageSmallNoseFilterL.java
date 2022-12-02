
package com.kiwiple.imageframework.gpuimage.filter.effect;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

/**
 * [U+Camera>겔러리>편집>성형]의 갸름하게 기능을 위해 추가. 현재 사용하지 않음
 */
public class ImageSmallNoseFilterL extends ImageFilter {
    private int mScaleUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "small_nose_fragment");
        mScaleUniform = GLES20.glGetUniformLocation(mProgram, "scale");

        setScale(0.5f);
    }

    public void setScale(float newValue) {
        setFloat(newValue, mScaleUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(1.f, 0.f, 0.5f, 10.f, "Scale"));
            mArtFilterInfo = new ArtFilterInfo("Small nose", progressInfo);
        }
        return mArtFilterInfo;
    }
}
