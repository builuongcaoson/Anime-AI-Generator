package com.sola.anime.ai.generator.data.manager

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdmobManagerImpl @Inject constructor(
    private val context: Context,
    private val analyticManager: AnalyticManager
): AdmobManager {

    companion object {
        var rewardCreate: RewardedAd? = null
        var rewardCreateAgain: RewardedAd? = null
    }

    private var isLoadingRewardCreate = false
    private var isLoadingRewardCreateAgain = false

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
                    analyticManager.logEvent(AnalyticManager.TYPE.ADMOB_CLICKED, "admob_clicked")
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
                    analyticManager.logEvent(AnalyticManager.TYPE.ADMOB_CLICKED, "admob_clicked")
                }
            }
            it.show(activity){
                isRewarded = true
            }
        } ?: run {
            failed()
        }
    }

}