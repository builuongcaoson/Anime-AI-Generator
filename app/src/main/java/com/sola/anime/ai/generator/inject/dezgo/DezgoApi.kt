package com.sola.anime.ai.generator.inject.dezgo

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface DezgoApi {

    @Multipart
    @POST("text2image")
    @Streaming
    suspend fun text2image(
        @Part("prompt") prompt: RequestBody,
        @Part("negative_prompt") negative_prompt: RequestBody,
        @Part("guidance") guidance: RequestBody,
        @Part("upscale") upscale: RequestBody,
        @Part("sampler") sampler: RequestBody,
        @Part("steps") steps: RequestBody,
        @Part("model") model: RequestBody,
        @Part("width") width: RequestBody,
        @Part("height") height: RequestBody,
        @Part("seed") seed: RequestBody
    ): ResponseBody

    @Multipart
    @POST("image2image")
    @Streaming
    suspend fun image2image(
        @Part("prompt") prompt: RequestBody,
        @Part("negative_prompt") negative_prompt: RequestBody,
        @Part("guidance") guidance: RequestBody,
        @Part("upscale") upscale: RequestBody,
        @Part("sampler") sampler: RequestBody,
        @Part("steps") steps: RequestBody,
        @Part("model") model: RequestBody,
        @Part("seed") seed: RequestBody,
        @Part("strength") strength: RequestBody,
        @Part file: MultipartBody.Part
    ): ResponseBody

}