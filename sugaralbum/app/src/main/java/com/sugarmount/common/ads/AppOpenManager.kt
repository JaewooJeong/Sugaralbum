package com.sugarmount.common.ads

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.sugarmount.common.env.MvConfig
import com.sugarmount.common.utils.log
import com.sugarmount.sugaralbum.GlobalApplication
import com.sugarmount.sugaralbum.R
import java.util.*
import javax.inject.Inject

class AppOpenManager {
    private var appOpenAd: AppOpenAd? = null
    lateinit var application: Application

    private var isLoadingAd = false
    var isShowingAd = false
    private val isAdAvailable: Boolean
        get() = appOpenAd != null && wasLoadTimeLessThanNHoursAgo()
    private var loadTime: Long = 0

    /**
     * Request an ad
     * Have unused ad, no need to fetch another.
     */
    fun loadAd(context: Context) {
        if (isLoadingAd || isAdAvailable) {
            log.e("### isAdAvailable true.")
            return
        }
        /**
         * Called when an app open ad has loaded.
         * @param ad the loaded app open ad.
         */
        /**
         * Called when an app open ad has failed to load.
         * @param loadAdError the error.
         * Handle the error.
         */
        val loadCallback: AppOpenAd.AppOpenAdLoadCallback = object : AppOpenAd.AppOpenAdLoadCallback() {
            /**
             * Called when an app open ad has loaded.
             * @param ad the loaded app open ad.
             */
            override fun onAdLoaded(ad: AppOpenAd) {
                log.e("### onAdLoaded")
                appOpenAd = ad
                isLoadingAd = false
                loadTime = Date().time

//                Toast.makeText(context, "onAdLoaded", Toast.LENGTH_SHORT).show()
            }
            /**
             * Called when an app open ad has failed to load.
             * @param loadAdError the error.
             * Handle the error.
             */
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                isLoadingAd = false
                log.e("### onAdFailedToLoad fail")
            }
        }

        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            application.getString(if (MvConfig.debug) R.string.app_opening_ad_unit_id_test else R.string.app_opening_ad_unit_id),
            request,
            loadCallback
        )
    }
    /**
     * Only show ad if there is not already an app open ad currently showing
     * and an ad is available.
     */
    fun showAdIfAvailable(
        activity: Activity,
        onShowAdCompleteListener: GlobalApplication.OnShowAdCompleteListener
    ) {

        // If the app open ad is already showing, do not show the ad again.
        if (isShowingAd) {
            log.d("The app open ad is already showing.")
            onShowAdCompleteListener.onShowAdComplete()
            return
        }

        // If the app open ad is not available yet, invoke the callback then load the ad.
        if (!isAdAvailable) {
            log.d("The app open ad is not ready yet.")
            onShowAdCompleteListener.onShowAdComplete()
            loadAd(activity)
            return
        }

        log.e("Will show ad.")
        appOpenAd!!.apply {
            fullScreenContentCallback = object : FullScreenContentCallback() {
                /**
                 * Set the reference to null so isAdAvailable() returns false.
                 */
                override fun onAdDismissedFullScreenContent() {
                    log.e("onAdDismissedFullScreenContent")
                    appOpenAd = null
                    isShowingAd = false
                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    log.e("onAdFailedToShowFullScreenContent: %s", adError.message)
                    appOpenAd = null
                    isShowingAd = false
                    onShowAdCompleteListener.onShowAdComplete()
                    loadAd(activity)
                }

                override fun onAdShowedFullScreenContent() {
                    log.e("onAdShowedFullScreenContent")
                }
            }
            isShowingAd = true
            appOpenAd!!.show(activity)
        }
    }

    /**
     * Show the ad if one isn't already showing.
     *
     * @param activity the activity that shows the app open ad
     */
    fun showAdIfAvailable(activity: Activity) {
        showAdIfAvailable(
            activity,
            object : GlobalApplication.OnShowAdCompleteListener {
                override fun onShowAdComplete() {
                    // Empty because the user will go back to the activity that shows the ad.
                }
            }
        )
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long = 4): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }
}