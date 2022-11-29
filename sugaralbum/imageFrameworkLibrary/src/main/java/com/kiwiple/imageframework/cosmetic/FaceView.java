
package com.kiwiple.imageframework.cosmetic;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 눈크게, 볼갸름하게 등 성형기능을 지원하기 위해 얼굴, 눈, 볼, 턱 등 특정 영역의 이미지를 반환하는 View 상속 클래스
 * 
 * @version 2.0
 */
public class FaceView extends View {
    private Bitmap mSourceImage;
    private Paint mTmpPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private Paint mPOuterBullsEye = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPInnerBullsEye = new Paint(Paint.ANTI_ALIAS_FLAG);
    
    private boolean DEBUG = false;

    private ArrayList<Face> detectedFaces = new ArrayList<Face>();

    private Bitmap mSubBitmap;
    private Canvas mCanvas;
    // for eye
    private RectF mLeftEyeBound = new RectF();
    private Bitmap mLeftEyeBitmap;
    private RectF mRightEyeBound = new RectF();
    private Bitmap mRightEyeBitmap;
    // for cheek
    private RectF mCheekBound = new RectF();
    private Bitmap mCheekBitmap;
    // for face
    private RectF mFaceBound = new RectF();
    private Bitmap mFaceBitmap;
    // for chin
    private RectF mChinBound = new RectF();
    private Bitmap mChinBitmap;

    private Matrix mImageMatrix = new Matrix();
    private RectF mTempSrc = new RectF();
    private RectF mTempDst = new RectF();
    
    
    //aubergine : face rect  추가함. 
    private RectF mMouseBound = new RectF();
    private RectF mNoseBound = new RectF();
    public RectF mLeftCheekBound = new RectF();
    public RectF mRightCheekBound = new RectF();
    

