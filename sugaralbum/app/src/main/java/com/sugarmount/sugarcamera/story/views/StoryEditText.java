package com.sugarmount.sugarcamera.story.views;

import com.kiwiple.debug.L;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class StoryEditText extends androidx.appcompat.widget.AppCompatEditText {
	
	private OnBackKeyPressListener mOnBackKeyPressListener;

	public StoryEditText(@NonNull Context context) {
		super(context);
	}

	public StoryEditText(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public StoryEditText(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public static interface OnBackKeyPressListener{
		public void onBackKeyPress(); 
	}

	@Override
	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
			L.d("Keycode : " + keyCode +", event : " + event); 
			if(mOnBackKeyPressListener != null){
				mOnBackKeyPressListener.onBackKeyPress(); 
			}
		}
		return super.onKeyPreIme(keyCode, event);
	}

	public void setOnBackKeyPressListener(OnBackKeyPressListener listener){
		mOnBackKeyPressListener = listener; 
	}
}
