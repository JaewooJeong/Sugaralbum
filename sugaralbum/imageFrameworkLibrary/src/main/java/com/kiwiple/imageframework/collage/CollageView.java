
package com.kiwiple.imageframework.collage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewConfiguration;

import com.almeros.android.multitouch.gesturedetectors.MoveGestureDetector;
import com.almeros.android.multitouch.gesturedetectors.RotateGestureDetector;
import com.kiwiple.imageframework.Constants;
import com.kiwiple.imageframework.util.BitmapUtils;
import com.kiwiple.imageframework.util.FileUtils;
import com.kiwiple.imageframework.util.ImageDownloadManager;
import com.kiwiple.imageframework.util.ImageDownloadManager.ImageDownloadManagerListener;
import com.kiwiple.imageframework.util.ImageDownloadManager.ImageInfo;

/**
 * View를 상속 받은 클래스.<br>
 * 1~9 장의 이미지 파일을 합성한다. <br>
 * Gesture 이벤트를 이용하여 각 이미지를 Crop, Zoom In/Out, Rotate 시킬 수 있다.
 * 
 * @version 2.0
 */
public class CollageView extends View {
    private final static int HANDLER_MESSAGE_INVALIDATE = 0;
    private final static int HANDLER_MESSAGE_SELECT_FRAME = 1;
    private final static int HANDLER_MESSAGE_DESELECT_FRAME = 2;
    // private final static int HANDLER_MESSAGE_SHOW_GRID_GUIDE = 3;
    // private final static int HANDLER_MESSAGE_HIDE_GRID_GUIDE = 4;

    // template infos
    private DesignTemplate mDesignTemplate;
    // Bitmap scale시 깨지는 현상 보완
    private ArrayList<CollageFrameView> mFrameViews = new ArrayList<CollageFrameView>();

