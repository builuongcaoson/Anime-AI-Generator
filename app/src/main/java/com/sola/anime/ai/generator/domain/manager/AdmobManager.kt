package com.sola.anime.ai.generator.domain.manager

import android.app.Activity

interface AdmobManager {

    fun loadAndShowOpenAd(activity: Activity)

    fun isShowingOpenAd(): Boolean

    fun loadReward()

    fun showReward(activity: Activity, success: () -> Unit, failed: () -> Unit = {})

    fun loadFullItem()

    fun showFullItem(activity: Activity, done: () -> Unit)

    fun loadAndShowOpenSplash(activity: Activity, loaded: () -> Unit, failedOrSuccess: () -> Unit)

}