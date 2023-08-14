package com.sola.anime.ai.generator.feature.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Base64
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.isNetworkAvailable
import com.basic.common.extension.makeToast
import com.basic.common.extension.tryOrNull
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.App
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.common.util.AESEncyption
import com.sola.anime.ai.generator.common.util.CommonUtil
import com.sola.anime.ai.generator.common.util.RootUtil
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.*
import com.sola.anime.ai.generator.databinding.ActivitySplashBinding
import com.sola.anime.ai.generator.domain.interactor.SyncData
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.domain.manager.PermissionManager
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject


@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : LsActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    companion object {
        private const val PERMISSION_NOTIFICATION = 1
    }

    @Inject lateinit var prefs: Preferences
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var networkDialog: NetworkDialog
    @Inject lateinit var syncData: SyncData
    @Inject lateinit var serverApiRepo: ServerApiRepository
    @Inject lateinit var admobManager: AdmobManager
    @Inject lateinit var permissionManager: PermissionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Timber.tag("Main12345").e("Device model: ${getDeviceModel()}")
        Timber.tag("Main12345").e("Device id: ${getDeviceId()}")
//        val encrypt = AESEncyption.encrypt("DEZGO-EB7294CF008D43E746840EF0A88000892E435F3E1D3381B555BB9A76E5CE1FBC0F942263") ?: ""
//        Timber.tag("Main12345").e("Key: $encrypt")
//        Timber.tag("Main12345").e("Key 2: ${AESEncyption.decrypt("EQ0ZHqdX8RfMxK0MzqhP3knekWDlOhpfmoneWFnW81CPM7vfBxsxgOhX5gozRasooSMzd+fcOAcDO/cS+9T4091L4cE8PbbHkaCrDPbPqLw=")}")

        initReviewManager()
        initView()
        initObservable()
        initData()
    }

    private fun initReviewManager() {
        App.app.loadReviewInfo()
    }

    private fun syncUserPurchased() {
        Purchases.sharedInstance.getCustomerInfoWith { customerInfo ->
            val isActive = customerInfo.entitlements["premium"]?.isActive ?: false
            Timber.tag("Main12345").e("##### SPLASH #####")
            Timber.tag("Main12345").e("Is upgraded: ${prefs.isUpgraded.get()}")
            Timber.tag("Main12345").e("Is active: $isActive")

            if (isActive){
                lifecycleScope.launch(Dispatchers.Main) {
                    serverApiRepo.syncUser { userPremium ->
                        userPremium?.let {
                            if (userPremium.timeExpired == Constraint.Iap.SKU_LIFE_TIME){
                                prefs.isUpgraded.set(true)
                                prefs.timeExpiredPremium.set(-2)
                                return@syncUser
                            }

                            customerInfo
                                .latestExpirationDate
                                ?.takeIf { it.time > System.currentTimeMillis() }
                                ?.let { expiredDate ->
                                    prefs.isUpgraded.set(true)
                                    prefs.timeExpiredPremium.set(expiredDate.time)
                                }
                        }
                    }
                }
            }
        }
    }

    private fun initData() {
        // Reset credits changes
        prefs.creditsChanges.delete()

        // Reset number created in days if different days
        when {
            prefs.latestTimeCreatedArtwork.isSet && prefs.latestTimeCreatedArtwork.get().isToday() -> {}
            prefs.latestTimeCreatedArtwork.isSet && prefs.latestTimeCreatedArtwork.get().isOlderThanYesterday() -> {
                prefs.isUpgraded.delete()
                prefs.timeExpiredPremium.delete()
            }
            else -> {
                prefs.numberCreatedArtwork.delete()
                prefs.latestTimeCreatedArtwork.delete()
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            delay(500)
            when {
                !isNetworkAvailable() -> networkDialog.show(this@SplashActivity){
                    networkDialog.dismiss()

                    initData()
                }
                else -> {
//                    val integrityManager = IntegrityManagerFactory.create(applicationContext)
//                    val integrityTokenResponse = integrityManager.requestIntegrityToken(
//                            IntegrityTokenRequest.builder()
//                                .setCloudProjectNumber(561110823355)
//                                .setNonce(Base64.encodeToString(generateRandomNonce(), Base64.URL_SAFE or Base64.NO_WRAP))
//                                .build())
//                    val integrityToken = integrityTokenResponse.await().token()
//                    Timber.tag("Main12345").e("Integrity Token: $integrityToken")

                    when {
                        RootUtil.isDeviceRooted() || CommonUtil.isRooted(this@SplashActivity) -> {
                            makeToast("Your device is on our blocked list!")
                            finish()
                        }
                        else ->  syncRemoteConfig {
                            when {
                                configApp.blockDeviceIds.contains(getDeviceId()) -> {
                                    makeToast("Your device is on our blocked list!")
                                    finish()
                                    return@syncRemoteConfig
                                }
                            }

                            lifecycleScope.launch(Dispatchers.Main) {
                                when {
                                    !prefs.isSyncUserPurchased.get() && Purchases.isConfigured -> {
                                        syncUserPurchased()
                                        delay(500)
                                    }
                                }
                                syncData.execute(Unit)

                                when {
                                    !permissionManager.hasPermissionNotification() -> permissionManager.requestPermissionNotification(this@SplashActivity, PERMISSION_NOTIFICATION)
                                    else -> doTask()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_NOTIFICATION -> doTask()
        }
    }

    private fun syncRemoteConfig(done: () -> Unit) {
        Firebase.remoteConfig.let { config ->
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }
            config.setConfigSettingsAsync(configSettings)
            config
                .fetchAndActivate()
                .addOnSuccessListener {
                    configApp.scriptOpenSplash = tryOrNull { config.getLong("scriptOpenSplash") } ?: configApp.scriptOpenSplash
                    configApp.stepDefault = tryOrNull { config.getString("step_default").takeIf { it.isNotEmpty() } } ?: configApp.stepDefault
                    configApp.stepPremium = tryOrNull { config.getString("step_premium").takeIf { it.isNotEmpty() } } ?: configApp.stepPremium
                    configApp.maxNumberGenerateFree = tryOrNull { config.getLong("max_number_generate_free") } ?: configApp.maxNumberGenerateFree
                    configApp.maxNumberGeneratePremium = tryOrNull { config.getLong("max_number_generate_premium") } ?: configApp.maxNumberGeneratePremium
                    configApp.feature = tryOrNull { config.getString("feature").takeIf { it.isNotEmpty() } } ?: configApp.feature
                    configApp.version = tryOrNull { config.getLong("version") } ?: configApp.version
                    configApp.versionExplore = tryOrNull { config.getLong("version_explore") } ?: configApp.versionExplore
                    configApp.versionLoRA = tryOrNull { config.getLong("version_loRA") } ?: configApp.versionLoRA
                    configApp.versionIap = tryOrNull { config.getLong("version_iap") } ?: configApp.versionIap
                    configApp.versionProcess = tryOrNull { config.getLong("version_process") } ?: configApp.versionProcess
                    configApp.versionStyle = tryOrNull { config.getLong("version_style") } ?: configApp.versionStyle
                    configApp.versionModel = tryOrNull { config.getLong("version_model") } ?: configApp.versionModel
                    configApp.keyDezgo = tryOrNull { config.getString("key_dezgo").takeIf { it.isNotEmpty() } } ?: configApp.keyDezgo
                    configApp.keyDezgoPremium = tryOrNull { config.getString("key_dezgo_premium").takeIf { it.isNotEmpty() } } ?: configApp.keyDezgoPremium
                    configApp.blockDeviceIds = tryOrNull { config.getString("blockDeviceIds").takeIf { it.isNotEmpty() }?.split(", ") } ?: configApp.blockDeviceIds

                    Timber.e("scriptOpenSplash: ${configApp.scriptOpenSplash}")
                    Timber.e("stepDefault: ${configApp.stepDefault}")
                    Timber.e("stepPremium: ${configApp.stepPremium}")
                    Timber.e("maxNumberGenerateFree: ${configApp.maxNumberGenerateFree}")
                    Timber.e("maxNumberGeneratePremium: ${configApp.maxNumberGeneratePremium}")
                    Timber.e("feature: ${configApp.feature}")
                    Timber.e("version: ${configApp.version}")
                    Timber.e("versionExplore: ${configApp.versionExplore} --- ${prefs.versionExplore.get()}")
                    Timber.e("versionIap: ${configApp.versionIap} --- ${prefs.versionIap.get()}")
                    Timber.e("versionProcess: ${configApp.versionProcess} --- ${prefs.versionProcess.get()}")
                    Timber.e("versionStyle: ${configApp.versionStyle} --- ${prefs.versionStyle.get()}")
                    Timber.e("versionModel: ${configApp.versionModel} --- ${prefs.versionModel.get()}")
                    Timber.e("key_dezgo: ${configApp.keyDezgo}")
                    Timber.e("key_dezgo decrypt: ${AESEncyption.decrypt(configApp.keyDezgo)}")
                    Timber.e("key_dezgo premium decrypt: ${AESEncyption.decrypt(configApp.keyDezgoPremium)}")
                    configApp.blockDeviceIds.forEach { value ->
                        Timber.e("Block device id: $value")
                    }

                    done()
                }
                .addOnFailureListener {
                    done()
                }
        }
    }

    private fun initObservable() {

    }

    private fun doTask(){
        val task = {
            lifecycleScope.launch(Dispatchers.Main) {
                when {
                    prefs.isFirstTime.get() -> startFirst()
                    !prefs.isUpgraded.get() -> startIap(isKill = false)
                    else -> startMain()
                }

                finish()
            }
        }

        lifecycleScope.launch(Dispatchers.Main) {
            when {
                !prefs.isUpgraded.get() && configApp.scriptOpenSplash == 1L -> {
                    binding.textLoadingAd.text = "This action contains ads..."
                    admobManager.loadAndShowOpenSplash(this@SplashActivity
                        , loaded = { binding.viewLoadingAd.animate().alpha(0f).setDuration(250).start() }
                        , success = { task() }
                        , failed = { task() })
                }
                else -> {
                    task()
                }
            }
        }
    }

    private fun initView() {
        Glide.with(this)
            .load(R.drawable.ic_launcher)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.image)
    }

}