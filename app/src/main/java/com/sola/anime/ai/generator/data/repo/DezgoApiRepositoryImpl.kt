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
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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
            .forEachIndexed { index: Int, bodies ->
                bodies
                    .map { body ->
                        async {
                            progress(GenerateTextsToImagesProgress.LoadingWithId(body = body))

                            val style = styleDao.findById(body.styleId)
                            val prompt = when {
                                style != null -> body.prompt + style.prompts.random()
                                else -> body.prompt
                            }
                            val negativePrompt = when {
                                style != null -> body.negativePrompt + ""
                                else -> body.negativePrompt
                            }

                            try {
                                val response = dezgoApi.text2image(
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

                                ResponseTextToImage(body = body, response = response)
                            } catch (e: Exception){
                                e.printStackTrace()
                                ResponseTextToImage(body = body)
                            }
                        }
                    }.map {
                        val response = it.await()

                        response
                            .response
                            ?.byteStream()
                            ?.use { inputStream -> tryOrNull { inputStream.toFile(context) } }
                            ?.takeIf { file -> file.exists() }
                            ?.let { file ->
                                val doOnSuccess = suspendCoroutine { continuation ->
                                    doOnSuccess { result ->
                                        continuation.resume(result)
                                    }
                                }

                                progress(GenerateTextsToImagesProgress.SuccessWithId(body = response.body, file = file))
                        } ?: run {
                            val doOnFailed = suspendCoroutine { continuation ->
                                doOnFailed { result ->
                                    continuation.resume(result)
                                }
                            }

                            progress(GenerateTextsToImagesProgress.FailureWithId(body = response.body))
                        }
                    }

                delay(if (dataChunked.lastIndex == index) 0 else 3000)
            }

        progress(GenerateTextsToImagesProgress.Done)
        delay(1000)
        progress(GenerateTextsToImagesProgress.Idle)
    }

    private fun doOnSuccess(done: (Boolean) -> Unit){
        done(true)
    }

    private fun doOnFailed(done: (Boolean) -> Unit){
        done(true)
    }

    override suspend fun generateImagesToImages(
        contentUri: Uri
    ) {

//        val requestFile = contentUri.contentUriToRequestBody(context) ?: return
//        val body = MultipartBody.Part.createFormData("init_image", contentUri.authority, requestFile)
//
//        val response = dezgoApi.image2image(
//            prompt = "body".toRequestBody(),
//            negativePrompt = "Hello".toRequestBody(),
//            guidance = "7.5".toRequestBody(),
//            upscale = "1".toRequestBody(),
//            sampler = "euler_a".toRequestBody(),
//            steps = "10".toRequestBody(),
//            model = "anything_4_0".toRequestBody(),
//            seed = "645524234".toRequestBody(),
//            strength = "0.5".toRequestBody(),
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