package com.sola.anime.ai.generator.domain.manager

import android.app.Activity

interface AdmobManager {

    fun loadRewardCreate()

    fun showRewardCreate(activity: Activity, success: () -> Unit, failed: () -> Unit = {})

    fun loadRewardCreateAgain()

    fun showRewardCreateAgain(activity: Activity, success: () -> Unit, failed: () -> Unit = {})

    fun loadRewardUpscale()

    fun showRewardUpscale(activity: Activity, success: () -> Unit, failed: () -> Unit = {})

    fun loadAndShowOpenSplash(activity: Activity, loaded: () -> Unit, failedOrSuccess: () -> Unit)

}