
package com.kiwiple.imageframework.gpuimage.filter.effect;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.Size;
import com.kiwiple.imageframework.gpuimage.filter.ImageTwoInputFilter;
import com.kiwiple.imageframework.gpuimage.source.ImagePicture;

public class ImageMosaicFilter extends ImageTwoInputFilter {
    private int mInputTileSizeUniform;
    private int mDisplayTileSizeUniform;
    private int mNumTilesUniform;
    private int mColorOnUniform;

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "mosaic_fragment");

        mInputTileSizeUniform = GLES20.glGetUniformLocation(mProgram, "inputTileSize");
        mDisplayTileSizeUniform = GLES20.glGetUniformLocation(mProgram, "displayTileSize");
        mNumTilesUniform = GLES20.glGetUniformLocation(mProgram, "numTiles");
        mColorOnUniform = GLES20.glGetUniformLocation(mProgram, "colorOn");

        Size its = new Size(0.125f, 0.125f);
        Size dts = new Size(0.025f, 0.025f);

        setInputTileSize(its);
        setDisplayTileSize(dts);
        setNumTiles(64.f);
        setColorOn(true);
    }

    public void setColorOn(boolean yes) {
        GLES20.glUniform1i(mColorOnUniform, yes ? 1 : 0);
    }

    public void setNumTiles(float numTiles) {
        setFloat(numTiles, mNumTilesUniform, mProgram);
    }

    public void setInputTileSize(Size newTileSize) {
        Size inputTileSize = new Size();
        if(newTileSize.width > 1.0f) {
            inputTileSize.width = 1.0f;
        }
        if(newTileSize.height > 1.0f) {
            inputTileSize.height = 1.0f;
        }
        if(newTileSize.width < 0.0f) {
            inputTileSize.width = 0.0f;
        }
        if(newTileSize.height < 0.0f) {
            inputTileSize.height = 0.0f;
        }

        inputTileSize = newTileSize;

        setSize(inputTileSize, mInputTileSizeUniform, mProgram);
    }

    public void setDisplayTileSize(Size newTileSize) {
        Size displayTileSize = new Size();
        if(newTileSize.width > 1.0f) {
            displayTileSize.width = 1.0f;
        }
        if(newTileSize.height > 1.0f) {
            displayTileSize.height = 1.0f;
        }
        if(newTileSize.width < 0.0f) {
            displayTileSize.width = 0.0f;
        }
        if(newTileSize.height < 0.0f) {
            displayTileSize.height = 0.0f;
        }

        displayTileSize = newTileSize;

        setSize(displayTileSize, mDisplayTileSizeUniform, mProgram);
    }

    private ImagePicture imagePicture = new ImagePicture();

    public void setTileSet(Context context, Bitmap tileSet, int width, int height) {
        imagePicture.setOverrideSize(width, height);
        imagePicture.initWithImage(context, tileSet, true);
        imagePicture.addTarget(this);
        imagePicture.processImage();
        if(tileSet != null && !tileSet.isRecycled()) {
            tileSet.recycle();
        }
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(.050f, .001f, .025f, 1000.f, "Output tile size"));
            mArtFilterInfo = new ArtFilterInfo("Mosaic", progressInfo);
        }
        return mArtFilterInfo;
    }
}
