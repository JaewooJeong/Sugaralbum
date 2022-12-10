package com.sugarmount.sugaralbum.model;

import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.Uri;

import lombok.Data;

/**
 * Created by Jaewoo on 2021-08-30.
 */
@Data
public class ImageResData {
    public Long _id;	// cursor id
    public long date;
    public boolean isVideo;
    public boolean checked;
    public Uri contentUri;
    public Integer selectOrder; // 멀티 선택 순서

    public Matrix matrix = new Matrix();
    public Matrix savedMatrix = new Matrix();
    public PointF startPoint;
    public PointF midPoint;
    public Float oldDistance;
    public Double oldDegree;
}
