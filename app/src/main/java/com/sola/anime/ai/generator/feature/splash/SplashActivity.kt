package com.sola.anime.ai.generator.feature.splash

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.isNetworkAvailable
import com.basic.common.extension.makeToast
import com.basic.common.extension.tryOrNull
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
import com.sola.anime.ai.generator.domain.manager.UserPremiumManager
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
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
    @Inject lateinit var userPremiumManager: UserPremiumManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        Timber.tag("Main12345").e("Device model: ${getDeviceModel()}")
        Timber.tag("Main12345").e("Device id: ${getDeviceId()}")

        initView()
        initObservable()
        initData()
    }

    private fun initData() {
        // Reset credits changes
        prefs.creditsChanges.delete()
        prefs.userPurchasedChanges.delete()

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
                    syncRemoteConfig {
                        when {
                            configApp.blockedRoot && (RootUtil.isDeviceRooted() || CommonUtil.isRooted(this@SplashActivity)) -> {
                                makeToast("Your device is on our blocked list!")
                                finish()
                                return@syncRemoteConfig
                            }
                            configApp.blockDeviceIds.contains(getDeviceId()) -> {
                                makeToast("Your device is on our blocked list!")
                                finish()
                                return@syncRemoteConfig
                            }
                        }

                        syncData.execute(Unit)

                        doTask()
                    }
                }
            }
        }
    }

//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            PERMISSION_NOTIFICATION -> doTask()
//        }
//    }

    private fun syncRemoteConfig(done: () -> Unit) {
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
                    configApp.keyUpscale = tryOrNull { config.getString("key_upscale").takeIf { it.isNotEmpty() } } ?: configApp.keyUpscale
                    configApp.blockDeviceIds = tryOrNull { config.getString("blockDeviceIds").takeIf { it.isNotEmpty() }?.split(", ") } ?: configApp.blockDeviceIds
                    configApp.blockedRoot = tryOrNull { config.getBoolean("blockedRoot") } ?: configApp.blockedRoot

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
                    Timber.e("Key decrypt: ${AESEncyption.decrypt(configApp.keyDezgo)}")
                    Timber.e("Key premium decrypt: ${AESEncyption.decrypt(configApp.keyDezgoPremium)}")
                    Timber.e("Key upscale: ${configApp.keyUpscale}")
                    configApp.blockDeviceIds.forEach { value ->
                        Timber.e("Block device id: $value")
                    }
                    Timber.e("blockedRoot: ${configApp.blockedRoot}")

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
                !prefs.isUpgraded.get() && isNetworkAvailable() -> {
                    binding.textLoadingAd.text = "This action contains ads..."
                    admobManager.loadAndShowOpenSplash(this@SplashActivity
                        , loaded = { binding.viewLoadingAd.animate().alpha(0f).setDuration(250).start() }
                        , success = { task() }
                        , failed = { task() }
                    )
                }
                else -> {
                    userPremiumManager.syncUserPurchasedFromDatabase()

                    task()
                }
            }
        }
    }

    private fun initView() {
        binding.image.load(R.drawable.ic_launcher, errorRes = R.drawable.place_holder_image)
    }

}