    public FaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FaceView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        onAlternativeDraw(canvas, DEBUG);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 뷰의 크기를 이미지 비율에 맞춰 재조정한다.
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if(mSourceImage != null) {
            float calcWidthByHeight = mSourceImage.getWidth() / (float)mSourceImage.getHeight();
            if(width < (int)(height * calcWidthByHeight)) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                heightMeasureSpec = MeasureSpec.makeMeasureSpec((int)(width * (1 / calcWidthByHeight)),
                                                                MeasureSpec.EXACTLY);
            } else {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec((int)(height * calcWidthByHeight),
                                                               MeasureSpec.EXACTLY);
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

        if(mSourceImage != null) {
            mTempSrc.set(0, 0, mSourceImage.getWidth(), mSourceImage.getHeight());
            mTempDst.set(0, 0, right - left, bottom - top);

            // fit-center
            mImageMatrix.reset();
            mImageMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.CENTER);

            if(detectedFaces.size() != 0 && detectedFaces.get(0) != null) {
                mFaceBound.set(detectedFaces.get(0).mFaceBound);
                mChinBound.set(detectedFaces.get(0).mChinBound);
                mCheekBound.set(detectedFaces.get(0).mCheekBound);
                mLeftEyeBound.set(detectedFaces.get(0).mLeftEyeBound);
                mRightEyeBound.set(detectedFaces.get(0).mRightEyeBound);
                mMouseBound.set(detectedFaces.get(0).mMouseBound);
                mNoseBound.set(detectedFaces.get(0).mNoseBound);
                mLeftCheekBound.set(detectedFaces.get(0).mLeftCheekBound);
                mRightCheekBound.set(detectedFaces.get(0).mRightCheekBound);
                //얼굴 인식에서 사용한 이미지 크기 대비 원본 이미지의 크기에 대한 비율을 반영한다.
                scaleRect(mSourceImage.getWidth() / detectedFaces.get(0).mWidth, mFaceBound);
                scaleRect(mSourceImage.getWidth() / detectedFaces.get(0).mWidth, mCheekBound);
                scaleRect(mSourceImage.getWidth() / detectedFaces.get(0).mWidth, mChinBound);
                scaleRect(mSourceImage.getWidth() / detectedFaces.get(0).mWidth, mLeftEyeBound);
                scaleRect(mSourceImage.getWidth() / detectedFaces.get(0).mWidth, mRightEyeBound);
                scaleRect(mSourceImage.getWidth() / detectedFaces.get(0).mWidth, mMouseBound);
                scaleRect(mSourceImage.getWidth() / detectedFaces.get(0).mWidth, mNoseBound);
                scaleRect(mSourceImage.getWidth() / detectedFaces.get(0).mWidth, mLeftCheekBound);
                scaleRect(mSourceImage.getWidth() / detectedFaces.get(0).mWidth, mRightCheekBound);
                calculateFaceBound();
                invalidate();
            }
        }
    }
    
    
    
    private Paint mPaintFace = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPaintEye = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPaintChin = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPaintCheek = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPaintMouse= new Paint(Paint.ANTI_ALIAS_FLAG);
    
    
    /**
     * 적용된 보정 효과를 초기화 한다.
     * 
     * @version 2.0
     */
    public void init() {
        if(mCheekBitmap != null) {
            mCheekBitmap.recycle();
            mCheekBitmap = null;
        }
        if(mFaceBitmap != null) {
            mFaceBitmap.recycle();
            mFaceBitmap = null;
        }
        if(mLeftEyeBitmap != null) {
            mLeftEyeBitmap.recycle();
            mLeftEyeBitmap = null;
        }
        if(mRightEyeBitmap != null) {
            mRightEyeBitmap.recycle();
            mRightEyeBitmap = null;
        }
        if(mChinBitmap != null) {
            mChinBitmap.recycle();
            mChinBitmap = null;
        }
        
        
        mPaintFace.setStyle(Paint.Style.STROKE);
        mPaintFace.setColor(Color.RED);
    	
    	
        mPaintEye.setStyle(Paint.Style.STROKE);
        mPaintEye.setColor(Color.BLUE);
    	
    	
        mPaintChin.setStyle(Paint.Style.STROKE);
        mPaintChin.setColor(Color.GREEN);
    	
    	
        mPaintCheek.setStyle(Paint.Style.STROKE);
        mPaintCheek.setColor(Color.YELLOW);
    	
    	
        mPaintMouse.setStyle(Paint.Style.STROKE);
        mPaintMouse.setColor(Color.GRAY);
    }

    /**
     * 보정 효과가 적용된 이미지를 canvas에 그려준다.
     * 
     * @param canvas 보정 효과가 적용된 이미지를 그릴 canvas
     * @param output 최종 output일 경우 true로 설장한다.(false로 설정하면 사용자 가이드 요소도 포함된다.)
     * @version 2.0
     */
    public void onAlternativeDraw(Canvas canvas, boolean output) {
        canvas.drawBitmap(mSourceImage, mImageMatrix, mTmpPaint);
        if(mFaceBitmap != null) {
            canvas.drawBitmap(mFaceBitmap, mFaceBound.left, mFaceBound.top, null);
        }
        if(mCheekBitmap != null) {
            canvas.drawBitmap(mCheekBitmap, mCheekBound.left, mCheekBound.top, null);
        }
        if(mChinBitmap != null) {
            canvas.drawBitmap(mChinBitmap, mChinBound.left, mChinBound.top, null);
        }
        if(mLeftEyeBitmap != null) {
            canvas.drawBitmap(mLeftEyeBitmap, mLeftEyeBound.left, mLeftEyeBound.top, null);
        }
        if(mRightEyeBitmap != null) {
            canvas.drawBitmap(mRightEyeBitmap, mRightEyeBound.left, mRightEyeBound.top, null);
        }

        if(output) {
             drawFace(canvas);
        }
    }

    private void drawFace(Canvas canvas) {
    	
        canvas.drawRect(mFaceBound, mPaintFace);
        canvas.drawRect(mLeftEyeBound, mPaintEye);
        canvas.drawRect(mRightEyeBound, mPaintEye);
        canvas.drawRect(mChinBound, mPaintChin);
        //canvas.drawRect(mCheekBound, mPaintCheek);
        canvas.drawRect(mMouseBound, mPaintMouse);
        //canvas.drawRect(mNoseBound, mPaintMouse);
        canvas.drawRect(mLeftCheekBound, mPaintFace);
        canvas.drawRect(mRightCheekBound, mPaintFace);
    }

    /**
     * 인공지능 보정할 이미지를 설정한다.
     * 
     * @param bm 원본 이미지
     * @version 2.0
     */
    public void setImageBitmap(Bitmap image) {
        mPInnerBullsEye.setStyle(Paint.Style.STROKE);
        mPInnerBullsEye.setColor(Color.RED);

        mPOuterBullsEye.setStyle(Paint.Style.STROKE);
        mPOuterBullsEye.setColor(Color.RED);

        mSourceImage = image;

        // 이미지를 레이아웃에 맞춰주기 위해 다시 초기화 해 준다.
        requestLayout();
    }

    /**
     * 보정할 원본 이미지를 변경한다.
     * 
     * @param image 보정할 이미지
     * @version 2.0
     */
    public void changeImage(Bitmap image) {
        mSourceImage = image;
    }

    /**
     * 원본 이미지를 반환한다.
     * 
     * @return 원본 이미지
     * @version 2.0
     */
    public Bitmap getImageBitmap() {
        return mSourceImage;
    }

    /**
     * 원본 이미지에 포함된 얼굴의 개수를 반환한다.
     * 
     * @return 얼굴의 개수
     * @version 2.0
     */
    public int hasFace() {
        return detectedFaces.size();
    }

    /**
     * 얼굴 정보를 설정한다.
     * 
     * @param face 얼굴 정보
     * @version 2.0
     */
    public void setFace(Face face) {
        detectedFaces.clear();
        detectedFaces.add(face);
        requestLayout();
    }

    private void scaleRect(float scale, RectF rect) {
        rect.left *= scale;
        rect.top *= scale;
        rect.right *= scale;
        rect.bottom *= scale;
    }
    
    private void scaleBound(float scale, List<PointF> points){
    	for(int i = 0;i<points.size();i++){
    		points.get(i).x *=scale;
    		points.get(i).y *=scale;
    	}
    }

    private void calculateFaceBound() {
        mImageMatrix.mapRect(mFaceBound);
        mImageMatrix.mapRect(mLeftEyeBound);
        mImageMatrix.mapRect(mRightEyeBound);
        mImageMatrix.mapRect(mChinBound);
        mImageMatrix.mapRect(mCheekBound);
        mImageMatrix.mapRect(mMouseBound);
        mImageMatrix.mapRect(mNoseBound);
        mImageMatrix.mapRect(mLeftCheekBound);
        mImageMatrix.mapRect(mRightCheekBound);
    }

    /**
     * 좌측 눈 영역 이미지를 반환한다.
     * 
     * @return 좌측 눈 영역 이미지
     * @version 2.0
     */
    public Bitmap getLeftEyeBitmap() {
        if(mCanvas == null) {
            mSubBitmap = Bitmap.createBitmap(mSourceImage.getWidth(), mSourceImage.getHeight(),
                                             Config.ARGB_8888);
            mCanvas = new Canvas(mSubBitmap);
        }

        if(mLeftEyeBound.width() < 0 || mLeftEyeBound.height() < 0) {
            return null;
        }
        
        mCanvas.save();
        mCanvas.clipRect(mLeftEyeBound);
        onAlternativeDraw(mCanvas, DEBUG);
        mCanvas.restore();

        mLeftEyeBitmap = Bitmap.createBitmap((int)mLeftEyeBound.width(),
                                             (int)mLeftEyeBound.height(), Config.ARGB_8888);
        Canvas canvas = new Canvas(mLeftEyeBitmap);
        canvas.drawBitmap(mSubBitmap, convertRect(mLeftEyeBound),
                          new Rect(0, 0, mLeftEyeBitmap.getWidth(), mLeftEyeBitmap.getHeight()),
                          null);

        return mLeftEyeBitmap;
    }
    
    public RectF getLeftEyeBound() {
        return mLeftEyeBound;
    }

    /**
     * 우측 눈 영역 이미지를 반환한다.
     * 
     * @return 우측 눈 영역 이미지
     * @version 2.0
     */
    public Bitmap getRightEyeBitmap() {
        if(mCanvas == null) {
            mSubBitmap = Bitmap.createBitmap(mSourceImage.getWidth(), mSourceImage.getHeight(),
                                             Config.ARGB_8888);
            mCanvas = new Canvas(mSubBitmap);
        }
        
        if(mRightEyeBound.width() < 0 || mRightEyeBound.height() < 0) {
            return null;
        }

        mCanvas.save();
        mCanvas.clipRect(mRightEyeBound);
        onAlternativeDraw(mCanvas, DEBUG);
        mCanvas.restore();

        mRightEyeBitmap = Bitmap.createBitmap((int)mRightEyeBound.width(),
                                              (int)mRightEyeBound.height(), Config.ARGB_8888);
        Canvas canvas = new Canvas(mRightEyeBitmap);
        canvas.drawBitmap(mSubBitmap, convertRect(mRightEyeBound),
                          new Rect(0, 0, mRightEyeBitmap.getWidth(), mRightEyeBitmap.getHeight()),
                          null);

        return mRightEyeBitmap;
    }
    
    public RectF getRightEyeBound() {
        return mRightEyeBound;
    }

    /**
     * 볼 영역 이미지를 반환한다.
     * 
     * @return 볼 영역 이미지
     * @version 2.0
     */
    public Bitmap getCheekBitmap() {
        if(mCanvas == null) {
            mSubBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
            mCanvas = new Canvas(mSubBitmap);
        }
        
        if(mCheekBound.width() < 0 || mCheekBound.height() < 0) {
            return null;
        }

        mCanvas.save();
        mCanvas.clipRect(mCheekBound);
        onAlternativeDraw(mCanvas, DEBUG);
        mCanvas.restore();

        mCheekBitmap = Bitmap.createBitmap((int)mCheekBound.width(), (int)mCheekBound.height(),
                                           Config.ARGB_8888);
        Canvas canvas = new Canvas(mCheekBitmap);
        canvas.drawBitmap(mSubBitmap, convertRect(mCheekBound),
                          new Rect(0, 0, mCheekBitmap.getWidth(), mCheekBitmap.getHeight()), null);

        return mCheekBitmap;
    }
    
    public RectF getCheekBound() {
        return mCheekBound;
    }

    /**
     * 얼굴 영역 이미지를 반환한다.
     * 
     * @return 얼굴 영역 이미지
     * @version 2.0
     */
    public Bitmap getFaceBitmap() {
        if(mCanvas == null) {
            mSubBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
            mCanvas = new Canvas(mSubBitmap);
        }

        if(mFaceBound.width() <= 0 || mFaceBound.height() <= 0) {
            return null;
        }

        
        mCanvas.save();
        mCanvas.clipRect(mFaceBound);
        onAlternativeDraw(mCanvas, DEBUG);
        mCanvas.restore();

        mFaceBitmap = Bitmap.createBitmap((int)mFaceBound.width(), (int)mFaceBound.height(),
                                          Config.ARGB_8888);
        Canvas canvas = new Canvas(mFaceBitmap);
        canvas.drawBitmap(mSubBitmap, convertRect(mFaceBound),
                          new Rect(0, 0, mFaceBitmap.getWidth(), mFaceBitmap.getHeight()), null);

        return mFaceBitmap;
    }
    
    /**
     * 열굴 영역을 리턴한
     * @return
     */
    public RectF getFaceBound(){
    	return mFaceBound;
    }

    /**
     * 턱 영역 이미지를 반환한다.
     * 
     * @return 턱 영역 이미지
     * @version 2.0
     */
    public Bitmap getChinBitmap() {
        if(mCanvas == null) {
            mSubBitmap = Bitmap.createBitmap(getWidth(), getHeight(), Config.ARGB_8888);
            mCanvas = new Canvas(mSubBitmap);
        }
        
        if(mChinBound.width() < 0 || mChinBound.height() < 0) {
            return null;
        }

        mCanvas.save();
        mCanvas.clipRect(mChinBound);
        onAlternativeDraw(mCanvas, DEBUG);
        mCanvas.restore();

        mChinBitmap = Bitmap.createBitmap((int)mChinBound.width(), (int)mChinBound.height(),
                                          Config.ARGB_8888);
        Canvas canvas = new Canvas(mChinBitmap);
        canvas.drawBitmap(mSubBitmap, convertRect(mChinBound),
                          new Rect(0, 0, mChinBitmap.getWidth(), mChinBitmap.getHeight()), null);

        return mChinBitmap;
    }
    
    public RectF getChinBound() {
        return mChinBound;
    }
    
    public RectF getMouseBound(){
    	return mMouseBound;
    }
    
    public RectF getNoseBound(){
    	return mNoseBound;
    }

    
    public RectF getLeftCheekBound(){
    	return mLeftCheekBound;
    }
    
    public RectF getRightCheekBound(){
    	return mRightCheekBound;
    }
    
    private Rect convertRect(RectF rect) {
        return new Rect((int)rect.left, (int)rect.top, (int)rect.right, (int)rect.bottom);
    }
}
