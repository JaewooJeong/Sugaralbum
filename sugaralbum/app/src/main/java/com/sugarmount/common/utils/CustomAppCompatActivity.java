package com.sugarmount.common.utils;

import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.sugarmount.common.env.MvConfig;

/**
 * Created by Jaewoo on 2017-11-09.
 */
public class CustomAppCompatActivity extends AppCompatActivity implements MvConfig {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public boolean grantPermission(String[] permission) {
        if(permission.length >= 1) {
            int check = checkSelfPermission(permission[0]);
            if (check == PackageManager.PERMISSION_GRANTED) {
                log.d("Permission is granted");
                return true;
            } else {
                log.d("Permission is revoked");
                //ActivityCompat.requestPermissions(this, permission, 1);
                return false;
            }
        }

        return true;
    }

    public void requestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityCompat.requestPermissions(this, PERMISSIONS34, MY_PERMISSION_REQUEST);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, PERMISSIONS33, MY_PERMISSION_REQUEST);
        }else{
            ActivityCompat.requestPermissions(this, PERMISSIONS, MY_PERMISSION_REQUEST);
        }
    }

}
