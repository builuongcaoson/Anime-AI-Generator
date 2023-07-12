package com.sola.anime.ai.generator.data.repo

import android.content.Context
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.*
import com.sola.anime.ai.generator.common.util.AESEncyption
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.domain.model.status.GenerateImagesToImagesProgress
import com.sola.anime.ai.generator.domain.model.status.GenerateTextsToImagesProgress
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyImageToImage
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage
import com.sola.anime.ai.generator.domain.model.textToImage.ResponseImageToImage
import com.sola.anime.ai.generator.domain.model.textToImage.ResponseTextToImage
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.inject.dezgo.DezgoApi
import kotlinx.coroutines.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DezgoApiRepositoryImpl @Inject constructor(
    private val context: Context,
    private val dezgoApi: DezgoApi,
    private val styleDao: StyleDao
): DezgoApiRepository {

    override suspend fun generateTextsToImages(
        keyApi: String,
        subNegative: String,
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

                            val style = styleDao.findById(body.styleId)
                            val prompt = when {
                                style != null -> body.prompt + style.prompts.random()
                                else -> body.prompt
                            }

                            val negativePrompt = when {
                                body.negativePrompt.endsWith(",") -> body.negativePrompt + " " + subNegative
                                else -> body.negativePrompt + ", " + subNegative
                            }

                            try {
                                val response = dezgoApi.text2image(
                                    headerKey = keyApi,
                                    prompt = prompt.toRequestBody(MultipartBody.FORM),
                                    negativePrompt = negativePrompt.toRequestBody(MultipartBody.FORM),
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
                            } catch (e: Exception){
                                e.printStackTrace()
                                ResponseTextToImage(groupId = body.groupId, childId = body.id)
                            }
                        }
                    }.map {
                        val responseTextToImage = it.await()

                        responseTextToImage.response?.byteStream()?.use { inputStream ->
                            // Convert to file
                            val file = tryOrNull { inputStream.toFile(context) }

                            when {
                                file != null && file.exists() -> {
                                    progress(GenerateTextsToImagesProgress.SuccessWithId(groupId = responseTextToImage.groupId, childId = responseTextToImage.childId, file = file))
                                }
                                else -> {
                                    progress(GenerateTextsToImagesProgress.FailureWithId(groupId = responseTextToImage.groupId, childId = responseTextToImage.childId))
                                }
                            }

                            file
                        } ?: run {
                            progress(GenerateTextsToImagesProgress.FailureWithId(groupId = responseTextToImage.groupId, childId = responseTextToImage.childId))
                        }
                    }
                delay(if (dataChunked.lastIndex == index) 0 else 5000)
                responses
            }

        progress(GenerateTextsToImagesProgress.Done)
        delay(1000)
        progress(GenerateTextsToImagesProgress.Idle)
    }

    override suspend fun generateImagesToImages(
        keyApi: String,
        subNegative: String,
        datas: List<DezgoBodyImageToImage>,
        progress: (GenerateImagesToImagesProgress) -> Unit
    ) = withContext(Dispatchers.IO) {
        progress(GenerateImagesToImagesProgress.Loading)
        delay(250)
        val dataChunked = datas.flatMap { it.bodies }.chunked(5)
        dataChunked
            .flatMapIndexed { index: Int, bodies ->
                val responses = bodies
                    .map { body ->
                        async {
                            progress(GenerateImagesToImagesProgress.LoadingWithId(groupId = body.groupId, childId = body.id))

                            val style = styleDao.findById(body.styleId)
                            val prompt = when {
                                style != null -> body.prompt + style.prompts.random()
                                else -> body.prompt
                            }

                            val negativePrompt = when {
                                body.negativePrompt.endsWith(",") -> body.negativePrompt + " " + subNegative
                                else -> body.negativePrompt + ", " + subNegative
                            }

                            try {
                                val photoRequestBody = body.initImage.toRequestBody(context)
                                val photoPart = MultipartBody.Part.createFormData("init_image", "${System.currentTimeMillis()}.png", photoRequestBody!!)

                                val response = dezgoApi.image2image(
                                    headerKey = keyApi,
                                    prompt = prompt.toRequestBody(MultipartBody.FORM),
                                    negativePrompt = negativePrompt.toRequestBody(MultipartBody.FORM),
                                    guidance = body.guidance.toRequestBody(MultipartBody.FORM),
                                    upscale = body.upscale.toRequestBody(MultipartBody.FORM),
                                    sampler = body.sampler.toRequestBody(MultipartBody.FORM),
                                    steps = body.steps.toRequestBody(MultipartBody.FORM),
                                    model = body.model.toRequestBody(MultipartBody.FORM),
                                    seed = body.seed?.toRequestBody(MultipartBody.FORM),
                                    strength = body.strength.toRequestBody(MultipartBody.FORM),
                                    file = photoPart
                                )

                                ResponseImageToImage(groupId = body.groupId, childId = body.id, photoUri = body.initImage, response = response)
                            } catch (e: Exception){
                                Timber.e("Error: $e")
                                e.printStackTrace()
                                ResponseImageToImage(groupId = body.groupId, childId = body.id)
                            }
                        }
                    }.map {
                        val responseImg2Img = it.await()

                        responseImg2Img.response?.byteStream()?.use { inputStream ->
                            // Convert to file
                            val file = tryOrNull { inputStream.toFile(context) }

                            when {
                                file != null && file.exists() && responseImg2Img.photoUri != null -> {
                                    progress(GenerateImagesToImagesProgress.SuccessWithId(groupId = responseImg2Img.groupId, childId = responseImg2Img.childId, photoUri = responseImg2Img.photoUri, file = file))
                                }
                                else -> {
                                    progress(GenerateImagesToImagesProgress.FailureWithId(groupId = responseImg2Img.groupId, childId = responseImg2Img.childId))
                                }
                            }

                            file
                        } ?: run {
                            progress(GenerateImagesToImagesProgress.FailureWithId(groupId = responseImg2Img.groupId, childId = responseImg2Img.childId))
                        }
                    }
                delay(if (dataChunked.lastIndex == index) 0 else 5000)
                responses
            }

        progress(GenerateImagesToImagesProgress.Done)
        delay(1000)
        progress(GenerateImagesToImagesProgress.Idle)
    }

}