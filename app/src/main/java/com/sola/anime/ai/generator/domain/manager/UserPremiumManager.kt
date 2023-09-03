package com.sola.anime.ai.generator.domain.manager

import com.sola.anime.ai.generator.domain.model.config.userPurchased.UserPurchased

interface UserPremiumManager {

    suspend fun addOrUpdatePurchasedToDatabase(packagePurchased: String, timePurchased: Long, timeExpired: Long): UserPurchased?

    suspend fun syncUserPurchasedFromDatabase(): UserPurchased?

}