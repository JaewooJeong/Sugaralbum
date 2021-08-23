package com.sugarmount.common.ads

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
//import com.google.android.gms.ads.InterstitialAd
import com.sugarmount.common.model.MvConfig
import com.sugarmount.common.utils.log
import com.sugarmount.sugaralbum.R


class googleAds {
    companion object {
//        private lateinit var mInterstitialAd: InterstitialAd

        fun loadAds(context: Context) {
//            // 전면
//            mInterstitialAd = InterstitialAd(context)
//            mInterstitialAd.adUnitId = context.getString(if(MvConfig.debug) R.string.front_ad_unit_id_test else  R.string.front_ad_unit_id)
//            mInterstitialAd.adListener = object : AdListener() {
//                override fun onAdClosed() {
//                    log.e("onAdClosed")
//                    mInterstitialAd.loadAd(AdRequest.Builder().build())
//                }
//            }
//            mInterstitialAd.loadAd(AdRequest.Builder().build())
        }

        fun showAds() {
//            if (mInterstitialAd.isLoaded) {
//                mInterstitialAd.show()
//            } else {
//                log.e("%s", "The interstitial wasn't loaded yet.")
//            }
        }
    }
}