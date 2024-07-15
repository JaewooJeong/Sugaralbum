package com.sugarmount.sugarcamera;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration;
import com.kiwiple.debug.L;
import com.kiwiple.multimedia.canvas.ICanvasUser;
import com.kiwiple.multimedia.preview.PreviewManager;
import com.sugarmount.common.ads.GoogleAds;
import com.sugarmount.common.env.MvConfig;
import com.sugarmount.sugarcamera.utils.PermissionUtil;
import com.sugarmount.sugarcamera.utils.ViewCompat;

import java.util.Arrays;
import java.util.List;

public class CommonActivity extends FragmentActivity {

	private boolean mHasPermission = false;
	private boolean mHasCameraPermission = false;
	private boolean mHasStoragePermission = false;
	private boolean mMoveToIndexActivity = false;
	private int mNeedPermission = PermissionUtil.INVALID_PERMISSION_INDEX;


	@Override
	protected void onCreate(Bundle bundle) {
		if (!ICanvasUser.environment.isInitialized())
			ICanvasUser.environment.initialize(getApplicationContext());

		// 광고 init
		GoogleAds.Companion.createAds(getApplicationContext());

		// Initialize the Mobile Ads SDK.
		MobileAds.initialize(
				this,
				status -> {});

		if(MvConfig.debug) {
			List<String> testDeviceIds = Arrays.asList("1AAA21F530BFD426F7E5EB8B127D4796");
			RequestConfiguration configuration =
					new RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build();
			MobileAds.setRequestConfiguration(configuration);
		}

		PreviewManager.getInstance(this).setAudioFade(true);

		if(mNeedPermission != PermissionUtil.DO_NOT_CHECK_PERMISSION_INDEX){
			if(bundle != null){
				checkMoveIndexActivity(bundle);
				if(mMoveToIndexActivity){
					/*
					setHasPermission(false); 
					Intent indexIntent = new Intent(CommonActivity.this, StoryTest.class);
					indexIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(indexIntent);
					*/
					super.onCreate(bundle);
					return;
				}
			}

			switch (mNeedPermission) {
				case PermissionUtil.STORAGE_PERMISSION_INDEX:
					if(!PermissionUtil.hasStoragePermission(this)){
						setHasPermission(false);
						Intent permissionIntent = getIntent();
						permissionIntent.setClassName(this, PermissionGrantActivity.getActivityName());
						permissionIntent.putExtra(PermissionUtil.INTENT_KEY_CLASS_NAME, this.getClass().getName());
						permissionIntent.putExtra(PermissionUtil.INTENT_KEY_PERMISSION, PermissionUtil.STORAGE_PERMISSION_INDEX);
						permissionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(permissionIntent);
					}else{
						setHasPermission(true);
					}
					break;
				case PermissionUtil.CAMERA_PERMISSION_INDEX:
					if(!PermissionUtil.hasCameraPermission(this)){
						setHasPermission(false);
						Intent permissionIntent = getIntent();
						permissionIntent.setClassName(this, PermissionGrantActivity.getActivityName());
						permissionIntent.putExtra(PermissionUtil.INTENT_KEY_CLASS_NAME, this.getClass().getName());
						permissionIntent.putExtra(PermissionUtil.INTENT_KEY_PERMISSION, PermissionUtil.CAMERA_PERMISSION_INDEX);
						permissionIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
						startActivity(permissionIntent);
					}else{
						setHasPermission(true);
					}
					break;
			}
		}

		super.onCreate(bundle);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

		if(PermissionUtil.hasStoragePermission(this)){
			mHasStoragePermission = true;
		}else{
			mHasStoragePermission = false;
		}

		if(PermissionUtil.hasCameraPermission(this)){
			mHasCameraPermission = true;
		}else{
			mHasCameraPermission = false;
		}
		outState.putBoolean(PermissionUtil.CAMERA_PERMISSION_INDEX+"", mHasCameraPermission);
		outState.putBoolean(PermissionUtil.STORAGE_PERMISSION_INDEX+"", mHasStoragePermission);
		L.d("Save Permission, storage : " + mHasStoragePermission +", camera : " + mHasCameraPermission);

		super.onSaveInstanceState(outState);
	}

	private void checkMoveIndexActivity(Bundle bundle){

		mHasStoragePermission = bundle.getBoolean(PermissionUtil.STORAGE_PERMISSION_INDEX+"");
		mHasCameraPermission = bundle.getBoolean(PermissionUtil.CAMERA_PERMISSION_INDEX+"");
		L.d("restore Permission, storage : " + mHasStoragePermission +", camera : " + mHasCameraPermission);

		if(mHasStoragePermission){
			if(!PermissionUtil.hasStoragePermission(this)){
				mMoveToIndexActivity = true;
			}
		}

		if(mHasCameraPermission){
			if(!PermissionUtil.hasCameraPermission(this)){
				mMoveToIndexActivity = true;
			}
		}
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState,
									   PersistableBundle persistentState) {
		// TODO Auto-generated method stub
		if(mHasStoragePermission){
			if(!PermissionUtil.hasStoragePermission(this)){
				mMoveToIndexActivity = true;
			}
		}

		if(mHasCameraPermission){
			if(!PermissionUtil.hasCameraPermission(this)){
				mMoveToIndexActivity = true;
			}
		}
		super.onRestoreInstanceState(savedInstanceState, persistentState);
	}

	protected void setNeedPermission(int needPermission){
		this.mNeedPermission = needPermission;
	}

	protected void setBackgroundImage(ViewGroup bgLayout){
		ViewCompat.setBackground(bgLayout, getResources().getDrawable(PublicVariable.getBackgroundImageID()));
	}

	protected void setBackgroundImage(ImageView bgImage){
		bgImage.setImageResource(PublicVariable.getBackgroundImageID());
	}

	public boolean hasPermission() {
		return mHasPermission;
	}

	public void setHasPermission(boolean hasPermission) {
		this.mHasPermission = hasPermission;
	}

	public boolean getMoveToIndexActivity(){
		return mMoveToIndexActivity;
	}
}
