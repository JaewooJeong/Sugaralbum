
package com.kiwiple.imageframework.gpuimage.filter.custom;

import android.content.Context;

import com.kiwiple.imageframework.filter.CurvesPoint;
import com.kiwiple.imageframework.filter.Filter;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilterGroup;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageGrayscaleFilter;

/**
 * JNI로 구현된 필터를 OpenGL로 전환하기 위해 만든 메쏘드. 현재 개발 중단된 코드.
 */
public class RGBFilter extends ImageFilterGroup {

    public void initWithImage(Context context, final Filter filter) {
        if(filter.mBWMode) {
            ImageGrayscaleFilter imageGrayscaleFilter = new ImageGrayscaleFilter();
            imageGrayscaleFilter.init(context);
            addFilter(imageGrayscaleFilter);
        }
        if(!(filter.mAll == null && filter.mRed == null && filter.mGreen == null && filter.mBlue == null)
                && !(CurvesPoint.isIdentity(filter.mAll) && CurvesPoint.isIdentity(filter.mRed)
                        && CurvesPoint.isIdentity(filter.mGreen) && CurvesPoint.isIdentity(filter.mBlue))) {

        }
        if(filter.mSaturation <= 2.0f && filter.mSaturation >= .0f && filter.mSaturation != 1 /*
                                                                                               * 1이면
                                                                                               * 건너
                                                                                               * 뛰자~
                                                                                               */) {

        }
        if(filter.mBrightness >= -100 && filter.mBrightness <= 100 && filter.mBrightness != 0 /*
                                                                                               * 0이면
                                                                                               * 건너
                                                                                               * 뛰자~
                                                                                               */) {

        }
        if(filter.mContrast >= 0.5 && filter.mContrast <= 1.5 && filter.mContrast != 1/* 1이면 건너 뛰자~ */) {

        }
        if(filter.needVignette()) {

        }
        if(filter.needTexture()) {

        }
        if(filter.neetFrame()) {

        }
    }
}
