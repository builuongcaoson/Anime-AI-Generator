package com.sola.anime.ai.generator.domain.interactor

import android.content.Context
import com.basic.common.extension.tryOrNull
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
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
class SyncData @Inject constructor(
    private val context: Context,
    private val prefs: Preferences,
    private val processDao: ProcessDao,
    private val styleDao: StyleDao,
    private val modelDao: ModelDao,
    private val iapDao: IAPDao,
    private val exploreDao: ExploreDao,
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
            .doOnNext { syncData() }
            .doOnNext { syncProgress.onNext(Progress.Success) }
            .map { startTime -> System.currentTimeMillis() - startTime }
            .map { elapsed -> TimeUnit.MILLISECONDS.toMillis(elapsed) }
            .doOnNext { milliseconds -> Timber.i("Completed setup firebase config in $milliseconds milliseconds") }
    }

    private fun syncData() {
//        if (prefs.versionExplore.get() < configApp.versionExplore || exploreDao.getAll().isEmpty()){
//            Firebase.database.reference.child("v1/explore").get()
//                .addOnSuccessListener { snapshot ->
//                    val genericTypeIndicator = object : GenericTypeIndicator<List<Explore>>() {}
//                    val explores = tryOrNull { snapshot.getValue(genericTypeIndicator) } ?: emptyList()
//
//                    when {
//                        explores.isNotEmpty() -> {
////                            explores.forEach { explore ->
////                                explore.ratio = tryOrNull { explore.preview.split("zzz").getOrNull(1)?.replace("xxx",":") } ?: "1:1"
////                            }
//
//                            exploreDao.deleteAll()
//                            exploreDao.inserts(*explores.toTypedArray())
//                        }
//                        else -> syncExploresLocal()
//                    }
//
//                    Timber.e("Sync explore: ${explores.size}")
//
//                    prefs.versionExplore.set(configApp.versionExplore)
//                }
//                .addOnFailureListener {
//                    Timber.e("Error explore: ${it.message}")
//
//                    syncExploresLocal()
//                }
//        }

        if (prefs.versionIap.get() < configApp.versionIap || iapDao.getAll().isEmpty()){
            Firebase.database.reference.child("v1/iap").get()
                .addOnSuccessListener { snapshot ->
                    Timber.e("Key: ${snapshot.key} --- ${snapshot.childrenCount}")
                    val genericTypeIndicator = object : GenericTypeIndicator<List<IAP>>() {}
                    val iaps = tryOrNull { snapshot.getValue(genericTypeIndicator) } ?: emptyList()

                    when {
                        iaps.isNotEmpty() -> {
                            iaps.forEach { iap ->
                                iap.ratio = tryOrNull { iap.preview.split("zzz").getOrNull(1)?.replace("xxx",":") } ?: "1:1"
                            }

                            iapDao.deleteAll()
                            iapDao.inserts(*iaps.toTypedArray())
                        }
                        else -> syncIapLocal()
                    }

                    Timber.e("Sync iap: ${iaps.size}")

                    prefs.versionIap.set(configApp.versionIap)
                }
                .addOnFailureListener {
                    Timber.e("Error iap: ${it.message}")

                    syncIapLocal()
                }
        }

        if (prefs.versionProcess.get() < configApp.versionProcess || processDao.getAll().isEmpty()){
            Firebase.database.reference.child("v1/process").get()
                .addOnSuccessListener { snapshot ->
                    Timber.e("Key: ${snapshot.key} --- ${snapshot.childrenCount}")
                    val genericTypeIndicator = object : GenericTypeIndicator<List<Process>>() {}
                    val processes = tryOrNull { snapshot.getValue(genericTypeIndicator) } ?: emptyList()

                    when {
                        processes.isNotEmpty() -> {
                            processDao.deleteAll()
                            processDao.inserts(*processes.toTypedArray())
                        }
                        else -> syncProcessLocal()
                    }

                    Timber.e("Sync process: ${processes.size}")

                    prefs.versionProcess.set(configApp.versionProcess)
                }
                .addOnFailureListener {
                    Timber.e("Error process: ${it.message}")

                    syncProcessLocal()
                }
        }

        if (prefs.versionStyle.get() < configApp.versionStyle || styleDao.getAll().isEmpty()){
            Firebase.database.reference.child("v1/style").get()
                .addOnSuccessListener { snapshot ->
                    Timber.e("Key: ${snapshot.key} --- ${snapshot.childrenCount}")
                    val genericTypeIndicator = object : GenericTypeIndicator<List<Style>>() {}
                    val styles = tryOrNull { snapshot.getValue(genericTypeIndicator) } ?: emptyList()

                    when {
                        styles.isNotEmpty() -> {
                            styleDao.deleteAll()
                            styleDao.inserts(*styles.toTypedArray())
                        }
                        else -> syncStylesLocal()
                    }
                    Timber.e("Sync styles: ${styles.size}")

                    prefs.versionStyle.set(configApp.versionStyle)
                }
                .addOnFailureListener {
                    Timber.e("Error styles: ${it.message}")

                    syncStylesLocal()
                }
        }

    }

    private fun syncExploresLocal() {
        val inputStream = context.assets.open("explore.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<Explore>::class.java) } ?: arrayOf()

//        data.forEach { explore ->
//            explore.ratio = tryOrNull { explore.preview.split("zzz").getOrNull(1)?.replace("xxx",":") } ?: "1:1"
//        }

        exploreDao.deleteAll()
        exploreDao.inserts(*data)
    }

    private fun syncIapLocal() {
        val inputStream = context.assets.open("iap.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<IAP>::class.java) } ?: arrayOf()

        data.forEach { iapPreview ->
            iapPreview.ratio = tryOrNull { iapPreview.preview.split("zzz").getOrNull(1)?.replace("xxx",":") } ?: "1:1"
        }

        iapDao.deleteAll()
        iapDao.inserts(*data)
    }

    private fun syncStylesLocal() {
        val inputStream = context.assets.open("style.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<Style>::class.java) } ?: arrayOf()

        styleDao.deleteAll()
        styleDao.inserts(*data)
    }

    private fun syncProcessLocal() {
        val inputStream = context.assets.open("process.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<Process>::class.java) } ?: arrayOf()

        processDao.deleteAll()
        processDao.inserts(*data)
    }

}