    // drawing infos
    private Paint mBoxPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG
            | Paint.FILTER_BITMAP_FLAG);
    private Paint mDragPaint = new Paint();
    private Paint mDragBoxPaint = new Paint();
    private Paint mDrawBitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private Paint mBackgroundPatternPaint;

    private Bitmap mBackground;
    private Matrix mBackgroundMatrix = new Matrix();
    private Bitmap mUserBackground;
    private Matrix mUserBackgroundMatrix = new Matrix();

    private boolean mLayoutInitialize = false;

    private GestureDetector mGestureDetector;
    private ScaleGestureDetector mScaleDetector;
    private RotateGestureDetector mRotateDetector;
    private MoveGestureDetector mMoveDetector;
    private boolean mDragAndDropEnabled = true;

    /**
     * 편집중인 프레임
     */
    private CollageFrameView mLastFrameView;
    /**
     * 선택된 프레임
     */
    private CollageFrameView mCurrentFrameView;
    private CollageFrameView mDrapDestination = null;
    // for bounce animation
    private Timer mMoveTimer;

    private float mFillImageMinScale = 1.f;
    private float mFillImageMaxScale = 2.5f;

    private boolean mForceInvalidate;

    private OnFrameStatusChangedListener mOnFrameStatusChangedListener;

    private int mBackgroundColor = Color.WHITE;
    private int mWidth;
    private int mHeight;

    private boolean mIsDisableLongPress = false;
    // grid guide
    // 회전시 격자 모양의 가이드 라인을 표시 해주기 위한 코드로 구현 완료하였으나 사용하지 않아 주석처리.
    // private boolean mGridGuideVisible;
    // private Paint mRotateDegreePaint = new Paint();
    // private Paint mRotateDegreeStrokePaint = new Paint();
    // private Bitmap mGridBitmap;

    protected ImageDownloadManager mImageDownloadManager;
    
    private boolean mSingleFullFrame = false;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case HANDLER_MESSAGE_INVALIDATE:
                    if(msg.obj != null) {
                        invalidate(((CollageFrameView)msg.obj).getFrameRect());
                    } else {
                        mHandler.removeMessages(HANDLER_MESSAGE_INVALIDATE);
                        invalidate();
                    }
                    break;
                case HANDLER_MESSAGE_SELECT_FRAME:
                    selectFrame(msg.arg1);
                    break;
                case HANDLER_MESSAGE_DESELECT_FRAME:
                    deselectFrame();
                    break;
            // case HANDLER_MESSAGE_SHOW_GRID_GUIDE:
            // mGridGuideVisible = true;
            // if (msg.obj != null) {
            // invalidate(((CollageFrameView)msg.obj).getFrameRect());
            // } else {
            // mHandler.removeMessages(HANDLER_MESSAGE_INVALIDATE);
            // invalidate();
            // }
            // break;
            // case HANDLER_MESSAGE_HIDE_GRID_GUIDE:
            // mGridGuideVisible = false;
            // if (msg.obj != null) {
            // invalidate(((CollageFrameView)msg.obj).getFrameRect());
            // } else {
            // mHandler.removeMessages(HANDLER_MESSAGE_INVALIDATE);
            // invalidate();
            // }
            // break;
            }
        }
    };

    public CollageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    public CollageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CollageView(Context context) {
        super(context);
        init(context);
    }

    /**
     * U+Story<br>
     * 콜라주된 이미지를 스레드에서 바로 꺼내기 위한 생성자. <br>
     * 설정할 템플릿과 콜라주 이미지 크기를 입력하여 콜라주 뷰를 생성한다.
     * 
     * @param context Context
     * @param templateInfo 템플릿 정보
     * @param width 콜라주 이미지 가로 길이
     * @param height 콜라주 이미지 세로 길이
     */
    public CollageView(Context context, TemplateInfo templateInfo, int width, int height) {
        super(context);
        init(context);
        try {
            setTemplateInfo(templateInfo);
        } catch(IOException e) {
            e.printStackTrace();
        }
        onLayout(true, 0, 0, width, height);
    }

    private void init(Context context) {
        // 하드웨어 가속에서는 미지원 또는 API level 제한이 있어 software rendering을 하도록 고정.
        // 참고: http://developer.android.com/guide/topics/graphics/hardware-accel.html
        if(Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_SOFTWARE, new Paint(Paint.FILTER_BITMAP_FLAG));
        }

        // Setup Gesture Detectors
        mGestureDetector = new GestureDetector(context.getApplicationContext(),
                                               new SimpleGestureListener());
        mScaleDetector = new ScaleGestureDetector(context.getApplicationContext(),
                                                  new ScaleListener());
        mRotateDetector = new RotateGestureDetector(context.getApplicationContext(),
                                                    new RotateListener());
        mMoveDetector = new MoveGestureDetector(context.getApplicationContext(), new MoveListener());

        // Setup Paint
        mBoxPaint.setColor(Color.argb(0xb3, 0x1a, 0x5b, 0xbd));
        mBoxPaint.setStyle(Paint.Style.STROKE);
        mDragBoxPaint.setColor(Color.argb(0xb3, 0xff, 0x89, 0x00));
        mDragBoxPaint.setStyle(Paint.Style.STROKE);
        mDragPaint.setAlpha(0x7f);

        // mGridBitmap = BitmapFactory.decodeResource(getResources(),
        // getResources().getIdentifier("img_photo_grid", "drawable",
        // getContext().getApplicationContext().getPackageName()));

        // mRotateDegreePaint.setStyle(Style.FILL);
        // mRotateDegreePaint.setTextSize(16 * getResources().getDisplayMetrics().density);
        // mRotateDegreePaint.setTextAlign(Paint.Align.CENTER);
        // mRotateDegreePaint.setTypeface(Typeface.DEFAULT_BOLD);
        // mRotateDegreePaint.setColor(Color.argb(0xb3, 0xff, 0xff, 0xff));
        //
        // mRotateDegreeStrokePaint.setStyle(Style.STROKE);
        // mRotateDegreeStrokePaint.setTextSize(16 * getResources().getDisplayMetrics().density);
        // mRotateDegreeStrokePaint.setTextAlign(Paint.Align.CENTER);
        // mRotateDegreeStrokePaint.setTypeface(Typeface.DEFAULT_BOLD);
        // mRotateDegreeStrokePaint.setStrokeWidth(2);
        // mRotateDegreeStrokePaint.setColor(Color.argb(0x6f, 0x00, 0x00, 0x00));

        mImageDownloadManager = ImageDownloadManager.getInstance(context,
                                                                 context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                                                        .getPath());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 콜라주 가로/세로 비율에 맞춰서 뷰 크기를 재조정한다.
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if(mDesignTemplate != null) {
            // height 조절
            if(width < (height / mDesignTemplate.mAspectRatio)) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int)Math.round(width
                        * mDesignTemplate.mAspectRatio), MeasureSpec.EXACTLY);
            }
            // width 조절
            else {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int)Math.round(height
                        / mDesignTemplate.mAspectRatio), MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            }
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
        // 콜라주 json에서 정의된 콜라주의 크기 대비 뷰의 크기에 대한 비율을 반영한다.
        if(!mLayoutInitialize || changed) {
            mLayoutInitialize = true;

            float widthScaleFactor = (right - left) / (float)mDesignTemplate.mWidth;
            float heightScaleFactor = (bottom - top) / (float)mDesignTemplate.mHeight;
            mDesignTemplate.mLayoutWidthScaleFactor = widthScaleFactor;
            mDesignTemplate.mLayoutHeightScaleFactor = heightScaleFactor;
            widthScaleFactor = (right - left) / DesignTemplate.DEFAULT_OUTLINE_BASELINE;
            heightScaleFactor = (bottom - top) / DesignTemplate.DEFAULT_OUTLINE_BASELINE;
            
            // 콜라주 테두리 두께에 비율 반영
            float outlineScale = (widthScaleFactor > heightScaleFactor ? widthScaleFactor
                    : heightScaleFactor) / mDesignTemplate.mLayoutWidthScaleFactor < mDesignTemplate.mLayoutHeightScaleFactor ? mDesignTemplate.mLayoutWidthScaleFactor
                    : mDesignTemplate.mLayoutHeightScaleFactor;
            mDesignTemplate.mOutlineWidth = DesignTemplate.DEFAULT_OUTLINE_WIDTH * outlineScale;

            // 콜라주 프레임 테두리 두께에 비율 반영
            for(CollageFrameInfo info : mDesignTemplate.mFrameInfos) {
                info.mBorderWidth = info.mInitialBorderWidth * outlineScale;
            }

            // 배경 이미지에 비율 반영
            mBackgroundMatrix.reset();
            mBackgroundMatrix.preScale(mDesignTemplate.mLayoutWidthScaleFactor,
                                       mDesignTemplate.mLayoutHeightScaleFactor);

            if(mFrameViews.size() == 1) {
                mFrameViews.get(0).setSingleFullFrame(mSingleFullFrame);
            }
            // 프레임 초기화
            for(CollageFrameView view : mFrameViews) {
                view.initFrame(right - left, bottom - top);
            }

            // Setup Paint
            mBoxPaint.setStrokeWidth(mDesignTemplate.mOutlineWidth * 2);
            mDragBoxPaint.setStrokeWidth(mDesignTemplate.mOutlineWidth * 2);

            if(mUserBackground != null) {
                float scale;
                float dx = 0, dy = 0;

                // center crop
                if(mUserBackground.getWidth() * (getBottom() - getTop()) > (getRight() - getLeft())
                        * mUserBackground.getHeight()) {
                    scale = (float)(getBottom() - getTop()) / (float)mUserBackground.getHeight();
                    dx = ((getRight() - getLeft()) - mUserBackground.getWidth() * scale) * 0.5f;
                } else {
                    scale = (float)(getRight() - getLeft()) / (float)mUserBackground.getWidth();
                    dy = ((getBottom() - getTop()) - mUserBackground.getHeight() * scale) * 0.5f;
                }

                mUserBackgroundMatrix.setScale(scale, scale);
                mUserBackgroundMatrix.postTranslate((int)(dx + 0.5f), (int)(dy + 0.5f));
            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        CollageFrameImageManager.getInstance().release();
    }

    private boolean mMoveEnable = true;
    private CollageFrameView mTouchDownFrameView = null;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!isEnabled()) {
            return false;
        }
        mForceInvalidate = false;
        // drag 및 bounce animation 구현
        switch(event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                // change order ->lock:do not remove
                // mFrameInfos.remove(mLastFrameInfo);
                // mFrameInfos.add(mLastFrameInfo);
                mTouchDownFrameView = null;
                mTouchDownFrameView = findInnterPoint(event.getX(), event.getY());
                if(mTouchDownFrameView != null && mTouchDownFrameView != mCurrentFrameView) {
                    mTouchDownFrameView.setTouchDown(true);
                } else {
                    mTouchDownFrameView = null;
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // drag and drop mode
                if(mLastFrameView != null && mLastFrameView.isDragAndDropSource()) {
                    for(CollageFrameView view : mFrameViews) {
                        if(view != mLastFrameView && view.isInnterPoint(event.getX(), event.getY()) 
                                && !view.getIsVideoType()) { // 비디오인 경우 드래그앤 드랍 안되도록
                            mDrapDestination = view;
                            view.setDropDestination(true);
                        } else {
                            view.setDropDestination(false);
                        }
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                // drag and drop mode
                if(mLastFrameView != null && mLastFrameView.isDragAndDropSource()) {
                    mForceInvalidate = true;
                    mLastFrameView.endDragAndDrop();
                    if(mDrapDestination != null && mDrapDestination.isDropDestination()
                            && mLastFrameView != mDrapDestination) {
                        Bitmap temp = mDrapDestination.getImage();
                        int baseRotation = mDrapDestination.getBaseImageRotation();
                        mDrapDestination.setBaseImageRotation(mLastFrameView.getBaseImageRotation());
                        mDrapDestination.setImage(mLastFrameView.getImage());
                        mLastFrameView.setBaseImageRotation(baseRotation);
                        mLastFrameView.setImage(temp);
                        if(mCurrentFrameView != null) {
                            OnFrameStatusChangedListener listener = mOnFrameStatusChangedListener;
                            mOnFrameStatusChangedListener = null;
                            selectFrame(mFrameViews.indexOf(mDrapDestination));
                            mOnFrameStatusChangedListener = listener;
                        }
                        if(mOnFrameStatusChangedListener != null) {
                            mOnFrameStatusChangedListener.onFrameDragAndDrop(mFrameViews.indexOf(mLastFrameView),
                                                                             mFrameViews.indexOf(mDrapDestination));
                        }
                    }
                }
            case MotionEvent.ACTION_CANCEL:
                if(mLastFrameView != null) {
                    // 이미지가 프레임 영역을 벗어나지 않도록 조정해 준다.
                    if(mMoveTimer == null) {
                        mMoveTimer = new Timer();
                        TimerTask task = new TimerTask() {
                            @Override
                            public void run() {
                                if(mLastFrameView != null
                                        && mLastFrameView.adjustBoundImagePosition(false)) {
                                    Message invalidate = new Message();
                                    invalidate.what = HANDLER_MESSAGE_INVALIDATE;
                                    invalidate.obj = mLastFrameView;
                                    mHandler.sendMessage(invalidate);
                                } else {
                                    if(mMoveTimer != null) {
                                        mMoveTimer.cancel();
                                        mMoveTimer = null;
                                    }
                                }
                            }
                        };
                        mMoveTimer.schedule(task, 10, 10);
                    }
                    if(mLastFrameView.isDragAndDropSource()) {
                        mLastFrameView.endDragAndDrop();
                    }
                }
                if(mDrapDestination != null) {
                    mDrapDestination.setDropDestination(false);
                    mDrapDestination = null;
                }
                mMoveEnable = true;

                if(mTouchDownFrameView != null) {
                    mTouchDownFrameView.setTouchDown(false);
                }
                // hide grid guide
                // mHandler.removeMessages(HANDLER_MESSAGE_SHOW_GRID_GUIDE);
                // if (mGridGuideVisible) {
                // Message hideGrid = new Message();
                // hideGrid.what = HANDLER_MESSAGE_HIDE_GRID_GUIDE;
                // hideGrid.obj = mLastFrameView;
                // mHandler.sendMessageDelayed(hideGrid, ViewConfiguration.getDoubleTapTimeout());
                // }
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                // 멀티 터치일 때는 scale과 rotate 동작이므로 move 동작은 발생하지 않도록 해준다.
                mMoveEnable = false;
                break;
        }
        try {
            mGestureDetector.onTouchEvent(event);
            mRotateDetector.onTouchEvent(event);
            mScaleDetector.onTouchEvent(event);
            mMoveDetector.onTouchEvent(event);
        } catch(Exception e) {
            // do nothing
        }
        // 편집중인 프레임이 있으면 화면을 갱신해 준다.
        if(mLastFrameView != null) {
            if(mLastFrameView.isDragAndDropSource() || mForceInvalidate) {
                // 콜라주 전체 갱신
                mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
            } else {
                // 편집중인 프레임만 갱신.
                Message invalidate = new Message();
                invalidate.what = HANDLER_MESSAGE_INVALIDATE;
                invalidate.obj = mLastFrameView;
                mHandler.sendMessage(invalidate);
            }
        }
        if(mTouchDownFrameView != null && mTouchDownFrameView != mCurrentFrameView) {
            Message invalidate = new Message();
            invalidate.what = HANDLER_MESSAGE_INVALIDATE;
            invalidate.obj = mTouchDownFrameView;
            mHandler.sendMessage(invalidate);
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            // pinch zoom/in out gesture에 따라 이미지 확대/축소
            if(mLastFrameView != null && mLastFrameView.hasImage()
                    && !mLastFrameView.isDragAndDropSource()) {
                mLastFrameView.scaleImage(detector.getScaleFactor());
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
            // multi touch로 이미지 회전.
            if(mLastFrameView != null && mLastFrameView.hasImage()
                    && !mLastFrameView.isDragAndDropSource()) {
                mLastFrameView.rotateImage((int)detector.getRotationDegreesDelta());
            }
            return true;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            if(mMoveEnable) {
                // drag로 이미지 이동
                PointF d = detector.getFocusDelta();
                if(mLastFrameView != null && mLastFrameView.hasImage()) {
                    if(!mLastFrameView.isDragAndDropSource() && !mLastFrameView.getIsVideoType()) {
                        mLastFrameView.translateImage(d.x, d.y);
                    } else {
                        if (!mLastFrameView.getIsVideoType()) {
                            mLastFrameView.updateDragAndDrop(d.x, d.y);    
                        }
                    }
                }
            }
            return true;
        }
    }

    private class SimpleGestureListener extends GestureDetector.SimpleOnGestureListener {
        private Message mSelectFrameMessage;
        private CollageFrameView mFrameView;

        @Override
        public void onLongPress(MotionEvent e) {
            // drag&drop 동작
            if(mLastFrameView != null && mLastFrameView.hasImage() && mDragAndDropEnabled) {
            	if(!mIsDisableLongPress && !mLastFrameView.getIsVideoType())  
            		mLastFrameView.startDragAndDrop(true, e.getX(), e.getY());
            }
            invalidate();
        }

        @Override
        public boolean onDown(MotionEvent e) {
            // 위치 조정 도중 터치 이벤트 발생할 경우. 애니메이션 없이 위치 조정
            if(mMoveTimer != null && mLastFrameView != null
                    && mLastFrameView.adjustBoundImagePosition(true)) {
                Message invalidate = new Message();
                invalidate.what = HANDLER_MESSAGE_INVALIDATE;
                invalidate.obj = mLastFrameView;
                mHandler.sendMessage(invalidate);
            }
            mLastFrameView = findInnterPoint(e.getX(), e.getY());

            // mHandler.removeMessages(HANDLER_MESSAGE_HIDE_GRID_GUIDE);
            // if (mLastFrameView != null && !mGridGuideVisible) {
            // // show grid guide
            // Message showGrid = new Message();
            // showGrid.what = HANDLER_MESSAGE_SHOW_GRID_GUIDE;
            // showGrid.obj = mLastFrameView;
            // mHandler.sendMessageDelayed(showGrid, ViewConfiguration.getTapTimeout());
            // }
            return true;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            // 프레임 선택
            mFrameView = findInnterPoint(e.getX(), e.getY());
            if(mFrameView != null) {
                // U+Camera 이슈: 선택된 프레임을 다시 선택해도 포커스 유지하도록 요청. 2014년 9월 고도화
                // if(mCurrentFrameView != mFrameView) {
                mSelectFrameMessage = new Message();
                mSelectFrameMessage.what = HANDLER_MESSAGE_SELECT_FRAME;
                mSelectFrameMessage.arg1 = mFrameViews.indexOf(mFrameView);
                mHandler.sendMessageDelayed(mSelectFrameMessage,
                                            ViewConfiguration.getDoubleTapTimeout() - 150);
                // }
                // else {
                // mSelectFrameMessage = new Message();
                // mSelectFrameMessage.what = HANDLER_MESSAGE_DESELECT_FRAME;
                // mHandler.sendMessageDelayed(mSelectFrameMessage,
                // ViewConfiguration.getDoubleTapTimeout() - 150);
                // }
            }
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            try {
                // 프레임의 이미지를 초기화 한다.
                findInnterPoint(e.getX(), e.getY()).initImage();
                /**
                 * U+Story<br>
                 * 프레임 더블 탭시 초기화하기 위하여 추가
                 */
                findInnterPoint(e.getX(), e.getY()).setInitailizeScaleImage();
            } catch(NullPointerException exception) {
                // point is not inside of any frames
            }
            mHandler.removeMessages(HANDLER_MESSAGE_SELECT_FRAME);
            mHandler.removeMessages(HANDLER_MESSAGE_DESELECT_FRAME);
            return true;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        onAlternativeDraw(canvas, false);
    }

    /**
     * 합성된 이미지를 canvas에 그려준다.
     * 
     * @param canvas 합성된 이미지를 그릴 canvas
     * @param isOutput 최종 output일 경우 true로 설장한다.(false로 설정하면 사용자 가이드 요소도 포함된다.)
     * @version 2.0
     */
    public void onAlternativeDraw(Canvas canvas, boolean isOutput) {
        if(mDesignTemplate == null) {
            return;
        }
        if(mUserBackground != null) {
            // 사용자가 설정한 배경 이미지
            canvas.drawBitmap(mUserBackground, mUserBackgroundMatrix, null);
        } else if(mBackground != null) {
            // json에 정의 된 배경 이미지
            canvas.drawBitmap(mBackground, mBackgroundMatrix, null);
        } else {
            // json에 정의 된 배경 색
            canvas.drawColor(mBackgroundColor);
            if(mBackgroundPatternPaint != null) {
                canvas.drawPaint(mBackgroundPatternPaint);
            }
        }
        // 다각형 콜라주의 경우 프레임 크기 조절 시 콜라주 전체 크기도 같이 줄여준다.
        if(mDesignTemplate.mTemplateType == 2) {
            canvas.save();
            canvas.scale(1.f
                                 - (mFrameViews.get(0).getFrameBorderWidth()
                                         * mDesignTemplate.mLayoutWidthScaleFactor * 2) / mWidth,
                         1.f
                                 - (mFrameViews.get(0).getFrameBorderWidth()
                                         * mDesignTemplate.mLayoutHeightScaleFactor * 2) / mHeight,
                         mWidth / 2,
                         mHeight / 2);
        }
        for(CollageFrameView view : mFrameViews) {
            view.drawBackground(canvas, isOutput);
            view.drawImage(canvas, mDrawBitmapPaint, isOutput);
            view.drawFrame(canvas, isOutput);
        }
        for(CollageFrameView view : mFrameViews) {
            if(!isOutput) {
                if(view.isSelected()
                        && !(mLastFrameView != null && mLastFrameView.isDragAndDropSource())) {
                    // U+Camera에서 콜라주 선택 박스 제거
                    // view.drawSelection(canvas, mBoxPaint);
                } else if(view.isDropDestination()) {
                    // drag&drop destination에 선택 박스
                    view.drawSelection(canvas, mDragBoxPaint);
                }
                // drag&drop source의 반투명 이미지
                view.drawDragShodow(canvas, mDragPaint);
            }
        }
        if(mDesignTemplate.mTemplateType == 2) {
            canvas.restore();
        }
    }

    /**
     * x, y좌표에 있는 프레임을 반환한다.
     */
    private CollageFrameView findInnterPoint(float x, float y) {
        for(int i = mFrameViews.size() - 1; i >= 0; i--) {
            if(mFrameViews.get(i).isInnterPoint(x, y)) {
                return mFrameViews.get(i);
            }
        }
        return null;
    }

    /**
     * 뷰에 적용할 템플릿을 설정한다.
     * 
     * @param template 템플릿 정보
     * @version 1.0
     */
    public void setTemplateInfo(final TemplateInfo template) throws IOException {
        // 프레임에 설정된 이미지와 회전정보를 임시 저장해 둔다.
        SparseArray<Bitmap> frameImages = null;
        if(mFrameViews != null && mFrameViews.size() != 0) {
            frameImages = getFrameImages();
        }
        SparseArray<Integer> frameBaseRotation = new SparseArray<Integer>();
        int i = 0;
        for(CollageFrameView frameView : mFrameViews) {
            frameBaseRotation.put(i++, frameView.getBaseImageRotation());
        }
        
        clear();
        
        if(template == null) {
            throw new IllegalStateException("Design template is null");
        }
        mDesignTemplate = DesignTemplateManager.getInstance(getContext().getApplicationContext())
                                               .getDesignTemplate(template.getId());
        if(mDesignTemplate == null || !mDesignTemplate.isValid()) {
            throw new IllegalStateException("Desigsn template is not valid");
        }

        mBackgroundColor = mDesignTemplate.mBackgroundColor;
        for(i = 0; i < mDesignTemplate.mFrameInfos.size(); i++) {
            mFrameViews.add(new CollageFrameView(getContext().getApplicationContext(),
                                                 mDesignTemplate.mFrameInfos.get(i),
                                                 mDesignTemplate, i));
        }
        // 임시 저장한 이미지와 회전정보를 설정한다.
        if(frameImages != null) {
            setFrameImages(frameImages, frameBaseRotation);
        }
        for(CollageFrameView frameView : mFrameViews) {
            frameView.setImageScale(mFillImageMinScale, mFillImageMaxScale);
            if(mUserBackground != null || mBackgroundPatternPaint != null) {
                frameView.setFrameColor(Color.TRANSPARENT);
            }
        }

        // 배경 이미지 scheme에 따라 불러와서 설정한다.
        if(!TextUtils.isEmpty(mDesignTemplate.mBackgroundImageUrl)) {
            // network image
            if(FileUtils.isNetworkUrl(mDesignTemplate.mBackgroundImageUrl)) {
                ImageInfo info = new ImageInfo();
                info.caching = false;
                info.imageURL = mDesignTemplate.mBackgroundImageUrl;
                info.persistance = true;
                info.targetBitmapConfig = Config.ARGB_8888;
                info.listener = new ImageDownloadManagerListener() {
                    @Override
                    public void progressDownload(int progress) {
                    }

                    @Override
                    public void onImageDownloadComplete(String state, ImageInfo info) {
                        mBackground = info.bitmap;
                        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
                    }
                };
                mImageDownloadManager.ReqDownload(info);
            } else {
                // asset image
                new Thread() {
                    @Override
                    public void run() {
                        InputStream backgroundImageInputStream = null;
                        try {
                            if(mDesignTemplate.mIsThemeTemplate) {
                                backgroundImageInputStream = new FileInputStream(
                                                                                 DesignTemplateManager.getInstance(getContext().getApplicationContext())
                                                                                                      .getThemeBasePath(mDesignTemplate.mTheme) + File.separator
                                                                                         + mDesignTemplate.mBackgroundImageUrl);
                            } else {
                                backgroundImageInputStream = getContext().getApplicationContext()
                                                                         .getResources()
                                                                         .getAssets()
                                                                         .open(DesignTemplateManager.getInstance(getContext().getApplicationContext())
                                                                                                    .getAssetBasePath()
                                                                                       + mDesignTemplate.mBackgroundImageUrl);
                            }
                            mBackground = BitmapFactory.decodeStream(backgroundImageInputStream);
                        } catch(IOException e) {
                        } finally {
                            if(backgroundImageInputStream != null) {
                                try {
                                    backgroundImageInputStream.close();
                                } catch(IOException e) {
                                }
                            }
                        }
                        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
                    }
                }.start();
            }
        }

        // 콜라주를 레이아웃에 맞춰주기 위해 다시 초기화 해 준다.
        mLayoutInitialize = false;
        requestLayout();
    }

    /**
     * 사용자 지정 콜라주 템플릿 배경 이미지를 설정한다.
     * 
     * @param background 콜라주 템플릿 배경 이미지
     * @version 2.0
     */
    public void setTemplateBackgroundImage(Bitmap background) {
        mUserBackground = background;
        if(mUserBackground != null) {
            float scale;
            float dx = 0, dy = 0;

            // center crop
            if(mUserBackground.getWidth() * (getBottom() - getTop()) > (getRight() - getLeft())
                    * mUserBackground.getHeight()) {
                scale = (float)(getBottom() - getTop()) / (float)mUserBackground.getHeight();
                dx = ((getRight() - getLeft()) - mUserBackground.getWidth() * scale) * 0.5f;
            } else {
                scale = (float)(getRight() - getLeft()) / (float)mUserBackground.getWidth();
                dy = ((getBottom() - getTop()) - mUserBackground.getHeight() * scale) * 0.5f;
            }

            mUserBackgroundMatrix.setScale(scale, scale);
            mUserBackgroundMatrix.postTranslate((int)(dx + 0.5f), (int)(dy + 0.5f));
        }
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 템플릿 background color를 설정한다. <br>
     * Note that the color is an int containing alpha as well as r,g,b. This 32bit value is not
     * premultiplied, meaning that its alpha can be any value, regardless of the values of r,g,b.
     * See the Color class for more details.
     * 
     * @param color 템플릿 background color
     * @version 1.0
     */
    public void setTemplateBackgroundColor(int color) {
        mBackgroundColor = color;
        Message invalidate = new Message();
        invalidate.what = HANDLER_MESSAGE_INVALIDATE;
        mHandler.sendMessage(invalidate);
    }

    /**
     * U+Story<br>
     * 사용자가 설정한 콜라주 배경 컬러를 얻기 위한 메소드
     * 
     * @return int 설정된 color값
     */
    public int getTemplateBackgroundColor() {
        return mDesignTemplate.mBackgroundColor;
    }

    /**
     * 템플릿 background 패턴을 설정한다.
     * 
     * @param shader shader 패턴
     * @version 1.0
     */
    public void setTemplateBackgroundPattern(Shader shader) {
        if(shader != null) {
            if(mBackgroundPatternPaint == null) {
                mBackgroundPatternPaint = new Paint();
            }
            mBackgroundPatternPaint.setShader(shader);
        } else {
            mBackgroundPatternPaint = null;
        }
        Message invalidate = new Message();
        invalidate.what = HANDLER_MESSAGE_INVALIDATE;
        mHandler.sendMessage(invalidate);
    }

    /**
     * 콜라주 프레임의 테두리 색상을 설정한다.
     * 
     * @param index 프레임 index
     * @param color 테두리 색상
     * @version 2.0
     */
    public void setFrameBorderColor(int index, int color) {
        if(index < 0 || index > mFrameViews.size() - 1) {
            return;
        }
        mFrameViews.get(index).setFrameColor(color);
        Message invalidate = new Message();
        invalidate.what = HANDLER_MESSAGE_INVALIDATE;
        invalidate.obj = mFrameViews.get(index);
        mHandler.sendMessage(invalidate);
    }

    /**
     * 콜라주 프레임의 테두리 두께를 설정한다.
     * 
     * @param width 테두리 두께
     * @version 2.0
     */
    public void setFrameBorderWidth(float width) {
        for(int i = 0; i < mFrameViews.size(); i++) {
            mFrameViews.get(i).setFrameBorderWidth(width);
        }
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * U+Story<br>
     * 사용자가 설정한 콜라주의 여백 값을 얻기 위한 메소드
     * 
     * @return float 설정된 여백값
     */
    public float getFrameBorderWidth() {
        if(mFrameViews != null && !mFrameViews.isEmpty()) {
            return mFrameViews.get(0).getFrameBorderWidth();
        }
        return -1;
    }

    /**
     * 프레임의 크기를 변경한다. 
     */
    public void scaleFrame(float scale) {
        for(int i = 0; i < mFrameViews.size(); i++) {
            mFrameViews.get(i).scaleFrame(mFrameViews.get(i).getDefaultFrameScale()
                                                  / mFrameViews.get(i).getCurrentFrameScale()
                                                  * scale);
        }
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 콜라주 프레임 모서리의 라운딩 처리를 수행한다.
     * 
     * @param radius 원형의 크기
     * @version 2.0
     */
    public void setFrameCornerRadius(float radius) {
        for(int i = 0; i < mFrameViews.size(); i++) {
            mFrameViews.get(i).setFrameCornerRadius(radius);
        }
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * U+Story<br>
     * 콜라주 프레임의 round 값을 가져온다
     * 
     * @return float 설정된 round값
     */
    public float getFrameCornerRadius() {
        if(mFrameViews != null && !mFrameViews.isEmpty()) {
            return mFrameViews.get(0).getFrameCornerRadius();
        }
        return 0.f;
    }

    /**
     * 프레임들에 이미지를 설정한다.
     * 
     * @param images 이미지 목록
     * @version 1.0
     */
    public void setFrameImages(SparseArray<Bitmap> images) {
        int index;
        for(int i = 0; i < images.size(); i++) {
            if(i > mFrameViews.size() - 1) {
                break;
            }
            index = images.keyAt(i);
            mFrameViews.get(index).setImage(images.get(index));
        }
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 콜라주 프레임에 이미지와 기본 orientation값을 설정한다.
     * 
     * @param images 이미지 목록
     * @param frameBaseRotation 기본 orientation 목록
     * @version 2.0
     */
    public void setFrameImages(SparseArray<Bitmap> images, SparseArray<Integer> frameBaseRotation) {
        int index;
        for(int i = 0; i < images.size(); i++) {
            if(i > mFrameViews.size() - 1) {
                break;
            }
            index = images.keyAt(i);
            mFrameViews.get(index).setImage(images.get(index));
        }
        for(int i = 0; i < frameBaseRotation.size(); i++) {
            if(i > mFrameViews.size() - 1) {
                break;
            }
            index = frameBaseRotation.keyAt(i);
            mFrameViews.get(index).setBaseImageRotation(frameBaseRotation.get(index));
        }
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 현재 선택된 프레임에 적용될 이미지를 설정한다.
     * 
     * @param image 이미지
     * @version 1.0
     */
    public void setFrameImage(Bitmap image) {
        if(mCurrentFrameView != null) {
            mCurrentFrameView.setImage(image);
            Message invalidate = new Message();
            invalidate.what = HANDLER_MESSAGE_INVALIDATE;
            invalidate.obj = mCurrentFrameView;
            mHandler.sendMessage(invalidate);
        }
    }

    /**
     * 현재 선택된 프레임에 적용될 이미지를 설정한다. <br>
     * {@link #setFrameImage}와는 다르게 transformation 정보는 유지된다.
     * 
     * @param image 이미지
     * @version 1.0
     */
    public void changeFrameImage(Bitmap image) {
        if(mCurrentFrameView != null) {
            mCurrentFrameView.changeImage(image);
            Message invalidate = new Message();
            invalidate.what = HANDLER_MESSAGE_INVALIDATE;
            invalidate.obj = mCurrentFrameView;
            mHandler.sendMessage(invalidate);
        }
    }

    /**
     * 프레임에 적용될 이미지를 설정한다. <br>
     * {@link #setFrameImage}와는 다르게 transformation 정보는 유지된다.
     * 
     * @param 프레임 index
     * @param image 이미지
     * @version 1.0
     */
    public void changeFrameImage(Bitmap image, int index) {
        if(index < 0 || index > mFrameViews.size() - 1) {
            return;
        }
        mFrameViews.get(index).changeImage(image);
        Message invalidate = new Message();
        invalidate.what = HANDLER_MESSAGE_INVALIDATE;
        invalidate.obj = mFrameViews.get(index);
        mHandler.sendMessage(invalidate);
    }

    /**
     * 프레임에 적용될 이미지를 설정한다. <br>
     * {@link #setFrameImage}와는 다르게 transformation 정보는 유지된다.
     * 
     * @param images 이미지 목록
     * @version 1.0
     */
    public void changeFrameImages(ArrayList<Bitmap> images) {
        for(int i = 0; i < images.size(); i++) {
            if(i > mFrameViews.size() - 1) {
                break;
            }
            mFrameViews.get(i).changeImage(images.get(i));
        }
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 콜라주 프레임에 이미지를 설정한다.
     * 
     * @param index 프레임 index
     * @param image 이미지
     * @version 2.0
     */
    public void setFrameImage(int index, Bitmap image) {
        mFrameViews.get(index).setImage(image);
        Message invalidate = new Message();
        invalidate.what = HANDLER_MESSAGE_INVALIDATE;
        invalidate.obj = mCurrentFrameView;
        mHandler.sendMessage(invalidate);
    }

    /**
     * U+Story<br>
     * 특정 프레임을 초기화시키기 위한 메소드 추가
     * 
     * @param index 프레임 인덱스
     */
    public void setInitImage(int index) {
        mFrameViews.get(index).initImage();
    }

    /**
     * U+Story<br>
     * 초기 값 설정하기위하여 내용 변경 <br>
     * 콜라주 프레임 이미지의 기본 회전각도를 설정한다.
     * 
     * @param index 프레임 index
     * @param rotation 기본 orientation
     * @version 2.0
     */
    public void setFrameImageDefaultRotation(int index, int rotation) {
        mFrameViews.get(index).setBaseImageRotation(rotation);
        // mFrameViews.get(index).rotateImage(rotation);
    }

    /**
     * U+Story<br>
     * 프레임의 초기 rotation을 가져온다.
     * 
     * @param index 프레임 인덱스
     * @return int 설정된 rotation값
     */
    public int getFrameImageDefaultRotation(int index) {
        return mFrameViews.get(index).getBaseImageRotation();
    }

    /**
     * U+Story<br>
     * 특정 프레임의 초기 rotation값을 설정한다.
     * 
     * @param index 프레임 인덱스
     * @param rotation 설정할 rotation값
     */
    public void setFrameImageBaseRotation(int index, int rotation) {
        mFrameViews.get(index).rotateImage(rotation);
    }

    /**
     * U+Story<br>
     * 특정 프레임의 초기 scale값을 설정한다.
     * 
     * @param index 프레임 인덱스
     * @param scale 설정할 scale값
     */
    public void setFrameImageBaseScale(int index, float scale) {
        mFrameViews.get(index).scaleImage(scale);
    }

    /**
     * U+Story<br>
     * 특정 프레임의 초기 translate값을 설정한다.
     * 
     * @param index 프레임 인덱스
     * @param x 프레임의 x축 이동 값
     * @param y 프레임의 y축 이동 값
     */
    public void setFrameImageBaseTranslate(int index, float x, float y) {
        mFrameViews.get(index).translateImage(x, y);
    }

    /**
     * U+Story<br>
     * 특정 프레임의 초기 scale값을 가져오기 위한 메소드
     * 
     * @param index 프레임 인덱스
     * @return float 설정된 scale값
     */
    public float getFrameImageDefaultScale(int index) {
        return mFrameViews.get(index).getOritinalScaleImage();
    }
    
    /**
     * U+Story<br>
     * 특정 프레임이 비디오 타입인지 설정. 무비 다이어리에서만 사용.
     * 
     * @param index 프레임 인덱스
     * @param isVideoType 비디오 타입 여부 설정. default는 false
     */
    public void setIsVideoType(int index, boolean isVideoType) {
        mFrameViews.get(index).setIsVideoType(isVideoType);
    }
    
    /**
     * U+Story<br>
     * 특정 프레임이 비디오 타입인지 여부 반환.
     * 
     * @param index 프레임 인덱스.
     * @return 프레임의 비디오 타입인지 여부
     */
    public boolean getIsVideoType(int index) {
        return mFrameViews.get(index).getIsVideoType();
    }

    /**
     * 콜라주 프레임의 이미지 목록을 반환한다.
     * 
     * @return 이미지 목록
     * @version 1.0
     */
    public SparseArray<Bitmap> getFrameImages() {
        SparseArray<Bitmap> images = new SparseArray<Bitmap>();
        int i = 0;
        for(CollageFrameView view : mFrameViews) {
            images.put(i++, view.getImage());
        }
        return images;
    }

    /**
     * 모든 프레임에 이미지가 설정되어 있는지 확인한다.
     */
    public boolean isAllHasFrameImage() {
        boolean hasAllImage = true;
        for(CollageFrameView view : mFrameViews) {
            if(!view.hasImage()) {
                hasAllImage = false;
            }
        }
        return hasAllImage;
    }

    /**
     * 프레임을 선택한다.
     */
    private void selectFrame(int index) {
        if(index < 0 || index > mFrameViews.size() - 1) {
            return;
        }
        mCurrentFrameView = mFrameViews.get(index);
        for(int i = 0; i < mFrameViews.size(); i++) {
            if(i != index) {
                mFrameViews.get(i).setSelected(false);
            } else {
                mFrameViews.get(i).setSelected(true);
            }
        }
        if(mOnFrameStatusChangedListener != null && mCurrentFrameView != null) {
            mOnFrameStatusChangedListener.onFrameSelected(true,
                                                          mFrameViews.indexOf(mCurrentFrameView));
        }
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 현재 선택된 프레임의 index를 반환한다.
     * 
     * @return 프레임 index. 선택된 프레임이 없으면 -1
     * @version 1.0
     */
    public int getSelectedFrameIndex() {
        return mFrameViews.indexOf(mCurrentFrameView);
    }

    /**
     * 프레임 선택을 해제한다.
     * 
     * @version 1.0
     */
    public void deselectFrame() {
        for(CollageFrameView view : mFrameViews) {
            view.setSelected(false);
        }
        if(mOnFrameStatusChangedListener != null && mCurrentFrameView != null) {
            mOnFrameStatusChangedListener.onFrameSelected(false,
                                                          mFrameViews.indexOf(mCurrentFrameView));
        }
        mCurrentFrameView = null;
        mHandler.sendEmptyMessage(HANDLER_MESSAGE_INVALIDATE);
    }

    /**
     * 선택된 프레임의 이미지를 90도 회전시킨다.
     * 
     * @version 1.0
     */
    public void rotateImage() {
        if(mCurrentFrameView != null) {
            mCurrentFrameView.rotateImage(-90);
            mCurrentFrameView.adjustBoundImagePosition(true);
            Message invalidate = new Message();
            invalidate.what = HANDLER_MESSAGE_INVALIDATE;
            invalidate.obj = mCurrentFrameView;
            mHandler.sendMessage(invalidate);
        }
    }

    /**
     * 선택됨 프레임의 이미지를 좌우 대칭 시킨다.
     * 
     * @version 1.0
     */
    public void flipImage() {
        if(mCurrentFrameView != null) {
            mCurrentFrameView.flipImage();
            Message invalidate = new Message();
            invalidate.what = HANDLER_MESSAGE_INVALIDATE;
            invalidate.obj = mCurrentFrameView;
            mHandler.sendMessage(invalidate);
        }
    }

    /**
     * 선택한 프레임의 Rect를 반환한다.
     * 
     * @return 프레임 Rect
     * @version 1.0
     */
    public Rect getSelectedFrameRect() {
        if(mCurrentFrameView != null) {
            return mCurrentFrameView.getFrameRect();
        }
        return null;
    }

    /**
     * 프레임의 Rect를 반환한다.
     * 
     * @param index 프레임 index
     * @return 프레임 Rect
     * @version 1.0
     */
    public Rect getFrameRect(int index) {
        if(index != -1 && index <= mFrameViews.size() - 1) {
            return mFrameViews.get(index).getFrameRect();
        }
        return null;
    }

    /**
     * 전체 프레임의 Rect를 반환한다.
     * 
     * @return 전체 프레임 Rect
     * @version 1.0
     */
    public ArrayList<Rect> getFrameRects() {
        ArrayList<Rect> rects = new ArrayList<Rect>();
        for(CollageFrameView frameView : mFrameViews) {
            rects.add(frameView.getFrameRect());
        }
        return rects;
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
        for(CollageFrameView frameView : mFrameViews) {
            rects.add(frameView.getFrameBounds());
        }
        return rects;
    }

    /**
     * 프레임에서 사람의 얼굴이 배치되기 적절한 위치를 반환한다.
     */
    public ArrayList<ArrayList<RectF>> getFaceBounds() {
        ArrayList<ArrayList<RectF>> rects = new ArrayList<ArrayList<RectF>>();
        ArrayList<RectF> faceRects;
        for(CollageFrameInfo frameInfo : mDesignTemplate.mFrameInfos) {
            faceRects = new ArrayList<RectF>();
            for(RectF face : frameInfo.mFaceRect) {
                faceRects.add(new RectF(face));
            }
            rects.add(faceRects);
        }
        return rects;
    }

    /**
     * U+Story<br>
     * 특정 프레임의 scale값을 가져온다.<br>
     * 초기값과 사용자의 액션에 의해 설정된 값을 가져옴
     * 
     * @param index 가져올 프레임 인덱스
     * @return float 설정되어있는 scale값
     */
    public float getImageScaleWidthIndex(int index) {
        if(mFrameViews.size() > index) {
            return mFrameViews.get(index).getCurrentScaleImage();
        }
        return 1.f;
    }
    
    /**
     * 프레임에 적용된 scale값을 얻어온다.
     */
    public float[] getFrameScale(int index) {
        if(mFrameViews.size() > index) {
            return mFrameViews.get(index).getFrameScale();
        }
        return null;
    }

    /**
     * U+Story<br>
     * 특정 프레임의 rotation값을 가져온다.<br>
     * 초기값과 사용자의 액션에 의해 설정된 값을 가져옴
     * 
     * @param index 가져올 프레임 인덱스
     * @return float 설정되어있는 rotation값
     */
    public float getImageRotateWidthIndex(int index) {
        if(mFrameViews.size() > index) {
            return mFrameViews.get(index).getCurrentRotateImage();
        }
        return 0.f;
    }

    /**
     * U+Story<br>
     * 특정 프레임의 translate값을 가져온다.<br>
     * 초기값과 사용자의 액션에 의해 설정된 값을 가져옴
     * 
     * @param index 가져올 프레임 인덱스
     * @return PointF 설정되어있는 tranlate값
     */
    public PointF getImageTranslateWithIndex(int index) {
        if(mFrameViews.size() > index) {
            return new PointF(mFrameViews.get(index).getCurrentTranslateXImage(),
                              mFrameViews.get(index).getCurrentTranslateYImage());
        }
        return new PointF(0, 0);
    }

    /**
     * 프레임의 배경을 투명하게 설정한다.
     * 
     * @param index 프레임 index
     * @param enable true 이면 프레임의 배경을 투명하게 설정한다. false 이면 프레임에 설정된 이미지가 보여진다.
     * @version 1.0
     */
    public void requestTransparentFrame(int index, boolean enable) {
        if(index != -1 && index <= mFrameViews.size() - 1) {
            mFrameViews.get(index).setTransparentFrame(enable);
            if(enable) {
                selectFrame(index);
            }
        }
    }

    /**
     * 프레임 이미지의 scale 범위를 설정한다.
     * 
     * @param minScale 최소 scale 값
     * @param maxScale 최대 scale 값
     * @version 1.0
     */
    public void setFrameImageScale(float minScale, float maxScale) {
        mFillImageMinScale = minScale;
        mFillImageMaxScale = maxScale;
        for(CollageFrameView frameView : mFrameViews) {
            frameView.setImageScale(mFillImageMinScale, mFillImageMaxScale);
        }
    }

    /**
     * 프레임 선택 박스 color를 설정한다. <br>
     * Note that the color is an int containing alpha as well as r,g,b. This 32bit value is not
     * premultiplied, meaning that its alpha can be any value, regardless of the values of r,g,b.
     * See the Color class for more details.
     * 
     * @param color 프레임 선택 박스 color
     * @version 1.0
     */
    public void setFrameSelectionColor(int color) {
        mBoxPaint.setColor(color);
    }

    /**
     * 프레임 Drag & Drop 박스 color를 설정한다. <br>
     * Note that the color is an int containing alpha as well as r,g,b. This 32bit value is not
     * premultiplied, meaning that its alpha can be any value, regardless of the values of r,g,b.
     * See the Color class for more details.
     * 
     * @param color 프레임 Drag & Drop 박스 color
     * @version 1.0
     */
    public void setFrameDragNDropColor(int color) {
        mDragBoxPaint.setColor(color);
    }

    /**
     * 프레임 이미지 Drag & Drop 기능 사용 유무를 설정한다.
     * 
     * @param enabled true 이면 Drag & Drop 기능 사용. false 이면 Drag & Drop 사용 불가.
     * @version 1.0
     */
    public void setFrameDragNDropEnabled(boolean enabled) {
        mDragAndDropEnabled = enabled;
    }

    /**
     * 프레임 background color를 설정한다. <br>
     * Note that the color is an int containing alpha as well as r,g,b. This 32bit value is not
     * premultiplied, meaning that its alpha can be any value, regardless of the values of r,g,b.
     * See the Color class for more details.
     * 
     * @param color 프레임 background color
     * @version 1.0
     */
    public void setFrameBackgroundColor(int color) {
        for(CollageFrameView frameView : mFrameViews) {
            frameView.setBackgroundColor(color);
        }
    }

    /**
     * 모든 프레임에 설정된 이미지가 프레임 영역을 벗어나지 않도록 보정한다. 
     */
    public void adjustBoundImagePosition() {
        for(CollageFrameView frameView : mFrameViews) {
            frameView.adjustBoundImagePosition(true);
        }
    }
    
    /**
     * 프레임이 1개인 콜라주일 경우 프레임의 이미지 영역을 프레임 영역에서 콜라주 전체 영역으로 확장한다.
     */
    public void setSingleFullFrame(boolean isSingleFullFrame) {
        mSingleFullFrame = isSingleFullFrame;
    }

    /**
     * 합성된 이미지를 반환한다.
     * 
     * @param size 합성된 이미지의 크기
     * @return 합성된 이미지
     * @version 1.0
     */
    public Bitmap getCollageImage(int size) {
        int width = getWidth();
        int height = getHeight();
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
        canvas.scale(width / (float)getWidth(), height / (float)getHeight());
        onAlternativeDraw(canvas, true);
        canvas.restore();
        
        // 데모 버전일 경우 워터마크를 그려준다.
        if(Constants.DEMO_VERSION) {
            BitmapUtils.applyWaterMarkImage(getContext(), bitmap);
        }
        return bitmap;
    }

    /**
     * View가 가지는 리소스를 해제한다.
     * 
     * @version 1.0
     */
    public void clear() {
        if(mBackground != null && !mBackground.isRecycled()) {
            mBackground.recycle();
            mBackground = null;
        }
        if(mDesignTemplate != null) {
            mDesignTemplate = null;
        }
        for(CollageFrameView view : mFrameViews) {
            view.clear();
        }
        mFrameViews.clear();
        mLastFrameView = null;
        mCurrentFrameView = null;
        if(mMoveTimer != null) {
            mMoveTimer.cancel();
            mMoveTimer = null;
        }
        System.gc();
    }

    /**
     * 디자인 테마(싱글)에서 LongClick 막기위한 flag 
     */
    public void disableLongPressEvent(){
    	mIsDisableLongPress = true;
    }
    
    public void enableLongPressEvent(){
    	mIsDisableLongPress = false;
    }
    
    /**
     * {@link #OnFrameStatusChangedListener}<br>
     * 프레임의 상태 변경을 감지하는 리스너를 등록한다.
     * 
     * @param listener 프레임 상태 변경 리스너
     * @version 1.0
     */
    public void setFrameStatusChangedListener(OnFrameStatusChangedListener listener) {
        mOnFrameStatusChangedListener = listener;
    }

    /**
     * 프레임의 상태가 변경되면 호출되는 class
     * 
     * @version 1.0
     */
    public interface OnFrameStatusChangedListener {
        /**
         * 프레임이 선택되거나 선택이 해제되면 호출된다.
         * 
         * @param selected 프레임 선택 여부
         * @version 1.0
         */
        public void onFrameSelected(boolean selected, int index);

        /**
         * 프레임간에 이미지가 교체되면 호출된다.
         * 
         * @param from 선택한 프레임 index
         * @param to 이동된 프레임 index
         * @version 1.0
         */
        public void onFrameDragAndDrop(int from, int to);
    }
}
