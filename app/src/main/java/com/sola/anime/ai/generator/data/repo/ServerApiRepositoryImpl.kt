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
        val gson = Gson()
        val userPremium = tryOrNull { gson.fromJson(json, UserPremium::class.java) }
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

                prefs.userPremium.set(gson.toJson(userPremium))
                prefs.isSyncUserPurchased.set(true)
                prefs.isSyncUserPurchasedFailed.set(false)
            }
            else -> {
                prefs.userPremium.delete()
                prefs.isSyncUserPurchased.delete()
                prefs.isSyncUserPurchasedFailed.delete()

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
                val userPremium = UserPremium().apply {
                    this.id = "-1"
                    this.appUserId = appUserId
                    this.timePurchased = timePurchased
                    this.timeExpired = timeExpired
                    this.numberCreatedArtworkInDay = "0"
                    this.totalNumberCreatedArtwork = "0"
                    this.latestTimeCreatedArtwork = "0"
                }
                prefs.userPremium.set(Gson().toJson(userPremium))
                prefs.isSyncUserPurchased.set(true)
                prefs.isSyncUserPurchasedFailed.set(false)

                success()
            }
            else -> {
                prefs.userPremium.delete()
                prefs.isSyncUserPurchased.delete()
                prefs.isSyncUserPurchasedFailed.delete()

                success()
            }
        }
        Timber.tag("Main11111").e("##### INSERT USER PREMIUM #####")
        Timber.tag("Main11111").e("Message: $message")
    }

    override suspend fun updateCreatedArtworkInDay() {

    }


}