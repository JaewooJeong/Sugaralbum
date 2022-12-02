package com.sugarmount.sugaralbum

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.DisplayMetrics
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.google.android.gms.ads.*
import com.sugarmount.common.utils.CustomAppCompatActivity
import kotlinx.android.synthetic.main.activity_finish.*


class ActivityFinish : CustomAppCompatActivity() {
    private var videoUri: String = ""
    private var adView: AdView? = null
    private var initialLayoutComplete = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        setContentView(R.layout.activity_finish)
        initAds()
        getIntentData()
        initToolbar()
    }

    private fun initToolbar() {
        imageView1.setOnClickListener {
            finish() // close this activity as oppose to navigating up
        }
        lottieAnimationView.setOnClickListener {
            gotoVideo()
        }
        relativeLayout2.setOnClickListener {
            gotoVideo()
        }
    }

    private fun gotoVideo() {
        intent = Intent(Intent.ACTION_VIEW, Uri.parse(videoUri))
        intent.setDataAndType(Uri.parse(videoUri), "video/mp4")
        startActivityForResult(intent, MY_VIDEO_REQUEST)
    }

    // override function
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == MY_VIDEO_REQUEST) {
            finish()
        }
    }

    private fun getIntentData() {
        val intent = intent
        if (intent != null) {
            val uri = intent.getStringExtra(EXTRA_URI_INFO)
            videoUri = if(uri is String)
                uri
            else
                ""
        }else{

        }
    }

    private fun initAds() {
        /** 적응형 배너 광고  */
        // 배너 광고 호출
        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        // Since we're loading the banner based on the adContainerView size, we need to wait until this
        // view is laid out before we can get the width.
        ad_view_container.viewTreeObserver.addOnGlobalLayoutListener(
            OnGlobalLayoutListener {
                if (!initialLayoutComplete) {
                    initialLayoutComplete = true
                    loadBanner(getAdSize())
                }
            })
    }

    private fun getAdSize(): AdSize {
        // Step 2 - Determine the screen width (less decorations) to use for the ad width.
        val display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)
        val widthPixels = outMetrics.widthPixels.toFloat()
        val density = outMetrics.density
        val adWidth = (widthPixels / density).toInt()

        // Step 3 - Get adaptive ad size and return for setting on the ad view.
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
    }

    private fun loadBanner(adSize: AdSize) {
        adView = AdView(this)
        adView!!.adUnitId = applicationContext.getString(if (debug) R.string.banner_ad_unit_id_test else R.string.banner_ad_unit_id)
        ad_view_container.addView(adView)
        adView!!.setAdSize(adSize)
        val adRequest = AdRequest.Builder().build()
        adView!!.loadAd(adRequest)
    }
}
