package com.sola.anime.ai.generator.feature.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.transparent
import com.basic.common.extension.tryOrNull
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.App
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Navigator
import com.sola.anime.ai.generator.common.extension.startFirst
import com.sola.anime.ai.generator.common.extension.startMain
import com.sola.anime.ai.generator.common.extension.startTutorial
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.databinding.ActivitySplashBinding
import com.sola.anime.ai.generator.domain.interactor.SyncConfigApp
import com.sola.anime.ai.generator.domain.manager.AdmobManager
import com.uber.autodispose.android.lifecycle.scope
import com.uber.autodispose.autoDispose
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : LsActivity() {

    @Inject lateinit var syncConfigApp: SyncConfigApp
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var admobManager: AdmobManager
    @Inject lateinit var configApp: ConfigApp

    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        when {
            !prefs.isUpgraded.get() -> admobManager.loadRewardCreate()
        }

        initReviewManager()
        initView()
        initObservable()
        initData()
    }

    private fun initReviewManager() {
        App.app.loadReviewManager()
    }

    private fun initData() {
//        val currentTime = System.currentTimeMillis()
//        val expiryTime = prefs.timeExpiredIap.get()
//        when {
//            !prefs.isUpgraded.get() -> {}
//            prefs.timeExpiredIap.get() == -2L -> {}
//            prefs.timeExpiredIap.get() == -3L -> prefs.isUpgraded.set(false)
//            prefs.timeExpiredIap.get() != -1L && currentTime >= expiryTime -> {
//                prefs.isUpgraded.set(false)
//            }
//        }

        lifecycleScope.launch(Dispatchers.Main) {
            val token = FirebaseInstallations.getInstance().getToken(false).await().token
            Timber.e("Token Firebase Installation: $token")
            syncRemoteConfig()
            delay(1000)
            when {
                !prefs.isSyncedData.get() -> syncConfigApp.execute(Unit)
                else -> {
                    binding.textStatus.text = "Syncing data, please wait a moment..."
                    delay(1000)
                    binding.textStatus.text = "Data sync complete!"
                    delay(1000)
                    handleSuccess()
                }
            }
        }
    }

    private fun syncRemoteConfig(numberSync: Int = 1) {
        Firebase.remoteConfig.let { config ->
            val configSettings = remoteConfigSettings {
                minimumFetchIntervalInSeconds = 0
            }
            config.setConfigSettingsAsync(configSettings)
            config
                .fetchAndActivate()
                .addOnSuccessListener {
                    Log.e("Main12345","############### FETCH AND ACTIVATE $numberSync ##############")
                    if (config.getString("script_iap").isEmpty()){
                        if (numberSync > 1){
                            syncRemoteConfig(numberSync - 1)
                        }
                        return@addOnSuccessListener
                    }

                    configApp.scriptIap = config.getString("script_iap").takeIf { it.isNotEmpty() } ?: configApp.scriptIap

                    Log.e("Main12345","###############")
                    Log.e("Main12345","script_show_iap: ${configApp.scriptIap}")
                }
        }
    }

    private fun initObservable() {
        syncConfigApp
            .syncProgress()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                when (it){
                    is SyncConfigApp.Progress.Running -> {
                        binding.textStatus.text = "Syncing data, please wait a moment..."
                    }
                    else -> {}
                }
            }

        syncConfigApp
            .syncProgress()
            .filter { it is SyncConfigApp.Progress.Success }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                when (it){
                    is SyncConfigApp.Progress.Success -> {
                        binding.textStatus.text = "Data sync complete!"
                    }
                    else -> {}
                }
            }

        syncConfigApp
            .syncProgress()
            .filter { it is SyncConfigApp.Progress.Success }
            .delay(1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(AndroidSchedulers.mainThread())
            .autoDispose(scope())
            .subscribe {
                when (it){
                    is SyncConfigApp.Progress.Success -> {
                        handleSuccess()
                    }
                    else -> {}
                }
            }
    }

    private fun handleSuccess(){
        prefs.isSyncedData.set(true)
        binding.viewLottie.cancelAnimation()

        when {
            prefs.isFirstTime.get() -> startFirst()
//          !prefs.isViewTutorial.get() -> startTutorial()
            else -> startMain()
        }

        finish()
    }

    private fun initView() {

    }

}