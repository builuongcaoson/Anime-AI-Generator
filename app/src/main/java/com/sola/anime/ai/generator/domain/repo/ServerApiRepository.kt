package com.sola.anime.ai.generator.domain.repo


interface ServerApiRepository {

    suspend fun promoCode(promoCode: String, success: (isActive: Boolean, promo: String) -> Unit, failed: () -> Unit)

}