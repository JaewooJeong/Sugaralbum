
package com.kiwiple.imageframework.ia;

class AutoIntelligentUtils {
    // 1:left, 2:top, 3:right, 4:bottom 5: center
    public static float getCurrentBrightness(byte[] pix, int index, int width, int height) {
        float brightness = 0;
        int count;
        switch(index) {
            case 0:
                count = 10;
                for(int i = 0; i < height * width; i += count) {
                    int y = (0xff & (pix[i])) - 16;
                    if(y < 0)
                        y = 0;
                    brightness += y;
                }
                brightness = brightness / (height * width / count);
                break;
            case 1:
                count = height * (width / 3);
                for(int i = 0; i < height; i++) {
                    for(int j = 0; j < width / 3; j++) {
                        int y = (0xff & pix[i * width + j]) - 16;
                        if(y < 0)
                            y = 0;
                        brightness += y;
                    }
                }
                brightness = brightness / count;
                break;
            case 2:
                count = (height / 3) * width;
                for(int i = 0; i < count; i++) {
                    int y = (0xff & pix[i]) - 16;
                    if(y < 0)
                        y = 0;
                    brightness += y;
                }
                brightness = brightness / count;
                break;
            case 3:
                count = height * (width / 3);
                for(int i = 0; i < height; i++) {
                    for(int j = (width / 3) * 2; j < width; j++) {
                        int y = (0xff & pix[i * width + j]) - 16;
                        if(y < 0)
                            y = 0;
                        brightness += y;
                    }
                }
                brightness = brightness / count;
                break;
            case 4:
                count = (height / 3) * width;
                for(int i = (height / 3) * 2 * width; i < height * width; i++) {
                    int y = (0xff & pix[i]) - 16;
                    if(y < 0)
                        y = 0;
                    brightness += y;
                }
                brightness = brightness / count;
                break;
            case 5:
                // center
                count = height * width / 9;
                for(int i = height / 3; i < height * 2 / 3; i++) {
                    for(int j = width / 3; j < width * 2 / 3; j++) {
                        int y = (0xff & pix[i * width + j]) - 16;
                        if(y < 0)
                            y = 0;
                        brightness += y;
                    }
                }
                brightness = brightness / count;
                break;
        }
        return brightness;
    }

    public static boolean isSunLight(float... brightness) {
        for(int i = 0; i <= brightness.length - 2; i++) {
            for(int j = i + 1; j <= brightness.length - 1; j++) {
                if(Math.abs(brightness[i] - brightness[j]) > 70) {
                    return true;
                }
            }
        }
        return false;
    }

    public static float isLowBrightness(float brightness, boolean isPatial) {
        float threshold = isPatial ? 50 : 80;
        return brightness < 0 || brightness > threshold ? 0 : threshold - brightness;
    }

    public static float isHighBrightness(float brightness, boolean isPatial) {
        float threshold = isPatial ? 110 : 150;
        return brightness < threshold || brightness > 255 ? 0 : brightness - threshold;
    }

    public static float max(float... values) {
        float maxValue = 0;
        for(float value : values) {
            if(value > maxValue) {
                maxValue = value;
            }
        }
        return maxValue;
    }
}
