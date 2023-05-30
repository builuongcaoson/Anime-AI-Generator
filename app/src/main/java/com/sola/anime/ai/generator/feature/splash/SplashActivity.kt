package com.sola.anime.ai.generator.feature.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.format.DateUtils
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.isNetworkAvailable
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.getCustomerInfoWith
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.text.SimpleDateFormat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initReviewManager()
        initView()
        initObservable()
        initData()
    }

    private fun initReviewManager() {
        App.app.loadReviewManager()
    }

    private fun initRevenuecat(){
        Purchases.logLevel = LogLevel.DEBUG
        Purchases.debugLogsEnabled = true
        Purchases.configure(PurchasesConfiguration.Builder(this, Constraint.Info.REVENUECAT_KEY).build())
        syncUserPurchased()
    }

    @SuppressLint("SimpleDateFormat")
    private fun syncUserPurchased() {
        // Reset Premium
        prefs.isUpgraded.delete()

        Purchases.sharedInstance.getCustomerInfoWith { customerInfo ->
            customerInfo.entitlements.all.forEach {
                Timber.tag("Main12345").e("Key: ${it.key} --- ${it.value.isActive}")
            }
            customerInfo.latestExpirationDate?.let { date ->
                Timber.tag("Main12345").e("Time Expired: ${SimpleDateFormat("dd/MM/yyyy - hh:mm:ss").format(date)}")
            }
            val isActive = customerInfo.entitlements["premium"]?.isActive ?: false
            Timber.tag("Main12345").e("Premium is active: $isActive")
            prefs.isUpgraded.set(isActive)
            prefs.timeExpiredIap.delete()

            when {
                prefs.isUpgraded.get() && !DateUtils.isToday(prefs.latestTimeCreatedArtwork.get()) -> {
                    prefs.numberCreatedArtwork.delete()
                    prefs.latestTimeCreatedArtwork.delete()
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
                    val token = FirebaseInstallations.getInstance().getToken(false).await().token
                    Timber.e("Token Firebase Installation: $token")
                    syncRemoteConfig()
                    initRevenuecat()
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

//    private fun syncDatas(){
//        syncFolders()
//        syncArtProcess()
//        syncStyles()
//        syncIap()
//        syncExplores()
//    }
//
//    private fun syncFolders() {
//        if (!prefs.isCreateDefaultFolder.get()){
//            val folder = Folder(display = "All")
//            folderDao.inserts(folder)
//
//            prefs.isCreateDefaultFolder.set(true)
//        }
//    }
//
//    private fun syncExplores() {
//        val inputStream = assets.open("explore.json")
//        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
//        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<Explore>::class.java) } ?: arrayOf()
//
//        data.forEach { explore ->
//            Glide.with(this).load(explore.preview).preload()
//
//            explore.ratio = tryOrNull { explore.preview.split("zzz").getOrNull(1)?.replace("xxx",":") } ?: "1:1"
//        }
//
//        exploreDao.deleteAll()
//        exploreDao.inserts(*data)
//    }
//
//    private fun syncIap() {
//        val inputStream = assets.open("iap.json")
//        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
//        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<IAP>::class.java) } ?: arrayOf()
//
//        data.forEach { iapPreview ->
//            Glide.with(this).load(iapPreview.preview).preload()
//
//            iapPreview.ratio = tryOrNull { iapPreview.preview.split("zzz").getOrNull(1)?.replace("xxx",":") } ?: "1:1"
//        }
//
//        iapDao.deleteAll()
//        iapDao.inserts(*data)
//    }
//
//    private fun syncStyles() {
//        val inputStream = assets.open("style.json")
//        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
//        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<Style>::class.java) } ?: arrayOf()
//
//        data.forEach {
//            Glide.with(this).load(it.preview).preload()
//        }
//
//        styleDao.deleteAll()
//        styleDao.inserts(*data)
//    }
//
//    private fun syncArtProcess() {
//        val inputStream = assets.open("process.json")
//        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
//        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<Process>::class.java) } ?: arrayOf()
//
//        data.forEach {
//            Glide.with(this).load(it.preview).preload()
//        }
//
//        progressDao.deleteAll()
//        progressDao.inserts(*data)
//    }

    private fun syncRemoteConfig(numberSync: Int = 1) {
        Firebase.remoteConfig.let { config ->
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }
            config.setConfigSettingsAsync(configSettings)
            config
                .fetchAndActivate()
                .addOnSuccessListener {
                    Timber.tag("Main12345").e("############### FETCH AND ACTIVATE $numberSync ##############")
                    if (config.getString("script_iap").isEmpty()) {
                        if (numberSync > 1) {
                            syncRemoteConfig(numberSync - 1)
                        }
                        return@addOnSuccessListener
                    }

                    configApp.scriptIap = config.getString("script_iap").takeIf { it.isNotEmpty() }
                        ?: configApp.scriptIap

                    Timber.tag("Main12345").e("###############")
                    Timber.tag("Main12345").e("script_show_iap: ${configApp.scriptIap}")
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
            prefs.isSyncedData.set(true)
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