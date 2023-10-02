package com.sola.anime.ai.generator.common

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.installations.FirebaseInstallations
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.GetStoreProductsCallback
import com.revenuecat.purchases.models.StoreProduct
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.common.extension.deviceId
import com.sola.anime.ai.generator.common.extension.deviceModel
import com.sola.anime.ai.generator.data.Preferences
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import timber.log.Timber
import java.util.Arrays
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

    private val skus by lazy { listOf(Constraint.Iap.SKU_LIFE_TIME, Constraint.Iap.SKU_WEEK, Constraint.Iap.SKU_YEAR, Constraint.Iap.SKU_CREDIT_1000, Constraint.Iap.SKU_CREDIT_3000, Constraint.Iap.SKU_CREDIT_5000, Constraint.Iap.SKU_CREDIT_10000) }
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

        // Step Revenuecat
        initRevenuecat()

        // Network listener
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
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

        // Init admob
        val testDeviceIds = listOf("29D59506E999419336DCBF6CE24F8F1F")
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(testDeviceIds).build()
        MobileAds.setRequestConfiguration(configuration)
        MobileAds.initialize(this) {}

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

    fun loadReviewInfo(){
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                reviewInfo = task.result
                Timber.tag("Main12345").e("Loaded Review infor")
            }
        }
    }

    private fun initRevenuecat(){
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.debugLogsEnabled = BuildConfig.DEBUG
        Purchases.configure(PurchasesConfiguration.Builder(this, Constraint.Info.REVENUECAT_KEY).build())
        Purchases.sharedInstance.setDisplayName("${deviceId()}---${deviceModel()}")
        Purchases.sharedInstance.getProducts(skus, object: GetStoreProductsCallback {
            override fun onError(error: PurchasesError) {

            }

            override fun onReceived(storeProducts: List<StoreProduct>) {

            }
        })
    }

}