package com.sugarmount.sugarcamera.story.views;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.sugarmount.sugaralbum.R;
import com.sugarmount.sugarcamera.utils.DisplayUtil;

public class TwoButtonTextDialog extends Dialog {

	private TextView mMessage1;
	private TextView mMessage2;
	private Button mOK;
	private Button mCancel;
	
	private OnBtnClickListener mBtnClickListener;
	
	public interface OnBtnClickListener {
		public void onClick(View view, boolean flag);
	}
	
	public TwoButtonTextDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		init(context);
	}

	public TwoButtonTextDialog(Context context, int theme) {
		super(context, theme);
		init(context);
	}

	public TwoButtonTextDialog(Context context) {
		super(context, android.R.style.Theme_Translucent_NoTitleBar);
		init(context);
	}

	private void init(Context context) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
		setCanceledOnTouchOutside(false);
		
		View view = getLayoutInflater().inflate(R.layout.story_dialog_text_two_button, null);
		setContentView(view, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		
		mMessage1 = (TextView) view.findViewById(R.id.dialog_message1);
		mMessage2 = (TextView) view.findViewById(R.id.dialog_message2);
		mOK = (Button) view.findViewById(R.id.dialog_ok);
		mCancel = (Button) view.findViewById(R.id.dialog_cancel);
		mOK.setOnClickListener(mButtonClickListener);
		mCancel.setOnClickListener(mButtonClickListener);
				
		int displayLandscapeWidth  = DisplayUtil.getDisplaySizeWidth(getContext());
		Configuration configuration = Resources.getSystem().getConfiguration();
		if(configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			displayLandscapeWidth  = DisplayUtil.getDisplaySizeHeight(getContext());
		}
		
		int frameWidth = displayLandscapeWidth - view.getPaddingLeft() - view.getPaddingRight();
		LinearLayout dialogFrame = (LinearLayout) view.findViewById(R.id.dialog_frame);
		dialogFrame.getLayoutParams().width = frameWidth;
	}
	
	public void setOnBtnClickListener(OnBtnClickListener listener) {
		mBtnClickListener = listener;
	}
	
	View.OnClickListener mButtonClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int i = v.getId();
			if (i == R.id.dialog_ok) {
				if (mBtnClickListener != null) {
					mBtnClickListener.onClick(v, true);
				}

			} else if (i == R.id.dialog_cancel) {
				if (mBtnClickListener != null) {
					mBtnClickListener.onClick(v, false);
				}

			}
			dismiss();
		}
	};
	
	public void setDialogMessage1(CharSequence message) {
		if(!TextUtils.isEmpty(message)) {
			mMessage1.setText(message);
		}
	}
	
	public void setDialogMessage2(CharSequence message) {
		mMessage2.setText(message);
	}
	
	public void setMessage1TextSize(float size) {
		mMessage1.setTextSize(size); 
	}
	
	public void setMessage2TextSize(float size) {
		mMessage2.setTextSize(size); 
	}
	
	public void setOkBtnText(CharSequence text) {
		mOK.setText(text);
	}
	
// bnp csy 130911 : [
	public void setOkBtnTextColor(int resColorID) {
		mOK.setTextColor(resColorID);
	}
	
	public void setOkBtnTextColor(ColorStateList colors) {
		mOK.setTextColor(colors);
	}
// bnp csy 130911 : ]
	
	public void setCancelBtnText(CharSequence text) {
		mCancel.setText(text);
	}
	
	public void setCancelBtnTextColor(int resColorID) {
		mCancel.setTextColor(resColorID);
	}
	
	@Override
	public void show() {
		if (TextUtils.isEmpty(mMessage2.getText())) {
			mMessage2.setVisibility(View.GONE);
		}
		super.show();
	}
}