package com.sola.anime.ai.generator.domain.repo

import com.sola.anime.ai.generator.domain.model.server.UserPremium

interface ServerApiRepository {

    suspend fun syncUser(appUserId: String, success: (UserPremium?) -> Unit)

    suspend fun insertUserPremium(appUserId: String, timePurchased: String, timeExpired: String, success: () -> Unit)

    suspend fun updateCreatedArtworkInDay()

}