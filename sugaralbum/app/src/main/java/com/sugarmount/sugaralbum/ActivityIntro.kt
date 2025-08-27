package com.sugarmount.sugaralbum

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import com.sugarmount.common.env.MvConfig
import com.sugarmount.common.env.MvConfig.INTRO_TIME
import com.sugarmount.common.utils.CustomAppCompatActivity
import com.sugarmount.common.utils.log
import java.util.*
import kotlin.concurrent.schedule

class ActivityIntro : CustomAppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        if(GlobalApplication.isRefresh()) {
            Timer().schedule(INTRO_TIME) {
                runOnUiThread {
                    // 6. go to main
                    goIntent(ActivityMain::class.java)
                    GlobalApplication.setRefresh(false)
                    cancel()
                }
            }

        }else {
            val intent = intent
            if (intent.getBooleanExtra(MvConfig.EXTRA_PERMISSION, false)) {
                requestPermissions()
            } else {
                checkPanelType()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MvConfig.MY_PERMISSION_REQUEST -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted
                    log.d("permission was granted")
                    // create a daemon thread
                    checkPanelType()
                } else {
                    // permission denied
                    log.d("permission denied")
                    goIntent(ActivityEmpty::class.java)
                }
            }
        }
    }

    private fun checkPanelType(){
        Timer().schedule(INTRO_TIME) {
            runOnUiThread {
                val application = application as? GlobalApplication

                if(application == null) {
                    // 6. go to main
                    goIntent(ActivityMain::class.java)
                    cancel()
                    return@runOnUiThread
                }

                // Show the app open ad.
                application.showAdIfAvailable(
                    this@ActivityIntro,
                    GlobalApplication.OnShowAdCompleteListener { // 6. go to main
                        goIntent(ActivityMain::class.java)
                        cancel()
                    })

            }
        }
    }

    private fun goIntent(cls: Class<*>){
        val intent = Intent(applicationContext, cls)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
    }
}
