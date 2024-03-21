package com.sola.anime.ai.generator.data.manager

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.appopen.AppOpenAd.AppOpenAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.App
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.feature.iap.IapActivity
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
        private var openSplash: AppOpenAd? = null
    }

    private val openAdManager by lazy { OpenAdManager() }
    private var isLoadingRewardCreate = false
    private var isLoadingOpenSplash = false

    override fun loadAndShowOpenAd(activity: Activity) {
        openAdManager.loadAndShowOpenAd(activity)
    }

    override fun isShowingOpenAd() = openAdManager.isShowingAd

    override fun loadReward() {
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
                    Timber.tag("Admob").e("Error: $adError")
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    rewardCreate = ad

                    isLoadingRewardCreate = false
                }
            })
    }

    override fun showReward(activity: Activity, success: () -> Unit, failed: () -> Unit) {
        var isRewarded = false

        rewardCreate?.let {
            it.fullScreenContentCallback = object: FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    App.app.canOpenBackground = true

                    rewardCreate = null

                    when {
                        isRewarded -> success()
                        else -> failed()
                    }
                }
                override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                    App.app.canOpenBackground = true

                    rewardCreate = null

                    failed()
                    Timber.tag("Admob").e("Error: $p0")
                }

                override fun onAdClicked() {
                    analyticManager.logEvent(AnalyticManager.TYPE.ADMOB_CLICKED)
                }
            }
            App.app.canOpenBackground = false
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
                    Timber.tag("Admob").e("Error: $p0")
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

    override fun loadFullItem() {

    }

    override fun showFullItem(activity: Activity, done: () -> Unit) {

    }

}

private class OpenAdManager {

    private var appOpenAd: AppOpenAd? = null
    private var isLoadingAd = false
    private var loadTime: Long = 0
    var isShowingAd = false

    private fun loadAd(context: Context, loadedCompleted: (Boolean) -> Unit = {}) {
        if (isLoadingAd || isAdAvailable()) {
            loadedCompleted(true)
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
                    loadTime = Date().time
                    loadedCompleted(true)
                }
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    isLoadingAd = false
                    loadedCompleted(false)
                }
            }
        )
    }

    private fun wasLoadTimeLessThanNSecondsAgo(second: Long): Boolean {
        val dateDifference: Long = Date().time - loadTime
        val numMilliSecondsPerSecond: Long = 1000
        return dateDifference < numMilliSecondsPerSecond * second
    }

    private fun isAdAvailable(): Boolean {
        return wasLoadTimeLessThanNSecondsAgo(0)
    }

    fun loadAndShowOpenAd(activity: Activity){
        if (activity is SplashActivity || activity is IapActivity){
            return
        }

        loadAd(activity) { loadedCompleted ->
            if (loadedCompleted){
                showAdIfAvailable(activity) {}
            }
        }
    }

    private fun showAdIfAvailable(activity: Activity, task: () -> Unit) {
        if (isShowingAd) {
            return
        }

        if (!isAdAvailable()) {
            loadAd(activity)
            return
        }

        appOpenAd?.fullScreenContentCallback =
            object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    appOpenAd = null
                    isShowingAd = false
                    task()
                }

                /** Called when fullscreen content failed to show. */
                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    appOpenAd = null
                    isShowingAd = false
                    task()
                }
            }

        isShowingAd = true
        appOpenAd?.show(activity)
    }
}
