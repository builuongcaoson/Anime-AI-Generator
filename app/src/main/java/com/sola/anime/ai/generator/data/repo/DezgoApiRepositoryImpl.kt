package com.sola.anime.ai.generator.data.repo

import android.content.Context
import com.sola.anime.ai.generator.common.extension.toBitmap
import com.sola.anime.ai.generator.common.extension.toFile
import com.sola.anime.ai.generator.domain.model.status.GenerateTextsToImagesProgress
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage
import com.sola.anime.ai.generator.domain.model.textToImage.ResponseTextToImage
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.inject.dezgo.DezgoApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DezgoApiRepositoryImpl @Inject constructor(
    private val context: Context,
    private val dezgoApi: DezgoApi
): DezgoApiRepository {

    override suspend fun generateTextsToImages(
        datas: List<DezgoBodyTextToImage>,
        progress: (GenerateTextsToImagesProgress) -> Unit
    ) = withContext(Dispatchers.IO) {
        progress(GenerateTextsToImagesProgress.Loading)
        delay(250)
        val dataChunked = datas.flatMap { it.bodies }.chunked(5)
        dataChunked
            .flatMapIndexed { index: Int, bodies ->
                val responses = bodies
                    .map { body ->
                        async {
                            progress(GenerateTextsToImagesProgress.LoadingWithId(groupId = body.groupId, childId = body.id))

                            Timber.e("Loading group id: ${body.groupId} --- Child id: ${body.id}")

                            val response = dezgoApi.text2image(
                                prompt = body.prompt.toRequestBody(MultipartBody.FORM),
                                negative_prompt = body.negative_prompt.toRequestBody(MultipartBody.FORM),
                                guidance = body.guidance.toRequestBody(MultipartBody.FORM),
                                upscale = body.upscale.toRequestBody(MultipartBody.FORM),
                                sampler = body.sampler.toRequestBody(MultipartBody.FORM),
                                steps = body.steps.toRequestBody(MultipartBody.FORM),
                                model = body.model.toRequestBody(MultipartBody.FORM),
                                width = body.width.toRequestBody(MultipartBody.FORM),
                                height = body.height.toRequestBody(MultipartBody.FORM),
                                seed = body.seed?.toRequestBody(MultipartBody.FORM)
                            )

                            ResponseTextToImage(groupId = body.groupId, childId = body.id, response = response)
                        }
                    }.map {
                        val responseTextToImage = it.await()

                        responseTextToImage.response.byteStream().use { inputStream ->
                            // Convert to bitmap
                            val bitmap = inputStream.toBitmap()
                            val file = bitmap?.toFile(context)

                            when {
                                bitmap != null && file != null -> {
                                    progress(GenerateTextsToImagesProgress.SuccessWithId(groupId = responseTextToImage.groupId, childId = responseTextToImage.childId, bitmap = bitmap, file = file))
                                }
                                else -> {
                                    progress(GenerateTextsToImagesProgress.FailureWithId(groupId = responseTextToImage.groupId, childId = responseTextToImage.childId))
                                }
                            }

                            bitmap
                        }
                    }
                Timber.e("Chunked: ${dataChunked.lastIndex} --- $index")
                delay(if (dataChunked.lastIndex == index) 0 else 5000)
                responses
            }

        progress(GenerateTextsToImagesProgress.Done)
        delay(1000)
        progress(GenerateTextsToImagesProgress.Idle)
    }

}