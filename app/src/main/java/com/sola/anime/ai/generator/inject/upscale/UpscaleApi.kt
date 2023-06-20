package com.sola.anime.ai.generator.inject.upscale

import com.sola.anime.ai.generator.domain.model.upscale.BodyUpscale
import com.sola.anime.ai.generator.domain.model.upscale.ResponseUpscale
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.*

interface UpscaleApi {

    @POST("run")
    suspend fun upscale(
        @Body body: BodyUpscale
    ): ResponseUpscale?

}