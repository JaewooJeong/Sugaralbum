package com.sugarmount.sugarcamera.utils;


import java.util.ArrayList;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.kiwiple.debug.L;

public abstract class PermissionUtil {
	
	public static final int INVALID_PERMISSION_INDEX = -1;
	public static final int DO_NOT_CHECK_PERMISSION_INDEX = 0; 
	public static final int CAMERA_PERMISSION_INDEX = 1;
	public static final int STORAGE_PERMISSION_INDEX = 2;
	
	public static final String INTENT_KEY_PERMISSION = "intent_key_permission";
	public static final String INTENT_KEY_CLASS_NAME = "intent_key_class_name";
	
    public static final String [] CameraPermissions = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, 
		Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO };
    public static final String [] StoragePermissions = { Manifest.permission.READ_EXTERNAL_STORAGE };

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static final String [] CameraPermissions33 = {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_AUDIO, Manifest.permission.READ_MEDIA_VIDEO };
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static final String [] StoragePermissions33 = { Manifest.permission.READ_MEDIA_IMAGES };

/**
     * Check that all given permissions have been granted by verifying that each entry in the
     * given array is of the value {@link PackageManager#PERMISSION_GRANTED}.
     *
     * @see Context#onRequestPermissionsResult(int, String[], int[])
     */
    public static boolean verifyPermissions(int[] grantResults) {
        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static boolean hasCameraPermission(Context context){
        // Below Android M all permissions are granted at install time and are already available.
        if (!overMNC()) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Verify that all required permissions have been granted
            for (String permission : CameraPermissions33) {
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    L.d("not granted permission : " + permission);
                    return false;
                } else {
                    L.d("granted permission : " + permission);
                }
            }
        }else{
            // Verify that all required permissions have been granted
            for (String permission : CameraPermissions) {
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    L.d("not granted permission : " + permission);
                    return false;
                } else {
                    L.d("granted permission : " + permission);
                }
            }
        }
        return true;    	
    }
    
    public static String[] getCameraPermissionArray(Context context){
    	ArrayList<String> permissionList = new ArrayList<String>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (String permission : CameraPermissions33) {
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission);
                }
            }
        }else{
            for (String permission : CameraPermissions) {
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    permissionList.add(permission);
                }
            }
        }
    	String[] permissionArray = new String[permissionList.size()]; 
    	for( int i = 0 ; i < permissionList.size(); i++){
    		permissionArray[i] = permissionList.get(i); 
    	}
    	return permissionArray;  
    }
    
    public static boolean hasStoragePermission(Context context){
        // Below Android M all permissions are granted at install time and are already available.
        if (!overMNC()) {
            return true;
        }

        // Verify that all required permissions have been granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            for (String permission : StoragePermissions33) {
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    L.d("not granted permission : " + permission);
                    return false;
                }else{
                    L.d("granted permission : " + permission);
                }
            }
        }else{
            for (String permission : StoragePermissions) {
                if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    L.d("not granted permission : " + permission);
                    return false;
                }else{
                    L.d("granted permission : " + permission);
                }
            }
        }

        return true;    	
    }
    /**
     * Returns true if the Context has access to all given permissions.
     * Always returns true on platforms below M.
     *
     * @see Context#checkSelfPermission(String)
     */
    public static boolean hasSelfPermission(Context context, String[] permissions) {
        // Below Android M all permissions are granted at install time and are already available.
        if (!overMNC()) {
            return true;
        }

        // Verify that all required permissions have been granted
        for (String permission : permissions) {
            if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if the Context has access to a given permission.
     * Always returns true on platforms below M.
     *
     * @see Context#checkSelfPermission(String)
     */
    public static boolean hasSelfPermission(Context context, String permission) {
        // Below Android M all permissions are granted at install time and are already available.
        if (!overMNC()) {
            return true;
        }

        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean overMNC() {
        /*
         TODO: In the Android M Preview release, checking if the platform is M is done through
         the codename, not the version code. Once the API has been finalised, the following check
         should be used: */
        return true;
    }}
