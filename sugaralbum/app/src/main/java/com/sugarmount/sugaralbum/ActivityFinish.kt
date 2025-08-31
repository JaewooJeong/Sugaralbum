package com.sugarmount.sugaralbum

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import com.google.android.gms.ads.*
import com.sugarmount.common.utils.CustomAppCompatActivity
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.airbnb.lottie.LottieAnimationView
import com.sugarmount.common.env.MvConfig.EXTRA_URI_INFO
import com.sugarmount.common.env.MvConfig.MY_VIDEO_REQUEST
import com.sugarmount.common.env.MvConfig.debug


class ActivityFinish : CustomAppCompatActivity() {
    private var videoUri: String = ""
    private var adView: AdView? = null
    private var initialLayoutComplete = false
    
    private lateinit var imageView1: ImageView
    private lateinit var lottieAnimationView: LottieAnimationView
    private lateinit var relativeLayout2: RelativeLayout
    private lateinit var ad_view_container: FrameLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())

        setContentView(R.layout.activity_finish)
        setInsetView(this.findViewById(R.id.coordinatorLayout))
        initViews()
        initAds()
        getIntentData()
        initToolbar()
    }
    
    private fun initViews() {
        imageView1 = findViewById(R.id.imageView1)
        lottieAnimationView = findViewById(R.id.lottieAnimationView)
        relativeLayout2 = findViewById(R.id.relativeLayout2)
        ad_view_container = findViewById(R.id.ad_view_container)
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

    override fun onStart() {
        super.onStart()

        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY

    }
}
