package com.sola.anime.ai.generator.data.repo

import android.content.Context
import android.net.Uri
import android.util.Log
import com.basic.common.extension.tryOrNull
import com.sola.anime.ai.generator.common.extension.contentUriToRequestBody
import com.sola.anime.ai.generator.common.extension.toBitmap
import com.sola.anime.ai.generator.common.extension.toFile
import com.sola.anime.ai.generator.data.db.query.StyleDao
import com.sola.anime.ai.generator.domain.model.status.GenerateTextsToImagesProgress
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage
import com.sola.anime.ai.generator.domain.model.textToImage.ResponseTextToImage
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.inject.dezgo.DezgoApi
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DezgoApiRepositoryImpl @Inject constructor(
    private val context: Context,
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
                                style != null -> body.negativePrompt + ""
                                else -> body.negativePrompt
                            }

                            val response = dezgoApi.text2image(
                                prompt = prompt.toRequestBody(MultipartBody.FORM),
                                negativePrompt = negativePrompt.toRequestBody(MultipartBody.FORM),
                                guidance = body.guidance.toRequestBody(MultipartBody.FORM),
                                upscale = body.upscale.toRequestBody(MultipartBody.FORM),
                                sampler = body.sampler.toRequestBody(MultipartBody.FORM),
                                steps = body.steps.toRequestBody(MultipartBody.FORM),
                                model = body.model.toRequestBody(MultipartBody.FORM),
                                width = body.width.toRequestBody(MultipartBody.FORM),
                                height = body.height.toRequestBody(MultipartBody.FORM),
                                seed = body.seed.toRequestBody(MultipartBody.FORM)
                            )

                            ResponseTextToImage(groupId = body.groupId, childId = body.id, response = response)
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
        contentUri: Uri
    ) {

//        val requestFile = contentUri.contentUriToRequestBody(context) ?: return
//        val body = MultipartBody.Part.createFormData("init_image", contentUri.authority, requestFile)
//
//        val response = dezgoApi.image2image(
//            prompt = "body".toRequestBody(MultipartBody.FORM),
//            negativePrompt = "Hello".toRequestBody(MultipartBody.FORM),
//            guidance = "7.5".toRequestBody(MultipartBody.FORM),
//            upscale = "1".toRequestBody(MultipartBody.FORM),
//            sampler = "euler_a".toRequestBody(MultipartBody.FORM),
//            steps = "10".toRequestBody(MultipartBody.FORM),
//            model = "anything_4_0".toRequestBody(MultipartBody.FORM),
//            seed = "645524234".toRequestBody(MultipartBody.FORM),
//            strength = "0.5".toRequestBody(MultipartBody.FORM),
//            file = body
//        )

//        response.byteStream().use { inputStream ->
            // Convert to bitmap
//            val bitmap = inputStream.toBitmap()
//            val file = bitmap?.toFile(context)

//            when {
//                bitmap != null && file != null -> {
//                    progress(GenerateTextsToImagesProgress.SuccessWithId(groupId = responseTextToImage.groupId, childId = responseTextToImage.childId, bitmap = bitmap, file = file))
//                }
//                else -> {
//                    progress(GenerateTextsToImagesProgress.FailureWithId(groupId = responseTextToImage.groupId, childId = responseTextToImage.childId))
//                }
//            }

//            Timber.e("Bitmap size: ${bitmap?.width} --- ${bitmap?.height}")

//            bitmap
//        }

//        response.byteStream()
    }

}