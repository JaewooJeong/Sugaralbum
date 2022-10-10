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
import com.sugarmount.common.model.MvConfig
import com.sugarmount.common.utils.log
import com.sugarmount.sugaralbum.GlobalApplication
import com.sugarmount.sugaralbum.R
import java.util.*
import javax.inject.Inject

class AppOpenManager @Inject constructor(): LifecycleObserver {
    private var appOpenAd: AppOpenAd? = null
    private var currentActivity: Activity? = null
    private val adRequest: AdRequest by lazy { AdRequest.Builder().build() }
    private lateinit var application: Application
    private var isShowingAd = false
    private val isAdAvailable: Boolean
        get() = appOpenAd != null && wasLoadTimeLessThanNHoursAgo()
    private var loadTime: Long = 0

    /** LifecycleObserver method that shows the app open ad when the app moves to foreground. */
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        // Show the ad (if available) when the app moves to foreground.
        currentActivity?.let {
            showAdIfAvailable(it)
        }
    }

    fun initialize(application: Application){
        this.application = application

        application.registerActivityLifecycleCallbacks(object:
            Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                // An ad activity is started when an ad is showing, which could be AdActivity class from Google
                // SDK or another activity class implemented by a third party mediation partner. Updating the
                // currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
                // one that shows the ad.

                // An ad activity is started when an ad is showing, which could be AdActivity class from Google
                // SDK or another activity class implemented by a third party mediation partner. Updating the
                // currentActivity only when an ad is not showing will ensure it is not an ad activity, but the
                // one that shows the ad.
                if (!isShowingAd) {
                    currentActivity = activity
                }
//                currentActivity = activity
//                showAdIfAvailable()
                log.e("## onStart")
            }
            override fun onActivityResumed(activity: Activity) {
//                currentActivity = activity
                log.e("## onResume")
            }
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, bundle: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }
    /**
     * Request an ad
     * Have unused ad, no need to fetch another.
     */
    fun loadAd(context: Context) {
        if (isAdAvailable) {
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
                loadTime = Date().time
            }
            /**
             * Called when an app open ad has failed to load.
             * @param loadAdError the error.
             * Handle the error.
             */
            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                log.e("### onAdFailedToLoad fail")
            }
        }
        AppOpenAd.load(
            context,
            application.getString(if (MvConfig.debug) R.string.app_opening_ad_unit_id_test else R.string.app_opening_ad_unit_id),
            adRequest,
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
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
        if (!isShowingAd && isAdAvailable) {
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
                        log.e("onAdFailedToShowFullScreenContent")
                        isShowingAd = false
                        onShowAdCompleteListener.onShowAdComplete()
                    }
                    override fun onAdShowedFullScreenContent() {
                        log.e("onAdShowedFullScreenContent")
                    }
                }
                isShowingAd = true
                show(activity)
            }
        } else {
            log.e("Can not show ad.")
            onShowAdCompleteListener.onShowAdComplete()
            loadAd(activity)
        }
    }

    /**
     * Show the ad if one isn't already showing.
     *
     * @param activity the activity that shows the app open ad
     */
    fun showAdIfAvailable(activity: Activity) {
        showAdIfAvailable(
            activity
        ) {
            // Empty because the user will go back to the activity that shows the ad.
        }
    }

    private fun wasLoadTimeLessThanNHoursAgo(numHours: Long = 4): Boolean {
        val dateDifference = Date().time - loadTime
        val numMilliSecondsPerHour = 3600000
        return dateDifference < numMilliSecondsPerHour * numHours
    }
}