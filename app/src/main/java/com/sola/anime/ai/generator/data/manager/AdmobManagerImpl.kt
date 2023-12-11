package com.sola.anime.ai.generator.data.manager

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.feature.splash.SplashActivity
import timber.log.Timber
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdmobManagerImpl @Inject constructor(
    private val context: Context,
    private val analyticManager: AnalyticManager
): AdmobManager {

    companion object {
        private var rewardCreate: RewardedAd? = null
        private var rewardCreateAgain: RewardedAd? = null
        private var rewardUpscale: RewardedAd? = null
        private var openSplash: AppOpenAd? = null
    }

    private val openAdManager by lazy { OpenAdManager() }
    private val interstitialManager by lazy { InterstitialManager() }
    private var isLoadingRewardCreate = false
    private var isLoadingRewardCreateAgain = false
    private var isLoadingRewardUpscale = false
    private var isLoadingOpenSplash = false

    override fun loadAndShowOpenAd(activity: Activity) {
        openAdManager.loadAndShowOpenAd(activity)
    }

    override fun isShowingOpenAd() = openAdManager.isShowingAd

    override fun loadAndShowFullItem(activity: Activity, task: () -> Unit) {
        interstitialManager.loadAndShowAd(activity, task = task)
    }

    override fun isFullItemAvailable() = interstitialManager.isAdAvailable()

    override fun loadRewardCreate() {
        when {
            isLoadingRewardCreate || rewardCreate != null -> return
        }

        isLoadingRewardCreate = true

        RewardedAd.load(context,
            context.getString(R.string.key_reward_create),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardCreate = null

                    isLoadingRewardCreate = false
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardCreate = ad

                    isLoadingRewardCreate = false
                }
            })
    }

    override fun showRewardCreate(activity: Activity, success: () -> Unit, failed: () -> Unit) {
        var isRewarded = false

        rewardCreate?.let {
            it.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardCreate = null

                    when {
                        isRewarded -> success()
                        else -> failed()
                    }
                }
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    rewardCreate = null

                    failed()
                }

                override fun onAdClicked() {
                    analyticManager.logEvent(AnalyticManager.TYPE.ADMOB_CLICKED)
                }
            }
            it.show(activity){
                isRewarded = true
            }
        } ?: run {
            failed()
        }
    }

    override fun loadRewardCreateAgain() {
        when {
            isLoadingRewardCreateAgain || rewardCreateAgain != null -> return
        }

        isLoadingRewardCreateAgain = true

        RewardedAd.load(context,
            context.getString(R.string.key_reward_create_again),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardCreateAgain = null

                    isLoadingRewardCreateAgain = false
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardCreateAgain = ad

                    isLoadingRewardCreateAgain = false
                }
            })
    }

    override fun showRewardCreateAgain(activity: Activity, success: () -> Unit, failed: () -> Unit) {
        var isRewarded = false

        rewardCreateAgain?.let {
            it.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardCreateAgain = null

                    when {
                        isRewarded -> success()
                        else -> failed()
                    }
                }
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    rewardCreateAgain = null

                    failed()
                }

                override fun onAdClicked() {
                    analyticManager.logEvent(AnalyticManager.TYPE.ADMOB_CLICKED)
                }
            }
            it.show(activity){
                isRewarded = true
            }
        } ?: run {
            failed()
        }
    }

    override fun loadRewardUpscale() {
        when {
            isLoadingRewardUpscale || rewardUpscale != null -> return
        }

        isLoadingRewardUpscale = true

        RewardedAd.load(context,
            context.getString(R.string.key_reward_upscale),
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    rewardUpscale = null

                    isLoadingRewardUpscale = false
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardUpscale = ad

                    isLoadingRewardUpscale = false
                }
            })
    }

    override fun showRewardUpscale(activity: Activity, success: () -> Unit, failed: () -> Unit) {
        var isRewarded = false

        rewardUpscale?.let {
            it.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardUpscale = null

                    when {
                        isRewarded -> success()
                        else -> failed()
                    }
                }
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    rewardUpscale = null

                    failed()
                }

                override fun onAdClicked() {
                    analyticManager.logEvent(AnalyticManager.TYPE.ADMOB_CLICKED)
                }
            }
            it.show(activity){
                isRewarded = true
            }
        } ?: run {
            failed()
        }
    }

    override fun loadAndShowOpenSplash(
        activity: Activity,
        loaded: () -> Unit,
        failedOrSuccess: () -> Unit
    ) {
        when {
            isLoadingOpenSplash || openSplash != null -> return
        }

        isLoadingOpenSplash = true

        val taskFailed = {
            isLoadingOpenSplash = false
            openSplash = null

            failedOrSuccess()
        }

        val taskSuccess = {
            isLoadingOpenSplash = false
            openSplash = null

            failedOrSuccess()
        }

        AppOpenAd.load(
            activity,
            context.getString(R.string.key_open_splash),
            AdRequest.Builder().build(),
            AppOpenAd.APP_OPEN_AD_ORIENTATION_PORTRAIT,
            object: AppOpenAdLoadCallback() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    taskFailed()
                }

                override fun onAdLoaded(p0: AppOpenAd) {
                    loaded()

                    openSplash = p0
                    openSplash?.let {
                        it.fullScreenContentCallback = object: FullScreenContentCallback() {
                            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                                taskFailed()
                            }

                            override fun onAdDismissedFullScreenContent() {
                                taskSuccess()
                            }

                            override fun onAdClicked() {
                                analyticManager.logEvent(AnalyticManager.TYPE.ADMOB_CLICKED)
                            }
                        }
                        it.show(activity)
                    } ?: run {
                        taskFailed()
                    }
                }
            })
    }

}

