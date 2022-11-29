
package com.kiwiple.imageframework.gpuimage.filter.colorprocessing;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import android.content.Context;
import android.opengl.GLES20;

import com.kiwiple.imageframework.filter.CurvesPoint;
import com.kiwiple.imageframework.gpuimage.filter.ImageFilter;

/**
 * JNI로 구현된 필터를 OpenGL로 전환하기 위해 만든 메쏘드. 현재 개발 중단된 코드.
 */
public class ImagePrimitiveRGBFilter extends ImageFilter {
    private int mGrayscaleUniform;

    // curve
    private int mToneCurveTextureUniform;
    private int[] mToneCurveTexture = new int[1];
    private ByteBuffer mToneCurveByteArray;
    ArrayList<Float> mRedCurve = new ArrayList<Float>();
    ArrayList<Float> mGreenCurve = new ArrayList<Float>();
    ArrayList<Float> mBlueCurve = new ArrayList<Float>();
    ArrayList<Float> mRgbCompositeCurve = new ArrayList<Float>();

    private int mSaturationUniform;
    private int mBrightnessUniform;
    private int mContrastUniform;

    @Override
    public void init(Context context) {
        super.initWithFragmentShaderFromResource(context, "primitive_rgb_fragment");

        mGrayscaleUniform = GLES20.glGetUniformLocation(mProgram, "grayscale");
        if(mGrayscaleUniform != -1)
            setGrayScale(false);
        mToneCurveTextureUniform = GLES20.glGetUniformLocation(mProgram, "toneCurveTexture");
        if(mToneCurveTextureUniform != -1) {
            ArrayList<CurvesPoint> defaultCurve = new ArrayList<CurvesPoint>();
            defaultCurve.add(new CurvesPoint(0, 0));
            // defaultCurve.add(new CurvesPoint(128, 128));
            defaultCurve.add(new CurvesPoint(255, 255));
            setRgbCompositeControlPoints(defaultCurve);
            setRedControlPoints(defaultCurve);
            setGreenControlPoints(defaultCurve);
            setBlueControlPoints(defaultCurve);
        }
        mSaturationUniform = GLES20.glGetUniformLocation(mProgram, "saturation");
        if(mSaturationUniform != -1)
            setSaturation(1.f);
        mBrightnessUniform = GLES20.glGetUniformLocation(mProgram, "brightness");
        if(mBrightnessUniform != -1)
            setContrast(1.f);
        mContrastUniform = GLES20.glGetUniformLocation(mProgram, "contrast");
        if(mContrastUniform != -1)
            setContrast(1.f);
    }

    // GrayScale
    public void setGrayScale(boolean newValue) {
        setInteger(newValue ? 1 : 0, mGrayscaleUniform, mProgram);
    }

    // Curve
    private static ArrayList<Float> getPreparedSplineCurve(ArrayList<CurvesPoint> points) {
        if(points != null && points.size() > 0) {
            ArrayList<CurvesPoint> newPoints = new ArrayList<CurvesPoint>(points);
            // Sort the array.
            Collections.sort(newPoints, new Comparator<CurvesPoint>() {
                @Override
                public int compare(CurvesPoint a, CurvesPoint b) {
                    int returnValue = 0;
                    if(a.mX == b.mX) {
                        returnValue = 0;
                    } else if(a.mX < b.mX) {
                        returnValue = -1;
                    } else {
                        returnValue = 1;
                    }
                    return returnValue;
                }
            });

            // // Convert from (0, 1) to (0, 255).
            // ArrayList<CurvesPoint> convertedPoints = new ArrayList<CurvesPoint>();
            // for (int i = 0; i < newPoints.size(); i++) {
            // CurvesPoint point = newPoints.get(i);
            // point.mX = (short)(point.mX * 255);
            // point.mY = (short)(point.mY * 255);
            //
            // convertedPoints.add(point);
            // }

            ArrayList<CurvesPoint> splinePoints = splineCurve(newPoints);

            // If we have a first point like (0.3, 0) we'll be missing some points at the beginning
            // that should be 0.
            CurvesPoint firstSplinePoint = splinePoints.get(0);

            if(firstSplinePoint.mX > 0) {
                for(int i = firstSplinePoint.mX; i >= 0; i--) {
                    CurvesPoint newPoint = new CurvesPoint(i, 0);
                    splinePoints.add(0, newPoint);
                }
            }

            // Insert points similarly at the end, if necessary.
            CurvesPoint lastSplinePoint = splinePoints.get(splinePoints.size() - 1);

            if(lastSplinePoint.mX < 255) {
                for(int i = lastSplinePoint.mX + 1; i <= 255; i++) {
                    CurvesPoint newPoint = new CurvesPoint(i, 255);
                    splinePoints.add(newPoint);
                }
            }

            // Prepare the spline points.
            ArrayList<Float> preparedSplinePoints = new ArrayList<Float>();
            for(int i = 0; i < splinePoints.size(); i++) {
                CurvesPoint newPoint = splinePoints.get(i);
                CurvesPoint origPoint = new CurvesPoint(newPoint.mX, newPoint.mX);

                float distance = (float)Math.sqrt(Math.pow((origPoint.mX - newPoint.mX), 2.0)
                        + Math.pow((origPoint.mY - newPoint.mY), 2.0));

                if(origPoint.mY > newPoint.mY) {
                    distance = -distance;
                }
                preparedSplinePoints.add(distance);
            }
            return preparedSplinePoints;
        }
        return new ArrayList<Float>();
    }

