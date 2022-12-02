
package com.kiwiple.imageframework.gpuimage.filter;

import java.util.ArrayList;

import android.content.Context;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ImageOutput;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.ImageInput;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.Size;

/**
 * 여러 필터들의 효과를 조합하기 위한 필터.
 */
public class ImageFilterGroup extends ImageOutput implements ImageInput {
    /**
     * 필터 그룹이 포함하고 있는 필터 목록
     */
    private ArrayList<ImageOutput> filters = new ArrayList<ImageOutput>();
    /**
     * 마지막 필터
     */
    public ImageOutput terminalFilter;
    /**
     * 시작 필터 목록
     */
    public ArrayList<ImageInput> initialFilters = new ArrayList<ImageInput>();;
    private ImageFilter inputFilterToIgnoreForUpdates;

    @Override
    public void init(Context context) {
        super.init(context);

        filters.clear();
        deleteOutputTexture();
    }

    public void addFilter(ImageOutput newTarget) {
        filters.add(newTarget);
    }

    public ImageOutput filterAtIndex(int filterIndex) {
        return filters.get(filterIndex);
    }

    public int filterCount() {
        return filters.size();
    }

    @Override
    public void addTarget(ImageInput newTarget) {
        terminalFilter.addTarget(newTarget);
    }

    @Override
    public void addTarget(ImageInput newTarget, int textureLocation) {
        terminalFilter.addTarget(newTarget, textureLocation);
    }

    @Override
    public void removeAllTargets() {
        for(ImageOutput targetToRemove : filters) {
            targetToRemove.destroyAll();
        }
    }

    @Override
    public void newFrameReadyAtTime(final int frameTime, int textureIndex) {
        for(ImageInput currentFilter : initialFilters) {
            if(currentFilter != inputFilterToIgnoreForUpdates) {
                currentFilter.newFrameReadyAtTime(frameTime, textureIndex);
            }
        }
    }

    @Override
    public void setInputTexture(int newInputTexture, int textureIndex) {
        for(ImageInput currentFilter : initialFilters) {
            currentFilter.setInputTexture(newInputTexture, textureIndex);
        }
    }

    @Override
    public int nextAvailableTextureIndex() {
        return 0;
    }

    @Override
    public void setInputSize(Size newSize, int textureIndex) {
        for(ImageInput currentFilter : initialFilters) {
            currentFilter.setInputSize(newSize, textureIndex);
        }
    }

    @Override
    public void forceProcessingAtSize(Size frameSize) {
        for(ImageOutput currentTarget : filters) {
            currentTarget.forceProcessingAtSize(frameSize);
        }
    }

    @Override
    public Size maximumOutputSize() {
        return new Size(0, 0);
    }

    @Override
    public void endProcessing() {
        for(ImageInput currentFilter : initialFilters) {
            currentFilter.endProcessing();
        }
    }

    @Override
    public float getOutputWidth() {
        return terminalFilter.getOutputWidth();
    }

    @Override
    public float getOutputHeight() {
        return terminalFilter.getOutputHeight();
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        return null;
    }

    @Override
    public ArtFilterInfo getSecondFilterInfo() {
        return null;
    }
}
