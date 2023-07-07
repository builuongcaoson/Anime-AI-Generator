package com.sola.anime.ai.generator.data.repo

import android.content.Context
import android.net.Uri
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.BuildConfig
import com.sola.anime.ai.generator.common.ConfigApp
import com.sola.anime.ai.generator.common.Constraint
import com.sola.anime.ai.generator.common.extension.contentUriToRequestBody
import com.sola.anime.ai.generator.common.extension.getDeviceId
import com.sola.anime.ai.generator.common.extension.toFile
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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DezgoApiRepositoryImpl @Inject constructor(
    private val context: Context,
    private val configApp: ConfigApp,
    private val prefs: Preferences,
    private val dezgoApi: DezgoApi,
    private val styleDao: StyleDao
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

                            val style = styleDao.findById(body.styleId)
                            val prompt = when {
                                style != null -> body.prompt + style.prompts.random()
                                else -> body.prompt
                            }
                            val negativePrompt = when {
                                style != null -> body.negativePrompt + ", " + context.getDeviceId()
                                else -> body.negativePrompt + ", " + context.getDeviceId()
                            }

                            try {
                                val decryptKey = when {
                                    (!BuildConfig.DEBUG || BuildConfig.SCRIPT) && !prefs.isUpgraded.get() -> AESEncyption.decrypt(Constraint.Dezgo.KEY) ?: ""
                                    (!BuildConfig.DEBUG || BuildConfig.SCRIPT) && prefs.isUpgraded.get() -> AESEncyption.decrypt(Constraint.Dezgo.KEY_PREMIUM) ?: ""
                                    else -> AESEncyption.decrypt(Constraint.Dezgo.RAPID_KEY) ?: ""
                                }


                                val response = dezgoApi.text2image(
                                    headerKey = decryptKey,
                                    prompt = prompt.toRequestBody(),
                                    negativePrompt = negativePrompt.toRequestBody(),
                                    guidance = body.guidance.toRequestBody(),
                                    upscale = body.upscale.toRequestBody(),
                                    sampler = body.sampler.toRequestBody(),
                                    steps = body.steps.toRequestBody(),
                                    model = body.model.toRequestBody(),
                                    width = body.width.toRequestBody(),
                                    height = body.height.toRequestBody(),
                                    seed = body.seed?.toRequestBody()
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
                                style != null -> body.negativePrompt + ", " + context.getDeviceId()
                                else -> body.negativePrompt + ", " + context.getDeviceId()
                            }

                            try {
                                val photoRequestBody = body.initImage.contentUriToRequestBody(context)
                                val photoPart = MultipartBody.Part.createFormData("init_image", body.initImage.authority, photoRequestBody!!)

                                val decryptKey = when {
                                    (!BuildConfig.DEBUG || BuildConfig.SCRIPT) && !prefs.isUpgraded.get() -> AESEncyption.decrypt(Constraint.Dezgo.KEY) ?: ""
                                    (!BuildConfig.DEBUG || BuildConfig.SCRIPT) && prefs.isUpgraded.get() -> AESEncyption.decrypt(Constraint.Dezgo.KEY_PREMIUM) ?: ""
                                    else -> AESEncyption.decrypt(Constraint.Dezgo.RAPID_KEY) ?: ""
                                }

                                val response = dezgoApi.image2image(
                                    headerKey = decryptKey,
                                    prompt = prompt.toRequestBody(),
                                    negativePrompt = negativePrompt.toRequestBody(),
                                    guidance = body.guidance.toRequestBody(),
                                    upscale = body.upscale.toRequestBody(),
                                    sampler = body.sampler.toRequestBody(),
                                    steps = body.steps.toRequestBody(),
                                    model = body.model.toRequestBody(),
                                    seed = body.seed?.toRequestBody(),
                                    strength = body.strength.toRequestBody(),
                                    file = photoPart
                                )

                                ResponseImageToImage(groupId = body.groupId, childId = body.id, response = response)
                            } catch (e: Exception){
                                e.printStackTrace()
                                ResponseImageToImage(groupId = body.groupId, childId = body.id)
                            }
                        }
                    }.map {
                        val responseTextToImage = it.await()

                        responseTextToImage.response?.byteStream()?.use { inputStream ->
                            // Convert to file
                            val file = tryOrNull { inputStream.toFile(context) }

                            when {
                                file != null && file.exists() -> {
                                    progress(GenerateImagesToImagesProgress.SuccessWithId(groupId = responseTextToImage.groupId, childId = responseTextToImage.childId, file = file))
                                }
                                else -> {
                                    progress(GenerateImagesToImagesProgress.FailureWithId(groupId = responseTextToImage.groupId, childId = responseTextToImage.childId))
                                }
                            }

                            file
                        } ?: run {
                            progress(GenerateImagesToImagesProgress.FailureWithId(groupId = responseTextToImage.groupId, childId = responseTextToImage.childId))
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