
package com.kiwiple.imageframework.gpuimage.filter.effect;

import java.nio.FloatBuffer;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoInputFilter;

public class ImageMaskFilter extends ImageTwoInputFilter {
    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "mask_shader");
    }

    @Override
    public void renderToTextureWithVertices(FloatBuffer vertices, FloatBuffer textureCoordinates,
            final int sourceTexture) {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        super.renderToTextureWithVertices(vertices, textureCoordinates, sourceTexture);
        GLES20.glDisable(GLES20.GL_BLEND);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            mArtFilterInfo = new ArtFilterInfo("Mask", null);
        }
        return mArtFilterInfo;
    }
}
