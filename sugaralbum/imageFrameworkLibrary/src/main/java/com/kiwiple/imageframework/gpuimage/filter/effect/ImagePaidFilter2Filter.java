
package com.kiwiple.imageframework.gpuimage.filter.effect;

import java.util.ArrayList;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.gpuimage.ArtFilterInfo;
import com.kiwiple.imageframework.gpuimage.ProgressInfo;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

/**
 * custom 필터 개발을 위해 추가. 여러 필터를 하나 shader code로 합쳐서 메모리 사용을 최소하 하기 위함.
 * Level + Posterize + Hue
 */
public class ImagePaidFilter2Filter extends ImageFilter {
    private int mMinUniform;
    private int mMidUniform;
    private int mMaxUniform;
    private int mMinOutputUniform;
    private int mMaxOutputUniform;

    // Posterize
    private int mColorLevelsUniform;

    // Hue
    private int mHueAdjustUniform;

    private Vector3 mMinVector = new Vector3();
    private Vector3 mMidVector = new Vector3();
    private Vector3 mMaxVector = new Vector3();
    private Vector3 mMinOutputVector = new Vector3();
    private Vector3 mMaxOutputVector = new Vector3();

    private ArtFilterInfo mArtFilterInfo;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "paid_filter_2_fragment");

        mMinUniform = GLES20.glGetUniformLocation(mProgram, "minValue");
        mMidUniform = GLES20.glGetUniformLocation(mProgram, "midValue");
        mMaxUniform = GLES20.glGetUniformLocation(mProgram, "maxValue");
        mMinOutputUniform = GLES20.glGetUniformLocation(mProgram, "minOutput");
        mMaxOutputUniform = GLES20.glGetUniformLocation(mProgram, "maxOutput");

        setRedMin(0.f, 1.f, 1.f, 0.f, 1.f);
        setGreenMin(0.f, 1.f, 1.f, 0.f, 1.f);
        setBlueMin(0.f, 1.f, 1.f, 0.f, 1.f);

        mColorLevelsUniform = GLES20.glGetUniformLocation(mProgram, "colorLevels");

        setColorLevels(10);

        mHueAdjustUniform = GLES20.glGetUniformLocation(mProgram, "hueAdjust");
        if(mHueAdjustUniform != -1)
            setHue(90.f);
    }

    public void updateUniforms() {
        setVec3(mMinVector, mMinUniform, mProgram);
        setVec3(mMidVector, mMidUniform, mProgram);
        setVec3(mMaxVector, mMaxUniform, mProgram);
        setVec3(mMinOutputVector, mMinOutputUniform, mProgram);
        setVec3(mMaxOutputVector, mMaxOutputUniform, mProgram);
    }

    public void setMin(float min, float mid, float max, float minOut, float maxOut) {
        setRedMin(min, mid, max, minOut, maxOut);
        setGreenMin(min, mid, max, minOut, maxOut);
        setBlueMin(min, mid, max, minOut, maxOut);
    }

    public void setMin(float min, float mid, float max) {
        setMin(min, mid, max, 0.0f, 1.0f);
    }

    public void setRedMin(float min, float mid, float max, float minOut, float maxOut) {
        mMinVector.one = min;
        mMidVector.one = mid;
        mMaxVector.one = max;
        mMinOutputVector.one = minOut;
        mMaxOutputVector.one = maxOut;

        updateUniforms();
    }

    public void setRedMin(float min, float mid, float max) {
        setRedMin(min, mid, max, 0.0f, 1.0f);
    }

    public void setGreenMin(float min, float mid, float max, float minOut, float maxOut) {
        mMinVector.two = min;
        mMidVector.two = mid;
        mMaxVector.two = max;
        mMinOutputVector.two = minOut;
        mMaxOutputVector.two = maxOut;

        updateUniforms();
    }

    public void setGreenMin(float min, float mid, float max) {
        setRedMin(min, mid, max, 0.0f, 1.0f);
    }

    public void setBlueMin(float min, float mid, float max, float minOut, float maxOut) {
        mMinVector.three = min;
        mMidVector.three = mid;
        mMaxVector.three = max;
        mMinOutputVector.three = minOut;
        mMaxOutputVector.three = maxOut;

        updateUniforms();
    }

    public void setBlueMin(float min, float mid, float max) {
        setRedMin(min, mid, max, 0.0f, 1.0f);
    }

    public void setColorLevels(int newValue) {
        setFloat(newValue, mColorLevelsUniform, mProgram);
    }

    public void setHue(float newHue) {
        float hue = (float)((newHue % 360.0f) * Math.PI / 180.f);
        setFloat(hue, mHueAdjustUniform, mProgram);
    }

    @Override
    public ArtFilterInfo getFilterInfo() {
        if(mArtFilterInfo == null) {
            ArrayList<ProgressInfo> progressInfo = new ArrayList<ProgressInfo>(5);
            progressInfo.add(new ProgressInfo(1.f, .0f, .0f, 100.f, "Low"));
            progressInfo.add(new ProgressInfo(1.f, .0f, 1.f, 100.f, "Midium"));
            progressInfo.add(new ProgressInfo(1.f, .0f, 1.f, 100.f, "High"));
            progressInfo.add(new ProgressInfo(11.f, 1.f, 9.f, 1.f, "Posterize"));
            progressInfo.add(new ProgressInfo(360.f, 0.f, 90.f, 1.f, "Hue"));
            mArtFilterInfo = new ArtFilterInfo("PaidFilter2 Filter", progressInfo);
        }
        return mArtFilterInfo;
    }
}
