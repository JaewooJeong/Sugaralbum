
package com.kiwiple.imageframework.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

/**
 * {@link com.kiwiple.imageframework.filter.live#LiveFilterController}의 라이브 필터 효과를 출력하기 위한
 * {@link android.view#View} 상속 클래스
 * 
 * @version 2.0
 */
public class CameraFilterView extends View {
    private int mOrientation;
    private boolean mFlip = false;
    private Bitmap mFilterImage;
    private Paint mPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);
    private Matrix mMatrix = new Matrix();
    private boolean mInitializedMatrix = false;

    /*
        fixes #12701
        Nexus 5X의 경우, 라이브 필터를 적용 후 카메라 전환 (전면 <-> 후면) 도중 1프레임이 더 적용되어 이전 프레임 잔재가 잠시 보이는 이슈가 있음.
        따라서, Nexus 5X와 같은 경우에는 FirstFrame을 스킵해줘야함.
    */
    private boolean mIsFirstFrameSkip = true;

    private Bitmap mOriginalBitmap;

    public CameraFilterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CameraFilterView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraFilterView(Context context) {
        super(context);
    }

    /**
     * 뷰를 회전시킨다.
     * 
     * @param orientation 회전 값
     */
    public void setOrientation(int orientation) {
        if(orientation < 0) {
            orientation += 360;
        }
        if(orientation > 360) {
            orientation -= 360;
        }
        mOrientation = orientation;
        mIsFirstFrameSkip = true;
        if (!Build.MODEL.contains("Nexus 5X")) {
            initFitStartMatrix();
        }
    }

    /**
     * 뷰를 좌우 대칭으로 회전
     * 
     * @param flip 좌우 대칭 회전 여부
     */
    public void setFlip(boolean flip) {
        if(mFlip != flip) {
            mFlip = flip;
            initFitStartMatrix();
        }
    }

    /**
     * 비트맵 이미지를 설정
     * 
     * @param image 비트맵 이미지
     */
    public void setFilterImage(Bitmap image) {
        mFilterImage = image;
        initFitStartMatrix();
    }

    /**
     * 원본 이미지를 설정
     * 
     * @param image 원본 비트맵 이미지
     */
    public void setOriginalImage(Bitmap image) {
        mOriginalBitmap = image;
    }

    /**
     * 필터가 적용된 이미지를 반환
     * 
     * @return 필터가 적용된 비트맵 이미지
     */
    public Bitmap getFilterImage() {
        return mFilterImage;
    }

    /**
     * 원본 이미지를 반환.
     * 
     * @return 원본 이미지
     */
    public Bitmap getOriginalImage() {
        return mOriginalBitmap;
    }

    /**
     * 뷰(이미지)의 Matrix를 반환
     * 
     * @return 이미지의 Matrix
     */
    public Matrix getImageMatrix() {
        return mMatrix;
    }

    /**
     * 초기화 여부를 반환
     * 
     * @return 초기화 여부
     */
    public boolean isInitialize() {
        return mInitializedMatrix;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        initFitStartMatrix();
    }

    /**
     * U+Camera 과거 버전에서는 화면에 꽉 차도록 프리뷰 화면을 구성하였으나, 잘려서 보이지 않는 영역 때문에 fit-start로 변경
     */
    private void initCenterCropMatrix() {
        mInitializedMatrix = false;
        if(mFilterImage == null) {
            return;
        }
        mMatrix.reset();
        float dwidth = mFilterImage.getWidth();
        float dheight = mFilterImage.getHeight();
        float vwidth = getWidth();
        float vheight = getHeight();

        if(vwidth == 0 || vheight == 0) {
            return;
        }

        if(mOrientation == 90 || mOrientation == 270) {
            float tmp = dwidth;
            dwidth = dheight;
            dheight = tmp;
        }

        float scaleX;
        float scaleY;
        float dx = 0, dy = 0;

        if(dwidth * vheight > vwidth * dheight) {
            scaleX = vheight / dheight;
            scaleY = scaleX;
        } else {
            scaleX = vwidth / dwidth;
            scaleY = scaleX;
        }
        // }

        if(mOrientation == 90 || mOrientation == 270) {
            float tmp = dwidth;
            dwidth = dheight;
            dheight = tmp;
        }
        dx = (vwidth - dwidth * scaleX) * 0.5f;
        dy = (vheight - dheight * scaleY) * 0.5f;

        mMatrix.postScale(scaleX, scaleY);
        mMatrix.postTranslate((int)(dx + 0.5f), (int)(dy + 0.5f));

        if(mFlip) {
            mMatrix.preTranslate(dwidth, 0);
            mMatrix.preScale(-1.0f, 1.0f);
        }

        mInitializedMatrix = true;
    }

    private void initFitStartMatrix() {
        mInitializedMatrix = false;
        if(mFilterImage == null) {
            return;
        }
        mMatrix.reset();
        float dwidth = mFilterImage.getWidth();
        float dheight = mFilterImage.getHeight();
        float vwidth = getWidth();
        float vheight = getHeight();

        if(vwidth == 0 || vheight == 0) {
            return;
        }

        if(mOrientation == 90 || mOrientation == 270) {
            float tmp = dwidth;
            dwidth = dheight;
            dheight = tmp;
        }

        float widthRatio = vwidth / dwidth;
        float heightRatio = vheight / dheight;

        float scale;
        if(heightRatio > widthRatio) {
            scale = vwidth / dwidth;
        } else {
            scale = vheight / dheight;
        }

        if(mOrientation == 90 || mOrientation == 270) {
            float tmp = dwidth;
            dwidth = dheight;
            dheight = tmp;
        }
        mMatrix.postRotate(mOrientation);
        mMatrix.postScale(scale, scale);

        int resId = getResources().getIdentifier("switcher_size", "dimen",
                                                 getContext().getPackageName());
        float bottomMenuHeight = 0;
        if(resId != 0) {
            bottomMenuHeight = getResources().getDimension(resId);
        }
        if(mOrientation == 90) {
            if(heightRatio > widthRatio) {
                // fit width, fit center
                // mMatrix.postTranslate(vwidth, (vheight - dwidth * scale - bottomMenuHeight) / 2);
                // fit width, fit end
                mMatrix.postTranslate(vwidth, vheight - dwidth * scale - bottomMenuHeight);
            } else {
                // fit height
                mMatrix.postTranslate(vwidth - (vwidth - dheight * scale) / 2, 0);
            }
        } else if(mOrientation == 180) {
            mMatrix.postTranslate(vheight, vwidth);
        } else if(mOrientation == 270) {
            /*
                fixes #12701
                Nexus 5X의 경우, 라이브 필터를 적용시 프리뷰가 밀리는 현상이 존재하여 분기 처리
            */
            if (Build.MODEL.contains("Nexus 5X")) {
                mMatrix.postTranslate(0, vheight - bottomMenuHeight);
            } else {
                mMatrix.postTranslate(0, vheight);
            }

        }

        if(mFlip) {
            mMatrix.preTranslate(dwidth, 0);
            mMatrix.preScale(-1.0f, 1.0f);
        }

        mInitializedMatrix = true;
    }

    @Override
    public void draw(Canvas canvas) {
        if(!mInitializedMatrix) {
            initFitStartMatrix();
        } else {
            /*
                fixes #12701
                Nexus 5X의 경우, 라이브 필터를 적용 후 카메라 전환 (전면 <-> 후면) 도중 1프레임이 더 적용되어 이전 프레임 잔재가 잠시 보이는 이슈가 있음.
                따라서, Nexus 5X와 같은 경우에는 FirstFrame을 스킵해줘야함.
            */
            if (Build.MODEL.contains("Nexus 5X") && mIsFirstFrameSkip) {
                mIsFirstFrameSkip = false;
                return;
            }
            canvas.drawBitmap(mFilterImage, mMatrix, mPaint);
        }
    }
}
