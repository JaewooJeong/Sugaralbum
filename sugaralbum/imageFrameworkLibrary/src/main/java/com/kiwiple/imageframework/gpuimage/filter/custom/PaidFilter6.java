
package com.kiwiple.imageframework.gpuimage.filter.custom;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageColorBurnBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageMultiplyBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageNormalBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageSoftLightBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageLevelsFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageSketchFilter;
import com.kiwiple.imageframework.gpuimage.source.ImagePicture;

public class PaidFilter6 extends ImageFilterGroup {
    private ImageLevelsFilter mImageLevelsFilter = new ImageLevelsFilter();
    private ImageColorBurnBlendFilter imageColorBurnBlendFilter = new ImageColorBurnBlendFilter();
    private ImageSketchFilter imageSketchFilter = new ImageSketchFilter();
    private ImageMultiplyBlendFilter imageMultiplyBlendFilter = new ImageMultiplyBlendFilter();
    private ImageSoftLightBlendFilter imageSoftLightBlendFilter = new ImageSoftLightBlendFilter();
    private ImageNormalBlendFilter imageNormalBlendFilter = new ImageNormalBlendFilter();
    private ImagePicture textureImage1 = new ImagePicture();
    private ImagePicture textureImage2 = new ImagePicture();
    private ImagePicture textureImage3 = new ImagePicture();

    private ArtFilterInfo mArtFilterInfo1;
    private ArtFilterInfo mArtFilterInfo2;

    public void initWithImage(Context context, final Bitmap texture1, final Bitmap texture2,
            final Bitmap texture3) {
        super.init(context);
        imageSketchFilter.init(context);
        imageSketchFilter.setWeight(0.5f);
        addFilter(imageSketchFilter);

        imageMultiplyBlendFilter.init(context);
        addFilter(imageMultiplyBlendFilter);

        imageSoftLightBlendFilter.init(context);
        addFilter(imageSoftLightBlendFilter);

        imageNormalBlendFilter.init(context);
        addFilter(imageNormalBlendFilter);

        mImageLevelsFilter.init(context);
        addFilter(mImageLevelsFilter);

        imageColorBurnBlendFilter.init(context);
        addFilter(imageColorBurnBlendFilter);

        // texture 1
        textureImage1.initWithImage(context, texture1);
        addFilter(textureImage1);
        textureImage1.addTarget(imageMultiplyBlendFilter, 1);
        textureImage1.processImage();

        // texture 2
        textureImage2.initWithImage(context, texture2);
        addFilter(textureImage2);
        textureImage2.addTarget(imageSoftLightBlendFilter, 1);
        textureImage2.processImage();

        // texture 3
        textureImage3.initWithImage(context, texture3);
        addFilter(textureImage3);
        textureImage3.addTarget(imageNormalBlendFilter, 1);
        textureImage3.processImage();

        // second original image
        // ImagePicture secondImage = new ImagePicture();
        // secondImage.initWithImage(context, bmp);
        // addFilter(secondImage);
        // secondImage.addTarget(imageColorBurnBlendFilter, 1);
        // secondImage.processImage();

        // start filtering
        imageSketchFilter.addTarget(mImageLevelsFilter);
        mImageLevelsFilter.addTarget(imageColorBurnBlendFilter);
        imageColorBurnBlendFilter.addTarget(imageMultiplyBlendFilter);
        imageMultiplyBlendFilter.addTarget(imageSoftLightBlendFilter);
        imageSoftLightBlendFilter.addTarget(imageNormalBlendFilter);

        initialFilters.clear();
        initialFilters.add(imageSketchFilter);
        terminalFilter = imageNormalBlendFilter;
    }

    private ImagePicture secondImage = new ImagePicture();

    public void addColorBurnBlendFilterTarget(Context context, Bitmap image) {
        secondImage.initWithImage(context, image);
        addFilter(secondImage);
        secondImage.addTarget(imageColorBurnBlendFilter, 1);
        secondImage.processImage();
    }

    public void setMin(float min) {
        mImageLevelsFilter.setMin(min, 1.0f, 1.0f, 0.0f, 1.0f);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo1 == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(.8f, .2f, .4f, 100.f, "Color level"));
            mArtFilterInfo1 = new ArtFilterInfo("Color pencil", progressInfo, "0900703806",
                                                "olleh_color_pencil", "item_color_pencil2");
        }
        return mArtFilterInfo1;
    }

    @Override
    public ArtFilterInfo getSecondFilterInfo() {
        if(mArtFilterInfo2 == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(1);
            progressInfo.add(new ProgressInfo(.8f, .2f, .6f, 100.f, "Color level"));
            mArtFilterInfo2 = new ArtFilterInfo("Color pencil", progressInfo, "0900703806",
                                                "olleh_color_pencil", "item_color_pencil2");
        }
        return mArtFilterInfo2;
    }
}
