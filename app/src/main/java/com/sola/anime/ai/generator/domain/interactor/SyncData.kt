package com.sola.anime.ai.generator.domain.interactor

import android.content.Context
import com.basic.common.extension.tryOrNull
import com.bumptech.glide.Glide
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.sola.anime.ai.generator.data.db.query.*
import com.sola.anime.ai.generator.domain.model.config.Data
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.iap.IAP
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
class SyncData @Inject constructor(
    private val context: Context,
    private val processDao: ProcessDao,
    private val styleDao: StyleDao,
    private val iapDao: IAPDao,
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
            .delay(1, TimeUnit.SECONDS)
            .doOnNext { syncData() }
            .doOnNext { syncProgress.onNext(Progress.Success) }
            .map { startTime -> System.currentTimeMillis() - startTime }
            .map { elapsed -> TimeUnit.MILLISECONDS.toMillis(elapsed) }
            .doOnNext { milliseconds -> Timber.i("Completed setup firebase config in $milliseconds milliseconds") }
    }

    private fun syncData() {
        Firebase.database.reference.child("v1").get()
            .addOnSuccessListener { snapshot ->
                Timber.e("Key: ${snapshot.key} --- ${snapshot.childrenCount}")
                val data = snapshot.getValue(Data::class.java)
                snapshot.children.forEach { childDataSnapshot ->
                    when {
                        childDataSnapshot.key == "process" -> {
                            val genericTypeIndicator = object : GenericTypeIndicator<List<Process>>() {}
                            val progresses = childDataSnapshot.getValue(genericTypeIndicator)
                            Timber.e("Process: ${Gson().toJson(progresses)}")

                            childDataSnapshot.children.forEach { child ->
//                                val processes = childDataSnapshot.getValue(Process::class.java)
//                                Timber.e("Data: ${Gson().toJson(processes)}")
//                                Timber.e("Key: ${child.key} --- ${child.childrenCount}")
                            }

                        }
                    }
                    Timber.e("Child key: ${childDataSnapshot.key} --- ${childDataSnapshot.childrenCount}")
                }

            }
            .addOnFailureListener {

            }
//        Firebase.database.reference.child("v1").addValueEventListener(object: ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                Timber.e("Key: ${snapshot.key} --- ${snapshot.childrenCount}")
//                snapshot.children.forEach { childDataSnapshot ->
//                    Timber.e("Child key: ${childDataSnapshot.key} --- ${childDataSnapshot.childrenCount}")
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                syncExploresLocal()
//                syncStylesLocal()
//                syncArtProcessLocal()
//                syncIapLocal()
//            }
//        })
    }

    private fun syncExploresLocal() {
        val inputStream = context.assets.open("explore.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<Explore>::class.java) } ?: arrayOf()

        data.forEach { explore ->
            Glide.with(context).asBitmap().load(explore.preview).preload()

            explore.ratio = tryOrNull { explore.preview.split("zzz").getOrNull(1)?.replace("xxx",":") } ?: "1:1"
        }

        exploreDao.deleteAll()
        exploreDao.inserts(*data)
    }

    private fun syncIapLocal() {
        val inputStream = context.assets.open("iap.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<IAP>::class.java) } ?: arrayOf()

        data.forEach { iapPreview ->
            Glide.with(context).asBitmap().load(iapPreview.preview).preload()

            iapPreview.ratio = tryOrNull { iapPreview.preview.split("zzz").getOrNull(1)?.replace("xxx",":") } ?: "1:1"
        }

        iapDao.deleteAll()
        iapDao.inserts(*data)
    }

    private fun syncStylesLocal() {
        val inputStream = context.assets.open("style.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<Style>::class.java) } ?: arrayOf()

        data.forEach {
            Glide.with(context).asBitmap().load(it.preview).preload()
        }

        styleDao.deleteAll()
        styleDao.inserts(*data)
    }

    private fun syncArtProcessLocal() {
        val inputStream = context.assets.open("process.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<Process>::class.java) } ?: arrayOf()

        data.forEach {
            Glide.with(context).asBitmap().load(it.preview).preload()
        }

        processDao.deleteAll()
        processDao.inserts(*data)
    }

}