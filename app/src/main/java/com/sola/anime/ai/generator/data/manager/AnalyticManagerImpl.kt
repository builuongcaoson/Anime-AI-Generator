package com.sola.anime.ai.generator.data.manager

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticManagerImpl @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
): AnalyticManager {

    override fun logEvent(type: AnalyticManager.TYPE, event: String) {
        firebaseAnalytics.logEvent(BuildConfig.VERSION_NAME){
            param(type.name, event)
        }
    }

}