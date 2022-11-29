
package com.kiwiple.imageframework.gpuimage.source;

import android.content.Context;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ImageOutput;
import com.kiwiple.imageframework.gpuimage.ShaderUtils;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.ImageInput;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.Size;

public class ImagePicture extends ImageOutput {
    /**
     * 이미지 크기
     */
    private Size mPixelSizeOfImage = new Size();
    private boolean mHasProcessedImage;
    private Size mOverrideSize = new Size();

    public void initWithImage(Context context, final Bitmap bmp) {
        initWithImage(context, bmp, false);
    }

    public void initWithImage(Context context, final Bitmap bmp, boolean overrideInputSize) {
        super.init(context);
        mHasProcessedImage = false;

        // TODO: Dispatch this whole thing asynchronously to move image loading off main thread
        if(!overrideInputSize) {
            mPixelSizeOfImage.width = bmp.getWidth();
            mPixelSizeOfImage.height = bmp.getHeight();
        } else {
            mPixelSizeOfImage.width = mOverrideSize.width;
            mPixelSizeOfImage.height = mOverrideSize.height;
        }

        Size pixelSizeToUseForTexture = mPixelSizeOfImage;

        Boolean shouldRedrawUsingCoreGraphics = false;

        // For now, deal with images larger than the maximum texture size by resizing to be within
        // that limit
        Size scaledImageSizeToFitOnGPU = ShaderUtils.sizeThatFitsWithinATextureForSize(mPixelSizeOfImage);
        if(!(scaledImageSizeToFitOnGPU.equals(mPixelSizeOfImage))) {
            mPixelSizeOfImage = scaledImageSizeToFitOnGPU;
            pixelSizeToUseForTexture = mPixelSizeOfImage;
            shouldRedrawUsingCoreGraphics = true;
        }

        Bitmap newBmp = null;

        if(shouldRedrawUsingCoreGraphics) {
            newBmp = Bitmap.createScaledBitmap(bmp, (int)pixelSizeToUseForTexture.width,
                                               (int)pixelSizeToUseForTexture.height, false);
        } else {
            newBmp = bmp;
        }

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOutputTexture[0]);
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, newBmp, 0);

        if(!newBmp.equals(bmp)) {
            newBmp.recycle();
        }
    }

    public void setOverrideSize(int width, int height) {
        mOverrideSize.width = width;
        mOverrideSize.height = height;
    }

    public static double logB(double x) {
        return Math.log(x) / Math.log(2);
    }

    @Override
    public void removeAllTargets() {
        super.removeAllTargets();
        mHasProcessedImage = false;
    }

    /**
     * rendering 시작
     */
    public void processImage() {
        mHasProcessedImage = true;
        for(ImageInput currentTarget : mTargets) {
            int indexOfObject = mTargets.indexOf(currentTarget);
            int textureIndexOfTarget = mTargetTextureIndices.get(indexOfObject);

            currentTarget.setInputSize(mPixelSizeOfImage, textureIndexOfTarget);
            currentTarget.newFrameReadyAtTime(0, textureIndexOfTarget);
        }
    }

    @Override
    public void addTarget(ImageInput newTarget, int textureLocation) {
        super.addTarget(newTarget, textureLocation);
        if(mHasProcessedImage) {
            newTarget.setInputSize(mPixelSizeOfImage, textureLocation);
            newTarget.newFrameReadyAtTime(0, textureLocation);
        }
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        return null;
    }

    @Override
    public ArtFilterInfo getSecondFilterInfo() {
        return null;
    }

    @Override
    public void newFrameReadyAtTime(int frameTime, int textureIndex) {
    }

    @Override
    public void setInputTexture(int newInputTexture, int textureIndex) {
    }

    @Override
    public int nextAvailableTextureIndex() {
        return 0;
    }

    @Override
    public void setInputSize(Size newSize, int textureIndex) {
    }

    @Override
    public Size maximumOutputSize() {
        return null;
    }

    @Override
    public void endProcessing() {
        // TODO Auto-generated method stub
    }

    @Override
    public float getOutputWidth() {
        return 0;
    }

    @Override
    public float getOutputHeight() {
        return 0;
    }
}
