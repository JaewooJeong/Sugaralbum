
package com.kiwiple.imageanalysis.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * 이미지 뷰를 회전시킨 뷰
 */
public class RotateImageView extends ImageView {
    private float mRotateDegree;

    public RotateImageView(Context context) {
        super(context);
    }

    public RotateImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RotateImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * 회전시킬 각도
     * 
     * @param rotation 회전각
     */
    public void setImageRotation(float rotation) {
        mRotateDegree = rotation;
    }

    /**
     * 설정되어있는 회전 값 반환
     * 
     * @return 회전 값
     */
    public float getImageRotation() {
        return mRotateDegree;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int cX = getWidth() >> 1;
        int cY = getHeight() >> 1;

        canvas.save();
        canvas.rotate(mRotateDegree, cX, cY);
        super.onDraw(canvas);
        canvas.restore();
    }
}
