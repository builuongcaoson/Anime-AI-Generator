package com.sola.anime.ai.generator

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.sola.anime.ai.generator.common.App

class FullManager {

    private var full: InterstitialAd? = null
    private var isLoadingAd = false
    private var adLastShownTime: Long = 0
    private var adDisplayInterval = if (BuildConfig.DEBUG) 0L else 0L // Millisecond
    private var isShowingAd = false

    fun loadAd(context: Context, key: String, adDisplayInterval: Long = 0, loadedCompleted: (Boolean) -> Unit = {}) {
        this.adDisplayInterval = adDisplayInterval

        if (full != null) {
            loadedCompleted(true)
            return
        }

        if (isLoadingAd) {
            return
        }

        isLoadingAd = true

        val request = AdRequest.Builder().build()
        InterstitialAd.load(
            context,
            key,
            request,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    full = ad
                    isLoadingAd = false
                    ad.setOnPaidEventListener { adValue ->
                        // Extract the impression-level ad revenue data.
                        val valueMicros = adValue.valueMicros
                        val currencyCode = adValue.currencyCode
                        val extras = ad.responseInfo?.responseExtras

                        val loadedAdapterResponseInfo = ad.responseInfo?.loadedAdapterResponseInfo
                        val adSourceName = loadedAdapterResponseInfo?.adSourceName

                        val mediationABTestName = extras?.getString("mediation_ab_test_name")?.takeIf { it.isNotEmpty() } ?: "null"
                        val mediationABTestVariant = extras?.getString("mediation_ab_test_variant")?.takeIf { it.isNotEmpty() } ?: "null"

                        AnalyticManager.eventTrackingAdRevenue(valueMicros, "inter", mediationABTestName, mediationABTestVariant, currencyCode, adSourceName)
                    }
                    loadedCompleted(true)
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    loadedCompleted(false)
                }
            }
        )
    }

    fun isAdAvailable(): Boolean {
        return  full != null && System.currentTimeMillis() - adLastShownTime >= adDisplayInterval
    }

    fun showAdIfAvailable(activity: Activity, key: String, task: () -> Unit = {}) {
        if (isShowingAd) {
            return
        }

        full?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    App.app.canOpenBackground = true
                    full = null
                    isShowingAd = false
                    adLastShownTime = System.currentTimeMillis()
                    loadAd(activity, key, adDisplayInterval)

                    task()
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    App.app.canOpenBackground = true
                    full = null
                    isShowingAd = false
                    adLastShownTime = System.currentTimeMillis()
                    loadAd(activity, key, adDisplayInterval)

                    task()
                }
            }

            isShowingAd = true
            App.app.canOpenBackground = false
            ad.show(activity)
        } ?: run {
            task()
        }
    }
}