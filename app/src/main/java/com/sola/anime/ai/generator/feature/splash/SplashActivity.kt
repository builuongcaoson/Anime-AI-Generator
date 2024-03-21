package com.sola.anime.ai.generator.feature.splash

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.isNetworkAvailable
import com.basic.common.extension.makeToast
import com.basic.common.extension.tryOrNull
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.GoogleMobileAdsConsentManager
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.SingletonOpenManager
import com.sola.anime.ai.generator.common.App
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.common.util.CommonUtil
import com.sola.anime.ai.generator.common.util.RootUtil
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivitySplashBinding
import com.sola.anime.ai.generator.domain.interactor.SyncData
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.domain.manager.PermissionManager
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : LsActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    @Inject lateinit var prefs: Preferences
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var networkDialog: NetworkDialog
    @Inject lateinit var syncData: SyncData
    @Inject lateinit var admobManager: AdmobManager
    @Inject lateinit var permissionManager: PermissionManager
    @Inject lateinit var singletonOpenManager: SingletonOpenManager
    @Inject lateinit var googleMobileAdsConsentManager: GoogleMobileAdsConsentManager

    private val isMobileAdsInitializeCalled = AtomicBoolean(false)
    private val subjectCreatedSplash: Subject<Unit> = PublishSubject.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Timber.tag("Main12345").e("Device model: ${deviceModel()}")
        Timber.tag("Main12345").e("Device id: ${deviceId()}")
        if (Build.VERSION.SDK_INT >= 34) {
            Timber.tag("Main12345").e("Device id 2: $deviceId")
        }
        Timber.tag("Main12345").e("Lasted time formatted created artwork: ${prefs.latestTimeCreatedArtwork.get().getTimeFormatted()}")
        Timber.tag("Main12345").e("Lasted time is Today: ${prefs.latestTimeCreatedArtwork.get().isToday()}")

        App.app.isStartedSplash = true

        subjectCreatedSplash
            .debounce(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                Observable
                    .timer(5, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .autoDispose(scope())
                    .subscribe {
                        if (googleMobileAdsConsentManager.isShowConsentForm) return@subscribe
                        initData()
                    }

                googleMobileAdsConsentManager.gatherConsent(this) { consentError ->
                    if (consentError != null) {
                        Timber.w(String.format("%s: %s", consentError.errorCode, consentError.message))
                    }

                    if (googleMobileAdsConsentManager.canRequestAds) {
                        initializeMobileAdsSdk()
                    }

                    initData()
                }

                if (googleMobileAdsConsentManager.canRequestAds) {
                    initializeMobileAdsSdk()
                }
            }

        subjectCreatedSplash.onNext(Unit)

        initView()
        initObservable()
    }

    private fun initializeMobileAdsSdk() {
        if (isMobileAdsInitializeCalled.getAndSet(true)) {
            return
        }

        if (BuildConfig.DEBUG){
            val configuration = RequestConfiguration.Builder().setTestDeviceIds(arrayListOf("2919AB1DDAF7ECFC2ECF83A842FA2EA6")).build()
            MobileAds.setRequestConfiguration(configuration)
        }

        MobileAds.initialize(this) { initializationStatus ->
            val statusMap = initializationStatus.adapterStatusMap
            for (adapterClass in statusMap.keys) {
                val status = statusMap[adapterClass]
                Timber.d(String.format("Adapter name: %s, Description: %s, Latency: %d", adapterClass, status?.description, status?.latency))
            }
        }
    }

    private fun initData() {
        // Reset credits changes
        prefs.creditsChanges.delete()
        prefs.userPurchasedChanges.delete()

        lifecycleScope.launch(Dispatchers.Main) {
            binding.viewLoadingAd.animate().alpha(1f).setDuration(250L).start()
            delay(500)
            when {
                !isNetworkAvailable() -> networkDialog.show(this@SplashActivity){
                    networkDialog.dismiss()

                    initData()
                }
                else -> syncRemoteConfig()
            }
        }
    }

    private fun doTaskAfterSyncFirebaseRemoteConfig(){
        when {
            configApp.blockedRoot && (RootUtil.isDeviceRooted() || CommonUtil.isRooted(this@SplashActivity)) -> {
                makeToast("Your device is on our blocked list!")
                finish()
                return
            }
            configApp.blockDeviceIds.contains(deviceId()) -> {
                makeToast("Your device is on our blocked list!")
                finish()
                return
            }
            configApp.blockDeviceModels.contains(deviceModel()) -> {
                makeToast("Your device is on our blocked list!")
                finish()
                return
            }
            configApp.blockVersions.contains(BuildConfig.VERSION_CODE.toString()) -> {
                makeToast("Your device is on our blocked list!")
                finish()
                return
            }
        }

        syncData.execute(Unit)

        doTask()
    }

    private fun syncRemoteConfig() {
        Firebase.remoteConfig.let { config ->
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }
            config.setConfigSettingsAsync(configSettings)
            config
                .fetchAndActivate()
                .addOnSuccessListener {
                    configApp.stepDefault = tryOrNull { config.getString("step_default").takeIf { it.isNotEmpty() } } ?: configApp.stepDefault
                    configApp.stepPremium = tryOrNull { config.getString("step_premium").takeIf { it.isNotEmpty() } } ?: configApp.stepPremium
                    configApp.maxNumberGenerateFree = when {
//                        BuildConfig.DEBUG -> 0L
                        else -> tryOrNull { config.getLong("max_number_generate_free_4") } ?: configApp.maxNumberGenerateFree
                    }
                    configApp.maxNumberGenerateReward = when {
//                        BuildConfig.DEBUG -> 3L
                        else -> tryOrNull { config.getLong("max_number_generate_reward") } ?: configApp.maxNumberGenerateReward
                    }
                    configApp.maxNumberGeneratePremium = when {
//                        BuildConfig.DEBUG -> 5L
                        else -> tryOrNull { config.getLong("max_number_generate_premium") } ?: configApp.maxNumberGeneratePremium
                    }
                    configApp.feature = tryOrNull { config.getString("feature").takeIf { it.isNotEmpty() } } ?: configApp.feature
                    configApp.version = tryOrNull { config.getLong("version") } ?: configApp.version
                    configApp.versionExplore = tryOrNull { config.getLong("version_explore") } ?: configApp.versionExplore
                    configApp.versionLoRA = tryOrNull { config.getLong("version_loRA") } ?: configApp.versionLoRA
                    configApp.versionIap = tryOrNull { config.getLong("version_iap") } ?: configApp.versionIap
                    configApp.versionProcess = tryOrNull { config.getLong("version_process") } ?: configApp.versionProcess
                    configApp.versionStyle = tryOrNull { config.getLong("version_style") } ?: configApp.versionStyle
                    configApp.versionModel = tryOrNull { config.getLong("version_model") } ?: configApp.versionModel
                    configApp.keyDezgo = tryOrNull { config.getString("key_4").takeIf { it.isNotEmpty() } } ?: configApp.keyDezgo
                    configApp.keyDezgoPremium = tryOrNull { config.getString("key_premium_4").takeIf { it.isNotEmpty() } } ?: configApp.keyDezgoPremium
                    configApp.keyUpscale = tryOrNull { config.getString("key_upscale").takeIf { it.isNotEmpty() } } ?: configApp.keyUpscale
                    configApp.blockDeviceIds = tryOrNull { config.getString("blockDeviceIds").takeIf { it.isNotEmpty() }?.split(", ") } ?: configApp.blockDeviceIds
                    configApp.blockDeviceModels = tryOrNull { config.getString("blockDeviceModels").takeIf { it.isNotEmpty() }?.split(", ") } ?: configApp.blockDeviceModels
                    configApp.blockVersions = tryOrNull { config.getString("blockVersions").takeIf { it.isNotEmpty() }?.split(", ") } ?: configApp.blockVersions
                    configApp.blockedRoot = tryOrNull { config.getBoolean("blockedRoot") } ?: configApp.blockedRoot
                    configApp.fullScreenChangesDisplayInterval = tryOrNull { config.getLong("fullScreenChangesDisplayInterval") } ?: configApp.fullScreenChangesDisplayInterval
                    configApp.isFullScreenChanges = tryOrNull { config.getBoolean("isShowFullScreenChanges") } ?: configApp.isFullScreenChanges
                    configApp.isOpenSplashOrBackground = tryOrNull { config.getBoolean("isShowOpenAd_3") } ?: configApp.isOpenSplashOrBackground
                    configApp.scriptIap = tryOrNull { config.getString("script_iap").takeIf { it.isNotEmpty() } } ?: configApp.scriptIap

                    Timber.e("stepDefault: ${configApp.stepDefault}")
                    Timber.e("stepPremium: ${configApp.stepPremium}")
                    Timber.e("maxNumberGenerateFree: ${configApp.maxNumberGenerateFree}")
                    Timber.e("maxNumberGenerateReward: ${configApp.maxNumberGenerateReward}")
                    Timber.e("maxNumberGeneratePremium: ${configApp.maxNumberGeneratePremium}")
                    Timber.e("feature: ${configApp.feature}")
                    Timber.e("version: ${configApp.version}")
                    Timber.e("versionExplore: ${configApp.versionExplore} --- ${prefs.versionExplore.get()}")
                    Timber.e("versionIap: ${configApp.versionIap} --- ${prefs.versionIap.get()}")
                    Timber.e("versionProcess: ${configApp.versionProcess} --- ${prefs.versionProcess.get()}")
                    Timber.e("versionStyle: ${configApp.versionStyle} --- ${prefs.versionStyle.get()}")
                    Timber.e("versionModel: ${configApp.versionModel} --- ${prefs.versionModel.get()}")
                    Timber.e("Key dezgo: ${configApp.keyDezgo}")
                    Timber.e("Key dezgo premium: ${configApp.keyDezgoPremium}")
                    Timber.e("Key upscale: ${configApp.keyUpscale}")
                    Timber.e("Block device ids: ${configApp.blockDeviceIds.joinToString { it }}")
                    Timber.e("Block device models: ${configApp.blockDeviceModels.joinToString { it }}")
                    Timber.e("Block versions: ${configApp.blockVersions.joinToString { it }}")
                    Timber.e("blockedRoot: ${configApp.blockedRoot}")
                    Timber.e("isShowFullScreenChanges: ${configApp.isFullScreenChanges}")
                    Timber.e("isShowOpenAd: ${configApp.isOpenSplashOrBackground}")
                    Timber.e("scriptIap: ${configApp.scriptIap}")

                    doTaskAfterSyncFirebaseRemoteConfig()
                }
                .addOnFailureListener {
                    doTaskAfterSyncFirebaseRemoteConfig()
                }
        }
    }

    private fun initObservable() {

    }

    private fun doTask(){
        val task = {
            lifecycleScope.launch(Dispatchers.Main) {
                App.app.isStartedSplash = false

                when {
                    prefs.isFirstTime.get() -> {
                        startFirst()
                        finish()
                    }
                    !prefs.isUpgraded.get() -> {
                        startIap(isKill = false)
                        finish()
                    }
                    else -> startMain(viewLoadingAds = binding.viewLoadingAds, isFull = false) { finish() }
                }
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            resetNumberCreatedArtworkIfOtherToday()

            when {
                !prefs.isUpgraded.get() && isNetworkAvailable() && configApp.isFullScreenChanges -> {
                    binding.textLoadingAd.text = getString(R.string.this_action_contains_ads)

                    App.app.fullScreenChanges.loadAd(this@SplashActivity, getString(R.string.key_full_screen_changes), configApp.fullScreenChangesDisplayInterval * 1000L) { isSuccess ->
                        when {
                            isSuccess -> {
                                lifecycleScope.launch(Dispatchers.Main) {
                                    binding.viewLoadingAd.animate().alpha(0f).setDuration(250).start()
                                    delay(250L)
                                    App.app.fullScreenChanges.showAdIfAvailable(this@SplashActivity, getString(R.string.key_full_screen_changes)) {
                                        task()
                                    }
                                }
                            }
                            else -> task()
                        }
                    }
                }
                !prefs.isUpgraded.get() && isNetworkAvailable() && configApp.isOpenSplashOrBackground -> {
                    binding.textLoadingAd.text = getString(R.string.this_action_contains_ads)

                    admobManager.loadAndShowOpenSplash(this@SplashActivity
                        , loaded = { binding.viewLoadingAd.animate().alpha(0f).setDuration(250).start() }
                        , failedOrSuccess = { task() }
                    )
                }
                else -> task()
            }
        }
    }

    private fun resetNumberCreatedArtworkIfOtherToday() {
        // Reset number created in days if different days
        when {
            !prefs.isUpgraded.get() || prefs.latestTimeCreatedArtwork.get().isToday() -> {}
            else -> {
                prefs.numberCreatedArtwork.delete()
                prefs.latestTimeCreatedArtwork.delete()
            }
        }
    }

    private fun initView() {
        binding.image.load(R.drawable.ic_launcher)
    }

}