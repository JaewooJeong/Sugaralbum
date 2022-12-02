
package com.kiwiple.imageframework.cosmetic;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 어러개의 얼굴이 인식된 경우, 편집 할 얼굴을 선택할 수 있도록 인식된 열굴 영역을 표시해준다. 
 * 선택 액션은 뷰에서 하지 않고, 액티비티에서 관리한다.
 * 
 * @version 1.0
 */
public class FaceSelectView extends View {
	private final String TAG = "FaceSelectView";
	private boolean DEBUG = false;
	private final int RECT_THICK = 5; 
	
	private Bitmap mSourceImage;
	private float mBaseScale = 1;

    private Paint mTmpPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private Paint mPaintOther = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPaintSelect = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint mPaintfill = new Paint(Paint.ANTI_ALIAS_FLAG);

    private ArrayList<RectF> mFaceBounds = new ArrayList<RectF>();
    private int mSelectedIndex = 0;
    private Matrix mImageMatrix = new Matrix();
    private RectF mTempSrc = new RectF();
    private RectF mTempDst = new RectF();

    public FaceSelectView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public FaceSelectView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FaceSelectView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        onAlternativeDraw(canvas);
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
       
        if(DEBUG) Log.e(TAG,"onLayout mSourceImage:"+mSourceImage);
        if(mSourceImage != null) {
            mTempSrc.set(0, 0, mSourceImage.getWidth(), mSourceImage.getHeight());
            mTempDst.set(0, 0, right - left, bottom - top);
            mImageMatrix.reset();
            mImageMatrix.setRectToRect(mTempSrc, mTempDst, ScaleToFit.CENTER);
            if(DEBUG){
	            Log.e(TAG,"onLayout mFaceBounds:"+mFaceBounds.size());
	            Log.e(TAG,"onLayout mSourceImage.getWidth():"+mSourceImage.getWidth());
	            Log.e(TAG,"getWidth:"+getWidth()+",getHeight: "+getHeight());
            }
            if(mFaceBounds.size() != 0){
            	for(int i = 0;i<mFaceBounds.size();i++){
	                RectF tmp =  scaleRect(mBaseScale/getWidth(), mFaceBounds.get(i));
	                mImageMatrix.mapRect(tmp);
            	}
                invalidate();
            }
        }
    }


    /**
     * 8월고도화 추가 기능 :  여러개의 얼굴이 인식된 경우, 인식된 얼굴 표
     * @param canvas
     */
    public void onAlternativeDraw(Canvas canvas) {
    
        canvas.drawBitmap(mSourceImage, mImageMatrix, mTmpPaint);
        canvas.drawRect(0, 0,getWidth(), getHeight(),mPaintfill );
        canvas.clipRect(0,0,0,0);
		RectF tmpRect;
		for (int i = 0; i < mFaceBounds.size(); i++) {
			tmpRect = scaleRect( getWidth() / mBaseScale , mFaceBounds.get(i));
			canvas.clipRect(tmpRect, Region.Op.UNION);
			canvas.drawBitmap(mSourceImage, mImageMatrix, mTmpPaint);
		}
		
		canvas.clipRect(0, 0,getWidth(), getHeight(), Region.Op.UNION);
		
		for (int i = 0; i < mFaceBounds.size(); i++) {
			tmpRect = scaleRect( getWidth() / mBaseScale , mFaceBounds.get(i));
			
			if (i == mSelectedIndex) canvas.drawRect(tmpRect, mPaintSelect);
			else canvas.drawRect(tmpRect, mPaintOther);
		}
    }

    private RectF convertRect(RectF rect) {
    	return new RectF(rect.left+5,rect.top+5,rect.right-5,rect.bottom-5);
    }

    public void setImageBitmap(Bitmap image) {
    	//선택된 인물 영역 표시에 사용 
    	mPaintSelect.setStyle(Paint.Style.STROKE);
    	mPaintSelect.setColor(Color.rgb(229, 81, 125));//#e5517d
    	mPaintSelect.setStrokeWidth(RECT_THICK); 
    	//선택되지 않은 인물 영역 표시에 사용 
    	mPaintOther.setStyle(Paint.Style.STROKE);
    	mPaintOther.setColor(Color.WHITE);
    	mPaintOther.setStrokeWidth(RECT_THICK);
    	
    	mPaintfill.setColor(Color.BLACK);
    	mPaintfill.setStyle(Paint.Style.FILL);
    	mPaintfill.setAlpha(150);
    	
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
     * 여러개의 얼굴이 인식된경우, 얼굴 선택을 위해서 해당 정보를 지정한다.
     * 
     * @param faces
     * @param selectedIndex
     */
    public void setFace(Face[] faces, int selectedIndex) {
    	mFaceBounds.clear();
        mSelectedIndex = selectedIndex;
        for(int i = 0;i<faces.length;i++){
        	RectF tmp = new RectF();
        	tmp.left = faces[i].mFaceBound.left;
        	tmp.top = faces[i].mFaceBound.top;
        	tmp.right = faces[i].mFaceBound.right;
        	tmp.bottom = faces[i].mFaceBound.bottom;
        	if(DEBUG) Log.e(TAG, "setFace, tmp:"+tmp);
        	mFaceBounds.add(tmp);
        }
        
        mBaseScale = faces[0].mWidth;
        
        requestLayout();
    }
    
    
    public void setSelectedIndex(int index){
    	this.mSelectedIndex = index;
    	requestLayout();
    }

    private RectF scaleRect(float scale, RectF rect) {
    	RectF tmp = new RectF();
    	tmp.left = rect.left * scale;
        tmp.top = rect.top * scale;
        tmp.right = rect.right * scale;
        tmp.bottom = rect.bottom * scale;
        return tmp;
    }

}
