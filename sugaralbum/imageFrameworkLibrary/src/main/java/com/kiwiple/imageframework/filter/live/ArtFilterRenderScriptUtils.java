
package com.kiwiple.imageframework.filter.live;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import androidx.renderscript.Allocation;
import androidx.renderscript.Element;
import androidx.renderscript.Float2;
import androidx.renderscript.Float3;
import androidx.renderscript.RenderScript;
import androidx.renderscript.Type;

import com.kiwiple.imageframework.ScriptC_blend_alpha;
import com.kiwiple.imageframework.ScriptC_blend_alpha_mask;
import com.kiwiple.imageframework.ScriptC_blend_color_burn;
import com.kiwiple.imageframework.ScriptC_blend_color_darken;
import com.kiwiple.imageframework.ScriptC_blend_color_dodge;
import com.kiwiple.imageframework.ScriptC_blend_dissolve;
import com.kiwiple.imageframework.ScriptC_blend_luminance_threshold;
import com.kiwiple.imageframework.ScriptC_blend_mask;
import com.kiwiple.imageframework.ScriptC_blend_multiply;
import com.kiwiple.imageframework.ScriptC_blend_normal;
import com.kiwiple.imageframework.ScriptC_blend_opacity_mask;
import com.kiwiple.imageframework.ScriptC_blend_overlay;
import com.kiwiple.imageframework.ScriptC_blend_soft_light;
import com.kiwiple.imageframework.ScriptC_color_invert;
import com.kiwiple.imageframework.ScriptC_gray_scale;
import com.kiwiple.imageframework.ScriptC_halftone;
import com.kiwiple.imageframework.ScriptC_hue_saturation;
import com.kiwiple.imageframework.ScriptC_luminance_threshold;
import com.kiwiple.imageframework.ScriptC_opacity;
import com.kiwiple.imageframework.ScriptC_pinch_distortion;
import com.kiwiple.imageframework.ScriptC_pixellater;
import com.kiwiple.imageframework.ScriptC_posterize;
import com.kiwiple.imageframework.ScriptC_saturation;
import com.kiwiple.imageframework.ScriptC_stretch_outside;
import com.kiwiple.imageframework.filter.live.color.AdaptiveThreshold;
import com.kiwiple.imageframework.filter.live.color.Hue;
import com.kiwiple.imageframework.filter.live.color.Levels;
import com.kiwiple.imageframework.filter.live.custom.BrightToon;
import com.kiwiple.imageframework.filter.live.custom.ColorPencil;
import com.kiwiple.imageframework.filter.live.custom.Custom14;
import com.kiwiple.imageframework.filter.live.custom.Custom15;
import com.kiwiple.imageframework.filter.live.custom.Custom16;
import com.kiwiple.imageframework.filter.live.custom.Marshmallow;
import com.kiwiple.imageframework.filter.live.custom.MyCartoon;
import com.kiwiple.imageframework.filter.live.custom.OilPainting;
import com.kiwiple.imageframework.filter.live.custom.OilPastel;
import com.kiwiple.imageframework.filter.live.custom.OldPainting;
import com.kiwiple.imageframework.filter.live.custom.PostGray;
import com.kiwiple.imageframework.filter.live.custom.RainbowFilter;
import com.kiwiple.imageframework.filter.live.custom.Soft;
import com.kiwiple.imageframework.filter.live.custom.SoftToon;
import com.kiwiple.imageframework.filter.live.custom.UnsharpMaskPinchDistortion;
import com.kiwiple.imageframework.filter.live.custom.UnsharpMaskTiltShift;
import com.kiwiple.imageframework.filter.live.custom.UnsharpMaskTiltShiftCircle;
import com.kiwiple.imageframework.filter.live.custom.WashDrawing;
import com.kiwiple.imageframework.filter.live.effect.Kuwahara;
import com.kiwiple.imageframework.filter.live.effect.PaidFilter2;
import com.kiwiple.imageframework.filter.live.effect.SketchFilter;
import com.kiwiple.imageframework.filter.live.effect.ThresholdSketch;
import com.kiwiple.imageframework.filter.live.effect.Toon;
import com.kiwiple.imageframework.filter.live.imageprocessing.BoxBlur;
import com.kiwiple.imageframework.filter.live.imageprocessing.Dilation;
import com.kiwiple.imageframework.filter.live.imageprocessing.Erosion;
import com.kiwiple.imageframework.filter.live.imageprocessing.GaussianBlur;
import com.kiwiple.imageframework.filter.live.imageprocessing.HighPass;
import com.kiwiple.imageframework.filter.live.imageprocessing.Opening;
import com.kiwiple.imageframework.filter.live.imageprocessing.RGBDilation;
import com.kiwiple.imageframework.filter.live.imageprocessing.RGBErosion;
import com.kiwiple.imageframework.filter.live.imageprocessing.RGBOpening;
import com.kiwiple.imageframework.filter.live.imageprocessing.SmartBlur;
import com.kiwiple.imageframework.filter.live.imageprocessing.SobelEdgeFilter;
import com.kiwiple.imageframework.filter.live.imageprocessing.ThresholdEdgeDetection;
import com.kiwiple.imageframework.filter.live.imageprocessing.TiltShift;
import com.kiwiple.imageframework.filter.live.imageprocessing.TiltShiftCircle;
import com.kiwiple.imageframework.filter.live.imageprocessing.UnsharpMask;
import com.kiwiple.imageframework.gpuimage.ArtFilterUtils;
import com.kiwiple.imageframework.gpuimage.ImageOutput;
import com.kiwiple.imageframework.util.FileUtils;

