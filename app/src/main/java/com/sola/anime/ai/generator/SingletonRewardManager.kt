package com.sola.anime.ai.generator

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.sola.anime.ai.generator.feature.iap.IapActivity
import com.sola.anime.ai.generator.feature.splash.SplashActivity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SingletonRewardManager @Inject constructor(){

    private var rewardAd: RewardedAd? = null
        set(value) {
            isRewarded = false
            field = value
        }
    private var isLoadingAd = false
    private var adLastShownTime: Long = 0
    private val adDisplayInterval = if (BuildConfig.DEBUG) 0 else 0
    private var isShowingAd = false
    private var isRewarded = false

    fun loadAd(context: Context, key: String, loadedCompleted: (Boolean) -> Unit = {}) {
        if (isLoadingAd || rewardAd != null) {
            return
        }

        isLoadingAd = true

        val request = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            key,
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardAd = ad
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
        return  rewardAd != null && System.currentTimeMillis() - adLastShownTime >= adDisplayInterval
    }

    fun loadAndShowAd(activity: Activity, key: String, failed: () -> Unit = {}, success: () -> Unit = {}){
        when {
            isAdAvailable() -> showAdIfAvailable(activity, key, failed, success)
            else -> loadAd(activity, key) { loadedCompleted ->
                isShowingAd = false

                if (loadedCompleted){
                    showAdIfAvailable(activity, key, failed, success)
                }
                when {
                    loadedCompleted -> showAdIfAvailable(activity, key, failed, success)
                    else -> success()
                }
            }
        }
    }

    fun showAdIfAvailable(activity: Activity, key: String, failed: () -> Unit = {}, success: () -> Unit = {}) {
        if (activity is SplashActivity || activity is IapActivity){
            return
        }

        if (isShowingAd) {
            return
        }

        rewardAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    when {
                        isRewarded -> success()
                        else -> failed()
                    }

                    rewardAd = null
                    isShowingAd = false
                    adLastShownTime = System.currentTimeMillis()
                    loadAd(activity, key)
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    when {
                        isRewarded -> success()
                        else -> failed()
                    }

                    rewardAd = null
                    isShowingAd = false
                    adLastShownTime = System.currentTimeMillis()
                    loadAd(activity, key)
                }
            }

            isShowingAd = true

            ad.show(activity) {
                isRewarded = true
            }
        } ?: run {
            failed()
        }
    }

    fun isShowing() = isShowingAd

}