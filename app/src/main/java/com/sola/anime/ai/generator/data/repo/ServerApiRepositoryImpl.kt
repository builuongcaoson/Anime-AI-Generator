package com.sola.anime.ai.generator.data.repo

import android.content.Context
import com.basic.common.extension.tryOrNull
import com.google.gson.Gson
import com.sola.anime.ai.generator.data.Preferences
import com.sola.anime.ai.generator.domain.manager.AnalyticManager
import com.sola.anime.ai.generator.domain.model.server.Message
import com.sola.anime.ai.generator.domain.model.server.UserPremium
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
    private val serverApi: ServerApi,
    private val prefs: Preferences,
    private val analyticManager: AnalyticManager
): ServerApiRepository {

    override suspend fun syncUser(appUserId: String, success: () -> Unit) {
        val json = withContext(Dispatchers.IO) {
            serverApi.syncUser(appUserId.toRequestBody())
        }
        val userPremium = tryOrNull { Gson().fromJson(json, UserPremium::class.java) }
        when {
            userPremium?.id != null -> {
                Timber.tag("Main11111").e("##### SYNC USER #####")
                Timber.tag("Main11111").e("Id: ${userPremium.id}")
                Timber.tag("Main11111").e("AppUserId: ${userPremium.appUserId}")
                Timber.tag("Main11111").e("isUpgraded: ${userPremium.isUpgraded}")
                Timber.tag("Main11111").e("timePurchased: ${userPremium.timePurchased}")
                Timber.tag("Main11111").e("timeExpired: ${userPremium.timeExpired}")
                Timber.tag("Main11111").e("numberCreatedArtworkInDay: ${userPremium.numberCreatedArtworkInDay}")
                Timber.tag("Main11111").e("totalNumberCreatedArtwork: ${userPremium.totalNumberCreatedArtwork}")
                Timber.tag("Main11111").e("latestTimeCreatedArtwork: ${userPremium.latestTimeCreatedArtwork}")
                Timber.tag("Main11111").e("country: ${userPremium.country}")

                prefs.isSyncUserPurchased.set(true)
                prefs.isSyncUserPurchasedFailed.set(false)
            }
            else -> {
                prefs.isSyncUserPurchased.set(false)
                prefs.isSyncUserPurchasedFailed.set(true)

                analyticManager.logEvent(AnalyticManager.TYPE.SYNC_USER_PREMIUM_FAILED)
            }
        }
    }

    override suspend fun insertUserPremium(
        appUserId: String,
        timePurchased: String,
        timeExpired: String,
        success: () -> Unit
    ) {
        val json = withContext(Dispatchers.IO) {
            serverApi.insertUserPremium(appUserId = appUserId.toRequestBody(), timePurchased = timePurchased.toRequestBody(), timeExpired = timeExpired.toRequestBody())
        }
        val message = tryOrNull { Gson().fromJson(json, Message::class.java) }
        when {
            message?.message != null && message.message == "Insert success" -> {
                prefs.isSyncUserPurchased.set(true)
                prefs.isSyncUserPurchasedFailed.set(false)

                success()
            }
            else -> {
                prefs.isSyncUserPurchased.set(false)
                prefs.isSyncUserPurchasedFailed.set(true)

                success()
            }
        }
        Timber.tag("Main11111").e("##### INSERT USER PREMIUM #####")
        Timber.tag("Main11111").e("Message: $message")
    }

    override suspend fun updateCreatedArtworkInDay() {

    }


}