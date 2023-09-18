package com.sola.anime.ai.generator.common

import android.app.Application
import com.google.firebase.installations.FirebaseInstallations
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.common.extension.deviceId
import com.sola.anime.ai.generator.common.extension.getDeviceModel
import com.sola.anime.ai.generator.data.Preferences
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.plugins.RxJavaPlugins
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application() {

    companion object {
        lateinit var app: App
    }

    init {
        app = this
    }

    @Inject lateinit var prefs: Preferences

    override fun onCreate() {
        super.onCreate()

        // Setup Timber
        Timber.plant(Timber.DebugTree())

        // RxThrowable
        RxJavaPlugins.setErrorHandler { e ->
            Timber.e("Error: $e")
        }

        // Step Revenuecat
        initRevenuecat()

        // Register firebase token
        initFirebaseCloudMessaging()
    }

    private fun initFirebaseCloudMessaging() {
        FirebaseInstallations.getInstance().getToken(false)
            .addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Timber.w( "Fetching FCM registration token failed")
                    return@addOnCompleteListener
                }
                Timber.d("Token FCM: " + task.result)
            }
    }

    private fun initRevenuecat(){
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.debugLogsEnabled = BuildConfig.DEBUG
        Purchases.configure(PurchasesConfiguration.Builder(this, Constraint.Info.REVENUECAT_KEY).build())
        Purchases.sharedInstance.setDisplayName("${deviceId()}---${getDeviceModel()}")
    }

}