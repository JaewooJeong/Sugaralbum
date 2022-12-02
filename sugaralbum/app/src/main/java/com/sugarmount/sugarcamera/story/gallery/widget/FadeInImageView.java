package com.sugarmount.sugarcamera.story.gallery.widget;


import com.sugarmount.sugaralbum.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class FadeInImageView extends ImageView {
	
	private Drawable[] mImages = new Drawable[2];

	public FadeInImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public FadeInImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		init(context);
	}

	public FadeInImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
		init(context);
	}
	
	private void init(Context context) {
		mImages[0] = getResources().getDrawable(R.drawable.transparent);
	}
	
	public BitmapDrawable getBitmapDrawable() {
		return ((BitmapDrawable)mImages[1]);
	}
	
	public void setFadeInBitmap(Bitmap bm) {
		// TODO Auto-generated method stub
		mImages[1] = new BitmapDrawable(getResources(), bm);
		TransitionDrawable td = new TransitionDrawable(mImages); 
		super.setImageDrawable(td);
		td.startTransition(260);
	}
	
	public void setFadeInBitmap(Drawable drawable) {
	    if (drawable == null) {
	        super.setImageDrawable(null);
	        return;
	    }
	    mImages[1] = drawable;
	    TransitionDrawable td = new TransitionDrawable(mImages); 
        super.setImageDrawable(td);
        td.startTransition(260);
	}
	
}