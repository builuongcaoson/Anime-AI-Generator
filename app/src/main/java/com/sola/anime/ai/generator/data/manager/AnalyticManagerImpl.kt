package com.sola.anime.ai.generator.data.manager

import android.content.Context
import com.basic.common.extension.tryOrNull
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.common.extension.deviceId
import com.sola.anime.ai.generator.common.extension.deviceModel
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticManagerImpl @Inject constructor(
    private val context: Context,
    private val firebaseAnalytics: FirebaseAnalytics,
    private val prefs: Preferences
): AnalyticManager {

    override fun logEvent(type: AnalyticManager.TYPE) {
        tryOrNull {
            firebaseAnalytics.logEvent("version_${BuildConfig.VERSION_CODE}"){
                when {
                    prefs.isUpgraded() || prefs.getCredits() > 0f -> param(type.name, "${deviceModel()} - ${context.deviceId()} - ${prefs.purchasedOrderLastedId.get()}")
                    else -> param(type.name, "${deviceModel()} - ${context.deviceId()}")
                }
            }
        }
    }

}