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
class OpenAdManager @Inject constructor(
    private val rewardAdManager: RewardAdManager
){

    private var appOpenAd: AppOpenAd? = null
        set(value) {
            isLoadingAd = false
            isShowingAd = false
            field = value
        }
    private var isLoadingAd = false
    private var adLastShownTime: Long = 0
    private val adDisplayInterval = if (BuildConfig.DEBUG) 0 else 0
    private var isShowingAd = false

    fun loadAd(context: Context, loadedCompleted: (Boolean) -> Unit = {}) {
        if (isLoadingAd || appOpenAd != null) {
            return
        }
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            context.getString(R.string.key_open_splash),
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    appOpenAd = ad
                    loadedCompleted(true)
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    appOpenAd = null
                    loadedCompleted(false)
                }
            }
        )
    }

    fun isAdAvailable(): Boolean {
        return  appOpenAd != null && System.currentTimeMillis() - adLastShownTime >= adDisplayInterval
    }

    fun loadAndShowAd(activity: Activity, task: () -> Unit = {}){
        if (activity is SplashActivity || activity is IapActivity){
            return
        }
        when {
            isAdAvailable() -> showAdIfAvailable(activity, task)
            else -> loadAd(activity) { loadedCompleted ->
                isShowingAd = false

                if (loadedCompleted){
                    showAdIfAvailable(activity, task)
                }
                when {
                    loadedCompleted -> showAdIfAvailable(activity, task)
                    else -> task()
                }
            }
        }
    }

    fun showAdIfAvailable(activity: Activity, task: () -> Unit = {}) {
        if (activity is SplashActivity || activity is IapActivity || rewardAdManager.isShowing()){
            return
        }
        if (isShowingAd) {
            return
        }
        appOpenAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    task()

                    appOpenAd = null
                    adLastShownTime = System.currentTimeMillis()
                    loadAd(activity)

                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    task()

                    appOpenAd = null
                    adLastShownTime = System.currentTimeMillis()
                    loadAd(activity)
                }
            }
            isShowingAd = true
            ad.show(activity)
        } ?: run {
            task()
        }
    }
}