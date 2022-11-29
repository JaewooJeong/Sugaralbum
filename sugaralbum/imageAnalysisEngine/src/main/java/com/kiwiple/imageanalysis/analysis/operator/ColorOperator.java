
package com.kiwiple.imageanalysis.analysis.operator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import android.graphics.Color;

import com.kiwiple.imageanalysis.utils.SmartLog;

/**
 * 이미지의 컬러셋의 분석 클래스<br>
 * 이미지 1개에 대한 4x4 컬러셋 분석을 한다.<br>
 * 이미지의 4x4 컬러셋은 QualityOperator에서 구할 수 있다.<br>
 * jpeg 포멧만 가능
 */
public class ColorOperator {

    private static final String TAG = ColorOperator.class.getSimpleName();

    // 컬러셋
    private Map<String, Integer> mColorset;
    private Map<String, Integer> mBnWColorset;

    private int mAvgBrightnessValue;
    private float mSumBrightnessValue;

    private int mAvgLuminanceValue;
    private float mSumLuminanceValue;

    private ArrayList<String> mRepresent16ColorNames = new ArrayList<String>();

    /**
     * 생성자 클래스 (컬러셋을 셋팅한다)
     */
    public ColorOperator() {
        // 컬러셋을 셋팅
        setColorSet();
    }

    private void setColorSet() {
        mColorset = new HashMap<String, Integer>();
        mColorset.put("white", Color.rgb(255, 255, 255));
        mColorset.put("yellow", Color.rgb(255, 255, 0));
        mColorset.put("magenta", Color.rgb(255, 0, 255));
        mColorset.put("red", Color.rgb(255, 0, 0));
        mColorset.put("gray", Color.rgb(128, 128, 128));
        mColorset.put("olive", Color.rgb(128, 128, 0));
        mColorset.put("purple", Color.rgb(128, 0, 128));
        mColorset.put("maroon", Color.rgb(128, 0, 0));
        mColorset.put("cyan", Color.rgb(0, 255, 255));
        mColorset.put("lime", Color.rgb(0, 255, 0));
        mColorset.put("teal", Color.rgb(0, 128, 128));
        mColorset.put("green", Color.rgb(0, 128, 0));
        mColorset.put("blue", Color.rgb(0, 0, 255));
        mColorset.put("navy", Color.rgb(0, 0, 128));
        mColorset.put("black", Color.rgb(0, 0, 0));

        mBnWColorset = new HashMap<String, Integer>();
        mBnWColorset.put("white", Color.rgb(255, 255, 255));
        mBnWColorset.put("black", Color.rgb(0, 0, 0));
        mBnWColorset.put("gray", Color.rgb(128, 128, 128));
    }

    /**
     * 대표색으로 정의된 15색의 이름의 배열을 반환한다.
     * 
     * @return ArrayList 대표 15색의 이름 배열
     */
    public static ArrayList<String> getColorSetNames() {
        ArrayList<String> colorNames = new ArrayList<String>();
        colorNames.add("white");
        colorNames.add("yellow");
        colorNames.add("magenta");
        colorNames.add("red");
        colorNames.add("gray");
        colorNames.add("olive");
        colorNames.add("purple");
        colorNames.add("maroon");
        colorNames.add("cyan");
        colorNames.add("lime");
        colorNames.add("teal");
        colorNames.add("green");
        colorNames.add("blue");
        colorNames.add("navy");
        colorNames.add("black");
        return colorNames;
    }

    /**
     * 컬러셋(16분할)에서 대표색을 추출해낸다. <br>
     * 각 16분할의 대표색을 구하고, 16개의 대표색 중 가장 빈도가 많은 색의 이름을 반환.
     * 
     * @param colorSet 4x4의 RGB String값
     * @return String 가장 빈도가 많은 색의 이름
     */
    public String getRepresentColorName(String colorSet) {
        String[] colorArr = colorSet.split(" ");
        // 컬러가 16분할이 아니라면
        if(colorArr.length < 16) {
            SmartLog.e(TAG, "color analysis fail");
            return null;
        }

        // 밝기 정도를 구해보자
        // 일단 수치를 초기화하고
        resetValues();

        Map<String, Integer> colorCountset = new HashMap<String, Integer>();
        for(int i = 0; i < colorArr.length; i++) {
            String colorString = colorArr[i];
            // 대표색을 컬러셋에서 뽑는다.
            String representColorName = getColorStringFromColorset(colorString);
            if(representColorName != null) {
                mRepresent16ColorNames.add(representColorName);
                if(colorCountset.containsKey(representColorName)) {
                    Integer count = colorCountset.get(representColorName);
                    count++;
                    colorCountset.put(representColorName, count);
                } else {
                    colorCountset.put(representColorName, 1);
                }
            }
        }

        mAvgBrightnessValue = (int)((mSumBrightnessValue / colorArr.length) * 100);
        mAvgLuminanceValue = (int)((mSumLuminanceValue / colorArr.length) * 100);

        // 전체 대표색을 뽑아보자
        String maxColorName = null;
        int maxColorCount = 0;
        Iterator<String> iterator = colorCountset.keySet().iterator();
        while(iterator.hasNext()) {
            String colorName = (String)iterator.next();
            int colorCount = colorCountset.get(colorName);

            if(colorCount > maxColorCount) {
                maxColorCount = colorCount;
                maxColorName = colorName;
            }
        }
        return maxColorName;
    }

