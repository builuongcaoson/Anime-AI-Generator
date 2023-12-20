package com.sola.anime.ai.generator.data.repo

import android.content.Context
import com.basic.common.extension.tryOrNull
import com.google.firebase.database.DatabaseReference
import com.google.gson.Gson
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.ExploreDao
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.domain.model.config.explore.Explore
import com.sola.anime.ai.generator.domain.model.config.lora.LoRAGroup
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.sola.anime.ai.generator.domain.repo.SyncRepository
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val context: Context,
    private val reference: DatabaseReference,
    private val prefs: Preferences,
    private val configApp: ConfigApp,
    private val modelDao: ModelDao,
    private val loRAGroupDao: LoRAGroupDao,
    private val exploreDao: ExploreDao
): SyncRepository {

    override suspend fun syncModelsAndLoRAs(progress: (SyncRepository.Progress) -> Unit) = withContext(Dispatchers.IO) {
        progress(SyncRepository.Progress.Running)

//        val deferredModels = async {
//            when {
//                prefs.versionModel.get() < configApp.versionModel || modelDao.getAll().isEmpty() -> reference.child("v2/models").get().await()
//                else -> null
//            }
//        }
//        val deferredLoRAs = async {
//            when {
//                prefs.versionLoRA.get() < configApp.versionLoRA || loRAGroupDao.getAll().isEmpty() -> reference.child("v2/loRAs").get().await()
//                else -> null
//            }
//        }
//
//        val (snapshotModels, snapshotLoRAs) = awaitAll(deferredModels, deferredLoRAs)
//
//        launch(Dispatchers.IO) {
//            snapshotModels?.let { snapshot ->
//                val genericTypeIndicator = object : GenericTypeIndicator<List<Model>>() {}
//                val datas = tryOrNull { snapshot.getValue(genericTypeIndicator) } ?: emptyList()
//
//                when {
//                    datas.isNotEmpty() -> {
//                        modelDao.deleteAll()
//                        modelDao.inserts(*datas.shuffled().toTypedArray())
//
//                        prefs.versionModel.set(configApp.versionModel)
//                    }
//                    prefs.versionModel.get() < configApp.versionModel || modelDao.getAll().isEmpty() -> syncModelsLocal()
//                }
//            } ?: run {
//                when {
//                    prefs.versionModel.get() < configApp.versionModel || modelDao.getAll().isEmpty() -> syncModelsLocal()
//                }
//            }
//        }
//        launch(Dispatchers.IO) {
//            snapshotLoRAs?.let { snapshot ->
//                val genericTypeIndicator = object : GenericTypeIndicator<List<LoRAGroup>>() {}
//                val datas = tryOrNull { snapshot.getValue(genericTypeIndicator) } ?: emptyList()
//
//                when {
//                    datas.isNotEmpty() -> {
//                        datas.forEach { loRAGroup ->
//                            loRAGroup.childs.forEach { loRA ->
//                                loRA.ratio = listOf("1:1", "2:3", "3:4").random()
//                            }
//                        }
//
//                        loRAGroupDao.deleteAll()
//                        loRAGroupDao.inserts(*datas.shuffled().toTypedArray())
//
//                        prefs.versionLoRA.set(configApp.versionLoRA)
//                    }
//                    prefs.versionLoRA.get() < configApp.versionLoRA || loRAGroupDao.getAll().isEmpty() -> syncLoRAsLocal()
//                }
//            } ?: run {
//                when {
//                    prefs.versionLoRA.get() < configApp.versionLoRA || loRAGroupDao.getAll().isEmpty() -> syncLoRAsLocal()
//                }
//            }
//        }

        when {
            prefs.versionModel.get() < configApp.versionModel || modelDao.getAll().isEmpty() -> syncModelsLocal()
        }

//        when {
//            prefs.versionLoRA.get() < configApp.versionLoRA || loRAGroupDao.getAll().isEmpty() -> syncLoRAsLocal()
//        }

        randomSortOrderModels()
        randomSortOrderLoRAs()

        delay(250L)
        progress(SyncRepository.Progress.SyncedModelsAndLoRAs)
    }

    private fun randomSortOrderModels(){
        val models = modelDao.getAll()
        models.forEach { model ->
            model.sortOrder = (-1000..1000).random()
        }
        modelDao.updates(*models.toTypedArray())
    }

    private fun randomSortOrderLoRAs(){
        val loRAGroups = loRAGroupDao.getAll()
        loRAGroups.forEach { loRAGroup ->
            loRAGroup.childs.forEach { loRA ->
                loRA.sortOrder = (-1000..1000).random()
            }
        }
        loRAGroupDao.updates(*loRAGroups.toTypedArray())
    }

    private fun randomSortOrderExplores(){
        val explores = exploreDao.getAll()
        explores.forEach { explore ->
            explore.sortOrder = (-1000..1000).random()
        }
        exploreDao.updates(*explores.toTypedArray())
    }

    private fun syncLoRAsLocal() {
        val inputStream = context.assets.open("loRA_v6.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val datas = tryOrNull { Gson().fromJson(bufferedReader, Array<LoRAGroup>::class.java) } ?: arrayOf()

        datas.forEach { loRAGroup ->
            loRAGroup.childs.forEach { loRA ->
                loRA.ratio = listOf("1:1", "2:3", "3:2").random()
            }
        }

        datas.forEach { loRAGroup ->
            loRAGroup.childs.forEach { loRA ->
                loRA.ratio = listOf("1:1", "2:3", "3:4").random()
            }
        }

        loRAGroupDao.deleteAll()
        loRAGroupDao.inserts(*datas.toList().shuffled().toTypedArray())

        prefs.versionLoRA.set(configApp.versionLoRA)
    }

    private fun syncModelsLocal() {
        val inputStream = context.assets.open("model_v7.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val datas = tryOrNull { Gson().fromJson(bufferedReader, Array<Model>::class.java) } ?: arrayOf()

        modelDao.deleteAll()
        modelDao.inserts(*datas.toList().shuffled().toTypedArray())

        Timber.e("Models size: ${datas.size}")

        prefs.versionModel.set(configApp.versionModel)
    }

    override suspend fun syncExplores(progress: (SyncRepository.Progress) -> Unit) = withContext(Dispatchers.IO) {
        progress(SyncRepository.Progress.Running)

//        val deferredExplores = async {
//            when {
//                prefs.versionExplore.get() < configApp.versionExplore || exploreDao.getAll().isEmpty() -> reference.child("v2/explores").get().await()
//                else -> null
//            }
//        }

//        val snapshotExplores = deferredExplores.await()

//        launch(Dispatchers.IO) {
//            snapshotExplores?.let { snapshot ->
//                val genericTypeIndicator = object : GenericTypeIndicator<List<Explore>>() {}
//                val datas = tryOrNull { snapshot.getValue(genericTypeIndicator) } ?: emptyList()
//
//                when {
//                    datas.isNotEmpty() -> {
//                        datas.forEach { explore ->
//                            explore.ratio = tryOrNull { explore.previews.firstOrNull()?.split("zzz")?.getOrNull(1)?.replace("xxx",":") } ?: "1:1"
//                        }
//
//                        exploreDao.deleteAll()
//                        exploreDao.inserts(*datas.shuffled().toTypedArray())
//
//                        prefs.versionExplore.set(configApp.versionExplore)
//                    }
//                    prefs.versionExplore.get() < configApp.versionExplore || exploreDao.getAll().isEmpty() -> syncExploresLocal()
//                }
//            } ?: run {
//                when {
//                    prefs.versionExplore.get() < configApp.versionExplore || exploreDao.getAll().isEmpty() -> syncExploresLocal()
//                }
//            }
//        }

        when {
            prefs.versionExplore.get() < configApp.versionExplore || exploreDao.getAll().isEmpty() -> syncExploresLocal()
        }

        randomSortOrderExplores()

        delay(250L)
        progress(SyncRepository.Progress.SyncedExplores)
    }

    private fun syncExploresLocal() {
        val inputStream = context.assets.open("explore_v3.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val datas = tryOrNull { Gson().fromJson(bufferedReader, Array<Explore>::class.java) } ?: arrayOf()

        datas.forEach { explore ->
            explore.ratio = tryOrNull { explore.previews.firstOrNull()?.split("zzz")?.getOrNull(1)?.replace("xxx",":") } ?: "1:1"
        }

        exploreDao.deleteAll()
        exploreDao.inserts(*datas.toList().shuffled().toTypedArray())

        prefs.versionExplore.set(configApp.versionExplore)
    }

}