    private static ArrayList<CurvesPoint> splineCurve(ArrayList<CurvesPoint> points) {
        ArrayList<Double> sdA = secondDerivative(points);

        // Is [points count] equal to [sdA count]?
        // int n = [points count];
        int n = sdA.size();
        double sd[] = new double[n];

        // From NSMutableArray to sd[n];
        for(int i = 0; i < n; i++) {
            sd[i] = sdA.get(i);
        }

        ArrayList<CurvesPoint> output = new ArrayList<CurvesPoint>();

        for(int i = 0; i < n - 1; i++) {
            CurvesPoint cur = points.get(i);
            CurvesPoint next = points.get(i + 1);

            for(int x = cur.mX; x < next.mX; x++) {
                double t = (double)(x - cur.mX) / (next.mX - cur.mX);

                double a = 1 - t;
                double b = t;
                double h = next.mX - cur.mX;

                double y = a * cur.mY + b * next.mY + (h * h / 6)
                        * ((a * a * a - a) * sd[i] + (b * b * b - b) * sd[i + 1]);

                if(y > 255.0) {
                    y = 255.0;
                } else if(y < 0.0) {
                    y = 0.0;
                }
                output.add(new CurvesPoint(x, (int)y));
            }
        }

        // If the last point is (255, 255) it doesn't get added.
        if(output.size() == 255) {
            output.add(points.get(points.size() - 1));
        }
        return output;
    }

    private static ArrayList<Double> secondDerivative(ArrayList<CurvesPoint> points) {
        int n = points.size();
        if((n <= 0) || (n == 1)) {
            return null;
        }

        double[][] matrix = new double[n][3];
        double[] result = new double[n];
        matrix[0][1] = 1;
        // What about matrix[0][1] and matrix[0][0]? Assuming 0 for now (Brad L.)
        matrix[0][0] = 0;
        matrix[0][2] = 0;

        for(int i = 1; i < n - 1; i++) {
            CurvesPoint P1 = points.get(i - 1);
            CurvesPoint P2 = points.get(i);
            CurvesPoint P3 = points.get(i + 1);
            matrix[i][0] = (double)(P2.mX - P1.mX) / 6;
            matrix[i][1] = (double)(P3.mX - P1.mX) / 3;
            matrix[i][2] = (double)(P3.mX - P2.mX) / 6;
            result[i] = (double)(P3.mY - P2.mY) / (P3.mX - P2.mX) - (double)(P2.mY - P1.mY)
                    / (P2.mX - P1.mX);
        }

        // What about result[0] and result[n-1]? Assuming 0 for now (Brad L.)
        result[0] = 0;
        result[n - 1] = 0;

        matrix[n - 1][1] = 1;
        // What about matrix[n-1][0] and matrix[n-1][2]? For now, assuming they are 0 (Brad L.)
        matrix[n - 1][0] = 0;
        matrix[n - 1][2] = 0;

        // solving pass1 (up->down)
        for(int i = 1; i < n; i++) {
            double k = matrix[i][0] / matrix[i - 1][1];
            matrix[i][1] -= k * matrix[i - 1][2];
            matrix[i][0] = 0;
            result[i] -= k * result[i - 1];
        }
        // solving pass2 (down->up)
        for(int i = n - 2; i >= 0; i--) {
            double k = matrix[i][2] / matrix[i + 1][1];
            matrix[i][1] -= k * matrix[i + 1][0];
            matrix[i][2] = 0;
            result[i] -= k * result[i + 1];
        }
        double y2[] = new double[n];
        for(int i = 0; i < n; i++) {
            y2[i] = result[i] / matrix[i][1];
        }

        ArrayList<Double> output = new ArrayList<Double>();
        for(int i = 0; i < n; i++) {
            output.add(y2[i]);
        }
        return output;
    }

