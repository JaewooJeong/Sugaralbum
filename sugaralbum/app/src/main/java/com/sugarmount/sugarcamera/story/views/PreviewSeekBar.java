package com.sugarmount.sugarcamera.story.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.SeekBar;

/**
 * PreviewSeekBar.
 */
public class PreviewSeekBar extends SeekBar {
	
	private boolean isSeekBarEnable = true; 

	public PreviewSeekBar(Context context) {
		super(context);
	}

	public PreviewSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PreviewSeekBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public boolean isSeekBarEnable() {
		return isSeekBarEnable;
	}

	public void setSeekBarEnable(boolean isSeekBarEnable) {
		this.isSeekBarEnable = isSeekBarEnable;
	}


	private OnSeekBarChangeListener mOnSeekBarListener = null;
	@Override
	public void setOnSeekBarChangeListener(OnSeekBarChangeListener seekBarListener) {
		// TODO Auto-generated method stub
		super.setOnSeekBarChangeListener(seekBarListener);
		mOnSeekBarListener = seekBarListener;
		
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		int action = event.getAction() & MotionEvent.ACTION_MASK;
		if(!isSeekBarEnable()){
			return true; 
		}
		switch (action) {
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_MOVE:
				float progress = event.getX() / getWidth();
				if (getMax() * progress > getSecondaryProgress()) {
					return true;
				}
				break;

			case MotionEvent.ACTION_UP:
				setPressed(false);
				invalidate();
				//ACTION_UP일때 강제 호출
				mOnSeekBarListener.onStopTrackingTouch(this);
				return true;

			default:
				break;
		}
		return super.onTouchEvent(event);
	}

	@Override
	public synchronized void setProgress(int progress) {

		if (progress <= getSecondaryProgress()) {
			super.setProgress(progress);
		}
	}
}