    /**
     * 반환 값들을 초기화한다.
     */
    public void resetValues() {
        mAvgBrightnessValue = 0;
        mSumBrightnessValue = 0.f;

        mAvgLuminanceValue = 0;
        mSumLuminanceValue = 0.f;

        mRepresent16ColorNames.clear();
    }

    /**
     * 현재 분석중인 이미지의 밝기 (Brightness) 값을 반환<br>
     * getRepresentColorName() 이후에 값이 계산됨.
     * 
     * @return int 밝기 값. range 0(어두움) ~ 100 (밝음)
     */
    public int getAvgBrightnessValue() {
        return mAvgBrightnessValue;
    }

    /**
     * 현재 분석 중인 이미지의 밝기 (Luminance) 값을 반환 <br>
     * getRepresentColorName() 이후에 값이 계산됨.
     * 
     * @return int 밝기 값. range 0(어두움) ~ 100 (밝음)
     */
    public int getAvgLuminanceValue() {
        return mAvgLuminanceValue;
    }

    /**
     * 4x4 컬러셋의 대표색 배열을 반환 <br>
     * mRepresent16ColorNames()이후에 값이 계산됨.
     * 
     * @return ArrayList 4x4 컬러셋의 대표색 배열
     */
    public ArrayList<String> getRepresent16ColorNames() {
        return mRepresent16ColorNames;
    }

    /**
     * 이미지의 대표색이 몇종류인지 반환해준다. <br>
     * 4x4의 16개의 대표색 중 중복을 제거한 Count의 반환<br>
     * getRepresentColorName() 이후에 값이 계산됨.
     * 
     * @return int 이미지의 대표색 갯수
     */
    public int getRepresentColorCount() {
        int representColorCount = 0;
        HashSet<String> hs = new HashSet<String>(mRepresent16ColorNames);
        ArrayList<String> resultList = new ArrayList<String>(hs);
        if(resultList != null) {
            representColorCount = resultList.size();
        }
        return representColorCount;
    }

    // 컬러셋 중에 어느 컬러에 가까운지를 추출
    private String getColorStringFromColorset(String rgbColorString) {
        if(rgbColorString.length() > 5) {
            int inputColor = Integer.parseInt(rgbColorString, 16);
            int r = (inputColor >> 16) & 0xFF;
            int g = (inputColor >> 8) & 0xFF;
            int b = (inputColor >> 0) & 0xFF;

            mSumLuminanceValue += (r * 0.2126f + g * 0.7152f + b * 0.0722f) / 255;
            return getBestMatchingHsvColor(Color.rgb(r, g, b));

        } else {
            return null;
        }
    }

    private String getBestMatchingHsvColor(int pixelColor) {
        // largest difference is 360(H), 1(S), 1(V)
        float currentDifference = 360 + 1 + 1;
        // name of the best matching colour
        String closestColorName = null;
        // get HSV values for the pixel's colour
        float[] pixelColorHsv = new float[3];
        Color.colorToHSV(pixelColor, pixelColorHsv);

        // 밝기 정도를 체크해보자.
        if(pixelColorHsv.length > 2) {
            mSumBrightnessValue += pixelColorHsv[2];
        }

        Iterator<String> colorNameIterator = mColorset.keySet().iterator();
        // continue iterating if the map contains a next colour and the difference is greater than
        // zero.
        // a difference of zero means we've found an exact match, so there's not point in iterating
        // further.
        while(colorNameIterator.hasNext() && currentDifference > 0) {
            // this colour's name
            String currentColorName = colorNameIterator.next();
            // this colour's int value
            int color = mColorset.get(currentColorName);
            // get HSV values for this colour
            float[] colorHsv = new float[3];
            Color.colorToHSV(color, colorHsv);
            // calculate sum of absolute differences that indicates how good this match is
            float difference = Math.abs(pixelColorHsv[0] - colorHsv[0])
                    + Math.abs(pixelColorHsv[1] - colorHsv[1])
                    + Math.abs(pixelColorHsv[2] - colorHsv[2]);
            // a smaller difference means a better match, so store it
            if(currentDifference > difference) {
                currentDifference = difference;
                closestColorName = currentColorName;
            }
        }
        return closestColorName;
    }
}
