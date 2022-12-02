
package com.kiwiple.imageframework.filter;

import java.io.Serializable;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * curve 값을 저장하기 위한 class
 * 
 * @version 2.0
 */
public class CurvesPoint implements Serializable, Parcelable {
    private static final long serialVersionUID = 475156581088368577L;

    /**
     * input 값으로 0~255의 범위를 가진다.
     * 
     * @version 2.0
     */
    public short mX;
    /**
     * output 값으로 0~255의 범위를 가진다.
     * 
     * @version 2.0
     */
    public short mY;

    public CurvesPoint() {
    }

    public CurvesPoint(int x, int y) {
        mX = (short)x;
        mY = (short)y;
    }

    private CurvesPoint(Parcel in) {
        mX = (short)in.readInt();
        mY = (short)in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mX);
        dest.writeInt(mY);
    }

    public static final Parcelable.Creator<CurvesPoint> CREATOR = new Creator<CurvesPoint>() {
        @Override
        public CurvesPoint[] newArray(int size) {
            return new CurvesPoint[size];
        }

        @Override
        public CurvesPoint createFromParcel(Parcel source) {
            return new CurvesPoint(source);
        }
    };

    /**
     * 기본 커브[[0,0], [255,255]] 인지 확인한다.
     */
    public static boolean isIdentity(ArrayList<CurvesPoint> curvesPoint) {
        if(curvesPoint == null
                || curvesPoint.size() == 0
                || (curvesPoint.size() == 2 && curvesPoint.get(0).mX == 0
                        && curvesPoint.get(0).mY == 0 && curvesPoint.get(1).mX == 255 && curvesPoint.get(1).mY == 255)) {
            return true;
        }
        return false;
    }
}
