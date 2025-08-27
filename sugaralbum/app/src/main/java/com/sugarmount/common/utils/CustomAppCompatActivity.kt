package com.sugarmount.common.utils

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.sugarmount.common.env.MvConfig
import com.sugarmount.sugaralbum.R
import androidx.activity.enableEdgeToEdge
/**
 * Created by Jaewoo on 2017-11-09.
 */
open class CustomAppCompatActivity : AppCompatActivity(), MvConfig {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        // 시스템 다크 모드 설정에 따라 상태바 스타일 자동 설정
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        val isDarkMode = Utils.isDarkModeEnabled(this)
        insetsController.isAppearanceLightStatusBars = !isDarkMode
        insetsController.isAppearanceLightNavigationBars = !isDarkMode
    }

    fun setInsetView(view: View) {
        ViewCompat.setOnApplyWindowInsetsListener(view) { v, windowInsets ->
            val systemBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars()).top
            val statusBarsInsets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top

            view.setPadding(0, statusBarsInsets, 0, 0)
            if(Utils.isDarkModeEnabled(this))
                view.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.main1))
            else
                view.setBackgroundColor(ContextCompat.getColor(applicationContext, R.color.main4))

            windowInsets
        }
    }

    fun grantPermission(permission: Array<String?>): Boolean {
        if (permission.size >= 1) {
            val check = checkSelfPermission(permission[0]!!)
            if (check == PackageManager.PERMISSION_GRANTED) {
                log.d("Permission is granted")
                return true
            } else {
                log.d("Permission is revoked")
                //ActivityCompat.requestPermissions(this, permission, 1);
                return false
            }
        }

        return true
    }

    fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            ActivityCompat.requestPermissions(
                this,
                MvConfig.PERMISSIONS34,
                MvConfig.MY_PERMISSION_REQUEST
            )
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(
                this,
                MvConfig.PERMISSIONS33,
                MvConfig.MY_PERMISSION_REQUEST
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                MvConfig.PERMISSIONS,
                MvConfig.MY_PERMISSION_REQUEST
            )
        }
    }
}
