package com.sola.anime.ai.generator

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RewardAdManager @Inject constructor(){

    private var rewardAd: RewardedAd? = null
        set(value) {
            isLoadingAd = false
            isShowingAd = false
            isRewarded = false
            field = value
        }
    private var isLoadingAd = false
    private var adLastShownTime: Long = 0
    private val adDisplayInterval = if (BuildConfig.DEBUG) 0 else 0
    private var isShowingAd = false
    private var isRewarded = false

    fun loadAd(context: Context, loadedCompleted: (Boolean) -> Unit = {}) {
        if (isLoadingAd || rewardAd != null) {
            return
        }
        isLoadingAd = true
        val request = AdRequest.Builder().build()
        RewardedAd.load(
            context,
            context.getString(R.string.key_reward_create),
            request,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardAd = ad
                    loadedCompleted(true)
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    rewardAd = null
                    loadedCompleted(false)
                }
            }
        )
    }

    fun isAdAvailable(): Boolean {
        return  rewardAd != null && System.currentTimeMillis() - adLastShownTime >= adDisplayInterval
    }

    fun loadAndShowAd(activity: Activity, failed: () -> Unit = {}, success: () -> Unit = {}){
        when {
            isAdAvailable() -> showAdIfAvailable(activity, failed, success)
            else -> loadAd(activity) { loadedCompleted ->
                if (loadedCompleted){
                    showAdIfAvailable(activity, failed, success)
                }
                when {
                    loadedCompleted -> showAdIfAvailable(activity, failed, success)
                    else -> success()
                }
            }
        }
    }

    fun showAdIfAvailable(activity: Activity, failed: () -> Unit = {}, success: () -> Unit = {}) {
        if (isShowingAd) {
            return
        }
        isRewarded = false
        rewardAd?.let { ad ->
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    when {
                        isRewarded -> success()
                        else -> failed()
                    }

                    rewardAd = null
                    adLastShownTime = System.currentTimeMillis()
                    loadAd(activity)
                }
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    when {
                        isRewarded -> success()
                        else -> failed()
                    }

                    rewardAd = null
                    adLastShownTime = System.currentTimeMillis()
                    loadAd(activity)
                }
            }
            isShowingAd = true
            ad.show(activity) { isRewarded = true }
        } ?: run {
            failed()
        }
    }

    fun isShowing() = isShowingAd

}