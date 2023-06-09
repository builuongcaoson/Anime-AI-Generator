package com.sola.anime.ai.generator.inject.server

import com.google.gson.JsonElement
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface ServerApi {

    @Multipart
    @POST("sync-user.php")
    @Streaming
    suspend fun syncUser(
        @Part("appUserId") appUserId: RequestBody
    ): JsonElement?

    @Multipart
    @POST("update-created-artwork-in-day.php")
    @Streaming
    suspend fun updateCreatedArtworkInDay(
        @Part("appUserId") appUserId: RequestBody,
        @Part("numberCreated") numberCreated: RequestBody
    ): JsonElement?

    @Multipart
    @POST("insert-user-premium.php")
    @Streaming
    suspend fun insertUserPremium(
        @Part("appUserId") appUserId: RequestBody,
        @Part("timePurchased") timePurchased: RequestBody,
        @Part("timeExpired") timeExpired: RequestBody
    ): JsonElement?

}