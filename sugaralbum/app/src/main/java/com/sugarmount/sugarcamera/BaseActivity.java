package com.sugarmount.sugarcamera;

import android.annotation.SuppressLint;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.MediaScannerConnectionClient;
import android.net.Uri;
import android.text.TextUtils;

@SuppressLint("HandlerLeak")
public class BaseActivity extends CommonActivity {
	
	private MediaScannerConnection mMediaScannerConnection;
	
	public interface MediaScanListener {
		void onScanCompleted();
	}
	
	public void scanMediaAndFinish(final String path, final MediaScanListener mediaScanListener) {
		if(TextUtils.isEmpty(path)) {
			return;
		}

		mMediaScannerConnection = new MediaScannerConnection(this, new MediaScannerConnectionClient() {
			@Override
			public void onMediaScannerConnected() {
				mMediaScannerConnection.scanFile(path, null);
			}

			@Override
			public void onScanCompleted(String path, Uri uri) {
				mMediaScannerConnection.disconnect();
				mMediaScannerConnection = null;

				runOnUiThread(new Runnable(){

					@Override
					public void run() {
						// TODO Auto-generated method stub
						if(mediaScanListener != null) {
							mediaScanListener.onScanCompleted();
						}
					}
				});
			}
		});
		
		mMediaScannerConnection.connect();			
	}
}