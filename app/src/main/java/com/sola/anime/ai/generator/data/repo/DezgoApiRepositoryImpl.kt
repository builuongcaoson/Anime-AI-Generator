package com.sola.anime.ai.generator.data.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.sola.anime.ai.generator.common.extension.toBitmap
import com.sola.anime.ai.generator.common.extension.toFile
import com.sola.anime.ai.generator.domain.model.status.GenerateTextsToImagesProgress
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoBodyTextToImage
import com.sola.anime.ai.generator.domain.model.textToImage.ResponseTextToImage
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.inject.dezgo.DezgoApi
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
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

    private val progressTextsToImages: Subject<GenerateTextsToImagesProgress> =
        BehaviorSubject.createDefault(GenerateTextsToImagesProgress.Idle)

    override fun progress(): Observable<GenerateTextsToImagesProgress> = progressTextsToImages

    override suspend fun generateTextsToImages(
        datas: List<DezgoBodyTextToImage>
    ) = withContext(Dispatchers.IO) {
        if (progressTextsToImages.blockingFirst().isLoading) return@withContext
        progressTextsToImages.onNext(GenerateTextsToImagesProgress.Loading)

        val dataChunked = datas.flatMap { it.bodies }.chunked(5)
        dataChunked
            .flatMapIndexed { index: Int, bodies ->
                val responses = bodies
                    .map { body ->
                        async {
                            progressTextsToImages.onNext(GenerateTextsToImagesProgress.LoadingWithId(groupId = body.groupId, childId = body.id))

                            Timber.e("Loading group id: ${body.groupId} --- Child id: ${body.id}")

                            val response = dezgoApi.text2image(
                                prompt = body.prompt.toRequestBody(MultipartBody.FORM),
                                negative_prompt = body.negative_prompt.toRequestBody(MultipartBody.FORM),
                                guidance = "7.5".toRequestBody(MultipartBody.FORM),
                                upscale = "1".toRequestBody(MultipartBody.FORM),
                                sampler = "euler_a".toRequestBody(MultipartBody.FORM),
                                steps = "50".toRequestBody(MultipartBody.FORM),
                                model = "anything_4_0".toRequestBody(MultipartBody.FORM),
                                width = "320".toRequestBody(MultipartBody.FORM),
                                height = "320".toRequestBody(MultipartBody.FORM),
                                seed = "3107070471".toRequestBody(MultipartBody.FORM)
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
                                    progressTextsToImages.onNext(GenerateTextsToImagesProgress.SuccessWithId(groupId = responseTextToImage.groupId, childId = responseTextToImage.childId, bitmap = bitmap, file = file))
                                }
                                else -> {
                                    progressTextsToImages.onNext(GenerateTextsToImagesProgress.FailureWithId(groupId = responseTextToImage.groupId, childId = responseTextToImage.childId))
                                }
                            }

                            bitmap
                        }
                    }
                Timber.e("Chunked: ${dataChunked.lastIndex} --- $index")
                delay(if (dataChunked.lastIndex == index) 0 else 5000)
                responses
            }

        progressTextsToImages.onNext(GenerateTextsToImagesProgress.Done)
        delay(1000)
        progressTextsToImages.onNext(GenerateTextsToImagesProgress.Idle)
    }

}