public class ArtFilterRenderScriptUtils {
    public static final boolean LITE_VERSION = true;

    private static RenderScript mRS;

    /**
     * blend 필터 정상 동작을 확인해보기 위한 테스트 이미지
     */
    private static Allocation mTempSubImages;
    /**
     * 원본 이미지 복사
     */
    private static Allocation mAllocationSub;
    /**
     * 각 필터에 필요한 텍스처 이미지
     */
    private static Allocation[] mSubImages = new Allocation[3];
    /**
     * 필터 이름
     */
    private static String mFilterName;
    /**
     * 필터 파라미터
     */
    private static float[] mConvertedParams;

    public static void init(RenderScript rs, Context context) {
        mRS = rs;
    }

    /**
     * Allocation을 초기화한다.
     */
    public static void reset(int width, int height) {
        if(mAllocationSub != null) {
            mAllocationSub.destroy();
        }
        cleanSubImages();
        Type.Builder tb = new Type.Builder(mRS, Element.RGBA_8888(mRS));
        tb.setX(width);
        tb.setY(height);
        mAllocationSub = Allocation.createTyped(mRS, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                                RsYuv.ALLOCATION_USAGE_FULL);
        mSubImages[0] = Allocation.createTyped(mRS, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                               RsYuv.ALLOCATION_USAGE_FULL);
        mSubImages[1] = Allocation.createTyped(mRS, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                               RsYuv.ALLOCATION_USAGE_FULL);
        mSubImages[2] = Allocation.createTyped(mRS, tb.create(), RsYuv.MIPMAPCONTROL_FULL,
                                               RsYuv.ALLOCATION_USAGE_FULL);
    }

    /**
     * 텍스처 이미지를 불러오고, 필터를 초기화한다.
     */
    public static void initFilter(Context context, String filterName, float[] params, int width,
            int height) {
        mFilterName = filterName;

        // Setting Texture
        initSubImages(context, width, height, filterName);
        // Convert Params
        mConvertedParams = ArtFilterUtils.getConvertParams(filterName, params, (width + height) / 2);

        initFilter(context, width, height);
    }

    public static boolean isEqaulFilterName(ImageOutput filter, String filterName) {
        return filter.getFilterInfo().filterName.equals(filterName);
    }

