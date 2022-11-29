
package com.kiwiple.imageframework.sticker;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Layout;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import com.almeros.android.multitouch.gesturedetectors.MoveGestureDetector;
import com.almeros.android.multitouch.gesturedetectors.RotateGestureDetector;
import com.kiwiple.imageframework.Constants;
import com.kiwiple.imageframework.collage.DesignTemplate;
import com.kiwiple.imageframework.collage.DesignTemplateManager;
import com.kiwiple.imageframework.collage.TemplateInfo;
import com.kiwiple.imageframework.util.BitmapUtils;
import com.kiwiple.imageframework.util.FileUtils;
import com.kiwiple.imageframework.util.ImageDownloadManager;
import com.kiwiple.imageframework.util.ImageDownloadManager.ImageDownloadManagerListener;
import com.kiwiple.imageframework.util.ImageDownloadManager.ImageInfo;
import com.kiwiple.imageframework.util.SmartLog;
import com.kiwiple.imageframework.util.TransformUtils;
import com.kiwiple.imageframework.view.ImageFrameView;
import com.kiwiple.imageframework.view.ScalableViewController;
import com.kiwiple.imageframework.view.ScalableViewController.OnInvalidateListener;
import com.kiwiple.imageframework.view.StickerTextEditView;

/**
 * View를 상속 받은 클래스.<br>
 * 스티커를 추가한다. <br>
 * Gesture 이벤트를 이용하여 각 스티커를 Crop, Zoom In/Out, Rotate 시킬 수 있다.
 * 
 * @version 2.0
 */
public class StickerView extends View {
    private static final String TAG = StickerView.class.getSimpleName();

    private final static int HANDLER_MESSAGE_INVALIDATE = 0;
    private final static int HANDLER_MESSAGE_SELECT_FRAME = 1;
    private final static int HANDLER_MESSAGE_DESELECT_FRAME = 2;
    private static final float DEFAULT_OUTLINE_WIDTH = 5.f;
    private static final float DEFAULT_OUTLINE_BASELINE = 800.f;
    /**
     * 이미지 스티커
     * 
     * @version 1.0
     */
    public static final int STICKER_TYPE_IMAGE = 0;
    /**
     * 텍스트 스티커
     * 
     * @version 1.0
     */
    public static final int STICKER_TYPE_TEXT = 1;

    /**
     * 스티커 버튼 좌상단 배치. Use with {@link #setCloseButtonLocation}, {@link #setScaleButtonLocation}
     * 
     * @version 1.0
     */
    public final static int STICKER_BUTTON_LOCATION_LEFT_TOP = 0;
    /**
     * 스티커 버튼 우상단 배치. Use with {@link #setCloseButtonLocation}, {@link #setScaleButtonLocation}
     * 
     * @version 1.0
     */
    public final static int STICKER_BUTTON_LOCATION_RIGHT_TOP = 1;
    /**
     * 스티커 버튼 좌하단 배치. Use with {@link #setCloseButtonLocation}, {@link #setScaleButtonLocation}
     * 
     * @version 1.0
     */
    public final static int STICKER_BUTTON_LOCATION_LEFT_BOTTOM = 2;
    /**
     * 스티커 버튼 우하단 배치. Use with {@link #setCloseButtonLocation}, {@link #setScaleButtonLocation}
     * 
     * @version 1.0
     */
    public final static int STICKER_BUTTON_LOCATION_RIGHT_BOTTOM = 3;

    // sticker info
    private ArrayList<StickerFrameView> mStickerFrameViews = new ArrayList<StickerFrameView>();
    private int mLastId = 0;

    private Paint mDrawBitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private boolean mSelectionPaintOverride = false;
    private Paint mSelectionPaint = new Paint();

    private boolean mLayoutInitialize = false;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private RotateGestureDetector mRotateDetector;
    private MoveGestureDetector mMoveDetector;

    // 스티커 최소 크기
    protected float mFrameMinScale = .2f;
    // 스티커 최대 크기
    protected float mFrameMaxScale = 4.0f;
    // 스티커 기본 크기
    protected float mFrameDefaultScale = .65f;

    /**
     * 편집중인 스티커
     */
    private StickerFrameView mLastImageView;
    /**
     * 선택된 스티커
     */
    private StickerFrameView mSelectedStickerFrameView;
    // 삭제, 크기 조절, 편집 버튼
    private Bitmap mDelete;
    private Bitmap mScale;
    private Bitmap mDeleteSel;
    private Bitmap mScaleSel;
    private Bitmap mTextEdit;
    private Bitmap mTextEditSel;

    // TextSticker 추가 시 입력 완료,취소 버튼으로 11번가에서 사용했음.
    private View mInputStickerCancel;
    private View mInputStickerDone;

    /**
     * TextSticker 글자 수 제한
     */
    private int mLimitLength = 50;
    private int mDefaultTextColor = Color.WHITE;
    private float mDefaultTextSize = 50;
    /**
     * TextSticker 추가 시 입력창의 테두리 모양
     */
    private ShapeDrawable mInputTextBackground;

    private float mLayoutScaleFactor;

    private int mScaleLocation = STICKER_BUTTON_LOCATION_RIGHT_BOTTOM;
    private int mDeleteLocation = STICKER_BUTTON_LOCATION_LEFT_TOP;
    private int mTextEditLocation = STICKER_BUTTON_LOCATION_LEFT_BOTTOM;

    private OnStickerStatusChangedListener mOnStickerStatusChangedListener;

    private Rect mStickerViewRect = new Rect();
    /**
     * 스티커의 이동 가능 영역 제한
     */
    private Point mTranslatePadding = new Point(20, 20);

    // added Template version 2
    private DesignTemplate mDesignTemplate;
    private ImageDownloadManager mImageDownloadManager;
    private TemplateInfo mTemplateInfo;

    /**
     * 선택되지 않은 스티커에 대한 편집 가능 여부
     */
    private boolean mEnableUnselectedSticker = true;

    /**
     * U+Story<br>
     * 타이밍 이슈로 인한 getWidth(), getHeight()대신 사용하기 위한 값
     */
    private int mWidth;
    private int mHeight;

