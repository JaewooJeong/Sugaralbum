
package com.kiwiple.imageframework.collage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region;

import com.kiwiple.imageframework.util.CollageRect;
import com.kiwiple.imageframework.util.FileUtils;
import com.kiwiple.imageframework.util.SmartLog;
import com.kiwiple.imageframework.view.ImageFrameView;
import com.larvalabs.svgandroid.SVG;
import com.larvalabs.svgandroid.SVGParseException;
import com.larvalabs.svgandroid.SVGParser;

class CollageFrameView extends ImageFrameView {
    private static final String TAG = CollageFrameInfo.class.getSimpleName();
    private DesignTemplate mDesignTemplate;

    // clip area info
    private SVG mSvg;
    private Path mOriginalClipPath;
    private CollageRect mClipBound = new CollageRect();
    private Region mClipRegion = new Region();

    // design frame image info
    private boolean mTransparentFrame = false;

    // drag and drop
    private boolean mDragAndDropSource;
    private Matrix mDragAndDropMatrix = new Matrix();
    private boolean mDragAndDropDestination;
    private RectF mDragAndDropImageSrc = new RectF();
    private RectF mDragAndDropImageDestination = new RectF();

    private Paint mFrameColorPaint;
    private CornerPathEffect mFrameCornerPathEffect;
    private float mFrameBorderWidth;
    private int mFrameBorderColor;

    private Paint mUplusBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private Paint mImagePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    
    private boolean mSingleFullFrame = false;
    private boolean mIsVideoType = false;

    // constructor
    public CollageFrameView(Context context, CollageFrameInfo frameInfo,
            DesignTemplate designTemplate, int id) throws IOException {
        super(context, id);
        mFrameInfo = frameInfo;
        mFrameBorderWidth = ((CollageFrameInfo)mFrameInfo).mBorderWidth;
        mFrameBorderColor = ((CollageFrameInfo)mFrameInfo).mBorderColor;
        InputStream svgInputStrem = null;
        InputStream frameImageInputStream = null;
        try {
            if(designTemplate.mIsThemeTemplate) {
                svgInputStrem = new FileInputStream(
                                                    DesignTemplateManager.getInstance(context)
                                                                         .getThemeBasePath(designTemplate.mTheme)
                                                            + File.separator + frameInfo.mSvgString);
            } else {
                svgInputStrem = context.getResources()
                                       .getAssets()
                                       .open(DesignTemplateManager.getInstance(context)
                                                                  .getAssetBasePath()
                                                     + frameInfo.mSvgString);
            }
            mSvg = SVGParser.getSVGFromInputStream(svgInputStrem);
            if(mSvg != null) {
                switch(mSvg.getType()) {
                    case SVG.TYPE_RECT:
                        mOriginalClipPath = new Path();
                        /**
                         * U+Story<br>
                         * Matrix 값을 파싱하기 위한 내용 변경
                         */
                        // ------------------------- 여기부터 변경 -----------------------------------
                        Matrix matrix = mSvg.getTransform();
                        float[] rect = mSvg.getRect();
                        Path tmpPath = new Path();
                        tmpPath.addRect(rect[0], rect[1], rect[2], rect[3], Direction.CW);
                        if(matrix != null) {
                            mOriginalClipPath.addPath(tmpPath, matrix);
                        } else {
                            mOriginalClipPath.addPath(tmpPath);
                        }
                        // ------------------------- 여기까지 변경 -----------------------------------
                        break;
                    case SVG.TYPE_CIRCLE:
                        mOriginalClipPath = new Path();
                        float[] circle = mSvg.getCircle();
                        mOriginalClipPath.addCircle(circle[0], circle[1], circle[2], Direction.CW);
                        break;
                    case SVG.TYPE_OVAL:
                        mOriginalClipPath = new Path();
                        mOriginalClipPath.addOval(mSvg.getOval(), Direction.CW);
                        break;
                    case SVG.TYPE_PATH:
                        mOriginalClipPath = mSvg.getPath();
                        break;
                    case SVG.TYPE_POLYGON:
                        mOriginalClipPath = mSvg.getPolygon();
                        break;
                    case SVG.TYPE_UNKNOWN:
                        mOriginalClipPath = null;
                        break;
                }
            }

            mDesignTemplate = designTemplate;
        } catch(SVGParseException e) {
        } finally {
            if(svgInputStrem != null) {
                try {
                    svgInputStrem.close();
                } catch(IOException e) {
                }
            }
            if(frameImageInputStream != null) {
                try {
                    frameImageInputStream.close();
                } catch(IOException e) {
                }
            }
        }
    }

