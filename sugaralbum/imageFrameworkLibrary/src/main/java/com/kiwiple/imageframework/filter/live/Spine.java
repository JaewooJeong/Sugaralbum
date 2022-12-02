
package com.kiwiple.imageframework.filter.live;

import java.util.ArrayList;

import com.kiwiple.imageframework.filter.CurvesPoint;

public class Spine {
    private static void calculateSpline(ArrayList<CurvesPoint> points, float[] resultBuf) {
        int n = points.size();

        float[] u = new float[n];

        // Initialise the spline with "natural" lower boundary condition.
        resultBuf[0] = (short)(u[0] = 0.0f);

        int i;
        for(i = 0; i < n; i++) {
            resultBuf[i] = u[i] = 0.0f;
        }

        // This is the decomposition loop of the tridiagonal algorithm.
        for(i = 1; i < n - 1; ++i) {
            float sig = (points.get(i).mX - points.get(i - 1).mX)
                    / (points.get(i + 1).mX - points.get(i - 1).mX);
            float p = sig * resultBuf[i - 1] + 2.0f;
            resultBuf[i] = (sig - 1.0f) / p;

            u[i] = (points.get(i + 1).mY - points.get(i).mY)
                    / (points.get(i + 1).mX - points.get(i).mX)
                    - (points.get(i).mY - points.get(i - 1).mY)
                    / (points.get(i).mX - points.get(i - 1).mX);

            u[i] = (6.0f * u[i] / (points.get(i + 1).mX - points.get(i - 1).mX) - sig * u[i - 1])
                    / p;
        }

        resultBuf[n - 1] = 0;

        int k;

        // This is the backsubstitution loop of the tridiagonal algorithm.
        for(k = n - 2; k >= 0; --k) {
            resultBuf[k] = resultBuf[k] * resultBuf[k + 1] + u[k];
        }
    }

    private static float interpolateSpline(ArrayList<CurvesPoint> points, float[] derivatives,
            float value) {
        int n = points.size();

        // We will find the right place in the table by means of bisection.
        int klo = 0;
        int khi = n - 1;
        while(khi - klo > 1) {
            int k = (khi + klo) >> 1;
            if(points.get(k).mX > value)
                khi = k;
            else
                klo = k;
        }

        // klo and khi now bracket the input value of x.
        float h = points.get(khi).mX - points.get(klo).mX;

        if(h == 0.0f) {
            // printf("Bad x input. The x values must be distinct.");
        }

        float a = (points.get(khi).mX - value) / h;
        float b = (value - points.get(klo).mX) / h;

        // Cubic spline polynomial is now evaluated.
        return a * points.get(klo).mY + b * points.get(khi).mY
                + ((a * a * a - a) * derivatives[klo] + (b * b * b - b) * derivatives[khi])
                * (h * h) / 6.0f;
    }

    public static void makeLookupTable(ArrayList<CurvesPoint> points, int[] outputTable) {
        if(points == null || points.size() == 0) {
            points = new ArrayList<CurvesPoint>();
            points.add(new CurvesPoint(0, 0));
            points.add(new CurvesPoint(255, 255));
        }
        int startPointX, endPointX;
        float value;
        float[] derivatives = new float[points.size()];

        startPointX = points.get(0).mX;
        endPointX = points.get(points.size() - 1).mX;

        // Calcullate Spline
        calculateSpline(points, derivatives);

        // Interpolate
        short y;
        for(y = 0; y < 256; y++) {
            value = interpolateSpline(points, derivatives, y);
            if(value > 255)
                value = 255.0f;
            else if(value < 0)
                value = 0.0f;

            if(y <= startPointX)
                outputTable[y] = points.get(0).mY;
            else if(y >= endPointX)
                outputTable[y] = points.get(points.size() - 1).mY;
            else
                outputTable[y] = (int)value;
        }
    }
}
