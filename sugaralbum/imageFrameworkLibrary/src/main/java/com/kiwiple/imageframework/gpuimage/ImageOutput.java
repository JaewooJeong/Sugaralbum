
package com.kiwiple.imageframework.gpuimage;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ShaderUtils.ImageInput;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.Size;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

/**
 * 필터의 최상위 클래스로 텍스처를 초기화/해제하고 연결된 필터 목록을 관리한다.
 */
public abstract class ImageOutput implements ImageInput {
    public ArrayList<ImageInput> mTargets = new ArrayList<ImageInput>();
    public ArrayList<Integer> mTargetTextureIndices = new ArrayList<Integer>();

    public boolean mOverrideInputSize = false;
    public Size mForcedMaximumSize = new Size(0.f, 0.f);
    public Size mInputTextureSize = new Size(0.f, 0.f);
    public int[] mOutputTexture = new int[1];

    /**
     * 연결된 필터 목록 및 텍스처를 초기화 한다.
     */
    public void init(Context context) {
        mTargets.clear();
        mTargetTextureIndices.clear();

        initializeOutputTexture();
    }

    /**
     * 모든 리소스를 해제한다.
     */
    public void destroyAll() {
        removeAllTargets();
        deleteOutputTexture();
    }

    /**
     * target에 텍스처를 설정한다.
     */
    public void setInputTextureForTarget(ImageInput target, int inputTextureIndex) {
        target.setInputTexture(textureForOutput(), inputTextureIndex);
    }

    /**
     * 바인딩 된 텍스처를 반환한다.
     */
    public int textureForOutput() {
        return mOutputTexture[0];
    }

    /**
     * 텍스처 변경 사항을 업데이트한다.
     */
    public void notifyTargetsAboutNewOutputTexture() {
        for(ImageInput currentTarget : mTargets) {
            setInputTextureForTarget(currentTarget,
                                     mTargetTextureIndices.get(mTargets.indexOf(currentTarget)));
        }
    }

    /**
     * 다음 필터를 연결한다.
     */
    public void addTarget(ImageInput newTarget) {
        int nextAvailableTextureIndex = newTarget.nextAvailableTextureIndex();
        this.addTarget(newTarget, nextAvailableTextureIndex);
    }

    /**
     * 특정 위치에 필터를 연결한다.
     */
    public void addTarget(ImageInput newTarget, int textureLocation) {
        if(mTargets.contains(newTarget)) {
            return;
        }
        setInputTextureForTarget(newTarget, textureLocation);
        mTargets.add(newTarget);
        mTargetTextureIndices.add(textureLocation);
    }

    /**
     * 연결된 필터를 삭제한다.
     */
    public void removeTarget(ImageFilter targetToRemove) {
        if(!mTargets.contains(targetToRemove)) {
            return;
        }

        int indexOfObject = mTargets.indexOf(targetToRemove);
        int textureIndexOfTarget = mTargetTextureIndices.get(indexOfObject);

        targetToRemove.setInputSize(new Size(0.f, 0.f), textureIndexOfTarget);
        targetToRemove.setInputTexture(0, textureIndexOfTarget);

        mTargetTextureIndices.remove(indexOfObject);
        mTargets.remove(targetToRemove);
        targetToRemove.endProcessing();

    }

    /**
     * 연결된 모든 필터를 삭제한다.
     */
    public void removeAllTargets() {
        for(ImageInput targetToRemove : mTargets) {
            int textureIndexOfTarget = mTargetTextureIndices.get(mTargets.indexOf(targetToRemove));
            if(targetToRemove instanceof ImageOutput) {
                ((ImageOutput)targetToRemove).destroyAll();
            }
            targetToRemove.setInputSize(new Size(0.f, 0.f), textureIndexOfTarget);
            targetToRemove.setInputTexture(0, textureIndexOfTarget);
        }
        mTargets.clear();
        mTargetTextureIndices.clear();
    }

    /**
     * 텍스처를 초기화한다.
     */
    public void initializeOutputTexture() {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glGenTextures(1, mOutputTexture, 0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mOutputTexture[0]);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        // This is necessary for non-power-of-two textures
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                               GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                               GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    /**
     * 텍스처를 해제한다.
     */
    public void deleteOutputTexture() {
        if(mOutputTexture[0] != 0) {
            GLES20.glDeleteTextures(1, mOutputTexture, 0);
            mOutputTexture[0] = 0;
        }
    }

    public void forceProcessingAtSize(Size frameSize) {
    }

    /**
     * 필터 정보를 반환한다.
     */
    public abstract ArtFilterInfo getFilterInfo();

    /**
     * 두 번째 필터 정보를 반환한다.(일반적으로 필터 파라미터 값의 범위, 기본 값을 다르게 설정하기 위해서 사용한다.)
     */
    public abstract ArtFilterInfo getSecondFilterInfo();
}
