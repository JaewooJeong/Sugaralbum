
package com.kiwiple.imageframework.gpuimage;

import static javax.microedition.khronos.opengles.GL10.GL_RGBA;
import static javax.microedition.khronos.opengles.GL10.GL_UNSIGNED_BYTE;

import java.io.IOException;
import java.io.InputStream;
import java.nio.IntBuffer;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.opengl.GLES20;
import android.text.TextUtils;
import android.util.Log;

import com.kiwiple.imageframework.filter.CurvesPoint;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.ImageInput;
import com.kiwiple.imageframework.gpuimage.ShaderUtils.Size;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageAlphaBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageAlphaMaskBlendFilterL;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageChromaKeyBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageColorBurnBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageColorDodgeBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageDarkenBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageDissolveBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageLuminanceThresholdBlendFilterL;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageMaskBlendFilterL;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageMultiplyBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageNormalBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageOpacityMaskBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageOverlayBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageScreenBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.blends.ImageSoftLightBlendFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageAdaptiveThresholdFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageChromaKeyFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageColorTemperatureFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageGrayscaleFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageHueFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageHueSaturationFilterL;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageLevelsFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageLuminanceThresholdFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageOpacityFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImagePrimitiveRGBFilter;
import com.kiwiple.imageframework.gpuimage.filter.colorprocessing.ImageSaturationFilter;
import com.kiwiple.imageframework.gpuimage.filter.custom.PaidFilter1;
import com.kiwiple.imageframework.gpuimage.filter.custom.PaidFilter10;
import com.kiwiple.imageframework.gpuimage.filter.custom.PaidFilter11;
import com.kiwiple.imageframework.gpuimage.filter.custom.PaidFilter13;
import com.kiwiple.imageframework.gpuimage.filter.custom.PaidFilter15;
import com.kiwiple.imageframework.gpuimage.filter.custom.PaidFilter2;
import com.kiwiple.imageframework.gpuimage.filter.custom.PaidFilter4;
import com.kiwiple.imageframework.gpuimage.filter.custom.PaidFilter5;
import com.kiwiple.imageframework.gpuimage.filter.custom.PaidFilter6;
import com.kiwiple.imageframework.gpuimage.filter.custom.PaidFilter7;
import com.kiwiple.imageframework.gpuimage.filter.custom.PaidFilter8;
import com.kiwiple.imageframework.gpuimage.filter.custom.PaidFilter9;
import com.kiwiple.imageframework.gpuimage.filter.custom.SoftFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageBigEyeFilterL;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageCrosshatchFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageDirectionalShiftFilterL;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageHalftoneFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageKuwaharaFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageMaskFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageMosaicFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImagePinchDistortionFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImagePixellateFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImagePolkaDotFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImagePosterizeFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageShiftToTopFilterL;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageShiftToVerticalCenterFilterL;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageSketchFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageSmallNoseFilterL;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageThresholdSketchFilter;
import com.kiwiple.imageframework.gpuimage.filter.effect.ImageToonFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageBoxBlurFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageDilationFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageDirectionalDetectionFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageErosionFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageGaussianBlurFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageOpeningFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageRGBDilationFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageRGBErosionFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageRGBOpeningFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageSmartBlurFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageSobelEdgeDetectionFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageThresholdEdgeDetectionFilter;
import com.kiwiple.imageframework.gpuimage.filter.imageprocessing.ImageUnsharpMaskFilter;
import com.kiwiple.imageframework.gpuimage.source.ImagePicture;

//ImagePicture.addTarget(a);
//ImagePicture.process í•  ê²½ìš° aê°€ ìˆ˜í–‰ëœë‹¤.
//
//a.addTarget(b);
//ImagePicture.process í•  ê²½ìš° aê°€ ìˆ˜í–‰ ëœ í›„ bê°€ ìˆ˜í–‰ ëœë‹¤.
//
//ImangeTwoInputFilter c;
//a.addTarget(c);
//ImagePicture.process í•  ê²½ìš° aê°€ ìˆ˜í–‰ ëœ í›„ bê°€ ìˆ˜í–‰ ë˜ì§€ë§Œ cì— inputì´ í•˜ë‚˜ ì´ë¯€ë¡œ cëŠ” ìˆ˜í–‰ë˜ì§€ ì•ŠëŠ”ë‹¤.
//
//b.addTarget(c);
//ImagePicture.process í•  ê²½ìš° aê°€ ìˆ˜í–‰ ëœ í›„ bê°€ ìˆ˜í–‰ ë˜ì§€ë§Œ cê°€ ìˆ˜í–‰ëœë‹¤.
//

//case1: 1ê°œ ë‹¨ì¼ í•„í„° ìˆ˜í–‰
//ImagePicture.init
//Filter.init
//ImagePicture.addTarget(Filter)
//ImagePicture.process()

//case2: 2ê°œ í•„í„° ì—°ì† ìˆ˜í–‰
//ImagePicture.init
//Filter1.init
//Filter2.init
//Filter1.addTarget(Filter2)
//ImagePicture.addTarget(Filter1)
//ImagePicture.process()

//case3: í•„í„° 2ê°œë¥¼ ë°›ëŠ” TwoInputFilter
//ImagePicture.init
//TwoInputFilter.init
//ImagePicture.addTarget(TwoInputFilter)
//
//ImagePicture2.init
//ImagePicture2.addTarget(TwoInputFilter)
//ImagePicture2.process()
//
//ImagePicture.process()

public class ArtFilterUtils {
    private static final String TAG = ArtFilterUtils.class.getSimpleName();
    public static boolean sIsLiteMode = true;

    public static void getBitmapFromGL(Bitmap image, int width, int height) {
        convertToBitmap(image, width, height);
    }

    private static IntBuffer ib;

