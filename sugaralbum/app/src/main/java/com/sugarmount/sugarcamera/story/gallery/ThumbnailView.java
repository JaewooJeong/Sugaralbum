package com.sugarmount.sugarcamera.story.gallery;


import com.kiwiple.debug.L;
import com.sugarmount.sugarcamera.story.gallery.widget.FadeInImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

public class ThumbnailView extends FadeInImageView {
	
	private static final String TAG = "ThumbnailView";
	
	public static enum LoadState { DONE, QUEUE, LOAD };
	private LoadState mLoadState = LoadState.DONE;
	
	private RowItemView mRowItemView;	// parent
	
	// image data
	private String mContentPath;
	
	private Matrix mMatrix = new Matrix();
	private float[] mMatrixValue = new float[9];
	
	public ThumbnailView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public ThumbnailView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public ThumbnailView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}
	
	private void init(Context context) {
		setScaleType(ScaleType.MATRIX);
		mMatrix.getValues(mMatrixValue);
	}
	
	public void setRowItemView(RowItemView rowItemView) {
		mRowItemView = rowItemView;
	}
	
	public void displayVideoUI(int visibility) {
		if(mRowItemView != null) {
			mRowItemView.displayVideoUI(visibility);
		}
	}
	
	public void displayInvalidThumbnail() {
		if(mRowItemView != null) {
			mRowItemView.displayInvalidThumbnail();
		}
	}
	
	public void setLoadState(LoadState state) {
		mLoadState = state;
	}
	
	public LoadState getLoadState() {
		return mLoadState;
	}
	
	public void setImageBitmap(Bitmap bm, String contentPath) {
		if(mContentPath != null && 
				mContentPath.equals(contentPath)) {
			super.setImageBitmap(bm);
		} else {
			super.setFadeInBitmap(bm);
			mContentPath = contentPath;
		}
		
		if(bm != null) {
			setImageFitOnView(getWidth(), getHeight());
		} else {
			mContentPath = null;
		}
	}
	
	public void setImageDrawable(Drawable drawable, String contentPath) {
        if(mContentPath != null && 
                mContentPath.equals(contentPath)) {
            super.setImageDrawable(drawable);
        } else {
            super.setFadeInBitmap(drawable);
            mContentPath = contentPath;
        }
        
        if(drawable != null) {
            setImageFitOnView(getWidth(), getHeight());
        } else {
            mContentPath = null;
        }
    }
	
	@Override
	public void setImageDrawable(Drawable drawable) {
	    super.setImageDrawable(drawable);
	    
	    if (drawable != null) {
	        setImageFitOnView(getWidth(), getHeight());
        } else {
            mContentPath = null;
        }
	}
	
	@Override
	public void setImageBitmap(Bitmap bm) {
		// TODO Auto-generated method stub
		super.setImageBitmap(bm);
		
		if(bm != null) {
			setImageFitOnView(getWidth(), getHeight());
		} else {
			mContentPath = null;
		}
	}
	
	public String getImagePath() {
		return mContentPath;
	}
	
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		// TODO Auto-generated method stub
		super.onSizeChanged(w, h, oldw, oldh);
		
		setImageFitOnView(w, h);
	}
	
	private void setImageFitOnView(int widthOfView, int heightOfView) {
		
		Drawable drawable = getDrawable();
		if(drawable == null) { return; }
		
		int imageW = drawable.getIntrinsicWidth();
		int imageH = drawable.getIntrinsicHeight();
		
		if(imageW < imageH) {
			mMatrixValue[0] = mMatrixValue[4] = widthOfView / (float)imageW;
		} else {
			mMatrixValue[0] = mMatrixValue[4] = heightOfView / (float)imageH;
		}
		
		// set center
		int scaleW = Math.round(imageW * mMatrixValue[0]);
		int scaleH = Math.round(imageH * mMatrixValue[4]);
		mMatrixValue[2] = (widthOfView / 2.f) - (scaleW / 2.f);
		mMatrixValue[5] = (heightOfView / 2.f) - (scaleH / 2.f);
		
		mMatrix.setValues(mMatrixValue);
		setImageMatrix(mMatrix);
	}
	
}
