package com.sugarmount.sugarcamera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.widget.Toast;

import com.kiwiple.debug.L;
import com.sugarmount.sugaralbum.R;
import com.sugarmount.sugarcamera.utils.PermissionUtil;

public class PermissionGrantActivity extends Activity {
    
    private String mClassName; 
    
    public static String getActivityName(){
    	return "com.sugarmount.sugarcamera.PermissionGrantActivity"; 
    }

	@Override
	public void onCreate(Bundle savedInstanceState,
			PersistableBundle persistentState) {
		// TODO Auto-generated method stub
		getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT)); 
		super.onCreate(savedInstanceState, persistentState);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		int index = getIntent().getIntExtra(PermissionUtil.INTENT_KEY_PERMISSION, 0);
		mClassName = getIntent().getStringExtra(PermissionUtil.INTENT_KEY_CLASS_NAME); 
		if(index == PermissionUtil.CAMERA_PERMISSION_INDEX){
			if(!PermissionUtil.hasCameraPermission(this)){
				requestPermissions(PermissionUtil.getCameraPermissionArray(this),PermissionUtil.CAMERA_PERMISSION_INDEX);
			}
		}else if(index == PermissionUtil.STORAGE_PERMISSION_INDEX){
			if(!PermissionUtil.hasStoragePermission(this)){
				requestPermissions(PermissionUtil.StoragePermissions, PermissionUtil.STORAGE_PERMISSION_INDEX);
			}
		}
		super.onResume();
	}
	
	@Override
	public void onRequestPermissionsResult(int requestCode,
			String[] permissions, int[] grantResults) {

		boolean bReturn = true; 
        if (requestCode == PermissionUtil.CAMERA_PERMISSION_INDEX || requestCode == PermissionUtil.STORAGE_PERMISSION_INDEX) {
            // Received permission result for camera permission.
        	for(int i = 0; i<grantResults.length; i++){
        		L.d("permission : " + permissions[i] +", result : " + grantResults[i]);
        		
        		if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
        			bReturn = false; 
        		}
        	}

        }
        if(bReturn){
        	Intent intent = getIntent(); 
        	L.d("return class name : " + mClassName); 
        	intent.setClassName(this, mClassName);
        	startActivity(intent);
        } else {
            Toast.makeText(getBaseContext(), R.string.permission_not_granted, Toast.LENGTH_SHORT).show();
        }
        finish(); 
	}
	

}
