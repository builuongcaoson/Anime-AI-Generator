package com.sola.anime.ai.generator.domain.repo

interface ServerApiRepository {

    suspend fun syncUser(appUserId: String, success: () -> Unit)

    suspend fun insertUserPremium(appUserId: String, timePurchased: String, timeExpired: String, success: () -> Unit)

    suspend fun updateCreatedArtworkInDay()

}