
package com.kiwiple.imageframework.util;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import android.graphics.Rect;
import android.graphics.RectF;

/**
 * 콜라주의 Rect를 계산하기 위한 RectF 상속 클래스
 */
public class CollageRect extends RectF {
    private DecimalFormat mRectFFormat = new DecimalFormat("#.#",
                                                           new DecimalFormatSymbols(Locale.US));

    public CollageRect() {
    }

    public CollageRect(RectF r) {
        super(r);
    }

    /**
     * 가로 길이를 설정
     * 
     * @param width 가로 길이
     */
    public void setWidth(float width) {
        right = left + width;
    }

    /**
     * 세로 길이를 설정
     * 
     * @param height 세로 길이
     */
    public void setHeight(float height) {
        bottom = top + height;
    }

    /**
     * Rect에 특정 배수를 곱함
     * 
     * @param multi 배수
     */
    public void multiply(float multi) {
        left *= multi;
        top *= multi;
        right *= multi;
        bottom *= multi;
    }

    /**
     * 여백을 설정 (상하좌우 전체 적용)
     * 
     * @param padding 여백
     */
    public void padding(float padding) {
        padding(padding, padding, padding, padding);
    }

    /**
     * 각 여백을 설정
     * 
     * @param left 좌측 여백
     * @param top 상단 여백
     * @param right 우측 여백
     * @param bottom 하단 여백
     */
    public void padding(float left, float top, float right, float bottom) {
        add(left, top, -right, -bottom);
    }

    /**
     * 현재 값에 추가
     * 
     * @param left 좌측 값
     * @param top 상단 값
     * @param right 우측 값
     * @param bottom 하단 값
     */
    public void add(float left, float top, float right, float bottom) {
        this.left += left;
        this.top += top;
        this.right += right;
        this.bottom += bottom;
    }

    /**
     * Rect를 이동 시킴
     * 
     * @param distanceX x이동 거리
     * @param distanceY y이동 거리
     */
    public void translate(float distanceX, float distanceY) {
        left += distanceX;
        top += distanceY;
        right += distanceX;
        bottom += distanceY;
    }

    /**
     * scale값 설정
     * 
     * @param scale scale
     * @param center true의 경우 가운데를 축으로, false의 경우 좌측 상단 기준
     */
    public void scale(float scale, boolean center) {
        if(center) {
            float distanceX = width() * (scale - 1) / 2;
            float distanceY = height() * (scale - 1) / 2;
            left -= distanceX;
            top -= distanceY;
            right += distanceX;
            bottom += distanceY;
        } else {
            left += left * (scale - 1);
            top += top * (scale - 1);
            right += right * (scale - 1);
            bottom += bottom * (scale - 1);
        }
    }

    /**
     * 좌측,상단의 경우 내림, 우측 하단의 경우 반올림한다.
     */
    public void adjustBoundToFloorCeil() {
        left = (float)Math.floor(left);
        top = (float)Math.floor(top);
        right = (float)Math.ceil(right);
        bottom = (float)Math.ceil(bottom);
    }

    public void format() {
        left = Float.parseFloat(mRectFFormat.format(left));
        top = Float.parseFloat(mRectFFormat.format(top));
        right = Float.parseFloat(mRectFFormat.format(right));
        bottom = Float.parseFloat(mRectFFormat.format(bottom));
    }

    /**
     * 정수형태의 Rect 반환
     * 
     * @return Rect
     */
    public Rect getRect() {
        return new Rect((int)Math.floor(left), (int)Math.floor(top), (int)Math.ceil(right),
                        (int)Math.ceil(bottom));
    }

    /**
     * Point를 배열로 반환 (좌, 상단, 우, 상단, 우, 하단, 좌, 하단)
     * 
     * @return Point를 배열
     */
    public float[] getPoints() {
        return new float[] {
                left, top, right, top, right, bottom, left, bottom
        };
    }

    /**
     * 주어진 rect를 scale한다
     * 
     * @param rect 대상 rect
     * @param scale scale 값
     * @param center true의 경우 가운데를 축으로, false의 경우 좌측상단 기준
     */
    public static void scale(Rect rect, float scale, boolean center) {
        if(center) {
            float distanceX = rect.width() * (scale - 1) / 2;
            float distanceY = rect.height() * (scale - 1) / 2;
            rect.left -= distanceX;
            rect.top -= distanceY;
            rect.right += distanceX;
            rect.bottom += distanceY;
        } else {
            rect.left += rect.left * (scale - 1);
            rect.top += rect.top * (scale - 1);
            rect.right += rect.right * (scale - 1);
            rect.bottom += rect.bottom * (scale - 1);
        }
    }
}
