package com.sola.anime.ai.generator.inject.dezgo

import com.sola.anime.ai.generator.common.Constraint
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
        @Header(Constraint.Dezgo.HEADER_KEY) headerKey: String,
        @Part("prompt") prompt: RequestBody,
        @Part("negative_prompt") negativePrompt: RequestBody,
        @Part("guidance") guidance: RequestBody,
        @Part("upscale") upscale: RequestBody,
        @Part("sampler") sampler: RequestBody,
        @Part("steps") steps: RequestBody,
        @Part("model") model: RequestBody,
        @Part("width") width: RequestBody,
        @Part("height") height: RequestBody,
        @Part("lora1") lora1: RequestBody?,
        @Part("lora1_strength") lora1Strength: RequestBody?,
        @Part("lora2") lora2: RequestBody?,
        @Part("lora2_strength") lora2Strength: RequestBody?,
        @Part("seed") seed: RequestBody?
    ): ResponseBody

    @Multipart
    @POST("image2image")
    @Streaming
    suspend fun image2image(
        @Header(Constraint.Dezgo.HEADER_KEY) headerKey: String,
        @Part("prompt") prompt: RequestBody,
        @Part("negative_prompt") negativePrompt: RequestBody,
        @Part("guidance") guidance: RequestBody,
        @Part("upscale") upscale: RequestBody,
        @Part("sampler") sampler: RequestBody,
        @Part("steps") steps: RequestBody,
        @Part("model") model: RequestBody,
        @Part("strength") strength: RequestBody,
        @Part("lora1") lora1: RequestBody?,
        @Part("lora1_strength") lora1Strength: RequestBody?,
        @Part("lora2") lora2: RequestBody?,
        @Part("lora2_strength") lora2Strength: RequestBody?,
        @Part file: MultipartBody.Part,
        @Part("seed") seed: RequestBody?
    ): ResponseBody

}