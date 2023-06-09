package com.sola.anime.ai.generator.feature.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateUtils
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.isNetworkAvailable
import com.basic.common.extension.tryOrNull
import com.google.firebase.database.ktx.database
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
import com.sola.anime.ai.generator.common.extension.startFirst
import com.sola.anime.ai.generator.common.extension.startMain
import com.sola.anime.ai.generator.common.ui.dialog.NetworkDialog
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.*
import com.sola.anime.ai.generator.databinding.ActivitySplashBinding
import com.sola.anime.ai.generator.domain.interactor.SyncData
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import com.sola.anime.ai.generator.inject.server.ServerApi
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : LsActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    @Inject lateinit var prefs: Preferences
    @Inject lateinit var configApp: ConfigApp
    @Inject lateinit var folderDao: FolderDao
    @Inject lateinit var progressDao: ProcessDao
    @Inject lateinit var styleDao: StyleDao
    @Inject lateinit var iapDao: IAPDao
    @Inject lateinit var exploreDao: ExploreDao
    @Inject lateinit var networkDialog: NetworkDialog
    @Inject lateinit var syncData: SyncData
    @Inject lateinit var serverApiRepo: ServerApiRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

//        val encrypt = AESEncyption.encrypt("DEZGO-677ADADF008D43E746840EF0A88000892E435F3ECB11A537CCC322B11511AB524D2EE56D") ?: ""
//        Timber.tag("Main12345").e("Key: $encrypt")
//        Timber.tag("Main12345").e("Key 2: ${AESEncyption.decrypt(Constraint.Api.DEZGO_KEY)}")

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

            if (isActive){
                when {
                    !prefs.isSyncUserPurchased.get() -> {
                        lifecycleScope.launch(Dispatchers.Main) {
                            serverApiRepo.syncUser(appUserId = customerInfo.originalAppUserId) {

                            }
                        }
                    }
                    prefs.isSyncUserPurchasedFailed.get() -> {
                        if (configApp.skipSyncPremium){
                            customerInfo
                                .latestExpirationDate
                                ?.let { expiredDate ->
                                    prefs.isUpgraded.set(true)
                                    prefs.timeExpiredIap.set(expiredDate.time)
                                }
                            return@getCustomerInfoWith
                        }

                        customerInfo
                            .allPurchaseDatesByProduct
                            .filter { it.value != null }
                            .filter { it.key.contains(Constraint.Iap.SKU_WEEK) || it.key.contains(Constraint.Iap.SKU_MONTH) ||it.key.contains(Constraint.Iap.SKU_YEAR) }
                            .takeIf { it.isNotEmpty() }
                            ?.maxByOrNull { it.value!! }
                            ?.let { map ->
                                val latestPurchasedProduct = map.key
                                val latestDatePurchased = map.value ?: return@let
                                val expiredDate = customerInfo.getExpirationDateForProductId(latestPurchasedProduct) ?: return@let

                                val expiredDateTime = when {
                                    prefs.isUpgraded.get() -> expiredDate.time
                                    else -> latestDatePurchased.time + if (BuildConfig.DEBUG) 0 else 21600000L // Day time purchased + 6 hours
                                }

                                val differenceInMillis = expiredDateTime - Date().time
                                if (differenceInMillis > 0){
                                    val days = TimeUnit.MILLISECONDS.toDays(differenceInMillis)
                                    val hours = TimeUnit.MILLISECONDS.toHours(differenceInMillis) % 24
                                    val minutes = TimeUnit.MILLISECONDS.toMinutes(differenceInMillis) % 60
                                    val seconds = TimeUnit.MILLISECONDS.toSeconds(differenceInMillis) % 60

                                    when {
                                        days <= 0 && hours <= 0 && minutes <= 0 && seconds <= 0 -> {
                                            prefs.isUpgraded.delete()
                                            prefs.timeExpiredIap.delete()
                                        }
                                        else -> {
                                            prefs.isUpgraded.set(true)
                                            prefs.timeExpiredIap.set(expiredDate.time)
                                        }
                                    }

                                    Timber.tag("Main12345").e("Time Purchased: ${SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(latestDatePurchased)}")
                                    Timber.tag("Main12345").e("Time Expired: ${SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(expiredDate)}")
                                    Timber.tag("Main12345").e("Date: $days --- $hours:$minutes:$seconds")
                                } else {
                                    prefs.isUpgraded.delete()
                                    prefs.timeExpiredIap.delete()
                                }
                                Timber.tag("Main12345").e("DifferenceInMillis: $differenceInMillis --- ${latestDatePurchased.time} --- ${Date().time}")
                            }
                        when {
                            prefs.isUpgraded.get() && !DateUtils.isToday(prefs.latestTimeCreatedArtwork.get()) -> {
                                prefs.numberCreatedArtwork.delete()
                                prefs.latestTimeCreatedArtwork.delete()
                            }
                        }
                    }
                }
            } else {
                prefs.isUpgraded.delete()
                prefs.timeExpiredIap.delete()
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
                    Timber.e("Token Firebase Installation: $token")
                    syncRemoteConfig {
                        lifecycleScope.launch(Dispatchers.Main) {
                            syncUserPurchased()
                            delay(500)
                            when {
                                !prefs.isSyncedData.get() -> {
                                    syncData.execute(Unit)

                                    handleSuccess()
                                }
                                else -> handleSuccess()
                            }
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
                    configApp.skipSyncPremium = tryOrNull { config.getBoolean("skipSyncPremium") } ?: false
                    configApp.scriptIap = config.getString("script_iap").takeIf { it.isNotEmpty() } ?: configApp.scriptIap

                    Timber.tag("Main12345").e("###############")
                    Timber.tag("Main12345").e("skipSyncPremium: ${configApp.skipSyncPremium}")
                    Timber.tag("Main12345").e("script_show_iap: ${configApp.scriptIap}")

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
        lifecycleScope.launch(Dispatchers.Main) {
            binding.textStatus.text = getString(R.string.syncing_data_please_wait)
            delay(1000)
            binding.textStatus.text = getString(R.string.syncing_data_complete)
            delay(500)
            binding.viewLottie.cancelAnimation()

            when {
                prefs.isFirstTime.get() -> startFirst()
                else -> startMain()
            }

            finish()
        }
    }

    private fun initView() {

    }

}