    // init methods
    @Override
    public void initFrame(int width, int height) {
        super.initFrame(width, height);
        // setup clip
        if(mSingleFullFrame) {
            mOriginalClipPath.reset();
            mOriginalClipPath.addRect(0, 0, mDesignTemplate.mWidth, mDesignTemplate.mHeight, Path.Direction.CW);
        }
        mOriginalClipPath.computeBounds(mClipBound, true);
        // setup frame info
        mFrameWidthScale = mFrameInfo.mScale * mDesignTemplate.mLayoutWidthScaleFactor;
        mFrameHeightScale = mFrameInfo.mScale * mDesignTemplate.mLayoutHeightScaleFactor;
        mFrameRect.setEmpty();
        mFrameRect.set(mClipBound);
        mClipRegion.setPath(mOriginalClipPath, new Region((int)mClipBound.left,
                                                          (int)mClipBound.top,
                                                          (int)mClipBound.right,
                                                          (int)mClipBound.bottom));

        // 프레임을 뷰에 맞춤
        mFrameMatrix.reset();
        mFrameMatrix.postScale(mFrameWidthScale, mFrameHeightScale);
        if(!mSingleFullFrame) {
            mFrameMatrix.preTranslate(mFrameInfo.mCoordinateX, mFrameInfo.mCoordinateY);
        }
        mFrameMatrix.preRotate(mFrameInfo.mRotation, mFrameRect.centerX(), mFrameRect.centerY());
        // float[] values = new float[9];
        // mFrameMatrix.getValues(values);
        // values[Matrix.MTRANS_X] = (float)Math.floor(values[Matrix.MTRANS_X]);
        // values[Matrix.MTRANS_Y] = (float)Math.floor(values[Matrix.MTRANS_Y]);
        // mFrameMatrix.setValues(values);

        // setup selection box
        mSelectionRect.set(mFrameRect);
        mSelectionRect.padding(mFrameBorderWidth);
        mSelectionRect.adjustBoundToFloorCeil();

        // setup filled image
        if(mImage != null) {
            initImage();
        }
        setFrameColor(mFrameBorderColor);
        mBackgroundPaint.setColor(((CollageFrameInfo)mFrameInfo).mBackgroundColor);
        mBackgroundPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void initImage() {
        float widthScale = (mOriginalImageRotation == 0 || mOriginalImageRotation == 180 ? mClipBound.width()
                : mClipBound.height())
                / mImage.getWidth();
        float heightScale = (mOriginalImageRotation == 0 || mOriginalImageRotation == 180 ? mClipBound.height()
                : mClipBound.width())
                / mImage.getHeight();

        /**
         * U+Story에서 사용하기 위하여 변경한 값. 초기 scale값을 적용하여 콜라주의 초기 Matrix값을 설정한다.
         */
        // ------------------------- 여기부터 변경 -------------------------------------------
        mOriginalImageScaleFactor = widthScale > heightScale ? widthScale : heightScale;
        mImageFlip = false;

        // 이미지를 프레임에 맞춤
        mImageMatrix.reset();
        mImageMatrix.postTranslate(mClipBound.left
                                           + (mClipBound.width() - mImage.getWidth()
                                                   * mOriginalImageScaleFactor) / 2,
                                   mClipBound.top
                                           + (mClipBound.height() - mImage.getHeight()
                                                   * mOriginalImageScaleFactor) / 2);
        mImageMatrix.preScale(mOriginalImageScaleFactor, mOriginalImageScaleFactor);
        mImageMatrix.preRotate(mOriginalImageRotation, mImage.getWidth() / 2,
                               mImage.getHeight() / 2);
        // ------------------------- 여기까지 변경 -------------------------------------------

        mDragAndDropImageSrc.set(0, 0, mImage.getWidth(), mImage.getHeight());
        int size = mDesignTemplate.mWidth > mDesignTemplate.mHeight ? mDesignTemplate.mWidth
                : mDesignTemplate.mHeight;
        size *= 0.5f;
        mDragAndDropImageDestination.set(0, 0, size * mDesignTemplate.mLayoutWidthScaleFactor, size
                * mDesignTemplate.mLayoutHeightScaleFactor);

        mImageScaleFactor = 1.0f;
        mCurrentImageRotation = 0;

        /**
         * U+Story에서 사용하기 위하여 추가한 변수. 이동 값을 초기화 시켜줌
         */
        mImageTranslateXFactor = 0.f;
        mImageTranslateYFactor = 0.f;
    }

    /**
     * U+Story<br>
     * 프레임의 scale값을 초기화 시키기 위한 메소드
     */
    public void setInitailizeScaleImage() {
        mImageScaleFactor = 1.f;
    }

    // public methods
    // image transform info
    @Override
    public void setImage(Bitmap image) {
        super.setImage(image);
        mTransparentFrame = false;
    }

    /**
     * 배경 색상 설정
     * 
     * @param color 배경 색상
     */
    public void setBackgroundColor(int color) {
        mBackgroundPaint.setColor(color);
    }

    /**
     * 콜라주 프레임의 테두리 색상을 설정한다.
     * 
     * @param color 테두리 색상
     * @version 2.0
     */
    public void setFrameColor(int color) {
        if(mFrameColorPaint == null) {
            mFrameColorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            mFrameColorPaint.setStyle(Style.STROKE);
            mFrameColorPaint.setStrokeWidth(mFrameBorderWidth);
        }
        mFrameBorderColor = color;
        mFrameColorPaint.setColor(mFrameBorderColor);
    }

    /**
     * U+Story<br>
     * 프레임에 설정된 색상 값을 반환
     * 
     * @return int 설정된 프레임 색상
     */
    public int getFrameColor() {
        return mFrameBorderColor;
    }

    /**
     * 테두리 두께를 설정
     * 
     * @param width 설정할 테두리 두께
     */
    public void setFrameBorderWidth(float width) {
        mFrameColorPaint.setStrokeWidth(width * 4 / mFrameScaleFactor);
        mSelectionRect.padding(-mFrameBorderWidth);
        mSelectionRect.padding(width);
        mSelectionRect.adjustBoundToFloorCeil();
        mFrameBorderWidth = width;
        // initFrame();
        // mSelectionRect.set(mFrameRect);
        // mSelectionRect.padding(((CollageFrameInfo) mFrameInfo).mBorderWidth);
        // mSelectionRect.adjustBoundToFloorCeil();
    }

    /**
     * U+Story<br>
     * 설정된 프레임의 여백 크기 값을 반환
     * 
     * @return float 여백 크기 값
     */
    public float getFrameBorderWidth() {
        return mFrameBorderWidth;
    }

    /**
     * U+Story<br>
     * 프레임에 설정되는 round값을 저장하기 위한 변수 추가
     */
    private float mFrameCornerRadius = 0;

    /**
     * 외각 둥근 정도 설정
     * 
     * @param radius 설정 값
     */
    public void setFrameCornerRadius(float radius) {
        /**
         * U+Story<br>
         * 프레임에 설정되는 round값을 저장
         */
        mFrameCornerRadius = radius;
        if(radius > 0) {
            mFrameCornerPathEffect = new CornerPathEffect(radius);
        } else {
            mFrameCornerPathEffect = null;
        }
        mBackgroundPaint.setPathEffect(mFrameCornerPathEffect);
        mUplusBgPaint.setPathEffect(mFrameCornerPathEffect);
        mFrameColorPaint.setPathEffect(mFrameCornerPathEffect);
    }

    /**
     * U+Story<br>
     * 프레임에 설정된 round값을 반환하는 메소드
     * 
     * @return float 프레임에 설정된 round값
     */
    public float getFrameCornerRadius() {
        return mFrameCornerRadius;
    }

    /**
     * 프레임 배경을 투명하게 할 것인지 여부
     * 
     * @param isTransparent 투명 여부
     */
    public void setTransparentFrame(boolean isTransparent) {
        mTransparentFrame = isTransparent;
    }

    /**
     * Drag&Drop 시작
     */
    public void startDragAndDrop(boolean dragAndDrop, float x, float y) {
        mDragAndDropSource = dragAndDrop;

        mDragAndDropMatrix.reset();
        mDragAndDropMatrix.setRectToRect(mDragAndDropImageSrc, mDragAndDropImageDestination,
                                         ScaleToFit.CENTER);
        mDragAndDropMatrix.postTranslate(x - mDragAndDropImageDestination.centerX(), y
                - mDragAndDropImageDestination.centerY());
        mDragAndDropMatrix.preRotate(mOriginalImageRotation, mDragAndDropImageSrc.centerX(),
                                     mDragAndDropImageSrc.centerY());
    }

    /**
     * Drag 좌표 업데이트
     */
    public void updateDragAndDrop(float x, float y) {
        mDragAndDropMatrix.postTranslate(x, y);
    }

    /**
     * Drag&Drop 진행 여부
     */
    public boolean isDragAndDropSource() {
        return mDragAndDropSource;
    }

    /**
     * Drag&Drop 종료
     */
    public void endDragAndDrop() {
        mDragAndDropSource = false;
    }

    /**
     * Drag 좌표에 있는 프레임을 하이라이트 처리 해주기 위해 설정
     */
    public void setDropDestination(boolean dropDestination) {
        mDragAndDropDestination = dropDestination;
    }

    /**
     * Drag 좌표에 있는 프레임 여부
     */
    public boolean isDropDestination() {
        return mDragAndDropDestination;
    }

    // TODO: 사용안함. 삭제 해야 함.
    private boolean mOnTouchDown = false;

    public void setTouchDown(boolean touchDown) {
        mOnTouchDown = touchDown;
    }

    public boolean getTouchDown() {
        return mOnTouchDown;
    }
    
    /**
     * 무비다이어리에서 사용. 프레임이 한개인 콜라주의 경우 이미지를 프레임에 맞추지 않도록 한다.(콜라주 전체가 이미지 영역)
     */
    public void setSingleFullFrame(boolean isSingleFullFrame) {
        mSingleFullFrame = isSingleFullFrame;
    }
    
    public boolean getIsVideoType() {
        return mIsVideoType;
    }
    
    public void setIsVideoType(boolean isVideoType) {
        mIsVideoType = isVideoType;
    }

    private CollageRect mComputedClipRect = new CollageRect();
    private CollageRect mComputedImageRect = new CollageRect();
    private Matrix mConcatMatrix = new Matrix();
    private float mDistance;

    // bounce animation
    public boolean adjustBoundImagePosition(boolean noAnimation) {
        if(mImage == null) {
            return false;
        }
        mFrameMatrix.mapRect(mComputedClipRect, mClipBound);
        mComputedClipRect.format();

        mConcatMatrix.setConcat(mFrameMatrix, mImageMatrix);
        mConcatMatrix.mapRect(mComputedImageRect, mDragAndDropImageSrc);
        mComputedImageRect.format();

        SmartLog.d(TAG,
                   "clip:" + mComputedClipRect.toString() + ",image:"
                           + mComputedImageRect.toString());
        boolean adjust = false;
        // 이미지 넓이가 프레임보다 작을때.
        if(mComputedImageRect.width() < mComputedClipRect.width()) {
            mDistance = mComputedClipRect.centerX() - mComputedImageRect.centerX();
            // 중앙에서 0.1 이상 떨어져 있을 경우
            if(Math.abs(mDistance) > 0.1f) {
                adjust = true;
                if(Math.abs(mDistance) > 0.1f && !noAnimation) {
                    translateImage(mDistance * 0.1f, 0);
                } else {
                    translateImage(mDistance, 0);
                }

            }
        } else {
            // 왼쪽 경계에서 0.1 이상 떨어져 있을 경우
            if(mComputedImageRect.left - mComputedClipRect.left > 0.1f) {
                adjust = true;
                mDistance = mComputedImageRect.left - mComputedClipRect.left;
                if(mDistance > 0.1f && !noAnimation) {
                    translateImage(-mDistance * 0.1f, 0);
                } else {
                    translateImage(-mDistance, 0);
                }
            }
            // 오른쪽 경계에서 0.1 이상 떨어져 있을 경우
            else if(mComputedClipRect.right - mComputedImageRect.right > 0.1f) {
                adjust = true;
                mDistance = mComputedClipRect.right - mComputedImageRect.right;
                if(mDistance > 0.1f && !noAnimation) {
                    translateImage(mDistance * 0.1f, 0);
                } else {
                    translateImage(mDistance, 0);
                }
            }
        }
        // 이미지 높이가 프레임보다 작을때.
        if(mComputedImageRect.height() < mComputedClipRect.height()) {
            mDistance = mComputedClipRect.centerY() - mComputedImageRect.centerY();
            // 중앙에서 0.1 이상 떨어져 있을 경우
            if(Math.abs(mDistance) > 0.1f) {
                adjust = true;
                if(Math.abs(mDistance) > 0.1f && !noAnimation) {
                    translateImage(0, mDistance * 0.1f);
                } else {
                    translateImage(0, mDistance);
                }
            }
        } else {
            // 위쪽 경계로 붙이기
            // 위쪽 경계에서 0.1 이상 떨어져 있을 경우
            if(mComputedImageRect.top - mComputedClipRect.top > 0.1f) {
                adjust = true;
                mDistance = mComputedImageRect.top - mComputedClipRect.top;
                if(mDistance > 0.1f && !noAnimation) {
                    translateImage(0, -mDistance * 0.1f);
                } else {
                    translateImage(0, -mDistance);
                }
            }
            // 아래쪽 경계에서 0.1 이상 떨어져 있을 경우
            else if(mComputedClipRect.bottom - mComputedImageRect.bottom > 0.1f) {
                adjust = true;
                mDistance = mComputedClipRect.bottom - mComputedImageRect.bottom;
                if(mDistance > 0.1f && !noAnimation) {
                    translateImage(0, mDistance * 0.1f);
                } else {
                    translateImage(0, mDistance);
                }
            }
        }
        return adjust;
    }

    // 이미지가 세팅되지 않았을 경우 배경을 투명하게 만들어 준다.
    @Override
    public void drawBackground(Canvas canvas, boolean isOutput) {
        if(mOriginalClipPath != null) {
            if(!mTransparentFrame) {
                if(mImage == null) {
                    mUplusBgPaint.setStyle(Style.FILL);
                    mUplusBgPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
                    mUplusBgPaint.setColor(Color.TRANSPARENT);

                    getFrameCanvas().save();

                    getFrameCanvas().concat(mFrameMatrix);
                    getFrameCanvas().drawColor(Color.BLACK, Mode.SRC);
                    getFrameCanvas().drawPath(mOriginalClipPath, mUplusBgPaint);

                    /**
                     * U+Story<br>
                     * fixes #8392 : 20141001_keylime - to remove collage border of clean theme
                     */
                    // 프레임의 크기를 테두리 두께로 조절.
                    if(!isOutput || mFrameColorPaint.getStrokeWidth() != 0.f) {
                        // mFrameColorPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
                        // tmp.drawPath(mOriginalClipPath, mFrameColorPaint);
                        // mFrameColorPaint.setXfermode(null);

                        mFrameColorPaint.setXfermode(new PorterDuffXfermode(Mode.SRC));
                        int originalColor = mFrameColorPaint.getColor();
                        mFrameColorPaint.setColor(Color.BLACK);
                        getFrameCanvas().drawPath(mOriginalClipPath, mFrameColorPaint);
                        mFrameColorPaint.setColor(originalColor);
                    }
                    getFrameCanvas().restore();

                    mImagePaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
                    canvas.drawBitmap(getFrameImage(), 0, 0, mImagePaint);
                    mImagePaint.setXfermode(null);
                }
            }
        }
    }

    // selection box
    @Override
    public boolean isInnterPoint(float x, float y) {
        if(mDesignTemplate.mTemplateType > 1) {
            float[] point = invertTransformPoints(mFrameMatrix, x, y);
            if(point != null && mClipRegion.contains((int)point[0], (int)point[1])) {
                return true;
            }
            return false;
        }
        return super.isInnterPoint(x, y);
    }

    @Override
    public void drawImage(Canvas canvas, Paint bitmapPaint, boolean isOutput) {
        if(mOriginalClipPath != null) {
            if(mImage != null) {
                getFrameCanvas().save();

                getFrameCanvas().concat(mFrameMatrix);
                getFrameCanvas().drawColor(Color.TRANSPARENT, Mode.CLEAR);
                getFrameCanvas().drawPath(mOriginalClipPath, mBackgroundPaint);

                /**
                 * U+Story<br>
                 * fixes #8392 : 20141001_keylime - to remove collage border of clean theme
                 */
                // 프레임의 크기를 테두리 두께로 조절
                if(!isOutput || mFrameColorPaint.getStrokeWidth() != 0.f) {
                    mFrameColorPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
                    getFrameCanvas().drawPath(mOriginalClipPath, mFrameColorPaint);
                    mFrameColorPaint.setXfermode(null);
                }

                bitmapPaint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
                getFrameCanvas().drawBitmap(mImage, mImageMatrix, bitmapPaint);
                getFrameCanvas().restore();
                bitmapPaint.setXfermode(null);
                canvas.drawBitmap(getFrameImage(), 0, 0, bitmapPaint);
            }
        }
    }

    @Override
    public void drawFrame(Canvas canvas, boolean isOutput) {
        if(!isOutput || mFrameColorPaint.getStrokeWidth() != 0.f) {
            if(mFrameColorPaint.getColor() != Color.TRANSPARENT) {
                canvas.save();
                canvas.concat(mFrameMatrix);
                canvas.drawPath(mOriginalClipPath, mFrameColorPaint);
                if (mIsVideoType) {
                    Bitmap resources = BitmapFactory.decodeResource(mContext.getResources(),
                                                                    FileUtils.getBitmapResourceId(mContext,
                                                                            "btn_gallery_play_2_nor"));
                    RectF videoIconRect = new RectF();
                    videoIconRect.left = mFrameRect.left + (mFrameRect.width() - resources.getWidth()/2) / 2;
                    videoIconRect.top = mFrameRect.top + (mFrameRect.height() - resources.getHeight()/2) / 2;
                    videoIconRect.right = videoIconRect.left + resources.getWidth()/2;
                    videoIconRect.bottom = videoIconRect.top + resources.getHeight()/2;
                    canvas.drawBitmap(resources, null, videoIconRect, null);
                }
                canvas.restore();
            }
        }
    }

    @Override
    public void drawSelection(Canvas canvas, Paint selectionPaint) {
        mSelectionPaint.set(selectionPaint);
        // TODO : setStrokeWidth는 선택 박스가 STORKE style일 때 설정했던 값이 남아 있는 것으로 보여짐. 삭제 필요.
        mSelectionPaint.setStrokeWidth(Math.round(mSelectionPaint.getStrokeWidth() * 2
                / mFrameScaleFactor)
                + mFrameColorPaint.getStrokeWidth());
        mSelectionPaint.setPathEffect(mFrameCornerPathEffect);
        mSelectionPaint.setAlpha(0x7F);
        mSelectionPaint.setStyle(Style.FILL);

        getFrameCanvas().save();

        getFrameCanvas().concat(mFrameMatrix);
        getFrameCanvas().drawColor(Color.TRANSPARENT, Mode.CLEAR);
        getFrameCanvas().drawPath(mOriginalClipPath, mSelectionPaint);

        // clear outside of selection
        mFrameColorPaint.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
        getFrameCanvas().drawPath(mOriginalClipPath, mFrameColorPaint);
        mFrameColorPaint.setXfermode(null);

        getFrameCanvas().restore();

        canvas.drawBitmap(getFrameImage(), 0, 0, mImagePaint);
    }

    /**
     * Drag&Drop에서 반투명의 source 이미지를 그려준다. 
     */
    public void drawDragShodow(Canvas canvas, Paint paint) {
        if(mImage != null && mDragAndDropSource) {
            canvas.drawBitmap(mImage, mDragAndDropMatrix, paint);
        }
    }

    @Override
    public void clear() {
        super.clear();
        mSvg = null;
        mOriginalClipPath = null;
        mDesignTemplate = null;
    }

    @Override
    public ImageFrameView copy() {
        return null;
    }

    private Canvas getFrameCanvas() {
        return CollageFrameImageManager.getInstance().getCanvas(mLayoutWidth, mLayoutHeight);
    }

    private Bitmap getFrameImage() {
        return CollageFrameImageManager.getInstance().getImage(mLayoutWidth, mLayoutHeight);
    }
}
