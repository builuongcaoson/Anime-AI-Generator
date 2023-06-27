package com.sola.anime.ai.generator.feature.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.provider.Settings
import android.text.format.DateUtils
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.isNetworkAvailable
import com.basic.common.extension.tryOrNull
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.getCustomerInfoWith
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.App
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.getDeviceId
import com.sola.anime.ai.generator.common.extension.getDeviceModel
import com.sola.anime.ai.generator.common.extension.startFirst
import com.sola.anime.ai.generator.common.extension.startIap
import com.sola.anime.ai.generator.common.extension.startMain
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.common.util.AESEncyption
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.*
import com.sola.anime.ai.generator.databinding.ActivitySplashBinding
import com.sola.anime.ai.generator.domain.interactor.SyncData
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.max

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : LsActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    @Inject lateinit var prefs: Preferences
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var networkDialog: NetworkDialog
    @Inject lateinit var syncData: SyncData
    @Inject lateinit var serverApiRepo: ServerApiRepository
    @Inject lateinit var admobManager: AdmobManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        prefs.creditsChanges.delete()

//        Timber.tag("Main12345").e("Device model: ${getDeviceModel()}")
//        val encrypt = AESEncyption.encrypt("DEZGO-677ADADF008D43E746840EF0A88000892E435F3ECB11A537CCC322B11511AB524D2EE56D") ?: ""
//        Timber.tag("Main12345").e("Key: $encrypt")
//        Timber.tag("Main12345").e("Key 2: ${AESEncyption.decrypt(Constraint.Dezgo.DEZGO_KEY)}")

        initReviewManager()
        initView()
        initObservable()
        initData()
    }

    private fun initReviewManager() {
        App.app.loadReviewInfo()
    }

    @SuppressLint("SimpleDateFormat")
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
        lifecycleScope.launch(Dispatchers.Main) {
            delay(500)
            when {
                !isNetworkAvailable() -> networkDialog.show(this@SplashActivity){
                    networkDialog.dismiss()

                    initData()
                }
                else -> {
                    val token = try {
                        FirebaseInstallations.getInstance().getToken(false).await().token
                    } catch (e: Exception){
                        e.printStackTrace()
                        null
                    }
                    Timber.tag("Token A/B").e("Token Firebase Installation: $token")
                    syncRemoteConfig {
                        lifecycleScope.launch(Dispatchers.Main) {
                            when {
                                !prefs.isSyncUserPurchased.get() && Purchases.isConfigured -> {
                                    syncUserPurchased()
                                    delay(500)
                                }
                            }
                            syncData.execute(Unit)

                            handleSuccess()
                        }
                    }
                }
            }
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
                    configApp.scriptIap = tryOrNull { config.getString("script_iap").takeIf { it.isNotEmpty() } } ?: configApp.scriptIap
                    configApp.stepDefault = tryOrNull { config.getString("step_default").takeIf { it.isNotEmpty() } } ?: configApp.stepDefault
                    configApp.stepPremium = tryOrNull { config.getString("step_premium").takeIf { it.isNotEmpty() } } ?: configApp.stepPremium
                    configApp.maxNumberGenerateFree = tryOrNull { config.getLong("max_number_generate_free") } ?: configApp.maxNumberGenerateFree
                    configApp.maxNumberGeneratePremium = tryOrNull { config.getLong("max_number_generate_premium") } ?: configApp.maxNumberGeneratePremium
                    configApp.feature = tryOrNull { config.getString("feature").takeIf { it.isNotEmpty() } } ?: configApp.feature
                    configApp.version = tryOrNull { config.getLong("version") } ?: configApp.version
                    configApp.versionExplore = tryOrNull { config.getLong("version_explore") } ?: configApp.versionExplore
                    configApp.versionIap = tryOrNull { config.getLong("version_iap") } ?: configApp.versionIap
                    configApp.versionProcess = tryOrNull { config.getLong("version_process") } ?: configApp.versionProcess
                    configApp.versionStyle = tryOrNull { config.getLong("version_style") } ?: configApp.versionStyle

                    Timber.e("scriptIap: ${configApp.scriptIap}")
                    Timber.e("stepDefault: ${configApp.stepDefault}")
                    Timber.e("stepPremium: ${configApp.stepPremium}")
                    Timber.e("maxNumberGenerateFree: ${configApp.maxNumberGenerateFree}")
                    Timber.e("maxNumberGeneratePremium: ${configApp.maxNumberGeneratePremium}")
                    Timber.e("feature: ${configApp.feature}")
                    Timber.e("version: ${configApp.version}")
                    Timber.e("versionExplore: ${configApp.versionExplore}")
                    Timber.e("versionIap: ${configApp.versionIap}")
                    Timber.e("versionProcess: ${configApp.versionProcess}")
                    Timber.e("versionStyle: ${configApp.versionStyle}")

                    done()
                }
                .addOnFailureListener {
                    done()
                }
        }
    }

    private fun initObservable() {

    }

    private fun handleSuccess(){
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
            binding.textLoadingAd.text = "Loading ads..."
            admobManager.loadAndShowOpenSplash(this@SplashActivity, loaded = {
                binding.viewLoadingAd.animate().alpha(0f).setDuration(250).start()
            }, success = { task() }, failed = { task() })
        }
    }

    private fun initView() {
        Glide.with(this)
            .load(R.drawable.ic_launcher)
            .transition(DrawableTransitionOptions.withCrossFade())
            .into(binding.image)
    }

}