package com.sola.anime.ai.generator.domain.interactor

import android.content.Context
import com.basic.common.extension.tryOrNull
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.*
import com.sola.anime.ai.generator.domain.model.Folder
import com.sola.anime.ai.generator.domain.model.config.artprocess.ArtProcess
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
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
    private val prefs: Preferences,
    private val folderDao: FolderDao,
    private val artProgressDao: ArtProcessDao,
    private val styleDao: StyleDao,
    private val iapPreviewDao: IapPreviewDao,
    private val exploreDao: ExploreDao
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
            .doOnNext { syncFolders() }
            .doOnNext { syncArtProcess() }
            .doOnNext { syncStyles() }
            .doOnNext { syncIap() }
            .doOnNext { syncExplores() }
            .doOnNext { syncProgress.onNext(Progress.Success) }
            .map { startTime -> System.currentTimeMillis() - startTime }
            .map { elapsed -> TimeUnit.MILLISECONDS.toMillis(elapsed) }
            .doOnNext { milliseconds -> Timber.i("Completed setup firebase config in $milliseconds milliseconds") }
    }

    private fun syncFolders() {
        if (!prefs.isCreateDefaultFolder.get()){
            val folder = Folder(display = "All")
            folderDao.inserts(folder)

            prefs.isCreateDefaultFolder.set(true)
        }
    }

    private fun syncExplores() {
        val inputStream = context.assets.open("explore_v1.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<Explore>::class.java) } ?: arrayOf()

        data.forEach { explore ->
            Glide.with(context).asBitmap().load(explore.preview).preload()

            explore.ratio = tryOrNull { explore.preview.split("zzz").getOrNull(1)?.replace("xxx",":") } ?: "1:1"
        }

        exploreDao.deleteAll()
        exploreDao.inserts(*data)
    }

    private fun syncIap() {
        val inputStream = context.assets.open("iap_v1.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<IapPreview>::class.java) } ?: arrayOf()

        data.forEach { iapPreview ->
            Glide.with(context).asBitmap().load(iapPreview.preview).preload()

            iapPreview.ratio = tryOrNull { iapPreview.preview.split("zzz").getOrNull(1)?.replace("xxx",":") } ?: "1:1"
        }

        iapPreviewDao.deleteAll()
        iapPreviewDao.inserts(*data)
    }

    private fun syncStyles() {
        val inputStream = context.assets.open("style_v1.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<Style>::class.java) } ?: arrayOf()

        data.forEach {
            Glide.with(context).asBitmap().load(it.preview).preload()
        }

        styleDao.deleteAll()
        styleDao.inserts(*data)
    }

    private fun syncArtProcess() {
        val inputStream = context.assets.open("art_process_v1.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<ArtProcess>::class.java) } ?: arrayOf()

        data.forEach {
            Glide.with(context).asBitmap().load(it.preview).preload()
        }

        artProgressDao.deleteAll()
        artProgressDao.inserts(*data)
    }

}