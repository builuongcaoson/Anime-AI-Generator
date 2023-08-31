package com.sola.anime.ai.generator.domain.repo

import com.sola.anime.ai.generator.domain.model.server.UserPremium

interface ServerApiRepository {

//    suspend fun syncUser(success: (UserPremium?) -> Unit)

//    suspend fun updateCreatedArtworkInDay()

    suspend fun promoCode(promoCode: String, success: (isActive: Boolean, promo: String) -> Unit, failed: () -> Unit)

}