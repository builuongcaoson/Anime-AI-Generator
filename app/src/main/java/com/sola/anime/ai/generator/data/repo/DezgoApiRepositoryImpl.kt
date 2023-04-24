package com.sola.anime.ai.generator.data.repo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.sola.anime.ai.generator.domain.model.status.GenerateTextsToImagesProgress
import com.sola.anime.ai.generator.domain.model.textToImage.BodyTextToImage
import com.sola.anime.ai.generator.domain.model.textToImage.DezgoTextToImage
import com.sola.anime.ai.generator.domain.model.textToImage.ResponseTextToImage
import com.sola.anime.ai.generator.domain.repo.DezgoApiRepository
import com.sola.anime.ai.generator.inject.dezgo.DezgoApi
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import kotlinx.coroutines.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.await
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
        datas: List<DezgoTextToImage>
    ) = withContext(Dispatchers.IO) {
        if (progressTextsToImages.blockingFirst().isLoading) return@withContext
        progressTextsToImages.onNext(GenerateTextsToImagesProgress.Loading)


        val abc = async {
            datas
                .chunked(5)
                .flatMap { childDatas ->
                val bitmaps = childDatas
                    .map { data ->
                        async(Dispatchers.IO) {
                            progressTextsToImages.onNext(GenerateTextsToImagesProgress.LoadingWithId(groupId = data.id, childId = data.dezgoBodyTextToImage.id))

                            val response = dezgoApi.text2image(
                                prompt = "a cute beautiful girl listening to relaxing music with her headphones that takes her to a surreal forest, young anime girl, long wavy blond hair, sky blue eyes, full round face, miniskirt, front view, mid - shot, highly detailed, digital art by wlop, trending on artstation".toRequestBody(MultipartBody.FORM),
                                negative_prompt = "(character out of frame)1.4, (worst quality)1.2, (low quality)1.6, (normal quality)1.6, lowres, (monochrome)1.1, (grayscale)1.3, acnes, skin blemishes, bad anatomy, DeepNegative,(fat)1.1, bad hands, text, error, missing fingers, extra limbs, missing limbs, extra digits, fewer digits, cropped, jpeg artifacts,signature, watermark, furry, elf ears".toRequestBody(MultipartBody.FORM),
                                guidance = "7.5".toRequestBody(MultipartBody.FORM),
                                upscale = "2".toRequestBody(MultipartBody.FORM),
                                sampler = "euler_a".toRequestBody(MultipartBody.FORM),
                                steps = "50".toRequestBody(MultipartBody.FORM),
                                model = "anything_4_0".toRequestBody(MultipartBody.FORM),
                                width = "320".toRequestBody(MultipartBody.FORM),
                                height = "320".toRequestBody(MultipartBody.FORM),
                                seed = "3107070471".toRequestBody(MultipartBody.FORM)
                            )

                            ResponseTextToImage(groupId = data.id, childId = data.dezgoBodyTextToImage.id, response = response)
                        }
                    }
                    .awaitAll()
                    .map { responseTextToImage ->
                        responseTextToImage.response.byteStream().use { inputStream ->
                            // Convert to bitmap
                            val options = BitmapFactory.Options()
                            options.inPreferredConfig = Bitmap.Config.ARGB_8888
                            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)

                            when {
                                bitmap != null -> {
                                    progressTextsToImages.onNext(GenerateTextsToImagesProgress.SuccessWithId(groupId = responseTextToImage.groupId, childId = responseTextToImage.childId, bitmap = bitmap))
                                }
                                else -> {
                                    progressTextsToImages.onNext(GenerateTextsToImagesProgress.FailureWithId(groupId = responseTextToImage.groupId, childId = responseTextToImage.childId))
                                }
                            }
                            Timber.e("Bitmap width: ${bitmap?.width} --- ${bitmap?.height}")

                            bitmap
                        }
                    }
                delay(5000)
                bitmaps
            }
        }.await()


    }

}