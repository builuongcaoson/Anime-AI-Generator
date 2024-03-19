package com.sola.anime.ai.generator

import androidx.core.os.bundleOf
import com.google.firebase.analytics.FirebaseAnalytics
import com.sola.anime.ai.generator.common.App
import timber.log.Timber
import java.util.Locale

object AnalyticManager {

    fun eventTrackingAdRevenue(valueMicros: Long, adFormat: String, abTestName: String, abTestVariant: String, currencyCode: String, adSourceName: String?) {
        Timber.e("valueMicros: " + valueMicros + " --- Revenue: " + valueMicros / 1000000.0 + " --- adFormat: " + adFormat + " --- abTestName: " + abTestName + " --- abTestVariant: " + abTestVariant + " --- currencyCode: " + currencyCode + " --- adSourceName: " + adSourceName)

        FirebaseAnalytics.getInstance(App.app).logEvent("ad_revenue", bundleOf(
            "valuemicros" to valueMicros,
            "value_micros" to valueMicros,
            "ad_format" to adFormat,
            "ab_test_name" to abTestName,
            "ab_test_variant" to abTestVariant
        ))
    }

    fun eventGDPR(success: Boolean){
        Timber.e("Grant: $success")

        FirebaseAnalytics.getInstance(App.app).logEvent("GDPR", bundleOf(
            "status" to if (success) "consent" else "not_consent",
            "country" to Locale.getDefault().country
        ))
    }

}