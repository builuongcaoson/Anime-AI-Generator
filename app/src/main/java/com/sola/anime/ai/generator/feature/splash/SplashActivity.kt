package com.sola.anime.ai.generator.feature.splash

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.basic.common.base.LsActivity
import com.basic.common.extension.lightStatusBar
import com.basic.common.extension.transparent
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.R
import com.sola.anime.ai.generator.common.App
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
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@SuppressLint("CustomSplashScreen")
@AndroidEntryPoint
class SplashActivity : LsActivity<ActivitySplashBinding>(ActivitySplashBinding::inflate) {

    @Inject lateinit var syncConfigApp: SyncConfigApp
    @Inject lateinit var prefs: Preferences
    @Inject lateinit var admobManager: AdmobManager

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
        syncConfigApp.execute(Unit)
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
                        binding.viewLottie.cancelAnimation()

                        when {
                            prefs.isFirstTime.get() -> startFirst()
//                            !prefs.isViewTutorial.get() -> startTutorial()
                            else -> startMain()
                        }
                        finish()
                    }
                    else -> {}
                }
            }
    }

    private fun initView() {

    }

}