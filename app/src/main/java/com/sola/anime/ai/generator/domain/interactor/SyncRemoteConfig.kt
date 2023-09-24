package com.sola.anime.ai.generator.domain.interactor

import android.content.Context
import com.basic.common.extension.tryOrNull
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import com.google.gson.Gson
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.*
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.iap.IAP
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.sola.anime.ai.generator.domain.model.config.process.Process
import com.sola.anime.ai.generator.domain.model.config.style.Style
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRemoteConfig @Inject constructor(
    private val context: Context,
    private val configApp: ConfigApp
) : Interactor<Unit>() {

    override fun buildObservable(params: Unit): Flowable<*> {
        return Flowable.just(System.currentTimeMillis())
            .doOnNext { syncRemoteConfig() }
            .map { startTime -> System.currentTimeMillis() - startTime }
            .map { elapsed -> TimeUnit.MILLISECONDS.toMillis(elapsed) }
            .doOnNext { milliseconds -> Timber.i("Completed setup firebase config in $milliseconds milliseconds") }
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
                    configApp.blockDeviceIds = tryOrNull { config.getString("blockDeviceIds").takeIf { it.isNotEmpty() }?.split(", ") } ?: configApp.blockDeviceIds
                    configApp.blockDeviceModels = tryOrNull { config.getString("blockDeviceModels").takeIf { it.isNotEmpty() }?.split(", ") } ?: configApp.blockDeviceModels
                    configApp.blockedRoot = tryOrNull { config.getBoolean("blockedRoot") } ?: configApp.blockedRoot

                    Timber.e("Block device ids: ${configApp.blockDeviceIds.joinToString { it }}")
                    Timber.e("Block device models: ${configApp.blockDeviceModels.joinToString { it }}")
                    Timber.e("blockedRoot: ${configApp.blockedRoot}")
                }
        }
    }

}