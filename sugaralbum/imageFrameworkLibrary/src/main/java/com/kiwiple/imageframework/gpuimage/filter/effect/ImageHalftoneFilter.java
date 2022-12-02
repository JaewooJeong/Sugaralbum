
package com.kiwiple.imageframework.gpuimage.filter.effect;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;

public class ImageHalftoneFilter extends ImagePixellateFilter {
    private int mColorToReplaceUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "halftone_fragment");

        mColorToReplaceUniform = GLES20.glGetUniformLocation(mProgram, "colorToReplace");
        if(mColorToReplaceUniform != -1)
            setColorToReplaceRed(0.0f);

        setFractionalWidthOfAPixel(0.01f);
    }

    public void setColorToReplaceRed(float color) {
        Vector3 colorToReplace = new Vector3(color, color, color);
        setVec3(colorToReplace, mColorToReplaceUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(2);
            progressInfo.add(new ProgressInfo(.1f, .01f, .02f, 100.f, "Fractional width"));
            progressInfo.add(new ProgressInfo(1.f, .01f, .0f, 100.f, "Background color"));
            mArtFilterInfo = new ArtFilterInfo("A/F2", progressInfo);
        }
        return mArtFilterInfo;
    }
}
