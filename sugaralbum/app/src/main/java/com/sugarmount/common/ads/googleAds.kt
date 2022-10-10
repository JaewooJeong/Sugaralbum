package com.sugarmount.common.ads

import android.content.Context
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
//import com.google.android.gms.ads.InterstitialAd
import com.sugarmount.common.model.MvConfig
import com.sugarmount.common.utils.log
import com.sugarmount.sugaralbum.R


class googleAds {
    companion object {
        private var mInterstitialAd: InterstitialAd? = null

        fun loadAd(context: Context) {
            // 전면
            val adRequest = AdRequest.Builder().build()
            InterstitialAd.load(
                context,
                context.getString(if(MvConfig.debug) R.string.front_ad_unit_id_test else  R.string.front_ad_unit_id),
                adRequest,
                object : InterstitialAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        log.d(adError?.message)
                        mInterstitialAd = null
                        val error = "domain: ${adError.domain}, code: ${adError.code}, " +
                                "message: ${adError.message}"
                    }

                    override fun onAdLoaded(interstitialAd: InterstitialAd) {
                        log.d("Ad was loaded.")
                        mInterstitialAd = interstitialAd
                    }
                }
            )
        }

    }
}