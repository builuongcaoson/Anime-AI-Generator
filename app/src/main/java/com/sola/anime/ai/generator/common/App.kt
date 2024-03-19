package com.sola.anime.ai.generator.common

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.basic.common.extension.isNetworkAvailable
import com.basic.common.extension.tryOrNull
import com.google.android.gms.ads.MobileAds
import com.google.firebase.installations.FirebaseInstallations
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.PurchasesError
import com.revenuecat.purchases.interfaces.GetStoreProductsCallback
import com.revenuecat.purchases.models.StoreProduct
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.FullManager
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.SingletonOpenManager
import com.sola.anime.ai.generator.common.extension.deviceId
import com.sola.anime.ai.generator.common.extension.deviceModel
import com.sola.anime.ai.generator.data.Preferences
import dagger.hilt.android.HiltAndroidApp
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Application.ActivityLifecycleCallbacks, LifecycleObserver {

    companion object {
        lateinit var app: App
    }

    init {
        app = this
    }

    @Inject lateinit var prefs: Preferences
    @Inject lateinit var singletonOpenManager: SingletonOpenManager
    @Inject lateinit var configApp: ConfigApp

    private val skus by lazy { listOf(Constraint.Iap.SKU_LIFE_TIME, Constraint.Iap.SKU_WEEK, Constraint.Iap.SKU_YEAR, Constraint.Iap.SKU_CREDIT_1000, Constraint.Iap.SKU_CREDIT_3000, Constraint.Iap.SKU_CREDIT_5000, Constraint.Iap.SKU_CREDIT_10000) }
    private var currentActivity: Activity? = null

    // For network
    val subjectNetworkChanges: Subject<Boolean> = BehaviorSubject.createDefault(true)

    // For admob
    val fullScreenChanges by lazy { FullManager() }
    var isStartedSplash = false

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

        // Register firebase token
        initFirebaseCloudMessaging()

        // For open ads
        registerActivityLifecycleCallbacks(this)
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
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
        Purchases.sharedInstance.setDisplayName("${deviceId()}---${deviceModel()}")
        Purchases.sharedInstance.getProducts(skus, object: GetStoreProductsCallback {
            override fun onError(error: PurchasesError) {

            }

            override fun onReceived(storeProducts: List<StoreProduct>) {

            }
        })
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onMoveToForeground() {
        when {
            !prefs.isUpgraded.get() && isNetworkAvailable() && configApp.isOpenSplashOrBackground && !isStartedSplash -> currentActivity?.takeIf { singletonOpenManager.isAdAvailable() }?.let { activity ->
                tryOrNull { Handler(Looper.getMainLooper()).postDelayed({ singletonOpenManager.showAdIfAvailable(activity, getString(R.string.key_open_splash)) }, 250L) }
            }
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {}

    override fun onActivityPaused(activity: Activity) {}

    override fun onActivityStopped(activity: Activity) {}

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}

    override fun onActivityDestroyed(activity: Activity) {}

}