    private void updateToneCurveTexture() {
        if(mToneCurveTexture[0] == 0) {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
            GLES20.glGenTextures(1, mToneCurveTexture, 0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mToneCurveTexture[0]);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER,
                                   GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER,
                                   GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S,
                                   GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T,
                                   GLES20.GL_CLAMP_TO_EDGE);
            mToneCurveByteArray = ByteBuffer.allocate(256 * 4);
        } else {
            GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mToneCurveTexture[0]);
        }

        if((mRedCurve.size() >= 256) && (mGreenCurve.size() >= 256) && (mBlueCurve.size() >= 256)
                && (mRgbCompositeCurve.size() >= 256)) {
            mToneCurveByteArray.position(0);
            for(int currentCurveIndex = 0; currentCurveIndex < 256; currentCurveIndex++) {
                // BGRA for upload to texture
                mToneCurveByteArray.put((byte)Math.min(Math.max(currentCurveIndex
                                                                        + mRedCurve.get(currentCurveIndex)
                                                                        + mRgbCompositeCurve.get(currentCurveIndex),
                                                                0), 255));
                mToneCurveByteArray.put((byte)Math.min(Math.max(currentCurveIndex
                                                                        + mGreenCurve.get(currentCurveIndex)
                                                                        + mRgbCompositeCurve.get(currentCurveIndex),
                                                                0), 255));
                mToneCurveByteArray.put((byte)Math.min(Math.max(currentCurveIndex
                                                                        + mBlueCurve.get(currentCurveIndex)
                                                                        + mRgbCompositeCurve.get(currentCurveIndex),
                                                                0), 255));
                mToneCurveByteArray.put((byte)255);
            }

            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, 256 /* width */,
                                1 /* height */, 0, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE,
                                mToneCurveByteArray);
        }
    }

    @Override
    public void renderToTextureWithVertices(FloatBuffer vertices, FloatBuffer textureCoordinates,
            final int sourceTexture) {
        GLES20.glUseProgram(mProgram);
        // change order..
        setFilterFBO();
        setUniformsForProgramAtIndex(0);

        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE2);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, sourceTexture);
        GLES20.glUniform1i(mFilterInputTextureUniform, 2);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE3);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mToneCurveTexture[0]);
        GLES20.glUniform1i(mToneCurveTextureUniform, 3);

        GLES20.glVertexAttribPointer(mFilterPositionAttribute, 2, GLES20.GL_FLOAT, false, 0,
                                     vertices);

        GLES20.glVertexAttribPointer(mFilterTextureCoordinateAttribute, 2, GLES20.GL_FLOAT, false,
                                     0, textureCoordinates);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
    }

    public void setRgbCompositeControlPoints(ArrayList<CurvesPoint> newValue) {
        mRgbCompositeCurve = getPreparedSplineCurve(newValue);
        updateToneCurveTexture();
    }

    public void setRedControlPoints(ArrayList<CurvesPoint> newValue) {
        mRedCurve = getPreparedSplineCurve(newValue);
        updateToneCurveTexture();
    }

    public void setGreenControlPoints(ArrayList<CurvesPoint> newValue) {
        mGreenCurve = getPreparedSplineCurve(newValue);
        updateToneCurveTexture();
    }

    public void setBlueControlPoints(ArrayList<CurvesPoint> newValue) {
        mBlueCurve = getPreparedSplineCurve(newValue);
        updateToneCurveTexture();
    }

    public void setSaturation(float newValue) {
        setFloat(newValue, mSaturationUniform, mProgram);
    }

    public void setBrightness(float newValue) {
        setFloat(newValue, mBrightnessUniform, mProgram);
    }

    public void setContrast(float newValue) {
        setFloat(newValue, mContrastUniform, mProgram);
    }
}
