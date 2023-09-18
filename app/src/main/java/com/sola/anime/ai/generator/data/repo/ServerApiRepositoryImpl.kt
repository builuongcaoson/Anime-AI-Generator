package com.sola.anime.ai.generator.data.repo

import android.content.Context
import com.basic.common.extension.tryOrNull
import com.google.gson.Gson
import com.sola.anime.ai.generator.common.extension.deviceId
import com.sola.anime.ai.generator.domain.model.server.PromoCode
import com.sola.anime.ai.generator.domain.repo.ServerApiRepository
import com.sola.anime.ai.generator.inject.server.ServerApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServerApiRepositoryImpl @Inject constructor(
    private val context: Context,
    private val serverApi: ServerApi
): ServerApiRepository {

    override suspend fun promoCode(promoCode: String, success: (isActive: Boolean, promo: String) -> Unit, failed: () -> Unit) {
        val json = withContext(Dispatchers.IO) {
            try {
                serverApi.promoCode(context.deviceId().toRequestBody(), promoCode.toRequestBody())
            } catch (e: Exception){
                e.printStackTrace()
                null
            }
        } ?: run {
            failed()
            return
        }
        val response = tryOrNull { Gson().fromJson(json, PromoCode::class.java) }
        when {
            response?.isActive != null && response.promo != null -> {
                success(response.isActive == "true", response.promo)
            }
            else -> {
                failed()
            }
        }

        Timber.tag("Main11111").e("##### PROMO CODE #####")
        Timber.tag("Main11111").e("PromoCode: $promoCode")
        Timber.tag("Main11111").e("IsActive: ${response?.isActive}")
    }

}