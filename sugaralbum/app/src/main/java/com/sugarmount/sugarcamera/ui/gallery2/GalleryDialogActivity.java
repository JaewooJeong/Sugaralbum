package com.sugarmount.sugarcamera.ui.gallery2;

import android.app.Dialog;
import android.os.Debug;
import android.util.DisplayMetrics;

import com.sugarmount.sugarcamera.BaseActivity;

public abstract class GalleryDialogActivity extends BaseActivity {

	@Override
	protected void onResume() {
		super.onResume();
		System.gc();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		System.gc();
	}
	
	protected void releaseDialog(Dialog dialog) {
		if (dialog != null) {
			if (dialog.isShowing()) {
				dialog.dismiss();
			}
		}
	}


}
