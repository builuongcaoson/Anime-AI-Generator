package com.sola.anime.ai.generator

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.sola.anime.ai.generator.feature.iap.IapActivity
import com.sola.anime.ai.generator.feature.splash.SplashActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SingletonOpenManager @Inject constructor(
    private val rewardManager: SingletonRewardManager
){

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var adLastShownTime: Long = 0
    private val adDisplayInterval = if (BuildConfig.DEBUG) 0 else 0
    private var isShowingAd = false

    fun loadAd(context: Context, key: String, loadedCompleted: (Boolean) -> Unit = {}) {
        if (isLoadingAd || appOpenAd != null) {
            return
        }

        isLoadingAd = true

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            key,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    isLoadingAd = false
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
        return  appOpenAd != null && System.currentTimeMillis() - adLastShownTime >= adDisplayInterval
    }

    fun loadAndShowAd(activity: Activity, key: String, task: () -> Unit = {}){
        if (activity is SplashActivity || activity is IapActivity){
            return
        }

        when {
            isAdAvailable() -> showAdIfAvailable(activity, key, task)
            else -> loadAd(activity, key) { loadedCompleted ->
                isShowingAd = false

                if (loadedCompleted){
                    showAdIfAvailable(activity, key, task)
                }
                when {
                    loadedCompleted -> showAdIfAvailable(activity, key, task)
                    else -> task()
                }
            }
        }
    }

    fun showAdIfAvailable(activity: Activity, key: String, task: () -> Unit = {}) {
        if (activity is SplashActivity || activity is IapActivity || rewardManager.isShowing()){
            return
        }

        if (isShowingAd) {
            return
        }

        appOpenAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    adLastShownTime = System.currentTimeMillis()
                    loadAd(activity, key)

                    task()
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                    adLastShownTime = System.currentTimeMillis()
                    loadAd(activity, key)

                    task()
                }
            }

            isShowingAd = true

            ad.show(activity)
        } ?: run {
            task()
        }
    }
}