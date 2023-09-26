package com.sola.anime.ai.generator.data.manager

import android.app.Activity
import android.content.Context
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdmobManagerImpl @Inject constructor(
    private val context: Context,
    private val analyticManager: AnalyticManager
): AdmobManager {

    override fun loadRewardCreate() {

    }

    override fun showRewardCreate(activity: Activity, success: () -> Unit, failed: () -> Unit) {
        success()
    }

    override fun loadRewardCreateAgain() {

    }

    override fun showRewardCreateAgain(activity: Activity, success: () -> Unit, failed: () -> Unit) {
        success()
    }

    override fun loadRewardUpscale() {

    }

    override fun showRewardUpscale(activity: Activity, success: () -> Unit, failed: () -> Unit) {
        success()
    }

    override fun loadAndShowOpenSplash(
        activity: Activity,
        loaded: () -> Unit,
        failedOrSuccess: () -> Unit
    ) {
        loaded()
        failedOrSuccess()
    }

}