    private Handler mHandler = new Handler() {
        @Override
        public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
            if(msg.what == HANDLER_MESSAGE_INVALIDATE && mBlockHandlerInvalidate
                    || msg.what == HANDLER_MESSAGE_SELECT_FRAME && mBlockHandlerSelect
                    || msg.what == HANDLER_MESSAGE_DESELECT_FRAME && mBlockHandlerDeselect) {
                msg.recycle();
                return false;
            }
            return super.sendMessageAtTime(msg, uptimeMillis);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case HANDLER_MESSAGE_INVALIDATE:
                    if(!mBlockHandlerInvalidate) {
                        mHandler.removeMessages(HANDLER_MESSAGE_INVALIDATE);
                        invalidate();
                    }
                    break;
                case HANDLER_MESSAGE_SELECT_FRAME:
                    if(!mBlockHandlerSelect) {
                        selectSticker((StickerFrameView)msg.obj);
                    }
                    break;
                case HANDLER_MESSAGE_DESELECT_FRAME:
                    if(!mBlockHandlerDeselect) {
                        invokeDeselectSticker();
                    }
                    break;
            }
        }
    };

    // [U+Camera>겔러리>편집>성형>메이크업] 기능에서 확대/축소 기능 지원
    private boolean mScalableTouch = false;
    private ScalableViewController mScalableViewController;
    private float[] mScaledPoint = new float[2];

    public StickerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public StickerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public StickerView(Context context) {
        super(context);
        init(context);
    }

    /**
     * U+Story<br>
     * 스티커뷰를 생성할때 특정 크기로 생성하기 위한 생성자
     * 
     * @param context Context
     * @param width 스티커뷰 가로 길이
     * @param height 스티커뷰 세로 길이
     */
    public StickerView(Context context, int width, int height) {
        super(context);
        mFrameDefaultScale = 1.f;
        init(context);
        onLayout(true, 0, 0, width, height);
    }

    /**
     * U+Story<br>
     * 스티커뷰를 생성할때 특정 크기로 생성하기 위한 생성자
     * 
     * @param context Context
     * @param width 스티커뷰 가로 길이
     * @param height 스티커뷰 세로 길이
     * @param template 템플릿 정보
     */
    public StickerView(Context context, int width, int height, TemplateInfo template) {
        super(context);

        mTemplateInfo = template;
        if(mTemplateInfo == null) {
            throw new IllegalStateException("Design template is null");
        }
        mLayoutInitialize = false;

        mFrameDefaultScale = 1.f;
        init(context);
        onLayout(true, 0, 0, width, height);
    }

    private void init(Context context) {
        if(Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, new Paint(Paint.FILTER_BITMAP_FLAG));
        }

        mDelete = BitmapFactory.decodeResource(getResources(),
                                               FileUtils.getBitmapResourceId(context,
                                                                             "sticker_x_nor"));
        mScale = BitmapFactory.decodeResource(getResources(),
                                              FileUtils.getBitmapResourceId(context,
                                                                            "sticker_rotate_nor"));
        mDeleteSel = BitmapFactory.decodeResource(getResources(),
                                                  FileUtils.getBitmapResourceId(context,
                                                                                "sticker_x_sel"));
        mScaleSel = BitmapFactory.decodeResource(getResources(),
                                                 FileUtils.getBitmapResourceId(context,
                                                                               "sticker_rotate_sel"));
        mTextEdit = BitmapFactory.decodeResource(getResources(),
                                                 FileUtils.getBitmapResourceId(context,
                                                                               "ic_menu_edit"));
        mTextEditSel = BitmapFactory.decodeResource(getResources(),
                                                    FileUtils.getBitmapResourceId(context,
                                                                                  "ic_menu_edit"));

        // Setup Gesture Detectors
        mGestureDetector = new GestureDetector(context.getApplicationContext(),
                                               new SimpleGestureListener());
        mScaleDetector = new ScaleGestureDetector(context.getApplicationContext(),
                                                  new ScaleListener());
        mRotateDetector = new RotateGestureDetector(context.getApplicationContext(),
                                                    new RotateListener());
        mMoveDetector = new MoveGestureDetector(context.getApplicationContext(), new MoveListener());

        // Setup Paint
        mSelectionPaint.setColor(Color.WHITE);
        mSelectionPaint.setShadowLayer(1, 0, 0, Color.argb(0xc3, 0x00, 0x00, 0x00));
        mSelectionPaint.setStyle(Paint.Style.STROKE);
        mSelectionPaint.setFlags(Paint.ANTI_ALIAS_FLAG);

        mImageDownloadManager = ImageDownloadManager.getInstance(context,
                                                                 context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                                                        .getPath());
    }

    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mLayoutInitialize = false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if(isInEditMode()) {
            return;
        }

        mWidth = right - left;
        mHeight = bottom - top;

        // calc layout size
        if(!mLayoutInitialize) {
            mLayoutInitialize = true;

            float widthScaleFactor = (right - left) / DEFAULT_OUTLINE_BASELINE;
            float heightScaleFactor = (bottom - top) / DEFAULT_OUTLINE_BASELINE;
            mLayoutScaleFactor = widthScaleFactor < heightScaleFactor ? widthScaleFactor
                    : heightScaleFactor;

            // Setup Paint
            if(!mSelectionPaintOverride) {
                mSelectionPaint.setStrokeWidth(DEFAULT_OUTLINE_WIDTH * mLayoutScaleFactor * 2);
            }

            initTemplateInfo();

            if(mScalableViewController != null) {
                mScalableViewController.onLayout(changed, left, top, right, bottom);
            }
        }
        mStickerViewRect.set(mTranslatePadding.x, mTranslatePadding.y, (right - left)
                - mTranslatePadding.x, (bottom - top) - mTranslatePadding.y);
    }

    /**
     * 뷰에 적용할 템플릿을 설정한다.
     * 
     * @param template 템플릿 정보
     * @version 1.0
     */
    public void setTemplateInfo(final TemplateInfo template) {
        mTemplateInfo = template;
        if(mTemplateInfo == null) {
            throw new IllegalStateException("Design template is null");
        }
        mLayoutInitialize = false;
        requestLayout();
    }

    private void initTemplateInfo() {
        if(mTemplateInfo == null) {
            return;
        }
        mDesignTemplate = DesignTemplateManager.getInstance(getContext().getApplicationContext())
                                               .getDesignTemplate(mTemplateInfo.getId());
        if(mDesignTemplate == null || !mDesignTemplate.isValid()) {
            return;
        }
        clear();

        SmartLog.e(TAG, "init Template");

        for(StickerFrameInfo info : mDesignTemplate.mStickerInfos) {
            addSticker(null, info);
        }

        for(TextStickerFrameInfo info : mDesignTemplate.mTextStickerInfos) {
            addTextSticker(info);
        }
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    private boolean mMoveEnable = true;
    /**
     * 스티커 편집 index. [U+Camera>겔러리>편집>드로잉]의 히스토리 기능에서 사용
     */
    private int mEditProgress = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isEnabled()) {
            return false;
        }
        if(mScalableViewController != null) {
            mScaledPoint[0] = event.getX();
            mScaledPoint[1] = event.getY();
            mScalableViewController.getInvertedMatrix().mapPoints(mScaledPoint);
            event.setLocation(mScaledPoint[0], mScaledPoint[1]);
        }
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mEditProgress++;
                if(mSelectedStickerFrameView != null) {
                    if(mSelectedStickerFrameView.isInnerScaleToolboxPoint(event.getX(),
                                                                          event.getY())) {
                        mSelectedStickerFrameView.setScaleToolboxSelection(true);
                    } else if(mSelectedStickerFrameView.isInnerDeleteToolboxPoint(event.getX(),
                                                                                  event.getY())) {
                        mSelectedStickerFrameView.setDeleteToolboxSelection(true);
                    } else if(mSelectedStickerFrameView instanceof TextFrameView
                            && ((TextFrameView)mSelectedStickerFrameView).isInnerTextEditToolboxPoint(event.getX(),
                                                                                                      event.getY())) {
                        ((TextFrameView)mSelectedStickerFrameView).setEditTextToolboxSelection(true);
                    }
                }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                mMoveEnable = false;
                break;
        }
        try {
            mGestureDetector.onTouchEvent(event);
            if(mLastImageView != null) {
                mRotateDetector.onTouchEvent(event);
                mScaleDetector.onTouchEvent(event);
                mMoveDetector.onTouchEvent(event);
            }
        } catch(Exception e) {
            // do nothing
        }

        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if(mSelectedStickerFrameView != null) {
                    if(mSelectedStickerFrameView.isScaleToolboxSelected()) {
                        mSelectedStickerFrameView.setScaleToolboxSelection(false);
                    } else if(mSelectedStickerFrameView.isDeleteToolboxSelected()) {
                        mSelectedStickerFrameView.setDeleteToolboxSelection(false);
                    } else if(mSelectedStickerFrameView instanceof TextFrameView
                            && ((TextFrameView)mSelectedStickerFrameView).isEditTextToolboxSelected()) {
                        ((TextFrameView)mSelectedStickerFrameView).setEditTextToolboxSelection(false);
                    }
                }
                mMoveEnable = true;
                break;
        }
        if(mLastImageView != null) {
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }

        if(mScalableTouch && mLastImageView == null // && mSelectedStickerFrameView == null
                && mScalableViewController != null) {
            mScaledPoint[0] = event.getX();
            mScaledPoint[1] = event.getY();
            mScalableViewController.getScaleMatrix().mapPoints(mScaledPoint);
            event.setLocation(mScaledPoint[0], mScaledPoint[1]);
            return mScalableViewController.onTouchEvent(event);
        }
        return mLastImageView != null || mSelectedStickerFrameView != null;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if(mLastImageView != null
                    && (mEnableUnselectedSticker || mLastImageView == mSelectedStickerFrameView)) {
                mLastImageView.scaleFrame(detector.getScaleFactor());
                addHistory(mLastImageView, HISTORY_ACTION_MODIFY);
            }
            return true;
        }
    }

    private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            if(Math.abs(detector.getRotationDegreesDelta()) < 1) {
                return false;
            }
            if(mLastImageView != null
                    && (mEnableUnselectedSticker || mLastImageView == mSelectedStickerFrameView)) {
                mLastImageView.rotateFrame((int)detector.getRotationDegreesDelta());
                addHistory(mLastImageView, HISTORY_ACTION_MODIFY);
                if(mOnStickerStatusChangedListener != null) {
                    mOnStickerStatusChangedListener.onStickerMatrixChanged();
                }
            }
            return true;
        }
    }

    private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDown(MotionEvent e) {
            if(mSelectedStickerFrameView != null && isToolBoxSelected()) {
                mLastImageView = mSelectedStickerFrameView;
            } else {
                mLastImageView = findInnterPoint(e.getX(), e.getY());
                // 스티커 선택 해제
                if(mLastImageView == null) {
                    invokeDeselectSticker();
                }
            }
            if(mLastImageView != null) {
                mLastImageView.setEditProgress(mEditProgress);
            }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            StickerFrameView frameView = findInnterPoint(e.getX(), e.getY());
            // 스티커 선택

            // 스티커 삭제
            if(mSelectedStickerFrameView != null
                    && mSelectedStickerFrameView.isDeleteToolboxSelected()) {
                removeSticker(mStickerFrameViews.indexOf(mSelectedStickerFrameView));
                mSelectedStickerFrameView = null;
                if(mOnStickerStatusChangedListener != null) {
                    mOnStickerStatusChangedListener.onStickerMatrixChanged();
                }
            }
            // 텍스트 스티커 수정
            else if(mSelectedStickerFrameView != null
                    && mSelectedStickerFrameView instanceof TextFrameView
                    && ((TextFrameView)mSelectedStickerFrameView).isEditTextToolboxSelected()) {
                addTextSticker(true);
            } else if(frameView != null && mSelectedStickerFrameView != frameView) {
                Message selectFrameMessage = new Message();
                selectFrameMessage.what = HANDLER_MESSAGE_SELECT_FRAME;
                selectFrameMessage.obj = frameView;
                mHandler.sendMessage(selectFrameMessage);
            }
            // 스티커 선택 해제
            else {
                if(mSelectedStickerFrameView != null
                        && (frameView == null || mSelectedStickerFrameView == frameView)) {
                    mHandler.sendEmptyMessage(HANDLER_MESSAGE_DESELECT_FRAME);
                }
            }
            return true;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            if(mSelectedStickerFrameView != null
                    && mSelectedStickerFrameView.isScaleToolboxSelected()) {
                PointF centerPoint = mSelectedStickerFrameView.getCenterPoint();
                // 회전
                mSelectedStickerFrameView.rotateFrame((float)Math.toDegrees(Math.atan2(detector.getPrevMotionEvent()
                                                                                               .getY()
                                                                                               - centerPoint.y,
                                                                                       detector.getPrevMotionEvent()
                                                                                               .getX()
                                                                                               - centerPoint.x)
                        - (Math.atan2(detector.getCurrMotionEvent().getY() - centerPoint.y,
                                      detector.getCurrMotionEvent().getX() - centerPoint.x))));
                // 스케일
                mSelectedStickerFrameView.scaleFrameByRadius(TransformUtils.getDiameter(detector.getCurrMotionEvent()
                                                                                                .getX()
                                                                                                - centerPoint.x,
                                                                                        detector.getCurrMotionEvent()
                                                                                                .getY()
                                                                                                - centerPoint.y));
                addHistory(mLastImageView, HISTORY_ACTION_MODIFY);
                if(mOnStickerStatusChangedListener != null) {
                    mOnStickerStatusChangedListener.onStickerMatrixChanged();
                }
            } else if(mLastImageView != null && !mLastImageView.isDeleteToolboxSelected()
                    && !mLastImageView.isScaleToolboxSelected() && mMoveEnable
                    && (mEnableUnselectedSticker || mLastImageView == mSelectedStickerFrameView)) {
                Rect stickerRect = mLastImageView.getFrameRect();
                stickerRect.left += detector.getFocusDelta().x;
                stickerRect.right += detector.getFocusDelta().x;
                stickerRect.top += detector.getFocusDelta().y;
                stickerRect.bottom += detector.getFocusDelta().y;

                // 스티커 이동 영역 제한
                if(Rect.intersects(mStickerViewRect, stickerRect)) {
                    mLastImageView.translateFrame(detector.getFocusDelta().x,
                                                  detector.getFocusDelta().y);
                    addHistory(mLastImageView, HISTORY_ACTION_MODIFY);
                    if(mOnStickerStatusChangedListener != null) {
                        mOnStickerStatusChangedListener.onStickerMatrixChanged();
                    }
                }
            }
            return true;
        }
    }

    /**
     * 삭제/크기 조절/편집 버튼이 선택 되어 있는지 확인
     */
    private boolean isToolBoxSelected() {
        if(mSelectedStickerFrameView.isDeleteToolboxSelected()
                || mSelectedStickerFrameView.isScaleToolboxSelected()
                || (mSelectedStickerFrameView instanceof TextFrameView && ((TextFrameView)mSelectedStickerFrameView).isEditTextToolboxSelected())) {
            return true;
        }
        return false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        onAlternativeDraw(canvas, false);
    }

    /**
     * 스티커 이미지를 canvas에 그려준다.
     * 
     * @param canvas 합성된 스티커를 그릴 canvas
     * @param isOutput 최종 output일 경우 true로 설장한다.(false로 설정하면 사용자 가이드 요소도 포함된다.)
     * @version 2.0
     */
    public void onAlternativeDraw(Canvas canvas, boolean isOutput) {
        if(!isOutput && mScalableViewController != null) {
            canvas.save();
            canvas.concat(mScalableViewController.getScaleMatrix());
        }
        for(int i = 0; i < mStickerFrameViews.size(); i++) {
            StickerFrameView imageView = mStickerFrameViews.get(i);
            if(imageView instanceof TextFrameView) {
                if(!TextUtils.isEmpty(((TextFrameView)imageView).getText())) {
                    imageView.drawFrame(canvas, isOutput);
                    ((TextFrameView)imageView).drawText(canvas);
                }
            } else if(imageView instanceof AnimatedStickerFrameView) {
                imageView.drawImage(canvas, mDrawBitmapPaint, isOutput);
                invalidate();
            } else {
                imageView.drawImage(canvas, mDrawBitmapPaint, isOutput);
            }
        }
        for(int i = 0; i < mStickerFrameViews.size(); i++) {
            StickerFrameView imageView = mStickerFrameViews.get(i);
            if(!isOutput) {
                if(mScalableViewController != null) {
                    imageView.setExtraScale(1.f / mScalableViewController.getScale());
                }
                if(imageView instanceof TextFrameView) {
                    if(!TextUtils.isEmpty(((TextFrameView)imageView).getText())) {
                        if(imageView.isSelected()) {
                            imageView.drawSelection(canvas, mSelectionPaint);
                            imageView.drawToolbox(canvas, mDrawBitmapPaint);
                        } else if(imageView.isSubSelected()) {
                            imageView.drawSelection(canvas, mSelectionPaint);
                        }
                    }
                } else {
                    if(imageView.isSelected()) {
                        imageView.drawSelection(canvas, mSelectionPaint);
                        imageView.drawToolbox(canvas, mDrawBitmapPaint);
                    } else if(imageView.isSubSelected()) {
                        imageView.drawSelection(canvas, mSelectionPaint);
                    }
                }
            }
        }
        if(!isOutput && mScalableViewController != null) {
            canvas.restore();
        }
    }
    
    /**
     * 스티커 이미지를 canvas에 그려준다.<br>
     * GIF 스티커의 특정 위치에 해당하는 스티커 이미지로 그려준다.
     * 
     * @param index 움직이는 스티커의 특정 위치
     * @param canvas 합성된 스티커를 그릴 canvas
     * @param isOutput 최종 output일 경우 true로 설장한다.(false로 설정하면 사용자 가이드 요소도 포함된다.)
     * @version 3.0
     */
    public void onAlternativeDraw(int index, Canvas canvas, boolean isOutput) {
        if(!isOutput && mScalableViewController != null) {
            canvas.save();
            canvas.concat(mScalableViewController.getScaleMatrix());
        }
        for(StickerFrameView imageView : mStickerFrameViews) {
            if(imageView instanceof TextFrameView) {
                if(!TextUtils.isEmpty(((TextFrameView)imageView).getText())) {
                    imageView.drawFrame(canvas, isOutput);
                    ((TextFrameView)imageView).drawText(canvas);
                }
            } else if(imageView instanceof AnimatedStickerFrameView) {
                ((AnimatedStickerFrameView)imageView).drawImageWithIndex(index, canvas,
                                                                         mDrawBitmapPaint, isOutput);
            } else {
                imageView.drawImage(canvas, mDrawBitmapPaint, isOutput);
            }
        }
        for(StickerFrameView imageView : mStickerFrameViews) {
            if(!isOutput) {
                if(mScalableViewController != null) {
                    imageView.setExtraScale(1.f / mScalableViewController.getScale());
                }
                if(imageView instanceof TextFrameView) {
                    if(!TextUtils.isEmpty(((TextFrameView)imageView).getText())) {
                        if(imageView.isSelected()) {
                            imageView.drawSelection(canvas, mSelectionPaint);
                            imageView.drawToolbox(canvas, mDrawBitmapPaint);
                        } else if(imageView.isSubSelected()) {
                            imageView.drawSelection(canvas, mSelectionPaint);
                        }
                    }
                } else {
                    if(imageView.isSelected()) {
                        imageView.drawSelection(canvas, mSelectionPaint);
                        imageView.drawToolbox(canvas, mDrawBitmapPaint);
                    } else if(imageView.isSubSelected()) {
                        imageView.drawSelection(canvas, mSelectionPaint);
                    }
                }
            }
        }
        if(!isOutput && mScalableViewController != null) {
            canvas.restore();
        }
    }

    /**
     * x, y좌표에 있는 스티커를 반환한다.
     */
    private StickerFrameView findInnterPoint(float x, float y) {
        for(int i = mStickerFrameViews.size() - 1; i >= 0; i--) {
            if(mStickerFrameViews.get(i).isInnterPoint(x, y)) {
                return mStickerFrameViews.get(i);
            }
        }
        return null;
    }

    /**
     * 스티커를 추가한다
     * 
     * @param sticker 스티커 이미지
     * @param info 스티커 정보
     * @return 추가된 스티커 index
     */
    public int addSticker(Bitmap sticker, StickerFrameInfo info) {
        StickerFrameView stickerView = new StickerFrameView(getContext(), mLastId++, mScale,
                                                            mScaleSel, mScaleLocation, mDelete,
                                                            mDeleteSel, mDeleteLocation, info,
                                                            mDesignTemplate);
        // 중앙으로 이동
        if(sticker != null || info == null) {

            /**
             * U+Story<br>
             * 스티커 설정값을 복원하기 위하여 순서 변경
             */
            stickerView.setImage(sticker);
            stickerView.initFrame(mWidth, mHeight);

            stickerView.setFrameScale(mFrameMinScale, mFrameMaxScale);
            stickerView.setDefaultScale(mFrameDefaultScale);

            if(info == null) {
                stickerView.translateFrame((mWidth - sticker.getWidth()) / 2,
                                           (mHeight - sticker.getHeight()) / 2);
            } else {
                stickerView.translateFrame(info.mCoordinateX, info.mCoordinateY);
            }

            // stickerView.setImage(sticker);
            // stickerView.initFrame();

            mStickerFrameViews.add(stickerView);

            if(info == null) {
                Message message = new Message();
                message.what = HANDLER_MESSAGE_SELECT_FRAME;
                message.obj = stickerView;
                mHandler.sendMessage(message);
            } else {
                mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
            }
        } else {
            loadImageFromUrl(stickerView, info, mFrameMinScale, mFrameMaxScale);
            stickerView.setEnable(false);
            mStickerFrameViews.add(stickerView);
        }
        addHistory(stickerView, HISTORY_ACTION_ADD);
        return mStickerFrameViews.size() - 1;
    }
    
    /**
     * 애니메이션 스티커를 추가한다.
     * 
     * @param stickers 애니메이션 스티커 비트맵 배열
     * @param info 애니메이션 스티커 정보
     * @param duration 애니메이션 gif 교체 주기 m/s
     * @return stickerIndex
     * @version 3.0
     */
    public int addAnimatedSticker(Bitmap[] stickers, StickerFrameInfo info, int duration) {
        AnimatedStickerFrameView stickerView = new AnimatedStickerFrameView(getContext(),
                                                                            mLastId++, mScale,
                                                                            mScaleSel,
                                                                            mScaleLocation,
                                                                            mDelete, mDeleteSel,
                                                                            mDeleteLocation, info,
                                                                            mDesignTemplate);
        // 중앙으로 이동
        if((stickers != null && stickers.length > 0) || info == null) {

            /**
             * U+Story<br>
             * 스티커 설정값을 복원하기 위하여 순서 변경
             */
            stickerView.setAnimatedImage(stickers);
            stickerView.initFrame(mWidth, mHeight);

            stickerView.setFrameScale(mFrameMinScale, mFrameMaxScale);
            stickerView.setDefaultScale(mFrameDefaultScale);

            if(info == null) {
                stickerView.translateFrame((mWidth - stickers[0].getWidth()) / 2,
                                           (mHeight - stickers[0].getHeight()) / 2);
            } else {
                stickerView.translateFrame(info.mCoordinateX, info.mCoordinateY);
            }

            // stickerView.setImage(sticker);
            // stickerView.initFrame();
            stickerView.setAnimationDuration(duration);
            mStickerFrameViews.add(stickerView);
            
//            stickerView.startAnimation();
            startAnimatedSticker();

            if(info == null) {
                Message message = new Message();
                message.what = HANDLER_MESSAGE_SELECT_FRAME;
                message.obj = stickerView;
                mHandler.sendMessage(message);
            } else {
                mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
            }
        } else {
            loadImageFromUrl(stickerView, info, mFrameMinScale, mFrameMaxScale);
            stickerView.setEnable(false);
            mStickerFrameViews.add(stickerView);
        }
        addHistory(stickerView, HISTORY_ACTION_ADD);
        return mStickerFrameViews.size() - 1;
    }
    
    /**
     * 애니메이션 스티커를 추가한다.
     * 
     * @param stickers 애니메이션 스티커 비트맵 배열
     * @param info 애니메이션 스티커 정보
     * @param duration 애니메이션 gif 교체 주기 m/s
     * @return stickerIndex
     * @version 3.0
     */
    public int addAnimatedStickerRestore(Bitmap[] stickers, int duration) {
        AnimatedStickerFrameView stickerView = new AnimatedStickerFrameView(getContext(),
                                                                            mLastId++, mScale,
                                                                            mScaleSel,
                                                                            mScaleLocation,
                                                                            mDelete, mDeleteSel,
                                                                            mDeleteLocation, null,
                                                                            mDesignTemplate);
        // 중앙으로 이동
        if(stickers != null && stickers.length > 0) {
            /**
             * U+Story<br>
             * 스티커 설정값을 복원하기 위하여 순서 변경
             */
            stickerView.setAnimatedImage(stickers);
            stickerView.initFrame(mWidth, mHeight);

            stickerView.setFrameScale(mFrameMinScale, mFrameMaxScale);
            stickerView.setDefaultScale(mFrameDefaultScale);

            stickerView.setAnimationDuration(duration);
            mStickerFrameViews.add(stickerView);
            
//            stickerView.startAnimation();
            startAnimatedSticker();
        }
        return mStickerFrameViews.size() - 1;
    }

    /**
     * 스티커 뷰에 있는 애니메이션 스티커들의 애니메이션을 시작
     * 
     * @version 3.0
     */
    public void startAnimatedSticker() {
        // 모든 스티커의 애니메이션을 새로 시작함
        stopAnimatedSticker();
        for(StickerFrameView stickerFrameView : mStickerFrameViews) {
            if(stickerFrameView instanceof AnimatedStickerFrameView) {
                ((AnimatedStickerFrameView)stickerFrameView).startAnimation();
            }
        }
    }

    /**
     * 스티커 뷰에 있는 애니메이션 스티커들의 애니메이션을 멈춤
     * 
     * @version 3.0
     */
    public void stopAnimatedSticker() {
        for(StickerFrameView stickerFrameView : mStickerFrameViews) {
            if(stickerFrameView instanceof AnimatedStickerFrameView) {
                ((AnimatedStickerFrameView)stickerFrameView).stopAnimation();
            }
        }
    }
    
    /**
     * 합성된 스티커 이미지를 반환<br>
     * GIF 스티커의 특정 위치의 스티커로 변환시켜 합성한다.
     * 
     * @param size 합성된 이미지의 크기
     * @param index GIF 스티커의 특정 위치
     * @return 합성된 이미지
     * @version 3.0
     */
    public Bitmap getStickerImageAnimationIndex(int size, int index) {
        int width = mWidth;
        int height = mHeight;
        if(width > height) {
            height = (int)(height / (float)width * size);
            width = size;
        } else {
            width = (int)(width / (float)height * size);
            height = size;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.save();
        canvas.scale(width / (float)mWidth, height / (float)mHeight);
        onAlternativeDraw(index, canvas, true);
        canvas.restore();
        return bitmap;
    }

    /**
     * GIF 스티커가 포함되었는지 여부를 반환
     * 
     * @return GIF 스티커 포함 여부
     * @version 3.0
     */
    public boolean hasAnimatedSticker() {
        for(StickerFrameView frameView : mStickerFrameViews) {
            if(frameView instanceof AnimatedStickerFrameView) {
                return true;
            }
        }
        return false;
    }

    /**
     * GIF 스티커의 프레임 갯수 반환<br>
     * GIF의 프레임 갯수는 모두 동일하다.
     * 
     * @return GIF 스티커 프레임 갯수
     * @version 3.0
     */
    public int getAnimationStickerFrameCount() {
        for(StickerFrameView frameView : mStickerFrameViews) {
            if(frameView instanceof AnimatedStickerFrameView) {
                return ((AnimatedStickerFrameView)frameView).getAnimationImageCount();
            }
        }
        return 0;
    }

    private void loadImageFromUrl(final StickerFrameView stickerView, final StickerFrameInfo info,
            final float minScale, final float maxScale) {
        final String url = info.mImageUrl;
        if(FileUtils.isNetworkUrl(url)) {
            ImageInfo imageInfo = new ImageInfo();
            imageInfo.caching = false;
            imageInfo.imageURL = info.mImageUrl;
            imageInfo.persistance = true;
            imageInfo.targetBitmapConfig = Config.ARGB_8888;
            imageInfo.listener = new ImageDownloadManagerListener() {
                @Override
                public void progressDownload(int progress) {
                }

                @Override
                public void onImageDownloadComplete(String state, ImageInfo info) {
                    stickerView.setImage(info.bitmap);
                    stickerView.initFrame(mWidth, mHeight);
                    stickerView.setFrameScale(minScale, maxScale);
                    mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
                }
            };
            mImageDownloadManager.ReqDownload(imageInfo);
        } else {
            /**
             * U+Story<br>
             * 타이밍 이슈로 인한 스레드 제거
             */
            // new Thread() {
            // @Override
            // public void run() {
            InputStream backgroundImageInputStream = null;
            try {
                if(mDesignTemplate.mIsThemeTemplate) {
                    backgroundImageInputStream = new FileInputStream(
                                                                     DesignTemplateManager.getInstance(getContext())
                                                                                          .getThemeBasePath(mDesignTemplate.mTheme)
                                                                             + File.separator + url);

                } else {
                    backgroundImageInputStream = getContext().getResources()
                                                             .getAssets()
                                                             .open(DesignTemplateManager.getInstance(getContext())
                                                                                        .getAssetBasePath()
                                                                           + url);
                }
                stickerView.setImage(BitmapFactory.decodeStream(backgroundImageInputStream));
                stickerView.initFrame(mWidth, mHeight);
                stickerView.setFrameScale(minScale, maxScale);
                mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
            } catch(IOException e) {
            } finally {
                if(backgroundImageInputStream != null) {
                    try {
                        backgroundImageInputStream.close();
                    } catch(IOException e) {
                    }
                }
            }
            // }
            // }.start();
        }
    }

    /**
     * 스티커를 추가한다.
     * 
     * @param sticker 스티커 이미지
     * @return 추가된 스티커 index
     * @version 1.0
     */
    public int addSticker(Bitmap sticker) {
        return addSticker(sticker, null);
    }

    /**
     * U+Story<br>
     * 스티커 데이터 정보를 가지고 스티커를 복원한다.<br>
     * 
     * @param sticker 스티커 이미지
     * @return 추가된 스티커 index
     */
    public int addStickerRestore(Bitmap sticker) {
        StickerFrameView stickerView = new StickerFrameView(getContext(), mLastId++, mScale,
                                                            mScaleSel, mScaleLocation, mDelete,
                                                            mDeleteSel, mDeleteLocation, null,
                                                            mDesignTemplate);
        stickerView.setImage(sticker);
        stickerView.initFrame(mWidth, mHeight);
        stickerView.setFrameScale(mFrameMinScale, mFrameMaxScale);
        stickerView.setDefaultScale(mFrameDefaultScale);
        mStickerFrameViews.add(stickerView);
        return mStickerFrameViews.size() - 1;
    }

    /**
     * 스티커를 삭제한다.
     * 
     * @param index 스티커 index
     * @version 1.0
     */
    public void removeSticker(int index) {
        if(index > mStickerFrameViews.size() - 1) {
            return;
        }
        StickerFrameView sticker = mStickerFrameViews.get(index);
        sticker.clear();
        if(mStickerFrameViews.remove(sticker) && mOnStickerStatusChangedListener != null) {
            mOnStickerStatusChangedListener.onStickerRemoved(index);
        }
        if(mSelectedStickerFrameView == sticker) {
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_DESELECT_FRAME);
        } else {
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
        addHistory(sticker, HISTORY_ACTION_DELETE);
    }

    /**
     * 모든 스티커를 삭제한다.
     */
    public void removeAllStickers() {
        for(StickerFrameView sticker : mStickerFrameViews) {
            sticker.clear();
        }
        mStickerFrameViews.clear();
    }

    /**
     * 현재 선택된 스티커를 삭제한다.
     * 
     * @version 1.0
     */
    public void removeSelectedSticker() {
        if(mSelectedStickerFrameView != null) {
            removeSticker(mStickerFrameViews.indexOf(mSelectedStickerFrameView));
        }
    }

    private void selectSticker(StickerFrameView selecteView) {
        if(selecteView == null) {
            return;
        }
        mSelectedStickerFrameView = selecteView;
        int selectIndex = -1;
        for(int i = 0; i < mStickerFrameViews.size(); i++) {
//        for(StickerFrameView frameView : mStickerFrameViews) {
            StickerFrameView frameView = mStickerFrameViews.get(i);
            if(frameView.isSelected()) {
                frameView.setSelected(false);
            }
            if(mSelectedStickerFrameView.equals(frameView)) {
                selectIndex = i;
            }
            frameView.setSubSelected(mSelectedStickerFrameView.getId());
        }
        mSelectedStickerFrameView.setSelected(true);
        mStickerFrameViews.remove(mSelectedStickerFrameView);
        mStickerFrameViews.add(mSelectedStickerFrameView);
        if(mOnStickerStatusChangedListener != null) {
            if(mSelectedStickerFrameView instanceof TextFrameView) {
                mOnStickerStatusChangedListener.onStickerSelected(true, STICKER_TYPE_TEXT, selectIndex);
            } else {
                mOnStickerStatusChangedListener.onStickerSelected(true, STICKER_TYPE_IMAGE, selectIndex);
            }
        }
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 특정 위치의 스티커를 선택
     * 
     * @param index 선택 위치
     */
    public void selectSticker(int index) {
        if(index < mStickerFrameViews.size()) {
            selectSticker(mStickerFrameViews.get(index));
        }
    }

    /**
     * 스티커 간의 관계 형성. [U+Camera>겔러리>편집>뷰티>메이크 업] 기능에서 속눈썹/볼터치를 쌍으로 선택하기 위해 사용.
     */
    public void addRelativeSticker(int indexA, int indexB) {
        if(indexA < mStickerFrameViews.size() && indexB < mStickerFrameViews.size()) {
            mStickerFrameViews.get(indexA).addRelativeSticker(mStickerFrameViews.get(indexB)
                                                                                .getId());
            mStickerFrameViews.get(indexB).addRelativeSticker(mStickerFrameViews.get(indexA)
                                                                                .getId());
        }
    }

    /**
     * 현재 선택된 스티커의 index를 반환한다.
     * 
     * @return 스티커 index. 선택된 스티커 없으면 -1
     * @version 1.0
     */
    public int getSelectedStickerIndex() {
        if(mSelectedStickerFrameView != null) {
            return mStickerFrameViews.indexOf(mSelectedStickerFrameView);
        }
        return -1;
    }

    /**
     * 선택된 스티커의 alpha를 변경
     * 
     * @param alpha
     */
    public void setSelectedStickerImageAlpha(int alpha) {
        if(mSelectedStickerFrameView != null) {
            mSelectedStickerFrameView.setImageAlpha(alpha);
            for(StickerFrameView frameView : mStickerFrameViews) {
                if(frameView.isSubSelected()) {
                    frameView.setImageAlpha(alpha);
                }
            }
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 선택된 스티커의 투명도 반환
     * 
     * @return 투명도
     */
    public int getSelectedStickerImageAlpha() {
        if(mSelectedStickerFrameView != null) {
            return mSelectedStickerFrameView.getImageAlpha();
        }
        return 0;
    }

    /**
     * 특정 스티커의 alpha값 설정
     * 
     * @param index 특정 스티커 index
     * @param alpha 설정할 투명도
     */
    public void setStickerImageAlpha(int index, int alpha) {
        if(index < mStickerFrameViews.size()) {
            mStickerFrameViews.get(index).setImageAlpha(alpha);
            for(StickerFrameView frameView : mStickerFrameViews) {
                if(frameView.isSubSelected()) {
                    frameView.setImageAlpha(alpha);
                }
            }
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 특정 스티커를 이동시킨다.
     * 
     * @param index 특정 스티커 index
     * @param dx x이동 거리
     * @param dy y이동 거리
     */
    public void setStickerTranslate(int index, float dx, float dy) {
        if(index < mStickerFrameViews.size()) {
            mStickerFrameViews.get(index).setTranslateFrame(dx, dy);
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 특정 스티커의 scale값을 변경
     * 
     * @param index 특정 스티커 index
     * @param scale scale값
     */
    public void setStickerScale(int index, float scale) {
        if(index < mStickerFrameViews.size()) {
            mStickerFrameViews.get(index).scaleFrameLeftTop(scale);
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 스티커 선택을 해제한다.
     * 
     * @version 1.0
     */
    public void deselectSticker() {
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_DESELECT_FRAME);
    }

    private void invokeDeselectSticker() {
        for(StickerFrameView imageView : mStickerFrameViews) {
            imageView.setSelected(false);
            imageView.setSubSelected(false);
        }
        if(mOnStickerStatusChangedListener != null) {
            if(mSelectedStickerFrameView instanceof TextFrameView) {
                mOnStickerStatusChangedListener.onStickerSelected(false, STICKER_TYPE_TEXT, -1);
            } else {
                mOnStickerStatusChangedListener.onStickerSelected(false, STICKER_TYPE_IMAGE, -1);
            }
        }
        mSelectedStickerFrameView = null;
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 스티커 개수를 반환한다.
     * 
     * @return 스티커 개수
     * @version 1.0
     */
    public int getStickerCount() {
        return mStickerFrameViews.size();
    }
    
    /**
     * 총 스티커 중 이미지 스티커 개수를 반환한다.
     * @return 이미지 스티커 개수
     */
    public int getImageStickerCount() {
        if (mStickerFrameViews == null || mStickerFrameViews.isEmpty()) {
            return 0;
        }
        
        int count = 0;
        for (int i = 0; i < mStickerFrameViews.size(); i++) {
            StickerFrameView stickerFrameView = mStickerFrameViews.get(i);
            if (!(stickerFrameView instanceof TextFrameView)) {
                if (stickerFrameView instanceof AnimatedStickerFrameView) {
                    count += ((AnimatedStickerFrameView)stickerFrameView).getAnimationImageCount();
                } else {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * 스티커 이미지의 scale 범위를 설정한다.
     * 
     * @param minScale 최소 scale 값
     * @param maxScale 최대 scale 값
     * @version 1.0
     */
    public void setStickerScale(float minScale, float maxScale) {
        mFrameMinScale = minScale;
        mFrameMaxScale = maxScale;
        for(StickerFrameView imageView : mStickerFrameViews) {
            imageView.setFrameScale(minScale, maxScale);
        }
    }

    /**
     * Desc : 스티커 이미지의 기본 scale 범위를 설정한다.
     * 
     * @Method Name : setStickerDefaultScale
     * @param defScale
     * @version 1.0
     */
    public void setStickerDefaultScale(float defScale) {
        mFrameDefaultScale = defScale;

        for(StickerFrameView imageView : mStickerFrameViews) {
            imageView.setDefaultScale(defScale);
        }
    }

    /**
     * 스티커 선택 박스 color를 설정한다. <br>
     * Note that the color is an int containing alpha as well as r,g,b. This 32bit value is not
     * premultiplied, meaning that its alpha can be any value, regardless of the values of r,g,b.
     * See the Color class for more details.
     * 
     * @param color 스티커 선택 박스 color
     * @version 1.0
     */
    public void setStickerSelectionColor(int color) {
        mSelectionPaint.setColor(color);
    }

    /**
     * 스티커 선택 박스 color, width를 설정한다. <br>
     * Note that the color is an int containing alpha as well as r,g,b. This 32bit value is not
     * premultiplied, meaning that its alpha can be any value, regardless of the values of r,g,b.
     * See the Color class for more details.
     * 
     * @param color 스티커 선택 박스 color
     * @param width 스티커 선택 박스 border width
     * @version 1.0
     */
    public void setStickerSelectionColorWithWidth(int color, float width) {
        mSelectionPaint.setColor(color);
        mSelectionPaint.setStrokeWidth(width);
        mSelectionPaintOverride = true;
    }

    /**
     * 스티커 삭제 버튼의 이미지를 설정한다.
     * 
     * @param normalResId normal 상태의 버튼 이미지 resource id
     * @param pressedResId pressed 상태의 버튼 이미지 resource id. -1 이면 normalResId 사용
     * @version 1.0
     */
    public void setCloseImage(int normalResId, int pressedResId) {
        setCloseImage(BitmapFactory.decodeResource(getResources(), normalResId),
                      BitmapFactory.decodeResource(getResources(), pressedResId));
    }

    /**
     * 스티커 삭제 버튼의 이미지를 설정한다.
     * 
     * @param normalImage normal 상태의 버튼 이미지
     * @param pressedImage pressed 상태의 버튼 이미지. null 이면 normalImage 사용
     * @version 1.0
     */
    public void setCloseImage(Bitmap normalImage, Bitmap pressedImage) {
        if(mDelete != null) {
            mDelete.recycle();
        }
        if(mDeleteSel != null) {
            mDeleteSel.recycle();
        }
        mDelete = normalImage;
        mDeleteSel = pressedImage;
        for(StickerFrameView imageView : mStickerFrameViews) {
            imageView.setCloseImage(normalImage, pressedImage);
        }
    }

    /**
     * 스티커 회전 및 크기조절 버튼의 위치를 설정한다.
     * 
     * @param location 다음 중 하나의 값을 가진다.
     * @see {@link #STICKER_BUTTON_LOCATION_LEFT_TOP}<br>
     *      {@link #STICKER_BUTTON_LOCATION_RIGHT_TOP}<br>
     *      {@link #STICKER_BUTTON_LOCATION_LEFT_BOTTOM}<br>
     *      {@link #STICKER_BUTTON_LOCATION_RIGHT_BOTTOM}
     * @version 1.0
     */
    public void setScaleButtonLocation(int location) {
        mScaleLocation = location;
        if(location >= 0 && location <= 3) {
            for(StickerFrameView imageView : mStickerFrameViews) {
                imageView.setScaleToolBoxRect(location);
            }
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 스티커 삭제 버튼의 위치를 설정한다.
     * 
     * @param location 다음 중 하나의 값을 가진다.
     * @see {@link #STICKER_BUTTON_LOCATION_LEFT_TOP}<br>
     *      {@link #STICKER_BUTTON_LOCATION_RIGHT_TOP}<br>
     *      {@link #STICKER_BUTTON_LOCATION_LEFT_BOTTOM}<br>
     *      {@link #STICKER_BUTTON_LOCATION_RIGHT_BOTTOM}
     * @version 1.0
     */
    public void setCloseButtonLocation(int location) {
        mDeleteLocation = location;
        if(location >= 0 && location <= 3) {
            for(StickerFrameView imageView : mStickerFrameViews) {
                imageView.setDeleteToolBoxRect(location);
            }
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 텍스트 스티커 수정 버튼의 위치를 설정한다.
     * 
     * @param location 다음 중 하나의 값을 가진다.
     * @see {@link #STICKER_BUTTON_LOCATION_LEFT_TOP}<br>
     *      {@link #STICKER_BUTTON_LOCATION_RIGHT_TOP}<br>
     *      {@link #STICKER_BUTTON_LOCATION_LEFT_BOTTOM}<br>
     *      {@link #STICKER_BUTTON_LOCATION_RIGHT_BOTTOM}
     * @version 1.0
     */
    public void setEditButtonLocation(int location) {
        mTextEditLocation = location;
        if(location >= 0 && location <= 3) {
            for(StickerFrameView imageView : mStickerFrameViews) {
                if(imageView instanceof TextFrameView) {
                    ((TextFrameView)imageView).setEditTextToolBoxRect(location);
                }
            }
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 스티커 수정 버튼의 이미지를 설정한다.
     * 
     * @param normalResId normal 상태의 버튼 이미지 resource id
     * @param pressedResId pressed 상태의 버튼 이미지 resource id. -1 이면 normalResId 사용
     * @version 1.0
     */
    public void setTextEditImage(int normalResId, int pressedResId) {
        setTextEditImage(BitmapFactory.decodeResource(getResources(), normalResId),
                         BitmapFactory.decodeResource(getResources(), pressedResId));
    }

    /**
     * 스티커 수정 버튼의 이미지를 설정한다.
     * 
     * @param normalImage normal 상태의 버튼 이미지
     * @param pressedImage pressed 상태의 버튼 이미지. null 이면 normalImage 사용
     * @version 1.0
     */
    public void setTextEditImage(Bitmap normalImage, Bitmap pressedImage) {
        if(mTextEdit != null) {
            mTextEdit.recycle();
        }
        if(mTextEditSel != null) {
            mTextEditSel.recycle();
        }
        mTextEdit = normalImage;
        mTextEditSel = pressedImage;
        for(StickerFrameView imageView : mStickerFrameViews) {
            if(imageView instanceof TextFrameView) {
                ((TextFrameView)imageView).setEditTextImage(normalImage, pressedImage);
            }
        }
    }

    /**
     * 스티커 회전, 확대/축소 버튼의 이미지를 설정한다.
     * 
     * @param normalResId normal 상태의 버튼 이미지 resource id
     * @param pressedResId pressed 상태의 버튼 이미지 resource id. -1 이면 normalResId 사용
     * @version 1.0
     */
    public void setScaleImage(int normalResId, int pressedResId) {
        setScaleImage(BitmapFactory.decodeResource(getResources(), normalResId),
                      BitmapFactory.decodeResource(getResources(), pressedResId));
    }

    /**
     * 스티커 회전, 확대/축소 버튼의 이미지를 설정한다.
     * 
     * @param normalImage normal 상태의 버튼 이미지
     * @param pressedImage pressed 상태의 버튼 이미지. null 이면 normalImage 사용
     * @version 1.0
     */
    public void setScaleImage(Bitmap normalImage, Bitmap pressedImage) {
        if(mScale != null) {
            mScale.recycle();
        }
        if(mScaleSel != null) {
            mScaleSel.recycle();
        }
        mScale = normalImage;
        mScaleSel = pressedImage;
        for(StickerFrameView imageView : mStickerFrameViews) {
            imageView.setCloseImage(normalImage, pressedImage);
        }
    }

    /**
     * 특정 스티커의 이미지를 반환
     * 
     * @param index 특정 스티커 index
     * @return 스티커 이미지
     */
    public Bitmap getImage(int index) {
        if(index > mStickerFrameViews.size() - 1) {
            return null;
        }
        return mStickerFrameViews.get(index).getImage();
    }

    /**
     * 합성된 스키터 이미지를 반환한다.
     * 
     * @param size 합성된 이미지의 크기
     * @return 합성된 이미지
     * @version 1.0
     */
    public Bitmap getStickerImage(int size) {
        int width = mWidth;
        int height = mHeight;
        if(width > height) {
            height = (int)(height / (float)width * size);
            width = size;
        } else {
            width = (int)(width / (float)height * size);
            height = size;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);
        canvas.save();
        canvas.scale(width / (float)mWidth, height / (float)mHeight);
        onAlternativeDraw(canvas, true);
        canvas.restore();
        if(Constants.DEMO_VERSION) {
            BitmapUtils.applyWaterMarkImage(getContext(), bitmap);
        }
        return bitmap;
    }

    private void addTextSticker(TextStickerFrameInfo info) {
        TextFrameView textView = new TextFrameView(getContext(), mLastId++, mScale, mScaleSel,
                                                   mScaleLocation, mDelete, mDeleteSel,
                                                   mDeleteLocation, mTextEdit, mTextEditSel,
                                                   mTextEditLocation);
        textView.initWithInfo(info);
        textView.setFrameScale(mFrameMinScale, mFrameMaxScale);
        mStickerFrameViews.add(textView);
        Message message = new Message();
        message.what = HANDLER_MESSAGE_SELECT_FRAME;
        message.obj = textView;
        mHandler.sendMessage(message);
    }

    /**
     * 텍스트 스티커를 추가한다.
     * 
     * @version 1.0
     */
    public void addTextSticker() {
        addTextSticker(false);
    }

    /**
     * 선택된 텍스트 스티커의 문자열을 변경한다.
     * 
     * @param text 변경할 문자열
     * @version 2.0
     */
    public void changeTextSticker(String text) {
        ((TextFrameView)mSelectedStickerFrameView).setText(text);
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 텍스트 스티커 입력창 버튼을 추가한다.<br>
     * Button의 parent view는 RelativeLayout이다.
     * 
     * @param cancel 텍스트 입력 취소 버튼
     * @param confirm 텍스트 입력 완료 버튼
     * @version 1.0
     */
    public void setTextStickerButton(View cancel, View confirm) {
        mInputStickerCancel = cancel;
        mInputStickerDone = confirm;
    }

    /**
     * 해당 인덱스 텍스트 스티커의 문자열을 반환한다.
     * 
     * @param index 문자열을 반환할 텍스트 스티커의 index
     * @return 텍스트 스티커의 문자열
     * @version 2.0
     */
    public String getTextSticker(int index) {
        if(index > mStickerFrameViews.size() - 1
                || !(mStickerFrameViews.get(index) instanceof TextFrameView)) {
            return null;
        }
        return ((TextFrameView)mStickerFrameViews.get(index)).getText();
    }

    /**
     * 선택된 텍스트 스티커가 있으면 해당 텍스트 스티커의 문자열을 변경하고, 없으면 새로운 텍스트 스티커를 추가한다.
     * 
     * @param text 변경 또는 추가할 문자열
     * @version 2.0
     */
    public boolean setTextSticker(String text) {
        if(mSelectedStickerFrameView == null
                || !(mSelectedStickerFrameView instanceof TextFrameView)) {
            addTextSticker(text);
            return false;
        } else {
            ((TextFrameView)mSelectedStickerFrameView).setText(text);
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
            return true;
        }
    }

    /**
     * 새로운 텍스트 스티커를 추가한다.
     * 
     * @param text 추가할 문자열
     * @return 추가된 텍스트 스티커 index
     * @version 2.0
     */
    public int addTextSticker(String text) {
        TextFrameView textView = new TextFrameView(getContext(), mLastId++, mScale, mScaleSel,
                                                   mScaleLocation, mDelete, mDeleteSel,
                                                   mDeleteLocation, mTextEdit, mTextEditSel,
                                                   mTextEditLocation);
        textView.setText(text);
        textView.setTextColor(mDefaultTextColor);
        textView.initFrame(mWidth, mHeight);
        textView.translateFrame((mWidth - textView.getTextWidth()) / 2,
                                (mHeight - textView.getTextHeight()) / 5);
        textView.setFrameScale(mFrameMinScale, mFrameMaxScale);
        mStickerFrameViews.add(textView);
        Message message = new Message();
        message.what = HANDLER_MESSAGE_SELECT_FRAME;
        message.obj = textView;
        mHandler.sendMessage(message);

        return mStickerFrameViews.size() - 1;
    }

    /**
     * 텍스트 스티커 데이터 정보를 가지고 텍스트 스티커를 복원한다.<br>
     * U+Story
     * 
     * @param text 텍스트
     * @return 추가된 텍스트 스티커 index
     */
    public int addTextStickerRestore(String text) {
        TextFrameView textView = new TextFrameView(getContext(), mLastId++, mScale, mScaleSel,
                                                   mScaleLocation, mDelete, mDeleteSel,
                                                   mDeleteLocation, mTextEdit, mTextEditSel,
                                                   mTextEditLocation);
        textView.setText(text);
        textView.setTextColor(mDefaultTextColor);
        textView.initFrame(mWidth, mHeight);
        textView.setFrameScale(mFrameMinScale, mFrameMaxScale);
        mStickerFrameViews.add(textView);

        return mStickerFrameViews.size() - 1;
    }

    /**
     * 11번가에서 사용하던 method. 스티커 추가/편집 시에 별도의 UI를 제공한다.
     */
    private void addTextSticker(boolean currentFrameModify) {
        if(mInputStickerCancel != null) {
            ViewParent parent = mInputStickerCancel.getParent();
            if(parent instanceof ViewGroup) {
                ((ViewGroup)parent).removeView(mInputStickerCancel);
            }
        }
        if(mInputStickerCancel != null) {
            ViewParent parent = mInputStickerDone.getParent();
            if(parent instanceof ViewGroup) {
                ((ViewGroup)parent).removeView(mInputStickerDone);
            }
        }

        final Dialog dialog = new Dialog(getContext());

        final StickerTextEditView editLayout = new StickerTextEditView(getContext());
        editLayout.setTextStickerEdit(currentFrameModify);

        final EditText editText = editLayout.getEditText();
        editText.setTextSize(mDefaultTextSize);
        if(currentFrameModify && mSelectedStickerFrameView != null
                && mSelectedStickerFrameView instanceof TextFrameView) {
            editText.setText(((TextFrameView)mSelectedStickerFrameView).getText());
            if(editText.length() > 0) {
                editText.setSelection(editText.length());
            }
            editText.setTextColor(((TextFrameView)mSelectedStickerFrameView).getTextColor());
        } else {
            editText.setTextColor(mDefaultTextColor);
        }

        if(mInputTextBackground != null) {
            editText.setBackgroundDrawable(mInputTextBackground);
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                SmartLog.d(TAG, "onTextChanged: " + s + ", start: " + start + ", before: " + before
                        + ", count" + count);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                SmartLog.d(TAG, "beforeTextChanged: " + s + ", start: " + start + ", after: "
                        + after + ", count" + count);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(s.length() > mLimitLength) {
                    s.delete(mLimitLength, s.length());
                    if(mOnStickerStatusChangedListener != null) {
                        mOnStickerStatusChangedListener.onStickerTextLengthExceed();
                    }
                }
                SmartLog.d(TAG, "afterTextChanged: " + s.toString());
            }
        });

        if(mInputStickerCancel != null) {
            editLayout.addView(mInputStickerCancel);
            mInputStickerCancel.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                }
            });
        }
        if(mInputStickerDone != null) {
            editLayout.addView(mInputStickerDone);
            mInputStickerDone.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog.dismiss();
                    StringBuffer text = new StringBuffer(editText.getText().toString());
                    int insertCount = 0;
                    try {
                        if(text.length() != 0) {
                            Layout layout = editText.getLayout();
                            for(int i = 0; i < layout.getLineCount(); i++) {
                                int lastCharIndex = layout.getLineEnd(i) - 1 + insertCount;
                                if(text.charAt(lastCharIndex) != '\n') {
                                    text.insert(lastCharIndex + 1, '\n');
                                    insertCount++;
                                }
                            }
                            if(text.charAt(text.length() - 1) == '\n') {
                                text.deleteCharAt(text.length() - 1);
                            }
                        }
                    } catch(Throwable e) {
                    }
                    String resultText = text.toString();
                    if(editLayout.isTextStickerEdit() && mSelectedStickerFrameView != null
                            && mSelectedStickerFrameView instanceof TextFrameView) {
                        if(!TextUtils.isEmpty(resultText)) {
                            ((TextFrameView)mSelectedStickerFrameView).setText(resultText);
                            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
                        } else {
                            removeSelectedSticker();
                        }
                    } else {
                        if(!TextUtils.isEmpty(resultText)) {
                            addTextSticker(resultText);
                        }
                    }
                }
            });
        }

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(editLayout);

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = LayoutParams.MATCH_PARENT;
        lp.height = LayoutParams.MATCH_PARENT;
        lp.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE;
        dialog.show();
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    /**
     * 선택된 텍스트 스티커의 {@link android.graphics.Paint#Style}을 변경한다.
     * 
     * @param style 변경할 스타일
     * @version 2.0
     */
    public void setTextStickerStyle(Style style) {
        if(mSelectedStickerFrameView != null && mSelectedStickerFrameView instanceof TextFrameView) {
            ((TextFrameView)mSelectedStickerFrameView).setStyle(style);
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 특정 텍스트 스티커의 스타일을 변경
     * 
     * @param index 텍스트 스티커 index
     * @param style 스타일
     */
    public void setTextStickerStyle(int index, Style style) {
        if(index > mStickerFrameViews.size() - 1
                || !(mStickerFrameViews.get(index) instanceof TextFrameView)) {
            return;
        }
        ((TextFrameView)mStickerFrameViews.get(index)).setStyle(style);
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 특정 텍스트 스티커 스타일을 가져옴
     * 
     * @param index 텍스트 스티커 index
     * @return 스타일
     */
    public Style getTextStickerStyle(int index) {
        if(index > mStickerFrameViews.size() - 1
                || !(mStickerFrameViews.get(index) instanceof TextFrameView)) {
            return Style.FILL;
        }
        return ((TextFrameView)mStickerFrameViews.get(index)).getStyle();
    }

    /**
     * 텍스트 스티커의 폰트를 변경한다.
     * 
     * @param typeface 폰트
     * @version 1.0
     */
    public void setTextStickerFont(Typeface typeface) {
        if(mSelectedStickerFrameView != null && mSelectedStickerFrameView instanceof TextFrameView) {
            ((TextFrameView)mSelectedStickerFrameView).setTextTypeface(typeface);
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 특정 텍스트 스티커의 폰트를 변경
     * 
     * @param index 텍스트 스티커 index
     * @param typeface 폰트
     */
    public void setTextStickerFont(int index, Typeface typeface) {
        if(index > mStickerFrameViews.size() - 1
                || !(mStickerFrameViews.get(index) instanceof TextFrameView)) {
            return;
        }
        ((TextFrameView)mStickerFrameViews.get(index)).setTextTypeface(typeface);
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 텍스트 스티커의 색상을 변경한다. <br>
     * Note that the color is an int containing alpha as well as r,g,b. This 32bit value is not
     * premultiplied, meaning that its alpha can be any value, regardless of the values of r,g,b.
     * See the Color class for more details.
     * 
     * @param color 텍스트 색상
     * @version 1.0
     */
    public void setTextStickerTextColor(int color) {
        if(mSelectedStickerFrameView != null && mSelectedStickerFrameView instanceof TextFrameView) {
            ((TextFrameView)mSelectedStickerFrameView).setTextColor(color);
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 특정 텍스트 스티커의 글자 색상을 변경한다
     * 
     * @param index 텍스트 스티커 index
     * @param color 글자 색상
     */
    public void setTextStickerTextColor(int index, int color) {
        if(index < 0 || index > mStickerFrameViews.size() - 1
                || !(mStickerFrameViews.get(index) instanceof TextFrameView)) {
            return;
        }
        ((TextFrameView)mStickerFrameViews.get(index)).setTextColor(color);
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 특정 텍스트 스티커의 글자 색상을 반환
     * 
     * @param index 텍스트 스티커 index
     * @return 글자 색상
     */
    public int getTextStickerTextColor(int index) {
        if(index < 0 || index > mStickerFrameViews.size() - 1
                || !(mStickerFrameViews.get(index) instanceof TextFrameView)) {
            return 0;
        }
        return ((TextFrameView)mStickerFrameViews.get(index)).getTextColor();
    }

    /**
     * 텍스트 스티커의 글자 테두리 색상을 설정한다.
     * 
     * @param color 테두리 색상
     * @version 2.0
     */
    public void setTextStickerTextBorderColor(int color) {
        if(mSelectedStickerFrameView != null && mSelectedStickerFrameView instanceof TextFrameView) {
            ((TextFrameView)mSelectedStickerFrameView).setTextBorderColor(color);
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 특정 텍스트 스티커의 테두리 색상을 설정
     * 
     * @param index 텍스트 스티커 index
     * @param color 테두리 색상
     */
    public void setTextStickerTextBorderColor(int index, int color) {
        if(index < 0 || index > mStickerFrameViews.size() - 1
                || !(mStickerFrameViews.get(index) instanceof TextFrameView)) {
            return;
        }
        ((TextFrameView)mStickerFrameViews.get(index)).setTextBorderColor(color);
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 특정 텍스트 스티커의 테두리 색상을 반환
     * 
     * @param index 텍스트 스티커 index
     * @return 테두리 색상
     */
    public int getTextStickerTextBorderColor(int index) {
        if(index < 0 || index > mStickerFrameViews.size() - 1
                || !(mStickerFrameViews.get(index) instanceof TextFrameView)) {
            return 0;
        }
        return ((TextFrameView)mStickerFrameViews.get(index)).getTextBorderColor();
    }

    /**
     * 텍스트 스티커의 글자 테두리 두께를 설정한다.
     * 
     * @param width 테두리 두께
     * @version 2.0
     */
    public void setTextStickerTextBorderWidth(float width) {
        if(mSelectedStickerFrameView != null && mSelectedStickerFrameView instanceof TextFrameView) {
            ((TextFrameView)mSelectedStickerFrameView).setTextBorderWidth(width);
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 특정 텍스트 스티커의 글자 테두리 두께를 설정
     * 
     * @param index 텍스트 스티커 index
     * @param width 글자 테두리 두께
     */
    public void setTextStickerTextBorderWidth(int index, float width) {
        if(index < 0 || index > mStickerFrameViews.size() - 1
                || !(mStickerFrameViews.get(index) instanceof TextFrameView)) {
            return;
        }
        ((TextFrameView)mStickerFrameViews.get(index)).setTextBorderWidth(width);
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 텍스트 스티커의 글자 두께를 설정한다.
     * 
     * @param width
     * @version 2.0
     */
    public void setTextStickerTextWidth(float width) {
        if(mSelectedStickerFrameView != null && mSelectedStickerFrameView instanceof TextFrameView) {
            ((TextFrameView)mSelectedStickerFrameView).setTextWidth(width);
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 특정 텍스트 스티커의 글자 두께를 설정한다
     * 
     * @param index 텍스트 스티커 index
     * @param width 글자 두께
     */
    public void setTextStickerTextWidth(int index, float width) {
        if(index < 0 || index > mStickerFrameViews.size() - 1
                || !(mStickerFrameViews.get(index) instanceof TextFrameView)) {
            return;
        }
        ((TextFrameView)mStickerFrameViews.get(index)).setTextWidth(width);
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 텍스트 스티커의 글자 테두리 두께를 반환한다.
     * 
     * @param index 텍스트 스티커의 index
     * @return 테두리 두께
     * @version 2.0
     */
    public float getTextStickerTextBorderWidth(int index) {
        if(index > mStickerFrameViews.size() - 1
                || !(mStickerFrameViews.get(index) instanceof TextFrameView)) {
            return 0;
        }
        return ((TextFrameView)mStickerFrameViews.get(index)).getTextBorderWidth();
    }

    /**
     * 텍스트 스티커의 글자 두께를 반환한다.
     * 
     * @param index 텍스트 스티커의 index
     * @return 글자 두께
     * @version 2.0
     */
    public float getTextStickerTextWidth(int index) {
        if(index > mStickerFrameViews.size() - 1
                || !(mStickerFrameViews.get(index) instanceof TextFrameView)) {
            return 0;
        }
        return ((TextFrameView)mStickerFrameViews.get(index)).getTextFillWidth();
    }

    /**
     * 텍스트 스티커의 기본 색상을 설정한다. <br>
     * Note that the color is an int containing alpha as well as r,g,b. This 32bit value is not
     * premultiplied, meaning that its alpha can be any value, regardless of the values of r,g,b.
     * See the Color class for more details.
     * 
     * @param color 텍스트 색상
     * @version 1.0
     */
    public void setTextStickerDefaultTextColor(int color) {
        mDefaultTextColor = color;
    }

    /**
     * 텍스트 스티커 입력창의 글자 크기를 설정한다.
     * 
     * @param textSize 텍스트 크기.
     * @version 1.0
     */
    public void setTextStickerTextSize(float textSize) {
        mDefaultTextSize = textSize;
    }

    /**
     * 텍스트 입력창의 테두리 색상과 두께를 설정한다.
     * 
     * @param color 테두리 색상
     * @param width 테두리 두께
     * @version 1.0
     */
    public void setTextInputBorderColorWithWidth(final int color, final float width) {
        mInputTextBackground = new ShapeDrawable();
        mInputTextBackground.setShape(new RectShape());
        mInputTextBackground.getPaint().setColor(color);
        mInputTextBackground.getPaint().setStyle(Style.STROKE);
        mInputTextBackground.getPaint().setStrokeWidth(width);
    }

    /**
     * 텍스트 스티커의 배경을 설정한다.
     * 
     * @param stickerBackground 배경 이미지
     * @version 1.0
     */
    public void setTextStickerBackground(Bitmap stickerBackground) {
        if(mSelectedStickerFrameView != null && mSelectedStickerFrameView instanceof TextFrameView) {
            ((TextFrameView)mSelectedStickerFrameView).setTextBackground(stickerBackground);
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 텍스트 스티커의 길이를 제한 한다.
     * 
     * @param limitLength 텍스트 스티커의 제한 길이
     * @version 1.0
     */
    public void setTextStickerLimitLength(int limitLength) {
        mLimitLength = limitLength;
    }

    /**
     * U+Story<br>
     * 스티커 이동 영역 제한 값을 가져온다.
     * 
     * @return Point 스티커 이동 영역 제한 값
     */
    public Point getTranslatePadding() {
        return mTranslatePadding;
    }

    /**
     * 스티커 이동 영역을 제한한다.
     * 
     * @param padding 스티커 이동 영역 제한 x, y 값. default 20, 20
     * @version 1.0
     */
    public void setTranslatePadding(Point padding) {
        mTranslatePadding.x = padding.x;
        mTranslatePadding.y = padding.y;
    }

    /**
     * 선택되지 않은 스티커에 대한 편집 가능 여부 설정
     */
    public void setEnableUnselectedSticker(boolean enable) {
        mEnableUnselectedSticker = enable;
    }

    /**
     * 뷰 전체에 대한 확대/축소 기능 지원 여부
     */
    public void setScalableTouch(boolean scalableTouch) {
        mScalableTouch = scalableTouch;
    }

    /**
     * 뷰 전체에 대한 확대/축소 기능을 지원하는 ScalableViewController를 설정
     */
    public void setScalableViewController(ScalableViewController controller) {
        mScalableViewController = controller;
    }

    /**
     * 뷰 전체에 대한 확대/축소 기능 지원 여부 및 ScalableViewController를 설정
     * 
     * @param scalable Scale 여부
     */
    public void setScalable(boolean scalable) {
        mScalableTouch = scalable;
        if(scalable) {
            mScalableViewController = new ScalableViewController(getContext());
            mScalableViewController.setOnInvalidateListener(new OnInvalidateListener() {
                @Override
                public void onInvalidate() {
                    invalidate();
                }
            });
        } else if(mScalableViewController != null) {
            mScalableViewController.setOnInvalidateListener(null);
            mScalableViewController = null;
        }
    }

    /**
     * View가 가지는 리소스를 해제한다.
     * 
     * @version 1.0
     */
    public void clear() {
        mStickerFrameViews.clear();
    }

    /**
     * U+Story<br>
     * 전체 프레임의 Bounds(순수 크기)를 반환한다.<br>
     * Rect(0, 0, width, height)로 반환
     * 
     * @return ArrayList 전체 프레임 Bounds
     */
    public ArrayList<Rect> getFrameBounds() {
        ArrayList<Rect> rects = new ArrayList<Rect>();
        for(StickerFrameView frameView : mStickerFrameViews) {
            rects.add(frameView.getFrameBounds());
        }
        return rects;
    }

    /**
     * U+Story<br>
     * 스티커에 설정된 rotation값을 가져온다
     * 
     * @param index 스티커 인덱스
     * @return float 스티커 rotation값
     */
    public float getFrameImageCurrentRotation(int index) {
        return mStickerFrameViews.get(index).getCurrentFrameRotate();
    }
    
    

    /**
     * U+Story<br>
     * 특정 스티커의 회전 값을 설정한다
     * 
     * @param index 스티커 인덱스
     * @param rotation 설정할 rotation값
     */
    public void setFrameImageBaseRotation(int index, float rotation) {
        mStickerFrameViews.get(index).rotateFrame(rotation);
    }

    /**
     * U+Story<br>
     * 특정 스티커의 translate 값 중 X값을 가져온다
     * 
     * @param index 스티커 인덱스
     * @return float translate의 x값
     */
    public float getFrameImageCurrentTranslateX(int index) {
        return mStickerFrameViews.get(index).getCurrentFrameTranslateX();
    }

    /**
     * U+Story<br>
     * 특정 스티커의 translate 값 중 Y값을 가져온다
     * 
     * @param index 스티커 인덱스
     * @return float translate의 y값
     */
    public float getFrameImageCurrentTranslateY(int index) {
        return mStickerFrameViews.get(index).getCurrentFrameTranslateY();
    }

    /**
     * U+Story<br>
     * 특정 스티커의 이동 값을 설정한다.
     * 
     * @param index 스티커 인덱스
     * @param x 이동 x값
     * @param y 이동 y값
     */
    public void setFrameImageBaseTranslate(int index, float x, float y) {
        mStickerFrameViews.get(index).translateFrame(x, y);
    }

    /**
     * U+Story<br>
     * 특정 스티커의 scale값을 가져온다
     * 
     * @param index 스티커 인덱스
     * @return float 스티커의 scale값
     */
    public float getFrameImageCurrentScale(int index) {
        return mStickerFrameViews.get(index).getCurrentFrameScale();
    }
    
    public int getStickerId(int index) {
        return mStickerFrameViews.get(index).getId();
    }
    
    public int getStickerIndex(int id) {
        for(ImageFrameView view : mStickerFrameViews) {
            if(view.getId() == id) {
                return mStickerFrameViews.indexOf(view);
            }
        }
        return -1;
    }

    /**
     * U+Story<br>
     * 특정 스티커의 크기 값을 설정한다.
     * 
     * @param index 스티커 인덱스
     * @param scale 설정할 scale값
     */
    public void setFrameImageBaseScale(int index, float scale) {
        mStickerFrameViews.get(index).scaleFrame(scale);
    }

    /**
     * {@link #OnStickerStatusChangedListener}<br>
     * 스티커 상태 변경을 감지하는 리스너를 등록한다.
     * 
     * @param listener 스티커 상태 변경 리스너
     * @version 1.0
     */

    public void setStickerStatusChangedListener(OnStickerStatusChangedListener listener) {
        mOnStickerStatusChangedListener = listener;
    }

    /**
     * 스티커의 상태가 변경되면 호출되는 class
     * 
     * @version 1.0
     */

    public interface OnStickerStatusChangedListener {
        /**
         * 스티커가 선택되거나 선택이 해제되면 호출된다.
         * 
         * @param selected 스티커 선택 여부
         * @param stickerType 스티커 종류
         * @see {@link StickerView#STICKER_TYPE_IMAGE}<br>
         *      {@link StickerView#STICKER_TYPE_TEXT}
         * @version 1.0
         */
        public void onStickerSelected(boolean selected, int stickerType, int index);

        public void onStickerRemoved(int index);

        /**
         * 텍스트 스티커 입력시 제한 길이를 넘어갈 경우 호출된다.
         * 
         * @version 1.0
         */
        public void onStickerTextLengthExceed();

        /**
         * 스티커의 위치/각도/크기가 변경되면 호출된다.
         * 
         * @version 2.0
         */
        public void onStickerMatrixChanged();
    }

    // [U+Camera>겔러리>편집>그리기]의 히스토리 기능에서 사용
    private ArrayList<StickerFrameView> queue = new ArrayList<StickerFrameView>();
    private ArrayList<Integer> actionQueue = new ArrayList<Integer>();
    private static final int HISTORY_ACTION_ADD = 0;
    private static final int HISTORY_ACTION_DELETE = 1;
    private static final int HISTORY_ACTION_MODIFY = 2;
    private int mCurrentIndex = -1;
    private boolean mUseHistory = false;

    /**
     * History 사용 여부 설정
     * 
     * @param useHistory History 사용 여부
     */
    public void setUseHistory(boolean useHistory) {
        mUseHistory = useHistory;
    }

    private void addHistory(StickerFrameView stickerFrameView, int action) {
        if(!mUseHistory) {
            return;
        }
        if(action == HISTORY_ACTION_MODIFY && mCurrentIndex != -1
                && queue.get(mCurrentIndex).equals(stickerFrameView)
                && actionQueue.get(mCurrentIndex) == HISTORY_ACTION_MODIFY
                && queue.get(mCurrentIndex).getEditProgress() == stickerFrameView.getEditProgress()) {
            queue.set(mCurrentIndex, stickerFrameView.copy());
        } else {
            for(int i = 0; i < queue.size();) {
                if(i <= mCurrentIndex) {
                    i++;
                } else {
                    queue.remove(i);
                    actionQueue.remove(i);
                }
            }
            queue.add(stickerFrameView.copy());
            actionQueue.add(action);
        }

        mCurrentIndex = queue.size() - 1;
    }

    /**
     * 미용 효과를 한단계 이전으로 되돌린다.
     * 
     * @version 2.0
     */
    public void undo() {
        if(canUndo()) {
            if(mCurrentIndex < 0) {
                mStickerFrameViews.clear();
            } else {
                int action = actionQueue.get(mCurrentIndex);
                StickerFrameView stickerFrameView = queue.get(mCurrentIndex);
                int index = mStickerFrameViews.indexOf(stickerFrameView);
                switch(action) {
                    case HISTORY_ACTION_ADD:
                        if(index != -1) {
                            mStickerFrameViews.remove(stickerFrameView);
                        }
                        break;
                    case HISTORY_ACTION_DELETE:
                        mStickerFrameViews.add(stickerFrameView.copy());
                        break;
                    case HISTORY_ACTION_MODIFY:
                        if(index != -1) {
                            for(int i = mCurrentIndex - 1; i >= 0; i--) {
                                if(queue.get(i).equals(stickerFrameView)) {
                                    mStickerFrameViews.set(index, queue.get(i).copy());
                                    break;
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            mCurrentIndex--;
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * {@link #undo()}로 되돌린 미용 효과를 다시 적용한다.
     * 
     * @version 2.0
     */
    public void redo() {
        if(canRedo()) {
            mCurrentIndex++;
            StickerFrameView stickerFrameView = queue.get(mCurrentIndex);
            int action = actionQueue.get(mCurrentIndex);
            int index = mStickerFrameViews.indexOf(stickerFrameView);
            switch(action) {
                case HISTORY_ACTION_ADD:
                    mStickerFrameViews.add(stickerFrameView.copy());
                    break;
                case HISTORY_ACTION_DELETE:
                    if(index != -1) {
                        mStickerFrameViews.remove(stickerFrameView);
                    }
                    break;
                case HISTORY_ACTION_MODIFY:
                    if(index != -1) {
                        mStickerFrameViews.set(index, stickerFrameView.copy());
                    }
                    break;
                default:
                    break;
            }
            mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
        }
    }

    /**
     * 되돌릴 수 있는 미용효과가 있는지 확인한다.
     * 
     * @return 되돌릴 수 있는 미용 효과가 있으면 true를 반환
     * @version 2.0
     */
    public boolean canUndo() {
        if(mCurrentIndex >= 0) {
            return true;
        }
        return false;
    }

    /**
     * 다시 적용할 수 있는 미용효과가 있는지 확인한다.
     * 
     * @return 다시 적용할 수 있는 미용 효과가 있으면 true를 반환
     * @version 2.0
     */
    public boolean canRedo() {
        if(queue.size() - 1 > mCurrentIndex) {
            return true;
        }
        return false;
    }

    // 선택/선택 해제/화면 업데이트 기능을 일시적으로 막는 용도
    private boolean mBlockHandlerSelect = false;
    private boolean mBlockHandlerDeselect = false;
    private boolean mBlockHandlerInvalidate = false;

    public void setBlockHandler(boolean select, boolean deselect, boolean invalidate) {
        mBlockHandlerSelect = select;
        mBlockHandlerDeselect = deselect;
        mBlockHandlerInvalidate = invalidate;
    }
}
