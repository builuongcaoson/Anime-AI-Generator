package com.sola.anime.ai.generator.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.util.Log
import com.google.android.play.core.ktx.requestReview
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.getCustomerInfoWith
import com.sola.anime.ai.generator.data.Preferences
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.text.SimpleDateFormat
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

    // For review
    val manager by lazy { ReviewManagerFactory.create(this) }
    var reviewInfo: ReviewInfo? = null

    // For network
    val subjectNetworkChanges: Subject<Boolean> = BehaviorSubject.createDefault(true)

    override fun onCreate() {
        super.onCreate()

        // Setup Timber
        Timber.plant(Timber.DebugTree())

        // RxThrowable
        RxJavaPlugins.setErrorHandler { e ->
            Timber.e("Error: $e")
        }

        // Network listener
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            connectivityManager?.registerDefaultNetworkCallback(object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    subjectNetworkChanges.onNext(true)
                }

                override fun onLost(network: Network) {
                    subjectNetworkChanges.onNext(false)
                }
            })
        }
    }

    fun loadReviewManager(){
        when {
            !prefs.isRated.get() -> {
                val request = manager.requestReviewFlow()
                request.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        reviewInfo = task.result
                    }
                }
            }
        }
    }

}