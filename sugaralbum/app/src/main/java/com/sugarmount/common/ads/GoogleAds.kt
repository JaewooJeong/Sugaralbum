package com.sugarmount.common.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.sugarmount.common.env.MvConfig
import com.sugarmount.common.utils.log
import com.sugarmount.sugaralbum.R

class GoogleAds {
    companion object {
        private var mInterstitialAd: InterstitialAd? = null

        fun createAds(context: Context) {
            // ADS SDK
            MobileAds.initialize(context)
            loadInterstitialAd(context)
        }

        fun loadInterstitialAd(context: Context) {
            // 전면
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                context.getString(if(MvConfig.debug) R.string.front_ad_unit_id_test else  R.string.front_ad_unit_id),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        mInterstitialAd = null
//                        val error = "domain: ${adError.domain}, code: ${adError.code}, message: ${adError.message}"
                        log.d(adError.message)
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        log.d("Ad was loaded.")
                        mInterstitialAd = interstitialAd
                    }
                }
            )
        }

        fun showInterstitialAd(activity: Activity) {
            if(mInterstitialAd != null) {
                mInterstitialAd?.show(activity)
            } else {
                log.e("The interstitial ad wasn't ready yet.")
            }
        }

    }
}