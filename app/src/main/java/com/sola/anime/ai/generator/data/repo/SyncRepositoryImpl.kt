package com.sola.anime.ai.generator.data.repo

import android.content.Context
import com.basic.common.extension.tryOrNull
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.LoRAGroupDao
import com.sola.anime.ai.generator.data.db.query.ModelDao
import com.sola.anime.ai.generator.domain.model.config.lora.LoRAGroup
import com.sola.anime.ai.generator.domain.model.config.model.Model
import com.sola.anime.ai.generator.domain.repo.SyncRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepositoryImpl @Inject constructor(
    private val context: Context,
    private val prefs: Preferences,
    private val configApp: ConfigApp,
    private val modelDao: ModelDao,
    private val loRAGroupDao: LoRAGroupDao
): SyncRepository {

    override suspend fun syncModelsAndLoRAs(progress: (SyncRepository.Progress) -> Unit) = withContext(Dispatchers.IO) {
        val deferredModels = async {
            when {
                prefs.versionModel.get() < configApp.versionModel || modelDao.getAll().isEmpty() -> Firebase.database.reference.child("v2/models").get().await()
                else -> null
            }
        }
        val deferredLoRAs = async {
            when {
                prefs.versionLoRA.get() < configApp.versionLoRA || loRAGroupDao.getAll().isEmpty() -> Firebase.database.reference.child("v2/loRAs").get().await()
                else -> null
            }
        }

        val (snapshotModels, snapshotLoRAs) = awaitAll(deferredModels, deferredLoRAs)

        snapshotModels?.let { snapshot ->
            val genericTypeIndicator = object : GenericTypeIndicator<List<Model>>() {}
            val datas = tryOrNull { snapshot.getValue(genericTypeIndicator) } ?: emptyList()

            when {
                datas.isNotEmpty() -> {
                    modelDao.deleteAll()
                    modelDao.inserts(*datas.toTypedArray())
                }
                else -> syncModelsLocal()
            }

            prefs.versionModel.set(configApp.versionModel)
        } ?: run {
            syncModelsLocal()
        }

        snapshotLoRAs?.let { snapshot ->
            val genericTypeIndicator = object : GenericTypeIndicator<List<LoRAGroup>>() {}
            val datas = tryOrNull { snapshot.getValue(genericTypeIndicator) } ?: emptyList()

            when {
                datas.isNotEmpty() -> {
                    loRAGroupDao.deleteAll()
                    loRAGroupDao.inserts(*datas.toTypedArray())
                }
                else -> syncLoRAsLocal()
            }

            prefs.versionLoRA.set(configApp.versionLoRA)
        } ?: run {
            syncLoRAsLocal()
        }
    }

    private fun syncLoRAsLocal() {
        val inputStream = context.assets.open("loRA_v2.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<LoRAGroup>::class.java) } ?: arrayOf()

        loRAGroupDao.deleteAll()
        loRAGroupDao.inserts(*data)
    }

    private fun syncModelsLocal() {
        val inputStream = context.assets.open("model_v2.json")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        val data = tryOrNull { Gson().fromJson(bufferedReader, Array<Model>::class.java) } ?: arrayOf()

        modelDao.deleteAll()
        modelDao.inserts(*data)
    }

    override fun syncExplore() {

    }

}