    /**
     * OpenGL에서 이미지를 읽어와서 Bitmap에 복사한다.
     */
    public static void convertToBitmap(Bitmap image, int width, int height) {
        if(ib == null || ib.capacity() != width * height) {
            ib = IntBuffer.allocate(width * height);
        }
        ib.position(0);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
        ib.position(0);

        // Bitmap bitmap = Bitmap.createBitmap(width, height,
        // Bitmap.Config.ARGB_8888);
        image.copyPixelsFromBuffer(ib);
        // return bitmap;
    }

    public static Bitmap getBitmapFromGL(int width, int height) {
        return convertToBitmap(width, height);
    }

    /**
     * OpenGL에서 이미지를 읽어와서 Bitmap에 복사한다.
     */
    public static Bitmap convertToBitmap(int width, int height) {
        IntBuffer ib = IntBuffer.allocate(width * height);
        GLES20.glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, ib);

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(ib);
        return bitmap;
    }

    /**
     * JNI로 구현된 필터를 OpenGL로 전환하기 위해 만든 메쏘드. 현재 개발 중단된 코드.
     */
    public static Bitmap processPrimitiveRGBFilter(Context context, Bitmap image,
            boolean grayScale, ArrayList<CurvesPoint> curveComposite,
            ArrayList<CurvesPoint> curveRed, ArrayList<CurvesPoint> curveGreen,
            ArrayList<CurvesPoint> curveBlue, float saturation, float brightness, float contrast) {
        Log.d("ArtFilterUtils", "start");
        if(image == null) {
            return image;
        }

        Bitmap result = null;

        Log.d("ArtFilterUtils", "setup start");
        ImagePicture ip = new ImagePicture();
        ip.initWithImage(context, image);
        Log.d("ArtFilterUtils", "setup end");

        Log.d("ArtFilterUtils", "filter init start");
        ImagePrimitiveRGBFilter filter = new ImagePrimitiveRGBFilter();
        filter.init(context);
        filter.setGrayScale(grayScale);
        filter.setRgbCompositeControlPoints(curveComposite);
        filter.setRedControlPoints(curveRed);
        filter.setGreenControlPoints(curveGreen);
        filter.setBlueControlPoints(curveBlue);
        filter.setSaturation(saturation);
        filter.setBrightness(brightness);
        filter.setContrast(contrast);
        Log.d("ArtFilterUtils", "filter init end");

        if(!image.isRecycled()) {
            image.recycle();
            causeGC(image.getWidth(), image.getHeight());
        }

        Log.d("ArtFilterUtils", "filter processing start");
        ip.addTarget(filter);
        ip.processImage();
        Log.d("ArtFilterUtils", "filter processing end");
        Log.d("ArtFilterUtils", "filtering image making start");
        // --------------------------------------------------------------------------------
        result = getBitmapFromGL((int)filter.getOutputWidth(), (int)filter.getOutputHeight());
        Log.d("ArtFilterUtils", "filtering image making end");
        if(result.getHeight() != image.getHeight() || result.getWidth() != image.getWidth()) {
            Log.e("ArtFilterUtils", "size missmatch");
            Bitmap resized = Bitmap.createScaledBitmap(result, image.getWidth(), image.getHeight(),
                                                       false);
            result.recycle();
            result = resized;
        }
        ip.destroyAll();
        return result;
    }

    private static void causeGC(int width, int height) {
        if(width >= 1600 || height >= 1600) {
            System.gc();
        }
    }

    /**
     * 필터를 초기화하고 파라미터를 세팅한다.
     */
    public static void initFilter(Context context, int width, int height) {
        /*
         * if (TextUtils.isEmpty(filterName) || image == null) { return image; } Bitmap[] subImages
         * = initSubImages(context, filterName); Bitmap result = null; ImagePicture ip = new
         * ImagePicture(); ip.initWithImage(context, image);
         */
        boolean needsSecondImage = false;
        /*
         * ImageInput filter = null; float[] convertedParams = getConvertParams(filterName, params,
         * (image.getWidth() + image.getHeight()) / 2);
         */// color filter
        if(sImageAdaptiveThresholdFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageAdaptiveThresholdFilter.init(context);
            sImageAdaptiveThresholdFilter.setBlurSize(convertedParams[0]);
            filter = sImageAdaptiveThresholdFilter;
        } else if(sImageChromaKeyFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageChromaKeyFilter.init(context);
            sImageChromaKeyFilter.setThresholdSensitivity(convertedParams[0]);
            sImageChromaKeyFilter.setSmoothing(convertedParams[1]);
            filter = sImageChromaKeyFilter;
        } else if(sImageGrayscaleFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageGrayscaleFilter.init(context);
            filter = sImageGrayscaleFilter;
        } else if(sImageHueFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageHueFilter.init(context);
            sImageHueFilter.setHue(convertedParams[0]);
            filter = sImageHueFilter;
        } else if(sImageLevelsFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageLevelsFilter.init(context);
            sImageLevelsFilter.setMin(convertedParams[0], convertedParams[1], convertedParams[2]);
            filter = sImageLevelsFilter;
        } else if(sImageLuminanceThresholdFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageLuminanceThresholdFilter.init(context);
            sImageLuminanceThresholdFilter.setThreshold(convertedParams[0]);
            filter = sImageLuminanceThresholdFilter;
        } else if(sImageOpacityFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageOpacityFilter.init(context);
            sImageOpacityFilter.setOpacity(convertedParams[0]);
            filter = sImageOpacityFilter;
        } else if(sImageSaturationFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageSaturationFilter.init(context);
            sImageSaturationFilter.setSaturation(convertedParams[0]);
            filter = sImageSaturationFilter;
        } else if(sImageHueSaturationFilterL.getFilterInfo().filterName.equals(sFilterName)) {
            sImageHueSaturationFilterL.init(context);
            sImageHueSaturationFilterL.setRedSaturation(convertedParams[0]);
            sImageHueSaturationFilterL.setYellowSaturation(convertedParams[1]);
            sImageHueSaturationFilterL.setGreenSaturation(convertedParams[2]);
            sImageHueSaturationFilterL.setCyanSaturation(convertedParams[3]);
            sImageHueSaturationFilterL.setBlueSaturation(convertedParams[4]);
            sImageHueSaturationFilterL.setMagentaSaturation(convertedParams[5]);
            filter = sImageHueSaturationFilterL;
        } else if(sImageColorTemperatureFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageColorTemperatureFilter.init(context);
            sImageColorTemperatureFilter.setTemperature(convertedParams[0]);
            filter = sImageColorTemperatureFilter;
        }
        // image filter
        else if(sImageBoxBlurFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageBoxBlurFilter.init(context);
            sImageBoxBlurFilter.setBlurSize(convertedParams[0]);
            filter = sImageBoxBlurFilter;
        } else if(sImageDilationFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageDilationFilter.initWithRadius(context, (int)convertedParams[0]);
            filter = sImageDilationFilter;
        } else if(sImageDirectionalDetectionFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageDirectionalDetectionFilter.init(context);
            sImageDirectionalDetectionFilter.setWeight(convertedParams[0]);
            filter = sImageDirectionalDetectionFilter;
        } else if(sImageErosionFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageErosionFilter.initWithRadius(context, (int)convertedParams[0]);
            filter = sImageErosionFilter;
        } else if(sImageGaussianBlurFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageGaussianBlurFilter.init(context);
            sImageGaussianBlurFilter.setBlurSize(convertedParams[0]);
            filter = sImageGaussianBlurFilter;
        } else if(sImageOpeningFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageOpeningFilter.initWithRadius(context, (int)convertedParams[0]);
            filter = sImageOpeningFilter;
        } else if(sImageRGBDilationFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageRGBDilationFilter.initWithRadius(context, (int)convertedParams[0]);
            filter = sImageRGBDilationFilter;
        } else if(sImageRGBErosionFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageRGBErosionFilter.initWithRadius(context, (int)convertedParams[0]);
            filter = sImageRGBErosionFilter;
        } else if(sImageRGBOpeningFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageRGBOpeningFilter.initWithRadius(context, (int)convertedParams[0]);
            filter = sImageRGBOpeningFilter;
        } else if(sImageSobelEdgeDetectionFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageSobelEdgeDetectionFilter.init(context);
            sImageSobelEdgeDetectionFilter.setWeight(convertedParams[0]);
            filter = sImageSobelEdgeDetectionFilter;
        } else if(sImageThresholdEdgeDetectionFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageThresholdEdgeDetectionFilter.init(context);
            sImageThresholdEdgeDetectionFilter.setThreshold(convertedParams[0]);
            sImageThresholdEdgeDetectionFilter.setWeight(convertedParams[1]);
            filter = sImageThresholdEdgeDetectionFilter;
        } else if(sImageUnsharpMaskFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageUnsharpMaskFilter.init(context);
            sImageUnsharpMaskFilter.setIntensity(convertedParams[0]);
            sImageUnsharpMaskFilter.setBlur(ArtFilterUtils.getWeightedParam(width, height, 1.f));
            filter = sImageUnsharpMaskFilter;
        } else if(sImageSmartBlurFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageSmartBlurFilter.init(context);
            sImageSmartBlurFilter.setBlurSize(convertedParams[0]);
            sImageSmartBlurFilter.setThreshold(convertedParams[1]);
            filter = sImageSmartBlurFilter;
        }
        // blending filter
        else if(sImageAlphaBlendFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageAlphaBlendFilter.init(context);
            sImageAlphaBlendFilter.setMix(convertedParams[0]);
            filter = sImageAlphaBlendFilter;
            needsSecondImage = true;
        } else if(sImageAlphaMaskBlendFilterL.getFilterInfo().filterName.equals(sFilterName)) {
            sImageAlphaMaskBlendFilterL.init(context);
            sImageAlphaMaskBlendFilterL.setMix(convertedParams[0]);
            filter = sImageAlphaMaskBlendFilterL;
            needsSecondImage = true;
        } else if(sImageChromaKeyBlendFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageChromaKeyBlendFilter.init(context);
            sImageChromaKeyBlendFilter.setThresholdSensitivity(convertedParams[0]);
            sImageChromaKeyBlendFilter.setSmoothing(convertedParams[1]);
            filter = sImageChromaKeyBlendFilter;
            needsSecondImage = true;
        } else if(sImageColorBurnBlendFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageColorBurnBlendFilter.init(context);
            filter = sImageColorBurnBlendFilter;
            needsSecondImage = true;
        } else if(sImageColorDodgeBlendFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageColorDodgeBlendFilter.init(context);
            filter = sImageColorDodgeBlendFilter;
            needsSecondImage = true;
        } else if(sImageDarkenBlendFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageDarkenBlendFilter.init(context);
            filter = sImageDarkenBlendFilter;
            needsSecondImage = true;
        } else if(sImageDissolveBlendFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageDissolveBlendFilter.init(context);
            sImageDissolveBlendFilter.setMix(convertedParams[0]);
            filter = sImageDissolveBlendFilter;
            needsSecondImage = true;
        } else if(sImageLuminanceThresholdBlendFilterL.getFilterInfo().filterName.equals(sFilterName)) {
            sImageLuminanceThresholdBlendFilterL.init(context);
            sImageLuminanceThresholdBlendFilterL.setThreshold(convertedParams[0]);
            filter = sImageLuminanceThresholdBlendFilterL;
            needsSecondImage = true;
        } else if(sImageMaskBlendFilterL.getFilterInfo().filterName.equals(sFilterName)) {
            sImageMaskBlendFilterL.init(context);
            filter = sImageMaskBlendFilterL;
            needsSecondImage = true;
        } else if(sImageMultiplyBlendFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageMultiplyBlendFilter.init(context);
            filter = sImageMultiplyBlendFilter;
            needsSecondImage = true;
        } else if(sImageNormalBlendFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageNormalBlendFilter.init(context);
            filter = sImageNormalBlendFilter;
            needsSecondImage = true;
        } else if(sImageOpacityMaskBlendFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageOpacityMaskBlendFilter.init(context);
            sImageOpacityMaskBlendFilter.setOpacity(convertedParams[0]);
            filter = sImageOpacityMaskBlendFilter;
            needsSecondImage = true;
        } else if(sImageOverlayBlendFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageOverlayBlendFilter.init(context);
            filter = sImageOverlayBlendFilter;
            needsSecondImage = true;
        } else if(sImageScreenBlendFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageScreenBlendFilter.init(context);
            filter = sImageScreenBlendFilter;
            needsSecondImage = true;
        } else if(sImageSoftLightBlendFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageSoftLightBlendFilter.init(context);
            filter = sImageSoftLightBlendFilter;
            needsSecondImage = true;
        }
        // effect filter
        else if(sImageCrosshatchFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageCrosshatchFilter.init(context);
            sImageCrosshatchFilter.setCrossHatchSpacing(convertedParams[0]);
            sImageCrosshatchFilter.setLineWidth(convertedParams[1]);
            filter = sImageCrosshatchFilter;
        } else if(sImageHalftoneFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageHalftoneFilter.init(context);
            sImageHalftoneFilter.setFractionalWidthOfAPixel(convertedParams[0]);
            sImageHalftoneFilter.setColorToReplaceRed(convertedParams[1]);
            filter = sImageHalftoneFilter;
        } else if(sImageKuwaharaFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageKuwaharaFilter.init(context);
            sImageKuwaharaFilter.setRadius((int)(convertedParams[0]));
            filter = sImageKuwaharaFilter;
        } else if(sImageMaskFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageMaskFilter.init(context);
            filter = sImageMaskFilter;
            needsSecondImage = true;
        } else if(sImageMosaicFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageMosaicFilter.init(context);
            // Size inputSize = new Size(convertedParams[0],
            // convertedParams[0]);
            Size displaySize = new Size(convertedParams[0], convertedParams[0]);
            // sImageMosaicFilter.setInputTileSize(inputSize);
            sImageMosaicFilter.setDisplayTileSize(displaySize);
            sImageMosaicFilter.setTileSet(context, subImages[0], width, height);
            sImageMosaicFilter.setColorOn(false);
            filter = sImageMosaicFilter;
        } else if(sImagePinchDistortionFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImagePinchDistortionFilter.init(context);
            sImagePinchDistortionFilter.setRadius(10.f);
            sImagePinchDistortionFilter.setCenter(new PointF(0.5f, 0.5f));
            sImagePinchDistortionFilter.setScale(convertedParams[0]);
            filter = sImagePinchDistortionFilter;
        } else if(sImageShiftToTopFilterL.getFilterInfo().filterName.equals(sFilterName)) {
            sImageShiftToTopFilterL.init(context);
            sImageShiftToTopFilterL.setScale(convertedParams[0]);
            filter = sImageShiftToTopFilterL;
        } else if(sImageShiftToVerticalCenterFilterL.getFilterInfo().filterName.equals(sFilterName)) {
            sImageShiftToVerticalCenterFilterL.init(context);
            sImageShiftToVerticalCenterFilterL.setScale(convertedParams[0]);
            filter = sImageShiftToVerticalCenterFilterL;
        } else if(sImageBigEyeFilterL.getFilterInfo().filterName.equals(sFilterName)) {
            sImageBigEyeFilterL.init(context);
            sImageBigEyeFilterL.setScale(convertedParams[0]);
            filter = sImageBigEyeFilterL;
        } else if(sImageSmallNoseFilterL.getFilterInfo().filterName.equals(sFilterName)) {
            sImageSmallNoseFilterL.init(context);
            sImageSmallNoseFilterL.setScale(convertedParams[0]);
            filter = sImageSmallNoseFilterL;
        } else if(sImageDirectionalShiftFilterL.getFilterInfo().filterName.equals(sFilterName)) {
            sImageDirectionalShiftFilterL.init(context);
            sImageDirectionalShiftFilterL.setScale(convertedParams[0]);
            sImageDirectionalShiftFilterL.setDirection(convertedParams[1], convertedParams[2]);
            filter = sImageDirectionalShiftFilterL;
        } else if(sImagePixellateFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImagePixellateFilter.init(context);
            sImagePixellateFilter.setFractionalWidthOfAPixel(convertedParams[0]);
            filter = sImagePixellateFilter;
        } else if(sImagePolkaDotFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImagePolkaDotFilter.init(context);
            sImagePolkaDotFilter.setFractionalWidthOfAPixel(convertedParams[0]);
            sImagePolkaDotFilter.setDotScaling(convertedParams[1]);
            filter = sImagePolkaDotFilter;
        } else if(sImagePosterizeFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImagePosterizeFilter.init(context);
            sImagePosterizeFilter.setColorLevels((int)convertedParams[0]);
            filter = sImagePosterizeFilter;
        } else if(sImageSketchFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageSketchFilter.init(context);
            sImageSketchFilter.setWeight(convertedParams[0]);
            filter = sImageSketchFilter;
        } else if(sImageThresholdSketchFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageThresholdSketchFilter.init(context);
            sImageThresholdSketchFilter.setThreshold(convertedParams[0]);
            sImageThresholdSketchFilter.setWeight(convertedParams[1]);
            filter = sImageThresholdSketchFilter;
        } else if(sImageToonFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sImageToonFilter.init(context);
            sImageToonFilter.setThreshold(convertedParams[0]);
            sImageToonFilter.setQuantizationLevels(convertedParams[1]);
            sImageToonFilter.setWeight(convertedParams[2]);
            filter = sImageToonFilter;
        } else if(sPaidFilter1.getFilterInfo().filterName.equals(sFilterName)) {
            sPaidFilter1.initWithImage(context, subImages[0], subImages[1], convertedParams[1]);
            sPaidFilter1.setThreshold(convertedParams[0]);
            filter = sPaidFilter1;
        } else if(sPaidFilter2.getFilterInfo().filterName.equals(sFilterName)) {
            sPaidFilter2.init(context);
            sPaidFilter2.setHue(convertedParams[0]);
            sPaidFilter2.setWeight(convertedParams[1]);
            sPaidFilter2.setColorLevels((int)convertedParams[2]);
            filter = sPaidFilter2;
        } else if(sPaidFilter4.getFilterInfo().filterName.equals(sFilterName)) {
            sPaidFilter4.initWithImage(context, subImages[0], width, height);
            sPaidFilter4.setThreshold(convertedParams[0]);
            sPaidFilter4.setWeight(convertedParams[1]);
            sPaidFilter4.setRadius((int)convertedParams[2]);
            filter = sPaidFilter4;
        } else if(sPaidFilter5.getFilterInfo().filterName.equals(sFilterName)) {
            sPaidFilter5.initWithImage(context, subImages[0], convertedParams[1]);
            sPaidFilter5.setWeight(convertedParams[0], width, height);
            filter = sPaidFilter5;
        } else if(sPaidFilter6.getFilterInfo().filterName.equals(sFilterName)) {
            sPaidFilter6.initWithImage(context, subImages[0], subImages[1], subImages[2]);
            sPaidFilter6.setMin(convertedParams[0]);
            filter = sPaidFilter6;
        } else if(sPaidFilter7.getFilterInfo().filterName.equals(sFilterName)) {
            sPaidFilter7.initWithImage(context, width, height);
            sPaidFilter7.setWeight(convertedParams[0]);
            sPaidFilter7.setSaturation(convertedParams[1]);
            filter = sPaidFilter7;
        } else if(sPaidFilter8.getFilterInfo().filterName.equals(sFilterName)) {
            sPaidFilter8.initWithImage(context, width, height);
            sPaidFilter8.setSaturation(convertedParams[0]);
            filter = sPaidFilter8;
        } else if(sPaidFilter9.getFilterInfo().filterName.equals(sFilterName)) {
            sPaidFilter9.setThreshold(convertedParams[0]);
            sPaidFilter9.initWithImage(context, subImages[0], convertedParams[1]);
            filter = sPaidFilter9;
        } else if(sPaidFilter10.getFilterInfo().filterName.equals(sFilterName)) {
            sPaidFilter10.initWithImage(context,
                                        ArtFilterUtils.getWeightedParam(width, height, 1.f),
                                        subImages[0], subImages[1], convertedParams[0]);
            filter = sPaidFilter10;
        } else if(sPaidFilter11.getFilterInfo().filterName.equals(sFilterName)) {
            sPaidFilter11.initWithImage(context,
                                        ArtFilterUtils.getWeightedParam(width, height, 1.f),
                                        subImages[0]);
            sPaidFilter11.setRadius((int)convertedParams[0]);
            filter = sPaidFilter11;
        } else if(sPaidFilter13.getFilterInfo().filterName.equals(sFilterName)) {
            sPaidFilter13.initWithImage(context, width, height, subImages[0], subImages[1],
                                        subImages[2], convertedParams[0]);
            filter = sPaidFilter13;
        } else if(sPaidFilter15.getFilterInfo().filterName.equals(sFilterName)) {
            sPaidFilter15.initWithImage(context, convertedParams[4]);
            sPaidFilter15.setBlur1(convertedParams[0]);
            sPaidFilter15.setBlur2(convertedParams[1]);
            sPaidFilter15.setThreshold(convertedParams[2]);
            sPaidFilter15.setOpacity(convertedParams[3]);
            filter = sPaidFilter15;
        } else if(sSoftFilter.getFilterInfo().filterName.equals(sFilterName)) {
            sSoftFilter.initWithImage(context, convertedParams[0]);
            sSoftFilter.setBlur(convertedParams[1]);
            filter = sSoftFilter;
        }

        /*
         * if (!image.isRecycled()) { image.recycle(); System.gc(); } if (filter != null) {
         * ip.addTarget(filter); // two input filter. ImagePicture secondImage = null; if
         * (needsSecondImage) { secondImage = new ImagePicture(); secondImage.initWithImage(context,
         * subImages[0]); secondImage.processImage(); secondImage.addTarget(filter); }
         * ip.processImage(); //
         * -------------------------------------------------------------------------------- result =
         * getBitmapFromGL((int)filter.getOutputWidth(), (int)filter.getOutputHeight()); if
         * (result.getHeight() != image.getHeight() || result.getWidth() != image.getWidth()) {
         * Bitmap resized = Bitmap.createScaledBitmap(result, image.getWidth(), image.getHeight(),
         * false); result.recycle(); result = resized; } ip.destroyAll(); } for (int i = 0; i <
         * subImages.length; i++) { if (subImages[i] != null) { if (subImages[i] != null &&
         * !subImages[i].isRecycled()) { subImages[i].recycle(); } } }
         */

    }

    /**
     * 원본 이미지 바인딩
     */
    private static ImagePicture ip = new ImagePicture();
    /**
     * 각 필터에 필요한 텍스처 이미지
     */
    private static Bitmap[] subImages;
    /**
     * 현재 처리중인 필터
     */
    private static ImageInput filter;
    /**
     * blend 필터를 직접적으로 요청할 경우 사용되나, 실제 활용하고 있지는 않음
     */
    private static boolean needsSecondImage;
    /**
     * 현재 처리중인 필터 이름
     */
    private static String sFilterName;
    /**
     * 필터 파라미터
     */
    private static float[] convertedParams;

    /**
     * 텍스처 이미지를 불러오고, 필터를 초기화한다.
     */
    public static void initFilter(Context context, String filterName, float[] params, int width,
            int height) {
        if(subImages != null) {
            for(int i = 0; i < subImages.length; i++) {
                if(subImages[i] != null) {
                    if(subImages[i] != null && !subImages[i].isRecycled()) {
                        subImages[i].recycle();
                    }
                }
            }
        }
        sFilterName = filterName;
        subImages = initSubImages(context, filterName);

        convertedParams = getConvertParams(filterName, params, (width + height) / 2);
    }

    /**
     * 필터를 적용한다.
     */
    public static void processArtFilter(Context context, Bitmap image) {
        if(TextUtils.isEmpty(sFilterName)) {
            return;
        }
        try {
            ip.initWithImage(context, image);

            initFilter(context, image.getWidth(), image.getHeight());
            if(filter instanceof PaidFilter6) {
                sPaidFilter6.addColorBurnBlendFilterTarget(context, image);
            }

            if(filter != null) {
                ip.addTarget(filter);

                // two input filter.
                ImagePicture secondImage = null;
                if(needsSecondImage) {
                    secondImage = new ImagePicture();
                    secondImage.initWithImage(context, subImages[0]);
                    secondImage.processImage();
                    secondImage.addTarget(filter);
                }

                ip.processImage();

                getBitmapFromGL(image, image.getWidth(), image.getHeight());

                ip.destroyAll();
            }
        } catch(RuntimeException e) {
            Log.e(TAG, "processArtFilter failed", e);
        }
    }

    /**
     * 텍스처 이미지를 불러온다.
     */
    private static Bitmap[] initSubImages(Context context, String filterName) {
        Bitmap[] subImages = new Bitmap[3];
        InputStream[] is = new InputStream[3];
        try {
            if(sImageMosaicFilter.getFilterInfo().filterName.equals(filterName)) {
                subImages[0] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af_mosaic_squares",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            } else if(sPaidFilter5.getFilterInfo().filterName.equals(filterName)) {
                subImages[0] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af5_texture",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            } else if(sPaidFilter1.getFilterInfo().filterName.equals(filterName)) {
                subImages[0] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af1_texture1",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            } else if(sPaidFilter4.getFilterInfo().filterName.equals(filterName)) {
                subImages[0] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af4_texture1_bg",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            } else if(sPaidFilter10.getFilterInfo().filterName.equals(filterName)) {
                subImages[0] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af10_texture1",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            } else if(sPaidFilter13.getFilterInfo().filterName.equals(filterName)) {
                subImages[0] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af13_texture1",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            } else if(sPaidFilter6.getFilterInfo().filterName.equals(filterName)) {
                subImages[0] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af6_texture1",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            } else if(sPaidFilter11.getFilterInfo().filterName.equals(filterName)) {
                subImages[0] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af11_texture1",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            } else if(sPaidFilter9.getFilterInfo().filterName.equals(filterName)) {
                subImages[0] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af9_texture_re",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            }

            if(sPaidFilter1.getFilterInfo().filterName.equals(filterName)) {
                subImages[1] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af1_texture2",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            } else if(sPaidFilter10.getFilterInfo().filterName.equals(filterName)) {
                subImages[1] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af10_texture2",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            } else if(sPaidFilter13.getFilterInfo().filterName.equals(filterName)) {
                subImages[1] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af13_texture2",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            } else if(sPaidFilter6.getFilterInfo().filterName.equals(filterName)) {
                subImages[1] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af6_texture2",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            }

            if(sPaidFilter13.getFilterInfo().filterName.equals(filterName)) {
                subImages[2] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af13_texture3",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            } else if(sPaidFilter6.getFilterInfo().filterName.equals(filterName)) {
                subImages[2] = BitmapFactory.decodeResource(context.getResources(),
                                                            context.getResources()
                                                                   .getIdentifier("af6_texture3",
                                                                                  "drawable",
                                                                                  context.getApplicationContext()
                                                                                         .getPackageName()));
            }
        } finally {
            for(int i = 0; i < is.length; i++) {
                if(is[i] != null) {
                    try {
                        is[i].close();
                    } catch(IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        }
        return subImages;
    }

    /**
     * 필터 파라미터를 각 필터에 맞게 변환한다.
     */
    public static float[] getConvertParams(String filterName, float[] originalParams, int size) {
        float[] params = new float[6];
        boolean selectedFilter = false;
        for(ImageOutput filter : getAllFilterList()) {
            if(filter.getFilterInfo().filterName.equals(filterName)) {
                selectedFilter = true;
                ArrayList<ProgressInfo> progressInfo = filter.getFilterInfo().progressInfo;
                if(progressInfo != null) {
                    int i = 0;
                    for(ProgressInfo info : progressInfo) {
                        // kuwahara only
                        if((sImageKuwaharaFilter.getFilterInfo().filterName.equals(filter) && i == 0)
                                || (sPaidFilter11.getFilterInfo().filterName.equals(filter) && i == 0)) {
                            params[i] = originalParams != null ? (originalParams[i] * 2f)
                                    / info.valueToProgress + info.minValue : info.defaultValue;
                        } else if(sPaidFilter4.getFilterInfo().filterName.equals(filter) && i == 2) {
                            params[i] = originalParams != null ? (originalParams[i] * 1.5f)
                                    / info.valueToProgress + info.minValue : info.defaultValue;
                        } else {
                            params[i] = originalParams != null ? originalParams[i]
                                    / info.valueToProgress + info.minValue : info.defaultValue;
                        }
                        // 대상 이미지의 크기에 따라 파라미터 값의 변경이 필요한 경우.
                        if(info.needWeight) {
                            params[i] *= (size / 640.f);
                        }
                        i++;
                    }
                }
                break;
            }
        }
        if(selectedFilter) {
            return params;
        }
        return originalParams;
    }

    /**
     * 대상 이미지의 크기에 따라 파라미터 값의 변경
     */
    public static float getWeightedParam(int width, int height, float param) {
        return param * (width + height) / 2 / 640.f;
    }

    /**
     * 대상 이미지의 크기에 따라 파라미터 값의 변경
     */
    public static float getWeightedParam(float width, float height, float param) {
        if(width == 0 || height == 0) {
            return param;
        }
        return param * (width + height) / 2 / 640.f;
    }

    /**
     * 전체 필터 목록을 반환한다.
     */
    public static ArrayList<ImageOutput> getAllFilterList() {
        ArrayList<ImageOutput> filterList = new ArrayList<ImageOutput>();
        filterList.addAll(getColorFilterList());
        filterList.addAll(getImageFilterList());
        filterList.addAll(getBlendFilterList());
        filterList.addAll(getEffectFilterList());
        filterList.addAll(getCustomFilterList());
        return filterList;
    }

    public static ImageAdaptiveThresholdFilter sImageAdaptiveThresholdFilter = new ImageAdaptiveThresholdFilter();
    public static ImageChromaKeyFilter sImageChromaKeyFilter = new ImageChromaKeyFilter();
    public static ImageGrayscaleFilter sImageGrayscaleFilter = new ImageGrayscaleFilter();
    public static ImageHueFilter sImageHueFilter = new ImageHueFilter();
    public static ImageLevelsFilter sImageLevelsFilter = new ImageLevelsFilter();
    public static ImageLuminanceThresholdFilter sImageLuminanceThresholdFilter = new ImageLuminanceThresholdFilter();
    public static ImageOpacityFilter sImageOpacityFilter = new ImageOpacityFilter();
    public static ImageSaturationFilter sImageSaturationFilter = new ImageSaturationFilter();
    public static ImageHueSaturationFilterL sImageHueSaturationFilterL = new ImageHueSaturationFilterL();
    public static ImageColorTemperatureFilter sImageColorTemperatureFilter = new ImageColorTemperatureFilter();

    /**
     * color 필터 목록을 반환한다.
     */
    public static ArrayList<ImageOutput> getColorFilterList() {
        ArrayList<ImageOutput> filterList = new ArrayList<ImageOutput>();
        filterList.add(sImageAdaptiveThresholdFilter);
        filterList.add(sImageChromaKeyFilter);
        filterList.add(sImageGrayscaleFilter);
        filterList.add(sImageHueFilter);
        filterList.add(sImageLevelsFilter);
        filterList.add(sImageLuminanceThresholdFilter);
        filterList.add(sImageOpacityFilter);
        filterList.add(sImageSaturationFilter);
        filterList.add(sImageHueSaturationFilterL);
        filterList.add(sImageColorTemperatureFilter);
        return filterList;
    }

    public static ImageBoxBlurFilter sImageBoxBlurFilter = new ImageBoxBlurFilter();
    public static ImageDilationFilter sImageDilationFilter = new ImageDilationFilter();
    public static ImageDirectionalDetectionFilter sImageDirectionalDetectionFilter = new ImageDirectionalDetectionFilter();
    public static ImageErosionFilter sImageErosionFilter = new ImageErosionFilter();
    public static ImageGaussianBlurFilter sImageGaussianBlurFilter = new ImageGaussianBlurFilter();
    public static ImageOpeningFilter sImageOpeningFilter = new ImageOpeningFilter();
    public static ImageRGBDilationFilter sImageRGBDilationFilter = new ImageRGBDilationFilter();
    public static ImageRGBErosionFilter sImageRGBErosionFilter = new ImageRGBErosionFilter();
    public static ImageRGBOpeningFilter sImageRGBOpeningFilter = new ImageRGBOpeningFilter();
    public static ImageSobelEdgeDetectionFilter sImageSobelEdgeDetectionFilter = new ImageSobelEdgeDetectionFilter();
    public static ImageThresholdEdgeDetectionFilter sImageThresholdEdgeDetectionFilter = new ImageThresholdEdgeDetectionFilter();
    public static ImageUnsharpMaskFilter sImageUnsharpMaskFilter = new ImageUnsharpMaskFilter();
    public static ImageSmartBlurFilter sImageSmartBlurFilter = new ImageSmartBlurFilter();

    /**
     * image processing 필터 목록을 반환한다.
     */
    public static ArrayList<ImageOutput> getImageFilterList() {
        ArrayList<ImageOutput> filterList = new ArrayList<ImageOutput>();
        filterList.add(sImageBoxBlurFilter);
        filterList.add(sImageDilationFilter);
        filterList.add(sImageDirectionalDetectionFilter);
        filterList.add(sImageErosionFilter);
        filterList.add(sImageGaussianBlurFilter);
        filterList.add(sImageOpeningFilter);
        filterList.add(sImageRGBDilationFilter);
        filterList.add(sImageRGBErosionFilter);
        filterList.add(sImageRGBOpeningFilter);
        filterList.add(sImageSobelEdgeDetectionFilter);
        filterList.add(sImageThresholdEdgeDetectionFilter);
        filterList.add(sImageUnsharpMaskFilter);
        filterList.add(sImageSmartBlurFilter);
        return filterList;
    }

    public static ImageAlphaBlendFilter sImageAlphaBlendFilter = new ImageAlphaBlendFilter();
    public static ImageAlphaMaskBlendFilterL sImageAlphaMaskBlendFilterL = new ImageAlphaMaskBlendFilterL();
    public static ImageChromaKeyBlendFilter sImageChromaKeyBlendFilter = new ImageChromaKeyBlendFilter();
    public static ImageColorBurnBlendFilter sImageColorBurnBlendFilter = new ImageColorBurnBlendFilter();
    public static ImageColorDodgeBlendFilter sImageColorDodgeBlendFilter = new ImageColorDodgeBlendFilter();
    public static ImageDarkenBlendFilter sImageDarkenBlendFilter = new ImageDarkenBlendFilter();
    public static ImageDissolveBlendFilter sImageDissolveBlendFilter = new ImageDissolveBlendFilter();
    public static ImageLuminanceThresholdBlendFilterL sImageLuminanceThresholdBlendFilterL = new ImageLuminanceThresholdBlendFilterL();
    public static ImageMaskBlendFilterL sImageMaskBlendFilterL = new ImageMaskBlendFilterL();
    public static ImageMultiplyBlendFilter sImageMultiplyBlendFilter = new ImageMultiplyBlendFilter();
    public static ImageNormalBlendFilter sImageNormalBlendFilter = new ImageNormalBlendFilter();
    public static ImageOpacityMaskBlendFilter sImageOpacityMaskBlendFilter = new ImageOpacityMaskBlendFilter();
    public static ImageOverlayBlendFilter sImageOverlayBlendFilter = new ImageOverlayBlendFilter();
    public static ImageScreenBlendFilter sImageScreenBlendFilter = new ImageScreenBlendFilter();
    public static ImageSoftLightBlendFilter sImageSoftLightBlendFilter = new ImageSoftLightBlendFilter();

    /**
     * blend 필터 목록을 반환한다.
     */
    public static ArrayList<ImageOutput> getBlendFilterList() {
        ArrayList<ImageOutput> filterList = new ArrayList<ImageOutput>();
        filterList.add(sImageAlphaBlendFilter);
        filterList.add(sImageAlphaMaskBlendFilterL);
        filterList.add(sImageChromaKeyBlendFilter);
        filterList.add(sImageColorBurnBlendFilter);
        filterList.add(sImageColorDodgeBlendFilter);
        filterList.add(sImageDarkenBlendFilter);
        filterList.add(sImageDissolveBlendFilter);
        filterList.add(sImageLuminanceThresholdBlendFilterL);
        filterList.add(sImageMaskBlendFilterL);
        filterList.add(sImageMultiplyBlendFilter);
        filterList.add(sImageNormalBlendFilter);
        filterList.add(sImageOpacityMaskBlendFilter);
        filterList.add(sImageOverlayBlendFilter);
        filterList.add(sImageScreenBlendFilter);
        filterList.add(sImageSoftLightBlendFilter);
        return filterList;
    }

    public static ImageCrosshatchFilter sImageCrosshatchFilter = new ImageCrosshatchFilter();
    public static ImageHalftoneFilter sImageHalftoneFilter = new ImageHalftoneFilter();
    public static ImageKuwaharaFilter sImageKuwaharaFilter = new ImageKuwaharaFilter();
    public static ImageMaskFilter sImageMaskFilter = new ImageMaskFilter();
    public static ImageMosaicFilter sImageMosaicFilter = new ImageMosaicFilter();
    public static ImagePinchDistortionFilter sImagePinchDistortionFilter = new ImagePinchDistortionFilter();
    public static ImageShiftToTopFilterL sImageShiftToTopFilterL = new ImageShiftToTopFilterL();
    public static ImageShiftToVerticalCenterFilterL sImageShiftToVerticalCenterFilterL = new ImageShiftToVerticalCenterFilterL();
    public static ImageBigEyeFilterL sImageBigEyeFilterL = new ImageBigEyeFilterL();
    public static ImageSmallNoseFilterL sImageSmallNoseFilterL = new ImageSmallNoseFilterL();
    public static ImageDirectionalShiftFilterL sImageDirectionalShiftFilterL = new ImageDirectionalShiftFilterL();
    public static ImagePixellateFilter sImagePixellateFilter = new ImagePixellateFilter();
    public static ImagePolkaDotFilter sImagePolkaDotFilter = new ImagePolkaDotFilter();
    public static ImagePosterizeFilter sImagePosterizeFilter = new ImagePosterizeFilter();
    public static ImageSketchFilter sImageSketchFilter = new ImageSketchFilter();
    public static ImageThresholdSketchFilter sImageThresholdSketchFilter = new ImageThresholdSketchFilter();
    public static ImageToonFilter sImageToonFilter = new ImageToonFilter();

    /**
     * effect 필터 목록을 반환한다.
     */
    public static ArrayList<ImageOutput> getEffectFilterList() {
        ArrayList<ImageOutput> filterList = new ArrayList<ImageOutput>();
        filterList.add(sImageCrosshatchFilter);
        filterList.add(sImageHalftoneFilter);
        filterList.add(sImageKuwaharaFilter);
        filterList.add(sImageMaskFilter);
        filterList.add(sImageMosaicFilter);
        filterList.add(sImagePinchDistortionFilter);
        filterList.add(sImageShiftToTopFilterL);
        filterList.add(sImageShiftToVerticalCenterFilterL);
        filterList.add(sImageBigEyeFilterL);
        filterList.add(sImageSmallNoseFilterL);
        filterList.add(sImageDirectionalShiftFilterL);
        filterList.add(sImagePixellateFilter);
        filterList.add(sImagePolkaDotFilter);
        filterList.add(sImagePosterizeFilter);
        filterList.add(sImageSketchFilter);
        filterList.add(sImageThresholdSketchFilter);
        filterList.add(sImageToonFilter);
        return filterList;
    }

    public static PaidFilter1 sPaidFilter1 = new PaidFilter1();
    public static PaidFilter2 sPaidFilter2 = new PaidFilter2();
    public static PaidFilter4 sPaidFilter4 = new PaidFilter4();
    public static PaidFilter5 sPaidFilter5 = new PaidFilter5();
    public static PaidFilter6 sPaidFilter6 = new PaidFilter6();
    public static PaidFilter7 sPaidFilter7 = new PaidFilter7();
    public static PaidFilter8 sPaidFilter8 = new PaidFilter8();
    public static PaidFilter9 sPaidFilter9 = new PaidFilter9();
    public static PaidFilter10 sPaidFilter10 = new PaidFilter10();
    public static PaidFilter11 sPaidFilter11 = new PaidFilter11();
    public static PaidFilter13 sPaidFilter13 = new PaidFilter13();
    public static PaidFilter15 sPaidFilter15 = new PaidFilter15();
    public static SoftFilter sSoftFilter = new SoftFilter();

    /**
     * custom 필터 목록을 반환한다.
     */
    public static ArrayList<ImageOutput> getCustomFilterList() {
        ArrayList<ImageOutput> filterList = new ArrayList<ImageOutput>();
        filterList.add(sPaidFilter1);
        filterList.add(sPaidFilter2);
        filterList.add(sPaidFilter4);
        filterList.add(sPaidFilter5);
        filterList.add(sPaidFilter6);
        filterList.add(sPaidFilter7);
        filterList.add(sPaidFilter8);
        filterList.add(sPaidFilter9);
        filterList.add(sPaidFilter10);
        filterList.add(sPaidFilter11);
        filterList.add(sPaidFilter13);
        filterList.add(sPaidFilter15);
        filterList.add(sSoftFilter);
        return filterList;
    }
}
