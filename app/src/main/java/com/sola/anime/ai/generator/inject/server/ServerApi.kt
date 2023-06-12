package com.sola.anime.ai.generator.inject.server

import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ServerApi {

    @Multipart
    @POST("sync-user-v2.php")
    @Streaming
    suspend fun syncUser(
        @Part("deviceId") deviceId: RequestBody,
    ): JsonElement?

    @Multipart
    @POST("update-created-artwork-in-day.php")
    @Streaming
    suspend fun updateCreatedArtworkInDay(
        @Part("deviceId") deviceId: RequestBody,
        @Part("numberCreated") numberCreated: RequestBody
    ): JsonElement?

}