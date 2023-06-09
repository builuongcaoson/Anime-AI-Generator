package com.sola.anime.ai.generator.data.manager

import android.annotation.SuppressLint
import com.basic.common.extension.tryOrNull
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AnalyticManagerImpl @Inject constructor(
    private val firebaseAnalytics: FirebaseAnalytics
): AnalyticManager {

    @SuppressLint("SimpleDateFormat")
    override fun logEvent(type: AnalyticManager.TYPE) {
        tryOrNull {
            val calendar = Calendar.getInstance().apply {
                timeZone = TimeZone.getTimeZone("GMT")
            }
            firebaseAnalytics.logEvent("version_${BuildConfig.VERSION_CODE}"){
                param(type.name, SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(calendar.time))
            }
        }
    }

}