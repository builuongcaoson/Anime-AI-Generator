package com.sola.anime.ai.generator.data.repo

import android.content.Context
import androidx.core.net.toUri
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.sola.anime.ai.generator.common.extension.downloadImage
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.model.upscale.BodyUpscale
import com.sola.anime.ai.generator.domain.repo.UpscaleApiRepository
import com.sola.anime.ai.generator.inject.upscale.UpscaleApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UpscaleApiRepositoryImpl @Inject constructor(
    private val context: Context,
    private val upscaleApi: UpscaleApi,
    private val analyticManager: AnalyticManager
): UpscaleApiRepository {

    override suspend fun upscale(file: File, done: (File?) -> Unit) = withContext(Dispatchers.IO){
        analyticManager.logEvent(AnalyticManager.TYPE.UPSCALE_CLICKED)

        val ref = Firebase.storage.reference.child("v1/upscale/${System.currentTimeMillis()}.png")
        val fileUrl = ref.putFile(file.toUri()).continueWithTask { task ->
            if (!task.isSuccessful) {
                return@continueWithTask null
            }
            ref.downloadUrl
        }.await()

        val fileUpscale = fileUrl?.let {
            val responseUpscale = withContext(Dispatchers.IO) {
                try {
                    val body = BodyUpscale().apply {
                        this.upscale = 3
                        this.image = fileUrl.toString()
                    }
                    upscaleApi.upscale(body)
                } catch (e: Exception){
                    null
                }
            }

            try {
                responseUpscale?.outputUrl?.let { downloadImage(context, it) }
            } catch (e: Exception){
                null
            }
        }

        when {
            fileUpscale != null && fileUpscale.exists() -> analyticManager.logEvent(AnalyticManager.TYPE.UPSCALE_SUCCESS)
            else -> analyticManager.logEvent(AnalyticManager.TYPE.UPSCALE_FAILED)
        }

        done(fileUpscale)
    }

}