    /**
     * 필터를 초기화 하고, 파라미터를 세팅한다.
     */
    private static void initFilter(Context context, int width, int height) {
        if(isEqaulFilterName(ArtFilterUtils.sImageLuminanceThresholdFilter, mFilterName)) {
            mLuminanceThreshold = new ScriptC_luminance_threshold(mRS);
            mLuminanceThreshold.set_threshold(mConvertedParams[0]);
        } else if(isEqaulFilterName(ArtFilterUtils.sImagePosterizeFilter, mFilterName)) {
            mPosterize = new ScriptC_posterize(mRS);
            mPosterize.set_colorLevels(mConvertedParams[0]);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageMaskBlendFilterL, mFilterName)) {
            mMaskBlend = new ScriptC_blend_mask(mRS);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageOverlayBlendFilter, mFilterName)) {
            mOverlayBlend = new ScriptC_blend_overlay(mRS);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageGaussianBlurFilter, mFilterName)) {
            mGaussianBlur = new GaussianBlur(mRS, width, height);
            mGaussianBlur.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageBoxBlurFilter, mFilterName)) {
            mBoxBlur = new BoxBlur(mRS, width, height);
            mBoxBlur.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageSaturationFilter, mFilterName)) {
            mSaturation = new ScriptC_saturation(mRS);
            mSaturation.set_saturation(mConvertedParams[0]);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageOpacityFilter, mFilterName)) {
            mOpacity = new ScriptC_opacity(mRS);
            mOpacity.set_alpha(mConvertedParams[0]);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageLevelsFilter, mFilterName)) {
            mLevelsFilter = new Levels(mRS);
            mLevelsFilter.setParams(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageAlphaBlendFilter, mFilterName)) {
            mAlphaBlend = new ScriptC_blend_alpha(mRS);
            mAlphaBlend.set_mixturePercent(mConvertedParams[0]);
        } else if(isEqaulFilterName(ArtFilterUtils.sImagePinchDistortionFilter, mFilterName)) {
            mPinchDistortion = new ScriptC_pinch_distortion(mRS);
            mPinchDistortion.set_center(new Float2(width / 2.f, height / 2.f));
            mPinchDistortion.set_radius(10.f);
            mPinchDistortion.set_scale(mConvertedParams[0]);
            mPinchDistortion.set_width(width);
            mPinchDistortion.set_height(height);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageKuwaharaFilter, mFilterName)) {
            mKuwahara = new Kuwahara(mRS, width, height);
            mKuwahara.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageAlphaMaskBlendFilterL, mFilterName)) {
            mAlphaMaskBlend = new ScriptC_blend_alpha_mask(mRS);
            mAlphaMaskBlend.set_mixturePercent(mConvertedParams[0]);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageColorBurnBlendFilter, mFilterName)) {
            mColorBurnBlend = new ScriptC_blend_color_burn(mRS);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageLuminanceThresholdBlendFilterL,
                                    mFilterName)) {
            mLuminanceThresholdBlend = new ScriptC_blend_luminance_threshold(mRS);
            mLuminanceThresholdBlend.set_threshold(mConvertedParams[0]);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageHueFilter, mFilterName)) {
            mHue = new Hue(mRS);
            mHue.setParams(mConvertedParams[0]);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter1, mFilterName)) {
            mRainbowFilter = new RainbowFilter(mRS);
            mRainbowFilter.setSubImages(mSubImages);
            mRainbowFilter.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageToonFilter, mFilterName)) {
            mToon = new Toon(mRS, width, height);
            mToon.setValues(mConvertedParams);
        } /*
           * else if (isEqaulFilterName(ArtFilterUtils.sImagePaidFilter2Filter, mFilterName)) {
           * mPaidFilter2 = new PaidFilter2(mRS); mPaidFilter2.setValues(mConvertedParams); }
           */else if(isEqaulFilterName(ArtFilterUtils.sImageHalftoneFilter, mFilterName)) {
            mHalftone = new ScriptC_halftone(mRS);
            mHalftone.set_fractionalWidthOfPixel(mConvertedParams[0]);
            mHalftone.set_colorToReplace(new Float3(mConvertedParams[1], mConvertedParams[1],
                                                    mConvertedParams[1]));
            float ratio = (float)height / width;
            mHalftone.set_aspectRatio(ratio);
        } else if(isEqaulFilterName(ArtFilterUtils.sImagePixellateFilter, mFilterName)) {
            mPixellater = new ScriptC_pixellater(mRS);
            mPixellater.set_fractionalWidthOfPixel(mConvertedParams[0]);
            float ratio = (float)height / width;
            mPixellater.set_aspectRatio(ratio);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageColorDodgeBlendFilter, mFilterName)) {
            mColorDodgeBlend = new ScriptC_blend_color_dodge(mRS);
        } /*
           * else if (isEqaulFilterName(ArtFilterUtils.sImageColorInvertFilter, mFilterName)) {
           * mColorInvert = new ScriptC_color_invert(mRS); }
           */else if(isEqaulFilterName(ArtFilterUtils.sImageDarkenBlendFilter, mFilterName)) {
            mColorDarkBlend = new ScriptC_blend_color_darken(mRS);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageDissolveBlendFilter, mFilterName)) {
            mDissolveBlend = new ScriptC_blend_dissolve(mRS);
            mDissolveBlend.set_mixturePercent(mConvertedParams[0]);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageGrayscaleFilter, mFilterName)) {
            mGrayScale = new ScriptC_gray_scale(mRS);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageMultiplyBlendFilter, mFilterName)) {
            mMultiplyBlend = new ScriptC_blend_multiply(mRS);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageSobelEdgeDetectionFilter, mFilterName)) {
            mSobelEdgeFilter = new SobelEdgeFilter(mRS, width, height);
            mSobelEdgeFilter.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter2, mFilterName)) {
            mMyCartoon = new MyCartoon(mRS, width, height);
            mMyCartoon.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageNormalBlendFilter, mFilterName)) {
            mNormalBlend = new ScriptC_blend_normal(mRS);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageOpacityMaskBlendFilter, mFilterName)) {
            mOpacityMaskBlend = new ScriptC_blend_opacity_mask(mRS);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageSoftLightBlendFilter, mFilterName)) {
            mSoftLightBlend = new ScriptC_blend_soft_light(mRS);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter9, mFilterName)) {
            mMarshmallow = new Marshmallow(mRS, width, height);
            mMarshmallow.setSubImages(mSubImages[0]);
            mMarshmallow.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageSketchFilter, mFilterName)) {
            mSketchFilter = new SketchFilter(mRS, width, height);
            mSketchFilter.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageHueSaturationFilterL, mFilterName)) {
            mHueSaturation = new ScriptC_hue_saturation(mRS);
            mHueSaturation.set_redSaturation(mConvertedParams[0]);
            mHueSaturation.set_yellowSaturation(mConvertedParams[1]);
            mHueSaturation.set_greenSaturation(mConvertedParams[2]);
            mHueSaturation.set_cyanSaturation(mConvertedParams[3]);
            mHueSaturation.set_blueSaturation(mConvertedParams[4]);
            mHueSaturation.set_magentaSaturation(mConvertedParams[5]);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter5, mFilterName)) {
            mPostGray = new PostGray(mRS, width, height);
            mPostGray.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter7, mFilterName)) {
            mSoftToon = new SoftToon(mRS, width, height);
            mSoftToon.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter8, mFilterName)) {
            mBrightToon = new BrightToon(mRS, width, height);
            mBrightToon.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageRGBDilationFilter, mFilterName)) {
            mRGBDilation = new RGBDilation(mRS, width, height);
            mRGBDilation.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageRGBErosionFilter, mFilterName)) {
            mRGBErosion = new RGBErosion(mRS, width, height);
            mRGBErosion.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageRGBOpeningFilter, mFilterName)) {
            mRGBOpening = new RGBOpening(mRS, width, height);
            mRGBOpening.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter6, mFilterName)) {
            mColorPencil = new ColorPencil(mRS, width, height);
            mColorPencil.setParam(mConvertedParams[0]);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageThresholdEdgeDetectionFilter, mFilterName)) {
            mThresholdEdgeDetection = new ThresholdEdgeDetection(mRS, width, height);
            mThresholdEdgeDetection.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageThresholdSketchFilter, mFilterName)) {
            mThresholdSketch = new ThresholdSketch(mRS, width, height);
            mThresholdSketch.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter4, mFilterName)) {
            mWashDrawing = new WashDrawing(mRS, width, height);
            mWashDrawing.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageDilationFilter, mFilterName)) {
            mDilation = new Dilation(mRS, width, height);
            mDilation.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageErosionFilter, mFilterName)) {
            mErosion = new Erosion(mRS, width, height);
            mErosion.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageOpeningFilter, mFilterName)) {
            mOpening = new Opening(mRS, width, height);
            mOpening.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageAdaptiveThresholdFilter, mFilterName)) {
            mAdaptiveThreshold = new AdaptiveThreshold(mRS, width, height);
            mAdaptiveThreshold.setParams(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageUnsharpMaskFilter, mFilterName)) {
            mUnsharpMask = new UnsharpMask(mRS, width, height);
            mUnsharpMask.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter11, mFilterName)) {
            mOilPainting = new OilPainting(mRS, width, height);
            mOilPainting.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter10, mFilterName)) {
            mOldPainting = new OldPainting(mRS, width, height);
            mOldPainting.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter10, mFilterName)) {
            mOldPainting = new OldPainting(mRS, width, height);
            mOldPainting.setValues(mConvertedParams);
        }
        // else if (isEqaulFilterName(ArtFilterUtils.sImageTiltShiftFilter, mFilterName)) {
        // mTiltShift = new TiltShift(mRS, width, height); mTiltShift.setValues(mConvertedParams);
        // } else if (isEqaulFilterName(ArtFilterUtils.sImageTiltShiftCircleFilterL, mFilterName))
        // { mTiltShiftCircle = new TiltShiftCircle(mRS, width, height);
        // mTiltShiftCircle.setValues(mConvertedParams); }
        else if(isEqaulFilterName(ArtFilterUtils.sImageSmartBlurFilter, mFilterName)) {
            mSmartBlur = new SmartBlur(mRS, width, height);
            mSmartBlur.setValues(mConvertedParams);
        }
        // else if
        // (isEqaulFilterName(ArtFilterUtils.sImageHighPassFilter, mFilterName)) { mHighPass = new
        // HighPass(mRS, width, height); mHighPass.setValues(mConvertedParams); } else if
        // (isEqaulFilterName(ArtFilterUtils.sUnsharpMaskTiltShiftFilter, mFilterName)) {
        // mUnsharpMaskTiltShift = new UnsharpMaskTiltShift(mRS, width, height);
        // mUnsharpMaskTiltShift.setValues(mConvertedParams); } else if
        // (isEqaulFilterName(ArtFilterUtils.sUnsharpMaskTiltShiftCircleFilter, mFilterName)) {
        // mUnsharpMaskTiltShiftCircle = new UnsharpMaskTiltShiftCircle(mRS, width, height);
        // mUnsharpMaskTiltShiftCircle.setValues(mConvertedParams); } else if
        // (isEqaulFilterName(ArtFilterUtils.sUnsharpMaskPinchDistortionFilter, mFilterName)) {
        // mUnsharpMaskPinchDistortion = new UnsharpMaskPinchDistortion(mRS, width, height);
        // mUnsharpMaskPinchDistortion.setValues(mConvertedParams); } else if
        // (isEqaulFilterName(ArtFilterUtils.sPaidFilter14, mFilterName)) { mCustom14 = new
        // Custom14(mRS, width, height); mCustom14.setValues(mConvertedParams); }
        else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter15, mFilterName)) {
            mCustom15 = new Custom15(mRS, width, height);
            mCustom15.setValues(mConvertedParams);
        }
        // else if
        // (isEqaulFilterName(ArtFilterUtils.sPaidFilter16, mFilterName)) { mCustom16 = new
        // Custom16(mRS, width, height); mCustom16.setValues(mConvertedParams); }
        else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter13, mFilterName)) {
            mOilPastel = new OilPastel(mRS, width, height);
            mOilPastel.setValues(mConvertedParams);
        } else if(isEqaulFilterName(ArtFilterUtils.sSoftFilter, mFilterName)) {
            mSoft = new Soft(mRS, width, height);
            mSoft.setValues(mConvertedParams);
        } else if("Stretch outside".equals(mFilterName)) {
            mStretchOutside = new ScriptC_stretch_outside(mRS);
            mStretchOutside.set_width(width);
            mStretchOutside.set_height(height);
            mStretchOutside.set_radius(mConvertedParams[0] / 100f);
        }
    }

    private static BoxBlur mBoxBlur;
    private static GaussianBlur mGaussianBlur;
    private static ScriptC_blend_mask mMaskBlend;
    private static ScriptC_blend_overlay mOverlayBlend;
    private static ScriptC_luminance_threshold mLuminanceThreshold;
    private static ScriptC_posterize mPosterize;
    private static ScriptC_saturation mSaturation;
    private static ScriptC_opacity mOpacity;
    private static Levels mLevelsFilter;
    private static ScriptC_blend_alpha mAlphaBlend;
    private static ScriptC_pinch_distortion mPinchDistortion;

    private static Kuwahara mKuwahara;
    private static ScriptC_blend_alpha_mask mAlphaMaskBlend;
    private static ScriptC_blend_color_burn mColorBurnBlend;
    private static ScriptC_blend_luminance_threshold mLuminanceThresholdBlend;
    private static Hue mHue;
    private static Toon mToon;
    private static RainbowFilter mRainbowFilter;
    private static PaidFilter2 mPaidFilter2;
    private static ScriptC_pixellater mPixellater;
    private static ScriptC_halftone mHalftone;
    private static ScriptC_blend_color_dodge mColorDodgeBlend;
    private static ScriptC_color_invert mColorInvert;
    private static ScriptC_blend_color_darken mColorDarkBlend;
    private static ScriptC_blend_dissolve mDissolveBlend;
    private static ScriptC_gray_scale mGrayScale;
    private static ScriptC_blend_multiply mMultiplyBlend;
    private static SobelEdgeFilter mSobelEdgeFilter;
    private static MyCartoon mMyCartoon;
    private static ScriptC_blend_normal mNormalBlend;
    private static ScriptC_blend_opacity_mask mOpacityMaskBlend;
    private static ScriptC_blend_soft_light mSoftLightBlend;
    private static Marshmallow mMarshmallow;
    private static SketchFilter mSketchFilter;
    private static ScriptC_hue_saturation mHueSaturation;
    private static PostGray mPostGray;
    private static SoftToon mSoftToon;
    private static BrightToon mBrightToon;
    private static RGBDilation mRGBDilation;
    private static RGBErosion mRGBErosion;
    private static RGBOpening mRGBOpening;
    private static ColorPencil mColorPencil;
    private static ThresholdEdgeDetection mThresholdEdgeDetection;
    private static ThresholdSketch mThresholdSketch;
    private static WashDrawing mWashDrawing;
    private static Dilation mDilation;
    private static Erosion mErosion;
    private static Opening mOpening;
    private static AdaptiveThreshold mAdaptiveThreshold;
    private static UnsharpMask mUnsharpMask;
    private static OilPainting mOilPainting;
    private static OldPainting mOldPainting;
    private static TiltShift mTiltShift;
    private static TiltShiftCircle mTiltShiftCircle;
    private static SmartBlur mSmartBlur;
    private static HighPass mHighPass;
    private static UnsharpMaskTiltShift mUnsharpMaskTiltShift;
    private static UnsharpMaskTiltShiftCircle mUnsharpMaskTiltShiftCircle;
    private static UnsharpMaskPinchDistortion mUnsharpMaskPinchDistortion;
    private static Custom14 mCustom14;
    private static Custom15 mCustom15;
    private static Custom16 mCustom16;
    private static OilPastel mOilPastel;
    private static Soft mSoft;
    private static ScriptC_stretch_outside mStretchOutside;

    /**
     * 필터를 적용한다.
     */
    public static void processArtFilter(Allocation allocation) {
        if(isEqaulFilterName(ArtFilterUtils.sImageLuminanceThresholdFilter, mFilterName)) {
            mLuminanceThreshold.forEach_luminance_threshold(allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImagePosterizeFilter, mFilterName)) {
            mPosterize.forEach_posterize(allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageMaskBlendFilterL, mFilterName)) {
            mMaskBlend.forEach_maskBlend(mTempSubImages, allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageOverlayBlendFilter, mFilterName)) {
            mOverlayBlend.forEach_overlayBlend(mTempSubImages, allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageGaussianBlurFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mGaussianBlur.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageBoxBlurFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mBoxBlur.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageSaturationFilter, mFilterName)) {
            mSaturation.forEach_saturationFilter(allocation, allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageOpacityFilter, mFilterName)) {
            mOpacity.forEach_opacity(allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageLevelsFilter, mFilterName)) {
            mLevelsFilter.excute(allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageAlphaBlendFilter, mFilterName)) {
            mAlphaBlend.forEach_alphaBlend(mTempSubImages, allocation);
        } else if(ArtFilterUtils.sImagePinchDistortionFilter.getFilterInfo().filterName.equals(mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mPinchDistortion.set_inAllocation(mAllocationSub);
            mPinchDistortion.forEach_pinch_distortion(allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageKuwaharaFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mKuwahara.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageAlphaMaskBlendFilterL, mFilterName)) {
            mAlphaMaskBlend.forEach_alphaMaskBlend(mTempSubImages, allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageColorBurnBlendFilter, mFilterName)) {
            mColorBurnBlend.forEach_colorBurnBlend(mTempSubImages, allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageLuminanceThresholdBlendFilterL,
                                    mFilterName)) {
            mLuminanceThresholdBlend.forEach_luminanceThresholdBlend(mTempSubImages, allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageHueFilter, mFilterName)) {
            mHue.excute(allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter1, mFilterName)) {
            mRainbowFilter.excute(allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageToonFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mToon.excute(allocation, mAllocationSub);
        } /*
           * else if (isEqaulFilterName(ArtFilterUtils.sImagePaidFilter2Filter, mFilterName)) {
           * mPaidFilter2.excute(allocation); }
           */else if(isEqaulFilterName(ArtFilterUtils.sImageHalftoneFilter, mFilterName)) {
            mHalftone.set_inAllocation(allocation);
            mHalftone.forEach_halftone(allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImagePixellateFilter, mFilterName)) {
            mPixellater.set_inAllocation(allocation);
            mPixellater.forEach_pixellater(allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageColorDodgeBlendFilter, mFilterName)) {
            mColorDodgeBlend.forEach_colorDodgeBlend(mTempSubImages, allocation);
        }/*
          * else if (isEqaulFilterName(ArtFilterUtils.sImageColorInvertFilter, mFilterName)) {
          * mColorInvert.forEach_color_invert(allocation); }
          */else if(isEqaulFilterName(ArtFilterUtils.sImageDarkenBlendFilter, mFilterName)) {
            mColorDarkBlend.forEach_colorDarkenBlend(mTempSubImages, allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageDissolveBlendFilter, mFilterName)) {
            mDissolveBlend.forEach_dissolveBlend(mTempSubImages, allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageGrayscaleFilter, mFilterName)) {
            mGrayScale.forEach_grayScale(allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageMultiplyBlendFilter, mFilterName)) {
            mMultiplyBlend.forEach_multiplyBlend(mTempSubImages, allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageSobelEdgeDetectionFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mSobelEdgeFilter.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter2, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mMyCartoon.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageNormalBlendFilter, mFilterName)) {
            mNormalBlend.forEach_normalBlend(mTempSubImages, allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageOpacityMaskBlendFilter, mFilterName)) {
            mOpacityMaskBlend.forEach_opacityMaskBlend(mTempSubImages, allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageSoftLightBlendFilter, mFilterName)) {
            mSoftLightBlend.forEach_softLightBlend(mTempSubImages, allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter9, mFilterName)) {
            mMarshmallow.excute(allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageSketchFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mSketchFilter.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageHueSaturationFilterL, mFilterName)) {
            mHueSaturation.forEach_hue_saturation(allocation);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter5, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mPostGray.excute(mSubImages[0], allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter7, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mSoftToon.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter8, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mBrightToon.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageRGBDilationFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mRGBDilation.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageRGBErosionFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mRGBErosion.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageRGBOpeningFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mRGBOpening.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter6, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mColorPencil.excute(allocation, mAllocationSub, mSubImages[0], mSubImages[1],
                                mSubImages[2]);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageThresholdEdgeDetectionFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mThresholdEdgeDetection.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageThresholdSketchFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mThresholdSketch.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter4, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mWashDrawing.excute(mSubImages[0], allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageDilationFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mDilation.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageErosionFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mErosion.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageOpeningFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mOpening.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageAdaptiveThresholdFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mAdaptiveThreshold.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sImageUnsharpMaskFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mUnsharpMask.excute(allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter11, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mOilPainting.excute(mSubImages[0], allocation, mAllocationSub);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter10, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mOldPainting.excute(mSubImages[0], mSubImages[1], allocation, mAllocationSub);
        }
        // else if (isEqaulFilterName(ArtFilterUtils.sImageTiltShiftFilter, mFilterName)) {
        // mAllocationSub.copyFrom(allocation); mTiltShift.excute(allocation, mAllocationSub); }
        // else if (isEqaulFilterName(ArtFilterUtils.sImageTiltShiftCircleFilterL, mFilterName)) {
        // mAllocationSub.copyFrom(allocation); mTiltShiftCircle.excute(allocation,
        // mAllocationSub); }
        else if(isEqaulFilterName(ArtFilterUtils.sImageSmartBlurFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mSmartBlur.excute(allocation, mAllocationSub);
        }
        // } else if (isEqaulFilterName(ArtFilterUtils.sImageHighPassFilter,
        // mFilterName)) { mAllocationSub.copyFrom(allocation); mHighPass.excute(allocation,
        // mAllocationSub); } else if
        // (isEqaulFilterName(ArtFilterUtils.sUnsharpMaskTiltShiftFilter, mFilterName)) {
        // mAllocationSub.copyFrom(allocation); mUnsharpMaskTiltShift.excute(allocation,
        // mAllocationSub); } else if
        // (isEqaulFilterName(ArtFilterUtils.sUnsharpMaskTiltShiftCircleFilter, mFilterName)) {
        // mAllocationSub.copyFrom(allocation); mUnsharpMaskTiltShiftCircle.excute(allocation,
        // mAllocationSub); } else if
        // (isEqaulFilterName(ArtFilterUtils.sUnsharpMaskPinchDistortionFilter, mFilterName)) {
        // mAllocationSub.copyFrom(allocation); mUnsharpMaskPinchDistortion.excute(allocation,
        // mAllocationSub); } else if (isEqaulFilterName(ArtFilterUtils.sPaidFilter14,
        // mFilterName)) { mAllocationSub.copyFrom(allocation); mCustom14.excute(allocation,
        // mAllocationSub); }
        else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter15, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mCustom15.excute(allocation, mAllocationSub);
        }
        // else if (isEqaulFilterName(ArtFilterUtils.sPaidFilter16,
        // mFilterName)) { mAllocationSub.copyFrom(allocation); mCustom16.excute(allocation,
        // mAllocationSub); }
        else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter13, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mOilPastel.excute(allocation, mAllocationSub, mSubImages[0], mSubImages[1],
                              mSubImages[2]);
        }
        // mAllocationSub); }
        else if(isEqaulFilterName(ArtFilterUtils.sSoftFilter, mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mSoft.excute(allocation, mAllocationSub);
        } else if("Stretch outside".equals(mFilterName)) {
            mAllocationSub.copyFrom(allocation);
            mStretchOutside.set_inAllocation(mAllocationSub);
            mStretchOutside.forEach_pinch_distortion(allocation);
        }
    }

    /**
     * 텍스처 이미지 리소스를 해제한다.
     */
    private static void cleanSubImages() {
        for(int i = 0; i < mSubImages.length; i++) {
            if(mSubImages[i] != null) {
                mSubImages[i].destroy();
                mSubImages[i] = null;
            }
        }
    }

    /**
     * 텍스처 이미지를 불러온다.
     */
    private static void initSubImages(Context context, int width, int height, String filterName) {
        // Clean Texture
        Bitmap image = null;
        if(isEqaulFilterName(ArtFilterUtils.sImageMosaicFilter, filterName)) {
            image = FileUtils.getBitmapResource(context, "af_mosaic_squares", width, height,
                                                Config.ARGB_8888);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter5, filterName)) {
            image = FileUtils.getBitmapResource(context, "af5_texture", width, height,
                                                Config.ARGB_8888);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter1, filterName)) {
            image = FileUtils.getBitmapResource(context, "af1_texture1", width, height,
                                                Config.ARGB_8888);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter4, filterName)) {
            image = FileUtils.getBitmapResource(context, "af4_texture1_bg", width, height,
                                                Config.ARGB_8888);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter10, filterName)) {
            image = FileUtils.getBitmapResource(context, "af10_texture1", width, height,
                                                Config.ARGB_8888);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter13, filterName)) {
            image = FileUtils.getBitmapResource(context, "af13_texture1", width, height,
                                                Config.ARGB_8888);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter6, filterName)) {
            image = FileUtils.getBitmapResource(context, "af6_texture1", width, height,
                                                Config.ARGB_8888);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter11, filterName)) {
            image = FileUtils.getBitmapResource(context, "af11_texture1", width, height,
                                                Config.ARGB_8888);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter9, filterName)) {
            image = FileUtils.getBitmapResource(context, "af9_texture_re", width, height,
                                                Config.ARGB_8888);
        }
        if(image != null) {
            mSubImages[0].copyFrom(image);
            image.recycle();
            image = null;
        }

        if(isEqaulFilterName(ArtFilterUtils.sPaidFilter1, filterName)) {
            image = FileUtils.getBitmapResource(context, "af1_texture2", width, height,
                                                Config.ARGB_8888);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter10, filterName)) {
            image = FileUtils.getBitmapResource(context, "af10_texture2", width, height,
                                                Config.ARGB_8888);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter13, filterName)) {
            image = FileUtils.getBitmapResource(context, "af13_texture2", width, height,
                                                Config.ARGB_8888);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter6, filterName)) {
            image = FileUtils.getBitmapResource(context, "af6_texture2", width, height,
                                                Config.ARGB_8888);
        }
        if(image != null) {
            mSubImages[1].copyFrom(image);
            image.recycle();
            image = null;
        }

        if(isEqaulFilterName(ArtFilterUtils.sPaidFilter13, filterName)) {
            image = FileUtils.getBitmapResource(context, "af13_texture3", width, height,
                                                Config.ARGB_8888);
        } else if(isEqaulFilterName(ArtFilterUtils.sPaidFilter6, filterName)) {
            image = FileUtils.getBitmapResource(context, "af6_texture3", width, height,
                                                Config.ARGB_8888);
        }
        if(image != null) {
            mSubImages[2].copyFrom(image);
            image.recycle();
            // mSubImages[2] = Allocation.createFromBitmap(mRS, image, RsYuv.MIPMAPCONTROL,
            // RsYuv.ALLOCATION_USAGE);
        }

        // temp code
        // image = FileUtils.getBitmapResource(context, "mask", width, height, Config.ARGB_8888);
        // mTempSubImages = Allocation.createFromBitmap(mRS, image, RsYuv.MIPMAPCONTROL,
        // RsYuv.ALLOCATION_USAGE);
    }
}
