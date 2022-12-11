package com.sugarmount.common.utils;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.sugarmount.common.model.MvConfig;

/**
 * Created by Jaewoo on 2017-11-09.
 */
public class CustomAppCompatActivity extends AppCompatActivity implements MvConfig {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public boolean grantPermission(String[] permission) {
        int check = checkSelfPermission(permission[0]);
        if (check == PackageManager.PERMISSION_GRANTED) {
            log.d("Permission is granted");
            return true;
        } else {
            log.d("Permission is revoked");
            return false;
        }
    }

    public void requestPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this, PERMISSIONS33, MY_PERMISSION_REQUEST);
        }else{
            ActivityCompat.requestPermissions(this, PERMISSIONS, MY_PERMISSION_REQUEST);
        }
    }

}
