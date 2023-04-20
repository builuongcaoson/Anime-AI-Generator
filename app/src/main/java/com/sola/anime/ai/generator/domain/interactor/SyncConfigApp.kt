package com.sola.anime.ai.generator.domain.interactor

import android.content.Context
import com.basic.common.extension.tryOrNull
import com.google.gson.Gson
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.data.db.query.IapPreviewDao
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.domain.model.PreviewIap
import com.sola.anime.ai.generator.domain.model.config.DataConfigApp
import com.sola.anime.ai.generator.domain.model.config.iap.IapPreview
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
class SyncConfigApp @Inject constructor(
    private val context: Context,
    private val configApp: ConfigApp,
    private val styleDao: StyleDao,
    private val iapPreviewDao: IapPreviewDao,
) : Interactor<Unit>() {

    sealed class Progress{
        object Idle: Progress()
        object Running: Progress()
        object Success: Progress()
    }

    private val syncProgress: Subject<Progress> = BehaviorSubject.createDefault(Progress.Idle)

    fun syncProgress(): Observable<Progress> = syncProgress

    override fun buildObservable(params: Unit): Flowable<*> {
        return Flowable.just(System.currentTimeMillis())
            .doOnNext { syncProgress.onNext(Progress.Running) }
            .delay(1, TimeUnit.SECONDS)
            .doOnNext { syncConfigApp() }
            .doOnNext { syncStyles() }
            .doOnNext { syncIap() }
            .doOnNext { syncProgress.onNext(Progress.Success) }
            .map { startTime -> System.currentTimeMillis() - startTime }
            .map { elapsed -> TimeUnit.MILLISECONDS.toMillis(elapsed) }
            .doOnNext { milliseconds -> Timber.i("Completed setup firebase config in $milliseconds milliseconds") }
    }

    private fun syncIap() {
        val inputStream = context.assets.open("iap_v1.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<IapPreview>::class.java) } ?: arrayOf()

        data.forEach {
            it.ratio = it.preview.split("")
        }

        iapPreviewDao.deleteAll()
        iapPreviewDao.inserts(*data)
    }

    private fun syncStyles() {
        val inputStream = context.assets.open("style_v1.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<Style>::class.java) } ?: arrayOf()

        styleDao.deleteAll()
        styleDao.inserts(*data)
    }

    private fun syncConfigApp() {
        val inputStream = context.assets.open("anime_ai_generator_v1.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val gson = Gson()
        val data = gson.fromJson(bufferedReader, DataConfigApp::class.java)

        // Iap


        // Art
        configApp.artProcessPreviews = ArrayList(data.art.processPreviews).apply {
            this.shuffle()
        }

    }


}