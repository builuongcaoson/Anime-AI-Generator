package com.sola.anime.ai.generator.domain.interactor

import android.content.Context
import com.google.gson.Gson
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.domain.model.PreviewIap
import com.sola.anime.ai.generator.domain.model.config.DataConfigApp
import com.sola.anime.ai.generator.domain.model.config.ProcessPreview
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
    private val configApp: ConfigApp
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
            .map { startTime -> System.currentTimeMillis() - startTime }
            .map { elapsed -> TimeUnit.MILLISECONDS.toMillis(elapsed) }
            .doOnNext { milliseconds -> Timber.i("Completed setup firebase config in $milliseconds milliseconds") }
    }

    private fun syncConfigApp() {
        val inputStream = context.assets.open("anime_ai_generator_v1.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val gson = Gson()
        val data = gson.fromJson(bufferedReader, DataConfigApp::class.java)

        // Iap
        configApp.previewsIap1 = ArrayList(data.app.iap.preview1.mapNotNull { PreviewIap(it.preview, it.ratio) })
        configApp.previewsIap2 = ArrayList(data.app.iap.preview2.mapNotNull { PreviewIap(it.preview, it.ratio) })
        configApp.previewsIap3 = ArrayList(data.app.iap.preview3.mapNotNull { PreviewIap(it.preview, it.ratio) })

        // Art
        configApp.artProcessPreviews = ArrayList(data.art.processPreviews).apply {
            this.shuffle()
        }

        syncProgress.onNext(Progress.Success)
    }


}