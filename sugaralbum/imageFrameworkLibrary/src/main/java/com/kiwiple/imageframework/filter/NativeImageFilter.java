
package com.kiwiple.imageframework.filter;

import java.nio.ByteBuffer;

/**
 * RGB 기반 필터 처리를 제공하는 클래스
 * 
 * @version 2.0
 */
public class NativeImageFilter {
    /**
     * curve값을 이용한 이미지처리 기능을 제공한다.
     * 
     * @param bitmap 원본 이미지
     * @param width 이미지 가로 길이
     * @param height 이미지 세로 길이
     * @param all 모든 RGB 채널의 curve값 목록.
     * @param r Red 채널의 curve값 목록
     * @param g Green 채널의 curve값 목록
     * @param b Blue 채널의 curve값 목록
     * @remark all, r, g, b 파라미터는 [input color, output color ...]의 배열 구성되며 input color와 output
     *         color값은 0~255의 범위를 가진다.
     * @version 2.0
     */
    public static native void curveProcessing(ByteBuffer bitmap, int width, int height,
            short[] all, short[] r, short[] g, short[] b);

    /**
     * 대비값을 이용한 이미지처리 기능을 제공한다.
     * 
     * @param bitmap 원본 이미지
     * @param width 이미지 가로 길이
     * @param height 이미지 세로 길이
     * @param contrast 대비 값
     * @remark contrast는 0.5~1.5의 값을 가진다.<br>
     *         1.0: 기본 값<br>
     *         0.5: 낮은 대비 값<br>
     *         1.5: 높은 대비 값
     * @version 2.0
     */
    public static native void contrastProcessing(ByteBuffer bitmap, int width, int height,
            float contrast);

    /**
     * 밝기값을 이용한 이미지처리 기능을 제공한다.
     * 
     * @param bitmap 원본 이미지
     * @param width 이미지 가로 길이
     * @param height 이미지 세로 길이
     * @param brightness 밝기 값
     * @remark brightness는 밝기 값으로 -100~100의 값을 가진다. <br>
     *         0: 기본 값<br>
     *         -100: 가장 어두운 값<br>
     *         100: 가장 밝은 값
     * @version 2.0
     */
    public static native void brightnessProcessing(ByteBuffer bitmap, int width, int height,
            int brightness);

    /**
     * 원본 이미지에 텍스처 이미지를 합성한다.
     * 
     * @param bitmap 원본 이미지
     * @param texture 텍스처 이미지
     * @param width 이미지 가로 길이
     * @param height 이미지 세로 길이
     * @param alpha 텍스처 이미지의 두명도
     * @remark alpha는 텍스처 이미지의 투명도로 0~100의 값을 가진다.<br>
     *         0: 투명<br>
     *         50: 반 투명<br>
     *         100: 불투명
     * @version 2.0
     */
    public static native void textureProcessing(ByteBuffer bitmap, ByteBuffer texture, int width,
            int height, int alpha);

    public static native short[] getSpline(short[] pointList);

    /**
     * 이미지를 단색으로 변경한다.
     * 
     * @param bitmap 원본 이미지
     * @param width 이미지 가로 길이
     * @param height 이미지 세로 길이
     * @version 2.0
     */
    public static native void grayProcessing(ByteBuffer bitmap, int width, int height);

    /**
     * 채도를 이용한 이미지처리 기능을 제공한다.
     * 
     * @param bitmap 원본 이미지
     * @param width 이미지 가로 길이
     * @param height 이미지 세로 길이
     * @param saturation 채도 값
     * @remark saturation은 채도 값으로 0.0~2.0의 값을 가진다.<br>
     *         1.0: 기본 값<br>
     *         0.0: 낮은 채도 값<br>
     *         1.0: 높은 채도 값
     * @version 2.0
     */
    public static native void saturationProcessing(ByteBuffer bitmap, int width, int height,
            float saturation);

    /**
     * ByteBuffer에 메모리 공간을 할당한다.
     * 
     * @param size
     * @return
     * @version 2.0
     */
    public static native Object allocByteBuffer(int size);

    /**
     * ByteBuffer에 할당된 메모리 공간을 해제한다.
     * 
     * @param buffer
     * @version 2.0
     */
    public static native void freeByteBuffer(ByteBuffer buffer);

    /**
     * YUV 이미지를 RGB 이미지로 변환한다.
     * 
     * @param pixels RGB 이미지
     * @param yuv YUV 이미지
     * @param width 이미지 가로 길이
     * @param height 이미지 세로 길이
     * @version 2.0
     */
    public static native void YUVtoRBG(int[] pixels, byte[] yuv, int width, int height);

    /**
     * RGB 이미지를 YUV 이미지로 변환한다.
     * 
     * @param pixels RGB 이미지
     * @param yuv YUV 이미지
     * @param width 이미지 가로 길이
     * @param height 이미지 세로 길이
     * @version 2.0
     */
    public static native void RGBtoYUV(int[] pixels, byte[] yuv, int width, int height);

    public static native void RGBtoYUV2(byte[] pixels, byte[] yuv, int width, int height);

    static {
        System.loadLibrary("native_filter");
    }
}
