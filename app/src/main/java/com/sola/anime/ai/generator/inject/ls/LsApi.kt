package com.sola.anime.ai.generator.inject.ls

import com.sola.anime.ai.generator.common.Constraint
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface LsApi {

    @Multipart
    @POST("text2img.php")
    @Streaming
    suspend fun text2image(
        @Part("width") width: RequestBody,
        @Part("height") height: RequestBody,
        @Part("prompt") prompt: RequestBody,
        @Part("negative_prompt") negativePrompt: RequestBody,
        @Part("guidance") guidance: RequestBody,
        @Part("upscale") upscale: RequestBody,
        @Part("sampler") sampler: RequestBody,
        @Part("steps") steps: RequestBody,
        @Part("model") model: RequestBody,
        @Part("key") key: RequestBody
    ): ResponseBody

    @Multipart
    @POST("img2img.php")
    @Streaming
    suspend fun image2image(
        @Part("prompt") prompt: RequestBody,
        @Part("negative_prompt") negativePrompt: RequestBody,
        @Part("guidance") guidance: RequestBody,
        @Part("upscale") upscale: RequestBody,
        @Part("sampler") sampler: RequestBody,
        @Part("steps") steps: RequestBody,
        @Part("model") model: RequestBody,
        @Part("key") key: RequestBody,
        @Part("strength") strength: RequestBody,
        @Part file: MultipartBody.Part
    ): ResponseBody

}