class InterstitialManager {

    private var fullItem: InterstitialAd? = null
    private var isLoadingAd = false
    private var adLastShownTime: Long = 0
    private val adDisplayInterval = if (BuildConfig.DEBUG) 0 else 15000 // 0 seconds for debug - 15 seconds for release
    private var isShowingAd = false

    private fun loadAd(activity: Activity, loadedCompleted: (Boolean) -> Unit = {}) {
        if (isLoadingAd) {
            return
        }

        isLoadingAd = true

        InterstitialAd.load(activity, activity.getString(R.string.key_full_item), AdRequest.Builder().build(), object: InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(p0: LoadAdError) {
                fullItem = null
                isLoadingAd = false
                loadedCompleted(false)
            }
            override fun onAdLoaded(p0: InterstitialAd) {
                fullItem = p0
                isLoadingAd = false
                loadedCompleted(true)
            }
        })
    }

    fun loadAndShowAd(activity: Activity, task: () -> Unit){
        when {
            isAdAvailable() -> loadAd(activity) { loadedCompleted ->
                isShowingAd = false

                when {
                    loadedCompleted -> showAdIfAvailable(activity) { task() }
                    else -> task()
                }
            }
            else -> task()
        }
    }

    fun isAdAvailable(): Boolean {
        return System.currentTimeMillis() - adLastShownTime >= adDisplayInterval
    }

    private fun showAdIfAvailable(activity: Activity, task: () -> Unit) {
        if (isShowingAd) {
            return
        }

        fullItem?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                fullItem = null
                isShowingAd = false
                adLastShownTime = System.currentTimeMillis()

                task()
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                fullItem = null
                isShowingAd = false
                adLastShownTime = System.currentTimeMillis()

                task()
            }
        }

        isShowingAd = true
        fullItem?.show(activity)
    }
}

private class OpenAdManager {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var adLastShownTime: Long = 0
    private val adDisplayInterval = if (BuildConfig.DEBUG) 0 else 0 // 0 seconds for debug - 15 seconds for release
    var isShowingAd = false

    private fun loadAd(context: Context, loadedCompleted: (Boolean) -> Unit = {}) {
        if (isLoadingAd) {
            return
        }

        isLoadingAd = true

        val request = AdRequest.Builder().build()
        AppOpenAd.load(
            context,
            context.getString(R.string.key_open_splash),
            request,
            object : AppOpenAdLoadCallback() {
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

    private fun isAdAvailable(): Boolean {
        return System.currentTimeMillis() - adLastShownTime >= adDisplayInterval
    }

    fun loadAndShowOpenAd(activity: Activity){
        if (activity is SplashActivity){
            return
        }

        Timber.e("loadAndShowOpenAd")

        when {
            isAdAvailable() -> loadAd(activity) { loadedCompleted ->
                isShowingAd = false

                if (loadedCompleted){
                    showAdIfAvailable(activity) {}
                }

                Timber.e("loadAndShowOpenAd: $loadedCompleted")
            }
        }
    }

    private fun showAdIfAvailable(activity: Activity, task: () -> Unit) {
        if (isShowingAd) {
            return
        }

        appOpenAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    adLastShownTime = System.currentTimeMillis()

                    task()
                }

                /** Called when fullscreen content failed to show. */
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                    adLastShownTime = System.currentTimeMillis()

                    task()
                }
            }

        isShowingAd = true
        appOpenAd?.show(activity)
    }
}
