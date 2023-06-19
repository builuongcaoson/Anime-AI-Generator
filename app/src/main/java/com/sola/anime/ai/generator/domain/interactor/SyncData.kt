package com.sola.anime.ai.generator.domain.interactor

import android.content.Context
import com.basic.common.extension.tryOrNull
import com.bumptech.glide.Glide
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.*
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
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
    private val prefs: Preferences,
    private val processDao: ProcessDao,
    private val styleDao: StyleDao,
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
        if (prefs.versionExplore.get() < configApp.versionExplore){
            Firebase.database.reference.child("v1/explore").get()
                .addOnSuccessListener { snapshot ->
                    val genericTypeIndicator = object : GenericTypeIndicator<List<Explore>>() {}
                    val explores = tryOrNull { snapshot.getValue(genericTypeIndicator) } ?: emptyList()

                    when {
                        explores.isNotEmpty() -> {
                            explores.forEach { explore ->
                                explore.ratio = tryOrNull { explore.preview.split("zzz").getOrNull(1)?.replace("xxx",":") } ?: "1:1"
                            }

                            exploreDao.deleteAll()
                            exploreDao.inserts(*explores.toTypedArray())
                        }
                        else -> syncExploresLocal()
                    }

                    prefs.versionExplore.set(configApp.versionExplore)
                }
                .addOnFailureListener {
                    syncExploresLocal()
                }
        }

        if (prefs.versionIap.get() < configApp.versionIap){
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

                    prefs.versionIap.set(configApp.versionIap)
                }
                .addOnFailureListener {
                    syncIapLocal()
                }
        }

        if (prefs.versionProcess.get() < configApp.versionProcess){
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

                    prefs.versionProcess.set(configApp.versionProcess)
                }
                .addOnFailureListener {
                    syncProcessLocal()
                }
        }

        if (prefs.versionStyle.get() < configApp.versionStyle)
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

                prefs.versionStyle.set(configApp.versionStyle)
            }
            .addOnFailureListener {
                syncStylesLocal()
            }

//        Firebase.database.reference.child("v1/").get()
//            .addOnSuccessListener { snapshot ->
//                Timber.e("Key: ${snapshot.key} --- ${snapshot.childrenCount}")
//                snapshot.children.forEach { childDataSnapshot ->
//                    when (childDataSnapshot.key) {
//                        "explore" -> {
//                            val genericTypeIndicator = object : GenericTypeIndicator<List<Explore>>() {}
//                            val explores = tryOrNull { childDataSnapshot.getValue(genericTypeIndicator) } ?: emptyList()
//
//                            when {
//                                explores.isNotEmpty() -> {
//                                    explores.forEach { explore ->
//                                        explore.ratio = tryOrNull { explore.preview.split("zzz").getOrNull(1)?.replace("xxx",":") } ?: "1:1"
//                                    }
//
//                                    exploreDao.deleteAll()
//                                    exploreDao.inserts(*explores.toTypedArray())
//                                }
//                                else -> syncExploresLocal()
//                            }
//                        }
//                        "iap" -> {
//                            val genericTypeIndicator = object : GenericTypeIndicator<List<IAP>>() {}
//                            val iaps = tryOrNull { childDataSnapshot.getValue(genericTypeIndicator) } ?: emptyList()
//
//                            when {
//                                iaps.isNotEmpty() -> {
//                                    iaps.forEach { iap ->
//                                        iap.ratio = tryOrNull { iap.preview.split("zzz").getOrNull(1)?.replace("xxx",":") } ?: "1:1"
//                                    }
//
//                                    iapDao.deleteAll()
//                                    iapDao.inserts(*iaps.toTypedArray())
//                                }
//                                else -> syncExploresLocal()
//                            }
//                        }
//                        "process" -> {
//                            val genericTypeIndicator = object : GenericTypeIndicator<List<Process>>() {}
//                            val processes = tryOrNull { childDataSnapshot.getValue(genericTypeIndicator) } ?: emptyList()
//
//                            when {
//                                processes.isNotEmpty() -> {
//                                    processDao.deleteAll()
//                                    processDao.inserts(*processes.toTypedArray())
//                                }
//                                else -> syncExploresLocal()
//                            }
//                        }
//                        "style" -> {
//                            val genericTypeIndicator = object : GenericTypeIndicator<List<Style>>() {}
//                            val styles = tryOrNull { childDataSnapshot.getValue(genericTypeIndicator) } ?: emptyList()
//
//                            when {
//                                styles.isNotEmpty() -> {
//                                    styleDao.deleteAll()
//                                    styleDao.inserts(*styles.toTypedArray())
//                                }
//                                else -> syncExploresLocal()
//                            }
//                        }
//                    }
//
//                    prefs.isSyncedData.set(true)
//                }
//            }
//            .addOnFailureListener {
//                syncExploresLocal()
//                syncIapLocal()
//                syncProcessLocal()
//                syncStylesLocal()
//
//                prefs.isSyncedData.set(true)
//            }
    }

    private fun syncExploresLocal() {
        val inputStream = context.assets.open("explore.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<Explore>::class.java) } ?: arrayOf()

        data.forEach { explore ->
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