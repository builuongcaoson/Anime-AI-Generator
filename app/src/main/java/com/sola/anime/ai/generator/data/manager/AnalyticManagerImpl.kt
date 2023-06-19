package com.sola.anime.ai.generator.data.manager

import android.annotation.SuppressLint
import android.content.Context
import com.basic.common.extension.tryOrNull
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.common.extension.getDeviceId
import com.sola.anime.ai.generator.common.extension.getDeviceModel
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticManagerImpl @Inject constructor(
    private val context: Context,
    private val firebaseAnalytics: FirebaseAnalytics
): AnalyticManager {

    @SuppressLint("SimpleDateFormat")
    override fun logEvent(type: AnalyticManager.TYPE) {
        tryOrNull {
            firebaseAnalytics.logEvent("version_${BuildConfig.VERSION_CODE}"){
                param(type.name, "${getDeviceModel()} - ${context.getDeviceId()}")
            }
        }
    }

}